const fs = require('fs');
const f = 'backend/src/main/java/com/next2me/next2view/service/CompanyService.java';
let lines = fs.readFileSync(f, 'utf8').split(/\r?\n/);

if (lines.some(l => l.includes('ActivityLogService'))) {
  console.log('SKIP: already patched');
  process.exit(0);
}

// 1. Add import
const impIdx = lines.findIndex(l => l.includes('import com.next2me.next2view.repository.CompanyRepository;'));
if (impIdx < 0) { console.error('FAIL: cannot find CompanyRepository import'); process.exit(1); }
lines.splice(impIdx, 0, 'import com.next2me.next2view.service.ActivityLogService;');

// 2. Add fields: activityLogService + userRepository
const fieldIdx = lines.findIndex(l => l.includes('private final AuditLogRepository auditLogRepository;'));
if (fieldIdx < 0) { console.error('FAIL: cannot find auditLogRepository field'); process.exit(1); }
lines.splice(fieldIdx + 1, 0,
  '    private final ActivityLogService activityLogService;',
  '    private final com.next2me.next2view.repository.UserRepository userRepository;'
);

// Helper: find actor User from email
function addActivityAfterAudit(actionStr, logConst, descVerb) {
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('.action("' + actionStr + '")') && 
        i + 1 < lines.length && lines[i+1].includes('.entityType("companies")')) {
      let close = i;
      while (close < lines.length && !lines[close].trim().startsWith('.build())')) close++;
      if (close >= lines.length) return;
      lines.splice(close + 1, 0, '',
        '        // Activity log',
        '        userRepository.findByEmail(actorEmail).ifPresent(actor ->',
        '            activityLogService.logActivity(actor, ActivityLogService.' + logConst + ', ActivityLogService.COMPANY,',
        '                c.getId(), c.getName(), null,',
        '                null, actor.getFullName() + " ' + descVerb + ' company \\\\"" + c.getName() + "\\\\"")',
        '        );'
      );
      return;
    }
  }
}

addActivityAfterAudit('DELETE', 'DELETED', 'deleted');
addActivityAfterAudit('UPDATE', 'UPDATED', 'updated');
addActivityAfterAudit('CREATE', 'CREATED', 'created');

fs.writeFileSync(f, lines.join('\n'), 'utf8');
console.log('DONE: CompanyService patched with activity logging');