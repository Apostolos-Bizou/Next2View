const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectsView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

const oldMedia = `@media (max-width: 768px) {
  .projects-content { padding: 14px 12px; }
  .projects-table { display: none; }
  .projects-cards { display: flex !important; flex-direction: column; gap: 12px; }`;

const newMedia = `@media (max-width: 768px) {
  .content { padding: 14px 10px !important; }
  .panel { overflow-x: auto !important; -webkit-overflow-scrolling: touch; }
  .pb { padding: 0 10px !important; overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .proj-tbl { min-width: 700px; }
  .ph { flex-wrap: wrap; gap: 8px; padding: 12px 10px !important; }
  .ph-title { font-size: 14px !important; width: 100%; }
  .ph-select { font-size: 11px !important; }
  .projects-cards { display: flex !important; flex-direction: column; gap: 12px; }`;

if (!c.includes(oldMedia)) { console.log('ERROR: media not found'); process.exit(1); }
c = c.replace(oldMedia, newMedia);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectsView.vue', c, 'utf8');
console.log('OK - ProjectsView horizontal scroll added');
