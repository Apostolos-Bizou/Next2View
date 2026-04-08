package com.next2me.next2view.controller;

import com.next2me.next2view.dto.ProjectDto;
import com.next2me.next2view.dto.ProjectRequest;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

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
            Principal principal
    ) {
        UUID actorId = UUID.fromString(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(req, actorId, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO','DEPT_HEAD')")
    public ResponseEntity<ProjectDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest req,
            Principal principal
    ) {
        UUID actorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(projectService.update(id, req, actorId, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Principal principal
    ) {
        projectService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}