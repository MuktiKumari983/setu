package com.sih.innovationplatform.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.innovationplatform.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiGatewayContractTest {

    private final RemoteAiGateway gateway = new RemoteAiGateway(new AppProperties(), new ObjectMapper());

    @Test
    void candidatePayloadIncludesAvailabilityAndExperience() {
        AiModels.CandidateProfile candidate = new AiModels.CandidateProfile(
                1L, "Team", "water sensors", List.of("IoT"), List.of("river"),
                List.of("Sensor deployment"), "Weekends", List.of("river sensor rollout"));

        Map<String, Object> payload = gateway.candidateMap(candidate);

        assertEquals(Set.of("id", "name", "description", "skills", "tags", "capabilities",
                        "availability", "experience"), payload.keySet());
        assertEquals("Weekends", payload.get("availability"));
        assertEquals(List.of("river sensor rollout"), payload.get("experience"));
    }

    @Test
    void requirementPayloadUsesCanonicalKeys() {
        AiModels.MatchingRequirement requirement = new AiModels.MatchingRequirement(
                "Water Quality", List.of("river"), List.of("IoT"), List.of("Sensors"));

        Map<String, Object> payload = gateway.requirementMap(requirement);

        assertEquals(Set.of("category", "keywords", "requiredSkills", "capabilities"), payload.keySet());
        assertEquals("Water Quality", payload.get("category"));
    }

    @Test
    void entityTypePathUsesCanonicalUppercaseVocabulary() {
        assertEquals("UNIVERSITY", MatchType.UNIVERSITY.forAi());
        assertEquals("TEAM", MatchType.TEAM.forAi());
        assertEquals("MENTOR", MatchType.MENTOR.forAi());
        assertEquals("INDUSTRY", MatchType.INDUSTRY.forAi());
        assertTrue(Set.of("UNIVERSITY", "TEAM", "MENTOR", "INDUSTRY").contains(MatchType.TEAM.forAi()));
    }
}
