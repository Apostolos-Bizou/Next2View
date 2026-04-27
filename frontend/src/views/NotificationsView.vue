<template>
  <div class="content">
    <div class="notif-header-bar">
      <div class="notif-summary">
        <span v-if="critical.length" class="notif-count critical">{{ critical.length }} {{ t('notif.critical') }}</span>
        <span v-if="warnings.length" class="notif-count warning">{{ warnings.length }} {{ t('notif.warnings') }}</span>
        <span v-if="infos.length" class="notif-count info">{{ infos.length }} {{ t('notif.infos') }}</span>
        <span v-if="!allNotifs.length" class="notif-count ok">✓ Όλα καλά</span>
      </div>
      <div style="display:flex;gap:8px;">
        <button class="filter-btn" :class="{active: filter===''}" @click="filter=''">{{ t('notif.all') }}</button>
        <button class="filter-btn" :class="{active: filter==='critical'}" @click="filter='critical'">🔴 {{ t('notif.critical') }}</button>
        <button class="filter-btn" :class="{active: filter==='warning'}" @click="filter='warning'">🟡 {{ t('notif.warnings') }}</button>
        <button class="filter-btn" :class="{active: filter==='info'}" @click="filter='info'">🔵 Ενημερώσεις</button>
      </div>
    </div>

    <div v-if="!filtered.length" class="notif-empty">
      <div class="notif-empty-ico">✓</div>
      <div class="notif-empty-txt">{{ t('notif.emptyText') }}</div>
      <div class="notif-empty-sub">{{ t('notif.emptySub') }}</div>
    </div>

    <div class="notif-list">
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'

const { t } = useI18n()
const store = useProjectStore()
const router = useRouter()
const filter = ref('')

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
  return notifs.sort((a, b) => order[a.level] - order[b.level])
})

const critical = computed(() => allNotifs.value.filter(n => n.level === 'critical'))
const warnings = computed(() => allNotifs.value.filter(n => n.level === 'warning'))
const infos    = computed(() => allNotifs.value.filter(n => n.level === 'info'))

const filtered = computed(() => {
  if (!filter.value) return allNotifs.value
  return allNotifs.value.filter(n => n.level === filter.value)
})

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

@media (max-width: 768px) {
  .notif-content { padding: 14px 12px; }
  .notif-tabs { overflow-x: auto; -webkit-overflow-scrolling: touch; padding-bottom: 4px; }
  .notif-tab { white-space: nowrap; font-size: 11px; padding: 6px 12px; }
  .notif-card { padding: 12px 14px; }
}
</style>