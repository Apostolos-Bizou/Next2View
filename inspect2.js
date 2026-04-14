const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

const lines = c.split('\n');
console.log('Line 39:', JSON.stringify(lines[38]));
console.log('Line 40:', JSON.stringify(lines[39]));
console.log('Line 41:', JSON.stringify(lines[40]));
