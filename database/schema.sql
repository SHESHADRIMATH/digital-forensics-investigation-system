-- =====================================================================
--  Digital Forensics Investigation System - MySQL schema + demo data
--  Run this once in MySQL Workbench (File > Open SQL Script > Execute)
--  or:  mysql -u root -p < database/schema.sql
-- =====================================================================

DROP DATABASE IF EXISTS forensics_db;
CREATE DATABASE forensics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE forensics_db;

-- Dedicated application account (the web app never logs in as root).
-- Password must match web/WEB-INF/classes/db.properties
CREATE USER IF NOT EXISTS 'forensics_app'@'localhost' IDENTIFIED BY 'Forensics@2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON forensics_db.* TO 'forensics_app'@'localhost';
FLUSH PRIVILEGES;

-- 1. USERS ------------------------------------------------------------
-- Passwords are never stored in plain text: password_hash = SHA-256(salt + password)
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('ADMIN','INVESTIGATOR','ANALYST') NOT NULL,
    password_hash CHAR(64)     NOT NULL,
    salt          CHAR(32)     NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login    DATETIME     NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. CASES ------------------------------------------------------------
CREATE TABLE cases (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    case_number   VARCHAR(20)  NOT NULL UNIQUE,          -- e.g. DF-2026-001
    title         VARCHAR(150) NOT NULL,
    description   TEXT,
    incident_type VARCHAR(60)  NOT NULL,
    priority      ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status        ENUM('OPEN','IN_PROGRESS','ON_HOLD','CLOSED') NOT NULL DEFAULT 'OPEN',
    assigned_to   INT NULL,
    created_by    INT NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    closed_at     DATETIME NULL,
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    FOREIGN KEY (created_by)  REFERENCES users(id)
);

-- 3. EVIDENCE ---------------------------------------------------------
-- The file itself lives on disk (evidence-storage/); MySQL keeps metadata + hash.
CREATE TABLE evidence (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    case_id          INT          NOT NULL,
    file_name        VARCHAR(255) NOT NULL,              -- original name
    stored_name      VARCHAR(300) NOT NULL,              -- name on disk
    evidence_type    ENUM('LOG','DOCUMENT','IMAGE','SCREENSHOT','TEXT','CSV','SYSTEM_REPORT') NOT NULL,
    file_size        BIGINT       NOT NULL,
    sha256_original  CHAR(64)     NOT NULL,              -- hash taken at upload time
    integrity        ENUM('NOT_VERIFIED','VERIFIED','MODIFIED') NOT NULL DEFAULT 'NOT_VERIFIED',
    last_verified_at DATETIME     NULL,
    status           ENUM('COLLECTED','IN_ANALYSIS','ANALYZED','ARCHIVED') NOT NULL DEFAULT 'COLLECTED',
    description      TEXT,
    collected_by     INT          NOT NULL,
    collected_at     DATETIME     NOT NULL,
    uploaded_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id)      REFERENCES cases(id),
    FOREIGN KEY (collected_by) REFERENCES users(id)
);

-- 4. HASH CHECKS (history of every integrity verification) ------------
CREATE TABLE hash_checks (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    evidence_id   INT      NOT NULL,
    computed_hash CHAR(64) NOT NULL,
    result        ENUM('VERIFIED','MODIFIED') NOT NULL,
    checked_by    INT      NOT NULL,
    checked_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evidence_id) REFERENCES evidence(id),
    FOREIGN KEY (checked_by)  REFERENCES users(id)
);

-- 5. CHAIN OF CUSTODY -------------------------------------------------
-- Append-only: the app never UPDATEs or DELETEs custody rows.
CREATE TABLE chain_of_custody (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    evidence_id  INT          NOT NULL,
    action       VARCHAR(60)  NOT NULL,                  -- Collected, Hash Generated, Transferred ...
    performed_by INT          NOT NULL,
    handed_to    INT          NULL,                      -- only for transfers
    notes        VARCHAR(500),
    action_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evidence_id)  REFERENCES evidence(id),
    FOREIGN KEY (performed_by) REFERENCES users(id),
    FOREIGN KEY (handed_to)    REFERENCES users(id)
);

-- 6. TIMELINE EVENTS --------------------------------------------------
CREATE TABLE timeline_events (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    case_id     INT          NOT NULL,
    event_time  DATETIME     NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    source      ENUM('MANUAL','LOG','SYSTEM') NOT NULL DEFAULT 'MANUAL',
    evidence_id INT NULL,
    created_by  INT NOT NULL,
    FOREIGN KEY (case_id)     REFERENCES cases(id),
    FOREIGN KEY (evidence_id) REFERENCES evidence(id),
    FOREIGN KEY (created_by)  REFERENCES users(id)
);

-- 7. FINDINGS ---------------------------------------------------------
CREATE TABLE findings (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    case_id     INT          NOT NULL,
    evidence_id INT NULL,
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    severity    ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
    status      ENUM('OPEN','CONFIRMED','DISMISSED') NOT NULL DEFAULT 'OPEN',
    created_by  INT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id)     REFERENCES cases(id),
    FOREIGN KEY (evidence_id) REFERENCES evidence(id),
    FOREIGN KEY (created_by)  REFERENCES users(id)
);

CREATE INDEX idx_evidence_case ON evidence(case_id);
CREATE INDEX idx_custody_evidence ON chain_of_custody(evidence_id, action_time);
CREATE INDEX idx_timeline_case ON timeline_events(case_id, event_time);

-- =====================================================================
--  DEMO DATA
--  Demo logins (username / password):
--    admin        / Admin@123     (ADMIN)
--    investigator / Invest@123    (INVESTIGATOR)
--    analyst      / Analyst@123   (ANALYST)
-- =====================================================================
INSERT INTO users (username, full_name, role, salt, password_hash) VALUES
('admin',        'System Administrator', 'ADMIN',        'c8f74a5a8852747a0cb9c93286673c8b', '7aabe3bc174a1fcb5ef7ac11689b89bc595e9379564678865e8cf2101b8f1ea9'),
('investigator', 'Priya Nair',           'INVESTIGATOR', 'b40483e1fc634ee5e5e9343b3a7d8154', '69a1ab39f937d5f94b5b0e4fb542bb118f78a629261ec0d935771061399db9e6'),
('analyst',      'Arjun Mehta',          'ANALYST',      'c07a1b28f128a371ac6e3dc2b5517ec9', 'cc756ddc48cc99913a2f04e529bdc3eb92ca4e9dd50e90f572ad921e20141b12');

INSERT INTO cases (case_number, title, description, incident_type, priority, status, assigned_to, created_by, created_at) VALUES
('DF-2026-001', 'Unauthorized access to HR file server',
 'Multiple failed logins followed by a successful login outside office hours. Payroll files were copied to a USB device.',
 'Unauthorized Access', 'HIGH', 'IN_PROGRESS', 2, 1, '2026-09-20 09:30:00'),
('DF-2026-002', 'Phishing email with malicious attachment',
 'Finance employee opened an invoice attachment that dropped a suspicious executable.',
 'Malware / Phishing', 'CRITICAL', 'OPEN', 2, 1, '2026-09-23 14:05:00'),
('DF-2026-003', 'Data leak via personal cloud storage',
 'Confidential design documents found on a public file-sharing link.',
 'Data Leakage', 'MEDIUM', 'ON_HOLD', NULL, 1, '2026-09-25 11:20:00');

INSERT INTO timeline_events (case_id, event_time, title, description, source, created_by) VALUES
(1, '2026-09-20 09:30:00', 'Case created', 'Case DF-2026-001 opened by System Administrator', 'SYSTEM', 1),
(1, '2026-09-19 22:41:10', 'Repeated LOGIN_FAILED for user hr_admin', 'Reported by SOC alert #4471', 'MANUAL', 2),
(1, '2026-09-19 22:47:55', 'Successful login from 10.0.4.23', 'Outside normal working hours', 'MANUAL', 2),
(1, '2026-09-19 22:52:30', 'USB device connected', 'SanDisk Cruzer, serial 4C530001', 'MANUAL', 2),
(2, '2026-09-23 14:05:00', 'Case created', 'Case DF-2026-002 opened by System Administrator', 'SYSTEM', 1),
(3, '2026-09-25 11:20:00', 'Case created', 'Case DF-2026-003 opened by System Administrator', 'SYSTEM', 1);

INSERT INTO findings (case_id, title, description, severity, status, created_by) VALUES
(1, 'Brute-force pattern before successful login', '14 failed logins within 6 minutes from the same workstation.', 'HIGH', 'CONFIRMED', 2),
(1, 'Payroll files copied to removable media', 'File access audit shows payroll_2026.xlsx copied at 22:55.', 'CRITICAL', 'OPEN', 2),
(2, 'Macro-enabled attachment executed', 'invoice_0923.docm spawned powershell.exe.', 'HIGH', 'OPEN', 3);
