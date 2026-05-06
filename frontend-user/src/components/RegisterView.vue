<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <button class="back-btn" @click="$emit('back')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h2>注册账号</h2>
      </div>
      <p class="card-subtitle">创建账号，开始 AI 创作之旅</p>

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
        <p v-if="error" class="error-msg">{{ error }}</p>
      </div>

      <!-- Step 2: Code + Password -->
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
          <label>设置密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="至少 6 位字符"
            minlength="6"
          />
        </div>

        <div class="form-group">
          <label>邀请码 <span class="optional">(可选)</span></label>
          <input
            v-model="inviteCode"
            type="text"
            placeholder="好友邀请码"
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button
          class="submit-btn"
          :disabled="loading || code.length < 6 || password.length < 6"
          @click="handleRegister"
        >
          {{ loading ? '注册中…' : '完成注册' }}
        </button>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 'success'" class="form-section success-section">
        <div class="success-icon">✓</div>
        <h3>注册成功</h3>
        <p>账号已创建，即将跳转到登录页面…</p>
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
const inviteCode = ref('')
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
    await authApi.sendCode(email.value, 'register')
    step.value = 'code'
  } catch (e) {
    error.value = e.message || '发送验证码失败，请稍后重试'
  } finally {
    sending.value = false
  }
}

async function handleRegister() {
  error.value = ''
  if (code.value.length !== 6) {
    error.value = '请输入 6 位验证码'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码至少需要 6 位字符'
    return
  }
  loading.value = true
  try {
    await authApi.register(
      email.value,
      code.value,
      password.value,
      inviteCode.value.trim() || undefined
    )
    step.value = 'success'
    setTimeout(() => {
      emit('success')
    }, 1500)
  } catch (e) {
    error.value = e.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  z-index: 200;
}

.register-card {
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

.optional {
  font-weight: 400;
  color: var(--muted);
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
</style>