const fs = require('fs');
const path = 'frontend/src/views/GuideView.vue';
let c = fs.readFileSync(path, 'utf8');
const lines = c.split('\n');

// Find security panel start and end
let secStart = -1;
let glossaryStart = -1;
for (let i = 0; i < lines.length; i++) {
  if (secStart === -1 && lines[i].includes("activeTab === 'security'")) {
    secStart = i;
  }
  if (lines[i].includes("activeTab === 'glossary'")) {
    glossaryStart = i;
    break;
  }
}

if (secStart === -1 || glossaryStart === -1) {
  console.error('Could not find panel boundaries');
  process.exit(1);
}

console.log(`Security panel: lines ${secStart + 1} to ${glossaryStart} (will be replaced)`);

// New comprehensive Security panel
const newSecurityPanel = `      <div v-if="activeTab === 'security'" class="guide-panel">
        <div class="g-sec-hero">
          <div class="g-sec-hero-icon">🛡️</div>
          <div class="g-sec-hero-text">
            <h2 class="g-sec-hero-title">Security Documentation</h2>
            <p class="g-sec-hero-subtitle">Complete security posture of Next2View — production-ready as of April 2026</p>
          </div>
          <div class="g-sec-hero-version">v1.10 + Legal Vault</div>
        </div>

        <div class="g-callout-warning">
          <strong>🔒 Access Control:</strong> This documentation is visible only to the CEO and to users with the <em>Security Documentation</em> permission. Principle of least privilege applies. All items marked ✅ are <strong>live in production</strong>.
        </div>

        <h3>1. Security Posture Overview</h3>
        <div class="g-grid-4">
          <div class="g-stat-card">
            <div class="g-stat-label">Authentication</div>
            <div class="g-stat-value">JWT RS256</div>
            <div class="g-stat-sub">60-min access + 7d refresh</div>
          </div>
          <div class="g-stat-card">
            <div class="g-stat-label">Password Hashing</div>
            <div class="g-stat-value">BCrypt cost=12</div>
            <div class="g-stat-sub">Industry standard</div>
          </div>
          <div class="g-stat-card">
            <div class="g-stat-label">Transport</div>
            <div class="g-stat-value">TLS 1.2+</div>
            <div class="g-stat-sub">HTTPS end-to-end + PFS</div>
          </div>
          <div class="g-stat-card">
            <div class="g-stat-label">Secrets Management</div>
            <div class="g-stat-value">Azure Key Vault</div>
            <div class="g-stat-sub">CMK + Managed Identity</div>
          </div>
        </div>

        <h3>2. Authentication & Authorization</h3>
        <table class="g-table">
          <thead><tr><th>Control</th><th>Implementation</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Login flow</td><td>JWT RS256 (asymmetric), 60-min access token, 7-day refresh token</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Password storage</td><td>BCrypt with cost factor 12, unique per-user salt</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Auto-refresh interceptor</td><td>Axios interceptor με queue + retry για concurrent 401s — UX seamless</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Token validation</td><td>JwtAuthFilter on every request: signature + expiry checks</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Role-based access</td><td>@PreAuthorize on controllers (CEO / DEPT_HEAD / VIEWER)</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Fine-grained permissions</td><td>14 per-user flags (viewFinance, viewLegal, manageCompanies, etc.)</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Central authorization</td><td>PermissionEvaluator component, re-checked in service layer</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Cross-company scoping</td><td>Department-based project visibility across all group companies</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Account lockout</td><td>5 failed attempts → 15-min lockout</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>MFA (TOTP / RFC 6238)</td><td>Production-ready με Google Authenticator integration; Phase A (per-user enforcement) ενεργό</td><td><span class="g-badge g-badge-ok">✅ Live in Production</span></td></tr>
            <tr><td>MFA Phase B (global)</td><td>Hard enforcement για όλους τους legal users — μετά την onboarding ολοκληρώνεται</td><td><span class="g-badge g-badge-progress">🟡 In Progress</span></td></tr>
          </tbody>
        </table>

        <h3>3. 🔐 Multi-Factor Authentication (MFA) — Architecture</h3>
        <table class="g-table">
          <thead><tr><th>Component</th><th>Specification</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Algorithm</td><td>TOTP per RFC 6238, SHA-1 HMAC, 30-second intervals, 6-digit codes</td><td><span class="g-badge g-badge-ok">✅ Standard</span></td></tr>
            <tr><td>Backend endpoints</td><td>POST /auth/mfa/setup · /verify · /disable · GET /status</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Compatible apps</td><td>Google Authenticator, Microsoft Authenticator, Authy, 1Password, Bitwarden</td><td><span class="g-badge g-badge-ok">✅ Universal</span></td></tr>
            <tr><td>Time window tolerance</td><td>±1 interval (clock drift compensation)</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Session integration</td><td>mfaVerifiedForSession claim στο JWT — required για legal/finance endpoints</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Frontend UX</td><td>QR rendering με api.qrserver.com fallback, 20-attempt retry, copy-paste secret</td><td><span class="g-badge g-badge-ok">✅ Implemented</span></td></tr>
            <tr><td>Library</td><td>dev.samstevens.totp 1.7.1 (Java)</td><td><span class="g-badge g-badge-ok">✅ Production</span></td></tr>
            <tr><td>User education</td><td>Step-by-step guide tab (visible to all users)</td><td><span class="g-badge g-badge-ok">✅ Live</span></td></tr>
          </tbody>
        </table>

        <h3>4. 🔒 Next2View Legal Vault — Cryptographic Architecture</h3>
        <p style="margin-bottom:14px;color:var(--text-secondary,#64748b);font-size:14px;">5-layer defence-in-depth για enterprise-grade encrypted contract storage. Each layer is independent — compromising one does not yield access to data.</p>

        <div class="g-layers">
          <div class="g-layer">
            <div class="g-layer-num">5</div>
            <div class="g-layer-content">
              <div class="g-layer-title">MFA Enforcement</div>
              <div class="g-layer-desc">TOTP required for legal endpoints — JWT mfaVerified=true claim</div>
            </div>
            <div class="g-badge g-badge-ok">✅</div>
          </div>
          <div class="g-layer">
            <div class="g-layer-num">4</div>
            <div class="g-layer-content">
              <div class="g-layer-title">Identity & Access Management</div>
              <div class="g-layer-desc">JWT RS256, RBAC, HttpOnly+Secure cookies, viewLegal permission scoping</div>
            </div>
            <div class="g-badge g-badge-ok">✅</div>
          </div>
          <div class="g-layer">
            <div class="g-layer-num">3</div>
            <div class="g-layer-content">
              <div class="g-layer-title">Application-Level Encryption</div>
              <div class="g-layer-desc">AES-256-GCM, per-document Data Encryption Key (DEK), unique 96-bit IV</div>
            </div>
            <div class="g-badge g-badge-ok">✅</div>
          </div>
          <div class="g-layer">
            <div class="g-layer-num">2</div>
            <div class="g-layer-content">
              <div class="g-layer-title">Cryptographic Key Wrapping</div>
              <div class="g-layer-desc">RSA-OAEP-256 wrap, RSA-3072 Customer-Managed Key (CMK), Azure Key Vault</div>
            </div>
            <div class="g-badge g-badge-ok">✅</div>
          </div>
          <div class="g-layer">
            <div class="g-layer-num">1</div>
            <div class="g-layer-content">
              <div class="g-layer-title">Infrastructure Encryption at Rest</div>
              <div class="g-layer-desc">Azure Storage SSE με CMK, GRS replication (North + West Europe), TLS 1.2+</div>
            </div>
            <div class="g-badge g-badge-ok">✅</div>
          </div>
        </div>

        <h3>5. Legal Vault — Cryptographic Specifications</h3>
        <table class="g-table">
          <thead><tr><th>Specification</th><th>Value</th></tr></thead>
          <tbody>
            <tr><td>Symmetric encryption</td><td>AES-256-GCM (Authenticated Encryption with Associated Data)</td></tr>
            <tr><td>Symmetric key size</td><td>256 bits</td></tr>
            <tr><td>Key wrapping algorithm</td><td>RSA-OAEP with SHA-256 (RSA-OAEP-256)</td></tr>
            <tr><td>Asymmetric key size</td><td>3072 bits (NIST SP 800-57 compliant through 2030)</td></tr>
            <tr><td>Hashing</td><td>SHA-256 (integrity verification + deduplication)</td></tr>
            <tr><td>Key Vault</td><td>Azure Key Vault με purge-protection IRREVERSIBLE</td></tr>
            <tr><td>CMK location</td><td>next2view-dev-kv/keys/legal-contracts-cmk</td></tr>
            <tr><td>Storage container</td><td>next2viewlegalstorage/legal-contracts (private, no public access)</td></tr>
            <tr><td>Retention</td><td>Soft-delete 90 days, versioning enabled, change-feed 90 days</td></tr>
            <tr><td>Replication</td><td>Geo-redundant: North Europe (primary) + West Europe (DR)</td></tr>
          </tbody>
        </table>

        <h3>6. Forensic Encryption Verification</h3>
        <p style="margin-bottom:14px;color:var(--text-secondary,#64748b);font-size:14px;">Real-world test: Revolut Business PDF (37.9 KB) uploaded → byte-level inspection of stored ciphertext.</p>
        <table class="g-table">
          <tbody>
            <tr><td><strong>Original (plaintext)</strong></td><td><code>%PDF-...</code> (standard PDF magic bytes)</td></tr>
            <tr><td><strong>Stored (ciphertext)</strong></td><td><code>?Ru\\...</code> (pseudo-random — indistinguishable from noise)</td></tr>
            <tr><td><strong>Content-Type at rest</strong></td><td>application/octet-stream (Azure cannot identify file type)</td></tr>
            <tr><td><strong>Size overhead</strong></td><td>+844 bytes (IV 12 + authTag 16 + wrappedDEK 816)</td></tr>
            <tr><td><strong>Integrity</strong></td><td>SHA-256 verified on every retrieval; tamper detection automatic</td></tr>
            <tr><td><strong>Decryption result</strong></td><td>Byte-perfect reconstruction of original PDF</td></tr>
          </tbody>
        </table>

        <h3>7. Data Protection (TLS, CSRF, Secrets)</h3>
        <table class="g-table">
          <thead><tr><th>Control</th><th>Implementation</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Transport encryption</td><td>TLS 1.2+ enforced via Azure Static Web Apps & Container Apps</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>CORS policy</td><td>Explicit allowed origins list, wildcard disabled</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>CSRF protection</td><td>Stateless JWT sessions (no session cookies); SameSite=Strict on auth cookies</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Secrets management</td><td>Azure Key Vault for JWT keys, DB credentials, storage keys</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Credentials in code</td><td>Zero hardcoded secrets — all via Managed Identity or environment variables</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Password reset</td><td>Time-limited token flow (V16 migration), single-use tokens</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
          </tbody>
        </table>

        <h3>8. File Security (Legacy Contract Storage)</h3>
        <table class="g-table">
          <thead><tr><th>Control</th><th>Implementation</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Upload size limit</td><td>10 MB per file, enforced server-side</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>File type whitelist</td><td>pdf, doc, docx, jpg, jpeg, png only</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>MIME validation</td><td>Magic-byte content inspection (defense against MIME spoofing)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Filename sanitization</td><td>UUID-based internal names; originals stored only as metadata</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Path traversal defense</td><td>UUID folder structure in Azure Blob; no user-controlled paths</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Download authorization</td><td>Permission re-check at service layer; SAS tokens with 1-hour TTL</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Audit logging</td><td>Every upload, download, and delete recorded with user + timestamp</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
          </tbody>
        </table>

        <h3>9. Database Security</h3>
        <table class="g-table">
          <thead><tr><th>Control</th><th>Implementation</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Hosting</td><td>Azure PostgreSQL Flexible Server 15 (managed, patched)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Automatic backups</td><td>Point-in-time restore (35-day window), geo-redundant</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Network access</td><td>Private endpoints + firewall rules; no public exposure</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Connection encryption</td><td>TLS enforced between app and database</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>SQL injection defense</td><td>JPA/Hibernate with parameterized queries throughout</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Schema migrations</td><td>Flyway with version control (V1 through V18 + Legal Vault)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Cryptographic erasure support</td><td>DEK destruction renders ciphertext permanently unreadable (NIST SP 800-88)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
          </tbody>
        </table>

        <h3>10. Infrastructure & Operations</h3>
        <table class="g-table">
          <thead><tr><th>Control</th><th>Implementation</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Backend hosting</td><td>Azure Container Apps (auto-scaling, managed)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Frontend hosting</td><td>Azure Static Web Apps with built-in CDN + TLS</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Container registry</td><td>Azure Container Registry (private, RBAC-controlled)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>CI/CD pipeline</td><td>GitHub Actions with branch protection on main</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Versioned deployments</td><td>Git tags for every stable release (rollback in &lt; 2 min)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Health monitoring</td><td>Azure Container Apps health probes, auto-rollback on failure</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Managed Identity</td><td>App MI με scoped roles (Blob Contributor, Key Vault Crypto User) — zero shared secrets</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Rate limiting</td><td>On auth endpoints to prevent brute-force attacks</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
          </tbody>
        </table>

        <h3>11. ⚖️ GDPR Compliance & Legal</h3>
        <table class="g-table">
          <thead><tr><th>Requirement</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Data Protection Impact Assessment (DPIA) — Article 35 GDPR</td><td><span class="g-badge g-badge-ok">✅ v1.1 Enhanced (regulator-ready)</span></td></tr>
            <tr><td>Technical Security Architecture document</td><td><span class="g-badge g-badge-ok">✅ v1.0 Published</span></td></tr>
            <tr><td>Records of Processing Activities (RoPA) — Article 30</td><td><span class="g-badge g-badge-ok">✅ Documented (Appendix A of DPIA)</span></td></tr>
            <tr><td>Data Minimisation statement</td><td><span class="g-badge g-badge-ok">✅ Per-field justification documented</span></td></tr>
            <tr><td>Right to Erasure procedure (Article 17)</td><td><span class="g-badge g-badge-ok">✅ 6-step procedure with cryptographic erasure</span></td></tr>
            <tr><td>Sub-processor register</td><td><span class="g-badge g-badge-ok">✅ MS Ireland (EEA), Anthropic Ireland (ZDR), GitHub (no PII)</span></td></tr>
            <tr><td>Cross-border transfer (SCCs)</td><td><span class="g-badge g-badge-ok">✅ NONE — all processing within EEA</span></td></tr>
            <tr><td>Retention policy (per category)</td><td><span class="g-badge g-badge-ok">✅ 11 categories documented με legal basis</span></td></tr>
            <tr><td>Breach notification procedure (72h)</td><td><span class="g-badge g-badge-ok">✅ Article 33 GDPR aligned</span></td></tr>
            <tr><td>ISO/IEC 27001:2022 Annex A controls</td><td><span class="g-badge g-badge-ok">✅ Mapped (A.5.15, A.8.2/5/24/15/16/28, etc.)</span></td></tr>
          </tbody>
        </table>

        <h3>12. Audit Logging & Monitoring</h3>
        <table class="g-table">
          <thead><tr><th>Capability</th><th>Implementation</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Authentication events</td><td>Login success/failure με user, IP, timestamp, user-agent</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>MFA events</td><td>Setup, verify, disable με full audit trail</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>File operations</td><td>Upload, download, list, delete με file ID + size</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Permission changes</td><td>Role assignments, permission grants — immutable history</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Administrative ops</td><td>User/company creation, project deletion</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Audit log retention</td><td>7 years (Greek tax law + ISO 27001 A.8.15 alignment)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Azure Activity Log</td><td>Management-plane operations (resource changes)</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Application Insights</td><td>Real-time telemetry, anomaly detection</td><td><span class="g-badge g-badge-ok">✅</span></td></tr>
            <tr><td>Security audit log UI</td><td>Admin view of all authentication and privileged actions</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
          </tbody>
        </table>

        <h3>13. 🔐 The "4-Factor Compromise" Guarantee</h3>
        <div class="g-callout-info">
          <p style="margin:0 0 10px 0;"><strong>To decrypt a single contract stored in Next2View Legal Vault, an adversary would need to simultaneously breach FOUR independent security perimeters:</strong></p>
          <ol style="margin:8px 0 0 22px;line-height:1.7;">
            <li>Azure Key Vault Customer-Managed Key (RSA-3072, purge-protected)</li>
            <li>JWT RS256 signing keys</li>
            <li>PostgreSQL database credentials and dump</li>
            <li>The user's personal MFA seed and an active authenticated session</li>
          </ol>
          <p style="margin:12px 0 0 0;font-size:13px;"><strong>Probability assessment:</strong> negligible under current commercial threat models. Nation-state actors are explicitly out of scope per documented threat model.</p>
        </div>

        <h3>14. Security Roadmap</h3>
        <table class="g-table">
          <thead><tr><th>Item</th><th>Description</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>MFA Phase B enforcement</td><td>Hard requirement για όλους τους legal/finance users (after onboarding)</td><td><span class="g-badge g-badge-progress">🟡 In Progress</span></td></tr>
            <tr><td>Auth rate limiting</td><td>Throttle failed login attempts per IP / account</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
            <tr><td>Security audit log UI</td><td>Admin dashboard για authentication και privileged actions</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
            <tr><td>WebAuthn / FIDO2</td><td>Hardware-backed authentication (YubiKey, platform authenticators)</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
            <tr><td>HSM upgrade (FIPS 140-2 Level 3)</td><td>Azure Key Vault Premium tier για enhanced key protection</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
            <tr><td>Penetration testing</td><td>External security audit before wider rollout</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
            <tr><td>SIEM integration</td><td>Centralised security event management with auto-correlation</td><td><span class="g-badge g-badge-planned">📋 Planned</span></td></tr>
          </tbody>
        </table>

        <h3>15. Documentation Repository</h3>
        <p style="color:var(--text-secondary,#64748b);font-size:14px;margin-bottom:14px;">All security documentation maintained as live artifacts:</p>
        <ul class="g-list">
          <li><strong>DPIA v1.1 Enhanced</strong> — 520 paragraphs, GDPR Article 35 compliance, RoPA appendix</li>
          <li><strong>Technical Security Architecture v1.0</strong> — 456 paragraphs, STRIDE threat model, incident response framework</li>
          <li><strong>Cost Analysis v2.0</strong> — Updated effort + market valuation reflecting Legal Vault scope</li>
          <li><strong>This Security Documentation</strong> — In-platform reference, always synchronized with deployed version</li>
        </ul>

        <div class="g-callout-success" style="margin-top:18px;">
          <strong>📊 Current security posture assessment:</strong> Defence-in-depth across 5 independent layers, GDPR Article 32 compliance documented, residual risk LOW across all scenarios per WP248 methodology, Article 36 prior consultation NOT required.
        </div>

        <div class="g-callout-warning" style="margin-top:14px;">
          <strong>⚠️ Access control for this page:</strong> Visible only to CEO and users with <em>Security Documentation</em> permission. Principle of least privilege applies.
        </div>
      </div>

`;

// Replace the old security panel
const oldLineCount = glossaryStart - secStart;
const newLines = newSecurityPanel.split('\n');
lines.splice(secStart, oldLineCount, ...newLines);

console.log(`✅ Replaced ${oldLineCount} lines with ${newLines.length} new lines`);

// =============================================================
// Add CSS for new components: g-sec-hero, g-grid-4, g-stat-card, g-layers, g-layer, g-table, g-badge, g-callout
// =============================================================
let styleEndIdx = -1;
for (let i = lines.length - 1; i >= 0; i--) {
  if (lines[i].includes('</style>')) {
    styleEndIdx = i;
    break;
  }
}

const newStyles = `
/* ============= Security Tab Updated Styles ============= */
.g-sec-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  background: linear-gradient(135deg, rgba(245,158,11,0.08) 0%, rgba(217,119,6,0.04) 100%);
  border: 1px solid rgba(245,158,11,0.3);
  border-radius: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.g-sec-hero-icon {
  font-size: 48px;
  line-height: 1;
}
.g-sec-hero-text {
  flex: 1;
  min-width: 260px;
}
.g-sec-hero-title {
  font-size: 22px;
  font-weight: 700;
  color: #b45309;
  margin: 0 0 4px 0;
}
.g-sec-hero-subtitle {
  color: var(--text-secondary, #64748b);
  font-size: 14px;
  margin: 0;
}
.g-sec-hero-version {
  background: rgba(245,158,11,0.15);
  color: #92400e;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
}

.g-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}
@media (max-width: 900px) {
  .g-grid-4 { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 500px) {
  .g-grid-4 { grid-template-columns: 1fr; }
}
.g-stat-card {
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  padding: 14px 16px;
}
.g-stat-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary, #64748b);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}
.g-stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text, #0f172a);
  margin-bottom: 4px;
}
.g-stat-sub {
  font-size: 12px;
  color: var(--text-secondary, #64748b);
}

.g-layers {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 14px 0;
}
.g-layer {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  border-left: 4px solid #1e3a8a;
}
.g-layer-num {
  width: 36px;
  height: 36px;
  background: rgba(30,58,138,0.1);
  color: #1e3a8a;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}
.g-layer-content {
  flex: 1;
  min-width: 0;
}
.g-layer-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text, #0f172a);
}
.g-layer-desc {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-top: 2px;
}

.g-table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0 24px 0;
  font-size: 13px;
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  overflow: hidden;
}
.g-table thead {
  background: rgba(30,58,138,0.05);
}
.g-table th {
  text-align: left;
  padding: 10px 14px;
  font-weight: 600;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-secondary, #64748b);
  border-bottom: 1px solid var(--border, #e2e8f0);
}
.g-table td {
  padding: 10px 14px;
  border-bottom: 1px solid rgba(226,232,240,0.5);
  vertical-align: top;
}
.g-table tbody tr:last-child td {
  border-bottom: none;
}
.g-table tbody tr:hover {
  background: rgba(241,245,249,0.5);
}
.g-table code {
  background: rgba(15,23,42,0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
}

.g-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.g-badge-ok {
  background: rgba(5,150,105,0.1);
  color: #047857;
  border: 1px solid rgba(5,150,105,0.3);
}
.g-badge-progress {
  background: rgba(245,158,11,0.1);
  color: #b45309;
  border: 1px solid rgba(245,158,11,0.3);
}
.g-badge-planned {
  background: rgba(100,116,139,0.1);
  color: #475569;
  border: 1px solid rgba(100,116,139,0.3);
}

.g-callout-info {
  background: linear-gradient(135deg, rgba(30,58,138,0.06) 0%, rgba(59,130,246,0.03) 100%);
  border: 1px solid rgba(30,58,138,0.25);
  border-left: 4px solid #1e3a8a;
  padding: 16px 20px;
  border-radius: 10px;
  margin: 14px 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text, #0f172a);
}

.g-callout-warning {
  background: rgba(245,158,11,0.06);
  border: 1px solid rgba(245,158,11,0.3);
  border-left: 4px solid #b45309;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--text, #0f172a);
  line-height: 1.55;
  margin: 14px 0;
}
`;

lines.splice(styleEndIdx, 0, newStyles);
console.log(`✅ Added new CSS for security tab components`);

fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log('\n🎉 Security tab fully updated with current production state!');
console.log('   - 15 sections covering all security aspects');
console.log('   - MFA architecture, Legal Vault crypto, GDPR compliance');
console.log('   - Forensic verification, 5-layer defence, 4-factor compromise guarantee');
console.log('   - Roadmap with current/planned items');
