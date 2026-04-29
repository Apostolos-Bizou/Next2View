const fs = require('fs');

// Fix en.json — remove from wrong place, add to correct pd section
const enPath = 'C:/Users/akage/Next2View/frontend/src/i18n/en.json';
let en = fs.readFileSync(enPath, 'utf8');

// Remove the 5 wrongly placed keys
const wrongKeys = [
  '    "historyTitle": "Project History",',
  '    "historyCreatedBy": "Created by",',
  '    "historyUnknown": "Unknown",',
  '    "historyLoading": "Loading...",',
  '    "historyNoChanges": "No recorded changes yet.",'
];
for (const k of wrongKeys) {
  en = en.replace(k + '\n', '');
  en = en.replace(k + '\r\n', '');
}

// Now find the SECOND "save": "Save" (pd section, line ~387) and add after it
let firstIdx = en.indexOf('"save": "Save"');
let secondIdx = en.indexOf('"save": "Save"', firstIdx + 1);
if (secondIdx > 0) {
  // Find end of that line
  let lineEnd = en.indexOf('\n', secondIdx);
  let insertPoint = lineEnd + 1;
  const newKeys = '    "historyTitle": "Project History",\n    "historyCreatedBy": "Created by",\n    "historyUnknown": "Unknown",\n    "historyLoading": "Loading...",\n    "historyNoChanges": "No recorded changes yet.",\n';
  en = en.substring(0, insertPoint) + newKeys + en.substring(insertPoint);
  console.log('en.json fixed: second save found at', secondIdx);
} else {
  console.log('en.json ERROR: second save not found');
}
fs.writeFileSync(enPath, en, 'utf8');

// Fix el.json — same approach
const elPath = 'C:/Users/akage/Next2View/frontend/src/i18n/el.json';
let el = fs.readFileSync(elPath, 'utf8');

const wrongKeysEl = [
  '    "historyTitle": "Ιστορικό Project",',
  '    "historyCreatedBy": "Δημιουργήθηκε από",',
  '    "historyUnknown": "Άγνωστος",',
  '    "historyLoading": "Φόρτωση...",',
  '    "historyNoChanges": "Δεν υπάρχουν καταγεγραμμένες αλλαγές.",'
];
for (const k of wrongKeysEl) {
  el = el.replace(k + '\n', '');
  el = el.replace(k + '\r\n', '');
}

let firstIdxEl = el.indexOf('"save": "Αποθήκευση"');
let secondIdxEl = el.indexOf('"save": "Αποθήκευση"', firstIdxEl + 1);
if (secondIdxEl > 0) {
  let lineEnd = el.indexOf('\n', secondIdxEl);
  let insertPoint = lineEnd + 1;
  const newKeysEl = '    "historyTitle": "Ιστορικό Project",\n    "historyCreatedBy": "Δημιουργήθηκε από",\n    "historyUnknown": "Άγνωστος",\n    "historyLoading": "Φόρτωση...",\n    "historyNoChanges": "Δεν υπάρχουν καταγεγραμμένες αλλαγές.",\n';
  el = el.substring(0, insertPoint) + newKeysEl + el.substring(insertPoint);
  console.log('el.json fixed: second save found at', secondIdxEl);
} else {
  console.log('el.json ERROR: second save not found');
}
fs.writeFileSync(elPath, el, 'utf8');

// Verify
const enV = fs.readFileSync(enPath, 'utf8');
const elV = fs.readFileSync(elPath, 'utf8');
// Check that historyTitle appears AFTER "delete": "Delete" (which is in pd section)
const enDeleteIdx = enV.lastIndexOf('"delete": "Delete"');
const enHistIdx = enV.indexOf('"historyTitle"');
console.log('en.json historyTitle after pd.delete:', enHistIdx > enDeleteIdx);

const elDeleteIdx = elV.lastIndexOf('"delete": "Διαγραφή"');
const elHistIdx = elV.indexOf('"historyTitle"');
console.log('el.json historyTitle after pd.delete:', elHistIdx > elDeleteIdx);