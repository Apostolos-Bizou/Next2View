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
          <span class="nav-ico">◈</span>Dashboard
        </router-link>
        <router-link to="/projects" class="nav-item" active-class="active">
          <span class="nav-ico">⬡</span>All Projects
          <span class="nav-count">{{ store.projects.length }}</span>
        </router-link>
        <router-link to="/guide" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:#f6ad55;">?</span>Guide
        </router-link>
        <router-link to="/admin" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--text-dim);">⚙</span>Admin
        </router-link>

        <div class="nav-section" style="margin-top:6px;">Κατηγορία</div>
        <router-link to="/projects?category=finance" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--finance);">$</span>Finance
          <span class="nav-count">{{ store.byCategory('finance').length }}</span>
        </router-link>
        <router-link to="/projects?category=legal" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--legal);">⚖</span>Legal
          <span class="nav-count">{{ store.byCategory('legal').length }}</span>
        </router-link>
        <router-link to="/projects?category=dev" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--dev);">⌨</span>Developing
          <span class="nav-count">{{ store.byCategory('dev').length }}</span>
        </router-link>
        <router-link to="/projects?category=marketing" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--marketing);">◈</span>Marketing
          <span class="nav-count">{{ store.byCategory('marketing').length }}</span>
        </router-link>

        <div class="nav-section" style="margin-top:6px;">Εταιρείες</div>
        <router-link
          v-for="co in store.companies" :key="co.id"
          :to="`/projects?companyId=${co.id}`"
          class="nav-item" active-class="active"
        >
          <span class="nav-ico" :style="`color:${co.color};`">{{ coIcon(co.code) }}</span>
          {{ coShortName(co.name) }}
          <span class="nav-count">{{ co.projectCount || 0 }}</span>
        </router-link>
      </nav>

      <div class="notif-row">
        <div class="notif-label">Notifications</div>
        <span class="notif-bell">🔔</span>
      </div>

      <div class="sidebar-actions">
        <button class="btn-sidebar btn-ai">
          <span style="font-size:16px;">✦</span> AI Report
        </button>
        <button class="btn-sidebar" @click="openNewProject">+ New Project</button>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:6px;">
          <button class="btn-sidebar" style="font-size:9px;">+ Company</button>
          <button class="btn-sidebar" style="font-size:9px;">+ User</button>
        </div>
      </div>

      <div class="sidebar-user">
        <div class="avatar">{{ initials }}</div>
        <div>
          <div class="user-name">{{ auth.user?.fullName?.split(' ')[0] || 'CEO' }}</div>
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
        <button class="topbar-btn" @click="openNewProject">+ New Project</button>
      </div>
      <router-view />
    </main>

    <!-- ════ NEW PROJECT MODAL ════ -->
    <div v-if="showNewProject" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-lg">
        <div class="modal-header">
          <div class="modal-title">+ New Project</div>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <!-- BASIC INFO -->
          <div class="form-section-title">Βασικά Στοιχεία</div>
          <div class="form-group">
            <label>Τίτλος *</label>
            <input v-model="form.title" placeholder="Τίτλος project" class="form-input" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Εταιρεία *</label>
              <select v-model="form.companyId" class="form-input">
                <option value="">Επίλεξε εταιρεία</option>
                <option v-for="co in store.companies" :key="co.id" :value="co.id">{{ co.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Κατηγορία *</label>
              <select v-model="form.category" class="form-input">
                <option value="">Επίλεξε κατηγορία</option>
                <option value="finance">$ Finance</option>
                <option value="legal">⚖ Legal</option>
                <option value="dev">⌨ Developing</option>
                <option value="marketing">◈ Marketing</option>
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
            <label>Περιγραφή Σύμβασης</label>
            <textarea v-model="form.contractDesc" placeholder="Σύντομη περιγραφή..." class="form-input" rows="2"></textarea>
          </div>

          <!-- SPECS -->
          <div class="form-section-title" style="margin-top:16px;">
            Specifications
            <button class="add-btn" @click="addSpec">+ Προσθήκη</button>
          </div>
          <div v-for="(s, i) in form.specs" :key="i" class="spec-row">
            <input type="checkbox" v-model="s.isDone" class="spec-check-input" />
            <input v-model="s.description" placeholder="Specification..." class="form-input spec-input" />
            <button class="del-btn" @click="form.specs.splice(i, 1)">✕</button>
          </div>

          <!-- MODULES & TASKS -->
          <div class="form-section-title" style="margin-top:16px;">
            Modules & Tasks
            <button class="add-btn" @click="addModule">+ Module</button>
          </div>
          <div v-for="(m, mi) in form.modules" :key="mi" class="module-builder">
            <div class="mb-head">
              <input v-model="m.name" placeholder="Όνομα module..." class="form-input mb-name" />
              <select v-model="m.color" class="form-input mb-color">
                <option value="finance">$ Finance</option>
                <option value="legal">⚖ Legal</option>
                <option value="dev">⌨ Dev</option>
                <option value="marketing">◈ Marketing</option>
              </select>
              <button class="del-btn" @click="form.modules.splice(mi, 1)">✕</button>
            </div>
            <div class="task-builder">
              <div v-for="(t, ti) in m.tasks" :key="ti" class="task-row">
                <input v-model="t.name" placeholder="Task..." class="form-input task-name-input" />
                <input v-model="t.assignee" placeholder="Assignee" class="form-input task-assign-input" />
                <input v-model.number="t.progress" type="number" min="0" max="100" class="form-input task-pct-input" />
                <span class="task-pct-lbl">%</span>
                <button class="del-btn" @click="m.tasks.splice(ti, 1)">✕</button>
              </div>
              <button class="add-task-btn" @click="addTask(m)">+ Task</button>
            </div>
          </div>

          <div v-if="formError" class="form-error">{{ formError }}</div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">Ακύρωση</button>
          <button class="btn-submit" :disabled="submitting" @click="submitProject">
            {{ submitting ? 'Δημιουργία...' : 'Δημιουργία Project' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/projects'

const auth = useAuthStore()
const store = useProjectStore()
const router = useRouter()
const route = useRoute()

const showNewProject = ref(false)
const submitting = ref(false)
const formError = ref('')

const emptyForm = () => ({
  title: '', companyId: '', category: '', budget: '', deadline: '',
  contractDesc: '', specs: [], modules: []
})
const form = ref(emptyForm())

onMounted(async () => {
  await Promise.all([store.fetchProjects(), store.fetchCompanies()])
})

function openNewProject() {
  form.value = emptyForm()
  formError.value = ''
  showNewProject.value = true
}
function closeModal() {
  showNewProject.value = false
  formError.value = ''
}
function addSpec() {
  form.value.specs.push({ description: '', isDone: false, sortOrder: form.value.specs.length })
}
function addModule() {
  form.value.modules.push({ name: '', color: form.value.category || 'dev', sortOrder: form.value.modules.length, tasks: [] })
}
function addTask(m) {
  m.tasks.push({ name: '', assignee: '', progress: 0, isDone: false, isBlocked: false, blockNote: '', comment: '', deadline: null, startWeek: null, durationWeeks: 1, sortOrder: m.tasks.length })
}

async function submitProject() {
  formError.value = ''
  if (!form.value.title || !form.value.companyId || !form.value.category) {
    formError.value = 'Τίτλος, Εταιρεία και Κατηγορία είναι υποχρεωτικά.'
    return
  }
  submitting.value = true
  try {
    const payload = {
      title: form.value.title,
      companyId: form.value.companyId,
      category: form.value.category,
      budget: form.value.budget ? Number(form.value.budget) : null,
      deadline: form.value.deadline || null,
      contractDesc: form.value.contractDesc || null,
      specs: form.value.specs.filter(s => s.description.trim()),
      modules: form.value.modules.filter(m => m.name.trim()).map((m, mi) => ({
        name: m.name, color: m.color, sortOrder: mi,
        tasks: m.tasks.filter(t => t.name.trim()).map((t, ti) => ({
          ...t, sortOrder: ti, isDone: t.progress === 100
        }))
      }))
    }
    const proj = await store.createProject(payload)
    closeModal()
    router.push(`/projects/${proj.id}`)
  } catch (e) {
    formError.value = e.response?.data?.message || 'Αποτυχία δημιουργίας project.'
  } finally {
    submitting.value = false
  }
}

function coIcon(code) {
  return { PF:'★', CW:'⚓', WM:'⊙', VS:'▲', OS:'·' }[code] || '·'
}
function coShortName(name) {
  const m = { 'Polaris Financial Services':'Polaris Financial', 'Crossworld Marine Services':'Crossworld Marine', 'WiMAS Training Center':'WiMAS', 'Varship Management':'Varship' }
  return m[name] || name.split(' ').slice(0,2).join(' ')
}

const initials = computed(() => {
  const name = auth.user?.fullName || ''
  return name.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase() || 'AB'
})

const pageTitle = computed(() => ({
  Dashboard: 'Group Dashboard',
  Projects: 'All Projects',
  ProjectDetail: store.selectedProject?.title || 'Project Detail',
  Guide: 'Οδηγός Χρήσης',
}[route.name] || 'Next2View'))

const pageSubtitle = computed(() => ({
  Dashboard: 'All companies · All categories · CEO view',
  Projects: `${store.projects.length} projects`,
  ProjectDetail: store.selectedProject ? `${store.selectedProject.companyName} · ${store.selectedProject.category}` : '',
  Guide: 'Πλήρης οδηγός πλατφόρμας',
}[route.name] || ''))

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.shell { display: flex; min-height: 100vh; }
.sidebar { width: 228px; background: var(--sidebar-bg); border-right: 1px solid var(--sidebar-border); display: flex; flex-direction: column; flex-shrink: 0; position: sticky; top: 0; height: 100vh; }
.logo { padding: 20px 18px 16px; border-bottom: 1px solid var(--sidebar-border); }
.logo-mark { font-family: 'Nunito Sans', sans-serif; font-size: 8px; color: var(--sidebar-active-border); letter-spacing: 3px; text-transform: uppercase; margin-bottom: 3px; }
.logo-name { font-size: 20px; font-weight: 800; color: #fff; letter-spacing: -0.5px; }
.logo-name span { color: var(--sidebar-active-border); }
.nav { padding: 12px 0; flex: 1; overflow-y: auto; }
.nav-section { font-family: 'Nunito Sans', sans-serif; font-size: 8px; letter-spacing: 2px; color: var(--sidebar-text-dim); padding: 10px 18px 4px; text-transform: uppercase; }
.nav-item { display: flex; align-items: center; gap: 9px; padding: 10px 18px; cursor: pointer; transition: all 0.15s; font-size: 13px; font-weight: 600; color: var(--sidebar-text); border-left: 2px solid transparent; text-decoration: none; }
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.nav-item.active { color: #fff; border-left-color: var(--sidebar-active-border); background: var(--sidebar-active-bg); }
.nav-ico { font-size: 13px; width: 15px; text-align: center; }
.nav-count { margin-left: auto; font-family: 'Nunito Sans', sans-serif; font-size: 8px; background: rgba(255,255,255,0.08); padding: 2px 6px; border-radius: 8px; color: var(--sidebar-text-dim); }
.notif-row { padding: 10px 14px 0; border-top: 1px solid var(--sidebar-border); display: flex; align-items: center; justify-content: space-between; }
.notif-label { font-size: 9px; letter-spacing: 1.5px; color: var(--sidebar-text-dim); text-transform: uppercase; font-family: 'Nunito Sans', sans-serif; font-weight: 700; }
.notif-bell { font-size: 18px; color: var(--sidebar-text); cursor: pointer; }
.sidebar-actions { padding: 10px 14px; display: flex; flex-direction: column; gap: 6px; }
.btn-sidebar { width: 100%; padding: 8px 12px; background: rgba(99,179,237,0.12); border: 1px solid rgba(99,179,237,0.25); border-radius: 6px; color: var(--sidebar-active-border); font-family: 'Nunito', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 0.5px; cursor: pointer; transition: all 0.2s; text-align: center; }
.btn-sidebar:hover { background: rgba(99,179,237,0.22); }
.btn-ai { background: rgba(139,92,246,0.15); border-color: rgba(139,92,246,0.35); color: #a78bfa; display: flex; align-items: center; justify-content: center; gap: 7px; font-size: 12px; }
.sidebar-user { padding: 12px 16px; border-top: 1px solid var(--sidebar-border); display: flex; align-items: center; gap: 9px; }
.avatar { width: 28px; height: 28px; background: var(--sidebar-active-border); border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 700; color: #1c2333; flex-shrink: 0; }
.user-name { font-size: 11px; font-weight: 600; color: #fff; }
.user-role { font-size: 9px; color: var(--sidebar-text-dim); font-family: 'Nunito Sans', sans-serif; }
.logout-btn { margin-left: auto; background: none; border: none; color: var(--sidebar-text-dim); cursor: pointer; font-size: 16px; padding: 4px; transition: color 0.15s; }
.logout-btn:hover { color: var(--red); }
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar { padding: 18px 32px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface); box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.page-title { font-size: 20px; font-weight: 900; color: var(--text); }
.page-subtitle { font-size: 12px; color: var(--text-dim); font-family: 'Nunito Sans', sans-serif; margin-top: 3px; font-weight: 600; }
.topbar-btn { padding: 8px 18px; background: var(--accent); border: none; border-radius: 7px; color: #fff; font-family: 'Nunito', sans-serif; font-size: 12px; font-weight: 700; cursor: pointer; transition: background 0.2s; }
.topbar-btn:hover { background: #2563eb; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; }
.modal { background: var(--surface); border-radius: 14px; width: 540px; max-width: 95vw; max-height: 90vh; box-shadow: 0 20px 60px rgba(0,0,0,0.2); overflow: hidden; display: flex; flex-direction: column; }
.modal-lg { width: 700px; }
.modal-header { padding: 18px 24px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); flex-shrink: 0; }
.modal-title { font-size: 16px; font-weight: 800; }
.modal-close { background: none; border: none; font-size: 18px; cursor: pointer; color: var(--text-dim); padding: 4px 8px; border-radius: 6px; }
.modal-close:hover { background: var(--surface3); }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 12px; overflow-y: auto; flex: 1; }
.modal-footer { padding: 14px 24px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 10px; background: var(--surface2); flex-shrink: 0; }
.form-section-title { font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; color: var(--text-dim); display: flex; align-items: center; justify-content: space-between; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
.form-group { display: flex; flex-direction: column; gap: 5px; flex: 1; }
.form-row { display: flex; gap: 12px; }
.form-group label { font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: var(--text-dim); }
.form-input { padding: 8px 11px; border: 1px solid var(--border-bright); border-radius: 7px; background: var(--surface2); color: var(--text); font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 600; outline: none; width: 100%; box-sizing: border-box; transition: border-color 0.15s; }
.form-input:focus { border-color: var(--accent); }
textarea.form-input { resize: vertical; min-height: 60px; }
.add-btn { font-family: 'Nunito', sans-serif; font-size: 10px; font-weight: 700; padding: 3px 10px; background: var(--accent-dim); border: 1px solid var(--accent); border-radius: 5px; color: var(--accent); cursor: pointer; }
.spec-row { display: flex; align-items: center; gap: 8px; }
.spec-check-input { width: 16px; height: 16px; flex-shrink: 0; accent-color: var(--accent); }
.spec-input { flex: 1; }
.del-btn { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 14px; padding: 4px 6px; border-radius: 4px; flex-shrink: 0; }
.del-btn:hover { color: var(--red); background: var(--red-dim); }
.module-builder { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 12px; }
.mb-head { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.mb-name { flex: 1; }
.mb-color { width: 130px; flex-shrink: 0; }
.task-builder { display: flex; flex-direction: column; gap: 6px; padding-left: 8px; border-left: 2px solid var(--border-bright); }
.task-row { display: flex; gap: 6px; align-items: center; }
.task-name-input { flex: 1; }
.task-assign-input { width: 100px; flex-shrink: 0; }
.task-pct-input { width: 52px; flex-shrink: 0; text-align: center; }
.task-pct-lbl { font-size: 11px; color: var(--text-dim); font-family: 'Nunito Sans', sans-serif; }
.add-task-btn { font-family: 'Nunito', sans-serif; font-size: 10px; font-weight: 700; padding: 4px 10px; background: transparent; border: 1px dashed var(--border-bright); border-radius: 5px; color: var(--text-dim); cursor: pointer; align-self: flex-start; margin-top: 2px; }
.add-task-btn:hover { border-color: var(--accent); color: var(--accent); }
.form-error { background: var(--red-dim); border: 1px solid rgba(220,38,38,0.2); border-radius: 7px; padding: 10px 14px; color: var(--red); font-size: 12px; font-weight: 600; }
.btn-cancel { padding: 9px 20px; background: var(--surface3); border: 1px solid var(--border-bright); border-radius: 7px; font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 700; cursor: pointer; color: var(--text-mid); }
.btn-submit { padding: 9px 24px; background: var(--accent); border: none; border-radius: 7px; font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 700; cursor: pointer; color: #fff; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
</style>