package com.next2me.next2view.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

/**
 * Validates uploaded contract files against the project's security policy.
 * - Whitelisted extensions: pdf, doc, docx, jpg, jpeg, png
 * - Max size: 10 MB
 * - Filename sanitization: alphanumeric + dot/dash/underscore/space only
 * - Content-Type must match extension
 */
@Component
public class FileValidator {

    public static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    // extension (lowercase) -> allowed content types
    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "pdf",  Set.of("application/pdf"),
            "doc",  Set.of("application/msword"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            "jpg",  Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png",  Set.of("image/png")
    );

    public static class Validated {
        public final String extension;
        public final String sanitizedFilename;
        public Validated(String ext, String name) {
            this.extension = ext;
            this.sanitizedFilename = name;
        }
    }

    public Validated validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw bad("File is required");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw bad("File exceeds maximum size of 10 MB");
        }

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw bad("Filename is required");
        }

        // Reject if contains path separators (path traversal defense)
        if (original.contains("/") || original.contains("\\") || original.contains("..")) {
            throw bad("Invalid filename");
        }

        String ext = extractExtension(original);
        if (ext == null || !ALLOWED.containsKey(ext)) {
            throw bad("File type not allowed. Permitted: pdf, doc, docx, jpg, png");
        }

        String contentType = file.getContentType();
        Set<String> allowedMimes = ALLOWED.get(ext);
        if (contentType == null || !allowedMimes.contains(contentType.toLowerCase())) {
            throw bad("Content-Type does not match extension");
        }

        String sanitized = sanitizeFilename(original);
        return new Validated(ext, sanitized);
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1).toLowerCase();
    }

    private String sanitizeFilename(String name) {
        // Keep only a-z, A-Z, 0-9, dot, dash, underscore, space
        String cleaned = name.replaceAll("[^a-zA-Z0-9._\\- ]", "_");
        // Collapse multiple spaces/underscores
        cleaned = cleaned.replaceAll("_+", "_").trim();
        if (cleaned.isEmpty() || cleaned.equals(".")) {
            cleaned = "file";
        }
        // Limit total length to 200 chars
        if (cleaned.length() > 200) {
            String ext = extractExtension(cleaned);
            int keepLen = 200 - (ext != null ? ext.length() + 1 : 0);
            cleaned = cleaned.substring(0, keepLen) + (ext != null ? "." + ext : "");
        }
        return cleaned;
    }

    private ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
