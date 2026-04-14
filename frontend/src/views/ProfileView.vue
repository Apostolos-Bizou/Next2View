<template>
  <div class="content">
    <div class="profile-grid">

      <!-- LEFT: User Info -->
      <div class="panel">
        <div class="ph">
          <div class="ph-title">👤 Στοιχεία Λογαριασμού</div>
        </div>
        <div class="pb">
          <div class="avatar-big">{{ initials }}</div>
          <div class="form-group">
            <label>Ονομα</label>
            <input v-model="form.fullName" class="form-input" />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input v-model="form.email" type="email" class="form-input" />
          </div>
          <div class="form-group">
            <label>Role</label>
            <input :value="auth.user?.role" class="form-input" disabled />
          </div>
          <div v-if="saveMsg" :class="`save-msg ${saveMsg.type}`">{{ saveMsg.text }}</div>
          <button class="btn-submit" @click="saveProfile" :disabled="saving">
            {{ saving ? 'Αποθήκευση...' : 'Αποθήκευση' }}
          </button>
        </div>
      </div>

      <!-- RIGHT: Password + MFA -->
      <div style="display:flex;flex-direction:column;gap:14px;">

        <!-- Password Change -->
        <div class="panel">
          <div class="ph"><div class="ph-title">🔑 Αλλαγή Κωδικού</div></div>
          <div class="pb">
            <div class="form-group">
              <label>Τρέχων Κωδικός</label>
              <input v-model="pwForm.current" type="password" class="form-input" placeholder="••••••••" />
            </div>
            <div class="form-group">
              <label>Νέος Κωδικός</label>
              <input v-model="pwForm.newPw" type="password" class="form-input" placeholder="Τουλάχιστον 8 χαρακτήρες" />
            </div>
            <div class="form-group">
              <label>Επιβεβαίωση Νέου</label>
              <input v-model="pwForm.confirm" type="password" class="form-input" placeholder="Επανάληψη" />
            </div>
            <div v-if="pwMsg" :class="`save-msg ${pwMsg.type}`">{{ pwMsg.text }}</div>
            <button class="btn-submit" @click="changePassword" :disabled="pwSaving">
              {{ pwSaving ? 'Αποθήκευση...' : 'Αλλαγή Κωδικού' }}
            </button>
          </div>
        </div>

        <!-- MFA Status -->
        <div class="panel">
          <div class="ph"><div class="ph-title">🔒 Two-Factor Authentication</div></div>
          <div class="pb">
            <div :class="`mfa-status-badge ${mfaEnabled ? 'on' : 'off'}`">
              {{ mfaEnabled ? '✅ MFA Ενεργό' : '⚠ MFA Ανενεργό' }}
            </div>
            <p class="mfa-desc">{{ mfaEnabled ? 'Ο λογαριασμός σου προστατεύεται με 2FA.' : 'Ενεργοποίησε MFA για επιπλέον ασφάλεια.' }}</p>
          </div>
        </div>
      </div>

      <!-- PERMISSIONS -->
      <div class="panel" style="grid-column:1/-1;">
        <div class="ph"><div class="ph-title">✅ Δικαιώματα Πρόσβασης</div></div>
        <div class="pb">
          <div class="perms-grid">
            <div v-for="perm in allPerms" :key="perm.key" :class="`perm-card ${permStore.can(perm.key) || permStore.isCEO() ? 'active' : 'inactive'}`">
              <span class="perm-icon">{{ perm.icon }}</span>
              <span class="perm-label">{{ perm.label }}</span>
              <span class="perm-dot">{{ permStore.can(perm.key) || permStore.isCEO() ? '✓' : '✗' }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permissions'
import api from '@/services/api'

const auth = useAuthStore()
const permStore = usePermissionStore()

const form = ref({ fullName: '', email: '' })
const saving = ref(false)
const saveMsg = ref(null)

const pwForm = ref({ current: '', newPw: '', confirm: '' })
const pwSaving = ref(false)
const pwMsg = ref(null)

const mfaEnabled = ref(false)

const initials = computed(() => {
  const name = form.value.fullName || ''
  return name.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase() || 'AK'
})

onMounted(async () => {
  form.value.fullName = auth.user?.fullName || ''
  form.value.email = auth.user?.email || ''
  await permStore.loadMyPermissions()
  try {
    const res = await api.get('/auth/mfa/status')
    mfaEnabled.value = res.data.mfaEnabled
  } catch {}
})

async function saveProfile() {
  saving.value = true
  saveMsg.value = null
  try {
    const userId = auth.user?.id
    await api.put(`/admin/users/${userId}`, {
      fullName: form.value.fullName,
      email: form.value.email,
      role: auth.user?.role,
      active: true
    })
    auth.user.fullName = form.value.fullName
    auth.user.email = form.value.email
    saveMsg.value = { type: 'success', text: 'Τα στοιχεία αποθηκεύτηκαν!' }
  } catch (e) {
    saveMsg.value = { type: 'error', text: e.response?.data?.message || 'Σφάλμα αποθήκευσης.' }
  } finally { saving.value = false }
}

async function changePassword() {
  pwMsg.value = null
  if (pwForm.value.newPw !== pwForm.value.confirm) {
    pwMsg.value = { type: 'error', text: 'Οι κωδικοί δεν ταιριάζουν.' }
    return
  }
  if (pwForm.value.newPw.length < 8) {
    pwMsg.value = { type: 'error', text: 'Τουλάχιστον 8 χαρακτήρες.' }
    return
  }
  pwSaving.value = true
  try {
    await api.post('/auth/change-password', {
      currentPassword: pwForm.value.current,
      newPassword: pwForm.value.newPw
    })
    pwMsg.value = { type: 'success', text: 'Ο κωδικός άλλαξε!' }
    pwForm.value = { current: '', newPw: '', confirm: '' }
  } catch (e) {
    pwMsg.value = { type: 'error', text: e.response?.data?.message || 'Λάθος τρέχων κωδικός.' }
  } finally { pwSaving.value = false }
}

const allPerms = [
  { key: 'viewFinance',    icon: '$',  label: 'Βλέπει Finance' },
  { key: 'viewLegal',      icon: '▪',  label: 'Βλέπει Legal' },
  { key: 'viewDev',        icon: '✨', label: 'Βλέπει Developing' },
  { key: 'viewMarketing',  icon: '◆',  label: 'Βλέπει Marketing' },
  { key: 'viewFinancials', icon: '💰', label: 'Financial Data' },
  { key: 'viewCeoNotes',   icon: '📓', label: 'CEO Notes' },
  { key: 'updateTasks',    icon: '✎',  label: 'Update Tasks' },
  { key: 'uploadFiles',    icon: '📎', label: 'Upload Files' },
  { key: 'createProject',  icon: '+',  label: 'Δημιουργία Project' },
  { key: 'editProject',    icon: '✎',  label: 'Επεξεργασία Project' },
  { key: 'manageUsers',    icon: '👤', label: 'Manage Users' },
  { key: 'manageCompanies',icon: '🏢', label: 'Manage Companies' },
  { key: 'aiCeoReport',    icon: '⦿',  label: 'AI CEO Report' },
  { key: 'aiContract',     icon: '📄', label: 'AI Contract' },
]
</script>

<style scoped>
.content { padding:26px 32px; overflow-y:auto; flex:1; }
.profile-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
.panel { background:var(--surface); border:1px solid var(--border); border-radius:10px; overflow:hidden; }
.ph { padding:14px 20px; border-bottom:1px solid var(--border); background:var(--surface2); }
.ph-title { font-size:14px; font-weight:800; }
.pb { padding:20px; display:flex; flex-direction:column; gap:12px; }
.avatar-big { width:64px; height:64px; background:var(--accent); border-radius:12px; display:flex; align-items:center; justify-content:center; font-size:22px; font-weight:800; color:#fff; margin:0 auto 8px; }
.form-group { display:flex; flex-direction:column; gap:5px; }
.form-group label { font-family:"Nunito Sans",sans-serif; font-size:10px; font-weight:700; letter-spacing:1px; text-transform:uppercase; color:var(--text-dim); }
.form-input { padding:9px 12px; border:1px solid var(--border-bright); border-radius:7px; background:var(--surface2); color:var(--text); font-family:"Nunito",sans-serif; font-size:13px; }
.form-input:focus { outline:none; border-color:var(--accent); }
.form-input:disabled { opacity:0.5; cursor:not-allowed; }
.btn-submit { padding:10px 20px; background:var(--accent); border:none; border-radius:7px; color:#fff; font-family:"Nunito",sans-serif; font-size:13px; font-weight:700; cursor:pointer; }
.btn-submit:disabled { opacity:0.5; cursor:not-allowed; }
.save-msg { padding:8px 12px; border-radius:7px; font-size:12px; font-weight:600; }
.save-msg.success { background:var(--green-dim); color:var(--green); }
.save-msg.error { background:var(--red-dim); color:var(--red); }
.mfa-status-badge { display:inline-block; padding:6px 14px; border-radius:20px; font-size:13px; font-weight:700; }
.mfa-status-badge.on { background:var(--green-dim); color:var(--green); }
.mfa-status-badge.off { background:var(--red-dim); color:var(--red); }
.mfa-desc { font-size:12px; color:var(--text-dim); margin:0; }
.perms-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(180px,1fr)); gap:8px; }
.perm-card { display:flex; align-items:center; gap:8px; padding:10px 14px; border-radius:8px; border:1px solid var(--border); }
.perm-card.active { background:var(--green-dim); border-color:var(--green); }
.perm-card.inactive { background:var(--surface2); opacity:0.5; }
.perm-icon { font-size:16px; }
.perm-label { font-size:12px; font-weight:600; flex:1; }
.perm-dot { font-size:14px; font-weight:800; }
.perm-card.active .perm-dot { color:var(--green); }
.perm-card.inactive .perm-dot { color:var(--red); }
@media (max-width:768px) { .profile-grid { grid-template-columns:1fr !important; } .content { padding:14px !important; } }
</style>