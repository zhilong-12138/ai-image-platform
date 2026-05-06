<template>
  <div class="admin-login">
    <div class="noise-overlay"></div>
    <div class="login-card">
      <div class="login-logo">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
          <rect width="48" height="48" rx="14" fill="url(#loginGrad)"/>
          <path d="M14 24C14 19.58 17.58 16 22 16C26.42 16 30 19.58 30 24" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="22" cy="24" r="3" fill="white"/>
          <defs>
            <linearGradient id="loginGrad" x1="0" y1="0" x2="48" y2="48">
              <stop offset="0%" stop-color="#0071E3"/>
              <stop offset="100%" stop-color="#5856D6"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
      <h1 class="login-title">管理后台</h1>
      <p class="login-subtitle">ImageStudio 管理员登录</p>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-field">
          <label class="field-label">管理员邮箱</label>
          <div class="field-input" :class="{ 'is-focus': inputFocus, 'is-error': errorMsg }">
            <input
              v-model="form.email"
              type="email"
              placeholder="admin@example.com"
              @focus="inputFocus = true"
              @blur="inputFocus = false"
            />
          </div>
        </div>

        <div v-if="errorMsg" class="error-tip">
          {{ errorMsg }}
        </div>

        <button type="submit" class="login-btn" :class="{ 'is-loading': loading }" :disabled="loading">
          <span v-if="!loading">登录</span>
          <span v-else class="loading-dots">
            <span></span><span></span><span></span>
          </span>
        </button>
      </form>

      <p class="login-footer">ImageStudio Admin Portal · v1.0</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api'

const router = useRouter()

const form = ref({ email: '' })
const loading = ref(false)
const inputFocus = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  errorMsg.value = ''
  if (!form.value.email) {
    errorMsg.value = '请输入管理员邮箱'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errorMsg.value = '请输入有效的邮箱地址'
    return
  }

  loading.value = true
  try {
    const res = await adminApi.login(form.value.email)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      errorMsg.value = res.msg || '登录失败，请检查邮箱'
    }
  } catch (e) {
    errorMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #F5F5F7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  position: relative;
  overflow: hidden;
}

/* Subtle radial glow instead of gradient */
.admin-login::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 800px 600px at 50% 40%, rgba(0, 113, 227, 0.06) 0%, transparent 70%);
  pointer-events: none;
}

.noise-overlay {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'><filter id='n'><feTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' stitchTiles='stitch'/><feColorMatrix values='0 0 0 0 0.078  0 0 0 0 0.078  0 0 0 0 0.074  0 0 0 0.025 0'/></filter><rect width='100%' height='100%' filter='url(%23n)'/></svg>");
  opacity: 0.4;
  pointer-events: none;
}

.login-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 24px;
  padding: 48px 40px 40px;
  width: 100%;
  max-width: 400px;
  box-shadow:
    0 20px 60px -20px rgba(0, 0, 0, 0.12),
    0 8px 24px -8px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
}

.login-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 16px rgba(0, 113, 227, 0.25));
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #1D1D1F;
  text-align: center;
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.login-subtitle {
  font-size: 14px;
  color: #86868B;
  text-align: center;
  margin: 0 0 36px;
  font-weight: 400;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #1D1D1F;
  letter-spacing: -0.01em;
}

.field-input {
  background: #F5F5F7;
  border: 1.5px solid transparent;
  border-radius: 12px;
  transition: all 0.2s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.field-input.is-focus {
  background: #FFFFFF;
  border-color: #007AFF;
  box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.12);
}

.field-input.is-error {
  border-color: #FF3B30;
  box-shadow: 0 0 0 4px rgba(255, 59, 48, 0.1);
}

.field-input input {
  width: 100%;
  padding: 14px 16px;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #1D1D1F;
  outline: none;
  font-family: inherit;
  border-radius: 12px;
}

.field-input input::placeholder {
  color: #AEAEB2;
}

.error-tip {
  font-size: 13px;
  color: #FF3B30;
  padding: 0 4px;
}

.login-btn {
  margin-top: 8px;
  width: 100%;
  padding: 14px 24px;
  background: #007AFF;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
  font-family: inherit;
  letter-spacing: -0.01em;
  min-height: 50px;
}

.login-btn:hover:not(:disabled) {
  background: #0077ED;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.3);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.login-btn.is-loading {
  opacity: 0.8;
  cursor: not-allowed;
}

.loading-dots {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: white;
  animation: dot-bounce 1.2s ease-in-out infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.6; }
  40% { transform: translateY(-6px); opacity: 1; }
}

.login-footer {
  text-align: center;
  font-size: 11px;
  color: #AEAEB2;
  margin: 28px 0 0;
  letter-spacing: 0.05em;
}
</style>