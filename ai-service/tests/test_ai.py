import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import pytest
from app.main import app
from fastapi.testclient import TestClient

client = TestClient(app)


def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"


def test_analyze_classifies_water():
    r = client.post("/analyze", json={
        "title": "River water contamination",
        "description": "The river near the village is contaminated with sewage and industrial effluent, "
                       "causing waterborne diseases in children.",
    })
    body = r.json()
    assert r.status_code == 200
    assert body["category"] == "Water Quality"
    assert body["urgency"] in ("HIGH", "MEDIUM")
    assert "IoT" in body["requiredSkills"] or "Sensors" in body["requiredSkills"]
    assert body["confidence"] > 0


def test_analyze_general_when_no_keywords():
    r = client.post("/analyze", json={
        "title": "Unusual request",
        "description": "We would like help with an unspecified matter that needs to be looked into.",
    })
    body = r.json()
    assert "General" in body["category"]


def test_duplicates_ranks_similar_first():
    existing = [
        {"id": 1, "title": "River contamination near Nagpur", "description": "sewage and industrial effluent in river"},
        {"id": 2, "title": "Solar street lights", "description": "street lighting maintenance"},
    ]
    r = client.post("/duplicates", json={
        "title": "River water pollution upstream",
        "description": "effluent discharge contaminating the river used for drinking",
        "existingProblems": existing,
    })
    body = r.json()
    assert body["similarProblems"][0]["id"] == 1
    assert body["similarProblems"][0]["similarity"] >= 0.25


def test_decompose_has_capabilities():
    r = client.post("/decompose", json={
        "title": "Flood early warning",
        "description": "flooding during monsoon, inundation of villages near the river, need early warning",
        "skills": [],
    })
    body = r.json()
    assert body["category"] == "Flood & Disaster"
    assert len(body["capabilities"]) >= 3
    first = body["capabilities"][0]
    assert {"capability", "description", "requiredSkills", "priority"} <= set(first.keys())


def test_match_scoring_skills():
    req = {
        "category": "Water Quality",
        "keywords": ["river", "contamination"],
        "requiredSkills": ["IoT", "GIS"],
        "capabilities": ["Sensor deployment"],
    }
    candidates = [
        {"id": 1, "name": "Team Alpha", "description": "water sensor specialists", "skills": ["IoT", "GIS", "Sensors"], "tags": ["water", "gis"], "capabilities": ["Sensor deployment"]},
        {"id": 2, "name": "Team Beta", "description": "web developers", "skills": ["Web Development"], "tags": ["web"], "capabilities": []},
    ]
    r = client.post("/match/university", json={"requirement": req, "candidates": candidates})
    body = r.json()
    results = body["results"]
    assert results[0]["id"] == 1
    assert results[0]["score"] > results[1]["score"]
    assert isinstance(results[0]["contributingFactors"], list)
    assert results[0]["contributingFactors"]


def test_recommend_returns_picks_and_narrative():
    req = {"category": "Education", "keywords": ["literacy"], "requiredSkills": ["Web Development", "Content Design"], "capabilities": []}
    payload = {
        "requirement": req,
        "universities": [{"id": 10, "name": "Anna Univ", "description": "e-learning lab", "skills": ["Web Development"], "tags": ["education"], "capabilities": []}],
        "teams": [{"id": 20, "name": "LearnBridge", "description": "built offline learning app", "skills": ["Web Development", "Content Design"], "tags": ["literacy"], "capabilities": []}],
        "mentors": [{"id": 30, "name": "Prof Rao", "description": "pedagogy expert", "skills": ["Content Design"], "tags": ["education"], "capabilities": []}],
        "industries": [],
    }
    r = client.post("/recommend", json=payload)
    body = r.json()
    assert len(body["stakeholders"]) == 3
    types = {s["type"] for s in body["stakeholders"]}
    assert types == {"university", "team", "mentor"}
    assert body["narrative"]


def test_recovery_finds_coverage():
    req = {"category": "Public Safety", "keywords": [], "requiredSkills": ["Computer Vision"], "capabilities": []}
    payload = {
        "missingCapability": "Computer Vision",
        "requirement": req,
        "universities": [],
        "teams": [
            {"id": 40, "name": "Vision Squad", "description": "cv engineers", "skills": ["Computer Vision", "Python"], "tags": ["cv"], "capabilities": []},
            {"id": 41, "name": "UI Team", "description": "frontend", "skills": ["Web Development"], "tags": ["web"], "capabilities": []},
        ],
        "mentors": [],
        "industries": [],
    }
    r = client.post("/recovery", json=payload)
    body = r.json()
    assert body["recommendations"][0]["missingGapClosed"] is True
    assert body["recommendations"][0]["id"] == 40