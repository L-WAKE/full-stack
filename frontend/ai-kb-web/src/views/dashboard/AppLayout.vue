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
  { path: "/dashboard", label: "概览", desc: "运营与健康度总览" },
  { path: "/spaces", label: "知识空间", desc: "空间与成员权限" },
  { path: "/documents", label: "文档治理", desc: "上传、解析与切片" },
  { path: "/chat", label: "检索问答", desc: "对话与引用来源" },
  { path: "/settings", label: "模型策略", desc: "检索与模型参数" },
];

const routeMetaMap = {
  dashboard: {
    parent: "Enterprise AI",
    title: "企业知识助手控制台",
    subtitle: "围绕知识入库、检索质量、权限治理和问答使用情况统一管理企业级知识库系统。",
  },
  spaces: {
    parent: "Knowledge Ops",
    title: "知识空间与场景设计",
    subtitle: "按业务域组织 FAQ、SOP、产品资料、交付文档和制度资产，为不同团队建立清晰边界。",
  },
  documents: {
    parent: "Knowledge Ops",
    title: "文档治理中心",
    subtitle: "统一管理文档上传、解析状态、切片质量、可见范围和入库健康度。",
  },
  chat: {
    parent: "Retrieval QA",
    title: "检索问答与引用追踪",
    subtitle: "面向客服、售前、交付和内部支持团队提供可追溯的知识问答能力。",
  },
  settings: {
    parent: "AI Strategy",
    title: "模型与检索策略",
    subtitle: "集中配置聊天模型、Embedding 方案和 RAG 参数，为生产化扩展保留能力。",
  },
};

const activeMenu = computed(() => route.path);
const routeMeta = computed(() => routeMetaMap[route.name] || routeMetaMap.dashboard);
const currentSpaceRole = computed(() => {
  const members = workspaceStore.activeSpaceDetail?.members || [];
  const current = members.find((item) => item.user_id === authStore.profile?.id);
  return current?.role || "-";
});
const activeSpaceSummary = computed(() => {
  const current = workspaceStore.spaces.find((item) => item.id === workspaceStore.activeSpaceId);
  if (!current) return "未选择知识空间";
  return `${current.name} / ${current.document_count} docs / ${current.member_count} members`;
});

onMounted(async () => {
  if (!authStore.profile) {
    await authStore.hydrate();
  }
  await workspaceStore.loadOverview();
  await workspaceStore.loadSpaces();
  await workspaceStore.loadRecentActivity();
});

async function handleSpaceChange() {
  await workspaceStore.loadActiveSpaceDetail();
  await Promise.all([workspaceStore.loadDocuments(), workspaceStore.loadConversations()]);
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
        <div class="header-logo">KA</div>
        <div>
          <div class="header-title">Enterprise Knowledge Assistant</div>
          <div class="header-subtitle">{{ activeSpaceSummary }}</div>
        </div>
      </div>

      <div class="console-header__actions">
        <div class="header-chip">
          <span class="header-chip__label">系统角色</span>
          <span class="header-chip__value">{{ authStore.profile?.role || "-" }}</span>
        </div>
        <div class="header-chip">
          <span class="header-chip__label">空间角色</span>
          <span class="header-chip__value">{{ currentSpaceRole }}</span>
        </div>
        <button class="header-logout" type="button" @click="logout">退出登录</button>
      </div>
    </header>

    <div class="console-body">
      <aside class="layout-sidebar">
        <div class="sidebar-caption">Modules</div>
        <button
          v-for="item in menuItems"
          :key="item.path"
          class="sidebar-link"
          :class="{ 'sidebar-link--active': activeMenu === item.path }"
          type="button"
          @click="router.push(item.path)"
        >
          <span class="sidebar-link__title">{{ item.label }}</span>
          <span class="sidebar-link__desc">{{ item.desc }}</span>
        </button>

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
              placeholder="选择知识空间"
              @change="handleSpaceChange"
            >
              <el-option v-for="space in workspaceStore.spaces" :key="space.id" :label="space.name" :value="space.id" />
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
  transition: grid-template-columns 0.24s ease;
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

.sidebar-link {
  width: 100%;
  display: grid;
  gap: 4px;
  text-align: left;
  margin-bottom: 8px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: transparent;
  color: var(--sidebar-text);
  cursor: pointer;
}

.sidebar-link:hover {
  background: var(--sidebar-hover);
}

.sidebar-link--active {
  border-color: var(--bmw-blue);
  background: var(--bmw-blue);
  color: var(--white);
}

.sidebar-link__title {
  font-size: 14px;
  font-weight: 900;
  letter-spacing: 0.03em;
}

.sidebar-link__desc {
  color: var(--sidebar-text-muted);
  font-size: 11px;
  line-height: 1.4;
}

.sidebar-link--active .sidebar-link__desc {
  color: rgba(255, 255, 255, 0.76);
}

.console-shell--collapsed .sidebar-caption,
.console-shell--collapsed .sidebar-link__desc {
  display: none;
}

.console-shell--collapsed .sidebar-link {
  place-items: center;
  padding: 14px 8px;
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

.page-header__row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
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

.space-switcher {
  width: 260px;
  margin-top: 6px;
}

.layout-content {
  flex: 1;
  min-height: 0;
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

  .page-header__row {
    flex-direction: column;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .space-switcher {
    width: 100%;
  }

  .layout-content {
    padding: 12px 16px 16px;
  }
}
</style>
