import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import engine
from app.knowledge import GENERAL_CATEGORY


def test_tokenize_handles_none_and_empty():
    assert engine.tokenize(None) == []
    assert engine.tokenize("") == []


def test_cosine_empty_vectors_zero():
    assert engine.cosine({}, {}) == 0.0
    assert engine.cosine({"water": 1.0}, {}) == 0.0


def test_jaccard_empty_sets_zero():
    assert engine.jaccard(set(), set()) == 0.0
    assert engine.jaccard(set(), {"water"}) == 0.0


def test_detect_category_generic_fallback():
    category, keywords, confidence = engine.detect_category("Unusual request")

    assert category == GENERAL_CATEGORY
    assert keywords == []
    assert confidence == 0.35


def test_score_candidate_clamps_to_maximum(monkeypatch):
    monkeypatch.setattr(engine, "jaccard", lambda a, b: 2.0)
    monkeypatch.setattr(engine, "cosine", lambda a, b: 2.0)

    result = engine.score_candidate(
        {
            "category": "Water Quality",
            "keywords": ["river"],
            "requiredSkills": ["IoT"],
            "capabilities": ["Sensors"],
        },
        {
            "id": 1,
            "name": "River Team",
            "description": "river sensors",
            "skills": ["IoT"],
            "tags": ["river"],
            "capabilities": ["Sensors"],
            "availability": "Weekends",
            "experience": ["river sensors"],
        },
    )

    assert result["score"] == 0.99
