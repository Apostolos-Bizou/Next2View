package com.next2me.next2view.controller;

import java.util.Map;
import com.next2me.next2view.model.ActivityLog;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.security.PermissionEvaluator;
import com.next2me.next2view.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/activity-log")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final PermissionEvaluator permissions;

    /**
     * GET /api/activity-log?limit=50&since=2026-04-29T10:00:00Z
     *
     * CEO: sees ALL activities.
     * Others: see only activities matching their allowedCategories().
     * Activities with category=NULL are visible to everyone.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityLog>> getActivities(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) Instant since,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        User user = permissions.requireUser(actorId);

        if (limit > 200) limit = 200;

        List<ActivityLog> activities;

        if (permissions.isCeo(user)) {
            activities = activityLogService.getRecentForCeo(limit, since);
        } else {
            Set<Project.Category> cats = permissions.allowedCategories(user);
            List<String> categoryNames = cats.stream()
                    .map(Enum::name)
                    .toList();
            activities = activityLogService.getRecentForCategories(categoryNames, limit, since);
        }


        // Filter out dismissed activities for this user
        Set<UUID> dismissed = activityLogService.getDismissedIds(actorId);
        if (!dismissed.isEmpty()) {
            activities = activities.stream()
                    .filter(a -> !dismissed.contains(a.getId()))
                    .toList();
        }

        return ResponseEntity.ok(activities);
    }

    /**
     * POST /api/activity-log/dismiss
     * Body: { "ids": ["uuid1", "uuid2"] }
     */
    @PostMapping("/dismiss")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> dismiss(
            @RequestBody Map<String, List<UUID>> body,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        List<UUID> ids = body.getOrDefault("ids", List.of());
        if (!ids.isEmpty()) {
            activityLogService.dismiss(actorId, ids);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/activity-log/dismiss-all
     * Dismisses all currently visible activities for this user.
     */
    @PostMapping("/dismiss-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> dismissAll(
            @RequestParam(defaultValue = "200") int limit,
            @AuthenticationPrincipal String userId
    ) {
        UUID actorId = parseUserId(userId);
        User user = permissions.requireUser(actorId);
        // Get all visible activity IDs for this user
        List<ActivityLog> visible;
        if (permissions.isCeo(user)) {
            visible = activityLogService.getRecentForCeo(limit, null);
        } else {
            Set<Project.Category> cats = permissions.allowedCategories(user);
            List<String> categoryNames = cats.stream().map(Enum::name).toList();
            visible = activityLogService.getRecentForCategories(categoryNames, limit, null);
        }
        List<UUID> ids = visible.stream().map(ActivityLog::getId).toList();
        if (!ids.isEmpty()) {
            activityLogService.dismissAll(actorId, ids);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/activity-log/entity/{entityType}/{entityId}
     * Returns activity history for a specific entity (e.g. project history).
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityLog>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @AuthenticationPrincipal String userId
    ) {
        parseUserId(userId); // auth check
        List<ActivityLog> history = activityLogService.getEntityHistory(entityType.toUpperCase(), entityId);
        return ResponseEntity.ok(history);
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
