const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', 'utf8');
const lines = c.split('\n');
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('@media') || lines[i].includes('.g2') || 
      lines[i].includes('.g4') || lines[i].includes('grid') ||
      lines[i].includes('kpi-strip')) {
    console.log(`L${i+1}: ${lines[i]}`);
  }
}
