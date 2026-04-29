const fs = require('fs');

function patchJson(file, lang) {
  let c = fs.readFileSync(file, 'utf8');
  if (c.includes('tabAlerts')) {
    console.log('SKIP ' + file + ': already patched');
    return;
  }

  const keys = lang === 'en' ? {
    tabAlerts: 'Alerts',
    tabActivity: 'Activity Log',
    refresh: 'Refresh',
    clearAll: 'Clear All',
    loadingActivity: 'Loading activity...',
    noActivity: 'No recent activity',
    noActivitySub: 'Actions will appear here as they happen.'
  } : {
    tabAlerts: 'Ειδοποιήσεις',
    tabActivity: 'Ιστορικό Ενεργειών',
    refresh: 'Ανανέωση',
    clearAll: 'Καθαρισμός',
    loadingActivity: 'Φόρτωση ενεργειών...',
    noActivity: 'Δεν υπάρχουν πρόσφατες ενέργειες',
    noActivitySub: 'Οι ενέργειες θα εμφανίζονται εδώ καθώς πραγματοποιούνται.'
  };

  // Find the closing of notif block and add keys before the last }
  // Insert after "filterInfo" line
  const marker = '"filterInfo"';
  const idx = c.indexOf(marker);
  if (idx < 0) {
    console.error('FAIL: cannot find filterInfo in ' + file);
    return;
  }
  // Find end of that line
  const lineEnd = c.indexOf('\n', idx);
  const insertPoint = lineEnd;

  let insert = '';
  for (const [k, v] of Object.entries(keys)) {
    insert += ',\n    "' + k + '": "' + v + '"';
  }

  c = c.slice(0, insertPoint) + insert + c.slice(insertPoint);
  fs.writeFileSync(file, c, 'utf8');
  console.log('DONE: ' + file + ' patched with activity i18n keys');
}

patchJson('frontend/src/i18n/en.json', 'en');
patchJson('frontend/src/i18n/el.json', 'el');