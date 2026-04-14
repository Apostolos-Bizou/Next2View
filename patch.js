const fs = require('fs');
const file = 'C:\\Users\\akage\\Next2View\\frontend\\src\\views\\LoginView.vue';
let c = fs.readFileSync(file, 'utf8');

c = c.replace(
  '      <div class="login-footer">',
  `      <div class="forgot-link" @click="router.push('/forgot-password')">Ξέχασες τον κωδικό;</div>
      <div class="login-footer">`
);

// Add router import
c = c.replace(
  'import { useAuthStore } from "@/stores/auth";',
  'import { useAuthStore } from "@/stores/auth";\nimport { useRouter } from "vue-router";'
);

// Add router const (after existing consts)
c = c.replace(
  'const router = useRouter();',
  'const router = useRouter();'
);

// Add style
c = c.replace(
  '.login-footer {',
  `.forgot-link { text-align:center; margin-top:12px; font-size:12px; color:var(--accent); cursor:pointer; font-weight:600; }
.forgot-link:hover { text-decoration:underline; }
.login-footer {`
);

fs.writeFileSync(file, c, 'utf8');
console.log('OK - forgot link: ' + (c.includes('forgot-password') ? 'YES' : 'NO'));