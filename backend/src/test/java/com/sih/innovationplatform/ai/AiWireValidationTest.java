package com.sih.innovationplatform.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiWireValidationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void validAnalyzeResponseIsAccepted() {
        String json = """
                {
                  "category": "Water Quality",
                  "urgency": "HIGH",
                  "requiredSkills": ["IoT"],
                  "keywords": ["river"],
                  "confidence": 0.8,
                  "explanation": "matched",
                  "rootCauses": ["Reported signal: river"],
                  "targetBeneficiaries": ["villagers"],
                  "challenges": ["Reported condition: river"],
                  "solutionDirections": ["Assess capability: Sensor deployment"],
                  "expectedOutcomes": ["Not specified in submission"],
                  "source": "REMOTE"
                }
                """;

        assertDoesNotThrow(() -> AiWire.parse(mapper, json, AiWire.AnalyzeResponse.class));
    }

    @Test
    void missingRequiredFieldIsRejected() {
        String json = """
                {
                  "category": "Water Quality",
                  "urgency": "HIGH",
                  "requiredSkills": ["IoT"],
                  "keywords": ["river"],
                  "confidence": 0.8,
                  "explanation": "matched",
                  "rootCauses": [],
                  "targetBeneficiaries": [],
                  "challenges": [],
                  "solutionDirections": [],
                  "expectedOutcomes": []
                }
                """;

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> AiWire.parse(mapper, json, AiWire.AnalyzeResponse.class));
        assertTrue(error.getMessage().contains("Invalid AI response"));
    }

    @Test
    void nonCanonicalEntityTypeIsRejected() {
        String json = """
                {"matchType":"student_team","results":[],"source":"REMOTE"}
                """;

        assertThrows(IllegalArgumentException.class,
                () -> AiWire.parse(mapper, json, AiWire.MatchResponse.class));
    }

    @Test
    void unknownResponseFieldIsRejected() {
        String json = """
                {"matchType":"TEAM","results":[],"source":"REMOTE","unexpected":true}
                """;

        assertThrows(IllegalArgumentException.class,
                () -> AiWire.parse(mapper, json, AiWire.MatchResponse.class));
    }
}
