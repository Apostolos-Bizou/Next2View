-- V28: Add description column to modules table
-- Purpose: Permanent module description (RichText), same concept as project/task description
-- Author: Next2View team
-- Date: 2026-07-06

ALTER TABLE modules
    ADD COLUMN IF NOT EXISTS description TEXT;

COMMENT ON COLUMN modules.description IS 'Permanent module description (HTML, sanitized via Jsoup/HtmlSanitizer). Same concept as projects.description and tasks.description.';
