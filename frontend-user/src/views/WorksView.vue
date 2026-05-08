<template>
  <div>
    <header class="content-header">
      <div class="section-eyebrow">我的</div>
      <h1 class="section-title">我的作品</h1>
      <p class="section-subtitle">你生成的所有图片都在这里，随时下载或整理。</p>
    </header>

    <!-- Loading state -->
    <div v-if="loading" class="works-loading">
      <div v-for="i in 5" :key="i" class="works-skeleton"></div>
    </div>

    <table v-else-if="works.length" class="works-table">
      <thead>
        <tr>
          <th style="width:72px"></th>
          <th>作品名称</th>
          <th style="width:100px">状态</th>
          <th style="width:140px">创建时间</th>
          <th style="width:80px"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="work in works" :key="work.id">
          <td>
            <div class="works-thumb" @click="showPreviewImg(work)">
              <img :src="getFirstImg(work.resultImages) || 'https://picsum.photos/200/200'" :alt="work.name || '作品'" />
            </div>
          </td>
          <td>
            <div class="works-name">{{ getWorkName(work) }}</div>
            <div class="works-date">{{ formatDate(work.createdAt) }}</div>
          </td>
          <td>
            <span :class="['status-badge', statusClass(work.status)]">
              <span v-if="work.status === 0" class="badge-dot"></span>
              {{ statusLabel(work.status) }}
            </span>
          </td>
          <td>{{ formatDate(work.createdAt) }}</td>
          <td>
            <div class="action-btns">
              <!-- Retry -->
              <button
                v-if="work.status === 3"
                class="action-btn"
                title="重试"
                @click="$emit('retry', work)"
              >
                <svg viewBox="0 0 24 24"><path d="M1 4v6h6"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
              </button>
              <!-- Download -->
              <a
                v-if="work.status === 2"
                :href="getFirstImg(work.resultImages)"
                download="ai-image.png"
                class="action-btn"
                title="下载"
                @click.stop
              >
                <svg viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"/></svg>
              </a>
              <!-- Delete -->
              <button class="action-btn danger" title="删除" @click="$emit('delete', work)">
                <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a1 1 0 011-1h4a1 1 0 011 1v2"/></svg>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else class="empty-state">
      <p>还没有生成任何作品</p>
      <p class="empty-hint">去提示词广场选择模板开始创作吧</p>
    </div>

    <div v-if="works.length > 0 && hasMore" class="load-more">
      <button class="load-more-btn" @click="$emit('load-more')">加载更多</button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  works: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  hasMore: { type: Boolean, default: false },
})
defineEmits(['delete', 'preview', 'retry', 'load-more'])

function getFirstImg(resultImages) {
  if (!resultImages) return ''
  try {
    const imgs = JSON.parse(resultImages)
    return imgs[0] || ''
  } catch {
    return ''
  }
}

function getWorkName(work) {
  return work.name || `生成 #${work.id}` || '未命名作品'
}

function formatDate(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  if (isNaN(d.getTime())) return dt
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function statusClass(s) {
  if (s === 0 || s === 1) return 'status-pending'
  if (s === 3) return 'status-failed'
  return 'status-done'
}

function statusLabel(s) {
  if (s === 0) return '等待中'
  if (s === 1) return '生成中'
  if (s === 2) return '已完成'
  if (s === 3) return '生成失败'
  return '未知'
}

function showPreviewImg(work) {
  const src = getFirstImg(work.resultImages)
  if (src) {
    window.dispatchEvent(new CustomEvent('preview:image', { detail: { src, name: getWorkName(work) } }))
  }
}
</script>

<style scoped>
.works-loading {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}
.works-skeleton {
  height: 64px;
  border-radius: 8px;
  background: var(--border);
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--muted);
}
.empty-hint {
  font-size: 13px;
  margin-top: 8px;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .works-loading {
    gap: 10px;
    margin-top: 16px;
  }
  .works-skeleton {
    height: 72px;
  }
  .empty-state {
    padding: 40px 20px;
  }
}
</style>