<template>
  <div class="reports-hub">
    <!-- Header -->
    <div class="rh-header">
      <div class="rh-title-row">
        <div class="rh-icon">📊</div>
        <div>
          <h1 class="rh-title">Reports Hub</h1>
          <p class="rh-subtitle">One-click reports · Professional PDF exports · Real-time data</p>
        </div>
      </div>
    </div>

    <!-- Template Gallery -->
    <div class="rh-section-label">Quick Templates</div>
    <div class="rh-gallery">
      <div
        v-for="tmpl in templates"
        :key="tmpl.id"
        class="rh-card"
        :class="{ active: selectedTemplate?.id === tmpl.id }"
        @click="selectTemplate(tmpl)"
      >
        <div class="rh-card-icon" :class="'icon-' + tmpl.category">
          {{ getIcon(tmpl.icon) }}
        </div>
        <div class="rh-card-body">
          <div class="rh-card-name">{{ tmpl.name }}</div>
          <div class="rh-card-desc">{{ tmpl.description }}</div>
          <div class="rh-card-tags">
            <span v-for="ds in tmpl.dataSources" :key="ds" class="rh-tag">{{ ds }}</span>
          </div>
        </div>
        <div v-if="tmpl.aiEnhanced" class="rh-ai-badge">✦ AI</div>
      </div>
    </div>

    <!-- Preview Pane -->
    <div v-if="selectedTemplate" class="rh-preview">
      <div class="rh-preview-header">
        <div class="rh-preview-title">
          {{ getIcon(selectedTemplate.icon) }} {{ selectedTemplate.name }}
        </div>
        <div class="rh-preview-actions">
          <button class="rh-btn rh-btn-preview" @click="loadPreview" :disabled="loadingPreview">
            {{ loadingPreview ? 'Loading...' : '👁 Preview Data' }}
          </button>
          <button class="rh-btn rh-btn-download" @click="downloadPdf" :disabled="downloading">
            {{ downloading ? 'Generating...' : '📄 Download PDF' }}
          </button>
        </div>
      </div>

      <!-- Preview Content -->
      <div v-if="previewData" class="rh-preview-content">
        <div class="rh-preview-meta">
          Generated: {{ formatDate(previewData.generatedAt) }}
        </div>

        <!-- Summary Cards -->
        <div v-if="previewData.summary" class="rh-summary-grid">
          <div v-for="(value, key) in previewData.summary" :key="key" class="rh-summary-card">
            <div class="rh-summary-label">{{ formatKey(key) }}</div>
            <div class="rh-summary-value" :class="getSummaryClass(key, value)">
              {{ formatValue(key, value) }}
            </div>
          </div>
        </div>

        <!-- Sections -->
        <div v-for="(section, idx) in previewData.sections" :key="idx" class="rh-section">
          <div class="rh-section-head">
            <span class="rh-section-title">{{ section.title }}</span>
            <span v-if="section.status" class="rh-status" :class="section.status?.toLowerCase()">
              {{ section.status === 'COMPLIANT' ? '✓ Compliant' : '⚠ Action Required' }}
            </span>
          </div>

          <!-- Checklist items -->
          <div v-if="section.items && section.items[0]?.check" class="rh-checklist">
            <div v-for="(item, i) in section.items" :key="i" class="rh-check-row">
              <span class="rh-check-icon">{{ item.status ? '✅' : '❌' }}</span>
              <span class="rh-check-text">{{ item.check || item.action }}</span>
              <span v-if="item.priority" class="rh-priority" :class="item.priority?.toLowerCase()">
                {{ item.priority }}
              </span>
            </div>
          </div>

          <!-- User table -->
          <div v-if="section.users" class="rh-table-wrap">
            <table class="rh-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>Company</th>
                  <th>MFA</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(u, i) in section.users" :key="i">
                  <td class="rh-td-name">{{ u.fullName || u.username }}</td>
                  <td><span class="rh-role-badge" :class="u.role?.toLowerCase()">{{ u.role }}</span></td>
                  <td>{{ u.company }}</td>
                  <td>
                    <span :class="u.mfaEnabled ? 'rh-mfa-on' : 'rh-mfa-off'">
                      {{ u.mfaEnabled ? '✓ Active' : '✗ Inactive' }}
                    </span>
                  </td>
                  <td>
                    <span v-if="u.actionRequired" class="rh-action-needed">Needs activation</span>
                    <span v-else class="rh-action-ok">—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Action items list -->
          <div v-if="section.items && section.items[0]?.action" class="rh-action-list">
            <div v-for="(item, i) in section.items" :key="i" class="rh-action-row">
              <span class="rh-priority" :class="item.priority?.toLowerCase()">{{ item.priority }}</span>
              <span class="rh-action-user">{{ item.user }}:</span>
              <span>{{ item.action }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else-if="!loadingPreview" class="rh-preview-empty">
        <div class="rh-preview-empty-icon">📋</div>
        <div>Click "Preview Data" to see a live preview of this report</div>
      </div>

      <!-- Loading state -->
      <div v-if="loadingPreview" class="rh-preview-loading">
        <div class="rh-spinner"></div>
        <div>Loading report data...</div>
      </div>
    </div>

    <!-- AI Conversational Mode -->
    <div v-if="selectedTemplate?.aiEnhanced" class="rh-ai-section">
      <div class="rh-ai-header">
        <span class="rh-ai-icon">✦</span>
        <span class="rh-ai-title">AI Assistant — Ρώτα ό,τι θέλεις</span>
      </div>
      <div class="rh-ai-input-row">
        <input
          v-model="aiQuestion"
          class="rh-ai-input"
          placeholder="π.χ. Πόσες υποθέσεις έχω ανοιχτές στο Legal της Crossworld;"
          @keyup.enter="askAi"
        />
        <button class="rh-btn rh-btn-ai" @click="askAi" :disabled="aiLoading || !aiQuestion.trim()">
          {{ aiLoading ? 'Σκέφτομαι...' : '✦ Ρώτα' }}
        </button>
      </div>
      <div v-if="aiAnswer" class="rh-ai-answer" v-html="renderMarkdown(aiAnswer)"></div>
      <div v-if="aiLoading" class="rh-ai-loading">
        <div class="rh-spinner"></div>
        <span>Αναλύω τα δεδομένα σου...</span>
      </div>
    </div>

    <!-- Empty state (no template selected) -->
    <div v-if="!selectedTemplate && templates.length > 0" class="rh-empty-state">
      <div class="rh-empty-icon">📊</div>
      <div class="rh-empty-text">Select a template above to preview and generate a report</div>
    </div>

    <!-- Error state -->
    <div v-if="error" class="rh-error">
      <span>⚠ {{ error }}</span>
      <button @click="error = null" class="rh-error-dismiss">✕</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reportService } from '../services/reportService'

const templates = ref([])
const selectedTemplate = ref(null)
const previewData = ref(null)
const loadingPreview = ref(false)
const downloading = ref(false)
const error = ref(null)
const aiQuestion = ref('')
const aiAnswer = ref('')
const aiLoading = ref(false)

const ICONS = {
  shield: '🛡️',
  briefcase: '💼',
  'folder-lock': '📁',
  smartphone: '📱'
}

function getIcon(iconName) {
  return ICONS[iconName] || '📊'
}

function selectTemplate(tmpl) {
  selectedTemplate.value = tmpl
  previewData.value = null
}

async function loadPreview() {
  if (!selectedTemplate.value) return
  loadingPreview.value = true
  error.value = null
  try {
    previewData.value = await reportService.getPreview(selectedTemplate.value.id)
  } catch (e) {
    error.value = 'Failed to load preview: ' + (e.response?.status === 403 ? 'Access denied' : e.message)
  } finally {
    loadingPreview.value = false
  }
}

async function downloadPdf() {
  if (!selectedTemplate.value) return
  downloading.value = true
  error.value = null
  try {
    await reportService.downloadReport(selectedTemplate.value.id)
  } catch (e) {
    error.value = 'Failed to generate PDF: ' + (e.response?.status === 403 ? 'Access denied' : e.message)
  } finally {
    downloading.value = false
  }
}

async function askAi() {
  if (!aiQuestion.value.trim()) return
  aiLoading.value = true
  aiAnswer.value = ''
  try {
    aiAnswer.value = await reportService.aiQuery(aiQuestion.value)
  } catch (e) {
    aiAnswer.value = '⚠ ' + (e.response?.data?.answer || e.message)
  } finally {
    aiLoading.value = false
  }
}

function renderMarkdown(text) {
  return text
    .replace(/## (.*)/g, '<h3 style="margin:12px 0 6px;color:var(--text);">$1</h3>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/- (.*)/g, '<div style="padding:2px 0 2px 12px;">• $1</div>')
    .replace(/\n/g, '<br>')
}

function formatDate(dt) {
  if (!dt) return '—'
  const d = new Date(dt)
  return d.toLocaleDateString('el-GR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatKey(key) {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, s => s.toUpperCase())
    .replace('Mfa', 'MFA')
    .replace('Tls', 'TLS')
    .replace('Jwt', 'JWT')
    .replace('Percent', '%')
}

function formatValue(key, value) {
  if (typeof value === 'boolean') return value ? '✓ Yes' : '✗ No'
  if (typeof value === 'number' && key.toLowerCase().includes('percent')) return value + '%'
  return String(value)
}

function getSummaryClass(key, value) {
  if (key.toLowerCase().includes('pending') && value > 0) return 'warning'
  if (key.toLowerCase().includes('coverage') && value < 100) return 'warning'
  if (key.toLowerCase().includes('coverage') && value >= 100) return 'success'
  return ''
}

onMounted(async () => {
  try {
    templates.value = await reportService.getTemplates()
  } catch (e) {
    if (e.response?.status === 403) {
      error.value = 'Reports Hub is available to CEO users only.'
    } else {
      error.value = 'Failed to load templates.'
    }
  }
})
</script>

<style scoped>
.reports-hub {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 20px;
}

/* Header */
.rh-header {
  margin-bottom: 28px;
}
.rh-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.rh-icon {
  font-size: 36px;
}
.rh-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text);
  margin: 0;
}
.rh-subtitle {
  font-size: 13px;
  color: var(--text-dim);
  margin: 4px 0 0;
}

/* Section Labels */
.rh-section-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--text-dim);
  font-family: 'Nunito Sans', sans-serif;
  margin-bottom: 14px;
}

/* Gallery */
.rh-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
  margin-bottom: 28px;
}
.rh-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}
.rh-card:hover {
  border-color: var(--accent);
  box-shadow: 0 2px 12px rgba(59,130,246,0.1);
}
.rh-card.active {
  border-color: var(--accent);
  background: var(--accent-dim, rgba(59,130,246,0.05));
  box-shadow: 0 2px 12px rgba(59,130,246,0.15);
}
.rh-card-icon {
  font-size: 28px;
  margin-bottom: 12px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}
.rh-card-icon.icon-compliance { background: rgba(34,197,94,0.1); }
.rh-card-icon.icon-management { background: rgba(59,130,246,0.1); }
.rh-card-icon.icon-legal { background: rgba(234,179,8,0.1); }
.rh-card-icon.icon-security { background: rgba(168,85,247,0.1); }

.rh-card-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
}
.rh-card-desc {
  font-size: 12px;
  color: var(--text-mid);
  line-height: 1.5;
  margin-bottom: 10px;
}
.rh-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.rh-tag {
  font-size: 9px;
  font-weight: 600;
  padding: 2px 7px;
  border-radius: 4px;
  background: var(--surface2);
  color: var(--text-dim);
  border: 1px solid var(--border);
}
.rh-ai-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  background: rgba(124,58,237,0.1);
  color: #7c3aed;
  border: 1px solid rgba(124,58,237,0.2);
}

/* Preview Pane */
.rh-preview {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.rh-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--surface2);
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
  gap: 12px;
}
.rh-preview-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}
.rh-preview-actions {
  display: flex;
  gap: 8px;
}
.rh-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid var(--border);
  cursor: pointer;
  transition: all 0.15s;
}
.rh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.rh-btn-preview {
  background: var(--surface);
  color: var(--text);
}
.rh-btn-preview:hover:not(:disabled) {
  background: var(--surface2);
}
.rh-btn-download {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
}
.rh-btn-download:hover:not(:disabled) {
  opacity: 0.9;
}

/* Preview Content */
.rh-preview-content {
  padding: 20px;
}
.rh-preview-meta {
  font-size: 11px;
  color: var(--text-dim);
  margin-bottom: 18px;
}

/* Summary Grid */
.rh-summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  margin-bottom: 24px;
}
.rh-summary-card {
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}
.rh-summary-label {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--text-dim);
  margin-bottom: 6px;
}
.rh-summary-value {
  font-size: 16px;
  font-weight: 800;
  color: var(--text);
  word-break: break-word;
}
.rh-summary-value.warning { color: var(--yellow, #d97706); }
.rh-summary-value.success { color: var(--green, #059669); }

/* Sections */
.rh-section {
  border: 1px solid var(--border);
  border-radius: 10px;
  margin-bottom: 14px;
  overflow: hidden;
}
.rh-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--surface2);
  border-bottom: 1px solid var(--border);
}
.rh-section-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text);
}
.rh-status {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
}
.rh-status.compliant {
  background: rgba(5,150,105,0.1);
  color: #059669;
}
.rh-status.action_required {
  background: rgba(217,119,6,0.1);
  color: #d97706;
}

/* Checklist */
.rh-checklist {
  padding: 12px 16px;
}
.rh-check-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  border-bottom: 1px solid var(--border);
}
.rh-check-row:last-child {
  border-bottom: none;
}
.rh-check-icon {
  font-size: 14px;
  flex-shrink: 0;
}
.rh-check-text {
  font-size: 12px;
  color: var(--text);
  flex: 1;
}

/* Table */
.rh-table-wrap {
  overflow-x: auto;
}
.rh-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.rh-table th {
  text-align: left;
  padding: 10px 14px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--text-dim);
  border-bottom: 2px solid var(--border);
}
.rh-table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
  color: var(--text);
}
.rh-td-name {
  font-weight: 600;
}
.rh-role-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
}
.rh-role-badge.ceo {
  background: rgba(59,130,246,0.1);
  color: #3b82f6;
}
.rh-role-badge.dept_head {
  background: rgba(5,150,105,0.1);
  color: #059669;
}
.rh-role-badge.viewer {
  background: rgba(148,163,184,0.1);
  color: #64748b;
}
.rh-mfa-on {
  color: #059669;
  font-weight: 600;
  font-size: 11px;
}
.rh-mfa-off {
  color: #dc2626;
  font-weight: 600;
  font-size: 11px;
}
.rh-action-needed {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(217,119,6,0.1);
  color: #d97706;
}
.rh-action-ok {
  color: var(--text-dim);
}

/* Priority badges */
.rh-priority {
  font-size: 9px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 4px;
  flex-shrink: 0;
}
.rh-priority.critical {
  background: rgba(220,38,38,0.1);
  color: #dc2626;
}
.rh-priority.high {
  background: rgba(217,119,6,0.1);
  color: #d97706;
}
.rh-priority.medium {
  background: rgba(59,130,246,0.1);
  color: #3b82f6;
}

/* Action list */
.rh-action-list {
  padding: 12px 16px;
}
.rh-action-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 12px;
  color: var(--text);
  border-bottom: 1px solid var(--border);
}
.rh-action-row:last-child { border-bottom: none; }
.rh-action-user {
  font-weight: 600;
}

/* Empty / Loading / Error states */
.rh-empty-state, .rh-preview-empty {
  text-align: center;
  padding: 48px 20px;
  color: var(--text-dim);
}
.rh-empty-icon, .rh-preview-empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.4;
}
.rh-empty-text {
  font-size: 14px;
}
.rh-preview-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: var(--text-dim);
  font-size: 13px;
}
.rh-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: rh-spin 0.7s linear infinite;
}
@keyframes rh-spin {
  to { transform: rotate(360deg); }
}
.rh-error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-top: 14px;
  background: rgba(220,38,38,0.06);
  border: 1px solid rgba(220,38,38,0.2);
  border-radius: 8px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
}
.rh-error-dismiss {
  background: none;
  border: none;
  color: #dc2626;
  cursor: pointer;
  font-size: 16px;
  padding: 0 4px;
}

.rh-ai-section {
  background: linear-gradient(135deg, rgba(124,58,237,0.03), rgba(59,130,246,0.03));
  border: 1px solid rgba(124,58,237,0.15);
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 20px;
}
.rh-ai-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.rh-ai-icon {
  font-size: 20px;
  color: #7c3aed;
}
.rh-ai-title {
  font-size: 14px;
  font-weight: 700;
  color: #7c3aed;
}
.rh-ai-input-row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.rh-ai-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text);
  background: var(--surface);
  font-family: 'Nunito', sans-serif;
}
.rh-ai-input:focus {
  border-color: #7c3aed;
  outline: none;
}
.rh-btn-ai {
  background: #7c3aed;
  color: white;
  border-color: #7c3aed;
  white-space: nowrap;
}
.rh-btn-ai:hover:not(:disabled) {
  background: #6d28d9;
}
.rh-ai-answer {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text);
}
.rh-ai-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  color: #7c3aed;
  font-size: 13px;
}

/* Responsive */
@media (max-width: 768px) {
  .rh-gallery {
    grid-template-columns: 1fr;
  }
  .rh-summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .rh-preview-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
