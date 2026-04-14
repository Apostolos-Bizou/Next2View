const fs = require('fs');
const file = 'C:\\Users\\akage\\Next2View\\backend\\src\\main\\java\\com\\next2me\\next2view\\service\\AuthService.java';
let lines = fs.readFileSync(file, 'utf8').split('\n');

// Find and remove the lone } after the MFA block
for (let i = 0; i < lines.length; i++) {
  if (lines[i].trim() === '}' && 
      lines[i-1] && lines[i-1].trim() === '}' &&
      lines[i+1] && lines[i+1].trim() === 'user.setFailedAttempts(0);') {
    lines.splice(i, 1);
    console.log('Removed duplicate } at line ' + (i+1));
    break;
  }
}

fs.writeFileSync(file, lines.join('\n'), 'utf8');
console.log('OK');