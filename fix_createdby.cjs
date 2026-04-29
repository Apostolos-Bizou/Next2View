const fs = require('fs');
const f = 'frontend/src/views/ProjectDetailView.vue';
let c = fs.readFileSync(f, 'utf8');

// The history section should only show when project is loaded AND has history
// Replace the v-if on createdBy to be safe
c = c.replace(
  'v-if="project.createdBy"',
  'v-if="project && project.createdBy"'
);

// Also wrap the entire history panel in v-if="project"
c = c.replace(
  '<div class="project-history-panel"',
  '<div v-if="project" class="project-history-panel"'
);

fs.writeFileSync(f, c, 'utf8');
console.log('DONE: fixed createdBy null check');