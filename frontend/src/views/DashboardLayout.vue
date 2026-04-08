<template>
  <div class="shell">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-mark">Next2me Group</div>
        <div class="logo-name">Next<span>2</span>View</div>
      </div>

      <nav class="nav">
        <div class="nav-section">CEO View</div>
        <router-link to="/" class="nav-item" exact-active-class="active">
          <span class="nav-ico">◈</span>Dashboard
          <span class="nav-count">{{ store.projects.length }}</span>
        </router-link>
        <router-link to="/projects" class="nav-item" active-class="active">
          <span class="nav-ico">⬡</span>All Projects
          <span class="nav-count">{{ store.projects.length }}</span>
        </router-link>

        <div class="nav-section" style="margin-top:6px;">Κατηγορία</div>
        <router-link to="/projects?category=finance" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--finance);">$</span>Finance
          <span class="nav-count">{{ store.byCategory("finance").length }}</span>
        </router-link>
        <router-link to="/projects?category=legal" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--legal);">⚖</span>Legal
          <span class="nav-count">{{ store.byCategory("legal").length }}</span>
        </router-link>
        <router-link to="/projects?category=dev" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--dev);">⌨</span>Developing
          <span class="nav-count">{{ store.byCategory("dev").length }}</span>
        </router-link>
        <router-link to="/projects?category=marketing" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--marketing);">◈</span>Marketing
          <span class="nav-count">{{ store.byCategory("marketing").length }}</span>
        </router-link>

        <div class="nav-section" style="margin-top:6px;">Εταιρείες</div>
        <router-link
          v-for="co in store.companies"
          :key="co.id"
          :to="`/projects?companyId=${co.id}`"
          class="nav-item"
          active-class="active"
        >
          <span class="nav-ico" :style="`color:${co.color};`">·</span>
          {{ co.name }}
          <span class="nav-count">{{ co.projectCount }}</span>
        </router-link>
      </nav>

      <div class="sidebar-actions">
        <button class="btn-sidebar btn-ai" @click="$emit('openAIReport')">
          <span style="font-size:16px;">✦</span> AI Report
        </button>
        <button class="btn-sidebar" @click="$emit('newProject')">+ New Project</button>
      </div>

      <div class="sidebar-user">
        <div class="avatar">ΑΒ</div>
        <div>
          <div class="user-name">{{ auth.user?.fullName || "CEO" }}</div>
          <div class="user-role">CEO · Full Access</div>
        </div>
        <button class="logout-btn" @click="handleLogout" title="Logout">↩</button>
      </div>
    </aside>

    <!-- MAIN -->
    <main class="main">
      <div class="topbar">
        <div>
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-subtitle">{{ pageSubtitle }}</div>
        </div>
      </div>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useProjectStore } from "@/stores/projects";

const auth = useAuthStore();
const store = useProjectStore();
const router = useRouter();
const route = useRoute();

onMounted(async () => {
  await Promise.all([store.fetchProjects(), store.fetchCompanies()]);
});

const pageTitle = computed(() => {
  const map = {
    Dashboard: "Group Dashboard",
    Projects: "All Projects",
    ProjectDetail: store.selectedProject?.title || "Project Detail",
  };
  return map[route.name] || "Next2View";
});

const pageSubtitle = computed(() => {
  const map = {
    Dashboard: "All companies · All categories · CEO view",
    Projects: `${store.projects.length} projects`,
    ProjectDetail: store.selectedProject
      ? `${store.selectedProject.companyName} · ${store.selectedProject.category}`
      : "",
  };
  return map[route.name] || "";
});

async function handleLogout() {
  await auth.logout();
  router.push("/login");
}
</script>

<style scoped>
.shell { display: flex; min-height: 100vh; }

.sidebar {
  width: 228px;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
}
.logo {
  padding: 20px 18px 16px;
  border-bottom: 1px solid var(--sidebar-border);
}
.logo-mark {
  font-family: "Nunito Sans", sans-serif;
  font-size: 8px;
  color: var(--sidebar-active-border);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 3px;
}
.logo-name {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5px;
}
.logo-name span { color: var(--sidebar-active-border); }

.nav { padding: 12px 0; flex: 1; overflow-y: auto; }
.nav-section {
  font-family: "Nunito Sans", sans-serif;
  font-size: 8px;
  letter-spacing: 2px;
  color: var(--sidebar-text-dim);
  padding: 10px 18px 4px;
  text-transform: uppercase;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 18px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 13px;
  font-weight: 600;
  color: var(--sidebar-text);
  border-left: 2px solid transparent;
  text-decoration: none;
}
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.nav-item.active {
  color: #fff;
  border-left-color: var(--sidebar-active-border);
  background: var(--sidebar-active-bg);
}
.nav-ico { font-size: 13px; width: 15px; text-align: center; }
.nav-count {
  margin-left: auto;
  font-family: "Nunito Sans", sans-serif;
  font-size: 9px;
  background: rgba(255,255,255,0.08);
  padding: 2px 6px;
  border-radius: 8px;
  color: var(--sidebar-text-dim);
}

.sidebar-actions {
  padding: 10px 14px;
  border-top: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.btn-sidebar {
  width: 100%;
  padding: 8px 12px;
  background: rgba(99,179,237,0.12);
  border: 1px solid rgba(99,179,237,0.25);
  border-radius: 6px;
  color: var(--sidebar-active-border);
  font-family: "Nunito", sans-serif;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-sidebar:hover { background: rgba(99,179,237,0.22); }
.btn-ai {
  background: rgba(139,92,246,0.15);
  border-color: rgba(139,92,246,0.35);
  color: #a78bfa;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}

.sidebar-user {
  padding: 12px 16px;
  border-top: 1px solid var(--sidebar-border);
  display: flex;
  align-items: center;
  gap: 9px;
}
.avatar {
  width: 28px; height: 28px;
  background: var(--sidebar-active-border);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: #1c2333;
  flex-shrink: 0;
}
.user-name { font-size: 11px; font-weight: 600; color: #fff; }
.user-role { font-size: 9px; color: var(--sidebar-text-dim); font-family: "Nunito Sans", sans-serif; }
.logout-btn {
  margin-left: auto;
  background: none;
  border: none;
  color: var(--sidebar-text-dim);
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  transition: color 0.15s;
}
.logout-btn:hover { color: var(--red); }

.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar {
  padding: 18px 32px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--surface);
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.page-title { font-size: 20px; font-weight: 900; color: var(--text); }
.page-subtitle {
  font-size: 12px;
  color: var(--text-dim);
  font-family: "Nunito Sans", sans-serif;
  margin-top: 3px;
  font-weight: 600;
}
</style>