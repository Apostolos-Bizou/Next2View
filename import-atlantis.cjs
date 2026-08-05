#!/usr/bin/env node
/**
 * import-atlantis.cjs — Extract the ATLANTIS Work Plan from the mockup HTML and
 * transform it into a Next2View project payload (atlantis-payload.json).
 *
 * READ-ONLY with respect to the system: no POST, no database access.
 * Output: ./atlantis-payload.json + console summary.
 */
'use strict';
const fs = require('fs');
const path = require('path');

const SOURCE = 'C:\\Users\\akage\\Documents\\Projects\\Next2View\\Next2View_WorkPlan_Mockup.html';
const OUT = path.join(__dirname, 'atlantis-payload.json');

// ── 1. extract the PLAN array (raw JSON decode, stop at first valid array) ──
const html = fs.readFileSync(SOURCE, 'utf8');
const markIdx = html.indexOf('const PLAN = ');
if (markIdx === -1) { console.error('ABORT: "const PLAN = " not found in source'); process.exit(1); }
const start = html.indexOf('[', markIdx);
if (start === -1) { console.error('ABORT: opening bracket not found'); process.exit(1); }

// balanced-bracket scan, string-aware — ends at the matching closing bracket
let depth = 0, inStr = false, escNext = false, end = -1;
for (let i = start; i < html.length; i++) {
  const c = html[i];
  if (escNext) { escNext = false; continue; }
  if (c === '\\') { escNext = true; continue; }
  if (c === '"') { inStr = !inStr; continue; }
  if (inStr) continue;
  if (c === '[') depth++;
  else if (c === ']') { depth--; if (depth === 0) { end = i; break; } }
}
if (end === -1) { console.error('ABORT: unbalanced brackets — PLAN array not closed'); process.exit(1); }

let PLAN;
try {
  PLAN = JSON.parse(html.slice(start, end + 1));
} catch (e) {
  console.error('ABORT: PLAN slice is not valid JSON:', e.message); process.exit(1);
}
if (!Array.isArray(PLAN) || !PLAN.length) { console.error('ABORT: PLAN is not a non-empty array'); process.exit(1); }

// ── 2. transform ────────────────────────────────────────────────────────────
let plusOneStripped = 0;
function toTime(v) {
  if (v === null || v === undefined || v === '') return null;
  let s = String(v).trim();
  if (/\+1$/.test(s)) { s = s.replace(/\+1$/, ''); plusOneStripped++; }
  return /^\d{2}:\d{2}$/.test(s) ? s + ':00' : s; // "HH:MM" -> "HH:MM:00"
}
const joinLines = v => (v === null || v === undefined || v === '') ? null : String(v).split('\n').map(s => s.trim()).filter(Boolean).join(',');

const modules = PLAN.map((p, mi) => ({
  name: p.phase,
  sortOrder: mi,
  description: null,
  tasks: (p.tasks || []).map((t, ti) => {
    const progress = Math.round((t.status || 0) * 100);
    return {
      name: (t.task || '').split('\n').map(s => s.trim()).filter(Boolean).join(' '),
      startDate: t.fromD || null,
      endDate: t.toD || null,
      startTime: toTime(t.fromT),
      endTime: toTime(t.toT),
      workDays: (t.days === null || t.days === undefined) ? null : t.days,
      environment: joinLines(t.env),
      assignee: joinLines(t.team),
      comment: t.remark || null,
      progress,
      isDone: progress === 100,
      isBlocked: false,
      blockNote: null,
      description: null,
      isGate: t.days === 0 || /sign-?off/i.test(t.task || ''),
      sortOrder: ti,
    };
  }),
}));

const payload = {
  title: 'Oceanis \u2192 Atlantis MIG',
  category: 'dev',
  workPlanEnabled: true,
  modules,
};

// ── 3. write output ─────────────────────────────────────────────────────────
fs.writeFileSync(OUT, JSON.stringify(payload, null, 2), { encoding: 'utf8' });

// ── 4. summary ──────────────────────────────────────────────────────────────
const allTasks = modules.flatMap(m => m.tasks);
const gates = allTasks.filter(t => t.isGate);
const nullDays = allTasks.filter(t => t.workDays === null);
const nullEnv = allTasks.filter(t => t.environment === null);
const nullTeam = allTasks.filter(t => t.assignee === null);
const longest = (arr, field) => arr.reduce((best, t) => {
  const v = t[field]; return (v && v.length > (best.len || 0)) ? { name: t.name, val: v, len: v.length } : best;
}, {});
const envMax = longest(allTasks, 'environment');
const asgMax = longest(allTasks, 'assignee');

console.log('════════ ATLANTIS import — summary (no POST, no DB) ════════');
console.log(`source:  ${SOURCE}`);
console.log(`output:  ${OUT}`);
console.log('─'.repeat(60));
console.log(`modules: ${modules.length}`);
console.log(`tasks:   ${allTasks.length}`);
console.log(`gates:   ${gates.length}`);
gates.forEach(g => console.log(`  ◆ ${g.name}${g.workDays === 0 ? '' : `  (workDays=${g.workDays} — name-rule match)`}`));
console.log('─'.repeat(60));
console.log(`tasks with null workDays:    ${nullDays.length}${nullDays.length ? '  → ' + nullDays.map(t => `"${t.name.slice(0, 40)}"`).join(', ') : ''}`);
console.log(`tasks with null environment: ${nullEnv.length}`);
console.log(`tasks with null assignee:    ${nullTeam.length}`);
console.log('─'.repeat(60));
console.log(`longest environment: ${envMax.len || 0} chars (limit 40)${(envMax.len || 0) > 40 ? '  ⚠ OVER LIMIT' : '  ✓'}`);
if (envMax.val) console.log(`  "${envMax.val}"  on task "${envMax.name}"`);
console.log(`longest assignee:    ${asgMax.len || 0} chars (limit 150)${(asgMax.len || 0) > 150 ? '  ⚠ OVER LIMIT' : '  ✓'}`);
if (asgMax.val) console.log(`  "${asgMax.val}"  on task "${asgMax.name}"`);
if (plusOneStripped) console.log(`note: stripped "+1" (overnight) suffix from ${plusOneStripped} time value(s) — the UI re-derives +1 from endTime < startTime`);
console.log('════════ done — nothing was posted anywhere ════════');
