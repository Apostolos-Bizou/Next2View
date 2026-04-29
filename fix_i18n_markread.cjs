const fs = require('fs');

function patch(file, value) {
  let c = fs.readFileSync(file, 'utf8');
  if (c.includes('markAllRead')) { console.log('SKIP ' + file); return; }
  c = c.replace('"clearAll"', '"clearAll": "' + (file.includes('el') ? 'Καθαρισμός' : 'Clear All') + '",\n    "markAllRead"');
  // Actually simpler — add after noActivitySub
  c = c.replace('"noActivitySub"', '"markAllRead": "' + value + '",\n    "noActivitySub"');
  fs.writeFileSync(file, c, 'utf8');
  console.log('DONE: ' + file);
}

patch('frontend/src/i18n/en.json', 'Mark all read');
patch('frontend/src/i18n/el.json', 'Σημείωση ως αναγνωσμένα');