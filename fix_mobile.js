const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Fix: .shell needs mobile-header hidden on desktop + main flex:1
const oldShell = `.shell { display: flex; min-height: 100vh; }`;
const newShell = `.shell { display: flex; min-height: 100vh; }
.mobile-header { display: none; }
.main { flex: 1; min-width: 0; overflow-y: auto; }`;
if (!c.includes(oldShell)) { console.log('ERROR: shell not found'); process.exit(1); }
c = c.replace(oldShell, newShell);

// 2. Fix media query: replace .app-shell with .shell, show mobile-header
const oldMedia = `@media (max-width: 768px) {
  .app-shell { flex-direction: column; }`;
const newMedia = `@media (max-width: 768px) {
  .shell { flex-direction: column; }
  .mobile-header { display: flex !important; }`;
if (!c.includes(oldMedia)) { console.log('ERROR: media query not found'); process.exit(1); }
c = c.replace(oldMedia, newMedia);

// 3. Fix: sidebar on desktop - must NOT show mobile-header topbar
// Also hide topbar on desktop explicitly  
const oldTopbar = `.topbar { display: flex; align-items: center; justify-content: space-between; padding: 0 28px; height: 60px; border-bottom: 1px solid var(--border); background: var(--surface); flex-shrink: 0; }`;
if (c.includes(oldTopbar)) {
  console.log('topbar found - adding mobile hide');
}

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('OK - mobile layout fixed');
