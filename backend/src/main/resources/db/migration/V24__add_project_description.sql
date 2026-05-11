-- V24: Add description column to projects table
-- Purpose: Permanent project description (RichText), separate from day-to-day CEO notes
-- Author: Next2View team
-- Date: 2026-05-11

ALTER TABLE projects
    ADD COLUMN description TEXT;

COMMENT ON COLUMN projects.description IS 'Permanent project description (HTML, sanitized via Jsoup). Distinct from ceo_notes which captures day-to-day updates.';
