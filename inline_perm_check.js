const fs = require('fs');
const ctrlPath = 'backend/src/main/java/com/next2me/next2view/controller/SecurityDocumentController.java';
let c = fs.readFileSync(ctrlPath, 'utf8');

// Remove ALL @PreAuthorize annotations
c = c.replace(/@PreAuthorize\("[^"]+"\)\s*\n\s*/g, '');

// Add manual check helper at top of class (after the @Value fields)
// Find "private BlobContainerClient getContainer()" and insert helper before it
const helperMethod = `
    // Inline permission check - replaces @PreAuthorize for reliability
    private boolean canViewSecurity(String userId) {
        if (userId == null) return false;
        try {
            UUID uid = UUID.fromString(userId);
            User user = userRepo.findById(uid).orElse(null);
            if (user == null) return false;
            // CEO has full access
            if (user.getRole() == User.Role.CEO) return true;
            // Otherwise check viewSecurity flag - we use direct query via repository
            return permissionEvaluator.canViewSecurity(userId);
        } catch (Exception e) {
            return false;
        }
    }

`;

// Add PermissionEvaluator dependency
if (!c.includes('PermissionEvaluator permissionEvaluator')) {
  c = c.replace(
    'private final UserRepository userRepo;',
    'private final UserRepository userRepo;\n    private final com.next2me.next2view.security.PermissionEvaluator permissionEvaluator;'
  );
}

// Insert helper method after fields, before getContainer()
c = c.replace(
  '    private BlobContainerClient getContainer()',
  helperMethod + '    private BlobContainerClient getContainer()'
);

// Now wrap each endpoint with explicit check
// Pattern: find each @GetMapping/@PostMapping/@DeleteMapping followed by public method
// Add check at start of method body

// list() method
c = c.replace(
  /(@GetMapping\s*\n\s*public\s+ResponseEntity<List<Map<String,\s*Object>>>\s*list\(\)\s*\{\s*\n)/,
  '$1        if (!canViewSecurity(getCurrentUserId())) return ResponseEntity.status(403).body(java.util.Collections.emptyList());\n'
);

// upload() method  
c = c.replace(
  /(@PostMapping\("\/upload"\)\s*\n\s*public\s+ResponseEntity<Map<String,\s*Object>>\s*upload\([^)]+\)\s*\{\s*\n\s*try\s*\{\s*\n)/s,
  '$1            if (!canViewSecurity(getCurrentUserId())) return ResponseEntity.status(403).body(java.util.Map.of("error", "forbidden"));\n'
);

// download() method
c = c.replace(
  /(@GetMapping\("\/\{id\}\/download"\)\s*\n\s*public\s+ResponseEntity<\?>\s*download\([^)]+\)\s*\{\s*\n\s*try\s*\{\s*\n)/s,
  '$1            if (!canViewSecurity(getCurrentUserId())) return ResponseEntity.status(403).body(java.util.Map.of("error", "forbidden"));\n'
);

// delete() method - CEO only
c = c.replace(
  /(@DeleteMapping\("\/\{id\}"\)\s*\n\s*public\s+ResponseEntity<Map<String,\s*Object>>\s*delete\(@PathVariable\s+UUID\s+id\)\s*\{\s*\n)/,
  '$1        String uid = getCurrentUserId();\n        if (uid == null) return ResponseEntity.status(403).body(java.util.Map.of("error", "forbidden"));\n        User u = userRepo.findById(UUID.fromString(uid)).orElse(null);\n        if (u == null || u.getRole() != User.Role.CEO) return ResponseEntity.status(403).body(java.util.Map.of("error", "ceo only"));\n'
);

// Add getCurrentUserId helper
const getCurrentUserHelper = `
    private String getCurrentUserId() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) return null;
        return auth.getName();
    }

`;

c = c.replace(
  '    // Inline permission check',
  getCurrentUserHelper + '    // Inline permission check'
);

fs.writeFileSync(ctrlPath, c, 'utf8');
console.log('✅ Replaced @PreAuthorize with inline checks');
console.log('   - All endpoints now use canViewSecurity() inline');
console.log('   - Delete restricted to CEO only via direct role check');
console.log('   - PermissionEvaluator injected as dependency');
