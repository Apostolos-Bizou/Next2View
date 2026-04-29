const fs = require('fs');
const filePath = 'C:/Users/akage/Next2View/frontend/src/views/ProjectDetailView.vue';
let code = fs.readFileSync(filePath, 'utf8');

// STEP 1: Add historyExpanded ref
code = code.replace(
  "const historyLoaded = ref(false)",
  "const historyLoaded = ref(false)\nconst historyExpanded = ref(false)"
);

// STEP 2: Replace the entire PROJECT HISTORY template block
const oldTemplate = `      <!-- PROJECT HISTORY (immutable audit trail) -->
      <div v-if="project" class="project-history-panel" style="margin-top:14px;">
        <div class="history-header">
          <div class="history-title">\u{1F4CB} Project History</div>
          <button v-if="!historyLoaded" class="filter-btn" @click="loadProjectHistory" style="font-size:10px;">Load History</button>
        </div>
        <div v-if="project && project.createdBy" class="history-created">
          Created by <strong>{{ project.createdByName || 'Unknown' }}</strong> \u2014 {{ formatHistoryDate(project.createdAt) }}
        </div>
        <div v-if="historyLoading" class="history-loading">Loading...</div>
        <div v-else-if="historyLoaded && !projectHistory.length" class="history-empty">No recorded changes yet.</div>
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
      </div>`;

const newTemplate = `      <!-- PROJECT HISTORY (collapsible audit trail) -->
      <div v-if="project" class="project-history-panel" style="margin-top:14px;">
        <div class="history-header" @click="historyExpanded = !historyExpanded; if (historyExpanded && !historyLoaded) loadProjectHistory()" style="cursor:pointer;user-select:none;">
          <div style="display:flex;align-items:center;gap:8px;">
            <span class="history-chevron" :class="{ open: historyExpanded }">\u25B6</span>
            <div class="history-title">\u{1F4CB} Project History</div>
          </div>
          <span v-if="historyLoaded && projectHistory.length" class="history-count">{{ projectHistory.length }}</span>
        </div>
        <div v-if="historyExpanded" class="history-body">
          <div v-if="project && project.createdBy" class="history-created">
            Created by <strong>{{ project.createdByName || 'Unknown' }}</strong> \u2014 {{ formatHistoryDate(project.createdAt) }}
          </div>
          <div v-if="historyLoading" class="history-loading">Loading...</div>
          <div v-else-if="historyLoaded && !projectHistory.length" class="history-empty">No recorded changes yet.</div>
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
      </div>`;

if (code.includes(oldTemplate)) {
  code = code.replace(oldTemplate, newTemplate);
  console.log('Template replaced: true');
} else {
  console.log('Template replaced: false — trying line-by-line');
  // Normalize line endings
  code = code.replace(/\r\n/g, '\n');
  const oldNorm = oldTemplate.replace(/\r\n/g, '\n');
  const newNorm = newTemplate.replace(/\r\n/g, '\n');
  if (code.includes(oldNorm)) {
    code = code.replace(oldNorm, newNorm);
    console.log('Template replaced (normalized): true');
  } else {
    console.log('ERROR: Could not find template block');
    process.exit(1);
  }
}

// STEP 3: Add CSS for chevron and count badge
const cssAddition = `
.history-chevron { font-size: 9px; color: var(--text-dim); transition: transform 0.2s ease; display: inline-block; }
.history-chevron.open { transform: rotate(90deg); }
.history-count { font-size: 10px; font-weight: 700; color: var(--accent); background: var(--accent-dim); padding: 2px 8px; border-radius: 10px; }
.history-body { animation: slideDown 0.2s ease; }
`;

code = code.replace(
  '.project-history-panel {',
  cssAddition + '\n.project-history-panel {'
);

fs.writeFileSync(filePath, code, 'utf8');

const verify = fs.readFileSync(filePath, 'utf8');
console.log('historyExpanded ref:', verify.includes('historyExpanded = ref(false)'));
console.log('chevron in template:', verify.includes('history-chevron'));
console.log('history-body wrapper:', verify.includes('history-body'));
console.log('auto-load on expand:', verify.includes('if (historyExpanded && !historyLoaded) loadProjectHistory()'));