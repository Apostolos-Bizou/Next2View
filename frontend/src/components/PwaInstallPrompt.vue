<template>
  <Transition name="pwa-slide">
    <div v-if="showPrompt" class="pwa-prompt">
      <div class="pwa-prompt-inner">
        <div class="pwa-icon">
          <img src="/icons/icon-192.png" alt="Next2View" />
        </div>
        <div class="pwa-text">
          <div class="pwa-title">{{ t('pwa.title') }}</div>
          <div class="pwa-subtitle">{{ t('pwa.subtitle') }}</div>
        </div>
        <div class="pwa-actions">
          <button class="pwa-btn-install" @click="install">{{ t('pwa.install') }}</button>
          <button class="pwa-btn-dismiss" @click="dismiss">{{ t('pwa.notNow') }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const showPrompt = ref(false)
let deferredPrompt = null

onMounted(() => {
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    deferredPrompt = e
    setTimeout(() => { showPrompt.value = true }, 3000)
  })
  window.addEventListener('appinstalled', () => {
    showPrompt.value = false
    deferredPrompt = null
  })
})

async function install() {
  if (!deferredPrompt) return
  deferredPrompt.prompt()
  const { outcome } = await deferredPrompt.userChoice
  if (outcome === 'accepted') showPrompt.value = false
  deferredPrompt = null
}

function dismiss() {
  showPrompt.value = false
  localStorage.setItem('pwa-dismissed', Date.now())
}
</script>

<style scoped>
.pwa-prompt {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  width: 90%;
  max-width: 480px;
}
.pwa-prompt-inner {
  background: #1c2333;
  border: 1px solid rgba(99,179,237,0.25);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
}
.pwa-icon img {
  width: 52px;
  height: 52px;
  border-radius: 12px;
}
.pwa-text {
  flex: 1;
}
.pwa-title {
  color: #e2e8f0;
  font-weight: 700;
  font-size: 14px;
  margin-bottom: 3px;
}
.pwa-subtitle {
  color: #718096;
  font-size: 12px;
}
.pwa-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pwa-btn-install {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 7px 16px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}
.pwa-btn-install:hover { background: #2563eb; }
.pwa-btn-dismiss {
  background: transparent;
  color: #718096;
  border: none;
  font-size: 11px;
  cursor: pointer;
  text-align: center;
}
.pwa-btn-dismiss:hover { color: #a0aec0; }
.pwa-slide-enter-active, .pwa-slide-leave-active {
  transition: all 0.4s ease;
}
.pwa-slide-enter-from, .pwa-slide-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}
</style>
