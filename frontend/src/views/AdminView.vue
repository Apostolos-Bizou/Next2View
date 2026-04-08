<template>
  <div class="content">
    <div class="admin-tabs">
      <div v-for="tab in tabs" :key="tab.id"
        :class="['admin-tab', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id">
        {{ tab.icon }} {{ tab.label }}
      </div>
    </div>

    <!-- ── USERS ── -->
    <div v-if="activeTab === 'users'" class="admin-panel">
      <div class="ap-header">
        <div class="ap-title">👤 Διαχείριση Χρηστών</div>
        <button class="ap-btn" @click="openUserModal()">+ Νέος Χρήστης</button>
      </div>
      <div v-if="loadingUsers" class="ap-loading">Φόρτωση...</div>
      <table v-else class="admin-tbl">
        <thead><tr>
          <th>Όνομα</th><th>Email</th><th>Role</th><th>Εταιρεία</th><th>Department</th><th>Status</th><th></th>
        </tr></thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td><div class="u-name">{{ u.fullName }}</div></td>
            <td class="td-sm">{{ u.email }}</td>
            <td><span :class="`role-badge ${u.role.toLowerCase()}`">{{ u.role }}</span></td>
            <td class="td-sm">{{ coShort(u.companyName) }}</td>
            <td class="td-sm">{{ u.department || '—' }}</td>
            <td><span :class="`status-dot ${u.active ? 'active' : 'inactive'}`">{{ u.active ? 'Active' : 'Inactive' }}</span></td>
            <td>
              <button class="icon-btn" @click="openUserModal(u)" title="Edit">✎</button>
              <button class="icon-btn red" @click="toggleUser(u)" :title="u.active ? 'Deactivate' : 'Activate'">{{ u.active ? '⏸' : '▶' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── COMPANIES ── -->
    <div v-if="activeTab === 'companies'" class="admin-panel">
      <div class="ap-header">
        <div class="ap-title">🏢 Διαχείριση Εταιρειών</div>
        <button class="ap-btn" @click="openCoModal()">+ Νέα Εταιρεία</button>
      </div>
      <div v-if="loadingCos" class="ap-loading">Φόρτωση...</div>
      <table v-else class="admin-tbl">
        <thead><tr>
          <th>Εταιρεία</th><th>Code</th><th>Χρώμα</th><th>Projects</th><th>Avg %</th><th>Status</th><th></th>
        </tr></thead>
        <tbody>
          <tr v-for="co in companies" :key="co.id">
            <td>
              <div style="display:flex;align-items:center;gap:10px;">
                <div class="co-av" :style="`background:${co.color}20;color:${co.color};`">{{ co.code }}</div>
                <div class="u-name">{{ co.name }}</div>
              </div>
            </td>
            <td class="td-sm">{{ co.code }}</td>
            <td><div class="color-swatch" :style="`background:${co.color};`"></div></td>
            <td class="td-sm">{{ co.projectCount }}</td>
            <td class="td-sm" :style="`color:${co.color};font-weight:700;`">{{ co.avgCompletion }}%</td>
            <td><span class="status-dot active">Active</span></td>
            <td>
              <button class="icon-btn" @click="openCoModal(co)" title="Edit">✎</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── USER MODAL ── -->
    <div v-if="showUserModal" class="modal-overlay" @click.self="showUserModal=false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ editUser?.id ? 'Επεξεργασία Χρήστη' : 'Νέος Χρήστης' }}</div>
          <button class="modal-close" @click="showUserModal=false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group"><label>Ονομα *</label>
            <input v-model="userForm.fullName" class="form-input" placeholder="Απόστολος Βίζου" /></div>
          <div class="form-group"><label>Email *</label>
            <input v-model="userForm.email" type="email" class="form-input" placeholder="user@next2me.com" /></div>
          <div v-if="!editUser?.id" class="form-group"><label>Password *</label>
            <input v-model="userForm.password" type="password" class="form-input" placeholder="••••••••" /></div>
          <div class="form-row">
            <div class="form-group"><label>Role *</label>
              <select v-model="userForm.role" class="form-input">
                <option value="CEO">CEO</option>
                <option value="DEPT_HEAD">Department Head</option>
                <option value="VIEWER">Viewer</option>
              </select>
            </div>
            <div class="form-group"><label>Department</label>
              <select v-model="userForm.department" class="form-input">
                <option value="">—</option>
                <option value="finance">Finance</option>
                <option value="legal">Legal</option>
                <option value="dev">Developing</option>
                <option value="marketing">Marketing</option>
                <option value="management">Management</option>
              </select>
            </div>
          </div>
          <div class="form-group"><label>Εταιρεία</label>
            <select v-model="userForm.companyId" class="form-input">
              <option value="">—</option>
              <option v-for="co in companies" :key="co.id" :value="co.id">{{ co.name }}</option>
            </select>
          </div>
          <div v-if="modalError" class="form-error">{{ modalError }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showUserModal=false">Ακύρωση</button>
          <button class="btn-submit" :disabled="modalSaving" @click="saveUser">
            {{ modalSaving ? 'Αποθήκευση...' : 'Αποθήκευση' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ── COMPANY MODAL ── -->
    <div v-if="showCoModal" class="modal-overlay" @click.self="showCoModal=false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ editCo?.id ? 'Επεξεργασία Εταιρείας' : 'Νέα Εταιρεία' }}</div>
          <button class="modal-close" @click="showCoModal=false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group"><label>Όνομα *</label>
            <input v-model="coForm.name" class="form-input" placeholder="Polaris Financial Services" /></div>
          <div class="form-row">
            <div class="form-group"><label>Code * (2-5 chars)</label>
              <input v-model="coForm.code" class="form-input" placeholder="PF" maxlength="5" /></div>
            <div class="form-group"><label>Χρώμα</label>
              <input v-model="coForm.color" type="color" class="form-input color-input" /></div>
          </div>
          <div class="form-group"><label>Περιγραφή</label>
            <input v-model="coForm.description" class="form-input" placeholder="TPA maritime healthcare..." /></div>
          <div v-if="modalError" class="form-error">{{ modalError }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showCoModal=false">Ακύρωση</button>
          <button class="btn-submit" :disabled="modalSaving" @click="saveCompany">
            {{ modalSaving ? 'Αποθήκευση...' : 'Αποθήκευση' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useProjectStore } from '@/stores/projects'

const store = useProjectStore()
const activeTab = ref('users')
const tabs = [
  { id: 'users',     label: 'Χρήστες',   icon: '👤' },
  { id: 'companies', label: 'Εταιρείες', icon: '🏢' },
]

const users = ref([])
const companies = ref([])
const loadingUsers = ref(false)
const loadingCos = ref(false)

// User modal
const showUserModal = ref(false)
const editUser = ref(null)
const userForm = ref({})
const modalError = ref('')
const modalSaving = ref(false)

// Company modal
const showCoModal = ref(false)
const editCo = ref(null)
const coForm = ref({})

onMounted(async () => {
  await Promise.all([loadUsers(), loadCompanies()])
})

async function loadUsers() {
  loadingUsers.value = true
  try {
    const res = await api.get('/admin/users')
    users.value = res.data
  } catch { users.value = [] }
  finally { loadingUsers.value = false }
}

async function loadCompanies() {
  loadingCos.value = true
  try {
    const res = await api.get('/companies')
    companies.value = res.data
    store.companies = res.data
  } catch { companies.value = [] }
  finally { loadingCos.value = false }
}

function openUserModal(u = null) {
  editUser.value = u
  modalError.value = ''
  userForm.value = u
    ? { fullName: u.fullName, email: u.email, role: u.role, department: u.department || '', companyId: u.companyId || '' }
    : { fullName: '', email: '', password: '', role: 'DEPT_HEAD', department: '', companyId: '' }
  showUserModal.value = true
}

async function saveUser() {
  modalError.value = ''
  if (!userForm.value.fullName || !userForm.value.email) {
    modalError.value = 'Όνομα και Email είναι υποχρεωτικά.'
    return
  }
  modalSaving.value = true
  try {
    if (editUser.value?.id) {
      await api.put(`/admin/users/${editUser.value.id}`, userForm.value)
    } else {
      await api.post('/admin/users', userForm.value)
    }
    showUserModal.value = false
    await loadUsers()
  } catch (e) {
    modalError.value = e.response?.data?.message || 'Σφάλμα αποθήκευσης.'
  } finally { modalSaving.value = false }
}

async function toggleUser(u) {
  try {
    await api.put(`/admin/users/${u.id}`, { ...u, active: !u.active })
    await loadUsers()
  } catch {}
}

function openCoModal(co = null) {
  editCo.value = co
  modalError.value = ''
  coForm.value = co
    ? { name: co.name, code: co.code, color: co.color, description: co.description || '' }
    : { name: '', code: '', color: '#3b82f6', description: '' }
  showCoModal.value = true
}

async function saveCompany() {
  modalError.value = ''
  if (!coForm.value.name || !coForm.value.code) {
    modalError.value = 'Όνομα και Code είναι υποχρεωτικά.'
    return
  }
  modalSaving.value = true
  try {
    if (editCo.value?.id) {
      await api.put(`/companies/${editCo.value.id}`, coForm.value)
    } else {
      await api.post('/companies', coForm.value)
    }
    showCoModal.value = false
    await loadCompanies()
  } catch (e) {
    modalError.value = e.response?.data?.message || 'Σφάλμα αποθήκευσης.'
  } finally { modalSaving.value = false }
}

function coShort(name) {
  if (!name) return '—'
  const m = { 'Polaris Financial Services':'Polaris Financial', 'Crossworld Marine Services':'Crossworld Marine', 'WiMAS Training Center':'WiMAS', 'Varship Management':'Varship' }
  return m[name] || name.split(' ').slice(0,2).join(' ')
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.admin-tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.admin-tab { padding: 8px 20px; border-radius: 8px; border: 1px solid var(--border-bright); background: var(--surface2); font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.15s; color: var(--text-mid); }
.admin-tab:hover { background: var(--surface3); }
.admin-tab.active { background: var(--accent-dim); border-color: var(--accent); color: var(--accent); }
.admin-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
.ap-header { padding: 16px 22px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.ap-title { font-size: 15px; font-weight: 800; }
.ap-btn { padding: 7px 16px; background: var(--accent); border: none; border-radius: 7px; color: #fff; font-family: 'Nunito', sans-serif; font-size: 12px; font-weight: 700; cursor: pointer; }
.ap-loading { padding: 32px; text-align: center; color: var(--text-dim); font-size: 13px; }
.admin-tbl { width: 100%; border-collapse: collapse; }
.admin-tbl th { font-family: "Nunito Sans", sans-serif; font-size: 10px; letter-spacing: 1.5px; color: var(--text-dim); text-align: left; padding: 12px 14px; border-bottom: 2px solid var(--border); font-weight: 700; text-transform: uppercase; }
.admin-tbl td { padding: 14px 14px; border-bottom: 1px solid var(--border); vertical-align: middle; }
.admin-tbl tr:last-child td { border-bottom: none; }
.admin-tbl tr:hover td { background: var(--accent-dim); }
.u-name { font-size: 14px; font-weight: 700; }
.td-sm { font-size: 12px; color: var(--text-mid); font-family: "Nunito Sans", sans-serif; }
.role-badge { font-size: 9px; font-weight: 700; padding: 3px 8px; border-radius: 5px; letter-spacing: 0.5px; }
.role-badge.ceo { background: #eff6ff; color: #1d4ed8; }
.role-badge.dept_head { background: #ecfdf5; color: #065f46; }
.role-badge.viewer { background: var(--surface2); color: var(--text-dim); border: 1px solid var(--border-bright); }
.status-dot { font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 5px; }
.status-dot.active { background: var(--green-dim); color: var(--green); }
.status-dot.inactive { background: var(--red-dim); color: var(--red); }
.icon-btn { background: none; border: 1px solid var(--border-bright); border-radius: 5px; padding: 4px 8px; cursor: pointer; font-size: 13px; color: var(--text-mid); margin-right: 4px; transition: all 0.15s; }
.icon-btn:hover { background: var(--surface3); }
.icon-btn.red:hover { background: var(--red-dim); border-color: var(--red); color: var(--red); }
.co-av { width: 32px; height: 32px; border-radius: 7px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; flex-shrink: 0; }
.color-swatch { width: 24px; height: 24px; border-radius: 5px; border: 1px solid var(--border-bright); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 2000; }
.modal { background: var(--surface); border-radius: 14px; width: 500px; max-width: 95vw; box-shadow: 0 20px 60px rgba(0,0,0,0.2); overflow: hidden; }
.modal-header { padding: 18px 24px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.modal-title { font-size: 15px; font-weight: 800; }
.modal-close { background: none; border: none; font-size: 18px; cursor: pointer; color: var(--text-dim); padding: 4px 8px; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 12px; }
.modal-footer { padding: 14px 24px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 10px; background: var(--surface2); }
.form-group { display: flex; flex-direction: column; gap: 5px; flex: 1; }
.form-row { display: flex; gap: 12px; }
.form-group label { font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: var(--text-dim); }
.form-input { padding: 8px 11px; border: 1px solid var(--border-bright); border-radius: 7px; background: var(--surface2); color: var(--text); font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 600; outline: none; width: 100%; box-sizing: border-box; }
.form-input:focus { border-color: var(--accent); }
.color-input { padding: 2px; height: 38px; cursor: pointer; }
.form-error { background: var(--red-dim); border: 1px solid rgba(220,38,38,0.2); border-radius: 7px; padding: 10px 14px; color: var(--red); font-size: 12px; font-weight: 600; }
.btn-cancel { padding: 9px 20px; background: var(--surface3); border: 1px solid var(--border-bright); border-radius: 7px; font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 700; cursor: pointer; color: var(--text-mid); }
.btn-submit { padding: 9px 24px; background: var(--accent); border: none; border-radius: 7px; font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 700; cursor: pointer; color: #fff; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
</style>