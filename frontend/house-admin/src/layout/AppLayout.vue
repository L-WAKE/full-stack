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
        <!-- <div class="sidebar-product">
          <div class="sidebar-product__name">轻量云</div>
        </div> -->

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
  --sidebar-width: 248px;
  height: 100vh;
  overflow: hidden;
  color: var(--text-main);
  background: var(--bg-page);
}

.console-shell--collapsed {
  --sidebar-width: 76px;
}

.console-header {
  min-height: 64px;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) auto;
  align-items: center;
  gap: var(--space-3);
  padding: 12px 20px;
  color: var(--sidebar-text);
  background:
    radial-gradient(circle at top left, rgba(61, 139, 255, 0.14), transparent 24%),
    linear-gradient(135deg, #11161d, #18212c);
  box-shadow: inset 0 -1px 0 var(--line-dark);
}

.console-header__brand,
.console-header__actions {
  display: flex;
  align-items: center;
}

.console-header__brand {
  gap: var(--space-3);
  min-width: 0;
}

.header-logo {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  color: var(--sidebar-bg);
  background: linear-gradient(180deg, #f4f8ff, #dbe7ff);
  border-radius: 14px;
  font-size: 13px;
  font-weight: 800;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.header-title {
  color: var(--sidebar-text-strong);
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.header-divider {
  width: 1px;
  height: 22px;
  background: rgba(255, 255, 255, 0.12);
}

.header-home {
  color: var(--sidebar-text-muted);
  font-size: 13px;
}

.console-header__actions {
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.header-meta {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  min-height: 38px;
  padding: 0 14px;
  color: var(--sidebar-text);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.04);
  font-size: 13px;
  cursor: pointer;
  transition:
    border-color var(--transition-fast),
    background-color var(--transition-fast),
    transform var(--transition-fast);
}

.header-meta__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4f92ff;
  box-shadow:
    12px 0 0 rgba(79, 146, 255, 0.72),
    24px 0 0 rgba(79, 146, 255, 0.45);
  margin-right: 28px;
  flex: 0 0 auto;
}

.header-meta__label {
  color: var(--sidebar-text-muted);
}

.header-meta__value {
  color: var(--sidebar-text-strong);
}

.header-meta--logout .header-meta__dot {
  background: #94a3b8;
  box-shadow:
    12px 0 0 rgba(148, 163, 184, 0.72),
    24px 0 0 rgba(148, 163, 184, 0.45);
}

.header-meta:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.08);
}

.header-meta:hover .header-meta__value {
  color: #ffffff;
}

.console-body {
  height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  min-height: 0;
  transition: grid-template-columns var(--transition-base);
}

.layout-sidebar {
  position: relative;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 14px 12px 18px;
  color: var(--sidebar-text);
  background:
    radial-gradient(circle at top, rgba(61, 139, 255, 0.14), transparent 32%),
    linear-gradient(180deg, var(--sidebar-bg), #18212c);
  box-shadow: inset -1px 0 0 var(--line-dark);
  overflow: visible;
}

.sidebar-product {
  min-height: 64px;
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 14px 16px;
  color: var(--sidebar-text-strong);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(10px);
}

.sidebar-product__name {
  font-size: 15px;
  font-weight: 700;
}

.sidebar-product__desc {
  margin-top: 6px;
  color: var(--sidebar-text-muted);
  font-size: 12px;
}

.console-shell--collapsed .sidebar-product {
  justify-content: center;
  padding-inline: 12px;
}

.sidebar-menu {
  flex: 1;
  min-height: 0;
  padding: 8px 0 64px;
}

.menu-icon {
  font-size: 17px;
}

.layout-sidebar :deep(.el-menu-item),
.layout-sidebar :deep(.el-sub-menu__title) {
  height: 42px;
  margin: 0 0 6px;
  border-radius: 12px;
  font-size: 14px;
  transition:
    background-color var(--transition-fast),
    color var(--transition-fast),
    transform var(--transition-fast);
}

.layout-sidebar :deep(.el-menu-item) {
  padding-left: 44px !important;
}

.layout-sidebar :deep(.el-sub-menu .el-menu-item) {
  height: 38px;
  margin-left: 10px;
  background: rgba(255, 255, 255, 0.02);
}

.layout-sidebar :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active);
  color: var(--sidebar-text-strong);
  box-shadow: 0 14px 26px rgba(23, 104, 255, 0.2);
}

.layout-sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: transparent;
  color: var(--sidebar-text);
}

.layout-sidebar :deep(.el-menu-item:hover),
.layout-sidebar :deep(.el-sub-menu__title:hover) {
  background: var(--sidebar-hover);
  transform: translateX(2px);
}

.layout-sidebar :deep(.el-menu-item.is-active:hover) {
  background: var(--sidebar-active);
}

.layout-sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title:hover) {
  background: var(--sidebar-hover);
}

.console-shell--collapsed :deep(.el-menu--collapse) {
  width: 52px;
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
  bottom: 18px;
  transform: translateX(-50%);
  width: 34px;
  height: 48px;
  display: grid;
  place-items: center;
  color: var(--sidebar-text);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  z-index: 3;
  transition:
    transform var(--transition-fast),
    background-color var(--transition-fast),
    color var(--transition-fast);
}

.sidebar-toggle:hover {
  color: #fff;
  background: var(--primary);
  transform: translateX(-50%) translateY(-1px);
}

.layout-main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
}

.page-header {
  width: min(100%, var(--content-max-width));
  margin: 0 auto;
  padding: 22px 24px 14px;
}

.page-header__breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 12px 0 10px;
  font-size: 30px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.page-header p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
  max-width: 760px;
}

.layout-content {
  flex: 1;
  min-height: 0;
  width: min(100%, var(--content-max-width));
  margin: 0 auto;
  padding: 0 24px 24px;
  overflow: auto;
}

@media (max-width: 1100px) {
  .console-header {
    grid-template-columns: minmax(240px, 1fr);
    height: auto;
  }

  .console-header__actions {
    justify-content: flex-start;
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

  .page-header {
    padding: 18px 16px 12px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .layout-content {
    padding: 0 16px 16px;
  }
}

:global(.sidebar-menu-popper.el-popper) {
  border-radius: var(--radius-md);
  border: 1px solid var(--line-color);
  box-shadow: var(--shadow-panel);
}

:global(.sidebar-menu-popper .el-menu) {
  min-width: 176px;
  padding: 6px;
  background: #fff;
}

:global(.sidebar-menu-popper .el-menu-item) {
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
}

:global(.sidebar-menu-popper .el-menu-item:hover) {
  background: #eef4ff;
  color: var(--primary);
}
</style>
