import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)

VALID_ANALYZE = {
    "title": "Water issue",
    "description": "River water is polluted",
}

VALID_REQUIREMENT = {
    "category": "Water Quality",
    "keywords": ["river"],
    "requiredSkills": ["IoT"],
    "capabilities": ["Sensors"],
}


def _candidate(candidate_id=1, name="Team Alpha", **overrides):
    candidate = {
        "id": candidate_id,
        "name": name,
        "description": "water sensor team",
        "skills": ["IoT"],
        "tags": ["river"],
        "capabilities": ["Sensors"],
        "availability": "",
        "experience": [],
    }
    candidate.update(overrides)
    return candidate


def _duplicates_payload(existing_problems=None, **overrides):
    payload = {
        "title": "River water pollution",
        "description": "Effluent is contaminating the river",
        "existingProblems": existing_problems or [],
    }
    payload.update(overrides)
    return payload


def _match_payload(candidates=None, requirement=None):
    return {
        "requirement": requirement or dict(VALID_REQUIREMENT),
        "candidates": candidates or [],
    }


def _stakeholder_payload(**overrides):
    payload = {
        "requirement": dict(VALID_REQUIREMENT),
        "universities": [],
        "teams": [],
        "mentors": [],
        "industries": [],
    }
    payload.update(overrides)
    return payload


def _recovery_payload(**overrides):
    payload = {
        "missingCapability": "IoT",
        "requirement": dict(VALID_REQUIREMENT),
        "universities": [],
        "teams": [],
        "mentors": [],
        "industries": [],
    }
    payload.update(overrides)
    return payload


@pytest.mark.parametrize(
    "field,length",
    [("title", 251), ("description", 10001), ("requiredOutcome", 1001)],
)
def test_analyze_rejects_oversized_fields(field, length):
    payload = dict(VALID_ANALYZE)
    payload[field] = "x" * length

    response = client.post("/analyze", json=payload)

    assert response.status_code == 422
    assert any(error["loc"][-1] == field for error in response.json()["detail"])


def test_analyze_accepts_maximum_lengths():
    payload = {
        "title": "t" * 250,
        "description": "d" * 10000,
        "requiredOutcome": "o" * 1000,
    }

    response = client.post("/analyze", json=payload)

    assert response.status_code == 200
    assert response.json()["source"] == "REMOTE"


@pytest.mark.parametrize(
    "payload",
    [
        [],
        {"title": ["Water issue"], "description": "River water is polluted"},
        {"title": "Water issue", "description": 1234},
        {"title": "Water issue", "description": "River water is polluted", "requiredOutcome": {"goal": "safe water"}},
    ],
    ids=["body-list", "title-list", "description-int", "outcome-dict"],
)
def test_analyze_rejects_wrong_json_types(payload):
    response = client.post("/analyze", json=payload)
    assert response.status_code == 422


@pytest.mark.parametrize("bad_id", [0, -1])
def test_duplicates_rejects_nonpositive_existing_id(bad_id):
    existing = [{"id": bad_id, "title": "River issue", "description": "pollution"}]

    response = client.post("/duplicates", json=_duplicates_payload(existing))

    assert response.status_code == 422
    assert any(error["loc"][-1] == "id" for error in response.json()["detail"])


def test_duplicates_rejects_too_many_existing_problems():
    existing = [
        {"id": index + 1, "title": f"Issue {index}", "description": "description"}
        for index in range(101)
    ]

    response = client.post("/duplicates", json=_duplicates_payload(existing))

    assert response.status_code == 422
    assert any(error["loc"][-1] == "existingProblems" for error in response.json()["detail"])


@pytest.mark.parametrize("field", ["title", "description"])
def test_duplicates_rejects_blank_request_fields(field):
    payload = _duplicates_payload()
    payload[field] = ""

    response = client.post("/duplicates", json=payload)

    assert response.status_code == 422
    assert any(error["loc"][-1] == field for error in response.json()["detail"])


def test_duplicates_rejects_blank_existing_title():
    existing = [{"id": 1, "title": "", "description": "valid description"}]

    response = client.post("/duplicates", json=_duplicates_payload(existing))

    assert response.status_code == 422
    assert any(error["loc"][-1] == "title" for error in response.json()["detail"])


@pytest.mark.parametrize("field", ["title", "description"])
def test_decompose_rejects_blank_request_fields(field):
    payload = {
        "title": "Flood early warning",
        "description": "Monsoon flooding near the river",
        "skills": [],
    }
    payload[field] = ""

    response = client.post("/decompose", json=payload)

    assert response.status_code == 422
    assert any(error["loc"][-1] == field for error in response.json()["detail"])


def test_decompose_rejects_too_many_skills():
    payload = {
        "title": "Flood early warning",
        "description": "Monsoon flooding near the river",
        "skills": ["engineering"] * 51,
    }

    response = client.post("/decompose", json=payload)

    assert response.status_code == 422
    assert any(error["loc"][-1] == "skills" for error in response.json()["detail"])


def test_match_rejects_negative_candidate_id():
    candidates = [_candidate(candidate_id=-5)]

    response = client.post("/match/TEAM", json=_match_payload(candidates))

    assert response.status_code == 422
    assert any(error["loc"][-1] == "id" for error in response.json()["detail"])


def test_match_rejects_empty_candidate_name():
    candidates = [_candidate(name="")]

    response = client.post("/match/TEAM", json=_match_payload(candidates))

    assert response.status_code == 422
    assert any(error["loc"][-1] == "name" for error in response.json()["detail"])


def test_match_rejects_too_many_candidates():
    candidates = [_candidate(index + 1, f"Team {index}") for index in range(501)]

    response = client.post("/match/TEAM", json=_match_payload(candidates))

    assert response.status_code == 422
    assert any(error["loc"][-1] == "candidates" for error in response.json()["detail"])


@pytest.mark.parametrize(
    "bad_requirement",
    [
        ["Water Quality"],
        {"keywords": "river"},
        {"requiredSkills": {"IoT": True}},
        {"capabilities": 123},
        {"category": ["Water Quality"]},
    ],
    ids=["requirement-list", "keywords-string", "skills-dict", "capabilities-int", "category-list"],
)
def test_match_rejects_bad_requirement_field_types(bad_requirement):
    response = client.post("/match/TEAM", json=_match_payload(requirement=bad_requirement))
    assert response.status_code == 422


@pytest.mark.parametrize("match_type", ["UNIVERSITY", "TEAM", "MENTOR", "INDUSTRY"])
def test_match_accepts_all_canonical_types(match_type):
    response = client.post(f"/match/{match_type}", json=_match_payload())

    assert response.status_code == 200
    assert response.json() == {
        "matchType": match_type,
        "results": [],
        "source": "REMOTE",
    }


@pytest.mark.parametrize("endpoint", ["/recommend", "/recovery"])
@pytest.mark.parametrize("group", ["universities", "teams", "mentors", "industries"])
def test_stakeholder_endpoints_reject_oversized_groups(endpoint, group):
    payload = _stakeholder_payload() if endpoint == "/recommend" else _recovery_payload()
    payload[group] = [_candidate(index + 1, f"Team {index}") for index in range(201)]

    response = client.post(endpoint, json=payload)

    assert response.status_code == 422
    assert any(error["loc"][-1] == group for error in response.json()["detail"])


@pytest.mark.parametrize(
    "payload",
    [
        [],
        {},
        {"requirement": ["Water Quality"]},
        {"requirement": dict(VALID_REQUIREMENT), "universities": {}},
        {"requirement": dict(VALID_REQUIREMENT), "teams": ["not a candidate"]},
        {"requirement": dict(VALID_REQUIREMENT), "mentors": [{"id": 1, "name": ""}]},
    ],
    ids=["body-list", "missing-requirement", "requirement-list", "universities-dict", "teams-strings", "blank-mentor-name"],
)
def test_recommend_rejects_invalid_payload_shapes(payload):
    response = client.post("/recommend", json=payload)
    assert response.status_code == 422


@pytest.mark.parametrize(
    "payload",
    [
        [],
        {"requirement": dict(VALID_REQUIREMENT)},
        {"missingCapability": ["IoT"], "requirement": dict(VALID_REQUIREMENT)},
        {"missingCapability": "IoT", "requirement": ["Water Quality"]},
        {"missingCapability": "IoT", "requirement": dict(VALID_REQUIREMENT), "industries": {}},
        {"missingCapability": "IoT", "requirement": dict(VALID_REQUIREMENT), "universities": ["not a candidate"]},
    ],
    ids=["body-list", "missing-capability", "capability-list", "requirement-list", "industries-dict", "universities-strings"],
)
def test_recovery_rejects_invalid_payload_shapes(payload):
    response = client.post("/recovery", json=payload)
    assert response.status_code == 422
