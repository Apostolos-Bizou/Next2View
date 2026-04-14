const fs = require('fs');
const file = 'C:\\Users\\akage\\Next2View\\backend\\src\\main\\java\\com\\next2me\\next2view\\service\\AuthService.java';
let c = fs.readFileSync(file, 'utf8');

// Remove the duplicate closing brace
c = c.replace(
  '            }\n        }\n        }\n        user.setFailedAttempts(0);',
  '            }\n        }\n        user.setFailedAttempts(0);'
);

fs.writeFileSync(file, c, 'utf8');
console.log('OK');