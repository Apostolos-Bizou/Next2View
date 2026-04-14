const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

const oldBtn = `<button class="topbar-btn" @click="openNewProject">+ New Project</button>`;
const newBtn = `<button v-if="permStore.isCEO() || permStore.can('createProject')" class="topbar-btn" @click="openNewProject">+ New Project</button>`;
if (!c.includes(oldBtn)) { console.log('ERROR: topbar button not found'); process.exit(1); }
c = c.replace(oldBtn, newBtn);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('OK - topbar New Project button hidden');
