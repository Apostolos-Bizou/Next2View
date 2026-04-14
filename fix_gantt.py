import re
path = r'C:\Users\akage\Next2View\frontend\src\views\DashboardView.vue'
content = open(path, encoding='utf-8').read()
old = "    weeks.push({ label: `${d.getDate()} ${months[d.getMonth()]}`, isToday })"
new = "    weeks.push({ num: i + 1, dateLabel: `${d.getDate()} ${months[d.getMonth()]}`, isCurrentWeek: isToday })"
result = content.replace(old, new)
print('Found:', old in content)
open(path, 'w', encoding='utf-8', newline='').write(result)
print('Done')
