package com.next2me.next2view.controller;

import com.next2me.next2view.dto.CeoNoteDto;
import com.next2me.next2view.model.CeoNote;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.CeoNoteRepository;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/notes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CEO')")
public class CeoNoteController {

    private final CeoNoteRepository noteRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CeoNoteDto>> getNotes(@PathVariable UUID projectId) {
        List<CeoNoteDto> notes = noteRepository
            .findByProjectIdOrderByCreatedAtDesc(projectId)
            .stream()
            .map(this::toDto)
            .toList();
        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<CeoNoteDto> addNote(
            @PathVariable UUID projectId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal String userId
    ) {
        if (userId == null || userId.equals("anonymousUser"))
            return ResponseEntity.status(401).build();

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        User user = userRepository.findById(UUID.fromString(userId)).orElse(null);

        CeoNote note = CeoNote.builder()
            .project(project)
            .content(body.getOrDefault("content", ""))
            .createdBy(user)
            .build();

        noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(note));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable UUID projectId,
            @PathVariable UUID noteId
    ) {
        noteRepository.findById(noteId).ifPresent(noteRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private CeoNoteDto toDto(CeoNote n) {
        return new CeoNoteDto(
            n.getId(),
            n.getContent(),
            n.getCreatedBy() != null ? n.getCreatedBy().getFullName() : "CEO",
            n.getCreatedAt()
        );
    }
}