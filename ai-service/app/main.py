"""AI service - deterministic NLP for problem analysis and stakeholder matching.

FastAPI application exposing the contract expected by the Java gateway
(RemoteAiGateway). All endpoints are synchronous and dependency-free apart
from FastAPI/Pydantic so the container stays tiny.
"""

import logging
import os

from fastapi import FastAPI
from pydantic import BaseModel, Field

from . import engine
from .knowledge import (
    DECOMPOSITION_TEMPLATES,
    FALLBACK_DECOMPOSITION,
    GENERAL_CATEGORY,
)

logger = logging.getLogger("ai-service")
logging.basicConfig(level=logging.INFO)

app = FastAPI(title="SIH Citizen Innovation - AI service", version="1.0.0")


class AnalyzeRequest(BaseModel):
    title: str
    description: str


class ExistingProblem(BaseModel):
    id: int
    title: str
    description: str = ""


class DuplicatesRequest(BaseModel):
    title: str
    description: str
    existingProblems: list[ExistingProblem] = []


class DecomposeRequest(BaseModel):
    title: str
    description: str
    skills: list[str] = []


class CandidateProfile(BaseModel):
    id: int
    name: str
    description: str = ""
    skills: list[str] = []
    tags: list[str] = []
    capabilities: list[str] = []


class Requirement(BaseModel):
    category: str = ""
    keywords: list[str] = []
    requiredSkills: list[str] = []
    capabilities: list[str] = []


class MatchRequest(BaseModel):
    requirement: Requirement
    candidates: list[CandidateProfile] = []


class RecommendRequest(BaseModel):
    requirement: Requirement
    universities: list[CandidateProfile] = []
    teams: list[CandidateProfile] = []
    mentors: list[CandidateProfile] = []
    industries: list[CandidateProfile] = []


class RecoveryRequest(BaseModel):
    missingCapability: str
    requirement: Requirement
    universities: list[CandidateProfile] = []
    teams: list[CandidateProfile] = []
    mentors: list[CandidateProfile] = []
    industries: list[CandidateProfile] = []


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze")
def analyze(req: AnalyzeRequest):
    text = f"{req.title}. {req.description}"
    category, keywords, conf = engine.detect_category(text)
    urgency, urgency_conf = engine.detect_urgency(text)
    skills = engine.extract_skills(req.title, req.description, category)
    all_keywords = keywords + engine.extract_keywords(text, category)
    # de-duplicate while keeping order
    seen = set()
    keywords_out = [k for k in all_keywords if not (k in seen or seen.add(k))]
    return {
        "category": category,
        "urgency": urgency,
        "requiredSkills": skills,
        "keywords": keywords_out[:8],
        "confidence": round(min(conf, urgency_conf), 3),
        "explanation": engine.explain_category(category, keywords[:5]),
    }


@app.post("/duplicates")
def duplicates(req: DuplicatesRequest):
    vec = engine.tf_vector(f"{req.title}. {req.description}")
    results = []
    for existing in req.existingProblems:
        other = engine.tf_vector(f"{existing.title}. {existing.description}")
        sim = engine.cosine(vec, other)
        if sim >= 0.25:
            results.append({"id": existing.id, "title": existing.title, "similarity": round(sim, 2)})
    results.sort(key=lambda r: r["similarity"], reverse=True)
    return {"similarProblems": results[:5]}


@app.post("/decompose")
def decompose(req: DecomposeRequest):
    text = f"{req.title}. {req.description}".lower()
    category, _, _ = engine.detect_category(text)
    template = DECOMPOSITION_TEMPLATES.get(category, FALLBACK_DECOMPOSITION)
    capabilities = []
    for name, skills_csv, description, priority in template:
        required = [s.strip() for s in skills_csv.split(",")]
        capabilities.append({
            "capability": name,
            "description": description,
            "requiredSkills": required,
            "priority": priority,
        })
    return {"capabilities": capabilities, "category": category}


@app.post("/match/{match_type}")
def match(match_type: str, req: MatchRequest):
    requirement = req.requirement.model_dump()
    results = [
        engine.score_candidate(requirement, c.model_dump())
        for c in req.candidates
    ]
    enriched = []
    for c, scored in zip(req.candidates, results):
        scored = dict(scored)
        scored["id"] = c.id
        enriched.append(scored)
    # names are already embedded in explanations
    ranked = engine.sort_scores(enriched)[:10]
    return {"matchType": match_type, "results": ranked}


def _stakeholder_row(pick_type: str, candidate: CandidateProfile, requirement: dict) -> dict:
    scored = engine.score_candidate(requirement, candidate.model_dump())
    req_skills = set(x.lower() for x in requirement.get("requiredSkills", []))
    cand_skills = set(x.lower() for x in (candidate.skills or []))
    gap = sorted(req_skills - cand_skills)
    details = {
        "name": candidate.name,
        "skills": candidate.skills or [],
        "tags": candidate.tags or [],
        "status": "candidate",
    }
    return {
        "type": pick_type,
        "id": candidate.id,
        "name": candidate.name,
        "score": scored["score"],
        "reason": scored["explanation"],
        "contribution": (
            f"Contributes {', '.join(candidate.skills) if candidate.skills else 'domain knowledge'}"
        ),
        "gapSkills": gap,
        "details": details,
    }


@app.post("/recommend")
def recommend(req: RecommendRequest):
    requirement = req.requirement.model_dump()
    picks = []
    for pick_type, group in (
        ("university", req.universities),
        ("team", req.teams),
        ("mentor", req.mentors),
        ("industry", req.industries),
    ):
        if not group:
            continue
        ranked = sorted(group, key=lambda c: engine.score_candidate(requirement, c.model_dump())["score"], reverse=True)
        picks.append(_stakeholder_row(pick_type, ranked[0], requirement))

    gaps = engine.missing_gaps(requirement, [c.model_dump() for c in req.universities + req.teams + req.mentors + req.industries])
    if gaps:
        narrative = (
            f"Recommended the strongest stakeholders per type. "
            f"Open gap(s) remain: {', '.join(gaps)} — add a focused mentor or industry with those skills."
        )
    else:
        narrative = "Recommended the strongest stakeholders per type. All required capabilities are covered."
    return {"stakeholders": picks, "narrative": narrative}


@app.post("/recovery")
def recovery(req: RecoveryRequest):
    requirement = req.requirement.model_dump()
    requirement["requiredSkills"] = [req.missingCapability]
    requirement["keywords"] = []
    requirement["capabilities"] = []
    all_candidates = (
        [("UNIVERSITY", c) for c in req.universities]
        + [("TEAM", c) for c in req.teams]
        + [("MENTOR", c) for c in req.mentors]
        + [("INDUSTRY", c) for c in req.industries]
    )
    recommendations = []
    for entity_type, c in all_candidates:
        scored = engine.score_candidate(requirement, c.model_dump())
        skills = set(x.lower() for x in (c.skills or []))
        target = req.missingCapability.lower()
        closed = any(target in s or s in target for s in skills)
        recommendations.append({
            "entityType": entity_type,
            "id": c.id,
            "name": c.name,
            "score": max(scored["score"], 0.6) if closed else scored["score"],
            "reason": (
                f"'{req.missingCapability}' directly covered by {c.name}"
                if closed else "partial/adjacent coverage — consider a knowledge transfer plan"
            ),
            "missingGapClosed": closed,
        })
    recommendations.sort(key=lambda r: r["score"], reverse=True)
    top_closed = [r for r in recommendations if r["missingGapClosed"]]
    if top_closed:
        narrative = (
            f"Best recovery path: work with {top_closed[0]['name']} ({top_closed[0]['entityType']}) "
            f"to close '{req.missingCapability}'. The needed capability exists outside the current team."
        )
    else:
        narrative = (
            f"No candidate directly covers '{req.missingCapability}'. "
            f"Recommended order combines the closest skill sets for a short knowledge-transfer sprint."
        )
    return {"recommendations": recommendations[:8], "narrative": narrative}


if __name__ == "__main__":
    import uvicorn

    port = int(os.environ.get("PORT", "8000"))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=False)