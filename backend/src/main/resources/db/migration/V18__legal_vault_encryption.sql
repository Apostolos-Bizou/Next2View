-- V18: Legal Vault encryption support for contract_files
-- Adds client-side AES-256-GCM encryption metadata + soft delete
-- Azure CMK handles at-rest encryption (transparent);
-- this layer adds application-level encryption (defense in depth).

-- =========================================================
-- 1. Encryption metadata columns (nullable for backward compat)
-- =========================================================
ALTER TABLE contract_files
  ADD COLUMN IF NOT EXISTS encrypted_dek   BYTEA,
  ADD COLUMN IF NOT EXISTS iv              BYTEA,
  ADD COLUMN IF NOT EXISTS auth_tag        BYTEA,
  ADD COLUMN IF NOT EXISTS sha256          VARCHAR(64),
  ADD COLUMN IF NOT EXISTS encryption_algo VARCHAR(30) DEFAULT 'AES-256-GCM',
  ADD COLUMN IF NOT EXISTS kek_key_id      VARCHAR(255);

-- =========================================================
-- 2. Soft delete columns (legal requirement)
-- =========================================================
ALTER TABLE contract_files
  ADD COLUMN IF NOT EXISTS deleted_at      TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS deleted_by      UUID REFERENCES users(id) ON DELETE SET NULL,
  ADD COLUMN IF NOT EXISTS delete_reason   VARCHAR(255);

-- =========================================================
-- 3. Storage tracking (which Azure resources were used)
-- =========================================================
ALTER TABLE contract_files
  ADD COLUMN IF NOT EXISTS storage_account VARCHAR(100),
  ADD COLUMN IF NOT EXISTS container_name  VARCHAR(100);

-- =========================================================
-- 4. Index on deleted_at for efficient active-only queries
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_contract_files_deleted
  ON contract_files(deleted_at)
  WHERE deleted_at IS NULL;

-- =========================================================
-- 5. Index on sha256 for duplicate detection
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_contract_files_sha256
  ON contract_files(sha256)
  WHERE sha256 IS NOT NULL;

-- =========================================================
-- 6. Add check constraint: encrypted files must have all crypto metadata
-- =========================================================
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chk_contract_files_encryption_complete'
  ) THEN
    ALTER TABLE contract_files
      ADD CONSTRAINT chk_contract_files_encryption_complete
      CHECK (
        -- Either ALL crypto fields are set, OR NONE are (legacy records)
        (encrypted_dek IS NOT NULL AND iv IS NOT NULL AND auth_tag IS NOT NULL AND sha256 IS NOT NULL)
        OR
        (encrypted_dek IS NULL AND iv IS NULL AND auth_tag IS NULL AND sha256 IS NULL)
      );
  END IF;
END $$;

-- =========================================================
-- 7. Column comments for documentation (DPIA reference)
-- =========================================================
COMMENT ON COLUMN contract_files.encrypted_dek   IS 'Data Encryption Key, wrapped by Azure Key Vault KEK (RSA-OAEP-256)';
COMMENT ON COLUMN contract_files.iv              IS 'AES-GCM Initialization Vector (12 bytes, random per file)';
COMMENT ON COLUMN contract_files.auth_tag        IS 'AES-GCM Authentication Tag (16 bytes, tamper detection)';
COMMENT ON COLUMN contract_files.sha256          IS 'SHA-256 hash of plaintext for integrity verification';
COMMENT ON COLUMN contract_files.encryption_algo IS 'Encryption algorithm identifier for future migration flexibility';
COMMENT ON COLUMN contract_files.kek_key_id      IS 'Azure Key Vault Key ID with version used for DEK wrapping';
COMMENT ON COLUMN contract_files.deleted_at      IS 'Soft delete timestamp; actual blob deletion happens after 90-day retention';
