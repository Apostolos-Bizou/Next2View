<template>
  <div class="content">
    <div class="guide-header">
      <div class="guide-badge">NEXT2VIEW</div>
      <div class="guide-title">📘 Οδηγός Χρήσης</div>
      <div class="guide-sub">Πλήρης οδηγός για κάθε λειτουργία της πλατφόρμας. Επίλεξε κατηγορία παρακάτω.</div>
    </div>

    <!-- TABS -->
    <div class="guide-tabs">
      <div v-for="tab in tabs" :key="tab.id"
        :class="['guide-tab', { active: activeTab === tab.id }, tab.id]"
        @click="activeTab = tab.id">
        {{ tab.icon }} {{ tab.label }}
      </div>
    </div>

    <!-- DASHBOARD TAB -->
    <div v-if="activeTab === 'dashboard'" class="guide-panel">
      <div class="g-section-title">◈ Dashboard — Η Κεντρική Σελίδα</div>
      <div class="g-desc">Το Dashboard είναι η πρώτη σελίδα που βλέπεις μόλις μπεις στο Next2View. Παρέχει real-time εικόνα για όλες τις εταιρείες και projects του Ομίλου.</div>
      <div class="g-subsection">KPI Strip</div>
      <div class="g-desc">Στην κορυφή εμφανίζεται το <strong>KPI Strip</strong> με τα βασικά metrics: Overall completion, ανά κατηγορία (Finance, Legal, Developing, Marketing) και At Risk projects.</div>
      <div class="g-subsection">Company Cards</div>
      <div class="g-desc">Κάθε εταιρεία εμφανίζεται με progress bar και ποσοστό ολοκλήρωσης. Κλικ σε εταιρεία για να δεις μόνο τα projects της.</div>
      <div class="g-subsection">Deadlines & Activity</div>
      <div class="g-desc">Στα δεξιά εμφανίζονται τα επερχόμενα deadlines (κόκκινο = urgent &lt;7 ημέρες) και το πρόσφατο activity feed.</div>
    </div>

    <!-- COMPANIES TAB -->
    <div v-if="activeTab === 'companies'" class="guide-panel">
      <div class="g-section-title">🏢 Εταιρείες</div>
      <div class="g-desc">Ο Όμιλος Next2me περιλαμβάνει 4 εταιρείες:</div>
      <div class="g-cards">
        <div class="g-card"><div class="g-card-icon" style="color:#3b82f6;">★</div><div class="g-card-title">Polaris Financial</div><div class="g-card-desc">TPA maritime healthcare & financial services</div></div>
        <div class="g-card"><div class="g-card-icon" style="color:#059669;">⚓</div><div class="g-card-title">Crossworld Marine</div><div class="g-card-desc">Crew management & marine operations</div></div>
        <div class="g-card"><div class="g-card-icon" style="color:#d97706;">⊙</div><div class="g-card-title">WiMAS</div><div class="g-card-desc">Maritime training & certification</div></div>
        <div class="g-card"><div class="g-card-icon" style="color:#7c3aed;">▲</div><div class="g-card-title">Varship</div><div class="g-card-desc">Ship management services</div></div>
      </div>
    </div>

    <!-- PROJECTS TAB -->
    <div v-if="activeTab === 'projects'" class="guide-panel">
      <div class="g-section-title">⬡ Projects</div>
      <div class="g-desc">Κάθε project ανήκει σε μία εταιρεία και μία κατηγορία. Το progress υπολογίζεται αυτόματα από τα tasks.</div>
      <div class="g-subsection">Κατηγορίες</div>
      <div class="g-cards">
        <div class="g-card"><div class="g-card-icon finance">$</div><div class="g-card-title">Finance</div><div class="g-card-desc">Χρηματοοικονομικά projects</div></div>
        <div class="g-card"><div class="g-card-icon legal">⚖</div><div class="g-card-title">Legal</div><div class="g-card-desc">Νομική συμμόρφωση & contracts</div></div>
        <div class="g-card"><div class="g-card-icon dev">⌨</div><div class="g-card-title">Developing</div><div class="g-card-desc">Software development</div></div>
        <div class="g-card"><div class="g-card-icon marketing">◈</div><div class="g-card-title">Marketing</div><div class="g-card-desc">Marketing & branding</div></div>
      </div>
      <div class="g-subsection">Status</div>
      <div class="g-desc">Κάθε project έχει αυτόματο status: <strong style="color:var(--green);">on_track</strong>, <strong style="color:var(--yellow);">delayed</strong>, <strong style="color:var(--red);">at_risk</strong>. Υπολογίζεται από deadline και completion.</div>
    </div>

    <!-- TASKS TAB -->
    <div v-if="activeTab === 'tasks'" class="guide-panel">
      <div class="g-section-title">✅ Tasks & Modules</div>
      <div class="g-desc">Κάθε project χωρίζεται σε <strong>Modules</strong> (φάσεις) και κάθε module σε <strong>Tasks</strong>.</div>
      <div class="g-subsection">Task States</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-dot" style="background:var(--green);"></span><div><strong>Done</strong> — Task ολοκληρώθηκε (progress 100%)</div></div>
        <div class="g-step"><span class="g-step-dot" style="background:var(--red);"></span><div><strong>Blocked</strong> — Task έχει εμπόδιο (εμφανίζεται ⚠ στο detail)</div></div>
        <div class="g-step"><span class="g-step-dot" style="background:var(--accent);"></span><div><strong>In Progress</strong> — Task σε εξέλιξη</div></div>
      </div>
    </div>

    <!-- AI TAB -->
    <div v-if="activeTab === 'ai'" class="guide-panel">
      <div class="g-section-title" style="color:#7c3aed;">✦ AI Features</div>
      <div class="g-desc">Το Next2View ενσωματώνει AI μέσω του <strong>Claude API (Anthropic)</strong> για έξυπνη ανάλυση.</div>
      <div class="g-subsection">AI CEO Report</div>
      <div class="g-desc">Πάτα <strong>"AI Report"</strong> στο sidebar για να λάβεις αυτόματη ανάλυση όλου του portfolio — completion trends, at-risk projects, προτεινόμενες ενέργειες.</div>
      <div class="g-subsection">Contract AI Analysis</div>
      <div class="g-desc">Σε κάθε project με ανεβασμένη σύμβαση, η AI συγκρίνει τους όρους της σύμβασης με το actual progress και εντοπίζει αποκλίσεις.</div>
      <div class="g-tip">💡 Το AI Report είναι διαθέσιμο μόνο για CEO role.</div>
    </div>

    <!-- ROLES TAB -->
    <div v-if="activeTab === 'roles'" class="guide-panel">
      <div class="g-section-title">👤 Roles & Δικαιώματα</div>
      <div class="g-desc">Το Next2View έχει 3 επίπεδα πρόσβασης.</div>
      <div class="g-role-grid">
        <div class="g-role ceo">
          <div class="g-role-icon">👑</div>
          <div class="g-role-title">CEO</div>
          <div class="g-role-perms">✓ Πλήρης πρόσβαση<br>✓ Όλες οι εταιρείες<br>✓ AI Reports<br>✓ CEO private notes<br>✓ Manage users & companies</div>
        </div>
        <div class="g-role dept">
          <div class="g-role-icon">🏢</div>
          <div class="g-role-title">Department Head</div>
          <div class="g-role-perms">✓ Δικά του projects<br>✓ Update tasks<br>✓ Upload contracts<br>✗ CEO Report<br>✗ Manage users</div>
        </div>
        <div class="g-role viewer">
          <div class="g-role-icon">👁</div>
          <div class="g-role-title">Viewer</div>
          <div class="g-role-perms">✓ Δει projects & tasks<br>✗ Δεν αλλάζει τίποτα<br>✗ Δεν έχει AI access</div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('dashboard')

const tabs = [
  { id: 'dashboard', label: 'Dashboard', icon: '◈' },
  { id: 'companies', label: 'Εταιρείες', icon: '🏢' },
  { id: 'projects',  label: 'Projects',  icon: '⬡' },
  { id: 'tasks',     label: 'Tasks',     icon: '✅' },
  { id: 'ai',        label: 'AI Features', icon: '✦' },
  { id: 'roles',     label: 'Roles',     icon: '👤' },
]
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.guide-header { background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%); border: 1px solid #a7f3d0; border-radius: 14px; padding: 28px 32px; margin-bottom: 24px; }
.guide-badge { font-size: 11px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; color: #059669; margin-bottom: 8px; font-family: 'Nunito Sans', sans-serif; }
.guide-title { font-size: 28px; font-weight: 900; color: #064e3b; margin-bottom: 6px; }
.guide-sub { font-size: 14px; color: #065f46; }
.guide-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 24px; }
.guide-tab { padding: 8px 16px; border-radius: 8px; border: 1px solid var(--border-bright); background: var(--surface2); font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.15s; color: var(--text-mid); font-family: 'Nunito Sans', sans-serif; }
.guide-tab:hover { background: var(--surface3); }
.guide-tab.active { background: var(--accent-dim); border-color: var(--accent); color: var(--accent); }
.guide-tab.ai { background: rgba(124,58,237,0.08); border-color: rgba(124,58,237,0.25); color: #7c3aed; }
.guide-tab.ai.active { background: rgba(124,58,237,0.15); }
.guide-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 24px 28px; }
.g-section-title { font-size: 18px; font-weight: 900; color: var(--text); margin-bottom: 12px; }
.g-subsection { font-size: 13px; font-weight: 800; color: var(--text); margin: 18px 0 6px; font-family: 'Nunito Sans', sans-serif; }
.g-desc { font-size: 13px; color: var(--text-mid); line-height: 1.7; }
.g-tip { background: rgba(59,130,246,0.08); border: 1px solid rgba(59,130,246,0.2); border-radius: 8px; padding: 12px 16px; font-size: 13px; color: var(--accent); margin-top: 14px; font-weight: 600; }
.g-cards { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; margin-top: 14px; }
.g-card { background: var(--surface2); border: 1px solid var(--border); border-radius: 10px; padding: 16px; text-align: center; }
.g-card-icon { font-size: 22px; margin-bottom: 8px; }
.g-card-icon.finance { color: var(--finance); }
.g-card-icon.legal   { color: var(--legal); }
.g-card-icon.dev     { color: var(--dev); }
.g-card-icon.marketing { color: var(--marketing); }
.g-card-title { font-size: 13px; font-weight: 800; margin-bottom: 4px; }
.g-card-desc { font-size: 11px; color: var(--text-dim); }
.g-steps { display: flex; flex-direction: column; gap: 10px; margin-top: 12px; }
.g-step { display: flex; align-items: center; gap: 12px; font-size: 13px; color: var(--text-mid); }
.g-step-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.g-role-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; margin-top: 16px; }
.g-role { border-radius: 10px; padding: 18px; border: 1px solid var(--border); }
.g-role.ceo    { background: #eff6ff; border-color: #bfdbfe; }
.g-role.dept   { background: #ecfdf5; border-color: #a7f3d0; }
.g-role.viewer { background: var(--surface2); }
.g-role-icon { font-size: 24px; margin-bottom: 8px; }
.g-role-title { font-size: 14px; font-weight: 800; margin-bottom: 8px; }
.g-role-perms { font-size: 12px; color: var(--text-mid); line-height: 1.8; }
</style>