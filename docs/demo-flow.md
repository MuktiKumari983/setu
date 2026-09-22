# Demo Flow — the complete end-to-end story

This is the one scenario you should be able to walk through live in the UI.

## Star scenario

> **"Poor waste collection and monitoring in rural area."**

### Stage 0 — Seeded ecosystem
The backend seeds 10 problems, 5 universities, 10 faculty, 5 teams, 5 industries, 10 projects
(3+ implemented, 2 failed pilots), matches and verified impact records. Log in as
`citizen@example.com` / `password123` to see the ecosystem already looks alive.

### Stage 1 — Citizen submits a problem
1. Login as **citizen** → **Challenges → Submit Challenge**.
2. Enter: *"Poor waste collection and monitoring in rural area"* /
   *"Waste collection is irregular and there is no tracking mechanism; garbage is piling up at village collection points."*
   Location: Hosur, Tamil Nadu · Beneficiaries: 8000 · Urgency: HIGH.
3. Submit → status **SUBMITTED**, gets a problem id.

### Stage 2 — AI Problem Intelligence
1. The backend calls the AI service → `POST /api/problems/{id}/analyze`.
2. UI shows the **analyzable result with reasons**:
   - Category: **Waste Management** (matched keywords waste, collection, village, tracking)
   - Urgency: **HIGH**
   - Required skills: **IoT, GIS, Data Analytics, Web Development**
   - Confidence 0.9+ and a plain-language **explanation box**.

### Stage 3 — Capability decomposition
The problem detail page shows sub-capabilities returned by `/decompose`:
- Collection scheduling → Backend, Data Analytics
- Route optimization → Algorithms, GIS
- Waste segregation → IoT, Computer Vision
- Monitoring dashboard → Web Development

### Stage 4 — Duplicate detection
A warning banner appears: **"Potential similar challenge found: #5 ... similarity 91%"**.
As admin you can **merge** (link problems) or **continue as separate challenge**.

### Stage 5 — Verification & matching
1. Admin/team verifies the problem (`VERIFIED`).
2. Run **Matching**: the API surfaces explainable scores:
   - University **Indian Institute of Technology, Delhi** 92% → "IOT Lab + Waste & Water Research Group + related project"
   - Student Team **GreenCoders** 94% → Java ✓ IoT ✓ Data Analytics ✓
   - Mentor **Dr. Anjali Verma** 89% → Solid Waste Engineering expertise
   - Industry **Tata Conservation Solutions** 91% → IoT hardware ✓ deployment ✓
3. **Multi-stakeholder recommendation** is shown as one combined card: University + Team + Mentor + Industry, each with its contribution ("University → research/lab support · Team → software build · Mentor → domain guidance · Industry → hardware + deployment").

### Stage 6 — Project workspace
Create the project. The workspace shows overview, members, tasks, milestones, comments, mentor and industry feedback, files.

### Stage 7 — Lifecycle tracking
Walk the status timeline SUBMITTED → … → PROTOTYPE → PILOT. The UI shows the progress tracker and percent.

### Stage 8 — Failed pilot + recovery (differentiator!)
1. Pilot fails → `POST /api/projects/{id}/failure` with reason **"GIS expertise missing"**.
2. The system marks the project **NEEDS_SUPPORT**; nobody deletes it.
3. `GET /api/projects/{id}/recovery-recommendations` → the AI identifies the **missing capability (GIS)** and recommends an alternative: a GIS faculty at a different university, a GIS-capable student team and/or an industry partner with geospatial expertise.
4. Continue the project → PILOT → IMPLEMENTATION.

### Stage 9 — Implementation tracking
Implementation reaches 100% → dashboard shows the progress = 100%.

### Stage 10 — Impact + evidence verification
1. Team submits impact: beneficiaries 8500, 4 villages, ₹2.4 lakh/yr saved, 100%.
2. Team uploads evidence (before/after photos, reports) → status **PENDING**.
3. **Platform admin** reviews → **VERIFIED**.
4. Impact counters on the analytics dashboard only count **VERIFIED** records.

### Stage 11 — Verify in dashboards
As **admin@example.com** you see verified beneficiaries, implementation rate, category charts etc.

---

## Other seeded stories you can demo
- Floating villages IoT water-quality project (IMPLEMENTATION, industry IoT partner).
- Rural broadband / digital literacy (PILOT).
- Flood early-warning using GIS (PILOT failed → NEEDS_SUPPORT, used for recovery demo).
- Agricultural price transparency (IMPLEMENTED + VERIFIED impact).

## Suggested demo order (time-boxed)
1. Dashboard overview (2 min)
2. Problem submission + AI analysis + duplicates (3 min)
3. Matching + multi-stakeholder recommendation (3 min)
4. Project creation + lifecycle walk (2 min)
5. Failure recovery (2 min)
6. Impact verification + analytics (3 min)