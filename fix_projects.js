const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectsView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Add permissions import after projectStore import
c = c.replace(
  'import { useProjectStore } from "@/stores/projects";',
  'import { useProjectStore } from "@/stores/projects";\nimport { usePermissionStore } from "@/stores/permissions";'
);

// 2. Add permStore instance after store declaration
c = c.replace(
  'const store = useProjectStore();\nconst router = useRouter();\nconst route = useRoute();',
  'const store = useProjectStore();\nconst permStore = usePermissionStore();\nconst router = useRouter();\nconst route = useRoute();'
);

// 3. Add permissions filter in the filtered computed
c = c.replace(
  'const filtered = computed(() => {\n  let ps = store.projects;\n  if (filterCat.value) ps = ps.filter(p => p.category === filterCat.value);\n  if (filterCo.value)  ps = ps.filter(p => p.companyId === filterCo.value);\n  return ps;\n});',
  'const filtered = computed(() => {\n  let ps = store.projects.filter(p => permStore.canViewCategory(p.category));\n  if (filterCat.value) ps = ps.filter(p => p.category === filterCat.value);\n  if (filterCo.value)  ps = ps.filter(p => p.companyId === filterCo.value);\n  return ps;\n});'
);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\ProjectsView.vue', c, 'utf8');
console.log('ProjectsView.vue OK');
