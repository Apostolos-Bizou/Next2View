const fs = require('fs');

// 1. Simplify alertKey in NotificationsView — just projectId:level
const notifFile = 'frontend/src/views/NotificationsView.vue';
let notif = fs.readFileSync(notifFile, 'utf8');
notif = notif.replace(
  'return n.projectId + ":" + n.level + ":" + n.title',
  'return n.projectId + ":" + n.level'
);
fs.writeFileSync(notifFile, notif, 'utf8');
console.log('DONE: NotificationsView alertKey = projectId:level');

// 2. Simplify DashboardLayout badge count
const layoutFile = 'frontend/src/views/DashboardLayout.vue';
let layout = fs.readFileSync(layoutFile, 'utf8');

const startMarker = 'const activeBadgeCount = computed';
const endMarker = 'const initials = computed';
const startIdx = layout.indexOf(startMarker);
const endIdx = layout.indexOf(endMarker);

if (startIdx > 0 && endIdx > startIdx) {
  const newLogic = `const activeBadgeCount = computed(() => {
  const keys = dismissedKeys.value
  let count = 0
  store.projects.forEach(p => {
    const dl = p.deadline ? Math.round((new Date(p.deadline) - new Date()) / 86400000) : null
    if (dl !== null && dl < 0 && p.status !== 'completed') {
      if (!keys.has(p.id + ':critical')) count++
    }
    else if (dl !== null && dl >= 0 && dl < 7 && p.status !== 'completed') {
      if (!keys.has(p.id + ':critical')) count++
    }
    else if (dl !== null && dl >= 7 && dl < 14 && p.status !== 'completed') {
      if (!keys.has(p.id + ':warning')) count++
    }
    if (p.status === 'at_risk') {
      if (!keys.has(p.id + ':critical')) count++
    }
    if (p.status === 'stale' || (p.updatedAgo && p.updatedAgo > 10080)) {
      if (!keys.has(p.id + ':warning')) count++
    }
    if (p.deadline && p.completion < 30 && dl !== null && dl < 30 && p.status !== 'completed') {
      if (!keys.has(p.id + ':warning')) count++
    }
    const blocked = p.modules ? p.modules.flatMap(m => (m.tasks || []).filter(t => t.isBlocked)) : []
    if (blocked.length > 0) {
      if (!keys.has(p.id + ':warning')) count++
    }
  })
  return count
})

`;
  layout = layout.slice(0, startIdx) + newLogic + layout.slice(endIdx);
  fs.writeFileSync(layoutFile, layout, 'utf8');
  console.log('DONE: DashboardLayout simplified to projectId:level');
} else {
  console.error('FAIL: markers not found');
}