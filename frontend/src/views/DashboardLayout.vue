<template>
  <div class="shell">
    <!-- MOBILE HEADER -->
    <div class="mobile-header">
      <button class="hamburger-btn" @click="sidebarOpen = !sidebarOpen">
        <span></span><span></span><span></span>
      </button>
      <div class="mobile-logo">Next2<span>View</span></div>
      <router-link to="/notifications" class="mobile-notif-btn">
        🔔<span v-if="activeBadgeCount > 0" class="mobile-notif-badge">{{ activeBadgeCount }}</span>
      </router-link>
    </div>

    <!-- MOBILE OVERLAY -->
    <div class="sidebar-overlay" v-if="sidebarOpen" @click="sidebarOpen = false"></div>

    <aside class="sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="logo">
        <div class="logo-mark">Next2me Group</div>
        <div class="logo-name">Next<span>2</span>View</div>
      </div>

      <nav class="nav">
        <div class="nav-section">CEO View</div>
        <router-link to="/" class="nav-item" exact-active-class="active">
          <span class="nav-ico">◈</span>Dashboard
        </router-link>
        <router-link to="/projects" class="nav-item" :class="route.path==='/projects' && !route.query.category && !route.query.companyId ? 'active' : ''">
          <span class="nav-ico">⬡</span>All Projects
          <span class="nav-count">{{ store.projects.length }}</span>
        </router-link>
        <router-link to="/guide" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:#f6ad55;">?</span>Guide
        </router-link>
        <router-link to="/notifications" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--yellow);">🔔</span>Notifications
          <span v-if="activeBadgeCount > 0" class="nav-count" style="background:var(--red);color:#fff;">{{ activeBadgeCount }}</span>
        </router-link>
        <router-link v-if="permStore.isCEO()" to="/admin" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--text-dim);">⚙</span>Admin
        </router-link>
        <router-link v-if="permStore.isCEO()" to="/reports" class="nav-item" active-class="active">
          <span class="nav-ico" style="color:var(--accent);">📊</span>Reports
        </router-link>

        <div v-if="['finance','legal','dev','marketing'].some(cat => permStore.canViewCategory(cat))" class="nav-section" style="margin-top:6px;">{{ tt('sidebar.categories') }}</div>
        <div v-if="permStore.canViewCategory('finance')" :class="['nav-item', route.query.category==='finance' ? 'active' : '']"
          @click="router.push('/projects?category=finance')">
          <span class="nav-ico" style="color:var(--finance);">$</span>Finance
          <span class="nav-count">{{ store.byCategory('finance').length }}</span>
        </div>
        <div v-if="permStore.canViewCategory('legal')" :class="['nav-item', route.query.category==='legal' ? 'active' : '']"
          @click="router.push('/projects?category=legal')">
          <span class="nav-ico" style="color:var(--legal);">⚖</span>Legal
          <span class="nav-count">{{ store.byCategory('legal').length }}</span>
        </div>
        <div v-if="permStore.canViewCategory('dev')" :class="['nav-item', route.query.category==='dev' ? 'active' : '']"
          @click="router.push('/projects?category=dev')">
          <span class="nav-ico" style="color:var(--dev);">⌨</span>Developing
          <span class="nav-count">{{ store.byCategory('dev').length }}</span>
        </div>
        <div v-if="permStore.canViewCategory('marketing')" :class="['nav-item', route.query.category==='marketing' ? 'active' : '']"
          @click="router.push('/projects?category=marketing')">
          <span class="nav-ico" style="color:var(--marketing);">◈</span>Marketing
          <span class="nav-count">{{ store.byCategory('marketing').length }}</span>
        </div>

        <div class="nav-section" style="margin-top:6px;">{{ tt('sidebar.companies') }}</div>
        <div
          v-for="co in store.companies" :key="co.id"
          :class="['nav-item', route.query.companyId===co.id ? 'active' : '']"
          @click="router.push(`/projects?companyId=${co.id}`)"
        >
          <span class="nav-ico" :style="`color:${co.color};`">{{ coIcon(co.code) }}</span>
          {{ coShortName(co.name) }}
          <span class="nav-count">{{ co.projectCount || 0 }}</span>
        </div>
      </nav>

      <div class="notif-row" @click="router.push('/notifications')" style="cursor:pointer;">
        <div class="notif-label">Notifications</div>
        <div style="display:flex;align-items:center;gap:6px;">
          <span v-if="activeBadgeCount > 0" class="notif-badge">{{ activeBadgeCount }}</span>
          <span class="notif-bell" :title="tt('nav.notifTooltip')">🔔</span>
        </div>
      </div>

      <div class="sidebar-actions">
        <button v-if="permStore.isCEO() || permStore.can('aiCeoReport')" class="btn-sidebar btn-ai" @click="openAiReport" :title="tt('tooltips.aiReport')">
          <span style="font-size:16px;">✦</span> AI Report
        </button>
        <button v-if="permStore.isCEO() || permStore.can('createProject')" class="btn-sidebar" @click="openNewProject" :title="tt('tooltips.newProject')">+ New Project</button>
        <div v-if="permStore.isCEO() || permStore.can('manageCompanies') || permStore.can('manageUsers')" style="display:grid;grid-template-columns:1fr 1fr;gap:6px;">
          <button v-if="permStore.isCEO() || permStore.can('manageCompanies')" class="btn-sidebar" style="font-size:9px;" @click="openNewCompany" :title="tt('tooltips.newCompany')">+ Company</button>
          <button v-if="permStore.isCEO() || permStore.can('manageUsers')" class="btn-sidebar" style="font-size:9px;" @click="openNewUser" :title="tt('tooltips.newUser')">+ User</button>
        </div>
      </div>

      <!-- Language Switcher -->
      <div class="lang-switcher">
        <button :class="['lang-btn', { active: locale === 'el' }]" @click.stop="switchLang('el')"><img src="https://flagcdn.com/w20/gr.png" width="14" height="10" alt="GR" style="vertical-align:middle;margin-right:4px;border-radius:1px;">EL</button>
        <button :class="['lang-btn', { active: locale === 'en' }]" @click.stop="switchLang('en')"><img src="https://flagcdn.com/w20/gb.png" width="14" height="10" alt="EN" style="vertical-align:middle;margin-right:4px;border-radius:1px;">EN</button>
      </div>

      <div class="sidebar-user" @click="router.push('/profile')" style="cursor:pointer;" :title="tt('tooltips.profile')">
        <div class="avatar">{{ initials }}</div>
        <div>
          <div class="user-name">{{ auth.user?.fullName?.split(' ')[0] || 'CEO' }}</div>
          <div class="user-role">{{ auth.user?.role === 'CEO' ? 'CEO · Full Access' : auth.user?.role || 'Viewer' }}</div>
        </div>
        <button class="mfa-btn" @click="openMfaModal" title="MFA">🔐</button>
        <button class="logout-btn" @click="handleLogout" title="Logout">↩</button>
      </div>
    </aside>

    <main class="main">
      <div class="topbar">
        <div>
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-subtitle">{{ pageSubtitle }}</div>
        </div>
        <button v-if="permStore.isCEO() || permStore.can('createProject')" class="topbar-btn" @click="openNewProject">+ New Project</button>
      </div>
      <router-view />
    </main>

    
    <!-- MFA NUDGE POPUP -->
    


    <!-- MFA SUCCESS POPUP -->
    

<!-- ════ NEW PROJECT MODAL ════ -->
    <div v-if="showNewProject" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-lg">
        <div class="modal-header">
          <div class="modal-title">+ New Project</div>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <!-- BASIC INFO -->
          <div class="form-section-title">{{ tt('form.basicInfo') }}</div>
          <div class="form-group">
            <label>{{ tt('form.title') }} *</label>
            <input v-model="form.title" :placeholder="tt('form.titlePlaceholder')" class="form-input" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>{{ tt('form.company') }} *</label>
              <select v-model="form.companyId" class="form-input">
                <option value="">{{ tt('form.selectCompany') }}</option>
                <option v-for="co in store.companies" :key="co.id" :value="co.id">{{ co.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>{{ tt('form.category') }} *</label>
              <select v-model="form.category" class="form-input">
                <option value="">{{ tt('form.selectCategory') }}</option>
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
              <label>{{ tt('form.startDate') }}</label>
              <input v-model="form.startDate" type="date" class="form-input" />
            </div>
            <div class="form-group">
              <label>Deadline</label>
              <input v-model="form.deadline" type="date" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>{{ tt('form.contractDesc') }}</label>
            <RichTextEditor v-model="form.contractDesc" :placeholder="tt('form.contractDescPlaceholder')" min-height="100px" />
          </div>

          <!-- SPECS -->
          <div class="form-section-title" style="margin-top:16px;">
            Specifications
            <button class="add-btn" @click="addSpec">{{ tt('form.addSpec') }}</button>
          </div>
          <div v-for="(s, i) in form.specs" :key="i" class="spec-row-full">
            <div class="spec-row">
              <input type="checkbox" v-model="s.isDone" class="spec-check-input" />
              <input v-model="s.description" placeholder="Specification..." class="form-input spec-input" />
              <button class="del-btn" @click="form.specs.splice(i, 1)">✕</button>
            </div>
            <div class="spec-dates">
              <div class="spec-date-field">
                <label class="spec-date-lbl">{{ tt('form.specStart') }}</label>
                <input v-model="s.startDate" type="date" class="form-input spec-date-input" />
              </div>
              <div class="spec-date-field">
                <label class="spec-date-lbl">{{ tt('form.specEnd') }}</label>
                <input v-model="s.endDate" type="date" class="form-input spec-date-input" />
              </div>
            </div>
            <div style="margin-top:4px;"><RichTextEditor v-model="s.notes" :placeholder="tt('form.specNotes')" compact min-height="60px" /></div>
          </div>

          <!-- MODULES & TASKS -->
          <div class="form-section-title" style="margin-top:16px;">
            Modules & Tasks
            <button class="add-btn" @click="addModule" :title="tt('form.addModuleTooltip')">+ Module</button>
          </div>
          <div v-for="(m, mi) in form.modules" :key="mi" class="module-builder">
            <div class="mb-head">
              <input v-model="m.name" :placeholder="tt('form.moduleName')" class="form-input mb-name" />
              <select v-model="m.color" class="form-input mb-color">
                <option value="finance">$ Finance</option>
                <option value="legal">⚖ Legal</option>
                <option value="dev">⌨ Dev</option>
                <option value="marketing">◈ Marketing</option>
              </select>
              <button class="del-btn" @click="form.modules.splice(mi, 1)">✕</button>
            </div>
            <div style="margin:4px 0 8px 0;"><RichTextEditor v-model="m.notes" :placeholder="tt('form.moduleNotes')" compact min-height="60px" /></div>
            <div class="task-builder">
              <div v-for="(t, ti) in m.tasks" :key="ti" class="task-row">
                <div style="display:flex;flex-direction:column;gap:4px;flex:1;">
                  <div style="display:flex;gap:6px;align-items:center;">
                    <input v-model="t.name" placeholder="Task..." class="form-input task-name-input" />
                    <input v-model="t.assignee" placeholder="Assignee" class="form-input task-assign-input" />
                    <input v-model.number="t.progress" type="number" min="0" max="100" class="form-input task-pct-input" />
                    <span class="task-pct-lbl">%</span>
                    <button class="del-btn" @click="m.tasks.splice(ti, 1)">✕</button>
                  </div>
                  <div style="display:flex;gap:6px;align-items:center;">
                    <label style="font-size:10px;color:var(--text-dim);min-width:50px;">{{ tt('form.taskStart') }}</label>
                    <input v-model="t.startDate" type="date" class="form-input" style="flex:1;font-size:11px;padding:4px 6px;" />
                    <label style="font-size:10px;color:var(--text-dim);min-width:30px;">{{ tt('form.taskEnd') }}</label>
                    <input v-model="t.endDate" type="date" class="form-input" style="flex:1;font-size:11px;padding:4px 6px;" />
                  </div>
                </div>
              </div>
              <button class="add-task-btn" @click="addTask(m)">+ Task</button>
            </div>
          </div>

          <div v-if="formError" class="form-error">{{ formError }}</div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">{{ tt('form.cancel') }}</button>
          <button class="btn-submit" :disabled="submitting" @click="submitProject">
            {{ submitting ? tt('form.creating') : tt('form.createProject') }}
          </button>
        </div>
      </div>
    </div>
  <!-- ════ AI REPORT MODAL ════ -->
  <div v-if="showAiReport" class="modal-overlay" @click.self="showAiReport=false">
    <div class="modal modal-ai">
      <div class="modal-header">
        <div class="modal-title">✦ AI CEO Report — Next2me Group</div>
        <button class="modal-close" @click="showAiReport=false">✕</button>
      </div>
      <div class="modal-body ai-body">
        <div v-if="aiLoading" class="ai-loading">
          <div class="ai-spinner"></div>
          <div class="ai-loading-txt">{{ tt('ai.analyzing', { count: store.projects.length }) }}</div>
        </div>
        <div v-else-if="aiReport" class="ai-report" v-html="renderMarkdown(aiReport)"></div>
        <div v-else class="ai-error">{{ tt('ai.reportFailed') }}</div>
      </div>
      <div class="modal-footer" v-if="!aiLoading">
        <button class="btn-cancel" @click="showAiReport=false">{{ tt('ai.close') }}</button>
        <button class="btn-submit" @click="loadAiReport">↺ {{ tt('ai.refresh') }}</button>
      </div>
    </div>
  </div>

  </div>
  <!-- MFA MODAL -->
  <div v-if="showMfaModal" class="modal-overlay" @click.self="showMfaModal=false">
    <div class="modal" style="width:460px;">
      <div class="modal-header">
        <div class="modal-title">🔐 Two-Factor Authentication</div>
        <button class="modal-close" @click="showMfaModal=false">✕</button>
      </div>
      <div class="modal-body">
        <div v-if="!mfaSetupData && !mfaEnabled" class="mfa-info">
          <div class="mfa-status off">⚠ {{ tt('mfa.disabled') }}</div>
          <p class="mfa-desc">{{ tt('mfa.enableDesc') }}</p>
          <button class="btn-submit" @click="setupMfa" :disabled="mfaLoading">{{ mfaLoading ? "..." : tt('mfa.enableBtn') }}</button>
        </div>
        <div v-if="mfaSetupData" class="mfa-setup">
          <div class="mfa-status">📱 {{ tt('mfa.scanQR') }}</div>
          <div class="mfa-qr-wrap">
            <img :src="qrCodeUrl" class="mfa-qr" alt="QR" />
          </div>
          <div class="mfa-secret-box">
            <span class="mfa-secret-label">Manual key:</span>
            <code class="mfa-secret">{{ mfaSetupData.secret }}</code>
          </div>
          <p class="mfa-desc">{{ tt('mfa.scanDesc') }}</p>
          <input v-model="mfaCode" type="text" maxlength="6" placeholder="000000" class="form-input mfa-code-input" />
          <div v-if="mfaError" class="form-error">{{ mfaError }}</div>
          <button class="btn-submit" @click="verifyMfa" :disabled="mfaLoading || mfaCode.length !== 6">{{ mfaLoading ? tt('mfa.verifying') : tt('mfa.verifyBtn') }}</button>
        </div>
        <div v-if="mfaEnabled && !mfaSetupData" class="mfa-info">
          <div class="mfa-status on">✅ {{ tt('mfa.enabled') }}</div>
          <p class="mfa-desc">{{ tt('mfa.protectedDesc') }}</p>
          <input v-model="mfaCode" type="text" maxlength="6" :placeholder="tt('mfa.disablePlaceholder')" class="form-input mfa-code-input" />
          <button class="btn-cancel" @click="disableMfa" :disabled="mfaLoading || mfaCode.length !== 6">{{ mfaLoading ? "..." : tt('mfa.disableBtn') }}</button>
        </div>
      </div>
    </div>
  </div>

  <!-- MFA MODAL -->
  <div v-if="showMfaModal" class="modal-overlay" @click.self="showMfaModal=false">
    <div class="modal" style="width:460px;">
      <div class="modal-header">
        <div class="modal-title">🔐 Two-Factor Authentication</div>
        <button class="modal-close" @click="showMfaModal=false">✕</button>
      </div>
      <div class="modal-body">
        <div v-if="!mfaSetupData && !mfaEnabled" class="mfa-info">
          <div class="mfa-status off">⚠ {{ tt('mfa.disabledAlt') }}</div>
          <p class="mfa-desc">{{ tt('mfa.enableDescAlt') }}</p>
          <button class="btn-submit" @click="setupMfa" :disabled="mfaLoading">{{ mfaLoading ? "..." : "Ενεργοποίηση MFA" }}</button>
        </div>
        <div v-if="mfaSetupData" class="mfa-setup">
          <div class="mfa-status">📱 {{ tt('mfa.scanQR') }}</div>
          <div class="mfa-qr-wrap">
            <img :src="qrCodeUrl" class="mfa-qr" alt="QR Code" />
          </div>
          <div class="mfa-secret-box">
            <span class="mfa-secret-label">Manual key:</span>
            <code class="mfa-secret">{{ mfaSetupData.secret }}</code>
          </div>
          <p class="mfa-desc">{{ tt('mfa.scanDescAlt') }}</p>
          <input v-model="mfaCode" type="text" maxlength="6" placeholder="000000" class="form-input mfa-code-input" />
          <div v-if="mfaError" class="form-error">{{ mfaError }}</div>
          <button class="btn-submit" @click="verifyMfa" :disabled="mfaLoading || mfaCode.length !== 6">{{ mfaLoading ? "Επαλήθευση..." : "Επαλήθευση & Ενεργοποίηση" }}</button>
        </div>
        <div v-if="mfaEnabled && !mfaSetupData" class="mfa-info">
          <div class="mfa-status on">✅ {{ tt('mfa.enabledAlt') }}</div>
          <p class="mfa-desc">{{ tt('mfa.protectedDescAlt') }}</p>
          <input v-model="mfaCode" type="text" maxlength="6" :placeholder="tt('mfa.disablePlaceholder')" class="form-input mfa-code-input" />
          <button class="btn-cancel" @click="disableMfa" :disabled="mfaLoading || mfaCode.length !== 6">{{ mfaLoading ? "..." : "Απενεργοποίηση MFA" }}</button>
        </div>
      </div>
    </div>
  </div>

  <!-- NEW COMPANY MODAL -->
  <div v-if="showNewCompany" class="modal-overlay" @click.self="showNewCompany=false">
    <div class="modal" style="width:460px;">
      <div class="modal-header">
        <div class="modal-title">🏢 New Company</div>
        <button class="modal-close" @click="showNewCompany=false">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>Company Name *</label>
          <input v-model="companyForm.name" type="text" class="form-input" placeholder="e.g. Polaris Financial Services" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Code *</label>
            <input v-model="companyForm.code" type="text" class="form-input" placeholder="e.g. PFS" maxlength="6" />
          </div>
          <div class="form-group">
            <label>Color</label>
            <select v-model="companyForm.color" class="form-input">
              <option value="finance">Finance (Green)</option>
              <option value="legal">Legal (Orange)</option>
              <option value="dev">Dev (Purple)</option>
              <option value="marketing">Marketing (Pink)</option>
            </select>
          </div>
        </div>
        <div v-if="companyError" class="form-error">{{ companyError }}</div>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" @click="showNewCompany=false">Cancel</button>
        <button class="btn-submit" @click="saveNewCompany" :disabled="companySaving">
          {{ companySaving ? 'Saving...' : 'Create Company' }}
        </button>
      </div>
    </div>
  </div>

  <!-- NEW USER MODAL -->
  <div v-if="showNewUser" class="modal-overlay" @click.self="showNewUser=false">
    <div class="modal" style="width:460px;">
      <div class="modal-header">
        <div class="modal-title">👤 New User</div>
        <button class="modal-close" @click="showNewUser=false">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-row">
          <div class="form-group">
            <label>First Name *</label>
            <input v-model="userForm.firstName" type="text" class="form-input" placeholder="Apostolos" />
          </div>
          <div class="form-group">
            <label>Last Name *</label>
            <input v-model="userForm.lastName" type="text" class="form-input" placeholder="Bizou" />
          </div>
        </div>
        <div class="form-group">
          <label>Email *</label>
          <input v-model="userForm.email" type="email" class="form-input" placeholder="user@next2me.com" />
        </div>
        <div class="form-group">
          <label>Password *</label>
          <input v-model="userForm.password" type="password" class="form-input" placeholder="Min 8 characters" />
        </div>
        <div class="form-group">
          <label>Role</label>
          <select v-model="userForm.role" class="form-input">
            <option value="CEO">CEO</option>
            <option value="MANAGER">Manager</option>
            <option value="MEMBER">Member</option>
          </select>
        </div>
        <div v-if="userError" class="form-error">{{ userError }}</div>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" @click="showNewUser=false">Cancel</button>
        <button class="btn-submit" @click="saveNewUser" :disabled="userSaving">
          {{ userSaving ? 'Saving...' : 'Create User' }}
        </button>
      </div>
    </div>
  </div>


  <!-- SSE TOAST NOTIFICATIONS -->
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/projects'
import { usePermissionStore } from '@/stores/permissions'
import api from '@/services/api'
import { connectSSE, disconnectSSE, onActivity } from '@/services/sseService'
import { useI18n } from 'vue-i18n'
import RichTextEditor from '@/components/RichTextEditor.vue'

const auth = useAuthStore()
const store = useProjectStore()
const permStore = usePermissionStore()
const router = useRouter()
const route = useRoute()
const { t: tt } = useI18n()

// ════ NEW COMPANY ════
const showNewCompany = ref(false)
const companyForm = ref({ name: '', code: '', color: 'finance' })
const companyError = ref('')
const companySaving = ref(false)

function openNewCompany() {
  companyForm.value = { name: '', code: '', color: 'finance' }
  companyError.value = ''
  showNewCompany.value = true
}

async function saveNewCompany() {
  if (!companyForm.value.name.trim() || !companyForm.value.code.trim()) {
    companyError.value = 'Name and Code are required.'; return
  }
  companySaving.value = true
  companyError.value = ''
  try {
    await api.post('/companies', {
      name: companyForm.value.name,
      code: companyForm.value.code.toUpperCase(),
      color: companyForm.value.color
    })
    showNewCompany.value = false
    await store.fetchCompanies()
  } catch(e) {
    companyError.value = e.response?.data?.message || 'Error creating company.'
  } finally { companySaving.value = false }
}

// ════ NEW USER ════
const showNewUser = ref(false)
const userForm = ref({ firstName: '', lastName: '', email: '', password: '', role: 'MEMBER' })
const userError = ref('')
const userSaving = ref(false)

function openNewUser() {
  userForm.value = { firstName: '', lastName: '', email: '', password: '', role: 'MEMBER' }
  userError.value = ''
  showNewUser.value = true
}

async function saveNewUser() {
  if (!userForm.value.firstName || !userForm.value.email || !userForm.value.password) {
    userError.value = 'First name, email and password are required.'; return
  }
  userSaving.value = true
  userError.value = ''
  try {
    await api.post('/auth/register', {
      firstName: userForm.value.firstName,
      lastName: userForm.value.lastName,
      email: userForm.value.email,
      password: userForm.value.password,
      role: userForm.value.role
    })
    showNewUser.value = false
  } catch(e) {
    userError.value = e.response?.data?.message || 'Error creating user.'
  } finally { userSaving.value = false }
}
const sidebarOpen = ref(false)
const showMfaModal = ref(false)

// Language switcher
const { locale } = useI18n()
function switchLang(lang) {
  locale.value = lang
  localStorage.setItem('n2v_locale', lang)
}

// Close sidebar on route change
watch(route, () => { sidebarOpen.value = false })

const showNewProject = ref(false)
const submitting = ref(false)
const formError = ref('')

const emptyForm = () => ({
  title: '', companyId: '', category: '', budget: '', startDate: '', deadline: '',
  contractDesc: '', specs: [], modules: []
})
const form = ref(emptyForm())

let unsubActivity = null

onMounted(async () => {
  await Promise.all([store.fetchProjects(), store.fetchCompanies(), permStore.loadMyPermissions()])
  // Connect SSE for real-time push notifications
  connectSSE()
  unsubActivity = onActivity(() => {
    // Dispatch event for NotificationsView to reload activity log
    window.dispatchEvent(new Event('sse-activity-received'))
  })
})

onUnmounted(() => {
  disconnectSSE()
  if (unsubActivity) unsubActivity()
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
  form.value.specs.push({ description: '', notes: '', isDone: false, sortOrder: form.value.specs.length, startDate: null, endDate: null })
}
function addModule() {
  form.value.modules.push({ name: '', notes: '', color: form.value.category || 'dev', sortOrder: form.value.modules.length, tasks: [] })
}
function addTask(m) {
  m.tasks.push({ name: '', assignee: '', progress: 0, isDone: false, isBlocked: false, blockNote: '', comment: '', deadline: null, startDate: null, endDate: null, startWeek: null, durationWeeks: 1, sortOrder: m.tasks.length })
}

async function submitProject() {
  formError.value = ''
  if (!form.value.title || !form.value.companyId || !form.value.category) {
    formError.value = tt('form.errRequired')
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
      specs: form.value.specs.filter(s => s.description.trim()).map(s => ({
        description: s.description,
        isDone: s.isDone,
        sortOrder: s.sortOrder,
        startDate: s.startDate || null,
        endDate: s.endDate || null
      })),
      modules: form.value.modules.filter(m => m.name.trim()).map((m, mi) => ({
        name: m.name, color: m.color, sortOrder: mi,
        tasks: m.tasks.filter(t => t.name.trim()).map((t, ti) => ({ startDate: t.startDate || null, endDate: t.endDate || null,
          ...t, sortOrder: ti, isDone: t.progress === 100,
          startWeek: t.startWeek || (mi + 1),
          durationWeeks: t.durationWeeks || 1
        }))
      }))
    }
    const proj = await store.createProject(payload)
    closeModal()
    router.push(`/projects/${proj.id}`)
  } catch (e) {
    formError.value = e.response?.data?.message || tt('form.errCreate')
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


const showMfaNudge = ref(false)
const showMfaSuccess = ref(false)
const firstName = computed(() => {
  const full = auth.user?.fullName || ''
  const first = full.split(' ')[0] || 'User'
  // Greek vocative approximation
  if (first.endsWith('ος')) return first.slice(0, -2) + 'ο'
  if (first.endsWith('ας')) return first.slice(0, -1)
  if (first.endsWith('ης')) return first.slice(0, -1)
  return first
})

function checkMfaNudge() {
  // Show only if user has NO MFA and hasn't dismissed
  const dismissed = sessionStorage.getItem('mfa_nudge_dismissed')
  if (dismissed) return
  // Check if user has MFA - if mfaEnabled is false, show nudge
  if (auth.user && !auth.user.mfaEnabled) {
    showMfaNudge.value = true
  }
}

function dismissMfaNudge() {
  showMfaNudge.value = false
  sessionStorage.setItem('mfa_nudge_dismissed', 'true')
}

function goToGuide() {
  showMfaNudge.value = false
  router.push('/guide')
}

// Check on mount after small delay
setTimeout(() => checkMfaNudge(), 1500)

window.addEventListener('mfa-activated', () => {
  showMfaNudge.value = false
  showMfaSuccess.value = true
  sessionStorage.setItem('mfa_nudge_dismissed', 'true')
})

// Badge: reads count from NotificationsView via global event
const activeBadgeCount = ref(window.__n2vAlertCount || 0)
window.addEventListener('alert-count-changed', () => {
  activeBadgeCount.value = window.__n2vAlertCount || 0
})
window.addEventListener('alerts-dismissed', () => {
  setTimeout(() => { activeBadgeCount.value = window.__n2vAlertCount || 0 }, 100)
})

const initials = computed(() => {
  const name = auth.user?.fullName || ''
  return name.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase() || 'AB'
})

const pageTitle = computed(() => ({
  Dashboard: 'Group Dashboard',
  Projects: 'All Projects',
  ProjectDetail: store.selectedProject?.title || 'Project Detail',
  Guide: tt('sidebar.guide'),
}[route.name] || 'Next2View'))

const pageSubtitle = computed(() => ({
  Dashboard: 'All companies · All categories · CEO view',
  Projects: `${store.projects.length} projects`,
  ProjectDetail: store.selectedProject ? `${store.selectedProject.companyName} · ${store.selectedProject.category}` : '',
  Guide: tt('tooltips.guideFull'),
}[route.name] || ''))

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}

// ════ AI REPORT ════
const showAiReport = ref(false)
const aiLoading = ref(false)
const aiReport = ref(null)

async function openAiReport() {
  showAiReport.value = true
  if (!aiReport.value) await loadAiReport()
}

async function loadAiReport() {
  aiLoading.value = true
  aiReport.value = null
  try {
    const res = await api.post('/ai/ceo-report')
    aiReport.value = res.data.report
  } catch (e) {
    aiReport.value = tt('ai.errorMd')
  } finally {
    aiLoading.value = false
  }
}

function renderMarkdown(text) {
  return text
    .replace(/^## (.*$)/gm, '<h2 class="ai-h2">$1</h2>')
    .replace(/^### (.*$)/gm, '<h3 class="ai-h3">$1</h3>')
    .replace(/^\*\*(.*)\*\*$/gm, '<strong>$1</strong>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.*$)/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^(?!<[hul])/gm, '')
    .split('\n').map(line =>
      line.startsWith('<') ? line : (line ? '<p>' + line + '</p>' : '')
    ).join('')
}
</script>

<style scoped>
.shell { display: flex; height: 100vh; overflow: hidden; }
.mobile-header { display: none; }
.main { flex: 1; min-width: 0; overflow-y: auto; }
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
.notif-badge { background: var(--red); color: #fff; font-size: 9px; font-weight: 800; padding: 2px 6px; border-radius: 10px; font-family: "Nunito Sans", sans-serif; }
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
.main { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
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
.spec-row-full { display: flex; flex-direction: column; gap: 6px; background: var(--surface2); border: 1px solid var(--border); border-radius: 7px; padding: 10px; }
.spec-row-full { display: flex; flex-direction: column; gap: 6px; background: var(--surface2); border: 1px solid var(--border); border-radius: 7px; padding: 10px; }
.spec-row { display: flex; align-items: center; gap: 8px; }
.spec-dates { display: flex; gap: 10px; padding-left: 24px; }
.spec-date-field { display: flex; flex-direction: column; gap: 3px; flex: 1; }
.spec-date-lbl { font-family: 'Nunito Sans', sans-serif; font-size: 9px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: var(--text-dim); }
.spec-date-input { font-size: 12px; padding: 5px 8px; }
.spec-check-input { width: 16px; height: 16px; flex-shrink: 0; accent-color: var(--accent); }
.spec-input { flex: 1; }
.del-btn { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 14px; padding: 4px 6px; border-radius: 4px; flex-shrink: 0; }
.del-btn:hover { color: var(--red); background: var(--red-dim); }
.module-builder { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 12px; }
.mb-head { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.mb-name { flex: 1; }
.mb-color { width: 130px; flex-shrink: 0; }
.task-builder { display: flex; flex-direction: column; gap: 6px; padding-left: 8px; border-left: 2px solid var(--border-bright); }
.task-row { display: flex; gap: 6px; align-items: flex-start; flex-direction: column; }
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
.modal-ai { width: 780px; max-height: 85vh; }
.ai-body { min-height: 300px; }
.ai-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px; gap: 20px; }
.ai-spinner { width: 40px; height: 40px; border: 3px solid rgba(139,92,246,0.2); border-top-color: #a78bfa; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.ai-loading-txt { color: var(--text-dim); font-size: 14px; font-family: "Nunito Sans", sans-serif; }
.ai-report { font-size: 14px; line-height: 1.8; color: var(--text-mid); }
.ai-report :deep(.ai-h2) { font-size: 16px; font-weight: 800; color: var(--text); margin: 20px 0 8px; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
.ai-report :deep(.ai-h3) { font-size: 14px; font-weight: 700; color: var(--text); margin: 14px 0 6px; }
.ai-report :deep(ul) { padding-left: 20px; margin: 8px 0; }
.ai-report :deep(li) { margin-bottom: 6px; }
.ai-report :deep(strong) { color: var(--text); font-weight: 700; }
.ai-report :deep(p) { margin-bottom: 10px; }
.ai-error { padding: 40px; text-align: center; color: var(--red); font-size: 14px; }

/* ════ MOBILE RESPONSIVE ════ */
.mobile-header { display: none; }

@media (max-width: 768px) {
  .shell { flex-direction: column; }
  .mobile-header { display: flex !important; }

  .mobile-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    height: 56px;
    background: var(--sidebar-bg);
    border-bottom: 1px solid var(--sidebar-border);
    position: sticky;
    top: 0;
    z-index: 100;
    flex-shrink: 0;
  }

  .hamburger-btn {
    width: 36px; height: 36px;
    background: none; border: none; cursor: pointer;
    display: flex; flex-direction: column;
    align-items: center; justify-content: center; gap: 5px;
    padding: 6px; border-radius: 8px;
    transition: background 0.2s;
  }
  .hamburger-btn:hover { background: rgba(255,255,255,0.08); }
  .hamburger-btn span {
    display: block; width: 20px; height: 2px;
    background: var(--sidebar-text); border-radius: 2px;
    transition: all 0.3s;
  }

  .mobile-logo {
    font-family: 'Nunito', sans-serif;
    font-size: 18px; font-weight: 900;
    color: #fff; letter-spacing: -0.5px;
  }
  .mobile-logo span { color: var(--sidebar-active-border); }

  .mobile-notif-btn {
    position: relative; font-size: 20px;
    text-decoration: none; padding: 6px;
    border-radius: 8px; transition: background 0.2s;
  }
  .mobile-notif-btn:hover { background: rgba(255,255,255,0.08); }
  .mobile-notif-badge {
    position: absolute; top: 0; right: 0;
    background: var(--red); color: #fff;
    font-size: 9px; font-weight: 700;
    width: 16px; height: 16px; border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
  }

  .sidebar-overlay {
    position: fixed; inset: 0;
    background: rgba(0,0,0,0.5);
    z-index: 199;
    backdrop-filter: blur(2px);
  }

  .sidebar {
    position: fixed !important;
    top: 0; left: 0; bottom: 0;
    width: 260px !important;
    z-index: 200;
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    height: 100vh !important;
    box-shadow: 4px 0 24px rgba(0,0,0,0.4);
  }
  .sidebar.sidebar-open {
    transform: translateX(0);
  }

  .main {
    width: 100% !important;
    min-width: 0 !important;
  }
}

.mfa-nudge-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);
  z-index: 9999; display: flex; align-items: center; justify-content: center;
  animation: mfaFadeIn 0.3s ease;
}
@keyframes mfaFadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes mfaBounce { 0% { transform: scale(0.8) translateY(20px); opacity: 0; } 100% { transform: scale(1) translateY(0); opacity: 1; } }
.mfa-nudge-modal {
  background: var(--surface); border-radius: 20px; padding: 36px 32px;
  max-width: 520px; width: 90%; text-align: center; position: relative;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3); animation: mfaBounce 0.4s ease;
  border-top: 4px solid #7c3aed;
}
.mfa-nudge-close {
  position: absolute; top: 14px; right: 18px; background: none; border: none;
  font-size: 18px; color: var(--text-dim); cursor: pointer;
}
.mfa-nudge-close:hover { color: var(--text); }
.mfa-nudge-icon { font-size: 48px; margin-bottom: 12px; }
.mfa-nudge-greeting {
  font-size: 22px; font-weight: 800; color: var(--text); margin-bottom: 18px;
  font-family: 'Nunito', sans-serif;
}
.mfa-nudge-body {
  text-align: left; font-size: 14px; line-height: 1.8; color: var(--text);
  margin-bottom: 24px; padding: 0 8px;
}
.mfa-nudge-body p { margin-bottom: 10px; }
.mfa-nudge-highlight {
  font-size: 18px !important; font-weight: 800; color: #dc2626;
  text-align: center; margin: 16px 0 !important;
  letter-spacing: 1px;
}
.mfa-nudge-love {
  font-size: 16px !important; font-weight: 700; color: #7c3aed;
  text-align: center; margin-top: 16px !important;
}
.mfa-nudge-btn {
  display: block; width: 100%; padding: 14px; background: #7c3aed;
  color: white; border: none; border-radius: 10px; font-size: 15px;
  font-weight: 700; cursor: pointer; margin-bottom: 10px;
  font-family: 'Nunito', sans-serif; transition: background 0.2s;
}
.mfa-nudge-btn:hover { background: #6d28d9; }
.mfa-nudge-dismiss {
  display: block; width: 100%; padding: 10px; background: none;
  border: 1px solid var(--border); border-radius: 8px; font-size: 12px;
  color: var(--text-dim); cursor: pointer; font-family: 'Nunito', sans-serif;
}
.mfa-nudge-dismiss:hover { border-color: var(--text-mid); color: var(--text-mid); }

.lang-switcher { display: flex; gap: 4px; padding: 8px 14px; border-top: 1px solid var(--sidebar-border); }
.lang-btn { flex: 1; padding: 5px 0; border: 1px solid var(--sidebar-border); border-radius: 6px; background: transparent; color: var(--sidebar-text-dim); font-size: 11px; font-weight: 700; cursor: pointer; transition: all 0.15s; font-family: 'Nunito', sans-serif; }
.lang-btn:hover { background: rgba(255,255,255,0.05); color: var(--sidebar-text); }
.lang-btn.active { background: var(--sidebar-active-bg); color: #fff; border-color: var(--sidebar-active-border); }

</style>