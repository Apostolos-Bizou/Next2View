const fs = require('fs');

// === 1. Add entity history endpoint to ActivityLogController ===
const ctrlFile = 'backend/src/main/java/com/next2me/next2view/controller/ActivityLogController.java';
let ctrl = fs.readFileSync(ctrlFile, 'utf8').split(/\r?\n/);

if (ctrl.some(l => l.includes('getEntityHistory'))) {
  console.log('SKIP controller: already has entity history endpoint');
} else {
  const parseIdx = ctrl.findIndex(l => l.includes('private UUID parseUserId'));
  if (parseIdx > 0) {
    ctrl.splice(parseIdx, 0,
      '    /**',
      '     * GET /api/activity-log/entity/{entityType}/{entityId}',
      '     * Returns activity history for a specific entity (e.g. project history).',
      '     */',
      '    @GetMapping("/entity/{entityType}/{entityId}")',
      '    @PreAuthorize("isAuthenticated()")',
      '    public ResponseEntity<List<ActivityLog>> getEntityHistory(',
      '            @PathVariable String entityType,',
      '            @PathVariable UUID entityId,',
      '            @AuthenticationPrincipal String userId',
      '    ) {',
      '        parseUserId(userId); // auth check',
      '        List<ActivityLog> history = activityLogService.getEntityHistory(entityType.toUpperCase(), entityId);',
      '        return ResponseEntity.ok(history);',
      '    }',
      ''
    );
    fs.writeFileSync(ctrlFile, ctrl.join('\n'), 'utf8');
    console.log('DONE: entity history endpoint added');
  }
}

// === 2. Patch ProjectDetailView.vue — add Project History section ===
const vueFile = 'frontend/src/views/ProjectDetailView.vue';
let vue = fs.readFileSync(vueFile, 'utf8');

if (vue.includes('project-history')) {
  console.log('SKIP frontend: already has project history');
  process.exit(0);
}

// Add HTML section AFTER CEO Notes section (before closing divs)
// Find "<!-- CEO NOTES -->" and insert AFTER that whole section
const historyHtml = `
      <!-- PROJECT HISTORY (immutable audit trail) -->
      <div class="project-history-panel" style="margin-top:14px;">
        <div class="history-header">
          <div class="history-title">\uD83D\uDCCB Project History</div>
          <button v-if="!historyLoaded" class="filter-btn" @click="loadProjectHistory" style="font-size:10px;">Load History</button>
        </div>
        <div v-if="project.createdBy" class="history-created">
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

// Insert after CEO Notes section — find the closing div after notes
// Actually, safer to insert before the last </div> of the project content
// Find line with "<!-- CEO NOTES -->"
const notesIdx = vue.indexOf('<!-- CEO NOTES -->');
if (notesIdx > 0) {
  // Find the section end — look for the next major section or end
  // Insert after the CEO notes panel closing
  // Let's find a reliable anchor: the edit modal or the end of template sections
  // Safest: insert before the edit modal
  const editModalAnchor = vue.indexOf('<!-- EDIT PROJECT MODAL');
  if (editModalAnchor > 0) {
    vue = vue.slice(0, editModalAnchor) + historyHtml + '\n\n      ' + vue.slice(editModalAnchor);
  } else {
    // Fallback: insert after CEO notes by finding end of notes section
    // Find 3rd </div> after CEO NOTES
    let pos = notesIdx;
    let divCount = 0;
    while (pos < vue.length && divCount < 5) {
      const nextDiv = vue.indexOf('</div>', pos + 1);
      if (nextDiv < 0) break;
      pos = nextDiv + 6;
      divCount++;
    }
    vue = vue.slice(0, pos) + '\n' + historyHtml + vue.slice(pos);
  }
}

// Add script variables and methods
vue = vue.replace(
  'const loading = ref(true)',
  `const loading = ref(true)
const projectHistory = ref([])
const historyLoaded = ref(false)
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
}`
);

// Auto-load history on mount
vue = vue.replace(
  'project.value = await store.fetchProject(route.params.id)\n  loading.value = false',
  'project.value = await store.fetchProject(route.params.id)\n  loading.value = false\n  loadProjectHistory()'
);

// Add CSS
vue = vue.replace(
  '</style>',
  `.project-history-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 18px 22px; }
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
</style>`
);

fs.writeFileSync(vueFile, vue, 'utf8');
console.log('DONE: ProjectDetailView patched with Project History section');