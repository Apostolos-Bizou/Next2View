package com.next2me.next2view.service;

import com.next2me.next2view.model.ActivityLog;
import com.next2me.next2view.model.ActivityDismissal;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.ActivityDismissalRepository;
import com.next2me.next2view.repository.ActivityLogRepository;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.security.PermissionEvaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityDismissalRepository activityDismissalRepository;
    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;
    private final PermissionEvaluator permissionEvaluator;
    private final ObjectMapper objectMapper;

    // ═══════════════════════════════════════════
    // Core logging + SSE broadcast
    // ═══════════════════════════════════════════

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(User actor, String actionType, String entityType,
                            UUID entityId, String entityName, String category,
                            UUID companyId, String description) {
        try {
            ActivityLog saved = activityLogRepository.save(ActivityLog.builder()
                    .actorId(actor.getId())
                    .actorName(actor.getFullName())
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .category(category)
                    .companyId(companyId)
                    .description(description)
                    .build());
            log.debug("Activity: {} {} {} '{}'", actor.getEmail(), actionType, entityType, entityName);
            broadcastToEligibleUsers(actor, saved);
        } catch (Exception e) {
            log.error("Failed to log activity: {} {} {}", actionType, entityType, entityName, e);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(User actor, String actionType, String entityType,
                            UUID entityId, String entityName, String category,
                            UUID companyId, String description, Map<String, Object> metadata) {
        try {
            ActivityLog saved = activityLogRepository.save(ActivityLog.builder()
                    .actorId(actor.getId())
                    .actorName(actor.getFullName())
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .category(category)
                    .companyId(companyId)
                    .description(description)
                    .metadata(metadata)
                    .build());
            broadcastToEligibleUsers(actor, saved);
        } catch (Exception e) {
            log.error("Failed to log activity: {} {} {}", actionType, entityType, entityName, e);
        }
    }

    // ═══════════════════════════════════════════
    // SSE broadcast to eligible users
    // ═══════════════════════════════════════════

    private void broadcastToEligibleUsers(User actor, ActivityLog activity) {
        try {
            String category = activity.getCategory();
            Project.Category cat = category != null ? Project.Category.valueOf(category) : null;

            Set<UUID> targetUserIds = new HashSet<>();
            List<User> activeUsers = userRepository.findAllByActiveTrue();

            for (User user : activeUsers) {
                if (user.getId().equals(actor.getId())) continue;
                try {
                    if (permissionEvaluator.isCeo(user)) {
                        targetUserIds.add(user.getId());
                    } else if (cat != null) {
                        Set<Project.Category> allowed = permissionEvaluator.allowedCategories(user);
                        if (allowed.contains(cat)) {
                            targetUserIds.add(user.getId());
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (!targetUserIds.isEmpty()) {
                String json = objectMapper.writeValueAsString(Map.of(
                    "id", activity.getId().toString(),
                    "actorName", activity.getActorName(),
                    "actionType", activity.getActionType(),
                    "entityType", activity.getEntityType(),
                    "entityName", activity.getEntityName() != null ? activity.getEntityName() : "",
                    "category", category != null ? category : "",
                    "description", activity.getDescription() != null ? activity.getDescription() : "",
                    "createdAt", activity.getCreatedAt().toString()
                ));
                sseEmitterService.broadcast(actor.getId(), json, targetUserIds);
                log.debug("SSE broadcast to {} users for {}", targetUserIds.size(), activity.getActionType());
            }
        } catch (Exception e) {
            log.error("SSE broadcast failed", e);
        }
    }

    // ═══════════════════════════════════════════
    // Query methods
    // ═══════════════════════════════════════════

    public List<ActivityLog> getRecentForCeo(int limit, Instant since) {
        PageRequest page = PageRequest.of(0, limit);
        return since != null
                ? activityLogRepository.findRecentSince(since, page)
                : activityLogRepository.findRecent(page);
    }

    public List<ActivityLog> getRecentForCategories(List<String> categories, int limit, Instant since) {
        if (categories == null || categories.isEmpty()) return new ArrayList<>();
        PageRequest page = PageRequest.of(0, limit);
        return since != null
                ? activityLogRepository.findRecentByCategoriesSince(categories, since, page)
                : activityLogRepository.findRecentByCategories(categories, page);
    }

    public List<ActivityLog> getEntityHistory(String entityType, UUID entityId) {
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    // ═══════════════════════════════════════════
    // Dismiss methods
    // ═══════════════════════════════════════════

    public Set<UUID> getDismissedIds(UUID userId) {
        return activityDismissalRepository.findDismissedActivityIdsByUserId(userId);
    }

    @Transactional
    public void dismiss(UUID userId, List<UUID> activityIds) {
        for (UUID activityId : activityIds) {
            if (!activityDismissalRepository.existsByUserIdAndActivityId(userId, activityId)) {
                activityDismissalRepository.save(ActivityDismissal.builder()
                        .userId(userId).activityId(activityId).build());
            }
        }
    }

    @Transactional
    public void dismissAll(UUID userId, List<UUID> activityIds) {
        for (UUID activityId : activityIds) {
            if (!activityDismissalRepository.existsByUserIdAndActivityId(userId, activityId)) {
                activityDismissalRepository.save(ActivityDismissal.builder()
                        .userId(userId).activityId(activityId).build());
            }
        }
    }

    // Action types
    public static final String CREATED        = "CREATED";
    public static final String UPDATED        = "UPDATED";
    public static final String DELETED        = "DELETED";
    public static final String COMPLETED      = "COMPLETED";
    public static final String UPLOADED       = "UPLOADED";
    public static final String COMMENTED      = "COMMENTED";
    public static final String STATUS_CHANGED = "STATUS_CHANGED";

    // Entity types
    public static final String PROJECT = "PROJECT";
    public static final String TASK    = "TASK";
    public static final String COMPANY = "COMPANY";
    public static final String USER    = "USER";
    public static final String FILE    = "FILE";
    public static final String MODULE  = "MODULE";
    public static final String COMMENT = "COMMENT";
}
