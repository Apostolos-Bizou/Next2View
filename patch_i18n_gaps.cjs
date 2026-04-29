const fs = require('fs');

// ═══ STEP 1: Add i18n keys to en.json ═══
const enPath = 'C:/Users/akage/Next2View/frontend/src/i18n/en.json';
let en = fs.readFileSync(enPath, 'utf8');

// Add after "save": "Save" in pd section (line ~382)
en = en.replace(
  '"cancel": "Cancel",\n    "save": "Save",',
  '"cancel": "Cancel",\n    "save": "Save",\n    "historyTitle": "Project History",\n    "historyCreatedBy": "Created by",\n    "historyUnknown": "Unknown",\n    "historyLoading": "Loading...",\n    "historyNoChanges": "No recorded changes yet.",'
);

fs.writeFileSync(enPath, en, 'utf8');
console.log('en.json historyTitle:', en.includes('"historyTitle"'));

// ═══ STEP 2: Add i18n keys to el.json ═══
const elPath = 'C:/Users/akage/Next2View/frontend/src/i18n/el.json';
let el = fs.readFileSync(elPath, 'utf8');

// Find the same pattern in el.json
el = el.replace(
  '"cancel": "Ακύρωση",\n    "save": "Αποθήκευση",',
  '"cancel": "Ακύρωση",\n    "save": "Αποθήκευση",\n    "historyTitle": "Ιστορικό Project",\n    "historyCreatedBy": "Δημιουργήθηκε από",\n    "historyUnknown": "Άγνωστος",\n    "historyLoading": "Φόρτωση...",\n    "historyNoChanges": "Δεν υπάρχουν καταγεγραμμένες αλλαγές.",'
);

fs.writeFileSync(elPath, el, 'utf8');
console.log('el.json historyTitle:', el.includes('"historyTitle"'));

// ═══ STEP 3: Fix hardcoded strings in ProjectDetailView.vue ═══
const vuePath = 'C:/Users/akage/Next2View/frontend/src/views/ProjectDetailView.vue';
let vue = fs.readFileSync(vuePath, 'utf8');

// Fix 1: "Άκυρο" → tt('pd.cancel')
vue = vue.replace(
  '>            Άκυρο\n',
  '>            {{ tt(\'pd.cancel\') }}\n'
);
// Try alternate spacing
vue = vue.replace(
  '>\n            Άκυρο\n',
  '>\n            {{ tt(\'pd.cancel\') }}\n'
);

// Fix 2: Project History title
vue = vue.replace(
  "📋 Project History",
  "📋 {{ tt('pd.historyTitle') }}"
);

// Fix 3: Created by ... Unknown
vue = vue.replace(
  "Created by <strong>{{ project.createdByName || 'Unknown' }}</strong>",
  "{{ tt('pd.historyCreatedBy') }} <strong>{{ project.createdByName || tt('pd.historyUnknown') }}</strong>"
);

// Fix 4: Loading...
vue = vue.replace(
  'class="history-loading">Loading...</div>',
  "class=\"history-loading\">{{ tt('pd.historyLoading') }}</div>"
);

// Fix 5: No recorded changes yet
vue = vue.replace(
  'class="history-empty">No recorded changes yet.</div>',
  "class=\"history-empty\">{{ tt('pd.historyNoChanges') }}</div>"
);

fs.writeFileSync(vuePath, vue, 'utf8');

const verify = fs.readFileSync(vuePath, 'utf8');
console.log('Άκυρο fixed:', !verify.includes('>            Άκυρο'));
console.log('historyTitle used:', verify.includes("tt('pd.historyTitle')"));
console.log('historyCreatedBy used:', verify.includes("tt('pd.historyCreatedBy')"));
console.log('historyLoading used:', verify.includes("tt('pd.historyLoading')"));
console.log('historyNoChanges used:', verify.includes("tt('pd.historyNoChanges')"));