<template>
  <div class="create-page-wrap">
    <div class="create-page">
    <header class="content-header">
      <div class="section-eyebrow">创作</div>
      <h1 class="section-title">创建图片</h1>
      <p class="section-subtitle">自由创作，描述你的想象。</p>
    </header>

    <!-- Prompt textarea -->
    <div class="create-section">
      <label class="create-label">提示词</label>
      <div class="create-textarea-wrap">
        <textarea
          ref="textareaRef"
          v-model="editablePrompt"
          class="create-textarea"
          rows="6"
          placeholder="描述你想要生成的图片效果..."
        ></textarea>
      </div>
    </div>

    <!-- Reference images -->
    <div class="create-section">
      <div class="create-label-row">
        <label class="create-label">参考图 <span class="create-label-hint">（可选，最多 4 张）</span></label>
      </div>
      <div class="ref-images">
        <div
          v-for="(img, idx) in refImages"
          :key="idx"
          class="ref-upload-slot has-image"
        >
          <div class="ref-preview"><img :src="img" alt="" /></div>
          <button class="ref-remove" type="button" @click="removeRef(idx)">
            <svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
          <div v-if="uploadingIdx === idx" class="ref-uploading">
            <div class="ref-uploading-spinner"></div>
          </div>
        </div>
        <label v-if="refImages.length < maxRefImages" class="ref-upload-slot" :for="`ref-input-${uid}`">
          <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
          <span>添加图片</span>
          <input :id="`ref-input-${uid}`" type="file" accept="image/*" @change="handleRefUpload" />
        </label>
      </div>
    </div>

    <!-- Points warning -->
    <div v-if="userPoints > 0 && costPoints > userPoints" class="points-warning">
      <svg viewBox="0 0 24 24" width="14" height="14"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      您的积分余额 ({{ userPoints }}) 不足以完成此生成 ({{ costPoints }} 积分)
    </div>

    <!-- Credits -->
    <div class="credits-row">
      <span class="credits-label">消耗积分</span>
      <span class="credits-value">{{ costPoints }}</span>
    </div>

    <!-- Actions -->
    <div class="create-actions">
      <button
        class="btn-create"
        :disabled="submitting || !editablePrompt.trim() || costPoints > userPoints"
        @click="handleSubmit"
      >
        {{ submitting ? '提交中…' : '开始制作' }}
      </button>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/authStore.js'
import { useAppStore } from '../stores/appStore.js'
import { generationApi, uploadApi } from '../api/services.js'

const authStore = useAuthStore()
const appStore = useAppStore()

const uid = Math.random().toString(36).slice(2, 8)
const editablePrompt = ref('')
const refImages = ref([])        // local data URLs for preview
const refImageUrls = ref([])     // OSS URLs after upload
const uploadingIdx = ref(-1)     // which slot is currently uploading
const submitting = ref(false)
const textareaRef = ref(null)

const costPoints = 10  // 固定消耗积分
const maxRefImages = 4

const userPoints = computed(() => authStore.user?.points || 0)

onMounted(() => {
  textareaRef.value?.focus()
})

async function handleRefUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (refImages.value.length >= maxRefImages) {
    e.target.value = ''
    return
  }

  // Local preview
  const reader = new FileReader()
  reader.onload = (ev) => {
    const slotIdx = refImages.value.length
    refImages.value.push(ev.target.result)
    uploadingIdx.value = slotIdx
    uploadToOss(file, slotIdx)
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

async function uploadToOss(file, slotIdx) {
  try {
    const res = await uploadApi.image(file)
    const finalUrl = typeof res === 'string' ? res : (res.data || res)
    refImageUrls.value.push(finalUrl)
    console.log('[CreateImage] OSS upload success, refImageUrls:', refImageUrls.value.length)
  } catch (err) {
    console.error('[CreateImage] OSS upload failed:', err)
    // Rollback preview
    const localIdx = refImages.value.findIndex((_, i) => i === slotIdx)
    if (localIdx !== -1) refImages.value.splice(localIdx, 1)
    appStore.showToast('图片上传失败', true)
  } finally {
    uploadingIdx.value = -1
  }
}

function removeRef(idx) {
  refImages.value.splice(idx, 1)
  refImageUrls.value.splice(idx, 1)
}

async function handleSubmit() {
  if (!editablePrompt.value.trim()) return
  if (submitting.value) return
  submitting.value = true

  try {
    const data = {
      promptId: null,
      params: JSON.stringify({ promptContent: editablePrompt.value }),
      refImages: JSON.stringify(refImageUrls.value),
    }
    await generationApi.submit(data)
    appStore.showToast('开始制作，请在我的作品页面查看进度')
    // Reset form
    editablePrompt.value = ''
    refImages.value = []
    refImageUrls.value = []
  } catch (e) {
    appStore.showToast(e.message || '提交失败', true)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-page-wrap {
  position: fixed;
  /*left: 240px;  sidebar width */
  /*top: 0;*/
  width: calc(50vw - 440px);  /* 50% of viewport */
  height: 100vh;
  overflow-y: auto;
  box-sizing: border-box;
  padding: 48px 48px 64px;
  background: var(--bg);
}

.create-page {
  max-width: 100%;
  width: 100%;
  padding: 0;
}

.content-header {
  margin-bottom: 32px;
}

.section-eyebrow {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 6px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 400;
  color: var(--fg);
  margin-bottom: 8px;
}

.section-subtitle {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

.create-section {
  margin-bottom: 28px;
  padding: 0;
}

.create-label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--muted);
  display: block;
  margin-bottom: 10px;
}

.create-label-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.create-label-hint {
  font-size: 12px;
  font-weight: 400;
  letter-spacing: 0;
  text-transform: none;
  color: var(--muted);
}

.create-textarea-wrap {
  position: relative;
}

.create-textarea {
  width: 100%;
  min-height: 160px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 12px;
  font-family: var(--font-ui);
  font-size: 15px;
  line-height: 1.7;
  color: var(--fg);
  background: var(--bg);
  resize: vertical;
  outline: none;
  transition: border-color 0.15s;
  box-sizing: border-box;
}

.create-textarea:focus {
  border-color: var(--accent);
}

.ref-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ref-upload-slot {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  border: 1.5px dashed var(--border);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  background: var(--bg);
  gap: 4px;
}

.ref-upload-slot:hover {
  border-color: var(--accent);
  background: oklch(58% 0.16 35 / 0.04);
}

.ref-upload-slot svg {
  width: 22px;
  height: 22px;
  stroke: var(--muted);
  fill: none;
  stroke-width: 1.5;
}

.ref-upload-slot:hover svg {
  stroke: var(--accent);
}

.ref-upload-slot span {
  font-size: 11px;
  color: var(--muted);
}

.ref-upload-slot input[type="file"] {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  width: 100%;
  height: 100%;
}

.ref-upload-slot.has-image {
  border-style: solid;
  border-color: var(--border);
}

.ref-preview {
  position: absolute;
  inset: 0;
}

.ref-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.ref-remove {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0,0,0,0.55);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s;
  z-index: 2;
}

.ref-upload-slot:hover .ref-remove {
  opacity: 1;
}

.ref-remove svg {
  width: 10px;
  height: 10px;
  stroke: #fff;
  fill: none;
  stroke-width: 2.5;
  stroke-linecap: round;
}

.ref-uploading {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.ref-uploading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.points-warning {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #fff5f5;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #e53e3e;
}

.points-warning svg {
  flex-shrink: 0;
}

.credits-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: oklch(97% 0.01 80);
  border: 1px solid var(--border);
  border-radius: 10px;
  margin-bottom: 24px;
  font-size: 14px;
}

.credits-label {
  color: var(--muted);
}

.credits-value {
  font-weight: 600;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.create-actions {
  padding: 0;
  display: flex;
  gap: 10px;
}

.btn-create {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  border: none;
  background: var(--fg);
  color: var(--surface);
  font-family: inherit;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  letter-spacing: 0.01em;
}

.btn-create:hover:not(:disabled) {
  background: oklch(25% 0.02 60);
}

.btn-create:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
