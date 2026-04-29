-- V22: Activity dismissals — per-user dismiss/archive of activity entries
CREATE TABLE IF NOT EXISTS activity_dismissals (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    activity_id UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_dismissal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_dismissal_activity FOREIGN KEY (activity_id) REFERENCES activity_log(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_activity UNIQUE (user_id, activity_id)
);

CREATE INDEX IF NOT EXISTS idx_dismissals_user_id ON activity_dismissals (user_id);