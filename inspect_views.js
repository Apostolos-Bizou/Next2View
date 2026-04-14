const fs = require('fs');
const files = [
  'ProjectsView.vue',
  'ProjectDetailView.vue', 
  'AdminView.vue',
  'NotificationsView.vue'
];
files.forEach(f => {
  let c = fs.readFileSync(`C:\\Users\\akage\\Next2View\\frontend\\src\\views\\${f}`, 'utf8');
  const lines = c.split('\n');
  let inMedia = false;
  console.log(`\n=== ${f} ===`);
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('@media')) { inMedia = true; }
    if (inMedia) {
      console.log(`L${i+1}: ${lines[i]}`);
      if (lines[i].includes('}') && inMedia && i > 0) { 
        // count to find end of media block
      }
    }
  }
});
