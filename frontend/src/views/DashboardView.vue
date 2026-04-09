<template>
  <div class="content">

    <!-- STALE BANNER -->
    <div v-if="store.atRisk.length" class="stale-banner">
      <span>⚠</span>
      <span class="stale-txt">
        <strong>{{ store.atRisk.length }} projects</strong> χρειάζονται προσοχή —
        {{ store.atRisk.map(p => p.title).join(", ") }}
      </span>
    </div>

    <!-- KPI STRIP -->
    <div class="kpi-strip">
      <div class="kpi total">
        <div class="kpi-lbl">Overall</div>
        <div class="kpi-val">{{ store.overallCompletion }}%</div>
        <div class="kpi-sub">{{ visibleProjects.length }} projects</div>
      </div>
      <div v-for="cat in visibleCategories" :key="cat.key" :class="`kpi ${cat.key}`">
        <div class="kpi-lbl">{{ cat.label }}</div>
        <div class="kpi-val">{{ catCompletion(cat.key) }}%</div>
        <div class="kpi-sub">{{ visibleProjects.filter(p => p.category === cat.key).length }} projects</div>
      </div>
      <div class="kpi alert">
        <div class="kpi-lbl">At Risk</div>
        <div class="kpi-val">{{ store.atRisk.length }}</div>
        <div class="kpi-sub">need action</div>
      </div>
    </div>

    <div class="g2">
      <!-- COMPANIES -->
      <div class="panel">
        <div class="ph">
          <div class="ph-title">Εταιρείες</div>
          <div class="ph-badge badge blue">{{ store.companies.filter(co => visibleProjects.some(p => p.companyId === co.id)).length }} entities</div>
        </div>
        <div class="pb">
          <div v-for="co in store.companies.filter(co => visibleProjects.some(p => p.companyId === co.id))" :key="co.id" class="co-row"
            @click="router.push(`/projects?companyId=${co.id}`)">
            <div class="co-av" :style="`color:${co.color};background:${co.color}15;`">{{ co.code }}</div>
            <div class="co-info">
              <div class="co-name">{{ coShort(co.name) }}</div>
              <div class="co-meta">{{ co.projectCount }} projects</div>
            </div>
            <div class="co-barw">
              <div class="co-bar">
                <div :style="`width:${co.avgCompletion}%;height:100%;background:${co.color};border-radius:2px;`"></div>
              </div>
            </div>
            <div class="co-pct" :style="`color:${co.color};`">{{ co.avgCompletion }}%</div>
          </div>
        </div>
      </div>

      <!-- RIGHT COLUMN -->
      <div>
        <!-- CAT BREAKDOWN -->
        <div class="panel" style="margin-bottom:14px;">
          <div class="ph"><div class="ph-title">Ανά Κατηγορία</div></div>
          <div class="pb">
            <div class="g4">
              <div v-for="cat in visibleCategories" :key="cat.key" class="cat-blk">
                <div :class="`cat-blk-lbl ${cat.key}`">{{ cat.icon }} {{ cat.label }}</div>
                <div :class="`cat-blk-pct ${cat.key}`">{{ catCompletion(cat.key) }}%</div>
                <div class="cat-blk-sub">
                  {{ visibleProjects.filter(p => p.category === cat.key).reduce((a,p)=>a+p.tasksDone,0) }}/
                  {{ visibleProjects.filter(p => p.category === cat.key).reduce((a,p)=>a+p.tasksTotal,0) }} tasks
                </div>
                <div class="cat-big-bar">
                  <div class="cat-big-fill" :style="`width:${catCompletion(cat.key)}%;background:var(--${cat.key});`"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- DEADLINES + ACTIVITY -->
        <div class="g2" style="margin-bottom:0;">
          <div class="panel" style="margin-bottom:0;">
            <div class="ph">
              <div class="ph-title">Deadlines</div>
              <div class="ph-badge badge red">{{ urgentCount }} urgent</div>
            </div>
            <div class="pb">
              <div v-for="p in upcomingDeadlines" :key="p.id" class="dl-row"
                @click="router.push(`/projects/${p.id}`)">
                <div class="dl-date">{{ formatDate(p.deadline) }}</div>
                <div style="flex:1;">
                  <div class="dl-label">{{ p.title }}</div>
                  <div class="dl-co">{{ p.companyName }}</div>
                </div>
                <div :class="`dl-days ${daysClass(p.deadline)}`">{{ daysLeft(p.deadline) }}d</div>
              </div>
              <div v-if="!upcomingDeadlines.length" class="empty-mini">Δεν υπάρχουν deadlines.</div>
            </div>
          </div>

          <div class="panel" style="margin-bottom:0;">
            <div class="ph"><div class="ph-title">Activity</div></div>
            <div class="pb">
              <div v-for="p in recentActivity" :key="p.id" class="act-item">
                <div class="act-dot" :style="`background:var(--${p.category});`"></div>
                <div class="act-txt">
                  <strong>{{ p.companyName }}</strong> — {{ p.title }}
                  <span :style="`color:var(--${p.category});font-weight:700;`"> {{ p.completion }}%</span>
                </div>
                <div class="act-time">{{ formatAgo(p.updatedAgo) }}</div>
              </div>
              <div v-if="!recentActivity.length" class="empty-mini">Δεν υπάρχει activity.</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ════ GANTT TIMELINE ════ -->
    <div class="gantt-panel" style="margin-top:14px;">
      <div class="gantt-ph">
        <div class="gantt-ph-title">📊 Project Timeline — All Active</div>
        <div style="display:flex;gap:8px;align-items:center;">
          <select v-model="ganttFilter" class="gantt-select">
            <option value="">Όλες κατηγορίες</option>
            <option value="finance">$ Finance</option>
            <option value="legal">⚖ Legal</option>
            <option value="dev">⌨ Developing</option>
            <option value="marketing">◈ Marketing</option>
          </select>
          <div class="ph-badge badge blue">{{ ganttProjects.length }} projects</div>
        </div>
      </div>
      <div class="gantt-scroll">
        <!-- WEEK HEADERS -->
        <div class="gantt-header">
          <div class="gantt-lbl-col">PROJECT</div>
          <div class="gantt-weeks-row">
            <div v-for="w in ganttWeeks" :key="w.num"
              :class="['gantt-wk-hd', { 'gantt-wk-today': w.isCurrentWeek }]">
              <div class="gantt-wk-num">W{{ w.num }}</div>
              <div class="gantt-wk-date">{{ w.dateLabel }}</div>
            </div>
          </div>
        </div>
        <!-- PROJECT ROWS -->
        <div v-if="!ganttProjects.length" class="gantt-empty">Δεν υπάρχουν projects με deadline.</div>
        <div v-for="p in ganttProjects" :key="p.id" class="gantt-proj-row gantt-proj-row-click"
          @click="router.push(`/projects/${p.id}`)">
          <div class="gantt-proj-lbl">
            <span :class="`cat-dot ${p.category}`"></span>
            <div>
              <div class="gantt-p-name">{{ p.title }}</div>
              <div class="gantt-p-co" :style="`color:var(--${p.category});`">{{ p.companyName }}</div>
            </div>
            <span class="gantt-p-pct" :style="`color:var(--${p.category});`">{{ p.completion }}%</span>
          </div>
          <div class="gantt-track">
            <div class="gantt-today-line" :style="`left:${todayPct}%`"></div>
            <div v-if="dashBarStyle(p).show" class="gantt-task-bar"
              :style="`left:${dashBarStyle(p).left}%;width:${dashBarStyle(p).width}%;background:var(--${p.category});opacity:0.85;`">
              <div class="gantt-task-fill" :style="`width:${p.completion}%;background:rgba(255,255,255,0.3);`"></div>
              <span class="gantt-task-label">{{ p.title }} · {{ p.completion }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useProjectStore } from "@/stores/projects";
import { usePermissionStore } from "@/stores/permissions";

const store = useProjectStore();
const permStore = usePermissionStore();
const router = useRouter();
const ganttFilter = ref("");

const visibleProjects = computed(() =>
  store.projects.filter(p => permStore.canViewCategory(p.category))
);

const categories = [
  { key: "finance",   label: "Finance",    icon: "$" },
  { key: "legal",     label: "Legal",      icon: "⚖" },
  { key: "dev",       label: "Developing", icon: "⌨" },
  { key: "marketing", label: "Marketing",  icon: "◈" },
];

const catCompletion = (cat) => {
  const ps = visibleProjects.value.filter(p => p.category === cat);
  if (!ps.length) return 0;
  return Math.round(ps.reduce((s, p) => s + p.completion, 0) / ps.length);
};

const upcomingDeadlines = computed(() =>
  [...visibleProjects.value].filter(p => p.deadline)
    .sort((a, b) => new Date(a.deadline) - new Date(b.deadline)).slice(0, 5)
);
const urgentCount = computed(() =>
  upcomingDeadlines.value.filter(p => daysLeft(p.deadline) < 7).length
);
const recentActivity = computed(() =>
  [...visibleProjects.value].sort((a, b) => a.updatedAgo - b.updatedAgo).slice(0, 5)
);

// ════ GANTT ════
const GANTT_WEEKS = 12
const ganttStart = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() - d.getDay()) // start of current week
  return d
})
const ganttEnd = computed(() => {
  const d = new Date(ganttStart.value)
  d.setDate(d.getDate() + GANTT_WEEKS * 7)
  return d
})
const totalDays = computed(() => (ganttEnd.value - ganttStart.value) / 86400000)

const ganttWeeks = computed(() => {
  const weeks = []
  const months = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"]
  for (let i = 0; i < GANTT_WEEKS; i++) {
    const d = new Date(ganttStart.value)
    d.setDate(d.getDate() + i * 7)
    const isToday = i === 0
    weeks.push({ num: i + 1, dateLabel: `${d.getDate()} ${months[d.getMonth()]}`, isCurrentWeek: isToday })
  }
  return weeks
})

const todayPct = computed(() => {
  const now = new Date()
  return Math.min(100, Math.max(0, (now - ganttStart.value) / (ganttEnd.value - ganttStart.value) * 100))
})

const ganttProjects = computed(() => {
  let ps = visibleProjects.value.filter(p => p.deadline)
  if (ganttFilter.value) ps = ps.filter(p => p.category === ganttFilter.value)
  return ps.sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
})

function barStyle(p) {
  const end = new Date(p.deadline)
  const start = new Date(end)
  start.setDate(start.getDate() - 28)
  const gs = ganttStart.value.getTime()
  const ge = ganttEnd.value.getTime()
  const range = ge - gs
  const left = Math.max(0, (start.getTime() - gs) / range * 100)
  const right = Math.min(100, (end.getTime() - gs) / range * 100)
  const width = Math.max(2, right - left)
  return `left:${left}%;width:${width}%;`
}

function dashBarStyle(p) {
  const gs = ganttStart.value.getTime()
  const ge = ganttEnd.value.getTime()
  const range = ge - gs
  const startRaw = p.startDate ? new Date(p.startDate).getTime() : gs
  const endRaw = p.deadline ? new Date(p.deadline).getTime() : ge
  const left = Math.max(0, (startRaw - gs) / range * 100)
  const right = Math.min(100, (endRaw - gs) / range * 100)
  const width = Math.max(2, right - left)
  if (right <= 0 || left >= 100) return { show: false }
  return { show: true, left, width }
}

function coShort(name) {
  const m = { 'Polaris Financial Services':'Polaris Financial', 'Crossworld Marine Services':'Crossworld Marine', 'WiMAS Training Center':'WiMAS', 'Varship Management':'Varship' }
  return m[name] || name.split(' ').slice(0,2).join(' ')
}
function daysLeft(deadline) {
  return Math.round((new Date(deadline) - new Date()) / 86400000)
}
function daysClass(deadline) {
  const d = daysLeft(deadline)
  return d < 7 ? "u" : d < 14 ? "w" : "ok"
}
function formatDate(iso) {
  if (!iso) return "—"
  const d = new Date(iso)
  const m = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"]
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`
}
function formatAgo(mins) {
  if (!mins) return "now"
  if (mins < 60) return `${mins}m`
  if (mins < 1440) return `${Math.round(mins/60)}h`
  return `${Math.round(mins/1440)}d`
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.stale-banner { background: var(--red-dim); border: 1px solid rgba(220,38,38,0.2); border-radius: 8px; padding: 13px 18px; margin-bottom: 18px; display: flex; align-items: center; gap: 12px; font-size: 13px; font-weight: 600; }
.stale-txt { color: var(--red); flex: 1; }
.kpi-strip { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-bottom: 20px; }
.kpi { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 20px 16px; position: relative; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.05); display: flex; flex-direction: column; align-items: center; text-align: center; }
.kpi::before { content: ""; position: absolute; top: 0; left: 0; right: 0; height: 3px; }
.kpi.total::before    { background: var(--accent); }
.kpi.finance::before  { background: var(--finance); }
.kpi.legal::before    { background: var(--legal); }
.kpi.dev::before      { background: var(--dev); }
.kpi.marketing::before{ background: var(--marketing); }
.kpi.alert::before    { background: var(--red); }
.kpi-lbl { font-family: "Nunito Sans", sans-serif; font-size: 9px; letter-spacing: 2px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 8px; font-weight: 700; }
.kpi-val { font-size: 36px; font-weight: 900; line-height: 1; margin-bottom: 6px; }
.kpi.total .kpi-val    { color: var(--accent); }
.kpi.finance .kpi-val  { color: var(--finance); }
.kpi.legal .kpi-val    { color: var(--legal); }
.kpi.dev .kpi-val      { color: var(--dev); }
.kpi.marketing .kpi-val{ color: var(--marketing); }
.kpi.alert .kpi-val    { color: var(--red); }
.kpi-sub { font-size: 11px; color: var(--text-dim); font-weight: 500; }
.g2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.g4 { display: grid; grid-template-columns: repeat(4,1fr); gap: 8px; }
.panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; margin-bottom: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.ph { padding: 16px 22px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.ph-title { font-size: 15px; font-weight: 800; color: var(--text); }
.ph-badge { font-family: "Nunito Sans", sans-serif; font-size: 9px; padding: 3px 8px; border-radius: 8px; letter-spacing: 1px; }
.pb { padding: 18px 22px; }
.co-row { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid var(--border); cursor: pointer; transition: all 0.15s; }
.co-row:last-child { border-bottom: none; }
.co-row:hover { opacity: 0.8; }
.co-av { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 800; flex-shrink: 0; }
.co-info { flex: 1; }
.co-name { font-size: 14px; font-weight: 800; }
.co-meta { font-size: 11px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 3px; }
.co-barw { width: 90px; }
.co-bar { height: 5px; background: var(--surface3); border-radius: 3px; overflow: hidden; }
.co-pct { font-size: 18px; font-weight: 900; min-width: 44px; text-align: right; }
.cat-blk { background: var(--surface2); border-radius: 7px; padding: 14px 12px; border: 1px solid var(--border); display: flex; flex-direction: column; align-items: center; text-align: center; }
.cat-blk-lbl { font-size: 9px; font-family: "Nunito Sans", sans-serif; letter-spacing: 1.5px; margin-bottom: 6px; text-transform: uppercase; font-weight: 700; }
.cat-blk-lbl.finance  { color: var(--finance); }
.cat-blk-lbl.legal    { color: var(--legal); }
.cat-blk-lbl.dev      { color: var(--dev); }
.cat-blk-lbl.marketing{ color: var(--marketing); }
.cat-blk-pct { font-size: 30px; font-weight: 900; margin-bottom: 2px; }
.cat-blk-pct.finance  { color: var(--finance); }
.cat-blk-pct.legal    { color: var(--legal); }
.cat-blk-pct.dev      { color: var(--dev); }
.cat-blk-pct.marketing{ color: var(--marketing); }
.cat-blk-sub { font-size: 11px; color: var(--text-dim); font-weight: 500; }
.cat-big-bar { height: 3px; background: var(--surface3); border-radius: 2px; overflow: hidden; margin-top: 9px; width: 100%; }
.cat-big-fill { height: 100%; border-radius: 2px; }
.dl-row { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); font-size: 13px; cursor: pointer; }
.dl-row:last-child { border-bottom: none; }
.dl-date { font-family: "Nunito Sans", sans-serif; font-size: 11px; color: var(--text-dim); min-width: 72px; font-weight: 600; }
.dl-label { font-weight: 700; font-size: 13px; }
.dl-co { font-size: 11px; color: var(--text-dim); font-weight: 600; }
.dl-days { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 800; padding: 4px 9px; border-radius: 5px; }
.dl-days.u  { background: var(--red-dim);    color: var(--red); }
.dl-days.w  { background: var(--yellow-dim); color: var(--yellow); }
.dl-days.ok { background: var(--green-dim);  color: var(--green); }
.act-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.act-item:last-child { border-bottom: none; }
.act-dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 3px; flex-shrink: 0; }
.act-txt { font-size: 13px; line-height: 1.6; flex: 1; color: var(--text-mid); font-weight: 500; }
.act-time { font-size: 11px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; flex-shrink: 0; margin-top: 3px; }
.empty-mini { padding: 16px 0; color: var(--text-dim); font-size: 12px; text-align: center; }

/* ════ GANTT ════ */
.gantt-select { background: var(--surface2); border: 1px solid var(--border-bright); border-radius: 6px; padding: 5px 10px; color: var(--text-mid); font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 600; cursor: pointer; }
.gantt-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.gantt-ph { padding: 14px 20px; border-bottom: 1px solid var(--border); background: var(--surface2); display: flex; align-items: center; justify-content: space-between; }
.gantt-ph-title { font-size: 14px; font-weight: 800; }
.gantt-scroll { overflow-x: auto; }
.gantt-header { display: flex; border-bottom: 2px solid var(--border); background: var(--surface2); }
.gantt-lbl-col { width: 260px; flex-shrink: 0; padding: 10px 16px; font-family: "Nunito Sans", sans-serif; font-size: 9px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 1px; font-weight: 700; display: flex; align-items: flex-end; }
.gantt-weeks-row { flex: 1; display: grid; grid-template-columns: repeat(12, 1fr); min-width: 600px; }
.gantt-wk-hd { padding: 8px 4px; text-align: center; border-left: 1px solid var(--border); }
.gantt-wk-num { font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 800; color: var(--text-dim); }
.gantt-wk-date { font-family: "Nunito Sans", sans-serif; font-size: 9px; color: var(--text-dim); margin-top: 2px; }
.gantt-wk-today { background: var(--accent-dim); }
.gantt-wk-today .gantt-wk-num { color: var(--accent); }
.gantt-wk-today .gantt-wk-date { color: var(--accent); }
.gantt-empty { padding: 32px; text-align: center; color: var(--text-dim); font-size: 13px; }
.gantt-proj-row { display: flex; align-items: center; border-bottom: 1px solid var(--border); min-height: 52px; }
.gantt-proj-row:last-child { border-bottom: none; }
.gantt-proj-row-click { cursor: pointer; transition: background 0.12s; }
.gantt-proj-row-click:hover { background: var(--accent-dim); }
.gantt-proj-lbl { width: 260px; flex-shrink: 0; padding: 10px 16px; display: flex; align-items: center; gap: 10px; }
.cat-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.cat-dot.finance   { background: var(--finance); }
.cat-dot.legal     { background: var(--legal); }
.cat-dot.dev       { background: var(--dev); }
.cat-dot.marketing { background: var(--marketing); }
.gantt-p-name { font-size: 12px; font-weight: 700; color: var(--text); }
.gantt-p-co { font-size: 10px; font-family: "Nunito Sans", sans-serif; margin-top: 2px; }
.gantt-p-pct { font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 800; margin-left: auto; }
.gantt-track { flex: 1; position: relative; height: 52px; display: flex; align-items: center; min-width: 600px; }
.gantt-today-line { position: absolute; top: 0; bottom: 0; width: 2px; background: var(--accent); opacity: 0.5; z-index: 2; pointer-events: none; }
.gantt-task-bar { position: absolute; height: 28px; border-radius: 5px; display: flex; align-items: center; overflow: hidden; min-width: 3px; z-index: 1; }
.gantt-task-fill { position: absolute; top: 0; left: 0; height: 100%; border-radius: 5px 0 0 5px; }
.gantt-task-label { font-size: 10px; font-weight: 700; color: #fff; padding: 0 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; position: relative; z-index: 1; }

@media (max-width: 768px) {
  .content { padding: 16px 12px; }
  .kpi-strip { grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px; }
  .g2 { grid-template-columns: 1fr !important; gap: 10px; }
  .g4 { grid-template-columns: 1fr !important; gap: 10px; }
  .panel { margin-bottom: 10px; }
  .kpi { padding: 16px 12px; }
  .kpi-val { font-size: 28px; }
  .kpi { padding: 14px 12px; }
  .kpi-val { font-size: 20px; }
  .dash-grid { flex-direction: column; }
  .dash-col-left, .dash-col-right { width: 100%; }
  .gantt-scroll { overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .gantt-proj-lbl { width: 160px; }
  .gantt-track { min-width: 400px; }
  .gantt-ph { flex-wrap: wrap; gap: 8px; }
  .ph-badge { display: none; }
}
</style>