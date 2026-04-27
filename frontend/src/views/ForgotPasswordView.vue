<template>
  <div class="login-shell">
    <div class="login-card">
      <div class="login-logo">
        <div class="logo-mark">Next2me Group</div>
        <div class="logo-name">Next<span>2</span>View</div>
        <div class="logo-sub">{{ t('auth.resetTitle') }}</div>
      </div>

      <div v-if="!sent">
        <form class="login-form" @submit.prevent="handleSubmit">
          <div class="field">
            <label>Email</label>
            <input v-model="email" type="email" placeholder="apostolos@next2me.com" required />
          </div>
          <div v-if="error" class="login-error">⚠ {{ error }}</div>
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>{{ t('auth.sendLink') }}</span>
          </button>
        </form>
      </div>

      <div v-else class="success-box">
        <div class="success-icon">✓</div>
        <div class="success-title">{{ t('auth.emailSent') }}</div>
        <div class="success-msg">{{ t('auth.emailSentMsg') }}</div>
      </div>

      <div class="back-link" @click="router.push('/login')">{{ t('auth.backToLogin') }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
const email = ref('')
const loading = ref(false)
const error = ref('')
const sent = ref(false)

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    await api.post('/auth/forgot-password', { email: email.value })
    sent.value = true
  } catch (e) {
    error.value = t('auth.sendError')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell { min-height:100vh; background:var(--sidebar-bg); display:flex; align-items:center; justify-content:center; padding:20px; }
.login-card { background:var(--surface); border-radius:16px; padding:40px 36px; width:100%; max-width:400px; box-shadow:0 24px 60px rgba(0,0,0,0.3); }
.login-logo { text-align:center; margin-bottom:32px; }
.logo-mark { font-family:"Nunito Sans",sans-serif; font-size:9px; color:var(--sidebar-active-border); letter-spacing:3px; text-transform:uppercase; margin-bottom:6px; }
.logo-name { font-size:32px; font-weight:900; color:var(--text); letter-spacing:-1px; }
.logo-name span { color:var(--accent); }
.logo-sub { font-size:12px; color:var(--text-dim); margin-top:4px; font-family:"Nunito Sans",sans-serif; font-weight:600; }
.login-form { display:flex; flex-direction:column; gap:16px; }
.field { display:flex; flex-direction:column; gap:6px; }
.field label { font-size:10px; font-weight:700; letter-spacing:1.5px; text-transform:uppercase; color:var(--text-dim); font-family:"Nunito Sans",sans-serif; }
.field input { background:var(--surface2); border:1px solid var(--border-bright); border-radius:8px; padding:11px 14px; color:var(--text); font-family:"Nunito",sans-serif; font-size:14px; }
.field input:focus { outline:none; border-color:var(--accent); }
.login-error { background:var(--red-dim); border:1px solid rgba(220,38,38,0.2); border-radius:8px; padding:10px 14px; color:var(--red); font-size:13px; font-weight:600; }
.login-btn { width:100%; padding:13px; background:var(--accent); border:none; border-radius:8px; color:#fff; font-family:"Nunito",sans-serif; font-size:14px; font-weight:800; cursor:pointer; display:flex; align-items:center; justify-content:center; gap:8px; }
.login-btn:hover:not(:disabled) { background:#2563eb; }
.login-btn:disabled { opacity:0.6; cursor:not-allowed; }
.spinner { width:16px; height:16px; border:2px solid rgba(255,255,255,0.3); border-top-color:#fff; border-radius:50%; animation:spin 0.7s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }
.success-box { text-align:center; padding:20px 0; }
.success-icon { font-size:48px; color:var(--green); margin-bottom:12px; }
.success-title { font-size:20px; font-weight:800; color:var(--text); margin-bottom:8px; }
.success-msg { font-size:13px; color:var(--text-dim); line-height:1.6; }
.back-link { text-align:center; margin-top:24px; font-size:12px; color:var(--accent); cursor:pointer; font-weight:600; }
.back-link:hover { text-decoration:underline; }
</style>