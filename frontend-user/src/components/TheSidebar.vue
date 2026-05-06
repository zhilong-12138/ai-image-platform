<template>
  <aside class="sidebar">
    <div class="sidebar-wordmark">灵<span>画</span></div>

    <nav class="sidebar-nav">
      <button class="nav-item" :class="{ active: activeTab === 'feed' }" @click="$emit('navigate', 'feed')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg></span>
        提示词广场
      </button>
      <button class="nav-item" :class="{ active: activeTab === 'works' }" @click="$emit('navigate', 'works')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><path d="M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2z"/><path d="M8.5 10a1.5 1.5 0 100-3 1.5 1.5 0 000 3zM21 15l-5-5L5 21"/></svg></span>
        我的作品
      </button>
      <button class="nav-item" :class="{ active: activeTab === 'favorites' }" @click="$emit('navigate', 'favorites')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/></svg></span>
        我的收藏
      </button>

      <div class="nav-divider"></div>

      <button class="nav-item nav-create" @click="$emit('create')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg></span>
        创建图片
      </button>
    </nav>

    <div class="sidebar-footer">
      <button class="nav-item" @click="$emit('navigate', 'profile')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg></span>
      </button>
      <button v-if="!isLoggedIn" class="nav-item" title="登录" @click="$emit('login')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4M10 17l5-5-5-5M15 12H3"/></svg></span>
      </button>
      <button v-else class="nav-item" title="退出登录" @click="$emit('logout')">
        <span class="nav-icon"><svg viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg></span>
      </button>
    </div>
  </aside>
</template>

<script setup>
defineProps({ activeTab: String, isLoggedIn: Boolean })
defineEmits(['navigate', 'create', 'logout', 'login'])
</script>