package com.next2me.next2view.controller;

import com.next2me.next2view.dto.UserScopeDto;
import com.next2me.next2view.model.User;
import com.next2me.next2view.security.PermissionEvaluator;
import com.next2me.next2view.service.UserScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST endpoints for managing per-user scope restrictions.
 * CEO-only access. Used by the Admin Permissions modal.
 */
@RestController
@RequestMapping("/scopes")
@RequiredArgsConstructor
public class ScopeController {

    private final UserScopeService userScopeService;
    private final PermissionEvaluator permissionEvaluator;

    /**
     * Get the current scope configuration for the given user.
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<UserScopeDto> getUserScopes(@PathVariable UUID userId) {
        return ResponseEntity.ok(userScopeService.getForUser(userId));
    }

    /**
     * Replace the user scope configuration. Sending empty lists removes all restrictions.
     * Atomic: both scope tables are updated in a single transaction.
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<UserScopeDto> setUserScopes(
            @PathVariable UUID userId,
            @RequestBody UserScopeDto dto,
            @AuthenticationPrincipal String actorId) {
        User actor = permissionEvaluator.requireUser(UUID.fromString(actorId));
        return ResponseEntity.ok(userScopeService.setForUser(userId, dto, actor));
    }
}
