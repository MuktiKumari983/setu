package com.sih.innovationplatform.ai;

import java.util.List;
import java.util.Map;

public final class AiModels {

    private AiModels() {
    }

    public enum AiSource {
        REMOTE,
        FALLBACK,
        LOCAL
    }

    public record AnalyzeResult(String category, String urgency, List<String> requiredSkills,
                                List<String> keywords, double confidence, String explanation,
                                List<String> rootCauses, List<String> targetBeneficiaries,
                                List<String> challenges, List<String> solutionDirections,
                                List<String> expectedOutcomes, AiSource source) {
        public AnalyzeResult(String category, String urgency, List<String> requiredSkills,
                             List<String> keywords, double confidence, String explanation) {
            this(category, urgency, requiredSkills, keywords, confidence, explanation,
                    List.of(), List.of(), List.of(), List.of(), List.of(), AiSource.LOCAL);
        }
    }

    public record ExistingProblem(Long id, String title, String description) {
    }

    public record SimilarProblem(Long id, String title, double similarity, AiSource source) {
        public SimilarProblem(Long id, String title, double similarity) {
            this(id, title, similarity, AiSource.LOCAL);
        }
    }

    public record Capability(String capability, String description, List<String> requiredSkills,
                             String priority, AiSource source) {
        public Capability(String capability, String description, List<String> requiredSkills, String priority) {
            this(capability, description, requiredSkills, priority, AiSource.LOCAL);
        }
    }

    public record CandidateProfile(Long id, String name, String description,
                                   List<String> skills, List<String> tags, List<String> capabilities,
                                   String availability, List<String> experience) {
        public CandidateProfile(Long id, String name, String description,
                                List<String> skills, List<String> tags, List<String> capabilities) {
            this(id, name, description, skills, tags, capabilities, "", List.of());
        }
    }

    public record MatchingRequirement(String category, List<String> keywords,
                                      List<String> requiredSkills, List<String> capabilities) {
    }

    public record ScoredMatch(Long id, double score, String explanation,
                              List<String> contributingFactors, AiSource source) {
        public ScoredMatch(Long id, double score, String explanation, List<String> contributingFactors) {
            this(id, score, explanation, contributingFactors, AiSource.LOCAL);
        }
    }

    public record StakeholderPick(String type, Long id, String name, double score,
                                  String reason, String contribution, List<String> gapSkills,
                                  Map<String, Object> details, AiSource source) {
        public StakeholderPick(String type, Long id, String name, double score,
                               String reason, String contribution, List<String> gapSkills,
                               Map<String, Object> details) {
            this(type, id, name, score, reason, contribution, gapSkills, details, AiSource.LOCAL);
        }
    }

    public record Recommendation(List<StakeholderPick> stakeholders, String narrative, AiSource source) {
        public Recommendation(List<StakeholderPick> stakeholders, String narrative) {
            this(stakeholders, narrative, AiSource.LOCAL);
        }
    }

    public record RecoveryPick(String entityType, Long id, String name, double score,
                               String reason, boolean missingGapClosed, AiSource source) {
        public RecoveryPick(String entityType, Long id, String name, double score,
                            String reason, boolean missingGapClosed) {
            this(entityType, id, name, score, reason, missingGapClosed, AiSource.LOCAL);
        }
    }

    public record RecoveryRecommendation(List<RecoveryPick> recommendations, String narrative, AiSource source) {
        public RecoveryRecommendation(List<RecoveryPick> recommendations, String narrative) {
            this(recommendations, narrative, AiSource.LOCAL);
        }
    }
}
