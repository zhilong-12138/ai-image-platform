<template>
  <Transition name="fade">
    <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-box" style="max-width: 360px">
        <div class="modal-title">确认删除</div>
        <div class="modal-body">确定要删除「{{ name }}」吗？此操作无法撤销。</div>
        <div class="delete-btns">
          <button class="modal-btn-small cancel" @click="$emit('close')">取消</button>
          <button class="modal-btn-small confirm" @click="$emit('confirm')">删除</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({ visible: Boolean, name: String })
defineEmits(['close', 'confirm'])
</script>

<style scoped>
.modal-overlay { position: fixed; inset: 0; z-index: 100; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; padding: 24px; }
.modal-box { background: var(--surface); border-radius: 16px; padding: 32px; max-width: 460px; width: 100%; box-shadow: 0 24px 80px rgba(0,0,0,0.2); }
.modal-title { font-size: 17px; font-weight: 600; margin-bottom: 8px; }
.modal-body { font-size: 14px; color: var(--muted); line-height: 1.5; margin-bottom: 24px; }
.delete-btns { display: flex; gap: 10px; justify-content: flex-end; }
.modal-btn-small { padding: 9px 20px; border-radius: 8px; border: none; font-family: inherit; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.15s; }
.modal-btn-small.cancel { background: var(--border); color: var(--fg); }
.modal-btn-small.cancel:hover { background: oklch(89% 0.015 80); }
.modal-btn-small.confirm { background: oklch(55% 0.18 25); color: #fff; }
.modal-btn-small.confirm:hover { background: oklch(50% 0.18 25); }
</style>