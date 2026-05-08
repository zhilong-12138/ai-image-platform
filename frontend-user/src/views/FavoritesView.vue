<template>
  <div>
    <header class="content-header">
      <div class="section-eyebrow">我的</div>
      <h1 class="section-title">我的收藏</h1>
      <p class="section-subtitle">收藏的灵感，一触即达。</p>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading-grid">
      <div v-for="i in 4" :key="i" class="loading-card"></div>
    </div>

    <!-- Grid -->
    <div v-else-if="favorites.length" class="masonry-grid">
      <div
        v-for="item in favorites"
        :key="item.id"
        class="masonry-item"
        @click="$emit('open', item)"
      >
        <img :src="(parseImgs(item.imageUrls))[0] || 'https://picsum.photos/400/300'" :alt="item.title" loading="lazy" />
        <div class="card-actions">
          <button class="card-action-btn favorited" title="取消收藏" @click.stop="$emit('unfavorite', item)">
            <svg viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" fill="currentColor"/></svg>
          </button>
          <button class="card-action-btn" title="复制提示词" @click.stop="copyPromptText(item)">
            <svg viewBox="0 0 24 24"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
          </button>
        </div>
        <div class="card-overlay">
          <div class="card-title">{{ item.title }}</div>
          <div class="card-meta">{{ item.description || 'AI 提示词' }}</div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>还没有收藏任何提示词</p>
      <p class="empty-hint">在提示词广场点击爱心收藏喜欢的模板</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { favApi } from '../api/services.js'

defineEmits(['open', 'unfavorite'])

const favorites = ref([])
const loading = ref(false)

function parseImgs(imageUrls) {
  if (!imageUrls) return []
  try {
    return JSON.parse(imageUrls)
  } catch {
    return imageUrls ? [imageUrls] : []
  }
}

async function copyPromptText(item) {
  try {
    await navigator.clipboard.writeText(item.content || '')
  } catch {}
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await favApi.list()
    favorites.value = data || []
  } catch {
    favorites.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 24px;
}
.loading-card {
  border-radius: 12px;
  height: 300px;
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
  .loading-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .loading-card {
    height: 200px;
  }
  .empty-state {
    padding: 40px 20px;
  }
}
</style>