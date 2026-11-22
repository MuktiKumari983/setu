DROP PROCEDURE IF EXISTS sih_add_column;
DROP PROCEDURE IF EXISTS sih_add_index;
DROP PROCEDURE IF EXISTS sih_add_fk;

DELIMITER $$

CREATE PROCEDURE sih_add_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition VARCHAR(500))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @sih_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE sih_stmt FROM @sih_sql;
        EXECUTE sih_stmt;
        DEALLOCATE PREPARE sih_stmt;
    END IF;
END$$

CREATE PROCEDURE sih_add_index(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_columns VARCHAR(255), IN p_unique BOOLEAN)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
    ) THEN
        SET @sih_sql = CONCAT('ALTER TABLE `', p_table, '` ADD ', IF(p_unique, 'UNIQUE ', ''), 'INDEX `', p_index, '` (`', REPLACE(p_columns, ',', '`,`'), '`)');
        PREPARE sih_stmt FROM @sih_sql;
        EXECUTE sih_stmt;
        DEALLOCATE PREPARE sih_stmt;
    END IF;
END$$

CREATE PROCEDURE sih_add_fk(IN p_table VARCHAR(64), IN p_constraint VARCHAR(64), IN p_column VARCHAR(64), IN p_ref_table VARCHAR(64), IN p_ref_column VARCHAR(64), IN p_on_delete VARCHAR(20))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND CONSTRAINT_NAME = p_constraint AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        SET @sih_sql = CONCAT('ALTER TABLE `', p_table, '` ADD CONSTRAINT `', p_constraint, '` FOREIGN KEY (`', p_column, '`) REFERENCES `', p_ref_table, '` (`', p_ref_column, '`) ON DELETE ', p_on_delete);
        PREPARE sih_stmt FROM @sih_sql;
        EXECUTE sih_stmt;
        DEALLOCATE PREPARE sih_stmt;
    END IF;
END$$

DELIMITER ;

CALL sih_add_column('roles', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('users', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('problem_categories', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('problems', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('problem_capabilities', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('universities', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('departments', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('faculty', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('student_teams', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('team_members', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('industries', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('industry_capabilities', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('projects', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('project_members', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('tasks', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('milestones', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('comments', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('documents', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('matches', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('project_status_history', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('failure_records', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('impact_records', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('impact_evidence', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('notifications', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL sih_add_column('problems', 'required_outcome', 'VARCHAR(1000)');
CALL sih_add_column('problems', 'ai_root_causes', 'TEXT');
CALL sih_add_column('problems', 'ai_target_beneficiaries', 'TEXT');
CALL sih_add_column('problems', 'ai_challenges', 'TEXT');
CALL sih_add_column('problems', 'ai_solution_directions', 'TEXT');
CALL sih_add_column('problems', 'ai_expected_outcomes', 'TEXT');
CALL sih_add_column('problems', 'ai_source', 'VARCHAR(20)');
CALL sih_add_column('matches', 'source', 'VARCHAR(20)');

UPDATE users SET role_id = NULL
WHERE role_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM roles r WHERE r.id = users.role_id);

UPDATE users SET linked_university_id = NULL
WHERE linked_university_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM universities u WHERE u.id = users.linked_university_id);

DELETE FROM team_members
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM team_members GROUP BY team_id, user_id
    ) duplicates
);

DELETE FROM industry_capabilities
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM industry_capabilities GROUP BY industry_id, capability
    ) duplicates
);

DELETE FROM problem_capabilities
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM problem_capabilities GROUP BY problem_id, capability
    ) duplicates
);

DELETE FROM project_members
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM project_members GROUP BY project_id, user_id
    ) duplicates
);

DELETE FROM matches
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM matches GROUP BY problem_id, entity_type, entity_id
    ) duplicates
);

DELETE FROM impact_records
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM impact_records GROUP BY project_id
    ) duplicates
);

UPDATE universities u
JOIN (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY name ORDER BY id) AS rn
    FROM universities
) d ON d.id = u.id
SET u.name = CONCAT(u.name, '-', u.id)
WHERE d.rn > 1;

UPDATE industries i
JOIN (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY name ORDER BY id) AS rn
    FROM industries
) d ON d.id = i.id
SET i.name = CONCAT(i.name, '-', i.id)
WHERE d.rn > 1;

UPDATE faculty f
JOIN (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY id) AS rn
    FROM faculty
    WHERE email IS NOT NULL
) d ON d.id = f.id
SET f.email = CONCAT(CAST(f.id AS CHAR), '-', f.email)
WHERE d.rn > 1;

CALL sih_add_fk('users', 'fk_users_role', 'role_id', 'roles', 'id', 'SET NULL');
CALL sih_add_fk('users', 'fk_users_univ', 'linked_university_id', 'universities', 'id', 'SET NULL');

CALL sih_add_index('universities', 'uk_universities_name', 'name', TRUE);
CALL sih_add_index('industries', 'uk_industries_name', 'name', TRUE);
CALL sih_add_index('faculty', 'uk_faculty_email', 'email', TRUE);
CALL sih_add_index('departments', 'uk_departments_univ_name', 'university_id,name', TRUE);
CALL sih_add_index('problem_capabilities', 'uk_problem_capabilities_problem_capability', 'problem_id,capability', TRUE);
CALL sih_add_index('team_members', 'uk_team_members_team_user', 'team_id,user_id', TRUE);
CALL sih_add_index('industry_capabilities', 'uk_industry_capabilities_industry_capability', 'industry_id,capability', TRUE);
CALL sih_add_index('project_members', 'uk_project_members_project_user', 'project_id,user_id', TRUE);
CALL sih_add_index('matches', 'uk_matches_problem_entity', 'problem_id,entity_type,entity_id', TRUE);
CALL sih_add_index('impact_records', 'uk_impact_records_project', 'project_id', TRUE);

CALL sih_add_index('problems', 'idx_problems_status', 'status', FALSE);
CALL sih_add_index('problems', 'idx_problems_cat', 'category_id', FALSE);
CALL sih_add_index('projects', 'idx_projects_status', 'status', FALSE);
CALL sih_add_index('matches', 'idx_matches_problem', 'problem_id', FALSE);
CALL sih_add_index('matches', 'idx_matches_type', 'entity_type', FALSE);
CALL sih_add_index('faculty', 'idx_faculty_univ', 'university_id', FALSE);
CALL sih_add_index('student_teams', 'idx_teams_univ', 'university_id', FALSE);
CALL sih_add_index('problems', 'idx_problems_status_category', 'status,category_id', FALSE);
CALL sih_add_index('problems', 'idx_problems_submitter_created', 'submitter_id,created_at', FALSE);
CALL sih_add_index('projects', 'idx_projects_problem_status', 'problem_id,status', FALSE);
CALL sih_add_index('projects', 'idx_projects_univ_status', 'university_id,status', FALSE);
CALL sih_add_index('matches', 'idx_matches_problem_entity_lookup', 'problem_id,entity_type,entity_id', FALSE);
CALL sih_add_index('impact_records', 'idx_impact_records_project_status', 'project_id,verification_status', FALSE);
CALL sih_add_index('project_members', 'idx_project_members_user_project', 'user_id,project_id', FALSE);
CALL sih_add_index('notifications', 'idx_notifications_user_read', 'user_id,is_read,created_at', FALSE);

DROP PROCEDURE IF EXISTS sih_add_column;
DROP PROCEDURE IF EXISTS sih_add_index;
DROP PROCEDURE IF EXISTS sih_add_fk;
