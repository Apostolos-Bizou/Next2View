package com.next2me.next2view.service;

import com.next2me.next2view.model.ActivityLog;
import com.next2me.next2view.model.ActivityDismissal;
import com.next2me.next2view.repository.ActivityDismissalRepository;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
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

    // ═══════════════════════════════════════════════════════
    // Core logging — async, never fails the caller
    // ═══════════════════════════════════════════════════════

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(User actor, String actionType, String entityType,
                            UUID entityId, String entityName, String category,
                            UUID companyId, String description) {
        try {
            activityLogRepository.save(ActivityLog.builder()
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
        } catch (Exception e) {
            log.error("Failed to log activity: {} {} {}", actionType, entityType, entityName, e);
        }
    }

    /** Overload with metadata */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(User actor, String actionType, String entityType,
                            UUID entityId, String entityName, String category,
                            UUID companyId, String description, Map<String, Object> metadata) {
        try {
            activityLogRepository.save(ActivityLog.builder()
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
        } catch (Exception e) {
            log.error("Failed to log activity: {} {} {}", actionType, entityType, entityName, e);
        }
    }

    // ═══════════════════════════════════════════════════════
    // Query methods (called by controller)
    // ═══════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════
    // Constants
    // ═══════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════
    // Dismiss methods
    // ═══════════════════════════════════════════════════════

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
