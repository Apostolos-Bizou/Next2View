const fs = require('fs');
const f = 'frontend/src/views/NotificationsView.vue';
let c = fs.readFileSync(f, 'utf8');

if (c.includes('dismissActivity')) {
  console.log('SKIP: already patched');
  process.exit(0);
}

// 1. Add dismiss button to each activity card
c = c.replace(
  `<span class="notif-days">{{ timeAgo(a.createdAt) }}</span>`,
  `<span class="notif-days">{{ timeAgo(a.createdAt) }}</span>
              <button class="dismiss-btn" @click.stop="dismissActivity(a.id)" title="Dismiss">✕</button>`
);

// 2. Add "Clear All" and "Dismiss All" to activity controls
c = c.replace(
  `<button v-if="activityLog.length" class="filter-btn" @click="activityLog=[]">{{ t('notif.clearAll') || 'Clear All' }}</button>`,
  `<button v-if="activityLog.length" class="filter-btn dismiss-all-btn" @click="dismissAllActivities">🗑️ {{ t('notif.clearAll') || 'Clear All' }}</button>`
);

// 3. Add dismiss functions in script
c = c.replace(
  'function timeAgo(iso)',
  `async function dismissActivity(id) {
  try {
    await api.post('/activity-log/dismiss', { ids: [id] })
    activityLog.value = activityLog.value.filter(a => a.id !== id)
  } catch (e) {
    console.error('Failed to dismiss:', e)
  }
}

async function dismissAllActivities() {
  try {
    await api.post('/activity-log/dismiss-all')
    activityLog.value = []
  } catch (e) {
    console.error('Failed to dismiss all:', e)
  }
}

function timeAgo(iso)`
);

// 4. Add CSS for dismiss button
c = c.replace(
  '.activity-card::before { background: var(--accent) !important; }',
  `.activity-card::before { background: var(--accent) !important; }
.activity-card { position: relative; }
.dismiss-btn { position: absolute; top: 10px; right: 10px; background: none; border: 1px solid var(--border); border-radius: 5px; color: var(--text-dim); cursor: pointer; font-size: 11px; padding: 2px 7px; transition: all 0.15s; z-index: 1; }
.dismiss-btn:hover { background: var(--red-dim); border-color: var(--red); color: var(--red); }
.dismiss-all-btn { background: var(--red-dim) !important; border-color: rgba(220,38,38,0.3) !important; color: var(--red) !important; }`
);

fs.writeFileSync(f, c, 'utf8');
console.log('DONE: NotificationsView.vue patched with dismiss buttons');