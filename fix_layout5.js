const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Hide AI Report button
const oldAI = `<button class="btn-sidebar btn-ai" @click="openAiReport">`;
const newAI = `<button v-if="permStore.isCEO() || permStore.can('aiCeoReport')" class="btn-sidebar btn-ai" @click="openAiReport">`;
if (!c.includes(oldAI)) { console.log('ERROR: AI button not found'); process.exit(1); }
c = c.replace(oldAI, newAI);

// 2. Hide New Project button
const oldNP = `<button class="btn-sidebar" @click="openNewProject">+ New Project</button>`;
const newNP = `<button v-if="permStore.isCEO() || permStore.can('createProject')" class="btn-sidebar" @click="openNewProject">+ New Project</button>`;
if (!c.includes(oldNP)) { console.log('ERROR: New Project button not found'); process.exit(1); }
c = c.replace(oldNP, newNP);

// 3. Hide + Company / + User buttons
const oldCU = `<div style="display:grid;grid-template-columns:1fr 1fr;gap:6px;">
          <button class="btn-sidebar" style="font-size:9px;" @click="openNewCompany">+ Company</button>
          <button class="btn-sidebar" style="font-size:9px;" @click="openNewUser">+ User</button>
        </div>`;
const newCU = `<div v-if="permStore.isCEO()" style="display:grid;grid-template-columns:1fr 1fr;gap:6px;">
          <button class="btn-sidebar" style="font-size:9px;" @click="openNewCompany">+ Company</button>
          <button class="btn-sidebar" style="font-size:9px;" @click="openNewUser">+ User</button>
        </div>`;
if (!c.includes(oldCU)) { console.log('ERROR: Company/User buttons not found'); process.exit(1); }
c = c.replace(oldCU, newCU);

// 4. Fix + New Project button in top right header
const oldNPHeader = `<button class="btn-new-project" @click="openNewProject">+ New Project</button>`;
const newNPHeader = `<button v-if="permStore.isCEO() || permStore.can('createProject')" class="btn-new-project" @click="openNewProject">+ New Project</button>`;
if (!c.includes(oldNPHeader)) { console.log('WARN: header New Project button not found - skipping'); }
else c = c.replace(oldNPHeader, newNPHeader);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('OK - all buttons hidden for non-CEO');
