<template>
  <div class="content">
    <div class="guide-header">
      <div class="guide-badge">NEXT2VIEW</div>
      <div class="guide-title">📘 Οδηγός Χρήσης</div>
      <div class="guide-sub">Πλήρης οδηγός για κάθε λειτουργία της πλατφόρμας. Επίλεξε κατηγορία παρακάτω.</div>
    </div>

    <div class="guide-tabs">
      <div v-for="tab in tabs" :key="tab.id"
        :class="['guide-tab', { active: activeTab === tab.id }]"
        :style="tab.style || ''"
        @click="activeTab = tab.id">
        {{ tab.icon }} {{ tab.label }}
      </div>
    </div>

    <!-- ── DASHBOARD ── -->
    <div v-if="activeTab === 'dashboard'" class="guide-panel">
      <div class="g-section-title">◈ Dashboard — Η Κεντρική Σελίδα</div>
      <div class="g-desc">Το Dashboard είναι η πρώτη σελίδα που βλέπεις μόλις μπεις στο Next2View. Από εδώ έχεις άμεση εικόνα για όλες τις εταιρείες και τα projects χωρίς να χρειαστεί να ψάξεις πουθενά.</div>

      <div class="g-subsection">📊 KPI Strip — Τα 6 βασικά νούμερα</div>
      <div class="g-kpi-row">
        <div class="g-kpi-card"><div class="g-kpi-ico">%</div><div class="g-kpi-lbl">Overall</div><div class="g-kpi-desc">Μέσος όρος % ολοκλήρωσης όλων των active projects του group.</div></div>
        <div class="g-kpi-card finance"><div class="g-kpi-ico">$</div><div class="g-kpi-lbl">Finance</div><div class="g-kpi-desc">Μέσος % ολοκλήρωσης όλων των Finance projects.</div></div>
        <div class="g-kpi-card legal"><div class="g-kpi-ico">⚖</div><div class="g-kpi-lbl">Legal</div><div class="g-kpi-desc">Μέσος % ολοκλήρωσης όλων των Legal projects.</div></div>
        <div class="g-kpi-card dev"><div class="g-kpi-ico">⌨</div><div class="g-kpi-lbl">Developing</div><div class="g-kpi-desc">Μέσος % ολοκλήρωσης όλων των Dev projects.</div></div>
        <div class="g-kpi-card marketing"><div class="g-kpi-ico">◈</div><div class="g-kpi-lbl">Marketing</div><div class="g-kpi-desc">Μέσος % ολοκλήρωσης όλων των Marketing projects.</div></div>
        <div class="g-kpi-card risk"><div class="g-kpi-ico">⚠</div><div class="g-kpi-lbl">At Risk</div><div class="g-kpi-desc">Αριθμός projects που χρειάζονται άμεση προσοχή (blocked, stale, deadline σε κίνδυνο).</div></div>
      </div>
      <div class="g-tip">💡 Αν το "At Risk" δείχνει &gt;0, κοίτα το κόκκινο banner στην κορυφή — σου λέει ακριβώς ποια projects έχουν πρόβλημα.</div>

      <div class="g-subsection">🏢 Panel Εταιρειών</div>
      <div class="g-desc">Δείχνει κάθε εταιρεία με τον αριθμό projects και το % ολοκλήρωσης. Κάνε κλικ σε μια εταιρεία για να δεις μόνο τα projects της.</div>

      <div class="g-subsection">📅 Upcoming Deadlines</div>
      <div class="g-desc">Τα επόμενα 5 deadlines ταξινομημένα χρονολογικά με χρωματική ένδειξη:</div>
      <div class="g-deadline-badges">
        <span class="g-badge red">🔴 &lt;7 ημέρες — Urgent</span>
        <span class="g-badge yellow">🟡 7-14 ημέρες — Προσοχή</span>
        <span class="g-badge green">🟢 &gt;14 ημέρες — OK</span>
      </div>

      <div class="g-subsection">📊 Timeline Gantt</div>
      <div class="g-desc">Στο κάτω μέρος του Dashboard βλέπεις όλα τα projects σε εβδομαδιαίο Gantt chart. Η μπλε κάθετη γραμμή δείχνει την <strong>τρέχουσα εβδομάδα</strong>. Μπορείς να φιλτράρεις ανά κατηγορία με το dropdown πάνω δεξιά.</div>
      <div class="g-tip">💡 Κάνε κλικ στον τίτλο ενός project στο Gantt για να πας απευθείας στη σελίδα του.</div>
    </div>

    <!-- ── ΕΤΑΙΡΕΙΕΣ ── -->
    <div v-if="activeTab === 'companies'" class="guide-panel">
      <div class="g-section-title">🏢 Διαχείριση Εταιρειών</div>
      <div class="g-desc">Οι εταιρείες είναι η βάση της οργάνωσης. Κάθε project ανήκει σε μία εταιρεία.</div>

      <div class="g-subsection">+ Πώς προσθέτω νέα εταιρεία</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Πάτα <strong>"+ Company"</strong> στο κάτω μέρος του sidebar αριστερά.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Συμπλήρωσε το <strong>όνομα εταιρείας</strong> (π.χ. "Polaris Financial Services").</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Βάλε ένα <strong>Short Code</strong> 2-3 γραμμάτων (π.χ. "PF") — εμφανίζεται ως avatar.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Επίλεξε <strong>χρώμα</strong> για να ξεχωρίζει οπτικά.</div></div>
        <div class="g-step"><span class="g-step-num">5</span><div>Πάτα <strong>"Save Company"</strong>. Εμφανίζεται αμέσως στο Dashboard και στο sidebar.</div></div>
      </div>

      <div class="g-subsection">🗑 Πώς διαγράφω εταιρεία</div>
      <div class="g-desc">Πήγαινε στο Manage (sidebar) → Companies → πάτα × δίπλα στην εταιρεία. Προσοχή: τα projects της εταιρείας δεν διαγράφονται αυτόματα.</div>
      <div class="g-tip">💡 Το χρώμα κάθε εταιρείας εμφανίζεται συνεχώς παντού — dashboard, progress bars, labels. Επίλεξε χρώματα που ξεχωρίζουν μεταξύ τους.</div>
    </div>

    <!-- ── PROJECTS ── -->
    <div v-if="activeTab === 'projects'" class="guide-panel">
      <div class="g-section-title">⬡ Διαχείριση Projects</div>
      <div class="g-desc">Κάθε project αντιπροσωπεύει μία εργασία ή ανάπτυξη που παρακολουθείς. Ένα project έχει κατηγορία, budget, deadline, modules και tasks.</div>

      <div class="g-subsection">+ Πώς δημιουργώ νέο project</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Πάτα <strong>"+ New Project"</strong> στο sidebar ή στην κορυφή δεξιά.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Συμπλήρωσε τον <strong>τίτλο</strong> (π.χ. "IMO CII Compliance 2026").</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Επίλεξε <strong>Εταιρεία</strong> και <strong>Κατηγορία</strong> (Finance / Legal / Developing / Marketing).</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Βάλε <strong>Budget</strong> (€) και <strong>Deadline</strong> (ημερομηνία).</div></div>
        <div class="g-step"><span class="g-step-num">5</span><div>Γράψε σύντομη <strong>περιγραφή σύμβασης</strong> — τι αφορά το project.</div></div>
        <div class="g-step"><span class="g-step-num">6</span><div>Πρόσθεσε <strong>Specifications</strong> — checklist με τα deliverables (π.χ. "Υποβολή εγγράφων Flag State").</div></div>
        <div class="g-step"><span class="g-step-num">7</span><div>Δημιούργησε <strong>Modules</strong> — οι φάσεις του project (π.χ. "Assessment Phase", "Submission", "Training").</div></div>
        <div class="g-step"><span class="g-step-num">8</span><div>Μέσα σε κάθε Module πρόσθεσε <strong>Tasks</strong> με assignee, deadline και % ολοκλήρωσης.</div></div>
        <div class="g-step"><span class="g-step-num">9</span><div>Πάτα <strong>"Save Project"</strong>. Εμφανίζεται αμέσως στο Dashboard και στο Timeline.</div></div>
      </div>

      <div class="g-subsection">✏ Πώς επεξεργάζομαι project</div>
      <div class="g-desc">Από το <strong>Project Detail</strong> (κλικ σε project) → πάτα το ✎ εικονίδιο πάνω δεξιά στο contract header. Ανοίγει το ίδιο form με όλα τα στοιχεία προγεμισμένα.</div>

      <div class="g-subsection">🔍 Πώς φιλτράρω projects</div>
      <div class="g-desc">Στο "All Projects" view υπάρχουν 2 dropdowns πάνω δεξιά: ένα για κατηγορία και ένα για εταιρεία. Μπορείς επίσης να κάνεις κλικ σε κατηγορία ή εταιρεία από το sidebar.</div>

      <div class="g-subsection">📊 Status ενός project</div>
      <div class="g-status-row">
        <span class="g-status on-track">🟢 On Track — Στο σωστό δρόμο</span>
        <span class="g-status delayed">🟡 Delayed — Καθυστέρηση</span>
        <span class="g-status at-risk">🔴 At Risk — Blocked ή deadline κοντά με χαμηλό %</span>
        <span class="g-status stale">🔴 Stale — Δεν ενημερώθηκε &gt;5 ημέρες</span>
      </div>
      <div class="g-tip">💡 Το status υπολογίζεται αυτόματα. Δεν χρειάζεται να το ορίσεις χειροκίνητα.</div>
    </div>

    <!-- ── TASKS ── -->
    <div v-if="activeTab === 'tasks'" class="guide-panel">
      <div class="g-section-title">✅ Tasks &amp; Modules</div>
      <div class="g-desc">Τα Tasks είναι οι μικρότερες μονάδες εργασίας. Οργανώνονται σε <strong>Modules</strong> (φάσεις). Το % κάθε Module = μέσος όρος tasks. Το % κάθε Project = μέσος όρος modules.</div>

      <div class="g-subsection">🏗 Δομή: Project → Module → Task</div>
      <div class="g-tree">
        <div class="g-tree-project">⬡ Project — π.χ. "ATLANTIS ERP" <span class="g-tree-pct">(88% = μέσος όρος modules)</span></div>
        <div class="g-tree-module">▦ Module — π.χ. "Core Modules" <span class="g-tree-pct">(100% = μέσος όρος tasks)</span></div>
        <div class="g-tree-task">· Task — π.χ. "Dashboard" ✓ <span class="g-tree-pct" style="color:var(--green);">100%</span></div>
        <div class="g-tree-task">· Task — π.χ. "Payroll calculator" <span class="g-tree-pct" style="color:var(--accent);">40%</span></div>
      </div>


      <div class="g-subsection">📊 Γράφημα Δομής Project</div>
      <div class="g-chart-wrap">
        <svg width="100%" viewBox="0 0 680 580" role="img" style="max-width:680px;display:block;margin:0 auto;">
          <title>Next2View — Δομή Project Schedule</title>
          <defs><marker id="garrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse"><path d="M2 1L8 5L2 9" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></marker></defs>
          <!-- PROJECT -->
          <g>
            <rect x="200" y="30" width="280" height="48" rx="10" fill="#7F77DD" fill-opacity="0.15" stroke="#7F77DD" stroke-width="1"/>
            <text x="340" y="51" text-anchor="middle" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#7F77DD">📋 Project</text>
            <text x="340" y="68" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#7F77DD">Τίτλος · Εταιρεία · startDate · deadline</text>
          </g>
          <!-- Lines Project to Modules -->
          <path d="M340 78 L340 95 L190 95 L190 110" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow)"/>
          <path d="M340 78 L340 95 L490 95 L490 110" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow)"/>
          <!-- MODULE 1 -->
          <g>
            <rect x="80" y="110" width="220" height="44" rx="8" fill="#1D9E75" fill-opacity="0.12" stroke="#1D9E75" stroke-width="1"/>
            <text x="190" y="128" text-anchor="middle" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#1D9E75">Module 1</text>
            <text x="190" y="144" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#1D9E75">Χρώμα · sortOrder</text>
          </g>
          <!-- MODULE 2 -->
          <g>
            <rect x="380" y="110" width="220" height="44" rx="8" fill="#1D9E75" fill-opacity="0.12" stroke="#1D9E75" stroke-width="1"/>
            <text x="490" y="128" text-anchor="middle" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#1D9E75">Module 2</text>
            <text x="490" y="144" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#1D9E75">Χρώμα · sortOrder</text>
          </g>
          <!-- Lines Modules to Tasks -->
          <path d="M190 154 L190 170 L120 170 L120 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow)"/>
          <path d="M190 154 L190 170 L300 170 L300 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow)"/>
          <path d="M490 154 L490 170 L420 170 L420 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow)"/>
          <path d="M490 154 L490 170 L590 170 L590 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow)"/>
          <!-- TASKS -->
          <g><rect x="40" y="190" width="160" height="40" rx="6" fill="#888" fill-opacity="0.1" stroke="#888" stroke-width="0.5"/><text x="120" y="207" text-anchor="middle" font-family="Nunito,sans-serif" font-size="12" font-weight="700" fill="#555">Task A</text><text x="120" y="221" text-anchor="middle" font-family="Nunito,sans-serif" font-size="10" fill="#888">Assignee · %</text></g>
          <g><rect x="220" y="190" width="160" height="40" rx="6" fill="#888" fill-opacity="0.1" stroke="#888" stroke-width="0.5"/><text x="300" y="207" text-anchor="middle" font-family="Nunito,sans-serif" font-size="12" font-weight="700" fill="#555">Task B</text><text x="300" y="221" text-anchor="middle" font-family="Nunito,sans-serif" font-size="10" fill="#888">Assignee · %</text></g>
          <g><rect x="340" y="190" width="160" height="40" rx="6" fill="#888" fill-opacity="0.1" stroke="#888" stroke-width="0.5"/><text x="420" y="207" text-anchor="middle" font-family="Nunito,sans-serif" font-size="12" font-weight="700" fill="#555">Task C</text><text x="420" y="221" text-anchor="middle" font-family="Nunito,sans-serif" font-size="10" fill="#888">Assignee · %</text></g>
          <g><rect x="520" y="190" width="140" height="40" rx="6" fill="#888" fill-opacity="0.1" stroke="#888" stroke-width="0.5"/><text x="590" y="207" text-anchor="middle" font-family="Nunito,sans-serif" font-size="12" font-weight="700" fill="#555">Task D</text><text x="590" y="221" text-anchor="middle" font-family="Nunito,sans-serif" font-size="10" fill="#888">Assignee · %</text></g>
          <!-- GANTT -->
          <rect x="20" y="270" width="640" height="240" rx="10" fill="none" stroke="#ccc" stroke-width="0.5" stroke-dasharray="4 4"/>
          <text x="40" y="295" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#555">📊 Gantt Timeline</text>
          <rect x="40" y="305" width="600" height="24" rx="4" fill="#f0f0f0"/>
          <text x="180" y="321" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#888">W1 · startDate</text>
          <text x="380" y="321" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#888">W2</text>
          <text x="570" y="321" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#888">W3 · deadline</text>
          <text x="40" y="348" font-family="Nunito,sans-serif" font-size="11" fill="#888">Module 1</text>
          <rect x="140" y="337" width="200" height="16" rx="3" fill="#1D9E75" opacity="0.8"/>
          <text x="40" y="376" font-family="Nunito,sans-serif" font-size="11" fill="#888">Module 2</text>
          <rect x="340" y="365" width="240" height="16" rx="3" fill="#1D9E75" opacity="0.8"/>
          <text x="40" y="404" font-family="Nunito,sans-serif" font-size="11" fill="#888">Task A</text>
          <rect x="140" y="393" width="100" height="12" rx="3" fill="#1D9E75" opacity="0.6"/>
          <text x="40" y="422" font-family="Nunito,sans-serif" font-size="11" fill="#888">Task B</text>
          <rect x="240" y="411" width="100" height="12" rx="3" fill="#1D9E75" opacity="0.6"/>
          <text x="40" y="440" font-family="Nunito,sans-serif" font-size="11" fill="#888">Task C</text>
          <rect x="340" y="429" width="120" height="12" rx="3" fill="#1D9E75" opacity="0.6"/>
          <text x="40" y="458" font-family="Nunito,sans-serif" font-size="11" fill="#888">Task D</text>
          <rect x="460" y="447" width="120" height="12" rx="3" fill="#1D9E75" opacity="0.6"/>
          <line x1="310" y1="305" x2="310" y2="465" stroke="#63B3ED" stroke-width="1.5" stroke-dasharray="3 3"/>
          <text x="313" y="478" font-family="Nunito,sans-serif" font-size="10" fill="#63B3ED">Σήμερα</text>
          <rect x="40" y="488" width="620" height="16" rx="5" fill="#EFF6FF"/>
          <text x="350" y="500" text-anchor="middle" font-family="Nunito,sans-serif" font-size="10" fill="#63B3ED">Auto progress: (σήμερα − taskStart) / (taskEnd − taskStart) × 100</text>
        </svg>
      </div>

      <div class="g-subsection">⚙️ Πώς υπολογίζεται το Progress</div>
      <div class="g-progress-rules">
        <div class="g-progress-rule">
          <div class="g-progress-rule-num">1</div>
          <div><strong>Κανείς δεν άγγιξε το task</strong> → progress = auto<br><span style="color:var(--text-dim);font-size:12px;">Ανεβαίνει μόνο του κάθε μέρα: (ημέρες που πέρασαν / συνολικές) × 100</span></div>
        </div>
        <div class="g-progress-rule">
          <div class="g-progress-rule-num">2</div>
          <div><strong>Κάποιος έβαλε manual %</strong> → το manual κερδίζει, το auto σταματά<br><span style="color:var(--text-dim);font-size:12px;">Π.χ. αν βάλεις 30%, παραμένει 30% ανεξάρτητα από τις ημέρες.</span></div>
        </div>
        <div class="g-progress-rule warning">
          <div class="g-progress-rule-num" style="background:var(--red-dim);color:var(--red);">⚠</div>
          <div><strong>Manual &lt; Auto − 20%</strong> → ειδοποίηση στον CEO<br><span style="color:var(--text-dim);font-size:12px;">Π.χ. Day 8/10 → auto=80%, manual=30% → διαφορά 50% → ⚠ "Task είναι πίσω από το πρόγραμμα"</span></div>
        </div>
        <div class="g-progress-rule success">
          <div class="g-progress-rule-num" style="background:var(--green);color:#fff;">✓</div>
          <div><strong>Πατάει ✅ checkbox</strong> → 100% αμέσως, κλείνει η εργασία</div>
        </div>
      </div>
      <div class="g-tip">💡 Το ⚠ εμφανίζεται δίπλα στο % του task στο Project Detail — ο CEO βλέπει αμέσως ποια tasks είναι πίσω από το πρόγραμμα.</div>
      <div class="g-subsection">✏ Πώς ενημερώνω ένα task</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Πήγαινε στο <strong>Project Detail</strong> (κλικ στο project).</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Κάνε κλικ στον τίτλο ενός <strong>Module</strong> για να το ανοίξεις/κλείσεις.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Βλέπεις κάθε task με: <strong>checkmark</strong>, όνομα, assignee, progress bar και %.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Για να αλλάξεις %, πήγαινε <strong>Edit Project (✎)</strong> → αλλάζεις το % του task → Save.</div></div>
      </div>

      <div class="g-subsection">💬 Πώς προσθέτω σχόλιο σε task</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Στο Project Detail, άνοιξε ένα Module.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Πάτα το 💬 εικονίδιο δεξιά σε κάθε task.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Γράψε το σχόλιο και πάτα <strong>"Αποθήκευση"</strong>.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Το σχόλιο εμφανίζεται ως preview κάτω από το task name και το AI το λαμβάνει υπόψη στις αναφορές.</div></div>
      </div>

      <div class="g-subsection">🚫 Πώς σημαδεύω ένα task ως Blocked</div>
      <div class="g-desc">Στο Edit Project → βάλε στο task note τη λέξη "BLOCK" και την αιτία (π.χ. "BLOCK — αναμένει έγγραφα από Flag State"). Το task εμφανίζεται με κόκκινο εικονίδιο και το project περνά σε status "At Risk".</div>
      <div class="g-tip">💡 Ένα task με 100% θεωρείται αυτόματα ✓ Done. Δεν χρειάζεται να κάνεις τίποτα άλλο.</div>
    </div>

    <!-- ── ΣΥΜΒΑΣΕΙΣ ── -->
    <div v-if="activeTab === 'contracts'" class="guide-panel">
      <div class="g-section-title">📄 Διαχείριση Συμβάσεων</div>
      <div class="g-desc">Κάθε project μπορεί να έχει ανεβασμένη την υπογεγραμμένη του σύμβαση. Το AI εξάγει αυτόματα τα βασικά στοιχεία και τα εμφανίζει ως διαμορφωμένη καρτέλα.</div>

      <div class="g-subsection">📎 Πώς ανεβάζω σύμβαση</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Πήγαινε στο <strong>Project Detail</strong> του project που θέλεις.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Βρες το panel <strong>"Υπογεγραμμένη Σύμβαση"</strong> στη δεξιά στήλη.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Πάτα <strong>"Ανέβασε κώρα"</strong> ή κάνε κλικ drag &amp; drop το αρχείο.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Υποστηρίζονται μορφές <strong>PDF, JPG, PNG</strong> έως 10MB.</div></div>
        <div class="g-step"><span class="g-step-num">5</span><div>Μόλις ανέβει, το AI ξεκινά αυτόματα την εξαγωγή στοιχείων.</div></div>
      </div>

      <div class="g-subsection">🤖 Τι εξάγει αυτόματα το AI</div>
      <div class="g-kpi-row">
        <div class="g-kpi-card"><div class="g-kpi-ico">👤</div><div class="g-kpi-lbl">Μέρη</div><div class="g-kpi-desc">Τα συμβαλλόμενα μέρη (πελάτης + πάροχος).</div></div>
        <div class="g-kpi-card finance"><div class="g-kpi-ico">€</div><div class="g-kpi-lbl">Αξία</div><div class="g-kpi-desc">Συνολική αξία σύμβασης.</div></div>
        <div class="g-kpi-card legal"><div class="g-kpi-ico">📅</div><div class="g-kpi-lbl">Ημερομηνίες</div><div class="g-kpi-desc">Έναρξη και λήξη σύμβασης.</div></div>
        <div class="g-kpi-card dev"><div class="g-kpi-ico">📋</div><div class="g-kpi-lbl">Υποχρεώσεις</div><div class="g-kpi-desc">Κύριες υποχρεώσεις αντισυμβαλλομένων.</div></div>
        <div class="g-kpi-card risk"><div class="g-kpi-ico">⚠</div><div class="g-kpi-lbl">Ρήτρες</div><div class="g-kpi-desc">Ποινές και ρήτρες του υπάρχει.</div></div>
        <div class="g-kpi-card marketing"><div class="g-kpi-ico">⚖</div><div class="g-kpi-lbl">Δίκαιο</div><div class="g-kpi-desc">Εφαρμοστέο δίκαιο και δικαιοδοσία.</div></div>
      </div>

      <div class="g-subsection">🖨 Πώς εκτυπώνω / κάνω export PDF</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Μόλις εξήχθηκαν τα στοιχεία, εμφανίζεται κουμπί <strong>"Export / Print"</strong> στην κορυφή της καρτέλας.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Πάτα το κουμπί — ανοίγει νέα σελίδα με print-ready layout.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Πάτα <strong>"Εκτύπωση / Αποθήκευση ως PDF"</strong> — από το dialog του browser επίλεξε "Save as PDF".</div></div>
      </div>
      <div class="g-tip">💡 Αν τα στοιχεία που εξήχθηκαν δεν φαίνονται σωστά (π.χ. λείπει γλώσσα ή νομισματική μονάδα), πάτα <strong>"AI Ανάλυση"</strong> για να επαναλάβεις την εξαγωγή.</div>
    </div>

    <!-- ── AI FEATURES ── -->
    <div v-if="activeTab === 'ai'" class="guide-panel">
      <div class="g-section-title" style="color:#7c3aed;">✦ AI Features — Powered by Claude</div>
      <div class="g-desc">Το Next2View ενσωματώνει AI μέσω του <strong>Claude API (Anthropic)</strong>. Τρία AI εργαλεία είναι διαθέσιμα από διαφορετικά σημεία της εφαρμογής.</div>

      <div class="g-ai-section">
        <div class="g-ai-title">🤖 AI CEO Report</div>
        <div class="g-desc">Πάτα <strong>"AI Report"</strong> στο sidebar για να λάβεις αυτόματη ανάλυση όλου του portfolio.</div>
        <div class="g-subsection">Πώς το χρησιμοποιείς</div>
        <div class="g-steps">
          <div class="g-step"><span class="g-step-num">1</span><div>Πάτα <strong>"AI Report"</strong> στο sidebar αριστερά.</div></div>
          <div class="g-step"><span class="g-step-num">2</span><div>Το AI αναλύει <strong>όλα τα projects</strong>: status, deadlines, Finance, Legal κλπ.</div></div>
          <div class="g-step"><span class="g-step-num">3</span><div>Σε 20 δευτερόλεπτα λαμβάνεις <strong>αναλυτική αναφορά</strong> με highlights και προτεινόμενες ενέργειες.</div></div>
        </div>
        <div class="g-subsection">Τι περιλαμβάνει η αναφορά</div>
        <div class="g-status-row" style="flex-wrap:wrap;">
          <span class="g-status on-track">✓ Overall completion</span>
          <span class="g-status delayed">⚠ Καθυστερημένα projects</span>
          <span class="g-status at-risk">🔴 Top 3 At Risk items</span>
          <span class="g-status stale">📊 Ανά κατηγορία ανάλυση</span>
          <span class="g-status on-track">💡 Προτεινόμενες ενέργειες</span>
        </div>
        <div class="g-tip">💡 Το "AI Report" είναι διαθέσιμο μόνο για <strong>CEO role</strong> — δεν εμφανίζεται σε Dept Head ή Viewer.</div>
      </div>

      <div class="g-ai-section" style="margin-top:20px;">
        <div class="g-ai-title">📄 AI Κλήρωση Στοιχείων Σύμβασης</div>
        <div class="g-desc">Μόλις ανεβάσεις σύμβαση (PDF/JPG/PNG), το AI εξάγει αυτόματα τα βασικά στοιχεία και τα εμφανίζει σε δομημένη καρτέλα μέσα στο Project Detail.</div>
        <div class="g-tip">💡 Υποστηρίζει PDF και εικόνες. Αναγνωρίζει Ελληνικά και Αγγλικά κείμενα.</div>
        <div class="g-subsection">📋 Παράδειγμα — Έτσι φαίνεται το αποτέλεσμα</div>
        <div class="g-contract-preview">
          <div class="g-cp-header">
            <div class="g-cp-title">Ενεργός Σύμβαση <span class="g-cp-badge">✓ Προεπισκόπηση</span></div>
            <div class="g-cp-actions">
              <span class="g-cp-btn ai">✦ AI Ανάλυση</span>
              <span class="g-cp-btn">Export</span>
              <span class="g-cp-btn">εκτύπωση</span>
            </div>
          </div>
          <div class="g-cp-body">
            <div class="g-cp-desc">Παροχή υπηρεσιών ανάπτυξης ολοκληρωμένου ERP συστήματος διαχείρισης πληρωμάτων και στόλου για θαλάσσιες μεταφορές, περιλαμβανομένης εκπαίδευσης χρηστών και υποστήριξης 12 μηνών.</div>
            <div class="g-cp-parties">
              <span class="g-cp-party">🏢 Crossworld Marine Services Itd</span>
              <span class="g-cp-party">🏢 OceanSoft Ltd</span>
            </div>
            <div class="g-cp-grid">
              <div class="g-cp-field">
                <div class="g-cp-label">ΑΞΙ ΣΥΜΒΑΣΗΣ</div>
                <div class="g-cp-value finance">€45,000 EUR</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">ΟΡΟΙ ΠΛΗΡΩΜΗΣ</div>
                <div class="g-cp-value small">30% προκαταβολή, 40% παράδοση beta, 30% τελική παράδοση</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">ΗΜΕΡ. ΕΝΑΡΞΗΣ</div>
                <div class="g-cp-value">1 Φεβρουαρίου 2026</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">ΗΜΕΡΟΜΗΝΙΑ ΛΗΞΗΣ</div>
                <div class="g-cp-value risk">20 Απριλίου 2026</div>
              </div>
            </div>
            <div class="g-cp-obligations">
              <div class="g-cp-obl-title">ΚΥΡΙΕΣ ΥΠΟΧΡΕΩΣΕΙΣ</div>
              <div class="g-cp-obl-item">1. Ανάπτυξη και παράδοση πλήρους ERP με modules Dashboard, Roster, RPA Hub, Payroll, Sign-Off</div>
              <div class="g-cp-obl-item">2. Εκπαίδευση χρηστών μέχρι 3 ημέρες πριν την τελική παράδοση</div>
              <div class="g-cp-obl-item">3. Τεχνική υποστήριξη 12 μηνών μετά την παράδοση, SLA 24 ωρών</div>
              <div class="g-cp-obl-item">4. Παράδοση πλήρους τεκμηρίωσης και source code σε αποθετήριο αποστολέα</div>
            </div>
            <div class="g-cp-penalty">
              <div class="g-cp-penalty-label">⚠ ΡΗΤΡΕΣ / ΠΟΙΝΕΣ</div>
              <div class="g-cp-penalty-val">Καθυστέρηση παράδοσης: 0,5% της συμβατικής αξίας ανά εβδομάδα καθυστέρησης, μέχρι 10% του συνόλου. Μη συμμόρφωση SLA: αποζημίωση 1% ανά παρέβαση</div>
            </div>
            <div class="g-cp-footer-grid">
              <div class="g-cp-field">
                <div class="g-cp-label">ΕΦΑΡΜΟΣΤΕΟ ΔΙΚΑΙΟ</div>
                <div class="g-cp-value small">Κυπριακό Δίκαιο — Δικαστήρια Λεμεσού</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">ΕΙΔΙΚΟΙ ΟΡΟΙ</div>
                <div class="g-cp-value small">Αποκλειστικότητα χρήσης κώδικα για 24 μήνες. NDA ισχύς 5 ετών μετά λήξη.</div>
              </div>
            </div>
            <div class="g-cp-notice">* Τα αποτελέσματα εξήχθηκαν αυτόματα από το Project detail και δεν αντικαθιστούν νομική συμβουλή.</div>
          </div>
        </div>
      </div>

      <div class="g-ai-section" style="margin-top:20px;">
        <div class="g-ai-title">🔍 AI Ανάλυση Σύμβασης vs Πρόοδος</div>
        <div class="g-desc">Μόλις υπάρχει ανεβασμένη σύμβαση σε ένα project, μπορείς να ζητήσεις σύγκριση των όρων της σύμβασης με το actual progress.</div>
        <div class="g-steps">
          <div class="g-step"><span class="g-step-num">1</span><div>Άνοιξε το project detail.</div></div>
          <div class="g-step"><span class="g-step-num">2</span><div>Πάτα το κουμπί <strong>"AI Ανάλυση Σύμβασης"</strong> στην καρτέλα σύμβασης.</div></div>
          <div class="g-step"><span class="g-step-num">3</span><div>Το AI συγκρίνει τι υπόσχεται η σύμβαση vs τι έχει γίνει πραγματικά.</div></div>
        </div>
        <div class="g-status-row">
          <span class="g-status on-track">🟢 Σύμφωνο</span>
          <span class="g-status delayed">🟡 Μερική Απόκλιση</span>
          <span class="g-status at-risk">🔴 Σοβαρή Απόκλιση</span>
        </div>
      </div>
    </div>

    <!-- ── NOTIFICATIONS ── -->
    <div v-if="activeTab === 'notifications'" class="guide-panel">
      <div class="g-section-title">🔔 Σύστημα Notifications</div>
      <div class="g-desc">Το Next2View έχει αυτόματο σύστημα για κάθε κρίσιμο event — χωρίς να χρειάζεται να ανανεώσεις το dashboard. Κάθε 5 λεπτά ελέγχονται όλα τα active projects.</div>

      <div class="g-subsection">🔔 To Bell Icon</div>
      <div class="g-desc">Εμφανίζεται στο κάτω μέρος του sidebar. Όταν υπάρχουν νέα notifications, εμφανίζεται κόκκινο badge με τον αριθμό των αδιάβαστων. Κλικ σε αυτό για να τα εξετάσεις.</div>
      <div class="g-tip">💡 Το badge <strong>εξαφανίζεται</strong> μόλις ανοίξεις τον πίνακα. Μόλις κλείσεις, τα notifications συνεχίζουν να λαμβάνονται.</div>

      <div class="g-subsection">⚡ Αυτόματα Triggers</div>
      <div class="g-notif-cards">
        <div class="g-notif-card red">
          <div class="g-notif-card-title">⏰ Deadline Risk <span class="g-notif-badge red">URGENT</span></div>
          <div class="g-notif-card-desc"><strong>Πότε:</strong> Λιγότερο από 7 ημέρες στο deadline και πρόοδος κάτω από 80%.</div>
          <div class="g-notif-card-desc"><strong>Τι σημαίνει:</strong> Το project κινδυνεύει να μη παραδοθεί έγκαιρα. Απαιτείται άμεση δράση.</div>
        </div>
        <div class="g-notif-card yellow">
          <div class="g-notif-card-title">😴 Stale Project <span class="g-notif-badge yellow">WARNING</span></div>
          <div class="g-notif-card-desc"><strong>Πότε:</strong> Το project δεν ενημερώθηκε για παραπάνω από 5 ημέρες.</div>
          <div class="g-notif-card-desc"><strong>Τι σημαίνει:</strong> Πιθανώς ξεχάστηκε ή εκκρεμεί. Κοίτα αν χρειάζεται follow-up.</div>
        </div>
        <div class="g-notif-card blue">
          <div class="g-notif-card-title">📈 Slow Progress Gap <span class="g-notif-badge blue">NOTICE</span></div>
          <div class="g-notif-card-desc"><strong>Πότε:</strong> Έχει περάσει περισσότερο από 70% της χρονικής περιόδου αλλά η πρόοδος είναι κάτω από 50%.</div>
          <div class="g-notif-card-desc"><strong>Τι σημαίνει:</strong> Ο ρυθμός δουλειάς δεν ακολουθεί τις προθεσμίες. Πρόκειται να καθυστερήσει.</div>
        </div>
        <div class="g-notif-card red">
          <div class="g-notif-card-title">🚫 Blocked Task <span class="g-notif-badge red">URGENT</span></div>
          <div class="g-notif-card-desc"><strong>Πότε:</strong> Οποιοδήποτε task σημειώνεται ως "BLOCK" με αιτία.</div>
          <div class="g-notif-card-desc"><strong>Τι σημαίνει:</strong> Κάτι εμποδίζει την εξέλιξη. Απαιτείται παρέμβαση από τον CEO.</div>
        </div>
        <div class="g-notif-card green">
          <div class="g-notif-card-title">📉 Under-billed <span class="g-notif-badge green">INFO</span></div>
          <div class="g-notif-card-desc"><strong>Πότε:</strong> Η πρόοδος του project είναι πάνω από 30% αλλά δεν έχει γίνει χρέωση.</div>
          <div class="g-notif-card-desc"><strong>Τι σημαίνει:</strong> Κίνδυνος απώλειας εσόδων — έλεγξε τις πληρωμές.</div>
        </div>
      </div>

      <div class="g-subsection">📩 Toast Notifications</div>
      <div class="g-desc">Κάθε νέο notification εμφανίζεται ως <strong>toast</strong> κάτω δεξιά της οθόνης για 5 δευτερόλεπτα. Κάνε κλικ πάνω του για να πας απευθείας στο project που αφορά.</div>

      <div class="g-subsection">🗑 Διαχείριση Notifications</div>
      <div class="g-notif-mgmt">
        <div class="g-mgmt-card"><div class="g-mgmt-ico">🔔</div><div class="g-mgmt-title">Άνοιγμα panel</div><div class="g-mgmt-desc">Κλικ στο bell icon στο sidebar. Βλέπεις όλα τα notifications με χρωματική κωδικοποίηση.</div></div>
        <div class="g-mgmt-card"><div class="g-mgmt-ico">👆</div><div class="g-mgmt-title">Πήγαινε στο project</div><div class="g-mgmt-desc">Κλικ σε οποιοδήποτε notification σε πηγαίνει απευθείας στο Project Detail.</div></div>
        <div class="g-mgmt-card"><div class="g-mgmt-ico">✓</div><div class="g-mgmt-title">Clear All</div><div class="g-mgmt-desc">Πάτα "Clear all" στο panel για να καθαρίσεις όλα τα notifications.</div></div>
      </div>
      <div class="g-tip">⏰ <strong>Συχνότητα ελέγχου:</strong> Κάθε <strong>5 λεπτά</strong> αυτόματα. Ο έλεγχος γίνεται και αμέσως μόλις ανοίξεις την εφαρμογή.</div>
    </div>

    <!-- ── ROLES ── -->
    <div v-if="activeTab === 'roles'" class="guide-panel">
      <div class="g-section-title">👤 Roles &amp; Δικαιώματα</div>
      <div class="g-desc">Το Next2View έχει 3 επίπεδα πρόσβασης. Κάθε χρήστης έχει ακριβώς αυτά που χρειάζεται — τίποτα παραπάνω.</div>

      <div class="g-role-grid">
        <div class="g-role ceo">
          <div class="g-role-icon">👑</div>
          <div class="g-role-title">CEO</div>
          <div class="g-role-perms">✓ Πλήρης πρόσβαση<br>✓ Όλες οι εταιρείες<br>✓ AI Reports<br>✓ CEO private notes<br>✓ Manage users<br>✓ Manage companies<br>✓ Διαγραφή projects</div>
        </div>
        <div class="g-role dept">
          <div class="g-role-icon">🏢</div>
          <div class="g-role-title">Department Head</div>
          <div class="g-role-perms">✓ Projects της κατηγορίας του<br>✓ Cross-company πρόσβαση<br>✓ Update tasks<br>✓ Upload/Delete αρχεία<br>✓ Δημιουργία projects<br>✕ AI Reports<br>✕ CEO notes<br>✕ Διαγραφή projects<br>✕ Manage users</div>
        </div>
        <div class="g-role viewer">
          <div class="g-role-icon">👁</div>
          <div class="g-role-title">Viewer</div>
          <div class="g-role-perms">✓ Δει projects<br>✓ Δει tasks<br>✗ Δεν αλλάζει τίποτα<br>✗ Δεν ανεβάζει files<br>✗ Δεν έχει AI access<br>✗ Δεν βλέπει CEO notes</div>
        </div>
      </div>
        <div class="g-explainer-box">
          <div class="g-explainer-title">💡 Πώς λειτουργεί η πρόσβαση Department Head</div>
          <div class="g-explainer-text">
            Ο κάθε Department Head "χρεώνεται" <strong>μία κατηγορία</strong> (Legal, Developing, Finance, ή Marketing). Βλέπει <strong>όλα τα projects</strong> αυτής της κατηγορίας σε <strong>όλες τις εταιρείες</strong> του ομίλου.
          </div>
          <div class="g-explainer-example">
            📌 <strong>Παράδειγμα:</strong> Ένας Legal Department Head ανήκει ως υπάλληλος σε μία εταιρεία, αλλά βλέπει νομικά projects από κάθε εταιρεία του ομίλου (Crossworld, Polaris, Creworld, κ.λπ.).
          </div>
          <div class="g-explainer-note">
            Η εταιρεία στην οποία ανήκει ο χρήστης είναι μόνο <em>HR info</em> — δεν περιορίζει τι projects βλέπει.
          </div>
        </div>

      <div class="g-subsection">➕ Πώς προσθέτω νέο χρήστη</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Πάτα <strong>"+ User"</strong> στο sidebar.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Συμπλήρωσε <strong>Όνομα</strong> και <strong>Ρόλο</strong>.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Επίλεξε <strong>Εταιρεία</strong> και <strong>Department</strong> (Finance/Legal/Dev/Marketing/Management).</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Πάτα <strong>"Save User"</strong>. Ο χρήστης εμφανίζεται στο Manage.</div></div>
      </div>
      <div class="g-tip">💡 Το CEO role έχει πρόσβαση στο <strong>CEO Private Notes</strong> — σημειώσεις που μπαίνουν στο Project Detail και δεν τις βλέπουν άλλοι χρήστες.</div>
    </div>

    <!-- ── ΓΛΩΣΣΑΡΙΟ ── -->
    <div v-if="activeTab === 'glossary'" class="guide-panel">
      <div class="g-section-title">📖 Γλωσσάριο</div>
      <div class="g-desc">Βασικοί όροι που χρησιμοποιούνται στο Next2View.</div>
      <div class="g-glossary">
        <div v-for="term in glossary" :key="term.word" class="g-gloss-row">
          <div class="g-gloss-term">{{ term.word }}</div>
          <div class="g-gloss-def">{{ term.def }}</div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('dashboard')

const tabs = [
  { id: 'dashboard',     label: 'Dashboard',    icon: '◈' },
  { id: 'companies',     label: 'Εταιρείες',    icon: '🏢' },
  { id: 'projects',      label: 'Projects',     icon: '⬡' },
  { id: 'tasks',         label: 'Tasks',        icon: '✅' },
  { id: 'contracts',     label: 'Συμβάσεις',    icon: '📄' },
  { id: 'ai',            label: 'AI Features',  icon: '✦', style: 'background:rgba(139,92,246,0.1);border-color:rgba(139,92,246,0.3);color:#7c3aed;' },
  { id: 'notifications', label: 'Notifications', icon: '🔔', style: 'background:rgba(220,38,38,0.07);border-color:rgba(220,38,38,0.25);color:var(--red);' },
  { id: 'roles',         label: 'Roles',        icon: '👤' },
  { id: 'glossary',      label: 'Γλωσσάριο',   icon: '📖' },
]

const glossary = [
  { word: 'Project',          def: 'Μία εργασία ή ανάπτυξη που παρακολουθείς (π.χ. "IMO CII Compliance", "Hivebee MVP"). Κάθε project ανήκει σε εταιρεία και κατηγορία.' },
  { word: 'Module',           def: 'Φάση ή ενότητα ενός project (π.χ. "Assessment Phase", "Frontend UI"). Περιέχει tasks.' },
  { word: 'Task',             def: 'Η μικρότερη μονάδα εργασίας. Έχει assignee, όρες προς ολοκλήρωσης. (0-100%).' },
  { word: '% Completion',     def: 'Task % → Module % (μέσος όρος tasks) → Project % (μέσος όρος modules). Υπολογίζεται αυτόματα.' },
  { word: 'Status',           def: 'Αυτόματη κατάσταση project: On Track / Delayed / At Risk / Stale. Βασίζεται στο %, deadline και blockers.' },
  { word: 'Stale',            def: 'Project που δεν έχει ενημερωθεί για παραπάνω από 5 ημέρες. Εμφανίζεται με κίτρινο warning.' },
  { word: 'Blocked',          def: 'Task που δεν μπορεί να προχωρήσει λόγω εξωτερικού παράγοντα. Σημαδεύεται με "BLOCK" στο note.' },
  { word: 'At Risk',          def: 'Project με blocked task, ή deadline <7 ημέρες με χαμηλό %, ή status >5 ημέρες.' },
  { word: 'KPI',              def: 'Key Performance Indicator — τα βασικά νούμερα που βλέπεις στα cards του Dashboard.' },
  { word: 'Gantt Timeline',   def: 'Εβδομαδιαίο γράφημα που δείχνει πότε τρέχει κάθε task. Η μπλε γραμμή = σήμερα.' },
  { word: 'Contract Summary', def: 'Αυτόματη εξαγωγή βασικών στοιχείων από ανεβασμένη σύμβαση (PDF/εικόνα) μέσω AI.' },
  { word: 'CEO Notes',        def: 'Private σημειώσεις του CEO σε κάθε project. Δεν τις βλέπουν άλλοι χρήστες.' },
  { word: 'SAS Token',        def: 'Προσωρινός, ασφαλής link για πρόσβαση στα αρχεία (π.χ. συμβάσεις). Ισχύει 1 ώρα.' },
  { word: 'Notification',     def: 'Αυτόματη ειδοποίηση για κρίσιμο event (deadline, blocked task, stale project κλπ). Εμφανίζεται ως toast και στο bell panel.' },
  { word: 'Toast',            def: 'Προσωρινή ειδοποίηση που εμφανίζεται κάτω δεξιά για 5 δευτερόλεπτα. Κλικ πάνω της για να πας στο project.' },
  { word: 'Under-billed',     def: 'Project που η πρόοδος του ξεπερνά το ζητούμενο κάτω από 30%+ αλλά δεν έχει γίνει χρέωση. Κίνδυνος απώλειας εσόδων.' },
]
</script>

<style scoped>
.content { padding: 26px 32px; overflow-y: auto; flex: 1; }
.guide-header { background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%); border: 1px solid #a7f3d0; border-radius: 14px; padding: 28px 32px; margin-bottom: 24px; }
.guide-badge { font-size: 11px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; color: #059669; margin-bottom: 8px; font-family: 'Nunito Sans', sans-serif; }
.guide-title { font-size: 28px; font-weight: 900; color: #064e3b; margin-bottom: 6px; }
.guide-sub { font-size: 14px; color: #065f46; }
.guide-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 24px; }
.guide-tab { padding: 7px 14px; border-radius: 8px; border: 1px solid var(--border-bright); background: var(--surface2); font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.15s; color: var(--text-mid); font-family: 'Nunito Sans', sans-serif; }
.guide-tab:hover { background: var(--surface3); }
.guide-tab.active { background: var(--accent-dim); border-color: var(--accent); color: var(--accent); }
.guide-panel { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 24px 28px; }
.g-section-title { font-size: 18px; font-weight: 900; color: var(--text); margin-bottom: 14px; }
.g-subsection { font-size: 13px; font-weight: 800; color: var(--text); margin: 20px 0 8px; }
.g-desc { font-size: 13px; color: var(--text-mid); line-height: 1.7; }
.g-tip { background: rgba(59,130,246,0.08); border: 1px solid rgba(59,130,246,0.2); border-radius: 8px; padding: 12px 16px; font-size: 13px; color: var(--accent); margin-top: 14px; font-weight: 600; }
.g-kpi-row { display: grid; grid-template-columns: repeat(6,1fr); gap: 10px; margin-top: 14px; }
.g-kpi-card { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 14px 10px; text-align: center; }
.g-kpi-card.finance { border-top: 2px solid var(--finance); }
.g-kpi-card.legal   { border-top: 2px solid var(--legal); }
.g-kpi-card.dev     { border-top: 2px solid var(--dev); }
.g-kpi-card.marketing{ border-top: 2px solid var(--marketing); }
.g-kpi-card.risk    { border-top: 2px solid var(--red); }
.g-kpi-ico { font-size: 18px; margin-bottom: 6px; }
.g-kpi-lbl { font-size: 11px; font-weight: 800; margin-bottom: 4px; }
.g-kpi-desc { font-size: 10px; color: var(--text-dim); line-height: 1.5; }
.g-deadline-badges { display: flex; gap: 10px; margin-top: 10px; flex-wrap: wrap; }
.g-badge { padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; }
.g-badge.red    { background: var(--red-dim);    color: var(--red); }
.g-badge.yellow { background: var(--yellow-dim); color: var(--yellow); }
.g-badge.green  { background: var(--green-dim);  color: var(--green); }
.g-steps { display: flex; flex-direction: column; gap: 10px; margin-top: 10px; }
.g-step { display: flex; align-items: flex-start; gap: 12px; background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 12px 14px; font-size: 13px; color: var(--text-mid); }
.g-step-num { background: var(--accent); color: #fff; border-radius: 50%; width: 22px; height: 22px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; flex-shrink: 0; }
.g-status-row { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
.g-status { padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; }
.g-status.on-track { background: var(--green-dim);  color: var(--green); }
.g-status.delayed   { background: var(--yellow-dim); color: var(--yellow); }
.g-status.at-risk   { background: var(--red-dim);    color: var(--red); }
.g-status.stale     { background: var(--red-dim);    color: var(--red); }
.g-tree { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 16px; margin-top: 10px; font-family: 'Nunito Sans', sans-serif; }
.g-tree-project { font-size: 13px; font-weight: 700; color: var(--text); margin-bottom: 8px; }
.g-tree-module  { font-size: 12px; font-weight: 700; color: var(--accent); margin-left: 20px; margin-bottom: 6px; background: rgba(59,130,246,0.06); padding: 6px 10px; border-radius: 5px; }
.g-tree-task    { font-size: 11px; color: var(--text-mid); margin-left: 40px; padding: 4px 0; }
.g-tree-pct     { font-size: 10px; color: var(--text-dim); }
.g-role-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; margin-top: 16px; }
.g-role { border-radius: 10px; padding: 18px; border: 1px solid var(--border); }
.g-role.ceo    { background: #eff6ff; border-color: #bfdbfe; }
.g-role.dept   { background: #ecfdf5; border-color: #a7f3d0; }
.g-role.viewer { background: var(--surface2); }
.g-role-icon { font-size: 24px; margin-bottom: 8px; }
.g-role-title { font-size: 14px; font-weight: 800; margin-bottom: 8px; color: var(--text); }
.g-role-perms { font-size: 12px; color: var(--text-mid); line-height: 1.9; }
  .g-explainer-box { margin-top: 20px; padding: 18px 20px; background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; }
  .g-explainer-title { font-size: 13px; font-weight: 800; color: #92400e; margin-bottom: 10px; }
  .g-explainer-text { font-size: 13px; color: var(--text); line-height: 1.7; margin-bottom: 10px; }
  .g-explainer-example { font-size: 12px; color: var(--text-mid); line-height: 1.7; padding: 10px 12px; background: rgba(255,255,255,0.6); border-radius: 6px; margin-bottom: 10px; }
  .g-explainer-note { font-size: 11px; color: var(--text-dim); font-style: italic; }
.g-glossary { margin-top: 14px; border: 1px solid var(--border); border-radius: 8px; overflow: hidden; }
.g-gloss-row { display: grid; grid-template-columns: 160px 1fr; border-bottom: 1px solid var(--border); }
.g-gloss-row:last-child { border-bottom: none; }
.g-gloss-term { padding: 10px 14px; font-size: 12px; font-weight: 800; color: var(--accent); background: var(--surface2); border-right: 1px solid var(--border); }
.g-gloss-def  { padding: 10px 14px; font-size: 12px; color: var(--text-mid); line-height: 1.6; }
.g-chart-wrap { margin: 16px 0; overflow-x: auto; }
.g-progress-rules { display: flex; flex-direction: column; gap: 10px; margin: 12px 0; }
.g-progress-rule { display: flex; align-items: flex-start; gap: 12px; background: var(--surface2); border-radius: 8px; padding: 12px 14px; }
.g-progress-rule.warning { background: var(--red-dim); }
.g-progress-rule.success { background: rgba(5,150,105,0.08); }
.g-progress-rule-num { width: 24px; height: 24px; border-radius: 50%; background: var(--accent); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800; flex-shrink: 0; }
.g-ai-section { background: rgba(124,58,237,0.05); border: 1px solid rgba(124,58,237,0.2); border-radius: 10px; padding: 18px 20px; }
.g-ai-title { font-size: 15px; font-weight: 800; color: #7c3aed; margin-bottom: 10px; }
.g-notif-cards { display: flex; flex-direction: column; gap: 10px; margin-top: 12px; }
.g-notif-card { border-radius: 8px; padding: 14px 16px; border: 1px solid; }
.g-notif-card.red    { background: var(--red-dim);    border-color: rgba(220,38,38,0.2); }
.g-notif-card.yellow { background: var(--yellow-dim); border-color: rgba(217,119,6,0.2); }
.g-notif-card.blue   { background: var(--accent-dim); border-color: rgba(59,130,246,0.2); }
.g-notif-card.green  { background: var(--green-dim);  border-color: rgba(5,150,105,0.2); }
.g-notif-card-title { font-size: 13px; font-weight: 800; color: var(--text); margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }
.g-notif-card-desc  { font-size: 12px; color: var(--text-mid); line-height: 1.6; }
.g-notif-badge { font-size: 9px; font-weight: 700; padding: 2px 7px; border-radius: 5px; }
.g-notif-badge.red    { background: var(--red);    color: #fff; }
.g-notif-badge.yellow { background: var(--yellow); color: #fff; }
.g-notif-badge.blue   { background: var(--accent); color: #fff; }
.g-notif-badge.green  { background: var(--green);  color: #fff; }
.g-notif-mgmt { display: grid; grid-template-columns: repeat(3,1fr); gap: 12px; margin-top: 12px; }
.g-mgmt-card { background: var(--surface2); border: 1px solid var(--border); border-radius: 8px; padding: 14px; text-align: center; }
.g-mgmt-ico   { font-size: 20px; margin-bottom: 6px; }
.g-mgmt-title { font-size: 12px; font-weight: 800; margin-bottom: 4px; }
.g-mgmt-desc  { font-size: 11px; color: var(--text-dim); line-height: 1.5; }
.g-contract-preview { border: 1px solid var(--border); border-radius: 10px; overflow: hidden; margin-top: 12px; }
.g-cp-header { background: var(--surface2); border-bottom: 1px solid var(--border); padding: 12px 16px; display: flex; align-items: center; justify-content: space-between; }
.g-cp-title { font-size: 13px; font-weight: 800; color: var(--text); display: flex; align-items: center; gap: 8px; }
.g-cp-badge { font-size: 10px; font-weight: 700; background: var(--green-dim); color: var(--green); padding: 2px 8px; border-radius: 5px; }
.g-cp-actions { display: flex; gap: 6px; }
.g-cp-btn { font-size: 10px; font-weight: 700; padding: 4px 10px; border-radius: 5px; cursor: pointer; background: var(--surface3); border: 1px solid var(--border-bright); color: var(--text-mid); }
.g-cp-btn.ai { background: rgba(124,58,237,0.1); border-color: rgba(124,58,237,0.3); color: #7c3aed; }
.g-cp-body { padding: 16px; }
.g-cp-desc { font-size: 12px; color: var(--text-mid); line-height: 1.6; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid var(--border); }
.g-cp-parties { display: flex; gap: 8px; margin-bottom: 14px; flex-wrap: wrap; }
.g-cp-party { background: var(--surface2); border: 1px solid var(--border); border-radius: 5px; padding: 4px 10px; font-size: 11px; font-weight: 600; color: var(--text-mid); }
.g-cp-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 14px; }
.g-cp-field { background: var(--surface2); border: 1px solid var(--border); border-radius: 7px; padding: 10px 12px; }
.g-cp-label { font-size: 9px; font-weight: 700; letter-spacing: 1px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 4px; font-family: "Nunito Sans", sans-serif; }
.g-cp-value { font-size: 15px; font-weight: 800; color: var(--text); }
.g-cp-value.finance { color: var(--finance); font-size: 18px; }
.g-cp-value.risk { color: var(--red); }
.g-cp-value.small { font-size: 12px; font-weight: 600; color: var(--text-mid); }
.g-cp-obligations { background: var(--surface2); border: 1px solid var(--border); border-radius: 7px; padding: 12px; margin-bottom: 12px; }
.g-cp-obl-title { font-size: 9px; font-weight: 700; letter-spacing: 1px; color: var(--text-dim); text-transform: uppercase; margin-bottom: 8px; }
.g-cp-obl-item { font-size: 12px; color: var(--text-mid); padding: 4px 0; border-bottom: 1px solid var(--border); }
.g-cp-obl-item:last-child { border-bottom: none; }
.g-cp-penalty { background: var(--red-dim); border: 1px solid rgba(220,38,38,0.2); border-radius: 7px; padding: 12px; margin-bottom: 12px; }
.g-cp-penalty-label { font-size: 11px; font-weight: 800; color: var(--red); margin-bottom: 6px; }
.g-cp-penalty-val { font-size: 12px; color: var(--red); line-height: 1.6; }
.g-cp-footer-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 10px; }
.g-cp-notice { font-size: 10px; color: var(--text-dim); font-style: italic; text-align: center; }
</style>