const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// Fix All Projects - exact match only (no query params)
const oldProjects = `<router-link to="/projects" class="nav-item" active-class="active">`;
const newProjects = `<router-link to="/projects" class="nav-item" :class="route.path==='/projects' && !route.query.category && !route.query.companyId ? 'active' : ''">`;
if (!c.includes(oldProjects)) { console.log('ERROR: projects link not found'); process.exit(1); }
c = c.replace(oldProjects, newProjects);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('OK - active state fixed');
