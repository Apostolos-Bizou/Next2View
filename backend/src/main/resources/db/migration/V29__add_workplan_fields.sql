-- V29: Add work-plan fields (v29-workplan)
-- Purpose: work-plan support — per-task start/end time, environment, work days,
--          gate flag; per-project work_plan_enabled visibility switch
-- Author: Next2View team
-- Date: 2026-08-05

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS start_time TIME;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS end_time TIME;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS environment VARCHAR(40);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS work_days NUMERIC(6,2);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS is_gate BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS work_plan_enabled BOOLEAN NOT NULL DEFAULT FALSE;
