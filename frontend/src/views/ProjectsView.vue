<template>
  <div class="content">
    <div class="panel">
      <div class="ph">
        <div class="ph-title">All Projects</div>
        <div class="badge blue">{{ store.projects.length }} projects</div>
      </div>
      <div class="pb" style="padding:0 18px;">
        <table class="proj-tbl">
          <thead><tr>
            <th>Project</th><th>Κατηγορία</th><th>Status</th>
            <th>Progress</th><th>Budget</th><th>Deadline</th>
          </tr></thead>
          <tbody>
            <tr
              v-for="p in store.projects"
              :key="p.id"
              class="proj-row"
              @click="router.push(`/projects/${p.id}`)"
            >
              <td>
                <div class="pname">{{ p.title }}</div>
                <div class="pco">{{ p.companyName }}</div>
              </td>
              <td><span :class="`cat-pill ${p.category}`">{{ catIcon(p.category) }} {{ catLabel(p.category) }}</span></td>
              <td><span :class="`sdot ${statusDot(p.status)}`"></span>{{ p.status }}</td>
              <td>
                <div class="pct-mono" :style="`color:var(--${p.category});`">{{ p.completion }}%</div>
                <div class="mbar"><div class="mbar-fill" :style="`width:${p.completion}%;background:var(--${p.category});`"></div></div>
              </td>
              <td style="font-size:14px;font-weight:700;color:var(--text-mid);">
                {{ p.budget ? "€" + (p.budget/1000).toFixed(0) + "k" : "—" }}
              </td>
              <td style="font-size:13px;font-weight:600;color:var(--text-dim);font-family:'Nunito Sans',sans-serif;">
                {{ formatDate(p.deadline) }}
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!store.projects.length" class="empty-state">
          <div style="font-size:28px;margin-bottom:8px;opacity:0.4;">⬡</div>
          <div>Δεν υπάρχουν projects.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useProjectStore } from "@/stores/projects";

const store = useProjectStore();
const router = useRouter();

const catIcon  = (c) => ({ finance:"$", legal:"⚖", dev:"⌨", marketing:"◈" }[c] || "·");
const catLabel = (c) => ({ finance:"Finance", legal:"Legal", dev:"Developing", marketing:"Marketing" }[c] || c);
const statusDot= (s) => ({ on_track:"g", delayed:"y", at_risk:"r", stale:"r" }[s] || "g");

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  const m = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"];
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`;
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.ph { padding: 16px 22px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.ph-title { font-size: 15px; font-weight: 800; }
.pb { padding: 18px 22px; }
.proj-tbl { width: 100%; border-collapse: collapse; }
.proj-tbl th { font-family: "Nunito Sans", sans-serif; font-size: 10px; letter-spacing: 1.5px; color: var(--text-dim); text-align: left; padding: 0 8px 14px 0; border-bottom: 2px solid var(--border); font-weight: 700; text-transform: uppercase; }
.proj-tbl td { padding: 18px 12px 18px 0; border-bottom: 1px solid var(--border); vertical-align: middle; }
.proj-tbl tr:last-child td { border-bottom: none; }
.proj-row { cursor: pointer; transition: all 0.12s; }
.proj-row:hover td { background: var(--accent-dim); }
.pname { font-size: 16px; font-weight: 800; }
.pco { font-size: 12px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 3px; }
.pct-mono { font-family: "Nunito Sans", sans-serif; font-size: 18px; font-weight: 800; }
.mbar { height: 5px; background: var(--surface3); border-radius: 3px; overflow: hidden; margin-top: 6px; width: 100px; }
.mbar-fill { height: 100%; border-radius: 2px; }
.sdot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 6px; }
.sdot.g { background: var(--green); }
.sdot.y { background: var(--yellow); }
.sdot.r { background: var(--red); }
.empty-state { text-align: center; padding: 36px 20px; color: var(--text-dim); font-size: 12px; }
</style>