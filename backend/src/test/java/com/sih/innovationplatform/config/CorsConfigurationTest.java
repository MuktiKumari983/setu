package com.sih.innovationplatform.config;

import com.sih.innovationplatform.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CorsConfigurationTest {

    @Test
    void corsUsesConfiguredOriginAndExplicitHeaders() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class));
        ReflectionTestUtils.setField(config, "corsOrigin", "https://app.example.test");

        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(List.of("https://app.example.test"), cors.getAllowedOrigins());
        assertFalse(cors.getAllowedHeaders().contains("*"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertTrue(cors.getAllowCredentials());
    }
}
