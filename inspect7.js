const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
const lines = c.split('\n');
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('Full Access') || lines[i].includes('sidebar-user') || lines[i].includes('user-role')) {
    console.log(`L${i+1}: ${lines[i]}`);
  }
}
