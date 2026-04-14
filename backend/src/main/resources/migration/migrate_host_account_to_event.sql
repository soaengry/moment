-- Migration: Move Host, AccountGroup, Account from wedding domain to event domain
-- Run manually against the database before deploying this version.

-- 1. AccountGroup: add event_id column (Hibernate auto-adds), then drop wedding_id
ALTER TABLE account_groups DROP COLUMN IF EXISTS wedding_id;

-- 2. Host: drop wedding-specific columns (Hibernate does NOT auto-drop)
ALTER TABLE hosts DROP COLUMN IF EXISTS father_name;
ALTER TABLE hosts DROP COLUMN IF EXISTS mother_name;
ALTER TABLE hosts DROP COLUMN IF EXISTS is_father_alive;
ALTER TABLE hosts DROP COLUMN IF EXISTS is_mother_alive;

-- 3. Create wedding_hosts table (Hibernate will auto-create via ddl-auto: update,
--    but include here for explicit tracking)
CREATE TABLE IF NOT EXISTS wedding_hosts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id     BIGINT       NOT NULL,
    father_name VARCHAR(50)  NULL,
    mother_name VARCHAR(50)  NULL,
    is_father_alive BOOLEAN  NOT NULL DEFAULT TRUE,
    is_mother_alive BOOLEAN  NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_wedding_hosts_host FOREIGN KEY (host_id) REFERENCES hosts(id) ON DELETE CASCADE
);
