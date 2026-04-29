const fs = require('fs');

// en.json — find pd cancel/save pattern with flexible whitespace
const enPath = 'C:/Users/akage/Next2View/frontend/src/i18n/en.json';
let en = fs.readFileSync(enPath, 'utf8');

// Search for the pd section's cancel+save
const enKeys = `    "historyTitle": "Project History",
    "historyCreatedBy": "Created by",
    "historyUnknown": "Unknown",
    "historyLoading": "Loading...",
    "historyNoChanges": "No recorded changes yet.",`;

// Find "save": "Save" in pd section and add after it
const enSaveMatch = en.match(/"save":\s*"Save",?\s*\n/);
if (enSaveMatch && !en.includes('"historyTitle"')) {
  const idx = en.indexOf(enSaveMatch[0]) + enSaveMatch[0].length;
  en = en.substring(0, idx) + enKeys + '\n' + en.substring(idx);
  fs.writeFileSync(enPath, en, 'utf8');
  console.log('en.json patched:', en.includes('"historyTitle"'));
} else if (en.includes('"historyTitle"')) {
  console.log('en.json already has historyTitle');
} else {
  console.log('en.json FAILED — could not find save pattern');
}

// el.json
const elPath = 'C:/Users/akage/Next2View/frontend/src/i18n/el.json';
let el = fs.readFileSync(elPath, 'utf8');

const elKeys = `    "historyTitle": "Ιστορικό Project",
    "historyCreatedBy": "Δημιουργήθηκε από",
    "historyUnknown": "Άγνωστος",
    "historyLoading": "Φόρτωση...",
    "historyNoChanges": "Δεν υπάρχουν καταγεγραμμένες αλλαγές.",`;

const elSaveMatch = el.match(/"save":\s*"Αποθήκευση",?\s*\n/);
if (elSaveMatch && !el.includes('"historyTitle"')) {
  const idx = el.indexOf(elSaveMatch[0]) + elSaveMatch[0].length;
  el = el.substring(0, idx) + elKeys + '\n' + el.substring(idx);
  fs.writeFileSync(elPath, el, 'utf8');
  console.log('el.json patched:', el.includes('"historyTitle"'));
} else if (el.includes('"historyTitle"')) {
  console.log('el.json already has historyTitle');
} else {
  console.log('el.json FAILED — could not find save pattern');
}