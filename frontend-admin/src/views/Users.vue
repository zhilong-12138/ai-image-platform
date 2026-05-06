<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">管理系统用户账户与积分</p>
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
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="tableData" style="width: 100%" :header-cell-style="tableHeaderStyle" fit>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="level" label="等级" width="70">
          <template #default="{ row }">
            <span class="level-badge">Lv.{{ row.level ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="80">
          <template #default="{ row }">
            <span class="points-value">{{ row.points ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status === 1 ? 'status-active' : 'status-disabled'">
              <span class="dot"></span>
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="150" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <button class="btn-text" @click="handleViewDetail(row)">详情</button>
              <button class="btn-text" @click="handleEditPoints(row)">积分</button>
              <button
                class="btn-text"
                :class="row.status === 1 ? 'btn-danger' : 'btn-success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </button>
            </div>
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
          @current-change="fetchUsers"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="520px" :close-on-click-modal="false">
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">邮箱</span>
          <span class="detail-value">{{ detailData.email || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">等级</span>
          <span class="detail-value">
            <span class="level-badge">Lv.{{ detailData.level ?? '-' }}</span>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">当前积分</span>
          <span class="detail-value points-value">{{ detailData.points ?? 0 }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态</span>
          <span class="detail-value">
            <span class="status-dot" :class="detailData.status === 1 ? 'status-active' : 'status-disabled'">
              <span class="dot"></span>
              {{ detailData.status === 1 ? '正常' : '禁用' }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邀请码</span>
          <span class="detail-value">{{ detailData.inviteCode || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邀请人</span>
          <span class="detail-value">{{ detailData.inviteBy || '-' }}</span>
        </div>
        <div class="detail-item detail-item--full">
          <span class="detail-label">注册时间</span>
          <span class="detail-value">{{ detailData.createdAt || '-' }}</span>
        </div>
      </div>
      <div class="detail-actions">
        <button class="btn-primary" @click="openDialogPoints">调整积分</button>
        <button
          class="btn-outline"
          :class="detailData.status === 1 ? 'btn-outline-danger' : 'btn-outline-success'"
          @click="handleDialogToggleStatus"
        >
          {{ detailData.status === 1 ? '禁用用户' : '启用用户' }}
        </button>
      </div>
    </el-dialog>

    <!-- 调整积分对话框 -->
    <el-dialog v-model="pointsDialogVisible" title="调整积分" width="440px" :close-on-click-modal="false">
      <div class="points-form">
        <div class="points-row">
          <span class="points-row-label">用户</span>
          <span class="points-row-value">{{ pointsForm.email }}</span>
        </div>
        <div class="points-row">
          <span class="points-row-label">当前积分</span>
          <span class="points-row-value points-value-lg">{{ pointsForm.currentPoints }}</span>
        </div>
        <div class="points-row">
          <span class="points-row-label">调整数量</span>
          <div class="points-input-wrap">
            <button class="points-step" @click="pointsForm.amount = Math.max(-99999, pointsForm.amount - 10)">−</button>
            <input type="number" v-model="pointsForm.amount" class="points-input" />
            <button class="points-step" @click="pointsForm.amount = Math.min(99999, pointsForm.amount + 10)">+</button>
          </div>
        </div>
        <div class="points-preview">
          调整后积分：<span class="points-result">{{ pointsForm.currentPoints + pointsForm.amount }}</span>
        </div>
      </div>
      <template #footer>
        <button class="btn-outline" @click="pointsDialogVisible = false">取消</button>
        <button class="btn-primary" @click="handleConfirmPoints">确定调整</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

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

const detailDialogVisible = ref(false)
const detailData = ref({})
const pointsDialogVisible = ref(false)
const pointsForm = ref({
  userId: null,
  email: '',
  currentPoints: 0,
  amount: 0
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.list(page.value, pageSize.value, searchEmail.value || undefined)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const handleReset = () => {
  searchEmail.value = ''
  page.value = 1
  fetchUsers()
}

const handleSizeChange = () => {
  page.value = 1
  fetchUsers()
}

const handleViewDetail = async (row) => {
  try {
    const res = await userApi.detail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error('获取用户详情失败')
    }
  } catch (e) {
    ElMessage.error('获取用户详情失败')
  }
}

const openDialogPoints = () => {
  pointsForm.value = {
    userId: detailData.value.id,
    email: detailData.value.email,
    currentPoints: detailData.value.points ?? 0,
    amount: 0
  }
  detailDialogVisible.value = false
  pointsDialogVisible.value = true
}

const handleDialogToggleStatus = async () => {
  const newStatus = detailData.value.status === 1 ? 0 : 1
  try {
    const res = await userApi.disable(detailData.value.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
      detailData.value.status = newStatus
      const row = tableData.value.find(r => r.id === detailData.value.id)
      if (row) row.status = newStatus
      detailDialogVisible.value = false
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleEditPoints = (row) => {
  pointsForm.value = {
    userId: row.id,
    email: row.email,
    currentPoints: row.points ?? 0,
    amount: 0
  }
  pointsDialogVisible.value = true
}

const handleConfirmPoints = async () => {
  if (pointsForm.value.amount === 0) {
    ElMessage.warning('调整数量不能为 0')
    return
  }
  try {
    const res = await userApi.updatePoints(pointsForm.value.userId, pointsForm.value.amount)
    if (res.code === 200) {
      ElMessage.success('积分已调整')
      pointsDialogVisible.value = false
      fetchUsers()
    } else {
      ElMessage.error(res.msg || '调整失败')
    }
  } catch (e) {
    ElMessage.error('调整积分失败')
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await userApi.disable(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
      row.status = newStatus
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchUsers()
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

.search-input input::placeholder {
  color: var(--apple-muted);
}

.clear-btn {
  color: var(--apple-muted);
  cursor: pointer;
  display: flex;
  padding: 4px;
}
.clear-btn:hover {
  color: var(--apple-text);
}

/* Table Card */
.table-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow-x: auto;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #F5F5F7;
}

:deep(.el-table__body tr:hover > td) {
  background: #FAFAFA !important;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(0,0,0,0.04);
  padding: 14px 16px;
  font-size: 14px;
}

:deep(.el-table__header-wrapper .cell) {
  white-space: normal;
  word-break: normal;
}

:deep(.el-table__body-wrapper .cell) {
  white-space: normal;
  word-break: break-word;
}

:deep(.el-table__row:last-child td) {
  border-bottom: none;
}

.level-badge {
  display: inline-block;
  background: rgba(0, 113, 227, 0.1);
  color: #007AFF;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
}

.points-value {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: var(--apple-text);
}

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-active { color: #30D158; }
.status-active .dot { background: #30D158; }
.status-disabled { color: #86868B; }
.status-disabled .dot { background: #86868B; }

/* Action buttons */
.action-btns {
  display: flex;
  gap: 4px;
  align-items: center;
}

.btn-text {
  padding: 5px 10px;
  border: none;
  background: rgba(0, 113, 227, 0.08);
  color: #007AFF;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-text:hover {
  background: rgba(0, 113, 227, 0.15);
}

.btn-text.btn-danger {
  background: rgba(255, 59, 48, 0.08);
  color: #FF3B30;
}
.btn-text.btn-danger:hover {
  background: rgba(255, 59, 48, 0.15);
}

.btn-text.btn-success {
  background: rgba(48, 209, 88, 0.08);
  color: #30D158;
}
.btn-text.btn-success:hover {
  background: rgba(48, 209, 88, 0.15);
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

:deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: #007AFF;
}

/* Primary button */
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
.btn-primary:hover {
  background: #0077ED;
}

/* Detail dialog */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: var(--apple-text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.detail-value {
  font-size: 15px;
  color: var(--apple-text);
  font-weight: 500;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

/* Points form */
.points-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.points-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.points-row-label {
  width: 80px;
  font-size: 14px;
  color: var(--apple-text-secondary);
  flex-shrink: 0;
}

.points-row-value {
  font-size: 15px;
  color: var(--apple-text);
  font-weight: 500;
}

.points-value-lg {
  font-size: 18px;
}

.points-input-wrap {
  display: flex;
  align-items: center;
  gap: 0;
  background: #F5F5F7;
  border-radius: 10px;
  overflow: hidden;
  border: 1.5px solid transparent;
}

.points-input-wrap:focus-within {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.1);
}

.points-step {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  color: var(--apple-text-secondary);
  font-size: 18px;
  cursor: pointer;
  font-family: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
}
.points-step:hover {
  color: var(--apple-text);
  background: rgba(0,0,0,0.04);
}

.points-input {
  width: 80px;
  text-align: center;
  border: none;
  background: transparent;
  font-size: 16px;
  font-weight: 600;
  color: var(--apple-text);
  outline: none;
  font-family: inherit;
  padding: 8px 0;
}

.points-preview {
  font-size: 14px;
  color: var(--apple-text-secondary);
  padding: 12px 16px;
  background: #F5F5F7;
  border-radius: 10px;
}

.points-result {
  font-weight: 700;
  color: #007AFF;
  font-size: 16px;
}

/* Outline buttons */
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
.btn-outline-danger {
  border-color: rgba(255,59,48,0.3);
  color: #FF3B30;
}
.btn-outline-danger:hover {
  background: rgba(255,59,48,0.05);
  border-color: rgba(255,59,48,0.4);
}
.btn-outline-success {
  border-color: rgba(48,209,88,0.3);
  color: #30D158;
}
.btn-outline-success:hover {
  background: rgba(48,209,88,0.05);
  border-color: rgba(48,209,88,0.4);
}

/* Dialog styles */
:deep(.el-dialog) {
  border-radius: 20px;
  padding: 0;
  overflow: hidden;
}
:deep(.el-dialog__header) {
  padding: 24px 28px 20px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  margin-right: 0;
}
:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: var(--apple-text);
  letter-spacing: -0.02em;
}
:deep(.el-dialog__body) {
  padding: 28px;
}
:deep(.el-dialog__footer) {
  padding: 16px 28px 24px;
  border-top: 1px solid rgba(0,0,0,0.04);
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
