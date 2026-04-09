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
      <div :class="`contract-header ${project.category}`" style="position:relative;">
        <button class="edit-project-btn" @click="openEditModal" title="Επεξεργασία">✎ Επεξεργασία</button>
        <div class="ch-top">
          <div>
            <div class="ch-title">{{ project.title }}</div>
            <div class="ch-meta">
              <span :style="`color:var(--${project.category});font-weight:700;`">{{ project.companyCode }}</span>
              <span>{{ project.companyName }}</span>
              <span v-if="project.deadline">Deadline: {{ formatDate(project.deadline) }}</span>
              <span v-if="project.budget">Budget: €{{ Number(project.budget).toLocaleString() }}</span>
            </div>
          </div>
          <span :class="`ch-badge ${project.category}`">{{ catLabel(project.category) }}</span>
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
            <div class="cs-val" :class="`sv-${project.status}`" style="font-size:13px;">{{ statusLabel(project.status) }}</div>
          </div>
          <div v-if="project.budget" class="cs-item">
            <div class="cs-lbl">Budget</div>
            <div class="cs-val">€{{ Number(project.budget).toLocaleString() }}</div>
          </div>
        </div>
        <div class="ch-progress-wrap">
          <div class="ch-progress-bar">
            <div class="ch-progress-fill" :style="`width:${project.completion}%;background:var(--${project.category});`"></div>
          </div>
          <span class="ch-progress-pct" :style="`color:var(--${project.category});`">{{ project.completion }}%</span>
        </div>
      </div>

      <!-- GANTT TIMELINE -->
      <div v-if="project.modules && project.modules.length" class="gantt-panel">
        <div class="gantt-ph">
          <div class="gantt-ph-title">📊 Project Timeline</div>
          <div class="gantt-ph-sub">{{ project.title }} · {{ ganttWeeks.length }} weeks</div>
        </div>
        <div class="gantt-scroll">
          <!-- HEADER -->
          <div class="gantt-header">
            <div class="gantt-lbl-col">MODULE / TASK</div>
            <div class="gantt-weeks-col">
              <!-- Week row -->
              <div class="gantt-week-row">
                <div v-for="w in ganttWeeks" :key="w.num"
                  :class="['gantt-wk-hd', { 'gantt-wk-today': w.isCurrentWeek }]"
                  :style="'flex: 7'">
                  <div class="gantt-wk-num">W{{ w.num }}</div>
                  <div class="gantt-wk-date">{{ w.dateLabel }}</div>
                </div>
              </div>
              <!-- Day row -->
              <div class="gantt-day-row">
                <div v-for="d in ganttDays" :key="d.index"
                  :class="['gantt-day-hd', { 'gantt-day-today': d.isToday, 'gantt-day-weekend': d.isWeekend }]">
                  <span class="gantt-day-num">{{ d.dayNum }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- PROJECT ROW -->
          <div class="gantt-proj-row">
            <div class="gantt-proj-lbl">
              <span :class="`cat-icon ${project.category}`">{{ catIcon(project.category) }}</span>
              <strong>{{ project.title }}</strong>
              <span class="gantt-proj-co">{{ project.companyName }}</span>
            </div>
            <div class="gantt-proj-track">
              <div class="gantt-today-line" :style="`left:${todayPct}%`"></div>
            </div>
          </div>

          <!-- MODULES + TASKS -->
          <template v-for="m in project.modules" :key="m.id">
            <!-- MODULE ROW -->
            <div class="gantt-mod-row" @click="toggleMod(m.id)">
              <div class="gantt-mod-lbl">
                <span :class="`mod-dot ${m.color || project.category}`"></span>
                <span class="mod-name">{{ m.name }}</span>
                <span class="mod-pct" :style="`color:var(--${m.color || project.category});`">{{ m.completion }}%</span>
              </div>
              <div class="gantt-track">
                <div class="gantt-today-line" :style="`left:${todayPct}%`"></div>
                <!-- Module span bar -->
                <div v-if="moduleBarStyle(m).show" class="gantt-mod-bar"
                  :style="`left:${moduleBarStyle(m).left}%;width:${moduleBarStyle(m).width}%;background:var(--${m.color || project.category});opacity:0.15;`">
                </div>
              </div>
            </div>

            <!-- TASK ROWS -->
            <template v-if="!collapsedMods.has(m.id)">
              <div v-for="t in m.tasks" :key="t.id" class="gantt-task-row">
                <div class="gantt-task-lbl">
                  <div :class="`task-chk ${t.isDone ? 'done' : t.isBlocked ? 'block' : ''}`">{{ t.isDone ? '✓' : '' }}</div>
                  <span :class="`task-name-g ${t.isDone ? 'done' : ''}`">{{ t.name }}</span>
                  <span v-if="t.isBlocked" class="task-blocked-ico">⚠</span>
                </div>
                <div class="gantt-track">
                  <div class="gantt-today-line" :style="`left:${todayPct}%`"></div>
                  <div v-if="taskBarStyle(t).show"
                    class="gantt-task-bar"
                    :style="`
                      left:${taskBarStyle(t).left}%;
                      width:${taskBarStyle(t).width}%;
                      background:${t.isDone ? '#a0a0b8' : t.isBlocked ? 'var(--red)' : 'var(--' + (m.color || project.category) + ')'};
                      opacity:${t.isDone ? 0.9 : 0.75};
                    `">
                    <div class="gantt-task-fill"
                      :style="`width:${t.progress}%;background:rgba(255,255,255,0.35);`"></div>
                    <span class="gantt-task-label">{{ t.name }}</span>
                  </div>
                </div>
              </div>

</template>
          </template>
        </div>
      </div>

      <!-- MODULES ACCORDION (detail) -->
      <div v-if="project.modules && project.modules.length" style="margin-top:14px;">
        <div class="modules-title">Modules & Tasks Detail</div>
        <div v-for="m in project.modules" :key="m.id+'acc'" class="module-group">
          <div class="mg-header" @click="toggleAcc(m.id)">
            <span class="mg-expand">{{ openAcc.has(m.id) ? '▼' : '▶' }}</span>
            <span class="mg-name">{{ m.name }}</span>
            <div class="mg-right">
              <div class="mg-bar-wrap"><div class="mg-bar"><div class="mg-bar-fill" :style="`width:${m.completion}%;background:var(--${m.color||project.category});`"></div></div></div>
              <span class="mg-pct" :style="`color:var(--${m.color||project.category});`">{{ m.completion }}%</span>
              <span class="mg-tasks">{{ m.tasks.filter(t=>t.isDone).length }}/{{ m.tasks.length }}</span>
            </div>
          </div>
          <div v-if="openAcc.has(m.id)" class="task-list">
            <div v-for="t in m.tasks" :key="t.id+'acc'" class="task-item">
              <div :class="`task-check ${t.isDone?'done':t.isBlocked?'block':''}`">{{ t.isDone?'✓':'' }}</div>
              <div style="flex:1;">
                <div :class="`task-name ${t.isDone?'done':''}`">{{ t.name }}</div>
                <div v-if="t.blockNote" class="task-note">⚠ {{ t.blockNote }}</div>
              </div>
              <span class="task-assignee">{{ t.assignee||'—' }}</span>
              <div class="task-pct-wrap">
                <div class="task-bar"><div class="task-bar-fill" :style="`width:${t.progress}%;background:${t.isDone?'var(--green)':'var(--'+( m.color||project.category)+')'};`"></div></div>
                <div class="task-pct" :style="`color:${t.isDone ? 'var(--green)' : 'var(--' + (m.color || project.category) + ')'}`">{{ t.progress }}%</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- SPECS -->
      <div v-if="project.specs && project.specs.length" class="specs-panel" style="margin-top:14px;">
        <div class="specs-title">📋 Specifications</div>
        <div class="specs-list">
          <div v-for="s in project.specs" :key="s.id" class="spec-item">
            <div :class="specCheckClass(s)">{{ s.isDone ? '✓' : '' }}</div>
            <div style="flex:1;">
              <div :class="specTxtClass(s)">{{ s.description }}</div>
              <div v-if="s.startDate || s.endDate" class="spec-dates-display">
                <span v-if="s.startDate">📅 {{ formatDate(s.startDate) }}</span>
                <span v-if="s.startDate && s.endDate"> → </span>
                <span v-if="s.endDate">{{ formatDate(s.endDate) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="project.contractDesc" class="contract-desc-panel" style="margin-top:14px;">
        <div class="cd-title">📄 Περιγραφή Σύμβασης</div>
        <div class="cd-text">{{ project.contractDesc }}</div>
      </div>

      <!-- CONTRACT FILES -->
      <div class="files-panel" style="margin-top:14px;">
        <div class="files-header">
          <div class="files-title">📎 Συμβόλαια & Αρχεία</div>
          <label class="files-upload-btn" :class="{uploading: uploading}">
            <input type="file" @change="uploadFile" accept=".pdf,.doc,.docx,.xlsx,.png,.jpg" style="display:none" :disabled="uploading" />
            {{ uploading ? "Ανέβασμα..." : "+ Ανέβασμα" }}
          </label>
        </div>
        <div v-if="files.length" class="files-list">
          <div v-for="f in files" :key="f.id" class="file-item">
            <span class="file-icon">{{ fileIcon(f.contentType) }}</span>
            <div class="file-info">
              <div class="file-name">{{ f.fileName }}</div>
              <div class="file-meta">{{ formatSize(f.fileSizeBytes) }} · {{ f.uploadedBy }} · {{ formatInstant(f.uploadedAt) }}</div>
            </div>
            <button class="file-del" @click="deleteFile(f.id)" title="Διαγραφή">✕</button>
          </div>
        </div>
        <div v-else-if="!uploading" class="files-empty">Δεν υπάρχουν αρχεία. Ανέβασε συμβόλαιο ή έγγραφο.</div>
        <div v-if="uploadError" class="files-error">{{ uploadError }}</div>
      </div>

      <!-- FINANCIAL OVERVIEW -->
      <div v-if="project.budget && permStore.can('viewFinancials')" class="fin-panel" style="margin-top:14px;">
        <div class="fin-title">💰 Financial Overview</div>
        <div class="fin-grid">
          <div class="fin-item">
            <div class="fin-lbl">Budget</div>
            <div class="fin-val">€{{ Number(project.budget).toLocaleString() }}</div>
          </div>
          <div class="fin-item">
            <div class="fin-lbl">Invoiced</div>
            <div class="fin-val invoiced">€{{ Number(project.invoiced || 0).toLocaleString() }}</div>
            <div class="fin-pct">{{ budgetPct(project.invoiced, project.budget) }}%</div>
          </div>
          <div class="fin-item">
            <div class="fin-lbl">Paid</div>
            <div class="fin-val paid">€{{ Number(project.paid || 0).toLocaleString() }}</div>
            <div class="fin-pct">{{ budgetPct(project.paid, project.budget) }}%</div>
          </div>
          <div class="fin-item">
            <div class="fin-lbl">Remaining</div>
            <div class="fin-val remaining">€{{ Number((project.budget || 0) - (project.paid || 0)).toLocaleString() }}</div>
          </div>
        </div>
        <div class="fin-bar-wrap">
          <div class="fin-bar-track">
            <div class="fin-bar-paid" :style="`width:${budgetPct(project.paid, project.budget)}%`"></div>
            <div class="fin-bar-invoiced" :style="`width:${Math.max(0, budgetPct(project.invoiced, project.budget) - budgetPct(project.paid, project.budget))}%`"></div>
          </div>
          <span class="fin-bar-lbl">{{ budgetPct(project.paid, project.budget) }}% paid</span>
        </div>
      </div>

      <!-- CEO NOTES -->
      <div v-if="permStore.isCEO() || permStore.can('viewCeoNotes')" class="notes-panel" style="margin-top:14px;">
        <div class="notes-header">
          <div class="notes-title">🔒 CEO Notes <span class="notes-private">Private</span></div>
          <button class="notes-add-btn" @click="showNoteInput=!showNoteInput">+ Νέα Σημείωση</button>
        </div>
        <div v-if="showNoteInput" class="note-input-wrap">
          <textarea v-model="newNote" placeholder="Γράψε σημείωση..." class="note-textarea" rows="3"></textarea>
          <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:8px;">
            <button class="note-cancel" @click="cancelNote">Ακύρωση</button>
            <button class="note-save" @click="saveNote" :disabled="!newNote.trim()">Αποθήκευση</button>
          </div>
        </div>
        <div v-if="notes.length" class="notes-list">
          <div v-for="n in notes" :key="n.id" class="note-item">
            <div class="note-content">{{ n.content }}</div>
            <div class="note-meta">
              <span>{{ n.createdBy }}</span>
              <span>{{ formatInstant(n.createdAt) }}</span>
              <button class="note-del" @click="deleteNote(n.id)">✕</button>
            </div>
          </div>
        </div>
        <div v-else-if="!showNoteInput" class="notes-empty">Δεν υπάρχουν σημειώσεις.</div>
      </div>

    </div>
    <div v-else class="loading">Project not found.</div>
  </div>
  <!-- EDIT PROJECT MODAL -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal=false">
    <div class="modal modal-edit">
      <div class="modal-header">
        <div class="modal-title">✎ Επεξεργασία Project</div>
        <button class="modal-close" @click="showEditModal=false">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-section-title">ΒΑΣΙΚΑ ΣΤΟΙΧΕΙΑ</div>
        <div class="form-group">
          <label>Τίτλος *</label>
          <input v-model="editForm.title" type="text" class="form-input" placeholder="Τίτλος project" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Εταιρεία *</label>
            <select v-model="editForm.companyId" class="form-input">
              <option v-for="co in companies" :key="co.id" :value="co.id">{{ co.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Κατηγορία *</label>
            <select v-model="editForm.category" class="form-input">
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
            <input v-model="editForm.budget" type="number" class="form-input" />
          </div>
          <div class="form-group">
            <label>Ημ. Εναρξης</label>
            <input v-model="editForm.startDate" type="date" class="form-input" />
          </div>
          <div class="form-group">
            <label>Deadline</label>
            <input v-model="editForm.deadline" type="date" class="form-input" />
          </div>
        </div>
        <div class="form-group">
          <label>Περιγραφή Σύμβασης</label>
          <textarea v-model="editForm.contractDesc" class="form-input" rows="3" placeholder="Σύντομη περιγραφή..."></textarea>
        </div>
        <div class="form-group">
          <label>Status</label>
          <select v-model="editForm.status" class="form-input">
            <option value="on_track">On Track</option>
            <option value="at_risk">At Risk</option>
            <option value="delayed">Delayed</option>
            <option value="completed">Completed</option>
          </select>
        </div>
        <div v-if="editError" class="form-error">{{ editError }}</div>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" @click="showEditModal=false">Ακύρωση</button>
        <button class="btn-submit" @click="saveEdit" :disabled="editSaving">
          {{ editSaving ? "Αποθήκευση..." : "Αποθήκευση" }}
        </button>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'
import { usePermissionStore } from '@/stores/permissions'

const route = useRoute()
const router = useRouter()
const store = useProjectStore()
const permStore = usePermissionStore()

const project = ref(null)
const loading = ref(true)
const collapsedMods = ref(new Set())
const openAcc = ref(new Set())

const GANTT_WEEKS = 3

const ganttStart = computed(() => {
  if (project.value?.startDate) return new Date(project.value.startDate)
  const d = new Date()
  d.setDate(d.getDate() - d.getDay())
  return d
})

const ganttEnd = computed(() => {
  const d = new Date(ganttStart.value)
  d.setDate(d.getDate() + GANTT_WEEKS * 7)
  return d
})

onMounted(async () => {
  project.value = await store.fetchProject(route.params.id)
  loading.value = false
  if (project.value?.modules?.length) {
    openAcc.value.add(project.value.modules[0].id)
  }
  await loadNotes()
  await loadFiles()
})

// ════ CEO NOTES ════
const notes = ref([])
const newNote = ref("")
const showNoteInput = ref(false)

async function loadNotes() {
  if (!project.value) return
  try {
    const res = await api.get(`/projects/${project.value.id}/notes`)
    notes.value = res.data
  } catch { notes.value = [] }
}

// ════ CONTRACT FILES ════
const files = ref([])
const uploading = ref(false)
const uploadError = ref("")

async function loadFiles() {
  if (!project.value) return
  try {
    const res = await api.get(`/projects/${project.value.id}/files`)
    files.value = res.data
  } catch { files.value = [] }
}

async function uploadFile(event) {
  const file = event.target.files[0]
  if (!file) return
  uploading.value = true
  uploadError.value = ""
  try {
    const fd = new FormData()
    fd.append("file", file)
    await api.post(`/projects/${project.value.id}/files`, fd, {
      headers: { "Content-Type": "multipart/form-data" }
    })
    await loadFiles()
  } catch (e) {
    uploadError.value = e.response?.data?.message || "Σφάλμα ανεβάσματος."
  } finally {
    uploading.value = false
    event.target.value = ""
  }
}

async function deleteFile(fileId) {
  if (!confirm("Διαγραφή αρχείου;")) return
  try {
    await api.delete(`/projects/${project.value.id}/files/${fileId}`)
    await loadFiles()
  } catch {}
}

function fileIcon(ct) {
  if (!ct) return "📄"
  if (ct.includes("pdf")) return "📕"
  if (ct.includes("word") || ct.includes("doc")) return "📘"
  if (ct.includes("sheet") || ct.includes("excel")) return "📗"
  if (ct.includes("image")) return "🖼"
  return "📄"
}

function formatSize(bytes) {
  if (!bytes) return "0 B"
  if (bytes < 1024) return bytes + " B"
  if (bytes < 1048576) return (bytes/1024).toFixed(1) + " KB"
  return (bytes/1048576).toFixed(1) + " MB"
}

function specCheckClass(s) { return s.isDone ? 'spec-check done' : 'spec-check' }
function specTxtClass(s) { return s.isDone ? 'spec-txt done' : 'spec-txt' }

// ════ EDIT PROJECT ════
const showEditModal = ref(false)
const editForm = ref({})
const editError = ref("")
const editSaving = ref(false)
const companies = ref([])

async function openEditModal() {
  editError.value = ""
  editForm.value = {
    title: project.value.title || "",
    companyId: project.value.companyId || "",
    category: project.value.category || "dev",
    budget: project.value.budget || 0,
    startDate: project.value.startDate || "",
    deadline: project.value.deadline || "",
    contractDesc: project.value.contractDesc || "",
    status: project.value.status || "on_track",
    modules: project.value.modules || [],
    specs: project.value.specs || [],
  }
  if (!companies.value.length) {
    try {
      const res = await api.get("/companies")
      companies.value = res.data
    } catch {}
  }
  showEditModal.value = true
}

async function saveEdit() {
  if (!editForm.value.title.trim()) { editError.value = "Ο τίτλος είναι υποχρεωτικός."; return }
  editSaving.value = true
  editError.value = ""
  try {
    await api.put(`/projects/${project.value.id}`, {
      title: editForm.value.title,
      companyId: editForm.value.companyId,
      category: editForm.value.category,
      budget: editForm.value.budget || 0,
      startDate: editForm.value.startDate || null,
      deadline: editForm.value.deadline || null,
      contractDesc: editForm.value.contractDesc || "",
      status: editForm.value.status,
      modules: editForm.value.modules.map(m => ({
        name: m.name, color: m.color, sortOrder: m.sortOrder || 0,
        tasks: (m.tasks || []).map(t => ({
          name: t.name, assignee: t.assignee, progress: t.progress,
          isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote,
          comment: t.comment, deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks, sortOrder: t.sortOrder || 0
        }))
      })),
      specs: editForm.value.specs.map(s => ({
        description: s.description, isDone: s.isDone, sortOrder: s.sortOrder || 0,
        startDate: s.startDate || null, endDate: s.endDate || null
      }))
    })
    showEditModal.value = false
    await loadProject()
  } catch (e) {
    editError.value = e.response?.data?.message || "Σφάλμα αποθήκευσης."
  } finally { editSaving.value = false }
}

function cancelNote() {
  showNoteInput.value = false
  newNote.value = ''
}

async function saveNote() {
  if (!newNote.value.trim()) return
  try {
    await api.post(`/projects/${project.value.id}/notes`, { content: newNote.value })
    newNote.value = ""
    showNoteInput.value = false
    await loadNotes()
  } catch (e) { console.error(e) }
}

async function deleteNote(noteId) {
  try {
    await api.delete(`/projects/${project.value.id}/notes/${noteId}`)
    await loadNotes()
  } catch (e) { console.error(e) }
}

// ════ FINANCIAL ════
function budgetPct(amount, budget) {
  if (!budget || !amount) return 0
  return Math.round((Number(amount) / Number(budget)) * 100)
}

function formatInstant(instant) {
  if (!instant) return ""
  const d = new Date(instant)
  const m = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"]
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}, ${d.getHours()}:${String(d.getMinutes()).padStart(2,"0")}`
}

function toggleMod(id) {
  const s = new Set(collapsedMods.value)
  if (s.has(id)) s.delete(id); else s.add(id)
  collapsedMods.value = s
}
function toggleAcc(id) {
  const s = new Set(openAcc.value)
  if (s.has(id)) s.delete(id); else s.add(id)
  openAcc.value = s
}

// ════ GANTT LOGIC ════
const ganttWeeks = computed(() => {
  const weeks = []
  const months = ["Ιαν","Φεβ","Μαρ","Απρ","Μαι","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ"]
  for (let i = 0; i < GANTT_WEEKS; i++) {
    const d = new Date(ganttStart.value)
    d.setDate(d.getDate() + i * 7)
    const weekEnd = new Date(d); weekEnd.setDate(weekEnd.getDate() + 7)
    const now = new Date(); const isCurrentWeek = now >= d && now < weekEnd
    weeks.push({ num: i + 1, dateLabel: d.getDate() + ' ' + months[d.getMonth()], isCurrentWeek })
  }
  return weeks
})

const todayPct = computed(() => {
  const now = new Date()
  const gs = ganttStart.value.getTime()
  const total = GANTT_WEEKS * 7 * 86400000
  return Math.min(100, Math.max(0, (now.getTime() - gs) / total * 100))
})

const ganttDays = computed(() => {
  const days = []
  const totalDays = GANTT_WEEKS * 7
  for (let i = 0; i < totalDays; i++) {
    const d = new Date(ganttStart.value)
    d.setDate(d.getDate() + i)
    const now = new Date()
    const isToday = d.toDateString() === now.toDateString()
    const isWeekend = d.getDay() === 0 || d.getDay() === 6
    days.push({ index: i, dayNum: d.getDate(), isToday, isWeekend, date: d })
  }
  return days
})

const totalGanttDays = computed(() => GANTT_WEEKS * 7)

function dayPct(dayIndex) {
  return (dayIndex / totalGanttDays.value) * 100
}
function dayWidthPct(days) {
  return (days / totalGanttDays.value) * 100
}

function weekPct(weekNum) {
  return ((weekNum - 1) / GANTT_WEEKS) * 100
}
function weekWidthPct(weeks) {
  return (weeks / GANTT_WEEKS) * 100
}

function taskBarStyle(t) {
  if (t.startDay != null && t.durationDays != null) {
    const left = dayPct(t.startDay)
    const width = Math.max(dayWidthPct(1), dayWidthPct(t.durationDays))
    if (left >= 100) return { show: false }
    return { show: true, left, width: Math.min(width, 100 - left) }
  }
  if (!t.startWeek) return { show: false }
  const left = weekPct(t.startWeek)
  const width = Math.max(weekWidthPct(t.durationWeeks || 1), weekWidthPct(1))
  return { show: true, left, width: Math.min(width, 100 - left) }
}

function moduleBarStyle(m) {
  const tasks = m.tasks.filter(t => t.startDay != null || t.startWeek)
  if (!tasks.length) return { show: false }
  let minDay, maxDay
  if (tasks[0].startDay != null) {
    minDay = Math.min(...tasks.map(t => t.startDay))
    maxDay = Math.max(...tasks.map(t => (t.startDay || 0) + (t.durationDays || 1)))
    const left = dayPct(minDay)
    const width = dayPct(maxDay) - left
    return { show: true, left, width: Math.min(width, 100 - left) }
  }
  const minW = Math.min(...tasks.map(t => t.startWeek))
  const maxW = Math.max(...tasks.map(t => (t.startWeek || 1) + (t.durationWeeks || 1)))
  const left = weekPct(minW)
  const width = weekPct(maxW) - left
  return { show: true, left, width: Math.min(width, 100 - left) }
}

const catLabel = (c) => ({ finance:'Finance', legal:'Legal', dev:'Developing', marketing:'Marketing' }[c] || c)
const catIcon  = (c) => ({ finance:'$', legal:'⚖', dev:'⌨', marketing:'◈' }[c] || '·')
const statusLabel = (s) => ({ on_track:'On Track', delayed:'Delayed', at_risk:'At Risk', stale:'Stale', completed:'Completed' }[s] || s)

function formatDate(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  const m = ['Ιαν','Φεβ','Μαρ','Απρ','Μαι','Ιουν','Ιουλ','Αυγ','Σεπ','Οκτ','Νοε','Δεκ']
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`
}
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.loading { color: var(--text-dim); font-size: 14px; padding: 40px; text-align: center; }
.breadcrumb { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--text-dim); margin-bottom: 18px; font-family: "Nunito Sans", sans-serif; }
.breadcrumb span { cursor: pointer; font-weight: 600; transition: color 0.15s; }
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
.ch-meta { font-size: 11px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; display: flex; gap: 14px; flex-wrap: wrap; }
.ch-badge { font-size: 9px; font-weight: 700; padding: 4px 12px; border-radius: 6px; letter-spacing: 1px; }
.ch-badge.finance  { background: var(--finance-dim);  color: var(--finance); }
.ch-badge.legal    { background: var(--legal-dim);    color: var(--legal); }
.ch-badge.dev      { background: var(--dev-dim);      color: var(--dev); }
.ch-badge.marketing{ background: var(--marketing-dim);color: var(--marketing); }
.contract-stats { display: grid; grid-template-columns: repeat(4,1fr); gap: 10px; margin-bottom: 16px; }
.cs-item { background: var(--surface2); border-radius: 8px; padding: 12px 14px; border: 1px solid var(--border); text-align: center; }
.cs-lbl { font-family: "Nunito Sans", sans-serif; font-size: 8px; letter-spacing: 1.5px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 5px; font-weight: 700; }
.cs-val { font-size: 20px; font-weight: 900; color: var(--text); }
.sv-on_track { color: var(--green) !important; }
.sv-delayed  { color: var(--yellow) !important; }
.sv-at_risk  { color: var(--red) !important; }
.ch-progress-wrap { display: flex; align-items: center; gap: 12px; }
.ch-progress-bar { flex: 1; height: 6px; background: var(--surface3); border-radius: 3px; overflow: hidden; }
.ch-progress-fill { height: 100%; border-radius: 3px; transition: width 0.5s; }
.ch-progress-pct { font-family: "Nunito Sans", sans-serif; font-size: 13px; font-weight: 800; min-width: 36px; text-align: right; }

/* ════ GANTT ════ */
.gantt-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.gantt-ph { padding: 14px 20px; border-bottom: 1px solid var(--border); background: var(--surface2); display: flex; align-items: center; justify-content: space-between; }
.gantt-ph-title { font-size: 14px; font-weight: 800; }
.gantt-ph-sub { font-size: 11px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; }
.gantt-scroll { overflow-x: auto; min-width: 0; }
.gantt-header { display: flex; border-bottom: 2px solid var(--border); background: var(--surface2); position: sticky; top: 0; z-index: 3; }
.gantt-lbl-col { width: 260px; flex-shrink: 0; padding: 10px 16px; font-family: "Nunito Sans", sans-serif; font-size: 9px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 1px; font-weight: 700; display: flex; align-items: flex-end; }
.gantt-weeks-col { flex: 1; display: flex; flex-direction: column; min-width: 600px; }
.gantt-week-row { display: flex; border-bottom: 1px solid var(--border); }
.gantt-day-row { display: flex; border-bottom: 1px solid var(--border); }
.gantt-wk-hd { flex: 7; padding: 5px 4px; text-align: center; border-left: 1px solid var(--border); }
.gantt-day-hd { flex: 1; padding: 3px 1px; text-align: center; border-left: 1px solid var(--border); min-width: 0; }
.gantt-day-num { font-family: "Nunito Sans", sans-serif; font-size: 8px; color: var(--text-dim); }
.gantt-day-today { background: var(--accent-dim); }
.gantt-day-today .gantt-day-num { color: var(--accent); font-weight: 800; }
.gantt-day-weekend { background: var(--surface3); }
.gantt-track { flex: 1; position: relative; height: 52px; display: flex; align-items: center; min-width: 600px; }
.gantt-wk-hd { padding: 8px 4px; text-align: center; border-left: 1px solid var(--border); }
.gantt-wk-num { font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 800; color: var(--text-dim); }
.gantt-wk-date { font-family: "Nunito Sans", sans-serif; font-size: 9px; color: var(--text-dim); margin-top: 2px; }
.gantt-wk-today .gantt-wk-num { color: var(--accent); }
.gantt-wk-today .gantt-wk-date { color: var(--accent); }
.gantt-wk-today { background: var(--accent-dim); }

.gantt-proj-row { display: flex; align-items: center; min-height: 44px; background: var(--surface2); border-bottom: 2px solid var(--border); }
.gantt-proj-lbl { width: 260px; flex-shrink: 0; padding: 10px 16px; display: flex; align-items: center; gap: 8px; }
.gantt-proj-lbl strong { font-size: 13px; font-weight: 800; }
.gantt-proj-co { font-size: 10px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-left: 4px; }
.cat-icon { font-size: 14px; }
.gantt-proj-track { flex: 1; position: relative; height: 44px; min-width: 600px; }

.gantt-mod-row { display: flex; align-items: center; min-height: 38px; border-bottom: 1px solid var(--border); cursor: pointer; transition: background 0.12s; }
.gantt-mod-row:hover { background: var(--surface2); }
.gantt-mod-lbl { width: 260px; flex-shrink: 0; padding: 8px 16px; display: flex; align-items: center; gap: 8px; }
.mod-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.mod-dot.finance  { background: var(--finance); }
.mod-dot.legal    { background: var(--legal); }
.mod-dot.dev      { background: var(--dev); }
.mod-dot.marketing{ background: var(--marketing); }
.mod-name { font-size: 12px; font-weight: 700; flex: 1; }
.mod-pct { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 800; }

.gantt-task-row { display: flex; align-items: center; min-height: 34px; border-bottom: 1px solid var(--border); }
.gantt-task-row:last-child { border-bottom: 1px solid var(--border); }
.gantt-task-lbl { width: 260px; flex-shrink: 0; padding: 6px 16px 6px 32px; display: flex; align-items: center; gap: 8px; }
.task-chk { width: 14px; height: 14px; border-radius: 3px; border: 1.5px solid var(--border-bright); display: flex; align-items: center; justify-content: center; font-size: 8px; flex-shrink: 0; }
.task-chk.done  { background: var(--green); border-color: var(--green); color: #fff; }
.task-chk.block { background: var(--red-dim); border-color: var(--red); }
.task-name-g { font-size: 11px; font-weight: 600; color: var(--text-mid); }
.task-name-g.done { color: var(--text-dim); text-decoration: line-through; }
.task-blocked-ico { color: var(--red); font-size: 10px; }

.gantt-track { flex: 1; position: relative; height: 34px; display: flex; align-items: center; min-width: 600px; }
.gantt-today-line { position: absolute; top: 0; bottom: 0; width: 2px; background: var(--accent); opacity: 0.5; z-index: 2; pointer-events: none; }
.gantt-mod-bar { position: absolute; height: 100%; border-radius: 3px; z-index: 0; }
.gantt-task-bar { position: absolute; height: 22px; border-radius: 4px; display: flex; align-items: center; overflow: hidden; min-width: 3px; z-index: 1; }
.gantt-task-fill { position: absolute; top: 0; left: 0; height: 100%; border-radius: 4px 0 0 4px; }
.gantt-task-label { font-size: 10px; font-weight: 700; color: #fff; padding: 0 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; position: relative; z-index: 1; }

/* ACCORDION */
.modules-title { font-size: 11px; font-weight: 700; color: var(--text-dim); text-transform: uppercase; letter-spacing: 1px; font-family: "Nunito Sans", sans-serif; margin-bottom: 8px; }
.module-group { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; margin-bottom: 6px; }
.mg-header { padding: 12px 16px; display: flex; align-items: center; gap: 10px; cursor: pointer; background: var(--surface2); transition: background 0.15s; }
.mg-header:hover { background: var(--surface3); }
.mg-expand { font-size: 9px; color: var(--text-dim); width: 10px; }
.mg-name { font-size: 13px; font-weight: 700; flex: 1; }
.mg-right { display: flex; align-items: center; gap: 10px; }
.mg-bar-wrap { width: 80px; }
.mg-bar { height: 4px; background: var(--surface3); border-radius: 2px; overflow: hidden; }
.mg-bar-fill { height: 100%; border-radius: 2px; }
.mg-pct { font-family: "Nunito Sans", sans-serif; font-size: 12px; font-weight: 800; min-width: 36px; text-align: right; }
.mg-tasks { font-family: "Nunito Sans", sans-serif; font-size: 11px; color: var(--text-dim); min-width: 36px; text-align: right; }
.task-list { padding: 0 16px; }
.task-item { display: flex; align-items: center; gap: 10px; padding: 9px 0; border-bottom: 1px solid var(--border); }
.task-item:last-child { border-bottom: none; }
.task-check { width: 16px; height: 16px; border-radius: 4px; border: 1.5px solid var(--border-bright); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 9px; }
.task-check.done  { background: var(--green); border-color: var(--green); color: #fff; }
.task-check.block { background: var(--red-dim); border-color: var(--red); }
.task-name { font-size: 12px; font-weight: 600; }
.task-name.done { color: var(--text-dim); text-decoration: line-through; }
.task-note { font-size: 10px; color: var(--red); margin-top: 2px; }
.task-assignee { font-size: 10px; color: var(--text-dim); background: var(--surface2); padding: 3px 8px; border-radius: 5px; white-space: nowrap; }
.task-pct-wrap { display: flex; align-items: center; gap: 6px; }
.task-bar { width: 60px; height: 3px; background: var(--surface3); border-radius: 2px; overflow: hidden; }
.task-bar-fill { height: 100%; border-radius: 2px; }
.task-pct { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; min-width: 32px; text-align: right; }

.specs-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; }
.specs-title { font-size: 13px; font-weight: 800; margin-bottom: 12px; }
.specs-list { display: flex; flex-direction: column; gap: 8px; }
.spec-item { display: flex; align-items: center; gap: 10px; }
.spec-check { width: 16px; height: 16px; border-radius: 4px; border: 1.5px solid var(--border-bright); display: flex; align-items: center; justify-content: center; font-size: 9px; flex-shrink: 0; }
.spec-check.done { background: var(--green); border-color: var(--green); color: #fff; }
.spec-txt { font-size: 13px; color: var(--text-mid); }
.spec-txt.done { color: var(--text-dim); text-decoration: line-through; }
.spec-dates-display { font-size: 10px; color: var(--text-dim); font-family: 'Nunito Sans', sans-serif; margin-top: 3px; display: flex; gap: 6px; align-items: center; }
.spec-dates-display { font-size: 10px; color: var(--text-dim); font-family: 'Nunito Sans', sans-serif; margin-top: 3px; display: flex; gap: 6px; align-items: center; }
.contract-desc-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; }
.cd-title { font-size: 13px; font-weight: 800; margin-bottom: 8px; }
.cd-text { font-size: 13px; color: var(--text-mid); line-height: 1.7; }

/* ════ FINANCIAL ════ */
.fin-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; }
.fin-title { font-size: 13px; font-weight: 800; margin-bottom: 14px; }
.fin-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 14px; }
.fin-item { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 12px 14px; text-align: center; }
.fin-lbl { font-family: "Nunito Sans", sans-serif; font-size: 8px; letter-spacing: 1.5px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 5px; font-weight: 700; }
.fin-val { font-size: 18px; font-weight: 900; color: var(--text); }
.fin-val.invoiced { color: var(--yellow); }
.fin-val.paid { color: var(--green); }
.fin-val.remaining { color: var(--text-mid); }
.fin-pct { font-family: "Nunito Sans", sans-serif; font-size: 10px; color: var(--text-dim); margin-top: 3px; }
.fin-bar-wrap { display: flex; align-items: center; gap: 12px; }
.fin-bar-track { flex: 1; height: 8px; background: var(--surface3); border-radius: 4px; overflow: hidden; display: flex; }
.fin-bar-paid { height: 100%; background: var(--green); border-radius: 4px 0 0 4px; transition: width 0.5s; }
.fin-bar-invoiced { height: 100%; background: var(--yellow); opacity: 0.6; transition: width 0.5s; }
.fin-bar-lbl { font-family: "Nunito Sans", sans-serif; font-size: 11px; font-weight: 700; color: var(--green); white-space: nowrap; }

/* ════ CEO NOTES ════ */
.notes-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
.notes-header { padding: 14px 20px; border-bottom: 1px solid var(--border); background: var(--surface2); display: flex; align-items: center; justify-content: space-between; }
.notes-title { font-size: 13px; font-weight: 800; display: flex; align-items: center; gap: 8px; }
.notes-private { font-size: 9px; font-weight: 700; background: rgba(124,58,237,0.1); color: #7c3aed; padding: 2px 8px; border-radius: 5px; letter-spacing: 0.5px; }
.notes-add-btn { font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; background: var(--accent); border: none; border-radius: 6px; color: #fff; cursor: pointer; }
.note-input-wrap { padding: 14px 20px; border-bottom: 1px solid var(--border); background: var(--surface2); }
.note-textarea { width: 100%; padding: 10px 12px; border: 1px solid var(--border-bright); border-radius: 7px; background: var(--surface); color: var(--text); font-family: "Nunito", sans-serif; font-size: 13px; resize: vertical; box-sizing: border-box; }
.note-textarea:focus { outline: none; border-color: var(--accent); }
.note-cancel { padding: 7px 14px; background: var(--surface3); border: 1px solid var(--border-bright); border-radius: 6px; font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 700; cursor: pointer; color: var(--text-mid); }
.note-save { padding: 7px 16px; background: var(--accent); border: none; border-radius: 6px; font-family: "Nunito", sans-serif; font-size: 12px; font-weight: 700; cursor: pointer; color: #fff; }
.note-save:disabled { opacity: 0.4; cursor: not-allowed; }
.notes-list { padding: 0 20px; }
.note-item { padding: 14px 0; border-bottom: 1px solid var(--border); }
.note-item:last-child { border-bottom: none; }
.note-content { font-size: 13px; color: var(--text); line-height: 1.6; margin-bottom: 8px; white-space: pre-wrap; }
.note-meta { display: flex; align-items: center; gap: 12px; font-size: 10px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; }
.note-del { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 12px; padding: 2px 6px; border-radius: 4px; margin-left: auto; }
.note-del:hover { color: var(--red); background: var(--red-dim); }
.notes-empty { padding: 24px 20px; color: var(--text-dim); font-size: 12px; text-align: center; font-family: "Nunito Sans", sans-serif; }
.edit-project-btn { position: absolute; top: 14px; right: 16px; font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); border-radius: 6px; color: #fff; cursor: pointer; transition: background 0.2s; }
.edit-project-btn:hover { background: rgba(255,255,255,0.25); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: var(--surface); border-radius: 12px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); display: flex; flex-direction: column; overflow: hidden; }
.modal-header { padding: 18px 24px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; background: var(--surface2); }
.modal-title { font-size: 15px; font-weight: 800; color: var(--text); }
.modal-close { background: none; border: none; font-size: 18px; color: var(--text-dim); cursor: pointer; padding: 4px 8px; border-radius: 4px; }
.modal-close:hover { background: var(--surface3); color: var(--text); }
.modal-body { padding: 24px; overflow-y: auto; flex: 1; }
.modal-footer { padding: 16px 24px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 10px; background: var(--surface2); }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.form-group label { font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: var(--text-dim); }
.form-input { background: var(--surface2); border: 1px solid var(--border-bright); border-radius: 7px; padding: 9px 12px; color: var(--text); font-family: 'Nunito Sans', sans-serif; font-size: 13px; width: 100%; box-sizing: border-box; }
.form-input:focus { outline: none; border-color: var(--accent); }
.form-row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }
.btn-cancel { font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 700; padding: 9px 20px; border: 1px solid var(--border-bright); border-radius: 7px; background: var(--surface2); color: var(--text-mid); cursor: pointer; }
.btn-submit { font-family: 'Nunito', sans-serif; font-size: 13px; font-weight: 700; padding: 9px 20px; border: none; border-radius: 7px; background: var(--accent); color: #fff; cursor: pointer; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.form-error { color: var(--red); font-size: 12px; margin-top: 8px; }
.form-section-title { font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; color: var(--text-dim); margin-bottom: 12px; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
.modal-edit { width: 620px; max-height: 85vh; }
.form-section-title { font-family: "Nunito Sans", sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; color: var(--text-dim); margin-bottom: 12px; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
.files-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
.files-header { padding: 14px 20px; border-bottom: 1px solid var(--border); background: var(--surface2); display: flex; align-items: center; justify-content: space-between; }
.files-title { font-size: 13px; font-weight: 800; }
.files-upload-btn { font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; background: var(--accent); border: none; border-radius: 6px; color: #fff; cursor: pointer; transition: background 0.2s; }
.files-upload-btn:hover { background: #2563eb; }
.files-upload-btn.uploading { opacity: 0.6; cursor: not-allowed; }
.files-list { padding: 0 20px; }
.file-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.file-item:last-child { border-bottom: none; }
.file-icon { font-size: 22px; flex-shrink: 0; }
.file-info { flex: 1; }
.file-name { font-size: 13px; font-weight: 700; color: var(--text); }
.file-meta { font-size: 10px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 2px; }
.file-del { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 14px; padding: 4px 8px; border-radius: 4px; }
.file-del:hover { color: var(--red); background: var(--red-dim); }
.files-empty { padding: 24px 20px; color: var(--text-dim); font-size: 12px; text-align: center; font-family: "Nunito Sans", sans-serif; }
.files-error { padding: 10px 20px; color: var(--red); font-size: 12px; background: var(--red-dim); }

@media (max-width: 768px) {
  .project-detail { padding: 12px; }
  .contract-header { padding: 16px 14px; border-radius: 10px; }
  .contract-kpis { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .kpi-box { padding: 12px 10px; }
  .kpi-val { font-size: 18px; }
  .gantt-panel { overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .gantt-lbl-col { width: 130px; font-size: 10px; }
  .gantt-mod-lbl { width: 130px; }
  .gantt-task-lbl { width: 130px; }
  .gantt-weeks-col { min-width: 500px; }
  .gantt-track { min-width: 500px; }
  .edit-project-btn { font-size: 10px; padding: 4px 10px; }
  .modules-title { font-size: 11px; }
  .mg-header { padding: 10px 12px; }
  .financial-grid { grid-template-columns: repeat(2, 1fr); }
  .fin-box { padding: 12px 10px; }
  .fin-val { font-size: 18px; }
  .notes-panel { padding: 14px 12px; }
}
</style>