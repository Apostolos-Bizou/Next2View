<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-mark">Next2me Group</div>
        <div class="logo-name">Next<span>2</span>View</div>
      </div>
      <nav class="nav">
        <div class="nav-section">CEO View</div>
        <router-link to="/" class="nav-item" exact-active-class="active">
          <span class="nav-ico">◉</span>Dashboard
          <span class="nav-count">{{ store.projects.length }}</span>
        </router-link>
        <router-link to="/projects" class="nav-item" active-class="active">
          <span class="nav-ico">⬵</span>All Projects
          <span class="nav-count">{{ store.projects.length }}</span>
        </router-link>
        <div class="nav-section" style="margin-top:6px;">Κατηγορία</div>
        <router-link to="/projects?category=finance" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--finance);">$</span>Finance
          <span class="nav-count">{{ store.byCategory("finance").length }}</span>
        </router-link>
        <router-link to="/projects?category=legal" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--legal);">▪</span>Legal
          <span class="nav-count">{{ store.byCategory("legal").length }}</span>
        </router-link>
        <router-link to="/projects?category=dev" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--dev);">✨</span>Developing
          <span class="nav-count">{{ store.byCategory("dev").length }}</span>
        </router-link>
        <router-link to="/projects?category=marketing" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--marketing);">◆</span>Marketing
          <span class="nav-count">{{ store.byCategory("marketing").length }}</span>
        </router-link>
        <div class="nav-section" style="margin-top:6px;">Εταιρείες</div>
        <router-link
          v-for="co in store.companies" :key="co.id"
          :to="`/projects?companyId=${co.id}`"
          class="nav-item" active-class="active"
        >
          <span class="nav-ico" :style="`color:${co.color};`">·</span>
          {{ co.name }}
          <span class="nav-count">{{ co.projectCount || 0 }}</span>
        </router-link>
      </nav>
      <div class="sidebar-actions">
        <button class="btn-sidebar btn-ai">
          <span style="font-size:16px;">✦</span> AI Report
        </button>
        <button class="btn-sidebar" @click="showNewProject = true">+ New Project</button>
      </div>
      <div class="sidebar-user">
        <div class="avatar">{{ initials }}</div>
        <div>
          <div class="user-name">{{ auth.user?.fullName || "CEO" }}</div>
          <div class="user-role">CEO · Full Access</div>
        </div>
        <button class="logout-btn" @click="handleLogout" title="Logout">↩</button>
      </div>
    </aside>

    <main class="main">
      <div class="topbar">
        <div>
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-subtitle">{{ pageSubtitle }}</div>
        </div>
      </div>
      <router-view />
    </main>

    <!-- NEW PROJECT MODAL -->
    <div v-if="showNewProject" class="modal-overlay" @click.self="showNewProject = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">New Project</div>
          <button class="modal-close" @click="showNewProject = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Title *</label>
            <input v-model="form.title" placeholder="Project title" class="form-input" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Company *</label>
              <select v-model="form.companyId" class="form-input">
                <option value="">Select company</option>
                <option v-for="co in store.companies" :key="co.id" :value="co.id">{{ co.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Category *</label>
              <select v-model="form.category" class="form-input">
                <option value="">Select category</option>
                <option value="finance">Finance</option>
                <option value="legal">Legal</option>
                <option value="dev">Developing</option>
                <option value="marketing">Marketing</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Budget (€)</label>
              <input v-model="form.budget" type="number" placeholder="0" class="form-input" />
            </div>
            <div class="form-group">
              <label>Deadline</label>
              <input v-model="form.deadline" type="date" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="form.contractDesc" placeholder="Project description..." class="form-input" rows="3"></textarea>
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showNewProject = false">Cancel</button>
          <button class="btn-submit" :disabled="submitting" @click="submitProject">
            {{ submitting ? "Creating..." : "Create Project" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useProjectStore } from "@/stores/projects";

const auth = useAuthStore();
const store = useProjectStore();
const router = useRouter();
const route = useRoute();

const showNewProject = ref(false);
const submitting = ref(false);
const formError = ref("");
const form = ref({ title: "", companyId: "", category: "", budget: "", deadline: "", contractDesc: "" });

onMounted(async () => {
  await Promise.all([store.fetchProjects(), store.fetchCompanies()]);
});

const initials = computed(() => {
  const name = auth.user?.fullName || "";
  return name.split(" ").map(w => w[0]).join("").substring(0, 2).toUpperCase() || "AB";
});

const pageTitle = computed(() => ({
  Dashboard: "Group Dashboard",
  Projects: "All Projects",
  ProjectDetail: store.selectedProject?.title || "Project Detail",
}[route.name] || "Next2View"));

const pageSubtitle = computed(() => ({
  Dashboard: "All companies · All categories · CEO view",
  Projects: `${store.projects.length} projects`,
  ProjectDetail: store.selectedProject ? `${store.selectedProject.companyName} · ${store.selectedProject.category}` : "",
}[route.name] || ""));

async function submitProject() {
  formError.value = "";
  if (!form.value.title || !form.value.companyId || !form.value.category) {
    formError.value = "Title, Company and Category are required.";
    return;
  }
  submitting.value = true;
  try {
    await store.createProject({
      title: form.value.title,
      companyId: form.value.companyId,
      category: form.value.category,
      budget: form.value.budget ? Number(form.value.budget) : null,
      deadline: form.value.deadline || null,
      contractDesc: form.value.contractDesc || null,
    });
    showNewProject.value = false;
    form.value = { title: "", companyId: "", category: "", budget: "", deadline: "", contractDesc: "" };
  } catch (e) {
    formError.value = e.response?.data?.message || "Failed to create project.";
  } finally {
    submitting.value = false;
  }
}

async function handleLogout() {
  await auth.logout();
  router.push("/login");
}
</script>

<style scoped>
.shell { display: flex; min-height: 100vh; }
.sidebar { width: 228px; background: var(--sidebar-bg); border-right: 1px solid var(--sidebar-border); display: flex; flex-direction: column; flex-shrink: 0; position: sticky; top: 0; height: 100vh; }
.logo { padding: 20px 18px 16px; border-bottom: 1px solid var(--sidebar-border); }
.logo-mark { font-family: "Nunito Sans", sans-serif; font-size: 8px; color: var(--sidebar-active-border); letter-spacing: 3px; text-transform: uppercase; margin-bottom: 3px; }
.logo-name { font-size: 20px; font-weight: 800; color: #fff; letter-spacing: -0.5px; }
.logo-name span { color: var(--sidebar-active-border); }
.nav { padding: 12px 0; flex: 1; overflow-y: auto; }
.nav-section { font-family: "Nunito Sans", sans-serif; font-size: 8px; letter-spacing: 2px; color: var(--sidebar-text-dim); padding: 10px 18px 4px; text-transform: uppercase; }
.nav-item { display: flex; align-items: center; gap: 9px; padding: 10px 18px; cursor: pointer; transition: all 0.15s; font-size: 13px; font-weight: 600; color: var(--sidebar-text); border-left: 2px solid transparent; text-decoration: none; }
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.nav-item.active { color: #fff; border-left-color: var(--sidebar-active-border); background: var(--sidebar-active-bg); }
.nav-ico { font-size: 13px; width: 15px; text-align: center; }
.nav-count { margin-left: auto; font-family: "Nunito Sans", sans-serif; font-size: 9px; background: rgba(255,255,255,0.08); padding: 2px 6px; border-radius: 8px; color: var(--sidebar-text-dim); }
.sidebar-actions { padding: 10px 14px; border-top: 1px solid var(--sidebar-border); display: flex; flex-direction: column; gap: 6px; }
.btn-sidebar { width: 100%; padding: 8px 12px; background: rgba(99,179,237,0.12); border: 1px solid rgba(99,179,237,0.25); border-radius: 6px; color: var(--sidebar-active-border); font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-sidebar:hover { background: rgba(99,179,237,0.22); }
.btn-ai { background: rgba(139,92,246,0.15); border-color: rgba(139,92,246,0.35); color: #a78bfa; display: flex; align-items: center; justify-content: center; gap: 7px; }
.sidebar-user { padding: 12px 16px; border-top: 1px solid var(--sidebar-border); display: flex; align-items: center; gap: 9px; }
.avatar { width: 28px; height: 28px; background: var(--sidebar-active-border); border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 700; color: #1c2333; flex-shrink: 0; }
.user-name { font-size: 11px; font-weight: 600; color: #fff; }
.user-role { font-size: 9px; color: var(--sidebar-text-dim); font-family: "Nunito Sans", sans-serif; }
.logout-btn { margin-left: auto; background: none; border: none; color: var(--sidebar-text-dim); cursor: pointer; font-size: 16px; padding: 4px; transition: color 0.15s; }
.logout-btn:hover { color: var(--red); }
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar { padding: 18px 32px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface); box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.page-title { font-size: 20px; font-weight: 900; color: var(--text); }
.page-subtitle { font-size: 12px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 3px; font-weight: 600; }

/* MODAL */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: var(--surface); border-radius: 14px; width: 540px; max-width: 95vw; box-shadow: 0 20px 60px rgba(0,0,0,0.2); overflow: hidden; }
.modal-header { padding: 20px 24px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.modal-title { font-size: 16px; font-weight: 800; }
.modal-close { background: none; border: none; font-size: 18px; cursor: pointer; color: var(--text-dim); padding: 4px 8px; border-radius: 6px; }
.modal-close:hover { background: var(--surface3); color: var(--text); }
.modal-body { padding: 22px 24px; display: flex; flex-direction: column; gap: 14px; }
.modal-footer { padding: 16px 24px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 10px; background: var(--surface2); }
.form-group { display: flex; flex-direction: column; gap: 5px; flex: 1; }
.form-row { display: flex; gap: 12px; }
.form-group label { font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: var(--text-dim); }
.form-input { padding: 9px 12px; border: 1px solid var(--border-bright); border-radius: 7px; background: var(--surface2); color: var(--text); font-family: "Nunito", sans-serif; font-size: 13px; font-weight: 600; outline: none; width: 100%; box-sizing: border-box; transition: border-color 0.15s; }
.form-input:focus { border-color: var(--accent); }
textarea.form-input { resize: vertical; min-height: 80px; }
.form-error { background: var(--red-dim); border: 1px solid rgba(220,38,38,0.2); border-radius: 7px; padding: 10px 14px; color: var(--red); font-size: 12px; font-weight: 600; }
.btn-cancel { padding: 9px 20px; background: var(--surface3); border: 1px solid var(--border-bright); border-radius: 7px; font-family: "Nunito", sans-serif; font-size: 13px; font-weight: 700; cursor: pointer; color: var(--text-mid); }
.btn-submit { padding: 9px 24px; background: var(--accent); border: none; border-radius: 7px; font-family: "Nunito", sans-serif; font-size: 13px; font-weight: 700; cursor: pointer; color: #fff; transition: opacity 0.15s; }
.btn-submit:hover { opacity: 0.88; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
</style>