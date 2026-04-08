<template>
  <div class="content">
    <div v-if="loading" class="loading">Φόρτωση...</div>
    <div v-else-if="project">

      <!-- BREADCRUMB -->
      <div class="breadcrumb">
        <span @click="router.push('/')">Dashboard</span>
        <span class="bc-sep">›</span>
        <span @click="router.push('/projects')">Projects</span>
        <span class="bc-sep">›</span>
        <span :style="`color:var(--${project.category});`">{{ project.title }}</span>
      </div>

      <!-- CONTRACT HEADER -->
      <div :class="`contract-header ${project.category}`">
        <div class="ch-top">
          <div>
            <div class="ch-title">{{ project.title }}</div>
            <div class="ch-meta">
              <span :style="`color:${project.companyColor};font-weight:700;`">{{ project.companyCode }}</span>
              <span>{{ project.companyName }}</span>
              <span v-if="project.deadline">Deadline: {{ formatDate(project.deadline) }}</span>
              <span v-if="project.budget">Budget: €{{ Number(project.budget).toLocaleString() }}</span>
            </div>
          </div>
          <span :class="`ch-badge ${project.category}`">{{ catLabel(project.category) }}</span>
        </div>

        <!-- STATS ROW -->
        <div class="contract-stats">
          <div class="cs-item">
            <div class="cs-lbl">Overall</div>
            <div class="cs-val" :style="`color:var(--${project.category});`">{{ project.completion }}%</div>
          </div>
          <div class="cs-item">
            <div class="cs-lbl">Tasks</div>
            <div class="cs-val">{{ project.tasksDone }}/{{ project.tasksTotal }}</div>
          </div>
          <div class="cs-item">
            <div class="cs-lbl">Status</div>
            <div class="cs-val" :class="`status-val-${project.status}`" style="font-size:13px;">
              {{ statusLabel(project.status) }}
            </div>
          </div>
          <div v-if="project.budget" class="cs-item">
            <div class="cs-lbl">Budget</div>
            <div class="cs-val">€{{ Number(project.budget).toLocaleString() }}</div>
          </div>
        </div>

        <!-- PROGRESS BAR -->
        <div class="ch-progress-wrap">
          <div class="ch-progress-bar">
            <div class="ch-progress-fill" :style="`width:${project.completion}%;background:var(--${project.category});`"></div>
          </div>
          <span class="ch-progress-pct" :style="`color:var(--${project.category});`">{{ project.completion }}%</span>
        </div>
      </div>

      <!-- SPECS -->
      <div v-if="project.specs && project.specs.length" class="specs-panel">
        <div class="specs-title">📋 Specifications</div>
        <div class="specs-list">
          <div v-for="s in project.specs" :key="s.id" class="spec-item">
            <div :class="`spec-check ${s.isDone ? 'done' : ''}`">{{ s.isDone ? "✓" : "" }}</div>
            <div :class="`spec-txt ${s.isDone ? 'done' : ''}`">{{ s.description }}</div>
          </div>
        </div>
      </div>

      <!-- MODULES -->
      <div v-if="project.modules && project.modules.length">
        <div class="modules-title">Modules & Tasks</div>
        <div v-for="m in project.modules" :key="m.id" class="module-group">
          <div class="mg-header" @click="toggleMod(m.id)">
            <span class="mg-expand">{{ openMods.has(m.id) ? "▼" : "▶" }}</span>
            <span class="mg-name">{{ m.name }}</span>
            <div class="mg-right">
              <div class="mg-bar-wrap">
                <div class="mg-bar">
                  <div class="mg-bar-fill" :style="`width:${m.completion}%;background:var(--${m.color});`"></div>
                </div>
              </div>
              <span class="mg-pct" :style="`color:var(--${m.color});`">{{ m.completion }}%</span>
              <span class="mg-tasks">{{ m.tasks.filter(t=>t.isDone).length }}/{{ m.tasks.length }}</span>
            </div>
          </div>

          <div v-if="openMods.has(m.id)" class="task-list">
            <div v-for="t in m.tasks" :key="t.id" class="task-item">
              <div :class="`task-check ${t.isDone ? 'done' : t.isBlocked ? 'block' : ''}`">
                {{ t.isDone ? "✓" : "" }}
              </div>
              <div style="flex:1;">
                <div :class="`task-name ${t.isDone ? 'done' : ''}`">{{ t.name }}</div>
                <div v-if="t.blockNote" class="task-note">⚠ {{ t.blockNote }}</div>
                <div v-if="t.comment" class="task-comment">💬 {{ t.comment }}</div>
              </div>
              <span class="task-assignee">{{ t.assignee || "—" }}</span>
              <div class="task-pct-wrap">
                <div class="task-bar">
                  <div class="task-bar-fill" :style="`width:${t.progress}%;background:${t.isDone ? 'var(--green)' : 'var(--'+m.color+')'};`"></div>
                </div>
                <div class="task-pct" :style="`color:${t.isDone ? 'var(--green)' : 'var(--'+m.color+')'};`">{{ t.progress }}%</div>
              </div>
            </div>
            <div v-if="!m.tasks.length" class="task-empty">Δεν υπάρχουν tasks.</div>
          </div>
        </div>
      </div>

      <!-- CONTRACT DESC -->
      <div v-if="project.contractDesc" class="contract-desc-panel">
        <div class="cd-title">📄 Περιγραφή Σύμβασης</div>
        <div class="cd-text">{{ project.contractDesc }}</div>
      </div>

    </div>
    <div v-else class="loading">Project not found.</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useProjectStore } from "@/stores/projects";

const route = useRoute();
const router = useRouter();
const store = useProjectStore();

const project = ref(null);
const loading = ref(true);
const openMods = ref(new Set());

onMounted(async () => {
  project.value = await store.fetchProject(route.params.id);
  loading.value = false;
  // Ανοίγουμε αυτόματα το πρώτο module
  if (project.value?.modules?.length) {
    openMods.value.add(project.value.modules[0].id);
  }
});

function toggleMod(id) {
  const s = new Set(openMods.value);
  if (s.has(id)) s.delete(id);
  else s.add(id);
  openMods.value = s;
}

const catLabel = (c) => ({ finance:"Finance", legal:"Legal", dev:"Developing", marketing:"Marketing" }[c] || c);
const statusLabel = (s) => ({ on_track:"On Track", delayed:"Delayed", at_risk:"At Risk", stale:"Stale", completed:"Completed" }[s] || s);

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  const m = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"];
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`;
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.loading { color: var(--text-dim); font-size: 14px; padding: 40px; text-align: center; }
.breadcrumb { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--text-dim); margin-bottom: 18px; font-family: "Nunito Sans", sans-serif; }
.breadcrumb span { cursor: pointer; transition: color 0.15s; font-weight: 600; }
.breadcrumb span:hover { color: var(--accent); }
.bc-sep { color: var(--border-bright); }

.contract-header { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 20px 24px; margin-bottom: 14px; position: relative; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,0.06); }
.contract-header::before { content: ""; position: absolute; top: 0; left: 0; bottom: 0; width: 4px; }
.contract-header.finance::before  { background: var(--finance); }
.contract-header.legal::before    { background: var(--legal); }
.contract-header.dev::before      { background: var(--dev); }
.contract-header.marketing::before{ background: var(--marketing); }
.ch-top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 16px; }
.ch-title { font-size: 22px; font-weight: 900; margin-bottom: 6px; }
.ch-meta { font-size: 11px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; display: flex; gap: 14px; align-items: center; flex-wrap: wrap; }
.ch-badge { font-size: 9px; font-weight: 700; padding: 4px 12px; border-radius: 6px; letter-spacing: 1px; }
.ch-badge.finance  { background: var(--finance-dim);  color: var(--finance); }
.ch-badge.legal    { background: var(--legal-dim);    color: var(--legal); }
.ch-badge.dev      { background: var(--dev-dim);      color: var(--dev); }
.ch-badge.marketing{ background: var(--marketing-dim);color: var(--marketing); }
.contract-stats { display: grid; grid-template-columns: repeat(4,1fr); gap: 10px; margin-bottom: 16px; }
.cs-item { background: var(--surface2); border-radius: 8px; padding: 12px 14px; border: 1px solid var(--border); text-align: center; }
.cs-lbl { font-family: "Nunito Sans", sans-serif; font-size: 8px; letter-spacing: 1.5px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 5px; font-weight: 700; }
.cs-val { font-size: 20px; font-weight: 900; color: var(--text); }
.status-val-on_track { color: var(--green) !important; }
.status-val-delayed  { color: var(--yellow) !important; }
.status-val-at_risk  { color: var(--red) !important; }
.status-val-stale    { color: var(--yellow) !important; }
.ch-progress-wrap { display: flex; align-items: center; gap: 12px; }
.ch-progress-bar { flex: 1; height: 6px; background: var(--surface3); border-radius: 3px; overflow: hidden; }
.ch-progress-fill { height: 100%; border-radius: 3px; transition: width 0.5s; }
.ch-progress-pct { font-family: "Nunito Sans", sans-serif; font-size: 13px; font-weight: 800; min-width: 36px; text-align: right; }

.specs-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; margin-bottom: 14px; }
.specs-title { font-size: 13px; font-weight: 800; margin-bottom: 12px; }
.specs-list { display: flex; flex-direction: column; gap: 8px; }
.spec-item { display: flex; align-items: center; gap: 10px; }
.spec-check { width: 16px; height: 16px; border-radius: 4px; border: 1.5px solid var(--border-bright); display: flex; align-items: center; justify-content: center; font-size: 9px; flex-shrink: 0; }
.spec-check.done { background: var(--green); border-color: var(--green); color: #fff; }
.spec-txt { font-size: 13px; font-weight: 500; color: var(--text-mid); }
.spec-txt.done { color: var(--text-dim); text-decoration: line-through; }

.modules-title { font-size: 13px; font-weight: 800; color: var(--text-dim); text-transform: uppercase; letter-spacing: 1px; font-family: "Nunito Sans", sans-serif; margin-bottom: 10px; margin-top: 4px; }
.module-group { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; margin-bottom: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.mg-header { padding: 13px 18px; display: flex; align-items: center; gap: 10px; cursor: pointer; background: var(--surface2); transition: background 0.15s; }
.mg-header:hover { background: var(--surface3); }
.mg-expand { font-size: 9px; color: var(--text-dim); width: 10px; }
.mg-name { font-size: 13px; font-weight: 700; flex: 1; }
.mg-right { display: flex; align-items: center; gap: 10px; }
.mg-bar-wrap { width: 80px; }
.mg-bar { height: 4px; background: var(--surface3); border-radius: 2px; overflow: hidden; }
.mg-bar-fill { height: 100%; border-radius: 2px; }
.mg-pct { font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 800; min-width: 36px; text-align: right; }
.mg-tasks { font-family: "Nunito Sans", sans-serif; font-size: 11px; color: var(--text-dim); min-width: 36px; text-align: right; }

.task-list { padding: 0 18px; }
.task-item { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--border); }
.task-item:last-child { border-bottom: none; }
.task-check { width: 16px; height: 16px; border-radius: 4px; border: 1.5px solid var(--border-bright); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 9px; }
.task-check.done  { background: var(--green); border-color: var(--green); color: #fff; }
.task-check.block { background: var(--red-dim); border-color: var(--red); }
.task-name { font-size: 12px; font-weight: 600; color: var(--text); }
.task-name.done { color: var(--text-dim); text-decoration: line-through; }
.task-note { font-size: 10px; color: var(--red); font-family: "Nunito Sans", sans-serif; margin-top: 2px; }
.task-comment { font-size: 10px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 2px; }
.task-assignee { font-size: 10px; font-family: "Nunito Sans", sans-serif; color: var(--text-dim); background: var(--surface2); padding: 3px 8px; border-radius: 5px; white-space: nowrap; }
.task-pct-wrap { display: flex; align-items: center; gap: 6px; }
.task-bar { width: 60px; height: 3px; background: var(--surface3); border-radius: 2px; overflow: hidden; }
.task-bar-fill { height: 100%; border-radius: 2px; }
.task-pct { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; min-width: 32px; text-align: right; }
.task-empty { padding: 16px 0; color: var(--text-dim); font-size: 12px; text-align: center; }

.contract-desc-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; margin-top: 14px; }
.cd-title { font-size: 13px; font-weight: 800; margin-bottom: 8px; }
.cd-text { font-size: 13px; color: var(--text-mid); line-height: 1.7; }
</style>