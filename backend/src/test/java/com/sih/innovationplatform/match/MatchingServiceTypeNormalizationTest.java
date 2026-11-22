package com.sih.innovationplatform.match;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchingServiceTypeNormalizationTest {

    @Test
    void lowercaseAiTypeIsUppercased() {
        assertEquals("TEAM", MatchingService.normalizeMatchType("team"));
    }

    @Test
    void studentTeamAliasIsMappedToTeam() {
        assertEquals("TEAM", MatchingService.normalizeMatchType("STUDENT_TEAM"));
    }

    @Test
    void nullTypeBecomesEmptyString() {
        assertEquals("", MatchingService.normalizeMatchType(null));
    }
}
