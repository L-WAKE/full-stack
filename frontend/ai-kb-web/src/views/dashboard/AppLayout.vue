<script setup>
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

import { useAuthStore } from "../../stores/auth";
import { useWorkspaceStore } from "../../stores/workspace";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const workspaceStore = useWorkspaceStore();

const menuItems = [
  { path: "/dashboard", label: "总览" },
  { path: "/spaces", label: "知识库空间" },
  { path: "/documents", label: "文档中心" },
  { path: "/chat", label: "检索问答" },
  { path: "/settings", label: "模型设置" },
];

const activeMenu = computed(() => route.path);
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

function logout() {
  authStore.logout();
  router.push("/login");
}
</script>

<template>
  <el-container class="page-shell app-shell">
    <el-aside width="272px" class="sidebar">
      <div>
        <div class="brand-wrap">
          <div class="brand-mark">AI</div>
          <div>
            <div class="brand">AI KB Console</div>
            <div class="brand-sub">房产业务知识中台</div>
          </div>
        </div>

        <el-menu :default-active="activeMenu" class="menu">
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
            @click="router.push(item.path)"
          >
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="profile-panel">
        <div class="profile-title">{{ authStore.profile?.display_name || "未登录" }}</div>
        <div class="profile-role">系统角色：{{ authStore.profile?.role || "-" }}</div>
        <div class="profile-role subtle">当前空间角色：{{ currentSpaceRole }}</div>
        <div class="profile-hint">owner 可以管成员和文档权限，member 仅能操作自己上传的文档。</div>
        <el-button link type="primary" class="logout-btn" @click="logout">退出登录</el-button>
      </div>
    </el-aside>

    <el-container class="content-shell">
      <el-header class="header">
        <div>
          <div class="header-title">{{ route.name }}</div>
          <div class="header-sub">围绕上传、解析、切片与检索，持续补齐真实 RAG 业务闭环</div>
        </div>
        <div class="header-actions">
          <el-select
            v-model="workspaceStore.activeSpaceId"
            placeholder="选择知识库空间"
            style="width: 280px"
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
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  background:
    radial-gradient(circle at top left, rgba(33, 106, 255, 0.1), transparent 26%),
    radial-gradient(circle at 85% 12%, rgba(17, 168, 132, 0.13), transparent 22%),
    linear-gradient(180deg, #f6f8fc 0%, #edf3fb 100%);
}

.sidebar {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px 18px;
  background:
    radial-gradient(circle at top, rgba(90, 132, 255, 0.16), transparent 22%),
    linear-gradient(180deg, #122344 0%, #162b55 52%, #101d39 100%);
  color: #f6f9ff;
  box-shadow: 18px 0 40px rgba(16, 29, 57, 0.18);
}

.brand-wrap {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 18px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  background: linear-gradient(135deg, #70a1ff, #67e8c8);
  color: #0d2146;
}

.brand {
  font-size: 18px;
  font-weight: 800;
}

.brand-sub {
  margin-top: 6px;
  font-size: 14px;
  color: rgba(244, 248, 255, 0.82);
}

.menu {
  margin-top: 18px;
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  height: 48px;
  margin: 8px 0;
  border-radius: 14px;
  color: rgba(241, 246, 255, 0.9);
  font-size: 15px;
  font-weight: 600;
  background: transparent;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(76, 129, 255, 0.24), rgba(70, 228, 194, 0.18));
  color: #ffffff;
}

.profile-panel {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.profile-title {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
}

.profile-role {
  margin-top: 8px;
  color: rgba(244, 248, 255, 0.88);
}

.profile-hint {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(244, 248, 255, 0.72);
}

.subtle {
  font-size: 13px;
}

.logout-btn {
  margin-top: 10px;
}

.content-shell {
  padding: 18px 18px 20px 8px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 26px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(213, 222, 236, 0.8);
  backdrop-filter: blur(14px);
  box-shadow: 0 18px 45px rgba(31, 55, 88, 0.08);
}

.header-title {
  font-size: 28px;
  font-weight: 800;
  color: #17325c;
  text-transform: capitalize;
}

.header-sub {
  margin-top: 6px;
  color: #6780a5;
  font-size: 15px;
}

.main-content {
  padding: 18px 0 0;
}
</style>
