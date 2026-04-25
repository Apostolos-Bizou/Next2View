-- V19: Security Documents Module
-- Stores administrative security documentation (DPIA, Tech Sec Architecture, etc.)
-- Access gated via viewSecurity permission or CEO role

CREATE TABLE IF NOT EXISTS security_document (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename     VARCHAR(255) NOT NULL,
    blob_path    VARCHAR(500) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    size_bytes   BIGINT NOT NULL,
    description  TEXT,
    uploader_id  UUID NOT NULL REFERENCES users(id),
    uploaded_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_security_document_uploader ON security_document(uploader_id);
CREATE INDEX IF NOT EXISTS idx_security_document_deleted ON security_document(deleted_at) WHERE deleted_at IS NULL;
