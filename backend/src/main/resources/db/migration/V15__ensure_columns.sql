-- V15: Ensure all required columns exist
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS manual_progress BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS start_date DATE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS end_date DATE;
