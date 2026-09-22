package com.sih.innovationplatform.problem;

import java.util.List;

record ProblemCreateRequest(
        String title,
        String description,
        String location,
        Integer estimatedBeneficiaries,
        String urgency,
        String requiredOutcome,
        String documentUrl
) {}

record ProblemUpdateRequest(
        String title,
        String description,
        String location,
        Integer estimatedBeneficiaries,
        String urgency,
        String requiredOutcome,
        String documentUrl
) {}

record ProblemResponse(
        Long id,
        String title,
        String description,
        UserBrief submitter,
        CategoryBrief category,
        String location,
        Integer estimatedBeneficiaries,
        String urgency,
        String requiredOutcome,
        String documentUrl,
        String status,
        String aiCategory,
        String aiUrgency,
        Double aiConfidence,
        String aiSkills,
        String aiKeywords,
        String aiExplanation,
        Double aiSimilarity,
        DuplicateBrief duplicateOf,
        List<CapabilityBrief> capabilities,
        String createdAt
) {
    record UserBrief(Long id, String name, String email) {}
    record CategoryBrief(Long id, String name) {}
    record DuplicateBrief(Long id, String title, Double similarity) {}
    record CapabilityBrief(Long id, String capability, String description, List<String> requiredSkills, String priority) {}
}