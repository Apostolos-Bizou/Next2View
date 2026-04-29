const fs = require('fs');

// === 1. Patch ActivityLogService — add dismiss methods ===
const svcFile = 'backend/src/main/java/com/next2me/next2view/service/ActivityLogService.java';
let svc = fs.readFileSync(svcFile, 'utf8').split(/\r?\n/);

if (svc.some(l => l.includes('ActivityDismissal'))) {
  console.log('SKIP service: already patched');
} else {
  // Add import
  const impIdx = svc.findIndex(l => l.includes('import com.next2me.next2view.model.ActivityLog;'));
  svc.splice(impIdx + 1, 0, 'import com.next2me.next2view.model.ActivityDismissal;',
    'import com.next2me.next2view.repository.ActivityDismissalRepository;');

  // Add field
  const fieldIdx = svc.findIndex(l => l.includes('private final ActivityLogRepository activityLogRepository;'));
  svc.splice(fieldIdx + 1, 0, '    private final ActivityDismissalRepository activityDismissalRepository;');

  // Add dismiss methods before the constants section
  const constIdx = svc.findIndex(l => l.includes('// Action types'));
  if (constIdx > 0) {
    svc.splice(constIdx, 0,
      '    // ═══════════════════════════════════════════════════════',
      '    // Dismiss methods',
      '    // ═══════════════════════════════════════════════════════',
      '',
      '    public Set<UUID> getDismissedIds(UUID userId) {',
      '        return activityDismissalRepository.findDismissedActivityIdsByUserId(userId);',
      '    }',
      '',
      '    @Transactional',
      '    public void dismiss(UUID userId, List<UUID> activityIds) {',
      '        for (UUID activityId : activityIds) {',
      '            if (!activityDismissalRepository.existsByUserIdAndActivityId(userId, activityId)) {',
      '                activityDismissalRepository.save(ActivityDismissal.builder()',
      '                        .userId(userId).activityId(activityId).build());',
      '            }',
      '        }',
      '    }',
      '',
      '    @Transactional',
      '    public void dismissAll(UUID userId, List<UUID> activityIds) {',
      '        for (UUID activityId : activityIds) {',
      '            if (!activityDismissalRepository.existsByUserIdAndActivityId(userId, activityId)) {',
      '                activityDismissalRepository.save(ActivityDismissal.builder()',
      '                        .userId(userId).activityId(activityId).build());',
      '            }',
      '        }',
      '    }',
      '',
      '    import java.util.Set; // will be at top',
      ''
    );
  }

  // Add Set import at top
  const setImpIdx = svc.findIndex(l => l.includes('import java.util.ArrayList;'));
  if (setImpIdx >= 0 && !svc.some(l => l.includes('import java.util.Set;'))) {
    svc.splice(setImpIdx + 1, 0, 'import java.util.Set;');
  }

  // Remove the misplaced import line inside methods
  svc = svc.filter(l => l.trim() !== 'import java.util.Set; // will be at top');

  fs.writeFileSync(svcFile, svc.join('\n'), 'utf8');
  console.log('DONE: ActivityLogService patched with dismiss methods');
}

// === 2. Patch ActivityLogController — add dismiss endpoints ===
const ctrlFile = 'backend/src/main/java/com/next2me/next2view/controller/ActivityLogController.java';
let ctrl = fs.readFileSync(ctrlFile, 'utf8').split(/\r?\n/);

if (ctrl.some(l => l.includes('dismiss'))) {
  console.log('SKIP controller: already patched');
} else {
  // Add imports
  const impIdx2 = ctrl.findIndex(l => l.includes('import com.next2me.next2view.model.ActivityLog;'));
  ctrl.splice(impIdx2, 0, 'import java.util.Map;');

  // Add dismiss endpoints before parseUserId method
  const parseIdx = ctrl.findIndex(l => l.includes('private UUID parseUserId'));
  if (parseIdx > 0) {
    ctrl.splice(parseIdx, 0,
      '    /**',
      '     * POST /api/activity-log/dismiss',
      '     * Body: { "ids": ["uuid1", "uuid2"] }',
      '     */',
      '    @PostMapping("/dismiss")',
      '    @PreAuthorize("isAuthenticated()")',
      '    public ResponseEntity<Void> dismiss(',
      '            @RequestBody Map<String, List<UUID>> body,',
      '            @AuthenticationPrincipal String userId',
      '    ) {',
      '        UUID actorId = parseUserId(userId);',
      '        List<UUID> ids = body.getOrDefault("ids", List.of());',
      '        if (!ids.isEmpty()) {',
      '            activityLogService.dismiss(actorId, ids);',
      '        }',
      '        return ResponseEntity.ok().build();',
      '    }',
      '',
      '    /**',
      '     * POST /api/activity-log/dismiss-all',
      '     * Dismisses all currently visible activities for this user.',
      '     */',
      '    @PostMapping("/dismiss-all")',
      '    @PreAuthorize("isAuthenticated()")',
      '    public ResponseEntity<Void> dismissAll(',
      '            @RequestParam(defaultValue = "200") int limit,',
      '            @AuthenticationPrincipal String userId',
      '    ) {',
      '        UUID actorId = parseUserId(userId);',
      '        User user = permissions.requireUser(actorId);',
      '        // Get all visible activity IDs for this user',
      '        List<ActivityLog> visible;',
      '        if (permissions.isCeo(user)) {',
      '            visible = activityLogService.getRecentForCeo(limit, null);',
      '        } else {',
      '            Set<Project.Category> cats = permissions.allowedCategories(user);',
      '            List<String> categoryNames = cats.stream().map(Enum::name).toList();',
      '            visible = activityLogService.getRecentForCategories(categoryNames, limit, null);',
      '        }',
      '        List<UUID> ids = visible.stream().map(ActivityLog::getId).toList();',
      '        if (!ids.isEmpty()) {',
      '            activityLogService.dismissAll(actorId, ids);',
      '        }',
      '        return ResponseEntity.ok().build();',
      '    }',
      ''
    );
  }

  // Filter dismissed in GET endpoint — add dismissed filter after getting activities
  // Find "return ResponseEntity.ok(activities);" and add filter before it
  const returnIdx = ctrl.findIndex(l => l.includes('return ResponseEntity.ok(activities)'));
  if (returnIdx > 0) {
    ctrl.splice(returnIdx, 0,
      '',
      '        // Filter out dismissed activities for this user',
      '        Set<UUID> dismissed = activityLogService.getDismissedIds(actorId);',
      '        if (!dismissed.isEmpty()) {',
      '            activities = activities.stream()',
      '                    .filter(a -> !dismissed.contains(a.getId()))',
      '                    .toList();',
      '        }',
      ''
    );
  }

  // Add Set import
  if (!ctrl.some(l => l.includes('import java.util.Set;'))) {
    const setIdx = ctrl.findIndex(l => l.includes('import java.util.UUID;'));
    ctrl.splice(setIdx + 1, 0, 'import java.util.Set;');
  }

  fs.writeFileSync(ctrlFile, ctrl.join('\n'), 'utf8');
  console.log('DONE: ActivityLogController patched with dismiss endpoints');
}