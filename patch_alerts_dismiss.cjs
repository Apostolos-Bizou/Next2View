const fs = require('fs');
const f = 'frontend/src/views/NotificationsView.vue';
let lines = fs.readFileSync(f, 'utf8').split(/\r?\n/);

if (lines.some(l => l.includes('dismissedAlerts'))) {
  console.log('SKIP: already patched');
  process.exit(0);
}

// 1. Add dismiss button to each alert card — after notif-arrow
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('class="notif-arrow"') && lines[i].includes('›')) {
    lines.splice(i + 1, 0,
      '        <button class="dismiss-btn" @click.stop="dismissAlert(n)" title="Dismiss">✕</button>'
    );
    break;
  }
}

// 2. Add "Mark all read" button next to filter buttons in alerts header
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('filterInfo') && lines[i].includes('filter-btn')) {
    lines.splice(i + 1, 0,
      '        <button v-if="filtered.length" class="filter-btn dismiss-all-btn" @click="dismissAllAlerts">✓ {{ t(\'notif.markAllRead\') || \'Mark all read\' }}</button>'
    );
    break;
  }
}

// 3. Add script logic after "const filter = ref('')"
const filterIdx = lines.findIndex(l => l.trim() === "const filter = ref('')");
if (filterIdx >= 0) {
  lines.splice(filterIdx + 1, 0,
    '',
    '// Dismissed alerts (localStorage)',
    'const dismissedAlerts = ref(new Set(JSON.parse(localStorage.getItem("n2v_dismissed_alerts") || "[]")))',
    '',
    'function alertKey(n) {',
    '  return n.projectId + ":" + n.icon + ":" + n.level',
    '}',
    '',
    'function dismissAlert(n) {',
    '  dismissedAlerts.value.add(alertKey(n))',
    '  localStorage.setItem("n2v_dismissed_alerts", JSON.stringify([...dismissedAlerts.value]))',
    '}',
    '',
    'function dismissAllAlerts() {',
    '  filtered.value.forEach(n => dismissedAlerts.value.add(alertKey(n)))',
    '  localStorage.setItem("n2v_dismissed_alerts", JSON.stringify([...dismissedAlerts.value]))',
    '}',
    ''
  );
}

// 4. Modify allNotifs — filter out dismissed
// Find "return notifs.sort" and add filter before it
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('return notifs.sort')) {
    lines.splice(i, 0,
      '  // Filter out dismissed alerts',
      '  const activeNotifs = notifs.filter(n => !dismissedAlerts.value.has(alertKey(n)))',
      ''
    );
    // Fix the sort line to use activeNotifs
    lines[i + 3] = lines[i + 3].replace('return notifs.sort', 'return activeNotifs.sort');
    break;
  }
}

// 5. Add CSS
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('.notif-arrow')) {
    lines.splice(i + 1, 0,
      '.notif-card { position: relative; }',
      '.notif-card .dismiss-btn { position: absolute; top: 10px; right: 10px; background: none; border: 1px solid var(--border); border-radius: 5px; color: var(--text-dim); cursor: pointer; font-size: 11px; padding: 2px 7px; transition: all 0.15s; opacity: 0; }',
      '.notif-card:hover .dismiss-btn { opacity: 1; }',
      '.notif-card .dismiss-btn:hover { background: var(--red-dim); border-color: var(--red); color: var(--red); }'
    );
    break;
  }
}

// 6. Add i18n key
// Already has fallback || 'Mark all read' so no i18n patch needed for now

fs.writeFileSync(f, lines.join('\n'), 'utf8');
console.log('DONE: Alert dismiss functionality added');