const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// 1. Add permissions import
c = c.replace(
  'import { useProjectStore } from "@/stores/projects";',
  'import { useProjectStore } from "@/stores/projects";\nimport { usePermissionStore } from "@/stores/permissions";'
);

// 2. Add permStore instance
c = c.replace(
  'const store = useProjectStore();\nconst router = useRouter();\nconst ganttFilter = ref("");',
  'const store = useProjectStore();\nconst permStore = usePermissionStore();\nconst router = useRouter();\nconst ganttFilter = ref("");'
);

// 3. Add visibleProjects computed (filtered by permissions) - insert after ganttFilter line
c = c.replace(
  'const categories = [',
  'const visibleProjects = computed(() =>\n  store.projects.filter(p => permStore.canViewCategory(p.category))\n);\n\nconst categories = ['
);

// 4. visibleCategories - only show cats user can see
c = c.replace(
  'const categories = [\n  { key: "finance",   label: "Finance",    icon: "$" },\n  { key: "legal",     label: "Legal",      icon: "▪" },\n  { key: "dev",       label: "Developing", icon: "✦" },\n  { key: "marketing", label: "Marketing",  icon: "●" },\n];',
  'const categories = [\n  { key: "finance",   label: "Finance",    icon: "$" },\n  { key: "legal",     label: "Legal",      icon: "▪" },\n  { key: "dev",       label: "Developing", icon: "✦" },\n  { key: "marketing", label: "Marketing",  icon: "●" },\n];\n\nconst visibleCategories = computed(() =>\n  categories.filter(cat => permStore.canViewCategory(cat.key))\n);'
);

// 5. catCompletion uses visibleProjects
c = c.replace(
  'const catCompletion = (cat) => {\n  const ps = store.byCategory(cat);\n  if (!ps.length) return 0;\n  return Math.round(ps.reduce((s, p) => s + p.completion, 0) / ps.length);\n};',
  'const catCompletion = (cat) => {\n  const ps = visibleProjects.value.filter(p => p.category === cat);\n  if (!ps.length) return 0;\n  return Math.round(ps.reduce((s, p) => s + p.completion, 0) / ps.length);\n};'
);

// 6. upcomingDeadlines uses visibleProjects
c = c.replace(
  'const upcomingDeadlines = computed(() =>\n  [...store.projects].filter(p => p.deadline)',
  'const upcomingDeadlines = computed(() =>\n  [...visibleProjects.value].filter(p => p.deadline)'
);

// 7. recentActivity uses visibleProjects
c = c.replace(
  'const recentActivity = computed(() =>\n  [...store.projects].sort((a, b) => a.updatedAgo - b.updatedAgo).slice(0, 5)\n);',
  'const recentActivity = computed(() =>\n  [...visibleProjects.value].sort((a, b) => a.updatedAgo - b.updatedAgo).slice(0, 5)\n);'
);

// 8. ganttProjects uses visibleProjects
c = c.replace(
  'const ganttProjects = computed(() => {\n  let ps = store.projects.filter(p => p.deadline)',
  'const ganttProjects = computed(() => {\n  let ps = visibleProjects.value.filter(p => p.deadline)'
);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', c, 'utf8');
console.log('DashboardView.vue OK');
