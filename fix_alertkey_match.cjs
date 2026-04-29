const fs = require('fs');

// 1. Fix NotificationsView — simplify alertKey (no icon)
const notifFile = 'frontend/src/views/NotificationsView.vue';
let notif = fs.readFileSync(notifFile, 'utf8');
notif = notif.replace(
  'return n.projectId + ":" + n.icon + ":" + n.level',
  'return n.projectId + ":" + n.level + ":" + n.title'
);
fs.writeFileSync(notifFile, notif, 'utf8');
console.log('DONE: NotificationsView alertKey simplified');

// 2. Fix DashboardLayout — match the same key format
const layoutFile = 'frontend/src/views/DashboardLayout.vue';
let layout = fs.readFileSync(layoutFile, 'utf8');

// Replace the entire activeBadgeCount logic with simpler version
const oldLogic = `function alertKeyFromProject(p, icon, level) {
  return p.id + ':' + icon + ':' + level
}

const activeBadgeCount = computed(() => {
  let count = 0
  store.projects.forEach(p => {
    const dl = p.deadline ? Math.round((new Date(p.deadline) - new Date()) / 86400000) : null
    if (dl !== null && dl < 0 && p.status !== 'completed') {
      if (!dismissedKeys.value.has(alertKeyFromProject(p, '\\uD83D\\uDD34', 'critical'))) count++
    } else if (dl !== null && dl >= 0 && dl < 7 && p.status !== 'completed') {
      if (!dismissedKeys.value.has(alertKeyFromProject(p, '\\u26A0\\uFE0F', 'critical'))) count++
    } else if (dl !== null && dl >= 7 && dl < 14 && p.status !== 'completed') {
      if (!dismissedKeys.value.has(alertKeyFromProject(p, '\\uD83D\\uDFE1', 'warning'))) count++
    }
    if (p.status === 'at_risk') {
      if (!dismissedKeys.value.has(alertKeyFromProject(p, '\\uD83D\\uDEA8', 'critical'))) count++
    }
    if (p.status === 'stale' || p.updatedAgo > 10080) {
      if (!dismissedKeys.value.has(alertKeyFromProject(p, '\\uD83D\\uDFE0', 'warning'))) count++
    }
  })
  return count
})`;

// Since the old logic has escaped unicode that might not match exactly, let's find and replace by markers
// Find from "function alertKeyFromProject" to the closing of activeBadgeCount
const startMarker = 'function alertKeyFromProject';
const endMarker = 'const initials = computed';

const startIdx = layout.indexOf(startMarker);
const endIdx = layout.indexOf(endMarker);

if (startIdx > 0 && endIdx > startIdx) {
  const newLogic = `const activeBadgeCount = computed(() => {
  const keys = dismissedKeys.value
  let count = 0
  store.projects.forEach(p => {
    const dl = p.deadline ? Math.round((new Date(p.deadline) - new Date()) / 86400000) : null
    // Overdue
    if (dl !== null && dl < 0 && p.status !== 'completed') {
      if (!keys.has(p.id + ':critical:' + 'Overdue: ' + p.title) && !keys.has(p.id + ':critical:Overdue: ' + p.title)) count++
    }
    // Urgent (< 7 days)
    else if (dl !== null && dl >= 0 && dl < 7 && p.status !== 'completed') {
      if (!keys.has(p.id + ':critical:' + 'Urgent Deadline: ' + p.title)) count++
    }
    // Approaching (< 14 days)
    else if (dl !== null && dl >= 7 && dl < 14 && p.status !== 'completed') {
      if (!keys.has(p.id + ':warning:' + 'Deadline Approaching: ' + p.title)) count++
    }
    // At Risk
    if (p.status === 'at_risk') {
      if (!keys.has(p.id + ':critical:At Risk: ' + p.title)) count++
    }
    // Stale
    if (p.status === 'stale' || (p.updatedAgo && p.updatedAgo > 10080)) {
      if (!keys.has(p.id + ':warning:Stale Project: ' + p.title)) count++
    }
    // Low progress
    if (p.deadline && p.completion < 30 && dl !== null && dl < 30 && p.status !== 'completed') {
      if (!keys.has(p.id + ':warning:Low Progress: ' + p.title)) count++
    }
    // Blocked
    const blocked = p.modules ? p.modules.flatMap(m => (m.tasks || []).filter(t => t.isBlocked)) : []
    if (blocked.length > 0) {
      if (!keys.has(p.id + ':warning:Blocked Tasks: ' + p.title)) count++
    }
  })
  return count
})

`;
  layout = layout.slice(0, startIdx) + newLogic + layout.slice(endIdx);
  fs.writeFileSync(layoutFile, layout, 'utf8');
  console.log('DONE: DashboardLayout activeBadgeCount matches alertKey format');
} else {
  console.error('FAIL: could not find markers in DashboardLayout');
}