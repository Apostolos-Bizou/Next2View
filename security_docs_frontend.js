const fs = require('fs');
const path = 'frontend/src/views/GuideView.vue';
let c = fs.readFileSync(path, 'utf8');
const lines = c.split('\n');

// =============================================================
// STEP 1: Find security panel start
// =============================================================
let secStartIdx = -1;
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes("activeTab === 'security'")) {
    secStartIdx = i;
    break;
  }
}

if (secStartIdx === -1) {
  console.error('Could not find security panel');
  process.exit(1);
}

console.log(`Security panel starts at line ${secStartIdx + 1}`);

// =============================================================
// STEP 2: Find the closing tag of the security panel hero (g-sec-hero)
// We will insert the action toolbar AFTER the hero and the access-control callout
// Let's find the line with "Access Control:" callout end, then insert after it
// =============================================================
let insertAfterIdx = -1;
for (let i = secStartIdx; i < lines.length; i++) {
  if (lines[i].includes('All items marked') && lines[i].includes('live in production')) {
    // Find next </div>
    for (let j = i; j < lines.length; j++) {
      if (lines[j].trim() === '</div>') {
        insertAfterIdx = j;
        break;
      }
    }
    break;
  }
}

if (insertAfterIdx === -1) {
  console.error('Could not find insertion point');
  process.exit(1);
}

console.log(`Will insert toolbar + docs section after line ${insertAfterIdx + 1}`);

// =============================================================
// STEP 3: Build the toolbar + Security Documents Manager section
// =============================================================
const newSection = `
        <!-- Action Toolbar -->
        <div class="g-sec-toolbar">
          <button class="g-btn-action g-btn-print" @click="printSecurityTab">
            <span class="g-btn-icon">🖨️</span>
            <span class="g-btn-text">Print This Tab as PDF</span>
          </button>
          <button class="g-btn-action g-btn-upload" @click="triggerUpload" v-if="permStore.isCEO() || permStore.can('viewSecurity')">
            <span class="g-btn-icon">📤</span>
            <span class="g-btn-text">Upload Document</span>
          </button>
          <input ref="secFileInput" type="file" @change="handleSecUpload" style="display:none" accept=".pdf,.docx,.doc,.xlsx,.xls,.pptx,.ppt,.txt,.md" />
        </div>

        <h3>📚 Security Documents Repository</h3>
        <p style="color:var(--text-secondary,#64748b);font-size:14px;margin-bottom:14px;">
          Επίσημα έγγραφα τεκμηρίωσης ασφάλειας — DPIA, Technical Security Architecture, Audit reports, etc.
          Μπορείς να κατεβάσεις τα υπάρχοντα ή να ανεβάσεις νέες εκδόσεις.
        </p>

        <div v-if="secDocsLoading" class="g-loading">⏳ Loading documents...</div>

        <div v-else-if="securityDocs.length === 0" class="g-empty">
          <p style="margin:8px 0;color:var(--text-secondary,#64748b);">Κανένα έγγραφο δεν έχει ανέβει ακόμη.</p>
          <p style="margin:0;font-size:13px;color:var(--text-secondary,#64748b);">Πάτησε "Upload Document" για να ξεκινήσεις.</p>
        </div>

        <div v-else class="g-docs-list">
          <div v-for="doc in securityDocs" :key="doc.id" class="g-doc-card">
            <div class="g-doc-icon">{{ getDocIcon(doc.filename) }}</div>
            <div class="g-doc-content">
              <div class="g-doc-title">{{ doc.filename }}</div>
              <div class="g-doc-meta">
                <span>📅 {{ formatDate(doc.uploadedAt) }}</span>
                <span>•</span>
                <span>👤 {{ doc.uploaderName || 'Unknown' }}</span>
                <span>•</span>
                <span>📦 {{ formatBytes(doc.sizeBytes) }}</span>
              </div>
              <div v-if="doc.description" class="g-doc-desc">{{ doc.description }}</div>
            </div>
            <div class="g-doc-actions">
              <button class="g-btn-mini g-btn-download" @click="downloadDoc(doc)" title="Download">
                ⬇️
              </button>
              <button v-if="permStore.isCEO()" class="g-btn-mini g-btn-delete" @click="deleteDoc(doc)" title="Delete">
                🗑️
              </button>
            </div>
          </div>
        </div>

        <div v-if="secDocsError" class="g-callout-warning" style="margin-top:14px;">
          ⚠️ {{ secDocsError }}
        </div>

`;

lines.splice(insertAfterIdx + 1, 0, ...newSection.split('\n'));
console.log(`✅ Inserted Security Documents section`);

// =============================================================
// STEP 4: Add the script logic for upload/download/delete
// Find <script setup> opening and add our refs/methods
// =============================================================
let scriptSetupIdx = -1;
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('<script setup>') || lines[i].match(/^\s*<script\s+setup/)) {
    scriptSetupIdx = i;
    break;
  }
}

if (scriptSetupIdx === -1) {
  console.error('Could not find <script setup>');
  process.exit(1);
}

// Find import lines section - we need to add 'onMounted' import
// Look for existing 'ref' import line or 'computed' import
let vueImportIdx = -1;
for (let i = scriptSetupIdx; i < scriptSetupIdx + 30; i++) {
  if (lines[i] && lines[i].includes("from 'vue'")) {
    vueImportIdx = i;
    break;
  }
}

if (vueImportIdx !== -1) {
  // Check if onMounted is already imported
  if (!lines[vueImportIdx].includes('onMounted')) {
    lines[vueImportIdx] = lines[vueImportIdx].replace(
      /import\s*\{([^}]+)\}/,
      (match, imports) => `import { ${imports.trim()}, onMounted }`
    );
    console.log(`✅ Added onMounted import to line ${vueImportIdx + 1}`);
  }
}

// =============================================================
// STEP 5: Find the right place to inject our script logic
// Look for 'const visibleTabs' line and inject after it
// =============================================================
let injectAfterIdx = -1;
for (let i = scriptSetupIdx; i < lines.length; i++) {
  if (lines[i].includes('visibleTabs') && lines[i].includes('computed')) {
    injectAfterIdx = i;
    break;
  }
}

if (injectAfterIdx === -1) {
  console.error('Could not find visibleTabs line for injection');
  process.exit(1);
}

const scriptAddition = `
// ============= Security Documents Manager =============
import api from '@/services/api'
const securityDocs = ref([])
const secDocsLoading = ref(false)
const secDocsError = ref('')
const secFileInput = ref(null)

const loadSecurityDocs = async () => {
  if (!permStore.isCEO() && !permStore.can('viewSecurity')) return
  secDocsLoading.value = true
  secDocsError.value = ''
  try {
    const res = await api.get('/security-documents')
    securityDocs.value = res.data || []
  } catch (e) {
    secDocsError.value = 'Δεν φορτώθηκαν τα έγγραφα: ' + (e.response?.data?.error || e.message)
    console.error('Load security docs failed:', e)
  } finally {
    secDocsLoading.value = false
  }
}

const triggerUpload = () => {
  if (secFileInput.value) secFileInput.value.click()
}

const handleSecUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  const description = prompt('Description (optional):') || ''
  const fd = new FormData()
  fd.append('file', file)
  if (description) fd.append('description', description)
  secDocsLoading.value = true
  secDocsError.value = ''
  try {
    await api.post('/security-documents/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    await loadSecurityDocs()
  } catch (e) {
    secDocsError.value = 'Upload failed: ' + (e.response?.data?.error || e.message)
  } finally {
    secDocsLoading.value = false
    if (secFileInput.value) secFileInput.value.value = ''
  }
}

const downloadDoc = async (doc) => {
  try {
    const res = await api.get(\`/security-documents/\${doc.id}/download\`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', doc.filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    secDocsError.value = 'Download failed: ' + (e.response?.data?.error || e.message)
  }
}

const deleteDoc = async (doc) => {
  if (!confirm(\`Διαγραφή του "\${doc.filename}"?\`)) return
  try {
    await api.delete(\`/security-documents/\${doc.id}\`)
    await loadSecurityDocs()
  } catch (e) {
    secDocsError.value = 'Delete failed: ' + (e.response?.data?.error || e.message)
  }
}

const getDocIcon = (filename) => {
  const ext = (filename || '').split('.').pop().toLowerCase()
  if (['pdf'].includes(ext)) return '📕'
  if (['docx','doc'].includes(ext)) return '📘'
  if (['xlsx','xls'].includes(ext)) return '📗'
  if (['pptx','ppt'].includes(ext)) return '📙'
  return '📄'
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const sizes = ['B','KB','MB','GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + sizes[i]
}

const formatDate = (iso) => {
  if (!iso) return ''
  try {
    const d = new Date(iso)
    return d.toLocaleDateString('el-GR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch { return iso }
}

const printSecurityTab = () => {
  // Switch to security tab if not active (just in case)
  if (activeTab.value !== 'security') {
    activeTab.value = 'security'
  }
  // Wait a tick then print
  setTimeout(() => {
    window.print()
  }, 100)
}

onMounted(() => {
  loadSecurityDocs()
})
`;

lines.splice(injectAfterIdx + 1, 0, ...scriptAddition.split('\n'));
console.log(`✅ Injected script logic`);

// =============================================================
// STEP 6: Add CSS for new components (action toolbar, doc cards, print styles)
// =============================================================
let styleEndIdx = -1;
for (let i = lines.length - 1; i >= 0; i--) {
  if (lines[i].includes('</style>')) {
    styleEndIdx = i;
    break;
  }
}

const newStyles = `
/* ============= Security Documents Manager ============= */
.g-sec-toolbar {
  display: flex;
  gap: 10px;
  margin: 18px 0 24px 0;
  flex-wrap: wrap;
}
.g-btn-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--bg-card, #ffffff);
  color: var(--text, #0f172a);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.g-btn-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.g-btn-print {
  border-color: rgba(30,58,138,0.3);
  background: rgba(30,58,138,0.05);
  color: #1e3a8a;
}
.g-btn-print:hover {
  background: rgba(30,58,138,0.1);
}
.g-btn-upload {
  border-color: rgba(5,150,105,0.3);
  background: rgba(5,150,105,0.05);
  color: #047857;
}
.g-btn-upload:hover {
  background: rgba(5,150,105,0.1);
}
.g-btn-icon { font-size: 16px; line-height: 1; }
.g-btn-text { font-size: 14px; }

.g-loading, .g-empty {
  text-align: center;
  padding: 32px 16px;
  background: var(--bg-card, #ffffff);
  border: 1px dashed var(--border, #e2e8f0);
  border-radius: 12px;
}

.g-docs-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.g-doc-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  transition: all 0.2s;
}
.g-doc-card:hover {
  border-color: rgba(30,58,138,0.3);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.g-doc-icon {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;
}
.g-doc-content {
  flex: 1;
  min-width: 0;
}
.g-doc-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text, #0f172a);
  margin-bottom: 4px;
  word-break: break-word;
}
.g-doc-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary, #64748b);
  flex-wrap: wrap;
}
.g-doc-desc {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-top: 4px;
  font-style: italic;
}
.g-doc-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.g-btn-mini {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--bg-card, #ffffff);
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.g-btn-mini:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}
.g-btn-download:hover { background: rgba(5,150,105,0.1); border-color: rgba(5,150,105,0.4); }
.g-btn-delete:hover { background: rgba(220,38,38,0.1); border-color: rgba(220,38,38,0.4); }

/* ============= Print Styles for Security Tab ============= */
@media print {
  /* Hide everything except security panel */
  body > *:not(#app),
  .sidebar,
  .nav,
  nav,
  header,
  .header,
  .top-bar,
  .topbar,
  .dashboard-sidebar,
  .layout-sidebar,
  .guide-tabs,
  .g-sec-toolbar,
  .g-doc-actions,
  button {
    display: none !important;
  }

  /* Clean print layout */
  body, html {
    background: white !important;
    color: black !important;
    margin: 0 !important;
    padding: 0 !important;
  }

  .guide-panel,
  .layout-main,
  main,
  .main-content {
    padding: 0 !important;
    margin: 0 !important;
    background: white !important;
    max-width: none !important;
  }

  /* Show only security panel */
  .guide-panel {
    display: block !important;
  }

  /* Print-friendly card and table styles */
  .g-stat-card,
  .g-doc-card,
  .g-layer,
  .g-sec-hero {
    border: 1px solid #ccc !important;
    background: white !important;
    page-break-inside: avoid;
  }

  .g-table {
    page-break-inside: auto;
    border: 1px solid #ccc;
  }

  .g-table tr {
    page-break-inside: avoid;
  }

  .g-callout-info,
  .g-callout-warning,
  .g-callout-success {
    border: 1px solid #999 !important;
    background: #f9f9f9 !important;
    page-break-inside: avoid;
  }

  h2, h3 {
    page-break-after: avoid;
  }

  .g-badge {
    border: 1px solid #999 !important;
  }

  /* Hide download icons in print */
  .g-doc-icon {
    font-size: 20px !important;
  }

  /* Add print header */
  .guide-panel::before {
    content: "Next2View — Security Documentation — Printed " attr(data-print-date);
    display: block;
    text-align: center;
    font-size: 11px;
    color: #666;
    border-bottom: 1px solid #ccc;
    padding-bottom: 6px;
    margin-bottom: 14px;
  }
}
`;

lines.splice(styleEndIdx, 0, newStyles);
console.log(`✅ Added CSS (toolbar, doc cards, print styles)`);

fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log('\n🎉 Frontend Security Documents Manager + Print PDF complete!');
console.log('   - Upload/Download/Delete UI');
console.log('   - Print PDF button (window.print() with optimized CSS)');
console.log('   - File icons by type, formatted size, dates');
console.log('   - CEO can delete; viewSecurity can upload/download');
