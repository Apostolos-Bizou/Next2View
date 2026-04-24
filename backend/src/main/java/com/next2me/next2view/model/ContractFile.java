package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract file entity with client-side encryption metadata (Legal Vault v1.0).
 *
 * Encryption model (defense in depth):
 *   Layer 1: Azure Storage encryption-at-rest via Customer-Managed Key (CMK)
 *   Layer 2: Application-level AES-256-GCM BEFORE upload
 *            - Random 256-bit DEK per file, wrapped by KEK (Key Vault RSA-3072)
 *            - Random 96-bit IV per file
 *            - 128-bit GCM auth tag detects tampering
 *   Integrity: SHA-256 of plaintext verifies decrypted content
 *
 * Legacy records (pre-V18) have null encryption fields;
 * chk_contract_files_encryption_complete constraint enforces all-or-nothing.
 */
@Entity
@Table(name = "contract_files")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "blob_path", nullable = false, length = 500)
    private String blobPath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private Instant uploadedAt = Instant.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ==========================================================
    // V18 - Client-side encryption metadata
    // ==========================================================

    /** DEK wrapped by Azure Key Vault KEK (RSA-OAEP-256). */
    @Column(name = "encrypted_dek")
    private byte[] encryptedDek;

    /** AES-GCM Initialization Vector (12 bytes, random per file). */
    @Column(name = "iv")
    private byte[] iv;

    /** AES-GCM Authentication Tag (16 bytes, tamper detection). */
    @Column(name = "auth_tag")
    private byte[] authTag;

    /** SHA-256 hash of plaintext for integrity verification (hex). */
    @Column(name = "sha256", length = 64)
    private String sha256;

    /** Encryption algorithm identifier (e.g., AES-256-GCM). */
    @Column(name = "encryption_algo", length = 30)
    @Builder.Default
    private String encryptionAlgo = "AES-256-GCM";

    /** Azure Key Vault Key ID with version used for DEK wrapping. */
    @Column(name = "kek_key_id", length = 255)
    private String kekKeyId;

    // ==========================================================
    // V18 - Soft delete (legal requirement: 90-day retention)
    // ==========================================================

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @Column(name = "delete_reason", length = 255)
    private String deleteReason;

    // ==========================================================
    // V18 - Storage tracking (audit/forensics)
    // ==========================================================

    @Column(name = "storage_account", length = 100)
    private String storageAccount;

    @Column(name = "container_name", length = 100)
    private String containerName;

    /** True only if all crypto metadata is present. */
    @Transient
    public boolean isEncrypted() {
        return encryptedDek != null && iv != null && authTag != null && sha256 != null;
    }
}
