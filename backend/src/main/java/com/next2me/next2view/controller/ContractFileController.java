package com.next2me.next2view.controller;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.next2me.next2view.model.ContractFile;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.*;

@RestController
@RequestMapping("/projects/{projectId}/files")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CEO')")
public class ContractFileController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    @Value("${azure.storage.account:}")
    private String storageAccount;

    @Value("${azure.storage.key:}")
    private String storageKey;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        try {
            if (storageAccount != null && !storageAccount.isBlank() &&
                storageKey != null && !storageKey.isBlank()) {
                String connStr = String.format(
                    "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                    storageAccount, storageKey);
                BlobServiceClient client = new BlobServiceClientBuilder()
                    .connectionString(connStr).buildClient();
                containerClient = client.getBlobContainerClient("contracts");
                log.info("Azure Blob Storage initialized: {}", storageAccount);
            }
        } catch (Exception e) {
            log.warn("Azure Storage init failed: {}", e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listFiles(@PathVariable UUID projectId) {
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
            m.put("uploadedBy", f.getUploadedBy() != null ? f.getUploadedBy().getFullName() : "CEO");
            m.put("downloadUrl", "/api/projects/" + projectId + "/files/" + f.getId() + "/download");
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable UUID projectId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId
    ) throws Exception {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User user = userId != null && !userId.equals("anonymousUser")
            ? userRepository.findById(UUID.fromString(userId)).orElse(null) : null;

        String blobName = projectId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        if (containerClient != null) {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        }

        ContractFile cf = ContractFile.builder()
            .project(project)
            .fileName(file.getOriginalFilename())
            .blobPath(blobName)
            .contentType(file.getContentType())
            .fileSizeBytes(file.getSize())
            .uploadedBy(user)
            .build();
        em.persist(cf);

        Map<String, Object> result = new HashMap<>();
        result.put("id", cf.getId());
        result.put("fileName", cf.getFileName());
        result.put("fileSizeBytes", cf.getFileSizeBytes());
        result.put("uploadedAt", cf.getUploadedAt());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> download(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId
    ) {
        ContractFile cf = em.find(ContractFile.class, fileId);
        if (cf == null) return ResponseEntity.notFound().build();

        if (containerClient != null) {
            try {
                BlobClient blobClient = containerClient.getBlobClient(cf.getBlobPath());
                String sasUrl = blobClient.getBlobUrl();
                return ResponseEntity.ok(Map.of("url", sasUrl, "fileName", cf.getFileName()));
            } catch (Exception e) {
                log.error("Download error: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(Map.of("fileName", cf.getFileName(), "url", ""));
    }

    @DeleteMapping("/{fileId}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId
    ) {
        ContractFile cf = em.find(ContractFile.class, fileId);
        if (cf != null) {
            cf.setIsActive(false);
            if (containerClient != null) {
                try { containerClient.getBlobClient(cf.getBlobPath()).delete(); } catch (Exception ignored) {}
            }
        }
        return ResponseEntity.noContent().build();
    }
}