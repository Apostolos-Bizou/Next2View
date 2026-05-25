-- V27: Task-level file attachments for contract_files
-- Adds a nullable task_id so the SAME encrypted-file system (Legal Vault v1.0)
-- can attach files to a specific task, not only to a project.
--
-- Design:
--   * task_id is NULLABLE. NULL = project-level file (existing behaviour, unchanged).
--     NOT NULL = task-level file (new).
--   * project_id stays NOT NULL: task files also record their parent project
--     (derived from task -> module -> project) so the existing MFA gate,
--     category scoping, and project-level queries keep working unchanged.
--   * ON DELETE CASCADE: deleting a task removes its file rows (blobs remain
--     under Azure 90-day soft-delete retention, same as project files).
--
-- Mirrors the idempotent style of V18 (ADD COLUMN IF NOT EXISTS).
-- Safe to re-run. Does NOT touch existing rows (all keep task_id = NULL).

-- =========================================================
-- 1. Add nullable task_id column
-- =========================================================
ALTER TABLE contract_files
  ADD COLUMN IF NOT EXISTS task_id UUID REFERENCES tasks(id) ON DELETE CASCADE;

-- =========================================================
-- 2. Index for fast active-file lookup per task
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_contract_files_task
  ON contract_files(task_id)
  WHERE task_id IS NOT NULL AND deleted_at IS NULL;

-- =========================================================
-- 3. Documentation comment (DPIA reference)
-- =========================================================
COMMENT ON COLUMN contract_files.task_id IS 'Optional task association (V27). NULL = project-level file; NOT NULL = task-level attachment. Parent project_id is still recorded for MFA/category scoping.';
