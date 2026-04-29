package com.next2me.next2view.controller;

import com.next2me.next2view.model.ContractFile;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.security.FileValidator;
import com.next2me.next2view.security.PermissionEvaluator;
import com.next2me.next2view.service.ContractFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for encrypted contract files (Legal Vault v1.0).
 *
 * Routes:
 *   GET    /projects/{projectId}/files              - list active files
 *   POST   /projects/{projectId}/files              - upload + encrypt
 *   GET    /projects/{projectId}/files/{id}/content - download + decrypt (binary)
 *   DELETE /projects/{projectId}/files/{id}         - soft delete
 *
 * Permission model (unchanged from previous version):
 *   - requireCanRead(project)  for list + download
 *   - requireCanWrite(project) for upload + delete
 *
 * Responsibility split:
 *   - Controller: auth, permissions, file validation, HTTP mapping
 *   - Service (ContractFileService): encryption, blob ops, audit, DB persistence
 */
@RestController
@RequestMapping("/projects/{projectId}/files")
@RequiredArgsConstructor
@Slf4j
public class ContractFileController {

    private final ProjectRepository projectRepository;
    private final PermissionEvaluator permissions;
    private final FileValidator fileValidator;
    private final ContractFileService contractFileService;

    // =================================================================
    // LIST
    // =================================================================

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listFiles(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Project project = requireProject(projectId);
        permissions.requireCanRead(actor, project);
        permissions.requireMfaForFiles(project, isMfaVerified(), actor);

        List<ContractFile> files = contractFileService.listActive(projectId);

        List<Map<String, Object>> result = files.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("fileName", f.getFileName());
            m.put("contentType", f.getContentType());
            m.put("fileSizeBytes", f.getFileSizeBytes());
            m.put("uploadedAt", f.getUploadedAt());
            m.put("uploadedBy", f.getUploadedBy() != null ? f.getUploadedBy().getFullName() : "Unknown");
            m.put("encrypted", f.isEncrypted());
            m.put("downloadUrl", "/api/projects/" + projectId + "/files/" + f.getId() + "/content");
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // =================================================================
    // UPLOAD (encrypted)
    // =================================================================

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable UUID projectId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Project project = requireProject(projectId);
        permissions.requireCanWrite(actor, project);
        permissions.requireMfaForFiles(project, isMfaVerified(), actor);

        // Validate (throws 400 if invalid)
        FileValidator.Validated v = fileValidator.validate(file);

        // Delegate encryption + upload + audit to service
        ContractFile cf = contractFileService.uploadEncrypted(file, project, actor, v.sanitizedFilename);

        Map<String, Object> result = new HashMap<>();
        result.put("id", cf.getId());
        result.put("fileName", cf.getFileName());
        result.put("fileSizeBytes", cf.getFileSizeBytes());
        result.put("uploadedAt", cf.getUploadedAt());
        result.put("encrypted", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // =================================================================
    // DOWNLOAD (returns decrypted binary stream)
    // =================================================================

    @GetMapping("/{fileId}/content")
    public ResponseEntity<Resource> downloadContent(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));

        // Permission check: load project first to verify read access
        // The service will also validate project match via findActiveById
        Project project = requireProject(projectId);
        permissions.requireCanRead(actor, project);
        permissions.requireMfaForFiles(project, isMfaVerified(), actor);

        ContractFileService.DecryptedFile decrypted =
                contractFileService.downloadDecrypted(fileId, actor);

        ByteArrayResource body = new ByteArrayResource(decrypted.plaintext());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(decrypted.contentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(decrypted.plaintext().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + decrypted.fileName() + "\"")
                .body(body);
    }

    // =================================================================
    // DELETE (soft delete)
    // =================================================================

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @RequestParam(value = "reason", required = false) String reason,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Project project = requireProject(projectId);
        permissions.requireCanWrite(actor, project);
        permissions.requireMfaForFiles(project, isMfaVerified(), actor);

        contractFileService.softDelete(fileId, actor, reason);
        return ResponseEntity.noContent().build();
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private boolean isMfaVerified() {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null) return false;
                return auth.getAuthorities().stream().anyMatch(a -> "MFA_VERIFIED".equals(a.getAuthority()));
            } catch (Exception e) { return false; }
        }
    
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
