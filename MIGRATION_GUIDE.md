# Next2View i18n — Migration Guide
## Phase 1: Silent Integration (Zero Visual Change)

---

### Step 1: Add the files to your repo

```
frontend/
  public/
    i18n/
      i18n.js        ← the i18n module
      el.json        ← Greek translations
      en.json        ← English translations
```

---

### Step 2: Load i18n in index.html

Add these lines **before** your `<script>` block (before your app JS):

```html
<!-- i18n System -->
<script src="i18n/i18n.js"></script>
<script>
  // Load translations and initialize
  Promise.all([
    fetch('i18n/el.json').then(r => r.json()),
    fetch('i18n/en.json').then(r => r.json())
  ]).then(([el, en]) => {
    N2Vi18n.init({ el, en });
    // After i18n is ready, render the app
    renderAll();
  });
</script>
```

**Alternative (inline, no fetch):** If you prefer no extra HTTP requests,
you can embed the JSON directly:

```html
<script>
  N2Vi18n.init({
    el: { /* paste el.json contents */ },
    en: { /* paste en.json contents */ }
  });
</script>
```

---

### Step 3: Replace hardcoded strings (view by view)

Here are the **exact** code changes. Each one replaces a Greek string
with `t('key')` which returns the **same Greek string** (since locale=el).

#### 3a. Month formatter — `fmtD` function

**BEFORE:**
```js
const fmtD=iso=>{
  if(!iso)return'—';
  const d=new Date(iso),
  m=['Ιαν','Φεβ','Μαρ','Απρ','Μαι','Ιουν','Ιουλ','Αυγ','Σεπ','Οκτ','Νοε','Δεκ'];
  return d.getDate()+' '+m[d.getMonth()]+' '+d.getFullYear();
};
```

**AFTER:**
```js
const fmtD=iso=>N2Vi18n.fmtDate(iso);
```

#### 3b. Alert / Confirm messages

**BEFORE:**
```js
alert('Συμπλήρωσε όνομα & code.');
alert('Συμπλήρωσε τίτλο.');
alert('Βάλε ένα valid API key.');
alert('Το αρχείο είναι μεγαλύτερο από 10MB.');
confirm('Αφαίρεση σύμβασης;');
confirm('Διαγραφή εταιρείας;');
confirm('Το key δεν αρχίζει με sk-ant-. Είσαι σίγουρος;');
```

**AFTER:**
```js
alert(t('manage.fillNameCode'));
alert(t('manage.fillTitle'));
alert(t('errors.invalidApiKey'));
alert(t('errors.fileTooLarge'));
confirm(t('contract.removeContract'));
confirm(t('manage.deleteCompany'));
confirm(t('errors.apiKeyWarning'));
```

#### 3c. Empty states

**BEFORE:**
```js
'<div class="empty-state"><div class="empty-ico">🏢</div><div>Καμία εταιρεία.</div></div>'
'<div class="empty-state"><div class="empty-ico">👤</div><div>Κανένας χρήστης.</div></div>'
'<div class="empty-state"><div class="empty-ico">◌</div><div>Δεν υπάρχουν projects.</div></div>'
'<div class="empty-state"><div class="empty-ico">◌</div><div>Δεν υπάρχουν modules.</div></div>'
```

**AFTER:**
```js
`<div class="empty-state"><div class="empty-ico">🏢</div><div>${t('manage.noCompanies')}</div></div>`
`<div class="empty-state"><div class="empty-ico">👤</div><div>${t('manage.noUsers')}</div></div>`
`<div class="empty-state"><div class="empty-ico">◌</div><div>${t('projects.noProjects')}</div></div>`
`<div class="empty-state"><div class="empty-ico">◌</div><div>${t('detail.noModules')}</div></div>`
```

#### 3d. Contract card labels

**BEFORE:**
```html
<div class="cscard-field-label">Αντικείμενο Σύμβασης</div>
<div class="cscard-field-label">Συμβαλλόμενα Μέρη</div>
<div class="cscard-field-label">Αξία Σύμβασης</div>
<div class="cscard-field-label">Όροι Πληρωμής</div>
<div class="cscard-field-label">Κύριες Υποχρεώσεις</div>
<div class="cscard-penalty-label">⚠ Ρήτρες / Ποινές</div>
<div class="cscard-field-label">⚖️ Εφαρμοστέο Δίκαιο</div>
<div class="cscard-field-label">✨ Ιδιαίτεροι Όροι</div>
```

**AFTER (in template literals):**
```js
`<div class="cscard-field-label">${t('contract.subject')}</div>`
`<div class="cscard-field-label">${t('contract.parties')}</div>`
`<div class="cscard-field-label">${t('contract.value')}</div>`
`<div class="cscard-field-label">${t('contract.paymentTerms')}</div>`
`<div class="cscard-field-label">${t('contract.obligations')}</div>`
`<div class="cscard-penalty-label">⚠ ${t('contract.penalties')}</div>`
`<div class="cscard-field-label">⚖️ ${t('contract.governingLaw')}</div>`
`<div class="cscard-field-label">✨ ${t('contract.specialTerms')}</div>`
```

#### 3e. Button labels (in template literals)

**BEFORE:**
```js
'✦ AI Ανάλυση Σύμβασης'
'🖨 Export / Print'
'↺ Ανανέωση'
'Αποθήκευση'
'Κλείσιμο'
```

**AFTER:**
```js
`✦ ${t('actions.aiAnalysis')}`
`🖨 ${t('actions.export')}`
`↺ ${t('actions.refresh')}`
t('actions.save')
t('actions.close')
```

#### 3f. Contract badge

**BEFORE:**
```js
'<span class="badge">✓ Υπογεγραμμένη</span>'
'<span class="badge unsigned">⚠ Μη υπογεγραμμένη</span>'
```

**AFTER:**
```js
`<span class="badge">✓ ${t('contract.signed')}</span>`
`<span class="badge unsigned">⚠ ${t('contract.unsigned')}</span>`
```

#### 3g. Subtitle / header

**BEFORE:**
```js
textContent='Οδηγός χρήσης Next2View'
```

**AFTER:**
```js
textContent=t('guide.title')
```

#### 3h. Dropdown defaults

**BEFORE:**
```js
'<option value="">Όλες εταιρείες</option>'
'<option value="">— Επίλεξε —</option>'
```

**AFTER:**
```js
`<option value="">${t('projects.allCompanies')}</option>`
`<option value="">${t('manage.selectCompany')}</option>`
```

#### 3i. Stale/risk notification text

**BEFORE:**
```js
`<strong>${stale.length} projects</strong> χρειάζονται προσοχή`
```

**AFTER:**
```js
`<strong>${stale.length} projects</strong> ${t('status.needsAttention')}`
```

#### 3j. Export function date locale

**BEFORE:**
```js
const now = new Date().toLocaleDateString('el-GR');
```

**AFTER:**
```js
const now = new Date().toLocaleDateString(N2Vi18n.locale() === 'el' ? 'el-GR' : 'en-US');
```

---

### Step 4: Re-render on locale change

Add this once, after init:

```js
N2Vi18n.onChange(() => {
  renderAll();  // or whatever your main render function is called
});
```

This means: when someone switches language, the entire UI re-renders
with the new translations. Since `renderAll()` already works, this is
zero-risk.

---

### Step 5 (Phase 4 only): Add Language Switcher button

When ready to go live, add this to the sidebar (bottom area):

```html
<div class="lang-switch" onclick="N2Vi18n.toggle(); renderAll();"
     style="display:flex;align-items:center;gap:6px;padding:8px 12px;
            cursor:pointer;border-radius:8px;font-size:12px;font-weight:700;
            background:var(--surface2);border:1px solid var(--border);
            color:var(--text-dim);margin:8px 12px;">
  <span style="font-size:14px;">🌐</span>
  <span id="lang-label">ΕΛ / EN</span>
</div>
```

**Until Phase 4, do NOT add this button.** The app runs in Greek only.

---

## Checklist

- [ ] Add `i18n.js`, `el.json`, `en.json` to `frontend/public/i18n/`
- [ ] Add `<script src="i18n/i18n.js">` before app JS
- [ ] Init with `N2Vi18n.init({ el, en })`
- [ ] Replace `fmtD` with `N2Vi18n.fmtDate`
- [ ] Replace alert/confirm strings (6 instances)
- [ ] Replace empty state strings (4 instances)
- [ ] Replace contract card labels (8 instances)
- [ ] Replace button labels (~5 instances)
- [ ] Replace badge text (2 instances)
- [ ] Replace dropdown defaults (2 instances)
- [ ] Replace notification text (1 instance)
- [ ] Replace export date locale (1 instance)
- [ ] Test: app looks identical in Greek ✓
- [ ] Test: change locale to 'en' in console → everything English ✓
- [ ] Add language switcher button (Phase 4)
