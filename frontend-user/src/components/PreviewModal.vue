<template>
  <Transition name="fade">
    <div v-if="visible" class="preview-overlay" @click.self="$emit('close')">
      <button class="preview-close" @click="$emit('close')">
        <svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
      <img class="preview-img" :src="src" :alt="name" />
      <div class="preview-name">{{ name }}</div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({ visible: Boolean, src: String, name: String })
defineEmits(['close'])
</script>

<style scoped>
.preview-overlay { position: fixed; inset: 0; z-index: 200; background: rgba(0,0,0,0.85); display: flex; align-items: center; justify-content: center; padding: 24px; }
.preview-img { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 8px; box-shadow: 0 32px 80px rgba(0,0,0,0.5); }
.preview-close { position: fixed; top: 20px; right: 20px; width: 40px; height: 40px; border-radius: 50%; background: rgba(255,255,255,0.15); border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background 0.15s; }
.preview-close:hover { background: rgba(255,255,255,0.25); }
.preview-close svg { width: 20px; height: 20px; stroke: #fff; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
.preview-name { position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%); color: rgba(255,255,255,0.8); font-size: 14px; background: rgba(0,0,0,0.5); padding: 8px 18px; border-radius: 100px; backdrop-filter: blur(8px); white-space: nowrap; }

@media (max-width: 768px) {
  .preview-overlay {
    padding: 16px;
  }
  .preview-img {
    max-width: 100%;
    max-height: 85vh;
  }
  .preview-name {
    bottom: 80px;
    font-size: 12px;
    padding: 6px 14px;
  }
}
</style>