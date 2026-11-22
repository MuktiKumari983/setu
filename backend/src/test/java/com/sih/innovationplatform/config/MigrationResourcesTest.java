package com.sih.innovationplatform.config;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationResourcesTest {

    @Test
    void baselineMigrationContainsHardenedSchema() throws Exception {
        String sql = read("db/migration/V1__baseline.sql");

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS users"));
        assertTrue(sql.contains("BIGINT NOT NULL DEFAULT 0"));
        assertTrue(sql.contains("CONSTRAINT fk_users_role"));
        assertTrue(sql.contains("UNIQUE KEY uk_matches_problem_entity"));
        assertTrue(sql.contains("CREATE INDEX idx_projects_problem_status"));
    }

    @Test
    void upgradeMigrationIsIdempotentAndAddsMissingObjects() throws Exception {
        String sql = read("db/migration/V2__harden_schema.sql");

        assertTrue(sql.contains("CREATE PROCEDURE sih_add_column"));
        assertTrue(sql.contains("information_schema.COLUMNS"));
        assertTrue(sql.contains("CALL sih_add_column('matches', 'source'"));
        assertTrue(sql.contains("CALL sih_add_fk('users', 'fk_users_role'"));
        assertTrue(sql.contains("CALL sih_add_index('projects', 'idx_projects_problem_status'"));
    }

    private String read(String resource) throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
