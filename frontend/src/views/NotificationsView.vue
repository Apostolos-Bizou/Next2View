<template>
  <div class="content">
        <!-- TABS: Alerts vs Activity -->
    <div class="notif-tabs-bar">
      <button class="notif-tab" :class="{active: activeTab==='activity'}" @click="activeTab='activity'; loadActivity()">
        📋 {{ t('notif.tabActivity') || 'Activity Log' }}
        <span v-if="activityLog.length" class="tab-badge activity">{{ activityLog.length }}</span>
      </button>
      <button class="notif-tab" :class="{active: activeTab==='alerts'}" @click="activeTab='alerts'">
        ⚠ {{ t('notif.tabAlerts') || 'Alerts' }}
        <span v-if="allNotifs.length" class="tab-badge alerts">{{ allNotifs.length }}</span>
      </button>
    </div>

    <!-- ALERTS TAB -->
    <div v-if="activeTab==='alerts'" class="notif-header-bar">
      <div class="notif-summary">
        <span v-if="critical.length" class="notif-count critical">{{ critical.length }} {{ t('notif.critical') }}</span>
        <span v-if="warnings.length" class="notif-count warning">{{ warnings.length }} {{ t('notif.warnings') }}</span>
        <span v-if="infos.length" class="notif-count info">{{ infos.length }} {{ t('notif.infos') }}</span>
        <span v-if="!allNotifs.length" class="notif-count ok">{{ t('notif.allOk') }}</span>
      </div>
      <div style="display:flex;gap:8px;">
        <button class="filter-btn" :class="{active: filter===''}" @click="filter=''">{{ t('notif.all') }}</button>
        <button class="filter-btn" :class="{active: filter==='critical'}" @click="filter='critical'">🔴 {{ t('notif.critical') }}</button>
        <button class="filter-btn" :class="{active: filter==='warning'}" @click="filter='warning'">🟡 {{ t('notif.warnings') }}</button>
        <button class="filter-btn" :class="{active: filter==='info'}" @click="filter='info'">{{ t('notif.filterInfo') }}</button>
        <button v-if="filtered.length" class="filter-btn dismiss-all-btn" @click="dismissAllAlerts">✓ {{ t('notif.markAllRead', 'Mark all read') }}</button>
      </div>
    </div>

    <div v-if="activeTab==='alerts' && !filtered.length" class="notif-empty">
      <div class="notif-empty-ico">✓</div>
      <div class="notif-empty-txt">{{ t('notif.emptyText') }}</div>
      <div class="notif-empty-sub">{{ t('notif.emptySub') }}</div>
    </div>

    <div v-if="activeTab==='alerts' && filtered.length" class="notif-list">
      <div v-for="n in filtered" :key="n.id"
        :class="['notif-card', n.level]"
        @click="n.projectId && router.push(`/projects/${n.projectId}`)">
        <div class="notif-icon">{{ n.icon }}</div>
        <div class="notif-body">
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-desc">{{ n.description }}</div>
          <div class="notif-meta">
            <span v-if="n.company" class="notif-co">{{ n.company }}</span>
            <span v-if="n.category" :class="`notif-cat ${n.category}`">{{ catLabel(n.category) }}</span>
            <span v-if="n.daysLeft !== undefined" :class="`notif-days ${n.daysLeft < 0 ? 'overdue' : n.daysLeft < 7 ? 'urgent' : ''}`">
              {{ n.daysLeft < 0 ? t('notif.daysOverdue', {d: Math.abs(n.daysLeft)}) : t('notif.daysLeft', {d: n.daysLeft}) }}
            </span>
          </div>
        </div>
        <div class="notif-arrow">›</div>
        <button class="dismiss-btn" @click.stop="dismissAlert(n)" title="Dismiss">✕</button>
      </div>
    </div>

    <!-- ACTIVITY TAB -->
    <div v-if="activeTab==='activity'" class="activity-section">
      <div class="activity-controls">
        <button class="filter-btn" @click="loadActivity">↻ {{ t('notif.refresh') || 'Refresh' }}</button>
        <button v-if="activityLog.length" class="filter-btn dismiss-all-btn" @click="dismissAllActivities">🗑️ {{ t('notif.clearAll') || 'Clear All' }}</button>
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
            <div class="notif-title">{{ a.actorName }}</div>
            <div class="notif-desc">{{ a.description || (actionLabel(a.actionType) + ' ' + entityLabel(a.entityType) + ' ' + (a.entityName || '')) }}</div>
            <div class="notif-meta">
              <span v-if="a.category" :class="'notif-cat ' + a.category">{{ a.category }}</span>
              <span class="notif-days">{{ timeAgo(a.createdAt) }}</span>
              <button class="dismiss-btn" @click.stop="dismissActivity(a.id)" title="Dismiss">✕</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'
import api from '@/services/api'

const { t } = useI18n()
const store = useProjectStore()
const router = useRouter()
const filter = ref('')

// Dismissed alerts (localStorage)
const dismissedAlerts = ref(new Set(JSON.parse(localStorage.getItem("n2v_dismissed_alerts") || "[]")))

function alertKey(n) {
  return n.projectId + ":" + n.level
}

function dismissAlert(n) {
  dismissedAlerts.value.add(alertKey(n))
  localStorage.setItem("n2v_dismissed_alerts", JSON.stringify([...dismissedAlerts.value]))
}

function dismissAllAlerts() {
  filtered.value.forEach(n => dismissedAlerts.value.add(alertKey(n)))
  localStorage.setItem("n2v_dismissed_alerts", JSON.stringify([...dismissedAlerts.value]))
}

const activeTab = ref('activity')
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

onMounted(() => {
  loadActivity()
  startActivityPolling()
})
onUnmounted(() => {
  if (activityTimer) clearInterval(activityTimer)
  window.removeEventListener('sse-activity-received', onSseActivity)
})

function actionIcon(type) {
  return { CREATED: '➕', UPDATED: '✏️', DELETED: '🗑️', COMPLETED: '✅', UPLOADED: '📎', COMMENTED: '💬', STATUS_CHANGED: '🔄', TASK_ADDED: '➕', TASK_REMOVED: '🗑️', TASK_COMPLETED: '✅', TASK_REOPENED: '🔄', TASK_PROGRESS: '📊', TASK_REASSIGNED: '👤', TASK_BLOCKED: '🚫', TASK_UNBLOCKED: '✅' }[type] || '📌'
}

function actionLabel(type) {
  return { CREATED: 'created', UPDATED: 'updated', DELETED: 'deleted', COMPLETED: 'completed', UPLOADED: 'uploaded', COMMENTED: 'commented on', STATUS_CHANGED: 'changed status of', TASK_ADDED: 'added task in', TASK_REMOVED: 'removed task from', TASK_COMPLETED: 'completed task in', TASK_REOPENED: 'reopened task in', TASK_PROGRESS: 'changed progress in', TASK_REASSIGNED: 'reassigned task in', TASK_BLOCKED: 'blocked task in', TASK_UNBLOCKED: 'unblocked task in' }[type] || type
}

function entityLabel(type) {
  return { PROJECT: 'project', TASK: 'task', COMPANY: 'company', USER: 'user', FILE: 'file', MODULE: 'module', COMMENT: 'comment' }[type] || type
}

async function dismissActivity(id) {
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

function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return mins + 'm ago'
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return hrs + 'h ago'
  const days = Math.floor(hrs / 24)
  return days + 'd ago'
}

function daysLeft(deadline) {
  return Math.round((new Date(deadline) - new Date()) / 86400000)
}

const allNotifs = computed(() => {
  const notifs = []
  let id = 0

  store.projects.forEach(p => {
    // 1. OVERDUE deadline
    if (p.deadline) {
      const dl = daysLeft(p.deadline)
      if (dl < 0 && p.status !== 'completed') {
        notifs.push({
          id: id++, level: 'critical', icon: '🔴',
          title: t('notif.overdueTitle', {title: p.title}),
          description: t('notif.overdueDesc', {days: Math.abs(dl), pct: p.completion}),
          company: p.companyName, category: p.category,
          daysLeft: dl, projectId: p.id
        })
      }
      // 2. Deadline < 7 days
      else if (dl >= 0 && dl < 7 && p.status !== 'completed') {
        notifs.push({
          id: id++, level: 'critical', icon: '⚠️',
          title: t('notif.urgentTitle', {title: p.title}),
          description: t('notif.urgentDesc', {days: dl, pct: p.completion}),
          company: p.companyName, category: p.category,
          daysLeft: dl, projectId: p.id
        })
      }
      // 3. Deadline < 14 days
      else if (dl >= 7 && dl < 14 && p.status !== 'completed') {
        notifs.push({
          id: id++, level: 'warning', icon: '🟡',
          title: t('notif.approachTitle', {title: p.title}),
          description: t('notif.approachDesc', {days: dl, pct: p.completion}),
          company: p.companyName, category: p.category,
          daysLeft: dl, projectId: p.id
        })
      }
    }

    // 4. At Risk status
    if (p.status === 'at_risk') {
      notifs.push({
        id: id++, level: 'critical', icon: '🚨',
        title: `At Risk: ${p.title}`,
        description: t('notif.atRiskDesc'),
        company: p.companyName, category: p.category, projectId: p.id
      })
    }

    // 5. Stale (delayed updates)
    if (p.status === 'stale' || p.updatedAgo > 10080) { // 7 days in minutes
      notifs.push({
        id: id++, level: 'warning', icon: '😴',
        title: t('notif.staleTitle', {title: p.title}),
        description: t('notif.staleDesc', {days: Math.round((p.updatedAgo||0)/1440)}),
        company: p.companyName, category: p.category, projectId: p.id
      })
    }

    // 6. Delayed status
    if (p.status === 'delayed') {
      notifs.push({
        id: id++, level: 'warning', icon: '⏰',
        title: t('notif.delayedTitle', {title: p.title}),
        description: t('notif.delayedDesc', {pct: p.completion}),
        company: p.companyName, category: p.category, projectId: p.id
      })
    }

    // 7. Low completion + approaching deadline
    if (p.deadline && p.completion < 30 && daysLeft(p.deadline) < 30 && p.status !== 'completed') {
      notifs.push({
        id: id++, level: 'warning', icon: '📉',
        title: t('notif.lowProgressTitle', {title: p.title}),
        description: t('notif.lowProgressDesc', {pct: p.completion, days: daysLeft(p.deadline)}),
        company: p.companyName, category: p.category, projectId: p.id
      })
    }

    // 8. Blocked tasks
    const blockedTasks = p.modules?.flatMap(m => m.tasks?.filter(t => t.isBlocked) || []) || []
    if (blockedTasks.length > 0) {
      notifs.push({
        id: id++, level: 'warning', icon: '🚫',
        title: `Blocked Tasks: ${p.title}`,
        description: t('notif.blockedDesc', {count: blockedTasks.length}),
        company: p.companyName, category: p.category, projectId: p.id
      })
    }

    // 9. Completed — good news
    if (p.status === 'completed' || p.completion === 100) {
      notifs.push({
        id: id++, level: 'info', icon: '✅',
        title: t('notif.completedTitle', {title: p.title}),
        description: t('notif.completedDesc'),
        company: p.companyName, category: p.category, projectId: p.id
      })
    }
  })

  // Sort: critical first, then warning, then info
  const order = { critical: 0, warning: 1, info: 2 }
  // Filter out dismissed alerts
  const activeNotifs = notifs.filter(n => !dismissedAlerts.value.has(alertKey(n)))

  return activeNotifs.sort((a, b) => order[a.level] - order[b.level])
})

const critical = computed(() => allNotifs.value.filter(n => n.level === 'critical'))
const warnings = computed(() => allNotifs.value.filter(n => n.level === 'warning'))
const infos    = computed(() => allNotifs.value.filter(n => n.level === 'info'))

const filtered = computed(() => {
  if (!filter.value) return allNotifs.value
  return allNotifs.value.filter(n => n.level === filter.value)
})

// Sync badge count to DashboardLayout
watch(activityLog, (val) => {
  window.__n2vAlertCount = val.length
  window.dispatchEvent(new Event('alert-count-changed'))
}, { immediate: true })

const catLabel = (c) => t('notif.cats.' + c, c)
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.notif-header-bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.notif-summary { display: flex; gap: 8px; }
.notif-count { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; padding: 5px 12px; border-radius: 20px; }
.notif-count.critical { background: var(--red-dim); color: var(--red); }
.notif-count.warning  { background: var(--yellow-dim); color: var(--yellow); }
.notif-count.info     { background: var(--accent-dim); color: var(--accent); }
.notif-count.ok       { background: var(--green-dim); color: var(--green); }
.filter-btn { font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; border: 1px solid var(--border-bright); border-radius: 6px; background: var(--surface2); color: var(--text-mid); cursor: pointer; transition: all 0.15s; }
.filter-btn:hover { background: var(--surface3); }
.filter-btn.active { background: var(--accent-dim); border-color: var(--accent); color: var(--accent); }
.notif-empty { text-align: center; padding: 80px 20px; }
.notif-empty-ico { font-size: 48px; margin-bottom: 16px; color: var(--green); }
.notif-empty-txt { font-size: 18px; font-weight: 800; color: var(--text); margin-bottom: 8px; }
.notif-empty-sub { font-size: 13px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; }
.notif-list { display: flex; flex-direction: column; gap: 8px; }
.notif-card { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; display: flex; align-items: flex-start; gap: 14px; cursor: pointer; transition: all 0.15s; position: relative; overflow: hidden; }
.notif-card::before { content: ""; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; }
.notif-card.critical::before { background: var(--red); }
.notif-card.warning::before  { background: var(--yellow); }
.notif-card.info::before     { background: var(--green); }
.notif-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); transform: translateY(-1px); }
.notif-card.critical { background: linear-gradient(to right, rgba(220,38,38,0.03), var(--surface)); }
.notif-card.warning  { background: linear-gradient(to right, rgba(245,158,11,0.03), var(--surface)); }
.notif-icon { font-size: 22px; flex-shrink: 0; margin-top: 2px; }
.notif-body { flex: 1; }
.notif-title { font-size: 14px; font-weight: 800; color: var(--text); margin-bottom: 5px; }
.notif-desc { font-size: 12px; color: var(--text-mid); line-height: 1.5; margin-bottom: 10px; }
.notif-meta { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.notif-co { font-size: 10px; color: var(--text-dim); background: var(--surface2); padding: 2px 8px; border-radius: 5px; font-family: "Nunito Sans", sans-serif; font-weight: 600; }
.notif-cat { font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 5px; }
.notif-cat.finance   { background: var(--finance-dim);  color: var(--finance); }
.notif-cat.legal     { background: var(--legal-dim);    color: var(--legal); }
.notif-cat.dev       { background: var(--dev-dim);      color: var(--dev); }
.notif-cat.marketing { background: var(--marketing-dim);color: var(--marketing); }
.notif-days { font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 5px; background: var(--surface2); color: var(--text-dim); font-family: "Nunito Sans", sans-serif; }
.notif-days.urgent  { background: var(--red-dim); color: var(--red); }
.notif-days.overdue { background: var(--red-dim); color: var(--red); }
.notif-arrow { color: var(--text-dim); font-size: 20px; flex-shrink: 0; margin-top: 4px; }
.notif-card { position: relative; }
.notif-card .dismiss-btn { position: absolute; top: 12px; right: 12px; background: var(--surface2); border: 1px solid var(--border); border-radius: 6px; color: var(--text-dim); cursor: pointer; font-size: 12px; padding: 4px 8px; transition: all 0.15s; }

.notif-card .dismiss-btn:hover { background: var(--red-dim); border-color: var(--red); color: var(--red); }

@media (max-width: 768px) {
  .notif-content { padding: 14px 12px; }
  .notif-tabs { overflow-x: auto; -webkit-overflow-scrolling: touch; padding-bottom: 4px; }
  .notif-tab { white-space: nowrap; font-size: 11px; padding: 6px 12px; }
  .notif-card { padding: 12px 14px; }
}
.notif-tabs-bar { display: flex; gap: 0; margin-bottom: 16px; border-bottom: 2px solid var(--border); }
.notif-tab { font-family: "Nunito", sans-serif; font-size: 13px; font-weight: 700; padding: 10px 20px; border: none; background: none; color: var(--text-dim); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.15s; display: flex; align-items: center; gap: 8px; }
.notif-tab:hover { color: var(--text); }
.notif-tab.active { color: var(--accent); border-bottom-color: var(--accent); }
.tab-badge { font-size: 10px; font-weight: 800; padding: 1px 7px; border-radius: 10px; font-family: "Nunito Sans", sans-serif; }
.tab-badge.alerts { background: var(--red-dim); color: var(--red); }
.tab-badge.activity { background: var(--accent-dim); color: var(--accent); }
.activity-section { margin-top: 8px; }
.activity-controls { display: flex; gap: 8px; margin-bottom: 16px; }
.activity-card::before { background: var(--accent) !important; }
.activity-card { position: relative; }
.dismiss-btn { position: absolute; top: 10px; right: 10px; background: none; border: 1px solid var(--border); border-radius: 5px; color: var(--text-dim); cursor: pointer; font-size: 11px; padding: 2px 7px; transition: all 0.15s; z-index: 1; }
.dismiss-btn:hover { background: var(--red-dim); border-color: var(--red); color: var(--red); }
.dismiss-all-btn { background: var(--red-dim) !important; border-color: rgba(220,38,38,0.3) !important; color: var(--red) !important; }
</style>