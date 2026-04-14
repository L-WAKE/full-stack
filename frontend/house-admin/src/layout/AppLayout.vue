<script setup>
import {
  Briefcase,
  Expand,
  Files,
  Fold,
  HomeFilled,
  House,
  Notebook,
  Operation,
  Setting,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isSidebarCollapsed = ref(false)

const menuIconMap = {
  '/dashboard': HomeFilled,
  '/housing': House,
  '/housing/whole-rent': House,
  '/housing/shared-rent': House,
  '/housing/centralized': House,
  '/customer': UserFilled,
  '/customer/tenant': User,
  '/customer/landlord': UserFilled,
  '/workorder': Notebook,
  '/workorder/maintenance': Operation,
  '/workorder/cleaning': Briefcase,
  '/system': Setting,
  '/system/employee': User,
  '/system/role': Files,
  '/system/menu': Setting,
  '/system/notice': Files
}

const menus = computed(() => userStore.menus)
const isDashboard = computed(() => route.path === '/dashboard')
const sidebarToggleText = computed(() => (isSidebarCollapsed.value ? '展开侧栏' : '收起侧栏'))

function resolveMenuIcon(path) {
  return menuIconMap[path] || Files
}

function handleSelect(path) {
  router.push(path)
}

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  if (!userStore.menus.length) {
    await userStore.bootstrap()
  }
})
</script>

<template>
  <div class="layout-shell" :class="{ 'layout-shell--collapsed': isSidebarCollapsed }">
    <aside class="layout-sidebar">
      <div class="brand-panel" :class="{ 'brand-panel--collapsed': isSidebarCollapsed }">
        <div class="brand-mark">HM</div>
        <div v-show="!isSidebarCollapsed" class="brand-copy">
          <div class="brand-title">房源管理系统</div>
          <div class="brand-subtitle">租赁运营管理平台</div>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isSidebarCollapsed"
        :collapse-transition="false"
        class="sidebar-menu"
        text-color="#d8e6f2"
        active-text-color="#ffffff"
        background-color="transparent"
        @select="handleSelect"
      >
        <template v-for="menu in menus" :key="menu.path">
          <el-sub-menu
            v-if="menu.children?.length"
            :index="menu.path"
            popper-class="sidebar-menu-popper"
          >
            <template #title>
              <el-icon class="menu-icon">
                <component :is="resolveMenuIcon(menu.path)" />
              </el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon class="menu-icon">
                <component :is="resolveMenuIcon(child.path)" />
              </el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon class="menu-icon">
              <component :is="resolveMenuIcon(menu.path)" />
            </el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>

      <button class="sidebar-toggle" type="button" @click="toggleSidebar">
        <el-icon class="sidebar-toggle__icon">
          <Fold v-if="!isSidebarCollapsed" />
          <Expand v-else />
        </el-icon>
        <span v-show="!isSidebarCollapsed">{{ sidebarToggleText }}</span>
      </button>
    </aside>

    <main class="layout-main">
      <header v-if="isDashboard" class="layout-header">
        <div>
          <h1>租赁运营后台</h1>
          <p>围绕房源、租客、房东、工单和权限的一体化管理平台。</p>
        </div>
        <div class="header-actions">
          <div class="user-badge">
            <div class="user-name">{{ userStore.profile?.displayName || '管理员' }}</div>
            <div class="user-role">{{ userStore.profile?.roleCode || '系统管理员' }}</div>
          </div>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <section class="layout-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<style scoped lang="scss">
.layout-shell {
  --sidebar-width: 260px;
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--sidebar-width) 1fr;
  transition: grid-template-columns 0.24s ease;
}

.layout-shell--collapsed {
  --sidebar-width: 84px;
}

.layout-sidebar {
  padding: 28px 14px 18px;
  background: var(--sidebar-bg);
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.layout-shell--collapsed .layout-sidebar {
  padding-left: 10px;
  padding-right: 10px;
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  min-height: 72px;
}

.brand-panel--collapsed {
  justify-content: center;
}

.brand-copy {
  min-width: 0;
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #f59f00, #ffd166);
  color: #3f2c00;
  font-weight: 700;
  flex-shrink: 0;
}

.brand-title {
  font-weight: 700;
}

.brand-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  min-height: 0;
  width: 100%;
}

.menu-icon {
  font-size: 18px;
}

.layout-shell--collapsed :deep(.el-menu--collapse) {
  width: 64px;
}

.layout-shell--collapsed :deep(.el-menu--collapse > .el-menu-item),
.layout-shell--collapsed :deep(.el-menu--collapse > .el-sub-menu > .el-sub-menu__title) {
  justify-content: center;
}

.layout-shell--collapsed :deep(.el-sub-menu__icon-arrow) {
  display: none;
}

.layout-shell--collapsed :deep(.el-menu-item .el-icon),
.layout-shell--collapsed :deep(.el-sub-menu__title .el-icon) {
  margin-right: 0;
}

.layout-shell--collapsed :deep(.el-menu-item span),
.layout-shell--collapsed :deep(.el-sub-menu__title span) {
  display: none;
}

.layout-shell--collapsed :deep(.el-menu-item .menu-icon),
.layout-shell--collapsed :deep(.el-sub-menu__title .menu-icon) {
  display: inline-flex;
}

.layout-shell--collapsed :deep(.el-menu-item:hover),
.layout-shell--collapsed :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.12);
}

.sidebar-toggle {
  width: 100%;
  min-height: 44px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.layout-shell--collapsed .sidebar-toggle {
  width: 64px;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.22);
}

.sidebar-toggle__icon {
  font-size: 18px;
}

.layout-main {
  padding: 22px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 26px 28px;
  margin-bottom: 20px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(11, 114, 133, 0.92), rgba(22, 50, 79, 0.94)),
    linear-gradient(135deg, #0b7285, #16324f);
  color: #fff;
  box-shadow: 0 18px 50px rgba(11, 31, 51, 0.2);
}

.layout-header h1 {
  margin: 0 0 8px;
}

.layout-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.78);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-badge {
  min-width: 120px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
}

.user-name {
  font-weight: 700;
}

.user-role {
  font-size: 12px;
  opacity: 0.74;
}

.layout-content {
  min-width: 0;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .layout-sidebar {
    padding-bottom: 0;
  }

  .sidebar-toggle {
    display: none;
  }

  .layout-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: space-between;
  }
}

:global(.sidebar-menu-popper.el-popper) {
  border-radius: 14px;
  border: 1px solid rgba(19, 34, 56, 0.08);
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.12);
}

:global(.sidebar-menu-popper .el-menu) {
  min-width: 168px;
  padding: 8px;
  background: #ffffff;
}

:global(.sidebar-menu-popper .el-menu-item) {
  color: #304256;
  border-radius: 10px;
}

:global(.sidebar-menu-popper .el-menu-item:hover) {
  background: #eef6ff;
  color: #0b7285;
}

:global(.sidebar-menu-popper .el-menu-item.is-active) {
  background: rgba(11, 114, 133, 0.12);
  color: #0b7285;
}
</style>
