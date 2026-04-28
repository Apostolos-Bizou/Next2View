<template>
  <div class="content">
    <div v-if="loading" class="loading">{{ t('pd.loading') }}</div>
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
        <button v-if="permStore.isCEO() || permStore.can('editProject')" class="edit-project-btn" @click="openEditModal" :title="t('pd.edit')">{{ t('pd.editBtn') }}</button>
        <button v-if="permStore.isCEO()" class="delete-project-btn" @click="confirmDeleteProject" :title="t('pd.deleteProject')">🗑 {{ t('pd.delete') }}</button>
        <div class="ch-top">
          <div>
            <div class="ch-title">{{ project.title }}</div>
            <div class="ch-meta">
              <span :style="`color:var(--${project.category});font-weight:700;`">{{ project.companyCode }}</span>
              <span>{{ project.companyName }}</span>
              <span v-if="project.startDate || project.deadline" :title="timelineTooltip">📅 {{ formatDateRange(project.startDate, project.deadline) }}</span>
              <span v-if="daysRemainingLabel" :class="['ch-days-pill', daysRemainingClass]">{{ daysRemainingLabel }}</span>
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
            <div class="cs-val" :style="`color:${smartStatus.color};font-size:13px;`">{{ smartStatus.icon }} {{ smartStatus.label }}</div>
          </div>
          <div v-if="project.budget" class="cs-item">
            <div class="cs-lbl">Budget</div>
            <div class="cs-val">€{{ Number(project.budget).toLocaleString() }}</div>
          </div>
        </div>
        <div class="ch-progress-wrap">
          <div class="ch-progress-label">{{ t('pd.tasks') }}</div>
          <div class="ch-progress-bar">
            <div class="ch-progress-fill" :style="`width:${project.completion}%;background:var(--${project.category});`"></div>
          </div>
          <span class="ch-progress-pct" :style="`color:var(--${project.category});`">{{ project.completion }}%</span>
        </div>
        <div v-if="timeProgress !== null" class="ch-progress-wrap" style="margin-top:8px;">
          <div class="ch-progress-label">{{ t('pd.time') }}</div>
          <div class="ch-progress-bar">
            <div class="ch-progress-fill" :style="`width:${timeProgress}%;background:${smartStatus.color};opacity:0.8;`"></div>
          </div>
          <span class="ch-progress-pct" :style="`color:${smartStatus.color};`">{{ timeProgress }}%</span>
        </div>
      </div>

      <!-- GANTT TIMELINE v2 -->
      <GanttV2 :project="project" @task-click="handleGanttTaskClick" />

      <!-- MODULES ACCORDION (detail) -->
      <div v-if="project.modules && project.modules.length" style="margin-top:14px;">
        <div class="modules-title">Modules & Tasks Detail</div>
        <div v-for="m in project.modules" :key="m.id+'acc'" class="module-group">
          <div class="mg-header" @click="toggleAcc(m.id)">
            <span class="mg-expand">{{ openAcc.has(m.id) ? '▼' : '▶' }}</span>
            <span class="mg-name">{{ m.name }}</span>
            <button class="delete-mod-btn" @click.stop="confirmDeleteModule(m)" :title="t('pd.deleteModule')">🗑</button>
            <div class="mg-right">
              <div class="mg-bar-wrap"><div class="mg-bar"><div class="mg-bar-fill" :style="`width:${m.completion}%;background:var(--${m.color||project.category});`"></div></div></div>
              <span class="mg-pct" :style="`color:var(--${m.color||project.category});`">{{ m.completion }}%</span>
              <span class="mg-tasks">{{ m.tasks.filter(tk=>tk.isDone).length }}/{{ m.tasks.length }}</span>
            </div>
          </div>
          <div v-if="openAcc.has(m.id)" class="task-list">
            <div v-for="tk in m.tasks" :key="tk.id+'acc'" class="task-item">
              <div :class="`task-check ${tk.isDone?'done':tk.isBlocked?'block':''}`" @click.stop="toggleTask(tk)" style="cursor:pointer;">{{ tk.isDone?'✓':'' }}</div>
              <div style="flex:1;">
                <div :class="`task-name ${tk.isDone?'done':''}`">{{ tk.name }}</div>
                <button class="delete-task-btn" @click.stop="confirmDeleteTask(tk, m)" :title="t('pd.deleteTask')">🗑</button>
                <div v-if="tk.blockNote" class="task-note">⚠ {{ tk.blockNote }}</div>
              </div>
              <span class="task-assignee">{{ tk.assignee||'—' }}</span>
              <div class="task-pct-wrap">
                <div class="task-bar"><div class="task-bar-fill" :style="`width:${displayProgress(tk)}%;background:${tk.isDone?'var(--green)':'var(--'+( m.color||project.category)+')'};`"></div></div>
                <div class="task-pct" :style="`color:${tk.isDone ? 'var(--green)' : 'var(--' + (m.color || project.category) + ')'}`">{{ displayProgress(tk) }}%<span v-if="mismatchAlert(tk)" :title="t('pd.behindSchedule')" style="color:var(--red);margin-left:4px;">⚠</span></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- SPECS -->
      <div v-if="project.specs && project.specs.length" class="specs-panel" style="margin-top:14px;">
        <div class="specs-title" style="display:flex;align-items:center;justify-content:space-between;">📋 Specifications <button @click="quickAddModule" style="font-size:11px;padding:4px 12px;background:var(--accent);color:#fff;border:none;border-radius:5px;cursor:pointer;font-weight:700;">+ Module</button></div>
        <div class="specs-list">
          <div v-for="s in project.specs" :key="s.id" class="spec-item" @click="openSpecDetail(s)" style="cursor:pointer;">
            <div :class="specCheckClass(s)" @click.stop="toggleSpec(s)" style="cursor:pointer;">{{ s.isDone ? '✓' : '' }}</div>
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
        <div class="cd-title">{{ t('pd.contractDesc') }}</div>
        <div class="cd-text">{{ project.contractDesc }}</div>
      </div>

      <!-- CONTRACT FILES -->
      <div class="files-panel" style="margin-top:14px;">
        <div class="files-header">
          <div class="files-title">{{ t('pd.filesTitle') }}</div>
          <label v-if="permStore.isCEO() || permStore.can('uploadFiles')" class="files-upload-btn" :class="{uploading: uploading}">
            <input type="file" @change="uploadFile" accept=".pdf,.doc,.docx,.xlsx,.png,.jpg" style="display:none" :disabled="uploading" />
            {{ uploading ? t('pd.uploading') : t('pd.upload') }}
          </label>
        </div>
        <div v-if="files.length" class="files-list">
          <div v-for="f in files" :key="f.id" class="file-item" @click="openFile(f)" style="cursor:pointer;" :title="t('pd.clickToOpen')">
            <span class="file-icon">{{ fileIcon(f.contentType) }}</span>
            <div class="file-info">
              <div class="file-name">{{ f.fileName }}</div>
              <div class="file-meta">{{ formatSize(f.fileSizeBytes) }} · {{ f.uploadedBy }} · {{ formatInstant(f.uploadedAt) }}</div>
            </div>
            <button class="file-del" @click.stop="deleteFile(f.id)" :title="t('pd.delete')">✕</button>
          </div>
        </div>
        <div v-else-if="!uploading" class="files-empty">{{ t('pd.noFiles') }}</div>
        <div v-if="uploadError" class="files-error">{{ uploadError }}</div>
        <div v-if="fileError" class="files-error">{{ fileError }}</div>
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
          <button class="notes-add-btn" @click="showNoteInput=!showNoteInput">{{ t('pd.newNote') }}</button>
        </div>
        <div v-if="showNoteInput" class="note-input-wrap">
          <textarea v-model="newNote" :placeholder="t('pd.notePlaceholder')" class="note-textarea" rows="3"></textarea>
          <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:8px;">
            <button class="note-cancel" @click="cancelNote">{{ t('pd.cancel') }}</button>
            <button class="note-save" @click="saveNote" :disabled="!newNote.trim()">{{ t('pd.save') }}</button>
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
        <div v-else-if="!showNoteInput" class="notes-empty">{{ t('pd.noNotes') }}</div>
      </div>

    </div>
    <div v-else class="loading">Project not found.</div>
  </div>
  
    <!-- TASK EDIT MODAL (Gantt v3) -->
    <div v-if="editingTask" class="modal-overlay" @click.self="closeTaskEdit">
      <div class="modal task-edit-modal">
        <div class="modal-header">
          <div class="modal-title">{{ t('pd.editTask') }}</div>
          <button class="modal-close" @click="closeTaskEdit" :disabled="editingTaskSaving">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="editingTaskError" class="te-error">{{ editingTaskError }}</div>

          <div class="te-field">
            <label>{{ t('pd.name') }}</label>
            <input type="text" v-model="editingTask.name" :disabled="editingTaskSaving" />
          </div>

          <div class="te-row">
            <div class="te-field">
              <label>Assignee</label>
              <input type="text" v-model="editingTask.assignee" :placeholder="t('pd.assigneePlaceholder')" :disabled="editingTaskSaving" />
            </div>
            <div class="te-field">
              <label>Module</label>
              <input type="text" :value="editingTaskModule ? editingTaskModule.name : ''" disabled />
            </div>
          </div>

          <div class="te-field">
            <label>
              {{ t('pd.progress') }}: <strong>{{ editingTask.progress || 0 }}%</strong>
            </label>
            <input
              type="range"
              min="0"
              max="100"
              step="5"
              v-model.number="editingTask.progress"
              :disabled="editingTaskSaving"
              class="te-range"
            />
            <div class="te-range-ticks">
              <span>0%</span><span>25%</span><span>50%</span><span>75%</span><span>100%</span>
            </div>
          </div>

          <div class="te-row">
            <div class="te-field">
              <label>{{ t('pd.start') }}</label>
              <input type="date" v-model="editingTask.startDate" :disabled="editingTaskSaving" />
            </div>
            <div class="te-field">
              <label>{{ t('pd.end') }}</label>
              <input type="date" v-model="editingTask.endDate" :disabled="editingTaskSaving" />
            </div>
          </div>

          <div class="te-toggles">
            <label class="te-toggle">
              <input type="checkbox" v-model="editingTask.isDone" :disabled="editingTaskSaving" />
              <span>{{ t('pd.completed') }}</span>
            </label>
            <label class="te-toggle">
              <input type="checkbox" v-model="editingTask.isBlocked" :disabled="editingTaskSaving" />
              <span>{{ t('pd.blocked') }}</span>
            </label>
          </div>

          <div v-if="editingTask.isBlocked" class="te-field">
            <label>{{ t('pd.blockNoteLabel') }}</label>
            <input
              type="text"
              v-model="editingTask.blockNote"
              :placeholder="t('pd.blockNotePlaceholder')"
              :disabled="editingTaskSaving"
            />
          </div>

          <div class="te-field">
            <label>{{ t('pd.comment') }}</label>
            <textarea
              v-model="editingTask.comment"
              rows="3"
              :placeholder="t('pd.commentPlaceholder')"
              :disabled="editingTaskSaving"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer te-footer">
          <button class="te-btn te-btn-danger" @click="deleteTaskFromModal" :disabled="editingTaskSaving">
            🗑 Διαγραφή
          </button>
          <div style="flex:1"></div>
          <button class="te-btn te-btn-ghost" @click="closeTaskEdit" :disabled="editingTaskSaving">
            Άκυρο
          </button>
          <button class="te-btn te-btn-primary" @click="saveTaskEdit" :disabled="editingTaskSaving">
            {{ editingTaskSaving ? t('pd.saving') : t('pd.save') }}
          </button>
        </div>
      </div>
    </div>

    <!-- EDIT PROJECT MODAL -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal=false">
    <div class="modal modal-edit">
      <div class="modal-header">
        <div class="modal-title">{{ t('pd.editProject') }}</div>
        <button class="modal-close" @click="showEditModal=false">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-section-title">{{ t('pd.basicInfo') }}</div>
        <div class="form-group">
          <label>{{ t('pd.titleReq') }}</label>
          <input v-model="editForm.title" type="text" class="form-input" :placeholder="t('pd.titlePlaceholder')" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>{{ t('pd.companyReq') }}</label>
            <select v-model="editForm.companyId" class="form-input">
              <option v-for="co in companies" :key="co.id" :value="co.id">{{ co.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ t('pd.categoryReq') }}</label>
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
            <label>{{ t('pd.startDateLabel') }}</label>
            <input v-model="editForm.startDate" type="date" class="form-input" />
          </div>
          <div class="form-group">
            <label>Deadline</label>
            <input v-model="editForm.deadline" type="date" class="form-input" />
          </div>
        </div>
        <div class="form-group">
          <label>{{ t('pd.contractDescLabel') }}</label>
          <textarea v-model="editForm.contractDesc" class="form-input" rows="3" :placeholder="t('pd.briefDesc')"></textarea>
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
      <!-- MODULES SECTION -->
      <div class="modal-body" style="border-top:1px solid var(--border);padding-top:20px;">
        <div class="form-section-title" style="display:flex;justify-content:space-between;align-items:center;">
          MODULES & TASKS
          <button @click="editAddModule" style="font-size:11px;padding:4px 10px;background:var(--accent);color:#fff;border:none;border-radius:5px;cursor:pointer;">+ Module</button>
        </div>
        <div v-for="(m, mi) in editForm.modules" :key="mi" style="margin-bottom:16px;background:var(--surface2);border-radius:8px;padding:12px;">
          <div style="display:flex;gap:8px;align-items:center;margin-bottom:8px;">
            <input v-model="m.name" type="text" class="form-input" style="flex:1;" :placeholder="'Module ' + (mi+1) + ' name'" />
            <select v-model="m.color" class="form-input" style="width:130px;">
              <option value="finance">Finance</option>
              <option value="legal">Legal</option>
              <option value="dev">Dev</option>
              <option value="marketing">Marketing</option>
            </select>
            <button @click="editForm.modules.splice(mi,1)" style="background:var(--red-dim);color:var(--red);border:none;border-radius:5px;padding:4px 8px;cursor:pointer;">✕</button>
          </div>
          <div v-for="(tk, ti) in m.tasks" :key="ti" style="display:flex;gap:6px;align-items:center;margin-bottom:6px;padding-left:12px;">
            <div style="display:flex;flex-direction:column;gap:4px;flex:1;">
              <div style="display:flex;gap:6px;align-items:center;">
                <input v-model="tk.name" type="text" class="form-input" style="flex:2;" placeholder="Task name" />
                <input v-model="tk.assignee" type="text" class="form-input" style="flex:1;" placeholder="Assignee" />
                <input v-model.number="tk.progress" type="number" class="form-input" style="width:65px;" min="0" max="100" placeholder="%" />
                <button @click="m.tasks.splice(ti,1)" style="background:var(--red-dim);color:var(--red);border:none;border-radius:5px;padding:4px 6px;cursor:pointer;">✕</button>
              </div>
              <div style="display:flex;gap:6px;align-items:center;padding-left:4px;">
                <label style="font-size:10px;color:var(--text-dim);min-width:55px;">{{ t('pd.start') }}</label>
                <input v-model="tk.startDate" type="date" class="form-input" style="flex:1;font-size:11px;padding:4px 6px;" />
                <label style="font-size:10px;color:var(--text-dim);min-width:35px;">{{ t('pd.end') }}</label>
                <input v-model="tk.endDate" type="date" class="form-input" style="flex:1;font-size:11px;padding:4px 6px;" />
              </div>
            </div>
          </div>
          <button @click="m.tasks.push({name:'',assignee:'',progress:0,isDone:false,isBlocked:false,blockNote:'',comment:'',deadline:null,startWeek:mi+1,durationWeeks:1,sortOrder:m.tasks.length})"
            style="font-size:11px;padding:3px 10px;background:var(--surface3);color:var(--text-mid);border:1px solid var(--border-bright);border-radius:5px;cursor:pointer;margin-left:12px;">
            + Task
          </button>
        </div>
        <div v-if="editForm.modules.length===0" style="text-align:center;color:var(--text-dim);font-size:13px;padding:12px;">
          {{ t('pd.noModules') }}
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="showEditModal=false">{{ t('pd.cancel') }}</button>
        <button class="btn-submit" @click="saveEdit" :disabled="editSaving">
          {{ editSaving ? t('pd.saving') : t('pd.save') }}
        </button>
      </div>
    </div>
  </div>

</template>

<script setup>
import GanttV2 from '@/components/GanttV2.vue'
import { getTimeProgress, getDaysRemaining, getSmartStatus, formatDateRange, formatDaysRemaining } from '@/utils/projectMetrics'

// ─── Gantt v3: Task Edit Modal ───

// ══ Smart project metrics ══
const timeProgress = computed(() => {
  if (!project.value) return null
  return getTimeProgress(project.value.startDate, project.value.deadline)
})
const daysRemaining = computed(() => {
  if (!project.value || !project.value.deadline) return null
  return getDaysRemaining(project.value.deadline)
})
const daysRemainingLabel = computed(() => {
  const d = daysRemaining.value
  if (d === null) return ''
  return formatDaysRemaining(d)
})
const daysRemainingClass = computed(() => {
  const d = daysRemaining.value
  if (d === null) return ''
  if (d < 0) return 'overdue'
  if (d <= 7) return 'urgent'
  if (d <= 30) return 'warning'
  return 'ok'
})
const smartStatus = computed(() => {
  if (!project.value) return { code: 'on_track', label: 'On Track', color: '#059669', icon: '🟢' }
  return getSmartStatus(timeProgress.value, project.value.completion, project.value.status)
})
const timelineTooltip = computed(() => {
  if (!project.value) return ''
  const tp = timeProgress.value
  if (tp === null) return t('pd.timelineRange')
  return t('pd.timeElapsed', {pct: tp})
})

const editingTask = ref(null)           // the task being edited (cloned)
const editingTaskModule = ref(null)     // the module that owns it
const editingTaskSaving = ref(false)
const editingTaskError = ref('')

function handleGanttTaskClick(taskId) {
  if (!taskId) return
  const mods = project.value?.modules || []
  for (const m of mods) {
    const t = (m.tasks || []).find(x => x.id === taskId)
    if (t) {
      // Clone the task so edits don't mutate the list until Save
      editingTask.value = JSON.parse(JSON.stringify(t))
      editingTaskModule.value = m
      editingTaskError.value = ''
      return
    }
  }
}

function closeTaskEdit() {
  editingTask.value = null
  editingTaskModule.value = null
  editingTaskError.value = ''
  editingTaskSaving.value = false
}

async function saveTaskEdit() {
  if (!editingTask.value || !editingTaskModule.value) return
  editingTaskSaving.value = true
  editingTaskError.value = ''
  const edited = editingTask.value
  const mod = editingTaskModule.value
  // Clamp progress to 0-100
  edited.progress = Math.max(0, Math.min(100, parseInt(edited.progress) || 0))
  // If progress is 100, auto-mark as done; if not done, keep current isDone
  if (edited.progress === 100) edited.isDone = true
  // If manually toggled done, set progress accordingly
  if (edited.isDone && edited.progress < 100) edited.progress = 100
  if (!edited.isDone && edited.progress === 100) edited.progress = 99

  // Apply the edit back to the project tree locally (optimistic)
  const modIdx = project.value.modules.findIndex(x => x.id === mod.id)
  if (modIdx === -1) { editingTaskSaving.value = false; return }
  const taskIdx = project.value.modules[modIdx].tasks.findIndex(x => x.id === edited.id)
  if (taskIdx === -1) { editingTaskSaving.value = false; return }
  // Preserve id + update fields
  Object.assign(project.value.modules[modIdx].tasks[taskIdx], edited)

  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title,
      companyId: project.value.companyId,
      category: project.value.category,
      budget: project.value.budget || 0,
      startDate: project.value.startDate || null,
      deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      status: project.value.status,
      specs: (project.value.specs || []).map(s => ({
        description: s.description, isDone: s.isDone,
        sortOrder: s.sortOrder || 0,
        startDate: s.startDate || null, endDate: s.endDate || null
      })),
      modules: (project.value.modules || []).map(m2 => ({
        name: m2.name, color: m2.color, sortOrder: m2.sortOrder || 0,
        tasks: (m2.tasks || []).map(t => ({
          name: t.name, assignee: t.assignee, progress: t.progress,
          isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote,
          comment: t.comment, deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks,
          startDay: t.startDay, durationDays: t.durationDays,
          sortOrder: t.sortOrder || 0,
          startDate: t.startDate || null, endDate: t.endDate || null
        }))
      }))
    })
    closeTaskEdit()
    await loadProject()
  } catch (e) {
    editingTaskError.value = t('pd.err.saveFailed')
    // Revert by reloading
    await loadProject()
  } finally {
    editingTaskSaving.value = false
  }
}

async function deleteTaskFromModal() {
  if (!editingTask.value || !editingTaskModule.value) return
  if (!confirm(t('pd.confirmDeleteTask', {name: editingTask.value.name}))) return
  const modId = editingTaskModule.value.id
  const taskId = editingTask.value.id
  editingTaskSaving.value = true
  const modIdx = project.value.modules.findIndex(x => x.id === modId)
  if (modIdx === -1) { editingTaskSaving.value = false; return }
  const taskIdx = project.value.modules[modIdx].tasks.findIndex(x => x.id === taskId)
  if (taskIdx === -1) { editingTaskSaving.value = false; return }
  project.value.modules[modIdx].tasks.splice(taskIdx, 1)

  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title,
      companyId: project.value.companyId,
      category: project.value.category,
      budget: project.value.budget || 0,
      startDate: project.value.startDate || null,
      deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      status: project.value.status,
      specs: (project.value.specs || []).map(s => ({
        description: s.description, isDone: s.isDone,
        sortOrder: s.sortOrder || 0,
        startDate: s.startDate || null, endDate: s.endDate || null
      })),
      modules: (project.value.modules || []).map(m2 => ({
        name: m2.name, color: m2.color, sortOrder: m2.sortOrder || 0,
        tasks: (m2.tasks || []).map(t => ({
          name: t.name, assignee: t.assignee, progress: t.progress,
          isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote,
          comment: t.comment, deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks,
          startDay: t.startDay, durationDays: t.durationDays,
          sortOrder: t.sortOrder || 0,
          startDate: t.startDate || null, endDate: t.endDate || null
        }))
      }))
    })
    closeTaskEdit()
    await loadProject()
  } catch (e) {
    editingTaskError.value = t('pd.err.deleteFailed')
    await loadProject()
  } finally {
    editingTaskSaving.value = false
  }
}



import { ref, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import api from '@/services/api'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'
import { usePermissionStore } from '@/stores/permissions'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const store = useProjectStore()
const permStore = usePermissionStore()

const project = ref(null)
const loading = ref(true)
const collapsedMods = ref(new Set())
const openAcc = ref(new Set())

const GANTT_WEEKS = 12

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
  // Permission guard: skip fetch αν δεν μπορεί να δει CEO notes
  if (!permStore.isCEO() && !permStore.can('viewCeoNotes')) {
    notes.value = []
    return
  }
  try {
    const res = await api.get(`/projects/${project.value.id}/notes`)
    notes.value = res.data
  } catch { notes.value = [] }
}

// ════ CONTRACT FILES ════
const files = ref([])
const uploading = ref(false)
const uploadError = ref("")

const fileError = ref("")

async function openFile(f) {
  fileError.value = ""
  try {
    const url = `/projects/${project.value.id}/files/${f.id}/content`
    const res = await api.get(url, { responseType: "blob" })
    const blob = new Blob([res.data], { type: f.contentType || "application/octet-stream" })
    const objectUrl = URL.createObjectURL(blob)
    const isPreviewable = f.contentType && (
      f.contentType.includes("pdf") || f.contentType.startsWith("image/")
    )
    if (isPreviewable) {
      window.open(objectUrl, "_blank")
      setTimeout(() => URL.revokeObjectURL(objectUrl), 60000)
    } else {
      const a = document.createElement("a")
      a.href = objectUrl
      a.download = f.fileName || "document"
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(objectUrl)
    }
  } catch (e) {
    console.error("File open error:", e)
    fileError.value = t('pd.err.openFailed')
  }
}

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
    uploadError.value = e.response?.data?.message || t('pd.err.uploadFailed')
  } finally {
    uploading.value = false
    event.target.value = ""
  }
}

async function deleteFile(fileId) {
  if (!confirm(t('pd.confirmDeleteFile'))) return
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

async function toggleTask(t) {
  const prev = t.isDone
  const prevPct = t.progress
  t.isDone = !t.isDone
  t.progress = t.isDone ? 100 : 0
  t.manualProgress = true
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title,
      companyId: project.value.companyId,
      category: project.value.category,
      budget: project.value.budget || 0,
      startDate: project.value.startDate || null,
      deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      status: project.value.status,
      specs: (project.value.specs || []).map(s => ({
        description: s.description, isDone: s.isDone, sortOrder: s.sortOrder || 0,
        startDate: s.startDate || null, endDate: s.endDate || null
      })),
      modules: (project.value.modules || []).map(m => ({
        name: m.name, color: m.color, sortOrder: m.sortOrder || 0,
        tasks: (m.tasks || []).map(t2 => ({
          name: t2.name, assignee: t2.assignee, progress: t2.progress,
          isDone: t2.isDone, isBlocked: t2.isBlocked, blockNote: t2.blockNote,
          comment: t2.comment, deadline: t2.deadline,
          startWeek: t2.startWeek, durationWeeks: t2.durationWeeks,
          startDay: t2.startDay, durationDays: t2.durationDays, sortOrder: t2.sortOrder || 0, manualProgress: t2.manualProgress || false,
          startDate: t2.startDate || null, endDate: t2.endDate || null,

        }))
      }))
    })
  } catch(e) {
    t.isDone = prev
    t.progress = prevPct
  }
}

async function toggleSpec(s) {
  const prev = s.isDone
  s.isDone = !s.isDone
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title,
      companyId: project.value.companyId,
      category: project.value.category,
      budget: project.value.budget || 0,
      startDate: project.value.startDate || null,
      deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      status: project.value.status,
      specs: (project.value.specs || []).map(s2 => ({
        description: s2.description, isDone: s2.isDone, sortOrder: s2.sortOrder || 0,
        startDate: s2.startDate || null, endDate: s2.endDate || null
      })),
      modules: (project.value.modules || []).map(m => ({
        name: m.name, color: m.color, sortOrder: m.sortOrder || 0,
        tasks: (m.tasks || []).map(t => ({
          name: t.name, assignee: t.assignee, progress: t.progress,
          isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote,
          comment: t.comment, deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks,
          startDay: t.startDay, durationDays: t.durationDays, sortOrder: t.sortOrder || 0, startDate: t.startDate || null, endDate: t.endDate || null
        }))
      }))
    })
  } catch(e) {
    s.isDone = prev
  }
}

async function confirmDeleteProject() {
  if (!confirm(t('pd.confirmDeleteProject', {name: project.value.title}))) return
  try {
    await api.delete('/projects/' + project.value.id)
    router.push('/projects')
  } catch(e) { alert(t('pd.err.deleteProjectFailed')) }
}

async function confirmDeleteModule(m) {
  if (!confirm(t('pd.confirmDeleteModule', {name: m.name}))) return
  const idx = project.value.modules.findIndex(x => x.id === m.id)
  if (idx === -1) return
  project.value.modules.splice(idx, 1)
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title, companyId: project.value.companyId,
      category: project.value.category, budget: project.value.budget || 0,
      startDate: project.value.startDate || null, deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '', status: project.value.status,
      specs: (project.value.specs || []).map(s => ({ description: s.description, isDone: s.isDone, sortOrder: s.sortOrder || 0, startDate: s.startDate || null, endDate: s.endDate || null })),
      modules: (project.value.modules || []).map(m2 => ({
        name: m2.name, color: m2.color, sortOrder: m2.sortOrder || 0,
        tasks: (m2.tasks || []).map(t => ({ name: t.name, assignee: t.assignee, progress: t.progress, isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote, comment: t.comment, deadline: t.deadline, startWeek: t.startWeek, durationWeeks: t.durationWeeks, startDay: t.startDay, durationDays: t.durationDays, sortOrder: t.sortOrder || 0, startDate: t.startDate || null, endDate: t.endDate || null }))
      }))
    })
  } catch(e) { await loadProject() }
}

async function confirmDeleteTask(t, m) {
  if (!confirm(t('pd.confirmDeleteTask', {name: t.name}))) return
  const tidx = m.tasks.findIndex(x => x.id === t.id)
  if (tidx === -1) return
  m.tasks.splice(tidx, 1)
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title, companyId: project.value.companyId,
      category: project.value.category, budget: project.value.budget || 0,
      startDate: project.value.startDate || null, deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '', status: project.value.status,
      specs: (project.value.specs || []).map(s => ({ description: s.description, isDone: s.isDone, sortOrder: s.sortOrder || 0, startDate: s.startDate || null, endDate: s.endDate || null })),
      modules: (project.value.modules || []).map(m2 => ({
        name: m2.name, color: m2.color, sortOrder: m2.sortOrder || 0,
        tasks: (m2.tasks || []).map(t2 => ({ name: t2.name, assignee: t2.assignee, progress: t2.progress, isDone: t2.isDone, isBlocked: t2.isBlocked, blockNote: t2.blockNote, comment: t2.comment, deadline: t2.deadline, startWeek: t2.startWeek, durationWeeks: t2.durationWeeks, startDay: t2.startDay, durationDays: t2.durationDays, sortOrder: t2.sortOrder || 0, manualProgress: t2.manualProgress || false }))
      }))
    })
  } catch(e) { await loadProject() }
}

// ════ SPEC DETAIL MODAL ════
const showSpecModal = ref(false)
const selectedSpec = ref({})
const specSaveError = ref('')
const specSaving = ref(false)

function openSpecDetail(s) {
  selectedSpec.value = { ...s }
  specSaveError.value = ''
  showSpecModal.value = true
}

async function saveSpecDetail() {
  specSaving.value = true
  specSaveError.value = ''
  // Update in project.specs
  const idx = project.value.specs.findIndex(s => s.id === selectedSpec.value.id)
  if (idx !== -1) {
    project.value.specs[idx] = { ...selectedSpec.value }
  }
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title,
      companyId: project.value.companyId,
      category: project.value.category,
      budget: project.value.budget || 0,
      startDate: project.value.startDate || null,
      deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      status: project.value.status,
      specs: (project.value.specs || []).map(s => ({
        description: s.description, isDone: s.isDone, sortOrder: s.sortOrder || 0,
        startDate: s.startDate || null, endDate: s.endDate || null
      })),
      modules: (project.value.modules || []).map(m => ({
        name: m.name, color: m.color, sortOrder: m.sortOrder || 0,
        tasks: (m.tasks || []).map(t => ({
          name: t.name, assignee: t.assignee, progress: t.progress,
          isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote,
          comment: t.comment, deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks,
          startDay: t.startDay, durationDays: t.durationDays,
          sortOrder: t.sortOrder || 0, manualProgress: t.manualProgress || false,
          startDate: t.startDate || null, endDate: t.endDate || null
        }))
      }))
    })
    showSpecModal.value = false
  } catch(e) {
    specSaveError.value = t('pd.err.saveFailed')
  } finally { specSaving.value = false }
}

function quickAddModule() {
  // Open edit modal and scroll to modules section
  openEditModal()
  nextTick(() => {
    setTimeout(() => {
      const modSection = document.querySelector('.modal .form-section-title:last-of-type')
      if (modSection) modSection.scrollIntoView({ behavior: 'smooth' })
      // Auto-add one empty module if none exist
      if (editForm.value.modules.length === 0) {
        editAddModule()
      }
    }, 200)
  })
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
    modules: (project.value.modules || []).map(m => ({ ...m, tasks: (m.tasks || []).map(t => ({ ...t })) })),
    specs: (project.value.specs || []).map(s => ({ ...s })),
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
  if (!editForm.value.title.trim()) { editError.value = t('pd.err.titleRequired'); return }
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
          startWeek: t.startWeek, durationWeeks: t.durationWeeks, sortOrder: t.sortOrder || 0,
          startDate: t.startDate || null, endDate: t.endDate || null
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
    editError.value = e.response?.data?.message || t('pd.err.saveFailed')
  } finally { editSaving.value = false }
}

async function loadProject() {
  project.value = await store.fetchProject(route.params.id)
}

function editAddModule() {
  editForm.value.modules.push({
    name: '', color: editForm.value.category || 'dev', sortOrder: editForm.value.modules.length,
    tasks: []
  })
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
  const m = t('months.short')
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
  const months = t('months.short')
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

// ════ AUTO PROGRESS ════
function autoProgressPct(t) {
  if (t.startDay == null || t.durationDays == null || !project.value?.startDate) return null
  const projStart = new Date(project.value.startDate).getTime()
  const taskStart = projStart + t.startDay * 86400000
  const taskEnd = taskStart + t.durationDays * 86400000
  const now = new Date().getTime()
  if (now <= taskStart) return 0
  if (now >= taskEnd) return 100
  return Math.round((now - taskStart) / (taskEnd - taskStart) * 100)
}

function displayProgress(t) {
  if (t.manualProgress) return t.progress
  const auto = autoProgressPct(t)
  return auto !== null ? auto : t.progress
}

function mismatchAlert(t) {
  if (!t.manualProgress) return false
  const auto = autoProgressPct(t)
  if (auto === null) return false
  return (auto - t.progress) > 20
}

function taskBarStyle(t) {
  const totalDays = GANTT_WEEKS * 7
  // Πρώτα: αν υπάρχουν startDate/endDate χρησιμοποίησε τα (V13)
  if (t.startDate && project.value?.startDate) {
    const projStart = new Date(project.value.startDate).getTime()
    const gs = ganttStart.value.getTime()
    const taskStart = new Date(t.startDate).getTime()
    const taskEnd = t.endDate ? new Date(t.endDate).getTime() : taskStart + 7 * 86400000
    const left = (taskStart - gs) / (totalDays * 86400000) * 100
    const width = Math.max((1 / totalDays) * 100, (taskEnd - taskStart) / 86400000 / totalDays * 100)
    if (left >= 100 || left + width < 0) return { show: false }
    const l = Math.max(0, left)
    const w = Math.min(width, 100 - l)
    return { show: true, left: l, width: w }
  }
  if (t.startDay != null && t.durationDays != null) {
    const left = (t.startDay / totalDays) * 100
    const width = Math.max((1 / totalDays) * 100, (t.durationDays / totalDays) * 100)
    if (left >= 100) return { show: false }
    return { show: true, left, width: Math.min(width, 100 - left) }
  }
  if (t.startWeek != null) {
    const left = ((t.startWeek - 1) * 7 / totalDays) * 100
    const dur = (t.durationWeeks || 1) * 7
    const width = Math.max((1 / totalDays) * 100, (dur / totalDays) * 100)
    if (left >= 100) return { show: false }
    return { show: true, left, width: Math.min(width, 100 - left) }
  }
  return { show: false }
}

function specBarStyle(s) {
  if (!s.startDate || !project.value?.startDate) return { show: false }
  const projStart = new Date(project.value.startDate).getTime()
  const specStart = new Date(s.startDate).getTime()
  const specEnd = s.endDate ? new Date(s.endDate).getTime() : specStart + 86400000
  const totalDays = GANTT_WEEKS * 7
  const left = (specStart - projStart) / 86400000 / totalDays * 100
  const width = Math.max(1, (specEnd - specStart) / 86400000 / totalDays * 100)
  if (left >= 100 || left + width < 0) return { show: false }
  const l = Math.max(0, left)
  const w = Math.min(width, 100 - l)
  return { show: true, left: l, width: w, css: 'position:absolute;top:50%;transform:translateY(-50%);left:' + l + '%;width:' + w + '%;background:' + (s.isDone ? '#a0a0b8' : 'var(--legal)') + ';opacity:0.9;border-radius:4px;height:20px;display:flex;align-items:center;overflow:hidden;min-width:20px;' }
}

function moduleBarStyle(m) {
  const totalDays = GANTT_WEEKS * 7
  const tasks = m.tasks.filter(t => t.startDay != null || t.startWeek != null)
  if (!tasks.length) return { show: false }
  if (tasks[0].startDay != null) {
    const minDay = Math.min(...tasks.map(t => t.startDay || 0))
    const maxDay = Math.max(...tasks.map(t => (t.startDay || 0) + (t.durationDays || 1)))
    const left = (minDay / totalDays) * 100
    const width = ((maxDay - minDay) / totalDays) * 100
    return { show: true, left, width: Math.min(width, 100 - left) }
  }
  const minW = Math.min(...tasks.map(t => t.startWeek || 1))
  const maxW = Math.max(...tasks.map(t => (t.startWeek || 1) + (t.durationWeeks || 1)))
  const left = ((minW - 1) * 7 / totalDays) * 100
  const width = ((maxW - minW) * 7 / totalDays) * 100
  return { show: true, left, width: Math.min(Math.max(width, 3), 100 - left) }
}

const catLabel = (c) => ({ finance:'Finance', legal:'Legal', dev:'Developing', marketing:'Marketing' }[c] || c)
const catIcon  = (c) => ({ finance:'$', legal:'⚖', dev:'⌨', marketing:'◈' }[c] || '·')
const statusLabel = (s) => ({ on_track:'On Track', delayed:'Delayed', at_risk:'At Risk', stale:'Stale', completed:'Completed' }[s] || s)

function formatDate(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  const m = t('months.short')
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
.gantt-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; overflow: visible; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
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

.gantt-spec-bar { position:absolute; }
.gantt-spec-bar-txt { font-size:10px; color:#fff; padding:0 6px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; font-weight:600; width:100%; display:block; }
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
.delete-project-btn { position: absolute; top: 14px; right: 130px; font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; background: rgba(220,38,38,0.2); border: 1px solid rgba(220,38,38,0.4); border-radius: 6px; color: #fca5a5; cursor: pointer; transition: background 0.2s; }
.delete-project-btn:hover { background: rgba(220,38,38,0.35); }
.delete-mod-btn { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 13px; padding: 2px 6px; border-radius: 4px; opacity: 0.5; transition: opacity 0.2s; margin-left: auto; }
.delete-mod-btn:hover { opacity: 1; color: var(--red); }
.delete-task-btn { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 12px; padding: 2px 4px; border-radius: 4px; opacity: 0; transition: opacity 0.2s; margin-left: 4px; }
.task-row:hover .delete-task-btn { opacity: 0.6; }
.delete-task-btn:hover { opacity: 1 !important; color: var(--red); }
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
.file-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); transition: background 0.15s; border-radius: 6px; padding-left: 8px; }
.file-item:hover { background: var(--accent-dim); }
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
  .content { padding: 12px !important; overflow-x: hidden !important; }
  .project-detail { padding: 12px; }
  .contract-stats { grid-template-columns: 1fr 1fr !important; }
  .fin-grid { grid-template-columns: 1fr 1fr !important; }
  .modal { width: 95vw !important; max-width: 95vw !important; }
  .modal-body { padding: 14px !important; }
  .form-row { grid-template-columns: 1fr !important; }
  .contract-header { padding: 16px 14px; border-radius: 10px; }
  .contract-kpis { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .kpi-box { padding: 12px 10px; }
  .kpi-val { font-size: 18px; }
  .gantt-panel { overflow: visible; }
  .gantt-scroll { overflow-x: auto; -webkit-overflow-scrolling: touch; min-width: 0; }
  .gantt-header { min-width: 600px; }
  .gantt-lbl-col { width: 110px; flex-shrink: 0; font-size: 9px; }
  .gantt-mod-lbl { width: 110px; flex-shrink: 0; }
  .gantt-task-lbl { width: 110px; flex-shrink: 0; }
  .gantt-weeks-col { min-width: 480px; }
  .gantt-track { min-width: 480px; }
  .gantt-ph { flex-wrap: wrap; gap: 8px; padding: 10px 12px; }
  .gantt-ph-sub { font-size: 10px; }
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





/* ═══ TASK EDIT MODAL (Gantt v3) ═══ */
.task-edit-modal { width: 560px; max-width: 95vw; max-height: 90vh; display: flex; flex-direction: column; }
.task-edit-modal .modal-body { overflow-y: auto; padding: 20px 22px; display: flex; flex-direction: column; gap: 14px; }

.te-error {
  background: var(--red-dim, #fee2e2);
  color: var(--red, #dc2626);
  padding: 10px 14px; border-radius: 6px;
  font-size: 13px; font-weight: 600;
  border-left: 3px solid var(--red, #dc2626);
}

.te-field { display: flex; flex-direction: column; gap: 6px; }
.te-field label {
  font-family: "Nunito Sans", sans-serif;
  font-size: 11px; font-weight: 800;
  letter-spacing: 1.2px; text-transform: uppercase;
  color: var(--text-dim);
}
.te-field input[type="text"],
.te-field input[type="date"],
.te-field textarea {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  background: var(--surface);
  color: var(--text);
  transition: border-color 0.15s;
  width: 100%;
  box-sizing: border-box;
}
.te-field input[type="text"]:focus,
.te-field input[type="date"]:focus,
.te-field textarea:focus {
  outline: none;
  border-color: var(--accent, #3b82f6);
}
.te-field input:disabled,
.te-field textarea:disabled {
  background: var(--surface2);
  color: var(--text-dim);
  cursor: not-allowed;
}
.te-field textarea { resize: vertical; min-height: 64px; font-family: inherit; }

.te-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.te-row .te-field { min-width: 0; }
@media (max-width: 520px) { .te-row { grid-template-columns: 1fr; } }

.te-range {
  width: 100%;
  -webkit-appearance: none; appearance: none;
  height: 6px; border-radius: 3px;
  background: var(--surface3);
  outline: none;
}
.te-range::-webkit-slider-thumb {
  -webkit-appearance: none; appearance: none;
  width: 20px; height: 20px; border-radius: 50%;
  background: var(--accent, #3b82f6);
  cursor: pointer; border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}
.te-range::-moz-range-thumb {
  width: 20px; height: 20px; border-radius: 50%;
  background: var(--accent, #3b82f6);
  cursor: pointer; border: 2px solid #fff;
}
.te-range-ticks {
  display: flex; justify-content: space-between;
  font-family: "Nunito Sans", sans-serif;
  font-size: 10px; font-weight: 700;
  color: var(--text-dim);
  letter-spacing: 0.5px;
  padding: 2px 2px 0;
}

.te-toggles { display: flex; gap: 18px; flex-wrap: wrap; }
.te-toggle {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer;
  padding: 8px 14px;
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 13px; font-weight: 600;
  color: var(--text);
  transition: all 0.12s;
}
.te-toggle:hover { background: var(--surface3); }
.te-toggle input { cursor: pointer; margin: 0; }
.te-toggle:has(input:checked) {
  background: var(--accent-dim, #dbeafe);
  border-color: var(--accent, #3b82f6);
  color: var(--accent, #3b82f6);
}

.te-footer {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 22px;
  border-top: 1px solid var(--border);
  background: var(--surface2);
}
.te-btn {
  padding: 9px 18px;
  border: none; border-radius: 6px;
  font-size: 13px; font-weight: 700;
  cursor: pointer;
  transition: all 0.12s;
  font-family: inherit;
}
.te-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.te-btn-primary {
  background: var(--accent, #3b82f6);
  color: #fff;
}
.te-btn-primary:hover:not(:disabled) { background: var(--accent-dark, #2563eb); }
.te-btn-ghost {
  background: transparent;
  color: var(--text-dim);
}
.te-btn-ghost:hover:not(:disabled) { background: var(--surface3); color: var(--text); }
.te-btn-danger {
  background: var(--red-dim, #fee2e2);
  color: var(--red, #dc2626);
}
.te-btn-danger:hover:not(:disabled) { background: var(--red, #dc2626); color: #fff; }


/* ══ Smart project metrics styles ══ */
.ch-days-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  margin-left: 6px;
}
.ch-days-pill.ok       { background: rgba(5,150,105,0.1);   color: #059669; }
.ch-days-pill.warning  { background: rgba(217,119,6,0.1);   color: #d97706; }
.ch-days-pill.urgent   { background: rgba(220,38,38,0.1);   color: #dc2626; }
.ch-days-pill.overdue  { background: rgba(220,38,38,0.15);  color: #dc2626; font-weight: 800; }
.ch-progress-label {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-mid);
  min-width: 70px;
  margin-right: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

</style>