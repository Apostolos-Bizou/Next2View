const fs = require('fs');
const path = 'frontend/src/views/GuideView.vue';
let c = fs.readFileSync(path, 'utf8');
const lines = c.split('\n');

// =============================================================
// STEP 1: Add MFA tab as FIRST tab in tabs array (line 700)
// =============================================================
let tabsArrayIdx = -1;
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('const tabs = [')) {
    tabsArrayIdx = i;
    break;
  }
}
if (tabsArrayIdx === -1) {
  console.error('Could not find tabs array');
  process.exit(1);
}

const mfaTabLine = "  { id: 'mfa',           label: 'MFA Setup',     icon: '🔐', style: 'background:rgba(5,150,105,0.1);border-color:rgba(5,150,105,0.35);color:#047857;' },";

// Insert AFTER "const tabs = [" line
lines.splice(tabsArrayIdx + 1, 0, mfaTabLine);
console.log(`✅ Added MFA tab at line ${tabsArrayIdx + 2}`);

// =============================================================
// STEP 2: Find Security panel start and end, then INSERT MFA panel BEFORE it
// =============================================================
// Re-scan for security panel after the array insertion (line numbers shifted)
let securityPanelStartIdx = -1;
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes("activeTab === 'security'")) {
    securityPanelStartIdx = i;
    break;
  }
}
if (securityPanelStartIdx === -1) {
  console.error('Could not find security panel');
  process.exit(1);
}
console.log(`Security panel starts at line ${securityPanelStartIdx + 1}`);

// =============================================================
// STEP 3: Build MFA panel content
// =============================================================
const mfaPanel = `      <div v-if="activeTab === 'mfa'" class="guide-panel">
        <div class="g-mfa-hero">
          <div class="g-mfa-hero-icon">🔐</div>
          <div class="g-mfa-hero-text">
            <h2 class="g-mfa-hero-title">Multi-Factor Authentication (MFA)</h2>
            <p class="g-mfa-hero-subtitle">Δεύτερος παράγοντας ελέγχου για την ασφάλεια του λογαριασμού σου</p>
          </div>
          <div class="g-mfa-hero-badge">⚠️ Υποχρεωτικό για Legal & Finance</div>
        </div>

        <div class="g-section">
          <h3>📌 Τι είναι το MFA;</h3>
          <p>Το <strong>Multi-Factor Authentication (MFA)</strong> προσθέτει ένα επιπλέον επίπεδο ασφάλειας στον λογαριασμό σου. Πέρα από τον κωδικό πρόσβασης, χρειάζεσαι έναν 6-ψήφιο κωδικό που αλλάζει κάθε 30 δευτερόλεπτα και παράγεται από εφαρμογή στο κινητό σου.</p>
          <p>Έτσι, ακόμα κι αν κάποιος μάθει τον κωδικό σου, <strong>δεν μπορεί να μπει στον λογαριασμό σου</strong> χωρίς το κινητό σου.</p>
        </div>

        <div class="g-section g-section-warning">
          <h3>🚨 Πότε είναι ΥΠΟΧΡΕΩΤΙΚΟ;</h3>
          <p>Το MFA είναι <strong>υποχρεωτικό</strong> για όσους χρήστες έχουν πρόσβαση σε:</p>
          <ul class="g-list">
            <li><strong>Legal documents</strong> (συμβάσεις, νομικά αρχεία) — χρειάζεται για κάθε upload/download</li>
            <li><strong>Finance data</strong> (οικονομικά στοιχεία, πληρωμές, IBAN) — χρειάζεται για πρόσβαση στα οικονομικά</li>
            <li><strong>Confidential CEO Notes</strong> (εμπιστευτικές σημειώσεις) — απαιτεί επιπλέον επιβεβαίωση</li>
          </ul>
          <p style="margin-top:12px;">Αν δεν ενεργοποιήσεις το MFA, <strong>δεν θα έχεις πρόσβαση σε αυτές τις λειτουργίες</strong>. Το σύστημα θα σε ειδοποιήσει αυτόματα.</p>
        </div>

        <div class="g-section">
          <h3>📱 Βήμα 1: Εγκατάστησε το Google Authenticator</h3>
          <p>Πριν ξεκινήσεις τη ρύθμιση στο Next2View, χρειάζεσαι μια εφαρμογή authenticator στο κινητό σου. Συνιστούμε το <strong>Google Authenticator</strong> γιατί είναι δωρεάν, αξιόπιστο και εύκολο στη χρήση.</p>

          <div class="g-mfa-download-grid">
            <a href="https://apps.apple.com/app/google-authenticator/id388497605" target="_blank" rel="noopener" class="g-mfa-download-card g-mfa-apple">
              <div class="g-mfa-download-icon">🍎</div>
              <div class="g-mfa-download-text">
                <div class="g-mfa-download-label">iPhone / iPad</div>
                <div class="g-mfa-download-store">App Store</div>
              </div>
              <div class="g-mfa-download-arrow">→</div>
            </a>

            <a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2" target="_blank" rel="noopener" class="g-mfa-download-card g-mfa-android">
              <div class="g-mfa-download-icon">🤖</div>
              <div class="g-mfa-download-text">
                <div class="g-mfa-download-label">Android</div>
                <div class="g-mfa-download-store">Google Play</div>
              </div>
              <div class="g-mfa-download-arrow">→</div>
            </a>
          </div>

          <p class="g-mfa-alt"><strong>Εναλλακτικά:</strong> Μπορείς να χρησιμοποιήσεις και άλλες συμβατές εφαρμογές όπως <em>Microsoft Authenticator</em>, <em>Authy</em>, <em>1Password</em>, <em>Bitwarden</em>. Όλες λειτουργούν με το ίδιο πρότυπο (TOTP / RFC 6238).</p>
        </div>

        <div class="g-section">
          <h3>⚙️ Βήμα 2: Ενεργοποίησε το MFA στο Next2View</h3>
          <ol class="g-mfa-steps">
            <li>
              <strong>Πήγαινε στο Profile σου</strong>
              <p>Κάνε κλικ στο όνομά σου πάνω δεξιά → επίλεξε "Profile" (ή πάτησε το avatar σου).</p>
            </li>
            <li>
              <strong>Βρες την κατηγορία "Ασφάλεια"</strong>
              <p>Στη σελίδα Profile θα δεις section με τίτλο 🔐 <em>Ασφάλεια</em>. Εκεί υπάρχει το πεδίο "Έλεγχος Ταυτότητας (MFA)".</p>
            </li>
            <li>
              <strong>Πάτησε "Ενεργοποίηση MFA →"</strong>
              <p>Θα εμφανιστεί ένα QR code και ένα κρυπτογραφικό secret (alphanumeric κωδικός).</p>
            </li>
            <li>
              <strong>Άνοιξε το Google Authenticator στο κινητό σου</strong>
              <p>Πάτησε το <strong>+</strong> κάτω δεξιά → επίλεξε <strong>"Σκανάρισμα QR code"</strong>.</p>
            </li>
            <li>
              <strong>Σκάναρε το QR code</strong>
              <p>Στρέψε την κάμερα του κινητού σου στο QR code που βλέπεις στην οθόνη του υπολογιστή. Το app θα προσθέσει αυτόματα έναν λογαριασμό "Next2View" με το email σου.</p>
            </li>
            <li>
              <strong>Πάρε τον 6-ψήφιο κωδικό</strong>
              <p>Στην εφαρμογή θα δεις έναν 6-ψήφιο κωδικό κάτω από τον λογαριασμό "Next2View". Ο κωδικός αλλάζει κάθε 30 δευτερόλεπτα.</p>
            </li>
            <li>
              <strong>Πληκτρολόγησε τον κωδικό στο Next2View</strong>
              <p>Στο πεδίο "Κωδικός επαλήθευσης" γράψε τον 6-ψήφιο κωδικό και πάτησε <strong>"Επαλήθευση"</strong>.</p>
            </li>
            <li>
              <strong>✅ Έτοιμος!</strong>
              <p>Από εδώ και πέρα, κάθε φορά που μπαίνεις στο Next2View ή ζητάς πρόσβαση σε Legal/Finance, θα σου ζητείται ο 6-ψήφιος κωδικός από την εφαρμογή.</p>
            </li>
          </ol>
        </div>

        <div class="g-section">
          <h3>🔄 Βήμα 3: Πώς το χρησιμοποιείς καθημερινά</h3>
          <p>Όταν συνδέεσαι ή προσπαθείς να δεις Legal/Finance:</p>
          <ol class="g-list-numbered">
            <li>Άνοιξε το Google Authenticator στο κινητό σου</li>
            <li>Δες τον τρέχοντα 6-ψήφιο κωδικό κάτω από "Next2View"</li>
            <li>Πληκτρολόγησέ τον στην οθόνη πριν λήξει (έχεις 30 δευτερόλεπτα)</li>
            <li>Αν ο κωδικός λήξει πριν προλάβεις, περίμενε τον επόμενο και προσπάθησε ξανά</li>
          </ol>
        </div>

        <div class="g-section g-section-tip">
          <h3>💡 Συμβουλές & Best Practices</h3>
          <ul class="g-list">
            <li><strong>Backup:</strong> Πάρε screenshot του κρυπτογραφικού secret (ο κωδικός κάτω από το QR) και φύλαξέ τον σε ασφαλές μέρος. Αν χάσεις το κινητό σου, μπορείς να ξαναρυθμίσεις το MFA με αυτόν.</li>
            <li><strong>Πολλαπλές συσκευές:</strong> Μπορείς να σκανάρεις το ίδιο QR σε δεύτερο κινητό (π.χ. tablet) για backup.</li>
            <li><strong>Cloud sync:</strong> Το Google Authenticator υποστηρίζει συγχρονισμό με τον Google λογαριασμό σου — αν αλλάξεις κινητό, οι κωδικοί έρχονται μαζί σου.</li>
            <li><strong>Εμπιστευτικότητα:</strong> ΠΟΤΕ μην μοιραστείς το QR code ή τον 6-ψήφιο κωδικό με κανέναν, ούτε με υποστήριξη.</li>
            <li><strong>Ώρα κινητού:</strong> Βεβαιώσου ότι η ώρα του κινητού σου είναι σωστή (auto-sync). Λάθος ώρα = λάθος κωδικός.</li>
          </ul>
        </div>

        <div class="g-section g-section-warning">
          <h3>❓ Συχνές Ερωτήσεις</h3>

          <div class="g-faq">
            <div class="g-faq-q">Έχασα το κινητό μου. Τι κάνω;</div>
            <div class="g-faq-a">Επικοινώνησε άμεσα με τον CEO ή τον IT administrator. Θα σου απενεργοποιήσει το MFA από τη βάση και θα μπορέσεις να ξανασυνδεθείς και να το ρυθμίσεις από την αρχή σε νέα συσκευή.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Ο κωδικός δεν δουλεύει. Γιατί;</div>
            <div class="g-faq-a">Συνήθως είναι θέμα ώρας. Πήγαινε στις ρυθμίσεις του κινητού → Ώρα & Ημερομηνία → ενεργοποίησε "Αυτόματη ώρα δικτύου". Δοκίμασε ξανά μετά από 1 λεπτό.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Πρέπει ο Google λογαριασμός μου να είναι ίδιος με το email του Next2View;</div>
            <div class="g-faq-a"><strong>Όχι.</strong> Το Google Authenticator είναι ανεξάρτητη εφαρμογή. Δεν συνδέεται με κανένα Google account. Μπορείς να το έχεις σε οποιοδήποτε κινητό με οποιοδήποτε email.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Μπορώ να απενεργοποιήσω το MFA;</div>
            <div class="g-faq-a">Ναι, από το Profile → Ασφάλεια → "Απενεργοποίηση". ΟΜΩΣ, αν είσαι Legal ή Finance user, δεν θα έχεις πια πρόσβαση σε αυτές τις λειτουργίες μέχρι να το ξαναενεργοποιήσεις.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Τι σημαίνει "TOTP";</div>
            <div class="g-faq-a">Time-based One-Time Password (RFC 6238). Είναι το διεθνές πρότυπο για 6-ψήφιους κωδικούς που αλλάζουν με τον χρόνο. Λειτουργεί offline στο κινητό σου, χωρίς internet.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Είναι ασφαλές αν κάποιος δει το QR code μου;</div>
            <div class="g-faq-a"><strong>Όχι.</strong> Το QR περιέχει το secret σου. Αν κάποιος το σκανάρει, μπορεί να δημιουργήσει τους ίδιους κωδικούς με σένα. Πάντα κάνε το setup σε ιδιωτικό χώρο και μην το δείχνεις σε κανέναν.</div>
          </div>
        </div>

        <div class="g-callout-success">
          <strong>✅ Pro tip:</strong> Μόλις ολοκληρώσεις το setup, δοκίμασε να βγεις από τον λογαριασμό σου και να μπεις ξανά. Έτσι εξασφαλίζεις ότι το MFA δουλεύει σωστά πριν χρειαστείς πρόσβαση σε κρίσιμα δεδομένα.
        </div>
      </div>

`;

// Insert MFA panel BEFORE security panel
lines.splice(securityPanelStartIdx, 0, ...mfaPanel.split('\n'));
console.log(`✅ Inserted MFA panel before security panel`);

// =============================================================
// STEP 4: Find <style scoped> section and add MFA-specific styles
// =============================================================
let styleStartIdx = -1;
for (let i = lines.length - 1; i >= 0; i--) {
  if (lines[i].includes('<style scoped>')) {
    styleStartIdx = i;
    break;
  }
}

if (styleStartIdx === -1) {
  console.error('Could not find <style scoped>');
  process.exit(1);
}

// Find the closing </style>
let styleEndIdx = -1;
for (let i = styleStartIdx; i < lines.length; i++) {
  if (lines[i].includes('</style>')) {
    styleEndIdx = i;
    break;
  }
}

const mfaStyles = `
/* ============= MFA Tab Specific Styles ============= */
.g-mfa-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px;
  background: linear-gradient(135deg, rgba(5,150,105,0.08) 0%, rgba(16,185,129,0.04) 100%);
  border: 1px solid rgba(5,150,105,0.25);
  border-radius: 16px;
  margin-bottom: 28px;
  position: relative;
  flex-wrap: wrap;
}
.g-mfa-hero-icon {
  font-size: 56px;
  line-height: 1;
}
.g-mfa-hero-text {
  flex: 1;
  min-width: 240px;
}
.g-mfa-hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #047857;
  margin: 0 0 6px 0;
}
.g-mfa-hero-subtitle {
  color: var(--text-secondary, #64748b);
  font-size: 15px;
  margin: 0;
}
.g-mfa-hero-badge {
  background: rgba(220,38,38,0.1);
  color: #b91c1c;
  border: 1px solid rgba(220,38,38,0.3);
  padding: 8px 14px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
}

.g-mfa-download-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin: 16px 0;
}
@media (max-width: 600px) {
  .g-mfa-download-grid {
    grid-template-columns: 1fr;
  }
}
.g-mfa-download-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--bg-card, #ffffff);
  border: 2px solid var(--border, #e2e8f0);
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
  cursor: pointer;
}
.g-mfa-download-card:hover {
  border-color: rgba(5,150,105,0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.g-mfa-download-icon {
  font-size: 32px;
  line-height: 1;
}
.g-mfa-download-text {
  flex: 1;
}
.g-mfa-download-label {
  font-weight: 600;
  font-size: 15px;
  color: var(--text, #0f172a);
}
.g-mfa-download-store {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-top: 2px;
}
.g-mfa-download-arrow {
  font-size: 22px;
  color: #047857;
  font-weight: 700;
}
.g-mfa-alt {
  background: rgba(59,130,246,0.06);
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-secondary, #64748b);
  margin-top: 12px;
}

.g-mfa-steps {
  list-style: none;
  counter-reset: step;
  padding: 0;
  margin: 0;
}
.g-mfa-steps li {
  counter-increment: step;
  position: relative;
  padding: 14px 14px 14px 56px;
  margin-bottom: 12px;
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  border-left: 4px solid #047857;
}
.g-mfa-steps li::before {
  content: counter(step);
  position: absolute;
  left: 14px;
  top: 14px;
  width: 32px;
  height: 32px;
  background: #047857;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}
.g-mfa-steps li strong {
  display: block;
  font-size: 15px;
  color: var(--text, #0f172a);
  margin-bottom: 4px;
}
.g-mfa-steps li p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary, #64748b);
  line-height: 1.5;
}

.g-list-numbered {
  margin: 8px 0 0 22px;
  padding: 0;
}
.g-list-numbered li {
  margin-bottom: 6px;
  line-height: 1.6;
}

.g-faq {
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 10px;
}
.g-faq-q {
  font-weight: 600;
  color: var(--text, #0f172a);
  margin-bottom: 6px;
  font-size: 14px;
}
.g-faq-a {
  font-size: 14px;
  color: var(--text-secondary, #64748b);
  line-height: 1.55;
}

.g-callout-success {
  background: linear-gradient(135deg, rgba(5,150,105,0.1) 0%, rgba(16,185,129,0.05) 100%);
  border: 1px solid rgba(5,150,105,0.3);
  border-left: 4px solid #047857;
  padding: 14px 18px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--text, #0f172a);
  line-height: 1.55;
  margin-top: 16px;
}

.g-section-warning {
  background: rgba(245,158,11,0.05);
  border: 1px solid rgba(245,158,11,0.25);
  border-radius: 12px;
  padding: 18px 20px;
  margin: 18px 0;
}
.g-section-warning h3 {
  color: #b45309;
}

.g-section-tip {
  background: rgba(59,130,246,0.04);
  border: 1px solid rgba(59,130,246,0.2);
  border-radius: 12px;
  padding: 18px 20px;
  margin: 18px 0;
}
.g-section-tip h3 {
  color: #1d4ed8;
}
`;

// Insert styles before </style>
lines.splice(styleEndIdx, 0, mfaStyles);
console.log(`✅ Added MFA-specific CSS styles`);

fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log('\n🎉 GuideView updated successfully!');
console.log(`   - MFA tab added as FIRST tab`);
console.log(`   - MFA panel inserted before Security panel`);
console.log(`   - Visible to ALL users (Security still CEO-only)`);
console.log(`   - Custom styling added`);
