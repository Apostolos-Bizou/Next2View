-- V20: Report generation audit log
-- Reports Hub Phase 2

CREATE TABLE IF NOT EXISTS report_generation_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id VARCHAR(50) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    generated_by UUID NOT NULL REFERENCES users(id),
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    file_size_bytes BIGINT,
    parameters JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT
);

CREATE INDEX IF NOT EXISTS idx_report_log_generated_by ON report_generation_log(generated_by);
CREATE INDEX IF NOT EXISTS idx_report_log_template ON report_generation_log(template_id);
CREATE INDEX IF NOT EXISTS idx_report_log_date ON report_generation_log(generated_at DESC);
