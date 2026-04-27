// ═══════════════════════════════════════════════════════
//  Next2View — Lightweight i18n Module
//  Zero dependencies, works with single-file index.html
//  Usage: t('contract.title') → "Στοιχεία Σύμβασης" (el)
//                               → "Contract Details" (en)
// ═══════════════════════════════════════════════════════

const N2Vi18n = (() => {
  // ── State ──
  let _locale = localStorage.getItem('n2v_locale') || 'el';
  let _translations = {};   // { el: {...}, en: {...} }
  let _listeners = [];       // onChange callbacks

  // ── Load translations ──
  // Call this once at app startup with both locale objects
  function init(translations) {
    _translations = translations;
    const saved = localStorage.getItem('n2v_locale');
    if (saved && _translations[saved]) {
      _locale = saved;
    }
  }

  // ── Get current locale ──
  function locale() {
    return _locale;
  }

  // ── Set locale ──
  function setLocale(loc) {
    if (!_translations[loc]) {
      console.warn(`[i18n] Unknown locale: ${loc}`);
      return;
    }
    _locale = loc;
    localStorage.setItem('n2v_locale', loc);
    _listeners.forEach(fn => fn(loc));
  }

  // ── Toggle between el/en ──
  function toggle() {
    setLocale(_locale === 'el' ? 'en' : 'el');
  }

  // ── Translate a key ──
  // t('contract.title') → walks the nested object
  // t('errors.fileTooLarge') → "Το αρχείο είναι μεγαλύτερο από 10MB."
  function t(key, fallback) {
    const parts = key.split('.');
    let val = _translations[_locale];
    for (const p of parts) {
      if (val == null) break;
      val = val[p];
    }
    if (val !== undefined && val !== null) return val;

    // Fallback: try el, then return key
    if (_locale !== 'el') {
      let elVal = _translations['el'];
      for (const p of parts) {
        if (elVal == null) break;
        elVal = elVal[p];
      }
      if (elVal !== undefined && elVal !== null) return elVal;
    }

    return fallback || key;
  }

  // ── Listen for locale changes ──
  // Use this to re-render the UI when language changes
  function onChange(fn) {
    _listeners.push(fn);
    return () => { _listeners = _listeners.filter(f => f !== fn); };
  }

  // ── Date formatting (locale-aware) ──
  function fmtDate(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    const months = t('months.short');
    if (Array.isArray(months)) {
      return d.getDate() + ' ' + months[d.getMonth()] + ' ' + d.getFullYear();
    }
    return d.toLocaleDateString(_locale === 'el' ? 'el-GR' : 'en-US');
  }

  // ── Public API ──
  return { init, locale, setLocale, toggle, t, onChange, fmtDate };
})();

// Make globally available
window.N2Vi18n = N2Vi18n;
window.t = N2Vi18n.t;
