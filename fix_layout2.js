const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Hide ΚΑΤΗΓΟΡΙΑ label if no categories visible
const oldCatLabel = `<div class="nav-section" style="margin-top:6px;">ΞΞ±Ο„Ξ·Ξ³ΞΏΟΞ―Ξ±</div>`;
const newCatLabel = `<div v-if="['finance','legal','dev','marketing'].some(cat => permStore.canViewCategory(cat))" class="nav-section" style="margin-top:6px;">Κατηγορία</div>`;
if (!c.includes(oldCatLabel)) { console.log('ERROR: cat label not found'); process.exit(1); }
c = c.replace(oldCatLabel, newCatLabel);

// 2. Hide ΕΤΑΙΡΕΙΕΣ label if no companies visible
const oldCoLabel = `<div class="nav-section" style="margin-top:6px;">Ξ•Ο„Ξ±ΞΉΟΞµΞ―ΞµΟ‚</div>`;
const newCoLabel = `<div v-if="store.companies.some(co => store.projects.some(p => p.companyId === co.id && permStore.canViewCategory(p.category)))" class="nav-section" style="margin-top:6px;">Εταιρείες</div>`;
if (!c.includes(oldCoLabel)) { console.log('ERROR: co label not found'); process.exit(1); }
c = c.replace(oldCoLabel, newCoLabel);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('DashboardLayout.vue OK - labels fixed');
