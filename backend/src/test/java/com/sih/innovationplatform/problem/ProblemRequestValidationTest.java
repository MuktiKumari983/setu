package com.sih.innovationplatform.problem;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void blankTitleIsRejected() {
        ProblemCreateRequest request = new ProblemCreateRequest(
                " ", "A valid description", "Pune", 10, "MEDIUM", "Clean water", "");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validCreateRequestPassesValidation() {
        ProblemCreateRequest request = new ProblemCreateRequest(
                "Contaminated river", "River near the village is contaminated", "Pune", 10, "HIGH", "Clean water", "");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void invalidUrgencyIsRejected() {
        ProblemUpdateRequest request = new ProblemUpdateRequest(
                null, null, null, null, "URGENT", null, null);

        assertFalse(validator.validate(request).isEmpty());
    }
}
