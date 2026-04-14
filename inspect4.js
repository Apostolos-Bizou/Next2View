const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
const lines = c.split('\n');
for (let i = 505; i < 520; i++) {
  console.log(`L${i+1}: ${lines[i]}`);
}
