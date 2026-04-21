package com.next2me.next2view.controller;

import com.next2me.next2view.dto.ProjectDto;
import com.next2me.next2view.dto.ProjectRequest;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAll(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Project.Category category,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        return ResponseEntity.ok(projectService.findAll(actorId, companyId, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getOne(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        return ResponseEntity.ok(projectService.findById(id, actorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CEO','DEPT_HEAD')")
    public ResponseEntity<ProjectDto> create(
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        String actorEmail = userRepository.findById(actorId)
                .map(u -> u.getEmail()).orElse(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(req, actorId, actorEmail));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO','DEPT_HEAD')")
    public ResponseEntity<ProjectDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        String actorEmail = userRepository.findById(actorId)
                .map(u -> u.getEmail()).orElse(userId);
        return ResponseEntity.ok(projectService.update(id, req, actorId, actorEmail));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        String actorEmail = userRepository.findById(actorId)
                .map(u -> u.getEmail()).orElse(userId);
        projectService.delete(id, actorId, actorEmail);
        return ResponseEntity.noContent().build();
    }
    /**
     * Parse the authentication principal into a UUID.
     * Throws 401 if the principal is missing or invalid.
     */
    private UUID parseUserId(String userId) {
        if (userId == null || userId.isBlank() || "anonymousUser".equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid authentication");
        }
    }
}