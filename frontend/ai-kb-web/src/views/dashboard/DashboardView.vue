<script setup>
import { computed, onMounted } from "vue";

import { useWorkspaceStore } from "../../stores/workspace";

const workspaceStore = useWorkspaceStore();

const scenarioCards = [
  {
    title: "客户支持知识助手",
    desc: "沉淀 FAQ、工单 SOP、故障处理流程，帮助客服和售后团队快速检索标准答案。",
    metric: "Support / Service",
  },
  {
    title: "售前与实施交付知识库",
    desc: "统一管理产品资料、标书答疑、实施手册和交付 checklist，降低经验依赖。",
    metric: "Sales / Delivery",
  },
  {
    title: "制度与合规政策问答",
    desc: "用于企业制度、权限规范、审批规则和操作边界查询，适合要求可追溯引用的场景。",
    metric: "Policy / Compliance",
  },
];

const healthCards = computed(() => [
  {
    label: "已完成入库",
    value: workspaceStore.overview.ready_count,
    meta: "文档完成解析并可参与检索",
  },
  {
    label: "处理中",
    value: workspaceStore.overview.processing_count,
    meta: "正在进行解析或向量入库",
  },
  {
    label: "失败文档",
    value: workspaceStore.overview.failed_count,
    meta: "建议优先排查格式或解析链路",
  },
  {
    label: "切片总量",
    value: workspaceStore.overview.chunk_count,
    meta: `平均每篇 ${workspaceStore.overview.avg_chunks_per_document} 个切片`,
  },
]);

onMounted(async () => {
  await Promise.all([workspaceStore.loadOverview(), workspaceStore.loadSpaces(), workspaceStore.loadRecentActivity()]);
});
</script>

<template>
  <div class="page-shell">
    <section class="hero-card dashboard-hero">
      <div class="dashboard-hero__main">
        <span class="page-eyebrow">Enterprise RAG</span>
        <h2>把知识空间、文档治理、检索问答和引用追踪做成可运营的企业级后台。</h2>
        <p class="page-subtitle">
          当前系统支持 PDF / DOCX 文档入库、切片预览、向量检索、引用来源展示、知识空间权限控制和检索策略配置，
          已具备企业内部知识助手的核心骨架。
        </p>
      </div>

      <div class="dashboard-hero__aside">
        <div class="hero-aside__label">Knowledge Coverage</div>
        <div class="hero-aside__value">{{ Math.round((workspaceStore.overview.coverage_ratio || 0) * 100) }}%</div>
        <div class="hero-aside__meta">
          向量库模式：{{ workspaceStore.overview.vector_store_mode }} · 共享文档 {{ workspaceStore.overview.shared_count }} 份
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <article class="metric-card">
        <div class="metric-label">知识空间</div>
        <div class="metric-value">{{ workspaceStore.overview.space_count }}</div>
        <div class="metric-meta">按业务域划分的知识资产空间</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">文档总量</div>
        <div class="metric-value">{{ workspaceStore.overview.document_count }}</div>
        <div class="metric-meta">已接入系统并参与治理的文档</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">问答会话</div>
        <div class="metric-value">{{ workspaceStore.overview.conversation_count }}</div>
        <div class="metric-meta">用于验证检索质量与业务问答效果</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Business Scenarios</span>
            <h2>更像企业真场景的知识库方向</h2>
          </div>
        </div>
        <div class="scenario-list">
          <article v-for="card in scenarioCards" :key="card.title" class="scenario-item">
            <div class="scenario-item__top">
              <strong>{{ card.title }}</strong>
              <span>{{ card.metric }}</span>
            </div>
            <p>{{ card.desc }}</p>
          </article>
        </div>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Pipeline Health</span>
            <h2>入库链路健康度</h2>
          </div>
        </div>
        <div class="health-grid">
          <article v-for="card in healthCards" :key="card.label" class="health-item">
            <div class="health-item__value">{{ card.value }}</div>
            <div class="health-item__label">{{ card.label }}</div>
            <div class="health-item__meta">{{ card.meta }}</div>
          </article>
        </div>
      </article>
    </section>

    <section class="dashboard-grid dashboard-grid--activity">
      <article class="table-card">
        <div class="table-header">
          <div>
            <h3>最近文档活动</h3>
            <p>关注文档的解析状态、共享范围和切片规模。</p>
          </div>
        </div>
        <el-table :data="workspaceStore.recentActivity.documents" style="width: 100%">
          <el-table-column prop="file_name" label="文档" min-width="220" />
          <el-table-column prop="parse_status" label="状态" width="120" />
          <el-table-column prop="visibility_scope" label="可见范围" width="120" />
          <el-table-column prop="chunk_count" label="切片数" width="100" />
          <el-table-column prop="created_at" label="创建时间" min-width="180" />
        </el-table>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Conversation Pulse</span>
            <h2>最近会话与切片信号</h2>
          </div>
        </div>

        <div class="activity-stack">
          <div class="mini-section">
            <strong>最近会话</strong>
            <div v-if="workspaceStore.recentActivity.conversations.length === 0" class="placeholder-copy">
              还没有会话数据，创建一个问答会话后这里会显示最近的业务提问记录。
            </div>
            <div v-else class="mini-list">
              <article
                v-for="item in workspaceStore.recentActivity.conversations"
                :key="item.id"
                class="mini-item"
              >
                <div class="mini-item__title">{{ item.title }}</div>
                <div class="mini-item__meta">space #{{ item.space_id }} · {{ item.updated_at }}</div>
              </article>
            </div>
          </div>

          <div class="mini-section">
            <strong>最近切片</strong>
            <div v-if="workspaceStore.recentActivity.chunks.length === 0" class="placeholder-copy">
              文档解析完成后，这里会展示最近入库的切片信号。
            </div>
            <div v-else class="mini-list">
              <article v-for="item in workspaceStore.recentActivity.chunks" :key="item.id" class="mini-item">
                <div class="mini-item__title">{{ item.section_path || "未命名章节" }}</div>
                <div class="mini-item__meta">
                  doc #{{ item.document_id }} · page {{ item.page_no || "-" }} · token {{ item.token_estimate }}
                </div>
              </article>
            </div>
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
  margin: 10px 0 8px;
  font-size: clamp(24px, 3vw, 34px);
  line-height: 1.02;
  letter-spacing: -0.06em;
}

.dashboard-hero__aside {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 16px;
  border-radius: var(--radius-lg);
  color: var(--text-inverse);
  background:
    radial-gradient(circle at top right, rgba(255, 214, 102, 0.3), transparent 34%),
    linear-gradient(180deg, #162033, #131b28 65%, #123252);
}

.hero-aside__label {
  color: rgba(226, 232, 240, 0.72);
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-aside__value {
  margin-top: 10px;
  font-size: 42px;
  font-weight: 800;
  line-height: 0.95;
  letter-spacing: -0.06em;
}

.hero-aside__meta {
  margin-top: 8px;
  color: rgba(226, 232, 240, 0.74);
  line-height: 1.7;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(340px, 1fr);
  gap: var(--space-4);
}

.dashboard-grid--activity {
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.95fr);
}

.scenario-list,
.activity-stack {
  display: grid;
  gap: var(--space-3);
  margin-top: 12px;
}

.scenario-item,
.health-item,
.mini-item {
  padding: 12px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
}

.scenario-item__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.scenario-item__top span {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 214, 102, 0.22);
  color: #8b5c00;
  font-size: 12px;
  font-weight: 700;
}

.scenario-item p {
  margin: 10px 0 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.health-item__value {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.05em;
}

.health-item__label {
  margin-top: 6px;
  font-weight: 700;
}

.health-item__meta {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
  padding: 14px 16px 10px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
}

.table-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.mini-section {
  display: grid;
  gap: 12px;
}

.mini-section strong {
  font-size: 14px;
}

.mini-list {
  display: grid;
  gap: 10px;
}

.mini-item__title {
  font-weight: 700;
}

.mini-item__meta,
.placeholder-copy {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .dashboard-hero,
  .dashboard-grid,
  .dashboard-grid--activity {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .health-grid {
    grid-template-columns: 1fr;
  }
}
</style>
