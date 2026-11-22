package com.sih.innovationplatform.project;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void invalidProjectStatusIsRejected() {
        assertFalse(validator.validate(new StatusRequest("APPROVED", null)).isEmpty());
    }

    @Test
    void validProjectStatusPassesValidation() {
        assertTrue(validator.validate(new StatusRequest("IMPLEMENTATION", "Moving to rollout")).isEmpty());
    }

    @Test
    void progressAboveHundredIsRejected() {
        assertFalse(validator.validate(new ProgressRequest(120)).isEmpty());
    }

    @Test
    void taskWithoutTitleIsRejected() {
        assertFalse(validator.validate(new TaskRequest(" ", "Do work", null, "HIGH")).isEmpty());
    }

    @Test
    void failureWithoutReasonIsRejected() {
        assertFalse(validator.validate(new FailureRequest("PILOT", " ", null)).isEmpty());
    }
}
