package com.sih.innovationplatform.impact;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImpactRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void evidenceWithoutRecordIdIsRejected() {
        ImpactDtos.EvidenceRequest request =
                new ImpactDtos.EvidenceRequest(null, "WITNESS_STATEMENT", "Photo of handover", "");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void approvedIsNotAValidReviewStatus() {
        ImpactDtos.ReviewRequest request = new ImpactDtos.ReviewRequest("APPROVED");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void verifiedReviewStatusPassesValidation() {
        ImpactDtos.ReviewRequest request = new ImpactDtos.ReviewRequest("VERIFIED");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void implementationPercentAboveHundredIsRejected() {
        ImpactDtos.SubmitRequest request =
                new ImpactDtos.SubmitRequest(100L, 2, 0.0, "Reduced incidents", 6, 150, "");

        assertFalse(validator.validate(request).isEmpty());
    }
}
