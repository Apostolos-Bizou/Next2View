const fs = require('fs');

// DashboardLayout: replace activeBadgeCount with simple localStorage count
const layoutFile = 'frontend/src/views/DashboardLayout.vue';
let layout = fs.readFileSync(layoutFile, 'utf8');

const startMarker = '// Badge: count non-dismissed alerts';
const endMarker = 'const initials = computed';
const startIdx = layout.indexOf(startMarker);
const endIdx = layout.indexOf(endMarker);

if (startIdx < 0) { console.error('FAIL: start marker not found'); process.exit(1); }
if (endIdx < 0) { console.error('FAIL: end marker not found'); process.exit(1); }

const newLogic = `// Badge: count = total alerts - dismissed alerts (synced via custom event)
const activeBadgeCount = ref(0)

function recalcBadgeCount() {
  const dismissed = new Set(JSON.parse(localStorage.getItem('n2v_dismissed_alerts') || '[]'))
  // Count all potential alerts, subtract dismissed
  let total = 0
  store.projects.forEach(p => {
    const dl = p.deadline ? Math.round((new Date(p.deadline) - new Date()) / 86400000) : null
    // Each condition that generates an alert in NotificationsView
    if (dl !== null && dl < 0 && p.status !== 'completed') total++ // overdue
    if (dl !== null && dl >= 0 && dl < 7 && p.status !== 'completed') total++ // urgent
    if (dl !== null && dl >= 7 && dl < 14 && p.status !== 'completed') total++ // approaching
    if (p.status === 'at_risk') total++
    if (p.status === 'stale' || (p.updatedAgo && p.updatedAgo > 10080)) total++
    if (p.status === 'delayed') total++
    if (p.deadline && p.completion < 30 && dl !== null && dl < 30 && p.status !== 'completed') total++
    const blocked = p.modules ? p.modules.flatMap(m => (m.tasks || []).filter(t => t.isBlocked)) : []
    if (blocked.length > 0) total++
    if (p.status === 'completed' || p.completion === 100) total++
  })
  activeBadgeCount.value = Math.max(0, total - dismissed.size)
}

// Recalc on load and when alerts are dismissed
window.addEventListener('alerts-dismissed', recalcBadgeCount)

// Initial calc after projects load
const stopWatch = watch(() => store.projects.length, () => { recalcBadgeCount() })

`;

layout = layout.slice(0, startIdx) + newLogic + layout.slice(endIdx);

// Add watch import if not present
if (!layout.includes('watch,') && !layout.includes('watch }')) {
  layout = layout.replace(
    "import { ref, computed, onMounted,",
    "import { ref, computed, onMounted, watch,"
  );
  // If that didn't match, try another pattern
  if (!layout.includes('watch,') && !layout.includes('watch }')) {
    layout = layout.replace(
      "import { ref, computed, onMounted",
      "import { ref, computed, onMounted, watch"
    );
  }
}

fs.writeFileSync(layoutFile, layout, 'utf8');
console.log('DONE: DashboardLayout badge = total alerts - dismissed count');