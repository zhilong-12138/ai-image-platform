<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">图片审核</h1>
        <p class="page-subtitle">审核用户生成的图片内容</p>
      </div>
      <div class="header-right">
        <span class="pending-badge" v-if="pendingCount > 0">
          <span class="pending-dot"></span>
          {{ pendingCount }} 待审核
        </span>
      </div>
    </div>

    <div class="images-grid" v-loading="loading">
      <div v-for="item in imageList" :key="item.id" class="image-card">
        <div class="image-preview">
          <img v-if="getFirstImage(item.resultImages)" :src="getFirstImage(item.resultImages)" alt="生成图片" @error="handleImgError($event)" />
          <div v-else class="img-placeholder">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="18" height="18" rx="4" stroke="currentColor" stroke-width="1.5"/>
              <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
              <path d="M21 15l-5-5L5 21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="image-overlay">
            <span class="image-time">{{ formatTime(item.createdAt) }}</span>
            <span class="image-user">UID: {{ item.userId }}</span>
          </div>
        </div>
        <div class="image-footer">
          <span class="status-tag" :class="statusClass(item.status)">
            {{ statusText(item.status) }}
          </span>
          <div class="card-actions">
            <button class="btn-approve" @click="handleApprove(item.id)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              批准
            </button>
            <button class="btn-reject" @click="openReject(item.id)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
              </svg>
              拒绝
            </button>
          </div>
        </div>
      </div>

      <div v-if="!loading && imageList.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/>
            <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3 class="empty-title">暂无待审核图片</h3>
        <p class="empty-desc">所有图片均已处理完毕</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { imageApi } from '../api'

const loading = ref(false)
const imageList = ref([])

const pendingCount = computed(() => imageList.value.length)

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 16)
}

const statusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-generating', 2: 'status-done', 3: 'status-rejected' }
  return map[status] || 'status-pending'
}

const statusText = (status) => {
  const map = { 0: '待处理', 1: '生成中', 2: '已完成', 3: '已拒绝' }
  return map[status] || '未知'
}

const getFirstImage = (resultImages) => {
  if (!resultImages) return null
  try {
    const arr = JSON.parse(resultImages)
    return Array.isArray(arr) ? arr[0] : arr
  } catch {
    return resultImages
  }
}

const handleImgError = (e) => {
  e.target.style.display = 'none'
  e.target.nextElementSibling?.classList.add('visible')
}

const fetchImages = async () => {
  loading.value = true
  try {
    const res = await imageApi.list()
    if (res.code === 200) {
      imageList.value = res.data.records || []
    }
  } catch (e) {
    ElMessage.error('获取图片列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (id) => {
  try {
    const res = await imageApi.approve(id)
    if (res.code === 200) {
      ElMessage.success('已批准')
      imageList.value = imageList.value.filter(item => item.id !== id)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const openReject = (id) => {
  ElMessageBox.prompt('请输入拒绝原因', '拒绝图片', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入拒绝原因（可选）'
  }).then(({ value }) => {
    handleReject(id, value)
  }).catch(() => {})
}

const handleReject = async (id, reason) => {
  try {
    const res = await imageApi.reject(id, reason || '')
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      imageList.value = imageList.value.filter(item => item.id !== id)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchImages()
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

.header-right {
  display: flex;
  align-items: center;
}

.pending-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 149, 0, 0.1);
  color: #FF9500;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
}

.pending-dot {
  width: 7px;
  height: 7px;
  background: #FF9500;
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Images Grid */
.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.image-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: all 0.3s;
}

.image-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--apple-shadow-hover);
}

.image-preview {
  position: relative;
  width: 100%;
  height: 220px;
  background: #F5F5F7;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-card:hover .image-preview img {
  transform: scale(1.03);
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: none;
  align-items: center;
  justify-content: center;
  color: #D1D1D6;
}
.img-placeholder.visible,
.image-preview:not(:has(img[style*="display: none"])) ~ .img-placeholder {
  /* handled via JS */
}
.image-preview:has(img[style*="display: none"]) .img-placeholder {
  display: flex;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 14px;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.image-time, .image-user {
  font-size: 11px;
  color: rgba(255,255,255,0.85);
  font-weight: 500;
}

.image-footer {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.status-pending { background: rgba(255, 149, 0, 0.1); color: #FF9500; }
.status-generating { background: rgba(0, 113, 227, 0.1); color: #007AFF; }
.status-done { background: rgba(48, 209, 88, 0.1); color: #30D158; }
.status-rejected { background: rgba(255, 59, 48, 0.1); color: #FF3B30; }

.card-actions {
  display: flex;
  gap: 8px;
}

.btn-approve {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  background: rgba(48, 209, 88, 0.1);
  color: #30D158;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-approve:hover {
  background: rgba(48, 209, 88, 0.18);
}

.btn-reject {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  background: rgba(255, 59, 48, 0.08);
  color: #FF3B30;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-reject:hover {
  background: rgba(255, 59, 48, 0.15);
}

/* Empty state */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: rgba(48, 209, 88, 0.08);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #30D158;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--apple-text);
  margin: 0 0 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--apple-text-secondary);
  margin: 0;
}
</style>