package com.next2me.next2view.controller;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.next2me.next2view.model.SecurityDocument;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.SecurityDocumentRepository;
import com.next2me.next2view.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/security-documents")
@RequiredArgsConstructor
@Slf4j
public class SecurityDocumentController {

    private final SecurityDocumentRepository repo;
    private final UserRepository userRepo;
    private final com.next2me.next2view.security.PermissionEvaluator permissionEvaluator;

    @Value("${azure.storage.legal-vault.account-name:next2viewlegalstorage}")
    private String storageAccount;

    @Value("${azure.storage.security-docs.container:security-docs}")
    private String containerName;



    private String getCurrentUserId() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) return null;
        return auth.getName();
    }

    // Inline permission check - replaces @PreAuthorize for reliability
    private boolean canViewSecurity(String userId) {
        if (userId == null) return false;
        try {
            UUID uid = UUID.fromString(userId);
            User user = userRepo.findById(uid).orElse(null);
            if (user == null) return false;
            // CEO has full access
            if (user.getRole() == User.Role.CEO) return true;
            // Otherwise check viewSecurity flag - we use direct query via repository
            return permissionEvaluator.canViewSecurity(userId);
        } catch (Exception e) {
            return false;
        }
    }

    private BlobContainerClient getContainer() {
        BlobServiceClient client = new BlobServiceClientBuilder()
            .endpoint("https://" + storageAccount + ".blob.core.windows.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        BlobContainerClient container = client.getBlobContainerClient(containerName);
        if (!container.exists()) {
            container.create();
        }
        return container;
    }

    // List all active security documents
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        if (!canViewSecurity(getCurrentUserId())) return ResponseEntity.status(403).body(java.util.Collections.emptyList());
        List<SecurityDocument> docs = repo.findAllActive();
        List<Map<String, Object>> response = docs.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId().toString());
            m.put("filename", d.getFilename());
            m.put("contentType", d.getContentType());
            m.put("sizeBytes", d.getSizeBytes());
            m.put("description", d.getDescription());
            m.put("uploadedAt", d.getUploadedAt().toString());
            // Include uploader name
            userRepo.findById(d.getUploaderId()).ifPresent(u -> {
                m.put("uploaderName", u.getFullName() != null ? u.getFullName() : u.getEmail());
            });
            return m;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // Upload new document
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal String userId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            UUID uploaderId = UUID.fromString(userId);
            User uploader = userRepo.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String originalFilename = file.getOriginalFilename();
            String blobPath = "secdoc-" + UUID.randomUUID() + "-" + System.currentTimeMillis();

            BlobContainerClient container = getContainer();
            BlobClient blobClient = container.getBlobClient(blobPath);

            BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(file.getContentType());

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            SecurityDocument doc = SecurityDocument.builder()
                .filename(originalFilename)
                .blobPath(blobPath)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .description(description)
                .uploaderId(uploaderId)
                .uploadedAt(Instant.now())
                .build();
            repo.save(doc);

            log.info("Security document uploaded: {} ({} bytes) by {}",
                originalFilename, file.getSize(), uploader.getEmail());

            return ResponseEntity.ok(Map.of(
                "id", doc.getId().toString(),
                "filename", doc.getFilename(),
                "success", true
            ));
        } catch (Exception e) {
            log.error("Security document upload failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Download document
    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable UUID id) {
        try {
            if (!canViewSecurity(getCurrentUserId())) return ResponseEntity.status(403).body(java.util.Map.of("error", "forbidden"));
            SecurityDocument doc = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
            if (doc.getDeletedAt() != null) {
                return ResponseEntity.notFound().build();
            }

            BlobContainerClient container = getContainer();
            BlobClient blobClient = container.getBlobClient(doc.getBlobPath());

            byte[] data = blobClient.downloadContent().toBytes();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(doc.getContentType()));
            responseHeaders.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                    .filename(doc.getFilename())
                    .build()
            );
            responseHeaders.setContentLength(data.length);

            log.info("Security document downloaded: {} by user", doc.getFilename());

            return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(new InputStreamResource(new ByteArrayInputStream(data)));
        } catch (Exception e) {
            log.error("Security document download failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Soft-delete document
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        String uid = getCurrentUserId();
        if (uid == null) return ResponseEntity.status(403).body(java.util.Map.of("error", "forbidden"));
        User u = userRepo.findById(UUID.fromString(uid)).orElse(null);
        if (u == null || u.getRole() != User.Role.CEO) return ResponseEntity.status(403).body(java.util.Map.of("error", "ceo only"));
        SecurityDocument doc = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        doc.setDeletedAt(Instant.now());
        repo.save(doc);
        log.info("Security document soft-deleted: {}", doc.getFilename());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
