const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

const oldRole = `          <div class="user-role">CEO · Full Access</div>`;
const newRole = `          <div class="user-role">{{ auth.user?.role === 'CEO' ? 'CEO · Full Access' : auth.user?.role || 'Viewer' }}</div>`;
if (!c.includes(oldRole)) { console.log('ERROR: user-role not found'); process.exit(1); }
c = c.replace(oldRole, newRole);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardLayout.vue', c, 'utf8');
console.log('OK - user role label fixed');
