-- ============================================================
--  SIH26043 - Societal Innovation Collaboration Platform
--  MySQL 8 schema (source of truth)
-- ============================================================
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS sih_platform
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sih_platform;

-- ------------------------------------------------------------
-- Roles
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255),
  version     BIGINT NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- Problem categories
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS problem_categories (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(1000),
  version     BIGINT NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- Universities
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS universities (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(200) NOT NULL,
  location    VARCHAR(200),
  website     VARCHAR(255),
  description VARCHAR(2000),
  version     BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_universities_name (name)
);

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(150) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password      VARCHAR(100) NOT NULL,
  phone         VARCHAR(30),
  role_id       BIGINT NULL,
  linked_university_id BIGINT NULL,
  status        VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version       BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL,
  CONSTRAINT fk_users_univ FOREIGN KEY (linked_university_id) REFERENCES universities(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Problems (citizen-reported societal challenges)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS problems (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  submitter_id           BIGINT NOT NULL,
  title                  VARCHAR(250) NOT NULL,
  description            TEXT NOT NULL,
  category_id            BIGINT NULL,
  location               VARCHAR(255),
  estimated_beneficiaries INT DEFAULT 0,
  urgency                VARCHAR(20) DEFAULT 'MEDIUM',
  required_outcome       VARCHAR(1000),
  document_url           VARCHAR(500),
  status                 VARCHAR(30) DEFAULT 'SUBMITTED',
  ai_category            VARCHAR(150),
  ai_urgency             VARCHAR(20),
  ai_confidence          DECIMAL(5,4),
  ai_skills              VARCHAR(1000),
  ai_keywords            VARCHAR(1000),
  ai_explanation         TEXT,
  ai_root_causes         TEXT,
  ai_target_beneficiaries TEXT,
  ai_challenges          TEXT,
  ai_solution_directions TEXT,
  ai_expected_outcomes   TEXT,
  ai_source              VARCHAR(20),
  ai_similarity          DECIMAL(5,4) NULL,
  similar_problem_id     BIGINT NULL,
  created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version                BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_problems_submitter  FOREIGN KEY (submitter_id) REFERENCES users(id),
  CONSTRAINT fk_problems_category   FOREIGN KEY (category_id) REFERENCES problem_categories(id),
  CONSTRAINT fk_problems_similar    FOREIGN KEY (similar_problem_id) REFERENCES problems(id)
);

-- ------------------------------------------------------------
-- Capability decomposition of a problem
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS problem_capabilities (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  problem_id     BIGINT NOT NULL,
  capability     VARCHAR(200) NOT NULL,
  required_skills VARCHAR(500),
  description    VARCHAR(1000),
  priority       VARCHAR(20) DEFAULT 'MEDIUM',
  version        BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_caps_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
  UNIQUE KEY uk_problem_capabilities_problem_capability (problem_id, capability)
);

-- ------------------------------------------------------------
-- Departments
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  university_id BIGINT NOT NULL,
  name          VARCHAR(200) NOT NULL,
  description   VARCHAR(1000),
  version       BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_departments_univ FOREIGN KEY (university_id) REFERENCES universities(id) ON DELETE CASCADE,
  UNIQUE KEY uk_departments_univ_name (university_id, name)
);

-- ------------------------------------------------------------
-- Faculty / mentors
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS faculty (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  university_id    BIGINT NOT NULL,
  department_id    BIGINT NULL,
  name             VARCHAR(150) NOT NULL,
  title            VARCHAR(150),
  expertise        VARCHAR(1000),
  research_area    VARCHAR(500),
  previous_projects VARCHAR(1500),
  email            VARCHAR(150) NULL,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_faculty_univ  FOREIGN KEY (university_id) REFERENCES universities(id) ON DELETE CASCADE,
  CONSTRAINT fk_faculty_dept  FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
  UNIQUE KEY uk_faculty_email (email)
);

-- ------------------------------------------------------------
-- Student teams
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS student_teams (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  university_id     BIGINT NOT NULL,
  lead_id           BIGINT NULL,
  name              VARCHAR(150) NOT NULL,
  description       VARCHAR(1500),
  skills            VARCHAR(1000),
  technologies      VARCHAR(1000),
  interests         VARCHAR(1000),
  availability      VARCHAR(100),
  previous_projects VARCHAR(1500),
  status            VARCHAR(30) DEFAULT 'ACTIVE',
  version           BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_teams_univ FOREIGN KEY (university_id) REFERENCES universities(id),
  CONSTRAINT fk_teams_lead FOREIGN KEY (lead_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Team members
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS team_members (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  team_id      BIGINT NOT NULL,
  user_id      BIGINT NOT NULL,
  role_in_team VARCHAR(100) DEFAULT 'MEMBER',
  version      BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES student_teams(id) ON DELETE CASCADE,
  CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uk_team_members_team_user (team_id, user_id)
);

-- ------------------------------------------------------------
-- Industries
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS industries (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  name                   VARCHAR(200) NOT NULL,
  sector                 VARCHAR(150),
  description            VARCHAR(2000),
  expertise              VARCHAR(1000),
  technologies           VARCHAR(1000),
  hardware_capabilities  VARCHAR(1000),
  funding_support        VARCHAR(500),
  mentorship             VARCHAR(500),
  testing_capability     VARCHAR(500),
  deployment_capability  VARCHAR(500),
  location               VARCHAR(200),
  version                BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_industries_name (name)
);

-- ------------------------------------------------------------
-- Industry capabilities
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS industry_capabilities (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  industry_id BIGINT NOT NULL,
  capability VARCHAR(200) NOT NULL,
  description VARCHAR(500),
  version    BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_ic_industry FOREIGN KEY (industry_id) REFERENCES industries(id) ON DELETE CASCADE,
  UNIQUE KEY uk_industry_capabilities_industry_capability (industry_id, capability)
);

-- ------------------------------------------------------------
-- Projects (collaboration workspace)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  problem_id       BIGINT NOT NULL,
  title            VARCHAR(250) NOT NULL,
  description      VARCHAR(2000),
  university_id    BIGINT NULL,
  team_id          BIGINT NULL,
  lead_mentor_id   BIGINT NULL,
  industry_id      BIGINT NULL,
  status           VARCHAR(30) DEFAULT 'SUBMITTED',
  progress_percent INT DEFAULT 0,
  failure_reason   VARCHAR(1000),
  created_by       BIGINT NULL,
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_projects_problem    FOREIGN KEY (problem_id) REFERENCES problems(id),
  CONSTRAINT fk_projects_univ       FOREIGN KEY (university_id) REFERENCES universities(id),
  CONSTRAINT fk_projects_team       FOREIGN KEY (team_id) REFERENCES student_teams(id),
  CONSTRAINT fk_projects_mentor     FOREIGN KEY (lead_mentor_id) REFERENCES faculty(id),
  CONSTRAINT fk_projects_industry   FOREIGN KEY (industry_id) REFERENCES industries(id),
  CONSTRAINT fk_projects_creator    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Project members
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS project_members (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id      BIGINT NOT NULL,
  user_id         BIGINT NOT NULL,
  role_in_project VARCHAR(100) DEFAULT 'MEMBER',
  joined_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version         BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_pm_user    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uk_project_members_project_user (project_id, user_id)
);

-- ------------------------------------------------------------
-- Tasks
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tasks (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id   BIGINT NOT NULL,
  assigned_to  BIGINT NULL,
  title        VARCHAR(250) NOT NULL,
  description  VARCHAR(2000),
  status       VARCHAR(30) DEFAULT 'OPEN',
  priority     VARCHAR(20) DEFAULT 'MEDIUM',
  due_date     DATE NULL,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version      BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_tasks_project  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_tasks_assignee FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Milestones
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS milestones (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id   BIGINT NOT NULL,
  title        VARCHAR(250) NOT NULL,
  description  VARCHAR(1500),
  status       VARCHAR(30) DEFAULT 'PENDING',
  target_date  DATE NULL,
  completed_at TIMESTAMP NULL,
  version      BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_ms_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Comments (incl. mentor/industry feedback)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id   BIGINT NOT NULL,
  author_id    BIGINT NOT NULL,
  comment_type VARCHAR(30) DEFAULT 'GENERAL',
  content      TEXT NOT NULL,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version      BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_comments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_author  FOREIGN KEY (author_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Documents / files
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS documents (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id   BIGINT NOT NULL,
  uploader_id  BIGINT NULL,
  name         VARCHAR(250) NOT NULL,
  url          VARCHAR(500),
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version      BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_docs_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_docs_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Matches (polymorphic: entity_type + entity_id)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS matches (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  problem_id   BIGINT NOT NULL,
  entity_type  VARCHAR(30) NOT NULL,
  entity_id    BIGINT NOT NULL,
  score        DECIMAL(5,4) NOT NULL,
  explanation  VARCHAR(2000),
  contributing_factors VARCHAR(1500),
  status       VARCHAR(30) DEFAULT 'PROPOSED',
  source       VARCHAR(20),
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version      BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_matches_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
  UNIQUE KEY uk_matches_problem_entity (problem_id, entity_type, entity_id)
);

-- ------------------------------------------------------------
-- Project status history
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS project_status_history (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  status     VARCHAR(30) NOT NULL,
  note       VARCHAR(1000),
  changed_by BIGINT NULL,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version    BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_psh_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_psh_user    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Failure records (failure recovery)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS failure_records (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id         BIGINT NOT NULL,
  stage              VARCHAR(30),
  reason             VARCHAR(2000),
  missing_capability VARCHAR(300),
  reported_by        BIGINT NULL,
  resolved           BOOLEAN DEFAULT FALSE,
  created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version            BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_fr_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_fr_user    FOREIGN KEY (reported_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Impact records
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS impact_records (
  id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id            BIGINT NOT NULL,
  beneficiaries         BIGINT DEFAULT 0,
  locations_covered     INT DEFAULT 0,
  cost_savings          DECIMAL(14,2) DEFAULT 0,
  problem_reduction     VARCHAR(1000),
  duration_months       INT DEFAULT 0,
  implementation_percent INT DEFAULT 0,
  domain_metrics        VARCHAR(1500),
  verification_status   VARCHAR(30) DEFAULT 'PENDING',
  submitted_by          BIGINT NULL,
  submitted_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version               BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_ir_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_ir_user    FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE SET NULL,
  UNIQUE KEY uk_impact_records_project (project_id)
);

-- ------------------------------------------------------------
-- Impact evidence
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS impact_evidence (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  impact_record_id BIGINT NOT NULL,
  evidence_type    VARCHAR(50) DEFAULT 'DOCUMENT',
  description      VARCHAR(2000),
  file_url         VARCHAR(500),
  status           VARCHAR(30) DEFAULT 'PENDING',
  submitted_by     BIGINT NULL,
  reviewed_by      BIGINT NULL,
  reviewed_at      TIMESTAMP NULL,
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_ie_record  FOREIGN KEY (impact_record_id) REFERENCES impact_records(id) ON DELETE CASCADE,
  CONSTRAINT fk_ie_submit  FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_ie_review  FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Notifications
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  message    VARCHAR(500) NOT NULL,
  type       VARCHAR(50) DEFAULT 'INFO',
  link       VARCHAR(255),
  is_read    BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version    BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Indexes for the hot queries
-- ------------------------------------------------------------
CREATE INDEX idx_problems_status  ON problems(status);
CREATE INDEX idx_problems_cat     ON problems(category_id);
CREATE INDEX idx_projects_status  ON projects(status);
CREATE INDEX idx_matches_problem  ON matches(problem_id);
CREATE INDEX idx_matches_type     ON matches(entity_type);
CREATE INDEX idx_faculty_univ     ON faculty(university_id);
CREATE INDEX idx_teams_univ       ON student_teams(university_id);
CREATE INDEX idx_problems_status_category ON problems(status, category_id);
CREATE INDEX idx_problems_submitter_created ON problems(submitter_id, created_at);
CREATE INDEX idx_projects_problem_status ON projects(problem_id, status);
CREATE INDEX idx_projects_univ_status ON projects(university_id, status);
CREATE INDEX idx_matches_problem_entity_lookup ON matches(problem_id, entity_type, entity_id);
CREATE INDEX idx_impact_records_project_status ON impact_records(project_id, verification_status);
CREATE INDEX idx_project_members_user_project ON project_members(user_id, project_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read, created_at);
