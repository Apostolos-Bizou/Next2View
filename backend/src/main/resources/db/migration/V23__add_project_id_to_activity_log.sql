-- V23: Add project_id to activity_log for proper notification navigation
-- Allows FILE/TASK/COMMENT activities to link back to their parent project
-- Backward compatible: NULL allowed, no FK constraint (soft reference)

ALTER TABLE activity_log ADD COLUMN IF NOT EXISTS project_id UUID NULL;

CREATE INDEX IF NOT EXISTS idx_activity_log_project_id ON activity_log(project_id);
