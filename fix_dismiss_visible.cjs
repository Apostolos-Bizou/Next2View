const fs = require('fs');
const f = 'frontend/src/views/NotificationsView.vue';
let c = fs.readFileSync(f, 'utf8');

// 1. Remove opacity:0 from dismiss-btn — make always visible
c = c.replace(
  '.notif-card .dismiss-btn { position: absolute; top: 10px; right: 10px; background: none; border: 1px solid var(--border); border-radius: 5px; color: var(--text-dim); cursor: pointer; font-size: 11px; padding: 2px 7px; transition: all 0.15s; opacity: 0; }',
  '.notif-card .dismiss-btn { position: absolute; top: 12px; right: 12px; background: var(--surface2); border: 1px solid var(--border); border-radius: 6px; color: var(--text-dim); cursor: pointer; font-size: 12px; padding: 4px 8px; transition: all 0.15s; }'
);
c = c.replace('.notif-card:hover .dismiss-btn { opacity: 1; }', '');

// 2. Fix i18n — the t() fallback syntax needs fixing
c = c.replace(
  "t('notif.markAllRead') || 'Mark all read'",
  "t('notif.markAllRead', 'Mark all read')"
);

fs.writeFileSync(f, c, 'utf8');
console.log('DONE: dismiss button always visible + i18n fix');