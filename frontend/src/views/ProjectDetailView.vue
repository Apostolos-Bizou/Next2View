<template>
  <div class="content">
    <div v-if="loading" class="loading">{{ tt('pd.loading') }}</div>
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
            <div style="display:flex;align-items:center;gap:8px;">
              <span :class="`ch-badge ${project.category}`">{{ catLabel(project.category) }}</span>
              <button v-if="permStore.isCEO()" class="delete-project-btn" @click="confirmDeleteProject" :title="tt('pd.deleteProject')">🗑 {{ tt('pd.delete') }}</button>
              <button v-if="permStore.isCEO() || permStore.can('editProject')" class="edit-project-btn" @click="openEditModal" :title="tt('pd.edit')">✏️ {{ tt('pd.editBtn') }}</button>
            </div>
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
          <div class="ch-progress-label">{{ tt('pd.tasks') }}</div>
          <div class="ch-progress-bar">
            <div class="ch-progress-fill" :style="`width:${project.completion}%;background:var(--${project.category});`"></div>
          </div>
          <span class="ch-progress-pct" :style="`color:var(--${project.category});`">{{ project.completion }}%</span>
        </div>
        <div v-if="timeProgress !== null" class="ch-progress-wrap" style="margin-top:8px;">
          <div class="ch-progress-label">{{ tt('pd.time') }}</div>
          <div class="ch-progress-bar">
            <div class="ch-progress-fill" :style="`width:${timeProgress}%;background:${smartStatus.color};opacity:0.8;`"></div>
          </div>
          <span class="ch-progress-pct" :style="`color:${smartStatus.color};`">{{ timeProgress }}%</span>
        </div>
      </div>

      <!-- PROJECT DESCRIPTION (permanent) -->
      <div class="project-desc-panel" style="margin-top:14px;">
        <div class="pd-desc-header">
          <div class="pd-desc-title">{{ tt('pd.descriptionTitle') }}</div>
          <div v-if="canEditDescription" class="pd-desc-actions">
            <button v-if="descriptionDirty && !descriptionSaving" class="pd-desc-btn pd-desc-btn-cancel" @click="cancelDescriptionEdit">{{ tt('pd.cancel') }}</button>
            <button v-if="canEditDescription" class="pd-desc-btn pd-desc-btn-save" :disabled="!descriptionDirty || descriptionSaving" @click="saveDescription">
              {{ descriptionSaving ? tt('pd.saving') : tt('pd.save') }}
            </button>
          </div>
        </div>
        <div v-if="descriptionError" class="pd-desc-error">{{ descriptionError }}</div>
        <RichTextEditor v-if="canEditDescription" v-model="descriptionDraft" :placeholder="tt('pd.descriptionPlaceholder')" min-height="120px" />
        <div v-else-if="project.description" class="rte-display" v-html="sanitizedDescription"></div>
        <div v-else class="pd-desc-empty">{{ tt('pd.descriptionEmpty') }}</div>
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
            <button class="delete-mod-btn" @click.stop="confirmDeleteModule(m)" :title="tt('pd.deleteModule')">🗑</button>
            <div class="mg-right">
              <div class="mg-bar-wrap"><div class="mg-bar"><div class="mg-bar-fill" :style="`width:${m.completion}%;background:var(--${m.color||project.category});`"></div></div></div>
              <span class="mg-pct" :style="`color:var(--${m.color||project.category});`">{{ m.completion }}%</span>
              <span class="mg-tasks">{{ m.tasks.filter(t=>t.isDone).length }}/{{ m.tasks.length }}</span>
            </div>
          </div>
          <div v-if="m.description" class="module-desc-display rte-display" v-html="sanitizeHtml(m.description)"></div>
          <div v-if="openAcc.has(m.id)" class="task-list">
            <div v-for="t in m.tasks" :key="t.id+'acc'" class="task-item">
              <div :class="`task-check ${t.isDone?'done':t.isBlocked?'block':''}`" @click.stop="toggleTask(t)" style="cursor:pointer;">{{ t.isDone?'✓':'' }}</div>
              <div style="flex:1;">
                <div :class="`task-name ${t.isDone?'done':''}`">{{ t.name }}</div>
                <button class="delete-task-btn" @click.stop="confirmDeleteTask(t, m)" :title="tt('pd.deleteTask')">🗑</button>
                <div v-if="t.blockNote" class="task-note">⚠ {{ t.blockNote }}</div>
              </div>
              <span class="task-assignee">{{ t.assignee||'—' }}</span>
              <div class="task-pct-wrap">
                <div class="task-bar"><div class="task-bar-fill" :style="`width:${displayProgress(t)}%;background:${t.isDone?'var(--green)':'var(--'+( m.color||project.category)+')'};`"></div></div>
                <div class="task-pct" :style="`color:${t.isDone ? 'var(--green)' : 'var(--' + (m.color || project.category) + ')'}`">{{ displayProgress(t) }}%<span v-if="mismatchAlert(t)" :title="tt('pd.behindSchedule')" style="color:var(--red);margin-left:4px;">⚠</span></div>
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
        <div class="cd-title">{{ tt('pd.contractDesc') }}</div>
        <div class="cd-text rte-display" v-html="sanitizedContractDesc"></div>
      </div>

      <!-- CONTRACT FILES -->
      <div class="files-panel" style="margin-top:14px;">
        <div class="files-header">
          <div class="files-title">{{ tt('pd.filesTitle') }}</div>
          <label v-if="permStore.isCEO() || permStore.can('uploadFiles')" class="files-upload-btn" :class="{uploading: uploading}">
            <input type="file" @change="uploadFile" accept=".pdf,.doc,.docx,.xlsx,.png,.jpg" style="display:none" :disabled="uploading" />
            {{ uploading ? tt('pd.uploading') : tt('pd.upload') }}
          </label>
        </div>
        <div v-if="files.length" class="files-list">
          <div v-for="f in files" :key="f.id" class="file-item" @click="openFile(f)" style="cursor:pointer;" :title="tt('pd.clickToOpen')">
            <span class="file-icon">{{ fileIcon(f.contentType) }}</span>
            <div class="file-info">
              <div class="file-name">{{ f.fileName }}</div>
              <div class="file-meta">{{ formatSize(f.fileSizeBytes) }} · {{ f.uploadedBy }} · {{ formatInstant(f.uploadedAt) }}</div>
            </div>
            <button class="file-del" @click.stop="deleteFile(f.id)" :title="tt('pd.delete')">✕</button>
          </div>
        </div>
        <div v-else-if="!uploading" class="files-empty">{{ tt('pd.noFiles') }}</div>
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
          <button class="notes-add-btn" @click="showNoteInput=!showNoteInput">{{ tt('pd.newNote') }}</button>
        </div>
        <div v-if="showNoteInput" class="note-input-wrap">
          <RichTextEditor v-model="newNote" :placeholder="tt('pd.notePlaceholder')" min-height="90px" />
          <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:8px;">
            <button class="note-cancel" @click="cancelNote">{{ tt('pd.cancel') }}</button>
            <button class="note-save" @click="saveNote" :disabled="!newNote.trim()">{{ tt('pd.save') }}</button>
          </div>
        </div>
        <div v-if="notes.length" class="notes-list">
          <div v-for="n in notes" :key="n.id" class="note-item">
            <div class="note-content rte-display" v-html="sanitizeHtml(n.content)"></div>
            <div class="note-meta">
              <span>{{ n.createdBy }}</span>
              <span>{{ formatInstant(n.createdAt) }}</span>
              <button class="note-del" @click="deleteNote(n.id)">✕</button>
            </div>
          </div>
        </div>
        <div v-else-if="!showNoteInput" class="notes-empty">{{ tt('pd.noNotes') }}</div>
      </div>

    </div>
    <div v-else class="loading">Project not found.</div>
  </div>
  
    <!-- TASK EDIT MODAL (Gantt v3) -->
    <div v-if="editingTask" class="modal-overlay" @click.self="closeTaskEdit">
      <div class="modal task-edit-modal">
        <div class="modal-header">
          <div class="modal-title">{{ tt('pd.editTask') }}</div>
          <button class="modal-close" @click="closeTaskEdit" :disabled="editingTaskSaving">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="editingTaskError" class="te-error">{{ editingTaskError }}</div>

          <div class="te-field">
            <label>{{ tt('pd.name') }}</label>
            <input type="text" v-model="editingTask.name" :disabled="editingTaskSaving" />
          </div>

          <div class="te-field">
            <label>{{ tt('pd.taskDescription') }}</label>
            <RichTextEditor
              v-model="editingTask.description"
              :placeholder="tt('pd.taskDescriptionPlaceholder')"
              :disabled="editingTaskSaving"
              min-height="100px"
            />
          </div>

          <div class="te-row">
            <div class="te-field">
              <label>Assignee</label>
              <input type="text" v-model="editingTask.assignee" :placeholder="tt('pd.assigneePlaceholder')" :disabled="editingTaskSaving" />
            </div>
            <div class="te-field">
              <label>Module</label>
              <input type="text" :value="editingTaskModule ? editingTaskModule.name : ''" disabled />
            </div>
          </div>

          <div class="te-field">
            <label>
              {{ tt('pd.progress') }}: <strong>{{ editingTask.progress || 0 }}%</strong>
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
              <label>{{ tt('pd.start') }}</label>
              <input type="date" v-model="editingTask.startDate" :disabled="editingTaskSaving" />
            </div>
            <div class="te-field">
              <label>{{ tt('pd.end') }}</label>
              <input type="date" v-model="editingTask.endDate" :disabled="editingTaskSaving" />
            </div>
          </div>

          <div class="te-toggles">
            <label class="te-toggle">
              <input type="checkbox" v-model="editingTask.isDone" :disabled="editingTaskSaving" />
              <span>{{ tt('pd.completed') }}</span>
            </label>
            <label class="te-toggle">
              <input type="checkbox" v-model="editingTask.isBlocked" :disabled="editingTaskSaving" />
              <span>{{ tt('pd.blocked') }}</span>
            </label>
          </div>

          <div v-if="editingTask.isBlocked" class="te-field">
            <label>{{ tt('pd.blockNoteLabel') }}</label>
            <input
              type="text"
              v-model="editingTask.blockNote"
              :placeholder="tt('pd.blockNotePlaceholder')"
              :disabled="editingTaskSaving"
            />
          </div>

          <div class="te-field">
            <label>{{ tt('pd.comment') }}</label>
            <RichTextEditor
              v-model="editingTask.comment"
              :placeholder="tt('pd.commentPlaceholder')"
              :disabled="editingTaskSaving"
              min-height="90px"
            />
          </div>

          <!-- v5.4.0 TASKFILES: task file attachments (saved tasks only) -->
          <div class="te-field">
            <div class="tf-header">
              <label>{{ tt('pd.taskFilesTitle') }}</label>
              <label v-if="editingTask.id && (permStore.isCEO() || permStore.can('uploadFiles'))" class="files-upload-btn" :class="{uploading: taskFileUploading}">
                <input type="file" @change="uploadTaskFile" accept=".pdf,.doc,.docx,.xlsx,.png,.jpg" style="display:none" :disabled="taskFileUploading" />
                {{ taskFileUploading ? tt('pd.uploading') : tt('pd.upload') }}
              </label>
            </div>
            <div v-if="!editingTask.id" class="tf-hint">{{ tt('pd.taskFilesSaveFirst') }}</div>
            <div v-else>
              <div v-if="taskFiles.length" class="files-list tf-list">
                <div v-for="f in taskFiles" :key="f.id" class="file-item" @click="openTaskFile(f)" style="cursor:pointer;" :title="tt('pd.clickToOpen')">
                  <div class="file-info">
                    <div class="file-name">{{ f.fileName }}</div>
                    <div class="file-meta">{{ formatSize(f.fileSizeBytes) }} · {{ f.uploadedBy }} · {{ formatInstant(f.uploadedAt) }}</div>
                  </div>
                  <button class="file-del" @click.stop="deleteTaskFile(f.id)" :title="tt('pd.delete')">✕</button>
                </div>
              </div>
              <div v-else-if="!taskFileUploading" class="files-empty">{{ tt('pd.noFiles') }}</div>
              <div v-if="taskFileError" class="files-error">{{ taskFileError }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer te-footer">
          <button class="te-btn te-btn-danger" @click="deleteTaskFromModal" :disabled="editingTaskSaving">
            🗑 {{ tt('pd.delete') }}
          </button>
          <div style="flex:1"></div>
          <button class="te-btn te-btn-ghost" @click="closeTaskEdit" :disabled="editingTaskSaving">
            Άκυρο
          </button>
          <button class="te-btn te-btn-primary" @click="saveTaskEdit" :disabled="editingTaskSaving">
            {{ editingTaskSaving ? tt('pd.saving') : tt('pd.save') }}
          </button>
        </div>
      </div>
    </div>

    
      <!-- PROJECT HISTORY (collapsible audit trail) -->
      <div v-if="project" class="project-history-panel" style="margin-top:14px;">
        <div class="history-header" @click="historyExpanded = !historyExpanded; if (historyExpanded && !historyLoaded) loadProjectHistory()" style="cursor:pointer;user-select:none;">
          <div style="display:flex;align-items:center;gap:8px;">
            <span class="history-chevron" :class="{ open: historyExpanded }">▶</span>
            <div class="history-title">📋 {{ tt('pd.historyTitle') }}</div>
          </div>
          <span v-if="historyLoaded && projectHistory.length" class="history-count">{{ projectHistory.length }}</span>
        </div>
        <div v-if="historyExpanded" class="history-body">
          <div v-if="project && project.createdBy" class="history-created">
            {{ tt('pd.historyCreatedBy') }} <strong>{{ project.createdByName || tt('pd.historyUnknown') }}</strong> — {{ formatHistoryDate(project.createdAt) }}
          </div>
          <div v-if="historyLoading" class="history-loading">{{ tt('pd.historyLoading') }}</div>
          <div v-else-if="historyLoaded && !projectHistory.length" class="history-empty">{{ tt('pd.historyNoChanges') }}</div>
          <div v-else-if="historyLoaded" class="history-timeline">
            <div v-for="h in projectHistory" :key="h.id" class="history-item">
              <div class="history-dot"></div>
              <div class="history-content">
                <div class="history-actor">{{ h.actorName }}</div>
                <div class="history-action">{{ h.description || (h.actionType + ' ' + h.entityType.toLowerCase()) }}</div>
                <div class="history-time">{{ formatHistoryDate(h.createdAt) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- EDIT PROJECT MODAL -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal=false">
    <div class="modal modal-edit">
      <div class="modal-header">
        <div class="modal-title">{{ tt('pd.editProject') }}</div>
        <button class="modal-close" @click="showEditModal=false">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-section-title">{{ tt('pd.basicInfo') }}</div>
        <!-- scalar basic fields: responsive grid, all visible together -->
        <div class="basic-grid">
          <div class="form-group basic-grid__full">
            <label>{{ tt('pd.titleReq') }}</label>
            <input v-model="editForm.title" type="text" class="form-input" :placeholder="tt('pd.titlePlaceholder')" />
          </div>
          <div class="form-group">
            <label>{{ tt('pd.companyReq') }}</label>
            <select v-model="editForm.companyId" class="form-input">
              <option v-for="co in companies" :key="co.id" :value="co.id">{{ co.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ tt('pd.categoryReq') }}</label>
            <select v-model="editForm.category" class="form-input">
              <option value="finance">Finance</option>
              <option value="legal">Legal</option>
              <option value="dev">Developing</option>
              <option value="marketing">Marketing</option>
            </select>
          </div>
          <div class="form-group">
            <label>Budget (€)</label>
            <input v-model="editForm.budget" type="number" class="form-input" />
          </div>
          <div class="form-group">
            <label>{{ tt('pd.startDateLabel') }}</label>
            <input v-model="editForm.startDate" type="date" class="form-input" />
          </div>
          <div class="form-group">
            <label>Deadline</label>
            <input v-model="editForm.deadline" type="date" class="form-input" />
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
        </div>
        <!-- rich text fields: full width -->
        <div class="form-group">
          <label>{{ tt('pd.descriptionLabel') }}</label>
          <RichTextEditor v-model="editForm.description" :placeholder="tt('pd.descriptionPlaceholder')" min-height="100px" />
        </div>
        <div class="form-group">
          <label>{{ tt('pd.contractDescLabel') }}</label>
          <RichTextEditor v-model="editForm.contractDesc" :placeholder="tt('pd.briefDesc')" min-height="100px" />
        </div>
        <div v-if="editError" class="form-error">{{ editError }}</div>

        <!-- MODULES SECTION (same single scroll body) -->
        <div class="form-section-title form-section-title--modules" style="display:flex;justify-content:space-between;align-items:center;">
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
          <div class="module-desc-field">
            <label class="module-desc-label">{{ tt('pd.moduleDescLabel') }}</label>
            <RichTextEditor v-model="m.description" :placeholder="tt('pd.moduleDescPlaceholder')" min-height="70px" />
          </div>
          <div v-for="(t, ti) in m.tasks" :key="ti" style="display:flex;gap:6px;align-items:center;margin-bottom:6px;padding-left:12px;">
            <div style="display:flex;flex-direction:column;gap:4px;flex:1;">
              <div style="display:flex;gap:6px;align-items:center;">
                <input v-model="t.name" type="text" class="form-input" style="flex:2;" placeholder="Task name" />
                <input v-model="t.assignee" type="text" class="form-input" style="flex:1;" placeholder="Assignee" />
                <input v-model.number="t.progress" type="number" class="form-input" style="width:65px;" min="0" max="100" placeholder="%" />
                <button @click="m.tasks.splice(ti,1)" style="background:var(--red-dim);color:var(--red);border:none;border-radius:5px;padding:4px 6px;cursor:pointer;">✕</button>
              </div>
              <div style="display:flex;gap:6px;align-items:center;padding-left:4px;">
                <label style="font-size:10px;color:var(--text-dim);min-width:55px;">{{ tt('pd.start') }}</label>
                <input v-model="t.startDate" type="date" class="form-input" style="flex:1;font-size:11px;padding:4px 6px;" />
                <label style="font-size:10px;color:var(--text-dim);min-width:35px;">{{ tt('pd.end') }}</label>
                <input v-model="t.endDate" type="date" class="form-input" style="flex:1;font-size:11px;padding:4px 6px;" />
              </div>
              <!-- per-task files: saved tasks get list/upload/open/delete (lazy-loaded on expand); new tasks get a hint -->
              <div class="etf-wrap">
                <div v-if="!t.id" class="etf-hint">📎 {{ tt('pd.taskFilesSaveFirst') }}</div>
                <div v-else>
                  <button type="button" class="etf-toggle" @click="toggleTaskFilePanel(t.id)">
                    📎 {{ tt('pd.taskFilesTitle') }}
                    <span class="etf-caret">{{ openTaskFilePanels[t.id] ? '▲' : '▼' }}</span>
                  </button>
                  <div v-if="openTaskFilePanels[t.id]" class="etf-panel">
                    <label v-if="permStore.isCEO() || permStore.can('uploadFiles')" class="files-upload-btn etf-upload" :class="{uploading: editTaskUploading[t.id]}">
                      <input type="file" @change="uploadEditTaskFile($event, t.id)" accept=".pdf,.doc,.docx,.xlsx,.png,.jpg" style="display:none" :disabled="editTaskUploading[t.id]" />
                      {{ editTaskUploading[t.id] ? tt('pd.uploading') : tt('pd.upload') }}
                    </label>
                    <div v-if="(editTaskFiles[t.id] || []).length" class="files-list etf-list">
                      <div v-for="f in editTaskFiles[t.id]" :key="f.id" class="file-item" @click="openEditTaskFile(t.id, f)" style="cursor:pointer;" :title="tt('pd.clickToOpen')">
                        <div class="file-info">
                          <div class="file-name">{{ f.fileName }}</div>
                          <div class="file-meta">{{ formatSize(f.fileSizeBytes) }} · {{ f.uploadedBy }} · {{ formatInstant(f.uploadedAt) }}</div>
                        </div>
                        <button class="file-del" @click.stop="deleteEditTaskFile(t.id, f.id)" :title="tt('pd.delete')">✕</button>
                      </div>
                    </div>
                    <div v-else-if="!editTaskUploading[t.id] && !editTaskFileErr[t.id]" class="files-empty etf-empty">{{ tt('pd.noFiles') }}</div>
                    <div v-if="editTaskFileErr[t.id]" class="files-error">{{ editTaskFileErr[t.id] }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button @click="m.tasks.push({name:'',assignee:'',progress:0,isDone:false,isBlocked:false,blockNote:'',comment:'',description:'',deadline:null,startWeek:mi+1,durationWeeks:1,sortOrder:m.tasks.length})"
            style="font-size:11px;padding:3px 10px;background:var(--surface3);color:var(--text-mid);border:1px solid var(--border-bright);border-radius:5px;cursor:pointer;margin-left:12px;">
            + Task
          </button>
        </div>
        <div v-if="editForm.modules.length===0" style="text-align:center;color:var(--text-dim);font-size:13px;padding:12px;">
          {{ tt('pd.noModules') }}
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="showEditModal=false">{{ tt('pd.cancel') }}</button>
        <button class="btn-submit" @click="saveEdit" :disabled="editSaving">
          {{ editSaving ? tt('pd.saving') : tt('pd.save') }}
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

// Sanitize contract description HTML before rendering with v-html (XSS protection)
const sanitizedContractDesc = computed(() => {
  if (!project.value || !project.value.contractDesc) return ''
  return DOMPurify.sanitize(project.value.contractDesc, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'h1', 'h2', 'ul', 'ol', 'li', 'a', 'span'],
    ALLOWED_ATTR: ['href', 'target', 'rel', 'style'],
    ALLOWED_URI_REGEXP: /^(?:(?:https?|mailto):)/i,
  })
})

// Reusable HTML sanitizer for note/comment content (same whitelist as contractDesc)
function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'h1', 'h2', 'ul', 'ol', 'li', 'a', 'span'],
    ALLOWED_ATTR: ['href', 'target', 'rel', 'style'],
    ALLOWED_URI_REGEXP: /^(?:(?:https?|mailto):)/i,
  })
}
const smartStatus = computed(() => {
  if (!project.value) return { code: 'on_track', label: 'On Track', color: '#059669', icon: '🟢' }
  return getSmartStatus(timeProgress.value, project.value.completion, project.value.status)
})
const timelineTooltip = computed(() => {
  if (!project.value) return ''
  const tp = timeProgress.value
  if (tp === null) return tt('pd.timelineRange')
  return tt('pd.timeElapsed', {pct: tp})
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
      description: project.value.description || '',
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
          comment: t.comment, description: t.description || '', deadline: t.deadline,
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
    editingTaskError.value = tt('pd.err.saveFailed')
    // Revert by reloading
    await loadProject()
  } finally {
    editingTaskSaving.value = false
  }
}

async function deleteTaskFromModal() {
  if (!editingTask.value || !editingTaskModule.value) return
  if (!confirm(tt('pd.confirmDeleteTask', {name: editingTask.value.name}))) return
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
      description: project.value.description || '',
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
          comment: t.comment, description: t.description || '', deadline: t.deadline,
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
    editingTaskError.value = tt('pd.err.deleteFailed')
    await loadProject()
  } finally {
    editingTaskSaving.value = false
  }
}



import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import api from '@/services/api'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'
import { usePermissionStore } from '@/stores/permissions'
import RichTextEditor from '@/components/RichTextEditor.vue'
import DOMPurify from 'dompurify'

const { t: tt } = useI18n()
const route = useRoute()
const router = useRouter()
const store = useProjectStore()
const permStore = usePermissionStore()

const project = ref(null)
const loading = ref(true)
const projectHistory = ref([])
const historyLoaded = ref(false)
const historyExpanded = ref(false)
const historyLoading = ref(false)

async function loadProjectHistory() {
  if (!project.value) return
  historyLoading.value = true
  try {
    const res = await api.get('/activity-log/entity/PROJECT/' + project.value.id)
    projectHistory.value = res.data
    historyLoaded.value = true
  } catch (e) {
    console.error('Failed to load project history:', e)
    historyLoaded.value = true
  } finally {
    historyLoading.value = false
  }
}

function formatHistoryDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleDateString('el-GR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}
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
    fileError.value = isMfaError(e) ? '\u{1F512} MFA verification required to download legal files. Enable MFA in Profile.' : tt('pd.err.openFailed')
  }
}

async function loadFiles() {
  if (!project.value) return
  try {
    const res = await api.get(`/projects/${project.value.id}/files`)
    files.value = res.data
  } catch (e) { if (isMfaError(e)) { fileError.value = '\u{1F512} MFA verification required to access legal files. Enable MFA in your Profile \u2192 Security settings.'; } else { files.value = [] } }
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
    uploadError.value = isMfaError(e) ? '\u{1F512} MFA verification required to upload legal files. Enable MFA in Profile.' : (e.response?.data?.message || tt('pd.err.uploadFailed'))
  } finally {
    uploading.value = false
    event.target.value = ""
  }
}

async function deleteFile(fileId) {
  if (!confirm(tt('pd.confirmDeleteFile'))) return
  try {
    await api.delete(`/projects/${project.value.id}/files/${fileId}`)
    await loadFiles()
  } catch (e) { if (isMfaError(e)) { fileError.value = '\u{1F512} MFA verification required to delete legal files.'; } }
}


// MFA error detection helper
function isMfaError(e) {
  if (e?.response?.status !== 403) return false
  const msg = e?.response?.data?.message || e?.response?.data?.error || ''
  return msg.toLowerCase().includes('mfa') || msg === ''
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
      description: project.value.description || '',
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
      description: project.value.description || '',
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
          comment: t.comment, description: t.description || '', deadline: t.deadline,
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
  if (!confirm(tt('pd.confirmDeleteProject', {name: project.value.title}))) return
  try {
    await api.delete('/projects/' + project.value.id)
    router.push('/projects')
  } catch(e) { alert(tt('pd.err.deleteProjectFailed')) }
}

async function confirmDeleteModule(m) {
  if (!confirm(tt('pd.confirmDeleteModule', {name: m.name}))) return
  const idx = project.value.modules.findIndex(x => x.id === m.id)
  if (idx === -1) return
  project.value.modules.splice(idx, 1)
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title, companyId: project.value.companyId,
      category: project.value.category, budget: project.value.budget || 0,
      startDate: project.value.startDate || null, deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      description: project.value.description || '', description: project.value.description || '', status: project.value.status,
      specs: (project.value.specs || []).map(s => ({ description: s.description, isDone: s.isDone, sortOrder: s.sortOrder || 0, startDate: s.startDate || null, endDate: s.endDate || null })),
      modules: (project.value.modules || []).map(m2 => ({
        name: m2.name, color: m2.color, sortOrder: m2.sortOrder || 0,
        tasks: (m2.tasks || []).map(t => ({ name: t.name, assignee: t.assignee, progress: t.progress, isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote, comment: t.comment, deadline: t.deadline, startWeek: t.startWeek, durationWeeks: t.durationWeeks, startDay: t.startDay, durationDays: t.durationDays, sortOrder: t.sortOrder || 0, startDate: t.startDate || null, endDate: t.endDate || null }))
      }))
    })
  } catch(e) { await loadProject() }
}

async function confirmDeleteTask(t, m) {
  if (!confirm(tt('pd.confirmDeleteTask', {name: t.name}))) return
  const tidx = m.tasks.findIndex(x => x.id === t.id)
  if (tidx === -1) return
  m.tasks.splice(tidx, 1)
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title, companyId: project.value.companyId,
      category: project.value.category, budget: project.value.budget || 0,
      startDate: project.value.startDate || null, deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      description: project.value.description || '', description: project.value.description || '', status: project.value.status,
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
      description: project.value.description || '',
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
          comment: t.comment, description: t.description || '', deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks,
          startDay: t.startDay, durationDays: t.durationDays,
          sortOrder: t.sortOrder || 0, manualProgress: t.manualProgress || false,
          startDate: t.startDate || null, endDate: t.endDate || null
        }))
      }))
    })
    showSpecModal.value = false
  } catch(e) {
    specSaveError.value = tt('pd.err.saveFailed')
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

// ── Inline project description (permanent) ──
const descriptionDraft = ref('')
const descriptionSaving = ref(false)
const descriptionError = ref('')
const descriptionDirty = computed(() => {
  // Return false during initial load (project.value not yet populated)
  if (!project.value) return false
  const current = project.value.description || ''
  return (descriptionDraft.value || '') !== current
})
const canEditDescription = computed(() => permStore.isCEO() || permStore.can('editProject'))
const sanitizedDescription = computed(() => DOMPurify.sanitize(project.value?.description || ''))

function cancelDescriptionEdit() {
  descriptionDraft.value = (project.value && project.value.description) || ''
  descriptionError.value = ''
}

// Auto-sync descriptionDraft when project.value.description changes (initial load, SSE updates, etc.)
// Only syncs if the draft is empty or already matches the previous server value (user has no pending changes).
let lastSyncedDescription = null
watch(() => project.value && project.value.description, (newDesc) => {
  const incoming = newDesc || ''
  // First load OR external update where user has no pending edit
  if (lastSyncedDescription === null || descriptionDraft.value === lastSyncedDescription) {
    descriptionDraft.value = incoming
  }
  lastSyncedDescription = incoming
}, { immediate: true })

async function saveDescription() {
  if (!canEditDescription.value) return
  descriptionSaving.value = true
  descriptionError.value = ''
  try {
    await api.put('/projects/' + project.value.id, {
      title: project.value.title,
      companyId: project.value.companyId,
      category: project.value.category,
      budget: project.value.budget || 0,
      startDate: project.value.startDate || null,
      deadline: project.value.deadline || null,
      contractDesc: project.value.contractDesc || '',
      description: project.value.description || '',
      description: descriptionDraft.value || '',
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
          comment: t.comment, description: t.description || '', deadline: t.deadline,
          startWeek: t.startWeek, durationWeeks: t.durationWeeks,
          startDay: t.startDay, durationDays: t.durationDays,
          sortOrder: t.sortOrder || 0, manualProgress: t.manualProgress || false,
          startDate: t.startDate || null, endDate: t.endDate || null
        }))
      }))
    })
    await loadProject()
    descriptionDraft.value = (project.value && project.value.description) || ''
  } catch (e) {
    descriptionError.value = e.response?.data?.message || tt('pd.err.saveFailed')
  } finally {
    descriptionSaving.value = false
  }
}

async function openEditModal() {
  editError.value = ""
  resetEditTaskFileState()
  editForm.value = {
    title: project.value.title || "",
    companyId: project.value.companyId || "",
    category: project.value.category || "dev",
    budget: project.value.budget || 0,
    startDate: project.value.startDate || "",
    deadline: project.value.deadline || "",
    contractDesc: project.value.contractDesc || "",
    description: project.value.description || "",
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
  // Scroll modal body to top after render (fixes UX bug where users see Modules first)
  nextTick(() => {
    const body = document.querySelector('.modal-edit .modal-body')
    if (body) body.scrollTop = 0
  })
}

async function saveEdit() {
  if (!editForm.value.title.trim()) { editError.value = tt('pd.err.titleRequired'); return }
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
      description: editForm.value.description || "",
      status: editForm.value.status,
      modules: editForm.value.modules.map(m => ({
        name: m.name, color: m.color, sortOrder: m.sortOrder || 0,
        description: m.description || '',
        tasks: (m.tasks || []).map(t => ({
          name: t.name, assignee: t.assignee, progress: t.progress,
          isDone: t.isDone, isBlocked: t.isBlocked, blockNote: t.blockNote,
          comment: t.comment, description: t.description || '', deadline: t.deadline,
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
    editError.value = e.response?.data?.message || tt('pd.err.saveFailed')
  } finally { editSaving.value = false }
}

async function loadProject() {
  project.value = await store.fetchProject(route.params.id)
  descriptionDraft.value = (project.value && project.value.description) || ''
}

function editAddModule() {
  editForm.value.modules.push({
    name: '', color: editForm.value.category || 'dev', sortOrder: editForm.value.modules.length,
    description: '', tasks: []
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
  const m = Array.from({length:12}, (_, mi) => tt('months.' + mi))
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
  const months = Array.from({length:12}, (_, mi) => tt('months.' + mi))
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
  const m = Array.from({length:12}, (_, mi) => tt('months.' + mi))
  return `${d.getDate()} ${m[d.getMonth()]} ${d.getFullYear()}`
}

// v5.4.0 TASKFILES: task file attachments (reuses same encrypted backend, scoped per task)
const taskFiles = ref([])
const taskFileUploading = ref(false)
const taskFileError = ref('')

async function loadTaskFiles() {
  const id = editingTask.value && editingTask.value.id
  if (!id) { taskFiles.value = []; return }
  taskFileError.value = ''
  try {
    const res = await api.get(`/tasks/${id}/files`)
    taskFiles.value = res.data
  } catch (e) {
    if (isMfaError(e)) { taskFileError.value = '\u{1F512} ' + tt('pd.mfaFiles') }
    else { taskFiles.value = [] }
  }
}

async function uploadTaskFile(event) {
  const file = event.target.files[0]
  const id = editingTask.value && editingTask.value.id
  if (!file || !id) return
  taskFileUploading.value = true
  taskFileError.value = ''
  try {
    const fd = new FormData()
    fd.append('file', file)
    await api.post(`/tasks/${id}/files`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    await loadTaskFiles()
  } catch (e) {
    taskFileError.value = isMfaError(e) ? '\u{1F512} ' + tt('pd.mfaFiles') : (e.response?.data?.message || tt('pd.err.uploadFailed'))
  } finally {
    taskFileUploading.value = false
    if (event && event.target) event.target.value = ''
  }
}

async function openTaskFile(f) {
  const id = editingTask.value && editingTask.value.id
  if (!id) return
  try {
    const res = await api.get(`/tasks/${id}/files/${f.id}/content`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = f.fileName
    document.body.appendChild(a); a.click(); a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    taskFileError.value = isMfaError(e) ? '\u{1F512} ' + tt('pd.mfaFiles') : tt('pd.err.openFailed')
  }
}

async function deleteTaskFile(fileId) {
  if (!confirm(tt('pd.confirmDeleteFile'))) return
  const id = editingTask.value && editingTask.value.id
  if (!id) return
  try {
    await api.delete(`/tasks/${id}/files/${fileId}`)
    await loadTaskFiles()
  } catch (e) {
    if (isMfaError(e)) { taskFileError.value = '\u{1F512} ' + tt('pd.mfaFiles') }
  }
}

// Load task files whenever the edit modal opens on a saved task
watch(editingTask, (t) => {
  taskFileError.value = ''
  if (t && t.id) { loadTaskFiles() } else { taskFiles.value = [] }
})

// ════ PER-TASK FILES inside the full-screen EDIT view ════
// Separate, edit-view-scoped state keyed by task id. Reuses the existing
// /tasks/{id}/files endpoints WITHOUT touching the single-task editor state
// or its MFA gating. Files are lazy-loaded on demand (per-task expand), never
// all-at-once on mount.
const editTaskFiles = reactive({})       // taskId -> files[]
const editTaskUploading = reactive({})   // taskId -> bool
const editTaskFileErr = reactive({})     // taskId -> string
const openTaskFilePanels = reactive({})  // taskId -> bool (panel expanded)

function resetEditTaskFileState() {
  ;[editTaskFiles, editTaskUploading, editTaskFileErr, openTaskFilePanels]
    .forEach(map => Object.keys(map).forEach(k => delete map[k]))
}

function toggleTaskFilePanel(taskId) {
  if (!taskId) return
  const open = !openTaskFilePanels[taskId]
  openTaskFilePanels[taskId] = open
  // lazy: fetch only the first time this task's panel is opened
  if (open && editTaskFiles[taskId] === undefined) loadEditTaskFiles(taskId)
}

async function loadEditTaskFiles(taskId) {
  if (!taskId) return
  editTaskFileErr[taskId] = ''
  try {
    const res = await api.get(`/tasks/${taskId}/files`)
    editTaskFiles[taskId] = res.data
  } catch (e) {
    if (isMfaError(e)) { editTaskFileErr[taskId] = '\u{1F512} ' + tt('pd.mfaFiles') }
    else { editTaskFiles[taskId] = [] }
  }
}

async function uploadEditTaskFile(event, taskId) {
  const file = event.target.files[0]
  if (!file || !taskId) return
  editTaskUploading[taskId] = true
  editTaskFileErr[taskId] = ''
  try {
    const fd = new FormData()
    fd.append('file', file)
    await api.post(`/tasks/${taskId}/files`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    await loadEditTaskFiles(taskId)
  } catch (e) {
    editTaskFileErr[taskId] = isMfaError(e) ? '\u{1F512} ' + tt('pd.mfaFiles') : (e.response?.data?.message || tt('pd.err.uploadFailed'))
  } finally {
    editTaskUploading[taskId] = false
    if (event && event.target) event.target.value = ''
  }
}

async function openEditTaskFile(taskId, f) {
  if (!taskId) return
  try {
    const res = await api.get(`/tasks/${taskId}/files/${f.id}/content`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = f.fileName
    document.body.appendChild(a); a.click(); a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    editTaskFileErr[taskId] = isMfaError(e) ? '\u{1F512} ' + tt('pd.mfaFiles') : tt('pd.err.openFailed')
  }
}

async function deleteEditTaskFile(taskId, fileId) {
  if (!confirm(tt('pd.confirmDeleteFile'))) return
  if (!taskId) return
  try {
    await api.delete(`/tasks/${taskId}/files/${fileId}`)
    await loadEditTaskFiles(taskId)
  } catch (e) {
    if (isMfaError(e)) { editTaskFileErr[taskId] = '\u{1F512} ' + tt('pd.mfaFiles') }
  }
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
.ch-top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 16px; gap: 8px; }
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
.delete-project-btn { font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; background: #dc2626; border: 1px solid #b91c1c; border-radius: 6px; color: #fff; cursor: pointer; transition: background 0.2s; }
.delete-project-btn:hover { background: #b91c1c; }
.delete-mod-btn { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 13px; padding: 2px 6px; border-radius: 4px; opacity: 0.5; transition: opacity 0.2s; margin-left: auto; }
.delete-mod-btn:hover { opacity: 1; color: var(--red); }
.delete-task-btn { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 12px; padding: 2px 4px; border-radius: 4px; opacity: 0; transition: opacity 0.2s; margin-left: 4px; }
.task-row:hover .delete-task-btn { opacity: 0.6; }
.delete-task-btn:hover { opacity: 1 !important; color: var(--red); }
.edit-project-btn { font-family: "Nunito", sans-serif; font-size: 11px; font-weight: 700; padding: 6px 14px; background: var(--surface2); border: 1px solid var(--border-bright); border-radius: 6px; color: var(--text); cursor: pointer; transition: all 0.2s; }
.edit-project-btn:hover { background: var(--accent-dim); border-color: var(--accent); color: var(--accent); }
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
/* ---- FULL-SCREEN edit project modal (single scroll body, sticky header/footer via flex) ---- */
.modal-edit { width: 100vw; height: 100vh; max-width: 100vw; max-height: 100vh; border-radius: 0; }
.modal-edit .modal-header, .modal-edit .modal-footer { flex-shrink: 0; }
.modal-edit .modal-body { padding: 22px 32px; }
/* larger, bolder section headings with a thin divider underneath */
.modal-edit .form-section-title { font-size: 15px; font-weight: 800; letter-spacing: 0.3px; text-transform: none; color: var(--text); margin: 2px 0 16px; padding-bottom: 8px; border-bottom: 1px solid var(--border-bright); }
.modal-edit .form-section-title--modules { margin-top: 30px; }
/* basic fields: responsive grid — all visible together at the top */
.basic-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 4px 16px; align-items: start; }
.basic-grid .form-group { margin-bottom: 10px; }
.basic-grid__full { grid-column: 1 / -1; }
/* per-module description (edit form) */
.module-desc-field { margin: 0 0 10px; padding-left: 4px; }
.module-desc-label { display: block; font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: var(--text-dim); margin-bottom: 5px; }
/* per-module description (read view) */
.module-desc-display { font-size: 13px; color: var(--text-mid); line-height: 1.5; padding: 6px 12px 10px 26px; }
/* per-task files (edit view) */
.etf-wrap { padding-left: 4px; margin-top: 2px; }
.etf-hint { font-family: 'Nunito Sans', sans-serif; font-size: 10px; color: var(--text-dim); font-style: italic; }
.etf-toggle { display: inline-flex; align-items: center; gap: 6px; background: var(--surface3); border: 1px solid var(--border-bright); border-radius: 5px; color: var(--text-mid); font-family: 'Nunito Sans', sans-serif; font-size: 10px; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase; padding: 3px 10px; cursor: pointer; }
.etf-toggle:hover { border-color: var(--accent); color: var(--text); }
.etf-caret { font-size: 8px; color: var(--text-dim); }
.etf-panel { margin: 6px 0 2px; padding: 8px 10px; background: var(--surface); border: 1px solid var(--border); border-radius: 6px; }
.etf-upload { font-size: 10px; padding: 4px 10px; }
.etf-list { padding: 4px 0 0; }
.etf-list .file-item { padding: 8px; }
.etf-list .file-name { font-size: 12px; }
.etf-empty { padding: 10px; font-size: 11px; }
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
  /* full-screen edit modal stays edge-to-edge on mobile */
  .modal-edit { width: 100vw !important; max-width: 100vw !important; height: 100vh !important; max-height: 100vh !important; }
  .modal-edit .modal-body { padding: 14px !important; }
  .basic-grid { grid-template-columns: 1fr !important; }
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
/* v5.4.0 TASKFILES */
.tf-header { display: flex; align-items: center; justify-content: space-between; }
.tf-hint { font-size: 11px; color: var(--text-dim); font-style: italic; padding: 4px 0; }
.tf-list { padding: 0; }
.file-del { background: none; border: none; color: var(--text-dim); cursor: pointer; font-size: 13px; padding: 2px 6px; border-radius: 4px; }
.file-del:hover { color: var(--red); background: var(--red-dim); }

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


.history-chevron { font-size: 9px; color: var(--text-dim); transition: transform 0.2s ease; display: inline-block; }
.history-chevron.open { transform: rotate(90deg); }
.history-count { font-size: 10px; font-weight: 700; color: var(--accent); background: var(--accent-dim); padding: 2px 8px; border-radius: 10px; }
.history-body { animation: slideDown 0.2s ease; }

.project-history-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 18px 22px; }
.history-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.history-title { font-size: 11px; font-weight: 700; color: var(--text-dim); text-transform: uppercase; letter-spacing: 1.5px; }
.history-created { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 10px 14px; font-size: 13px; color: var(--text); margin-bottom: 14px; }
.history-created strong { color: var(--accent); }
.history-loading, .history-empty { font-size: 12px; color: var(--text-dim); text-align: center; padding: 20px; }
.history-timeline { display: flex; flex-direction: column; gap: 0; border-left: 2px solid var(--border-bright); margin-left: 8px; padding-left: 18px; }
.history-item { position: relative; padding: 8px 0; }
.history-dot { position: absolute; left: -24px; top: 12px; width: 8px; height: 8px; border-radius: 50%; background: var(--accent); border: 2px solid var(--surface); }
.history-actor { font-size: 12px; font-weight: 700; color: var(--text); }
.history-action { font-size: 11px; color: var(--text-mid); margin-top: 2px; }
.history-time { font-size: 10px; color: var(--text-dim); font-family: "Nunito Sans", sans-serif; margin-top: 3px; }
/* === Rendered rich text content (read-only display) === */
.rte-display {
  font-size: 12px;
  line-height: 1.6;
  color: var(--text);
}
.rte-display :deep(p) { margin: 0 0 6px 0; }
.rte-display :deep(p:last-child) { margin-bottom: 0; }
.rte-display :deep(h1) { font-size: 16px; font-weight: 700; margin: 6px 0 4px 0; line-height: 1.3; }
.rte-display :deep(h2) { font-size: 14px; font-weight: 700; margin: 6px 0 3px 0; line-height: 1.3; }
.rte-display :deep(ul), .rte-display :deep(ol) { padding-left: 22px; margin: 4px 0 6px 0; }
.rte-display :deep(ul li), .rte-display :deep(ol li) { margin: 2px 0; }
.rte-display :deep(ul) { list-style-type: disc; }
.rte-display :deep(ol) { list-style-type: decimal; }
.rte-display :deep(strong) { font-weight: 700; }
.rte-display :deep(em) { font-style: italic; }
.rte-display :deep(u) { text-decoration: underline; }
.rte-display :deep(a) { color: var(--accent); text-decoration: underline; }
.rte-display :deep(a:hover) { color: #1d4ed8; }

</style>