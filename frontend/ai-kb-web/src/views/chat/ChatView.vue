<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";

import { createConversation, sendMessage } from "../../api/chat";
import { useWorkspaceStore } from "../../stores/workspace";

const workspaceStore = useWorkspaceStore();
const activeConversationId = ref(null);
const draft = ref("");
const streamText = ref("");
const citations = ref([]);
const retrieving = ref(false);

const sampleQuestions = [
  "客户咨询处理流程时，应该优先参考哪份 SOP？",
  "当前文档里有没有关于权限或审批边界的说明？",
  "实施交付前需要检查哪些事项？",
];

const retrievalSummary = computed(() => {
  const result = workspaceStore.retrievalResult;
  if (!result) return null;
  return {
    count: result.chunks?.length || 0,
    mode: result.retrieval_mode || "vector",
  };
});

onMounted(async () => {
  await workspaceStore.loadSpaces();
  await workspaceStore.loadConversations();
});

function uploaderLabel(item) {
  const display = item.created_by_name || item.created_by_username || "未知上传人";
  if (item.created_by_username && item.created_by_name) return `${display} / ${item.created_by_username}`;
  return display;
}

function visibilityLabel(scope) {
  return scope === "PRIVATE" ? "仅自己可见" : "空间共享";
}

async function createNewConversation() {
  if (!workspaceStore.activeSpaceId) return;
  const { data } = await createConversation({
    space_id: workspaceStore.activeSpaceId,
    title: `业务问答 ${new Date().toLocaleString()}`,
  });
  activeConversationId.value = data.id;
  await workspaceStore.loadConversations();
  await workspaceStore.loadMessages(data.id);
}

async function openConversation(conversationId) {
  activeConversationId.value = conversationId;
  streamText.value = "";
  citations.value = [];
  await workspaceStore.loadMessages(conversationId);
}

function useSample(question) {
  draft.value = question;
}

async function submit() {
  if (!draft.value || !activeConversationId.value) return;
  retrieving.value = true;
  const content = draft.value;
  await sendMessage(activeConversationId.value, { content });
  await workspaceStore.loadMessages(activeConversationId.value);
  await workspaceStore.runRetrieval(content);
  draft.value = "";
  streamText.value = "";
  citations.value = [];
  const token = localStorage.getItem("ai-kb-token");

  const source = new EventSource(
    `/api/chat/conversations/${activeConversationId.value}/stream?question=${encodeURIComponent(content)}&access_token=${encodeURIComponent(token || "")}`,
  );
  source.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.type === "token") streamText.value += data.delta;
    if (data.type === "citation") citations.value = data.items;
    if (data.type === "done") {
      retrieving.value = false;
      source.close();
    }
  };
  source.onerror = () => {
    retrieving.value = false;
    source.close();
    ElMessage.warning("流式回答已结束或连接中断");
  };
}
</script>

<template>
  <div class="page-shell">
    <section class="page-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Retrieval QA</span>
          <h2>可追溯的企业知识问答</h2>
          <p class="page-subtitle">
            先检索知识空间中的文档切片，再展示引用来源与流式答案，适合客服、售前、交付和制度查询场景。
          </p>
        </div>
      </div>
    </section>

    <section class="chat-grid">
      <article class="panel-card conversation-panel">
        <div class="table-header table-header--tight">
          <div>
            <h3>会话列表</h3>
            <p>按知识空间组织业务问答记录。</p>
          </div>
          <el-button type="primary" plain @click="createNewConversation">新建会话</el-button>
        </div>

        <div class="conversation-list">
          <button
            v-for="conversation in workspaceStore.conversations"
            :key="conversation.id"
            class="conversation-item"
            :class="{ 'conversation-item--active': activeConversationId === conversation.id }"
            type="button"
            @click="openConversation(conversation.id)"
          >
            <strong>{{ conversation.title }}</strong>
            <span>{{ conversation.updated_at }}</span>
          </button>
        </div>
      </article>

      <article class="panel-card chat-panel">
        <div class="table-header table-header--tight">
          <div>
            <h3>问答工作区</h3>
            <p>先召回上下文，再以流式方式输出答案和引用。</p>
          </div>
        </div>

        <el-alert v-if="retrievalSummary" type="info" :closable="false" show-icon>
          <template #title>
            已命中 {{ retrievalSummary.count }} 条片段 / 当前检索模式 {{ retrievalSummary.mode }}
          </template>
        </el-alert>

        <div class="sample-list">
          <button v-for="question in sampleQuestions" :key="question" type="button" @click="useSample(question)">
            {{ question }}
          </button>
        </div>

        <div class="message-list">
          <article v-for="message in workspaceStore.messages" :key="message.id" class="message-item">
            <strong>{{ message.role }}</strong>
            <div>{{ message.content }}</div>
          </article>
          <article v-if="streamText" class="message-item message-item--assistant">
            <strong>assistant</strong>
            <div>{{ streamText }}</div>
          </article>
        </div>

        <div class="composer">
          <el-input v-model="draft" type="textarea" :rows="4" placeholder="基于当前知识空间发起提问" />
          <el-button type="primary" :disabled="!activeConversationId" :loading="retrieving" @click="submit">
            发送并检索
          </el-button>
        </div>
      </article>

      <article class="panel-card citation-panel">
        <div class="table-header table-header--tight">
          <div>
            <h3>引用来源</h3>
            <p>展示命中文档、页码、上传人和片段摘要。</p>
          </div>
        </div>

        <div v-if="workspaceStore.retrievalResult?.chunks?.length" class="citation-list citation-list--search">
          <article
            v-for="item in workspaceStore.retrievalResult.chunks"
            :key="`${item.document_id}-${item.chunk_id}`"
            class="citation-item"
          >
            <div class="citation-title">{{ item.document_name }}</div>
            <div class="citation-meta">
              <span>上传人：{{ uploaderLabel(item) }}</span>
              <span>可见范围：{{ visibilityLabel(item.visibility_scope) }}</span>
              <span>章节：{{ item.section_path || "-" }}</span>
              <span>页码：{{ item.page_no || "-" }}</span>
              <span>score：{{ Number(item.score).toFixed(3) }}</span>
            </div>
            <div>{{ item.content_preview }}</div>
          </article>
        </div>

        <div v-if="citations.length === 0" class="placeholder-copy">
          发起一次问答后，这里会展示答案引用的文档来源，帮助业务人员判断答复是否可信。
        </div>

        <div v-else class="citation-list">
          <article
            v-for="item in citations"
            :key="`${item.document_name}-${item.page_no}-${item.section_path}`"
            class="citation-item"
          >
            <div class="citation-title">{{ item.document_name }}</div>
            <div class="citation-meta">
              <span>上传人：{{ uploaderLabel(item) }}</span>
              <span>可见范围：{{ visibilityLabel(item.visibility_scope) }}</span>
              <span>章节：{{ item.section_path || "-" }}</span>
              <span>页码：{{ item.page_no || "-" }}</span>
            </div>
            <div>{{ item.snippet }}</div>
          </article>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.chat-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 360px;
  gap: var(--space-4);
}

.conversation-panel,
.chat-panel,
.citation-panel {
  min-height: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
}

.table-header--tight {
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border);
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.table-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.conversation-list,
.citation-list,
.message-list,
.sample-list {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.sample-list {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.sample-list button,
.conversation-item,
.citation-item,
.message-item {
  width: 100%;
  text-align: left;
  padding: 12px;
  border: 1px solid var(--border);
  background: var(--white);
  color: var(--text-main);
}

.sample-list button {
  cursor: pointer;
}

.sample-list button:hover,
.conversation-item:hover {
  border-color: var(--bmw-blue);
}

.conversation-item--active,
.message-item--assistant {
  border-color: var(--bmw-blue);
  background: rgba(28, 105, 212, 0.04);
}

.conversation-item strong,
.conversation-item span {
  display: block;
}

.conversation-item strong,
.citation-title {
  font-weight: 900;
}

.conversation-item span {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.message-item {
  line-height: 1.6;
}

.message-item strong {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--bmw-blue);
  font-weight: 900;
  text-transform: uppercase;
}

.composer {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.citation-meta {
  display: grid;
  gap: 4px;
  margin: 8px 0 10px;
  color: var(--text-muted);
  font-size: 12px;
}

.placeholder-copy {
  margin-top: var(--space-4);
  color: var(--text-muted);
  line-height: 1.7;
}

@media (max-width: 1280px) {
  .chat-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .sample-list {
    grid-template-columns: 1fr;
  }
}
</style>
