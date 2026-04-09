-- V9: Add start_date to projects
ALTER TABLE projects ADD COLUMN IF NOT EXISTS start_date DATE;