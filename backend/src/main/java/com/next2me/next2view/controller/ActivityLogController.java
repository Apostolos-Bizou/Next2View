package com.next2me.next2view.controller;

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

        return ResponseEntity.ok(activities);
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
