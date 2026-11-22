package com.sih.innovationplatform.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtServiceTokenLifecycleTest {

    private static final String STRONG_SECRET = "a-unique-random-jwt-secret-with-48-bytes";

    @Test
    void generatedTokenRoundTripsSubjectRoleAndUid() {
        JwtService jwtService = new JwtService(STRONG_SECRET, 60000L, false);

        String token = jwtService.generateToken("user@example.com", "STUDENT", 7L);
        Claims claims = jwtService.parse(token);

        assertEquals("user@example.com", claims.getSubject());
        assertEquals("user@example.com", jwtService.extractEmail(token));
        assertEquals("STUDENT", claims.get("role", String.class));
        assertEquals(7L, ((Number) claims.get("uid")).longValue());
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService jwtService = new JwtService(STRONG_SECRET, -1000L, false);

        String token = jwtService.generateToken("user@example.com", "STUDENT", 7L);

        assertFalse(jwtService.isValid(token));
    }

    @Test
    void tamperedTokenIsInvalid() {
        JwtService jwtService = new JwtService(STRONG_SECRET, 60000L, false);
        String token = jwtService.generateToken("user@example.com", "STUDENT", 7L);
        String[] parts = token.split("\\.");
        char first = parts[2].charAt(0);
        parts[2] = (first == 'A' ? 'B' : 'A') + parts[2].substring(1);
        String tampered = String.join(".", parts);

        assertFalse(jwtService.isValid(tampered));
    }
}
