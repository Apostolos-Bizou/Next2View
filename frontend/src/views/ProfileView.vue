<template>
  <div class="profile-view">
    <!-- Profile Header Card -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="profile-avatar">{{ initials }}</div>
        <div class="profile-info">
          <h2 class="profile-name">{{ user?.fullName || 'Χρήστης' }}</h2>
          <p class="profile-email">{{ user?.email || '' }}</p>
          <span class="profile-role">{{ user?.role || 'Χρήστης' }}</span>
        </div>
      </div>
    </div>

    <!-- Security Section Card -->
    <div class="profile-card">
      <div class="card-header">
        <h3 class="card-title">🔐 Ασφάλεια</h3>
      </div>
      <div class="card-content">
        
        <!-- MFA Row - Dynamic State -->
        <div class="security-row">
          <div class="security-info">
            <div class="security-label">Έλεγχος Ταυτότητας (MFA)</div>
            <div class="security-desc">Δεύτερος παράγοντας ελέγχου με εφαρμογή TOTP</div>
          </div>
          
          <!-- STATE A: MFA Disabled -->
          <div v-if="!mfaEnabled && setupState === 'idle'" class="security-controls">
            <span class="status-badge warning">⚠ Ανενεργό</span>
            <button class="btn-primary" @click="startSetup" :disabled="loading">
              Ενεργοποίηση MFA →
            </button>
          </div>

          <!-- STATE B: Setup Pending -->
          <div v-if="setupState === 'pending'" class="mfa-setup">
            <div class="setup-header">
              <h4>Ενεργοποίηση MFA</h4>
              <p>Σκανάρισε το QR code με την εφαρμογή TOTP (π.χ. Google Authenticator, Authy)</p>
            </div>
            
            <div class="setup-content">
              <div class="qr-section">
                <canvas ref="qrCanvas" class="qr-code" v-show="otpauthUrl"></canvas>
                <div class="manual-entry">
                  <label>Εναλλακτικά, χειροκίνητη εισαγωγή:</label>
                  <div class="secret-input">
                    <code class="secret-code">{{ secret }}</code>
                    <button class="btn-copy" @click="copySecret" title="Αντιγραφή">
                      📋
                    </button>
                  </div>
                </div>
              </div>
              
              <div class="verify-section">
                <label for="verify-code">Κώδικας επαλήθευσης (6 ψηφία):</label>
                <input 
                  id="verify-code"
                  v-model="codeInput" 
                  type="text" 
                  maxlength="6" 
                  placeholder="123456"
                  class="code-input"
                  :disabled="loading"
                />
                <div class="verify-controls">
                  <button class="btn-secondary" @click="cancelSetup" :disabled="loading">
                    Ακύρωση
                  </button>
                  <button class="btn-primary" @click="verifySetup" :disabled="!codeInput || codeInput.length !== 6 || loading">
                    {{ loading ? 'Επαλήθευση...' : 'Επαλήθευση' }}
                  </button>
                </div>
              </div>
            </div>
            
            <div v-if="error" class="error-message">{{ error }}</div>
          </div>

          <!-- STATE C: MFA Enabled -->
          <div v-if="mfaEnabled && setupState === 'idle'" class="security-controls">
            <span class="status-badge success">✅ Ενεργό</span>
            <button class="btn-danger" @click="startDisable" :disabled="loading">
              Απενεργοποίηση
            </button>
          </div>

          <!-- STATE C2: Disable Confirmation -->
          <div v-if="setupState === 'disabling'" class="mfa-disable">
            <div class="danger-warning">
              <h4>⚠️ Επικίνδυνη ενέργεια</h4>
              <p>Η απενεργοποίηση του MFA μειώνει την ασφάλεια του λογαριασμού σας.</p>
            </div>
            <div class="disable-form">
              <label for="disable-code">Εισάγετε κώδικα MFA για επιβεβαίωση:</label>
              <input 
                id="disable-code"
                v-model="codeInput" 
                type="text" 
                maxlength="6" 
                placeholder="123456"
                class="code-input"
                :disabled="loading"
              />
              <div class="verify-controls">
                <button class="btn-secondary" @click="cancelDisable" :disabled="loading">
                  Ακύρωση
                </button>
                <button class="btn-danger" @click="confirmDisable" :disabled="!codeInput || codeInput.length !== 6 || loading">
                  {{ loading ? 'Απενεργοποίηση...' : 'Επιβεβαίωση Απενεργοποίησης' }}
                </button>
              </div>
            </div>
            
            <div v-if="error" class="error-message">{{ error }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Info Box -->
    <div class="info-card">
      <h4>💡 Γιατί MFA;</h4>
      <p>Ο δεύτερος παράγοντας ελέγχου προστατεύει τον λογαριασμό σας ακόμα κι αν κάποιος μάθει τον κωδικό σας. Απαραίτητο για πρόσβαση σε νομικά έγγραφα υψηλής εμπιστευτικότητας.</p>
    </div>

    <!-- Success Message -->
    <div v-if="successMsg" class="success-message">{{ successMsg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import QRCode from 'qrcode'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

// Store
const authStore = useAuthStore()

// Reactive state
const setupState = ref('idle') // 'idle' | 'pending' | 'disabling'
const secret = ref('')
const otpauthUrl = ref('')
const codeInput = ref('')
const loading = ref(false)
const error = ref(null)
const successMsg = ref(null)

// Computed
const user = computed(() => authStore.user)
const mfaEnabled = computed(() => user.value?.mfaEnabled || false)

const initials = computed(() => {
  const name = user.value?.fullName || 'XX'
  return name.split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
})



// Methods
const qrCanvas = ref(null)

watch(otpauthUrl, async (val) => {
  if (!val) return
  await nextTick()
  if (!qrCanvas.value) return
  await QRCode.toCanvas(qrCanvas.value, val, { width: 200, margin: 2 })
})

const clearMessages = () => {
  error.value = null
  successMsg.value = null
}

const startSetup = async () => {
  loading.value = true
  clearMessages()
  
  try {
    const response = await api.post('/auth/mfa/setup')
    secret.value = response.data.secret
    otpauthUrl.value = response.data.otpauthUrl
    setupState.value = 'pending'
    await renderQR()
  } catch (err) {
    error.value = err.response?.data?.message || 'Αποτυχία δημιουργίας MFA setup'
  } finally {
    loading.value = false
  }
}

const verifySetup = async () => {
  loading.value = true
  clearMessages()
  
  try {
    await api.post('/auth/mfa/verify', { code: codeInput.value })
    
    // Update user MFA status (defensive pattern)
    if (authStore.user) {
      authStore.user.mfaEnabled = true
    }
    // Optional: call store method if exists
    if (authStore.updateUser && typeof authStore.updateUser === 'function') {
      await authStore.updateUser()
    }
    
    setupState.value = 'idle'
    codeInput.value = ''
    secret.value = ''
    otpauthUrl.value = ''
    successMsg.value = 'MFA ενεργοποιήθηκε επιτυχώς! 🎉'
    
    // Clear success message after 4 seconds
    setTimeout(() => {
      successMsg.value = null
    }, 4000)
    
  } catch (err) {
    error.value = err.response?.data?.message || 'Μη έγκυρος κώδικας επαλήθευσης'
  } finally {
    loading.value = false
  }
}

const cancelSetup = () => {
  setupState.value = 'idle'
  codeInput.value = ''
  secret.value = ''
  otpauthUrl.value = ''
  clearMessages()
}

const startDisable = () => {
  setupState.value = 'disabling'
  codeInput.value = ''
  clearMessages()
}

const confirmDisable = async () => {
  loading.value = true
  clearMessages()
  
  try {
    await api.post('/auth/mfa/disable', { code: codeInput.value })
    
    // Update user MFA status (defensive pattern)
    if (authStore.user) {
      authStore.user.mfaEnabled = false
    }
    // Optional: call store method if exists
    if (authStore.updateUser && typeof authStore.updateUser === 'function') {
      await authStore.updateUser()
    }
    
    setupState.value = 'idle'
    codeInput.value = ''
    successMsg.value = 'MFA απενεργοποιήθηκε.'
    
    // Clear success message after 4 seconds
    setTimeout(() => {
      successMsg.value = null
    }, 4000)
    
  } catch (err) {
    error.value = err.response?.data?.message || 'Μη έγκυρος κώδικας επαλήθευσης'
  } finally {
    loading.value = false
  }
}

const cancelDisable = () => {
  setupState.value = 'idle'
  codeInput.value = ''
  clearMessages()
}

const copySecret = async () => {
  try {
    await navigator.clipboard.writeText(secret.value)
    // Temporary success feedback
    const originalText = secret.value
    secret.value = 'Αντιγράφηκε!'
    setTimeout(() => {
      secret.value = originalText
    }, 1000)
  } catch (err) {
    console.warn('Clipboard API not available:', err)
  }
}

// Lifecycle
onMounted(() => {
  clearMessages()
})
</script>

<style scoped>
.profile-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card,
.info-card {
  background: var(--surface, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.profile-header {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--accent, #3b82f6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
}

.profile-name {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text, #1f2937);
}

.profile-email {
  margin: 0 0 8px 0;
  color: var(--text-mid, #6b7280);
  font-size: 14px;
}

.profile-role {
  display: inline-block;
  padding: 4px 12px;
  background: var(--accent-dim, #dbeafe);
  color: var(--accent, #3b82f6);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.card-header {
  padding: 20px 24px 0 24px;
  border-bottom: 1px solid var(--border, #e2e8f0);
}

.card-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text, #1f2937);
}

.card-content {
  padding: 20px 24px 24px 24px;
}

.security-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.security-info {
  flex: 1;
}

.security-label {
  font-weight: 600;
  color: var(--text, #1f2937);
  margin-bottom: 4px;
}

.security-desc {
  font-size: 14px;
  color: var(--text-mid, #6b7280);
}

.security-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.success {
  background: var(--green-dim, #dcfce7);
  color: var(--green, #16a34a);
}

.status-badge.warning {
  background: var(--yellow-dim, #fef3c7);
  color: var(--yellow, #d97706);
}

.btn-primary,
.btn-secondary,
.btn-danger,
.btn-copy {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--accent, #3b82f6);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--accent-dark, #2563eb);
}

.btn-secondary {
  background: var(--surface2, #f8fafc);
  color: var(--text-mid, #6b7280);
  border: 1px solid var(--border, #e2e8f0);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--border, #e2e8f0);
}

.btn-danger {
  background: var(--red, #dc2626);
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: var(--red-dark, #b91c1c);
}

.btn-copy {
  background: var(--surface2, #f8fafc);
  border: 1px solid var(--border, #e2e8f0);
  padding: 4px 8px;
  font-size: 12px;
}

.btn-copy:hover {
  background: var(--border, #e2e8f0);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mfa-setup,
.mfa-disable {
  width: 100%;
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 8px;
  padding: 20px;
  background: var(--surface2, #f8fafc);
}

.setup-header h4,
.danger-warning h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text, #1f2937);
}

.setup-header p,
.danger-warning p {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-mid, #6b7280);
}

.setup-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 16px;
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.qr-code {
  width: 200px;
  height: 200px;
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 8px;
}

.manual-entry {
  width: 100%;
}

.manual-entry label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-mid, #6b7280);
  margin-bottom: 8px;
}

.secret-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.secret-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  padding: 8px 12px;
  background: var(--surface, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 6px;
  flex: 1;
  word-break: break-all;
}

.verify-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.verify-section label,
.disable-form label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text, #1f2937);
}

.code-input {
  padding: 12px;
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 8px;
  font-size: 16px;
  text-align: center;
  letter-spacing: 2px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.code-input:focus {
  outline: none;
  border-color: var(--accent, #3b82f6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.verify-controls {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.danger-warning {
  background: var(--red-dim, #fef2f2);
  border: 1px solid var(--red, #dc2626);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.danger-warning h4 {
  color: var(--red, #dc2626);
}

.disable-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-card {
  padding: 20px 24px;
  background: var(--accent-dim, #dbeafe);
  border-color: var(--accent, #3b82f6);
}

.info-card h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--accent, #3b82f6);
}

.info-card p {
  margin: 0;
  font-size: 14px;
  color: var(--text-mid, #6b7280);
  line-height: 1.5;
}

.error-message {
  background: var(--red-dim, #fef2f2);
  border: 1px solid var(--red, #dc2626);
  color: var(--red, #dc2626);
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  margin-top: 12px;
}

.success-message {
  background: var(--green-dim, #dcfce7);
  border: 1px solid var(--green, #16a34a);
  color: var(--green, #16a34a);
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .profile-view {
    padding: 16px;
    gap: 16px;
  }
  
  .profile-header {
    padding: 20px;
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .profile-avatar {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  
  .security-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .setup-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .qr-code {
    width: 160px;
    height: 160px;
  }
  
  .verify-controls {
    flex-direction: column;
  }
}
</style>