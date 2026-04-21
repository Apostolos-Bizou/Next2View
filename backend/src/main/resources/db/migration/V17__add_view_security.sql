-- V17: Add view_security flag to user_permissions
-- This flag controls visibility of the Security Documentation tab in the Guide.
-- Default FALSE for all users; CEO will see it by code bypass (no DB flag needed).

ALTER TABLE user_permissions
  ADD COLUMN IF NOT EXISTS view_security BOOLEAN NOT NULL DEFAULT FALSE;
