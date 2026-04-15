<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

import { useAuthStore } from "../../stores/auth";
import { useWorkspaceStore } from "../../stores/workspace";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const workspaceStore = useWorkspaceStore();
const isSidebarCollapsed = ref(false);

const menuItems = [
  { path: "/dashboard", label: "总览" },
  { path: "/spaces", label: "知识库空间" },
  { path: "/documents", label: "文档中心" },
  { path: "/chat", label: "检索问答" },
  { path: "/settings", label: "模型设置" },
];

const routeMetaMap = {
  dashboard: {
    parent: "控制台",
    title: "总览",
    subtitle: "围绕空间、文档、会话和检索链路构建统一 AI 知识库后台。",
  },
  spaces: {
    parent: "知识运营",
    title: "知识库空间",
    subtitle: "统一管理业务知识空间、成员角色与协作权限边界。",
  },
  documents: {
    parent: "知识运营",
    title: "文档中心",
    subtitle: "管理上传、解析、切片、可见范围和文档入库状态。",
  },
  chat: {
    parent: "智能检索",
    title: "检索问答",
    subtitle: "基于当前空间发起问答，查看命中片段和引用来源。",
  },
  settings: {
    parent: "系统配置",
    title: "模型设置",
    subtitle: "预留 Chat、Embedding、Rerank 等模型配置和供应商接入边界。",
  },
};

const activeMenu = computed(() => route.path);
const routeMeta = computed(() => routeMetaMap[route.name] || routeMetaMap.dashboard);
const currentSpaceRole = computed(() => {
  const members = workspaceStore.activeSpaceDetail?.members || [];
  const current = members.find((item) => item.user_id === authStore.profile?.id);
  return current?.role || "-";
});

onMounted(async () => {
  if (!authStore.profile) {
    await authStore.hydrate();
  }
  await workspaceStore.loadOverview();
  await workspaceStore.loadSpaces();
});

function handleSpaceChange() {
  workspaceStore.loadActiveSpaceDetail();
  workspaceStore.loadDocuments();
  workspaceStore.loadConversations();
}

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
}

function logout() {
  authStore.logout();
  router.push("/login");
}
</script>

<template>
  <div class="console-shell" :class="{ 'console-shell--collapsed': isSidebarCollapsed }">
    <header class="console-header">
      <div class="console-header__brand">
        <div class="header-logo">AI</div>
        <div class="header-title">AI 知识库系统</div>
        <span class="header-divider"></span>
        <div class="header-home">控制台</div>
      </div>

      <div class="console-header__actions">
        <button class="header-meta" type="button">
          <span class="header-meta__dot"></span>
          <span class="header-meta__label">角色</span>
          <span class="header-meta__value">{{ authStore.profile?.role || "管理员" }}</span>
        </button>
        <button class="header-meta" type="button">
          <span class="header-meta__dot"></span>
          <span class="header-meta__label">空间角色</span>
          <span class="header-meta__value">{{ currentSpaceRole }}</span>
        </button>
        <button class="header-meta header-meta--logout" type="button" @click="logout">
          <span class="header-meta__dot"></span>
          <span class="header-meta__value">退出登录</span>
        </button>
      </div>
    </header>

    <div class="console-body">
      <aside class="layout-sidebar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isSidebarCollapsed"
          :collapse-transition="false"
          class="sidebar-menu"
          text-color="#c8cdd3"
          active-text-color="#ffffff"
          background-color="transparent"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
            @click="router.push(item.path)"
          >
            <span class="menu-dot"></span>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>

        <button
          class="sidebar-toggle"
          type="button"
          :aria-label="isSidebarCollapsed ? '展开导航' : '收起导航'"
          @click="toggleSidebar"
        >
          {{ isSidebarCollapsed ? ">" : "<" }}
        </button>
      </aside>

      <main class="layout-main">
        <header class="page-header">
          <div class="page-header__breadcrumb">
            <span>{{ routeMeta.parent }}</span>
            <span>/</span>
            <span>{{ routeMeta.title }}</span>
          </div>
          <div class="page-header__row">
            <div>
              <h1>{{ routeMeta.title }}</h1>
              <p>{{ routeMeta.subtitle }}</p>
            </div>
            <el-select
              v-model="workspaceStore.activeSpaceId"
              class="space-switcher"
              placeholder="选择知识库空间"
              @change="handleSpaceChange"
            >
              <el-option
                v-for="space in workspaceStore.spaces"
                :key="space.id"
                :label="space.name"
                :value="space.id"
              />
            </el-select>
          </div>
        </header>

        <section class="layout-content">
          <router-view />
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
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
}

.header-title {
  color: var(--sidebar-text-strong);
  font-size: 17px;
  font-weight: 700;
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

.console-body {
  height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  min-height: 0;
  transition: grid-template-columns 0.24s ease;
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
}

.sidebar-menu {
  flex: 1;
  min-height: 0;
  padding: 8px 0 64px;
  border-right: none;
  background: transparent;
}

.layout-sidebar :deep(.el-menu-item) {
  height: 42px;
  margin: 0 0 6px;
  border-radius: 12px;
  font-size: 14px;
  transition:
    background-color 0.18s ease,
    color 0.18s ease,
    transform 0.18s ease;
}

.layout-sidebar :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active);
  color: var(--sidebar-text-strong);
  box-shadow: 0 14px 26px rgba(23, 104, 255, 0.2);
}

.layout-sidebar :deep(.el-menu-item:hover) {
  background: var(--sidebar-hover);
  transform: translateX(2px);
}

.menu-dot {
  width: 6px;
  height: 6px;
  margin-right: 10px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.7;
}

.console-shell--collapsed :deep(.el-menu--collapse) {
  width: 52px;
}

.console-shell--collapsed :deep(.el-menu--collapse > .el-menu-item) {
  justify-content: center;
  padding: 0 !important;
}

.console-shell--collapsed .menu-dot {
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

.page-header__row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.page-header h1 {
  margin: 12px 0 10px;
  font-size: 30px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.page-header p {
  max-width: 760px;
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.space-switcher {
  width: 280px;
  margin-top: 10px;
}

.layout-content {
  flex: 1;
  min-height: 0;
  padding: 0 24px 24px;
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
    padding: 18px 16px 12px;
  }

  .page-header__row {
    flex-direction: column;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .space-switcher {
    width: 100%;
  }

  .layout-content {
    padding: 0 16px 16px;
  }
}
</style>
