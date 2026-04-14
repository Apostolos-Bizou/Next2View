const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

const oldMedia = `@media (max-width: 768px) {
  .content { padding: 16px 12px; }
  .kpi-strip { grid-template-columns: repeat(2, 1fr); gap: 8px; margin-bottom: 14px; }`;

const newMedia = `@media (max-width: 768px) {
  .content { padding: 16px 12px; }
  .kpi-strip { grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px; }
  .g2 { grid-template-columns: 1fr !important; gap: 10px; }
  .g4 { grid-template-columns: 1fr !important; gap: 10px; }
  .panel { margin-bottom: 10px; }
  .kpi { padding: 16px 12px; }
  .kpi-val { font-size: 28px; }`;

if (!c.includes(oldMedia)) { console.log('ERROR: media not found'); process.exit(1); }
c = c.replace(oldMedia, newMedia);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', c, 'utf8');
console.log('OK - dashboard mobile single column fixed');
