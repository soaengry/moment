-- ============================================================
-- reset_schema.sql
-- 전체 테이블 DROP 후 재생성
-- DB: moment (MySQL 8.x)
-- 생성일: 2026-04-15
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. DROP (자식 → 부모 순서)
-- ============================================================
DROP TABLE IF EXISTS attendances;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS post_images;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS guestbook_entries;
DROP TABLE IF EXISTS rsvps;
DROP TABLE IF EXISTS invitations;
DROP TABLE IF EXISTS wedding_hosts;
DROP TABLE IF EXISTS galleries;
DROP TABLE IF EXISTS weddings;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS account_groups;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS hero_images;
DROP TABLE IF EXISTS hosts;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS transportation;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS bank_prefixes;
DROP TABLE IF EXISTS banks;
DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 2. CREATE (부모 → 자식 순서)
-- ============================================================

-- ----------------------------------------------------------
-- users
-- ----------------------------------------------------------
CREATE TABLE users (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    email             VARCHAR(255) NOT NULL,
    password          VARCHAR(255),
    nickname          VARCHAR(50)  NOT NULL,
    profile_image_url VARCHAR(500),
    role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
    auth_provider     VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id       VARCHAR(255),
    is_email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    token_version     INT          NOT NULL DEFAULT 0,
    version           BIGINT       NOT NULL DEFAULT 0,
    deleted_at        DATETIME(6),
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email     (email),
    UNIQUE KEY uk_users_nickname  (nickname),
    INDEX idx_email               (email),
    INDEX idx_provider            (auth_provider, provider_id)
);

-- ----------------------------------------------------------
-- password_reset_tokens
-- ----------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    is_used    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_tokens_token (token),
    INDEX idx_token_expires (token, expires_at, is_used)
);

-- ----------------------------------------------------------
-- email_verifications
-- ----------------------------------------------------------
CREATE TABLE email_verifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    email             VARCHAR(255) NOT NULL,
    verification_code VARCHAR(255) NOT NULL,
    attempt_count     INT          NOT NULL DEFAULT 0,
    is_locked         BOOLEAN      NOT NULL DEFAULT FALSE,
    locked_until      DATETIME(6),
    expires_at        DATETIME(6)  NOT NULL,
    is_verified       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_email_code (email, verification_code)
);

-- ----------------------------------------------------------
-- banks
-- ----------------------------------------------------------
CREATE TABLE banks (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    bank_code VARCHAR(10)  NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_banks_bank_code (bank_code)
);

-- ----------------------------------------------------------
-- bank_prefixes
-- ----------------------------------------------------------
CREATE TABLE bank_prefixes (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    bank_id BIGINT      NOT NULL,
    prefix  VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bank_prefixes_bank_prefix (bank_id, prefix),
    CONSTRAINT fk_bank_prefixes_bank_id
        FOREIGN KEY (bank_id) REFERENCES banks (id)
);

-- ----------------------------------------------------------
-- events
-- ----------------------------------------------------------
CREATE TABLE events (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    title            VARCHAR(50)  NOT NULL,
    type             VARCHAR(50)  NOT NULL,
    date             DATETIME(6)  NOT NULL,
    location_name    VARCHAR(50),
    location_address VARCHAR(255),
    location_detail  VARCHAR(255),
    location_lat     DOUBLE,
    location_lng     DOUBLE,
    slug             VARCHAR(50)  NOT NULL,
    is_public        BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at       DATETIME(6),
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_events_slug (slug),
    INDEX idx_event_user_id (user_id),
    CONSTRAINT fk_events_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- hero_images
-- ----------------------------------------------------------
CREATE TABLE hero_images (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    event_id      BIGINT       NOT NULL,
    image_url     VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(500),
    order_index   INT          NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_hero_images_events_event_id
        FOREIGN KEY (event_id) REFERENCES events (id)
);

-- ----------------------------------------------------------
-- hosts
-- ----------------------------------------------------------
CREATE TABLE hosts (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    event_id          BIGINT       NOT NULL,
    email             VARCHAR(255) NOT NULL,
    role              VARCHAR(20)  NOT NULL,
    name              VARCHAR(255) NOT NULL,
    contact           VARCHAR(255),
    profile_image_url VARCHAR(255),
    introduction      TEXT,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- schedules
-- ----------------------------------------------------------
CREATE TABLE schedules (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    event_id    BIGINT       NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT          NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- announcements
-- ----------------------------------------------------------
CREATE TABLE announcements (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    event_id   BIGINT      NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    is_pinned  BOOLEAN      NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- transportation
-- ----------------------------------------------------------
CREATE TABLE transportation (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    event_id    BIGINT       NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT          NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- account_groups
-- ----------------------------------------------------------
CREATE TABLE account_groups (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    event_id    BIGINT       NOT NULL,
    group_name  VARCHAR(255) NOT NULL,
    order_index INT          NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- accounts
-- ----------------------------------------------------------
CREATE TABLE accounts (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    account_group_id BIGINT       NOT NULL,
    bank_name        VARCHAR(255) NOT NULL,
    bank_code        VARCHAR(255) NOT NULL,
    account_number   VARCHAR(255) NOT NULL,
    account_holder   VARCHAR(255) NOT NULL,
    kakao_pay_url    VARCHAR(255),
    order_index      INT          NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- weddings
-- ----------------------------------------------------------
CREATE TABLE weddings (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    event_id     BIGINT NOT NULL,
    notice       TEXT,
    parking_info TEXT,
    meal_info    TEXT,
    greeting     TEXT,
    version      INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_weddings_event_id (event_id),
    CONSTRAINT fk_weddings_events_event_id
        FOREIGN KEY (event_id) REFERENCES events (id)
);

-- ----------------------------------------------------------
-- wedding_hosts
-- ----------------------------------------------------------
CREATE TABLE wedding_hosts (
    id              BIGINT  NOT NULL AUTO_INCREMENT,
    host_id         BIGINT  NOT NULL,
    father_name     VARCHAR(255),
    mother_name     VARCHAR(255),
    is_father_alive BOOLEAN NOT NULL DEFAULT TRUE,
    is_mother_alive BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- galleries
-- ----------------------------------------------------------
CREATE TABLE galleries (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    wedding_id    BIGINT       NOT NULL,
    image_url     VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------------------------------------
-- invitations
-- ----------------------------------------------------------
CREATE TABLE invitations (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    event_id BIGINT      NOT NULL,
    user_id  BIGINT      NOT NULL,
    status   VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invitations_event_user (event_id, user_id),
    CONSTRAINT fk_invitations_events_event_id
        FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_invitations_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- rsvps
-- ----------------------------------------------------------
CREATE TABLE rsvps (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    wedding_id     BIGINT      NOT NULL,
    user_id        BIGINT,
    session_id     VARCHAR(100) NOT NULL,
    attendance     VARCHAR(10)  NOT NULL,
    name           VARCHAR(50)  NOT NULL,
    side           VARCHAR(20)  NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    attendee_count INT          NOT NULL DEFAULT 1,
    will_eat       BOOLEAN      NOT NULL DEFAULT FALSE,
    meal_count     INT          NOT NULL DEFAULT 0,
    will_ride      BOOLEAN      NOT NULL DEFAULT FALSE,
    ride_count     INT          NOT NULL DEFAULT 0,
    note           VARCHAR(50),
    consent        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_rsvps_session_wedding (session_id, wedding_id),
    CONSTRAINT fk_rsvps_weddings_wedding_id
        FOREIGN KEY (wedding_id) REFERENCES weddings (id),
    CONSTRAINT fk_rsvps_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- guestbook_entries
-- ----------------------------------------------------------
CREATE TABLE guestbook_entries (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    wedding_id  BIGINT      NOT NULL,
    user_id     BIGINT,
    author_name VARCHAR(50) NOT NULL,
    content     TEXT        NOT NULL,
    password    VARCHAR(255),
    is_secret   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_guestbook_entries_weddings_wedding_id
        FOREIGN KEY (wedding_id) REFERENCES weddings (id),
    CONSTRAINT fk_guestbook_entries_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- posts
-- ----------------------------------------------------------
CREATE TABLE posts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    event_id      BIGINT,
    content       VARCHAR(200) NOT NULL,
    like_count    INT          NOT NULL DEFAULT 0,
    comment_count INT          NOT NULL DEFAULT 0,
    deleted_at    DATETIME(6),
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_post_event_created (event_id, created_at),
    INDEX idx_post_user_created  (user_id, created_at),
    CONSTRAINT fk_posts_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- post_images
-- ----------------------------------------------------------
CREATE TABLE post_images (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    post_id     BIGINT       NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    order_index INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_images_posts_post_id
        FOREIGN KEY (post_id) REFERENCES posts (id)
);

-- ----------------------------------------------------------
-- comments
-- ----------------------------------------------------------
CREATE TABLE comments (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    content    VARCHAR(300) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_posts_post_id
        FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- post_likes
-- ----------------------------------------------------------
CREATE TABLE post_likes (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_likes_post_user (post_id, user_id),
    CONSTRAINT fk_post_likes_posts_post_id
        FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_likes_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- bookmarks
-- ----------------------------------------------------------
CREATE TABLE bookmarks (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookmarks_post_user (post_id, user_id),
    CONSTRAINT fk_bookmarks_posts_post_id
        FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_bookmarks_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ----------------------------------------------------------
-- attendances
-- ----------------------------------------------------------
CREATE TABLE attendances (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    event_id   BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_attendances_user_event (user_id, event_id)
);
