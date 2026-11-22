package com.sih.innovationplatform.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void apiExceptionUsesItsOwnStatusAndMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleApi(ApiException.forbidden("Not allowed"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Not allowed", response.getBody().get("message"));
    }

    @Test
    void genericExceptionDoesNotLeakInternalMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(new RuntimeException("secret connection string"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("message"));
        assertFalse(String.valueOf(response.getBody()).contains("secret"));
    }
}
