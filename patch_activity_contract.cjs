const fs = require('fs');
const f = 'backend/src/main/java/com/next2me/next2view/service/ContractFileService.java';
let lines = fs.readFileSync(f, 'utf8').split(/\r?\n/);

if (lines.some(l => l.includes('ActivityLogService'))) {
  console.log('SKIP: already patched');
  process.exit(0);
}

// 1. Add import
const impIdx = lines.findIndex(l => l.includes('import com.next2me.next2view.repository.ContractFileRepository;'));
if (impIdx < 0) { console.error('FAIL: cannot find ContractFileRepository import'); process.exit(1); }
lines.splice(impIdx, 0, 'import com.next2me.next2view.service.ActivityLogService;');

// 2. Add field
const fieldIdx = lines.findIndex(l => l.includes('private final AuditLogRepository auditLogRepository;'));
if (fieldIdx < 0) { console.error('FAIL: cannot find auditLogRepository field'); process.exit(1); }
lines.splice(fieldIdx + 1, 0, '    private final ActivityLogService activityLogService;');

// 3. After upload audit -> add activity
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('.action("CONTRACT_UPLOAD_ENCRYPTED")')) {
    let close = i;
    while (close < lines.length && !lines[close].trim().startsWith('.build())')) close++;
    if (close < lines.length) {
      lines.splice(close + 1, 0, '',
        '        activityLogService.logActivity(uploader, ActivityLogService.UPLOADED, ActivityLogService.FILE,',
        '                cf.getId(), sanitizedFilename, project.getCategory().name(),',
        '                project.getCompany().getId(), uploader.getFullName() + " uploaded file \\"" + sanitizedFilename + "\\"");'
      );
    }
    break;
  }
}

// 4. After soft-delete audit -> add activity
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('.action("CONTRACT_SOFT_DELETE")')) {
    let close = i;
    while (close < lines.length && !lines[close].trim().startsWith('.build())')) close++;
    if (close < lines.length) {
      lines.splice(close + 1, 0, '',
        '        activityLogService.logActivity(deleter, ActivityLogService.DELETED, ActivityLogService.FILE,',
        '                cf.getId(), cf.getFileName(), null,',
        '                null, deleter.getFullName() + " deleted file \\"" + cf.getFileName() + "\\"");'
      );
    }
    break;
  }
}

fs.writeFileSync(f, lines.join('\n'), 'utf8');
console.log('DONE: ContractFileService patched with activity logging');