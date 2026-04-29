const fs = require('fs');
const f = 'frontend/src/views/NotificationsView.vue';
let c = fs.readFileSync(f, 'utf8');

if (c.includes('activityLog') || c.includes('activity-tab')) {
  console.log('SKIP: already patched');
  process.exit(0);
}

// === 1. Add tabs BEFORE the filter buttons section ===
const tabsHtml = `    <!-- TABS: Alerts vs Activity -->
    <div class="notif-tabs-bar">
      <button class="notif-tab" :class="{active: activeTab==='alerts'}" @click="activeTab='alerts'">
        \u26A0 {{ t('notif.tabAlerts') || 'Alerts' }}
        <span v-if="allNotifs.length" class="tab-badge alerts">{{ allNotifs.length }}</span>
      </button>
      <button class="notif-tab" :class="{active: activeTab==='activity'}" @click="activeTab='activity'; loadActivity()">
        \uD83D\uDCCB {{ t('notif.tabActivity') || 'Activity Log' }}
        <span v-if="activityLog.length" class="tab-badge activity">{{ activityLog.length }}</span>
      </button>
    </div>`;

c = c.replace(
  '<div class="notif-header-bar">',
  tabsHtml + '\n\n    <!-- ALERTS TAB -->\n    <div v-if="activeTab===\'alerts\'" class="notif-header-bar">'
);

// === 2. Wrap existing content in v-if="activeTab==='alerts'" — close the div before style ===
// Find the closing </div> of notif-list and add activity section after
c = c.replace(
  '</div>\n  </div>\n</template>',
  `</div>
    </div>

    <!-- ACTIVITY TAB -->
    <div v-if="activeTab==='activity'" class="activity-section">
      <div class="activity-controls">
        <button class="filter-btn" @click="loadActivity">↻ {{ t('notif.refresh') || 'Refresh' }}</button>
        <button v-if="activityLog.length" class="filter-btn" @click="activityLog=[]">{{ t('notif.clearAll') || 'Clear All' }}</button>
      </div>
      <div v-if="activityLoading" class="notif-empty">
        <div class="notif-empty-ico">⏳</div>
        <div class="notif-empty-txt">{{ t('notif.loadingActivity') || 'Loading activity...' }}</div>
      </div>
      <div v-else-if="!activityLog.length" class="notif-empty">
        <div class="notif-empty-ico">📋</div>
        <div class="notif-empty-txt">{{ t('notif.noActivity') || 'No recent activity' }}</div>
        <div class="notif-empty-sub">{{ t('notif.noActivitySub') || 'Actions will appear here as they happen.' }}</div>
      </div>
      <div v-else class="notif-list">
        <div v-for="a in activityLog" :key="a.id" class="notif-card info activity-card">
          <div class="notif-icon">{{ actionIcon(a.actionType) }}</div>
          <div class="notif-body">
            <div class="notif-title">{{ a.actorName }} {{ actionLabel(a.actionType) }} {{ entityLabel(a.entityType) }}</div>
            <div class="notif-desc">{{ a.entityName || a.description }}</div>
            <div class="notif-meta">
              <span v-if="a.category" :class="'notif-cat ' + a.category">{{ a.category }}</span>
              <span class="notif-days">{{ timeAgo(a.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>`
);

// === 3. Add imports and script logic ===
c = c.replace(
  "import { ref, computed } from 'vue'",
  "import { ref, computed, onMounted, onUnmounted } from 'vue'"
);

c = c.replace(
  "import { useProjectStore } from '@/stores/projects'",
  "import { useProjectStore } from '@/stores/projects'\nimport api from '@/services/api'"
);

// Add activity state + methods before "const filter = ref('')"
c = c.replace(
  "const filter = ref('')",
  `const filter = ref('')
const activeTab = ref('alerts')
const activityLog = ref([])
const activityLoading = ref(false)
let activityTimer = null

async function loadActivity() {
  activityLoading.value = true
  try {
    const res = await api.get('/activity-log', { params: { limit: 50 } })
    activityLog.value = res.data
  } catch (e) {
    console.error('Failed to load activity log:', e)
  } finally {
    activityLoading.value = false
  }
}

function startActivityPolling() {
  activityTimer = setInterval(() => {
    if (activeTab.value === 'activity') loadActivity()
  }, 60000)
}

onMounted(() => startActivityPolling())
onUnmounted(() => { if (activityTimer) clearInterval(activityTimer) })

function actionIcon(type) {
  return { CREATED: '➕', UPDATED: '✏️', DELETED: '🗑️', COMPLETED: '✅', UPLOADED: '📎', COMMENTED: '💬', STATUS_CHANGED: '🔄' }[type] || '📌'
}

function actionLabel(type) {
  return { CREATED: 'created', UPDATED: 'updated', DELETED: 'deleted', COMPLETED: 'completed', UPLOADED: 'uploaded', COMMENTED: 'commented on', STATUS_CHANGED: 'changed status of' }[type] || type
}

function entityLabel(type) {
  return { PROJECT: 'project', TASK: 'task', COMPANY: 'company', USER: 'user', FILE: 'file', MODULE: 'module', COMMENT: 'comment' }[type] || type
}

function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return mins + 'm ago'
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return hrs + 'h ago'
  const days = Math.floor(hrs / 24)
  return days + 'd ago'
}`
);

// === 4. Add CSS for tabs and activity ===
c = c.replace(
  '</style>',
  `.notif-tabs-bar { display: flex; gap: 0; margin-bottom: 16px; border-bottom: 2px solid var(--border); }
.notif-tab { font-family: "Nunito", sans-serif; font-size: 13px; font-weight: 700; padding: 10px 20px; border: none; background: none; color: var(--text-dim); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.15s; display: flex; align-items: center; gap: 8px; }
.notif-tab:hover { color: var(--text); }
.notif-tab.active { color: var(--accent); border-bottom-color: var(--accent); }
.tab-badge { font-size: 10px; font-weight: 800; padding: 1px 7px; border-radius: 10px; font-family: "Nunito Sans", sans-serif; }
.tab-badge.alerts { background: var(--red-dim); color: var(--red); }
.tab-badge.activity { background: var(--accent-dim); color: var(--accent); }
.activity-section { margin-top: 8px; }
.activity-controls { display: flex; gap: 8px; margin-bottom: 16px; }
.activity-card::before { background: var(--accent) !important; }
</style>`
);

fs.writeFileSync(f, c, 'utf8');
console.log('DONE: NotificationsView.vue patched with Activity tab');