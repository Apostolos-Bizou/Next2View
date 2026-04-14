const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Fix onMounted - add loadMyPermissions
const oldMounted = `onMounted(async () => {
  await Promise.all([store.fetchProjects(), store.fetchCompanies()])
})`;
const newMounted = `onMounted(async () => {
  await Promise.all([store.fetchProjects(), store.fetchCompanies(), permStore.loadMyPermissions()])
})`;
if (!c.includes(oldMounted)) { console.log('ERROR: onMounted not found'); process.exit(1); }
c = c.replace(oldMounted, newMounted);

// 2. Hide Admin link - CEO only
const oldAdmin = `        <router-link to="/admin" class="nav-item" active-class="active">`;
const newAdmin = `        <router-link v-if="permStore.isCEO()" to="/admin" class="nav-item" active-class="active">`;
if (!c.includes(oldAdmin)) { console.log('ERROR: admin link not found'); process.exit(1); }
c = c.replace(oldAdmin, newAdmin);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('OK - onMounted + admin fixed');
