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
    return '控制台'
  }
  const parentPath = `/${segments[0]}`
  return routeTitleMap[parentPath] || '控制台'
})
const headerDescription = computed(() => {
  if (route.path.startsWith('/housing')) {
    return '集中管理房源台账、出租状态、项目分布与租赁效率，帮助运营团队快速完成资产盘点。'
  }
  if (route.path.startsWith('/customer')) {
    return '统一维护租客与房东信息，沉淀完整履约档案和沟通基础资料。'
  }
  if (route.path.startsWith('/workorder')) {
    return '围绕维修与保洁任务建立流程闭环，让异常响应、派单和完结进度更清晰。'
  }
  if (route.path.startsWith('/system')) {
    return '管理组织、角色、菜单和公告配置，确保运营权限边界清晰可控。'
  }
  return '围绕房源、客户、工单和系统配置构建统一经营后台。'
})

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
  <div class="console-shell" :class="{ 'console-shell--collapsed': isSidebarCollapsed }">
    <header class="console-header">
      <div class="console-header__brand">
        <div class="header-logo">HM</div>
        <div class="header-title">房源管理系统</div>
        <span class="header-divider"></span>
        <div class="header-home">控制台</div>
      </div>

      <div class="console-header__actions">
        <button class="header-meta" type="button">
          <span class="header-meta__dot"></span>
          <span class="header-meta__label">角色</span>
          <span class="header-meta__value">{{ userStore.profile?.roleCode || '管理员' }}</span>
        </button>
        <button class="header-meta" type="button">
          <span class="header-meta__dot"></span>
          <span class="header-meta__label">账号</span>
          <span class="header-meta__value">{{ userStore.profile?.displayName || '系统管理员' }}</span>
        </button>
        <button class="header-meta header-meta--logout" type="button" @click="handleLogout">
          <span class="header-meta__dot"></span>
          <span class="header-meta__value">退出登录</span>
        </button>
      </div>
    </header>

    <div class="console-body">
      <aside class="layout-sidebar">
        <div class="sidebar-product">
          <div class="sidebar-product__name">轻量云</div>
        </div>

        <el-menu
          :default-active="route.path"
          :collapse="isSidebarCollapsed"
          :collapse-transition="false"
          unique-opened
          class="sidebar-menu"
          text-color="#c8cdd3"
          active-text-color="#ffffff"
          background-color="transparent"
          @select="handleSelect"
        >
          <template v-for="menu in menus" :key="menu.path">
            <el-sub-menu v-if="menu.children?.length" :index="menu.path" popper-class="sidebar-menu-popper">
              <template #title>
                <el-icon class="menu-icon">
                  <component :is="resolveMenuIcon(menu.path)" />
                </el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
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

        <button
          class="sidebar-toggle"
          type="button"
          :aria-label="isSidebarCollapsed ? '展开导航' : '收起导航'"
          @click="toggleSidebar"
        >
          <el-icon class="sidebar-toggle__icon">
            <Fold v-if="!isSidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </button>
      </aside>

      <main class="layout-main">
        <header class="page-header">
          <div class="page-header__breadcrumb">
            <span>{{ currentParentTitle }}</span>
            <el-icon><ArrowRight /></el-icon>
            <span>{{ currentTitle }}</span>
          </div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ headerDescription }}</p>
        </header>

        <section class="layout-content">
          <router-view />
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
.console-shell {
  --sidebar-width: 200px;
  height: 100vh;
  overflow: hidden;
  color: var(--text-main);
  background: var(--bg-page);
}

.console-shell--collapsed {
  --sidebar-width: 48px;
}

.console-header {
  height: 50px;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  color: #d9dde3;
  background: #171819;
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.06);
}

.console-header__brand,
.console-header__actions {
  display: flex;
  align-items: center;
}

.console-header__brand {
  gap: 10px;
  min-width: 0;
}

.header-logo {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  color: #171819;
  background: #eef3fb;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 800;
}

.header-title {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.header-divider {
  width: 1px;
  height: 18px;
  background: rgba(255, 255, 255, 0.14);
}

.header-home {
  color: #c8cdd3;
  font-size: 13px;
}

.console-header__actions {
  justify-content: flex-end;
  gap: 18px;
}

.header-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  color: #d9dde3;
  border: none;
  background: transparent;
  font-size: 13px;
  cursor: pointer;
}

.header-meta__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow:
    12px 0 0 rgba(59, 130, 246, 0.72),
    24px 0 0 rgba(59, 130, 246, 0.45);
  margin-right: 28px;
  flex: 0 0 auto;
}

.header-meta__label {
  color: #8e969f;
}

.header-meta__value {
  color: #f5f7fa;
}

.header-meta--logout .header-meta__dot {
  background: #94a3b8;
  box-shadow:
    12px 0 0 rgba(148, 163, 184, 0.72),
    24px 0 0 rgba(148, 163, 184, 0.45);
}

.header-meta:hover .header-meta__value {
  color: #ffffff;
}

.console-body {
  height: calc(100vh - 50px);
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  min-height: 0;
  transition: grid-template-columns 0.2s ease;
}

.layout-sidebar {
  position: relative;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #c8cdd3;
  background: #1d1f21;
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.05);
  overflow: visible;
}

.sidebar-product {
  min-height: 56px;
  padding: 14px 16px 10px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-product__name {
  font-size: 15px;
  font-weight: 700;
}

.sidebar-product__desc {
  margin-top: 6px;
  color: #8b949e;
  font-size: 12px;
}

.console-shell--collapsed .sidebar-product {
  padding-inline: 0;
  text-align: center;
}

.sidebar-menu {
  flex: 1;
  min-height: 0;
  padding: 8px 0;
}

.menu-icon {
  font-size: 17px;
}

.layout-sidebar :deep(.el-menu-item),
.layout-sidebar :deep(.el-sub-menu__title) {
  height: 36px;
  margin: 0;
  border-radius: 0;
  font-size: 14px;
}

.layout-sidebar :deep(.el-menu-item) {
  padding-left: 44px !important;
}

.layout-sidebar :deep(.el-sub-menu .el-menu-item) {
  height: 34px;
  background: #171819;
}

.layout-sidebar :deep(.el-menu-item.is-active) {
  background: #1464ff;
  color: #fff;
}

.layout-sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: transparent;
  color: #c8cdd3;
}

.layout-sidebar :deep(.el-menu-item:hover),
.layout-sidebar :deep(.el-sub-menu__title:hover) {
  background: #25282b;
}

.layout-sidebar :deep(.el-menu-item.is-active:hover) {
  background: #1464ff;
}

.layout-sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title:hover) {
  background: #25282b;
}

.console-shell--collapsed :deep(.el-menu--collapse) {
  width: 48px;
}

.console-shell--collapsed :deep(.el-menu--collapse > .el-menu-item),
.console-shell--collapsed :deep(.el-menu--collapse > .el-sub-menu > .el-sub-menu__title) {
  justify-content: center;
  padding: 0 !important;
}

.console-shell--collapsed :deep(.el-menu-item .el-icon),
.console-shell--collapsed :deep(.el-sub-menu__title .el-icon) {
  margin-right: 0;
}

.sidebar-toggle {
  position: absolute;
  left: 50%;
  bottom: 16px;
  transform: translateX(-50%);
  width: 26px;
  height: 44px;
  display: grid;
  place-items: center;
  color: #d9dde3;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  background: #2a2d30;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 3;
}

.sidebar-toggle:hover {
  color: #fff;
  background: #1464ff;
}

.layout-main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #f4f6fa;
  overflow: hidden;
}

.page-header {
  min-height: 88px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e8ef;
}

.page-header__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--primary);
  font-size: 12px;
}

.page-header h1 {
  margin: 10px 0 8px;
  font-size: 22px;
  line-height: 1.15;
}

.page-header p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.55;
}

.layout-content {
  flex: 1;
  min-height: 0;
  padding: 14px 20px 18px;
  overflow: auto;
}

@media (max-width: 1100px) {
  .console-header {
    grid-template-columns: minmax(240px, 1fr);
    height: auto;
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .console-header__actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .console-header {
    grid-template-columns: 1fr;
  }

  .console-body {
    grid-template-columns: 1fr;
  }

  .layout-sidebar {
    display: none;
  }

  .layout-content {
    padding: 12px;
  }
}

:global(.sidebar-menu-popper.el-popper) {
  border-radius: 4px;
  border: 1px solid #dfe3ea;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.12);
}

:global(.sidebar-menu-popper .el-menu) {
  min-width: 176px;
  padding: 4px;
  background: #fff;
}

:global(.sidebar-menu-popper .el-menu-item) {
  border-radius: 2px;
  color: var(--text-secondary);
}

:global(.sidebar-menu-popper .el-menu-item:hover) {
  background: #eef4ff;
  color: var(--primary);
}
</style>
