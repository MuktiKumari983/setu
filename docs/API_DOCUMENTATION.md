# API Documentation

## Scope and base URL

This reference is derived from the actual `@RestController` mappings in `backend/src/main/java/com/sih/innovationplatform/`. Backend base URL: `http://localhost:8080`. Most routes start with `/api`; the health controller also exposes `/health`.

Requests and responses use JSON. Protected routes expect:

```
Authorization: Bearer <jwt>
```

The SPA obtains the token from `POST /api/auth/login` and stores it locally. Registration and login are public; all other backend routes require authentication unless listed below. Error responses are produced by `GlobalExceptionHandler` and include `status` and `message` fields.

## Authentication rules

| Scope | Rule |
|-------|------|
| `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/health`, `GET /health`, `/error`, OPTIONS | Public |
| `GET /api/auth/users/{id}`, `PUT /api/auth/users/{id}/role` | Authenticated plus `PLATFORM_ADMIN` or `GOVERNMENT_ADMIN` |
| Problem update | Problem submitter or `PLATFORM_ADMIN` / `GOVERNMENT_ADMIN` |
| Problem re-analysis | Problem submitter or `PLATFORM_ADMIN`, `GOVERNMENT_ADMIN`, `UNIVERSITY_ADMIN` |
| Problem verify and merge | `PLATFORM_ADMIN`, `GOVERNMENT_ADMIN`, `UNIVERSITY_ADMIN` |
| Matching run, recommend and problem recovery | Problem submitter or `PLATFORM_ADMIN`, `GOVERNMENT_ADMIN`, `UNIVERSITY_ADMIN` |
| Project mutations (status, tasks, milestones, comments, documents, progress, failure, resolution, recovery) | Project member, project creator, problem submitter, or `PLATFORM_ADMIN`/`GOVERNMENT_ADMIN` |
| Impact evidence review | `PLATFORM_ADMIN` or `GOVERNMENT_ADMIN` only; status must be `VERIFIED` or `REJECTED` |
| Notification mark-read | Notification owner only |
| All other listed routes | Authenticated JWT user; no additional route-level role check in the controller |

## Health and auth

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| GET | `/api/health` | Backend healthcheck | Public |
| GET | `/health` | Backend health alias | Public |
| POST | `/api/auth/register` | Register a user; self-registration accepts `CITIZEN`, `STUDENT`, `FACULTY`, `INDUSTRY` | Public |
| POST | `/api/auth/login` | Authenticate and receive JWT plus user summary | Public |
| GET | `/api/auth/me` | Current user summary | JWT |
| GET | `/api/auth/users/{id}` | Look up a user by id | Admin |
| PUT | `/api/auth/users/{id}/role` | Assign a known role to a user | Admin |

## Problems

Mapped by `ProblemController` at `/api/problems`.

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| POST | `/api/problems` | Create a problem for the current user | JWT |
| GET | `/api/problems` | List problems; optional query parameters `status`, `category`, `q` | JWT |
| GET | `/api/problems/{id}` | Get one problem with embedded analysis/capability data | JWT |
| PUT | `/api/problems/{id}` | Update a problem | Submitter/platform admin |
| POST | `/api/problems/{id}/analyze` | Run analysis, decomposition and duplicate check; persist results | Submitter/problem admin |
| POST | `/api/problems/{id}/verify` | Set problem status to `VERIFIED` | Problem admin |
| POST | `/api/problems/{id}/merge` | Merge a problem into `targetProblemId` from the JSON body | Problem admin |
| GET | `/api/problems/{id}/capabilities` | List persisted capabilities for a problem | JWT |

## Matching

Mapped by `MatchingController` at `/api/problems/{problemId}`.

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| POST | `/api/problems/{problemId}/match` | Run all stakeholder match types and persist match rows | Problem owner/admin |
| GET | `/api/problems/{problemId}/matches` | List stored matches; optional query parameter `type` | JWT |
| POST | `/api/problems/{problemId}/recommend` | Recommend one stakeholder per type with contributions and gaps | Problem owner/admin |
| GET | `/api/problems/{problemId}/recovery-recommendations` | Recommend collaborators for required query parameter `missingCapability` | Problem owner/admin |

## Catalog

Mapped by `CatalogController`.

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| GET | `/api/universities` | List universities with departments and faculty count | JWT |
| GET | `/api/student-teams` | List student teams with skills, technologies, interests and availability | JWT |
| GET | `/api/faculty` | List faculty with expertise and research area | JWT |
| GET | `/api/industries` | List industry partners and capabilities | JWT |

## Projects

Mapped by `ProjectController` at `/api/projects`.

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| POST | `/api/projects` | Create a project from a verified problem | JWT |
| GET | `/api/projects` | List projects visible to the current user | JWT |
| GET | `/api/projects/{id}` | Get project detail | JWT |
| PUT | `/api/projects/{id}/status` | Move the project to a valid lifecycle status | Project member/admin |
| POST | `/api/projects/{id}/tasks` | Add a task | Project member/admin |
| PUT | `/api/projects/{id}/tasks/{taskId}` | Update a task in this project | Project member/admin |
| POST | `/api/projects/{id}/milestones` | Add a milestone | Project member/admin |
| PUT | `/api/projects/{id}/milestones/{milestoneId}` | Update a milestone in this project | Project member/admin |
| POST | `/api/projects/{id}/comments` | Add a comment | Project member/admin |
| POST | `/api/projects/{id}/documents` | Add a document URL | Project member/admin |
| PUT | `/api/projects/{id}/progress` | Update progress percentage | Project member/admin |
| POST | `/api/projects/{id}/failure` | Report a failed stage and missing capability | Project member/admin |
| POST | `/api/projects/{id}/failure/resolve` | Resolve a failure; optional `note` in JSON body | Project member/admin |
| GET | `/api/projects/{id}/recovery-recommendations` | Recommend collaborators to recover from a failure | Project member/admin |
| GET | `/api/projects/{id}/timeline` | List project status history | JWT |

## Impact

Mapped by `ImpactController` at `/api`.

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| GET | `/api/projects/{id}/impact` | List impact records for a project | JWT |
| POST | `/api/projects/{id}/impact` | Submit an impact record | Project member/admin |
| POST | `/api/projects/{id}/impact/evidence` | Add evidence URL to the project's impact record | Project member/admin |
| PUT | `/api/impact/evidence/{evidenceId}/review` | Review evidence as `VERIFIED` or `REJECTED` | Admin |
| GET | `/api/impact/evidence` | List evidence records | JWT |
| GET | `/api/impact` | List all impact records | JWT |
| GET | `/api/impact/stats` | Verified/total beneficiary and record counts | JWT |

Documents and impact evidence accept URL strings. There is no multipart upload endpoint in the backend.

## Notifications and dashboard

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| GET | `/api/notifications` | List notifications for the current user | JWT |
| POST | `/api/notifications/{id}/read` | Mark one of the current user's notifications as read | JWT, owner |
| GET | `/api/notifications/unread-count` | Count unread notifications for the current user | JWT |
| GET | `/api/dashboard/analytics` | Platform analytics data | JWT |
| GET | `/api/dashboard/me` | Current role, name and email | JWT |

## AI service routes

These are FastAPI routes, not backend `@RestController` mappings. In Compose they are reachable only at `http://127.0.0.1:8000`; they are called by the backend and do not implement user authentication.

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/health` | Healthcheck used by Docker Compose |
| POST | `/analyze` | Structured problem analysis |
| POST | `/duplicates` | Similar-problem ranking |
| POST | `/decompose` | Capability decomposition |
| POST | `/match/{match_type}` | Scored candidates for one stakeholder type |
| POST | `/recommend` | Combined stakeholder recommendation |
| POST | `/recovery` | Missing-capability recovery ranking |

## Verification note

Endpoint behavior and auth rules were checked against controller annotations and service checks. Local verification results are recorded in `docs/TESTING.md`. No deployment was performed.
