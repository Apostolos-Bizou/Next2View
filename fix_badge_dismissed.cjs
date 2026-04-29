const fs = require('fs');

// === 1. Patch DashboardLayout.vue — badge reads dismissed alerts ===
const layoutFile = 'frontend/src/views/DashboardLayout.vue';
let layout = fs.readFileSync(layoutFile, 'utf8');

if (layout.includes('activeBadgeCount')) {
  console.log('SKIP layout: already patched');
} else {
  // Add reactive dismissed set + badge count computed
  layout = layout.replace(
    "const initials = computed(() => {",
    `// Badge: count non-dismissed alerts
const dismissedKeys = ref(new Set(JSON.parse(localStorage.getItem('n2v_dismissed_alerts') || '[]')))

// Listen for dismiss events from NotificationsView
window.addEventListener('alerts-dismissed', () => {
  dismissedKeys.value = new Set(JSON.parse(localStorage.getItem('n2v_dismissed_alerts') || '[]'))
})

function alertKeyFromProject(p, icon, level) {
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
})

const initials = computed(() => {`
  );

  // Replace sidebar nav badge: store.atRisk.length -> activeBadgeCount
  layout = layout.replace(
    'v-if="store.atRisk.length" class="nav-count" style="background:var(--red);color:#fff;">{{ store.atRisk.length }}',
    'v-if="activeBadgeCount > 0" class="nav-count" style="background:var(--red);color:#fff;">{{ activeBadgeCount }}'
  );

  // Replace notification row badge
  layout = layout.replace(
    'v-if="store.atRisk.length" class="notif-badge">{{ store.atRisk.length }}',
    'v-if="activeBadgeCount > 0" class="notif-badge">{{ activeBadgeCount }}'
  );

  // Replace mobile badge
  layout = layout.replace(
    'v-if="store.unreadCount > 0" class="mobile-notif-badge">{{ store.unreadCount }}',
    'v-if="activeBadgeCount > 0" class="mobile-notif-badge">{{ activeBadgeCount }}'
  );

  fs.writeFileSync(layoutFile, layout, 'utf8');
  console.log('DONE: DashboardLayout badge uses activeBadgeCount');
}

// === 2. Patch NotificationsView.vue — dispatch event on dismiss ===
const notifFile = 'frontend/src/views/NotificationsView.vue';
let notif = fs.readFileSync(notifFile, 'utf8');

if (notif.includes('alerts-dismissed')) {
  console.log('SKIP notif: already dispatches event');
} else {
  // After each localStorage.setItem for dismissed alerts, dispatch event
  notif = notif.replace(
    /localStorage\.setItem\("n2v_dismissed_alerts"/g,
    "localStorage.setItem(\"n2v_dismissed_alerts\""
  );

  // Add dispatch after dismissAlert function
  notif = notif.replace(
    "function dismissAlert(n) {\n  dismissedAlerts.value.add(alertKey(n))\n  localStorage.setItem(\"n2v_dismissed_alerts\", JSON.stringify([...dismissedAlerts.value]))",
    "function dismissAlert(n) {\n  dismissedAlerts.value.add(alertKey(n))\n  localStorage.setItem(\"n2v_dismissed_alerts\", JSON.stringify([...dismissedAlerts.value]))\n  window.dispatchEvent(new Event('alerts-dismissed'))"
  );

  // Add dispatch after dismissAllAlerts function
  notif = notif.replace(
    "function dismissAllAlerts() {\n  filtered.value.forEach(n => dismissedAlerts.value.add(alertKey(n)))\n  localStorage.setItem(\"n2v_dismissed_alerts\", JSON.stringify([...dismissedAlerts.value]))",
    "function dismissAllAlerts() {\n  filtered.value.forEach(n => dismissedAlerts.value.add(alertKey(n)))\n  localStorage.setItem(\"n2v_dismissed_alerts\", JSON.stringify([...dismissedAlerts.value]))\n  window.dispatchEvent(new Event('alerts-dismissed'))"
  );

  fs.writeFileSync(notifFile, notif, 'utf8');
  console.log('DONE: NotificationsView dispatches alerts-dismissed event');
}