# Architecture

## Purpose

This document describes the architecture of the SETU Societal Innovation Collaboration Platform prototype as implemented in this working copy. It is based on the repository source, `docker-compose.yml`, backend Flyway migrations and `docs/IMPLEMENTATION_AUDIT.md`. No unsupported production, deployment or ML claims are made.

## Verified ports and ingress

| Component | Verified address | Notes |
|-----------|------------------|-------|
| Frontend | http://localhost:5173 | Vite dev server in development; Nginx published on host port 5173 in Docker |
| Backend | http://localhost:8080 | Spring Boot; most endpoints are under `/api` |
| MySQL | host port 3307 | Container port 3306; named Docker volume `mysql-data` |
| AI service | http://127.0.0.1:8000 | FastAPI; loopback-only host binding in Compose |

In development, Vite proxies `/api` and `/files` to `http://localhost:8080`. In Docker, Nginx serves the built React bundle, proxies `/api` to `backend:8080`, and falls back to `index.html` for SPA routes. The AI port is intentionally loopback-only; it is an internal scoring service, not a public API.

## Component overview

| Component | Implementation | Responsibility |
|-----------|----------------|----------------|
| Browser SPA | React 18, Vite 5, React Router 6, Tailwind CSS 3, Recharts 2 | Pages for auth, challenges, analysis, matching, projects, impact, catalog, analytics and notifications |
| Backend | Spring Boot 3.5.4, Java 25, Spring MVC, Spring Security, JJWT, Spring Data JPA, Bean Validation | REST API, JWT bearer auth, role/service checks, orchestration, persistence and analytics |
| Database | MySQL 8.4 | Relational store for 24 domain tables |
| Migrations | Flyway core + flyway-mysql, `V1__baseline.sql` and `V2__harden_schema.sql` | Schema creation and hardening on backend startup; JPA `ddl-auto: none` |
| AI service | FastAPI 0.115.12, Uvicorn 0.34.3, Pydantic 2, deterministic rules, TF-IDF + cosine similarity | Stateless analysis, decomposition, duplicate ranking, stakeholder scoring, recommendations and recovery |
| AI fallback | `LocalAiGateway.java` | Java rule engine used when `AI_ENABLED=false` or remote AI calls fail |
| Reverse proxy | Nginx 1.27 in the frontend image | Serves static SPA and proxies `/api` to the backend in Docker |
| CI definition | `.github/workflows/ci.yml` | Runs backend, AI, frontend and Compose config jobs; workflow exists but has not been executed on GitHub |

## Data flow

```
Browser (React, :5173)
   ├─ dev: Vite /api proxy -> localhost:8080
   └─ Docker: Nginx /api proxy -> backend:8080
        |
        v
Spring Boot (:8080)
   ├─ JWT filter + service authorization
   ├─ Spring Data JPA -> MySQL (mysql:3306, host :3307)
   └─ RemoteAiGateway -> FastAPI (ai-service:8000, host loopback :8000)
```

Typical request path:

1. The user signs in through `POST /api/auth/login`; the backend returns a signed JWT and user summary.
2. The SPA sends the JWT as `Authorization: Bearer <token>` on protected requests.
3. Problem creation, re-analysis and matching are stored in MySQL by the backend.
4. For analysis and matching, the backend sends only the required payload to the AI service; the AI service does not read or write MySQL.
5. AI results include a `source` marker (`REMOTE` or the local fallback source). Remote failures are logged and fall back to the Java rule engine.
6. Project lifecycle, failure recovery and impact evidence are persisted by the backend and exposed to the SPA through REST endpoints.

## AI integration

`RemoteAiGateway.java` calls the FastAPI routes below. The configured HTTP timeout is applied when building the request factory. The FastAPI service defines Pydantic response models and validates request sizes.

| AI route | Purpose |
|----------|---------|
| `GET /health` | Container healthcheck |
| `POST /analyze` | Category, urgency, skills, keywords, confidence, explanation and structured analysis fields |
| `POST /duplicates` | TF-IDF/cosine similarity ranking of existing problems |
| `POST /decompose` | Category-based capability templates with required skills and priority |
| `POST /match/{match_type}` | Scored candidates for `UNIVERSITY`, `TEAM`, `MENTOR` or `INDUSTRY` |
| `POST /recommend` | Best stakeholder per type with contribution and gap skills |
| `POST /recovery` | Candidates that may close a missing capability after a failed pilot |

The engine is deterministic and rule-based. It is not an LLM, embedding model or trained classifier, and no paid provider is used.

## Docker healthcheck and readiness flow

`docker-compose.yml` defines the following startup behavior:

1. **MySQL** starts with `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` and `MYSQL_ROOT_PASSWORD`. Healthcheck: `mysqladmin ping` every 5 seconds, up to 20 retries. Data persists in the `mysql-data` volume.
2. **AI service** starts from `ai-service/Dockerfile`. Healthcheck: Python urllib request to `http://localhost:8000/health`. Its host port binding is `127.0.0.1:8000:8000`.
3. **Backend** waits for `mysql` and `ai-service` with `condition: service_healthy`. On startup Flyway applies `V1` then `V2`; the optional `DataSeeder` runs only when `APP_SEED_ENABLED=true` and the core tables are empty. Healthcheck: `curl -f http://localhost:8080/api/health`; the JRE image installs `curl` for this check.
4. **Frontend** waits for `backend` with `condition: service_healthy`. The build stage uses `npm ci` with `package.json` and `package-lock.json`, then Nginx serves `/app/dist` on container port 80, published as host port 5173.

This means `docker compose up --build` starts the stack in dependency order and the frontend is not started until the backend health endpoint responds successfully.

## Runtime configuration contract

Runtime settings come from environment variables; there is no Spring profile-specific configuration.

| Variable | Used by | Purpose |
|----------|---------|---------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Backend | JDBC connection |
| `JWT_SECRET`, `JWT_ALLOW_DEV_SECRET`, `JWT_EXPIRATION_MS` | Backend | Signing, development-secret allowance and token lifetime |
| `APP_SEED_ENABLED` | Backend | Enables deterministic demo seeding on an empty database |
| `AI_SERVICE_URL`, `AI_ENABLED` | Backend | AI endpoint and remote/fallback selection |
| `FRONTEND_ORIGIN` | Backend | Allowed CORS origin |
| `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` | Compose MySQL | Container initialization |

## Security boundary

- Authentication is stateless JWT bearer authentication with BCrypt password hashing.
- `SecurityConfig` permits registration, login and health endpoints; all other requests require authentication. `/api/auth/users/**` additionally requires `PLATFORM_ADMIN` or `GOVERNMENT_ADMIN`.
- Service-level checks enforce problem owner/admin rules, project member/admin rules, notification ownership and admin-only impact evidence review.
- This is prototype-level authorization. It is not production auth hardening: there is no token revocation/refresh flow, email verification, password reset or published secret-management process.

## Verification and boundaries

Recorded local verification is in `docs/TESTING.md`: backend 62 tests pass, AI 77 tests pass, frontend lint is clean with 18 Node checks and 8 Vitest tests passing, the frontend build passes, and `docker compose config` passes. The GitHub Actions workflow was not executed on GitHub, no deployment was performed, and no screenshots exist in this working copy.
