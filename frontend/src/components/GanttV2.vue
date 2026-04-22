<template>
  <div v-if="hasContent" class="ganttv2-panel">
    <!-- Panel header with zoom controls -->
    <div class="ganttv2-ph">
      <div>
        <div class="ganttv2-ph-title">📊 Project Timeline</div>
        <div class="ganttv2-ph-sub">
          {{ project.title }} · {{ rangeLabel }} · {{ resolvedZoom.toUpperCase() }} view
        </div>
      </div>
      <div class="ganttv2-zoom-controls" role="tablist" aria-label="Zoom level">
        <button
          v-for="z in ['day','week','month','fit']"
          :key="z"
          :class="['ganttv2-zoom-btn', { active: zoom === z }]"
          @click="zoom = z"
          :title="z === 'fit' ? 'Auto-fit to project range' : 'Zoom: ' + z"
        >{{ z === 'fit' ? 'Fit' : z.charAt(0).toUpperCase() + z.slice(1) }}</button>
      </div>
    </div>

    <!-- Scrollable wrapper -->
    <div
      class="ganttv2-scroll"
      ref="scrollWrap"
      @wheel="onWheel"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <div class="ganttv2-table" :style="{ '--cell-w': cellWidth + 'px', '--total-w': totalWidth + 'px' }">

        <!-- HEADER ROW (sticky top) -->
        <div class="ganttv2-header">
          <div class="ganttv2-header-label">MODULE / TASK</div>
          <div class="ganttv2-header-cells" :style="{ minWidth: totalWidth + 'px' }">
            <!-- Top row (months or year) -->
            <div class="ganttv2-header-row top">
              <div
                v-for="(cell, i) in headerTop"
                :key="'t'+i"
                class="ganttv2-header-cell month"
                :style="{ minWidth: cell.width + 'px', width: cell.width + 'px' }"
              >{{ cell.label }}</div>
            </div>
            <!-- Bottom row (days, weeks, or months) -->
            <div class="ganttv2-header-row bottom">
              <div
                v-for="(cell, i) in headerBottom"
                :key="'b'+i"
                :class="['ganttv2-header-cell', { weekend: cell.weekend, today: cell.today }]"
                :style="{ minWidth: cell.width + 'px', width: cell.width + 'px' }"
              >
                <div>{{ cell.label }}</div>
                <div v-if="cell.sub" class="ganttv2-header-sub">{{ cell.sub }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- PROJECT summary row -->
        <div class="ganttv2-row project-row" :style="{ minWidth: (labelColWidth + totalWidth) + 'px' }">
          <div class="ganttv2-label">
            <span :class="`cat-pill ${project.category}`">{{ catIcon(project.category) }}</span>
            <span class="label-text" style="font-weight:900;">{{ project.title }}</span>
            <span class="ganttv2-pct-pill project-pct">{{ project.completion || 0 }}%</span>
          </div>
          <div class="ganttv2-timeline" :style="{ minWidth: totalWidth + 'px' }">
            <div v-if="todayX !== null" class="ganttv2-today-line" :style="{ left: todayX + 'px' }"></div>
            <div
              v-if="projectBar.show"
              class="ganttv2-project-bar"
              :style="{ left: projectBar.left + 'px', width: projectBar.width + 'px' }"
            >
              <div class="ganttv2-project-bar-fill" :style="{ width: (project.completion || 0) + '%' }"></div>
              <span class="ganttv2-project-bar-label">
                {{ fmtShort(rangeStart) }} → {{ fmtShort(rangeEnd) }}
              </span>
            </div>
          </div>
        </div>

        <!-- SPECS section -->
        <template v-if="specsWithDates.length">
          <div class="ganttv2-row module-row" :style="{ minWidth: (labelColWidth + totalWidth) + 'px' }">
            <div class="ganttv2-label indent-1">
              <span class="mod-dot" style="background: var(--legal);"></span>
              <span class="label-text">📋 Specifications</span>
              <span class="ganttv2-pct-pill">{{ specsCompletion }}%</span>
            </div>
            <div class="ganttv2-timeline" :style="{ minWidth: totalWidth + 'px' }">
              <div v-if="todayX !== null" class="ganttv2-today-line faded" :style="{ left: todayX + 'px' }"></div>
            </div>
          </div>
          <div
            v-for="s in specsWithDates"
            :key="'spec-'+s.id"
            class="ganttv2-row task-row"
            :style="{ minWidth: (labelColWidth + totalWidth) + 'px' }"
          >
            <div class="ganttv2-label indent-2">
              <div :class="['task-check', { done: s.isDone }]">{{ s.isDone ? '✓' : '' }}</div>
              <span :class="['label-text', { done: s.isDone }]" style="font-size:12px;">{{ s.description }}</span>
            </div>
            <div class="ganttv2-timeline" :style="{ minWidth: totalWidth + 'px' }">
              <div v-if="todayX !== null" class="ganttv2-today-line faded" :style="{ left: todayX + 'px' }"></div>
              <template v-if="specBar(s).show">
                <div
                  class="ganttv2-task-bar legal"
                  :class="{ done: s.isDone }"
                  :title="`${s.description} — ${s.startDate}${s.endDate ? ' to ' + s.endDate : ''}`"
                  :style="{ left: specBar(s).left + 'px', width: specBar(s).width + 'px' }"
                >
                  <span v-if="specBar(s).nameFits" class="bar-text">{{ s.description }}</span>
                </div>
                <span
                  v-if="!specBar(s).nameFits"
                  class="ganttv2-bar-overflow"
                  :style="{ left: (specBar(s).left + specBar(s).width) + 'px' }"
                >{{ s.description }}</span>
              </template>
            </div>
          </div>
        </template>

        <!-- MODULES + TASKS -->
        <template v-for="m in project.modules" :key="m.id">
          <!-- Module row -->
          <div
            class="ganttv2-row module-row"
            :style="{ minWidth: (labelColWidth + totalWidth) + 'px' }"
            @click="toggleMod(m.id)"
          >
            <div class="ganttv2-label indent-1">
              <span class="mod-dot" :style="{ background: `var(--${m.color || project.category})` }"></span>
              <span class="label-text">{{ m.name }}</span>
              <span
                class="ganttv2-pct-pill"
                :style="{ color: `var(--${m.color || project.category})` }"
              >{{ m.completion }}%</span>
            </div>
            <div class="ganttv2-timeline" :style="{ minWidth: totalWidth + 'px' }">
              <div v-if="todayX !== null" class="ganttv2-today-line faded" :style="{ left: todayX + 'px' }"></div>
              <div
                v-if="moduleBar(m).show"
                class="ganttv2-module-bracket"
                :style="{
                  left: moduleBar(m).left + 'px',
                  width: moduleBar(m).width + 'px',
                  background: `var(--${m.color || project.category})`
                }"
              ></div>
            </div>
          </div>

          <!-- Task rows -->
          <template v-if="!collapsedMods.has(m.id)">
            <div
              v-for="t in m.tasks"
              :key="t.id"
              class="ganttv2-row task-row"
              :style="{ minWidth: (labelColWidth + totalWidth) + 'px' }"
            >
              <div class="ganttv2-label indent-2">
                <div :class="['task-check', { done: t.isDone, blocked: t.isBlocked }]">
                  {{ t.isDone ? '✓' : '' }}
                </div>
                <span :class="['label-text', { done: t.isDone }]">{{ t.name }}</span>
                <span v-if="t.isBlocked" class="task-warn">⚠</span>
              </div>
              <div class="ganttv2-timeline" :style="{ minWidth: totalWidth + 'px' }">
                <div v-if="todayX !== null" class="ganttv2-today-line faded" :style="{ left: todayX + 'px' }"></div>
                <template v-if="taskBar(t).show">
                  <div
                    :class="[
                      'ganttv2-task-bar',
                      m.color || project.category,
                      { done: t.isDone, blocked: t.isBlocked }
                    ]"
                    :title="`${t.name} — ${t.progress || 0}%${t.startDate ? ' (' + t.startDate + (t.endDate ? ' → ' + t.endDate : '') + ')' : ''}${t.assignee ? ' — ' + t.assignee : ''}${t.blockNote ? ' — BLOCKED: ' + t.blockNote : ''}`"
                    @click.stop="emit('task-click', t.id)"
                    :style="{ left: taskBar(t).left + 'px', width: taskBar(t).width + 'px' }"
                  >
                    <div class="bar-progress" :style="{ width: (t.progress || 0) + '%' }"></div>
                    <div
                      v-if="barPastOverlay(t).show"
                      class="bar-past-overlay"
                      :style="{ width: barPastOverlay(t).width + '%' }"
                    ></div>
                    <span v-if="t.isBlocked" class="bar-warn">⚠</span>
                    <template v-if="taskBar(t).nameFits">
                      <span class="bar-text">{{ t.name }}</span>
                      <span class="bar-pct">{{ t.progress || 0 }}%</span>
                    </template>
                  </div>
                  <span
                    v-if="!taskBar(t).nameFits"
                    class="ganttv2-bar-overflow"
                    :style="{ left: (taskBar(t).left + taskBar(t).width) + 'px' }"
                  >
                    {{ t.name }} <span class="ovf-pct">· {{ t.progress || 0 }}%</span>
                  </span>
                </template>
              </div>
            </div>
          </template>
        </template>

      </div>
    </div>

    <!-- Legend strip -->
    <div class="ganttv2-strip">
      <div class="strip-item"><div class="strip-swatch" style="background:var(--dev);"></div>Dev</div>
      <div class="strip-item"><div class="strip-swatch" style="background:var(--finance);"></div>Finance</div>
      <div class="strip-item"><div class="strip-swatch" style="background:var(--legal);"></div>Legal</div>
      <div class="strip-item"><div class="strip-swatch" style="background:var(--marketing);"></div>Marketing</div>
      <div class="strip-item"><div class="strip-swatch" style="background:var(--red);"></div>Blocked</div>
      <div class="strip-divider"></div>
      <div class="strip-item"><span class="kbd">Ctrl</span>+scroll: zoom</div>
      <div class="strip-item">Touch: pinch to zoom</div>
      <div class="strip-item">Hover bars for details</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'

const props = defineProps({
  project: { type: Object, required: true }
})
const emit = defineEmits(['task-click'])

// ═══════════════════ STATE ═══════════════════
const zoom = ref('fit')
const collapsedMods = ref(new Set())
const scrollWrap = ref(null)

const LABEL_W = 300
const LABEL_W_MOBILE = 160
const labelColWidth = computed(() => window.innerWidth < 768 ? LABEL_W_MOBILE : LABEL_W)

const CELL_W = { day: 40, week: 80, month: 120 }

const MONTH_SHORT = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
const MONTH_FULL_EL = ['Ιανουάριος','Φεβρουάριος','Μάρτιος','Απρίλιος','Μάιος','Ιούνιος','Ιούλιος','Αύγουστος','Σεπτέμβριος','Οκτώβριος','Νοέμβριος','Δεκέμβριος']
const DAY_SHORT_EL = ['Κυ','Δε','Τρ','Τε','Πε','Πα','Σα']

// ═══════════════════ HELPERS ═══════════════════
const MS_DAY = 86400000
const parseDate = (v) => {
  if (!v) return null
  const d = new Date(v); d.setHours(0,0,0,0); return d
}
const addDays = (d, n) => { const r = new Date(d); r.setDate(r.getDate() + n); return r }
const daysBetween = (a, b) => Math.round((b - a) / MS_DAY)
const startOfWeek = (d) => {
  const r = new Date(d); const day = r.getDay()
  r.setDate(r.getDate() + (day === 0 ? -6 : 1 - day))
  r.setHours(0,0,0,0); return r
}
const startOfMonth = (d) => { const r = new Date(d); r.setDate(1); r.setHours(0,0,0,0); return r }
const daysInMonth = (d) => new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate()
const today = () => { const t = new Date(); t.setHours(0,0,0,0); return t }

const catIcon = (c) => ({ finance:'$', legal:'⚖', dev:'⌨', marketing:'◈' }[c] || '·')

const fmtShort = (d) => d.getDate() + ' ' + MONTH_SHORT[d.getMonth()]
const fmtLong = (d) => d.getDate() + ' ' + MONTH_SHORT[d.getMonth()] + ' ' + d.getFullYear()

// ═══════════════════ COMPUTED ═══════════════════
const hasContent = computed(() => {
  return (props.project?.modules?.length > 0) ||
         (props.project?.specs?.some(s => s.startDate))
})

const specsWithDates = computed(() => {
  return (props.project?.specs || []).filter(s => s.startDate)
})

const specsCompletion = computed(() => {
  const specs = specsWithDates.value
  if (!specs.length) return 0
  const done = specs.filter(s => s.isDone).length
  return Math.round((done / specs.length) * 100)
})

// Collect all dates for range calculation
const allDates = computed(() => {
  const dates = []
  const p = props.project
  if (!p) return dates

  if (p.startDate) dates.push(new Date(p.startDate))
  if (p.deadline) dates.push(new Date(p.deadline))

  ;(p.modules || []).forEach(m => {
    (m.tasks || []).forEach(t => {
      if (t.startDate) dates.push(new Date(t.startDate))
      if (t.endDate) dates.push(new Date(t.endDate))
      // Legacy: startDay / durationDays
      if (!t.startDate && t.startDay != null && p.startDate) {
        const ps = new Date(p.startDate)
        dates.push(addDays(ps, t.startDay))
        dates.push(addDays(ps, t.startDay + (t.durationDays || 1)))
      }
      // Legacy: startWeek / durationWeeks
      if (!t.startDate && t.startWeek != null && p.startDate) {
        const ps = new Date(p.startDate)
        dates.push(addDays(ps, (t.startWeek - 1) * 7))
        dates.push(addDays(ps, (t.startWeek - 1) * 7 + (t.durationWeeks || 1) * 7))
      }
    })
  })

  ;(p.specs || []).forEach(s => {
    if (s.startDate) dates.push(new Date(s.startDate))
    if (s.endDate) dates.push(new Date(s.endDate))
  })

  return dates
})

const rangeStart = computed(() => {
  const dates = allDates.value
  if (!dates.length) return today()
  let min = dates[0]
  dates.forEach(d => { if (d < min) min = d })
  // Pad 1 week, snap to Monday
  const pad = addDays(min, -7)
  return startOfWeek(pad)
})

const rangeEnd = computed(() => {
  const dates = allDates.value
  if (!dates.length) {
    const t = today(); return addDays(t, 84)
  }
  let max = dates[0]
  dates.forEach(d => { if (d > max) max = d })
  // Pad 2 weeks, align to end of week
  const pad = addDays(max, 14)
  return addDays(startOfWeek(pad), 7)
})

const spanDays = computed(() => {
  return Math.max(7, daysBetween(rangeStart.value, rangeEnd.value))
})

const resolvedZoom = computed(() => {
  if (zoom.value !== 'fit') return zoom.value
  const d = spanDays.value
  if (d <= 45) return 'day'
  if (d <= 240) return 'week'
  return 'month'
})

const cellWidth = computed(() => CELL_W[resolvedZoom.value] || 80)

const rangeLabel = computed(() => fmtLong(rangeStart.value) + ' → ' + fmtLong(rangeEnd.value))

// Pixel position for a given date
function dateToX(d) {
  if (!d) return null
  const days = daysBetween(rangeStart.value, d)
  switch (resolvedZoom.value) {
    case 'day':   return days * CELL_W.day
    case 'week':  return (days / 7) * CELL_W.week
    case 'month': {
      const rs = rangeStart.value
      const months = (d.getFullYear() - rs.getFullYear()) * 12 + (d.getMonth() - rs.getMonth())
      const frac = (d.getDate() - 1) / daysInMonth(d)
      return (months + frac) * CELL_W.month
    }
  }
  return 0
}

const totalWidth = computed(() => {
  return dateToX(rangeEnd.value)
})

const todayX = computed(() => {
  const t = today()
  if (t < rangeStart.value || t > rangeEnd.value) return null
  return dateToX(t)
})

// ═══════════════════ HEADER CELLS ═══════════════════
function getWeekNum(d) {
  const target = new Date(d); target.setHours(0,0,0,0)
  target.setDate(target.getDate() + 3 - (target.getDay() + 6) % 7)
  const week1 = new Date(target.getFullYear(), 0, 4)
  return 1 + Math.round(((target - week1) / MS_DAY - 3 + (week1.getDay() + 6) % 7) / 7)
}

const headerTop = computed(() => {
  const cells = []
  const z = resolvedZoom.value
  const rs = rangeStart.value, re = rangeEnd.value

  if (z === 'day') {
    let cursor = new Date(rs)
    let monthStart = cursor
    while (cursor < re) {
      const next = addDays(cursor, 1)
      const lastOfMonth = next.getMonth() !== cursor.getMonth() || next >= re
      if (lastOfMonth) {
        const days = daysBetween(monthStart, next)
        cells.push({
          label: MONTH_FULL_EL[monthStart.getMonth()] + ' ' + monthStart.getFullYear(),
          width: days * CELL_W.day
        })
        monthStart = next
      }
      cursor = next
    }
  } else if (z === 'week') {
    let cursor = startOfWeek(rs)
    let monthStart = cursor
    while (cursor < re) {
      const next = addDays(cursor, 7)
      const lastOfMonth = next.getMonth() !== cursor.getMonth() || next >= re
      if (lastOfMonth) {
        const weeks = Math.ceil(daysBetween(monthStart, next) / 7)
        cells.push({
          label: MONTH_FULL_EL[monthStart.getMonth()] + ' ' + monthStart.getFullYear(),
          width: weeks * CELL_W.week
        })
        monthStart = next
      }
      cursor = next
    }
  } else { // month
    let cursor = startOfMonth(rs)
    let yearStart = cursor
    while (cursor < re) {
      const next = new Date(cursor.getFullYear(), cursor.getMonth() + 1, 1)
      const lastOfYear = next.getFullYear() !== cursor.getFullYear() || next >= re
      if (lastOfYear) {
        const months = (next.getFullYear() - yearStart.getFullYear()) * 12 + (next.getMonth() - yearStart.getMonth())
        cells.push({
          label: String(yearStart.getFullYear()),
          width: months * CELL_W.month
        })
        yearStart = next
      }
      cursor = next
    }
  }
  return cells
})

const headerBottom = computed(() => {
  const cells = []
  const z = resolvedZoom.value
  const rs = rangeStart.value, re = rangeEnd.value
  const t = today()

  if (z === 'day') {
    let cursor = new Date(rs)
    while (cursor < re) {
      const isWeekend = cursor.getDay() === 0 || cursor.getDay() === 6
      const isToday = cursor.getTime() === t.getTime()
      cells.push({
        label: cursor.getDate(),
        sub: DAY_SHORT_EL[cursor.getDay()],
        width: CELL_W.day,
        weekend: isWeekend,
        today: isToday
      })
      cursor = addDays(cursor, 1)
    }
  } else if (z === 'week') {
    let cursor = startOfWeek(rs)
    while (cursor < re) {
      const weekEnd = addDays(cursor, 6)
      const isToday = t >= cursor && t <= weekEnd
      cells.push({
        label: cursor.getDate() + ' ' + MONTH_SHORT[cursor.getMonth()],
        sub: 'W' + getWeekNum(cursor),
        width: CELL_W.week,
        today: isToday
      })
      cursor = addDays(cursor, 7)
    }
  } else { // month
    let cursor = startOfMonth(rs)
    while (cursor < re) {
      const isToday = t.getMonth() === cursor.getMonth() && t.getFullYear() === cursor.getFullYear()
      cells.push({
        label: MONTH_SHORT[cursor.getMonth()],
        width: CELL_W.month,
        today: isToday
      })
      cursor = new Date(cursor.getFullYear(), cursor.getMonth() + 1, 1)
    }
  }
  return cells
})

// ═══════════════════ BAR CALCULATIONS ═══════════════════
const MIN_BAR_PX = 32


// ─── Past overlay: compute how much of this task's bar is in the past ───
function barPastOverlay(t) {
  const dates = resolveTaskDates(t)
  if (!dates) return { show: false }
  const todayMs = today().getTime()
  // No overlay if today is before the task started
  if (todayMs <= dates.s) return { show: false }
  // Full overlay if today is after the task ended
  if (todayMs >= dates.e) return { show: true, width: 100 }
  // Partial overlay: percentage of elapsed portion
  const total = dates.e - dates.s
  const elapsed = todayMs - dates.s
  return { show: true, width: (elapsed / total) * 100 }
}

function computeBar(taskStartMs, taskEndMs, nameText) {
  const rsMs = rangeStart.value.getTime()
  const reMs = rangeEnd.value.getTime()
  if (taskEndMs < rsMs || taskStartMs > reMs) return { show: false }

  const s = Math.max(taskStartMs, rsMs)
  const e = Math.min(taskEndMs, reMs)
  const left = dateToX(new Date(s))
  const endX = dateToX(new Date(e))
  const rawWidth = endX - left
  const width = Math.max(MIN_BAR_PX, rawWidth)
  // Heuristic: does the name fit inside the bar?
  const nameLen = (nameText || '').length
  const nameFits = width >= (nameLen * 6.5 + 36)
  return { show: true, left, width, nameFits }
}

function resolveTaskDates(t) {
  if (t.startDate) {
    const s = parseDate(t.startDate).getTime()
    const e = t.endDate ? parseDate(t.endDate).getTime() : s + 7 * MS_DAY
    return { s, e }
  }
  if (t.startDay != null && t.durationDays != null && props.project?.startDate) {
    const ps = parseDate(props.project.startDate).getTime()
    return { s: ps + t.startDay * MS_DAY, e: ps + (t.startDay + (t.durationDays || 1)) * MS_DAY }
  }
  if (t.startWeek != null && props.project?.startDate) {
    const ps = parseDate(props.project.startDate).getTime()
    return { s: ps + (t.startWeek - 1) * 7 * MS_DAY, e: ps + ((t.startWeek - 1) + (t.durationWeeks || 1)) * 7 * MS_DAY }
  }
  return null
}

function taskBar(t) {
  const dates = resolveTaskDates(t)
  if (!dates) return { show: false }
  return computeBar(dates.s, dates.e, t.name)
}

function specBar(s) {
  if (!s.startDate) return { show: false }
  const start = parseDate(s.startDate).getTime()
  const end = s.endDate ? parseDate(s.endDate).getTime() : start + MS_DAY
  return computeBar(start, end, s.description)
}

function moduleBar(m) {
  const ranges = (m.tasks || []).map(resolveTaskDates).filter(Boolean)
  if (!ranges.length) return { show: false }
  const minS = Math.min(...ranges.map(r => r.s))
  const maxE = Math.max(...ranges.map(r => r.e))
  if (maxE < rangeStart.value.getTime() || minS > rangeEnd.value.getTime()) return { show: false }
  const left = dateToX(new Date(Math.max(minS, rangeStart.value.getTime())))
  const right = dateToX(new Date(Math.min(maxE, rangeEnd.value.getTime())))
  return { show: true, left, width: Math.max(4, right - left) }
}

const projectBar = computed(() => {
  const p = props.project
  if (!p?.startDate) return { show: false }
  const s = parseDate(p.startDate).getTime()
  const e = p.deadline ? parseDate(p.deadline).getTime() : s + 30 * MS_DAY
  const rsMs = rangeStart.value.getTime()
  const reMs = rangeEnd.value.getTime()
  if (e < rsMs || s > reMs) return { show: false }
  const left = dateToX(new Date(Math.max(s, rsMs)))
  const right = dateToX(new Date(Math.min(e, reMs)))
  return { show: true, left, width: Math.max(20, right - left) }
})

// ═══════════════════ INTERACTIONS ═══════════════════
function toggleMod(id) {
  const s = new Set(collapsedMods.value)
  if (s.has(id)) s.delete(id); else s.add(id)
  collapsedMods.value = s
}

function onWheel(ev) {
  if (!(ev.ctrlKey || ev.metaKey)) return
  ev.preventDefault()
  const levels = ['day','week','month']
  const cur = zoom.value === 'fit' ? resolvedZoom.value : zoom.value
  let idx = levels.indexOf(cur)
  if (ev.deltaY > 0 && idx < 2) idx++
  else if (ev.deltaY < 0 && idx > 0) idx--
  else return
  zoom.value = levels[idx]
}

let touchDist = 0
let touchStartZoom = null
function onTouchStart(ev) {
  if (ev.touches.length === 2) {
    const dx = ev.touches[0].clientX - ev.touches[1].clientX
    const dy = ev.touches[0].clientY - ev.touches[1].clientY
    touchDist = Math.sqrt(dx*dx + dy*dy)
    touchStartZoom = zoom.value === 'fit' ? resolvedZoom.value : zoom.value
  }
}
function onTouchMove(ev) {
  if (ev.touches.length === 2 && touchDist > 0) {
    const dx = ev.touches[0].clientX - ev.touches[1].clientX
    const dy = ev.touches[0].clientY - ev.touches[1].clientY
    const dist = Math.sqrt(dx*dx + dy*dy)
    const ratio = dist / touchDist
    const levels = ['month','week','day']
    const idx = levels.indexOf(touchStartZoom)
    let newIdx = idx
    if (ratio > 1.3) newIdx = Math.min(idx + 1, 2)
    else if (ratio < 0.77) newIdx = Math.max(idx - 1, 0)
    if (newIdx !== idx && zoom.value !== levels[newIdx]) {
      zoom.value = levels[newIdx]
    }
  }
}
function onTouchEnd() {
  touchDist = 0; touchStartZoom = null
}

// Scroll to today on mount
onMounted(async () => {
  await nextTick()
  if (todayX.value !== null && scrollWrap.value) {
    scrollWrap.value.scrollLeft = Math.max(0, todayX.value - 240)
  }
})
</script>

<style scoped>
/* ════════ PANEL ════════ */
.ganttv2-panel {
  background: var(--surface, #ffffff);
  border: 1px solid var(--border, #e6eaf0);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  margin-bottom: 14px;
}
.ganttv2-ph {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
  background: var(--surface2, #fafbfc);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.ganttv2-ph-title { font-size: 14px; font-weight: 800; color: var(--text); }
.ganttv2-ph-sub {
  font-size: 11px;
  color: var(--text-dim);
  font-family: "Nunito Sans", sans-serif;
  margin-top: 3px;
  font-weight: 600;
}

/* ════════ ZOOM CONTROLS ════════ */
.ganttv2-zoom-controls {
  display: flex;
  gap: 2px;
  background: var(--surface3, #f1f4f9);
  border: 1px solid var(--border);
  border-radius: 7px;
  padding: 3px;
}
.ganttv2-zoom-btn {
  font-family: "Nunito Sans", sans-serif;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1px;
  text-transform: uppercase;
  padding: 6px 12px;
  border: none;
  background: transparent;
  color: var(--text-dim);
  cursor: pointer;
  border-radius: 5px;
  transition: all 0.12s;
}
.ganttv2-zoom-btn:hover { color: var(--text); }
.ganttv2-zoom-btn.active {
  background: var(--text, #0b1220);
  color: var(--surface);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

/* ════════ SCROLL + TABLE ════════ */
.ganttv2-scroll {
  overflow-x: auto;
  overflow-y: auto;
  max-height: 70vh;
  -webkit-overflow-scrolling: touch;
  touch-action: pan-x pan-y;
}
.ganttv2-scroll::-webkit-scrollbar { height: 10px; width: 10px; }
.ganttv2-scroll::-webkit-scrollbar-track { background: var(--surface3); }
.ganttv2-scroll::-webkit-scrollbar-thumb { background: var(--border-bright, #d5dbe5); border-radius: 5px; }

.ganttv2-table { display: flex; flex-direction: column; }

/* ════════ HEADER ROW ════════ */
.ganttv2-header {
  position: sticky; top: 0; z-index: 6;
  background: var(--surface2);
  border-bottom: 2px solid var(--border);
  display: flex;
  min-height: 64px;
}
.ganttv2-header-label {
  position: sticky; left: 0; z-index: 7;
  width: 300px; min-width: 300px;
  padding: 10px 14px;
  background: var(--surface2);
  border-right: 2px solid var(--border);
  display: flex;
  align-items: flex-end;
  font-family: "Nunito Sans", sans-serif;
  font-size: 9px;
  letter-spacing: 2px;
  color: var(--text-dim);
  font-weight: 800;
  text-transform: uppercase;
  box-shadow: 3px 0 6px -3px rgba(0,0,0,0.06);
  flex-shrink: 0;
}
.ganttv2-header-cells { display: flex; flex-direction: column; flex: 1; }
.ganttv2-header-row {
  display: flex;
  border-bottom: 1px solid var(--border);
  flex: 1;
}
.ganttv2-header-row:last-child { border-bottom: none; }
.ganttv2-header-cell {
  border-right: 1px solid var(--border);
  padding: 4px 6px;
  text-align: center;
  font-family: "Nunito Sans", sans-serif;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-mid, #374151);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--surface2);
  box-sizing: border-box;
}
.ganttv2-header-cell.month {
  font-size: 13px;
  font-weight: 800;
  color: var(--text);
  background: var(--surface3);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.ganttv2-header-cell.weekend {
  background: rgba(220,38,38,0.03);
  color: var(--text-dim);
}
.ganttv2-header-cell.today {
  background: var(--accent-dim, #dbeafe);
  color: var(--accent, #3b82f6);
  font-weight: 900;
}
.ganttv2-header-sub {
  font-size: 9px;
  color: var(--text-dim);
  font-weight: 600;
  margin-top: 2px;
}

/* ════════ ROWS ════════ */
.ganttv2-row {
  display: flex;
  border-bottom: 1px solid var(--border);
  min-height: 52px;
}
.ganttv2-row.project-row {
  background: linear-gradient(90deg, #0b1a33 0%, #12264a 100%);
  color: #fff;
  min-height: 62px;
  border-bottom: 2px solid var(--border);
}
.ganttv2-row.project-row .ganttv2-label {
  background: #0b1a33;
  color: #fff;
  border-right-color: #1e3556;
}
.ganttv2-row.module-row {
  background: var(--surface3);
  cursor: pointer;
  min-height: 48px;
}
.ganttv2-row.module-row:hover { background: #e9edf4; }
.ganttv2-row.module-row .ganttv2-label { background: var(--surface3); }
.ganttv2-row.module-row:hover .ganttv2-label { background: #e9edf4; }
.ganttv2-row.task-row:hover { background: rgba(59,130,246,0.04); }

.ganttv2-label {
  position: sticky; left: 0; z-index: 5;
  width: 300px; min-width: 300px;
  padding: 8px 14px;
  background: var(--surface);
  border-right: 2px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  font-size: 14px;
  box-shadow: 3px 0 6px -3px rgba(0,0,0,0.06);
  box-sizing: border-box;
}
.ganttv2-label.indent-1 { padding-left: 24px; font-size: 13px; font-weight: 700; }
.ganttv2-label.indent-2 { padding-left: 44px; font-size: 13px; color: var(--text-mid); font-weight: 500; }

.cat-pill {
  width: 26px; height: 26px;
  border-radius: 5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.cat-pill.finance { background: var(--finance-dim, #ecfdf5); color: var(--finance, #059669); }
.cat-pill.legal { background: var(--legal-dim, #fffbeb); color: var(--legal, #d97706); }
.cat-pill.dev { background: var(--dev-dim, #f5f3ff); color: var(--dev, #7c3aed); }
.cat-pill.marketing { background: var(--marketing-dim, #fdf2f8); color: var(--marketing, #db2777); }

.ganttv2-row.project-row .cat-pill {
  background: rgba(255,255,255,0.15);
  color: #fff;
}

.mod-dot {
  width: 9px; height: 9px;
  border-radius: 50%;
  flex-shrink: 0;
}
.task-check {
  width: 16px; height: 16px;
  border-radius: 3px;
  border: 1.5px solid var(--border-bright);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 8px;
  background: var(--surface);
  color: transparent;
}
.task-check.done {
  background: var(--green, #059669);
  border-color: var(--green, #059669);
  color: #fff;
}
.task-check.blocked {
  background: var(--red-dim, #fee2e2);
  border-color: var(--red, #dc2626);
}
.task-warn { font-size: 12px; color: var(--red); flex-shrink: 0; }

.label-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.label-text.done {
  color: var(--text-dim);
  text-decoration: line-through;
}

.ganttv2-pct-pill {
  font-family: "Nunito Sans", sans-serif;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 7px;
  border-radius: 10px;
  background: var(--surface3);
  color: var(--text-dim);
  flex-shrink: 0;
}
.ganttv2-pct-pill.project-pct {
  background: rgba(255,255,255,0.15);
  color: #fff;
}

/* ════════ TIMELINE AREA ════════ */
.ganttv2-timeline {
  flex: 1;
  position: relative;
  background-image: repeating-linear-gradient(
    90deg,
    transparent 0,
    transparent calc(var(--cell-w) - 1px),
    rgba(0,0,0,0.04) calc(var(--cell-w) - 1px),
    rgba(0,0,0,0.04) var(--cell-w)
  );
}
.ganttv2-row.project-row .ganttv2-timeline {
  background-image: repeating-linear-gradient(
    90deg,
    transparent 0,
    transparent calc(var(--cell-w) - 1px),
    rgba(255,255,255,0.08) calc(var(--cell-w) - 1px),
    rgba(255,255,255,0.08) var(--cell-w)
  );
}

/* ════════ TODAY LINE ════════ */
.ganttv2-today-line {
  position: absolute; top: 0; bottom: 0;
  width: 2px;
  background: var(--red, #dc2626);
  z-index: 3;
  pointer-events: none;
  box-shadow: 0 0 8px rgba(220,38,38,0.4);
}
.ganttv2-today-line.faded { opacity: 0.3; }

/* ════════ BARS ════════ */
.ganttv2-task-bar {
  position: absolute; top: 50%; transform: translateY(-50%);
  height: 32px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  padding: 0 8px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  cursor: pointer;
  z-index: 2;
  overflow: hidden;
  transition: transform 0.12s, box-shadow 0.12s;
}
.ganttv2-task-bar:hover {
  transform: translateY(-50%) scaleY(1.08);
  box-shadow: 0 4px 12px rgba(0,0,0,0.18);
  z-index: 4;
}
.ganttv2-task-bar.finance { background: linear-gradient(90deg, #059669, #10b981); }
.ganttv2-task-bar.legal { background: linear-gradient(90deg, #d97706, #f59e0b); }
.ganttv2-task-bar.dev { background: linear-gradient(90deg, #7c3aed, #8b5cf6); }
.ganttv2-task-bar.marketing { background: linear-gradient(90deg, #db2777, #ec4899); }
.ganttv2-task-bar.done {
  filter: saturate(0.4) brightness(1.1);
  opacity: 0.72;
}
.ganttv2-task-bar.blocked {
  background: linear-gradient(90deg, #dc2626, #ef4444) !important;
  animation: pulse-blocked 2s infinite;
}
@keyframes pulse-blocked {
  0%,100% { box-shadow: 0 0 0 0 rgba(220,38,38,0.5); }
  50%     { box-shadow: 0 0 0 4px rgba(220,38,38,0); }
}
.ganttv2-task-bar .bar-progress {
  position: absolute; left: 0; top: 0; bottom: 0;
  background: rgba(255,255,255,0.28);
  border-radius: 5px 0 0 5px;
  pointer-events: none;
}
.ganttv2-task-bar .bar-text {
  position: relative; z-index: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.ganttv2-task-bar .bar-pct {
  position: relative; z-index: 2;
  margin-left: 6px;
  font-size: 10px;
  opacity: 0.9;
  font-family: "Nunito Sans", sans-serif;
  flex-shrink: 0;
}
.ganttv2-task-bar .bar-warn {
  margin-right: 5px;
  font-size: 12px;
  flex-shrink: 0;
}

/* Overflow label for short bars */
.ganttv2-bar-overflow {
  position: absolute; top: 50%; transform: translateY(-50%);
  padding-left: 7px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text);
  white-space: nowrap;
  pointer-events: none;
  cursor: pointer;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  z-index: 1;
}
.ganttv2-bar-overflow .ovf-pct {
  color: var(--text-dim);
  font-family: "Nunito Sans", sans-serif;
  font-size: 11px;
  font-weight: 700;
}

/* Module bracket */
.ganttv2-module-bracket {
  position: absolute; top: 50%; transform: translateY(-50%);
  height: 8px;
  border-radius: 3px;
  opacity: 0.45;
  z-index: 1;
}

/* Project summary bar */
.ganttv2-project-bar {
  position: absolute; top: 50%; transform: translateY(-50%);
  height: 22px;
  border-radius: 4px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.3);
  overflow: hidden;
  z-index: 1;
}
.ganttv2-project-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #60a5fa, #3b82f6);
  border-radius: 3px 0 0 3px;
}
.ganttv2-project-bar-label {
  position: absolute;
  top: 50%; left: 8px;
  transform: translateY(-50%);
  font-family: "Nunito Sans", sans-serif;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 1px;
  color: rgba(255,255,255,0.95);
  text-transform: uppercase;
  pointer-events: none;
  white-space: nowrap;
  z-index: 2;
}

/* ════════ LEGEND STRIP ════════ */
.ganttv2-strip {
  padding: 10px 20px;
  background: var(--surface2);
  border-top: 1px solid var(--border);
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
  font-size: 11px;
  color: var(--text-dim);
  font-weight: 600;
}
.strip-item { display: flex; align-items: center; gap: 6px; }
.strip-swatch { width: 14px; height: 10px; border-radius: 3px; }
.strip-divider { width: 1px; height: 12px; background: var(--border-bright); }
.kbd {
  font-family: "Nunito Sans", sans-serif;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 3px;
  background: var(--surface);
  border: 1px solid var(--border-bright);
  color: var(--text-mid);
}

/* ════════ MOBILE ════════ */
@media (max-width: 768px) {
  .ganttv2-ph {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .ganttv2-zoom-controls { align-self: flex-start; }
  .ganttv2-zoom-btn {
    padding: 5px 9px;
    font-size: 9px;
  }
  .ganttv2-label,
  .ganttv2-header-label {
    width: 160px;
    min-width: 160px;
    padding: 8px 10px;
  }
  .ganttv2-label.indent-1 { padding-left: 16px; }
  .ganttv2-label.indent-2 { padding-left: 28px; }
  .ganttv2-task-bar { font-size: 10px; height: 20px; padding: 0 6px; }
  .ganttv2-strip { font-size: 10px; gap: 10px; }
}


/* Task highlight flash after click */
@keyframes highlight-flash {
  0%   { background: rgba(251, 191, 36, 0.0); }
  20%  { background: rgba(251, 191, 36, 0.35); }
  100% { background: rgba(251, 191, 36, 0.0); }
}
.row-highlight-flash {
  animation: highlight-flash 2.5s ease-out;
}



/* Past-portion overlay for task bars (dichromy) */
.bar-past-overlay {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.55) 0%,
    rgba(255, 255, 255, 0.45) 85%,
    rgba(255, 255, 255, 0.25) 100%
  );
  pointer-events: none;
  z-index: 1;
  border-radius: inherit;
}
/* Ensure bar content stays above the past overlay */
.ganttv2-task-bar .bar-text,
.ganttv2-task-bar .bar-pct,
.ganttv2-task-bar .bar-warn {
  z-index: 3;
  position: relative;
}
/* Slight darken for completed tasks' past overlay (cleaner look) */
.ganttv2-task-bar.done .bar-past-overlay {
  background: linear-gradient(90deg, rgba(255,255,255,0.35), rgba(255,255,255,0.2));
}

</style>