package com.sih.innovationplatform.impact;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

record ImpactDtos() {

    record ImpactResult(
            Long id,
            Long projectId,
            String projectTitle,
            Long beneficiaries,
            Integer locationsCovered,
            Double costSavings,
            String problemReduction,
            Integer durationMonths,
            Integer implementationPercent,
            String domainMetrics,
            String verificationStatus,
            String submittedBy,
            String submittedAt,
            List<EvidenceResult> evidence
    ) {}

    record EvidenceResult(
            Long id,
            Long impactRecordId,
            String evidenceType,
            String description,
            String fileUrl,
            String status,
            String submittedBy,
            String reviewedBy,
            String reviewedAt
    ) {}

    record SubmitRequest(
            @NotNull(message = "Beneficiaries is required")
            @PositiveOrZero(message = "Beneficiaries cannot be negative")
            Long beneficiaries,
            @PositiveOrZero(message = "Locations covered cannot be negative")
            Integer locationsCovered,
            @PositiveOrZero(message = "Cost savings cannot be negative")
            Double costSavings,
            @Size(max = 1000, message = "Problem reduction must be 1000 characters or fewer")
            String problemReduction,
            @PositiveOrZero(message = "Duration cannot be negative")
            Integer durationMonths,
            @Min(value = 0, message = "Implementation percent cannot be negative")
            @Max(value = 100, message = "Implementation percent cannot exceed 100")
            Integer implementationPercent,
            @Size(max = 1500, message = "Domain metrics must be 1500 characters or fewer")
            String domainMetrics
    ) {}

    record EvidenceRequest(
            @NotNull(message = "Impact record is required")
            @Positive(message = "Impact record is required")
            Long impactRecordId,
            @NotBlank(message = "Evidence type is required")
            @Pattern(regexp = "(?i)^(WITNESS_STATEMENT|OFFICIAL_RECORD|PROJECT_DATA|AUDIT_REPORT|PHOTO|DOCUMENT)$", message = "Invalid evidence type")
            String evidenceType,
            @NotBlank(message = "Evidence description is required")
            @Size(max = 2000, message = "Evidence description must be 2000 characters or fewer")
            String description,
            @Size(max = 500, message = "Evidence URL must be 500 characters or fewer")
            String fileUrl
    ) {}

    record ReviewRequest(
            @NotBlank(message = "Review status is required")
            @Pattern(regexp = "(?i)^(VERIFIED|REJECTED)$", message = "Review status must be VERIFIED or REJECTED")
            String status
    ) {}
}