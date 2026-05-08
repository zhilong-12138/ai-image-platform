<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed, 'mobile-nav-open': mobileNavOpen }">
    <!-- Mobile Header -->
    <header class="mobile-header">
      <button class="mobile-menu-btn" @click="mobileNavOpen = !mobileNavOpen">
        <svg viewBox="0 0 24 24" v-if="!mobileNavOpen"><path d="M3 12h18M3 6h18M3 18h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        <svg viewBox="0 0 24 24" v-else><path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
      </button>
      <div class="mobile-logo">灵<span>画</span></div>
      <button class="mobile-create-btn" @click="handleCreate">
        <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
      </button>
    </header>

    <!-- Mobile Bottom Nav -->
    <nav class="mobile-bottom-nav">
      <button class="mobile-nav-item" :class="{ active: activeTab === 'feed' }" @click="handleNavigate('feed'); mobileNavOpen = false">
        <svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
        <span>广场</span>
      </button>
      <button class="mobile-nav-item" :class="{ active: activeTab === 'works' }" @click="handleNavigate('works'); mobileNavOpen = false">
        <svg viewBox="0 0 24 24"><path d="M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2z"/><path d="M8.5 10a1.5 1.5 0 100-3 1.5 1.5 0 000 3zM21 15l-5-5L5 21"/></svg>
        <span>作品</span>
      </button>
      <button class="mobile-nav-item" :class="{ active: activeTab === 'favorites' }" @click="handleNavigate('favorites'); mobileNavOpen = false">
        <svg viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/></svg>
        <span>收藏</span>
      </button>
      <button class="mobile-nav-item" :class="{ active: activeTab === 'profile' }" @click="handleNavigate('profile'); mobileNavOpen = false">
        <svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>
        <span>我的</span>
      </button>
    </nav>

    <!-- Mobile Overlay -->
    <Transition name="fade">
      <div v-if="mobileNavOpen" class="mobile-nav-overlay" @click="mobileNavOpen = false"></div>
    </Transition>

    <!-- Sidebar -->
    <TheSidebar :active-tab="activeTab" :is-logged-in="authStore.isLoggedIn" :collapsed="sidebarCollapsed"
                @navigate="handleNavigate" @create="handleCreate" @logout="handleLogout" @login="loginVisible = true"
                @toggle-collapse="sidebarCollapsed = !sidebarCollapsed"/>

    <main class="main">
      <FeedView v-if="activeTab === 'feed'" @open="openPrompt" @favorite="handleFavorite"/>
      <WorksView v-if="activeTab === 'works'" :works="works" :loading="worksLoading" @delete="showDelete"
                 @preview="showPreview" @retry="retryWork" @load-more="loadMoreWorks"/>
      <FavoritesView v-if="activeTab === 'favorites'" @open="openPrompt" @unfavorite="handleUnfavorite"/>
      <ProfileView v-if="activeTab === 'profile'" @refresh="refreshProfile"/>
      <CreateImageView v-if="activeTab === 'create'"/>
    </main>

    <!-- Auth Modal -->
    <Transition name="fade">
      <div v-if="authView" class="auth-overlay">
        <LoginView
            v-if="authView === 'login'"
            @success="onAuthSuccess"
            @register="authView = 'register'"
            @reset="authView = 'reset'"
        />
        <RegisterView
            v-if="authView === 'register'"
            @success="onRegisterSuccess"
            @back="authView = 'login'"
        />
        <ResetPasswordView
            v-if="authView === 'reset'"
            @success="onResetSuccess"
            @back="authView = 'login'"
        />
      </div>
    </Transition>

    <!-- Modals -->
    <PromptModal
        :visible="promptModalVisible"
        :prompt="selectedPrompt"
        :is-create-mode="isCreateMode"
        :user-points="authStore.user?.points || 0"
        @close="closePrompt"
        @create="handleCreateSubmit"
    />

    <DeleteModal
        :visible="deleteModalVisible"
        :name="deleteTarget?.name"
        @close="closeDelete"
        @confirm="confirmDelete"
    />

    <PreviewModal
        :visible="previewVisible"
        :src="previewSrc"
        :name="previewName"
        @close="previewVisible = false"
    />

    <!-- Toast -->
    <Toast :visible="appStore.toastVisible" :message="appStore.toastMsg" :is-error="appStore.toastError"/>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, onUnmounted} from 'vue'
import TheSidebar from './components/TheSidebar.vue'
import FeedView from './views/FeedView.vue'
import WorksView from './views/WorksView.vue'
import FavoritesView from './views/FavoritesView.vue'
import ProfileView from './views/ProfileView.vue'
import CreateImageView from './views/CreateImageView.vue'
import PromptModal from './components/PromptModal.vue'
import DeleteModal from './components/DeleteModal.vue'
import PreviewModal from './components/PreviewModal.vue'
import Toast from './components/Toast.vue'
import LoginView from './components/LoginView.vue'
import RegisterView from './components/RegisterView.vue'
import ResetPasswordView from './components/ResetPasswordView.vue'
import {useAuthStore} from './stores/authStore.js'
import {useAppStore} from './stores/appStore.js'
import {promptApi, generationApi, favApi} from './api/services.js'

const authStore = useAuthStore()
const appStore = useAppStore()

// ── Sidebar State ──
const sidebarCollapsed = ref(false)
const mobileNavOpen = ref(false)

// ── Navigation ──
const activeTab = ref('feed')

function handleNavigate(tab) {
  const authRequired = ['works', 'favorites', 'profile']
  if (authRequired.includes(tab) && !authStore.isLoggedIn) {
    authView.value = 'login'
    return
  }
  activeTab.value = tab
  mobileNavOpen.value = false
  if (tab === 'works') loadWorks()
}

// ── Auth Modal ──
// 记录需要登录后继续执行的操作
const pendingAction = ref(null)
const authView = ref(null)  // 'login' | 'register' | 'reset'

// 供外部组件调用的登录入口
function showLogin() {
  authView.value = 'login'
}

function requireAuth(actionFn, args) {
  if (authStore.isLoggedIn) {
    actionFn(...args)
  } else {
    pendingAction.value = {fn: actionFn, args}
    authView.value = 'login'
  }
}

function onAuthSuccess() {
  authView.value = null
  if (pendingAction.value) {
    const {fn, args} = pendingAction.value
    pendingAction.value = null
    fn(...args)
  } else {
    if (activeTab.value === 'works') loadWorks()
    if (activeTab.value === 'profile') refreshProfile()
  }
}

function onRegisterSuccess() {
  authView.value = 'login'
}

function onResetSuccess() {
  authView.value = 'login'
}

function handleLogout() {
  authStore.logout()
  activeTab.value = 'feed'
}

// ── Prompt Modal ──
const promptModalVisible = ref(false)
const selectedPrompt = ref(null)
const isCreateMode = ref(false)

// PromptModal is only used for opening existing prompts
// (clicking a prompt card → open in modal)
// Create mode lives in CreateImageView as a standalone page

function openPrompt(prompt) {
  isCreateMode.value = false
  selectedPrompt.value = prompt
  promptModalVisible.value = true
}

function handleCreate() {
  if (!authStore.isLoggedIn) {
    pendingAction.value = {fn: navigateToCreate, args: []}
    authView.value = 'login'
    return
  }
  navigateToCreate()
}

function navigateToCreate() {
  activeTab.value = 'create'
}

function closePrompt() {
  promptModalVisible.value = false
}

async function handleCreateSubmit({promptContent, params, refImages}) {
  if (!promptContent?.trim()) {
    appStore.showToast('请填写提示词', true)
    return
  }
  try {
    const data = {
      promptId: selectedPrompt.value?.id,
      params: JSON.stringify({promptContent, ...params}),
      refImages: JSON.stringify(refImages),
    }
    await generationApi.submit(data)
    closePrompt()
    activeTab.value = 'works'
    loadWorks()
    appStore.showToast('开始制作，请在我的作品页面查看进度')
  } catch (e) {
    appStore.showToast(e.message || '提交失败', true)
  }
}

// ── Favorites ──
function handleFavorite(prompt) {
  if (!authStore.isLoggedIn) {
    pendingAction.value = {fn: toggleFavoriteInternal, args: [prompt]}
    authView.value = 'login'
    return
  }
  toggleFavoriteInternal(prompt)
}

async function toggleFavoriteInternal(prompt) {
  try {
    if (prompt.isFavorited) {
      await favApi.remove(prompt.id)
    } else {
      await favApi.add(prompt.id)
    }
    prompt.isFavorited = !prompt.isFavorited
  } catch (e) {
    appStore.showToast(e.message || '操作失败', true)
  }
}

async function handleUnfavorite(prompt) {
  if (!authStore.isLoggedIn) {
    pendingAction.value = {fn: unfavoriteInternal, args: [prompt]}
    authView.value = 'login'
    return
  }
  unfavoriteInternal(prompt)
}

async function unfavoriteInternal(prompt) {
  try {
    await favApi.remove(prompt.id)
    // reload favorites view
    const favEl = document.querySelector('.masonry-grid')
    if (favEl) window.location.reload()
  } catch (e) {
    appStore.showToast(e.message || '取消收藏失败', true)
  }
}

// ── Works ──
const works = reactive([])
const worksLoading = ref(false)
const worksPage = ref(1)
const worksTotal = ref(0)

async function loadWorks() {
  worksLoading.value = true
  try {
    const data = await generationApi.list({page: 1, pageSize: 20})
    works.length = 0
    works.push(...(data.records || []))
    worksTotal.value = data.total || 0
    worksPage.value = 1
  } catch (e) {
    if (e.message?.includes('401')) {
      activeTab.value = 'feed'
      return
    }
    appStore.showToast(e.message || '加载作品失败', true)
  } finally {
    worksLoading.value = false
  }
}

async function loadMoreWorks() {
  if (works.length >= worksTotal.value) return
  worksPage.value++
  try {
    const data = await generationApi.list({page: worksPage.value, pageSize: 20})
    works.push(...(data.records || []))
  } catch (e) {
    appStore.showToast(e.message || '加载更多失败', true)
  }
}

async function retryWork(work) {
  try {
    await generationApi.retry(work.id)
    appStore.showToast('重新生成中…')
    const updated = await generationApi.status(work.id)
    const idx = works.findIndex(w => w.id === work.id)
    if (idx !== -1) Object.assign(works[idx], updated)
  } catch (e) {
    appStore.showToast(e.message || '重试失败', true)
  }
}

// ── Profile refresh ──
async function refreshProfile() {
  await authStore.fetchUserInfo()
}

// ── Delete Modal ──
const deleteModalVisible = ref(false)
const deleteTarget = ref(null)

function showDelete(work) {
  deleteTarget.value = work
  deleteModalVisible.value = true
}

function closeDelete() {
  deleteModalVisible.value = false
  deleteTarget.value = null
}

function confirmDelete() {
  if (!deleteTarget.value) return
  const idx = works.findIndex(w => w.id === deleteTarget.value.id)
  if (idx !== -1) works.splice(idx, 1)
  closeDelete()
}

// ── Preview Modal ──
const previewVisible = ref(false)
const previewSrc = ref('')
const previewName = ref('')

function showPreview(work) {
  const urls = work.resultImages ? JSON.parse(work.resultImages) : []
  previewSrc.value = urls[0] || work.img || ''
  previewName.value = work.name || ''
  previewVisible.value = true
}

// ── Keyboard ──
function onKeydown(e) {
  if (e.key === 'Escape') {
    closeDelete()
    previewVisible.value = false
    closePrompt()
    authView.value = null
  }
}

// ── Auth logout listener ──
function onAuthLogout() {
  authStore.logout()
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
  window.addEventListener('auth:logout', onAuthLogout)
  window.addEventListener('preview:image', (e) => {
    previewSrc.value = e.detail.src
    previewName.value = e.detail.name || ''
    previewVisible.value = true
  })
})
onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  window.removeEventListener('auth:logout', onAuthLogout)
})

defineExpose({showLogin})
</script>