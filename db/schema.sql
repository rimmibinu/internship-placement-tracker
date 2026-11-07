-- Reference schema (MySQL 8). The application creates/updates these tables itself via
-- spring.jpa.hibernate.ddl-auto=update, so running this script is OPTIONAL.
-- Run it if you want to create the schema manually or inspect it.
CREATE DATABASE IF NOT EXISTS placement_db;
USE placement_db;

CREATE TABLE IF NOT EXISTS companies (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(120) NOT NULL UNIQUE,
  industry    VARCHAR(80),
  website     VARCHAR(200),
  location    VARCHAR(100),
  description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS students (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(100) NOT NULL,
  email           VARCHAR(120) NOT NULL UNIQUE,
  roll_no         VARCHAR(30)  NOT NULL UNIQUE,
  department      VARCHAR(20)  NOT NULL,
  cgpa            DOUBLE       NOT NULL CHECK (cgpa BETWEEN 0 AND 10),
  backlogs        INT          NOT NULL CHECK (backlogs >= 0),
  graduation_year INT          NOT NULL,
  skills          VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS opportunities (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  title               VARCHAR(150) NOT NULL,
  opportunity_type    VARCHAR(20)  NOT NULL,          -- INTERNSHIP | PLACEMENT
  company_id          BIGINT       NOT NULL,
  description         VARCHAR(2000),
  location            VARCHAR(100),
  salary              DOUBLE,
  min_cgpa            DOUBLE       NOT NULL,
  max_backlogs        INT          NOT NULL,
  allowed_departments VARCHAR(200) NOT NULL DEFAULT 'ALL',
  openings            INT          NOT NULL DEFAULT 1,
  deadline            DATE         NOT NULL,
  status              VARCHAR(10)  NOT NULL DEFAULT 'OPEN',   -- OPEN | CLOSED
  created_at          DATETIME     NOT NULL,
  CONSTRAINT fk_opp_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS applications (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id     BIGINT      NOT NULL,
  opportunity_id BIGINT      NOT NULL,
  status         VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
  cover_note     VARCHAR(500),
  remarks        VARCHAR(300),
  applied_at     DATETIME    NOT NULL,
  updated_at     DATETIME    NOT NULL,
  CONSTRAINT uk_student_opportunity UNIQUE (student_id, opportunity_id),
  CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_app_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id)
);

CREATE INDEX idx_app_status ON applications(status);
CREATE INDEX idx_opp_deadline ON opportunities(deadline);
