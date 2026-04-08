-- Next2View Database Schema V1
-- Companies, Users, Projects, Modules, Tasks

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── COMPANIES ──
CREATE TABLE companies (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(5)   NOT NULL UNIQUE,
    color       VARCHAR(30)  NOT NULL DEFAULT '#3b82f6',
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── USERS ──
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(30)  NOT NULL CHECK (role IN ('CEO','DEPT_HEAD','VIEWER')),
    department      VARCHAR(30)  CHECK (department IN ('finance','legal','dev','marketing','management')),
    company_id      UUID REFERENCES companies(id) ON DELETE SET NULL,
    mfa_secret      VARCHAR(64),
    mfa_enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_attempts INT          NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── PROJECTS ──
CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           VARCHAR(255) NOT NULL,
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE RESTRICT,
    category        VARCHAR(20)  NOT NULL CHECK (category IN ('finance','legal','dev','marketing')),
    budget          NUMERIC(14,2),
    paid            NUMERIC(14,2) NOT NULL DEFAULT 0,
    invoiced        NUMERIC(14,2) NOT NULL DEFAULT 0,
    deadline        DATE,
    contract_desc   TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'on_track'
                    CHECK (status IN ('on_track','delayed','at_risk','stale','completed')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    last_updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_by      UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── PROJECT SPECIFICATIONS ──
CREATE TABLE project_specs (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    is_done     BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT     NOT NULL DEFAULT 0
);

-- ── MODULES ──
CREATE TABLE modules (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    color       VARCHAR(20)  NOT NULL DEFAULT 'dev',
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── TASKS ──
CREATE TABLE tasks (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id   UUID NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    assignee    VARCHAR(150),
    progress    INT  NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
    is_done     BOOLEAN NOT NULL DEFAULT FALSE,
    is_blocked  BOOLEAN NOT NULL DEFAULT FALSE,
    block_note  TEXT,
    comment     TEXT,
    deadline    DATE,
    start_week  INT,
    duration_weeks INT DEFAULT 1,
    sort_order  INT  NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── CEO NOTES ──
CREATE TABLE ceo_notes (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── INDEXES ──
CREATE INDEX idx_projects_company   ON projects(company_id);
CREATE INDEX idx_projects_category  ON projects(category);
CREATE INDEX idx_projects_status    ON projects(status);
CREATE INDEX idx_projects_deadline  ON projects(deadline);
CREATE INDEX idx_modules_project    ON modules(project_id);
CREATE INDEX idx_tasks_module       ON tasks(module_id);
CREATE INDEX idx_tasks_blocked      ON tasks(is_blocked) WHERE is_blocked = TRUE;
CREATE INDEX idx_users_email        ON users(email);
CREATE INDEX idx_users_company      ON users(company_id);
