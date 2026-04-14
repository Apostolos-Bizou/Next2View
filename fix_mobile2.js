const fs = require('fs');

// 1. Fix main.css - remove the * max-width that breaks tables
let css = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\assets\\main.css', 'utf8');
css = css.replace(/\r\n/g, '\n');
const oldStar = `* {\n  max-width: 100%;\n}`;
if (css.includes(oldStar)) {
  css = css.replace(oldStar, '/* max-width removed - breaks tables */');
  console.log('removed * max-width');
}
fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\assets\\main.css', css, 'utf8');

// 2. Fix AdminView - add mobile styles
let admin = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\AdminView.vue', 'utf8');
admin = admin.replace(/\r\n/g, '\n');
const oldAdminStyle = `</style>`;
const newAdminStyle = `@media (max-width: 768px) {
  .admin-content { padding: 14px 12px; overflow-x: hidden; }
  .admin-table { display: none !important; }
  .users-table { width: 100%; display: block; overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .panel { overflow-x: hidden; }
  .form-row { grid-template-columns: 1fr !important; }
  .modal { width: 95vw !important; max-width: 95vw !important; }
  .modal-body { padding: 14px !important; }
}
</style>`;
// Only replace the LAST </style>
const lastIdx = admin.lastIndexOf('</style>');
admin = admin.substring(0, lastIdx) + newAdminStyle;
fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\AdminView.vue', admin, 'utf8');
console.log('AdminView mobile styles added');

// 3. Fix ProjectDetailView - fix contract-stats grid
let detail = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectDetailView.vue', 'utf8');
detail = detail.replace(/\r\n/g, '\n');
const oldDetailMedia = `@media (max-width: 768px) {
  .project-detail { padding: 12px; }`;
const newDetailMedia = `@media (max-width: 768px) {
  .content { padding: 12px !important; overflow-x: hidden !important; }
  .project-detail { padding: 12px; }
  .contract-stats { grid-template-columns: 1fr 1fr !important; }
  .fin-grid { grid-template-columns: 1fr 1fr !important; }
  .modal { width: 95vw !important; max-width: 95vw !important; }
  .modal-body { padding: 14px !important; }
  .form-row { grid-template-columns: 1fr !important; }`;
if (!detail.includes(oldDetailMedia)) { console.log('ERROR: detail media not found'); process.exit(1); }
detail = detail.replace(oldDetailMedia, newDetailMedia);
fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectDetailView.vue', detail, 'utf8');
console.log('ProjectDetailView mobile styles fixed');

console.log('ALL DONE');
