const fs = require('fs');
const f = 'frontend/src/views/DashboardLayout.vue';
let c = fs.readFileSync(f,'utf8');

if (c.includes('mfa-nudge-modal')) {
  console.log('[SKIP] MFA nudge already exists');
  process.exit(0);
}

// Find closing </aside> and add the modal before </main> closing
const mainClose = c.indexOf('<!-- \u2550\u2550\u2550\u2550 NEW PROJECT MODAL');
if (mainClose === -1) {
  console.log('[ERROR] Cannot find NEW PROJECT MODAL marker');
  process.exit(1);
}

const modal = `
    <!-- MFA NUDGE POPUP -->
    <div v-if="showMfaNudge" class="mfa-nudge-overlay" @click.self="showMfaNudge = false">
      <div class="mfa-nudge-modal">
        <button class="mfa-nudge-close" @click="showMfaNudge = false">\u2715</button>
        <div class="mfa-nudge-icon">\ud83d\udd10</div>
        <div class="mfa-nudge-greeting">\u0393\u03b5\u03b9\u03b1 \u03c3\u03bf\u03c5 {{ firstName }}!</div>
        <div class="mfa-nudge-body">
          <p>\u039b\u03bf\u03b9\u03c0\u03cc\u03bd, {{ firstName }}, <strong>\u03c0\u03c1\u03cc\u03c3\u03b5\u03be\u03b5 \u03ba\u03b1\u03bb\u03ac</strong>,</p>
          <p>\u03a3\u03b5 \u03c0\u03b1\u03c1\u03b1\u03ba\u03b1\u03bb\u03ce \u03c0\u03b1\u03b1\u03ac\u03c1\u03b1 \u03c0\u03bf\u03bb\u03cd, \u03ad\u03bc\u03c0\u03b1 \u03c3\u03c4\u03bf <strong>Guide</strong> \u03ba\u03b1\u03b9 \u03b4\u03b9\u03ac\u03b2\u03b1\u03c3\u03b5 \u03bc\u03b5 \u03bb\u03b5\u03c0\u03c4\u03bf\u03bc\u03ad\u03c1\u03b5\u03b9\u03b1 \u03c4\u03bf section <strong>MFA Setup</strong>.</p>
          <p class="mfa-nudge-highlight">\u03a3\u03b5 \u03c0\u03b1\u03c1\u03b1\u03ba\u03b1\u03bb\u03ce \u0395\u039d\u0395\u03a1\u0393\u039f\u03a0\u039f\u0399\u0397\u03a3\u039f\u03a5\u039f\u03a5!!!</p>
          <p>\u0393\u03b9\u03b1\u03c4\u03af \u03b2\u03bb\u03ad\u03c0\u03c9 \u03bd\u03b1 \u03c0\u03b9\u03ac\u03bd\u03b5\u03b9 \u03ba\u03b1\u03bd\u03ad\u03bd\u03b1 \u03c3\u03ba\u03bf\u03c5\u03c0\u03cc\u03be\u03c5\u03bb\u03bf \u03bf \u0391\u03bd\u03b1\u03c3\u03c4\u03b1\u03c3\u03af\u03bf\u03c5 \u03ba\u03b1\u03b9 \u03bd\u03b1 \u03bc\u03b1\u03c2 \u03ba\u03ac\u03bd\u03b5\u03b9 \u03bc\u03c0\u03ac\u03bf\u03c5\u03bb\u03bf \u03c3\u03c4\u03bf \u03be\u03cd\u03bb\u03bf \u03cc\u03bb\u03bf\u03c5\u03c2 \u03c0\u03b1\u03c1\u03ad\u03b1.</p>
          <p class="mfa-nudge-love">Match Moods \u03c1\u03b5!! \u2764\ufe0f</p>
        </div>
        <button class="mfa-nudge-btn" @click="goToGuide">\ud83d\udcd6 \u03a0\u03ae\u03b3\u03b1\u03b9\u03bd\u03ad \u03bc\u03b5 \u03c3\u03c4\u03bf Guide</button>
        <button class="mfa-nudge-dismiss" @click="dismissMfaNudge">\u03a4\u03bf \u03ad\u03ba\u03b1\u03bd\u03b1 \u03ae\u03b4\u03b7 \u2014 \u03bc\u03b7\u03bd \u03c4\u03bf \u03be\u03b1\u03bd\u03b1\u03b4\u03b5\u03af\u03be\u03b5\u03b9\u03c2</button>
      </div>
    </div>

`;

c = c.substring(0, mainClose) + modal + c.substring(mainClose);

// Add reactive vars + logic in script section
// Find the script setup or setup() function
const scriptIdx = c.indexOf('const initials = computed');
if (scriptIdx === -1) {
  console.log('[ERROR] Cannot find initials computed');
  process.exit(1);
}

const scriptInsert = `
const showMfaNudge = ref(false)
const firstName = computed(() => {
  const full = auth.user?.fullName || ''
  const first = full.split(' ')[0] || 'User'
  // Greek vocative approximation
  if (first.endsWith('\u03bf\u03c2')) return first.slice(0, -1) + '\u03b5'
  if (first.endsWith('\u03b1\u03c2')) return first.slice(0, -1)
  if (first.endsWith('\u03b7\u03c2')) return first.slice(0, -1)
  return first
})

function checkMfaNudge() {
  // Show only if user has NO MFA and hasn't dismissed
  const dismissed = sessionStorage.getItem('mfa_nudge_dismissed')
  if (dismissed) return
  // Check if user has MFA - if mfaEnabled is false, show nudge
  if (auth.user && !auth.user.mfaEnabled) {
    showMfaNudge.value = true
  }
}

function dismissMfaNudge() {
  showMfaNudge.value = false
  sessionStorage.setItem('mfa_nudge_dismissed', 'true')
}

function goToGuide() {
  showMfaNudge.value = false
  router.push('/guide')
}

// Check on mount after small delay
setTimeout(() => checkMfaNudge(), 1500)

`;

c = c.substring(0, scriptIdx) + scriptInsert + c.substring(scriptIdx);

// Add ref import if not present
if (!c.includes("import { ref, computed, onMounted") && !c.includes("ref, computed")) {
  // ref should already be imported, just make sure
}

// Add CSS styles
const styleEnd = c.lastIndexOf('</style>');
const nudgeCss = `
.mfa-nudge-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);
  z-index: 9999; display: flex; align-items: center; justify-content: center;
  animation: mfaFadeIn 0.3s ease;
}
@keyframes mfaFadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes mfaBounce { 0% { transform: scale(0.8) translateY(20px); opacity: 0; } 100% { transform: scale(1) translateY(0); opacity: 1; } }
.mfa-nudge-modal {
  background: var(--surface); border-radius: 20px; padding: 36px 32px;
  max-width: 520px; width: 90%; text-align: center; position: relative;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3); animation: mfaBounce 0.4s ease;
  border-top: 4px solid #7c3aed;
}
.mfa-nudge-close {
  position: absolute; top: 14px; right: 18px; background: none; border: none;
  font-size: 18px; color: var(--text-dim); cursor: pointer;
}
.mfa-nudge-close:hover { color: var(--text); }
.mfa-nudge-icon { font-size: 48px; margin-bottom: 12px; }
.mfa-nudge-greeting {
  font-size: 22px; font-weight: 800; color: var(--text); margin-bottom: 18px;
  font-family: 'Nunito', sans-serif;
}
.mfa-nudge-body {
  text-align: left; font-size: 14px; line-height: 1.8; color: var(--text);
  margin-bottom: 24px; padding: 0 8px;
}
.mfa-nudge-body p { margin-bottom: 10px; }
.mfa-nudge-highlight {
  font-size: 18px !important; font-weight: 800; color: #dc2626;
  text-align: center; margin: 16px 0 !important;
  letter-spacing: 1px;
}
.mfa-nudge-love {
  font-size: 16px !important; font-weight: 700; color: #7c3aed;
  text-align: center; margin-top: 16px !important;
}
.mfa-nudge-btn {
  display: block; width: 100%; padding: 14px; background: #7c3aed;
  color: white; border: none; border-radius: 10px; font-size: 15px;
  font-weight: 700; cursor: pointer; margin-bottom: 10px;
  font-family: 'Nunito', sans-serif; transition: background 0.2s;
}
.mfa-nudge-btn:hover { background: #6d28d9; }
.mfa-nudge-dismiss {
  display: block; width: 100%; padding: 10px; background: none;
  border: 1px solid var(--border); border-radius: 8px; font-size: 12px;
  color: var(--text-dim); cursor: pointer; font-family: 'Nunito', sans-serif;
}
.mfa-nudge-dismiss:hover { border-color: var(--text-mid); color: var(--text-mid); }
`;

c = c.substring(0, styleEnd) + nudgeCss + c.substring(styleEnd);

fs.writeFileSync(f, c, 'utf8');
console.log('[OK] MFA Nudge popup added to DashboardLayout');