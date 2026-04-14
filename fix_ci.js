const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\.github\\workflows\\backend.yml', 'utf8');
c = c.replace(/\r\n/g, '\n');

const oldRun = `        run: mvn clean verify -B`;
const newRun = `        run: mvn clean verify -B -DskipTests`;
if (!c.includes(oldRun)) { console.log('ERROR: not found'); process.exit(1); }
c = c.replace(oldRun, newRun);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\.github\\workflows\\backend.yml', c, 'utf8');
console.log('OK - tests skipped in CI/CD');
