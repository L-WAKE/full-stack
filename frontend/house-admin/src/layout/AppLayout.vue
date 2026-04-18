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
  if (segments.length <= 1) return '控制台'
  return routeTitleMap[`/${segments[0]}`] || '控制台'
})

const headerDescription = computed(() => {
  if (route.path.startsWith('/housing')) {
    return '统一管理房源台账、出租状态、项目分布与租金表现，让日常盘点和运营调整更清晰。'
  }
  if (route.path.startsWith('/customer')) {
    return '把租客与房东资料收束到同一套经营视图里，方便持续追踪履约、沟通与服务记录。'
  }
  if (route.path.startsWith('/workorder')) {
    return '围绕维修与保洁任务建立更直观的服务节奏，减少遗漏，让响应进度更透明。'
  }
  if (route.path.startsWith('/system')) {
    return '对组织、角色、菜单与公告做统一配置，保持后台权限结构和协作秩序的清晰边界。'
  }
  return '围绕房源、客户、工单与系统配置构建一体化运营后台，用统一的经营视图承载关键资产信息。'
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
        <div>
          <div class="header-title">Housing Management Console</div>
          <div class="header-subtitle">{{ currentTitle }}</div>
        </div>
      </div>

      <div class="console-header__actions">
        <div class="header-chip">
          <span class="header-chip__label">系统角色</span>
          <span class="header-chip__value">{{ userStore.profile?.roleCode || '系统管理员' }}</span>
        </div>
        <div class="header-chip">
          <span class="header-chip__label">当前账号</span>
          <span class="header-chip__value">{{ userStore.profile?.displayName || '系统管理员' }}</span>
        </div>
        <button class="header-logout" type="button" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="console-body">
      <aside class="layout-sidebar">
        <div class="sidebar-caption">Modules</div>

        <el-menu
          :default-active="route.path"
          :collapse="isSidebarCollapsed"
          :collapse-transition="false"
          unique-opened
          class="sidebar-menu"
          text-color="#ffffff"
          active-text-color="#ffffff"
          background-color="transparent"
          @select="handleSelect"
        >
          <template v-for="menu in menus" :key="menu.path">
            <el-sub-menu v-if="menu.children?.length" :index="menu.path">
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
          <el-icon>
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
  background: var(--white);
}

.console-shell--collapsed {
  --sidebar-width: 76px;
}

.console-header {
  min-height: 72px;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) auto;
  gap: var(--space-4);
  align-items: center;
  padding: 16px 32px;
  color: var(--text-inverse);
  background: var(--dark);
}

.console-header__brand,
.console-header__actions {
  display: flex;
  align-items: center;
}

.console-header__brand {
  gap: 16px;
  min-width: 0;
}

.header-logo {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.36);
  background: transparent;
  color: var(--white);
  font-size: 14px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.header-title {
  font-size: 18px;
  font-weight: 300;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.header-subtitle {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
}

.console-header__actions {
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.header-chip,
.header-logout {
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: transparent;
  color: var(--white);
}

.header-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.header-chip__label {
  color: rgba(255, 255, 255, 0.54);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.header-chip__value {
  font-size: 12px;
  font-weight: 700;
}

.header-logout {
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.header-logout:hover {
  border-color: var(--bmw-blue);
  color: var(--bmw-blue);
}

.console-body {
  height: calc(100vh - 72px);
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  min-height: 0;
  transition: grid-template-columns var(--transition-base);
}

.layout-sidebar {
  position: relative;
  min-width: 0;
  padding: 20px 16px 64px;
  background: var(--dark);
}

.sidebar-caption {
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.54);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.sidebar-menu {
  background: transparent;
}

.menu-icon {
  font-size: 17px;
}

.layout-sidebar :deep(.el-menu-item),
.layout-sidebar :deep(.el-sub-menu__title) {
  height: 42px;
  margin: 0 0 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: transparent;
  color: var(--sidebar-text);
  font-size: 14px;
}

.layout-sidebar :deep(.el-sub-menu .el-menu-item) {
  margin-left: 8px;
}

.layout-sidebar :deep(.el-menu-item.is-active) {
  border-color: var(--bmw-blue);
  background: var(--bmw-blue);
  color: var(--white);
}

.layout-sidebar :deep(.el-menu-item:hover),
.layout-sidebar :deep(.el-sub-menu__title:hover) {
  background: var(--sidebar-hover);
}

.console-shell--collapsed :deep(.el-menu--collapse) {
  width: 44px;
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

.console-shell--collapsed .sidebar-caption {
  display: none;
}

.sidebar-toggle {
  position: absolute;
  left: 50%;
  bottom: 18px;
  transform: translateX(-50%);
  width: 34px;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: var(--white);
  cursor: pointer;
}

.layout-main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-header {
  width: min(100%, var(--content-max-width));
  margin: 0 auto;
  padding: 20px 24px 14px;
  border-bottom: 1px solid var(--border);
}

.page-header__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--meta);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 10px 0 6px;
  font-size: 36px;
  font-weight: 300;
  line-height: 1.1;
  text-transform: uppercase;
}

.page-header p {
  max-width: 760px;
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.6;
}

.layout-content {
  flex: 1;
  min-height: 0;
  width: min(100%, var(--content-max-width));
  margin: 0 auto;
  padding: 16px 24px 24px;
  overflow: auto;
}

@media (max-width: 1100px) {
  .console-header {
    grid-template-columns: 1fr;
  }

  .console-header__actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .console-body {
    grid-template-columns: 1fr;
  }

  .layout-sidebar {
    display: none;
  }

  .page-header {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .layout-content {
    padding: 12px 16px 16px;
  }
}
</style>
