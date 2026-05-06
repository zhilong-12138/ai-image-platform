<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">分类管理</h1>
        <p class="page-subtitle">维护提示词分类，用于用户端筛选展示</p>
      </div>
      <button class="btn-primary" @click="handleCreate">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        新建分类
      </button>
    </div>

    <div class="search-bar">
      <div class="search-field">
        <div class="search-input" :class="{ 'is-focus': nameFocus }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
            <path d="M16.5 16.5L21 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <input
            v-model="searchName"
            type="text"
            placeholder="搜索分类名称"
            @focus="nameFocus = true"
            @blur="nameFocus = false"
            @keyup.enter="handleSearch"
          />
          <span v-if="searchName" class="clear-btn" @click="searchName = ''">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
        </div>
      </div>

      <div class="search-field">
        <div class="search-input" :class="{ 'is-focus': codeFocus }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <input
            v-model="searchCode"
            type="text"
            placeholder="搜索分类编码"
            @focus="codeFocus = true"
            @blur="codeFocus = false"
            @keyup.enter="handleSearch"
          />
          <span v-if="searchCode" class="clear-btn" @click="searchCode = ''">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
        </div>
      </div>

      <div class="status-filter">
        <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 140px;">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </div>

      <button class="btn-primary" @click="handleSearch">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
          <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
          <path d="M16.5 16.5L21 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        搜索
      </button>
      <button class="btn-outline" @click="handleReset">重置</button>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="tableData" style="width: 100%" :header-cell-style="tableHeaderStyle">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="code" label="编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status === 1 ? 'status-active' : 'status-disabled'">
              <span class="dot"></span>
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <button class="btn-text" @click="handleEdit(row)">编辑</button>
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
    </div>

    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑分类' : '新建分类'" width="520px" :close-on-click-modal="false">
      <div class="edit-form">
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">名称</label>
            <input v-model="editForm.name" class="form-input" placeholder="例如：人像" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">编码</label>
            <input v-model="editForm.code" class="form-input" placeholder="例如：portrait" />
          </div>
        </div>
        <div class="form-row form-row--grid">
          <div class="form-field">
            <label class="form-label">排序</label>
            <input type="number" v-model="editForm.sort" min="0" class="form-input" />
          </div>
          <div class="form-field">
            <label class="form-label">状态</label>
            <div class="toggle-wrap">
              <button class="toggle-btn" :class="{ 'is-on': editForm.status === 1 }" @click="editForm.status = editForm.status === 1 ? 0 : 1">
                <span class="toggle-knob"></span>
              </button>
              <span class="toggle-label">{{ editForm.status === 1 ? '启用' : '禁用' }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn-outline" @click="editDialogVisible = false">取消</button>
        <button class="btn-primary" :class="{ 'is-loading': saving }" @click="handleSave" :disabled="saving">
          <span v-if="!saving">保存</span>
          <span v-else class="loading-dots"><span></span><span></span><span></span></span>
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { categoryApi } from '../api'

const tableHeaderStyle = {
  background: '#F5F5F7',
  color: '#1D1D1F',
  fontWeight: '600',
  fontSize: '13px',
  border: 'none',
  padding: '14px 16px'
}

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])

const searchName = ref('')
const searchCode = ref('')
const searchStatus = ref(undefined)
const nameFocus = ref(false)
const codeFocus = ref(false)

const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  name: '',
  code: '',
  sort: 0,
  status: 1
})

const buildQueryParams = () => {
  const params = {}
  if (searchName.value && searchName.value.trim()) params.name = searchName.value.trim()
  if (searchCode.value && searchCode.value.trim()) params.code = searchCode.value.trim()
  if (searchStatus.value !== undefined && searchStatus.value !== null) params.status = searchStatus.value
  return params
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.list(buildQueryParams())
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchCategories()
}

const handleReset = () => {
  searchName.value = ''
  searchCode.value = ''
  searchStatus.value = undefined
  fetchCategories()
}

const handleCreate = () => {
  editForm.value = { id: null, name: '', code: '', sort: 0, status: 1 }
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name || '',
    code: row.code || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1
  }
  editDialogVisible.value = true
}

const handleSave = async () => {
  if (!editForm.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (!editForm.value.code.trim()) {
    ElMessage.warning('请输入分类编码')
    return
  }
  saving.value = true
  try {
    const payload = {
      name: editForm.value.name.trim(),
      code: editForm.value.code.trim(),
      sort: Number(editForm.value.sort || 0),
      status: editForm.value.status === 1 ? 1 : 0
    }
    let res
    if (editForm.value.id) {
      res = await categoryApi.update(editForm.value.id, payload)
    } else {
      res = await categoryApi.create(payload)
    }
    if (res.code === 200) {
      ElMessage.success('已保存')
      editDialogVisible.value = false
      fetchCategories()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    const res = await categoryApi.update(row.id, { status: nextStatus })
    if (res.code === 200) {
      row.status = nextStatus
      ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchCategories()
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
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
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

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.search-field {
  flex: 1;
  min-width: 220px;
}

.search-input {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  padding: 10px 12px;
  color: var(--apple-text-secondary);
  transition: all 0.2s;
}

.search-input.is-focus {
  border-color: rgba(0,113,227,0.4);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12);
}

.search-input input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 14px;
  color: var(--apple-text);
  background: transparent;
}

.clear-btn {
  cursor: pointer;
  color: rgba(0,0,0,0.38);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.status-filter {
  min-width: 140px;
}

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
  white-space: nowrap;
}

.btn-primary:hover:not(:disabled) {
  background: #0077ED;
}

.btn-primary.is-loading {
  opacity: 0.8;
  cursor: not-allowed;
}

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
  white-space: nowrap;
}

.btn-outline:hover {
  border-color: rgba(0,0,0,0.2);
  background: rgba(0,0,0,0.03);
}

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

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(0,0,0,0.04);
  padding: 14px 16px;
  font-size: 14px;
}

.action-btns {
  display: flex;
  gap: 10px;
}

.btn-text {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: #007AFF;
  padding: 0;
}

.btn-text.btn-danger {
  color: #FF3B30;
}

.btn-text.btn-success {
  color: #34C759;
}

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
}

.status-dot .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-active {
  color: #34C759;
}

.status-active .dot {
  background: #34C759;
}

.status-disabled {
  color: #FF3B30;
}

.status-disabled .dot {
  background: #FF3B30;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: flex;
  gap: 14px;
}

.form-row--grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-field {
  flex: 1;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text);
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  border: 1.5px solid rgba(0,0,0,0.10);
  background: white;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: rgba(0,113,227,0.4);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12);
}

.toggle-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
}

.toggle-btn {
  width: 44px;
  height: 26px;
  border-radius: 13px;
  border: none;
  background: rgba(0,0,0,0.12);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  padding: 0;
}

.toggle-btn.is-on {
  background: rgba(52,199,89,0.9);
}

.toggle-knob {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: all 0.2s;
}

.toggle-btn.is-on .toggle-knob {
  left: 20px;
}

.toggle-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-secondary);
}

.loading-dots {
  display: flex;
  gap: 5px;
  justify-content: center;
  align-items: center;
}

.loading-dots span {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: white;
  animation: dot-bounce 1.2s ease-in-out infinite;
}

.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.6; }
  40% { transform: translateY(-5px); opacity: 1; }
}
</style>
