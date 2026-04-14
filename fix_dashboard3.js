const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// Filter companies panel - show only companies with visible projects
const oldCo = `<div v-for="co in store.companies" :key="co.id" class="co-row"`;
const newCo = `<div v-for="co in store.companies.filter(co => visibleProjects.some(p => p.companyId === co.id))" :key="co.id" class="co-row"`;
if (!c.includes(oldCo)) { console.log('ERROR: co-row not found'); process.exit(1); }
c = c.replace(oldCo, newCo);

// Fix "4 entities" badge to show visible count
const oldBadge = `<div class="ph-badge badge blue">{{ store.companies.length }} entities</div>`;
const newBadge = `<div class="ph-badge badge blue">{{ store.companies.filter(co => visibleProjects.some(p => p.companyId === co.id)).length }} entities</div>`;
if (!c.includes(oldBadge)) { console.log('ERROR: badge not found'); process.exit(1); }
c = c.replace(oldBadge, newBadge);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', c, 'utf8');
console.log('DashboardView.vue OK - companies filtered');
