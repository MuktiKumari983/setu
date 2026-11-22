package com.sih.innovationplatform.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.innovationplatform.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                .requestFactory(requestFactory(props))
                .build();
    }

    static SimpleClientHttpRequestFactory requestFactory(AppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(props.getAi().getTimeoutMs());
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    @Override
    public boolean isRemote() {
        return props.getAi().isEnabled();
    }

    private boolean remoteAvailable() {
        return props.getAi().isEnabled();
    }

    @Override
    public AiModels.AnalyzeResult analyze(String title, String description, String requiredOutcome) {
        if (!remoteAvailable()) {
            return fallback.analyze(title, description, requiredOutcome);
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("title", title);
            body.put("description", description);
            body.put("requiredOutcome", requiredOutcome == null ? "" : requiredOutcome);
            AiWire.AnalyzeResponse node = post("/analyze", body, AiWire.AnalyzeResponse.class);
            return new AiModels.AnalyzeResult(
                    node.category(), node.urgency(), node.requiredSkills(), node.keywords(),
                    node.confidence(), node.explanation(), node.rootCauses(), node.targetBeneficiaries(),
                    node.challenges(), node.solutionDirections(), node.expectedOutcomes(),
                    AiModels.AiSource.REMOTE);
        } catch (Exception e) {
            log.warn("AI analyze unavailable, using fallback: {}", e.getMessage());
            AiModels.AnalyzeResult result = fallback.analyze(title, description, requiredOutcome);
            return new AiModels.AnalyzeResult(result.category(), result.urgency(), result.requiredSkills(),
                    result.keywords(), result.confidence(), result.explanation(), result.rootCauses(),
                    result.targetBeneficiaries(), result.challenges(), result.solutionDirections(),
                    result.expectedOutcomes(), AiModels.AiSource.FALLBACK);
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
            AiWire.DuplicateResponse node = post("/duplicates", body, AiWire.DuplicateResponse.class);
            return node.similarProblems().stream()
                    .map(n -> new AiModels.SimilarProblem(n.id(), n.title(), n.similarity(), AiModels.AiSource.REMOTE))
                    .toList();
        } catch (Exception e) {
            log.warn("AI duplicates unavailable, using fallback: {}", e.getMessage());
            return fallback.duplicates(title, description, existing).stream()
                    .map(n -> new AiModels.SimilarProblem(n.id(), n.title(), n.similarity(), AiModels.AiSource.FALLBACK))
                    .toList();
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
            AiWire.DecomposeResponse node = post("/decompose", body, AiWire.DecomposeResponse.class);
            return node.capabilities().stream()
                    .map(n -> new AiModels.Capability(n.capability(), n.description(), n.requiredSkills(),
                            n.priority(), AiModels.AiSource.REMOTE))
                    .toList();
        } catch (Exception e) {
            log.warn("AI decompose unavailable, using fallback: {}", e.getMessage());
            return fallback.decompose(title, description, skills).stream()
                    .map(n -> new AiModels.Capability(n.capability(), n.description(), n.requiredSkills(),
                            n.priority(), AiModels.AiSource.FALLBACK))
                    .toList();
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
            AiWire.MatchResponse node = post("/match/" + type.forAi(), body, AiWire.MatchResponse.class);
            return node.results().stream()
                    .map(n -> new AiModels.ScoredMatch(n.id(), n.score(), n.explanation(),
                            n.contributingFactors(), AiModels.AiSource.REMOTE))
                    .toList();
        } catch (Exception e) {
            log.warn("AI match unavailable, using fallback: {}", e.getMessage());
            return fallback.match(type, requirement, candidates, app).stream()
                    .map(n -> new AiModels.ScoredMatch(n.id(), n.score(), n.explanation(),
                            n.contributingFactors(), AiModels.AiSource.FALLBACK))
                    .toList();
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
            AiWire.RecommendationResponse node = post("/recommend", body, AiWire.RecommendationResponse.class);
            List<AiModels.StakeholderPick> picks = node.stakeholders().stream()
                    .map(n -> new AiModels.StakeholderPick(n.type(), n.id(), n.name(), n.score(),
                            n.reason(), n.contribution(), n.gapSkills(), n.details(), AiModels.AiSource.REMOTE))
                    .toList();
            return new AiModels.Recommendation(picks, node.narrative(), AiModels.AiSource.REMOTE);
        } catch (Exception e) {
            log.warn("AI recommend unavailable, using fallback: {}", e.getMessage());
            AiModels.Recommendation result = fallback.recommend(requirement, universities, teams, mentors, industries, app);
            List<AiModels.StakeholderPick> picks = result.stakeholders().stream()
                    .map(n -> new AiModels.StakeholderPick(n.type(), n.id(), n.name(), n.score(),
                            n.reason(), n.contribution(), n.gapSkills(), n.details(), AiModels.AiSource.FALLBACK))
                    .toList();
            return new AiModels.Recommendation(picks, result.narrative(), AiModels.AiSource.FALLBACK);
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
            AiWire.RecoveryResponse node = post("/recovery", body, AiWire.RecoveryResponse.class);
            List<AiModels.RecoveryPick> picks = node.recommendations().stream()
                    .map(n -> new AiModels.RecoveryPick(n.entityType(), n.id(), n.name(), n.score(),
                            n.reason(), n.missingGapClosed(), AiModels.AiSource.REMOTE))
                    .toList();
            return new AiModels.RecoveryRecommendation(picks, node.narrative(), AiModels.AiSource.REMOTE);
        } catch (Exception e) {
            log.warn("AI recovery unavailable, using fallback: {}", e.getMessage());
            AiModels.RecoveryRecommendation result = fallback.recover(
                    missingCapability, requirement, universities, teams, mentors, industries, app);
            List<AiModels.RecoveryPick> picks = result.recommendations().stream()
                    .map(n -> new AiModels.RecoveryPick(n.entityType(), n.id(), n.name(), n.score(),
                            n.reason(), n.missingGapClosed(), AiModels.AiSource.FALLBACK))
                    .toList();
            return new AiModels.RecoveryRecommendation(picks, result.narrative(), AiModels.AiSource.FALLBACK);
        }
    }

    private <T extends AiWire.Validated> T post(String path, Object body, Class<T> type) {
        String json = client.post().uri(path).body(body).retrieve().body(String.class);
        return AiWire.parse(mapper, json, type);
    }

    Map<String, Object> requirementMap(AiModels.MatchingRequirement r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", r.category());
        m.put("keywords", r.keywords() == null ? List.of() : r.keywords());
        m.put("requiredSkills", r.requiredSkills() == null ? List.of() : r.requiredSkills());
        m.put("capabilities", r.capabilities() == null ? List.of() : r.capabilities());
        return m;
    }

    Map<String, Object> candidateMap(AiModels.CandidateProfile c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.id());
        m.put("name", c.name());
        m.put("description", c.description() == null ? "" : c.description());
        m.put("skills", c.skills() == null ? List.of() : c.skills());
        m.put("tags", c.tags() == null ? List.of() : c.tags());
        m.put("capabilities", c.capabilities() == null ? List.of() : c.capabilities());
        m.put("availability", c.availability() == null ? "" : c.availability());
        m.put("experience", c.experience() == null ? List.of() : c.experience());
        return m;
    }
}
