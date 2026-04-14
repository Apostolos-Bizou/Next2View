const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectDetailView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Add permissions import
c = c.replace(
  "import { useProjectStore } from '@/stores/projects'",
  "import { useProjectStore } from '@/stores/projects'\nimport { usePermissionStore } from '@/stores/permissions'"
);

// 2. Add permStore instance
c = c.replace(
  'const store = useProjectStore()',
  'const store = useProjectStore()\nconst permStore = usePermissionStore()'
);

// 3. Wrap Financial panel with v-if
c = c.replace(
  "<!-- FINANCIAL OVERVIEW -->\n      <div v-if=\"project.budget\" class=\"fin-panel\"",
  "<!-- FINANCIAL OVERVIEW -->\n      <div v-if=\"project.budget && permStore.can('viewFinancials')\" class=\"fin-panel\""
);

// 4. Wrap CEO Notes panel with v-if
c = c.replace(
  "<!-- CEO NOTES -->\n      <div class=\"notes-panel\"",
  "<!-- CEO NOTES -->\n      <div v-if=\"permStore.isCEO() || permStore.can('viewCeoNotes')\" class=\"notes-panel\""
);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectDetailView.vue', c, 'utf8');
console.log('ProjectDetailView.vue OK');
