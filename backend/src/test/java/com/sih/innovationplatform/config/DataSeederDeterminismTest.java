package com.sih.innovationplatform.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSeederDeterminismTest {

    @Test
    void seedScoresAreStableAndBounded() {
        assertEquals(0.90, DataSeeder.seedScore(5, "UNIVERSITY"), 0.000001);
        assertEquals(0.92, DataSeeder.seedScore(1, "TEAM"), 0.000001);
        assertEquals(0.88, DataSeeder.seedScore(0, "MENTOR"), 0.000001);
        assertEquals(0.87, DataSeeder.seedScore(0, "INDUSTRY"), 0.000001);
        assertEquals(DataSeeder.seedScore(3, "UNIVERSITY"), DataSeeder.seedScore(3, "UNIVERSITY"), 0.000001);
        assertTrue(DataSeeder.seedScore(99, "UNIVERSITY") <= 0.99);
    }
}
