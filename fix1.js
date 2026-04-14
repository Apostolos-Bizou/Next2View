const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\stores\\permissions.js', 'utf8');
c = c.replace(/\r\n/g, '\n');

const oldReturn = `  return { myPerms, loadMyPermissions, can, isCEO }`;
const newReturn = `  function canViewCategory(category) {
    if (isCEO()) return true
    const map = {
      finance:   'viewFinance',
      legal:     'viewLegal',
      dev:       'viewDev',
      marketing: 'viewMarketing',
    }
    const flag = map[category]
    if (!flag) return true
    return can(flag)
  }

  return { myPerms, loadMyPermissions, can, isCEO, canViewCategory }`;

if (!c.includes(oldReturn)) {
  console.log('ERROR: old string not found!');
  process.exit(1);
}

c = c.replace(oldReturn, newReturn);
fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\stores\\permissions.js', c, 'utf8');
console.log('permissions.js OK');
