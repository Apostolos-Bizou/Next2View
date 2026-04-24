package com.next2me.next2view.service;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.next2me.next2view.model.AuditLog;
import com.next2me.next2view.model.ContractFile;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.AuditLogRepository;
import com.next2me.next2view.repository.ContractFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrates encrypted contract file operations.
 *
 * Flow on upload:
 *   1. Validate file size/type (done by controller)
 *   2. Read bytes into memory (safe for contracts up to ~20MB)
 *   3. Encrypt via LegalVaultCrypto -> ciphertext + metadata
 *   4. Upload ciphertext to Azure Blob (legal-contracts container)
 *   5. Persist metadata to contract_files table
 *   6. Write audit log entry
 *
 * Flow on download:
 *   1. Load DB record (enforces soft-delete filter)
 *   2. Unwrap DEK via Key Vault
 *   3. Download ciphertext from Blob
 *   4. Decrypt + verify SHA-256 (tamper detection)
 *   5. Write audit log entry
 *   6. Return plaintext bytes to controller
 *
 * Permission checks are the controller's responsibility.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractFileService {

    private final ContractFileRepository contractFileRepository;
    private final AuditLogRepository auditLogRepository;
    private final LegalVaultCrypto crypto;

    /** Nullable — configured only if legal vault is set up. */
    @Autowired(required = false)
    private BlobContainerClient legalBlobContainerClient;

    /** Nullable — configured only if Key Vault is set up. */
    @Autowired(required = false)
    private CryptographyClient legalKekCryptoClient;

    @Value("${legal-vault.storage.account:}")
    private String storageAccountName;

    @Value("${legal-vault.storage.container:legal-contracts}")
    private String containerName;

    // =================================================================
    // UPLOAD
    // =================================================================

    /**
     * Encrypts the file and uploads to the Legal Vault.
     * @return the persisted ContractFile record (with metadata, not plaintext)
     */
    @Transactional
    public ContractFile uploadEncrypted(
            MultipartFile file,
            Project project,
            User uploader,
            String sanitizedFilename
    ) {
        requireVaultConfigured();

        byte[] plaintext;
        try {
            plaintext = file.getBytes();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read file");
        }

        // Duplicate detection via SHA-256
        String sha256 = crypto.sha256Hex(plaintext);
        contractFileRepository.findByProjectIdAndSha256(project.getId(), sha256).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An identical file already exists for this project (" + existing.getFileName() + ")");
        });

        // Encrypt (wraps DEK via Key Vault)
        LegalVaultCrypto.EncryptionResult enc = crypto.encrypt(plaintext, legalKekCryptoClient);

        // Build blob path: {projectId}/{uuid}/{sanitizedFilename}.enc
        UUID fileUuid = UUID.randomUUID();
        String blobName = project.getId() + "/" + fileUuid + "/" + sanitizedFilename + ".enc";

        // Upload ciphertext to Blob
        try {
            BlobClient blob = legalBlobContainerClient.getBlobClient(blobName);
            blob.upload(new ByteArrayInputStream(enc.ciphertext()), enc.ciphertext().length, true);
        } catch (Exception e) {
            log.error("Blob upload failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload to storage failed");
        }

        // Persist metadata
        ContractFile cf = ContractFile.builder()
                .project(project)
                .fileName(sanitizedFilename)
                .blobPath(blobName)
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .uploadedBy(uploader)
                .encryptedDek(enc.encryptedDek())
                .iv(enc.iv())
                .authTag(enc.authTag())
                .sha256(enc.sha256Hex())
                .encryptionAlgo("AES-256-GCM")
                .kekKeyId(enc.kekKeyId())
                .storageAccount(storageAccountName)
                .containerName(containerName)
                .build();
        cf = contractFileRepository.save(cf);

        // Audit
        auditLogRepository.save(AuditLog.builder()
                .userEmail(uploader.getEmail())
                .action("CONTRACT_UPLOAD_ENCRYPTED")
                .entityType("contract_files")
                .entityId(cf.getId())
                .newValue(Map.of(
                        "fileName", sanitizedFilename,
                        "projectId", project.getId().toString(),
                        "sizeBytes", file.getSize(),
                        "sha256", enc.sha256Hex(),
                        "algo", "AES-256-GCM"))
                .build());

        log.info("Encrypted contract uploaded: projectId={}, fileId={}, size={}B",
                project.getId(), cf.getId(), file.getSize());

        return cf;
    }

    // =================================================================
    // DOWNLOAD
    // =================================================================

    /** Holder for decrypted download payload. */
    public record DecryptedFile(byte[] plaintext, String fileName, String contentType) {}

    /**
     * Downloads, decrypts and verifies integrity of an encrypted contract file.
     * Writes an audit log entry before returning.
     */
    @Transactional
    public DecryptedFile downloadDecrypted(UUID fileId, User requester) {
        requireVaultConfigured();

        ContractFile cf = contractFileRepository.findActiveById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        if (!cf.isEncrypted()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "File is not encrypted (legacy record); use legacy download path");
        }

        // Download ciphertext from Blob
        byte[] ciphertext;
        try {
            BlobClient blob = legalBlobContainerClient.getBlobClient(cf.getBlobPath());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            blob.downloadStream(out);
            ciphertext = out.toByteArray();
        } catch (Exception e) {
            log.error("Blob download failed for {}: {}", cf.getBlobPath(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Download from storage failed");
        }

        // Decrypt + verify SHA-256 (throws SecurityException on tamper)
        byte[] plaintext;
        try {
            plaintext = crypto.decrypt(
                    ciphertext,
                    cf.getIv(),
                    cf.getAuthTag(),
                    cf.getEncryptedDek(),
                    cf.getSha256(),
                    legalKekCryptoClient);
        } catch (SecurityException e) {
            log.error("INTEGRITY FAILURE for fileId={}: {}", fileId, e.getMessage());
            auditLogRepository.save(AuditLog.builder()
                    .userEmail(requester.getEmail())
                    .action("CONTRACT_INTEGRITY_FAILURE")
                    .entityType("contract_files")
                    .entityId(fileId)
                    .newValue(Map.of("reason", e.getMessage()))
                    .build());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "File integrity check failed - possible tampering");
        }

        // Audit successful decrypt
        auditLogRepository.save(AuditLog.builder()
                .userEmail(requester.getEmail())
                .action("CONTRACT_DOWNLOAD_DECRYPTED")
                .entityType("contract_files")
                .entityId(fileId)
                .newValue(Map.of("fileName", cf.getFileName()))
                .build());

        log.info("Encrypted contract decrypted: fileId={}, requester={}", fileId, requester.getEmail());

        return new DecryptedFile(plaintext, cf.getFileName(), cf.getContentType());
    }

    // =================================================================
    // SOFT DELETE
    // =================================================================

    @Transactional
    public void softDelete(UUID fileId, User deleter, String reason) {
        ContractFile cf = contractFileRepository.findActiveById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        cf.setDeletedAt(Instant.now());
        cf.setDeletedBy(deleter);
        cf.setDeleteReason(reason);
        cf.setIsActive(false);
        contractFileRepository.save(cf);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(deleter.getEmail())
                .action("CONTRACT_SOFT_DELETE")
                .entityType("contract_files")
                .entityId(fileId)
                .oldValue(Map.of("fileName", cf.getFileName()))
                .newValue(Map.of("reason", Objects.toString(reason, "(none)")))
                .build());

        log.info("Contract soft-deleted: fileId={}, deleter={}", fileId, deleter.getEmail());
        // Note: blob remains for 90 days via Azure soft-delete retention.
    }

    // =================================================================
    // LIST
    // =================================================================

    public List<ContractFile> listActive(UUID projectId) {
        return contractFileRepository.findActiveByProjectId(projectId);
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private void requireVaultConfigured() {
        if (legalBlobContainerClient == null || legalKekCryptoClient == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Legal Vault is not configured on this server");
        }
    }
}
