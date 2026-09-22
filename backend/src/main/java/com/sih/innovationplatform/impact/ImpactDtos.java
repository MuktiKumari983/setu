package com.sih.innovationplatform.impact;

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
            Long beneficiaries,
            Integer locationsCovered,
            Double costSavings,
            String problemReduction,
            Integer durationMonths,
            Integer implementationPercent,
            String domainMetrics
    ) {}

    record EvidenceRequest(Long impactRecordId, String evidenceType, String description, String fileUrl) {}

    record ReviewRequest(String status) {}
}