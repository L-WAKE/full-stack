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
  const display = item.created_by_name || item.created_by_username || "未知上传者";
  if (item.created_by_username && item.created_by_name) return `${display} / ${item.created_by_username}`;
  return display;
}

function visibilityLabel(scope) {
  return scope === "PRIVATE" ? "仅自己可见" : "空间可见";
}

async function createNewConversation() {
  if (!workspaceStore.activeSpaceId) return;
  const { data } = await createConversation({
    space_id: workspaceStore.activeSpaceId,
    title: `会话 ${new Date().toLocaleString()}`,
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

async function submit() {
  if (!draft.value || !activeConversationId.value) return;
  retrieving.value = true;
  await sendMessage(activeConversationId.value, { content: draft.value });
  await workspaceStore.loadMessages(activeConversationId.value);
  const content = draft.value;
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
    ElMessage.warning("流式回答已结束。");
  };
}
</script>

<template>
  <div class="page-shell">
    <section class="page-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Retrieval QA</span>
          <h2>检索问答</h2>
          <p class="page-subtitle">基于当前知识空间发起问答，查看会话、检索命中片段与引用来源。</p>
        </div>
      </div>
    </section>

    <section class="chat-grid">
      <article class="panel-card conversation-panel">
        <div class="table-header table-header--tight">
          <div>
            <h3>会话列表</h3>
            <p>管理当前空间下的历史问答会话。</p>
          </div>
          <el-button type="primary" plain @click="createNewConversation">新建会话</el-button>
        </div>

        <div class="conversation-list">
          <button
            v-for="conversation in workspaceStore.conversations"
            :key="conversation.id"
            class="conversation-item"
            :class="{ 'conversation-item--active': activeConversationId === conversation.id }"
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
            <h3>问答区</h3>
            <p>发起问题并查看实时流式回答。</p>
          </div>
        </div>

        <el-alert v-if="retrievalSummary" type="info" :closable="false" show-icon>
          <template #title>
            已命中 {{ retrievalSummary.count }} 条片段，当前检索模式：{{ retrievalSummary.mode }}
          </template>
        </el-alert>

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
          <el-input v-model="draft" type="textarea" :rows="4" placeholder="基于当前空间发起提问" />
          <el-button type="primary" :disabled="!activeConversationId" :loading="retrieving" @click="submit">
            发送
          </el-button>
        </div>
      </article>

      <article class="panel-card citation-panel">
        <div class="table-header table-header--tight">
          <div>
            <h3>引用来源</h3>
            <p>展示检索命中片段与最终回答引用内容。</p>
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
              <span>上传者：{{ uploaderLabel(item) }}</span>
              <span>可见范围：{{ visibilityLabel(item.visibility_scope) }}</span>
              <span>分段：{{ item.section_path || "-" }}</span>
              <span>页码：{{ item.page_no || "-" }}</span>
              <span>score：{{ Number(item.score).toFixed(3) }}</span>
            </div>
            <div>{{ item.content_preview }}</div>
          </article>
        </div>

        <div v-if="citations.length === 0" class="placeholder-copy">
          发起一次流式问答后，这里会显示文档、上传者、可见范围和引用摘要。
        </div>

        <div v-else class="citation-list">
          <article v-for="item in citations" :key="`${item.document_name}-${item.page_no}-${item.section_path}`" class="citation-item">
            <div class="citation-title">{{ item.document_name }}</div>
            <div class="citation-meta">
              <span>上传者：{{ uploaderLabel(item) }}</span>
              <span>可见范围：{{ visibilityLabel(item.visibility_scope) }}</span>
              <span>分段：{{ item.section_path || "-" }}</span>
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
  grid-template-columns: 280px minmax(0, 1fr) 340px;
  gap: var(--space-4);
}

.conversation-panel,
.chat-panel,
.citation-panel {
  min-height: 620px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
}

.table-header--tight {
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--line-color);
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
}

.table-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.conversation-list,
.citation-list {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.conversation-item,
.citation-item,
.message-item {
  width: 100%;
  text-align: left;
  padding: 14px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
  color: var(--text-main);
}

.conversation-item--active {
  border-color: rgba(23, 104, 255, 0.4);
  background: rgba(237, 244, 255, 0.96);
}

.conversation-item strong,
.conversation-item span {
  display: block;
}

.conversation-item span {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

.message-list {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.message-item {
  line-height: 1.75;
}

.message-item strong {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--primary);
}

.message-item--assistant {
  background: rgba(237, 244, 255, 0.96);
}

.composer {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.citation-title {
  margin-bottom: 8px;
  font-weight: 700;
}

.citation-meta {
  display: grid;
  gap: 4px;
  margin-bottom: 10px;
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

  .conversation-panel,
  .chat-panel,
  .citation-panel {
    min-height: auto;
  }
}
</style>
