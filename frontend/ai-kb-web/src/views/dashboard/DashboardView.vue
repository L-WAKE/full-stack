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
  <div class="page-shell">
    <section class="hero-card dashboard-hero">
      <div class="dashboard-hero__main">
        <span class="page-eyebrow">知识总览</span>
        <h2>用更稳定、更清晰的方式掌控知识空间、文档入库与检索问答全局。</h2>
        <p class="page-subtitle">
          将知识空间、文档解析、切片结果和问答会话统一收敛到一个后台工作台里，帮助团队更快识别状态、定位问题并推进迭代。
        </p>
      </div>

      <div class="dashboard-hero__aside">
        <div class="hero-aside__label">空间快照</div>
        <div class="hero-aside__value">{{ workspaceStore.overview.space_count }}</div>
        <div class="hero-aside__meta">当前已接入并可访问的知识空间数量</div>
      </div>
    </section>

    <section class="metric-strip">
      <article class="metric-card">
        <div class="metric-label">知识空间</div>
        <div class="metric-value">{{ workspaceStore.overview.space_count }}</div>
        <div class="metric-meta">用于沉淀业务主题与协作边界</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">文档总量</div>
        <div class="metric-value">{{ workspaceStore.overview.document_count }}</div>
        <div class="metric-meta">已接入系统并参与入库流程的文档</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">会话数量</div>
        <div class="metric-value">{{ workspaceStore.overview.conversation_count }}</div>
        <div class="metric-meta">问答沉淀与问题追踪的历史会话</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Current Stage</span>
            <h2>当前开发阶段</h2>
            <p class="page-subtitle">
              系统已经完成基础登录、空间管理、文档上传、解析触发、切片预览和检索问答链路，适合作为真实 RAG 业务后台继续往生产方向推进。
            </p>
          </div>
        </div>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Next Focus</span>
            <h2>下一步重点</h2>
          </div>
        </div>
        <div class="note-list">
          <div class="note-card">
            <div class="note-card__title">向量服务</div>
            <div class="note-card__desc">补齐正式向量链路，让检索召回与入库流程完全落到稳定环境中。</div>
          </div>
          <div class="note-card">
            <div class="note-card__title">异步任务</div>
            <div class="note-card__desc">把解析、切片和向量化升级为后台任务，降低前台同步等待成本。</div>
          </div>
          <div class="note-card">
            <div class="note-card__title">权限细化</div>
            <div class="note-card__desc">补齐成员角色、文档可见范围和检索范围的更细粒度控制。</div>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(280px, 0.85fr);
  gap: var(--space-4);
}

.dashboard-hero h2 {
  margin: 18px 0 12px;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.02;
  letter-spacing: -0.05em;
}

.dashboard-hero__aside {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 22px;
  border-radius: var(--radius-lg);
  color: var(--text-inverse);
  background:
    radial-gradient(circle at top right, rgba(79, 146, 255, 0.42), transparent 30%),
    linear-gradient(180deg, #162033, #11161d 65%, #16283f);
}

.hero-aside__label {
  color: rgba(226, 232, 240, 0.72);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-aside__value {
  margin-top: 14px;
  font-size: 64px;
  font-weight: 800;
  line-height: 0.95;
  letter-spacing: -0.06em;
}

.hero-aside__meta {
  margin-top: 12px;
  color: rgba(226, 232, 240, 0.74);
  line-height: 1.65;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 1fr);
  gap: var(--space-4);
}

.note-list {
  display: grid;
  gap: var(--space-3);
  margin-top: 18px;
}

.note-card {
  padding: 16px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
}

.note-card__title {
  font-size: 16px;
  font-weight: 700;
}

.note-card__desc {
  margin-top: 8px;
  color: var(--text-muted);
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .dashboard-hero,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
