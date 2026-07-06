<template>
  <div class="content">
    <div class="guide-header">
      <div class="guide-badge">NEXT2VIEW</div>
      <div class="guide-title">{{ locale === 'el' ? '📘 Οδηγός Χρήσης' : '📘 User Guide' }}</div>
      <div class="guide-sub">{{ t('guide.subtitle') }}</div>
    </div>

    <div class="guide-tabs">
      <div v-for="tab in visibleTabs" :key="tab.id"
        :class="['guide-tab', { active: activeTab === tab.id }]"
        :style="tab.style || ''"
        @click="activeTab = tab.id">
        {{ tab.icon }} {{ tab.label }}
      </div>
    </div>

    <!-- ── DASHBOARD ── -->
    <div v-if="activeTab === 'dashboard' && locale === 'el'" class="guide-panel">
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


    <!-- ── DASHBOARD EN ── -->
    <div v-if="activeTab === 'dashboard' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">◈ Dashboard — The Main Page</div>
      <div class="g-desc">The Dashboard is the first page you see when you log into Next2View. From here you get an instant overview of all companies and projects without having to search anywhere.</div>

      <div class="g-subsection">📊 KPI Strip — The 6 Key Numbers</div>
      <div class="g-kpi-row">
        <div class="g-kpi-card"><div class="g-kpi-ico">%</div><div class="g-kpi-lbl">Overall</div><div class="g-kpi-desc">Average completion % of all active projects in the group.</div></div>
        <div class="g-kpi-card finance"><div class="g-kpi-ico">$</div><div class="g-kpi-lbl">Finance</div><div class="g-kpi-desc">Average completion % of all Finance projects.</div></div>
        <div class="g-kpi-card legal"><div class="g-kpi-ico">⚖</div><div class="g-kpi-lbl">Legal</div><div class="g-kpi-desc">Average completion % of all Legal projects.</div></div>
        <div class="g-kpi-card dev"><div class="g-kpi-ico">⌨</div><div class="g-kpi-lbl">Developing</div><div class="g-kpi-desc">Average completion % of all Dev projects.</div></div>
        <div class="g-kpi-card marketing"><div class="g-kpi-ico">◈</div><div class="g-kpi-lbl">Marketing</div><div class="g-kpi-desc">Average completion % of all Marketing projects.</div></div>
        <div class="g-kpi-card risk"><div class="g-kpi-ico">⚠</div><div class="g-kpi-lbl">At Risk</div><div class="g-kpi-desc">Number of projects that need immediate attention (blocked, stale, deadline at risk).</div></div>
      </div>
      <div class="g-tip">💡 If "At Risk" shows >0, check the red banner at the top — it tells you exactly which projects have issues.</div>

      <div class="g-subsection">🏢 Companies Panel</div>
      <div class="g-desc">Shows each company with its number of projects and completion %. Click on a company to see only its projects.</div>

      <div class="g-subsection">📅 Upcoming Deadlines</div>
      <div class="g-desc">The next 5 deadlines sorted chronologically with color indicators:</div>
      <div class="g-deadline-badges">
        <span class="g-badge red">🔴 &lt;7 days — Urgent</span>
        <span class="g-badge yellow">🟡 7-14 days — Caution</span>
        <span class="g-badge green">🟢 &gt;14 days — OK</span>
      </div>

      <div class="g-subsection">📊 Timeline Gantt</div>
      <div class="g-desc">At the bottom of the Dashboard you can see all projects in a weekly Gantt chart. The blue vertical line shows the <strong>current week</strong>. You can filter by category using the dropdown at the top right.</div>
      <div class="g-tip">💡 Click on a project title in the Gantt to go directly to its page.</div>
    </div>

    <!-- ── ΕΤΑΙΡΕΙΕΣ ── -->
    <div v-if="activeTab === 'companies' && locale === 'el'" class="guide-panel">
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


    <!-- ── COMPANIES EN ── -->
    <div v-if="activeTab === 'companies' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">🏢 Company Management</div>
      <div class="g-desc">Companies are the foundation of the organization. Each project belongs to one company.</div>

      <div class="g-subsection">+ How to add a new company</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Click <strong>"+ Company"</strong> at the bottom of the left sidebar.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Enter the <strong>company name</strong> (e.g. "Polaris Financial Services").</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Add a <strong>Short Code</strong> of 2-3 letters (e.g. "PF") — displayed as an avatar.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Choose a <strong>color</strong> for visual distinction.</div></div>
        <div class="g-step"><span class="g-step-num">5</span><div>Click <strong>"Save Company"</strong>. It appears immediately on the Dashboard and sidebar.</div></div>
      </div>

      <div class="g-subsection">🗑 How to delete a company</div>
      <div class="g-desc">Go to Manage (sidebar) → Companies → click × next to the company. Note: the company's projects are not deleted automatically.</div>
      <div class="g-tip">💡 Each company's color appears everywhere — dashboard, progress bars, labels. Choose colors that are easily distinguishable.</div>
    </div>

    <!-- ── PROJECTS ── -->
    <div v-if="activeTab === 'projects' && locale === 'el'" class="guide-panel">
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

      <div class="g-subsection">🖥 Full-screen επεξεργασία</div>
      <div class="g-desc">Το form επεξεργασίας ανοίγει πλέον σε <strong>πλήρη οθόνη</strong> — πιο εύκολη ανάγνωση και διαχείριση όταν το project έχει πολλά modules &amp; tasks.</div>

      <div class="g-subsection">▦ Περιγραφή ανά module</div>
      <div class="g-desc">Κάθε <strong>module</strong> έχει τώρα δικό του πεδίο <strong>περιγραφής</strong> (rich text), όπως και το project. Τη γράφεις στο form επεξεργασίας και εμφανίζεται κάτω από τον τίτλο του module στο Project Detail.</div>

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


    <!-- ── PROJECTS EN ── -->
    <div v-if="activeTab === 'projects' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">⬡ Project Management</div>
      <div class="g-desc">Each project represents a task or development that you track. A project has a category, budget, deadline, modules and tasks.</div>

      <div class="g-subsection">+ How to create a new project</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Click <strong>"+ New Project"</strong> in the sidebar or at the top right.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Enter the <strong>title</strong> (e.g. "IMO CII Compliance 2026").</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Select <strong>Company</strong> and <strong>Category</strong> (Finance / Legal / Developing / Marketing).</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Set the <strong>Budget</strong> (€) and <strong>Deadline</strong> (date).</div></div>
        <div class="g-step"><span class="g-step-num">5</span><div>Write a brief <strong>contract description</strong> — what the project is about.</div></div>
        <div class="g-step"><span class="g-step-num">6</span><div>Add <strong>Specifications</strong> — a checklist of deliverables (e.g. "Submit Flag State documents").</div></div>
        <div class="g-step"><span class="g-step-num">7</span><div>Create <strong>Modules</strong> — project phases (e.g. "Assessment Phase", "Submission", "Training").</div></div>
        <div class="g-step"><span class="g-step-num">8</span><div>Inside each Module add <strong>Tasks</strong> with assignee, deadline and completion %.</div></div>
        <div class="g-step"><span class="g-step-num">9</span><div>Click <strong>"Save Project"</strong>. It appears immediately on the Dashboard and Timeline.</div></div>
      </div>

      <div class="g-subsection">✏ How to edit a project</div>
      <div class="g-desc">From <strong>Project Detail</strong> (click on a project) → click the ✎ icon at the top right of the contract header. The same form opens with all fields pre-filled.</div>

      <div class="g-subsection">🖥 Full-screen editing</div>
      <div class="g-desc">The edit form now opens in <strong>full screen</strong> — easier to read and manage when a project has many modules &amp; tasks.</div>

      <div class="g-subsection">▦ Description per module</div>
      <div class="g-desc">Each <strong>module</strong> now has its own <strong>description</strong> field (rich text), just like the project. You write it in the edit form and it appears below the module title in Project Detail.</div>

      <div class="g-subsection">🔍 How to filter projects</div>
      <div class="g-desc">In the "All Projects" view there are 2 dropdowns at the top right: one for category and one for company. You can also click a category or company from the sidebar.</div>

      <div class="g-subsection">📊 Project Status</div>
      <div class="g-status-row">
        <span class="g-status on-track">🟢 On Track — On schedule</span>
        <span class="g-status delayed">🟡 Delayed — Behind schedule</span>
        <span class="g-status at-risk">🔴 At Risk — Blocked or deadline near with low %</span>
        <span class="g-status stale">🔴 Stale — Not updated for &gt;5 days</span>
      </div>
      <div class="g-tip">💡 Status is calculated automatically. You don't need to set it manually.</div>
    </div>

    <!-- ── TASKS ── -->
    <div v-if="activeTab === 'tasks' && locale === 'el'" class="guide-panel">
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

      <div class="g-subsection">📎 Αρχεία ανά task</div>
      <div class="g-desc">Κάθε <strong>task</strong> μπορεί να έχει δικά του αρχεία, ξεχωριστά από τα αρχεία σε επίπεδο project. Στο form επεξεργασίας, κάθε αποθηκευμένο task έχει block <strong>"Αρχεία Task"</strong> για ανέβασμα, άνοιγμα και διαγραφή.</div>
      <div class="g-tip">🔒 Τα αρχεία προστατεύονται με <strong>MFA</strong>. Αν δεν έχεις ενεργό MFA, ενεργοποίησέ το από <strong>Profile → Security</strong>.</div>
      <div class="g-tip">💡 Σε νέο task, αποθήκευσε πρώτα το project· μετά εμφανίζεται η δυνατότητα ανεβάσματος αρχείων.</div>

      <div class="g-tip">💡 Ένα task με 100% θεωρείται αυτόματα ✓ Done. Δεν χρειάζεται να κάνεις τίποτα άλλο.</div>
    </div>


    <!-- ── TASKS EN ── -->
    <div v-if="activeTab === 'tasks' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">✅ Tasks & Modules</div>
      <div class="g-desc">Tasks are the smallest work units. They are organized into <strong>Modules</strong> (phases). Module % = average of its tasks. Project % = average of its modules.</div>

      <div class="g-subsection">🏗 Structure: Project → Module → Task</div>
      <div class="g-tree">
        <div class="g-tree-project">⬡ Project — e.g. "ATLANTIS ERP" <span class="g-tree-pct">(88% = module average)</span></div>
        <div class="g-tree-module">▦ Module — e.g. "Core Modules" <span class="g-tree-pct">(100% = task average)</span></div>
        <div class="g-tree-task">· Task — e.g. "Dashboard" ✓ <span class="g-tree-pct" style="color:var(--green);">100%</span></div>
        <div class="g-tree-task">· Task — e.g. "Payroll calculator" <span class="g-tree-pct" style="color:var(--accent);">40%</span></div>
      </div>

      <div class="g-subsection">📊 Project Structure Diagram</div>
      <div class="g-chart-wrap">
        <svg width="100%" viewBox="0 0 680 580" role="img" style="max-width:680px;display:block;margin:0 auto;">
          <title>Next2View — Project Schedule Structure</title>
          <defs><marker id="garrow-en" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse"><path d="M2 1L8 5L2 9" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></marker></defs>
          <!-- PROJECT -->
          <g>
            <rect x="200" y="30" width="280" height="48" rx="10" fill="#7F77DD" fill-opacity="0.15" stroke="#7F77DD" stroke-width="1"/>
            <text x="340" y="51" text-anchor="middle" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#7F77DD">📋 Project</text>
            <text x="340" y="68" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#7F77DD">Title · Company · startDate · deadline</text>
          </g>
          <!-- Lines Project to Modules -->
          <path d="M340 78 L340 95 L190 95 L190 110" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow-en)"/>
          <path d="M340 78 L340 95 L490 95 L490 110" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow-en)"/>
          <!-- MODULE 1 -->
          <g>
            <rect x="80" y="110" width="220" height="44" rx="8" fill="#1D9E75" fill-opacity="0.12" stroke="#1D9E75" stroke-width="1"/>
            <text x="190" y="128" text-anchor="middle" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#1D9E75">Module 1</text>
            <text x="190" y="144" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#1D9E75">Color · sortOrder</text>
          </g>
          <!-- MODULE 2 -->
          <g>
            <rect x="380" y="110" width="220" height="44" rx="8" fill="#1D9E75" fill-opacity="0.12" stroke="#1D9E75" stroke-width="1"/>
            <text x="490" y="128" text-anchor="middle" font-family="Nunito,sans-serif" font-size="13" font-weight="700" fill="#1D9E75">Module 2</text>
            <text x="490" y="144" text-anchor="middle" font-family="Nunito,sans-serif" font-size="11" fill="#1D9E75">Color · sortOrder</text>
          </g>
          <!-- Lines Modules to Tasks -->
          <path d="M190 154 L190 170 L120 170 L120 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow-en)"/>
          <path d="M190 154 L190 170 L300 170 L300 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow-en)"/>
          <path d="M490 154 L490 170 L420 170 L420 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow-en)"/>
          <path d="M490 154 L490 170 L590 170 L590 190" fill="none" stroke="#888" stroke-width="1" marker-end="url(#garrow-en)"/>
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
          <text x="313" y="478" font-family="Nunito,sans-serif" font-size="10" fill="#63B3ED">Today</text>
          <rect x="40" y="488" width="620" height="16" rx="5" fill="#EFF6FF"/>
          <text x="350" y="500" text-anchor="middle" font-family="Nunito,sans-serif" font-size="10" fill="#63B3ED">Auto progress: (today − taskStart) / (taskEnd − taskStart) × 100</text>
        </svg>
      </div>

      <div class="g-subsection">⚙️ How Progress is Calculated</div>
      <div class="g-progress-rules">
        <div class="g-progress-rule">
          <div class="g-progress-rule-num">1</div>
          <div><strong>Nobody touched the task</strong> → progress = auto<br><span style="color:var(--text-dim);font-size:12px;">Increases automatically each day: (days elapsed / total days) × 100</span></div>
        </div>
        <div class="g-progress-rule">
          <div class="g-progress-rule-num">2</div>
          <div><strong>Someone set a manual %</strong> → manual wins, auto stops<br><span style="color:var(--text-dim);font-size:12px;">E.g. if you set 30%, it stays at 30% regardless of days passed.</span></div>
        </div>
        <div class="g-progress-rule warning">
          <div class="g-progress-rule-num" style="background:var(--red-dim);color:var(--red);">⚠</div>
          <div><strong>Manual &lt; Auto − 20%</strong> → CEO gets notified<br><span style="color:var(--text-dim);font-size:12px;">E.g. Day 8/10 → auto=80%, manual=30% → difference 50% → ⚠ "Task is behind schedule"</span></div>
        </div>
        <div class="g-progress-rule success">
          <div class="g-progress-rule-num" style="background:var(--green);color:#fff;">✓</div>
          <div><strong>Clicks ✅ checkbox</strong> → 100% immediately, task is closed</div>
        </div>
      </div>
      <div class="g-tip">💡 The ⚠ appears next to the task % in Project Detail — the CEO can instantly see which tasks are behind schedule.</div>

      <div class="g-subsection">✏ How to update a task</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Go to <strong>Project Detail</strong> (click on a project).</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Click on a <strong>Module</strong> title to expand/collapse it.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>You see each task with: <strong>checkmark</strong>, name, assignee, progress bar and %.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>To change the %, go to <strong>Edit Project (✎)</strong> → change the task % → Save.</div></div>
      </div>

      <div class="g-subsection">💬 How to add a comment to a task</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>In Project Detail, expand a Module.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Click the 💬 icon on the right of each task.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Write your comment and click <strong>"Save"</strong>.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>The comment appears as a preview below the task name and the AI takes it into account in reports.</div></div>
      </div>

      <div class="g-subsection">🚫 How to mark a task as Blocked</div>
      <div class="g-desc">In Edit Project → add the word "BLOCK" and the reason in the task note (e.g. "BLOCK — awaiting documents from Flag State"). The task displays a red icon and the project status changes to "At Risk".</div>

      <div class="g-subsection">📎 Files per task</div>
      <div class="g-desc">Each <strong>task</strong> can have its own files, separate from project-level files. In the edit form, every saved task has a <strong>"Task files"</strong> block to upload, open and delete.</div>
      <div class="g-tip">🔒 Files are protected with <strong>MFA</strong>. If you don't have MFA enabled, turn it on from <strong>Profile → Security</strong>.</div>
      <div class="g-tip">💡 For a new task, save the project first; the file-upload option appears afterwards.</div>

      <div class="g-tip">💡 A task with 100% is automatically considered ✓ Done. No further action needed.</div>
    </div>

        <!-- ── ΣΥΜΒΑΣΕΙΣ ── -->
    <div v-if="activeTab === 'contracts' && locale === 'el'" class="guide-panel">
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


    <!-- ── CONTRACTS EN ── -->
    <div v-if="activeTab === 'contracts' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">📄 Contract Management</div>
      <div class="g-desc">Each project can have its signed contract uploaded. The AI automatically extracts key information and displays it as a formatted card.</div>

      <div class="g-subsection">📎 How to upload a contract</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Go to the <strong>Project Detail</strong> of the desired project.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Find the <strong>"Signed Contract"</strong> panel in the right column.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Click <strong>"Upload now"</strong> or drag & drop the file.</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Supported formats: <strong>PDF, JPG, PNG</strong> up to 10MB.</div></div>
        <div class="g-step"><span class="g-step-num">5</span><div>Once uploaded, the AI automatically starts extracting key information.</div></div>
      </div>

      <div class="g-subsection">🤖 What the AI extracts automatically</div>
      <div class="g-kpi-row">
        <div class="g-kpi-card"><div class="g-kpi-ico">👤</div><div class="g-kpi-lbl">Parties</div><div class="g-kpi-desc">The contracting parties (client + provider).</div></div>
        <div class="g-kpi-card finance"><div class="g-kpi-ico">€</div><div class="g-kpi-lbl">Value</div><div class="g-kpi-desc">Total contract value.</div></div>
        <div class="g-kpi-card legal"><div class="g-kpi-ico">📅</div><div class="g-kpi-lbl">Dates</div><div class="g-kpi-desc">Contract start and end dates.</div></div>
        <div class="g-kpi-card dev"><div class="g-kpi-ico">📋</div><div class="g-kpi-lbl">Obligations</div><div class="g-kpi-desc">Main obligations of the contracting parties.</div></div>
        <div class="g-kpi-card risk"><div class="g-kpi-ico">⚠</div><div class="g-kpi-lbl">Penalties</div><div class="g-kpi-desc">Penalties and penalty clauses if applicable.</div></div>
        <div class="g-kpi-card marketing"><div class="g-kpi-ico">⚖</div><div class="g-kpi-lbl">Law</div><div class="g-kpi-desc">Governing law and jurisdiction.</div></div>
      </div>

      <div class="g-subsection">🖨 How to print / export as PDF</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Once the information is extracted, an <strong>"Export / Print"</strong> button appears at the top of the card.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Click the button — a new page opens with a print-ready layout.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Click <strong>"Print / Save as PDF"</strong> — from the browser dialog select "Save as PDF".</div></div>
      </div>
      <div class="g-tip">💡 If the extracted information doesn't look correct (e.g. missing language or currency), click <strong>"AI Analysis"</strong> to re-run the extraction.</div>
    </div>

        <!-- ── AI FEATURES ── -->
    <div v-if="activeTab === 'ai' && locale === 'el'" class="guide-panel">
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


    <!-- ── AI FEATURES EN ── -->
    <div v-if="activeTab === 'ai' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title" style="color:#7c3aed;">✦ AI Features — Powered by Claude</div>
      <div class="g-desc">Next2View integrates AI through the <strong>Claude API (Anthropic)</strong>. Three AI tools are available from different points within the application.</div>

      <div class="g-ai-section">
        <div class="g-ai-title">🤖 AI CEO Report</div>
        <div class="g-desc">Click <strong>"AI Report"</strong> in the sidebar to receive an automatic analysis of the entire portfolio.</div>
        <div class="g-subsection">How to use it</div>
        <div class="g-steps">
          <div class="g-step"><span class="g-step-num">1</span><div>Click <strong>"AI Report"</strong> in the left sidebar.</div></div>
          <div class="g-step"><span class="g-step-num">2</span><div>The AI analyzes <strong>all projects</strong>: status, deadlines, Finance, Legal, etc.</div></div>
          <div class="g-step"><span class="g-step-num">3</span><div>In 20 seconds you receive a <strong>detailed report</strong> with highlights and recommended actions.</div></div>
        </div>
        <div class="g-subsection">What the report includes</div>
        <div class="g-status-row" style="flex-wrap:wrap;">
          <span class="g-status on-track">✓ Overall completion</span>
          <span class="g-status delayed">⚠ Delayed projects</span>
          <span class="g-status at-risk">🔴 Top 3 At Risk items</span>
          <span class="g-status stale">📊 Per-category analysis</span>
          <span class="g-status on-track">💡 Recommended actions</span>
        </div>
        <div class="g-tip">💡 "AI Report" is available only for the <strong>CEO role</strong> — it does not appear for Dept Head or Viewer.</div>
      </div>

      <div class="g-ai-section" style="margin-top:20px;">
        <div class="g-ai-title">📄 AI Contract Data Extraction</div>
        <div class="g-desc">Once you upload a contract (PDF/JPG/PNG), the AI automatically extracts key information and displays it in a structured card within Project Detail.</div>
        <div class="g-tip">💡 Supports PDF and images. Recognizes both Greek and English text.</div>
        <div class="g-subsection">📋 Example — This is what the result looks like</div>
        <div class="g-contract-preview">
          <div class="g-cp-header">
            <div class="g-cp-title">Active Contract <span class="g-cp-badge">✓ Preview</span></div>
            <div class="g-cp-actions">
              <span class="g-cp-btn ai">✦ AI Analysis</span>
              <span class="g-cp-btn">Export</span>
              <span class="g-cp-btn">Print</span>
            </div>
          </div>
          <div class="g-cp-body">
            <div class="g-cp-desc">Provision of development services for a comprehensive ERP system for crew and fleet management in maritime transport, including user training and 12-month support.</div>
            <div class="g-cp-parties">
              <span class="g-cp-party">🏢 Crossworld Marine Services Ltd</span>
              <span class="g-cp-party">🏢 OceanSoft Ltd</span>
            </div>
            <div class="g-cp-grid">
              <div class="g-cp-field">
                <div class="g-cp-label">CONTRACT VALUE</div>
                <div class="g-cp-value finance">€45,000 EUR</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">PAYMENT TERMS</div>
                <div class="g-cp-value small">30% advance, 40% beta delivery, 30% final delivery</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">START DATE</div>
                <div class="g-cp-value">1 February 2026</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">END DATE</div>
                <div class="g-cp-value risk">20 April 2026</div>
              </div>
            </div>
            <div class="g-cp-obligations">
              <div class="g-cp-obl-title">KEY OBLIGATIONS</div>
              <div class="g-cp-obl-item">1. Development and delivery of full ERP with modules: Dashboard, Roster, RPA Hub, Payroll, Sign-Off</div>
              <div class="g-cp-obl-item">2. User training up to 3 days before final delivery</div>
              <div class="g-cp-obl-item">3. 12-month technical support after delivery, 24-hour SLA</div>
              <div class="g-cp-obl-item">4. Delivery of complete documentation and source code to sender repository</div>
            </div>
            <div class="g-cp-penalty">
              <div class="g-cp-penalty-label">⚠ PENALTIES / CLAUSES</div>
              <div class="g-cp-penalty-val">Late delivery: 0.5% of contract value per week of delay, up to 10% total. SLA non-compliance: 1% compensation per breach</div>
            </div>
            <div class="g-cp-footer-grid">
              <div class="g-cp-field">
                <div class="g-cp-label">GOVERNING LAW</div>
                <div class="g-cp-value small">Cyprus Law — Limassol Courts</div>
              </div>
              <div class="g-cp-field">
                <div class="g-cp-label">SPECIAL TERMS</div>
                <div class="g-cp-value small">Code exclusivity for 24 months. NDA valid for 5 years after expiry.</div>
              </div>
            </div>
            <div class="g-cp-notice">* Results were automatically extracted from Project detail and do not substitute legal advice.</div>
          </div>
        </div>
      </div>

      <div class="g-ai-section" style="margin-top:20px;">
        <div class="g-ai-title">🔍 AI Contract vs Progress Analysis</div>
        <div class="g-desc">Once a contract is uploaded to a project, you can request a comparison of contract terms against actual progress.</div>
        <div class="g-steps">
          <div class="g-step"><span class="g-step-num">1</span><div>Open the project detail.</div></div>
          <div class="g-step"><span class="g-step-num">2</span><div>Click the <strong>"AI Contract Analysis"</strong> button on the contract card.</div></div>
          <div class="g-step"><span class="g-step-num">3</span><div>The AI compares what the contract promises vs what has actually been done.</div></div>
        </div>
        <div class="g-status-row">
          <span class="g-status on-track">🟢 Compliant</span>
          <span class="g-status delayed">🟡 Partial Deviation</span>
          <span class="g-status at-risk">🔴 Serious Deviation</span>
        </div>
      </div>
    </div>

        <!-- ── NOTIFICATIONS ── -->
    <div v-if="activeTab === 'notifications' && locale === 'el'" class="guide-panel">
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


    <!-- ── NOTIFICATIONS EN ── -->
    <div v-if="activeTab === 'notifications' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">🔔 Notification System</div>
      <div class="g-desc">Next2View has an automatic system for every critical event — no need to refresh the dashboard. Every 5 minutes all active projects are checked.</div>

      <div class="g-subsection">🔔 The Bell Icon</div>
      <div class="g-desc">Appears at the bottom of the sidebar. When there are new notifications, a red badge shows the number of unread items. Click it to review them.</div>
      <div class="g-tip">💡 The badge <strong>disappears</strong> once you open the panel. When you close it, notifications continue to be received.</div>

      <div class="g-subsection">⚡ Automatic Triggers</div>
      <div class="g-notif-cards">
        <div class="g-notif-card red">
          <div class="g-notif-card-title">⏰ Deadline Risk <span class="g-notif-badge red">URGENT</span></div>
          <div class="g-notif-card-desc"><strong>When:</strong> Less than 7 days to deadline and progress below 80%.</div>
          <div class="g-notif-card-desc"><strong>What it means:</strong> The project is at risk of missing its delivery date. Immediate action required.</div>
        </div>
        <div class="g-notif-card yellow">
          <div class="g-notif-card-title">😴 Stale Project <span class="g-notif-badge yellow">WARNING</span></div>
          <div class="g-notif-card-desc"><strong>When:</strong> The project has not been updated for more than 5 days.</div>
          <div class="g-notif-card-desc"><strong>What it means:</strong> Possibly forgotten or pending. Check if follow-up is needed.</div>
        </div>
        <div class="g-notif-card blue">
          <div class="g-notif-card-title">📈 Slow Progress Gap <span class="g-notif-badge blue">NOTICE</span></div>
          <div class="g-notif-card-desc"><strong>When:</strong> More than 70% of the time period has passed but progress is below 50%.</div>
          <div class="g-notif-card-desc"><strong>What it means:</strong> The work pace doesn't match the deadlines. Likely to be delayed.</div>
        </div>
        <div class="g-notif-card red">
          <div class="g-notif-card-title">🚫 Blocked Task <span class="g-notif-badge red">URGENT</span></div>
          <div class="g-notif-card-desc"><strong>When:</strong> Any task is marked as "BLOCK" with a reason.</div>
          <div class="g-notif-card-desc"><strong>What it means:</strong> Something is blocking progress. CEO intervention required.</div>
        </div>
        <div class="g-notif-card green">
          <div class="g-notif-card-title">📉 Under-billed <span class="g-notif-badge green">INFO</span></div>
          <div class="g-notif-card-desc"><strong>When:</strong> Project progress is above 30% but no billing has been recorded.</div>
          <div class="g-notif-card-desc"><strong>What it means:</strong> Risk of revenue loss — check payments.</div>
        </div>
      </div>

      <div class="g-subsection">📩 Toast Notifications</div>
      <div class="g-desc">Each new notification appears as a <strong>toast</strong> at the bottom right of the screen for 5 seconds. Click on it to go directly to the related project.</div>

      <div class="g-subsection">🗑 Managing Notifications</div>
      <div class="g-notif-mgmt">
        <div class="g-mgmt-card"><div class="g-mgmt-ico">🔔</div><div class="g-mgmt-title">Open panel</div><div class="g-mgmt-desc">Click the bell icon in the sidebar. You see all notifications with color coding.</div></div>
        <div class="g-mgmt-card"><div class="g-mgmt-ico">👆</div><div class="g-mgmt-title">Go to project</div><div class="g-mgmt-desc">Click on any notification to go directly to the Project Detail.</div></div>
        <div class="g-mgmt-card"><div class="g-mgmt-ico">✓</div><div class="g-mgmt-title">Clear All</div><div class="g-mgmt-desc">Click "Clear all" in the panel to clear all notifications.</div></div>
      </div>
      <div class="g-tip">⏰ <strong>Check frequency:</strong> Every <strong>5 minutes</strong> automatically. A check also runs immediately when you open the application.</div>
    </div>

        <!-- ── ROLES ── -->
    <div v-if="activeTab === 'roles' && locale === 'el'" class="guide-panel">
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

      <!-- ═══ v5.1: Per-User Company + Project Scoping ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">🔑 Per-User Scoping (v5.1)</div>
      <div class="g-explainer-box" style="background: rgba(46,117,182,0.06); border-color: rgba(46,117,182,0.35);">
        <div class="g-explainer-title" style="color: #1d4f8f;">🎯 Πιο στενός έλεγχος πρόσβασης ανά χρήστη</div>
        <div class="g-explainer-text">
          Πέρα από το department και τα permission flags, ο CEO μπορεί να <strong>περιορίσει περαιτέρω</strong> τι βλέπει ένας χρήστης — επιλέγοντας <strong>συγκεκριμένες εταιρείες</strong> ή/και <strong>συγκεκριμένα projects</strong>.
        </div>
        <div class="g-explainer-text" style="margin-top: 12px;">
          Η πρόσβαση πλέον δουλεύει σε <strong>3 επίπεδα</strong> (όλα πρέπει να επιτρέπουν για να δει ο user ένα project):
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">1️⃣ Κατηγορία (Category)</div>
        <div class="g-explainer-text">
          Από το department του και τα <strong>view*</strong> permissions (viewLegal, viewFinance, viewDeveloping, viewMarketing). <em>Πρώτο φίλτρο.</em>
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 8px;">
        <div class="g-explainer-title">2️⃣ Εταιρεία (Company Scope) <span style="font-weight: normal; color: #6b7280; font-size: 0.9em;">— ΝΕΟ</span></div>
        <div class="g-explainer-text">
          <strong>"Όλες"</strong> (default): καμία επιπλέον στένωση. <strong>"Επιλεγμένες"</strong>: μόνο οι εταιρείες που έχει επιλέξει ο CEO. <em>Δεύτερο φίλτρο.</em>
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 8px;">
        <div class="g-explainer-title">3️⃣ Project (Project Scope) <span style="font-weight: normal; color: #6b7280; font-size: 0.9em;">— ΝΕΟ</span></div>
        <div class="g-explainer-text">
          <strong>"Όλα"</strong> (default): καμία επιπλέον στένωση. <strong>"Επιλεγμένα"</strong>: μόνο τα συγκεκριμένα projects που έχει επιλέξει ο CEO. <em>Τρίτο και τελικό φίλτρο.</em>
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 16px;">
        <div class="g-explainer-example">
          📌 <strong>Παράδειγμα:</strong> Η Μαριάννα είναι Legal DEPT_HEAD. Κανονικά θα έβλεπε <em>όλα</em> τα legal projects σε όλες τις εταιρείες του ομίλου. Ο CEO όμως, στο modal δικαιωμάτων της, επιλέγει στο Company Scope μόνο "Crossworld Marine" και "Polaris Financial".
        </div>
        <div class="g-explainer-note" style="margin-top: 10px;">
          ✨ <strong>Αποτέλεσμα:</strong> Η Μαριάννα βλέπει legal projects <strong>μόνο</strong> από αυτές τις 2 εταιρείες — όχι από τις υπόλοιπες 15 του ομίλου, ακόμα κι αν έχουν legal projects.
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px; background: rgba(245,158,11,0.08); border-color: rgba(245,158,11,0.35);">
        <div class="g-explainer-title" style="color: #92400e;">⚙️ Πώς το ρυθμίζει ο CEO</div>
        <div class="g-explainer-text">
          <strong>1.</strong> Admin Panel → Χρήστες → πάτα το <strong>🔑</strong> δίπλα στον χρήστη<br>
          <strong>2.</strong> Scroll στο modal κάτω από τα permissions → δες τα νέα sections <strong>🏢 Εταιρείες</strong> και <strong>📋 Πρότζεκτ</strong><br>
          <strong>3.</strong> Διάλεξε <em>"Όλες/Όλα"</em> (default — καμία στένωση) ή <em>"Επιλεγμένες/Επιλεγμένα"</em> και τσέκαρε όσες θέλεις<br>
          <strong>4.</strong> Πάτα <strong>Αποθήκευση</strong> — η αλλαγή εφαρμόζεται αμέσως στο επόμενο login/refresh του χρήστη
        </div>
        <div class="g-explainer-note" style="margin-top: 10px;">
          💡 <strong>Tip:</strong> Όταν αλλάζεις permissions στις κατηγορίες, η λίστα projects στο 3ο section <em>φιλτράρεται αυτόματα</em> για να βλέπεις μόνο projects των κατηγοριών που μπορεί όντως να δει ο user.
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px; background: rgba(107,114,128,0.08);">
        <div class="g-explainer-title" style="color: #4b5563;">👑 Σημείωση για CEO users</div>
        <div class="g-explainer-text">
          Ο CEO <strong>παρακάμπτει</strong> και τα δύο νέα φίλτρα (company + project scope). Βλέπει πάντα τα πάντα.
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

      <!-- ═══ NEW: Permissions Matrix ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">🔑 Permissions Matrix — 14 Flags</div>
      <div class="g-desc">Ο CEO μπορεί να ενεργοποιήσει οποιοδήποτε συνδυασμό από τα παρακάτω permissions για κάθε user (Admin Panel → Χρήστες → 🔑).</div>

      <div class="g-explainer-box" style="margin-top: 16px;">
        <div class="g-explainer-title">👁️ Ορατότητα Κατηγοριών (6 flags)</div>
        <div class="g-explainer-text">
          <strong>viewFinance</strong> — Βλέπει finance projects<br>
          <strong>viewLegal</strong> — Βλέπει legal projects<br>
          <strong>viewDev</strong> — Βλέπει developing projects<br>
          <strong>viewMarketing</strong> — Βλέπει marketing projects<br>
          <strong>viewFinancials</strong> — Βλέπει budget & financial data των projects<br>
          <strong>viewCeoNotes</strong> — Βλέπει τις CEO Private Notes
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">⚡ Ενέργειες (4 flags)</div>
        <div class="g-explainer-text">
          <strong>updateTasks</strong> — Αλλάζει progress & status των tasks<br>
          <strong>uploadFiles</strong> — Ανεβάζει & διαγράφει αρχεία σε projects<br>
          <strong>createProject</strong> — Δημιουργεί νέα projects<br>
          <strong>editProject</strong> — Επεξεργάζεται υπάρχοντα projects (τίτλο, deadline, κλπ)
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">👥 Διαχείριση (2 flags)</div>
        <div class="g-explainer-text">
          <strong>manageUsers</strong> — Δημιουργεί & επεξεργάζεται χρήστες<br>
          <strong>manageCompanies</strong> — Δημιουργεί & επεξεργάζεται εταιρείες
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">🤖 AI Features (2 flags)</div>
        <div class="g-explainer-text">
          <strong>aiCeoReport</strong> — Παράγει AI report για το Group Dashboard<br>
          <strong>aiContract</strong> — Αναλύει συμβάσεις με AI
        </div>
      </div>

      <!-- ═══ NEW: Sidebar Visibility Rules ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">🏢 Κανόνες εμφάνισης εταιρειών στο Sidebar</div>
      <div class="g-desc">Το Next2View φιλτράρει έξυπνα ποιες εταιρείες εμφανίζονται στο sidebar κάθε user.</div>

      <div class="g-explainer-box" style="margin-top: 16px; background: rgba(34,197,94,0.08); border-color: rgba(34,197,94,0.35);">
        <div class="g-explainer-title" style="color: #15803d;">✅ Εμφανίζεται όταν...</div>
        <div class="g-explainer-text">
          <strong>1.</strong> Έχει τουλάχιστον ένα project στις κατηγορίες που βλέπει ο user (π.χ. legal/finance)<br>
          <strong>2.</strong> Είναι τελείως άδεια (0 projects) ΚΑΙ ο user έχει <em>manageCompanies</em> (orphan protection)
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px; background: rgba(239,68,68,0.08); border-color: rgba(239,68,68,0.35);">
        <div class="g-explainer-title" style="color: #b91c1c;">❌ ΔΕΝ εμφανίζεται όταν...</div>
        <div class="g-explainer-text">
          Έχει projects μόνο σε κατηγορίες που ο user δεν βλέπει. Αυτό είναι <strong>data privacy</strong> — δεν διαρρέουμε πληροφορία για τμήματα εκτός του scope του user.
        </div>
      </div>

      <!-- ═══ NEW: Cross-user dynamic example ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">🔄 Πραγματικό παράδειγμα — Δυναμική εμφάνιση</div>
      <div class="g-explainer-box">
        <div class="g-explainer-example">
          📌 <strong>Σενάριο:</strong> Η Μαριάννα είναι Legal DEPT_HEAD. Μέχρι χθες <strong>δεν έβλεπε</strong> την εταιρεία "Polaris Financial" γιατί είχε μόνο finance projects χωρίς legal.
        </div>
        <div class="g-explainer-text" style="margin-top: 8px;">
          Σήμερα ο CEO δημιουργεί νέο <strong>legal</strong> project στην Polaris Financial.
        </div>
        <div class="g-explainer-note" style="margin-top: 8px;">
          ✨ <strong>Αποτέλεσμα:</strong> Με το επόμενο refresh της Μαριάννας, η Polaris Financial <strong>εμφανίζεται αυτόματα</strong> στο sidebar της με count (1). Το σύστημα παρακολουθεί τη δραστηριότητα σε real-time.
        </div>
      </div>

      <!-- ═══ NEW: Common scenarios Q&A ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">💡 Συχνά σενάρια</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>Θέλω κάποιον να δημιουργεί εταιρείες</strong><br>→ Ενεργοποίησε <code>manageCompanies</code></div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>Θέλω κάποιον να βλέπει legal και finance</strong><br>→ Ενεργοποίησε <code>viewLegal</code> + <code>viewFinance</code></div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>Θέλω κάποιον να δημιουργεί projects στο τμήμα του</strong><br>→ Ενεργοποίησε <code>createProject</code></div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>Θέλω να κρύψω τα financials από κάποιον</strong><br>→ Απενεργοποίησε <code>viewFinancials</code> (βλέπει τα projects αλλά όχι budget data)</div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>Νέα εταιρεία χωρίς projects εξαφανίζεται;</strong><br>→ Όχι. Εφόσον ο user έχει <code>manageCompanies</code>, βλέπει τις άδειες εταιρείες (orphan protection).</div></div>
      </div>

      <!-- ═══ NEW: Department vs Permissions ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">⚖️ Department vs Permissions — Ποια η διαφορά;</div>
      <div class="g-explainer-box">
        <div class="g-explainer-title">🏛️ Department = Βασική πρόσβαση</div>
        <div class="g-explainer-text">
          Το department (<strong>Legal / Finance / Dev / Marketing / Management</strong>) καθορίζεται όταν δημιουργείται ο χρήστης. Δίνει αυτόματα πρόσβαση στις αντίστοιχες κατηγορίες projects (π.χ. Legal department → βλέπει legal projects).
        </div>
      </div>
      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">🎛️ Permissions = Extra πρόσβαση</div>
        <div class="g-explainer-text">
          Τα 14 permission flags είναι <em>επιπλέον</em> δικαιώματα που δίνει ο CEO για συγκεκριμένες ενέργειες. Ανεξάρτητα από το department.
        </div>
      </div>
      <div class="g-tip" style="margin-top: 12px;">
        💡 <strong>Παράδειγμα:</strong> Η Μαριάννα είναι Legal DEPT_HEAD (department) αλλά έχει και <code>manageCompanies</code> permission. Βλέπει legal projects (από το department) ΚΑΙ δημιουργεί εταιρείες (από το permission).
      </div>
    </div>


    <!-- ── ROLES EN ── -->
    <div v-if="activeTab === 'roles' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">👤 Roles & Permissions</div>
      <div class="g-desc">Next2View has 3 access levels. Each user gets exactly what they need — nothing more.</div>

      <div class="g-role-grid">
        <div class="g-role ceo">
          <div class="g-role-icon">👑</div>
          <div class="g-role-title">CEO</div>
          <div class="g-role-perms">✓ Full access<br>✓ All companies<br>✓ AI Reports<br>✓ CEO private notes<br>✓ Manage users<br>✓ Manage companies<br>✓ Delete projects</div>
        </div>
        <div class="g-role dept">
          <div class="g-role-icon">🏢</div>
          <div class="g-role-title">Department Head</div>
          <div class="g-role-perms">✓ Projects in their category<br>✓ Cross-company access<br>✓ Update tasks<br>✓ Upload/Delete files<br>✓ Create projects<br>✕ AI Reports<br>✕ CEO notes<br>✕ Delete projects<br>✕ Manage users</div>
        </div>
        <div class="g-role viewer">
          <div class="g-role-icon">👁</div>
          <div class="g-role-title">Viewer</div>
          <div class="g-role-perms">✓ View projects<br>✓ View tasks<br>✗ Cannot change anything<br>✗ Cannot upload files<br>✗ No AI access<br>✗ Cannot see CEO notes</div>
        </div>
      </div>

      <div class="g-explainer-box">
        <div class="g-explainer-title">💡 How Department Head access works</div>
        <div class="g-explainer-text">
          Each Department Head is assigned <strong>one category</strong> (Legal, Developing, Finance, or Marketing). They see <strong>all projects</strong> in that category across <strong>all companies</strong> in the group.
        </div>
        <div class="g-explainer-example">
          📌 <strong>Example:</strong> A Legal Department Head belongs as an employee to one company, but sees legal projects from every company in the group (Crossworld, Polaris, Creworld, etc.).
        </div>
        <div class="g-explainer-note">
          The company the user belongs to is only <em>HR info</em> — it does not restrict which projects they can see.
        </div>
      </div>

      <!-- ═══ v5.1: Per-User Company + Project Scoping ═══ -->
      <div class="g-subsection" style="margin-top: 32px;">🔑 Per-User Scoping (v5.1)</div>
      <div class="g-explainer-box" style="background: rgba(46,117,182,0.06); border-color: rgba(46,117,182,0.35);">
        <div class="g-explainer-title" style="color: #1d4f8f;">🎯 Finer-grained access control per user</div>
        <div class="g-explainer-text">
          Beyond department and permission flags, the CEO can <strong>further restrict</strong> what a user sees — by selecting <strong>specific companies</strong> and/or <strong>specific projects</strong>.
        </div>
        <div class="g-explainer-text" style="margin-top: 12px;">
          Access now works on <strong>3 layers</strong> (all must allow for the user to see a project):
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">1️⃣ Category</div>
        <div class="g-explainer-text">
          Derived from the user's department and their <strong>view*</strong> permissions (viewLegal, viewFinance, viewDeveloping, viewMarketing). <em>First filter.</em>
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 8px;">
        <div class="g-explainer-title">2️⃣ Company Scope <span style="font-weight: normal; color: #6b7280; font-size: 0.9em;">— NEW</span></div>
        <div class="g-explainer-text">
          <strong>"All"</strong> (default): no extra restriction. <strong>"Selected"</strong>: only the companies the CEO has explicitly picked. <em>Second filter.</em>
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 8px;">
        <div class="g-explainer-title">3️⃣ Project Scope <span style="font-weight: normal; color: #6b7280; font-size: 0.9em;">— NEW</span></div>
        <div class="g-explainer-text">
          <strong>"All"</strong> (default): no extra restriction. <strong>"Selected"</strong>: only the specific projects the CEO has picked. <em>Third and final filter.</em>
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 16px;">
        <div class="g-explainer-example">
          📌 <strong>Example:</strong> Marianna is a Legal DEPT_HEAD. Normally she would see <em>all</em> legal projects across all companies in the group. But the CEO, in her permissions modal, picks only "Crossworld Marine" and "Polaris Financial" in Company Scope.
        </div>
        <div class="g-explainer-note" style="margin-top: 10px;">
          ✨ <strong>Result:</strong> Marianna now sees legal projects <strong>only</strong> from those 2 companies — not from the other 15 in the group, even if they have legal projects.
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px; background: rgba(245,158,11,0.08); border-color: rgba(245,158,11,0.35);">
        <div class="g-explainer-title" style="color: #92400e;">⚙️ How the CEO sets it up</div>
        <div class="g-explainer-text">
          <strong>1.</strong> Admin Panel → Users → click the <strong>🔑</strong> next to the user<br>
          <strong>2.</strong> Scroll past permissions to find the new <strong>🏢 Companies</strong> and <strong>📋 Projects</strong> sections<br>
          <strong>3.</strong> Pick <em>"All"</em> (default — no extra restriction) or <em>"Selected"</em> and check the ones you want<br>
          <strong>4.</strong> Click <strong>Save</strong> — the change takes effect on the user's next login/refresh
        </div>
        <div class="g-explainer-note" style="margin-top: 10px;">
          💡 <strong>Tip:</strong> When you toggle category permissions, the project list in section 3 <em>auto-filters</em> to show only projects in categories the user can actually access.
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px; background: rgba(107,114,128,0.08);">
        <div class="g-explainer-title" style="color: #4b5563;">👑 Note for CEO users</div>
        <div class="g-explainer-text">
          The CEO <strong>bypasses</strong> both new filters (company + project scope). The CEO always sees everything.
        </div>
      </div>


      <div class="g-subsection">➕ How to add a new user</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">1</span><div>Click <strong>"+ User"</strong> in the sidebar.</div></div>
        <div class="g-step"><span class="g-step-num">2</span><div>Fill in <strong>Name</strong> and <strong>Role</strong>.</div></div>
        <div class="g-step"><span class="g-step-num">3</span><div>Select <strong>Company</strong> and <strong>Department</strong> (Finance/Legal/Dev/Marketing/Management).</div></div>
        <div class="g-step"><span class="g-step-num">4</span><div>Click <strong>"Save User"</strong>. The user appears in Manage.</div></div>
      </div>
      <div class="g-tip">💡 The CEO role has access to <strong>CEO Private Notes</strong> — notes attached to Project Detail that are invisible to other users.</div>

      <!-- Permissions Matrix -->
      <div class="g-subsection" style="margin-top: 32px;">🔑 Permissions Matrix — 14 Flags</div>
      <div class="g-desc">The CEO can enable any combination of the following permissions for each user (Admin Panel → Users → 🔑).</div>

      <div class="g-explainer-box" style="margin-top: 16px;">
        <div class="g-explainer-title">👁️ Category Visibility (6 flags)</div>
        <div class="g-explainer-text">
          <strong>viewFinance</strong> — Can see finance projects<br>
          <strong>viewLegal</strong> — Can see legal projects<br>
          <strong>viewDev</strong> — Can see developing projects<br>
          <strong>viewMarketing</strong> — Can see marketing projects<br>
          <strong>viewFinancials</strong> — Can see budget & financial data of projects<br>
          <strong>viewCeoNotes</strong> — Can see CEO Private Notes
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">⚡ Actions (4 flags)</div>
        <div class="g-explainer-text">
          <strong>updateTasks</strong> — Can change progress & status of tasks<br>
          <strong>uploadFiles</strong> — Can upload & delete files in projects<br>
          <strong>createProject</strong> — Can create new projects<br>
          <strong>editProject</strong> — Can edit existing projects (title, deadline, etc.)
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">👥 Management (2 flags)</div>
        <div class="g-explainer-text">
          <strong>manageUsers</strong> — Can create & edit users<br>
          <strong>manageCompanies</strong> — Can create & edit companies
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">🤖 AI Features (2 flags)</div>
        <div class="g-explainer-text">
          <strong>aiCeoReport</strong> — Can generate AI report for the Group Dashboard<br>
          <strong>aiContract</strong> — Can analyze contracts with AI
        </div>
      </div>

      <!-- Sidebar Visibility Rules -->
      <div class="g-subsection" style="margin-top: 32px;">🏢 Company Visibility Rules in the Sidebar</div>
      <div class="g-desc">Next2View intelligently filters which companies appear in each user's sidebar.</div>

      <div class="g-explainer-box" style="margin-top: 16px; background: rgba(34,197,94,0.08); border-color: rgba(34,197,94,0.35);">
        <div class="g-explainer-title" style="color: #15803d;">✅ Shown when...</div>
        <div class="g-explainer-text">
          <strong>1.</strong> It has at least one project in the categories the user can see (e.g. legal/finance)<br>
          <strong>2.</strong> It is completely empty (0 projects) AND the user has <em>manageCompanies</em> permission (orphan protection)
        </div>
      </div>

      <div class="g-explainer-box" style="margin-top: 12px; background: rgba(239,68,68,0.08); border-color: rgba(239,68,68,0.35);">
        <div class="g-explainer-title" style="color: #b91c1c;">❌ NOT shown when...</div>
        <div class="g-explainer-text">
          It has projects only in categories the user cannot see. This is <strong>data privacy</strong> — we don't leak information about departments outside the user's scope.
        </div>
      </div>

      <!-- Dynamic example -->
      <div class="g-subsection" style="margin-top: 32px;">🔄 Real example — Dynamic visibility</div>
      <div class="g-explainer-box">
        <div class="g-explainer-example">
          📌 <strong>Scenario:</strong> Marianna is a Legal DEPT_HEAD. Until yesterday she <strong>could not see</strong> the company "Polaris Financial" because it only had finance projects without legal ones.
        </div>
        <div class="g-explainer-text" style="margin-top: 8px;">
          Today the CEO creates a new <strong>legal</strong> project in Polaris Financial.
        </div>
        <div class="g-explainer-note" style="margin-top: 8px;">
          ✨ <strong>Result:</strong> On Marianna's next refresh, Polaris Financial <strong>automatically appears</strong> in her sidebar with count (1). The system tracks activity in real-time.
        </div>
      </div>

      <!-- Common scenarios -->
      <div class="g-subsection" style="margin-top: 32px;">💡 Common Scenarios</div>
      <div class="g-steps">
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>I want someone to create companies</strong><br>→ Enable <code>manageCompanies</code></div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>I want someone to see legal and finance</strong><br>→ Enable <code>viewLegal</code> + <code>viewFinance</code></div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>I want someone to create projects in their department</strong><br>→ Enable <code>createProject</code></div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>I want to hide financials from someone</strong><br>→ Disable <code>viewFinancials</code> (they see projects but not budget data)</div></div>
        <div class="g-step"><span class="g-step-num">Q</span><div><strong>New company with no projects disappears?</strong><br>→ No. As long as the user has <code>manageCompanies</code>, they see empty companies (orphan protection).</div></div>
      </div>

      <!-- Department vs Permissions -->
      <div class="g-subsection" style="margin-top: 32px;">⚖️ Department vs Permissions — What's the difference?</div>
      <div class="g-explainer-box">
        <div class="g-explainer-title">🏛️ Department = Base access</div>
        <div class="g-explainer-text">
          The department (<strong>Legal / Finance / Dev / Marketing / Management</strong>) is set when the user is created. It automatically grants access to the corresponding project categories (e.g. Legal department → sees legal projects).
        </div>
      </div>
      <div class="g-explainer-box" style="margin-top: 12px;">
        <div class="g-explainer-title">🏛️ Permissions = Extra access</div>
        <div class="g-explainer-text">
          The 14 permission flags are <em>additional</em> rights granted by the CEO for specific actions. Independent of department.
        </div>
      </div>
      <div class="g-tip" style="margin-top: 12px;">
        💡 <strong>Example:</strong> Marianna is a Legal DEPT_HEAD (department) but also has <code>manageCompanies</code> permission. She sees legal projects (from department) AND creates companies (from permission).
      </div>
    </div>

        <!-- ── ΓΛΩΣΣΑΡΙΟ ── -->
      <div v-if="activeTab === 'mfa' && locale === 'el'" class="guide-panel">
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



      <!-- ── MFA EN ── -->
      <div v-if="activeTab === 'mfa' && locale !== 'el'" class="guide-panel">
        <div class="g-mfa-hero">
          <div class="g-mfa-hero-icon">🔐</div>
          <div class="g-mfa-hero-text">
            <h2 class="g-mfa-hero-title">Multi-Factor Authentication (MFA)</h2>
            <p class="g-mfa-hero-subtitle">Second factor verification for your account security</p>
          </div>
          <div class="g-mfa-hero-badge">⚠️ Required for Legal & Finance</div>
        </div>

        <div class="g-section">
          <h3>📌 What is MFA?</h3>
          <p><strong>Multi-Factor Authentication (MFA)</strong> adds an extra layer of security to your account. Beyond your password, you need a 6-digit code that changes every 30 seconds and is generated by an app on your phone.</p>
          <p>This way, even if someone learns your password, <strong>they cannot access your account</strong> without your phone.</p>
        </div>

        <div class="g-section g-section-warning">
          <h3>🚨 When is it REQUIRED?</h3>
          <p>MFA is <strong>required</strong> for users who have access to:</p>
          <ul class="g-list">
            <li><strong>Legal documents</strong> (contracts, legal files) — required for every upload/download</li>
            <li><strong>Finance data</strong> (financial information, payments, IBAN) — required for accessing financials</li>
            <li><strong>Confidential CEO Notes</strong> (confidential notes) — requires additional verification</li>
          </ul>
          <p style="margin-top:12px;">If you don't enable MFA, <strong>you won't have access to these features</strong>. The system will notify you automatically.</p>
        </div>

        <div class="g-section">
          <h3>📱 Step 1: Install Google Authenticator</h3>
          <p>Before setting up in Next2View, you need an authenticator app on your phone. We recommend <strong>Google Authenticator</strong> because it's free, reliable, and easy to use.</p>

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

          <p class="g-mfa-alt"><strong>Alternatives:</strong> You can also use other compatible apps like <em>Microsoft Authenticator</em>, <em>Authy</em>, <em>1Password</em>, <em>Bitwarden</em>. All work with the same standard (TOTP / RFC 6238).</p>
        </div>

        <div class="g-section">
          <h3>⚙️ Step 2: Enable MFA in Next2View</h3>
          <ol class="g-mfa-steps">
            <li>
              <strong>Go to your Profile</strong>
              <p>Click your name at the top right → select "Profile" (or click your avatar).</p>
            </li>
            <li>
              <strong>Find the "Security" section</strong>
              <p>On the Profile page you'll see a section titled 🔐 <em>Security</em>. There you'll find the "Identity Verification (MFA)" field.</p>
            </li>
            <li>
              <strong>Click "Enable MFA →"</strong>
              <p>A QR code and a cryptographic secret (alphanumeric code) will appear.</p>
            </li>
            <li>
              <strong>Open Google Authenticator on your phone</strong>
              <p>Tap the <strong>+</strong> at the bottom right → select <strong>"Scan QR code"</strong>.</p>
            </li>
            <li>
              <strong>Scan the QR code</strong>
              <p>Point your phone camera at the QR code on your computer screen. The app will automatically add a "Next2View" account with your email.</p>
            </li>
            <li>
              <strong>Get the 6-digit code</strong>
              <p>In the app you'll see a 6-digit code below the "Next2View" account. The code changes every 30 seconds.</p>
            </li>
            <li>
              <strong>Enter the code in Next2View</strong>
              <p>In the "Verification code" field, type the 6-digit code and click <strong>"Verify"</strong>.</p>
            </li>
            <li>
              <strong>✅ Done!</strong>
              <p>From now on, every time you log into Next2View or request access to Legal/Finance, you'll be asked for the 6-digit code from the app.</p>
            </li>
          </ol>
        </div>

        <div class="g-section">
          <h3>🔄 Step 3: How to use it daily</h3>
          <p>When logging in or trying to access Legal/Finance:</p>
          <ol class="g-list-numbered">
            <li>Open Google Authenticator on your phone</li>
            <li>See the current 6-digit code below "Next2View"</li>
            <li>Type it on screen before it expires (you have 30 seconds)</li>
            <li>If the code expires before you finish, wait for the next one and try again</li>
          </ol>
        </div>

        <div class="g-section g-section-tip">
          <h3>💡 Tips & Best Practices</h3>
          <ul class="g-list">
            <li><strong>Backup:</strong> Take a screenshot of the cryptographic secret (the code below the QR) and keep it in a safe place. If you lose your phone, you can reconfigure MFA with this code.</li>
            <li><strong>Multiple devices:</strong> You can scan the same QR on a second phone (e.g. tablet) as backup.</li>
            <li><strong>Cloud sync:</strong> Google Authenticator supports syncing with your Google account — if you change phones, your codes come with you.</li>
            <li><strong>Confidentiality:</strong> NEVER share your QR code or 6-digit code with anyone, not even support.</li>
            <li><strong>Phone time:</strong> Make sure your phone time is correct (auto-sync). Wrong time = wrong code.</li>
          </ul>
        </div>

        <div class="g-section g-section-warning">
          <h3>❓ Frequently Asked Questions</h3>

          <div class="g-faq">
            <div class="g-faq-q">I lost my phone. What do I do?</div>
            <div class="g-faq-a">Contact the CEO or IT administrator immediately. They will disable your MFA from the database and you'll be able to log in again and set it up from scratch on a new device.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">The code doesn't work. Why?</div>
            <div class="g-faq-a">Usually it's a time issue. Go to your phone settings → Date & Time → enable "Automatic network time". Try again after 1 minute.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Does my Google account need to be the same as my Next2View email?</div>
            <div class="g-faq-a"><strong>No.</strong> Google Authenticator is an independent app. It's not connected to any Google account. You can use it on any phone with any email.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Can I disable MFA?</div>
            <div class="g-faq-a">Yes, from Profile → Security → "Disable". HOWEVER, if you are a Legal or Finance user, you won't have access to those features until you re-enable it.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">What does "TOTP" mean?</div>
            <div class="g-faq-a">Time-based One-Time Password (RFC 6238). It's the international standard for 6-digit codes that change over time. Works offline on your phone, without internet.</div>
          </div>

          <div class="g-faq">
            <div class="g-faq-q">Is it safe if someone sees my QR code?</div>
            <div class="g-faq-a"><strong>No.</strong> The QR contains your secret. If someone scans it, they can generate the same codes as you. Always do the setup in a private space and don't show it to anyone.</div>
          </div>
        </div>

        <div class="g-callout-success">
          <strong>✅ Pro tip:</strong> Once you complete the setup, try logging out and back in. This ensures MFA works correctly before you need access to critical data.
        </div>
      </div>


            <div v-if="activeTab === 'security'" class="guide-panel">
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

        <!-- Action Toolbar -->
        <div class="g-sec-toolbar">
          <button class="g-btn-action g-btn-print" @click="printSecurityTab">
            <span class="g-btn-icon">🖨️</span>
            <span class="g-btn-text">Print This Tab as PDF</span>
          </button>
          <button class="g-btn-action g-btn-upload" @click="triggerUpload" v-if="permStore.isCEO() || permStore.can('viewSecurity')">
            <span class="g-btn-icon">📤</span>
            <span class="g-btn-text">Upload Document</span>
          </button>
          <input ref="secFileInput" type="file" @change="handleSecUpload" style="display:none" accept=".pdf,.docx,.doc,.xlsx,.xls,.pptx,.ppt,.txt,.md" />
        </div>

        <h3>📚 Security Documents Repository</h3>
        <p style="color:var(--text-secondary,#64748b);font-size:14px;margin-bottom:14px;">
          Επίσημα έγγραφα τεκμηρίωσης ασφάλειας — DPIA, Technical Security Architecture, Audit reports, etc.
          Μπορείς να κατεβάσεις τα υπάρχοντα ή να ανεβάσεις νέες εκδόσεις.
        </p>

        <div v-if="secDocsLoading" class="g-loading">⏳ Loading documents...</div>

        <div v-else-if="securityDocs.length === 0" class="g-empty">
          <p style="margin:8px 0;color:var(--text-secondary,#64748b);">Κανένα έγγραφο δεν έχει ανέβει ακόμη.</p>
          <p style="margin:0;font-size:13px;color:var(--text-secondary,#64748b);">Πάτησε "Upload Document" για να ξεκινήσεις.</p>
        </div>

        <div v-else class="g-docs-list">
          <div v-for="doc in securityDocs" :key="doc.id" class="g-doc-card">
            <div class="g-doc-icon">{{ getDocIcon(doc.filename) }}</div>
            <div class="g-doc-content">
              <div class="g-doc-title">{{ doc.filename }}</div>
              <div class="g-doc-meta">
                <span>📅 {{ formatDate(doc.uploadedAt) }}</span>
                <span>•</span>
                <span>👤 {{ doc.uploaderName || 'Unknown' }}</span>
                <span>•</span>
                <span>📦 {{ formatBytes(doc.sizeBytes) }}</span>
              </div>
              <div v-if="doc.description" class="g-doc-desc">{{ doc.description }}</div>
            </div>
            <div class="g-doc-actions">
              <button class="g-btn-mini g-btn-download" @click="downloadDoc(doc)" title="Download">
                ⬇️
              </button>
              <button v-if="permStore.isCEO()" class="g-btn-mini g-btn-delete" @click="deleteDoc(doc)" title="Delete">
                🗑️
              </button>
            </div>
          </div>
        </div>

        <div v-if="secDocsError" class="g-callout-warning" style="margin-top:14px;">
          ⚠️ {{ secDocsError }}
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
            <tr><td><strong>Stored (ciphertext)</strong></td><td><code>?Ru\...</code> (pseudo-random — indistinguishable from noise)</td></tr>
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


    <div v-if="activeTab === 'glossary' && locale === 'el'" class="guide-panel">
      <div class="g-section-title">📖 Γλωσσάριο</div>
      <div class="g-desc">Βασικοί όροι που χρησιμοποιούνται στο Next2View.</div>
      <div class="g-glossary">
        <div v-for="term in glossary" :key="term.word" class="g-gloss-row">
          <div class="g-gloss-term">{{ term.word }}</div>
          <div class="g-gloss-def">{{ term.def }}</div>
        </div>
      </div>
    </div>

    <!-- ── GLOSSARY EN ── -->
    <div v-if="activeTab === 'glossary' && locale !== 'el'" class="guide-panel">
      <div class="g-section-title">📖 Glossary</div>
      <div class="g-desc">Key terms used in Next2View.</div>
      <div class="g-glossary">
        <div v-for="term in glossaryEn" :key="term.word" class="g-gloss-row">
          <div class="g-gloss-term">{{ term.word }}</div>
          <div class="g-gloss-def">{{ term.def }}</div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePermissionStore } from '@/stores/permissions'

const { t, locale } = useI18n()
const activeTab = ref('dashboard')

const tabs = computed(() => [
  { id: 'mfa',           label: 'MFA Setup',     icon: '🔐', style: 'background:rgba(5,150,105,0.1);border-color:rgba(5,150,105,0.35);color:#047857;' },
  { id: 'dashboard',     label: 'Dashboard',    icon: '◈' },
  { id: 'companies',     label: t('guide.tabs.companies'),    icon: '🏢' },
  { id: 'projects',      label: 'Projects',     icon: '⬡' },
  { id: 'tasks',         label: 'Tasks',        icon: '✅' },
  { id: 'contracts',     label: t('guide.tabs.contracts'),    icon: '📄' },
  { id: 'ai',            label: 'AI Features',  icon: '✦', style: 'background:rgba(139,92,246,0.1);border-color:rgba(139,92,246,0.3);color:#7c3aed;' },
  { id: 'notifications', label: 'Notifications', icon: '🔔', style: 'background:rgba(220,38,38,0.07);border-color:rgba(220,38,38,0.25);color:var(--red);' },
  { id: 'roles',         label: 'Roles',        icon: '👤' },
  { id: 'security',      label: 'Security',     icon: '🔒', style: 'background:rgba(245,158,11,0.08);border-color:rgba(245,158,11,0.35);color:#b45309;' },

  { id: 'glossary',      label: t('guide.tabs.glossary'),   icon: '📖' },
])

  const permStore = usePermissionStore()
  const visibleTabs = computed(() => tabs.value.filter(t => t.id !== 'security' || permStore.canViewSecurity()))

// ============= Security Documents Manager =============
import api from '@/services/api'
const securityDocs = ref([])
const secDocsLoading = ref(false)
const secDocsError = ref('')
const secFileInput = ref(null)

const loadSecurityDocs = async () => {
  if (!permStore.isCEO() && !permStore.can('viewSecurity')) return
  secDocsLoading.value = true
  secDocsError.value = ''
  try {
    const res = await api.get('/security-documents')
    securityDocs.value = res.data || []
  } catch (e) {
    secDocsError.value = t('guide.loadError') + ': ' + (e.response?.data?.error || e.message)
    console.error('Load security docs failed:', e)
  } finally {
    secDocsLoading.value = false
  }
}

const triggerUpload = () => {
  if (secFileInput.value) secFileInput.value.click()
}

const handleSecUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  const description = prompt('Description (optional):') || ''
  const fd = new FormData()
  fd.append('file', file)
  if (description) fd.append('description', description)
  secDocsLoading.value = true
  secDocsError.value = ''
  try {
    await api.post('/security-documents/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    await loadSecurityDocs()
  } catch (e) {
    secDocsError.value = 'Upload failed: ' + (e.response?.data?.error || e.message)
  } finally {
    secDocsLoading.value = false
    if (secFileInput.value) secFileInput.value.value = ''
  }
}

const downloadDoc = async (doc) => {
  try {
    const res = await api.get(`/security-documents/${doc.id}/download`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', doc.filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    secDocsError.value = 'Download failed: ' + (e.response?.data?.error || e.message)
  }
}

const deleteDoc = async (doc) => {
  if (!confirm(`Διαγραφή του "${doc.filename}"?`)) return
  try {
    await api.delete(`/security-documents/${doc.id}`)
    await loadSecurityDocs()
  } catch (e) {
    secDocsError.value = 'Delete failed: ' + (e.response?.data?.error || e.message)
  }
}

const getDocIcon = (filename) => {
  const ext = (filename || '').split('.').pop().toLowerCase()
  if (['pdf'].includes(ext)) return '📕'
  if (['docx','doc'].includes(ext)) return '📘'
  if (['xlsx','xls'].includes(ext)) return '📗'
  if (['pptx','ppt'].includes(ext)) return '📙'
  return '📄'
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const sizes = ['B','KB','MB','GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + sizes[i]
}

const formatDate = (iso) => {
  if (!iso) return ''
  try {
    const d = new Date(iso)
    return d.toLocaleDateString('el-GR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch { return iso }
}

const printSecurityTab = () => {
  // Switch to security tab if not active
  if (activeTab.value !== 'security') {
    activeTab.value = 'security'
  }
  // Add a class to body to enable print isolation CSS
  document.body.classList.add('print-security-mode')
  // Wait for DOM to settle, then print
  setTimeout(() => {
    window.print()
    // Clean up after print dialog closes
    setTimeout(() => {
      document.body.classList.remove('print-security-mode')
    }, 1000)
  }, 200)
}

onMounted(() => {
  loadSecurityDocs()
})


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

const glossaryEn = [
  { word: 'Project',          def: 'A task or development you track (e.g. "IMO CII Compliance", "Hivebee MVP"). Each project belongs to a company and category.' },
  { word: 'Module',           def: 'A phase or section of a project (e.g. "Assessment Phase", "Frontend UI"). Contains tasks.' },
  { word: 'Task',             def: 'The smallest unit of work. Has an assignee and completion percentage (0-100%).' },
  { word: '% Completion',     def: 'Task % \u2192 Module % (average of tasks) \u2192 Project % (average of modules). Calculated automatically.' },
  { word: 'Status',           def: 'Automatic project state: On Track / Delayed / At Risk / Stale. Based on %, deadline and blockers.' },
  { word: 'Stale',            def: 'A project that has not been updated for more than 5 days. Displayed with a yellow warning.' },
  { word: 'Blocked',          def: 'A task that cannot progress due to an external factor. Marked with "BLOCK" in the note.' },
  { word: 'At Risk',          def: 'A project with a blocked task, or deadline <7 days with low %, or status >5 days without update.' },
  { word: 'KPI',              def: 'Key Performance Indicator \u2014 the key numbers displayed in the Dashboard cards.' },
  { word: 'Gantt Timeline',   def: 'A weekly chart showing when each task runs. The blue line = today.' },
  { word: 'Contract Summary', def: 'Automatic extraction of key information from an uploaded contract (PDF/image) via AI.' },
  { word: 'CEO Notes',        def: 'Private CEO notes on each project. Not visible to other users.' },
  { word: 'SAS Token',        def: 'A temporary, secure link for file access (e.g. contracts). Valid for 1 hour.' },
  { word: 'Notification',     def: 'An automatic alert for a critical event (deadline, blocked task, stale project, etc.). Appears as a toast and in the bell panel.' },
  { word: 'Toast',            def: 'A temporary notification that appears at the bottom right for 5 seconds. Click on it to go to the project.' },
  { word: 'Under-billed',     def: 'A project whose progress exceeds 30%+ but no billing has been recorded. Risk of revenue loss.' },
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

  /* === Security Documentation styles === */
  .sec-section-h { font-family: 'Nunito Sans', sans-serif; font-size: 11px; font-weight: 800; letter-spacing: 1.2px; text-transform: uppercase; color: var(--text-dim); margin: 28px 0 12px; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
  .sec-section-h:first-of-type { margin-top: 0; }

  .sec-overview-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 20px; }
  .sec-card { padding: 14px 16px; border-radius: 10px; border: 1px solid var(--border); background: var(--surface2); }
  .sec-card-green { border-left: 3px solid #10b981; }
  .sec-card-label { font-size: 10px; font-weight: 700; letter-spacing: 0.8px; text-transform: uppercase; color: var(--text-dim); margin-bottom: 6px; }
  .sec-card-value { font-size: 15px; font-weight: 800; color: var(--text); margin-bottom: 4px; }
  .sec-card-note { font-size: 11px; color: var(--text-mid); }

  .sec-table { width: 100%; border-collapse: collapse; margin-bottom: 12px; font-size: 13px; }
  .sec-table thead th { text-align: left; padding: 10px 12px; background: var(--surface2); font-weight: 700; color: var(--text-mid); font-size: 11px; letter-spacing: 0.6px; text-transform: uppercase; border-bottom: 2px solid var(--border); }
  .sec-table tbody td { padding: 10px 12px; border-bottom: 1px solid var(--border); vertical-align: top; color: var(--text); }
  .sec-table tbody td:first-child { font-weight: 700; width: 28%; }
  .sec-table tbody td:nth-child(2) { color: var(--text-mid); }
  .sec-table tbody tr:hover { background: var(--surface2); }
  .sec-table tbody tr:last-child td { border-bottom: none; }

  .sec-badge { display: inline-block; padding: 3px 9px; border-radius: 12px; font-size: 11px; font-weight: 700; white-space: nowrap; }
  .sec-badge-ok { background: rgba(16,185,129,0.12); color: #047857; border: 1px solid rgba(16,185,129,0.3); }
  .sec-badge-progress { background: rgba(245,158,11,0.12); color: #b45309; border: 1px solid rgba(245,158,11,0.3); }
  .sec-badge-planned { background: rgba(107,114,128,0.1); color: #4b5563; border: 1px solid rgba(107,114,128,0.25); }

  .sec-access-note { margin-top: 28px; padding: 14px 18px; background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; font-size: 13px; color: #92400e; line-height: 1.6; }
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


/* ============= Security Documents Manager ============= */
.g-sec-toolbar {
  display: flex;
  gap: 10px;
  margin: 18px 0 24px 0;
  flex-wrap: wrap;
}
.g-btn-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--bg-card, #ffffff);
  color: var(--text, #0f172a);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.g-btn-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.g-btn-print {
  border-color: rgba(30,58,138,0.3);
  background: rgba(30,58,138,0.05);
  color: #1e3a8a;
}
.g-btn-print:hover {
  background: rgba(30,58,138,0.1);
}
.g-btn-upload {
  border-color: rgba(5,150,105,0.3);
  background: rgba(5,150,105,0.05);
  color: #047857;
}
.g-btn-upload:hover {
  background: rgba(5,150,105,0.1);
}
.g-btn-icon { font-size: 16px; line-height: 1; }
.g-btn-text { font-size: 14px; }

.g-loading, .g-empty {
  text-align: center;
  padding: 32px 16px;
  background: var(--bg-card, #ffffff);
  border: 1px dashed var(--border, #e2e8f0);
  border-radius: 12px;
}

.g-docs-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.g-doc-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  transition: all 0.2s;
}
.g-doc-card:hover {
  border-color: rgba(30,58,138,0.3);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.g-doc-icon {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;
}
.g-doc-content {
  flex: 1;
  min-width: 0;
}
.g-doc-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text, #0f172a);
  margin-bottom: 4px;
  word-break: break-word;
}
.g-doc-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary, #64748b);
  flex-wrap: wrap;
}
.g-doc-desc {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-top: 4px;
  font-style: italic;
}
.g-doc-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.g-btn-mini {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--bg-card, #ffffff);
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.g-btn-mini:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}
.g-btn-download:hover { background: rgba(5,150,105,0.1); border-color: rgba(5,150,105,0.4); }
.g-btn-delete:hover { background: rgba(220,38,38,0.1); border-color: rgba(220,38,38,0.4); }

/* ============= Print isolation - applied via .print-security-mode body class ============= */
body.print-security-mode {
  /* When in print mode, hide everything except #app and security panel */
}

@media print {
  @page {
    size: A4;
    margin: 1.2cm;
  }

  /* CRITICAL: When body has print-security-mode class, isolate the security panel */
  body.print-security-mode > *:not(#app) {
    display: none !important;
  }

  /* Reset basics */
  body, html {
    background: white !important;
    color: black !important;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
    margin: 0 !important;
    padding: 0 !important;
  }

  /* Hide chrome - sidebar, header, navigation */
  .sidebar,
  aside,
  nav,
  header,
  .layout-header,
  .top-bar,
  .topbar,
  .dashboard-sidebar,
  .layout-sidebar,
  .app-sidebar,
  .new-project-btn,
  .add-button {
    display: none !important;
  }

  /* Hide guide tabs row and toolbar buttons */
  .guide-tabs,
  .g-sec-toolbar {
    display: none !important;
  }

  /* Hide ALL guide panels by default - we'll show only security via :has trick or just leave all blocks visible */
  /* Vue's v-if removes panels from DOM, so only the active one is rendered. Trust that. */

  /* Hide doc actions and small buttons */
  .g-doc-actions,
  button,
  .g-btn-mini {
    display: none !important;
  }

  /* Make content full-width and visible */
  .layout-main,
  main,
  .main-content,
  .guide-container,
  .container,
  .guide-page {
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    background: white !important;
    box-shadow: none !important;
    overflow: visible !important;
    height: auto !important;
  }

  /* Page header */
  .guide-header,
  .page-header {
    margin-bottom: 12pt !important;
    padding: 0 !important;
    border: none !important;
    background: white !important;
  }

  /* Allow guide-panel content to flow */
  .guide-panel {
    display: block !important;
    page-break-inside: auto !important;
    overflow: visible !important;
    height: auto !important;
    max-height: none !important;
    width: 100% !important;
  }

  /* Headings */
  h1, h2, h3, h4 {
    page-break-after: avoid;
    page-break-inside: avoid;
    color: black !important;
  }

  h1 { font-size: 18pt !important; }
  h2 { font-size: 15pt !important; margin-top: 14pt !important; }
  h3 { font-size: 12pt !important; margin-top: 10pt !important; margin-bottom: 6pt !important; }

  /* Tables */
  .g-table {
    page-break-inside: auto;
    border-collapse: collapse;
    width: 100% !important;
    font-size: 9pt !important;
    margin: 4pt 0 10pt 0 !important;
  }

  .g-table thead {
    display: table-header-group;
  }

  .g-table tbody tr {
    page-break-inside: avoid;
  }

  .g-table th, .g-table td {
    padding: 3pt 5pt !important;
    border: 1px solid #ccc !important;
    background: white !important;
    color: black !important;
    text-align: left;
    vertical-align: top;
  }

  .g-table thead th {
    background: #eee !important;
    font-weight: bold !important;
  }

  /* Cards */
  .g-stat-card,
  .g-doc-card,
  .g-layer,
  .g-sec-hero,
  .g-callout-info,
  .g-callout-warning,
  .g-callout-success {
    page-break-inside: avoid;
    border: 1px solid #999 !important;
    background: white !important;
    color: black !important;
    box-shadow: none !important;
    padding: 6pt !important;
    margin-bottom: 6pt !important;
    font-size: 10pt !important;
  }

  /* Stats grid */
  .g-grid-4 {
    display: grid !important;
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 4pt !important;
    margin-bottom: 10pt !important;
  }

  /* Layers */
  .g-layers {
    margin: 6pt 0 !important;
  }

  .g-layer {
    display: flex !important;
    margin-bottom: 3pt !important;
    padding: 4pt 6pt !important;
    align-items: center;
    gap: 8pt;
  }

  .g-layer-num {
    background: #1e3a8a !important;
    color: white !important;
    -webkit-print-color-adjust: exact !important;
    width: 24pt;
    height: 24pt;
    flex-shrink: 0;
  }

  /* Badges */
  .g-badge {
    border: 1px solid #999 !important;
    background: white !important;
    color: black !important;
    padding: 1pt 4pt !important;
    font-size: 8pt !important;
  }

  /* Hide loading/empty/error */
  .g-loading,
  .g-empty {
    display: none !important;
  }

  /* Section spacing */
  .g-section {
    margin-bottom: 10pt !important;
    page-break-inside: avoid;
  }

  /* Hero card compact */
  .g-sec-hero {
    padding: 8pt !important;
    margin-bottom: 10pt !important;
  }

  .g-sec-hero-icon {
    font-size: 24pt !important;
  }

  .g-sec-hero-title {
    font-size: 14pt !important;
  }

  /* Doc cards in repository */
  .g-doc-card {
    padding: 6pt 8pt !important;
    margin-bottom: 4pt !important;
  }

  .g-doc-icon {
    font-size: 18pt !important;
  }

  .g-doc-title {
    font-size: 10pt !important;
  }

  .g-doc-meta {
    font-size: 8pt !important;
  }
}

</style>