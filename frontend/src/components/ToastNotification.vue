<template>
  <transition-group name="toast-slide" tag="div" class="toast-container">
    <div v-for="toast in toasts" :key="toast.id" class="toast-card" @click="dismiss(toast.id)">
      <div class="toast-icon">{{ iconFor(toast.actionType) }}</div>
      <div class="toast-body">
        <div class="toast-title">{{ toast.actorName }}</div>
        <div class="toast-desc">{{ toast.description || toast.actionType + ' ' + toast.entityType }}</div>
      </div>
      <button class="toast-close" @click.stop="dismiss(toast.id)">&times;</button>
    </div>
  </transition-group>
</template>

<script setup>
import { ref } from 'vue'

const toasts = ref([])
let counter = 0

function addToast(data) {
  const id = ++counter
  toasts.value.push({ ...data, id })
  setTimeout(() => dismiss(id), 5000)
}

function dismiss(id) {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

function iconFor(type) {
  const icons = {
    CREATED: '+', UPDATED: '~', DELETED: 'x', DESCRIPTION_UPDATED: '~',
    TASK_ADDED: '+', TASK_REMOVED: 'x', TASK_COMPLETED: '\u2713',
    TASK_REOPENED: '\u21BA', TASK_PROGRESS: '%', TASK_REASSIGNED: '\u2192',
    TASK_BLOCKED: '!', TASK_UNBLOCKED: '\u2713',
    UPLOADED: '\u2191', COMPLETED: '\u2713'
  }
  return icons[type] || '\u2022'
}

defineExpose({ addToast })
</script>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column-reverse;
  gap: 8px;
  max-width: 380px;
}
.toast-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  background: var(--surface2, #1e2533);
  border: 1px solid var(--border-bright, #2d3748);
  border-left: 3px solid var(--accent, #3b82f6);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.3);
  cursor: pointer;
  transition: all 0.2s;
}
.toast-card:hover {
  background: var(--surface3, #252d3d);
  transform: translateX(-4px);
}
.toast-icon {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  background: var(--accent-dim, rgba(59,130,246,0.15));
  color: var(--accent, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
.toast-body { flex: 1; min-width: 0; }
.toast-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--text, #e2e8f0);
  margin-bottom: 2px;
}
.toast-desc {
  font-size: 11px;
  color: var(--text-dim, #8899aa);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.toast-close {
  background: none;
  border: none;
  color: var(--text-dim, #8899aa);
  font-size: 16px;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  flex-shrink: 0;
}
.toast-close:hover { color: var(--text, #e2e8f0); }

.toast-slide-enter-active { transition: all 0.3s ease; }
.toast-slide-leave-active { transition: all 0.2s ease; }
.toast-slide-enter-from { opacity: 0; transform: translateX(60px); }
.toast-slide-leave-to { opacity: 0; transform: translateX(60px); }
</style>
