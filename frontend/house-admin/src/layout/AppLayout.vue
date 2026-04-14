<script setup>
import {
  ArrowRight,
  Briefcase,
  Expand,
  Files,
  Fold,
  HomeFilled,
  House,
  Notebook,
  Operation,
  Setting,
  SwitchButton,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { routeTitleMap } from '../utils/locale'
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
const currentTitle = computed(() => routeTitleMap[route.path] || '房源管理系统')
const currentParentTitle = computed(() => {
  const segments = route.path.split('/').filter(Boolean)
  if (segments.length <= 1) {
    return '经营后台'
  }
  const parentPath = `/${segments[0]}`
  return routeTitleMap[parentPath] || '经营后台'
})
const headerDescription = computed(() => {
  if (route.path.startsWith('/housing')) {
    return '聚焦房源台账、出租状态、项目分布与租赁效率，帮助运营团队快速完成资产盘点。'
  }
  if (route.path.startsWith('/customer')) {
    return '统一维护租客与房东信息，沉淀完整履约档案和沟通基础资料。'
  }
  if (route.path.startsWith('/workorder')) {
    return '围绕维修与保洁任务建立流程闭环，让异常响应、派单和完结进度一目了然。'
  }
  if (route.path.startsWith('/system')) {
    return '沉淀组织、角色、菜单和公告配置，确保运营权限边界清晰可控。'
  }
  return '围绕房源、客户、工单和系统配置构建统一经营后台，信息密度更高，操作路径更清晰。'
})
const sidebarToggleText = computed(() => (isSidebarCollapsed.value ? '展开导航' : '收起导航'))

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
          <div class="brand-subtitle">Rental Operation Console</div>
        </div>
      </div>

      <div v-show="!isSidebarCollapsed" class="sidebar-overview">
        <div class="sidebar-overview__label">当前登录</div>
        <div class="sidebar-overview__name">{{ userStore.profile?.displayName || '系统管理员' }}</div>
        <div class="sidebar-overview__role">{{ userStore.profile?.roleCode || '管理员' }}</div>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isSidebarCollapsed"
        :collapse-transition="false"
        class="sidebar-menu"
        text-color="#c8d4e6"
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
      <header class="topbar">
        <div class="topbar-copy">
          <div class="topbar-breadcrumb">
            <span>{{ currentParentTitle }}</span>
            <el-icon><ArrowRight /></el-icon>
            <span>{{ currentTitle }}</span>
          </div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ headerDescription }}</p>
        </div>
        <div class="topbar-actions">
          <div class="topbar-badge">
            <span class="topbar-badge__label">角色</span>
            <span class="topbar-badge__value">{{ userStore.profile?.roleCode || '管理员' }}</span>
          </div>
          <div class="topbar-badge topbar-badge--accent">
            <span class="topbar-badge__label">账号</span>
            <span class="topbar-badge__value">{{ userStore.profile?.displayName || '系统管理员' }}</span>
          </div>
          <el-button class="logout-button" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
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
  --sidebar-width: 286px;
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  min-height: 100vh;
  transition: grid-template-columns 0.24s ease;
}

.layout-shell--collapsed {
  --sidebar-width: 96px;
}

.layout-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  color: #fff;
  background: var(--sidebar-bg);
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.06);
}

.brand-panel,
.sidebar-overview {
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(12px);
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 78px;
  padding: 14px;
}

.brand-panel--collapsed {
  justify-content: center;
}

.brand-mark {
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(135deg, #eff6ff, #bfdbfe);
  color: #1e3a8a;
  font-weight: 800;
  letter-spacing: 0.04em;
  flex-shrink: 0;
}

.brand-copy {
  min-width: 0;
}

.brand-title {
  font-size: 17px;
  font-weight: 800;
}

.brand-subtitle {
  margin-top: 4px;
  color: rgba(226, 232, 240, 0.72);
  font-size: 12px;
}

.sidebar-overview {
  padding: 14px;
}

.sidebar-overview__label {
  color: rgba(191, 219, 254, 0.78);
  font-size: 12px;
}

.sidebar-overview__name {
  margin-top: 10px;
  font-size: 16px;
  font-weight: 700;
}

.sidebar-overview__role {
  margin-top: 4px;
  color: rgba(226, 232, 240, 0.7);
  font-size: 13px;
}

.sidebar-menu {
  flex: 1;
  min-height: 0;
  padding: 8px 0;
}

.menu-icon {
  font-size: 18px;
}

.layout-sidebar :deep(.el-menu-item),
.layout-sidebar :deep(.el-sub-menu__title) {
  height: 46px;
  margin-bottom: 6px;
  border-radius: 14px;
}

.layout-sidebar :deep(.el-menu-item.is-active),
.layout-sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.32), rgba(37, 99, 235, 0.2));
  box-shadow: inset 0 0 0 1px rgba(147, 197, 253, 0.14);
}

.layout-sidebar :deep(.el-menu-item:hover),
.layout-sidebar :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08);
}

.layout-shell--collapsed :deep(.el-menu--collapse) {
  width: 64px;
}

.layout-shell--collapsed :deep(.el-menu--collapse > .el-menu-item),
.layout-shell--collapsed :deep(.el-menu--collapse > .el-sub-menu > .el-sub-menu__title) {
  justify-content: center;
}

.layout-shell--collapsed :deep(.el-sub-menu__icon-arrow),
.layout-shell--collapsed :deep(.el-menu-item span),
.layout-shell--collapsed :deep(.el-sub-menu__title span) {
  display: none;
}

.layout-shell--collapsed :deep(.el-menu-item .el-icon),
.layout-shell--collapsed :deep(.el-sub-menu__title .el-icon) {
  margin-right: 0;
}

.layout-shell--collapsed .sidebar-toggle {
  width: 64px;
  align-self: center;
}

.sidebar-toggle {
  width: 100%;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.07);
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.16);
  transform: translateY(-1px);
}

.layout-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 18px 18px 18px 8px;
}

.topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 26px 30px;
  border-radius: 32px;
  border: 1px solid rgba(255, 255, 255, 0.78);
  background:
    radial-gradient(circle at 0% 0%, rgba(96, 165, 250, 0.18), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(245, 248, 252, 0.96));
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(18px);
}

.topbar-copy {
  min-width: 0;
}

.topbar-breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}

.topbar h1 {
  margin: 16px 0 10px;
  font-size: 32px;
  line-height: 1.1;
  letter-spacing: -0.03em;
}

.topbar p {
  max-width: 760px;
  margin: 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.topbar-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.topbar-badge {
  min-width: 128px;
  padding: 12px 14px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(255, 255, 255, 0.72);
}

.topbar-badge--accent {
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.92), rgba(236, 253, 245, 0.88));
}

.topbar-badge__label {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
}

.topbar-badge__value {
  display: block;
  margin-top: 8px;
  font-weight: 700;
}

.logout-button {
  min-width: 124px;
}

.layout-content {
  flex: 1;
  min-height: 0;
  padding-top: 18px;
  overflow: hidden;
}

@media (max-width: 1080px) {
  .topbar {
    flex-direction: column;
  }

  .topbar-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .layout-sidebar {
    position: static;
    height: auto;
  }

  .sidebar-toggle {
    display: none;
  }

  .layout-main {
    padding: 12px;
  }

  .topbar {
    padding: 22px 20px;
    border-radius: 24px;
  }

  .topbar h1 {
    font-size: 28px;
  }
}

:global(.sidebar-menu-popper.el-popper) {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 20px 42px rgba(15, 23, 42, 0.12);
}

:global(.sidebar-menu-popper .el-menu) {
  min-width: 176px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.96);
}

:global(.sidebar-menu-popper .el-menu-item) {
  border-radius: 12px;
  color: var(--text-secondary);
}

:global(.sidebar-menu-popper .el-menu-item:hover) {
  background: rgba(37, 99, 235, 0.06);
  color: var(--primary);
}

:global(.sidebar-menu-popper .el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.10);
  color: var(--primary);
}
</style>
