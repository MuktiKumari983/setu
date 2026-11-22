package com.sih.innovationplatform.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AiWire {

    private static final Set<String> ENTITY_TYPES = Set.of("UNIVERSITY", "TEAM", "MENTOR", "INDUSTRY");

    private AiWire() {
    }

    public interface Validated {
        void validate();
    }

    public static <T extends Validated> T parse(ObjectMapper mapper, String json, Class<T> type) {
        try {
            T value = mapper.readValue(json, type);
            value.validate();
            return value;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid AI response: " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record AnalyzeResponse(String category, String urgency, List<String> requiredSkills,
                                  List<String> keywords, Double confidence, String explanation,
                                  List<String> rootCauses, List<String> targetBeneficiaries,
                                  List<String> challenges, List<String> solutionDirections,
                                  List<String> expectedOutcomes, String source) implements Validated {
        public void validate() {
            requireText(category, "category");
            requireText(urgency, "urgency");
            requireText(explanation, "explanation");
            requireList(requiredSkills, "requiredSkills");
            requireList(keywords, "keywords");
            requireList(rootCauses, "rootCauses");
            requireList(targetBeneficiaries, "targetBeneficiaries");
            requireList(challenges, "challenges");
            requireList(solutionDirections, "solutionDirections");
            requireList(expectedOutcomes, "expectedOutcomes");
            requireScore(confidence, "confidence");
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record SimilarProblemResponse(Long id, String title, Double similarity, String source) implements Validated {
        public void validate() {
            requireId(id, "id");
            requireText(title, "title");
            requireScore(similarity, "similarity");
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record DuplicateResponse(List<SimilarProblemResponse> similarProblems, String source) implements Validated {
        public void validate() {
            if (similarProblems == null) {
                throw new IllegalArgumentException("Missing similarProblems");
            }
            similarProblems.forEach(SimilarProblemResponse::validate);
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record CapabilityResponse(String capability, String description, List<String> requiredSkills,
                                     String priority, String source) implements Validated {
        public void validate() {
            requireText(capability, "capability");
            requireText(description, "description");
            requireText(priority, "priority");
            requireList(requiredSkills, "requiredSkills");
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record DecomposeResponse(List<CapabilityResponse> capabilities, String category, String source) implements Validated {
        public void validate() {
            if (capabilities == null) {
                throw new IllegalArgumentException("Missing capabilities");
            }
            capabilities.forEach(CapabilityResponse::validate);
            requireText(category, "category");
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record ScoredMatchResponse(Long id, Double score, String explanation,
                                      List<String> contributingFactors, String source) implements Validated {
        public void validate() {
            requireId(id, "id");
            requireScore(score, "score");
            requireText(explanation, "explanation");
            requireList(contributingFactors, "contributingFactors");
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record MatchResponse(String matchType, List<ScoredMatchResponse> results, String source) implements Validated {
        public void validate() {
            requireEntityType(matchType, "matchType");
            if (results == null) {
                throw new IllegalArgumentException("Missing results");
            }
            results.forEach(ScoredMatchResponse::validate);
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record StakeholderPickResponse(String type, Long id, String name, Double score, String reason,
                                          String contribution, List<String> gapSkills,
                                          Map<String, Object> details, String source) implements Validated {
        public void validate() {
            requireEntityType(type, "type");
            requireId(id, "id");
            requireText(name, "name");
            requireScore(score, "score");
            requireText(reason, "reason");
            requireText(contribution, "contribution");
            requireList(gapSkills, "gapSkills");
            if (details == null) {
                throw new IllegalArgumentException("Missing details");
            }
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record RecommendationResponse(List<StakeholderPickResponse> stakeholders, String narrative, String source) implements Validated {
        public void validate() {
            if (stakeholders == null) {
                throw new IllegalArgumentException("Missing stakeholders");
            }
            stakeholders.forEach(StakeholderPickResponse::validate);
            requireText(narrative, "narrative");
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record RecoveryPickResponse(String entityType, Long id, String name, Double score, String reason,
                                       Boolean missingGapClosed, String source) implements Validated {
        public void validate() {
            requireEntityType(entityType, "entityType");
            requireId(id, "id");
            requireText(name, "name");
            requireScore(score, "score");
            requireText(reason, "reason");
            if (missingGapClosed == null) {
                throw new IllegalArgumentException("Missing missingGapClosed");
            }
            requireSource(source);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record RecoveryResponse(List<RecoveryPickResponse> recommendations, String narrative, String source) implements Validated {
        public void validate() {
            if (recommendations == null) {
                throw new IllegalArgumentException("Missing recommendations");
            }
            recommendations.forEach(RecoveryPickResponse::validate);
            requireText(narrative, "narrative");
            requireSource(source);
        }
    }

    public static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + field);
        }
    }

    public static void requireList(List<String> value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("Missing " + field);
        }
        value.forEach(item -> requireText(item, field + " item"));
    }

    public static void requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid " + field);
        }
    }

    public static void requireScore(Double value, String field) {
        if (value == null || value < 0 || value > 1) {
            throw new IllegalArgumentException("Invalid " + field);
        }
    }

    public static void requireSource(String value) {
        if (!"REMOTE".equals(value)) {
            throw new IllegalArgumentException("Invalid source");
        }
    }

    public static void requireEntityType(String value, String field) {
        if (value == null || !ENTITY_TYPES.contains(value)) {
            throw new IllegalArgumentException("Invalid " + field);
        }
    }
}
