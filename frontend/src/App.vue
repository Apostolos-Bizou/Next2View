<template>
  <router-view />
  <PwaInstallPrompt />
</template>
<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import PwaInstallPrompt from "@/components/PwaInstallPrompt.vue";
const auth = useAuthStore();
onMounted(() => {
  auth.init();
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js')
      .then(reg => console.log('SW registered:', reg.scope))
      .catch(err => console.log('SW error:', err));
  }
});
</script>
