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
  { path: "/dashboard", label: "总览", desc: "业务运营看板" },
  { path: "/spaces", label: "知识空间", desc: "业务域与权限边界" },
  { path: "/documents", label: "文档治理", desc: "上传、解析、切片、审计" },
  { path: "/chat", label: "检索问答", desc: "会话与引用来源" },
  { path: "/settings", label: "模型策略", desc: "模型与检索配置" },
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
    subtitle: "按业务域沉淀 SOP、产品 FAQ、客户支持手册、实施交付文档等企业核心知识资产。",
  },
  documents: {
    parent: "Knowledge Ops",
    title: "文档治理中心",
    subtitle: "统一管理企业文档上传、解析状态、切片质量、可见范围和入库健康度。",
  },
  chat: {
    parent: "Retrieval QA",
    title: "检索问答与引用追踪",
    subtitle: "面向运营、售前、客服和交付团队提供可追溯的知识问答能力。",
  },
  settings: {
    parent: "AI Strategy",
    title: "模型与检索策略",
    subtitle: "集中配置聊天模型、Embedding 方案和 RAG 检索参数，为生产化扩展预留能力。",
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
  return `${current.name} · ${current.document_count} docs · ${current.member_count} members`;
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
  await Promise.all([
    workspaceStore.loadDocuments(),
    workspaceStore.loadConversations(),
  ]);
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
        <div class="sidebar-block">
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
        </div>

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
  --sidebar-width: 236px;
  height: 100vh;
  overflow: hidden;
  color: var(--text-main);
  background: var(--bg-page);
}

.console-shell--collapsed {
  --sidebar-width: 72px;
}

.console-header {
  min-height: 58px;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) auto;
  gap: var(--space-4);
  align-items: center;
  padding: 10px 18px;
  color: var(--sidebar-text);
  background:
    radial-gradient(circle at top left, rgba(95, 166, 255, 0.18), transparent 24%),
    linear-gradient(120deg, #0f1723, #152233 60%, #112942);
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.08);
}

.console-header__brand,
.console-header__actions {
  display: flex;
  align-items: center;
}

.console-header__brand {
  gap: 14px;
  min-width: 0;
}

.header-logo {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff6d8, #ffd38a);
  color: #111827;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.header-title {
  color: #f8fbff;
  font-size: 15px;
  font-weight: 700;
}

.header-subtitle {
  margin-top: 2px;
  color: rgba(226, 232, 240, 0.72);
  font-size: 12px;
}

.console-header__actions {
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.header-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
}

.header-chip__label {
  color: rgba(226, 232, 240, 0.65);
  font-size: 11px;
}

.header-chip__value {
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.header-logout {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
}

.console-body {
  height: calc(100vh - 58px);
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  transition: grid-template-columns 0.24s ease;
}

.layout-sidebar {
  position: relative;
  min-width: 0;
  padding: 14px 10px 60px;
  background:
    radial-gradient(circle at top, rgba(61, 139, 255, 0.12), transparent 30%),
    linear-gradient(180deg, #11161d, #18212c);
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.06);
}

.sidebar-block {
  display: grid;
  gap: 8px;
}

.sidebar-caption {
  margin-bottom: 4px;
  padding: 0 8px;
  color: rgba(226, 232, 240, 0.55);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar-link {
  width: 100%;
  display: grid;
  gap: 2px;
  text-align: left;
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: 16px;
  color: rgba(236, 242, 249, 0.86);
  background: transparent;
  cursor: pointer;
  transition: transform 0.18s ease, background-color 0.18s ease, border-color 0.18s ease;
}

.sidebar-link:hover {
  transform: translateX(2px);
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-link--active {
  border-color: rgba(105, 174, 255, 0.28);
  background: linear-gradient(135deg, rgba(23, 104, 255, 0.3), rgba(61, 139, 255, 0.18));
  box-shadow: 0 16px 28px rgba(23, 104, 255, 0.18);
}

.sidebar-link__title {
  font-size: 14px;
  font-weight: 700;
}

.sidebar-link__desc {
  color: rgba(226, 232, 240, 0.6);
  font-size: 11px;
  line-height: 1.4;
}

.console-shell--collapsed .sidebar-caption,
.console-shell--collapsed .sidebar-link__desc {
  display: none;
}

.console-shell--collapsed .sidebar-link {
  padding: 12px 10px;
  place-items: center;
}

.sidebar-toggle {
  position: absolute;
  left: 50%;
  bottom: 18px;
  transform: translateX(-50%);
  width: 30px;
  height: 38px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  color: rgba(236, 242, 249, 0.86);
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
  padding: 14px 16px 10px;
}

.page-header__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--primary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.page-header__row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.page-header h1 {
  margin: 8px 0 6px;
  font-size: 22px;
  line-height: 1.04;
  letter-spacing: -0.05em;
}

.page-header p {
  max-width: 760px;
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.55;
}

.space-switcher {
  width: 248px;
  margin-top: 4px;
}

.layout-content {
  flex: 1;
  min-height: 0;
  padding: 0 16px 16px;
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
    padding: 14px 12px 10px;
  }

  .page-header__row {
    flex-direction: column;
  }

  .page-header h1 {
    font-size: 20px;
  }

  .space-switcher {
    width: 100%;
  }

  .layout-content {
    padding: 0 12px 12px;
  }
}
</style>
