const { execSync } = require('child_process');
try { require('bcryptjs'); } catch(e) { execSync('npm install bcryptjs', {cwd: 'C:\\Users\\akage\\Next2View'}); }
const bcrypt = require('bcryptjs');
const hash = bcrypt.hashSync('Test@2026!', 12);
console.log(hash);
