import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const toastMsg = ref('')
  const toastError = ref(false)
  const toastVisible = ref(false)
  let toastTimer = null

  function showToast(msg, isError = false) {
    toastMsg.value = msg
    toastError.value = isError
    toastVisible.value = true
    clearTimeout(toastTimer)
    toastTimer = setTimeout(() => { toastVisible.value = false }, 3000)
  }

  function clearToast() {
    toastVisible.value = false
    clearTimeout(toastTimer)
  }

  return { toastMsg, toastError, toastVisible, showToast, clearToast }
})