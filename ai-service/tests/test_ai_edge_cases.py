import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from fastapi.testclient import TestClient

from app.knowledge import FALLBACK_DECOMPOSITION, GENERAL_CATEGORY
from app.main import app

client = TestClient(app)


def test_duplicates_empty_list_returns_empty_results():
    response = client.post("/duplicates", json={
        "title": "River water pollution",
        "description": "Effluent is contaminating the river",
        "existingProblems": [],
    })

    assert response.status_code == 200
    assert response.json() == {"similarProblems": [], "source": "REMOTE"}


def test_duplicates_all_low_similarity_returns_empty_results():
    response = client.post("/duplicates", json={
        "title": "River water contamination",
        "description": "Sewage effluent in the river",
        "existingProblems": [
            {
                "id": 9,
                "title": "Solar street lighting",
                "description": "Photovoltaic panels on city roads",
            }
        ],
    })

    assert response.status_code == 200
    assert response.json()["similarProblems"] == []


def test_duplicates_filters_low_similarity_results():
    response = client.post("/duplicates", json={
        "title": "River water contamination",
        "description": "Sewage effluent in the river",
        "existingProblems": [
            {
                "id": 1,
                "title": "River water contamination cleanup",
                "description": "Sewage effluent in river water",
            },
            {
                "id": 2,
                "title": "Solar street lighting",
                "description": "Photovoltaic panels on city roads",
            },
        ],
    })

    body = response.json()
    assert response.status_code == 200
    assert [item["id"] for item in body["similarProblems"]] == [1]
    assert body["similarProblems"][0]["similarity"] >= 0.25
    assert body["source"] == "REMOTE"


def test_match_empty_candidates_returns_empty_results():
    response = client.post("/match/TEAM", json={
        "requirement": {"category": "Water Quality", "requiredSkills": ["IoT"]},
        "candidates": [],
    })

    assert response.status_code == 200
    assert response.json() == {
        "matchType": "TEAM",
        "results": [],
        "source": "REMOTE",
    }


def test_recommend_empty_groups_returns_empty_stakeholders():
    response = client.post("/recommend", json={
        "requirement": {"category": "Water Quality", "requiredSkills": ["IoT"]},
    })

    body = response.json()
    assert response.status_code == 200
    assert body["stakeholders"] == []
    assert body["source"] == "REMOTE"
    assert body["narrative"] == (
        "Recommended the strongest stakeholders per type. "
        "Open gap(s) remain: iot — add a focused mentor or industry with those skills."
    )


def test_recommend_empty_groups_without_skills_reports_full_coverage():
    response = client.post("/recommend", json={
        "requirement": {"category": "Water Quality"},
    })

    assert response.status_code == 200
    assert response.json() == {
        "stakeholders": [],
        "narrative": "Recommended the strongest stakeholders per type. All required capabilities are covered.",
        "source": "REMOTE",
    }


def test_recovery_empty_groups_returns_empty_recommendations():
    response = client.post("/recovery", json={
        "missingCapability": "IoT",
        "requirement": {"category": "Water Quality", "requiredSkills": ["IoT"]},
    })

    body = response.json()
    assert response.status_code == 200
    assert body["recommendations"] == []
    assert body["source"] == "REMOTE"
    assert body["narrative"].startswith("No candidate directly covers 'IoT'.")


def test_analyze_no_keywords_returns_structured_fallback_fields():
    response = client.post("/analyze", json={
        "title": "Unusual request",
        "description": "We would like help with an unspecified matter that needs to be looked into.",
    })

    body = response.json()
    assert response.status_code == 200
    assert body["category"] == GENERAL_CATEGORY
    assert body["urgency"] == "LOW"
    assert body["confidence"] == 0.35
    assert body["rootCauses"] == ["Not stated in submission"]
    assert body["challenges"] == ["Not stated in submission"]
    assert body["targetBeneficiaries"] == ["Not specified in submission"]
    assert body["expectedOutcomes"] == ["Not specified in submission"]
    assert body["solutionDirections"] == [f"Assess capability: {row[0]}" for row in FALLBACK_DECOMPOSITION]
    assert body["explanation"].startswith("Text did not strongly match a specific challenge category")
    assert isinstance(body["keywords"], list)
    assert body["source"] == "REMOTE"
