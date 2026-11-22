# Database

## Source of truth

The authoritative schema is the Flyway migration set in `backend/src/main/resources/db/migration/`:

- `V1__baseline.sql` — creates the 24-table baseline schema.
- `V2__harden_schema.sql` — adds idempotent hardening for columns, constraints and indexes.

`backend/src/main/resources/application.yml` enables Flyway with `locations: classpath:db/migration`, `baseline-on-migrate: true`, `baseline-version: 1` and `validate-on-migrate: true`. JPA uses `ddl-auto: none`, so Hibernate does not create or alter tables. `database/schema.sql` remains in the repository as a legacy reference but is not mounted by `docker-compose.yml` and is not the deployment source of truth.

## 24-table schema

| Domain | Tables |
|--------|--------|
| Identity and reference | `roles`, `problem_categories`, `universities`, `users` |
| Problem intelligence | `problems`, `problem_capabilities` |
| Stakeholder catalog | `departments`, `faculty`, `student_teams`, `team_members`, `industries`, `industry_capabilities` |
| Projects and collaboration | `projects`, `project_members`, `tasks`, `milestones`, `comments`, `documents` |
| Matching and history | `matches`, `project_status_history`, `failure_records` |
| Impact and messaging | `impact_records`, `impact_evidence`, `notifications` |

Problem analysis columns include category, urgency, confidence, skills, keywords, explanation, structured analysis fields, source and similarity reference. Problem capability rows store required skills and priority. Projects carry progress, failure reason and timestamps; status changes are recorded in `project_status_history`. Impact records start `PENDING`, evidence starts `PENDING`, and admin review moves evidence to `VERIFIED` or `REJECTED` with the record status derived from all evidence.

`created_at`, `updated_at` and `ON UPDATE CURRENT_TIMESTAMP` are present on `problems`, `projects` and `impact_records`; other tables use the columns appropriate to their domain rather than claiming a universal timestamp pattern.

## Flyway V1 — baseline

`V1__baseline.sql`:

- Creates all 24 tables with `BIGINT AUTO_INCREMENT` primary keys.
- Defines `version BIGINT NOT NULL DEFAULT 0` on every table.
- Adds foreign keys for problem submission/category/similarity, capabilities, departments, faculty, teams, team members, industries, projects, project members, tasks, milestones, comments, documents, matches, status history, failures, impact records, impact evidence and notifications.
- Adds named unique keys such as `uk_universities_name`, `uk_faculty_email`, `uk_project_members_project_user`, `uk_matches_problem_entity` and `uk_impact_records_project`.
- Adds single-column and composite indexes including `idx_problems_status_category`, `idx_projects_problem_status`, `idx_matches_problem_entity_lookup` and `idx_notifications_user_read`.

## Flyway V2 — hardening

`V2__harden_schema.sql` uses temporary stored procedures to apply changes idempotently, then drops those procedures. It:

- Adds `version` columns if an existing pre-baseline database is missing them.
- Adds structured AI columns to `problems` and a `source` column to `problems` and `matches`.
- Clears invalid user role/university references before adding `fk_users_role` and `fk_users_univ`.
- Removes duplicate rows from relationship tables before adding unique keys.
- Renames duplicate university, industry and faculty names/emails before unique constraints are applied.
- Adds unique constraints for `universities.name`, `industries.name`, `faculty.email`, `departments(university_id, name)`, `problem_capabilities(problem_id, capability)`, `team_members(team_id, user_id)`, `industry_capabilities(industry_id, capability)`, `project_members(project_id, user_id)`, `matches(problem_id, entity_type, entity_id)` and `impact_records(project_id)`.
- Re-applies the composite indexes used by common query patterns, including problems by status/category and submitter/date, projects by problem/status and university/status, match lookup by problem/type/entity, impact records by project/status, project members by user/project and notifications by user/read/date.

## Foreign keys, unique constraints and composite indexes

Key integrity rules in the migration set:

- `users.role_id` and `users.linked_university_id` reference `roles.id` and `universities.id` with `ON DELETE SET NULL`.
- Child tables cascade from their parent where appropriate, for example capabilities from problems, tasks/milestones/comments/documents from projects, and evidence from impact records.
- Unique constraints prevent duplicate reference names/emails and duplicate membership, capability, match and impact-record relationships.
- Composite indexes support the filters used by the application: `problems(status, category_id)`, `problems(submitter_id, created_at)`, `projects(problem_id, status)`, `projects(university_id, status)`, `matches(problem_id, entity_type, entity_id)`, `impact_records(project_id, verification_status)`, `project_members(user_id, project_id)` and `notifications(user_id, is_read, created_at)`.
- All 24 tables have a `version` column, and JPA entities map it with `@Version` for optimistic locking.

## Deterministic seeding

Demo data is created by `backend/src/main/java/com/sih/innovationplatform/config/DataSeeder.java`, not by a SQL seed file.

- The seeder is a `CommandLineRunner` enabled only by `@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")`.
- Compose sets `APP_SEED_ENABLED=true`; the application default is `false`.
- It returns without writing if core tables such as roles, users, problems, universities, industries, projects or matches already contain rows.
- It inserts fixed roles, users, categories, universities, departments, faculty, teams, industries, problems, capabilities, projects, members, tasks, milestones, comments, documents, status history, failures, matches, impact records, evidence and notifications.
- Dates and numeric scores are fixed constants or deterministic calculations; there is no `Math.random()` call in the current seeder. BCrypt password hashes vary per generation, but the seeded login credentials are consistently `password123`.

The seeder creates the local demo scenario used by the UI: a rural waste-collection problem, stakeholder matching, a project workspace, failed pilots with recovery recommendations and implemented projects with verified impact.

## Operational notes

- MySQL data persists in the named `mysql-data` volume.
- Flyway owns schema evolution; avoid manual DDL against the running database.
- The backend cannot start successfully until its configured `DB_URL` points to an existing database/schema owner that Flyway can migrate.
- No database backup, restore or production migration procedure is included in this prototype.
