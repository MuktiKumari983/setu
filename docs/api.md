# API Documentation

Base URLs:
- Local: `http://localhost:8080/api`
- Docker frontend proxy: `/api` on `http://localhost:8081`

Auth: `Authorization: Bearer <jwt>` on all endpoints except register/login/health.

---

## Authentication

### POST `/api/auth/register`
Create an account. Default role unless specified.
```json
{ "name": "Ravi Kumar", "email": "ravi@example.com",
  "password": "password123", "role": "CITIZEN",
  "universityId": 1 }
```
Response 201: user + token.
Roles allowed at registration: `CITIZEN, STUDENT, FACULTY, UNIVERSITY_ADMIN, INDUSTRY`.

### POST `/api/auth/login`
```json
{ "email": "citizen@example.com", "password": "password123" }
```
Response: `{ token, user: { id, name, email, role } }`.

### GET `/api/auth/me`
Current user + profile counts.

---

## Problems

### POST `/api/problems`
Citizen submission.
```json
{ "title": "Poor waste collection in rural community",
  "description": "Waste collection is irregular and there is no tracking mechanism.",
  "category": null, "location": "Hosur, Tamil Nadu",
  "estimatedBeneficiaries": 5000, "urgency": "HIGH",
  "requiredOutcome": "Regular tracked waste collection", "imageUrl": null }
```
Response 201: problem (status `SUBMITTED`).

### GET `/api/problems`  — list with filters `?category=&status=&q=`
### GET `/api/problems/{id}` — detail incl. capabilities + matches + duplicate info

### POST `/api/problems/{id}/analyze`
Runs AI intelligence (also triggered on submit). Response:
```json
{ "category": "Waste Management", "urgency": "HIGH",
  "requiredSkills": ["IoT","Data Analytics","GIS","Web Development"],
  "keywords": ["waste","collection","village","tracking"],
  "confidence": 0.94,
  "explanation": "Matched 'waste','collection' strongly with category 'Waste Management'; 'tracking' implies IoT + Web Development." }
```

### POST `/api/problems/{id}/duplicates`  (also run on submit)
```json
{ "similarProblems": [ { "id": 102, "title": "...", "similarity": 0.91 } ] }
```

### POST `/api/problems/{id}/verify`  (admin) → status `VERIFIED`
### POST `/api/problems/{id}/merge`  `{ "targetProblemId": 102 }` → mark merged.
### PUT `/api/problems/{id}` — edit (citizen own / admin)

---

## Matching

### GET `/api/universities` — university profiles subs
### GET `/api/student-teams`
### GET `/api/faculty`
### GET `/api/industries`
### GET `/api/problems/{id}/matches?type=university`

### POST `/api/problems/{id}/match`
Runs all four matchers, returns scored + explained lists and stores rows in `matches`.

### POST `/api/problems/{id}/recommend`
Multi-stakeholder combination:
```json
{ "university": { ... , "score": 92, "reason": "IoT lab + water research" },
  "team": { ..., "score": 94, "reason": "...", "gapSkills": [] },
  "mentor": { ... },
  "industry": { ... } }
```

---

## Projects

### POST `/api/projects`
Create project from a `VERIFIED`/`MATCHED` problem. Body: `{ problemId, note }`.
Backend bundles the recommended combination into members.

### GET `/api/projects` — list (role-scoped)
### GET `/api/projects/{id}` — overview, members, tasks, milestones, comments, documents, status history

### PUT `/api/projects/{id}/status`
`{ "status": "PROTOTYPE", "note": "Prototype built" }` — validates legal transitions.
Lifecycle: `SUBMITTED → VERIFIED → MATCHED → TEAM_FORMED → PROPOSAL → PROTOTYPE → REVIEW → PILOT → IMPLEMENTATION → IMPACT`.

### POST `/api/projects/{id}/tasks` `{ title, description, assignedToId?, priority }`
### PUT `/api/projects/{id}/tasks/{taskId}` `{ status }` (OPEN/IN_PROGRESS/DONE)
### POST `/api/projects/{id}/milestones` `{ title, description, targetDate }`
### PUT `/api/projects/{id}/milestones/{milestoneId}` `{ status }`
### POST `/api/projects/{id}/comments` `{ content, commentType }`  (mentor/industry/general)
### POST `/api/projects/{id}/documents` `{ name, url }`
### PUT `/api/projects/{id}/progress` `{ progress }` (auto-derived from stages)

### POST `/api/projects/{id}/failure`
```json
{ "stage": "PILOT", "reason": "GIS expertise missing", "missingCapability": "GIS" }
```
Stored in `failure_records`; sets project status `NEEDS_SUPPORT`.

### GET `/api/projects/{id}/recovery-recommendations`
AI `/recovery` → alternate mentor/team/university/industry to close the gap.

### GET `/api/projects/{id}/timeline`
Status history with dates (feeds the UI progress tracker).

---

## Impact

### POST `/api/projects/{id}/impact`
```json
{ "beneficiaries": 8500, "locationsCovered": 4, "costSavings": 240000,
  "problemReduction": "Collection coverage up from 20% to 85%",
  "durationMonths": 9, "implementationPercent": 100, "domainMetrics": "..." }
```

### POST `/api/projects/{id}/impact/evidence`
`{ "impactRecordId", "evidenceType", "description", "fileUrl" }`
→ evidence status `PENDING`.

### PUT `/api/impact/evidence/{id}/review`  (admin)
`{ "status": "VERIFIED" }` → advances impact record : `PENDING → PARTIALLY_VERIFIED → VERIFIED`.
Only `VERIFIED` impact records appear in verified statistics.

---

## Dashboards

### GET `/api/dashboard/{scope}`  scope = `admin | government | university | industry | faculty | student | citizen`
Returns role-specific cards + lists + chart data. Aggregated via `GET /api/dashboard/analytics`:
```json
{ "totalChallenges": 12, "verifiedChallenges": 8, "matchedChallenges": 6,
  "activeProjects": 4, "implementedProjects": 3, "failedPilots": 2,
  "projectsNeedingSupport": 1, "verifiedBeneficiaries": 35000,
  "byCategory": {...}, "byStatus": {...},
  "universityParticipation": {...}, "industryParticipation": {...},
  "implementationRate": 0.72, "impactSummary": {...} }
```

## Notifications
### GET `/api/notifications` · POST `/api/notifications/{id}/read`

---

## Errors
All errors: `{ "message": "...", "status": 400 }` style JSON with appropriate HTTP codes (400/401/403/404/409/500).

## Sample curl

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"citizen@example.com","password":"password123"}'

curl http://localhost:8080/api/problems?status=OPEN \
  -H "Authorization: Bearer <token>"
```