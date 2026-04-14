const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');
const lines = c.split('\n');
// Show lines around loadMyPermissions and store loading
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('loadMyPermissions') || lines[i].includes('onMounted') || lines[i].includes('loadAll') || lines[i].includes('fetchProjects') || lines[i].includes('admin')) {
    console.log(`L${i+1}: ${lines[i]}`);
  }
}
