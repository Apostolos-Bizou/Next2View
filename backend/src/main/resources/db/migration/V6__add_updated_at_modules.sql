-- Next2View V6: Add updated_at to modules table
-- Module entity extends BaseEntity which requires updated_at

ALTER TABLE modules ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Also verify tasks has updated_at (already in schema but ensure)
-- Update existing rows
UPDATE modules SET updated_at = created_at WHERE updated_at IS NULL;