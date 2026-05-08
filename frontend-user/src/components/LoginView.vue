<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <span class="wordmark">灵<span>画</span></span>
      </div>
      <h2 class="login-title">欢迎回来</h2>
      <p class="login-subtitle">登录你的账号</p>

      <div class="login-form">
        <div class="form-group">
          <label>邮箱地址</label>
          <input
            v-model="email"
            type="email"
            placeholder="your@email.com"
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="输入密码"
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="form-footer">
          <span class="forgot-link" @click="$emit('reset')">忘记密码？</span>
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button
          class="login-btn"
          :disabled="loading || !email || !password"
          @click="handleLogin"
        >
          {{ loading ? '登录中…' : '登录' }}
        </button>

        <p class="switch-text">
          还没有账号？<span class="link" @click="$emit('register')">立即注册</span>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '../api/services.js'
import { useAuthStore } from '../stores/authStore.js'

const emit = defineEmits(['success', 'register', 'reset'])

const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  if (!email.value.trim()) {
    error.value = '请输入邮箱地址'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    error.value = '请输入有效的邮箱地址'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  loading.value = true
  try {
    const data = await authApi.login(email.value, password.value)
    authStore.setAuth(data.token, data.user)
    emit('success')
  } catch (e) {
    error.value = e.message || '登录失败，请检查邮箱和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  z-index: 200;
}

.login-card {
  background: var(--surface);
  border-radius: 20px;
  padding: 48px;
  width: 400px;
  max-width: calc(100vw - 48px);
  box-shadow: 0 32px 80px rgba(0,0,0,0.15);
}

.login-logo {
  text-align: center;
  margin-bottom: 32px;
}

.wordmark {
  font-family: var(--font-display);
  font-size: 32px;
  color: var(--fg);
}
.wordmark span {
  color: var(--accent);
}

.login-title {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 400;
  text-align: center;
  margin: 0 0 8px;
  color: var(--fg);
}

.login-subtitle {
  font-size: 14px;
  color: var(--muted);
  text-align: center;
  margin: 0 0 32px;
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

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.forgot-link {
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.15s;
}
.forgot-link:hover { color: var(--accent); }

.error-msg {
  font-size: 13px;
  color: #e53e3e;
  margin: 0 0 12px;
  padding: 10px 14px;
  background: #fff5f5;
  border-radius: 8px;
}

.login-btn {
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
}
.login-btn:hover:not(:disabled) { background: oklch(25% 0.02 60); }
.login-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.switch-text {
  font-size: 14px;
  color: var(--muted);
  text-align: center;
  margin: 24px 0 0;
}

.link {
  color: var(--accent);
  cursor: pointer;
  font-weight: 500;
}
.link:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .login-page {
    align-items: flex-end;
    padding: 0;
  }
  .login-card {
    width: 100%;
    max-width: 100%;
    border-radius: 20px 20px 0 0;
    padding: 32px 24px 40px;
  }
  .login-logo {
    margin-bottom: 24px;
  }
  .wordmark {
    font-size: 28px;
  }
  .login-title {
    font-size: 20px;
  }
  .form-group input {
    font-size: 16px; /* Prevent iOS zoom on focus */
  }
}
</style>