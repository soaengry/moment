-- 테스트 DB 스키마 보정: ddl-auto=update는 기존 NOT NULL 컬럼을 nullable로 바꾸지 않으므로
-- 마이그레이션 스크립트(migrate_host_account_to_event.sql 등)가 테스트 DB에 미적용된 경우를 보정한다.
-- 모든 구문은 컬럼이 이미 nullable이거나 존재하지 않는 경우 무시된다 (CONTINUE_ON_ERROR).

-- Event 테이블 location 컬럼 nullable 처리
ALTER TABLE events MODIFY COLUMN location_name VARCHAR(50) NULL;
ALTER TABLE events MODIFY COLUMN location_address TEXT NULL;
ALTER TABLE events MODIFY COLUMN location_detail TEXT NULL;
ALTER TABLE events MODIFY COLUMN location_lat DOUBLE NULL;
ALTER TABLE events MODIFY COLUMN location_lng DOUBLE NULL;

-- schedules 테이블: time 제거, wedding_id 제거
ALTER TABLE schedules MODIFY COLUMN time TIME NULL DEFAULT NULL;
ALTER TABLE schedules MODIFY COLUMN wedding_id BIGINT NULL DEFAULT NULL;

-- account_groups 테이블: wedding_id 제거
ALTER TABLE account_groups MODIFY COLUMN wedding_id BIGINT NULL DEFAULT NULL;

-- hosts 테이블: email nullable, 구 웨딩 컬럼 nullable
ALTER TABLE hosts MODIFY COLUMN email VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE hosts MODIFY COLUMN father_name VARCHAR(50) NULL DEFAULT NULL;
ALTER TABLE hosts MODIFY COLUMN mother_name VARCHAR(50) NULL DEFAULT NULL;
ALTER TABLE hosts MODIFY COLUMN is_father_alive TINYINT(1) NULL DEFAULT NULL;
ALTER TABLE hosts MODIFY COLUMN is_mother_alive TINYINT(1) NULL DEFAULT NULL;
