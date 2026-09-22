# SETU — Societal Innovation Collaboration Platform

**SIH 2026 | Problem Statement: SIH26043**

SETU is an AI-assisted platform that connects **societal challenges with universities, student teams, mentors, and industry partners** to help move solutions from problem identification to implementation and measurable social impact.

##  Problem

Societal problems are often identified but lack a structured pathway to find the right technical capabilities, collaborators, industry support, and implementation resources.

##  Solution

SETU creates an end-to-end workflow:

**Problem → AI Analysis → Capability Decomposition → Matching → Collaboration → Prototype → Pilot → Implementation → Verified Impact**

##  Key Features

*  **AI Problem Intelligence** — classifies problems, identifies urgency, skills, and similar challenges.
*  **Capability Decomposition** — breaks complex problems into required technical capabilities.
*  **University & Student Matching** — recommends universities and student teams based on capabilities.
*  **Industry Matching** — identifies suitable industry partners for mentorship, hardware, funding, testing, or deployment.
*  **Multi-Stakeholder Collaboration** — combines universities, students, mentors, and industry partners.
*  **Implementation Tracking** — tracks projects from submission to implementation.
*  **Failure Recovery** — supports rematching when a pilot fails or a required capability is missing.
*  **Impact Dashboard** — tracks beneficiaries, outcomes, implementation progress, and supporting evidence.
*  **Evidence-Based Verification** — records evidence for claimed social impact.

##  Tech Stack

| Layer      | Technologies                                 |
| ---------- | -------------------------------------------- |
| Frontend   | React, Vite, Tailwind CSS, Recharts          |
| Backend    | Java, Spring Boot, REST API, Spring Security |
| Database   | MySQL                                        |
| AI Service | Python, FastAPI, NLP/ML                      |
| Tools      | Git, GitHub, Docker, Postman                 |

##  Run with Docker

### Prerequisites

* Docker Desktop
* Git

### Setup

```bash
git clone https://github.com/MuktiKumari983/setu.git
cd setu
docker compose up -d --build
```

Open:

```text
http://localhost:5173
```

The main services are:

```text
Frontend    → http://localhost:5173
Backend     → http://localhost:8080
AI Service  → http://localhost:8000
MySQL       → localhost:3307
```

##  Demo Account

```text
Email: citizen@example.com
Password: password123
```

##  Project Structure

```text
setu/
├── frontend/       # React frontend
├── backend/        # Spring Boot backend
├── ai-service/     # Python AI service
├── database/       # Database schema
├── docs/           # Project documentation
├── docker-compose.yml
└── CONTRIBUTING.md
```

## Documentation

* [Architecture](docs/architecture.md)
* [API Documentation](docs/api.md)
* [Database](docs/database.md)
* [Demo Flow](docs/demo-flow.md)
* [Contributing Guide](CONTRIBUTING.md)

##  Collaboration

SETU is designed for collaborative development using Git branches and pull requests.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing.

---

**Built for Smart India Hackathon 2026 — SIH26043**

> *From societal problems to collaborative solutions and measurable impact.*
