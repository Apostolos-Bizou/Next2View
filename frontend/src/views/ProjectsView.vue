<template>
  <div class="content">
    <div class="panel">
      <div class="ph">
        <div class="ph-title">{{ pageTitle }}</div>
        <div style="display:flex;gap:8px;align-items:center;">
          <select v-model="filterCat" @change="applyFilters" class="ph-select">
            <option value="">Όλες κατηγορίες</option>
            <option value="finance">$ Finance</option>
            <option value="legal">⚖ Legal</option>
            <option value="dev">⌨ Developing</option>
            <option value="marketing">◈ Marketing</option>
          </select>
          <select v-model="filterCo" @change="applyFilters" class="ph-select">
            <option value="">Όλες εταιρείες</option>
            <option v-for="co in store.companies" :key="co.id" :value="co.id">{{ coShort(co.name) }}</option>
          </select>
          <div class="badge blue">{{ filtered.length }} projects</div>
        </div>
      </div>
      <div class="pb" style="padding:0 18px;">
        <table class="proj-tbl">
          <thead><tr>
            <th style="width:220px;">Project</th>
            <th style="width:110px;">Κατηγορία</th>
            <th style="width:80px;">Status</th>
            <th style="width:130px;">Progress</th>
            <th style="width:70px;">Budget</th>
            <th style="width:60px;">Tasks</th>
            <th style="width:100px;">Deadline</th>
            <th style="width:80px;">Update</th>
          </tr></thead>
          <tbody>
            <tr
              v-for="p in filtered"
              :key="p.id"
              class="proj-row"
              @click="router.push(`/projects/${p.id}`)"
            >
              <td>
                <div class="pname">{{ p.title }}</div>
                <div class="pco">{{ p.companyName }}</div>
              </td>
              <td>
                <span :class="`cat-pill ${p.category}`">
                  {{ catIcon(p.category) }} {{ catLabel(p.category) }}
                </span>
              </td>
              <td>
                <div style="display:flex;align-items:center;gap:5px;">
                  <span :class="`sdot ${statusDot(p.status)}`"></span>
                  <span class="status-txt">{{ statusLabel(p.status) }}</span>
                </div>
              </td>
              <td>
                <div class="pct-mono" :style="`color:var(--${p.category});`">{{ p.completion }}%</div>
                <div class="mbar">
                  <div class="mbar-fill" :style="`width:${p.completion}%;background:var(--${p.category});`"></div>
                </div>
              </td>
              <td class="td-num">{{ p.budget ? "€" + (p.budget/1000).toFixed(0) + "k" : "—" }}</td>
              <td class="td-num">
                <span :class="p.tasksDone === p.tasksTotal && p.tasksTotal > 0 ? 'tasks-done' : 'tasks-pend'">
                  {{ p.tasksDone }}/{{ p.tasksTotal }}
                </span>
              </td>
              <td class="td-date">{{ formatDate(p.deadline) }}</td>
              <td class="td-date">{{ formatAgo(p.updatedAgo) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="!filtered.length" class="empty-state">
          <div style="font-size:28px;margin-bottom:8px;opacity:0.4;">⬡</div>
          <div>Δεν υπάρχουν projects.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useProjectStore } from "@/stores/projects";

const store = useProjectStore();
const router = useRouter();
const route = useRoute();

const filterCat = ref("");
const filterCo  = ref("");

onMounted(async () => {
  // Διαβάζουμε query params από URL
  if (route.query.category) filterCat.value = route.query.category;
  if (route.query.companyId) filterCo.value = route.query.companyId;
  await store.fetchProjects();
  if (!store.companies.length) await store.fetchCompanies();
});

// Watch για αλλαγές στο URL (sidebar clicks)
watch(() => route.query, (q) => {
  filterCat.value = q.category || "";
  filterCo.value  = q.companyId || "";
});

function applyFilters() {
  const q = {};
  if (filterCat.value) q.category = filterCat.value;
  if (filterCo.value)  q.companyId = filterCo.value;
  router.replace({ path: "/projects", query: q });
}

const filtered = computed(() => {
  let ps = store.projects;
  if (filterCat.value) ps = ps.filter(p => p.category === filterCat.value);
  if (filterCo.value)  ps = ps.filter(p => p.companyId === filterCo.value);
  return ps;
});

const pageTitle = computed(() => {
  if (filterCat.value) return catLabel(filterCat.value) + " Projects";
  if (filterCo.value) {
    const co = store.companies.find(c => c.id === filterCo.value);
    return (co ? coShort(co.name) : "") + " Projects";
  }
  return "All Projects";
});

const catIcon  = (c) => ({ finance:"$", legal:"⚖", dev:"⌨", marketing:"◈" }[c] || "·");
const catLabel = (c) => ({ finance:"Finance", legal:"Legal", dev:"Developing", marketing:"Marketing" }[c] || c);
const statusDot= (s) => ({ on_track:"g", delayed:"y", at_risk:"r", stale:"r" }[s] || "g");
const statusLabel = (s) => ({ on_track:"On Track", delayed:"Delayed", at_risk:"At Risk", stale:"Stale", completed:"Done" }[s] || s);

function coShort(name) {
  const m = { "Polaris Financial Services":"Polaris Financial", "Crossworld Marine Services":"Crossworld Marine", "WiMAS Training Center":"WiMAS", "Varship Management":"Varship" };
  return m[name] || name.split(" ").slice(0,2).join(" ");
}

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  const m = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"];
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`;
}

function formatAgo(mins) {
  if (!mins && mins !== 0) return "—";
  if (mins < 60) return `${mins}m`;
  if (mins < 1440) return `${Math.round(mins/60)}h`;
  return `${Math.round(mins/1440)}d`;
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.ph { padding: 16px 22px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.ph-title { font-size: 15px; font-weight: 800; }
.ph-select { background: var(--surface2); border: 1px solid var(--border-bright); border-radius: 6px; padding: 5px 10px; color: var(--text-mid); font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 600; cursor: pointer; }
.pb { padding: 18px 22px; }
.proj-tbl { width: 100%; border-collapse: collapse; }
.proj-tbl th { font-family: "Nunito Sans", sans-serif; font-size: 10px; letter-spacing: 1.5px; color: var(--text-dim); text-align: left; padding: 0 8px 14px 0; border-bottom: 2px solid var(--border); font-weight: 700; text-transform: uppercase; }
.proj-tbl td { padding: 16px 10px 16px 0; border-bottom: 1px solid var(--border); vertical-align: middle; }
.proj-tbl tr:last-child td { border-bottom: none; }
.proj-row { cursor: pointer; transition: all 0.12s; }
.proj-row:hover td { background: var(--accent-dim); }
.pname { font-size: 15px; font-weight: 800; }
.pco { font-size: 11px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 3px; }
.pct-mono { font-family: "Nunito Sans", sans-serif; font-size: 17px; font-weight: 800; }
.mbar { height: 4px; background: var(--surface3); border-radius: 3px; overflow: hidden; margin-top: 5px; width: 90px; }
.mbar-fill { height: 100%; border-radius: 2px; }
.sdot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
.sdot.g { background: var(--green); }
.sdot.y { background: var(--yellow); }
.sdot.r { background: var(--red); }
.status-txt { font-size: 11px; font-weight: 600; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; }
.td-num { font-size: 13px; font-weight: 700; color: var(--text-mid); }
.td-date { font-size: 12px; font-weight: 600; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; }
.tasks-done { font-size: 12px; font-weight: 700; color: var(--green); }
.tasks-pend { font-size: 12px; font-weight: 700; color: var(--text-mid); }
.empty-state { text-align: center; padding: 36px 20px; color: var(--text-dim); font-size: 12px; }
</style>