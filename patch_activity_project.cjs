const fs = require('fs');
const f = 'backend/src/main/java/com/next2me/next2view/service/ProjectService.java';
let lines = fs.readFileSync(f, 'utf8').split(/\r?\n/);

if (lines.some(l => l.includes('ActivityLogService'))) {
  console.log('SKIP: already patched');
  process.exit(0);
}

// 1. Add import
const importIdx = lines.findIndex(l => l.includes('import com.next2me.next2view.repository.*'));
if (importIdx < 0) { console.error('FAIL: cannot find repository import'); process.exit(1); }
lines.splice(importIdx + 1, 0, 'import com.next2me.next2view.service.ActivityLogService;');

// 2. Add field
const fieldIdx = lines.findIndex(l => l.includes('private final AuditLogRepository auditLogRepository;'));
if (fieldIdx < 0) { console.error('FAIL: cannot find auditLogRepository field'); process.exit(1); }
lines.splice(fieldIdx + 1, 0, '    private final ActivityLogService activityLogService;');

// 3. After CREATE audit log save -> add activity log
const createAuditIdx = lines.findIndex((l, i) => l.includes('.action("CREATE")') && lines[i-1] && lines[i-1].includes('.userEmail(actorEmail)'));
if (createAuditIdx >= 0) {
  // Find the closing ");" of that auditLogRepository.save block
  let closeIdx = createAuditIdx;
  while (closeIdx < lines.length && !lines[closeIdx].trim().startsWith('.build())')) closeIdx++;
  if (closeIdx < lines.length) {
    lines.splice(closeIdx + 1, 0, '',
      '        activityLogService.logActivity(actor, ActivityLogService.CREATED, ActivityLogService.PROJECT,',
      '                p.getId(), p.getTitle(), p.getCategory().name(),',
      '                p.getCompany().getId(), actor.getFullName() + " created project \'" + p.getTitle() + "\'");'
    );
  }
}

// 4. After UPDATE audit log save -> add activity log
const updateAuditIdx = lines.findIndex((l, i) => l.includes('.action("UPDATE")') && lines[i+1] && lines[i+1].includes('.entityType("projects")'));
if (updateAuditIdx >= 0) {
  let closeIdx = updateAuditIdx;
  while (closeIdx < lines.length && !lines[closeIdx].trim().startsWith('.build())')) closeIdx++;
  if (closeIdx < lines.length) {
    lines.splice(closeIdx + 1, 0, '',
      '        activityLogService.logActivity(actor, ActivityLogService.UPDATED, ActivityLogService.PROJECT,',
      '                p.getId(), p.getTitle(), p.getCategory().name(),',
      '                p.getCompany().getId(), actor.getFullName() + " updated project \'" + p.getTitle() + "\'");'
    );
  }
}

// 5. After DELETE audit log save -> add activity log
const deleteAuditIdx = lines.findIndex((l, i) => l.includes('.action("DELETE")') && lines[i+1] && lines[i+1].includes('.entityType("projects")'));
if (deleteAuditIdx >= 0) {
  let closeIdx = deleteAuditIdx;
  while (closeIdx < lines.length && !lines[closeIdx].trim().startsWith('.build())')) closeIdx++;
  if (closeIdx < lines.length) {
    lines.splice(closeIdx + 1, 0, '',
      '        activityLogService.logActivity(actor, ActivityLogService.DELETED, ActivityLogService.PROJECT,',
      '                p.getId(), p.getTitle(), p.getCategory().name(),',
      '                p.getCompany().getId(), actor.getFullName() + " deleted project \'" + p.getTitle() + "\'");'
    );
  }
}

fs.writeFileSync(f, lines.join('\n'), 'utf8');
console.log('DONE: ProjectService patched with activity logging (create/update/delete)');