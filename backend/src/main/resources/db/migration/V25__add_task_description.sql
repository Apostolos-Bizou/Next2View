-- V25: Add description column to tasks table
-- Purpose: Permanent task description (RichText), separate from day-to-day comment
-- Author: Next2View team
-- Date: 2026-05-11

ALTER TABLE tasks
    ADD COLUMN description TEXT;

COMMENT ON COLUMN tasks.description IS 'Permanent task description (HTML, sanitized via Jsoup). Distinct from comment which captures day-to-day notes.';
