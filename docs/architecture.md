# Architecture

## High-level view

```
┌────────────────────────────┐
│    Browser (SPA)           │
│  React + Vite + Tailwind   │
│  Recharts · React Router   │
└─────────────┬──────────────┘
              │ REST /api  (JWT Bearer)
              ▼
┌────────────────────────────┐         ┌──────────────────────────────┐
│   Backend (Spring Boot)    │         │       AI Service (FastAPI)   │
│  Spring MVC · Security     │────────►│  /analyze /decompose         │
│  JWT auth · RBAC           │  REST   │  /duplicates /match/*        │
│  Services · Seeder         │  calls  │  /recommend /recovery        │
└─────────────┬──────────────┘         └──────────────────────────────┘
              │ Spring Data JPA          pure-python NLP
              ▼                          TF-IDF · cosine · rules
┌────────────────────────────┐
│        MySQL 8             │
│  25+ normalized tables     │
└────────────────────────────┘
```

## Modules

### Frontend (`frontend/`)
- Vite dev server during development (port 5173); Docker image served by Nginx (port 8081) which proxies `/api` to the backend.
- Pages: Auth, role dashboards, Challenges (list/submit/detail + AI analysis + duplicate warning), Matching & Recommendation, Projects workspace, Impact, Analytics, Profile.
- Single API client (`src/api/client.js`) injecting the JWT and targetting `VITE_API_BASE_URL`.

### Backend (`backend/`)
Layered within the `com.sih.innovationplatform` package:

```
config/    Security, CORS, AI client, DataSeeder
security/  JWT service + filter
user/      users, roles, auth
problem/   problem submission, analysis orchestration
match/     matching + multi-stakeholder recommendation
project/   workspace, lifecycle, failure recovery
impact/    impact records + evidence verification
dashboard/ analytics
notification/ notifications
common/    exceptions, DTO helpers
```

Key decisions:
- One authority-aware token: the JWT carries `role`; endpoints are guarded with `@PreAuthorize` / request matchers.
- The backend is the **single owner of data**; the AI service is **stateless scoring compute**. Matching sends candidate profiles to the AI service and stores only the returned scores + explanations.
- **Resilience:** if the AI service is unavailable (or `AI_ENABLED=false`), a built-in Java rule engine (keyword-based analysis, Jaccard-matching fallback) keeps the flow working for the demo.

### AI Service (`ai-service/`)
- Pure-Python NLP engine: stopword removal, Porter-style stemming, TF-IDF vectors, cosine similarity.
- Knowledge files (`app/knowledge.py`) contain category→keywords, skill→keywords and decomposition templates so the system is fully explainable and editable.
- The scoring interface is stable; a future ML model can be dropped into `app/engine.py` without touching the API contract.

### Database (`database/`)
- `schema.sql` creates the schema (mounted into MySQL in docker).
- Backend `DataSeeder` fills demo data on empty DB. `seed.sql` is a standalone reference dump.

## Data flow (problem → impact)

1. Citizen `POST /api/problems` → row with status `SUBMITTED`.
2. Backend calls AI `/analyze` → category, urgency, skills, keywords, explanation (stored on the problem). Also `/decompose` → capability breakdown stored in `problem_capabilities`.
3. Backend calls AI `/duplicates` against existing problems → `Potential similar challenge found (91%)`; admin can merge (link `similar_problem_id`, mark merged) or continue.
4. Verification moves problem to `VERIFIED`.
5. `matching` runs `/match/university`, `/match/team`, `/match/mentor`, `/match/industry` → scored + explained matches stored in `matches`.
6. `POST /api/problems/{id}/recommend` calls AI `/recommend` to build the multi-stakeholder combination.
7. A project (`projects`) is created from an accepted problem; lifecycle transitions recorded in `project_status_history`.
8. Workspace: tasks, milestones, comments, documents, mentor/industry feedback.
9. On pilot failure: `POST /api/projects/{id}/failure` → AI `/recovery` → new collaborator recommendations (project continues).
10. Implementation tracked with progress percent.
11. `POST /api/projects/{id}/impact` + evidence → verification workflow PENDING → PARTIALLY_VERIFIED → VERIFIED; only VERIFIED impacts count in analytics.

## Configuration

Everything via env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `AI_SERVICE_URL`, `AI_ENABLED`, `FRONTEND_ORIGIN`, `SPRING_PROFILES_ACTIVE`. See `.env.example`.

## Run modes

| Mode | Command | URLs |
|------|---------|------|
| Docker (prod-like) | `docker compose up --build` | frontend :8081 · backend :8080 · ai :8000 |
| Local dev | run the three services individually | frontend :5173 · backend :8080 · ai :8000 |

## Testing

- Backend: JUnit tests (`mvn test`) using H2 (MySQL mode).
- AI: pytest unit tests against the NLP engine.
- Frontend: production build compiles (`npm run build`).