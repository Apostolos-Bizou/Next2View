const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
const lines = c.split('\n');
// Find sidebar CSS and mobile styles
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('sidebar-open') || lines[i].includes('sidebar {') || 
      lines[i].includes('@media') || lines[i].includes('sidebarOpen') ||
      lines[i].includes('position:') || lines[i].includes('position :') ||
      lines[i].includes('z-index') || lines[i].includes('transform')) {
    console.log(`L${i+1}: ${lines[i]}`);
  }
}
