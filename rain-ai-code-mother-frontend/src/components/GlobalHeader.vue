<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { computed } from 'vue'

const router = useRouter()

const menuItems = [
  { key: '/', label: '首页' },
  { key: '/about', label: '关于' },
]

const selectedKeys = computed(() => {
  return [router.currentRoute.value.path]
})

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>

<template>
  <div class="global-header">
    <div class="header-left">
      <RouterLink to="/public" class="logo-link">
        <img src="/logo.png" alt="logo" class="logo" />
        <span class="site-title">Rain AI</span>
      </RouterLink>
    </div>

    <div class="header-nav">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        :items="menuItems"
        @click="handleMenuClick"
        class="nav-menu"
      />
    </div>

    <div class="header-right">
      <a-button type="primary">登录</a-button>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
}

.logo {
  height: 36px;
  width: auto;
  object-fit: contain;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
}

.header-nav {
  flex: 1;
  min-width: 0;
  margin: 0 24px;
}

.nav-menu {
  border-bottom: none !important;
  line-height: 62px;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
