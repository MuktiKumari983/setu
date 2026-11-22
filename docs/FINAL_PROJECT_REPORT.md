# Final Project Report

**Project:** SETU — Societal Innovation Collaboration Platform
**Problem statement:** SIH26043, Smart India Hackathon 2026
**Report date:** 2026-11-22
**Workspace:** `C:\Users\kumar\OneDrive\Desktop\setu`

## Problem

Citizens and communities report societal problems without a structured path to verified universities, student teams, faculty mentors and industry partners. Solutions are built in isolation, lifecycle progress is hard to track, failed pilots have no recovery path, and social impact is rarely verified with evidence.

## Solution

SETU is a working prototype that turns a citizen-reported challenge into an implementation workflow: submit and verify a problem, run explainable AI-style analysis, split it into capabilities, score and recommend stakeholders, create a project workspace, track the lifecycle, recover from failed pilots and record evidence-backed impact.

The AI layer is deterministic rule-based NLP with TF-IDF and cosine similarity. It requires no paid API or external model provider.

## Architecture

| Layer | Implementation |
|-------|----------------|
| Frontend | React 18, Vite 5, React Router 6, Tailwind CSS 3, Recharts 2; dev server and Docker Nginx on host port 5173 |
| Backend | Spring Boot 3.5.4, Java 25, Spring MVC, Spring Security, JWT, Spring Data JPA, Bean Validation; port 8080 |
| Database | MySQL 8.4, 24 tables, Flyway V1/V2, host port 3307 |
| AI service | FastAPI 0.115.12 with deterministic rules; loopback host port 8000 |
| Proxy | Nginx serves the built SPA and proxies `/api` to `backend:8080` |
| CI | GitHub Actions workflow for backend, AI, frontend and Compose config checks |

The backend owns persistent state and orchestrates AI calls. The AI service is stateless. Docker Compose gates startup with healthchecks: MySQL and AI must be healthy before the backend starts, and the backend must be healthy before the frontend starts. See `docs/ARCHITECTURE.md`.

## Features delivered

- JWT login, registration and role-based access with BCrypt password hashing.
- Problem submission, listing/filtering, update, verification, merge and persisted analysis/capabilities.
- Deterministic analysis, decomposition, duplicate ranking and explainable match scoring.
- University, student team, faculty and industry catalog endpoints and combined recommendations.
- Project creation, tasks, milestones, comments, document URLs, progress, status history and lifecycle transitions.
- Failure reporting and recovery recommendations for missing capabilities.
- Impact records, evidence review and verification state calculation.
- Role-aware pages, analytics, notifications, landing and not-found pages, shared error/empty/loading components.
- Flyway migrations, deterministic demo seeding and Docker healthcheck/readiness wiring.

## Phases completed

| Phase | Outcome represented in the working copy |
|-------|------------------------------------------|
| Phase 0 | Static implementation audit recorded in `docs/IMPLEMENTATION_AUDIT.md` |
| Phase 1 | Core workflow and validation corrections, with backend and AI contract tests |
| Phase 2 | Authorization, JWT revalidation, project access and registration hardening |
| Phase 3 | AI response models, validation, timeout handling, source markers and type normalization |
| Phase 4 | Flyway V1/V2 migrations, integrity constraints, indexes, version columns and deterministic seeding |
| Phase 5 | Frontend UX, accessibility, role-aware navigation and shared components |
| Phase 6 | Backend, AI and frontend tests plus lint/build scripts and a GitHub Actions workflow |
| Phase 7 | Docker/Docs corrections, uppercase documentation and local final verification |

## Verification results

Recorded in `docs/TESTING.md` and `docs/RELEASE_READINESS_REPORT.md`:

| Area | Result |
|------|--------|
| Backend | 63 tests pass, 0 failures/errors |
| Backend build | `mvnw package -DskipTests` passes and produces the executable jar |
| AI service | 77 tests pass |
| Frontend lint | Clean, 0 errors and 0 warnings |
| Frontend Node checks | 18 passes |
| Frontend Vitest tests | 8 passes across 5 test files |
| Frontend build | Passes; pre-existing chunk-size warning remains |
| Docker Compose config | Passes |

The GitHub Actions workflow exists but has not been executed on GitHub; its equivalent commands were run locally. `docker compose build` could not be verified locally because the Docker daemon was unavailable, while `docker compose config` passed. No deployment was performed.

## Known limitations

- The AI is deterministic rule-based NLP, not a trained ML model.
- There is no real object storage, email/SMS, or production auth hardening beyond JWT/roles.
- No screenshots exist in this working copy; real captures must be placed under `docs/screenshots/` before submission.
- npm audit findings remain and no forced audit fix was applied.
- The GitHub Actions workflow has not yet been executed remotely.
- No deployment was performed.

## GrowthX Off Campus

`N4Q268`
