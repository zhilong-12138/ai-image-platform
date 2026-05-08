<template>
  <div class="reset-page">
    <div class="reset-card">
      <div class="card-header">
        <button class="back-btn" @click="$emit('back')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h2>重置密码</h2>
      </div>
      <p class="card-subtitle">输入邮箱，我们会帮你找回账号</p>

      <!-- Step 1: Email -->
      <div v-if="step === 'email'" class="form-section">
        <div class="form-group">
          <label>邮箱地址</label>
          <div class="input-row">
            <input
              v-model="email"
              type="email"
              placeholder="your@email.com"
              @keyup.enter="handleSendCode"
            />
            <button
              class="send-code-btn"
              :disabled="sending"
              @click="handleSendCode"
            >
              {{ sending ? '发送中…' : '获取验证码' }}
            </button>
          </div>
        </div>
        <p class="hint-text">请输入注册时使用的邮箱地址</p>
        <p v-if="error" class="error-msg">{{ error }}</p>
      </div>

      <!-- Step 2: Code + New Password -->
      <div v-if="step === 'code'" class="form-section">
        <div class="form-group">
          <label>验证码 <span class="resend" @click="handleSendCode">重新发送</span></label>
          <input
            v-model="code"
            type="text"
            placeholder="请输入 6 位验证码"
            maxlength="6"
          />
          <p class="code-hint">验证码已发送至 {{ email }}</p>
        </div>

        <div class="form-group">
          <label>新密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="至少 6 位字符"
            minlength="6"
          />
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            @keyup.enter="handleReset"
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button
          class="submit-btn"
          :disabled="loading || code.length < 6 || password.length < 6"
          @click="handleReset"
        >
          {{ loading ? '处理中…' : '确认重置' }}
        </button>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 'success'" class="form-section success-section">
        <div class="success-icon">✓</div>
        <h3>密码已重置</h3>
        <p>请使用新密码登录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '../api/services.js'

const emit = defineEmits(['success', 'back'])

const step = ref('email')
const email = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const sending = ref(false)
const loading = ref(false)
const error = ref('')

async function handleSendCode() {
  error.value = ''
  if (!email.value.trim()) {
    error.value = '请输入邮箱地址'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    error.value = '请输入有效的邮箱地址'
    return
  }
  sending.value = true
  try {
    await authApi.resetPasswordSendCode(email.value)
    step.value = 'code'
  } catch (e) {
    error.value = e.message || '发送验证码失败，请稍后重试'
  } finally {
    sending.value = false
  }
}

async function handleReset() {
  error.value = ''
  if (code.value.length !== 6) {
    error.value = '请输入 6 位验证码'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码至少需要 6 位字符'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    await authApi.resetPassword(email.value, code.value, password.value)
    step.value = 'success'
    setTimeout(() => {
      emit('success')
    }, 1500)
  } catch (e) {
    error.value = e.message || '重置失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  z-index: 200;
}

.reset-card {
  background: var(--surface);
  border-radius: 20px;
  padding: 40px 48px;
  width: 400px;
  max-width: calc(100vw - 48px);
  box-shadow: 0 32px 80px rgba(0,0,0,0.15);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.back-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: transparent;
  color: var(--fg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.back-btn:hover {
  background: var(--bg);
  border-color: var(--accent);
}

.card-header h2 {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 400;
  color: var(--fg);
  margin: 0;
}

.card-subtitle {
  font-size: 14px;
  color: var(--muted);
  margin: 0 0 32px;
}

.form-section {
  margin-top: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--fg);
  margin-bottom: 8px;
}

.resend {
  margin-left: auto;
  color: var(--accent);
  cursor: pointer;
  font-weight: 400;
}
.resend:hover { text-decoration: underline; }

.input-row {
  display: flex;
  gap: 8px;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-family: inherit;
  font-size: 15px;
  color: var(--fg);
  background: var(--bg);
  outline: none;
  transition: border-color 0.15s;
  box-sizing: border-box;
}
.form-group input:focus { border-color: var(--accent); }

.send-code-btn {
  padding: 0 16px;
  border: 1px solid var(--accent);
  border-radius: 10px;
  background: transparent;
  font-family: inherit;
  font-size: 14px;
  color: var(--accent);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}
.send-code-btn:hover:not(:disabled) { background: var(--accent); color: var(--surface); }
.send-code-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.hint-text {
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 12px;
}

.code-hint {
  font-size: 12px;
  color: var(--muted);
  margin: 6px 0 0;
}

.error-msg {
  font-size: 13px;
  color: #e53e3e;
  margin: 0 0 12px;
  padding: 10px 14px;
  background: #fff5f5;
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 10px;
  background: var(--fg);
  font-family: inherit;
  font-size: 16px;
  font-weight: 500;
  color: var(--surface);
  cursor: pointer;
  transition: all 0.15s;
  letter-spacing: 0.02em;
  margin-top: 12px;
}
.submit-btn:hover:not(:disabled) { background: oklch(25% 0.02 60); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.success-section {
  text-align: center;
  padding: 24px 0;
}

.success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #10b981);
  color: white;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.success-section h3 {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 400;
  color: var(--fg);
  margin: 0 0 8px;
}

.success-section p {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

@media (max-width: 768px) {
  .reset-page {
    align-items: flex-end;
    padding: 0;
  }
  .reset-card {
    width: 100%;
    max-width: 100%;
    border-radius: 20px 20px 0 0;
    padding: 32px 24px 40px;
  }
  .input-row {
    flex-direction: column;
    gap: 10px;
  }
  .send-code-btn {
    padding: 12px 16px;
    width: 100%;
    text-align: center;
  }
  .form-group input {
    font-size: 16px; /* Prevent iOS zoom on focus */
  }
}
</style>