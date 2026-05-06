<template>
  <Transition name="slide-up">
    <div v-if="visible" :class="['toast', { error: isError }]">{{ message }}</div>
  </Transition>
</template>

<script setup>
defineProps({ visible: Boolean, message: String, isError: Boolean })
</script>

<style scoped>
.toast { position: fixed; bottom: 32px; left: 50%; transform: translateX(-50%); background: oklch(25% 0.02 60); color: #fff; padding: 12px 24px; border-radius: 100px; font-size: 14px; z-index: 300; box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
.toast.error { background: oklch(55% 0.18 25); }

.slide-up-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.slide-up-leave-active { transition: all 0.2s ease; }
.slide-up-enter-from { opacity: 0; transform: translateX(-50%) translateY(20px); }
.slide-up-leave-to { opacity: 0; transform: translateX(-50%) translateY(60px); }
</style>