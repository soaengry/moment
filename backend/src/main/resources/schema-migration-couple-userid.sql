-- Migration: Add user_id to couples table for host identification
ALTER TABLE couples ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;
