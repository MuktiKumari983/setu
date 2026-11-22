# Testing Report

Date: 2026-11-22

Scope: Phases 6–7 of `docs/IMPLEMENTATION_AUDIT.md` — backend tests, AI failure-path tests, frontend Vitest/RTL/jsdom tests, lint/CI scripts, Docker reproducibility/readiness changes, documentation, and recorded local verification. Final release verification details are recorded in `docs/RELEASE_READINESS_REPORT.md`.

## Environment

- Java 25.0.2, Maven Wrapper 3.9.9
- Node 24.16.0, npm 11.13.0
- Python virtual environment at `ai-service/.venv`
- Docker Compose config validation via local Docker CLI

## Commands and actual results

| Area | Command | Result |
|---|---|---|
| Backend | `.\mvnw.cmd test` from `backend` | `Tests run: 63, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS` |
| Backend build | `.\mvnw.cmd package -DskipTests` from `backend` | `BUILD SUCCESS`; produced `target/innovation-platform-1.0.0.jar` |
| AI service | `.\.venv\Scripts\python.exe -m pytest -q` from `ai-service` | `77 passed, 8 warnings in 2.00s` |
| Frontend lint | `npm run lint` from `frontend` | Passed with 0 errors and 0 warnings |
| Frontend Node tests | `node --test` via `npm test` | 18 passes: 17 real tests plus `src/test/setup.js` discovered as one file-level pass |
| Frontend component tests | `vitest run` via `npm test` | 5 test files, 8 tests passed |
| Frontend build | `npm run build` from `frontend` | Passed; existing >500 kB chunk warning remains |
| Compose config | `docker compose config --quiet` from repository root | Passed; env values suppressed |
| Docker image build | `docker compose build` from repository root | Not verified locally: Docker daemon was unavailable (`npipe:////./pipe/dockerDesktopLinuxEngine`) |

## Coverage added in Phase 6

- Backend:
  - `security/JwtServiceTokenLifecycleTest.java`: token round-trip, expiry, tampering.
  - `impact/ImpactReviewAuthorizationTest.java`: admin-only review, invalid review status, verified review state update.
  - `project/ProjectLifecycleAuthorizationTest.java`: membership checks for status change, failure report, failure resolution, recovery recommendations.
- AI service:
  - `tests/test_ai_validation.py`: request size, type, and canonical vocabulary validation.
  - `tests/test_ai_edge_cases.py`: empty and low-similarity paths across analyze, duplicates, recommend, and recovery endpoints.
  - `tests/test_engine.py`: tokenizer, cosine/Jaccard, fallback category, and score clamping.
- Frontend:
  - Vitest 2.1.9 with jsdom, React Testing Library, Jest DOM matchers, and user-event.
  - ESLint 9 flat config with JSX-aware React rules and React Hooks rules.
  - Component tests for `ErrorBanner`, `EmptyState`, `FormField`, `LandingPage`, and `NotFoundPage`.
  - `npm test` runs both the existing Node contract/utility tests and the new Vitest component tests.
- CI:
  - `.github/workflows/ci.yml` runs backend tests, AI pytest, frontend lint/tests/build, and `docker compose config`.

## Phase 7 changes verified locally

- Frontend image uses `npm ci` with `package.json` and `package-lock.json` copied before source.
- Backend image no longer masks `dependency:go-offline` failures with `|| true`.
- Backend runtime image installs `curl`; Compose healthcheck calls `/api/health`.
- Frontend container waits for `backend: service_healthy` before starting.
- Compose passes `DB_URL` instead of the unused `DB_HOST`/`DB_PORT`/`DB_NAME` variables; the no-op `SPRING_PROFILES_ACTIVE=docker` variable was removed.
- README, uppercase docs, and `docs/FINAL_PROJECT_REPORT.md` were corrected for verified ports, env vars, endpoints, GrowthX code, and known limitations.

## Caveats

- The GitHub Actions workflow was created but has not been executed on GitHub in this local verification run; the workflow-equivalent commands were run locally.
- `docker compose build` could not run because the local Docker daemon was unavailable; only `docker compose config` was verified.
- No image assets exist in this working copy, so real screenshots remain a pre-submission documentation task.
- No deployment was performed.
- `mvnw.cmd clean test` is avoided locally because a Windows file lock previously caused a `Failed to delete ...\backend\target\clas...` error; `mvnw.cmd test` is verified.
- Backend runs emit JVM native-access/dynamic-agent warnings and one generated Spring security password warning during the `@WebMvcTest` slice; all tests pass.
- AI runs emit pre-existing FastAPI/Starlette deprecation warnings; all tests pass.
- Frontend build emits the pre-existing chunk-size warning.
- npm reports 10 dependency audit findings (6 moderate, 2 high, 2 critical) from the current dependency tree; no forced audit fix was applied because it may introduce breaking changes.
