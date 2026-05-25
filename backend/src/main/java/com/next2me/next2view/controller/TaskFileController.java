package com.next2me.next2view.controller;

import com.next2me.next2view.model.ContractFile;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.Task;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.TaskRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for encrypted TASK-level file attachments (v5.4.0).
 *
 * Routes:
 *   GET    /tasks/{taskId}/files              - list active files for a task
 *   POST   /tasks/{taskId}/files              - upload + encrypt (attached to task)
 *   GET    /tasks/{taskId}/files/{id}/content - download + decrypt (binary)
 *   DELETE /tasks/{taskId}/files/{id}         - soft delete
 *
 * Security model — IDENTICAL to project files:
 *   The parent project is resolved via task -> module -> project, then:
 *     - requireCanRead(project)   for list + download
 *     - requireCanWrite(project)  for upload + delete
 *     - requireMfaForFiles(project, actor)  on EVERY operation
 *   => Files on tasks belonging to finance/legal projects inherit the same
 *      MFA gate as contracts. No bypass is possible.
 *
 * Encryption, blob storage, audit and soft-delete are delegated to the SAME
 * ContractFileService used for project files (uploadEncryptedForTask / listActiveForTask /
 * downloadDecrypted / softDelete).
 */
@RestController
@RequestMapping("/tasks/{taskId}/files")
@RequiredArgsConstructor
@Slf4j
public class TaskFileController {

    private final TaskRepository taskRepository;
    private final PermissionEvaluator permissions;
    private final FileValidator fileValidator;
    private final ContractFileService contractFileService;

    // =================================================================
    // LIST
    // =================================================================

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listFiles(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Task task = requireTask(taskId);
        Project project = task.getModule().getProject();
        permissions.requireCanRead(actor, project);
        permissions.requireMfaForFiles(project, actor);

        List<ContractFile> files = contractFileService.listActiveForTask(taskId);

        List<Map<String, Object>> result = files.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("fileName", f.getFileName());
            m.put("contentType", f.getContentType());
            m.put("fileSizeBytes", f.getFileSizeBytes());
            m.put("uploadedAt", f.getUploadedAt());
            m.put("uploadedBy", f.getUploadedBy() != null ? f.getUploadedBy().getFullName() : "Unknown");
            m.put("encrypted", f.isEncrypted());
            m.put("downloadUrl", "/api/tasks/" + taskId + "/files/" + f.getId() + "/content");
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // =================================================================
    // UPLOAD (encrypted)
    // =================================================================

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Task task = requireTask(taskId);
        Project project = task.getModule().getProject();
        permissions.requireCanWrite(actor, project);
        permissions.requireMfaForFiles(project, actor);

        FileValidator.Validated v = fileValidator.validate(file);

        ContractFile cf = contractFileService.uploadEncryptedForTask(file, task, actor, v.sanitizedFilename);

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
            @PathVariable UUID taskId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Task task = requireTask(taskId);
        Project project = task.getModule().getProject();
        permissions.requireCanRead(actor, project);
        permissions.requireMfaForFiles(project, actor);

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
            @PathVariable UUID taskId,
            @PathVariable UUID fileId,
            @RequestParam(value = "reason", required = false) String reason,
            @AuthenticationPrincipal String userId
    ) {
        User actor = permissions.requireUser(parseUserId(userId));
        Task task = requireTask(taskId);
        Project project = task.getModule().getProject();
        permissions.requireCanWrite(actor, project);
        permissions.requireMfaForFiles(project, actor);

        contractFileService.softDelete(fileId, actor, reason);
        return ResponseEntity.noContent().build();
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private Task requireTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
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
