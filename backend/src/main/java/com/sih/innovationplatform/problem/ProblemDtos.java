package com.sih.innovationplatform.problem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

record ProblemCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 250, message = "Title must be 250 characters or fewer")
        String title,
        @NotBlank(message = "Description is required")
        @Size(max = 10000, message = "Description must be 10000 characters or fewer")
        String description,
        @Size(max = 255, message = "Location must be 255 characters or fewer")
        String location,
        @Min(value = 0, message = "Estimated beneficiaries cannot be negative")
        Integer estimatedBeneficiaries,
        @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)?$", message = "Invalid urgency")
        String urgency,
        @Size(max = 1000, message = "Required outcome must be 1000 characters or fewer")
        String requiredOutcome,
        @Size(max = 500, message = "Document URL must be 500 characters or fewer")
        String documentUrl
) {}

record ProblemUpdateRequest(
        @Size(max = 250, message = "Title must be 250 characters or fewer")
        String title,
        @Size(max = 10000, message = "Description must be 10000 characters or fewer")
        String description,
        @Size(max = 255, message = "Location must be 255 characters or fewer")
        String location,
        @Min(value = 0, message = "Estimated beneficiaries cannot be negative")
        Integer estimatedBeneficiaries,
        @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)?$", message = "Invalid urgency")
        String urgency,
        @Size(max = 1000, message = "Required outcome must be 1000 characters or fewer")
        String requiredOutcome,
        @Size(max = 500, message = "Document URL must be 500 characters or fewer")
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
        String aiRootCauses,
        String aiTargetBeneficiaries,
        String aiChallenges,
        String aiSolutionDirections,
        String aiExpectedOutcomes,
        String aiSource,
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