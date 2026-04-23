/**
 * Project Metrics Utilities
 *
 * Helper functions για τον υπολογισμό time progress, task progress,
 * smart status, και formatting για projects.
 *
 * Ο "smart status" υπολογίζεται δυναμικά συγκρίνοντας το %χρόνου που έχει
 * περάσει με το %tasks που έχουν ολοκληρωθεί. Έτσι ένας CEO βλέπει σε μία
 * ματιά αν το project είναι On Track ή έχει πέσει πίσω.
 */

/**
 * Υπολογίζει το % χρόνου που έχει περάσει μεταξύ startDate και deadline.
 * @returns {number} 0-100, ή null αν λείπει κάποια ημερομηνία
 */
export function getTimeProgress(startDate, deadline) {
  if (!startDate || !deadline) return null;

  const start = new Date(startDate).getTime();
  const end = new Date(deadline).getTime();
  const now = Date.now();

  if (end <= start) return null;
  if (now <= start) return 0;
  if (now >= end) return 100;

  const pct = ((now - start) / (end - start)) * 100;
  return Math.round(pct);
}

/**
 * Υπολογίζει τις μέρες που απομένουν μέχρι το deadline.
 * Αρνητικός αριθμός = έχει περάσει (overdue).
 */
export function getDaysRemaining(deadline) {
  if (!deadline) return null;
  const end = new Date(deadline).getTime();
  const now = Date.now();
  const MS_DAY = 24 * 60 * 60 * 1000;
  return Math.ceil((end - now) / MS_DAY);
}

/**
 * Smart status: υπολογίζει την κατάσταση του project με βάση
 * το χάσμα μεταξύ χρόνου και tasks.
 *
 * Κανόνες:
 *   taskProgress === 100            → Completed (✅)
 *   timeProgress === null/missing    → fallback στο existing status
 *   timeProgress > 100 (overdue)     → Overdue (⏰)
 *   gap = timeProgress - taskProgress
 *   gap < 10                         → On Track (🟢)
 *   gap < 30                         → Behind (🟡)
 *   gap >= 30                        → Critical (🔴)
 *
 * @returns {{ code, label, color, icon }}
 */
export function getSmartStatus(timeProgress, taskProgress, fallbackStatus = 'on_track') {
  // Completed case
  if (taskProgress >= 100) {
    return { code: 'completed', label: 'Completed', color: '#059669', icon: '✅' };
  }

  // Αν δεν έχουμε time progress, fallback στο static status από DB
  if (timeProgress === null || timeProgress === undefined) {
    return mapStaticStatus(fallbackStatus);
  }

  // Overdue
  if (timeProgress >= 100) {
    return { code: 'overdue', label: 'Overdue', color: '#dc2626', icon: '⏰' };
  }

  const gap = timeProgress - (taskProgress || 0);

  if (gap < 10) {
    return { code: 'on_track', label: 'On Track', color: '#059669', icon: '🟢' };
  } else if (gap < 30) {
    return { code: 'behind', label: 'Behind Schedule', color: '#d97706', icon: '🟡' };
  } else {
    return { code: 'critical', label: 'Critical', color: '#dc2626', icon: '🔴' };
  }
}

/**
 * Fallback mapper για static status από τη DB.
 */
function mapStaticStatus(status) {
  const map = {
    on_track:  { code: 'on_track',  label: 'On Track',  color: '#059669', icon: '🟢' },
    delayed:   { code: 'behind',    label: 'Delayed',   color: '#d97706', icon: '🟡' },
    at_risk:   { code: 'critical',  label: 'At Risk',   color: '#dc2626', icon: '🔴' },
    stale:     { code: 'stale',     label: 'Stale',     color: '#6b7280', icon: '⚫' },
    completed: { code: 'completed', label: 'Completed', color: '#059669', icon: '✅' },
  };
  return map[status] || map.on_track;
}

/**
 * Format μια ημερομηνία σε σύντομη Ελληνική μορφή. Π.χ. "23 Απρ 2026".
 */
export function formatShortDate(date) {
  if (!date) return '—';
  const d = new Date(date);
  const months = ['Ιαν', 'Φεβ', 'Μαρ', 'Απρ', 'Μάι', 'Ιούν',
                  'Ιούλ', 'Αύγ', 'Σεπ', 'Οκτ', 'Νοέ', 'Δεκ'];
  return `${d.getDate()} ${months[d.getMonth()]} ${d.getFullYear()}`;
}

/**
 * Format ημερομηνιακό εύρος. Π.χ. "23 Απρ → 1 Σεπ 2026".
 * Αν είναι ίδια χρονιά, δείχνει τον χρόνο μόνο μία φορά.
 */
export function formatDateRange(startDate, deadline) {
  if (!startDate && !deadline) return '—';
  if (!startDate) return formatShortDate(deadline);
  if (!deadline) return formatShortDate(startDate);

  const s = new Date(startDate);
  const e = new Date(deadline);
  const sameYear = s.getFullYear() === e.getFullYear();

  const months = ['Ιαν', 'Φεβ', 'Μαρ', 'Απρ', 'Μάι', 'Ιούν',
                  'Ιούλ', 'Αύγ', 'Σεπ', 'Οκτ', 'Νοέ', 'Δεκ'];

  const sStr = sameYear
    ? `${s.getDate()} ${months[s.getMonth()]}`
    : `${s.getDate()} ${months[s.getMonth()]} ${s.getFullYear()}`;
  const eStr = `${e.getDate()} ${months[e.getMonth()]} ${e.getFullYear()}`;

  return `${sStr} → ${eStr}`;
}

/**
 * Human-friendly label για days remaining.
 *   120  → "120 μέρες απομένουν"
 *   1    → "1 μέρα απομένει"
 *   0    → "Λήγει σήμερα"
 *  -5    → "Πέρασε 5 μέρες"
 */
export function formatDaysRemaining(days) {
  if (days === null || days === undefined) return '';
  if (days < 0) return `Πέρασε ${Math.abs(days)} μέρ${Math.abs(days) === 1 ? 'α' : 'ες'}`;
  if (days === 0) return 'Λήγει σήμερα';
  if (days === 1) return '1 μέρα απομένει';
  return `${days} μέρες απομένουν`;
}
