package com.next2me.next2view.controller;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.next2me.next2view.model.AuditLog;
import com.next2me.next2view.model.ContractFile;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.AuditLogRepository;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.security.FileValidator;
import com.next2me.next2view.security.PermissionEvaluator;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/projects/{projectId}/files")
@RequiredArgsConstructor
@Slf4j
public class ContractFileController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PermissionEvaluator permissions;
    private final FileValidator fileValidator;

    @PersistenceContext
    private EntityManager em;

    @Value("${azure.storage.account:}")
    private String storageAccount;

    @Value("${azure.storage.key:}")
    private String storageKey;

    private BlobContainerClient containerClient;
    private StorageSharedKeyCredential sharedKeyCredential;

    @PostConstruct
    public void init() {
        try {
            if (storageAccount != null && !storageAccount.isBlank() &&
                storageKey != null && !storageKey.isBlank()) {
                sharedKeyCredential = new StorageSharedKeyCredential(storageAccount, storageKey);
                BlobServiceClient client = new BlobServiceClientBuilder()
                    .credential(sharedKeyCredential)
                    .endpoint("https://" + storageAccount + ".blob.core.windows.net")
                    .buildClient();
                containerClient = client.getBlobContainerClient("contracts");
                log.info("Azure Blob Storage initialized for account: {}", storageAccount);
            } else {
                log.warn("Azure Storage not configured: account={}", storageAccount);
            }
        } catch (Exception e) {
            log.warn("Azure Storage init failed: {}", e.getMessage());
        }
    }

    // ==================================================================
    // LIST files for a project. Requires read access to the project.
    // ==================================================================
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listFiles(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        permissions.requireCanRead(actor, project);

        List<ContractFile> files = em.createQuery(
            "SELECT f FROM ContractFile f WHERE f.project.id = :pid AND f.isActive = true ORDER BY f.uploadedAt DESC",
            ContractFile.class).setParameter("pid", projectId).getResultList();

        List<Map<String, Object>> result = files.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("fileName", f.getFileName());
            m.put("contentType", f.getContentType());
            m.put("fileSizeBytes", f.getFileSizeBytes());
            m.put("uploadedAt", f.getUploadedAt());
            m.put("uploadedBy", f.getUploadedBy() != null ? f.getUploadedBy().getFullName() : "Unknown");
            m.put("downloadUrl", "/api/projects/" + projectId + "/files/" + f.getId() + "/download");
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ==================================================================
    // UPLOAD a file. Requires WRITE access to the project.
    // Enforces: file type whitelist, size limit, filename sanitization.
    // ==================================================================
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable UUID projectId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId
    ) throws Exception {
        UUID actorId = parseUserId(userId);
        User actor = permissions.requireUser(actorId);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        permissions.requireCanWrite(actor, project);

        // Validate file (throws 400 if invalid)
        FileValidator.Validated v = fileValidator.validate(file);

        // Build safe blob path: {projectId}/{uuid}/{sanitized-filename}
        UUID fileUuid = UUID.randomUUID();
        String blobName = projectId + "/" + fileUuid + "/" + v.sanitizedFilename;

        if (containerClient != null) {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } else {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "File storage is not configured");
        }

        ContractFile cf = ContractFile.builder()
            .project(project)
            .fileName(v.sanitizedFilename)
            .blobPath(blobName)
            .contentType(file.getContentType())
            .fileSizeBytes(file.getSize())
            .uploadedBy(actor)
            .build();
        em.persist(cf);

        // Audit
        auditLogRepository.save(AuditLog.builder()
            .userEmail(actor.getEmail())
            .action("UPLOAD")
            .entityType("contract_files")
            .entityId(cf.getId())
            .newValue(Map.of(
                "fileName", v.sanitizedFilename,
                "projectId", projectId.toString(),
                "sizeBytes", file.getSize()))
            .build());

        Map<String, Object> result = new HashMap<>();
        result.put("id", cf.getId());
        result.put("fileName", cf.getFileName());
        result.put("fileSizeBytes", cf.getFileSizeBytes());
        result.put("uploadedAt", cf.getUploadedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ==================================================================
    // DOWNLOAD (returns SAS URL). Requires READ access to the project.
    // ==================================================================
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Map<String, String>> download(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        ContractFile cf = em.find(ContractFile.class, fileId);
        if (cf == null || !Boolean.TRUE.equals(cf.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        // Defense: the file's project must match the URL path
        if (!cf.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        permissions.requireCanRead(actor, cf.getProject());

        if (containerClient == null || sharedKeyCredential == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "File storage is not configured");
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(cf.getBlobPath());
            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusHours(1), permission);
            String sasToken = blobClient.generateSas(values);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            // Audit
            auditLogRepository.save(AuditLog.builder()
                .userEmail(actor.getEmail())
                .action("DOWNLOAD")
                .entityType("contract_files")
                .entityId(fileId)
                .newValue(Map.of("fileName", cf.getFileName()))
                .build());

            return ResponseEntity.ok(Map.of("url", sasUrl, "fileName", cf.getFileName()));
        } catch (Exception e) {
            log.error("SAS generation error for file {}: {}", fileId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not generate download link");
        }
    }

    // ==================================================================
    // DELETE a file. Requires WRITE access to the project.
    // Soft-delete in DB + hard delete in blob storage.
    // ==================================================================
    @DeleteMapping("/{fileId}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        ContractFile cf = em.find(ContractFile.class, fileId);
        if (cf == null) {
            return ResponseEntity.noContent().build();
        }
        if (!cf.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        permissions.requireCanWrite(actor, cf.getProject());

        cf.setIsActive(false);

        if (containerClient != null) {
            try {
                containerClient.getBlobClient(cf.getBlobPath()).delete();
            } catch (Exception e) {
                log.warn("Blob delete failed for {}: {}", cf.getBlobPath(), e.getMessage());
            }
        }

        // Audit
        auditLogRepository.save(AuditLog.builder()
            .userEmail(actor.getEmail())
            .action("DELETE")
            .entityType("contract_files")
            .entityId(fileId)
            .oldValue(Map.of("fileName", cf.getFileName()))
            .build());

        return ResponseEntity.noContent().build();
    }

    // ==================================================================
    // Helper: parse userId principal into UUID (401 on missing/invalid).
    // ==================================================================
    private UUID parseUserId(String userId) {
        if (userId == null || userId.isBlank() || "anonymousUser".equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
        }
    }
}
