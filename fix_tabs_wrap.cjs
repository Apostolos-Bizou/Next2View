const fs = require('fs');
const f = 'frontend/src/views/NotificationsView.vue';
let lines = fs.readFileSync(f, 'utf8').split(/\r?\n/);

// Find the closing </div> of notif-header-bar (line 29 area)
// Then wrap everything from notif-empty to the end of notif-list in v-if alerts
// Strategy: add v-if to notif-empty and notif-list

let changed = false;

for (let i = 0; i < lines.length; i++) {
  // Add v-if to notif-empty (only the alerts one, not the activity one)
  if (lines[i].includes('class="notif-empty"') && !lines[i].includes('v-if') && !lines[i].includes('activityLoading')) {
    lines[i] = lines[i].replace('class="notif-empty"', 'v-if="activeTab===\'alerts\' && !filtered.length" class="notif-empty"');
    changed = true;
  }
  // Add v-if to notif-list (only the alerts one)
  if (lines[i].includes('class="notif-list"') && !lines[i].includes('v-if') && !lines[i].includes('v-else') && !lines[i].includes('activity')) {
    // Check if next line has v-for with "n in filtered" — that's the alerts list
    if (i + 1 < lines.length && lines[i + 1].includes('n in filtered')) {
      lines[i] = lines[i].replace('<div class="notif-list">', '<div v-if="activeTab===\'alerts\' && filtered.length" class="notif-list">');
      changed = true;
    }
  }
}

// Also fix the notif-empty: remove the original v-if="!filtered.length" since we replaced it
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes("v-if=\"activeTab==='alerts' && !filtered.length\"") && lines[i].includes('v-if="!filtered.length"')) {
    // Double v-if — fix
    lines[i] = lines[i].replace('v-if="!filtered.length" ', '');
  }
}

if (changed) {
  fs.writeFileSync(f, lines.join('\n'), 'utf8');
  console.log('DONE: wrapped alerts content in v-if activeTab');
} else {
  console.log('SKIP: already wrapped or not found');
}