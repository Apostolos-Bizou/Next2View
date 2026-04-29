const fs = require('fs');
const filePath = 'C:/Users/akage/Next2View/frontend/src/views/NotificationsView.vue';
let code = fs.readFileSync(filePath, 'utf8');

// FIX 1: Update actionIcon to include TASK_* types
code = code.replace(
  "return { CREATED: '➕', UPDATED: '✏️', DELETED: '🗑️', COMPLETED: '✅', UPLOADED: '📎', COMMENTED: '💬', STATUS_CHANGED: '🔄' }[type] || '📌'",
  "return { CREATED: '➕', UPDATED: '✏️', DELETED: '🗑️', COMPLETED: '✅', UPLOADED: '📎', COMMENTED: '💬', STATUS_CHANGED: '🔄', TASK_ADDED: '➕', TASK_REMOVED: '🗑️', TASK_COMPLETED: '✅', TASK_REOPENED: '🔄', TASK_PROGRESS: '📊', TASK_REASSIGNED: '👤', TASK_BLOCKED: '🚫', TASK_UNBLOCKED: '✅' }[type] || '📌'"
);

// FIX 2: Update actionLabel to include TASK_* types
code = code.replace(
  "return { CREATED: 'created', UPDATED: 'updated', DELETED: 'deleted', COMPLETED: 'completed', UPLOADED: 'uploaded', COMMENTED: 'commented on', STATUS_CHANGED: 'changed status of' }[type] || type",
  "return { CREATED: 'created', UPDATED: 'updated', DELETED: 'deleted', COMPLETED: 'completed', UPLOADED: 'uploaded', COMMENTED: 'commented on', STATUS_CHANGED: 'changed status of', TASK_ADDED: 'added task in', TASK_REMOVED: 'removed task from', TASK_COMPLETED: 'completed task in', TASK_REOPENED: 'reopened task in', TASK_PROGRESS: 'changed progress in', TASK_REASSIGNED: 'reassigned task in', TASK_BLOCKED: 'blocked task in', TASK_UNBLOCKED: 'unblocked task in' }[type] || type"
);

// FIX 3: Change activity card to show description instead of constructed text
// Old: title = actorName + actionLabel + entityLabel, desc = entityName || description
// New: if description exists, show it directly; otherwise fallback
code = code.replace(
  "<div class=\"notif-title\">{{ a.actorName }} {{ actionLabel(a.actionType) }} {{ entityLabel(a.entityType) }}</div>",
  "<div class=\"notif-title\">{{ a.actorName }}</div>"
);

code = code.replace(
  "<div class=\"notif-desc\">{{ a.entityName || a.description }}</div>",
  "<div class=\"notif-desc\">{{ a.description || (actionLabel(a.actionType) + ' ' + entityLabel(a.entityType) + ' ' + (a.entityName || '')) }}</div>"
);

fs.writeFileSync(filePath, code, 'utf8');

const verify = fs.readFileSync(filePath, 'utf8');
console.log('TASK_ADDED icon:', verify.includes("TASK_ADDED: '➕'"));
console.log('TASK_PROGRESS label:', verify.includes("TASK_PROGRESS: 'changed progress in'"));
console.log('description first:', verify.includes("a.description || (actionLabel"));
console.log('actorName only title:', verify.includes('notif-title">{{ a.actorName }}</div>'));