<template>
  <div v-if="project && project.workPlanEnabled" class="wp-panel" style="margin-top:14px;">
    <div class="wp-header">
      <div class="wp-title">📋 {{ t('workPlan.title') }}</div>
      <div v-if="cutoverModules.length" class="wp-seg">
        <button :class="['wp-seg-btn', { active: activeTab === 'plan' }]" @click="activeTab = 'plan'">{{ t('workPlan.tabPlan') }}</button>
        <button :class="['wp-seg-btn', { active: activeTab === 'cutover' }]" @click="activeTab = 'cutover'">{{ t('workPlan.tabCutover') }}</button>
      </div>
    </div>
    <div v-if="activeTab === 'plan'" class="wp-scroll">
      <table class="wp-table">
        <thead>
          <tr>
            <th class="col-date">{{ t('workPlan.from') }}</th>
            <th class="col-date">{{ t('workPlan.to') }}</th>
            <th class="num col-days">{{ t('workPlan.days') }}</th>
            <th class="col-task">{{ t('workPlan.task') }}</th>
            <th>{{ t('workPlan.environment') }}</th>
            <th>{{ t('workPlan.teams') }}</th>
            <th class="col-remarks">{{ t('workPlan.remarks') }}</th>
            <th>{{ t('workPlan.status') }}</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="m in wpModules" :key="m.id">
            <tr class="wp-mrow" @click="toggle(m.id)">
              <td class="wp-dt">{{ m.minStart ? fmtDate(m.minStart) : '—' }}</td>
              <td class="wp-dt">{{ m.maxEnd ? fmtDate(m.maxEnd) : '—' }}</td>
              <td class="num wp-days">{{ m.sumDays }}</td>
              <td>
                <span class="wp-caret" :class="{ closed: !open.has(m.id) }">▼</span>
                <span class="wp-mdot" :style="`background:var(--${m.color || 'dev'});`"></span>
                <span class="wp-mname">{{ m.name || '—' }}</span>
                <span class="wp-count">{{ m.doneCount }}/{{ m.tasks.length }}</span>
              </td>
              <td></td>
              <td></td>
              <td></td>
              <td>
                <div class="wp-st">
                  <div class="wp-bar"><i :style="`width:${m.completion || 0}%;background:var(--${m.color || 'dev'});`"></i></div>
                  <span class="wp-pc" :style="`color:var(--${m.color || 'dev'});`">{{ m.completion || 0 }}%</span>
                </div>
              </td>
            </tr>
            <template v-if="open.has(m.id)">
              <tr v-for="task in m.tasks" :key="task.id" class="wp-trow wp-clickable" @click="$emit('task-click', task.id)">
                <td class="wp-dt">
                  {{ task.startDate ? fmtDate(task.startDate) : '—' }}
                  <span v-if="task.startTime" class="wp-tm">{{ fmtTime(task.startTime) }}</span>
                </td>
                <td class="wp-dt">
                  {{ task.endDate ? fmtDate(task.endDate) : '—' }}
                  <span v-if="task.endTime" class="wp-tm">{{ fmtTime(task.endTime) }}<b v-if="isNextDay(task)" class="wp-plus1" :title="t('workPlan.nextDay')">+1</b></span>
                </td>
                <td class="num wp-days">{{ task.workDays ?? '—' }}</td>
                <td class="wp-task">
                  <span v-if="task.isGate === true" class="wp-gate" :title="t('workPlan.gate')">◆</span>
                  <span :class="{ done: task.isDone }">{{ task.name || '—' }}</span>
                </td>
                <td>
                  <template v-if="envsOf(task).length">
                    <span v-for="e in envsOf(task)" :key="e" class="wp-env">{{ e }}</span>
                  </template>
                  <template v-else>—</template>
                </td>
                <td>
                  <template v-if="teamsOf(task).length">
                    <span v-for="tm in teamsOf(task)" :key="tm" class="wp-team">
                      <i :style="`background:var(--${teamColor(tm)});`"></i>{{ tm }}
                    </span>
                  </template>
                  <template v-else>—</template>
                </td>
                <td class="wp-remark" :title="remarkText(task) || null">{{ remarkShort(task) || '—' }}</td>
                <td>
                  <div class="wp-st">
                    <div class="wp-bar"><i :style="`width:${task.progress || 0}%;background:${task.isDone ? 'var(--green)' : 'var(--' + (m.color || 'dev') + ')'};`"></i></div>
                    <span class="wp-pc" :style="`color:${task.isDone ? 'var(--green)' : 'var(--' + (m.color || 'dev') + ')'};`">{{ task.progress || 0 }}%</span>
                  </div>
                </td>
              </tr>
              <tr v-if="!m.tasks.length" class="wp-trow">
                <td colspan="8" class="wp-empty">{{ t('workPlan.noTasks') }}</td>
              </tr>
            </template>
          </template>
        </tbody>
      </table>
    </div>

    <!-- CUTOVER RUNBOOK (6c) — modules whose non-gate steps all carry a start time, span <= 3 days -->
    <div v-else-if="activeTab === 'cutover'" class="wp-rb">
      <div v-for="m in cutoverModules" :key="m.id" class="wp-rb-block">
        <div class="wp-rb-head">
          <span class="wp-mdot" :style="`background:var(--${m.color || 'dev'});`"></span>
          <span class="wp-mname">{{ m.name || '—' }}</span>
          <span class="wp-rb-meta">
            {{ m.minStart ? fmtDate(m.minStart) : '—' }} → {{ m.maxEnd ? fmtDate(m.maxEnd) : '—' }}
            · {{ m.sumDays }}d
            · {{ m.doneCount }}/{{ m.tasks.length }} {{ t('workPlan.runbookSteps') }}
          </span>
        </div>
        <div class="wp-rb-timeline">
          <template v-for="row in stepRows(m)" :key="row.key">
            <div v-if="row.type === 'divider'" class="wp-rb-day">
              <span class="wp-rb-day-label">{{ row.label }}</span>
            </div>
            <div v-else class="wp-rb-step" @click="$emit('task-click', row.task.id)">
              <span :class="['wp-rb-dot', { gate: row.task.isGate === true, filled: (row.task.progress || 0) === 100 }]"></span>
              <span class="wp-rb-time">{{ stepTime(row.task) }}</span>
              <div class="wp-rb-body">
                <div :class="['wp-rb-name', { done: row.task.isDone }]">
                  <span v-if="row.task.isGate === true" class="wp-gate" :title="t('workPlan.gate')">◆</span>{{ row.task.name || '—' }}
                </div>
                <div v-if="teamsOf(row.task).length" class="wp-rb-teams">
                  <span v-for="tm in teamsOf(row.task)" :key="tm" class="wp-team">
                    <i :style="`background:var(--${teamColor(tm)});`"></i>{{ tm }}
                  </span>
                </div>
                <div v-if="remarkText(row.task)" class="wp-rb-remark" :title="remarkText(row.task)">{{ remarkShort(row.task) }}</div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DOMPurify from 'dompurify'

const props = defineProps({
  project: { type: Object, required: true },
})

defineEmits(['task-click'])

const { t } = useI18n()

// collapse state — local only, default: all modules open
const open = ref(new Set())
watch(
  () => props.project && props.project.modules,
  (mods) => {
    open.value = new Set((mods || []).map(m => m.id))
  },
  { immediate: true }
)
function toggle(id) {
  if (open.value.has(id)) open.value.delete(id)
  else open.value.add(id)
}

// warn once per task about over-long environment values — never truncate silently
const warnedEnv = new Set()

const wpModules = computed(() => {
  const mods = (props.project && props.project.modules) || []
  return mods.map(m => {
    const tasks = m.tasks || []
    tasks.forEach(task => {
      if (task.environment && task.environment.length > 40 && !warnedEnv.has(task.id)) {
        warnedEnv.add(task.id)
        console.warn(`[WorkPlanPanel] environment value exceeds 40 chars on task "${task.name}"`)
      }
    })
    const starts = tasks.map(x => x.startDate).filter(Boolean).sort()
    const ends = tasks.map(x => x.endDate).filter(Boolean).sort()
    const dayVals = tasks.map(x => x.workDays).filter(v => v !== null && v !== undefined)
    const sum = dayVals.reduce((s, v) => s + Number(v), 0)
    return {
      ...m,
      tasks,
      minStart: starts[0] || null,
      maxEnd: ends[ends.length - 1] || null,
      sumDays: dayVals.length ? parseFloat(sum.toFixed(2)) : '—',
      doneCount: tasks.filter(x => x.isDone).length,
    }
  })
})

// ── 6c: cutover runbook ─────────────────────────────────────────────────────
const activeTab = ref('plan')

// A module is a cutover window when BOTH hold:
//  (a) every non-gate task has a startTime, (b) the whole span fits in <= 3 calendar days.
// Modules without tasks (or without dates to measure a span) never qualify.
const cutoverModules = computed(() => wpModules.value.filter(m => {
  if (!m.tasks.length) return false
  const nonGate = m.tasks.filter(t => t.isGate !== true)
  if (!nonGate.every(t => !!t.startTime)) return false
  if (!m.minStart || !m.maxEnd) return false
  const spanDays = (new Date(m.maxEnd) - new Date(m.minStart)) / 86400000
  return spanDays <= 3
}))

// If the qualifying modules disappear (reload/edit), never strand the hidden tab.
watch(cutoverModules, (list) => {
  if (!list.length && activeTab.value === 'cutover') activeTab.value = 'plan'
})

// Chronological ordering for runbook steps (NOT sortOrder):
//  key = (startDate | endDate | '9999-12-31') + 'T' + (startTime | endTime | '99:99')
//  -> gates (endDate/endTime only) slot in at their deadline moment,
//  -> steps missing a date sink to the end, missing a time sink to the end of their day.
//  Ties keep DTO order (stable sort; the DTO arrives sorted by sortOrder).
function stepKey(t) {
  const d = t.startDate || t.endDate || '9999-12-31'
  const tm = (t.startTime || t.endTime) ? fmtTime(t.startTime || t.endTime) : '99:99'
  return d + 'T' + tm
}
function sortedSteps(m) {
  return [...m.tasks].sort((a, b) => {
    const ka = stepKey(a), kb = stepKey(b)
    // Equal keys must return 0 so the DTO order (sortOrder) survives as tie-break.
    return ka < kb ? -1 : ka > kb ? 1 : 0
  })
}
// start time on the left; gates only have an end time — show that instead
function stepTime(t) {
  const v = t.startTime || t.endTime
  return v ? fmtTime(v) : '—'
}
// Runbook rows with a day divider whenever the step date changes (incl. before the
// first step — it marks the window start). Undated steps sort last and group under
// a "no date" divider. No "+1" convention here: the runbook spans several days.
function stepDateOf(t) {
  return t.startDate || t.endDate || null
}
function fmtDayMonth(s) {
  const p = String(s).split('-')
  return p.length === 3 ? `${p[2]}/${p[1]}` : s
}
function stepRows(m) {
  const rows = []
  let prev
  sortedSteps(m).forEach(task => {
    const d = stepDateOf(task)
    if (rows.length === 0 || d !== prev) {
      rows.push({ type: 'divider', key: 'day-' + (d || 'none') + '-' + rows.length, label: d ? fmtDayMonth(d) : t('workPlan.noDate') })
      prev = d
    }
    rows.push({ type: 'step', key: task.id, task })
  })
  return rows
}

// 'YYYY-MM-DD' -> 'DD/MM/YY' (string split — no timezone surprises)
function fmtDate(s) {
  const p = String(s).split('-')
  if (p.length !== 3) return s
  return `${p[2]}/${p[1]}/${p[0].slice(2)}`
}
// LocalTime 'HH:mm[:ss]' -> 'HH:mm'
function fmtTime(s) {
  return String(s).slice(0, 5)
}
// overnight end (endTime before startTime lexicographically on HH:mm)
function isNextDay(task) {
  return !!(task.startTime && task.endTime && fmtTime(task.endTime) < fmtTime(task.startTime))
}
function envsOf(task) {
  return task.environment ? task.environment.split(',').map(s => s.trim()).filter(Boolean) : []
}
function teamsOf(task) {
  return task.assignee ? task.assignee.split(',').map(s => s.trim()).filter(Boolean) : []
}
// comment may be plain text (imported) or Tiptap HTML (edited via the modal) — strip to text
function remarkText(task) {
  if (!task.comment) return ''
  return DOMPurify.sanitize(String(task.comment), { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim()
}
function remarkShort(task) {
  const txt = remarkText(task)
  return txt.length > 60 ? txt.slice(0, 60) + '…' : txt
}
// deterministic color from name hash over existing category/accent CSS vars — no hardcoded team list
const TEAM_PALETTE = ['dev', 'finance', 'legal', 'marketing', 'accent']
function teamColor(name) {
  let h = 0
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) >>> 0
  return TEAM_PALETTE[h % TEAM_PALETTE.length]
}
</script>

<style scoped>
/* Visual language mirrors ProjectDetailView panels — existing CSS vars only, no new colors */
.wp-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
.wp-header { padding: 14px 20px; border-bottom: 1px solid var(--border); background: var(--surface2); display: flex; align-items: center; justify-content: space-between; }
.wp-title { font-size: 13px; font-weight: 800; color: var(--text); }
.wp-scroll { overflow-x: auto; }
.wp-table { width: 100%; min-width: 900px; border-collapse: collapse; }
.wp-table th { font-family: "Nunito Sans", sans-serif; font-size: 9px; font-weight: 800; letter-spacing: 1.4px; text-transform: uppercase; color: var(--text-dim); text-align: left; padding: 9px 10px; background: var(--surface2); border-bottom: 1px solid var(--border); white-space: nowrap; }
.wp-table th.num { text-align: right; }
/* fixed narrow date/days columns; Task takes the free width; Env/Team stay auto */
.wp-table th.col-date { width: 96px; }
.wp-table th.col-days { width: 56px; }
.wp-table th.col-task { width: 100%; }
.wp-table td { padding: 8px 10px; border-bottom: 1px solid var(--border); font-size: 13px; color: var(--text-mid); vertical-align: top; }
.wp-table td.num { text-align: right; }
.wp-mrow td { background: var(--surface3); cursor: pointer; user-select: none; }
.wp-mrow:hover td { background: var(--surface2); }
.wp-trow.wp-clickable { cursor: pointer; }
.wp-trow.wp-clickable:hover td { background: var(--accent-dim); }
.wp-table th.col-remarks { max-width: 220px; }
.wp-remark { max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: "Nunito Sans", sans-serif; font-size: 11.5px; color: var(--text-dim); }
.wp-caret { display: inline-block; font-size: 10px; color: var(--text-mid); margin-right: 6px; transition: transform 0.15s; }
.wp-caret.closed { transform: rotate(-90deg); }
.wp-mdot { display: inline-block; width: 9px; height: 9px; border-radius: 50%; margin-right: 7px; }
.wp-mname { font-size: 13.5px; font-weight: 800; color: var(--text); }
.wp-dt { font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 700; white-space: nowrap; }
.wp-tm { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 600; color: var(--text-dim); margin-left: 5px; }
.wp-plus1 { font-family: "Nunito Sans", sans-serif; font-size: 9px; font-weight: 800; color: var(--legal); margin-left: 2px; }
.wp-days { font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 800; }
.wp-count { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; color: var(--text-dim); white-space: nowrap; margin-left: 8px; }
.wp-task { max-width: 380px; }
.wp-task .done { color: var(--text-dim); text-decoration: line-through; }
.wp-gate { color: var(--dev); font-weight: 800; margin-right: 6px; }
.wp-env { display: inline-block; font-family: "Nunito Sans", sans-serif; font-size: 10.5px; font-weight: 800; background: var(--surface3); color: var(--text-mid); border: 1px solid var(--border); border-radius: 9px; padding: 2px 8px; margin: 0 4px 3px 0; white-space: nowrap; }
.wp-team { display: inline-flex; align-items: center; gap: 5px; font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; color: var(--text-mid); margin: 0 9px 3px 0; white-space: nowrap; }
.wp-team i { width: 7px; height: 7px; border-radius: 2px; flex-shrink: 0; }
.wp-st { display: flex; align-items: center; gap: 8px; min-width: 110px; }
.wp-bar { flex: 1; height: 8px; background: var(--surface3); border-radius: 4px; overflow: hidden; min-width: 46px; }
.wp-bar i { display: block; height: 100%; border-radius: 4px; }
.wp-pc { font-family: "Nunito Sans", sans-serif; font-size: 11.5px; font-weight: 800; width: 34px; text-align: right; }
.wp-empty { text-align: center; color: var(--text-dim); font-size: 12px; font-family: "Nunito Sans", sans-serif; }
/* segmented control — mirrors GanttV2 zoom controls (existing vars only) */
.wp-seg { display: flex; gap: 2px; background: var(--surface3); border: 1px solid var(--border); border-radius: 7px; padding: 3px; }
.wp-seg-btn { font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase; padding: 6px 12px; border: none; background: transparent; color: var(--text-dim); cursor: pointer; border-radius: 5px; transition: all 0.12s; }
.wp-seg-btn:hover { color: var(--text); }
.wp-seg-btn.active { background: var(--text); color: var(--surface); box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
/* cutover runbook — vertical timeline mirrors .history-timeline (existing vars only) */
.wp-rb { padding: 14px 20px; }
.wp-rb-block { margin-bottom: 18px; }
.wp-rb-block:last-child { margin-bottom: 4px; }
.wp-rb-head { display: flex; align-items: center; margin-bottom: 8px; }
.wp-rb-meta { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; color: var(--text-dim); margin-left: 10px; white-space: nowrap; }
.wp-rb-timeline { display: flex; flex-direction: column; gap: 0; border-left: 2px solid var(--border-bright); margin-left: 8px; padding-left: 18px; }
.wp-rb-step { position: relative; padding: 7px 6px; display: flex; align-items: flex-start; gap: 10px; cursor: pointer; border-radius: 6px; }
.wp-rb-step:hover { background: var(--accent-dim); }
.wp-rb-dot { position: absolute; left: -24px; top: 12px; width: 8px; height: 8px; border-radius: 50%; background: var(--surface); border: 2px solid var(--accent); }
.wp-rb-dot.gate { transform: rotate(45deg); border-radius: 2px; border-color: var(--dev); }
.wp-rb-dot.filled { background: var(--green); border-color: var(--green); }
.wp-rb-time { font-family: "Nunito Sans", sans-serif; font-size: 11.5px; font-weight: 800; color: var(--text-mid); min-width: 44px; padding-top: 1px; white-space: nowrap; }
.wp-rb-body { min-width: 0; }
.wp-rb-name { font-size: 13px; font-weight: 700; color: var(--text); }
.wp-rb-name.done { color: var(--text-dim); text-decoration: line-through; }
.wp-rb-teams { margin-top: 2px; }
.wp-rb-remark { font-family: "Nunito Sans", sans-serif; font-size: 11px; color: var(--text-dim); margin-top: 2px; }
.wp-rb-day { display: flex; align-items: center; gap: 8px; padding: 8px 0 2px; }
.wp-rb-day-label { font-family: "Nunito Sans", sans-serif; font-size: 9px; font-weight: 800; letter-spacing: 1.2px; text-transform: uppercase; color: var(--text-dim); white-space: nowrap; }
.wp-rb-day::after { content: ''; flex: 1; height: 1px; background: var(--border); }
</style>
