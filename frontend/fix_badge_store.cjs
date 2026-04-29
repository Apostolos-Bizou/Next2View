const fs = require('fs');

// === 1. Create a tiny shared reactive alertCount ===
// Instead of duplicating logic, NotificationsView will emit the count
// DashboardLayout will read it

// Patch NotificationsView: after computing allNotifs, update a global
const notifFile = 'frontend/src/views/NotificationsView.vue';
let notif = fs.readFileSync(notifFile, 'utf8');

if (notif.includes('window.__n2vAlertCount')) {
  console.log('SKIP notif: already patched');
} else {
  // After allNotifs computed, update global count
  // Find "const catLabel" and add watcher before it
  notif = notif.replace(
    'const catLabel',
    `// Sync badge count to DashboardLayout
watch(allNotifs, (val) => {
  window.__n2vAlertCount = val.length
  window.dispatchEvent(new Event('alert-count-changed'))
}, { immediate: true })

const catLabel`
  );
  
  // Add watch to imports if not present
  if (!notif.includes('watch,') && !notif.includes('watch }')) {
    notif = notif.replace(
      'import { ref, computed, onMounted, onUnmounted }',
      'import { ref, computed, onMounted, onUnmounted, watch }'
    );
  }
  
  fs.writeFileSync(notifFile, notif, 'utf8');
  console.log('DONE: NotificationsView emits alert count');
}

// === 2. Patch DashboardLayout: read the global count ===
const layoutFile = 'frontend/src/views/DashboardLayout.vue';
let layout = fs.readFileSync(layoutFile, 'utf8');

// Replace the entire badge logic block
const startMarker = '// Badge: count = total alerts - dismissed alerts';
const endMarker = 'const initials = computed';
const startIdx = layout.indexOf(startMarker);
const endIdx = layout.indexOf(endMarker);

if (startIdx < 0) { console.error('FAIL: start marker not found'); process.exit(1); }

const newLogic = `// Badge: reads count from NotificationsView via global event
const activeBadgeCount = ref(window.__n2vAlertCount || 0)
window.addEventListener('alert-count-changed', () => {
  activeBadgeCount.value = window.__n2vAlertCount || 0
})
window.addEventListener('alerts-dismissed', () => {
  setTimeout(() => { activeBadgeCount.value = window.__n2vAlertCount || 0 }, 100)
})

`;

layout = layout.slice(0, startIdx) + newLogic + layout.slice(endIdx);
fs.writeFileSync(layoutFile, layout, 'utf8');
console.log('DONE: DashboardLayout reads alert count from global');