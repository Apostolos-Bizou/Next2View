const fs = require('fs');
const f = 'frontend/src/views/NotificationsView.vue';
let c = fs.readFileSync(f, 'utf8');

if (c.includes('activity-section')) {
  // CSS exists but HTML missing — need to add HTML
  if (c.includes('v-if="activeTab===\'activity\'"')) {
    console.log('SKIP: activity HTML already exists');
    process.exit(0);
  }
}

const activityHtml = `
    <!-- ACTIVITY TAB -->
    <div v-if="activeTab==='activity'" class="activity-section">
      <div class="activity-controls">
        <button class="filter-btn" @click="loadActivity">\u21BB {{ t('notif.refresh') || 'Refresh' }}</button>
        <button v-if="activityLog.length" class="filter-btn dismiss-all-btn" @click="dismissAllActivities">\uD83D\uDDD1\uFE0F {{ t('notif.clearAll') || 'Clear All' }}</button>
      </div>
      <div v-if="activityLoading" class="notif-empty">
        <div class="notif-empty-ico">\u23F3</div>
        <div class="notif-empty-txt">{{ t('notif.loadingActivity') || 'Loading activity...' }}</div>
      </div>
      <div v-else-if="!activityLog.length" class="notif-empty">
        <div class="notif-empty-ico">\uD83D\uDCCB</div>
        <div class="notif-empty-txt">{{ t('notif.noActivity') || 'No recent activity' }}</div>
        <div class="notif-empty-sub">{{ t('notif.noActivitySub') || 'Actions will appear here as they happen.' }}</div>
      </div>
      <div v-else class="notif-list">
        <div v-for="a in activityLog" :key="a.id" class="notif-card info activity-card">
          <div class="notif-icon">{{ actionIcon(a.actionType) }}</div>
          <div class="notif-body">
            <div class="notif-title">{{ a.actorName }} {{ actionLabel(a.actionType) }} {{ entityLabel(a.entityType) }}</div>
            <div class="notif-desc">{{ a.entityName || a.description }}</div>
            <div class="notif-meta">
              <span v-if="a.category" :class="'notif-cat ' + a.category">{{ a.category }}</span>
              <span class="notif-days">{{ timeAgo(a.createdAt) }}</span>
              <button class="dismiss-btn" @click.stop="dismissActivity(a.id)" title="Dismiss">\u2715</button>
            </div>
          </div>
        </div>
      </div>
    </div>`;

// Insert before the last </div></template>
c = c.replace(
  '  </div>\n</template>',
  activityHtml + '\n  </div>\n</template>'
);

fs.writeFileSync(f, c, 'utf8');
console.log('DONE: Activity tab HTML added to template');