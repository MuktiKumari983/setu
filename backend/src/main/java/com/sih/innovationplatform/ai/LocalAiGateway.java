package com.sih.innovationplatform.ai;

import com.sih.innovationplatform.config.AppProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lightweight, fully explainable rule-based engine used when the FastAPI
 * service is disabled/unreachable. Keeps the whole workflow demonstrable.
 */
public class LocalAiGateway implements AiGateway {

    private static final Set<String> STOPWORDS = new HashSet<>(List.of(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being", "in", "on",
            "at", "to", "of", "and", "or", "for", "with", "by", "from", "as", "into", "our",
            "their", "we", "they", "it", "this", "that", "there", "here", "have", "has", "had",
            "do", "does", "did", "not", "but", "than", "very", "more", "most", "about", "all"));

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry("Waste Management", List.of("waste", "garbage", "collection", "dumping", "segregation",
                    "recycle", "litter", "disposal", "trash", "bins", "landfill", "sanitary")),
            Map.entry("Water Quality", List.of("water", "drinking", "contamination", "river", "groundwater",
                    "lake", "sanitation", "borewell", "quality", "purification", "chemicals", "effluent")),
            Map.entry("Public Health", List.of("health", "hospital", "disease", "clinic", "maternal",
                    "hygiene", "vaccination", "nutrition", "medicine", "outbreak", "malaria", "dengue")),
            Map.entry("Education", List.of("school", "education", "literacy", "classroom", "teacher",
                    "learning", "dropout", "students", "digital literacy", "teaching")),
            Map.entry("Renewable Energy", List.of("energy", "electricity", "solar", "power", "grid",
                    "lighting", "renewable", "battery", "inverter")),
            Map.entry("Agriculture & Livelihood", List.of("farmer", "crop", "agriculture", "irrigation",
                    "market", "price", "produce", "fertilizer", "income", "harvest")),
            Map.entry("Transportation", List.of("road", "transport", "commute", "traffic", "bus",
                    "pedestrian", "connectivity", "street")),
            Map.entry("Public Safety", List.of("safety", "crime", "street light", "disaster", "emergency",
                    "flood", "theft", "darkness", "vulnerable")),
            Map.entry("Flood & Disaster", List.of("flood", "flood level", "evacuation", "rainfall",
                    "early warning", "river overflow", "drainage", "monsoon")),
            Map.entry("Digital Inclusion", List.of("internet", "broadband", "digital", "mobile network",
                    "online", "mobile app", "network", "4g", "5g")),
            Map.entry("Housing & Civic Infrastructure", List.of("housing", "drainage", "sewage",
                    "infrastructure", "slum", "road repair", "street light", "parking")));

    private static final Map<String, List<String>> CATEGORY_SKILLS = Map.ofEntries(
            Map.entry("Waste Management", List.of("IoT", "Data Analytics", "GIS", "Web Development", "Computer Vision")),
            Map.entry("Water Quality", List.of("IoT", "Sensors", "Data Analytics", "GIS", "Web Development")),
            Map.entry("Public Health", List.of("Data Analytics", "Mobile App", "GIS", "Web Development")),
            Map.entry("Education", List.of("Web Development", "Mobile App", "Data Analytics", "Content Design")),
            Map.entry("Renewable Energy", List.of("IoT", "Embedded Systems", "Electrical Engineering", "Web Development")),
            Map.entry("Agriculture & Livelihood", List.of("Data Analytics", "Mobile App", "IoT", "GIS")),
            Map.entry("Transportation", List.of("IoT", "Data Analytics", "Algorithms", "GIS", "Web Development")),
            Map.entry("Public Safety", List.of("IoT", "Computer Vision", "Web Development", "GIS")),
            Map.entry("Flood & Disaster", List.of("IoT", "GIS", "Data Analytics", "Hydrological Modeling", "Web Development")),
            Map.entry("Digital Inclusion", List.of("Web Development", "Networking", "Mobile App", "Content Design")),
            Map.entry("Housing & Civic Infrastructure", List.of("GIS", "Civil Engineering", "Data Analytics", "Web Development")));

    private static final Map<String, List<List<String>>> DECOMPOSITION_TEMPLATES = Map.of(
            "Waste Management", List.of(
                    List.of("Collection scheduling", "Backend,Data Analytics", "Scheduling and tracking waste pickup for villages", "HIGH"),
                    List.of("Route optimization", "Algorithms,GIS", "Optimized collection routes for the fleet", "HIGH"),
                    List.of("Waste segregation", "IoT,Computer Vision", "Smart segregation monitoring at collection points", "MEDIUM"),
                    List.of("Monitoring dashboard", "Web Development", "Live dashboard for citizens and municipality", "HIGH")),
            "Water Quality", List.of(
                    List.of("Sensor deployment & telemetry", "IoT,Sensors", "Deploy water quality sensors with telemetry", "HIGH"),
                    List.of("Data analytics & alerts", "Data Analytics", "Detect contamination and trigger alerts", "HIGH"),
                    List.of("Spatial monitoring", "GIS", "Map affected villages and sources", "MEDIUM"),
                    List.of("Public dashboard", "Web Development", "Publish real-time water quality to citizens", "MEDIUM")),
            "Flood & Disaster", List.of(
                    List.of("Flood monitoring sensors", "IoT,Sensors", "River-level and rainfall sensing", "HIGH"),
                    List.of("Early warning system", "Data Analytics,Algorithms", "Prediction and alert thresholds", "CRITICAL"),
                    List.of("Evacuation mapping", "GIS", "Flood-risk maps per village", "HIGH"),
                    List.of("Alert dashboard", "Web Development", "Send alerts to residents and authorities", "HIGH")),
            "Public Health", List.of(
                    List.of("Health data collection", "Mobile App,Data Analytics", "Collect field health records", "HIGH"),
                    List.of("Outbreak analytics", "Data Analytics", "Detect disease clusters early", "HIGH"),
                    List.of("Clinic finder", "GIS,Mobile App", "Map nearest clinics and services", "MEDIUM"),
                    List.of("Awareness portal", "Web Development,Content Design", "Health education for communities", "MEDIUM")),
            "Education", List.of(
                    List.of("Content platform", "Web Development,Content Design", "Learning content in local languages", "HIGH"),
                    List.of("Attendance & progress tracking", "Data Analytics", "Monitor school participation", "MEDIUM"),
                    List.of("Digital literacy labs", "Networking", "Set up community computer and internet access", "MEDIUM"),
                    List.of("Analytics dashboard", "Web Development,Data Analytics", "District-level education dashboards", "MEDIUM")),
            "Renewable Energy", List.of(
                    List.of("Solar micro-grid", "Electrical Engineering", "Design community solar system", "HIGH"),
                    List.of("Energy monitoring", "IoT,Embedded Systems", "Measure usage and health of the grid", "HIGH"),
                    List.of("Revenue model", "Data Analytics", "Tariff and payment tracking", "MEDIUM"),
                    List.of("Mobile monitoring", "Mobile App", "Village residents view energy usage", "MEDIUM")),
            "Agriculture & Livelihood", List.of(
                    List.of("Field data collection", "Mobile App,Data Analytics", "Daily produce and price data", "HIGH"),
                    List.of("Market price engine", "Data Analytics", "Real-time price comparisons", "HIGH"),
                    List.of("Irrigation advisory", "IoT,GIS", "Soil and weather-based irrigation advice", "MEDIUM"),
                    List.of("Farmer dashboard", "Web Development", "Advisory and market dashboard", "MEDIUM")),
            "Transportation", List.of(
                    List.of("Fleet tracking", "IoT", "Vehicle/route tracking devices", "HIGH"),
                    List.of("Route analytics", "Data Analytics,Algorithms", "Utilization and congestion analysis", "HIGH"),
                    List.of("Route mapping", "GIS", "Map optimal routes and stops", "MEDIUM"),
                    List.of("Commuter app", "Web Development,Mobile App", "Real-time schedules for commuters", "MEDIUM")),
            "Digital Inclusion", List.of(
                    List.of("Community connectivity", "Networking", "Shared broadband at panchayat buildings", "HIGH"),
                    List.of("Digital literacy program", "Content Design", "Training for rural residents", "MEDIUM"),
                    List.of("Service kiosks", "Web Development", "E-services at community centers", "MEDIUM")));

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public AiModels.AnalyzeResult analyze(String title, String description, String requiredOutcome) {
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        String category = "General Civic Issue";
        int best = 0;
        List<String> bestKeywords = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : CATEGORY_KEYWORDS.entrySet()) {
            int hits = 0;
            List<String> matched = new ArrayList<>();
            for (String kw : e.getValue()) {
                if (text.contains(kw)) {
                    hits++;
                    matched.add(kw);
                }
            }
            if (hits > best) {
                best = hits;
                category = e.getKey();
                bestKeywords = matched;
            }
        }

        List<String> skills = CATEGORY_SKILLS.getOrDefault(category,
                List.of("Web Development", "Data Analytics", "Mobile App"));

        String urgency = "MEDIUM";
        if (text.contains("critical") || text.contains("immediate") || text.contains("emergency")
                || text.contains("life") || text.contains("death")) {
            urgency = "CRITICAL";
        } else if (text.contains("urgent") || text.contains("severe") || text.contains("piling")
                || text.contains("contaminated") || text.contains("outbreak") || text.contains("flood")) {
            urgency = "HIGH";
        }

        Set<String> tokens = tokenize(text);
        List<String> keywords = new ArrayList<>(new LinkedHashSet<>(tokens)).stream()
                .filter(t -> t.length() > 3).limit(8).toList();

        double confidence = Math.min(0.95, 0.55 + best * 0.08);
        String explanation = "Classified as \"" + category + "\" because the problem text matched keywords: "
                + (bestKeywords.isEmpty() ? "no strong category keywords" : String.join(", ", bestKeywords))
                + ". Required skills derived from the category profile.";

        List<String> rootCauses = bestKeywords.stream().limit(5).map(k -> "Reported signal: " + k).toList();
        List<String> challenges = keywords.stream().limit(5).map(k -> "Reported condition: " + k).toList();
        List<String> solutionDirections = skills.stream().limit(5).map(s -> "Assess capability: " + s).toList();
        List<String> expectedOutcomes = requiredOutcome == null || requiredOutcome.isBlank()
                ? List.of() : List.of("Stated outcome: " + requiredOutcome.trim());
        return new AiModels.AnalyzeResult(category, urgency, skills, keywords, confidence, explanation,
                rootCauses, List.of(), challenges, solutionDirections, expectedOutcomes, AiModels.AiSource.LOCAL);
    }

    @Override
    public List<AiModels.SimilarProblem> duplicates(String title, String description,
                                                    List<AiModels.ExistingProblem> existing) {
        List<AiModels.SimilarProblem> out = new ArrayList<>();
        Map<String, Double> freq = tf(title + " " + description);
        for (AiModels.ExistingProblem e : existing) {
            Map<String, Double> other = tf(e.title() + " " + e.description());
            double sim = cosine(freq, other);
            if (sim >= 0.25) {
                out.add(new AiModels.SimilarProblem(e.id(), e.title(), Math.round(sim * 100.0) / 100.0));
            }
        }
        out.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));
        return out.stream().limit(5).toList();
    }

    @Override
    public List<AiModels.Capability> decompose(String title, String description, List<String> skills) {
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        String category = detectCategory(text);
        List<AiModels.Capability> out = new ArrayList<>();
        List<List<String>> tmpl = DECOMPOSITION_TEMPLATES.getOrDefault(category, List.of(
                List.of("Field assessment & data collection", "Data Analytics,Mobile App", "Collect baseline field data", "HIGH"),
                List.of("Monitoring application", "Web Development,Data Analytics", "Build a monitoring dashboard", "MEDIUM"),
                List.of("Community engagement", "Content Design", "Inform and involve the community", "MEDIUM")));
        for (List<String> row : tmpl) {
            out.add(new AiModels.Capability(row.get(0), row.get(2),
                    Arrays.stream(row.get(1).split(",")).toList(), row.get(3)));
        }
        return out;
    }

    @Override
    public List<AiModels.ScoredMatch> match(MatchType type,
                                            AiModels.MatchingRequirement requirement,
                                            List<AiModels.CandidateProfile> candidates,
                                            AppProperties app) {
        List<AiModels.ScoredMatch> out = new ArrayList<>();
        List<String> reqSkills = normLower(requirement.requiredSkills());
        List<String> reqKeywords = normLower(requirement.keywords());
        for (AiModels.CandidateProfile c : candidates) {
            Set<String> cSkills = new HashSet<>(normLower(c.skills()));
            Set<String> cTags = new HashSet<>(normLower(c.tags()));

            double js = jaccard(new HashSet<>(reqSkills), cSkills);
            double jk = reqKeywords.isEmpty() ? 0 : jaccard(new HashSet<>(reqKeywords), cTags);
            double jd = jaccard(new HashSet<>(tokenize(requirement.category() == null ? ""
                    : requirement.category().toLowerCase(Locale.ROOT))),
                    tokenize(c.description() == null ? "" : c.description().toLowerCase(Locale.ROOT)));

            double score = 0.55 * js + 0.25 * jk + 0.20 * jd;

            List<String> factors = new ArrayList<>();
            List<String> matchedSkills = c.skills().stream()
                    .filter(s -> reqSkills.contains(s.toLowerCase(Locale.ROOT))).toList();
            List<String> matchedKw = c.tags().stream()
                    .filter(t -> reqKeywords.contains(t.toLowerCase(Locale.ROOT))).toList();
            if (!matchedSkills.isEmpty()) {
                factors.add("Matched skills: " + String.join(", ", matchedSkills));
            }
            if (!matchedKw.isEmpty()) {
                factors.add("Matched keywords: " + String.join(", ", matchedKw));
            }
            if (jd > 0.05) {
                factors.add("Profile overlap with problem domain " + Math.round(jd * 100) + "%");
            }
            if (factors.isEmpty()) {
                factors.add("Profile has " + c.skills().size() + " recorded capabilities but limited direct overlap");
            }

            String explanation = type.forAi() + " compatibility " + Math.round(score * 100)
                    + "% (skills overlap " + Math.round(js * 100) + "%, domain overlap " + Math.round((jk + jd) * 50) + "%).";
            out.add(new AiModels.ScoredMatch(c.id(), Math.round(score * 100.0) / 100.0, explanation, factors));
        }
        out.sort((a, b) -> Double.compare(b.score(), a.score()));
        return out;
    }

    @Override
    public AiModels.Recommendation recommend(AiModels.MatchingRequirement requirement,
                                             List<AiModels.CandidateProfile> universities,
                                             List<AiModels.CandidateProfile> teams,
                                             List<AiModels.CandidateProfile> mentors,
                                             List<AiModels.CandidateProfile> industries,
                                             AppProperties app) {
        List<AiModels.StakeholderPick> picks = new ArrayList<>();
        addBest(picks, "UNIVERSITY", universities, requirement, match(MatchType.UNIVERSITY, requirement, universities, app),
                "University research, labs and faculty capacity");
        addBest(picks, "TEAM", teams, requirement, match(MatchType.TEAM, requirement, teams, app),
                "Student team builds the software & integrations");
        addBest(picks, "MENTOR", mentors, requirement, match(MatchType.MENTOR, requirement, mentors, app),
                "Domain expertise and academic guidance");
        addBest(picks, "INDUSTRY", industries, requirement, match(MatchType.INDUSTRY, requirement, industries, app),
                "Hardware, funding and deployment support");

        String narrative = "Combined: " + picks.stream()
                .map(p -> p.name() + " (" + Math.round(p.score() * 100) + "%)")
                .collect(Collectors.joining(", ")) + ".";
        return new AiModels.Recommendation(picks, narrative);
    }

    private void addBest(List<AiModels.StakeholderPick> picks, String teamType,
                         List<AiModels.CandidateProfile> candidates,
                         AiModels.MatchingRequirement requirement,
                         List<AiModels.ScoredMatch> scored, String contribution) {
        if (scored.isEmpty()) {
            return;
        }
        AiModels.ScoredMatch top = scored.get(0);
        AiModels.CandidateProfile prof = candidates.stream()
                .filter(c -> c.id().equals(top.id())).findFirst().orElse(null);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("skills", prof == null ? List.of() : prof.skills());
        details.put("description", prof == null ? "" : prof.description());
        List<String> gap = prof == null ? List.of() : requirement.requiredSkills().stream()
                .filter(s -> !prof.skills().contains(s)).toList();
        picks.add(new AiModels.StakeholderPick(teamType, top.id(),
                prof == null ? "Unknown" : prof.name(), top.score(),
                top.explanation(), contribution, gap, details));
    }

    @Override
    public AiModels.RecoveryRecommendation recover(String missingCapability,
                                                   AiModels.MatchingRequirement requirement,
                                                   List<AiModels.CandidateProfile> universities,
                                                   List<AiModels.CandidateProfile> teams,
                                                   List<AiModels.CandidateProfile> mentors,
                                                   List<AiModels.CandidateProfile> industries,
                                                   AppProperties app) {
        List<AiModels.RecoveryPick> out = new ArrayList<>();
        String m = missingCapability == null ? "" : missingCapability.toLowerCase(Locale.ROOT);
        for (var entry : List.of(
                Map.entry("MENTOR", mentors),
                Map.entry("TEAM", teams),
                Map.entry("UNIVERSITY", universities),
                Map.entry("INDUSTRY", industries))) {
            for (AiModels.CandidateProfile c : entry.getValue()) {
                boolean full = c.skills().stream().anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(m));
                boolean partial = c.tags().stream().anyMatch(t -> t.toLowerCase(Locale.ROOT).contains(m))
                        || (c.description() != null && c.description().toLowerCase(Locale.ROOT).contains(m));
                if (full || partial) {
                    double score = full ? 0.9 : 0.7;
                    out.add(new AiModels.RecoveryPick(entry.getKey(), c.id(), c.name(), score,
                            "Has " + missingCapability + " capability"
                                    + (full ? " (listed as a skill)" : " (domain experience)"),
                            full));
                }
            }
        }
        out.sort((a, b) -> Boolean.compare(b.missingGapClosed(), a.missingGapClosed()));
        String narrative = "The failed stage needed \"" + missingCapability
                + "\". Recommended collaborators close this capability gap so the project can continue.";
        return new AiModels.RecoveryRecommendation(out.stream().limit(6).toList(), narrative);
    }

    // ---------- NLP helpers ----------

    private String detectCategory(String lowerText) {
        String bestCategory = "General Civic Issue";
        int best = 0;
        for (Map.Entry<String, List<String>> e : CATEGORY_KEYWORDS.entrySet()) {
            int hits = 0;
            for (String kw : e.getValue()) {
                if (lowerText.contains(kw)) {
                    hits++;
                }
            }
            if (hits > best) {
                best = hits;
                bestCategory = e.getKey();
            }
        }
        return bestCategory;
    }

    private Set<String> tokenize(String text) {
        Set<String> set = new LinkedHashSet<>();
        if (text == null) {
            return set;
        }
        for (String t : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+")) {
            if (!t.isBlank() && t.length() > 2 && !STOPWORDS.contains(t)) {
                set.add(t);
            }
        }
        return set;
    }

    private Map<String, Double> tf(String text) {
        Map<String, Double> map = new HashMap<>();
        for (String t : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+")) {
            if (!t.isBlank() && t.length() > 2 && !STOPWORDS.contains(t)) {
                map.merge(t, 1.0, Double::sum);
            }
        }
        return map;
    }

    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        double dot = 0, na = 0, nb = 0;
        for (double v : a.values()) {
            na += v * v;
        }
        for (double v : b.values()) {
            nb += v * v;
        }
        for (Map.Entry<String, Double> e : a.entrySet()) {
            dot += e.getValue() * b.getOrDefault(e.getKey(), 0.0);
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-9);
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0;
        }
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        if (union.isEmpty()) {
            return 0;
        }
        int inter = 0;
        for (String s : a) {
            if (b.contains(s)) {
                inter++;
            }
        }
        return (double) inter / union.size();
    }

    private List<String> normLower(List<String> list) {
        return list == null ? List.of()
                : list.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
    }
}