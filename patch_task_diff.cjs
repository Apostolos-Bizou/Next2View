const fs = require('fs');
const filePath = 'C:/Users/akage/Next2View/backend/src/main/java/com/next2me/next2view/service/ProjectService.java';
let code = fs.readFileSync(filePath, 'utf8');

// ═══ STEP 1: Add TaskSnapshot record and snapshotTasks helper method ═══
// Insert before the last closing brace of the class

const helperCode = `
    // ── Task-level diff support ──

    private record TaskSnapshot(String name, String assignee, int progress, boolean isDone, boolean isBlocked, String moduleName) {}

    private Map<String, TaskSnapshot> snapshotTasks(Project p) {
        Map<String, TaskSnapshot> map = new LinkedHashMap<>();
        for (var m : p.getModules()) {
            for (var t : m.getTasks()) {
                String key = m.getName() + "::" + t.getName();
                map.put(key, new TaskSnapshot(t.getName(), t.getAssignee(), t.getProgress(), t.getIsDone(), t.getIsBlocked(), m.getName()));
            }
        }
        return map;
    }

    private void logTaskDiffs(User actor, Project p, Map<String, TaskSnapshot> oldTasks) {
        Map<String, TaskSnapshot> newTasks = snapshotTasks(p);
        String projectTitle = p.getTitle();
        UUID projectId = p.getId();
        String category = p.getCategory().name();
        UUID companyId = p.getCompany().getId();
        String actorName = actor.getFullName();

        // New tasks (in new but not in old)
        for (var entry : newTasks.entrySet()) {
            if (!oldTasks.containsKey(entry.getKey())) {
                TaskSnapshot ts = entry.getValue();
                activityLogService.logActivity(actor, "TASK_ADDED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " added task '" + ts.name() + "' in " + ts.moduleName());
            }
        }

        // Deleted tasks (in old but not in new)
        for (var entry : oldTasks.entrySet()) {
            if (!newTasks.containsKey(entry.getKey())) {
                TaskSnapshot ts = entry.getValue();
                activityLogService.logActivity(actor, "TASK_REMOVED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " removed task '" + ts.name() + "' from " + ts.moduleName());
            }
        }

        // Changed tasks (in both — check diffs)
        for (var entry : newTasks.entrySet()) {
            if (!oldTasks.containsKey(entry.getKey())) continue;
            TaskSnapshot oldT = oldTasks.get(entry.getKey());
            TaskSnapshot newT = entry.getValue();

            // Completed
            if (!oldT.isDone() && newT.isDone()) {
                activityLogService.logActivity(actor, "TASK_COMPLETED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " completed task '" + newT.name() + "' in " + newT.moduleName());
            }
            // Uncompleted (was done, now not)
            else if (oldT.isDone() && !newT.isDone()) {
                activityLogService.logActivity(actor, "TASK_REOPENED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " reopened task '" + newT.name() + "' in " + newT.moduleName());
            }
            // Progress changed (only if not a completion/reopen)
            else if (oldT.progress() != newT.progress()) {
                activityLogService.logActivity(actor, "TASK_PROGRESS", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " changed progress of '" + newT.name() + "' from " + oldT.progress() + "% to " + newT.progress() + "% in " + newT.moduleName());
            }

            // Assignee changed
            if (!java.util.Objects.equals(oldT.assignee(), newT.assignee())) {
                String from = oldT.assignee() != null ? oldT.assignee() : "unassigned";
                String to = newT.assignee() != null ? newT.assignee() : "unassigned";
                activityLogService.logActivity(actor, "TASK_REASSIGNED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " reassigned '" + newT.name() + "' from " + from + " to " + to + " in " + newT.moduleName());
            }

            // Blocked/unblocked
            if (!oldT.isBlocked() && newT.isBlocked()) {
                activityLogService.logActivity(actor, "TASK_BLOCKED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " blocked task '" + newT.name() + "' in " + newT.moduleName());
            } else if (oldT.isBlocked() && !newT.isBlocked()) {
                activityLogService.logActivity(actor, "TASK_UNBLOCKED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " unblocked task '" + newT.name() + "' in " + newT.moduleName());
            }
        }
    }
`;

// Insert before the final closing brace of the class
const lastBrace = code.lastIndexOf('}');
code = code.substring(0, lastBrace) + helperCode + '\n}\n';

// ═══ STEP 2: In update(), snapshot BEFORE clear, and logTaskDiffs AFTER save ═══

// Find the update method's "Map<String, Object> oldVal" line and add snapshot after it
code = code.replace(
    '        Map<String, Object> oldVal = Map.of("title", p.getTitle());',
    '        Map<String, Object> oldVal = Map.of("title", p.getTitle());\n' +
    '        Map<String, TaskSnapshot> oldTasks = snapshotTasks(p);'
);

// After the existing logActivity call in update(), add logTaskDiffs
code = code.replace(
    `        activityLogService.logActivity(actor, ActivityLogService.UPDATED, ActivityLogService.PROJECT,
                p.getId(), p.getTitle(), p.getCategory().name(),
                p.getCompany().getId(), actor.getFullName() + " updated project '" + p.getTitle() + "'");

        return toDto(p);
    }`,
    `        activityLogService.logActivity(actor, ActivityLogService.UPDATED, ActivityLogService.PROJECT,
                p.getId(), p.getTitle(), p.getCategory().name(),
                p.getCompany().getId(), actor.getFullName() + " updated project '" + p.getTitle() + "'");

        // Granular task-level logging
        logTaskDiffs(actor, p, oldTasks);

        return toDto(p);
    }`
);

// ═══ STEP 3: Add LinkedHashMap import ═══
if (!code.includes('import java.util.LinkedHashMap')) {
    code = code.replace(
        'import java.util.*;',
        'import java.util.*;\nimport java.util.LinkedHashMap;'
    );
}

fs.writeFileSync(filePath, code, 'utf8');

// Verify
const verify = fs.readFileSync(filePath, 'utf8');
console.log('snapshotTasks added:', verify.includes('private Map<String, TaskSnapshot> snapshotTasks'));
console.log('logTaskDiffs added:', verify.includes('private void logTaskDiffs'));
console.log('oldTasks snapshot in update:', verify.includes('Map<String, TaskSnapshot> oldTasks = snapshotTasks(p)'));
console.log('logTaskDiffs called in update:', verify.includes('logTaskDiffs(actor, p, oldTasks)'));
console.log('TASK_ADDED action:', verify.includes('TASK_ADDED'));
console.log('TASK_COMPLETED action:', verify.includes('TASK_COMPLETED'));
console.log('TASK_PROGRESS action:', verify.includes('TASK_PROGRESS'));
console.log('TASK_REASSIGNED action:', verify.includes('TASK_REASSIGNED'));