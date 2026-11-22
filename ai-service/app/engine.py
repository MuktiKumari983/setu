"""Pure-Python NLP helpers: tokenisation, weighted TF vectors and cosine
similarity, plus deterministic rules for categorisation and matching."""

import math
import re

from .knowledge import (
    CATEGORIES,
    CATEGORY_SKILLS,
    DECOMPOSITION_TEMPLATES,
    FALLBACK_DECOMPOSITION,
    GENERAL_CATEGORY,
    SKILL_SYNONYMS,
    STOPWORDS,
    URGENCY_HIGH,
    URGENCY_MEDIUM,
)

_TOKEN_RE = re.compile(r"[a-z0-9]+", re.IGNORECASE)
_SUFFIXES = ("ing", "ed", "es", "s", "tion", "ment")

# Fixed corpus-size assumption for the idf term so vectors are computed in one pass.
_N_DOCS = 120.0

# Domain-frequency table (fraction of imaginary corpus documents containing the term).
_DOCFREQ = {
    "water": 0.18, "waste": 0.15, "health": 0.16, "school": 0.12, "solar": 0.10,
    "farmer": 0.12, "traffic": 0.10, "flood": 0.10, "internet": 0.12, "housing": 0.10,
    "pollution": 0.12, "sensor": 0.12, "data": 0.25, "river": 0.08, "village": 0.18,
    "city": 0.16, "road": 0.10, "power": 0.10, "crop": 0.08, "literacy": 0.10,
    "child": 0.10, "woman": 0.10, "safety": 0.10, "grid": 0.08, "monsoon": 0.06,
    "drainage": 0.07, "digital": 0.10, "municipal": 0.08, "citizen": 0.18,
    "community": 0.25, "district": 0.15, "state": 0.18, "government": 0.15,
}


def tokenize(text: str) -> list[str]:
    """Lower-case tokens with light suffix stripping and stopword removal."""
    if not text:
        return []
    tokens = _TOKEN_RE.findall(text.lower())
    out = []
    for t in tokens:
        word = t
        for suffix in _SUFFIXES:
            if len(word) > 5 and word.endswith(suffix):
                word = word[: -len(suffix)]
                break
        if word not in STOPWORDS and len(word) >= 3:
            out.append(word)
    return out


def _idf(term: str) -> float:
    df = _DOCFREQ.get(term, 0.10)
    return math.log((1.0 + _N_DOCS) / (1.0 + _N_DOCS * df)) + 1.0


def tf_vector(text: str) -> dict[str, float]:
    """Term-frequency vector weighted by idf."""
    counts: dict[str, int] = {}
    for token in tokenize(text):
        counts[token] = counts.get(token, 0) + 1
    total = sum(counts.values()) or 1
    return {term: (count / total) * _idf(term) for term, count in counts.items()}


def _norm(vector: dict[str, float]) -> float:
    return math.sqrt(sum(v * v for v in vector.values()))


def cosine(a: dict[str, float], b: dict[str, float]) -> float:
    denom = _norm(a) * _norm(b)
    if denom == 0.0:
        return 0.0
    return sum((a.get(t, 0.0) * b.get(t, 0.0)) for t in set(a) | set(b)) / denom


def jaccard(a: set, b: set) -> float:
    if not a and not b:
        return 0.0
    union = a | b
    if not union:
        return 0.0
    return len(a & b) / len(union)


# ---------------------------------------------------------------------------
# Analysis
# ---------------------------------------------------------------------------

def detect_category(text: str) -> tuple[str, list[str], float]:
    """Return (category, matched keywords, confidence)."""
    lower = text.lower()
    best, best_kw, best_score = GENERAL_CATEGORY, [], 0.0
    for category, keywords in CATEGORIES.items():
        hits = [kw for kw in keywords if kw in lower]
        # Longer, more specific keyword matches carry more weight.
        score = sum(0.6 + min(len(kw.split()), 3) * 0.4 for kw in hits)
        if score > best_score:
            best, best_kw, best_score = category, hits, score
    confidence = min(0.97, 0.35 + best_score * 0.13)
    if best_kw:
        confidence = max(confidence, 0.5)
    else:
        confidence = 0.35
    return best, best_kw, round(confidence, 3)


def detect_urgency(text: str) -> tuple[str, float]:
    lower = text.lower()
    high = sum(1 for kw in URGENCY_HIGH if kw in lower)
    medium = sum(1 for kw in URGENCY_MEDIUM if kw in lower)
    if high >= 2 or (high >= 1 and medium >= 2):
        return "HIGH", round(min(0.98, 0.7 + high * 0.06 + medium * 0.02), 3)
    if high >= 1 or medium >= 3:
        return "MEDIUM", round(0.6 + high * 0.05 + medium * 0.02, 3)
    return "LOW", 0.45


def extract_skills(title: str, description: str, category: str) -> list[str]:
    text = (title + " " + description).lower()
    base = list(CATEGORY_SKILLS.get(category, CATEGORY_SKILLS.get(GENERAL_CATEGORY, [])) or [])
    extras = [
        skill for skill, syns in SKILL_SYNONYMS.items()
        if any(syn in text for syn in syns) and skill not in base
    ]
    return (base + extras)[:6]


def extract_keywords(text: str, category: str) -> list[str]:
    tokens = [t for t in tokenize(text)]
    seen: list[str] = []
    for token in tokens:
        if token not in seen and len(token) > 3:
            seen.append(token)
        if len(seen) >= 8:
            break
    return seen


def explain_category(category: str, keywords: list[str]) -> str:
    if not keywords:
        return (
            f"Text did not strongly match a specific challenge category; "
            f"tagged as '{category}'. Add domain keywords for a more confident tag."
        )
    return (
        f"Matched '{category}' based on keyword(s): {', '.join(keywords[:5])}. "
        f"The classifier weighs specific multi-word phrases highest."
    )


# ---------------------------------------------------------------------------
# Matching / recommendation building blocks
# ---------------------------------------------------------------------------

def _norm_lower(value) -> list[str]:
    if not value:
        return []
    if isinstance(value, list):
        return [str(v).strip().lower() for v in value]
    return [str(value).strip().lower()]


def score_candidate(requirement: dict, candidate: dict) -> dict:
    """Composite relevance score for one candidate profile.

    Returns {score, explanation, contributingFactors}."""
    req_skills = set(_norm_lower(requirement.get("requiredSkills")))
    req_kw = set(_norm_lower(requirement.get("keywords")))
    req_caps = set(_norm_lower(requirement.get("capabilities")))
    category = (requirement.get("category") or "").lower()

    c_skills = set(_norm_lower(candidate.get("skills")))
    c_tags = set(_norm_lower(candidate.get("tags")))
    c_caps = set(_norm_lower(candidate.get("capabilities")))
    c_text = (
        " ".join([
            str(candidate.get("name") or ""),
            str(candidate.get("description") or ""),
            " ".join(str(v) for v in (candidate.get("skills") or [])),
            " ".join(str(v) for v in (candidate.get("tags") or [])),
        ])
        + " " + (category or "")
    )
    c_vec = tf_vector(c_text)

    factors: list[tuple[float, str]] = []

    skill_j = jaccard(req_skills, c_skills)
    skill_hit = (req_skills & c_skills)
    if req_skills and not skill_hit:
        skill_j *= 0.6  # missing required skills penalise hard
    factors.append((skill_j, "Skill overlap"))

    tag_j = jaccard(req_kw, c_tags)
    factors.append((tag_j, "Keyword alignment"))

    cap_j = jaccard(req_caps, c_caps)
    factors.append((cap_j * 0.5, "Capability coverage"))

    desc_sim = 0.0
    if req_kw:
        req_vec = tf_vector(" ".join(req_kw))
        desc_sim = cosine(req_vec, c_vec)
    factors.append((desc_sim * 0.8, "Description relevance"))

    availability_signal = 1.0 if str(candidate.get("availability") or "").strip() else 0.0
    factors.append((availability_signal, "Availability recorded"))

    experience_text = " ".join(str(v) for v in (candidate.get("experience") or []))
    experience_query = " ".join(req_kw) or " ".join(req_skills)
    exp_sim = cosine(tf_vector(experience_query), tf_vector(experience_text)) if experience_query else 0.0
    factors.append((exp_sim, "Relevant experience"))

    weights = [0.35, 0.20, 0.12, 0.15, 0.08, 0.10]
    score = sum(w * f for (f, _), w in zip(factors, weights))
    score = max(0.0, min(0.99, round(score, 4)))

    matched = sorted(skill_hit) if skill_hit else []
    explanation_parts = []
    if matched:
        explanation_parts.append(f"matched skills: {', '.join(matched)}")
    elif req_skills:
        explanation_parts.append("no required skills matched directly")
    if tag_j > 0:
        explanation_parts.append("domain tags overlap")
    if desc_sim > 0.2:
        explanation_parts.append("description references the problem domain")
    if exp_sim > 0.2:
        explanation_parts.append("experience references the problem domain")
    if availability_signal > 0:
        explanation_parts.append("availability is recorded")
    if not explanation_parts:
        explanation_parts.append("limited signal available")
    explanation = str(candidate.get("name") or "Candidate") + " — " + "; ".join(explanation_parts) + "."

    contributing = [f"{label}: {round(f, 3)}" for f, label in factors if f > 0.05]
    if not contributing:
        contributing = ["score below contribution threshold"]

    return {"score": score, "explanation": explanation, "contributingFactors": contributing}


def sort_scores(results: list[dict]) -> list[dict]:
    return sorted(results, key=lambda r: r["score"], reverse=True)


# ---------------------------------------------------------------------------
# Gap analysis (used by recovery and recommendation narrative)
# ---------------------------------------------------------------------------

def missing_gaps(requirement: dict, candidates: list[dict]) -> list[str]:
    req_skills = set(_norm_lower(requirement.get("requiredSkills")))
    if not req_skills:
        return []
    covered: set[str] = set()
    for c in candidates:
        covered |= set(_norm_lower(c.get("skills")))
    return sorted(req_skills - covered)


def match_any_requirement(req_skill: str, candidates: list[dict]) -> list[dict]:
    """Best candidates that cover a single missing capability."""
    target = req_skill.strip().lower()
    scored = []
    for c in candidates:
        skills = set(_norm_lower(c.get("skills")))
        closed = (skills & {t for t in (target.split() or [])}) == set() and target in skills
        hits = [s for s in skills if target in s or s in target]
        closed = bool(hits)
        reason = f"'{req_skill}' is covered by their skills" if hits else "partial domain overlap"
        score = 0.9 if hits else max(0.3, score_candidate({"requiredSkills": [req_skill]}, c)["score"])
        scored.append({
            "id": c.get("id"),
            "name": c.get("name"),
            "score": round(score, 3),
            "reason": reason,
            "missingGapClosed": closed,
        })
    return sorted(scored, key=lambda r: r["score"], reverse=True)[:5]