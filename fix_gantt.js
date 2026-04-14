const fs = require('fs');
const reqPath = 'C:/Users/akage/Next2View/backend/src/main/java/com/next2me/next2view/dto/ProjectRequest.java';
let r = fs.readFileSync(reqPath, 'utf8').replace(/\r\n/g, '\n');

r = r.replace(
  'LocalDate deadline, Integer startWeek, Integer durationWeeks, Integer startDay, Integer durationDays, Boolean manualProgress,',
  'LocalDate deadline, Integer startWeek, Integer durationWeeks, Integer startDay, Integer durationDays, Boolean manualProgress, LocalDate endDate,'
);

console.log('endDate added:', r.includes('LocalDate endDate'));
fs.writeFileSync(reqPath, r, 'utf8');
console.log('Done');
