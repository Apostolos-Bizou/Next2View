<template>
  <div class="login-shell">
    <div class="login-card">
      <div class="login-logo">
        <div class="logo-mark">Next2me Group</div>
        <div class="logo-name">Next<span>2</span>View</div>
        <div class="logo-sub">CEO Command Center</div>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="field">
          <label>Email</label>
          <input
            v-model="email"
            type="email"
            placeholder="apostolos@next2me.com"
            autocomplete="email"
            required
          />
        </div>
        <div class="field">
          <label>Password</label>
          <input
            v-model="password"
            type="password"
            placeholder="••••••••"
            autocomplete="current-password"
            required
          />
        </div>
        <div v-if="mfaRequired" class="field">
          <label>MFA Code</label>
          <input
            v-model="mfaCode"
            type="text"
            placeholder="6-digit code"
            maxlength="6"
            autocomplete="one-time-code"
          />
        </div>

        <div v-if="error" class="login-error">⚠ {{ error }}</div>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>{{ mfaRequired ? "Verify MFA" : "Sign In" }}</span>
        </button>
      </form>

      <div class="forgot-link" @click="router.push('/forgot-password')">{{ t('auth.forgotPassword') }}</div>
      <div class="login-footer">
        Next2View v1.0 · Next2me Group · Private
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

const router = useRouter();
const auth = useAuthStore();
const { t } = useI18n();

const email = ref("");
const password = ref("");
const mfaCode = ref("");
const mfaRequired = ref(false);
const loading = ref(false);
const error = ref("");

async function handleLogin() {
  loading.value = true;
  error.value = "";
  const result = await auth.login(
    email.value,
    password.value,
    mfaRequired.value ? mfaCode.value : null
  );
  loading.value = false;

  if (result.mfaRequired) {
    mfaRequired.value = true;
    return;
  }
  if (result.success) {
    router.push("/");
  } else {
    error.value = result.error || "Login failed";
  }
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  background: var(--sidebar-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-card {
  background: var(--surface);
  border-radius: 16px;
  padding: 40px 36px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 24px 60px rgba(0,0,0,0.3);
}
.login-logo {
  text-align: center;
  margin-bottom: 32px;
}
.logo-mark {
  font-family: "Nunito Sans", sans-serif;
  font-size: 9px;
  color: var(--sidebar-active-border);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.logo-name {
  font-size: 32px;
  font-weight: 900;
  color: var(--text);
  letter-spacing: -1px;
}
.logo-name span { color: var(--accent); }
.logo-sub {
  font-size: 12px;
  color: var(--text-dim);
  margin-top: 4px;
  font-family: "Nunito Sans", sans-serif;
  font-weight: 600;
}
.login-form { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: var(--text-dim);
  font-family: "Nunito Sans", sans-serif;
}
.field input {
  background: var(--surface2);
  border: 1px solid var(--border-bright);
  border-radius: 8px;
  padding: 11px 14px;
  color: var(--text);
  font-family: "Nunito", sans-serif;
  font-size: 14px;
  transition: border-color 0.15s;
}
.field input:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface);
}
.login-error {
  background: var(--red-dim);
  border: 1px solid rgba(220,38,38,0.2);
  border-radius: 8px;
  padding: 10px 14px;
  color: var(--red);
  font-size: 13px;
  font-weight: 600;
}
.login-btn {
  width: 100%;
  padding: 13px;
  background: var(--accent);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-family: "Nunito", sans-serif;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 4px;
}
.login-btn:hover:not(:disabled) { background: #2563eb; }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.forgot-link { text-align:center; margin-top:12px; font-size:12px; color:var(--accent); cursor:pointer; font-weight:600; }
.forgot-link:hover { text-decoration:underline; }
.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 10px;
  color: var(--text-dim);
  font-family: "Nunito Sans", sans-serif;
}
</style>