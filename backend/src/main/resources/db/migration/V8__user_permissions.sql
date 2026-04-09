-- V8: User Permissions
CREATE TABLE user_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- Visibility
    view_finance     BOOLEAN NOT NULL DEFAULT false,
    view_legal       BOOLEAN NOT NULL DEFAULT false,
    view_dev         BOOLEAN NOT NULL DEFAULT false,
    view_marketing   BOOLEAN NOT NULL DEFAULT false,
    view_financials  BOOLEAN NOT NULL DEFAULT false,
    view_ceo_notes   BOOLEAN NOT NULL DEFAULT false,
    -- Actions
    update_tasks     BOOLEAN NOT NULL DEFAULT false,
    upload_files     BOOLEAN NOT NULL DEFAULT false,
    create_project   BOOLEAN NOT NULL DEFAULT false,
    edit_project     BOOLEAN NOT NULL DEFAULT false,
    -- Management
    manage_users     BOOLEAN NOT NULL DEFAULT false,
    manage_companies BOOLEAN NOT NULL DEFAULT false,
    -- AI
    ai_ceo_report    BOOLEAN NOT NULL DEFAULT false,
    ai_contract      BOOLEAN NOT NULL DEFAULT false,
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id)
);

-- CEO gets all permissions automatically (handled in code)
-- Other roles start with all false, CEO customizes