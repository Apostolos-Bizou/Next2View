-- V11: Add start_day and duration_days to tasks (day-based Gantt)
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS start_day INTEGER;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS duration_days INTEGER;
