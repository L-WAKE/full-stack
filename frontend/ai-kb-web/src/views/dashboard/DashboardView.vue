<script setup>
import { computed, onMounted } from "vue";

import { useWorkspaceStore } from "../../stores/workspace";

const workspaceStore = useWorkspaceStore();

const scenarioCards = [
  {
    title: "客户支持知识助手",
    desc: "沉淀 FAQ、工单 SOP、故障处理流程和统一口径，让客服和售后团队快速获得标准答案。",
    metric: "Support / Service",
  },
  {
    title: "售前与交付知识库",
    desc: "统一管理产品资料、标书答疑、实施手册和交付清单，降低经验依赖，提高协作稳定性。",
    metric: "Sales / Delivery",
  },
  {
    title: "制度与合规问答",
    desc: "面向权限规范、审批边界、内部制度和审计要求，提供带引用的可追溯问答能力。",
    metric: "Policy / Compliance",
  },
];

const healthCards = computed(() => [
  {
    label: "已完成入库",
    value: workspaceStore.overview.ready_count,
    meta: "文档完成解析并可参与检索。",
  },
  {
    label: "处理中",
    value: workspaceStore.overview.processing_count,
    meta: "正在进行解析或向量入库。",
  },
  {
    label: "失败文档",
    value: workspaceStore.overview.failed_count,
    meta: "建议优先排查格式或解析链路。",
  },
  {
    label: "切片总量",
    value: workspaceStore.overview.chunk_count,
    meta: `平均每篇 ${workspaceStore.overview.avg_chunks_per_document} 个切片。`,
  },
]);

onMounted(async () => {
  await Promise.all([
    workspaceStore.loadOverview(),
    workspaceStore.loadSpaces(),
    workspaceStore.loadRecentActivity(),
  ]);
});
</script>

<template>
  <div class="page-shell">
    <section class="hero-card dashboard-hero">
      <div class="dashboard-hero__main">
        <span class="page-eyebrow">Enterprise RAG</span>
        <h2>把知识空间、文档治理、检索问答和引用追踪做成可运营的企业级后台。</h2>
        <p class="page-subtitle">
          当前系统支持 PDF / DOCX 入库、切片预览、向量检索、引用来源展示、知识空间权限控制和模型策略配置，
          适合客服、售前、交付、制度问答等真实业务场景。
        </p>
      </div>

      <div class="dashboard-hero__aside">
        <div class="hero-aside__label">Knowledge Coverage</div>
        <div class="hero-aside__value">{{ Math.round((workspaceStore.overview.coverage_ratio || 0) * 100) }}%</div>
        <div class="hero-aside__meta">
          向量模式 {{ workspaceStore.overview.vector_store_mode }} / 共享文档 {{ workspaceStore.overview.shared_count }} 份
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <article class="metric-card">
        <div class="metric-label">知识空间</div>
        <div class="metric-value">{{ workspaceStore.overview.space_count }}</div>
        <div class="metric-meta">按业务域组织的知识资产空间。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">文档总量</div>
        <div class="metric-value">{{ workspaceStore.overview.document_count }}</div>
        <div class="metric-meta">已接入系统并参与治理的文档。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">问答会话</div>
        <div class="metric-value">{{ workspaceStore.overview.conversation_count }}</div>
        <div class="metric-meta">用于验证检索质量和业务答复效果。</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Business Scenarios</span>
            <h2>更像企业真实场景的知识库方向</h2>
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
              还没有会话数据，创建一次问答后这里会展示最近的业务提问记录。
            </div>
            <div v-else class="mini-list">
              <article v-for="item in workspaceStore.recentActivity.conversations" :key="item.id" class="mini-item">
                <div class="mini-item__title">{{ item.title }}</div>
                <div class="mini-item__meta">space #{{ item.space_id }} / {{ item.updated_at }}</div>
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
                  doc #{{ item.document_id }} / page {{ item.page_no || "-" }} / token {{ item.token_estimate }}
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
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.8fr);
  gap: var(--space-4);
  padding: 28px;
}

.dashboard-hero h2 {
  margin: 10px 0 8px;
  font-size: clamp(30px, 4vw, 52px);
  font-weight: 300;
  line-height: 1.06;
  text-transform: uppercase;
}

.dashboard-hero__aside {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 18px;
  color: var(--white);
  background: var(--dark);
}

.hero-aside__label {
  color: var(--silver);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-aside__value {
  margin-top: 10px;
  font-size: 56px;
  font-weight: 300;
  line-height: 0.95;
}

.hero-aside__meta {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.6;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 1fr);
  gap: var(--space-4);
}

.dashboard-grid--activity {
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.95fr);
}

.scenario-list,
.activity-stack {
  display: grid;
  gap: var(--space-3);
  margin-top: 16px;
}

.scenario-item,
.health-item,
.mini-item {
  padding: 14px;
  border: 1px solid var(--border);
  background: var(--white);
}

.scenario-item__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.scenario-item__top strong,
.health-item__label,
.mini-item__title {
  font-weight: 900;
}

.scenario-item__top span {
  color: var(--bmw-blue);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.scenario-item p,
.health-item__meta,
.mini-item__meta,
.placeholder-copy {
  margin-top: 8px;
  color: var(--text-muted);
  line-height: 1.6;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.health-item__value {
  font-size: 34px;
  font-weight: 300;
  line-height: 1;
}

.table-header {
  padding: 18px 20px 10px;
  border-bottom: 1px solid var(--border);
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 900;
  text-transform: uppercase;
}

.table-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.mini-section {
  display: grid;
  gap: 12px;
}

.mini-section strong {
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
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
