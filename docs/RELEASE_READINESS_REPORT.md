# Release Readiness Report

**Project:** SETU — Societal Innovation Collaboration Platform
**Date:** 2026-11-22
**Workspace:** `C:\Users\kumar\OneDrive\Desktop\setu`

## Verdict

Not fully release-ready in this environment. All non-Docker regression checks pass, the AI HTTP service was exercised directly, and two confirmed issues were fixed. Full Compose build, container healthcheck, and browser-driven end-to-end verification remain blocked because the Docker daemon is unavailable. No deployment was performed.

No screenshot, API response, match result, or deployment status has been fabricated. No secret values are reproduced in this report.

## Environment checked

- Java 25.0.2, Maven Wrapper 3.9.9
- Node 24.16.0, npm 11.13.0
- Python virtual environment at `ai-service/.venv`
- Docker CLI 29.9.3, active context `desktop-linux`
- Local MySQL running on host port 3306; no Compose DB on host port 3307

## Docker readiness

| Check | Command | Actual result |
|---|---|---|
| Docker CLI/daemon | `docker version` | CLI 29.9.3 responded; daemon connection failed at `npipe:////./pipe/dockerDesktopLinuxEngine` |
| Docker Desktop process | `Get-Process -Name "Docker Desktop" -ErrorAction SilentlyContinue \| Select-Object ProcessName, Id, StartTime` | No process found |
| Docker contexts | `docker context ls` | `desktop-linux` active; daemon not connected |
| Compose file validation | `docker compose config --quiet` | `compose config OK` |
| Compose build | `docker compose build` | Not run; daemon unavailable |
| Compose runtime | `docker compose up -d` | Not run; daemon unavailable |

Start Docker Desktop, wait until the tray icon is ready, re-run `docker version`, and then repeat the Compose build/runtime checks before submission.

## Runtime and integration verification

### AI service

The service was started from the existing virtual environment:

```powershell
ai-service\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

`GET http://127.0.0.1:8000/health` returned `{"status":"ok"}`.

Direct HTTP checks with non-secret sample payloads returned real responses:

| Endpoint | Observed result |
|---|---|
| `POST /analyze` | category `Water Quality`, urgency `MEDIUM`, source `REMOTE` |
| `POST /decompose` | category `Flood & Disaster`, 4 capabilities, source `REMOTE` |
| `POST /match/TEAM` | 1 result, source `REMOTE` |
| `POST /recommend` | 1 stakeholder, source `REMOTE` |
| `POST /recovery` | top recommendation `Vision Squad`, missing gap closed `true`, source `REMOTE` |

These checks validate the standalone AI service only. They are not a backend/UI journey. The temporary Python processes were stopped after verification.

### Backend

The existing jar `backend/target/innovation-platform-1.0.0.jar` was started with `APP_SEED_ENABLED=true`, `AI_SERVICE_URL=http://127.0.0.1:8000`, and a separate verification database URL. Credentials were read programmatically from the example environment file and were not printed.

Startup failed before Tomcat became healthy. The concrete cause in `backend-release-stdout.log` was a Flyway database connection failure: access denied for the configured local MySQL user. The same environment credential check also failed through the MySQL CLI because the installed client lacks the `mysql_native_password` plugin. This is a local MySQL credential/plugin problem, not a confirmed application defect.

`GET http://127.0.0.1:8080/api/health` therefore remained unreachable and was recorded as failed. No Java process remained after the failed startup.

### Frontend and workflow inspection

The frontend route map and API call sites were inspected statically against the controller map. Public routes, protected routes, role-aware navigation, project creation, tasks/milestones, failure recovery, impact evidence, catalog, matching, and analytics flows are present. Live browser/end-to-end verification was not performed because the backend could not start locally and Compose is unavailable.

## Security review and fixes

Verified:

- Public endpoints are limited to authentication, health, error, and CORS preflight.
- All other API requests require authentication; role assignment is restricted to platform/government admins.
- JWT signature and expiry validation is present, and the bundled development secret is gated by an explicit allowance flag.
- Passwords are BCrypt-hashed; self-registration cannot assign privileged roles.
- CORS uses an explicit configured origin, not a wildcard.
- Request DTOs are validated with Bean Validation; frontend source contains no production API keys or real credentials.
- Dev-only demo credentials and dev secret defaults remain documented for the local demo; no production secret values are included here.

Confirmed issues fixed:

1. Project detail and timeline reads bypassed project membership checks. Scoped reads now call `ProjectAccessService.requireMemberOrAdmin` through `ProjectService.getForUser` at `backend/src/main/java/com/sih/innovationplatform/project/ProjectService.java:156`; controllers use it at `backend/src/main/java/com/sih/innovationplatform/project/ProjectController.java:36` and `backend/src/main/java/com/sih/innovationplatform/project/ProjectController.java:109`. Regression coverage is at `backend/src/test/java/com/sih/innovationplatform/project/ProjectLifecycleAuthorizationTest.java:32`.
2. The JWT tamper test modified the final Base64URL character, which can leave the decoded signature unchanged. It now mutates the first signature character at `backend/src/test/java/com/sih/innovationplatform/security/JwtServiceTokenLifecycleTest.java:39`. This was a test defect, not a production signing defect.

Accepted limitations, not changed in this pass: tokens are stored in `localStorage`; demo accounts and dev secret defaults are intended for local demonstration only; no production secret manager or deployment hardening is implemented.

## Regression results

| Area | Command | Actual result |
|---|---|---|
| Backend tests | `.\mvnw.cmd test` from `backend` | 63 tests pass, 0 failures/errors |
| Backend package | `.\mvnw.cmd package -DskipTests` from `backend` | `BUILD SUCCESS`; executable jar produced |
| AI tests | `.\.venv\Scripts\python.exe -m pytest -q` from `ai-service` | 77 passed |
| Frontend lint | `npm run lint` from `frontend` | 0 errors, 0 warnings |
| Frontend tests | `npm test` from `frontend` | 18 Node passes + 8 Vitest passes |
| Frontend build | `npm run build` from `frontend` | Passed; pre-existing chunk-size warning remains |
| Compose validation | `docker compose config --quiet` | Passed, env values suppressed |

Known non-failing warnings: JVM native-access/dynamic-agent warnings, a generated Spring security password warning in one test slice, FastAPI/Starlette deprecation warnings, frontend chunk-size warning, and npm audit findings. No forced dependency audit fix was applied.

## Demo readiness

- Working demo story and seeded accounts are documented in `README.md`.
- Real screenshots are still missing under `docs/screenshots/`; none were created or fabricated.
- GitHub Actions workflow exists but has not been executed on GitHub.
- Docker image build, Compose startup, container healthchecks, and browser-driven journey remain unverified.
- No deployment to AWS or any paid service was performed.

## Remaining actions

1. Start Docker Desktop and verify `docker version`.
2. Run `docker compose build` and `docker compose up -d`; confirm all healthchecks.
3. Execute the documented UI journey and capture real screenshots.
4. Run the GitHub Actions workflow remotely.
5. Review the final Git diff and obtain explicit approval before committing.
