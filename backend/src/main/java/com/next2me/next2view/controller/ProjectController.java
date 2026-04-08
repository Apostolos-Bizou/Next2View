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
            @RequestParam(required = false) Project.Category category
    ) {
        return ResponseEntity.ok(projectService.findAll(companyId, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CEO','DEPT_HEAD')")
    public ResponseEntity<ProjectDto> create(
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = UUID.fromString(userId);
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
        UUID actorId = UUID.fromString(userId);
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
        UUID actorId = UUID.fromString(userId);
        String actorEmail = userRepository.findById(actorId)
                .map(u -> u.getEmail()).orElse(userId);
        projectService.delete(id, actorEmail);
        return ResponseEntity.noContent().build();
    }
}