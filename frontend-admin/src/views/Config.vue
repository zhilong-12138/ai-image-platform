<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">系统配置</h1>
        <p class="page-subtitle">平台基础参数与密钥设置</p>
      </div>
    </div>

    <div class="config-card" v-loading="loading">
      <div class="config-section">
        <div class="section-header">
          <h3 class="section-title">积分规则</h3>
          <p class="section-desc">新用户注册和邀请奖励的积分配置</p>
        </div>
        <div class="config-form">
          <div class="config-row">
            <div class="config-field">
              <label class="field-label">初始积分</label>
              <span class="field-hint">新用户注册时赠送的积分数量</span>
              <div class="field-input">
                <input type="number" v-model="configForm.initPoints" min="0" class="num-input" />
                <span class="num-suffix">积分</span>
              </div>
            </div>
          </div>
          <div class="config-row config-row--grid">
            <div class="config-field">
              <label class="field-label">邀请人奖励</label>
              <span class="field-hint">用户邀请他人注册后获得的积分</span>
              <div class="field-input">
                <input type="number" v-model="configForm.invitePoints" min="0" class="num-input" />
                <span class="num-suffix">积分</span>
              </div>
            </div>
            <div class="config-field">
              <label class="field-label">被邀请人奖励</label>
              <span class="field-hint">被邀请人注册后获得的积分</span>
              <div class="field-input">
                <input type="number" v-model="configForm.invitedPoints" min="0" class="num-input" />
                <span class="num-suffix">积分</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="config-divider"></div>

      <div class="config-section">
        <div class="section-header">
          <h3 class="section-title">服务密钥</h3>
          <p class="section-desc">第三方 AI 服务与存储配置</p>
        </div>
        <div class="config-form">
          <div class="config-row">
            <div class="config-field">
              <label class="field-label">API Key</label>
              <span class="field-hint">AI 图片生成服务的访问密钥</span>
              <div class="field-input field-input--password" :class="{ 'is-showing': showApiKey }">
                <input
                  v-model="configForm.apiKey"
                  :type="showApiKey ? 'text' : 'password'"
                  class="text-input"
                  placeholder="sk-..."
                />
                <button class="toggle-visibility" @click="showApiKey = !showApiKey">
                  <svg v-if="showApiKey" width="18" height="18" viewBox="0 0 24 24" fill="none">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
          <div class="config-row">
            <div class="config-field">
              <label class="field-label">OSS 访问地址</label>
              <span class="field-hint">阿里云 OSS 的基础访问 URL</span>
              <div class="field-input">
                <input v-model="configForm.ossBaseUrl" class="text-input" placeholder="https://bucket.oss-cn-..." />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="config-actions">
        <button class="btn-primary" :class="{ 'is-loading': saving }" @click="handleSave" :disabled="saving">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <polyline points="17 21 17 13 7 13 7 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <polyline points="7 3 7 8 15 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          保存配置
        </button>
        <button class="btn-outline" @click="handleReset">重置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { configApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const showApiKey = ref(false)

const configForm = ref({
  initPoints: 100,
  invitePoints: 10,
  invitedPoints: 10,
  apiKey: '',
  ossBaseUrl: ''
})

const keyMap = {
  initPoints: 'init_points',
  invitePoints: 'invite_points',
  invitedPoints: 'invited_points',
  apiKey: 'api_key',
  ossBaseUrl: 'oss_base_url'
}

const fetchConfig = async () => {
  loading.value = true
  try {
    const res = await configApi.getAll()
    if (res.code === 200) {
      for (const item of res.data) {
        if (item.configKey === 'init_points') configForm.value.initPoints = parseInt(item.configValue) || 100
        if (item.configKey === 'invite_points') configForm.value.invitePoints = parseInt(item.configValue) || 10
        if (item.configKey === 'invited_points') configForm.value.invitedPoints = parseInt(item.configValue) || 10
        if (item.configKey === 'api_key') configForm.value.apiKey = item.configValue || ''
        if (item.configKey === 'oss_base_url') configForm.value.ossBaseUrl = item.configValue || ''
      }
    }
  } catch (e) {
    ElMessage.error('获取配置失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const updates = [
      configApi.update(keyMap.initPoints, configForm.value.initPoints.toString()),
      configApi.update(keyMap.invitePoints, configForm.value.invitePoints.toString()),
      configApi.update(keyMap.invitedPoints, configForm.value.invitedPoints.toString()),
      configApi.update(keyMap.apiKey, configForm.value.apiKey),
      configApi.update(keyMap.ossBaseUrl, configForm.value.ossBaseUrl)
    ]
    await Promise.allSettled(updates)
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  configForm.value = {
    initPoints: 100,
    invitePoints: 10,
    invitedPoints: 10,
    apiKey: '',
    ossBaseUrl: ''
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped>
.page {
  padding: 40px;
  max-width: 800px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--apple-text);
  margin: 0 0 6px;
  letter-spacing: -0.025em;
}

.page-subtitle {
  font-size: 15px;
  color: var(--apple-text-secondary);
  margin: 0;
  font-weight: 400;
}

/* Config Card */
.config-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.config-section {
  padding: 28px 32px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--apple-text);
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.section-desc {
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin: 0;
}

.config-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.04);
  margin: 0 32px;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-row {
  display: flex;
  gap: 16px;
}

.config-row--grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.config-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text);
  letter-spacing: -0.01em;
}

.field-hint {
  font-size: 12px;
  color: var(--apple-text-secondary);
}

.field-input {
  display: flex;
  align-items: center;
  gap: 0;
  background: #F5F5F7;
  border: 1.5px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.2s;
}

.field-input:focus-within {
  border-color: #007AFF;
  background: white;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.1);
}

.field-input--password {
  padding: 0 4px 0 14px;
}

.num-input {
  flex: 1;
  padding: 12px 14px;
  border: none;
  background: transparent;
  font-size: 15px;
  color: var(--apple-text);
  font-family: inherit;
  outline: none;
  min-width: 0;
}

.num-suffix {
  padding: 0 14px 0 4px;
  font-size: 14px;
  color: var(--apple-text-secondary);
  flex-shrink: 0;
}

.text-input {
  flex: 1;
  padding: 12px 14px;
  border: none;
  background: transparent;
  font-size: 15px;
  color: var(--apple-text);
  font-family: inherit;
  outline: none;
  min-width: 0;
}

.text-input::placeholder { color: var(--apple-muted); }

.toggle-visibility {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-text-secondary);
  flex-shrink: 0;
  border-radius: 6px;
  transition: all 0.2s;
}
.toggle-visibility:hover {
  color: var(--apple-text);
  background: rgba(0,0,0,0.04);
}

.config-actions {
  padding: 20px 32px 28px;
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Buttons */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 11px 22px;
  background: #007AFF;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-primary:hover:not(:disabled) {
  background: #0077ED;
}
.btn-primary.is-loading {
  opacity: 0.8;
  cursor: not-allowed;
}

.btn-outline {
  padding: 11px 22px;
  border: 1.5px solid rgba(0,0,0,0.12);
  background: transparent;
  color: var(--apple-text);
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-outline:hover {
  border-color: rgba(0,0,0,0.2);
  background: rgba(0,0,0,0.03);
}
</style>