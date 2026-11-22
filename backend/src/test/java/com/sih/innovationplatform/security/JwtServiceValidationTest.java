package com.sih.innovationplatform.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceValidationTest {

    private static final String DEV_SECRET = "dev-secret-change-me-please-32-characters-minimum";
    private static final String STRONG_SECRET = "a-unique-random-jwt-secret-with-48-bytes";

    @Test
    void developmentDefaultIsRejectedUnlessExplicitlyAllowed() {
        assertThrows(IllegalStateException.class, () -> new JwtService(DEV_SECRET, 60000L, false));
        assertDoesNotThrow(() -> new JwtService(DEV_SECRET, 60000L, true));
    }

    @Test
    void shortSecretIsRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 60000L, false));
    }

    @Test
    void strongSecretIsAccepted() {
        assertDoesNotThrow(() -> new JwtService(STRONG_SECRET, 60000L, false));
    }
}
