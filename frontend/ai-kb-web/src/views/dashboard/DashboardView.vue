<script setup>
import { onMounted } from "vue";

import { useWorkspaceStore } from "../../stores/workspace";

const workspaceStore = useWorkspaceStore();

onMounted(async () => {
  await workspaceStore.loadOverview();
  await workspaceStore.loadSpaces();
});
</script>

<template>
  <div class="dashboard-grid">
    <el-card shadow="never" class="metric-card">
      <div class="metric-kicker">Knowledge Spaces</div>
      <div class="metric-value">{{ workspaceStore.overview.space_count }}</div>
      <div class="metric-label">当前可访问知识库空间</div>
    </el-card>

    <el-card shadow="never" class="metric-card accent-blue">
      <div class="metric-kicker">Documents</div>
      <div class="metric-value">{{ workspaceStore.overview.document_count }}</div>
      <div class="metric-label">已接入文档总数</div>
    </el-card>

    <el-card shadow="never" class="metric-card accent-green">
      <div class="metric-kicker">Conversations</div>
      <div class="metric-value">{{ workspaceStore.overview.conversation_count }}</div>
      <div class="metric-label">会话沉淀与问题追踪</div>
    </el-card>

    <el-card shadow="never" class="wide-card">
      <div class="card-title">当前开发阶段</div>
      <div class="stage-copy">
        <p>系统已经完成基础登录、空间管理、文档上传、解析触发、切片预览和检索问答闭环。</p>
        <p>当前后端已切换到 PostgreSQL 正式数据库运行，Redis 已就绪，Qdrant 仍在补本机安装链路。</p>
      </div>
    </el-card>

    <el-card shadow="never" class="wide-card secondary-card">
      <div class="card-title">下一步重点</div>
      <div class="todo-grid">
        <div class="todo-item">
          <strong>向量服务</strong>
          <span>补齐 Qdrant 本机安装与向量入库，让检索彻底切到正式链路。</span>
        </div>
        <div class="todo-item">
          <strong>异步任务</strong>
          <span>把解析、切片和向量化升级为后台任务，而不是同步接口执行。</span>
        </div>
        <div class="todo-item">
          <strong>权限细化</strong>
          <span>补齐成员角色、文档可见范围和检索范围的更细粒度控制。</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.metric-card,
.wide-card {
  border-radius: 24px;
  border: 1px solid rgba(214, 223, 237, 0.9);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 18px 40px rgba(30, 54, 88, 0.08);
}

.metric-card {
  min-height: 190px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    radial-gradient(circle at top right, rgba(76, 129, 255, 0.12), transparent 28%),
    rgba(255, 255, 255, 0.86);
}

.accent-blue {
  background:
    radial-gradient(circle at top right, rgba(76, 129, 255, 0.18), transparent 30%),
    rgba(255, 255, 255, 0.86);
}

.accent-green {
  background:
    radial-gradient(circle at top right, rgba(46, 204, 153, 0.18), transparent 30%),
    rgba(255, 255, 255, 0.86);
}

.metric-kicker {
  color: #7890b4;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-value {
  margin-top: 12px;
  font-size: 56px;
  line-height: 1;
  font-weight: 800;
  color: #1755f5;
}

.metric-label {
  margin-top: 12px;
  color: #5e7598;
  font-size: 16px;
}

.wide-card {
  grid-column: 1 / -1;
}

.stage-copy p {
  margin: 12px 0 0;
  color: #3d5478;
  font-size: 17px;
  line-height: 1.75;
}

.secondary-card {
  background:
    radial-gradient(circle at bottom left, rgba(17, 168, 132, 0.08), transparent 24%),
    rgba(255, 255, 255, 0.82);
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.todo-item {
  padding: 18px;
  border-radius: 18px;
  background: #f5f8fd;
  border: 1px solid #e1e8f4;
}

.todo-item strong {
  display: block;
  margin-bottom: 8px;
  color: #17325c;
}

.todo-item span {
  color: #617a9d;
  line-height: 1.6;
}
</style>
