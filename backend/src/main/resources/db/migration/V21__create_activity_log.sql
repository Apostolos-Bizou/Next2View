-- V20: Activity Log for user-facing notifications
-- Tracks all user actions across the platform

CREATE TABLE IF NOT EXISTS activity_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id        UUID NOT NULL,
    actor_name      VARCHAR(255) NOT NULL,
    action_type     VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID,
    entity_name     VARCHAR(500),
    category        VARCHAR(50),
    company_id      UUID,
    description     TEXT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_activity_log_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activity_log_created_at  ON activity_log (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_log_actor_id    ON activity_log (actor_id);
CREATE INDEX IF NOT EXISTS idx_activity_log_category    ON activity_log (category);
CREATE INDEX IF NOT EXISTS idx_activity_log_entity_type ON activity_log (entity_type);
CREATE INDEX IF NOT EXISTS idx_activity_log_company_id  ON activity_log (company_id);
