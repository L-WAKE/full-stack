<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";

import {
  fetchModels,
  fetchRetrievalStrategy,
  updateChatModel,
  updateRetrievalStrategy,
} from "../../api/settings";

const loading = ref(false);
const saving = ref(false);
const models = ref([]);
const strategyForm = reactive({
  profile_name: "enterprise-balanced",
  top_k: 5,
  chunk_max_chars: 700,
  chunk_overlap: 120,
  rerank_enabled: false,
  citation_required: true,
  guardrail_enabled: true,
  answer_language: "zh-CN",
  description: "",
});
const activeChatModel = ref("");

const groupedModels = computed(() => {
  return {
    chat: models.value.filter((item) => item.type === "chat"),
    embedding: models.value.filter((item) => item.type === "embedding"),
    rerank: models.value.filter((item) => item.type === "rerank"),
  };
});

async function loadPage() {
  loading.value = true;
  try {
    const [{ data: modelData }, { data: strategyData }] = await Promise.all([
      fetchModels(),
      fetchRetrievalStrategy(),
    ]);
    models.value = modelData;
    activeChatModel.value = modelData.find((item) => item.type === "chat" && item.is_default)?.name || "";
    Object.assign(strategyForm, strategyData);
  } finally {
    loading.value = false;
  }
}

async function saveChatModel() {
  if (!activeChatModel.value) return;
  await updateChatModel(activeChatModel.value);
  ElMessage.success("默认聊天模型已更新");
  await loadPage();
}

async function saveStrategy() {
  saving.value = true;
  try {
    await updateRetrievalStrategy({ ...strategyForm });
    ElMessage.success("检索策略已保存");
  } finally {
    saving.value = false;
  }
}

onMounted(loadPage);
</script>

<template>
  <div class="page-shell" v-loading="loading">
    <section class="page-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">AI Strategy</span>
          <h2>模型与检索策略中心</h2>
          <p class="page-subtitle">
            用更贴近企业生产环境的方式配置聊天模型、Embedding 方案和 RAG 检索参数，
            为知识助手在客服、售前、交付、制度问答等场景落地做准备。
          </p>
        </div>
      </div>
    </section>

    <section class="setting-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Chat Model</span>
            <h2>聊天模型选型</h2>
          </div>
        </div>
        <el-radio-group v-model="activeChatModel" class="model-list">
          <label v-for="item in groupedModels.chat" :key="item.name" class="model-card">
            <el-radio :value="item.name">{{ item.name }}</el-radio>
            <div class="model-card__meta">
              <span>{{ item.provider }}</span>
              <span>{{ item.cost_level }}</span>
              <span>{{ item.status }}</span>
            </div>
            <p>{{ item.scenario }}</p>
          </label>
        </el-radio-group>
        <el-button type="primary" @click="saveChatModel">保存聊天模型</el-button>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Embedding & Rerank</span>
            <h2>检索基础能力选型</h2>
          </div>
        </div>
        <div class="stack-list">
          <div class="stack-block">
            <strong>Embedding 候选</strong>
            <article v-for="item in groupedModels.embedding" :key="item.name" class="info-card">
              <div class="info-card__title">{{ item.name }}</div>
              <div class="info-card__meta">{{ item.provider }} · {{ item.cost_level }} · {{ item.status }}</div>
              <p>{{ item.scenario }}</p>
            </article>
          </div>

          <div class="stack-block">
            <strong>Rerank 能力</strong>
            <article v-for="item in groupedModels.rerank" :key="item.name" class="info-card">
              <div class="info-card__title">{{ item.name }}</div>
              <div class="info-card__meta">{{ item.provider }} · {{ item.cost_level }} · {{ item.status }}</div>
              <p>{{ item.scenario }}</p>
            </article>
          </div>
        </div>
      </article>
    </section>

    <article class="panel-card">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Retrieval Profile</span>
          <h2>企业知识库检索策略</h2>
          <p class="page-subtitle">
            这部分决定文档切片规模、召回条数、是否强制引用与是否开启安全护栏，直接影响企业问答质量和可追溯性。
          </p>
        </div>
      </div>

      <el-form label-position="top" class="strategy-form">
        <el-form-item label="策略名称">
          <el-input v-model="strategyForm.profile_name" />
        </el-form-item>
        <el-form-item label="策略说明">
          <el-input v-model="strategyForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="召回 Top K">
          <el-slider v-model="strategyForm.top_k" :min="1" :max="10" show-input />
        </el-form-item>
        <el-form-item label="切片长度">
          <el-slider v-model="strategyForm.chunk_max_chars" :min="300" :max="1200" :step="50" show-input />
        </el-form-item>
        <el-form-item label="切片重叠">
          <el-slider v-model="strategyForm.chunk_overlap" :min="0" :max="240" :step="20" show-input />
        </el-form-item>
        <div class="toggle-grid">
          <el-switch
            v-model="strategyForm.citation_required"
            active-text="强制引用来源"
            inactive-text="允许摘要回答"
          />
          <el-switch
            v-model="strategyForm.guardrail_enabled"
            active-text="开启安全护栏"
            inactive-text="关闭安全护栏"
          />
          <el-switch
            v-model="strategyForm.rerank_enabled"
            active-text="预留 Rerank"
            inactive-text="关闭 Rerank"
          />
        </div>
        <el-form-item label="回答语言">
          <el-select v-model="strategyForm.answer_language">
            <el-option label="中文（zh-CN）" value="zh-CN" />
            <el-option label="English (en-US)" value="en-US" />
          </el-select>
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="saveStrategy">保存检索策略</el-button>
      </el-form>
    </article>
  </div>
</template>

<style scoped>
.setting-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.model-list,
.stack-list,
.strategy-form {
  display: grid;
  gap: var(--space-3);
  margin-top: 18px;
}

.model-card,
.info-card {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
}

.model-card {
  cursor: pointer;
}

.model-card__meta,
.info-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

.model-card p,
.info-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

.info-card__title {
  font-weight: 700;
}

.stack-block {
  display: grid;
  gap: 10px;
}

.toggle-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 1100px) {
  .setting-grid {
    grid-template-columns: 1fr;
  }

  .toggle-grid {
    grid-template-columns: 1fr;
  }
}
</style>
