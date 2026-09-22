package com.sih.innovationplatform.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.innovationplatform.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Talks to the FastAPI service. If the remote service is unreachable we
 * transparently fall back to the built-in rule engine so demos keep working.
 */
@Slf4j
@Service
public class RemoteAiGateway implements AiGateway {

    private final RestClient client;
    private final ObjectMapper mapper;
    private final AppProperties props;
    private final LocalAiGateway fallback;

    public RemoteAiGateway(AppProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
        this.fallback = new LocalAiGateway();
        this.client = RestClient.builder()
                .baseUrl(props.getAi().getUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public boolean isRemote() {
        return props.getAi().isEnabled();
    }

    private boolean remoteAvailable() {
        return props.getAi().isEnabled();
    }

    @Override
    public AiModels.AnalyzeResult analyze(String title, String description) {
        if (!remoteAvailable()) {
            return fallback.analyze(title, description);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("title", title);
            body.put("description", description);
            JsonNode node = post("/analyze", body);
            return new AiModels.AnalyzeResult(
                    text(node, "category"),
                    text(node, "urgency"),
                    stringList(node, "requiredSkills"),
                    stringList(node, "keywords"),
                    node.path("confidence").asDouble(0.8),
                    text(node, "explanation"));
        } catch (Exception e) {
            log.warn("AI analyze unavailable, using fallback: {}", e.getMessage());
            return fallback.analyze(title, description);
        }
    }

    @Override
    public List<AiModels.SimilarProblem> duplicates(String title, String description,
                                                    List<AiModels.ExistingProblem> existing) {
        if (!remoteAvailable() || existing.isEmpty()) {
            return fallback.duplicates(title, description, existing);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("title", title);
            body.put("description", description);
            body.put("existingProblems", existing.stream().map(e -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", e.id());
                m.put("title", e.title());
                m.put("description", e.description());
                return m;
            }).toList());
            JsonNode node = post("/duplicates", body);
            List<AiModels.SimilarProblem> out = new ArrayList<>();
            for (JsonNode n : node.path("similarProblems")) {
                out.add(new AiModels.SimilarProblem(
                        n.path("id").asLong(), text(n, "title"), n.path("similarity").asDouble()));
            }
            return out;
        } catch (Exception e) {
            log.warn("AI duplicates unavailable, using fallback: {}", e.getMessage());
            return fallback.duplicates(title, description, existing);
        }
    }

    @Override
    public List<AiModels.Capability> decompose(String title, String description, List<String> skills) {
        if (!remoteAvailable()) {
            return fallback.decompose(title, description, skills);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("title", title);
            body.put("description", description);
            body.put("skills", skills == null ? List.of() : skills);
            JsonNode node = post("/decompose", body);
            List<AiModels.Capability> out = new ArrayList<>();
            for (JsonNode n : node.path("capabilities")) {
                out.add(new AiModels.Capability(
                        text(n, "capability"), text(n, "description"),
                        stringList(n, "requiredSkills"), text(n, "priority")));
            }
            return out;
        } catch (Exception e) {
            log.warn("AI decompose unavailable, using fallback: {}", e.getMessage());
            return fallback.decompose(title, description, skills);
        }
    }

    @Override
    public List<AiModels.ScoredMatch> match(MatchType type,
                                            AiModels.MatchingRequirement requirement,
                                            List<AiModels.CandidateProfile> candidates,
                                            AppProperties app) {
        if (!remoteAvailable()) {
            return fallback.match(type, requirement, candidates, app);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("requirement", requirementMap(requirement));
            body.put("candidates", candidates.stream().map(this::candidateMap).toList());
            JsonNode node = post("/match/" + type.forAi(), body);
            List<AiModels.ScoredMatch> out = new ArrayList<>();
            for (JsonNode n : node.path("results")) {
                out.add(new AiModels.ScoredMatch(
                        n.path("id").asLong(), n.path("score").asDouble(),
                        text(n, "explanation"), stringList(n, "contributingFactors")));
            }
            return out;
        } catch (Exception e) {
            log.warn("AI match unavailable, using fallback: {}", e.getMessage());
            return fallback.match(type, requirement, candidates, app);
        }
    }

    @Override
    public AiModels.Recommendation recommend(AiModels.MatchingRequirement requirement,
                                             List<AiModels.CandidateProfile> universities,
                                             List<AiModels.CandidateProfile> teams,
                                             List<AiModels.CandidateProfile> mentors,
                                             List<AiModels.CandidateProfile> industries,
                                             AppProperties app) {
        if (!remoteAvailable()) {
            return fallback.recommend(requirement, universities, teams, mentors, industries, app);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("requirement", requirementMap(requirement));
            body.put("universities", universities.stream().map(this::candidateMap).toList());
            body.put("teams", teams.stream().map(this::candidateMap).toList());
            body.put("mentors", mentors.stream().map(this::candidateMap).toList());
            body.put("industries", industries.stream().map(this::candidateMap).toList());
            JsonNode node = post("/recommend", body);
            List<AiModels.StakeholderPick> picks = new ArrayList<>();
            for (JsonNode n : node.path("stakeholders")) {
                picks.add(new AiModels.StakeholderPick(
                        text(n, "type"), n.path("id").asLong(), text(n, "name"),
                        n.path("score").asDouble(), text(n, "reason"), text(n, "contribution"),
                        stringList(n, "gapSkills"),
                        n.path("details").isObject()
                                ? mapper.convertValue(n.path("details"), Map.class) : Map.of()));
            }
            return new AiModels.Recommendation(picks, text(node, "narrative"));
        } catch (Exception e) {
            log.warn("AI recommend unavailable, using fallback: {}", e.getMessage());
            return fallback.recommend(requirement, universities, teams, mentors, industries, app);
        }
    }

    @Override
    public AiModels.RecoveryRecommendation recover(String missingCapability,
                                                   AiModels.MatchingRequirement requirement,
                                                   List<AiModels.CandidateProfile> universities,
                                                   List<AiModels.CandidateProfile> teams,
                                                   List<AiModels.CandidateProfile> mentors,
                                                   List<AiModels.CandidateProfile> industries,
                                                   AppProperties app) {
        if (!remoteAvailable()) {
            return fallback.recover(missingCapability, requirement, universities, teams, mentors, industries, app);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("missingCapability", missingCapability);
            body.put("requirement", requirementMap(requirement));
            body.put("universities", universities.stream().map(this::candidateMap).toList());
            body.put("teams", teams.stream().map(this::candidateMap).toList());
            body.put("mentors", mentors.stream().map(this::candidateMap).toList());
            body.put("industries", industries.stream().map(this::candidateMap).toList());
            JsonNode node = post("/recovery", body);
            List<AiModels.RecoveryPick> picks = new ArrayList<>();
            for (JsonNode n : node.path("recommendations")) {
                picks.add(new AiModels.RecoveryPick(
                        text(n, "entityType"), n.path("id").asLong(), text(n, "name"),
                        n.path("score").asDouble(), text(n, "reason"), n.path("missingGapClosed").asBoolean()));
            }
            return new AiModels.RecoveryRecommendation(picks, text(node, "narrative"));
        } catch (Exception e) {
            log.warn("AI recovery unavailable, using fallback: {}", e.getMessage());
            return fallback.recover(missingCapability, requirement, universities, teams, mentors, industries, app);
        }
    }

    private JsonNode post(String path, Object body) {
        String json = client.post().uri(path).body(body).retrieve().body(String.class);
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Bad AI response: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> requirementMap(AiModels.MatchingRequirement r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", r.category());
        m.put("keywords", r.keywords() == null ? List.of() : r.keywords());
        m.put("requiredSkills", r.requiredSkills() == null ? List.of() : r.requiredSkills());
        m.put("capabilities", r.capabilities() == null ? List.of() : r.capabilities());
        return m;
    }

    private Map<String, Object> candidateMap(AiModels.CandidateProfile c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.id());
        m.put("name", c.name());
        m.put("description", c.description() == null ? "" : c.description());
        m.put("skills", c.skills() == null ? List.of() : c.skills());
        m.put("tags", c.tags() == null ? List.of() : c.tags());
        m.put("capabilities", c.capabilities() == null ? List.of() : c.capabilities());
        return m;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isTextual() ? v.asText() : (v.isMissingNode() ? "" : v.toString());
    }

    private List<String> stringList(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        for (JsonNode n : node.path(field)) {
            if (n.isTextual()) {
                out.add(n.asText());
            }
        }
        return out;
    }
}