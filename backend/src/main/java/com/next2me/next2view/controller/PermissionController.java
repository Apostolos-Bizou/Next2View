package com.next2me.next2view.controller;

import com.next2me.next2view.dto.PermissionDto;
import com.next2me.next2view.model.User;
import com.next2me.next2view.model.UserPermission;
import com.next2me.next2view.repository.UserPermissionRepository;
import com.next2me.next2view.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final UserPermissionRepository permRepo;
    private final UserRepository userRepo;

    // CEO gets own permissions (all true)
    @GetMapping("/me")
    public ResponseEntity<PermissionDto> getMyPermissions(
            @AuthenticationPrincipal String userId) {
        if (userId == null || userId.equals("anonymousUser"))
            return ResponseEntity.status(401).build();

        User user = userRepo.findById(UUID.fromString(userId)).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // CEO has all permissions
        if (user.getRole() == User.Role.CEO) {
            return ResponseEntity.ok(allTrue());
        }

        return permRepo.findByUserId(user.getId())
            .map(this::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(allFalse()));
    }

    // CEO only — get permissions for a specific user
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<PermissionDto> getUserPermissions(@PathVariable UUID userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getRole() == User.Role.CEO) return ResponseEntity.ok(allTrue());

        return permRepo.findByUserId(userId)
            .map(this::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(allFalse()));
    }

    // CEO only — set permissions for a user
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<PermissionDto> setUserPermissions(
            @PathVariable UUID userId,
            @RequestBody PermissionDto dto) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == User.Role.CEO)
            return ResponseEntity.ok(allTrue()); // CEO always has all

        UserPermission perm = permRepo.findByUserId(userId)
            .orElse(UserPermission.builder().user(user).build());

        perm.setViewFinance(dto.viewFinance());
        perm.setViewLegal(dto.viewLegal());
        perm.setViewDev(dto.viewDev());
        perm.setViewMarketing(dto.viewMarketing());
        perm.setViewFinancials(dto.viewFinancials());
        perm.setViewCeoNotes(dto.viewCeoNotes());
        perm.setUpdateTasks(dto.updateTasks());
        perm.setUploadFiles(dto.uploadFiles());
        perm.setCreateProject(dto.createProject());
        perm.setEditProject(dto.editProject());
        perm.setManageUsers(dto.manageUsers());
        perm.setManageCompanies(dto.manageCompanies());
        perm.setAiCeoReport(dto.aiCeoReport());
        perm.setAiContract(dto.aiContract());

        permRepo.save(perm);
        return ResponseEntity.ok(toDto(perm));
    }

    private PermissionDto toDto(UserPermission p) {
        return new PermissionDto(
            b(p.getViewFinance()), b(p.getViewLegal()),
            b(p.getViewDev()), b(p.getViewMarketing()),
            b(p.getViewFinancials()), b(p.getViewCeoNotes()),
            b(p.getUpdateTasks()), b(p.getUploadFiles()),
            b(p.getCreateProject()), b(p.getEditProject()),
            b(p.getManageUsers()), b(p.getManageCompanies()),
            b(p.getAiCeoReport()), b(p.getAiContract())
        );
    }

    private boolean b(Boolean v) { return v != null && v; }

    private PermissionDto allTrue() {
        return new PermissionDto(true,true,true,true,true,true,true,true,true,true,true,true,true,true);
    }
    private PermissionDto allFalse() {
        return new PermissionDto(false,false,false,false,false,false,false,false,false,false,false,false,false,false);
    }
}