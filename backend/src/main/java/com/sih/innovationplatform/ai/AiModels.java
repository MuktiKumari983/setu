package com.sih.innovationplatform.ai;

import java.util.List;
import java.util.Map;

/**
 * Shared models for the AI / matching contract.
 * Types are deliberately simple maps/records so a heavier model can be
 * dropped into the service later without touching the domain code.
 */
public final class AiModels {

    private AiModels() {
    }

    public record AnalyzeResult(String category, String urgency, List<String> requiredSkills,
                                List<String> keywords, double confidence, String explanation) {
    }

    public record ExistingProblem(Long id, String title, String description) {
    }

    public record SimilarProblem(Long id, String title, double similarity) {
    }

    public record Capability(String capability, String description, List<String> requiredSkills, String priority) {
    }

    public record CandidateProfile(Long id, String name, String description,
                                   List<String> skills, List<String> tags, List<String> capabilities) {
    }

    public record MatchingRequirement(String category, List<String> keywords,
                                      List<String> requiredSkills, List<String> capabilities) {
    }

    public record ScoredMatch(Long id, double score, String explanation, List<String> contributingFactors) {
    }

    public record StakeholderPick(String type, Long id, String name, double score,
                                  String reason, String contribution, List<String> gapSkills,
                                  Map<String, Object> details) {
    }

    public record Recommendation(List<StakeholderPick> stakeholders, String narrative) {
    }

    public record RecoveryPick(String entityType, Long id, String name, double score,
                               String reason, boolean missingGapClosed) {
    }

    public record RecoveryRecommendation(List<RecoveryPick> recommendations, String narrative) {
    }
}