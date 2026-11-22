# SETU Implementation Audit

**Project:** SETU – Societal Innovation Collaboration Platform
**Problem statement:** SIH26043
**Audit date:** 2026-11-22
**Audit type:** Phase 0 static source audit (read-only)
**Workspace:** `C:\Users\kumar\OneDrive\Desktop\setu`

---

## 1. Scope and method

This audit inspected the existing SETU workspace directly. No application source code, configuration, tests, or documentation files were modified during the audit. This document is the only file created.

### What was inspected

- Complete frontend tree: `frontend/`
- Complete Spring Boot backend tree: `backend/`
- FastAPI AI service: `ai-service/`
- MySQL schema: `database/schema.sql`
- Container configuration: `docker-compose.yml`, all Dockerfiles, `frontend/nginx.conf`
- Environment template and ignore rules: `.env.example`, `.gitignore`
- Existing docs: `README.md`, `CONTRIBUTING.md`, `docs/*.md`
- Root-level artifacts: `query`, `org/`, JVM crash logs, `.github/`

### Audit limitations

- The local workspace has **no `.git` directory**, so commit history, branches, remotes, and actual GitHub state could not be inspected.
- No application, service, database, or container was started.
- No build, lint, test, or Docker command was executed. Therefore this report contains **no runtime test results**.
- Findings are based on source code, configuration, and static cross-service contract analysis. Line numbers refer to the current workspace snapshot.

### Status legend

| Status | Meaning |
|---|---|
| **Implemented** | Source code shows the main behavior is present and connected to persistence/API where expected. |
| **Partial** | Some behavior exists, but validation, authorization, storage, UI, or workflow coverage is incomplete. |
| **UI-only** | Visual element exists without a real backend workflow. |
| **Missing** | No source implementation found. |
| **Broken** | Source shows a defect that prevents a workflow or produces invalid behavior. |
| **Insecure** | Source shows a security weakness that must be fixed before production/demo exposure. |

---

## 2. Executive summary

SETU is already a substantial working prototype, not an empty repository. It has:

- A React/Vite frontend with 12 pages and real REST API calls.
- A Spring Boot backend with 8 REST controllers, 7 services, 22 JPA entities, JWT authentication, BCrypt password encoding, rule-based AI orchestration, matching, projects, impact tracking, notifications, and analytics.
- A FastAPI service with 7 endpoints implementing deterministic keyword/TF-vector analysis, decomposition, matching, recommendations, and recovery suggestions.
- A 24-table MySQL schema with a Java-based demo data seeder.
- Docker Compose wiring for frontend `5173`, backend `8080`, AI `8000`, and MySQL `3307`.

However, the current codebase is **not production-ready** and several core workflows are broken or insecure.

### Highest-priority findings

| ID | Severity | Finding | Evidence |
|---|---|---|---|
| AUD-01 | P0 | Containerized frontend builds use `VITE_API_BASE_URL=/api`, but every client helper already starts with `/api`, producing `/api/api/...` requests. | `docker-compose.yml:67`, `frontend/Dockerfile:5-6`, `frontend/src/api/client.js:1-2,18,44` |
| AUD-02 | P0 | AI analysis data type mismatch crashes problem detail and matching pages when analysis exists. Backend stores comma-separated strings; frontend calls `.map()`/`.join()` on them. | `backend/.../ProblemService.java:63-64`, `backend/.../ProblemDtos.java:40-41`, `frontend/src/pages/ChallengeDetailPage.jsx:138,145`, `frontend/src/pages/MatchingPage.jsx:129` |
| AUD-03 | P0 | Impact evidence submission always sends `impactRecordId: 0`, which cannot exist. | `frontend/src/pages/ImpactPage.jsx:66-71`, `backend/.../ImpactService.java:49-50` |
| AUD-04 | P0 | Evidence review sends `APPROVED`, but backend accepts only `VERIFIED` or `REJECTED`. | `frontend/src/pages/ImpactPage.jsx:191-197`, `backend/.../ImpactService.java:73-75` |
| AUD-05 | P0 | Any anonymous visitor can self-register as `UNIVERSITY_ADMIN`, which is allowed to verify and merge problems. | `backend/.../AuthService.java:17-18`, `backend/.../ProblemService.java:149-151,162-164` |
| AUD-06 | P0 | Project status, failure, and failure-resolution endpoints have no membership/role authorization. | `backend/.../ProjectService.java:207-292` |
| AUD-07 | P0 | Task and milestone updates check membership against the URL project but fetch the child ID globally, enabling cross-project modification. | `backend/.../ProjectService.java:325-332,350-362` |
| AUD-08 | P1 | Configured AI timeout is never applied; every AI failure silently falls back to a different Java rule engine without telling the user or backend caller. | `application.yml:34`, `backend/.../RemoteAiGateway.java:34-37,66-69,120-123,180-183` |
| AUD-09 | P1 | Backend and frontend have no automated tests. AI has 8 happy-path test functions in source; they were not executed in this audit. | `backend/src/test` absent, `frontend/package.json:6-10`, `ai-service/tests/test_ai.py:13-123` |
| AUD-10 | P1 | Required architecture, API, database, audit, testing, and final report documents are missing or only exist under different/lowercase names. | `docs/` contains only `api.md`, `architecture.md`, `database.md`, `demo-flow.md` |

### Overall verdict

| Layer | Verdict |
|---|---|
| Frontend | Broad prototype with real API integration, but four P0 runtime defects, poor mobile navigation, weak accessibility, and no tests. |
| Backend | Feature-rich monolith with good structural separation, but authorization is missing on several mutating endpoints and validation/testing are largely absent. |
| AI service | Working deterministic rule engine with FastAPI endpoints, not an LLM/ML provider; contract and timeout handling need correction. |
| Database | Broad schema covering the domain, but missing foreign keys, unique constraints, indexes, migrations, and ORM coverage for two tables. |
| Docker/deployment | Compose structure is plausible and ports are correct, but containerized frontend API calls are broken by a base-URL mismatch. |
| Documentation | Substantial but stale: wrong ports, references to files/endpoints that do not exist, no screenshots, no GrowthX section, no final audit/testing report. |

---

## 3. Verified architecture snapshot

### 3.1 Repository structure

```text
setu/
├── .env.example
├── .gitignore
├── .github/                  # only Copilot modernize artifacts; no workflows
├── .vscode/
├── ai-service/
│   ├── app/                  # main.py, engine.py, knowledge.py
│   ├── tests/test_ai.py
│   ├── requirements.txt
│   └── Dockerfile
├── backend/
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   ├── Dockerfile
│   └── src/main/java/com/sih/innovationplatform/
│       ├── ai/ common/ config/ dashboard/ impact/ industry/
│       ├── match/ notification/ problem/ project/ security/
│       ├── team/ university/ user/
│       ├── HealthController.java
│       └── InnovationPlatformApplication.java
│   └── src/main/resources/application.yml
├── database/schema.sql
├── docs/
│   ├── api.md
│   ├── architecture.md
│   ├── database.md
│   └── demo-flow.md
├── frontend/
│   ├── src/                  # 22 first-party JS/JSX/CSS files
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml
├── CONTRIBUTING.md
├── README.md
├── query                     # stray 7-byte file containing "MySQL"
├── org/                      # stray compiled Spring class
├── hs_err_pid18912.log       # JVM crash artifact
└── replay_pid18912.log       # JVM replay artifact
```

Evidence: workspace directory listing; `docker-compose.yml:1-76`; `docs/` directory listing.

### 3.2 Technology stack actually present

| Layer | Actual stack | Evidence |
|---|---|---|
| Frontend | React 18, Vite 5, React Router 6, Tailwind CSS 3, Recharts 2 | `frontend/package.json:11-23` |
| Backend | Spring Boot 3.5.4, Java 25, Spring Security, Spring Data JPA, Lombok, JJWT, MySQL driver | `backend/pom.xml:8-23,74-98` |
| AI service | FastAPI 0.115.12, Uvicorn 0.34.3, Pydantic 2.x, Python rules engine | `ai-service/requirements.txt:1-5`, `ai-service/app/main.py:24` |
| Database | MySQL 8.4, schema mounted from `database/schema.sql` | `docker-compose.yml:3,15` |
| Reverse proxy | Nginx 1.27 in frontend image | `frontend/Dockerfile:14-21`, `frontend/nginx.conf:1-23` |
| Tests declared | JUnit 5/Mockito/Spring Security Test/H2 dependencies in backend; pytest/httpx in AI | `backend/pom.xml:74-88`, `ai-service/requirements.txt:4-5` |
| Tests actually present | AI only: 8 test functions; none in backend or frontend | `ai-service/tests/test_ai.py:13-123` |

### 3.3 Intended vs actual data flow

```text
Browser (React, 5173)
   ├─ dev: Vite proxy /api -> localhost:8080
   └─ docker: nginx /api -> backend:8080
        ↓
Spring Boot (8080)
   ├─ MySQL (mysql:3306, host 3307)
   ├─ FastAPI (ai-service:8000, host 8000)
   └─ seeded demo data via DataSeeder.java
```

This architecture exists in code. The main break is URL composition in the Docker frontend build (`AUD-01`).

---

## 4. Feature-by-feature implementation status

### 4.1 Authentication and roles

| Capability | Status | Evidence and notes |
|---|---|---|
| User registration | Partial / insecure | `AuthService.register` validates name, password length, email presence, and duplicate email (`backend/.../AuthService.java:26-54`), but permits self-registration as `UNIVERSITY_ADMIN` (`:17-18`). |
| Login with JWT | Implemented | BCrypt encoder at `config/SecurityConfig.java:62-65`; login authenticates and returns token/user at `AuthService.java:57-77`. |
| JWT filter | Partial / insecure | `JwtAuthFilter.java:32-43` reads Bearer token and builds authorities from the token's `role` claim, not the current DB role. Invalid tokens fall through to generic authorization handling. |
| Password storage | Implemented | `passwordEncoder.encode(request.password())` at `AuthService.java:49`; BCrypt bean at `SecurityConfig.java:62-65`. |
| Role-based authorization | Partial / insecure | Only one `@PreAuthorize` exists (`AuthController.java:44`). Most authorization is scattered service checks; many mutating endpoints have none. |
| User profile | Partial | `/api/auth/me` and `/api/dashboard/me` exist; frontend profile is read-only. |
| Logout/revocation | Missing | No token blacklist, refresh token, revocation, or logout endpoint found. |
| Email verification / password reset | Missing | No source implementation found. |

### 4.2 Problem submission

| Capability | Status | Evidence and notes |
|---|---|---|
| Submit title, description, location, beneficiaries, urgency, outcome, document URL | Implemented | `ProblemCreateRequest` fields at `backend/.../ProblemDtos.java:5-13`; create logic at `ProblemService.java:28-53`. |
| Authenticated submitter association | Implemented | `problem.setSubmitter(user)` at `ProblemService.java:47`. |
| MySQL persistence | Implemented | `problemRepo.save(problem)` at `ProblemService.java:50`; schema table at `database/schema.sql:47-72`. |
| Retrieval/list/filter | Implemented | `ProblemService.list` at `:112-122`; endpoint at `ProblemController.java:25-31`. |
| Retrieval by ID | Implemented | `ProblemService.get` at `:124-128`. |
| Update | Implemented with access check | Owner/admin check at `ProblemService.java:130-135`; no frontend helper or UI found. |
| Pagination | Missing | `list()` loads all non-merged problems and filters in Java (`ProblemService.java:112-121`); no `Pageable` found. |
| Request validation | Partial | Only title/description checked (`ProblemService.java:32-37`); DTO has no Bean Validation annotations (`ProblemDtos.java:5-23`). |
| Supporting document upload | Missing | Only a `documentUrl` string is stored; `multipart.max-file-size` exists (`application.yml:19-21`) but no upload endpoint. |
| Frontend submission form | Implemented | `frontend/src/pages/ChallengesPage.jsx:43-60,66-114` calls `problemApi.create`. |

### 4.3 AI problem intelligence

| Capability | Status | Evidence and notes |
|---|---|---|
| FastAPI analysis endpoint | Implemented | `POST /analyze` at `ai-service/app/main.py:93-110`. |
| Structured JSON from FastAPI | Partial | Returns `category`, `urgency`, `requiredSkills`, `keywords`, `confidence`, `explanation`. It does **not** return structured root causes, challenges, solution directions, expected outcomes, or full target-beneficiary analysis. |
| Required fields from vision | Missing | Root causes, target beneficiaries, challenges, possible solution directions, and expected outcomes are not separate AI response fields anywhere in `main.py:93-110` or `ProblemDtos.java:25-46`. |
| Backend orchestration | Implemented | `ProblemService.runAnalysis` calls analyze, decompose, duplicates, and persists results (`ProblemService.java:55-110`). |
| Store successful analysis | Implemented | AI fields written at `ProblemService.java:60-66,108-109`; capabilities persisted at `:77-88`. |
| View prior analysis | Broken in UI | Backend stores `aiSkills`/`aiKeywords` as strings (`ProblemService.java:63-64`, `ProblemDtos.java:40-41`), but frontend treats them as arrays (`ChallengeDetailPage.jsx:138,145`). |
| AI provider failures | Partial / insecure | Backend catches all exceptions and silently substitutes the Java fallback (`RemoteAiGateway.java:66-69` and equivalents). No `fallbackUsed` indicator is persisted or shown. |
| Timeout handling | Broken | `application.yml:34` sets `timeout-ms: 8000`, but `RemoteAiGateway.java:34-37` builds `RestClient` without using it. |
| Response validation | Missing/weak | `RemoteAiGateway.text()` and `stringList()` use lenient `JsonNode.path()` defaults (`RemoteAiGateway.java:248-261`); FastAPI routes define no `response_model`. |
| Secret exposure | Secure | No AI API key or provider secret found in `ai-service/`; no paid provider SDK present. |
| Authorization on re-analysis | Insecure | `POST /api/problems/{id}/analyze` has no ownership/role check (`ProblemController.java:45-49`, `ProblemService.java:55-110`). |
| JSON validation tests | Partial | Happy-path tests exist in `ai-service/tests/test_ai.py`; malformed/invalid response handling is untested. |

### 4.4 Capability decomposition

| Capability | Status | Evidence and notes |
|---|---|---|
| AI decomposition endpoint | Implemented | `POST /decompose` at `ai-service/app/main.py:126-140`. |
| Persistence | Implemented | Creates `ProblemCapability` rows at `ProblemService.java:77-88`; schema at `database/schema.sql:77-85`. |
| API retrieval | Implemented | `GET /api/problems/{id}/capabilities` at `ProblemController.java:65-68`; service at `ProblemService.java:187-189`. |
| Frontend display | Implemented | `ChallengeDetailPage.jsx:150-172` renders embedded `problem.capabilities`. |
| Required technical skills | Implemented | `requiredSkills` in capability response (`main.py:134-139`) and `String.join` persistence (`ProblemService.java:84`). |
| Team roles | Missing | No distinct role field in `ProblemCapability`, `CapabilityBrief`, or AI response. |
| Technologies | Missing | No distinct technology field in capability DTO/entity/response. |
| Infrastructure requirements | Missing | No source field found. |
| Resources | Missing | No source field found. |
| Estimated complexity | Missing | No source field found; `priority` exists but is not complexity. |
| Manual review/edit | Missing | No endpoint to edit persisted capabilities. |

### 4.5 Intelligent team matching

| Capability | Status | Evidence and notes |
|---|---|---|
| Match endpoint | Implemented | `POST /api/problems/{id}/match` at `MatchingController.java:17-20`. |
| FastAPI match endpoint | Implemented | `POST /match/{match_type}` at `ai-service/app/main.py:143-157`. |
| Scoring | Implemented | `engine.score_candidate` used by FastAPI; contributing factors and explanation returned (`main.py:146-157`). |
| Persistence | Implemented | `MatchingService.upsert` stores match rows; schema at `database/schema.sql:287-298`. |
| Explainable output | Implemented in UI | `frontend/src/pages/MatchingPage.jsx:139-169` shows score, explanation, and contributing factors. |
| Recommendation endpoint | Partial / broken contract | FastAPI emits lowercase `type` values (`main.py:189-194`); backend only remaps `STUDENT_TEAM` (`MatchingService.java:84-88`), so persisted entity types can become lowercase. |
| Skill overlap | Implemented | Candidate skills and requirement skills are sent by `RemoteAiGateway.requirementMap/candidateMap` (`:228-245`) and scored in `engine.py`. |
| Availability matching | Missing | `StudentTeam.availability` exists in schema, but it is not sent in `CandidateProfile` from `RemoteAiGateway.candidateMap`. |
| Relevant experience | Partial | Candidate `description`/`previousProjects` exists in the domain, but `candidateMap` sends only description, skills, tags, capabilities. |
| Accept/reject match lifecycle | Missing | `Match.status` column exists, but no endpoint/service transition found. |
| Authorization | Insecure | Any authenticated user can run matching and mutate problem status to `MATCHED` (`MatchingService.java:58-61,84-92`). |
| Sample/registered distinction | Missing | Matching results do not label whether a candidate is a registered account or seeded demo data. |

### 4.6 Project collaboration

| Capability | Status | Evidence and notes |
|---|---|---|
| Project creation | Implemented in backend | `POST /api/projects` at `ProjectController.java:20-24`; service checks problem status at `ProjectService.java:76-78`. |
| Project creation from UI | Missing | `projectApi.create` exists (`client.js:76`) but no page calls it. |
| Project listing/detail | Implemented | `ProjectService.list/get`/`toResponse` at `:123-205`; frontend pages exist. |
| Team membership | Partial | `project_members` table and `ProjectMember` entity exist; membership check helper exists (`ProjectService.java:432-443`). No join/leave UI. |
| Tasks | Implemented | Add/update task endpoints at `ProjectController.java:43-54`; UI at `ProjectDetailPage.jsx:171-218`. |
| Task assignment | Partial | Backend DTO accepts `assignedToId` (`ProjectService.java:319`), backend displays `assignedToName`, but UI has no assignment control. |
| Milestones | Implemented with IDOR risk | Endpoints exist; child ID not verified against project (`ProjectService.java:350-362`). |
| Comments | Implemented | `ProjectService.addComment` and UI exist. |
| Documents | Partial | Only name + URL string; no upload. |
| Progress updates | Implemented | `PUT /api/projects/{id}/progress` with clamped value (`ProjectService.java:397` region); UI slider exists. |
| Status tracking/history | Implemented | `project_status_history` table and timeline response; UI timeline at `ProjectDetailPage.jsx:395-412`. |
| Role-based permissions | Insecure/incomplete | Several lifecycle mutations have no authorization (`ProjectService.java:207-292`); frontend `canEdit` is based only on role/ student flag and does not match backend membership rules. |
| Chat/presence/live updates | Missing | Not implemented; README lists as future scope. |

### 4.7 Impact tracking and verification

| Capability | Status | Evidence and notes |
|---|---|---|
| Impact record submission | Implemented but weakly authorized | `ImpactService.submit` at `:26-45`; any authenticated user can submit for any project. |
| Beneficiaries, locations, cost savings, duration, percent, domain metrics | Implemented in backend | `ImpactService.java:32-43`; schema `impact_records` at `database/schema.sql:333-349`. |
| Evidence submission | Broken | Frontend sends `impactRecordId: 0` (`ImpactPage.jsx:67`); backend throws 404 (`ImpactService.java:49-50`). |
| Evidence review | Broken | Frontend sends `APPROVED` (`ImpactPage.jsx:192`); backend accepts only `VERIFIED`/`REJECTED` (`ImpactService.java:73-75`). |
| No automatic verification | Implemented | New records start `PENDING` (`ImpactService.java:41`); evidence starts `PENDING` (`:60`). |
| Authorized verification | Partial | Review requires `PLATFORM_ADMIN`/`GOVERNMENT_ADMIN` (`ImpactService.java:68-70`), but submit/evidence endpoints lack project membership checks. |
| Verification state logic | Broken edge case | All rejected evidence sets record to `PARTIALLY_VERIFIED` because `anyRejected` is true (`ImpactService.java:86-90`). |
| Supporting evidence with file upload | Missing | Only `fileUrl` string; no upload endpoint. |
| Frontend impact dashboard | Partial | Real stats/list at `ImpactPage.jsx:20-59`; hardcoded `implementationPercent: '100'` and empty `domainMetrics` at `:15,49-52`. |

### 4.8 Notifications, analytics, and catalog

| Capability | Status | Evidence and notes |
|---|---|---|
| Notification read/list/unread count | Implemented | `NotificationController.java:19-35`; ownership enforced in `NotificationService.java:37-39`. |
| Notification event generation | Missing | `NotificationService.push` exists at `:19-28` but has no callers; notifications only come from seed data. |
| Frontend notification mapping | Broken | Frontend uses `title`/`read` (`ProfilePage.jsx:23,57,61,66`), but entity exposes `message`/`isRead` (`Notification.java:34-47`). |
| Analytics dashboard | Implemented | `DashboardService.analytics` and `AnalyticsPage.jsx` use real API data; no hardcoded dashboard statistics found. |
| Analytics authorization | Insecure | `/api/dashboard/analytics` is open to any authenticated user (`DashboardController.java:19-22`). |
| Catalog endpoints | Implemented | `CatalogController.java:29-88`. |
| Catalog UI | Missing | `catalogApi` helpers exist (`client.js:107-112`) but are unused. |
| Pagination/sorting | Missing | No `Pageable` usage found. |

---

## 5. Detailed findings by category

## A. Fully implemented features

The following are genuinely present in source and connected to APIs/persistence:

1. **Spring Boot project structure and layering**
   Controllers, services, repositories, entities, DTO records, security configuration, and a global exception handler exist under `backend/src/main/java/com/sih/innovationplatform/`.

2. **JWT login and BCrypt password hashing**
   `AuthService.java:49` and `SecurityConfig.java:62-65`.

3. **Problem CRUD core**
   Create, list, get, update, verify, merge, capabilities endpoints at `ProblemController.java:19-68`; service logic at `ProblemService.java:28-189`.

4. **FastAPI AI endpoints**
   Seven routes at `ai-service/app/main.py:88,93,113,126,143,185,211`.

5. **AI analysis orchestration and persistence**
   `ProblemService.java:55-110` calls FastAPI through `RemoteAiGateway`, writes AI fields, replaces capabilities, and checks duplicates.

6. **Matching rule engine and persistence**
   `MatchingService.java` plus `ai-service/app/engine.py`.

7. **Project workspace core**
   Tasks, milestones, comments, documents, progress, status history, failure records, and recovery recommendations exist in `ProjectService.java` and `ProjectController.java`.

8. **Impact record core**
   Submit, list, stats, evidence, review endpoints exist in `ImpactController.java` and `ImpactService.java`.

9. **Analytics charts with real API data**
   `frontend/src/pages/AnalyticsPage.jsx:27-121` derives all displayed statistics from `/api/dashboard/analytics`.

10. **Docker Compose service topology**
   Correct ports, service names, named MySQL volume, MySQL/AI healthchecks, restart policies, and backend readiness gating in `docker-compose.yml:1-76`.

11. **AI happy-path tests**
   Seven test functions cover health, analyze, duplicates, decompose, match, recommend, and recovery (`ai-service/tests/test_ai.py:13-123`). They were not executed during this audit.

---

## B. Partially implemented features

1. **Problem submission validation**
   Manual title/description checks exist, but no Bean Validation and no pagination/upload.

2. **AI intelligence schema**
   Current schema is narrower than the project vision. Missing: root causes, target beneficiaries, challenges, solution directions, expected outcomes.

3. **Capability decomposition**
   Skills and priority are stored; roles, technologies, infrastructure, resources, and complexity are absent.

4. **Matching explainability**
   Scores and explanations exist; availability and actual registered-vs-seed labeling do not.

5. **Collaboration workspace**
   CRUD exists; assignment, file upload, notifications, and real-time collaboration do not.

6. **Impact verification**
   Admin review exists, but frontend status values are wrong and the all-rejected state is incorrect.

7. **Role-specific dashboards**
   Scoped buttons exist in several pages, but `Layout.jsx` navigation is identical for all roles and there is no dedicated role dashboard.

8. **Loading/error/empty/success states**
   Empty states are decent in several pages; loading and success states are inconsistent; several catches are silent (`Layout.jsx:25`, `ImpactPage.jsx:35`, `ProjectDetailPage.jsx:55`).

9. **Documentation**
   Four docs exist and contain useful material, but they are stale and required filenames are missing.

---

## C. UI-only / mock / hardcoded production values

1. **Landing page missing**
   `App.jsx:39` maps `/` and all unknown routes to `LoginPage`. No landing component exists.

2. **Demo credentials embedded in production bundle source**
   `frontend/src/pages/LoginPage.jsx:71-76` displays `password123` and demo emails. Useful for a local demo, unsafe in a production build.

3. **Hardcoded impact defaults**
   `ImpactPage.jsx:15,49-52` submits `implementationPercent: 100` and `domainMetrics: ''` because no inputs exist for them.

4. **Empty "Best matches so far" card**
   `ChallengeDetailPage.jsx:174-181` renders a heading and link but no matches.

5. **Hardcoded status-change note**
   `ProjectDetailPage.jsx:76` creates `Moved to ${newStatus} by ${user?.name}` rather than accepting a real note.

6. **Decorative live timestamp**
   `ProjectsPage.jsx:66` renders `new Date()` with no label/purpose.

7. **Static profile "Where to go next" links**
   `ProfilePage.jsx:77-86` duplicates navigation with no dynamic behavior.

8. **No file/evidence upload**
   `/files` is proxied (`vite.config.js:13-16`, `frontend/nginx.conf:15-18`) but no upload endpoint or UI exists.

---

## D. Missing features

1. Public landing page and real 404 page.
2. Public/anonymous problem browsing.
3. Project creation UI from a match.
4. Problem edit UI and duplicate-merge UI.
5. University/student/faculty/industry catalogs in the UI.
6. Role-specific dashboards.
7. Password reset, email verification, account editing.
8. Task assignment and due-date editing UI.
9. File upload for documents and impact evidence.
10. Match accept/reject lifecycle.
11. Team join/leave and real membership management.
12. Notification generation from workflow events.
13. Backend tests, frontend tests, lint/typecheck scripts, CI workflow.
14. Required uppercase docs: `ARCHITECTURE.md`, `API_DOCUMENTATION.md`, `DATABASE.md`, `IMPLEMENTATION_AUDIT.md`, `TESTING.md`, `FINAL_PROJECT_REPORT.md`.
15. Real screenshots and the required **GrowthX Off Campus** section/code.
16. Database migration tooling (Flyway/Liquibase or versioned SQL).
17. Pagination, sorting, and indexed search.
18. Optimistic locking and DB-level uniqueness for several relationships.

---

## E. Broken or insecure implementations

### E1. P0 — Docker frontend API base URL double prefix

`docker-compose.yml:67` sets `VITE_API_BASE_URL: /api`; `frontend/Dockerfile:5-6` exposes it to Vite; `frontend/src/api/client.js:1-2,18` concatenates it with helper paths that already include `/api`, e.g. `:44` `/api/auth/login`.

Static result: `/api/api/auth/login`. Nginx forwards `/api/` unchanged to `backend:8080` (`frontend/nginx.conf:8-13`), where no such mapping exists. Local dev works because the default base is empty and Vite proxies `/api`.

### E2. P0 — AI string/array mismatch crashes React pages

Backend `ProblemService.java:63-64` writes comma-separated `String` values into `aiSkills` and `aiKeywords`; `ProblemDtos.java:40-41` exposes them as `String`. Frontend uses:

- `ChallengeDetailPage.jsx:138`: `problem.aiSkills.map(...)`
- `ChallengeDetailPage.jsx:145`: `problem.aiKeywords.join(', ')`
- `MatchingPage.jsx:129`: `problem.aiSkills.join(', ')`

Any non-empty value causes a `TypeError`. No error boundary exists in `main.jsx`, so the React tree can blank.

### E3. P0 — Evidence submission cannot succeed

`ImpactPage.jsx:66-71` hardcodes `impactRecordId: 0`. `ImpactService.java:49-50` looks up ID 0 and throws 404. The form has no record selector.

### E4. P0 — Evidence review status mismatch

`ImpactPage.jsx:192` sends `APPROVED`; `ImpactService.java:73-75` rejects it. The button condition at `ImpactPage.jsx:191` also checks `APPROVED`, so the UI state is wrong even after a correct `VERIFIED` response.

### E5. P0 — Privilege escalation via self-registration

`AuthService.java:17-18` allows anonymous registration as `UNIVERSITY_ADMIN`. `ProblemService.java:149-151` and `:162-164` grant that role verification and merge powers. This defeats the evidence-based verification workflow.

### E6. P0 — Missing authorization on project lifecycle

`ProjectService.changeStatus` (`:207-246`), `reportFailure` (`:248-268`), and `resolveFailure` (`:270-292`) load the actor and project but never call `requireMemberOrAdmin` or an equivalent role check. Any authenticated user can mutate any project.

### E7. P0 — Cross-project child IDOR

`ProjectService.updateTask` checks membership for `projectId` but loads `taskId` globally (`:325-332`). `updateMilestone` does the same (`:350-362`). A member of project A can modify a task/milestone belonging to project B by mixing IDs.

### E8. P0 — Insecure defaults and demo seeding

- `docker-compose.yml:9-10,48` defines default DB/JWT passwords.
- `.env.example:10-11,19` repeats them.
- `application.yml:7,29` and `AppProperties.java:21` include fallback secrets.
- `DataSeeder.java:137-152` creates multiple accounts with public password `password123` on an empty database, with no production profile guard found.

### E9. P1 — JWT design weaknesses

`JwtService` tokens contain only subject/role/uid/iat/exp. `JwtAuthFilter.java:38-41` trusts the token role claim instead of the current DB role. No issuer, audience, jti, revocation, or user-status check found. Invalid tokens produce generic authorization outcomes rather than a clear 401 flow.

### E10. P1 — AI timeout unused and silent fallback

`application.yml:34` sets a timeout, but `RemoteAiGateway.java:34-37` ignores it. All six remote methods catch `Exception` and silently call `LocalAiGateway`. `isRemote()` (`RemoteAiGateway.java:41-43`) is never used by workflows. Java fallback can emit `CRITICAL` urgency (`LocalAiGateway.java:142-149`), while FastAPI emits only `HIGH|MEDIUM|LOW` (`ai-service/app/engine.py` usage in `main.py:97`). Stored results do not disclose which engine produced them.

### E11. P1 — Weak request validation and error mapping

`@Valid` appears only on auth controller methods, and auth DTO records have no constraints (`AuthService.java:84-88`). Other DTOs are plain records (`ProblemDtos.java:5-23`, `ProjectDtos.java`, `ImpactDtos.java`). Null bodies can cause NPEs. `GlobalExceptionHandler` returns raw exception messages for 500s, which can leak internals.

### E12. P1 — Notification field mismatch

Frontend uses `title` and `read`; backend entity has `message` and `isRead` with no `title`. Users see blank notification titles and incorrect unread state.

### E13. P1 — Mobile navigation blocked

`frontend/src/components/Layout.jsx:45` uses `hidden ... md:flex` for the only main navigation. No hamburger/drawer/bottom-nav alternative exists, so main sections are unreachable below `md` breakpoint.

### E14. P1 — Frontend role checks do not match backend authorization

`ProjectDetailPage.jsx:31` computes `canEdit` from role names, while backend uses project membership (`ProjectService.java:432-443`). Students who are not members see controls that return 403; authorized problem submitters may see none.

### E15. P1 — Self-assigned elevated roles

`RegisterPage.jsx:6-12,71-77` lets anonymous visitors choose `FACULTY`, `UNIVERSITY_ADMIN`, and `INDUSTRY`. Backend permits these at `AuthService.java:17-18`. `universityId` is accepted without FK validation (`AuthService.java:51`).

### E16. P2 — CORS, health, and deployment inconsistencies

- `SecurityConfig.java:53` hardcodes localhost origins including stale `:8081`, with credentials enabled.
- `HealthController` maps both `/api/health` and `/health`, but `SecurityConfig.java:43` permits only `/api/health`.
- Backend and frontend containers have no healthchecks; frontend waits only for backend startup, not readiness (`docker-compose.yml:56-60,72-73`).
- `DB_URL` is documented in `.env.example:14` but Compose passes `DB_HOST`/`DB_PORT`/`DB_NAME`, so changing `DB_URL` has no effect in Docker.
- `SPRING_PROFILES_ACTIVE=docker` (`docker-compose.yml:53`) has no `application-docker.yml`.
- `schema.sql:7-9` hardcodes `sih_platform`, so changing `MYSQL_DATABASE` can split configuration.

### E17. P2 — AI service exposure and contract drift

- AI port `8000:8000` is published with no authentication (`docker-compose.yml:28-29`; no auth in `ai-service/app/`).
- FastAPI routes have no `response_model`; backend parses responses leniently.
- FastAPI `/decompose` ignores the `skills` field sent by `RemoteAiGateway.java:111`.
- `/recommend` emits lowercase entity types (`ai-service/app/main.py:189-194`), but backend normalization only handles `STUDENT_TEAM` (`MatchingService.java:85`).
- Input fields have no length/list bounds (`main.py:27-85`).
- No global exception handler or structured logging in FastAPI; `logger` is declared but unused.

### E18. P2 — Database integrity gaps

- `users.role_id` and `users.linked_university_id` have no FKs (`database/schema.sql:23-33`) despite documented relationships and JPA `@ManyToOne`.
- `team_members` and `industry_capabilities` tables exist (`schema.sql:148-155,178-184`) but have no JPA entities.
- Missing UNIQUE constraints include `universities.name`, `industries.name`, `faculty.email`, `project_members(project,user)`, `matches(problem,type,entity)`, `impact_records.project_id`.
- Missing composite indexes include `problems(status,category_id)`, `matches(problem_id,entity_type,entity_id)`, `notifications(user_id,is_read)`, `impact_evidence(impact_record_id)`.
- No CHECK constraints for status/urgency/type values.
- `updated_at` exists on only 3 of 24 tables, contradicting `docs/database.md:62`.

### E19. P2 — Performance risks

- `ProjectService.toResponse` executes multiple repository calls per project and is called for every project in `list()`.
- `ProblemService.list` loads all problems then filters in Java (`:112-121`).
- `DashboardService.analytics` loads all problems/projects and repeatedly recomputes verified beneficiaries.
- `MatchingService.resolveName` performs one lookup per match row.
- No `Pageable`, projections, fetch joins, or batching found.

---

## F. Duplicate or unnecessary code

1. `App.jsx:33-34` declares two routes rendering `MatchingPage`.
2. `components/StatusPill.jsx` contains duplicate `VERIFIED` color entries with different values.
3. Dead frontend API helpers: `dashboardApi.me`, `problemApi.capabilities`, `projectApi.create`, `impactApi.allEvidence`, all `catalogApi` methods.
4. Inline error banner markup repeated across roughly nine pages; no shared `ErrorBanner`.
5. "Loading..." text repeated in four places; no shared `Loading` component.
6. `ProjectsPage.jsx:66` live timestamp has no purpose.
7. `ChallengeDetailPage.jsx:174-181` empty match card.
8. `frontend/dist/` build output is present in the source tree and stale by default.
9. Root artifacts `query`, `org/`, `hs_err_pid18912.log`, `replay_pid18912.log` are accidental clutter.
10. `.github/modernize/java-upgrade/hooks/*.json` contains local absolute paths and session metadata; privacy risk if published.
11. `docs/api.md` and `README.md` duplicate endpoint lists that already drift from source.

---

## 6. Cross-service communication verification

| Connection | Expected | Actual source behavior | Status |
|---|---|---|---|
| Browser dev → backend | Vite proxy `/api` → `localhost:8080` | `vite.config.js:6-18` | Works statically |
| Browser Docker → backend | nginx `/api` → `backend:8080` | `frontend/nginx.conf:8-13` works, but client requests `/api/api/...` | **Broken** |
| Backend → MySQL | `mysql:3306` inside Compose | `application.yml:5` with env override from `docker-compose.yml:43-47` | Wired correctly |
| Backend → FastAPI | `http://ai-service:8000` | `application.yml:32`, `docker-compose.yml:50`, `RemoteAiGateway.java:34-38` | Wired, timeout/fallback issues |
| FastAPI → backend | none | FastAPI is stateless; no callback | Correct |
| Frontend DTO ↔ backend DTO | arrays for AI skills/keywords expected | Backend sends strings | **Broken** |
| Frontend evidence status ↔ backend | `APPROVED` expected by UI | Backend expects `VERIFIED` | **Broken** |
| Frontend notification fields ↔ entity | `title`/`read` expected by UI | Entity has `message`/`isRead` | **Broken** |
| Matching entity types | consistent uppercase vocabulary | FastAPI lowercase; backend only remaps one special case | **Broken/partial** |
| SQL schema ↔ JPA entities | all tables mapped | Two tables unmapped; users FKs missing | **Partial** |
| Docs ↔ source | documented endpoints/ports/files exist | Multiple contradictions | **Broken** |

---

## 7. Database assessment

`database/schema.sql` creates 24 tables covering roles, users, problems, capabilities, universities, departments, faculty, teams, industries, projects, members, tasks, milestones, comments, documents, matches, history, failures, impact records/evidence, and notifications.

### Strengths

- Clear domain decomposition.
- `ON UPDATE` timestamps on problems, projects, impact records.
- Cascades on several child tables.
- Eleven explicit indexes.
- Named Docker volume for MySQL persistence.

### Weaknesses

- No migration tool; init script only runs on an empty MySQL data volume.
- Missing FKs on `users.role_id` and `users.linked_university_id`.
- Two tables have no JPA entity.
- Missing unique constraints and status CHECKs.
- Composite indexes missing for hot query patterns.
- `DataSeeder.java` is Java-side and condition is `problemRepo.count() == 0`; re-running with partial data can duplicate non-problem reference rows.
- Seeded scores use `Math.random()` in `DataSeeder.java:599-613`, so demo data is not deterministic.
- Documentation claims 5 industries/10 projects; seeder actually creates 7/11.

---

## 8. Security assessment summary

### Currently reasonable

- BCrypt password hashing.
- Stateless JWT bearer authentication.
- CSRF disabled appropriately for stateless bearer tokens.
- Ownership checks exist for problem update, notifications, and evidence review role.
- No hardcoded AI provider keys found.
- `.gitignore` excludes `.env`, dependencies, build output, caches, and logs.

### Must be fixed before exposure

1. Remove privileged self-registration and validate `universityId`.
2. Add authorization to all project mutation endpoints.
3. Fix task/milestone cross-project IDOR.
4. Restrict `POST /api/problems/{id}/analyze`.
5. Fail startup on default/short JWT secret; add token claims and DB role revalidation.
6. Gate demo seeding behind an explicit dev/demo profile.
7. Add Bean Validation to all request DTOs.
8. Sanitize global exception messages.
9. Restrict CORS origins via environment configuration.
10. Remove or protect the public AI port.
11. Gate demo credentials in the frontend behind development mode or remove them.
12. Validate URL schemes for document/evidence links to prevent `javascript:` anchors.

---

## 9. Test inventory

### Backend

- **Test files:** none. `backend/src/test` does not exist.
- **Declared dependencies:** `spring-boot-starter-test`, `spring-security-test`, H2 in `backend/pom.xml:74-88`.
- **Executed in this audit:** none.
- **Result:** no test result exists to report.

### AI service

- **Test file:** `ai-service/tests/test_ai.py`, 8 functions:
  1. `test_health`
  2. `test_analyze_classifies_water`
  3. `test_analyze_general_when_no_keywords`
  4. `test_duplicates_ranks_similar_first`
  5. `test_decompose_has_capabilities`
  6. `test_match_scoring_skills`
  7. `test_recommend_returns_picks_and_narrative`
  8. `test_recovery_finds_coverage`

  The functions are at lines 13, 19, 33, 42, 57, 70, 90, and 107.
- **Coverage:** happy-path endpoint coverage only.
- **Missing:** validation errors, malformed input, provider failure simulation, timeout handling, response schema enforcement, all match types, all decomposition templates, recovery no-coverage branch, Java↔Python contract tests.
- **Executed in this audit:** none.

### Frontend

- **Test files:** none.
- **Scripts:** only `dev`, `build`, `preview` (`frontend/package.json:6-10`).
- **Test dependencies:** none.
- **Executed in this audit:** none.

### Documentation claims

`docs/architecture.md:91` claims backend JUnit tests exist with H2; they do not. This must be corrected or fulfilled.

---

## 10. Documentation audit

### Existing files

| File | Useful content | Problems |
|---|---|---|
| `README.md` | Overview, feature table, architecture diagram, setup, env vars, API list, future scope | Frontend port 8081 instead of 5173; empty screenshots; references missing `database/seed.sql` and Postman collection; documents nonexistent endpoints; no GrowthX section |
| `CONTRIBUTING.md` | Branch/commit/PR conventions | Assumes an initialized Git repo; clone URL uses `your-org` placeholder |
| `docs/api.md` | Endpoint reference | Wrong port 8081; documents `/problems/{id}/duplicates` and `/dashboard/{scope}` that do not exist; wrong sample status values |
| `docs/architecture.md` | Component and data-flow diagrams | Wrong port 8081; claims JUnit tests and Porter stemming that are not present; references missing seed.sql |
| `docs/database.md` | 24-table catalogue | Wrong relationship chain; claims all tables have updated_at; lists nonexistent `ARCHIVED` status |
| `docs/demo-flow.md` | Scripted demo narrative | Seed counts/names/states do not match `DataSeeder.java` |

### Required but missing

- `docs/ARCHITECTURE.md`
- `docs/API_DOCUMENTATION.md`
- `docs/DATABASE.md`
- `docs/IMPLEMENTATION_AUDIT.md` — this file fulfills this requirement
- `docs/TESTING.md`
- `docs/FINAL_PROJECT_REPORT.md`
- Real screenshots
- `## GrowthX Off Campus` section with code `N4Q268`

---

## 11. Prioritized implementation plan

The following plan is proposed for approval. No implementation beyond this audit has been performed.

### Phase 1 — Core workflow correctness (do first)

1. Fix Docker API base URL (`AUD-01`).
2. Normalize AI string/array fields in the frontend or backend contract (`AUD-02`).
3. Fix impact evidence submission and review payloads (`AUD-03`, `AUD-04`).
4. Add ownership/role authorization to problem re-analysis.
5. Add Bean Validation to core request DTOs.
6. Add a global 401/403 handling path and shared error components.
7. Add focused tests for each fixed defect.

**Exit criteria:** login, problem submission, problem detail with analysis, re-analysis, and impact evidence flows work with real API data; relevant tests pass.

### Phase 2 — Security hardening

1. Remove privileged roles from self-registration; add admin-only role assignment if needed.
2. Add authorization to all project status/failure/recovery endpoints.
3. Fix task/milestone IDOR by loading children scoped to the project.
4. Enforce JWT secret requirements and revalidate role/status from DB each request.
5. Gate demo seeding behind a profile/property.
6. Sanitize exception responses and harden CORS.
7. Protect or unpublish the AI port.
8. Add authorization tests using Spring Security Test.

### Phase 3 — AI service and matching contract

1. Apply configured HTTP timeout and expose fallback source.
2. Define Pydantic `response_model`s and Java-side strict validation.
3. Normalize entity-type vocabulary across FastAPI and Spring.
4. Extend structured analysis fields toward the project vision without inventing results.
5. Include availability/experience in matching inputs if present in registered data.
6. Add Jest/Vitest-style contract tests for Java↔Python payloads.

### Phase 4 — Database and migration integrity

1. Add Flyway or a versioned SQL mechanism.
2. Add missing FKs, unique constraints, and composite indexes.
3. Map or remove unmapped tables.
4. Add optimistic locking to mutable entities.
5. Make seed data deterministic and profile-gated.

### Phase 5 — Frontend UX and accessibility

1. Add a landing page and 404 page.
2. Add responsive mobile navigation.
3. Add shared loading, empty, error, and success components.
4. Associate all form labels; add ARIA alerts and focus handling.
5. Implement role-aware navigation/dashboards based on backend capabilities.
6. Add project creation, task assignment, capability view, and catalog views where APIs exist.
7. Remove hardcoded impact values and demo credentials from production builds.

### Phase 6 — Testing and CI

1. Add backend JUnit 5/Mockito/Spring Security tests.
2. Expand AI pytest coverage for invalid inputs and failure paths.
3. Add Vitest + React Testing Library + jsdom for frontend.
4. Add `test`/`lint` scripts and a GitHub Actions workflow.
5. Report actual results in `docs/TESTING.md`.

### Phase 7 — Docker, documentation, final verification

1. Fix Docker build reproducibility (`npm ci`, lockfile copy, remove masked Maven failure).
2. Add backend healthcheck and readiness gating for frontend.
3. Correct env-var contract and remove no-op profile.
4. Update README with verified ports, real screenshots, GrowthX section, and known limitations.
5. Create uppercase docs and `docs/FINAL_PROJECT_REPORT.md`.
6. Run and record backend build/tests, AI tests, frontend build/tests, Docker config validation, and workflow smoke checks.

---

## 12. Proposed Phase 1 file-change set (pending approval)

These are the likely files to change in Phase 1; none have been changed yet.

| File | Reason |
|---|---|
| `docker-compose.yml` or `frontend/src/api/client.js` | Fix `/api/api/...` base URL |
| `frontend/src/pages/ChallengeDetailPage.jsx` | Normalize AI string fields |
| `frontend/src/pages/MatchingPage.jsx` | Normalize AI string fields |
| `frontend/src/pages/ImpactPage.jsx` | Fix evidence record ID and review status |
| `backend/.../ProblemController.java` or `ProblemService.java` | Authorize re-analysis |
| Backend request DTOs and controllers | Add Bean Validation and `@Valid` |
| `backend/.../GlobalExceptionHandler.java` | Sanitize error responses |
| `frontend/src/main.jsx` or new component | Add error boundary |
| New backend tests | Cover fixed authorization/validation behavior |
| New AI tests | Cover invalid response and contract cases |

---

## 13. Commands to be used for verification in later phases

These commands were **not run during this audit**. They are recorded for the approved implementation phases.

```powershell
# Backend
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd clean package -DskipTests

# AI service
cd ai-service
python -m pytest -q

# Frontend
cd frontend
npm ci
npm run build
npm test          # after a test runner is added

# Docker Compose validation
docker compose config
docker compose up -d --build
docker compose ps
curl http://localhost:8080/api/health
curl http://localhost:8000/health
```

---

## 14. Approval boundary

Phase 0 is complete. Per the agreed workflow, implementation should not begin until the audit findings and prioritized plan are approved. The next step after approval is **Phase 1 only**, followed by relevant tests and a phase report.
