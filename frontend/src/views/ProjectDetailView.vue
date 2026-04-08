<template>
  <div class="content">
    <div v-if="loading" class="loading">Φόρτωση...</div>
    <div v-else-if="project">
      <div class="breadcrumb">
        <span @click="router.push('/')">Dashboard</span>
        <span class="bc-sep">›</span>
        <span @click="router.push('/projects')">Projects</span>
        <span class="bc-sep">›</span>
        <span :style="`color:var(--${project.category});`">{{ project.title }}</span>
      </div>

      <div :class="`contract-header ${project.category}`">
        <div class="ch-top">
          <div>
            <div class="ch-title">{{ project.title }}</div>
            <div class="ch-meta">
              <span>{{ project.companyName }}</span>
              <span>Deadline: {{ formatDate(project.deadline) }}</span>
              <span v-if="project.budget">Budget: €{{ project.budget.toLocaleString() }}</span>
            </div>
          </div>
          <span :class="`ch-badge ${project.category}`">{{ project.category.toUpperCase() }}</span>
        </div>
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
            <div class="cs-val" style="font-size:12px;">{{ project.status }}</div>
          </div>
          <div v-if="project.budget" class="cs-item">
            <div class="cs-lbl">Budget</div>
            <div class="cs-val">€{{ Number(project.budget).toLocaleString() }}</div>
          </div>
        </div>
      </div>

      <!-- MODULES -->
      <div v-for="m in project.modules" :key="m.id" class="module-group">
        <div class="mg-header" @click="toggleMod(m.id)">
          <span class="mg-expand">{{ openMods.has(m.id) ? "▼" : "▶" }}</span>
          <span class="mg-name">{{ m.name }}</span>
          <span class="mg-pct" :style="`color:var(--${m.color});`">{{ m.completion }}%</span>
        </div>
        <div v-if="openMods.has(m.id)" class="task-list">
          <div v-for="t in m.tasks" :key="t.id" class="task-item">
            <div :class="`task-check ${t.isDone ? 'done' : t.isBlocked ? 'block' : ''}`">
              {{ t.isDone ? "✓" : "" }}
            </div>
            <div style="flex:1;">
              <div :class="`task-name ${t.isDone ? 'done' : ''}`">{{ t.name }}</div>
              <div v-if="t.blockNote" class="task-note">⚠ {{ t.blockNote }}</div>
            </div>
            <span class="task-assignee">{{ t.assignee || "—" }}</span>
            <div class="task-pct-wrap">
              <div class="task-bar">
                <div class="task-bar-fill" :style="`width:${t.progress}%;background:${t.isDone ? 'var(--green)' : 'var(--'+m.color+')'};`"></div>
              </div>
              <div class="task-pct" :style="`color:${t.isDone ? 'var(--green)' : 'var(--'+m.color+')'};`">{{ t.progress }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>
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
});

function toggleMod(id) {
  if (openMods.value.has(id)) openMods.value.delete(id);
  else openMods.value.add(id);
}

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  const m = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"];
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`;
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.loading { color: var(--text-dim); font-size: 14px; }
.breadcrumb { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--text-dim); margin-bottom: 18px; }
.breadcrumb span { cursor: pointer; transition: color 0.15s; }
.breadcrumb span:hover { color: var(--accent); }
.bc-sep { color: var(--border-bright); }
.contract-header { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 20px 24px; margin-bottom: 14px; position: relative; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,0.06); }
.contract-header::before { content: ""; position: absolute; top: 0; left: 0; bottom: 0; width: 3px; }
.contract-header.finance::before  { background: var(--finance); }
.contract-header.legal::before    { background: var(--legal); }
.contract-header.dev::before      { background: var(--dev); }
.contract-header.marketing::before{ background: var(--marketing); }
.ch-top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 14px; }
.ch-title { font-size: 20px; font-weight: 800; margin-bottom: 4px; }
.ch-meta { font-size: 10px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; display: flex; gap: 14px; }
.ch-badge { font-size: 9px; font-weight: 700; padding: 4px 10px; border-radius: 6px; }
.ch-badge.finance  { background: var(--finance-dim);  color: var(--finance); }
.ch-badge.legal    { background: var(--legal-dim);    color: var(--legal); }
.ch-badge.dev      { background: var(--dev-dim);      color: var(--dev); }
.ch-badge.marketing{ background: var(--marketing-dim);color: var(--marketing); }
.contract-stats { display: grid; grid-template-columns: repeat(4,1fr); gap: 10px; }
.cs-item { background: var(--surface2); border-radius: 8px; padding: 11px 13px; border: 1px solid var(--border); text-align: center; }
.cs-lbl { font-family: "Nunito Sans", sans-serif; font-size: 8px; letter-spacing: 1.5px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 4px; font-weight: 700; }
.cs-val { font-size: 18px; font-weight: 800; }
.module-group { background: var(--surface); border: 1px solid var(--border); border-radius: 9px; overflow: hidden; margin-bottom: 9px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.mg-header { padding: 11px 15px; display: flex; align-items: center; gap: 10px; cursor: pointer; background: var(--surface2); }
.mg-expand { font-size: 9px; color: var(--text-dim); }
.mg-name { font-size: 12px; font-weight: 700; flex: 1; }
.mg-pct { font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 700; }
.task-list { padding: 0 15px; }
.task-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid var(--border); }
.task-item:last-child { border-bottom: none; }
.task-check { width: 15px; height: 15px; border-radius: 3px; border: 1.5px solid var(--border-bright); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 8px; }
.task-check.done  { background: var(--green);   border-color: var(--green); color: #fff; }
.task-check.block { background: var(--red-dim); border-color: var(--red); }
.task-name { flex: 1; font-size: 11px; font-weight: 500; }
.task-name.done { color: var(--text-dim); text-decoration: line-through; }
.task-note { font-size: 9px; color: var(--red); font-family: "Nunito Sans", sans-serif; margin-top: 2px; }
.task-assignee { font-size: 9px; font-family: "Nunito Sans", sans-serif; color: var(--text-dim); background: var(--surface2); padding: 2px 6px; border-radius: 4px; }
.task-pct-wrap { display: flex; align-items: center; gap: 6px; }
.task-bar { width: 48px; height: 3px; background: var(--surface3); border-radius: 2px; overflow: hidden; }
.task-bar-fill { height: 100%; border-radius: 2px; }
.task-pct { font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 600; min-width: 26px; text-align: right; }
</style>