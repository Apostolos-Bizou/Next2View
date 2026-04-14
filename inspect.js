const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// Print exact line 43 to see what we have
const lines = c.split('\n');
console.log('Line 43 exact:', JSON.stringify(lines[42]));
console.log('Line 65 exact:', JSON.stringify(lines[64]));
