"""AI service - deterministic NLP for problem analysis and stakeholder matching.

FastAPI application exposing the contract expected by the Java gateway
(RemoteAiGateway). All endpoints are synchronous and dependency-free apart
from FastAPI/Pydantic so the container stays tiny.
"""

import logging
import os
from typing import Any, Literal

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

EntityType = Literal["UNIVERSITY", "TEAM", "MENTOR", "INDUSTRY"]
Urgency = Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"]
RemoteSource = Literal["REMOTE"]


class AnalyzeRequest(BaseModel):
    title: str = Field(min_length=1, max_length=250)
    description: str = Field(min_length=1, max_length=10000)
    requiredOutcome: str = Field(default="", max_length=1000)


class ExistingProblem(BaseModel):
    id: int = Field(gt=0)
    title: str = Field(min_length=1, max_length=250)
    description: str = Field(default="", max_length=10000)


class DuplicatesRequest(BaseModel):
    title: str = Field(min_length=1, max_length=250)
    description: str = Field(min_length=1, max_length=10000)
    existingProblems: list[ExistingProblem] = Field(default_factory=list, max_length=100)


class DecomposeRequest(BaseModel):
    title: str = Field(min_length=1, max_length=250)
    description: str = Field(min_length=1, max_length=10000)
    skills: list[str] = Field(default_factory=list, max_length=50)


class CandidateProfile(BaseModel):
    id: int = Field(gt=0)
    name: str = Field(min_length=1, max_length=200)
    description: str = Field(default="", max_length=2000)
    skills: list[str] = Field(default_factory=list, max_length=50)
    tags: list[str] = Field(default_factory=list, max_length=50)
    capabilities: list[str] = Field(default_factory=list, max_length=50)
    availability: str = Field(default="", max_length=200)
    experience: list[str] = Field(default_factory=list, max_length=50)


class Requirement(BaseModel):
    category: str = Field(default="", max_length=150)
    keywords: list[str] = Field(default_factory=list, max_length=50)
    requiredSkills: list[str] = Field(default_factory=list, max_length=50)
    capabilities: list[str] = Field(default_factory=list, max_length=50)


class MatchRequest(BaseModel):
    requirement: Requirement
    candidates: list[CandidateProfile] = Field(default_factory=list, max_length=500)


class RecommendRequest(BaseModel):
    requirement: Requirement
    universities: list[CandidateProfile] = Field(default_factory=list, max_length=200)
    teams: list[CandidateProfile] = Field(default_factory=list, max_length=200)
    mentors: list[CandidateProfile] = Field(default_factory=list, max_length=200)
    industries: list[CandidateProfile] = Field(default_factory=list, max_length=200)


class RecoveryRequest(BaseModel):
    missingCapability: str = Field(min_length=1, max_length=200)
    requirement: Requirement
    universities: list[CandidateProfile] = Field(default_factory=list, max_length=200)
    teams: list[CandidateProfile] = Field(default_factory=list, max_length=200)
    mentors: list[CandidateProfile] = Field(default_factory=list, max_length=200)
    industries: list[CandidateProfile] = Field(default_factory=list, max_length=200)


class AnalyzeResponse(BaseModel):
    category: str
    urgency: Urgency
    requiredSkills: list[str]
    keywords: list[str]
    confidence: float = Field(ge=0, le=1)
    explanation: str
    rootCauses: list[str]
    targetBeneficiaries: list[str]
    challenges: list[str]
    solutionDirections: list[str]
    expectedOutcomes: list[str]
    source: RemoteSource = "REMOTE"


class SimilarProblemResponse(BaseModel):
    id: int = Field(gt=0)
    title: str
    similarity: float = Field(ge=0, le=1)
    source: RemoteSource = "REMOTE"


class DuplicateResponse(BaseModel):
    similarProblems: list[SimilarProblemResponse]
    source: RemoteSource = "REMOTE"


class CapabilityResponse(BaseModel):
    capability: str
    description: str
    requiredSkills: list[str]
    priority: str
    source: RemoteSource = "REMOTE"


class DecomposeResponse(BaseModel):
    capabilities: list[CapabilityResponse]
    category: str
    source: RemoteSource = "REMOTE"


class ScoredMatchResponse(BaseModel):
    id: int = Field(gt=0)
    score: float = Field(ge=0, le=1)
    explanation: str
    contributingFactors: list[str]
    source: RemoteSource = "REMOTE"


class MatchResponse(BaseModel):
    matchType: EntityType
    results: list[ScoredMatchResponse]
    source: RemoteSource = "REMOTE"


class StakeholderPickResponse(BaseModel):
    type: EntityType
    id: int = Field(gt=0)
    name: str
    score: float = Field(ge=0, le=1)
    reason: str
    contribution: str
    gapSkills: list[str]
    details: dict[str, Any]
    source: RemoteSource = "REMOTE"


class RecommendationResponse(BaseModel):
    stakeholders: list[StakeholderPickResponse]
    narrative: str
    source: RemoteSource = "REMOTE"


class RecoveryPickResponse(BaseModel):
    entityType: EntityType
    id: int = Field(gt=0)
    name: str
    score: float = Field(ge=0, le=1)
    reason: str
    missingGapClosed: bool
    source: RemoteSource = "REMOTE"


class RecoveryResponse(BaseModel):
    recommendations: list[RecoveryPickResponse]
    narrative: str
    source: RemoteSource = "REMOTE"


BENEFICIARY_TERMS = {
    "children": ("child", "children"),
    "students": ("student", "students"),
    "farmers": ("farmer", "farmers"),
    "villagers": ("villager", "villagers"),
    "residents": ("resident", "residents"),
    "women": ("woman", "women"),
    "elderly": ("elderly", "senior citizen"),
    "patients": ("patient", "patients"),
    "workers": ("worker", "workers"),
    "commuters": ("commuter", "commuters"),
}


def structured_analysis_fields(text: str, category: str, keywords: list[str], skills: list[str],
                               required_outcome: str) -> dict[str, list[str]]:
    lower = text.lower()
    root_causes = [f"Reported signal: {keyword}" for keyword in keywords[:5]]
    challenges = [f"Reported condition: {keyword}" for keyword in keywords[:5]]
    beneficiaries = [
        label for label, terms in BENEFICIARY_TERMS.items()
        if any(term in lower for term in terms)
    ]
    template = DECOMPOSITION_TEMPLATES.get(category, FALLBACK_DECOMPOSITION)
    solutions = [f"Assess capability: {row[0]}" for row in template[:5]]
    outcomes = (
        [f"Stated outcome: {required_outcome.strip()}"]
        if required_outcome and required_outcome.strip() else []
    )
    return {
        "rootCauses": root_causes or ["Not stated in submission"],
        "targetBeneficiaries": beneficiaries or ["Not specified in submission"],
        "challenges": challenges or ["Not stated in submission"],
        "solutionDirections": solutions,
        "expectedOutcomes": outcomes or ["Not specified in submission"],
    }


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(req: AnalyzeRequest):
    text = f"{req.title}. {req.description}"
    category, keywords, conf = engine.detect_category(text)
    urgency, urgency_conf = engine.detect_urgency(text)
    skills = engine.extract_skills(req.title, req.description, category)
    all_keywords = keywords + engine.extract_keywords(text, category)
    seen = set()
    keywords_out = [k for k in all_keywords if not (k in seen or seen.add(k))]
    structured = structured_analysis_fields(text, category, keywords, skills, req.requiredOutcome)
    return {
        "category": category,
        "urgency": urgency,
        "requiredSkills": skills,
        "keywords": keywords_out[:8],
        "confidence": round(min(conf, urgency_conf), 3),
        "explanation": engine.explain_category(category, keywords[:5]),
        **structured,
        "source": "REMOTE",
    }


@app.post("/duplicates", response_model=DuplicateResponse)
def duplicates(req: DuplicatesRequest):
    vec = engine.tf_vector(f"{req.title}. {req.description}")
    results = []
    for existing in req.existingProblems:
        other = engine.tf_vector(f"{existing.title}. {existing.description}")
        sim = engine.cosine(vec, other)
        if sim >= 0.25:
            results.append({"id": existing.id, "title": existing.title, "similarity": round(sim, 2), "source": "REMOTE"})
    results.sort(key=lambda r: r["similarity"], reverse=True)
    return {"similarProblems": results[:5], "source": "REMOTE"}


@app.post("/decompose", response_model=DecomposeResponse)
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
            "source": "REMOTE",
        })
    return {"capabilities": capabilities, "category": category, "source": "REMOTE"}


@app.post("/match/{match_type}", response_model=MatchResponse)
def match(match_type: EntityType, req: MatchRequest):
    requirement = req.requirement.model_dump()
    results = [
        engine.score_candidate(requirement, c.model_dump())
        for c in req.candidates
    ]
    enriched = []
    for c, scored in zip(req.candidates, results):
        scored = dict(scored)
        scored["id"] = c.id
        scored["source"] = "REMOTE"
        enriched.append(scored)
    ranked = engine.sort_scores(enriched)[:10]
    return {"matchType": match_type, "results": ranked, "source": "REMOTE"}


def _stakeholder_row(pick_type: EntityType, candidate: CandidateProfile, requirement: dict) -> dict:
    scored = engine.score_candidate(requirement, candidate.model_dump())
    req_skills = set(x.lower() for x in requirement.get("requiredSkills", []))
    cand_skills = set(x.lower() for x in (candidate.skills or []))
    gap = sorted(req_skills - cand_skills)
    details = {
        "name": candidate.name,
        "skills": candidate.skills or [],
        "tags": candidate.tags or [],
        "availability": candidate.availability,
        "experience": candidate.experience,
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
        "source": "REMOTE",
    }


@app.post("/recommend", response_model=RecommendationResponse)
def recommend(req: RecommendRequest):
    requirement = req.requirement.model_dump()
    picks = []
    for pick_type, group in (
        ("UNIVERSITY", req.universities),
        ("TEAM", req.teams),
        ("MENTOR", req.mentors),
        ("INDUSTRY", req.industries),
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
    return {"stakeholders": picks, "narrative": narrative, "source": "REMOTE"}


@app.post("/recovery", response_model=RecoveryResponse)
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
            "source": "REMOTE",
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
    return {"recommendations": recommendations[:8], "narrative": narrative, "source": "REMOTE"}


if __name__ == "__main__":
    import uvicorn

    port = int(os.environ.get("PORT", "8000"))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=False)
