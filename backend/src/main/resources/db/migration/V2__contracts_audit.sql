-- Next2View V2: Contract Files + Audit Log + Refresh Tokens

CREATE TABLE contract_files (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    blob_path       VARCHAR(500) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT,
    uploaded_by     UUID REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE contract_summaries (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_file_id UUID NOT NULL REFERENCES contract_files(id) ON DELETE CASCADE,
    project_id       UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    parties          TEXT[],
    subject          TEXT,
    contract_value   NUMERIC(14,2),
    currency         VARCHAR(10),
    start_date       DATE,
    end_date         DATE,
    payment_terms    TEXT,
    key_obligations  TEXT[],
    penalties        TEXT,
    governing_law    TEXT,
    is_signed        BOOLEAN DEFAULT FALSE,
    special_terms    TEXT,
    extracted_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    extracted_by_ai  BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    user_email  VARCHAR(255),
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   UUID,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  INET,
    user_agent  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contract_files_project  ON contract_files(project_id);
CREATE INDEX idx_contract_summaries_proj ON contract_summaries(project_id);
CREATE INDEX idx_audit_log_user          ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity        ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created       ON audit_log(created_at DESC);
CREATE INDEX idx_refresh_tokens_user     ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash     ON refresh_tokens(token_hash);
