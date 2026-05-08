<template>
  <div>
    <header class="content-header">
      <div class="section-eyebrow">我的</div>
      <h1 class="section-title">个人资料</h1>
    </header>

    <div class="profile-sections">
      <!-- Basic info -->
      <div class="profile-card">
        <div class="profile-card-title">基本信息</div>
        <div class="profile-list">
          <div class="profile-row">
            <span class="profile-label">邮箱</span>
            <span class="profile-value">{{ user.email || '-' }}</span>
          </div>
          <div class="profile-row">
            <span class="profile-label">用户 ID</span>
            <span class="profile-value mono">#{{ user.id || '-' }}</span>
          </div>
          <div class="profile-row">
            <span class="profile-label">注册时间</span>
            <span class="profile-value">{{ formatDate(user.createdAt) }}</span>
          </div>
          <div class="profile-row">
            <span class="profile-label">积分余额</span>
            <span class="profile-value accent">{{ (user.points || 0).toLocaleString() }} 积分</span>
          </div>
        </div>
      </div>

      <!-- Invite code -->
      <div class="profile-card">
        <div class="profile-card-title">分享邀请码</div>
        <div class="invite-code-row">
          <div class="invite-code-box">{{ user.inviteCode || '-' }}</div>
          <button class="invite-copy-btn" @click="copyInvite">
            {{ copied ? '已复制' : '复制链接' }}
          </button>
        </div>
        <p class="invite-hint">分享您的邀请码，好友注册后您将获得额外积分奖励</p>
      </div>

      <!-- Usage stats -->
      <div class="profile-card">
        <div class="profile-card-title">使用统计</div>
        <div class="stats-summary">
          <div class="stats-item">
            <div class="stats-number">{{ totalGenerations }}</div>
            <div class="stats-label">图片生成次数</div>
          </div>
        </div>
        <div v-if="loadingHistory" class="loading-hint">加载中…</div>
        <div v-else-if="creditHistory.length" class="credits-table">
          <div class="credits-table-header">
            <span>ID</span><span>时间</span><span>消耗</span><span>剩余</span>
          </div>
          <div
              v-for="row in creditHistory"
              :key="row.id"
              class="credits-table-row"
          >
            <span class="mono">#{{ String(row.id).padStart(4, '0') }}</span>
            <span>{{ formatDate(row.createdAt) }}</span>
            <span class="cost">{{ row.amount }}</span>
            <span>{{ (row.balance || 0).toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useAuthStore} from '../stores/authStore.js'

defineEmits(['refresh'])

const authStore = useAuthStore()
const user = computed(() => authStore.user || {})

const creditHistory = ref([])
const loadingHistory = ref(false)
const copied = ref(false)
const totalGenerations = ref(0)

function formatDate(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  if (isNaN(d.getTime())) return dt
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

async function copyInvite() {
  try {
    const link = window.location.origin + '/invite/' + (user.value.inviteCode || '')
    await navigator.clipboard.writeText(link)
    copied.value = true
    setTimeout(() => (copied.value = false), 1500)
  } catch {
  }
}

onMounted(async () => {
  // Refresh user info
  await authStore.fetchUserInfo()

  // Load generation count for stats
  try {
    const {generationApi} = await import('../api/services.js')
    const res = await generationApi.count()
    totalGenerations.value = res || 0
  } catch {
  }

  // Load point logs
  try {
    const {api} = await import('../api/index.js')
    const res = await api.get('/admin/point-logs', {params: {page: 1, pageSize: 20}})
    creditHistory.value = res.data?.records || res.records || []
  } catch {
    creditHistory.value = []
  } finally {
    loadingHistory.value = false
  }
})
</script>

<style scoped>
.loading-hint {
  text-align: center;
  color: var(--muted);
  padding: 20px;
  font-size: 13px;
}
</style>