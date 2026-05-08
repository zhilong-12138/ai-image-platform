<template>
  <Transition name="fade">
    <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-box prompt-modal-box">
        <button class="modal-close" @click="$emit('close')">
          <svg viewBox="0 0 24 24">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>

        <!-- Header with thumb -->
        <div class="prompt-modal-header">
          <div :class="['prompt-thumb-wrap', { hidden: isCreateMode || !prompt?.imageUrls }]">
            <div class="prompt-thumb">
              <img :src="firstImg" alt=""/>
              <div class="prompt-thumb-overlay">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M15 3h6v6M14 10l7-7M9 21H3v-6M10 14L3 21"/>
                </svg>
              </div>
            </div>
          </div>
          <div class="prompt-meta">
            <div class="prompt-title">{{ isCreateMode ? '创建图片' : prompt?.title }}</div>
            <div class="prompt-author">
              {{ isCreateMode ? '自由创作，描述你的想象' : (prompt?.description || 'AI 提示词') }}
            </div>
            <div class="prompt-tags" v-if="!isCreateMode && prompt?.params">
              <span v-if="prompt.costPoints" class="prompt-tag cost-tag">
                消耗 {{ prompt.costPoints }} 积分
              </span>
            </div>
          </div>
        </div>

        <!-- Prompt textarea -->
        <div class="prompt-section-label"><span>提示词</span></div>
        <div class="prompt-textarea-wrap">
          <textarea
              ref="textareaRef"
              class="prompt-textarea"
              v-model="editablePrompt"
              rows="4"
              :placeholder="isCreateMode ? '描述你想要生成的图片效果...' : '输入你的提示词修改内容...'"
          ></textarea>
          <button class="copy-btn" :class="{ copied: copyDone }" @click="copyPrompt">
            {{ copyDone ? '已复制' : '复制' }}
          </button>
        </div>

        <!-- Reference images -->
        <div class="prompt-section-label"><span>{{ refImageLabel }}</span></div>
        <div class="ref-images">
          <div
              v-for="(img, idx) in refImages"
              :key="idx"
              class="ref-upload-slot"
          >
            <div class="ref-preview"><img :src="img" alt=""/></div>
            <button class="ref-remove" type="button" @click="removeRef(idx)">
              <svg viewBox="0 0 24 24">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <label v-if="refImages.length < maxRefImages" class="ref-upload-slot" :for="`ref-input-${uid}`">
            <svg viewBox="0 0 24 24">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            <input :id="`ref-input-${uid}`" type="file" accept="image/*" @change="handleRefUpload"/>
          </label>
        </div>

        <!-- Points warning -->
        <div v-if="userPoints > 0 && costPoints > userPoints" class="points-warning">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          您的积分余额 ({{ userPoints }}) 不足以完成此生成 ({{ costPoints }} 积分)
        </div>

        <!-- Credits -->
        <div class="credits-row">
          <span class="credits-label">消耗积分</span>
          <span class="credits-value">{{ costPoints }}</span>
        </div>

        <!-- Actions -->
        <div class="modal-btns">
          <button class="modal-btn cancel" @click="$emit('close')">取消</button>
          <button
              class="modal-btn primary"
              :disabled="submitting || !editablePrompt.trim() || costPoints > userPoints || !refImageCountMet"
              @click="handleSubmit"
          >
            {{ submitting ? '提交中…' : '开始制作' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import {ref, watch, nextTick, computed} from 'vue'
import {uploadApi} from '../api/services.js'

const props = defineProps({
  visible: Boolean,
  prompt: Object,
  isCreateMode: Boolean,
  userPoints: {type: Number, default: 0},
})
const emit = defineEmits(['close', 'create'])

const uid = Math.random().toString(36).slice(2, 8)
const editablePrompt = ref('')
const refImages = ref([])       // local data URLs for preview
const refImageUrls = ref([])    // OSS URLs after upload
const copyDone = ref(false)
const textareaRef = ref(null)
const submitting = ref(false)

const costPoints = computed(() => props.prompt?.costPoints || 10)

// 是否需要参考图
const needsRefImage = computed(() => (props.prompt?.refImageCount ?? 0) > 0)
// 已上传图片是否满足后端要求数量
const refImageCountMet = computed(() => {
  const required = props.prompt?.refImageCount ?? 0
  const met = required === 0 ? true : refImageUrls.value.length >= required
  console.log('[DEBUG] refImageCountMet:', met, 'required:', required, 'urls length:', refImageUrls.value.length)
  return met
})
// 最大允许上传张数，取 refImageCount 和 4 的较小值
// create-mode（prompt=null）时默认给 4 张上限
const maxRefImages = computed(() => Math.min(props.prompt?.refImageCount ?? (props.isCreateMode ? 4 : 0), 4))
// 参考图标签文案
const refImageLabel = computed(() => {
  const n = props.prompt?.refImageCount ?? 0
  if (n === 0) return '参考图'
  return `参考图（需上传 ${n} 张，最多 ${maxRefImages.value} 张）`
})

const firstImg = computed(() => {
  if (!props.prompt?.imageUrls) return ''
  try {
    const imgs = JSON.parse(props.prompt.imageUrls)
    return imgs[0] || ''
  } catch {
    return props.prompt.imageUrls
  }
})

watch(() => props.visible, async (val) => {
  if (val) {
    // Pre-fill prompt content from selected prompt
    editablePrompt.value = props.isCreateMode ? '' : (props.prompt?.content || props.prompt?.prompt || '')
    refImages.value = []
    refImageUrls.value = []
    copyDone.value = false
    if (props.isCreateMode) {
      await nextTick()
      textareaRef.value?.focus()
    }
  }
})

async function handleRefUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  console.log('[DEBUG] handleRefUpload, file:', file.name, 'max:', maxRefImages.value, 'current:', refImages.value.length)
  // 超过上限则禁止上传
  if (refImages.value.length >= maxRefImages.value) {
    console.log('[DEBUG] handleRefUpload: exceeds max, returning')
    e.target.value = ''
    return
  }

  // Show local preview immediately
  const reader = new FileReader()
  reader.onload = (ev) => {
    refImages.value.push(ev.target.result)
    // Upload to OSS in background
    uploadToOss(file)
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

async function uploadToOss(file) {
  console.log('[DEBUG] uploadToOss called, file:', file.name, 'refImages:', refImages.value.length, 'refImageUrls:', refImageUrls.value.length)
  try {
    const url = await uploadApi.image(file)
    console.log('[DEBUG] uploadToOss response:', url, 'type:', typeof url)
    // url may be the full response or the data field
    const finalUrl = typeof url === 'string' ? url : (url.data || url)
    console.log('[DEBUG] finalUrl:', finalUrl)
    refImageUrls.value.push(finalUrl)
    console.log('[DEBUG] refImageUrls after push:', refImageUrls.value.length, refImageUrls.value)
  } catch (err) {
    console.error('[DEBUG] uploadToOss error:', err)
    // Remove the preview if upload fails
    refImages.value.pop()
  }
}

function removeRef(idx) {
  refImages.value.splice(idx, 1)
  refImageUrls.value.splice(idx, 1)
}

async function copyPrompt() {
  try {
    await navigator.clipboard.writeText(editablePrompt.value)
    copyDone.value = true
    setTimeout(() => (copyDone.value = false), 1500)
  } catch {
  }
}

function handleSubmit() {
  if (!editablePrompt.value.trim()) return
  if (submitting.value) return
  submitting.value = true

  emit('create', {
    promptContent: editablePrompt.value,
    params: {},
    refImages: refImageUrls.value,  // use OSS URLs for submission
  })

  // Reset after a tick (parent will close modal)
  setTimeout(() => {
    submitting.value = false
  }, 100)
}
</script>

<style scoped>
/* ── Modal base ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.modal-box {
  background: var(--surface);
  border-radius: 16px;
  padding: 32px;
  max-width: 460px;
  width: 100%;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.2);
  position: relative;
}

.prompt-modal-box {
  max-width: 520px;
}

.modal-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--border);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}

.modal-close:hover {
  background: oklch(89% 0.015 80);
}

.modal-close svg {
  width: 14px;
  height: 14px;
  stroke: var(--fg);
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

/* ── Prompt modal ── */
.prompt-modal-header {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.prompt-thumb-wrap {
  flex-shrink: 0;
}

.prompt-thumb-wrap.hidden {
  display: none;
}

.prompt-thumb {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
}

.prompt-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.prompt-thumb-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s;
  color: #fff;
  font-size: 12px;
}

.prompt-thumb:hover .prompt-thumb-overlay {
  opacity: 1;
}

.prompt-meta {
  flex: 1;
  min-width: 0;
}

.prompt-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 400;
  color: var(--fg);
  margin-bottom: 4px;
  line-height: 1.3;
}

.prompt-author {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 10px;
}

.prompt-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.prompt-tag {
  padding: 3px 10px;
  border-radius: 100px;
  background: var(--border);
  font-size: 12px;
  color: var(--muted);
}

.cost-tag {
  background: oklch(58% 0.16 35 / 0.1);
  color: var(--accent);
}

.prompt-section-label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--muted);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.prompt-textarea-wrap {
  position: relative;
  margin-bottom: 24px;
}

.prompt-textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px 80px 12px 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-family: var(--font-ui);
  font-size: 14px;
  line-height: 1.6;
  color: var(--fg);
  background: var(--bg);
  resize: vertical;
  outline: none;
  transition: border-color 0.15s;
}

.prompt-textarea:focus {
  border-color: var(--accent);
}

.copy-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface);
  font-family: inherit;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.15s;
}

.copy-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.copy-btn.copied {
  border-color: oklch(45% 0.1 145);
  color: oklch(45% 0.1 145);
}

.ref-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.ref-upload-slot {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1.5px dashed var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  background: var(--bg);
}

.ref-upload-slot:hover {
  border-color: var(--accent);
  background: oklch(58% 0.16 35 / 0.04);
}

.ref-upload-slot svg {
  width: 20px;
  height: 20px;
  stroke: var(--muted);
  fill: none;
  stroke-width: 1.5;
}

.ref-upload-slot:hover svg {
  stroke: var(--accent);
}

.ref-upload-slot input[type="file"] {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  width: 100%;
  height: 100%;
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
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s;
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
  padding: 10px 14px;
  background: oklch(97% 0.01 80);
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 20px;
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

.modal-btns {
  display: flex;
  gap: 10px;
}

.modal-btn {
  flex: 1;
  padding: 13px;
  border-radius: 10px;
  border: none;
  font-family: inherit;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  letter-spacing: 0.01em;
}

.modal-btn.cancel {
  background: var(--border);
  color: var(--fg);
}

.modal-btn.cancel:hover {
  background: oklch(89% 0.015 80);
}

.modal-btn.primary {
  background: var(--fg);
  color: var(--surface);
}

.modal-btn.primary:hover:not(:disabled) {
  background: oklch(25% 0.02 60);
}

.modal-btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Mobile responsive ── */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 0;
    align-items: flex-end;
  }
  .prompt-modal-box {
    max-width: 100%;
    border-radius: 16px 16px 0 0;
    padding: 24px 20px 32px;
    max-height: 90vh;
    overflow-y: auto;
  }
  .prompt-modal-header {
    flex-direction: column;
    gap: 12px;
    margin-bottom: 20px;
  }
  .prompt-thumb {
    width: 100%;
    height: 180px;
  }
  .prompt-textarea {
    min-height: 100px;
  }
  .ref-upload-slot {
    width: 70px;
    height: 70px;
  }
  .modal-btns {
    flex-direction: column;
  }
  .modal-btn {
    width: 100%;
  }
}
</style>