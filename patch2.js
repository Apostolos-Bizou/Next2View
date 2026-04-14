const fs = require('fs');
const file = 'C:\\Users\\akage\\Next2View\\backend\\src\\main\\java\\com\\next2me\\next2view\\service\\AuthService.java';
let c = fs.readFileSync(file, 'utf8');

// Find and replace the MFA block with regex
c = c.replace(
  /if \(user\.getMfaEnabled\(\) && user\.getRole\(\) == User\.Role\.CEO\) \{[\s\S]*?\}/,
  `if (user.getMfaEnabled()) {
            if (request.mfaCode() == null || request.mfaCode().isBlank()) {
                return new AuthResponse(null, null, 0, buildUserInfo(user), true);
            }
            boolean validCode = verifyTotp(user.getMfaSecret(), request.mfaCode());
            if (!validCode) {
                throw new BadCredentialsException("Invalid MFA code");
            }
        }`
);

fs.writeFileSync(file, c, 'utf8');
console.log('OK');