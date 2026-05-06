<template>
  <div>
    <header class="content-header">
      <div class="section-eyebrow">探索</div>
      <h1 class="section-title">提示词广场</h1>
      <p class="section-subtitle">浏览社区创作者，汲取灵感，用 AI 将想象化为现实。</p>
    </header>

    <!-- Category filters -->
    <div class="filter-bar">
      <button
        v-for="cat in categories"
        :key="cat.id"
        :class="['filter-chip', { active: activeCategoryId === (cat.id || '') }]"
        @click="selectCategory(cat.id)"
      >{{ cat.name }}</button>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-grid">
      <div v-for="i in 8" :key="i" class="loading-card"></div>
    </div>

    <!-- Prompt grid -->
    <div v-else class="masonry-grid">
      <div
        v-for="item in prompts"
        :key="item.id"
        class="masonry-item"
        @click="$emit('open', item)"
      >
        <img :src="(parseImgs(item.imageUrls))[0] || 'https://picsum.photos/400/300'" :alt="item.title" loading="lazy" />
        <div class="card-actions">
          <button class="card-action-btn" title="收藏" @click.stop="handleFavorite(item, $event)">
            <svg viewBox="0 0 24 24">
              <path v-if="item.isFavorited" d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" fill="currentColor"/>
              <path v-else d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/>
            </svg>
          </button>
          <button class="card-action-btn" title="复制提示词" @click.stop="copyPromptText(item, $event)">
            <svg viewBox="0 0 24 24"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
          </button>
        </div>
        <div class="card-overlay">
          <div class="card-title">{{ item.title }}</div>
          <div class="card-meta">{{ item.description || 'AI 提示词' }}</div>
        </div>
      </div>
    </div>

    <!-- Load more -->
    <div v-if="!loading && hasMore" class="load-more">
      <button class="load-more-btn" @click="loadMore">加载更多</button>
    </div>
    <div v-if="!loading && prompts.length === 0" class="empty-state">
      <p>暂无提示词</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { promptApi, categoryApi } from '../api/services.js'

const emit = defineEmits(['open', 'favorite'])

const categories = ref([])
const activeCategoryId = ref('')
const prompts = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(false)

async function loadCategories() {
  try {
    const data = await categoryApi.list()
    categories.value = [{ id: '', name: '全部' }, ...(data || [])]
  } catch {
    categories.value = [{ id: '', name: '全部' }]
  }
}

async function loadPrompts(reset = false) {
  if (reset) {
    page.value = 1
    prompts.value = []
  }
  loading.value = true
  try {
    const params = { page: page.value, pageSize: 20 }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const data = await promptApi.list(params)
    const records = data.records || data || []
    if (reset) {
      prompts.value = records
    } else {
      prompts.value.push(...records)
    }
    hasMore.value = prompts.value.length < (data.total || records.length)
  } catch {
    prompts.value = []
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  activeCategoryId.value = id
  loadPrompts(true)
}

function loadMore() {
  page.value++
  loadPrompts(false)
}

function parseImgs(imageUrls) {
  if (!imageUrls) return []
  try {
    return JSON.parse(imageUrls)
  } catch {
    return imageUrls ? [imageUrls] : []
  }
}

async function handleFavorite(item, event) {
  event.stopPropagation()
  emit('favorite', item)
}

async function copyPromptText(item, event) {
  event.stopPropagation()
  try {
    await navigator.clipboard.writeText(item.content || item.prompt || '')
    // Could show toast here
  } catch {}
}

onMounted(() => {
  loadCategories()
  loadPrompts(true)
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
</style>