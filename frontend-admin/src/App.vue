<template>
  <div id="app">
    <el-container>
      <el-aside width="240px" class="sidebar">
        <div class="logo">
          <div class="logo-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="8" fill="url(#adminGrad)"/>
              <path d="M8 14C8 10.69 10.69 8 14 8C17.31 8 20 10.69 20 14" stroke="white" stroke-width="2" stroke-linecap="round"/>
              <circle cx="14" cy="14" r="2.5" fill="white"/>
              <defs>
                <linearGradient id="adminGrad" x1="0" y1="0" x2="28" y2="28">
                  <stop offset="0%" stop-color="#0071e3"/>
                  <stop offset="100%" stop-color="#5856d6"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <span class="logo-text">ImageStudio</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/prompts">
            <el-icon><Document /></el-icon>
            <span>提示词管理</span>
          </el-menu-item>
          <el-menu-item index="/categories">
            <el-icon><CollectionTag /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/images">
            <el-icon><Picture /></el-icon>
            <span>图片审核</span>
          </el-menu-item>
          <el-menu-item index="/point-log">
            <el-icon><Coin /></el-icon>
            <span>积分日志</span>
          </el-menu-item>
          <el-menu-item index="/config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container class="main-wrapper">
        <el-header class="header">
          <div class="header-breadcrumb">
            <span class="header-title">管理后台</span>
          </div>
          <div class="header-right">
            <el-dropdown>
              <span class="admin-badge">
                <el-icon><Avatar /></el-icon>
                <span class="admin-name">管理员</span>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis, User, Document, Picture, Setting, ArrowDown, Coin, Avatar, CollectionTag } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeMenu = ref('/dashboard')

const handleMenuSelect = (index) => {
  activeMenu.value = index
  router.push(index)
}

const logout = () => {
  localStorage.removeItem('token')
  ElMessage.success('已退出登录')
  router.push('/login')
}

watch(
  () => router.currentRoute.value.path,
  (path) => {
    activeMenu.value = path
  },
  { immediate: true }
)
</script>

<style scoped>
:root {
  --apple-bg: #f5f5f7;
  --apple-surface: #ffffff;
  --apple-text: #1d1d1f;
  --apple-text-secondary: #86868b;
  --apple-accent: #0071e3;
  --apple-accent-hover: #0077ed;
  --apple-radius: 12px;
  --apple-radius-lg: 20px;
  --apple-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  --apple-shadow-hover: 0 8px 30px rgba(0, 0, 0, 0.12);
  --apple-blur: blur(20px);
}

#app {
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, sans-serif;
}

.el-container {
  height: 100%;
}

/* Sidebar */
.sidebar {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: var(--apple-blur);
  -webkit-backdrop-filter: var(--apple-blur);
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  border-radius: 0 20px 20px 0;
  margin: 10px 0 10px 10px;
  width: 230px !important;
  overflow: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  margin-bottom: 8px;
}

.logo-icon {
  flex-shrink: 0;
  filter: drop-shadow(0 2px 8px rgba(0, 113, 227, 0.3));
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--apple-text);
  letter-spacing: -0.02em;
}

/* Menu */
.sidebar-menu {
  border: none !important;
  background: transparent !important;
  padding: 0 12px;
  flex: 1;
}

:deep(.sidebar-menu .el-menu-item) {
  height: 46px;
  line-height: 46px;
  border-radius: var(--apple-radius);
  margin-bottom: 4px;
  color: var(--apple-text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
}

:deep(.sidebar-menu .el-menu-item:hover) {
  background: rgba(0, 0, 0, 0.04) !important;
  color: var(--apple-text);
}

:deep(.sidebar-menu .el-menu-item.is-active) {
  background: rgba(0, 113, 227, 0.1) !important;
  color: var(--apple-accent);
  font-weight: 600;
}

:deep(.sidebar-menu .el-menu-item.is-active .el-icon) {
  color: var(--apple-accent);
}

:deep(.sidebar-menu .el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

/* Main Wrapper */
.main-wrapper {
  flex-direction: column;
  background: var(--apple-bg);
}

/* Header */
.header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: var(--apple-blur);
  -webkit-backdrop-filter: var(--apple-blur);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  height: 64px;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--apple-text-secondary);
  letter-spacing: 0.01em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: var(--apple-radius);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
  background: rgba(0, 0, 0, 0.03);
}

.admin-badge:hover {
  background: rgba(0, 0, 0, 0.06);
}

.admin-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-text);
}

.dropdown-arrow {
  font-size: 12px;
  color: var(--apple-text-secondary);
}

.admin-badge .el-icon:not(.dropdown-arrow) {
  color: var(--apple-accent);
  font-size: 18px;
}

/* Main Content */
.main-content {
  padding: 0;
  overflow-y: auto;
}
</style>
