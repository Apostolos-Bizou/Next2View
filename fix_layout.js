const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Add permStore import - find existing imports in script setup
const oldImport = `import { useProjectStore } from '@/stores/projects'`;
const newImport = `import { useProjectStore } from '@/stores/projects'
import { usePermissionStore } from '@/stores/permissions'`;
if (!c.includes(oldImport)) { console.log('ERROR: import not found'); process.exit(1); }
c = c.replace(oldImport, newImport);

// 2. Add permStore instance - find existing store instance
const oldStore = `const store = useProjectStore()`;
const newStore = `const store = useProjectStore()
const permStore = usePermissionStore()`;
if (!c.includes(oldStore)) { console.log('ERROR: store not found'); process.exit(1); }
c = c.replace(oldStore, newStore);

// 3. Finance nav-item - add v-if
const oldFinance = `<div :class="['nav-item', route.query.category==='finance' ? 'active' : '']"
          @click="router.push('/projects?category=finance')">`;
const newFinance = `<div v-if="permStore.canViewCategory('finance')" :class="['nav-item', route.query.category==='finance' ? 'active' : '']"
          @click="router.push('/projects?category=finance')">`;
if (!c.includes(oldFinance)) { console.log('ERROR: finance not found'); process.exit(1); }
c = c.replace(oldFinance, newFinance);

// 4. Legal nav-item - add v-if
const oldLegal = `<div :class="['nav-item', route.query.category==='legal' ? 'active' : '']"
          @click="router.push('/projects?category=legal')">`;
const newLegal = `<div v-if="permStore.canViewCategory('legal')" :class="['nav-item', route.query.category==='legal' ? 'active' : '']"
          @click="router.push('/projects?category=legal')">`;
if (!c.includes(oldLegal)) { console.log('ERROR: legal not found'); process.exit(1); }
c = c.replace(oldLegal, newLegal);

// 5. Dev nav-item - add v-if
const oldDev = `<div :class="['nav-item', route.query.category==='dev' ? 'active' : '']"
          @click="router.push('/projects?category=dev')">`;
const newDev = `<div v-if="permStore.canViewCategory('dev')" :class="['nav-item', route.query.category==='dev' ? 'active' : '']"
          @click="router.push('/projects?category=dev')">`;
if (!c.includes(oldDev)) { console.log('ERROR: dev not found'); process.exit(1); }
c = c.replace(oldDev, newDev);

// 6. Marketing nav-item - add v-if
const oldMarketing = `<div :class="['nav-item', route.query.category==='marketing' ? 'active' : '']"
          @click="router.push('/projects?category=marketing')">`;
const newMarketing = `<div v-if="permStore.canViewCategory('marketing')" :class="['nav-item', route.query.category==='marketing' ? 'active' : '']"
          @click="router.push('/projects?category=marketing')">`;
if (!c.includes(oldMarketing)) { console.log('ERROR: marketing not found'); process.exit(1); }
c = c.replace(oldMarketing, newMarketing);

// 7. Companies v-for - filter by permission
const oldCo = `v-for="co in store.companies" :key="co.id"`;
const newCo = `v-for="co in store.companies.filter(co => store.projects.some(p => p.companyId === co.id && permStore.canViewCategory(p.category)))" :key="co.id"`;
if (!c.includes(oldCo)) { console.log('ERROR: companies v-for not found'); process.exit(1); }
c = c.replace(oldCo, newCo);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('DashboardLayout.vue OK - all 7 replacements done');
