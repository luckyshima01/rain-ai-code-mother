<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { computed, h } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { AppstoreOutlined, HomeOutlined, LogoutOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { type MenuProps, message } from 'ant-design-vue'
import { userLogout } from '@/api/userController.ts'

// 获取登录用户状态
const loginUserStore = useLoginUserStore()

const router = useRouter()

const originItems = [
  { key: '/', icon: h(HomeOutlined), label: h('span', { class: 'menu-label' }, '主页') },
  { key: '/admin/userManage', icon: h(TeamOutlined), label: h('span', { class: 'menu-label' }, '用户管理') },
  { key: '/admin/appManage', icon: h(AppstoreOutlined), label: h('span', { class: 'menu-label' }, '应用管理') },
]
// 过滤掉没有权限的菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((item) => {
    const menuKey = item?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser?.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}
// 展示在菜单栏的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

const selectedKeys = computed(() => {
  return [router.currentRoute.value.path]
})

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    router.push({
      path: '/user/login',
    })
  } else {
    message.error('退出登录失败' + res.data.message)
  }
}
</script>

<template>
  <div class="global-header">
    <div class="header-left">
      <RouterLink to="/" class="logo-link">
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
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="doLogout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
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

/* Hide the default full-width bottom indicator */
:deep(.nav-menu.ant-menu-horizontal > .ant-menu-item::after),
:deep(.nav-menu.ant-menu-horizontal > .ant-menu-item-selected::after),
:deep(.nav-menu.ant-menu-horizontal > .ant-menu-item:hover::after) {
  border-bottom: none !important;
}

/* Small gap between icon and text */
:deep(.nav-menu .ant-menu-item .anticon) {
  margin-right: 5px;
}

/* Underline only under the text label */
:deep(.nav-menu .ant-menu-item-selected .menu-label) {
  border-bottom: 2px solid #1677ff;
  padding-bottom: 1px;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
