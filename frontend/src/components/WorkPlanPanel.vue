<template>
  <div v-if="project && project.workPlanEnabled" class="wp-panel" style="margin-top:14px;">
    <div class="wp-header">
      <div class="wp-title">📋 {{ t('workPlan.title') }}</div>
      <div class="wp-seg">
        <button :class="['wp-seg-btn', { active: activeTab === 'plan' }]" @click="activeTab = 'plan'">{{ t('workPlan.tabPlan') }}</button>
        <button v-if="cutoverModules.length" :class="['wp-seg-btn', { active: activeTab === 'cutover' }]" @click="activeTab = 'cutover'">{{ t('workPlan.tabCutover') }}</button>
        <button :class="['wp-seg-btn', { active: activeTab === 'kpis' }]" @click="activeTab = 'kpis'">{{ t('workPlan.tabKpis') }}</button>
      </div>
    </div>

    <!-- KPI strip (phase A) — always unfiltered totals; time anchor = reference date -->
    <div class="wp-kpis">
      <div class="wp-kpi">
        <div class="wp-kpi-lbl">{{ t('workPlan.kOverall') }}</div>
        <div class="wp-kpi-val accent">{{ kpi.overall }}<small>%</small></div>
        <div class="wp-kpi-sub">{{ t('workPlan.kPlannedTo') }} {{ kpi.planned }}%</div>
      </div>
      <div class="wp-kpi" :class="kpi.variance > 0 ? 'good' : kpi.variance < 0 ? 'bad' : ''">
        <div class="wp-kpi-lbl">{{ t('workPlan.kVariance') }}</div>
        <div class="wp-kpi-val" :class="kpi.variance > 0 ? 'good' : kpi.variance < 0 ? 'bad' : ''">{{ kpi.variance > 0 ? '+' : '' }}{{ kpi.variance }}<small>pp</small></div>
        <div class="wp-kpi-sub">{{ kpi.variance < 0 ? t('workPlan.kBehind') : t('workPlan.kOnPlan') }}</div>
      </div>
      <div class="wp-kpi">
        <div class="wp-kpi-lbl">{{ t('workPlan.kNextGate') }}</div>
        <template v-if="kpi.nextGate">
          <div class="wp-kpi-val" :class="{ bad: kpi.nextGateDays < 0 }" :title="kpi.nextGate.g.name">{{ kpi.nextGateDays }}<small>d</small></div>
          <div class="wp-kpi-sub">{{ fmtDate(kpi.nextGate.d) }}</div>
        </template>
        <template v-else>
          <div class="wp-kpi-val">—</div>
          <div class="wp-kpi-sub"></div>
        </template>
      </div>
      <div class="wp-kpi">
        <div class="wp-kpi-lbl">{{ t('workPlan.kGates') }}</div>
        <div class="wp-kpi-val">{{ kpi.passedCount }}<small>/{{ kpi.gates.length }}</small></div>
        <div class="wp-kpi-sub wp-kpi-dia">{{ kpi.diamonds }}</div>
      </div>
      <div class="wp-kpi" :class="(kpi.overdue + kpi.flagged) ? 'bad' : 'good'">
        <div class="wp-kpi-lbl">{{ t('workPlan.kAtRisk') }}</div>
        <div class="wp-kpi-val" :class="(kpi.overdue + kpi.flagged) ? 'bad' : 'good'">{{ kpi.overdue + kpi.flagged }}</div>
        <div class="wp-kpi-sub">{{ kpi.overdue }} {{ t('workPlan.kOverdue') }} · {{ kpi.flagged }} {{ t('workPlan.kFlagged') }}</div>
      </div>
      <div class="wp-kpi">
        <div class="wp-kpi-lbl">{{ t('workPlan.kEffort') }}</div>
        <div class="wp-kpi-val">{{ kpi.effort }}<small>d</small></div>
        <div class="wp-kpi-sub">{{ wpModules.length }} {{ t('workPlan.kModules') }}</div>
      </div>
    </div>

    <!-- filter row (phase A) — applies to the PLAN table only; KPIs stay unfiltered -->
    <div v-if="activeTab === 'plan'" class="wp-ctrl">
      <div class="wp-fld">
        <label class="wp-ctrl-lbl">{{ t('workPlan.asOf') }}</label>
        <input type="date" class="wp-select" v-model="asOf" />
      </div>
      <div class="wp-fld">
        <label class="wp-ctrl-lbl">{{ t('workPlan.environment') }}</label>
        <select class="wp-select" v-model="fEnv">
          <option value="">{{ t('workPlan.allEnvs') }}</option>
          <option v-for="e in envOptions" :key="e" :value="e">{{ e }}</option>
        </select>
      </div>
      <div class="wp-fld">
        <label class="wp-ctrl-lbl">{{ t('workPlan.teams') }}</label>
        <select class="wp-select" v-model="fTeam">
          <option value="">{{ t('workPlan.allTeams') }}</option>
          <option v-for="tm in teamOptions" :key="tm" :value="tm">{{ tm }}</option>
        </select>
      </div>
      <div class="wp-chips">
        <button v-for="q in QUICK_VIEWS" :key="q" :class="['wp-chip', { on: quickView === q }]" @click="quickView = q">{{ t('workPlan.' + QUICK_LABEL[q]) }}</button>
      </div>
    </div>
    <!-- KPIs tab shows only the reference date — filters never apply there -->
    <div v-else-if="activeTab === 'kpis'" class="wp-ctrl">
      <div class="wp-fld">
        <label class="wp-ctrl-lbl">{{ t('workPlan.asOf') }}</label>
        <input type="date" class="wp-select" v-model="asOf" />
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
            <th>{{ t('workPlan.colHealth') }}</th>
            <th class="num">{{ t('workPlan.colVariance') }}</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="m in visibleModules" :key="m.id">
            <tr class="wp-mrow" @click="toggle(m.id)">
              <td class="wp-dt">{{ m.minStart ? fmtDate(m.minStart) : '—' }}</td>
              <td class="wp-dt">{{ m.maxEnd ? fmtDate(m.maxEnd) : '—' }}</td>
              <td class="num wp-days">{{ m.sumDays }}</td>
              <td>
                <span class="wp-caret" :class="{ closed: !open.has(m.id) }">▼</span>
                <span class="wp-mdot" :style="`background:var(--${m.color || 'dev'});`"></span>
                <span class="wp-mname">{{ m.name || '—' }}</span>
                <span class="wp-count">{{ m.doneCount }}/{{ m.tasks.length }}</span>
                <span v-if="cutoverIds.has(m.id)" class="wp-cut-badge">{{ t('workPlan.cutBadge') }}</span>
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
              <td></td>
              <td></td>
            </tr>
            <template v-if="open.has(m.id)">
              <tr v-for="task in m.visibleTasks" :key="task.id" :class="['wp-trow', 'wp-clickable', flagClass(task)]" @click="$emit('task-click', task.id)">
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
                <td><span :class="['wp-hb', taskHealth(task)]">{{ t('workPlan.' + HEALTH_KEY[taskHealth(task)]) }}</span></td>
                <td class="num">
                  <span v-if="taskVariance(task) !== null" class="wp-var">+{{ taskVariance(task) }}d</span>
                  <template v-else>—</template>
                </td>
              </tr>
              <tr v-if="!m.tasks.length" class="wp-trow">
                <td colspan="10" class="wp-empty">{{ t('workPlan.noTasks') }}</td>
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

    <!-- KPIS TAB (phase C.1 + C.2) — simulator / gates / team load / env / alerts -->
    <div v-else-if="activeTab === 'kpis'" class="wp-kpitab">
      <!-- left column: slip scenario above the gates -->
      <div class="wp-kpicol">
        <div class="wp-pane">
          <div class="wp-pane-head">
            <div class="wp-pane-title">{{ t('workPlan.pSim') }}</div>
            <div class="wp-pane-sub">{{ t('workPlan.pSimHint') }}</div>
          </div>
          <div class="wp-sim-ctrl">
            <select class="wp-select wp-sim-select" v-model="simTaskId">
              <option value="">—</option>
              <optgroup v-for="m in wpModules" :key="m.id" :label="m.name">
                <option v-for="tk in m.tasks.filter(x => x.isGate !== true && x.endDate)" :key="tk.id" :value="tk.id">{{ tk.name }}</option>
              </optgroup>
            </select>
            <input type="number" class="wp-select wp-sim-days" v-model.number="simDays" min="1" />
            <span class="wp-ctrl-lbl">{{ t('workPlan.simWd') }}</span>
            <button class="wp-btn" :disabled="!simTaskId" @click="runSim">{{ t('workPlan.simRun') }}</button>
            <button class="wp-btn-ghost" @click="resetSim">{{ t('workPlan.simReset') }}</button>
          </div>
          <div v-if="!simResult" class="wp-sim-empty">{{ t('workPlan.simEmpty') }}</div>
          <div v-else class="wp-sim-out">
            <div class="wp-sim-target"><b>{{ simResult.targetName }}</b> +{{ simResult.n }} {{ t('workPlan.simWd') }} · {{ fmtDate(simResult.oldEnd) }} → <b>{{ fmtDate(simResult.newEnd) }}</b></div>
            <div v-if="simResult.gate" class="wp-sim-gate">
              ◆ {{ t('workPlan.simImpact') }}: <b>{{ simResult.gate.name }}</b> — {{ fmtDate(simResult.gate.oldEnd) }} → <b class="bad">{{ fmtDate(simResult.gate.newEnd) }}</b>
            </div>
            <div v-else class="wp-sim-gate none">{{ t('workPlan.simNoGate') }}</div>
            <div class="wp-sim-list">
              <div v-for="mv in simResult.moved.slice(0, 8)" :key="mv.id" class="wp-sim-row">
                {{ mv.name }} · {{ mv.oldStart ? fmtDate(mv.oldStart) : fmtDate(mv.oldEnd) }} → {{ mv.newStart ? fmtDate(mv.newStart) : fmtDate(mv.newEnd) }}
              </div>
              <div v-if="simResult.moved.length > 8" class="wp-sim-row more">{{ t('workPlan.simMore', { n: simResult.moved.length - 8 }) }}</div>
            </div>
          </div>
        </div>

        <div class="wp-pane">
        <div class="wp-pane-head">
          <div class="wp-pane-title">◆ {{ t('workPlan.pGates') }}</div>
          <div class="wp-pane-sub">{{ kpi.passedCount }}/{{ kpi.gates.length }}</div>
        </div>
        <div v-for="g in gateRows" :key="g.id" class="wp-gate-row" @click="$emit('task-click', g.id)">
          <span :class="['wp-gr-dia', { filled: g.passed }]"></span>
          <div class="wp-gr-body">
            <div class="wp-gr-name">{{ g.name }}</div>
            <div class="wp-gr-sub">{{ g.pending }} {{ t('workPlan.pPending') }} · {{ g.completion }}%</div>
          </div>
          <div class="wp-gr-right">
            <span class="wp-gr-date">{{ g.date ? fmtDate(g.date) : '—' }}</span>
            <span :class="['wp-gr-badge', { ok: g.passed }]">{{ g.passed ? t('workPlan.badgePassed') : t('workPlan.badgePending') }}</span>
          </div>
        </div>
        </div>
      </div>

      <!-- right column: load + environments stacked, so the grid leaves no dead space -->
      <div class="wp-kpicol">
        <div class="wp-pane">
          <div class="wp-pane-head">
            <div class="wp-pane-title">{{ t('workPlan.pLoad') }}</div>
            <div class="wp-pane-sub">{{ t('workPlan.pLoadHint') }}</div>
          </div>
          <div v-for="l in teamLoad.rows" :key="l.name" class="wp-load-row">
            <div class="wp-load-name"><i :style="`background:var(--${teamColor(l.name)});`"></i>{{ l.name }}</div>
            <div class="wp-load-track">
              <i v-for="s in l.segments" :key="s.id" :style="`width:${teamLoad.max ? (s.days / teamLoad.max) * 100 : 0}%;background:${s.color};`" :title="s.title"></i>
            </div>
            <div class="wp-load-total">{{ l.total }}d</div>
          </div>
          <div class="wp-legend">
            <span v-for="(m, mi) in wpModules" :key="m.id" class="wp-legend-item">
              <i :style="`background:${moduleColor(mi)};`"></i>{{ m.name }}
            </span>
          </div>
          <div v-if="noTeamCount" class="wp-pane-note">{{ noTeamCount }} {{ t(noTeamCount === 1 ? 'workPlan.pNoTeamOne' : 'workPlan.pNoTeam') }}</div>
        </div>

        <div class="wp-pane">
          <div class="wp-pane-head">
            <div class="wp-pane-title">{{ t('workPlan.pEnv') }}</div>
          </div>
          <div v-for="e in envCoverage.rows" :key="e.name" class="wp-load-row">
            <div class="wp-load-name"><span class="wp-env">{{ e.name }}</span></div>
            <div class="wp-load-track"><i :style="`width:${envCoverage.max ? (e.days / envCoverage.max) * 100 : 0}%;background:var(--accent);`"></i></div>
            <div class="wp-load-total">{{ e.count }} · {{ e.days }}d</div>
          </div>
        </div>
      </div>

      <!-- automatic alerts — full width, regenerated on every change (phase C.2) -->
      <div class="wp-pane wp-alerts">
        <div class="wp-pane-head">
          <div class="wp-pane-title">{{ t('workPlan.pAlerts') }}</div>
          <div class="wp-pane-sub">{{ t('workPlan.pAlertsHint') }}</div>
        </div>
        <div v-if="!alerts.length" class="wp-al-none">✅ {{ t('workPlan.alNone') }}</div>
        <div v-else class="wp-al-list">
          <div v-for="(a, ai) in alerts" :key="ai + '-' + a.id" :class="['wp-al-card', a.sev]">
            <span class="wp-al-ico">{{ a.icon }}</span>
            <div class="wp-al-body">
              <div class="wp-al-title">{{ a.title }}</div>
              <div class="wp-al-desc">{{ a.desc }}</div>
            </div>
            <button class="wp-al-open" @click="$emit('task-click', a.id)">{{ t('workPlan.alOpen') }}</button>
          </div>
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

// ── KPI phase A ─────────────────────────────────────────────────────────────
// Greek public holidays inside the plan horizon, copied from the approved mockup.
// Needs updating for 2028 onwards.
// Used by the workday helpers below (phase C.2 slip scenario and alerts).
const GREEK_HOLIDAYS = new Set([
  '2026-10-28', '2026-12-25', '2026-12-28', '2026-12-29', '2026-12-30', '2026-12-31',
  '2027-01-01', '2027-01-06', '2027-03-15', '2027-03-25', '2027-05-03', '2027-06-21',
])

// ── workday helpers (phase C.2) ─────────────────────────────────────────────
// All date math runs on Date.UTC built from the string PARTS. new Date(iso)
// also parses as UTC, but any local-time getter afterwards could shift the
// day near midnight in negative-offset zones — UTC getters never can.
function isoToUTC(iso) {
  const p = String(iso).split('-')
  return Date.UTC(+p[0], +p[1] - 1, +p[2])
}
function utcToISO(ms) {
  return new Date(ms).toISOString().slice(0, 10)
}
function isWorkday(iso) {
  const day = new Date(isoToUTC(iso)).getUTCDay()
  return day !== 0 && day !== 6 && !GREEK_HOLIDAYS.has(String(iso))
}
function addWorkdays(iso, n) {
  if (!iso) return iso
  let ms = isoToUTC(iso)
  let left = Math.abs(n)
  const step = n >= 0 ? 86400000 : -86400000
  while (left > 0) {
    ms += step
    if (isWorkday(utcToISO(ms))) left--
  }
  return utcToISO(ms)
}

function todayISO() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}
const dd = (a, b) => Math.round((new Date(b) - new Date(a)) / 86400000)

// reference date — defaults to today, drives planned%, variance, next-gate, overdue, 7-day view
const asOf = ref(todayISO())
const fEnv = ref('')
const fTeam = ref('')
const quickView = ref('all')
const QUICK_VIEWS = ['all', 'week', 'risk', 'gates', 'cut', 'open']
const QUICK_LABEL = { all: 'qAll', week: 'qWeek', risk: 'qRisk', gates: 'qGates', cut: 'qCut', open: 'qOpen' }

const allTasks = computed(() => wpModules.value.flatMap(m => m.tasks))
const envOptions = computed(() => [...new Set(allTasks.value.flatMap(envsOf))].sort())
const teamOptions = computed(() => [...new Set(allTasks.value.flatMap(teamsOf))].sort())

// weight: workDays, or 0.25 when the task has none (mirrors the approved mockup)
const weightOf = t => (t.workDays !== null && t.workDays !== undefined) ? Number(t.workDays) : 0.25
const isOverdue = t => !!(t.endDate && t.endDate < asOf.value && (t.progress || 0) < 100)

const kpi = computed(() => {
  const tasks = allTasks.value
  const nonGate = tasks.filter(t => t.isGate !== true)
  // Weighted by work days, not task count. A 45-day task cannot count the
  // same as a 0.1-day one. This differs from the project header, which uses
  // calcCompletion; the label says which measure this is.
  const totW = nonGate.reduce((s, t) => s + weightOf(t), 0)
  const overall = totW ? Math.round(nonGate.reduce((s, t) => s + (t.progress || 0) * weightOf(t), 0) / totW) : 0
  // planned-to-reference-date: full weight past endDate, pro-rata inside the window, 0 before it
  let earned = 0
  nonGate.forEach(t => {
    if (!t.endDate) return
    const start = t.startDate || t.endDate
    if (asOf.value >= t.endDate) earned += weightOf(t)
    else if (asOf.value > start) {
      const span = Math.max(1, dd(start, t.endDate))
      earned += weightOf(t) * (dd(start, asOf.value) / span)
    }
  })
  const planned = totW ? Math.round((earned / totW) * 100) : 0
  const gates = tasks.filter(t => t.isGate === true)
  const passed = gates.filter(g => (g.progress || 0) === 100)
  // next pending gate CHRONOLOGICALLY (no name matching); undated pending gates can't be "next"
  const pendingDated = gates
    .filter(g => (g.progress || 0) < 100)
    .map(g => ({ g, d: g.endDate || g.startDate || null }))
    .filter(x => x.d)
    .sort((a, b) => (a.d < b.d ? -1 : a.d > b.d ? 1 : 0))
  const nextGate = pendingDated[0] || null
  return {
    overall,
    planned,
    variance: overall - planned,
    gates,
    passedCount: passed.length,
    diamonds: gates.map(g => ((g.progress || 0) === 100 ? '◆' : '◇')).join(''),
    nextGate,
    nextGateDays: nextGate ? dd(asOf.value, nextGate.d) : null,
    // Gates are approvals, not work. They are counted in the gates KPI;
    // including them here would report the same thing twice.
    overdue: nonGate.filter(isOverdue).length,
    flagged: nonGate.filter(t => t.isBlocked === true).length,
    effort: parseFloat(tasks.reduce((s, t) => s + (Number(t.workDays) || 0), 0).toFixed(2)),
  }
})

// ── filters (PLAN table only — modules with no visible tasks are hidden) ────
function taskVisible(t, m) {
  if (fEnv.value && !envsOf(t).includes(fEnv.value)) return false
  if (fTeam.value && !teamsOf(t).includes(fTeam.value)) return false
  switch (quickView.value) {
    case 'week': { const s = t.startDate || t.endDate; if (!s) return false; const d = dd(asOf.value, s); return d >= 0 && d <= 7 }
    case 'risk': return isOverdue(t) || t.isBlocked === true
    case 'gates': return t.isGate === true
    case 'cut': return cutoverIds.value.has(m.id)
    case 'open': return remarkText(t).includes('?')
    default: return true
  }
}
const filtersActive = computed(() => !!(fEnv.value || fTeam.value || quickView.value !== 'all'))
const visibleModules = computed(() => {
  if (!filtersActive.value) return wpModules.value.map(m => ({ ...m, visibleTasks: m.tasks }))
  return wpModules.value
    .map(m => ({ ...m, visibleTasks: m.tasks.filter(t => taskVisible(t, m)) }))
    .filter(m => m.visibleTasks.length > 0)
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

const cutoverIds = computed(() => new Set(cutoverModules.value.map(m => m.id)))

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

// ── phase C.1: KPIs tab panels ──────────────────────────────────────────────
// Gates listed chronologically with their module's pending work and completion.
const gateRows = computed(() => {
  const rows = []
  wpModules.value.forEach(m => {
    m.tasks.filter(tk => tk.isGate === true).forEach(g => {
      rows.push({
        id: g.id,
        name: g.name || '—',
        passed: (g.progress || 0) === 100,
        pending: m.tasks.filter(x => x.isGate !== true && (x.progress || 0) < 100).length,
        completion: m.completion || 0,
        date: g.endDate || g.startDate || null,
      })
    })
  })
  rows.sort((a, b) => {
    const ka = a.date || '9999-12-31', kb = b.date || '9999-12-31'
    return ka < kb ? -1 : ka > kb ? 1 : 0
  })
  return rows
})

// Segment colors cycle by module index — module.color is often the same for
// every phase of one project, which would make a stacked bar unreadable.
const MODULE_PALETTE = ['dev', 'accent', 'finance', 'legal', 'marketing']
// Eight phases against five palette vars: shades keep every segment
// distinct, so the legend actually explains the bar.
// Implemented with color-mix (baseline 2023; the app already relies on :has(),
// same support era): round 2 lightens with white, round 3 darkens with black.
function moduleColor(mi) {
  const base = `var(--${MODULE_PALETTE[mi % MODULE_PALETTE.length]})`
  const round = Math.floor(mi / MODULE_PALETTE.length)
  if (round === 0) return base
  if (round === 1) return `color-mix(in srgb, ${base} 55%, white)`
  return `color-mix(in srgb, ${base} 70%, black)`
}
const teamLoad = computed(() => {
  const map = {}
  wpModules.value.forEach((m, mi) => {
    m.tasks.forEach(tk => {
      const teams = teamsOf(tk)
      const d = Number(tk.workDays) || 0
      if (!teams.length || !d) return
      // A task shared by k teams contributes workDays/k to each — effort is
      // split evenly, so the totals still add up to the plan's working days.
      const share = d / teams.length
      teams.forEach(name => {
        if (!map[name]) map[name] = { name, total: 0, seg: new Map() }
        map[name].total += share
        map[name].seg.set(mi, (map[name].seg.get(mi) || 0) + share)
      })
    })
  })
  const rows = Object.values(map).map(l => ({
    name: l.name,
    total: Math.round(l.total * 10) / 10,
    segments: [...l.seg.entries()].sort((a, b) => a[0] - b[0]).map(([mi, days]) => ({
      id: mi,
      days,
      color: moduleColor(mi),
      title: `${(wpModules.value[mi] && wpModules.value[mi].name) || ''}: ${Math.round(days * 10) / 10}d`,
    })),
  })).sort((a, b) => b.total - a.total)
  const max = rows.length ? Math.max(...rows.map(r => r.total)) : 0
  return { rows, max }
})
const noTeamCount = computed(() => allTasks.value.filter(tk => tk.isGate !== true && !teamsOf(tk).length).length)

const envCoverage = computed(() => {
  const map = {}
  allTasks.value.forEach(tk => {
    envsOf(tk).forEach(e => {
      if (!map[e]) map[e] = { name: e, count: 0, days: 0 }
      map[e].count++
      map[e].days += Number(tk.workDays) || 0
    })
  })
  const rows = Object.values(map).map(r => ({ ...r, days: Math.round(r.days * 10) / 10 })).sort((a, b) => b.days - a.days)
  const max = rows.length ? Math.max(...rows.map(r => r.days)) : 0
  return { rows, max }
})

// ── phase C.2: slip scenario (in-memory only — nothing is saved, no PUT) ────
const simTaskId = ref('')
const simDays = ref(3)
const simResult = ref(null)

function runSim() {
  const target = allTasks.value.find(x => x.id === simTaskId.value)
  if (!target || !target.endDate) return
  const n = Math.max(1, Math.round(Number(simDays.value) || 1))
  const origEnd = target.endDate
  const moved = []
  allTasks.value.forEach(x => {
    if (x.id === target.id) return
    // Cascade anchor is startDate, falling back to endDate for gate-style
    // tasks that carry no start — otherwise no gate could ever move and the
    // "next gate impact" below would always be empty (same as the mockup).
    const s = x.startDate || x.endDate
    if (s && s >= origEnd) {
      moved.push({
        id: x.id, name: x.name, isGate: x.isGate === true,
        oldStart: x.startDate, newStart: x.startDate ? addWorkdays(x.startDate, n) : null,
        oldEnd: x.endDate, newEnd: x.endDate ? addWorkdays(x.endDate, n) : null,
      })
    }
  })
  const movedGates = moved.filter(x => x.isGate)
    .sort((a, b) => ((a.oldEnd || '') < (b.oldEnd || '') ? -1 : 1))
  simResult.value = {
    targetId: target.id,
    targetName: target.name,
    n,
    oldEnd: origEnd,
    newEnd: addWorkdays(origEnd, n),
    moved,
    gate: movedGates[0] || null,
  }
}
function resetSim() { simResult.value = null }

// ── phase C.2: automatic alerts (recomputed on every change) ────────────────
const alerts = computed(() => {
  const list = []
  const ref = asOf.value
  // (a) pending gate within 21 days with outstanding work in its phase
  gateRows.value.forEach(g => {
    if (!g.passed && g.date && g.pending > 0 && dd(ref, g.date) <= 21)
      list.push({ sev: 'crit', icon: '🔴', id: g.id, title: g.name, desc: t('workPlan.alGate', { n: g.pending, date: fmtDate(g.date) }) })
  })
  // (b) overdue non-gate tasks
  allTasks.value.forEach(x => {
    if (x.isGate !== true && isOverdue(x))
      list.push({ sev: 'crit', icon: '🔴', id: x.id, title: x.name, desc: t('workPlan.alOverdue', { date: fmtDate(x.endDate), p: x.progress || 0 }) })
  })
  // (c) blocked tasks — surface the block note when there is one
  allTasks.value.forEach(x => {
    if (x.isBlocked === true)
      list.push({ sev: 'crit', icon: '🔴', id: x.id, title: x.name, desc: x.blockNote || t('workPlan.alBlocked') })
  })
  // (d) open questions
  allTasks.value.forEach(x => {
    if (remarkText(x).includes('?'))
      list.push({ sev: 'warn', icon: '🟡', id: x.id, title: x.name, desc: t('workPlan.alOpenQ') + ' — ' + remarkShort(x) })
  })
  // (e) non-gate tasks without a team
  allTasks.value.forEach(x => {
    if (x.isGate !== true && !teamsOf(x).length)
      list.push({ sev: 'warn', icon: '🟡', id: x.id, title: x.name, desc: t('workPlan.alNoTeam') })
  })
  // (f) tasks starting or ending on a non-working day
  allTasks.value.forEach(x => {
    const bad = (x.startDate && !isWorkday(x.startDate)) ? x.startDate
      : (x.endDate && !isWorkday(x.endDate)) ? x.endDate : null
    if (bad) list.push({ sev: 'warn', icon: '🟡', id: x.id, title: x.name, desc: t('workPlan.alNonWork', { date: fmtDate(bad) }) })
  })
  // (g) active slip scenario
  if (simResult.value)
    list.push({ sev: 'info', icon: '🔵', id: simResult.value.targetId, title: simResult.value.targetName, desc: t('workPlan.alSim', { n: simResult.value.n, m: simResult.value.moved.length }) })
  return list
})

// ── phase B: health / variance / row flags ──────────────────────────────────
const HEALTH_KEY = { done: 'hDone', late: 'hLate', prog: 'hProg', soon: 'hSoon', todo: 'hTodo' }

// Calendar state machine, checked in this exact order. Tasks without dates -> 'todo'.
// Progress above zero means work has started, whatever the dates say.
// Some tasks carry progress without a start date, and the bar next to
// the badge would contradict it.
function taskHealth(task, ref = asOf.value) {
  if ((task.progress || 0) === 100) return 'done'
  if (task.endDate && task.endDate < ref) return 'late'
  if ((task.progress || 0) > 0) return 'prog'
  if (task.startDate && task.startDate <= ref) return 'prog'
  if (task.startDate && dd(ref, task.startDate) <= 7) return 'soon'
  return 'todo'
}

// Variance is measured against where the task should be today, not against
// a stored baseline. A real baseline needs V30 columns.
function taskVariance(task, ref = asOf.value) {
  if ((task.progress || 0) === 100) return null
  if (task.startDate && task.startDate > ref) return null
  if (task.endDate && task.endDate < ref) return dd(task.endDate, ref)
  if (!task.startDate || !task.endDate) return null
  if (task.workDays === null || task.workDays === undefined) return null
  const span = Math.max(1, dd(task.startDate, task.endDate))
  const expected = dd(task.startDate, ref) / span
  const actual = (task.progress || 0) / 100
  const v = Math.round((expected - actual) * Number(task.workDays) * 10) / 10
  return v > 0 ? v : null
}

// red (blocked) beats yellow (open question in the comment)
function flagClass(task) {
  if (task.isBlocked === true) return 'flag-red'
  if (remarkText(task).includes('?')) return 'flag-yellow'
  return ''
}

// 'YYYY-MM-DD' -> 'Δε 12/10/26' — weekday from ganttv2.daysShort (existing i18n),
// computed via Date.UTC on the string parts so no timezone can shift the day.
function fmtDate(s) {
  const p = String(s).split('-')
  if (p.length !== 3) return s
  const dow = new Date(Date.UTC(+p[0], +p[1] - 1, +p[2])).getUTCDay()
  return t('ganttv2.daysShort.' + dow) + ' ' + `${p[2]}/${p[1]}/${p[0].slice(2)}`
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
.wp-table { width: 100%; min-width: 1080px; border-collapse: collapse; }
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
/* KPI strip — mirrors DashboardView .kpi-strip/.kpi (existing vars only) */
.wp-kpis { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; padding: 14px 20px 12px; border-bottom: 1px solid var(--border); }
.wp-kpi { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 11px 10px; position: relative; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.05); text-align: center; }
.wp-kpi::before { content: ""; position: absolute; top: 0; left: 0; right: 0; height: 3px; background: var(--accent); }
.wp-kpi.good::before { background: var(--green); }
.wp-kpi.bad::before { background: var(--red); }
.wp-kpi-lbl { font-family: "Nunito Sans", sans-serif; font-size: 8.5px; letter-spacing: 1.6px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 6px; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.wp-kpi-val { font-size: 22px; font-weight: 900; line-height: 1.1; color: var(--text); }
.wp-kpi-val small { font-size: 12px; font-weight: 800; color: var(--text-dim); }
.wp-kpi-val.accent { color: var(--accent); }
.wp-kpi-val.good { color: var(--green); }
.wp-kpi-val.bad { color: var(--red); }
.wp-kpi-sub { font-family: "Nunito Sans", sans-serif; font-size: 9.5px; color: var(--text-dim); font-weight: 700; margin-top: 5px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; min-height: 12px; }
.wp-kpi-dia { color: var(--dev); letter-spacing: 2px; }
@media (max-width: 1100px) { .wp-kpis { grid-template-columns: repeat(3, 1fr); } }
/* filter row — selects mirror .ph-select, chips mirror quick-filter buttons */
.wp-ctrl { display: flex; gap: 14px; align-items: center; flex-wrap: wrap; padding: 10px 20px; background: var(--surface2); border-bottom: 1px solid var(--border); }
.wp-fld { display: flex; align-items: center; gap: 7px; }
.wp-ctrl-lbl { font-family: "Nunito Sans", sans-serif; font-size: 8.5px; font-weight: 800; letter-spacing: 1.4px; text-transform: uppercase; color: var(--text-dim); white-space: nowrap; }
.wp-select { background: var(--surface2); border: 1px solid var(--border-bright); border-radius: 6px; padding: 5px 10px; color: var(--text-mid); font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 600; cursor: pointer; background-color: var(--surface); }
.wp-chips { display: flex; gap: 5px; flex-wrap: wrap; }
.wp-chip { border: 1px solid var(--border-bright); background: var(--surface); border-radius: 10px; padding: 4px 11px; font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 800; color: var(--text-dim); cursor: pointer; transition: all 0.15s; }
.wp-chip:hover { color: var(--text); border-color: var(--text-dim); }
.wp-chip.on { background: var(--accent-dim); border-color: var(--accent); color: var(--accent); }
/* phase B — health badge (ProfileView .status-badge pattern: dim bg + var text) */
.wp-hb { display: inline-block; font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 800; padding: 3px 8px; border-radius: 8px; white-space: nowrap; }
.wp-hb.done { background: var(--green-dim); color: var(--green); }
.wp-hb.late { background: var(--red-dim); color: var(--red); }
.wp-hb.prog { background: var(--accent-dim); color: var(--accent); }
.wp-hb.soon { background: var(--yellow-dim); color: var(--yellow); }
.wp-hb.todo { background: var(--surface3); color: var(--text-dim); border: 1px solid var(--border-bright); }
/* phase B — variance (days behind) */
.wp-var { font-family: "Nunito Sans", sans-serif; font-size: 11.5px; font-weight: 800; color: var(--red); white-space: nowrap; }
/* phase B — cutover badge on module rows */
.wp-cut-badge { font-family: "Nunito Sans", sans-serif; font-size: 9px; font-weight: 700; padding: 2px 8px; border-radius: 8px; background: var(--legal-dim); color: var(--legal); margin-left: 8px; white-space: nowrap; }
/* phase B — row severity flags: 3px left stripe + light tint (hover still wins — higher specificity) */
.wp-trow.flag-red td { background: var(--red-dim); }
.wp-trow.flag-red td:first-child { box-shadow: inset 3px 0 0 var(--red); }
.wp-trow.flag-yellow td { background: var(--yellow-dim); }
.wp-trow.flag-yellow td:first-child { box-shadow: inset 3px 0 0 var(--yellow); }
/* phase C.1 — KPIs tab: 2-column grid of panels (1 column under 1000px) */
.wp-kpitab { padding: 14px 20px; display: grid; grid-template-columns: 1fr 1fr; gap: 14px; align-items: start; }
@media (max-width: 1000px) { .wp-kpitab { grid-template-columns: 1fr; } }
.wp-kpicol { display: flex; flex-direction: column; gap: 14px; min-width: 0; }
/* module legend under the stacked bars */
.wp-legend { display: flex; flex-wrap: wrap; gap: 4px 12px; padding: 8px 16px 10px; border-top: 1px solid var(--border); }
.wp-legend-item { display: inline-flex; align-items: center; gap: 5px; font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 700; color: var(--text-dim); }
.wp-legend-item i { width: 7px; height: 7px; border-radius: 2px; flex-shrink: 0; }
.wp-pane { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
.wp-pane-head { padding: 12px 16px; border-bottom: 1px solid var(--border); background: var(--surface2); display: flex; align-items: baseline; justify-content: space-between; gap: 10px; }
.wp-pane-title { font-size: 13px; font-weight: 800; color: var(--text); }
.wp-pane-sub { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; color: var(--text-dim); white-space: nowrap; }
.wp-pane-note { padding: 10px 16px; font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 600; color: var(--text-dim); background: var(--surface2); border-top: 1px solid var(--border); }
/* gates panel rows */
.wp-gate-row { display: flex; align-items: flex-start; gap: 11px; padding: 10px 16px; border-bottom: 1px solid var(--border); cursor: pointer; }
.wp-gate-row:last-child { border-bottom: none; }
.wp-gate-row:hover { background: var(--accent-dim); }
.wp-gr-dia { width: 10px; height: 10px; transform: rotate(45deg); border: 2px solid var(--dev); border-radius: 2px; background: var(--surface); flex-shrink: 0; margin-top: 4px; }
.wp-gr-dia.filled { background: var(--green); border-color: var(--green); }
.wp-gr-body { flex: 1; min-width: 0; }
.wp-gr-name { font-size: 12.5px; font-weight: 700; color: var(--text); }
.wp-gr-sub { font-family: "Nunito Sans", sans-serif; font-size: 10.5px; font-weight: 600; color: var(--text-dim); margin-top: 2px; }
.wp-gr-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.wp-gr-date { font-family: "Nunito Sans", sans-serif; font-size: 10.5px; font-weight: 600; color: var(--text-dim); white-space: nowrap; }
.wp-gr-badge { font-family: "Nunito Sans", sans-serif; font-size: 9px; font-weight: 800; letter-spacing: 0.5px; padding: 3px 8px; border-radius: 8px; background: var(--surface3); color: var(--text-dim); white-space: nowrap; }
.wp-gr-badge.ok { background: var(--green-dim); color: var(--green); }
/* team load & env coverage rows — bar mirrors the fin-bar-track pattern */
.wp-load-row { display: grid; grid-template-columns: 150px 1fr 64px; gap: 10px; align-items: center; padding: 9px 16px; border-bottom: 1px solid var(--border); }
.wp-load-row:last-of-type { border-bottom: none; }
.wp-load-name { display: flex; align-items: center; gap: 7px; font-size: 12.5px; font-weight: 700; color: var(--text-mid); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wp-load-name i { width: 7px; height: 7px; border-radius: 2px; flex-shrink: 0; }
.wp-load-track { height: 14px; background: var(--surface3); border-radius: 4px; overflow: hidden; display: flex; }
.wp-load-track i { display: block; height: 100%; }
.wp-load-total { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 800; text-align: right; color: var(--text-mid); white-space: nowrap; }
/* phase C.2 — slip scenario */
.wp-sim-ctrl { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; padding: 12px 16px; border-bottom: 1px solid var(--border); background: var(--surface2); }
.wp-sim-select { max-width: 240px; }
.wp-sim-days { width: 64px; }
.wp-btn { background: var(--accent); color: #fff; border: none; border-radius: 6px; padding: 6px 14px; font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 700; cursor: pointer; }
.wp-btn:hover { background: var(--accent); filter: brightness(0.92); }
.wp-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.wp-btn-ghost { background: transparent; border: 1px solid var(--border-bright); border-radius: 6px; padding: 6px 14px; font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 600; color: var(--text-mid); cursor: pointer; }
.wp-btn-ghost:hover { background: var(--surface2); }
.wp-sim-empty { padding: 14px 16px; font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 600; color: var(--text-dim); }
.wp-sim-out { padding: 12px 16px; font-size: 12.5px; color: var(--text-mid); }
.wp-sim-target { margin-bottom: 8px; }
.wp-sim-target b, .wp-sim-gate b { color: var(--text); }
.wp-sim-gate { padding: 8px 10px; background: var(--yellow-dim); border-radius: 6px; margin-bottom: 8px; }
.wp-sim-gate .bad { color: var(--red); }
.wp-sim-gate.none { background: var(--surface2); color: var(--text-dim); font-size: 11.5px; }
.wp-sim-list { display: flex; flex-direction: column; gap: 2px; }
.wp-sim-row { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 600; color: var(--text-dim); }
.wp-sim-row.more { font-weight: 800; }
/* phase C.2 — alerts (mirrors NotificationsView .notif-card: left severity stripe) */
.wp-alerts { grid-column: 1 / -1; }
.wp-al-none { padding: 18px 16px; text-align: center; font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 700; color: var(--text-dim); }
.wp-al-list { display: flex; flex-direction: column; gap: 8px; padding: 12px 16px; }
.wp-al-card { position: relative; overflow: hidden; display: flex; gap: 12px; align-items: flex-start; padding: 11px 14px 11px 18px; border: 1px solid var(--border); border-radius: 10px; background: var(--surface); }
.wp-al-card::before { content: ""; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; }
.wp-al-card.crit::before { background: var(--red); }
.wp-al-card.warn::before { background: var(--yellow); }
.wp-al-card.info::before { background: var(--accent); }
.wp-al-ico { font-size: 14px; flex-shrink: 0; margin-top: 1px; }
.wp-al-body { flex: 1; min-width: 0; }
.wp-al-title { font-size: 12.5px; font-weight: 800; color: var(--text); }
.wp-al-desc { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 600; color: var(--text-dim); margin-top: 2px; }
.wp-al-open { border: 1px solid var(--border-bright); background: var(--surface); border-radius: 5px; font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 800; padding: 3px 10px; color: var(--text-dim); cursor: pointer; flex-shrink: 0; }
.wp-al-open:hover { color: var(--text); border-color: var(--text-dim); }
</style>
