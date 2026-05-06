<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">积分日志</h1>
        <p class="page-subtitle">平台积分流动记录</p>
      </div>
    </div>

    <div class="search-bar">
      <div class="search-field">
        <div class="search-input" :class="{ 'is-focus': searchFocus }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
            <path d="M16.5 16.5L21 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <input
            v-model="searchEmail"
            type="email"
            placeholder="搜索用户邮箱"
            @focus="searchFocus = true"
            @blur="searchFocus = false"
            @keyup.enter="handleSearch"
          />
          <span v-if="searchEmail" class="clear-btn" @click="handleReset">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
        </div>
      </div>
      <button class="btn-primary" @click="handleSearch">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
          <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
          <path d="M16.5 16.5L21 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        搜索
      </button>
      <button v-if="searchEmail" class="btn-outline" @click="handleReset">重置</button>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="tableData" style="width: 100%" :header-cell-style="tableHeaderStyle">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户邮箱" width="220">
          <template #default="{ row }">
            <span class="user-email">{{ row.userEmail || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <span class="type-badge" :class="'type-' + row.type">{{ typeText(row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="变动数量" width="110">
          <template #default="{ row }">
            <span class="amount-value" :class="row.amount > 0 ? 'amount-positive' : 'amount-negative'">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="120">
          <template #default="{ row }">
            <span class="balance-value">{{ row.balance ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="remark-text">{{ row.remark || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar" v-if="total > 0">
        <span class="pagination-info">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          @current-change="fetchLogs"
          @size-change="() => { page = 1; fetchLogs(); }"
        />
      </div>

      <div v-if="!loading && tableData.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-desc">暂无积分记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pointLogApi, userApi } from '../api'

const tableHeaderStyle = {
  background: '#F5F5F7',
  color: '#1D1D1F',
  fontWeight: '600',
  fontSize: '13px',
  border: 'none',
  padding: '14px 16px'
}

const loading = ref(false)
const searchEmail = ref('')
const searchFocus = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const userEmailMap = ref({})

const typeText = (type) => {
  const map = { 1: '注册赠送', 2: '邀请奖励', 3: '充值', 4: '管理员调整' }
  return map[type] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

const fetchUserEmail = async (uid) => {
  if (userEmailMap.value[uid]) return userEmailMap.value[uid]
  try {
    const res = await userApi.detail(uid)
    if (res.code === 200) {
      userEmailMap.value[uid] = res.data.email
      return res.data.email
    }
  } catch (e) { /* ignore */ }
  return null
}

const fetchLogs = async () => {
  loading.value = true
  try {
    // Step 1: if searchEmail given, resolve userId first
    let resolvedUserId = null
    if (searchEmail.value.trim()) {
      const userRes = await userApi.list(1, 1, searchEmail.value.trim())
      if (userRes.code === 200 && userRes.data.records?.length > 0) {
        const matched = userRes.data.records.find(u => u.email === searchEmail.value.trim())
        if (matched) {
          resolvedUserId = matched.id
        } else {
          // email search matched but not exact — find by page scanning
          const allRes = await userApi.list(1, 100, searchEmail.value.trim())
          if (allRes.code === 200 && allRes.data.records?.length > 0) {
            const exact = allRes.data.records.find(r => r.email === searchEmail.value.trim())
            if (exact) resolvedUserId = exact.id
          }
        }
        if (!resolvedUserId) {
          tableData.value = []
          total.value = 0
          loading.value = false
          return
        }
      } else {
        tableData.value = []
        total.value = 0
        loading.value = false
        return
      }
    }

    // Step 2: fetch point logs
    const res = await pointLogApi.list(page.value, pageSize.value, resolvedUserId || undefined)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0

      // Enrich with user email
      const userIds = [...new Set(tableData.value.map(item => item.userId).filter(Boolean))]
      const pending = userIds.filter(uid => !userEmailMap.value[uid])
      for (const uid of pending) {
        const email = await fetchUserEmail(uid)
        if (email) {
          for (const item of tableData.value) {
            if (item.userId === uid) item.userEmail = email
          }
        }
      }
      // Fill already-known emails
      for (const item of tableData.value) {
        if (item.userId && userEmailMap.value[item.userId] && !item.userEmail) {
          item.userEmail = userEmailMap.value[item.userId]
        }
      }
    }
  } catch (e) {
    ElMessage.error('获取积分日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchLogs()
}

const handleReset = () => {
  searchEmail.value = ''
  page.value = 1
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.page {
  padding: 40px;
  max-width: 1400px;
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

/* Search */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.search-field {
  flex: 1;
  max-width: 360px;
}

.search-input {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--apple-surface);
  border: 1.5px solid transparent;
  border-radius: 12px;
  padding: 0 14px;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.search-input.is-focus {
  border-color: var(--apple-accent);
  box-shadow: 0 0 0 4px rgba(0,122,255,0.1), 0 2px 8px rgba(0,0,0,0.04);
}

.search-input svg {
  color: var(--apple-text-secondary);
  flex-shrink: 0;
}

.search-input input {
  flex: 1;
  padding: 11px 0;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--apple-text);
  outline: none;
  font-family: inherit;
}

.search-input input::placeholder { color: var(--apple-muted); }

.clear-btn {
  color: var(--apple-muted);
  cursor: pointer;
  display: flex;
  padding: 4px;
}
.clear-btn:hover { color: var(--apple-text); }

/* Buttons */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: #007AFF;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-primary:hover { background: #0077ED; }

.btn-outline {
  padding: 10px 18px;
  border: 1.5px solid rgba(0,0,0,0.12);
  background: transparent;
  color: var(--apple-text);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-outline:hover {
  border-color: rgba(0,0,0,0.2);
  background: rgba(0,0,0,0.03);
}

/* Table Card */
.table-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
}
:deep(.el-table__body tr:hover > td) {
  background: #FAFAFA !important;
}
:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(0,0,0,0.04);
  padding: 14px 16px;
  font-size: 14px;
}
:deep(.el-table__row:last-child td) {
  border-bottom: none;
}

.user-email {
  font-size: 13px;
  color: var(--apple-text);
}

.type-badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}
.type-1 { background: rgba(48, 209, 88, 0.1); color: #30D158; }
.type-2 { background: rgba(255, 149, 0, 0.1); color: #FF9500; }
.type-3 { background: rgba(0, 113, 227, 0.1); color: #007AFF; }
.type-4 { background: rgba(0, 0, 0, 0.05); color: #86868B; }

.amount-value {
  font-variant-numeric: tabular-nums;
  font-weight: 700;
  font-size: 14px;
}
.amount-positive { color: #30D158; }
.amount-negative { color: #FF3B30; }

.balance-value {
  font-variant-numeric: tabular-nums;
  color: var(--apple-text);
}

.remark-text {
  color: var(--apple-text-secondary);
  font-size: 13px;
}

.time-text {
  color: var(--apple-text-secondary);
  font-size: 13px;
}

/* Pagination */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid rgba(0,0,0,0.04);
}
.pagination-info {
  font-size: 13px;
  color: var(--apple-text-secondary);
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
}
.empty-icon {
  width: 72px;
  height: 72px;
  background: rgba(0,0,0,0.04);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-muted);
  margin-bottom: 16px;
}
.empty-desc {
  font-size: 14px;
  color: var(--apple-text-secondary);
  margin: 0;
}
</style>