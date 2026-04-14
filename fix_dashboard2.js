const fs = require('fs');
let c = fs.readFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', 'utf8');
c = c.replace(/\r\n/g, '\n');

// KPI strip loop
c = c.replace(
  '<div v-for="cat in categories" :key="cat.key" :class="`kpi ${cat.key}`">',
  '<div v-for="cat in visibleCategories" :key="cat.key" :class="`kpi ${cat.key}`">'
);

// Cat breakdown loop
c = c.replace(
  '<div v-for="cat in categories" :key="cat.key" class="cat-blk">',
  '<div v-for="cat in visibleCategories" :key="cat.key" class="cat-blk">'
);

// Also fix store.byCategory calls in template to use visibleProjects
c = c.replace(
  /store\.byCategory\(cat\.key\)\.reduce\(\(a,p\)=>a\+p\.tasksDone,0\)/g,
  'visibleProjects.filter(p => p.category === cat.key).reduce((a,p)=>a+p.tasksDone,0)'
);
c = c.replace(
  /store\.byCategory\(cat\.key\)\.reduce\(\(a,p\)=>a\+p\.tasksTotal,0\)/g,
  'visibleProjects.filter(p => p.category === cat.key).reduce((a,p)=>a+p.tasksTotal,0)'
);
c = c.replace(
  /store\.byCategory\(cat\.key\)\.length/g,
  'visibleProjects.filter(p => p.category === cat.key).length'
);

// Fix overall KPI to use visibleProjects count
c = c.replace(
  '<div class="kpi-sub">{{ store.projects.length }} projects</div>',
  '<div class="kpi-sub">{{ visibleProjects.length }} projects</div>'
);

fs.writeFileSync('C:\\Users\\akage\\Next2View\\frontend\\src\\views\\DashboardView.vue', c, 'utf8');
console.log('DashboardView template OK');
