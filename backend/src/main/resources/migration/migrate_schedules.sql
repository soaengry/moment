-- schedules 테이블 마이그레이션
-- 1. invitation_id 컬럼 제거 (이전 설계 잔재, NOT NULL 제약으로 insert 실패 원인)
ALTER TABLE schedules DROP COLUMN IF EXISTS invitation_id;

-- 2. wedding_id 컬럼을 event_id로 교체 (schedule이 event 직속으로 이동)
--    Hibernate ddl-auto:update가 event_id 컬럼을 자동 추가하므로
--    기존 wedding_id 컬럼만 제거
ALTER TABLE schedules DROP COLUMN IF EXISTS wedding_id;
