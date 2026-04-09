-- V7: Add start_date and end_date to project_specs
ALTER TABLE project_specs ADD COLUMN start_date DATE;
ALTER TABLE project_specs ADD COLUMN end_date DATE;