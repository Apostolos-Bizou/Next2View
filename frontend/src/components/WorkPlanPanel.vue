<template>
  <div v-if="project && project.workPlanEnabled" class="wp-panel" style="margin-top:14px;">
    <div class="wp-header">
      <div class="wp-title">📋 {{ t('workPlan.title') }}</div>
    </div>
    <div class="wp-scroll">
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
</style>
