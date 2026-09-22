# Database Design

MySQL 8 · `utf8mb4` · InnoDB. Schema source of truth: `database/schema.sql`.

## ER summary

```
users (1) ──< problems        (1) universities ──< departments ──< faculty
roles (1) ──< users           (1) universities ──< student_teams ──< team_members ──< users
                              (1) industries ──< industry_capabilities
problem_categories (1) ──< problems
problems ──< problem_capabilities
problems ──< matches (polymorphic: entity_type + entity_id)
problems (1) ──< projects ──< project_members / tasks / milestones / comments / documents
projects ──< project_status_history ──< failure_records ──< impact_records ──< impact_evidence
users ──< notifications
```

## Tables

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `roles` | Role master list | name (UNIQUE) |
| `users` | All accounts | email (UNIQUE), password (BCrypt), `role_id`, status |
| `problem_categories` | Category master | name, description |
| `problems` | Societal challenges | submitter_id FK, title, description, category_id, location, estimated_beneficiaries, urgency, required_outcome, **status**, ai_category, ai_confidence, ai_explanation, ai_keywords (CSV), similar_problem_id (FK self), document_url |
| `problem_capabilities` | Capability decomposition | problem_id FK, capability, required_skills (CSV), priority |
| `universities` | University profiles | name, location, website, description |
| `departments` | Departments of a university | university_id FK, name |
| `faculty` | Mentor profiles | university_id FK, department_id FK, name, title, expertise (CSV), research_area, previous_projects, email |
| `student_teams` | Student teams | university_id FK, lead_id FK→users, name, skills (CSV), technologies (CSV), interests (CSV), availability, status |
| `team_members` | Team ↔ users link | team_id FK, user_id FK, role_in_team |
| `industries` | Industry partners | name, sector, expertise (CSV), technologies (CSV), hardware_capabilities, funding_support, mentorship, testing_capability, deployment_capability |
| `industry_capabilities` | Industry capability tags | industry_id FK, capability |
| `projects` | Collaboration workspace | problem_id FK, university_id, team_id, lead_mentor_id (FK faculty), industry_id, **status**, progress_percent, failure_reason |
| `project_members` | Members of a project | project_id FK, user_id FK, role_in_project |
| `tasks` | Tasks | project_id FK, assigned_to FK→users, title, status, priority, due_date |
| `milestones` | Milestones | project_id FK, title, status, target_date, completed_at |
| `comments` | Discussion incl. mentor/industry | project_id FK, author_id FK, comment_type (GENERAL/MENTOR/INDUSTRY), content |
| `documents` | Workspace/files | project_id FK, uploader_id, name, url |
| `matches` | Matching results | problem_id FK, entity_type (UNIVERSITY/TEAM/MENTOR/INDUSTRY), entity_id, score, explanation, status (PROPOSED/ACCEPTED/REJECTED) |
| `project_status_history` | Lifecycle audit | project_id FK, status, note, changed_by FK, changed_at |
| `failure_records` | Failure recovery | project_id FK, stage, reason, missing_capability, resolved, reported_by FK |
| `impact_records` | Social impact claims | project_id FK, beneficiaries, locations_covered, cost_savings, problem_reduction, duration_months, implementation_percent, domain_metrics, **verification_status** (PENDING/PARTIALLY_VERIFIED/VERIFIED) |
| `impact_evidence` | Evidence uploads | impact_record_id FK, evidence_type, description, file_url, status, reviewed_by FK |
| `notifications` | In-app alerts | user_id FK, message, type, link, is_read |

## Lifecycle status values (projects)

```
SUBMITTED → VERIFIED → MATCHED → TEAM_FORMED → PROPOSAL → PROTOTYPE
→ REVIEW → PILOT → IMPLEMENTATION → IMPACT
```
Plus: `NEEDS_SUPPORT` (set on failure), `COMPLETED` (final), `ARCHIVED`.

Problem status mirrors project for early stages: `SUBMITTED → VERIFIED → MATCHED → MERGED`.

## Notes

- `matches.entity_type` + `entity_id` is a simple polymorphic association (no separate tables per entity) to avoid overengineering.
- Skill/technology fields are CSV strings for the MVP; a join table can replace them later via `problem_capabilities`-style normalization.
- All timestamps: `created_at` / `updated_at` with `DEFAULT CURRENT_TIMESTAMP`.
- Seed data is inserted by the backend `DataSeeder` on an empty database; `database/seed.sql` is a standalone reference dump equivalent.