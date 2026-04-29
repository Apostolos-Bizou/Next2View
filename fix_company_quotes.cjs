const fs = require('fs');
const f = 'backend/src/main/java/com/next2me/next2view/service/CompanyService.java';
let lines = fs.readFileSync(f, 'utf8').split(/\r?\n/);

for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('getFullName()') && lines[i].includes('company')) {
    // Replace: actor.getFullName() + " created company \\"" + c.getName() + "\\"")
    // With:    actor.getFullName() + " created company '" + c.getName() + "'")
    lines[i] = lines[i]
      .replace(/\\\\""/g, "'\"")
      .replace(/ \+ "\\\\"/g, " + \"'");
  }
}

fs.writeFileSync(f, lines.join('\n'), 'utf8');
console.log('DONE: fixed quotes in CompanyService');