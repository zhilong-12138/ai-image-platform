<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">提示词管理</h1>
        <p class="page-subtitle">配置和管理图片生成提示词模板</p>
      </div>
      <button class="btn-primary" @click="handleCreate">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        新建提示词
      </button>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="tableData" style="width: 100%" :header-cell-style="tableHeaderStyle">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="示意图" width="100">
          <template #default="{ row }">
            <div v-if="row.imageUrls" class="thumb-wrap" @click="previewImage(row.imageUrls)">
              <img :src="row.imageUrls" class="thumb" />
            </div>
            <span v-else class="no-img">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === 1 ? 'status-published' : 'status-draft'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="参考图" width="100">
          <template #default="{ row }">
            <span v-if="row.refImageCount && row.refImageCount > 0" class="ref-badge">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="2"/>
                <circle cx="8" cy="8.5" r="1.5" fill="currentColor"/>
              </svg>
              {{ row.refImageCount }}张
            </span>
            <span v-else class="no-img">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <button class="btn-text" @click="handleEdit(row)">编辑</button>
              <button
                class="btn-text"
                :class="row.status === 1 ? 'btn-warning' : 'btn-success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '下架' : '发布' }}
              </button>
              <button class="btn-text btn-danger" @click="handleDelete(row)">删除</button>
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
          @current-change="fetchPrompts"
          @size-change="() => { page = 1; fetchPrompts(); }"
        />
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑提示词' : '新建提示词'" width="600px" :close-on-click-modal="false">
      <div class="edit-form">
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">标题</label>
            <input v-model="editForm.title" class="form-input" placeholder="输入提示词标题" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">分类</label>
            <el-select v-model="editForm.categoryId" placeholder="选择分类" clearable style="width: 100%;">
              <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">描述</label>
            <textarea v-model="editForm.description" class="form-input form-textarea" rows="2" placeholder="简短描述"></textarea>
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">内容</label>
            <textarea v-model="editForm.content" class="form-input form-textarea" rows="4" placeholder="提示词内容"></textarea>
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">示意图</label>
            <div class="image-upload-area">
              <div v-if="editForm.imageUrls" class="preview-wrap">
                <img :src="editForm.imageUrls" class="preview-img" />
                <span class="remove-img" @click="editForm.imageUrls = ''">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                  </svg>
                </span>
              </div>
              <div v-else class="upload-trigger" @click="triggerUpload">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>点击上传</span>
                <span class="upload-tip">PNG/JPG ≤5MB</span>
              </div>
              <span v-if="uploading" class="upload-status">上传中...</span>
            </div>
            <input ref="uploadInput" type="file" accept="image/*" class="hidden" @change="handleFileChange" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">API 地址</label>
            <input v-model="editForm.apiUrl" class="form-input" placeholder="https://..." />
          </div>
        </div>
        <div class="form-row form-row--grid">
          <div class="form-field">
            <label class="form-label">排序权重</label>
            <input type="number" v-model="editForm.sort" min="0" class="form-input" />
          </div>
          <div class="form-field">
            <label class="form-label">需要参考图</label>
            <div class="toggle-wrap">
              <button
                class="toggle-btn"
                :class="{ 'is-on': editForm.refImageEnabled }"
                @click="editForm.refImageEnabled = !editForm.refImageEnabled"
              >
                <span class="toggle-knob"></span>
              </button>
              <span v-if="editForm.refImageEnabled" class="ref-count-wrap">
                <input
                  type="number"
                  v-model="editForm.refImageCount"
                  min="1"
                  max="4"
                  class="ref-count-input"
                />
                <span class="ref-count-label">张</span>
              </span>
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

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="640px">
      <img :src="previewUrl" style="max-width: 100%; border-radius: 12px;" />
    </el-dialog>

    <!-- 删除确认 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <p style="color: var(--apple-text-secondary); font-size: 15px;">确定要删除提示词「{{ deleteTarget?.title }}」吗？此操作不可撤销。</p>
      <template #footer>
        <button class="btn-outline" @click="deleteDialogVisible = false">取消</button>
        <button class="btn-danger" @click="confirmDelete">确认删除</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { promptApi, categoryApi } from '../api'
import api from '../api'

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
const uploading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const uploadInput = ref(null)
const categoryOptions = ref([])

const previewVisible = ref(false)
const previewUrl = ref('')
const deleteDialogVisible = ref(false)
const deleteTarget = ref(null)

const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  categoryId: null,
  title: '',
  description: '',
  content: '',
  imageUrls: '',
  apiUrl: '',
  sort: 0,
  refImageEnabled: false,
  refImageCount: 1
})

const triggerUpload = () => {
  uploadInput.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await api.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      editForm.value.imageUrls = res.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    if (uploadInput.value) uploadInput.value.value = ''
  }
}

const fetchPrompts = async () => {
  loading.value = true
  try {
    const res = await promptApi.list(page.value, pageSize.value)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('获取提示词列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await categoryApi.list()
    if (res.code === 200) {
      categoryOptions.value = res.data || []
    }
  } catch (e) {
    // ignore
  }
}

const handleCreate = () => {
  editForm.value = { id: null, categoryId: null, title: '', description: '', content: '', imageUrls: '', apiUrl: '', sort: 0, refImageEnabled: false, refImageCount: 1 }
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = {
    ...row,
    refImageEnabled: !!(row.refImageCount && row.refImageCount > 0),
    refImageCount: row.refImageCount > 0 ? row.refImageCount : 1
  }
  editDialogVisible.value = true
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const handleSave = async () => {
  if (!editForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  saving.value = true
  try {
    let res
    const payload = { ...editForm.value }
    if (!payload.refImageEnabled) {
      payload.refImageCount = 0
    }
    if (payload.id) {
      res = await promptApi.update(payload.id, payload)
    } else {
      res = await promptApi.create(payload)
    }
    if (res.code === 200) {
      ElMessage.success('已保存')
      editDialogVisible.value = false
      fetchPrompts()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await promptApi.publish(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '已发布' : '已下架')
      row.status = newStatus
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  deleteTarget.value = row
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!deleteTarget.value) return
  try {
    const res = await promptApi.delete(deleteTarget.value.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      deleteDialogVisible.value = false
      deleteTarget.value = null
      fetchPrompts()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchPrompts()
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
}
.btn-outline:hover {
  border-color: rgba(0,0,0,0.2);
  background: rgba(0,0,0,0.03);
}

.btn-danger {
  padding: 10px 18px;
  background: #FF3B30;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-danger:hover {
  background: #E5342B;
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

:deep(.el-table__header-wrapper .cell) {
  white-space: nowrap;
  word-break: normal;
}

:deep(.el-table__body-wrapper .cell) {
  white-space: nowrap;
  word-break: normal;
  overflow: hidden;
  text-overflow: ellipsis;
}
:deep(.el-table__row:last-child td) {
  border-bottom: none;
}

.thumb-wrap {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(0,0,0,0.06);
}
.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.no-img {
  color: var(--apple-muted);
}

.status-badge {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}
.status-published {
  background: rgba(48, 209, 88, 0.1);
  color: #30D158;
}
.status-draft {
  background: rgba(0,0,0,0.05);
  color: var(--apple-text-secondary);
}

.ref-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #007AFF;
  font-weight: 500;
}

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
.btn-text:hover { background: rgba(0, 113, 227, 0.15); }
.btn-text.btn-success { background: rgba(48, 209, 88, 0.08); color: #30D158; }
.btn-text.btn-success:hover { background: rgba(48, 209, 88, 0.15); }
.btn-text.btn-warning { background: rgba(255, 149, 0, 0.08); color: #FF9500; }
.btn-text.btn-warning:hover { background: rgba(255, 149, 0, 0.15); }
.btn-text.btn-danger { background: rgba(255, 59, 48, 0.08); color: #FF3B30; }
.btn-text.btn-danger:hover { background: rgba(255, 59, 48, 0.15); }

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

/* Edit form */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row--grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.form-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text);
  letter-spacing: -0.01em;
}

.form-input {
  padding: 11px 14px;
  border: 1.5px solid transparent;
  border-radius: 10px;
  background: #F5F5F7;
  font-size: 14px;
  color: var(--apple-text);
  font-family: inherit;
  transition: all 0.2s;
  outline: none;
}
.form-input:focus {
  border-color: #007AFF;
  background: white;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.1);
}
.form-input::placeholder {
  color: var(--apple-muted);
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
}

.image-upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-wrap {
  position: relative;
  width: 80px;
  height: 80px;
}
.preview-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid rgba(0,0,0,0.06);
}
.remove-img {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  background: #FF3B30;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100px;
  height: 80px;
  background: #F5F5F7;
  border: 1.5px dashed #D1D1D6;
  border-radius: 10px;
  cursor: pointer;
  color: var(--apple-text-secondary);
  font-size: 12px;
  transition: all 0.2s;
}
.upload-trigger:hover {
  border-color: #007AFF;
  color: #007AFF;
  background: rgba(0,122,255,0.04);
}
.upload-tip {
  font-size: 10px;
  color: var(--apple-muted);
}
.upload-status {
  font-size: 12px;
  color: var(--apple-text-secondary);
}

.hidden {
  display: none;
}

/* Toggle */
.toggle-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-btn {
  width: 44px;
  height: 26px;
  border-radius: 13px;
  border: none;
  background: #D1D1D6;
  cursor: pointer;
  position: relative;
  transition: background 0.25s;
  padding: 0;
}
.toggle-btn.is-on {
  background: #30D158;
}
.toggle-knob {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.25s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
}
.toggle-btn.is-on .toggle-knob {
  transform: translateX(18px);
}

.ref-count-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
}
.ref-count-input {
  width: 50px;
  padding: 6px 8px;
  border: 1.5px solid transparent;
  border-radius: 8px;
  background: #F5F5F7;
  font-size: 14px;
  color: var(--apple-text);
  font-family: inherit;
  outline: none;
  text-align: center;
}
.ref-count-input:focus {
  border-color: #007AFF;
}
.ref-count-label {
  font-size: 13px;
  color: var(--apple-text-secondary);
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
