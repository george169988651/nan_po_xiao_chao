<script setup>
import { useAuthStore } from '@/stores/auth'
import { Dish, List, ChatDotRound, SwitchButton, Food } from '@element-plus/icons-vue'

const auth = useAuthStore()

const menus = [
  { path: '/menu', title: '点餐', icon: Dish },
  { path: '/orders', title: '订单', icon: List },
  { path: '/chat', title: 'AI 服务员', icon: ChatDotRound },
]

function handleLogout() {
  auth.logout()
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="192px" class="aside">
      <div class="brand">
        <el-icon :size="18" class="brand-icon"><Food /></el-icon>
        <span>南坡小炒</span>
      </div>
      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title || '' }}</div>
        <div class="header-user">
          <span class="nickname">{{ auth.nickname || '食客' }}</span>
          <el-divider direction="vertical" />
          <el-button link :icon="SwitchButton" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: var(--npc-surface);
  border-right: 1px solid var(--npc-border);
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  height: var(--npc-header-h);
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--npc-text);
}

.brand-icon {
  color: var(--npc-accent);
}

.menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--npc-header-h);
  background: var(--npc-surface);
  border-bottom: 1px solid var(--npc-border);
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--npc-text);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  font-size: 13px;
  color: var(--npc-text-2);
}

.main {
  padding: 20px;
}
</style>
