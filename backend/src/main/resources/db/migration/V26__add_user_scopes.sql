-- V26: User-level company and project scoping (opt-in restriction)
-- Empty scope = no restriction (user sees all within their category permissions)
-- Non-empty scope = user limited to listed companies/projects

CREATE TABLE user_company_scope (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_company_scope UNIQUE (user_id, company_id)
);

CREATE INDEX idx_ucs_user ON user_company_scope(user_id);
CREATE INDEX idx_ucs_company ON user_company_scope(company_id);

CREATE TABLE user_project_scope (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_project_scope UNIQUE (user_id, project_id)
);

CREATE INDEX idx_ups_user ON user_project_scope(user_id);
CREATE INDEX idx_ups_project ON user_project_scope(project_id);

COMMENT ON TABLE user_company_scope IS 'Opt-in company restriction per user. Empty = no restriction.';
COMMENT ON TABLE user_project_scope IS 'Opt-in project restriction per user. Empty = no restriction.';
