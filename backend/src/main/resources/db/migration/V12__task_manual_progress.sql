-- V12: Add manual_progress flag to tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS manual_progress BOOLEAN DEFAULT FALSE;