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
  if (item.created_by_username && item.created_by_name) {
    return `${display} / ${item.created_by_username}`;
  }
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
    if (data.type === "token") {
      streamText.value += data.delta;
    }
    if (data.type === "citation") {
      citations.value = data.items;
    }
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
  <div class="chat-layout">
    <el-card shadow="never" class="conversation-pane">
      <div class="pane-header">
        <div class="card-title">会话列表</div>
        <el-button type="primary" plain @click="createNewConversation">新建会话</el-button>
      </div>
      <div class="conversation-list">
        <button
          v-for="conversation in workspaceStore.conversations"
          :key="conversation.id"
          class="conversation-item"
          @click="openConversation(conversation.id)"
        >
          <strong>{{ conversation.title }}</strong>
          <span>{{ conversation.updated_at }}</span>
        </button>
      </div>
    </el-card>

    <el-card shadow="never" class="chat-pane">
      <div class="card-title">检索问答</div>
      <el-alert
        v-if="retrievalSummary"
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 12px"
      >
        <template #title>
          已命中 {{ retrievalSummary.count }} 条片段，当前检索模式：{{ retrievalSummary.mode }}
        </template>
      </el-alert>
      <div class="message-list">
        <div v-for="message in workspaceStore.messages" :key="message.id" class="message-item">
          <strong>{{ message.role }}</strong>
          <div>{{ message.content }}</div>
        </div>
        <div v-if="streamText" class="message-item assistant-stream">
          <strong>assistant</strong>
          <div>{{ streamText }}</div>
        </div>
      </div>
      <div class="composer">
        <el-input v-model="draft" type="textarea" :rows="4" placeholder="基于当前空间发起提问" />
        <el-button type="primary" :disabled="!activeConversationId" :loading="retrieving" @click="submit">
          发送
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="citation-pane">
      <div class="card-title">引用来源</div>
      <div v-if="workspaceStore.retrievalResult?.chunks?.length" class="citation-list" style="margin-bottom: 14px">
        <div
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
        </div>
      </div>
      <div v-if="citations.length === 0" class="placeholder-copy">
        发起一次流式问答后，这里会显示文档、上传者、可见范围和引用摘要。
      </div>
      <div v-else class="citation-list">
        <div v-for="item in citations" :key="`${item.document_name}-${item.page_no}-${item.section_path}`" class="citation-item">
          <div class="citation-title">{{ item.document_name }}</div>
          <div class="citation-meta">
            <span>上传者：{{ uploaderLabel(item) }}</span>
            <span>可见范围：{{ visibilityLabel(item.visibility_scope) }}</span>
            <span>分段：{{ item.section_path || "-" }}</span>
            <span>页码：{{ item.page_no || "-" }}</span>
          </div>
          <div>{{ item.snippet }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.chat-layout {
  display: grid;
  grid-template-columns: 280px 1fr 340px;
  gap: 18px;
}

.conversation-pane,
.chat-pane,
.citation-pane {
  min-height: 620px;
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.conversation-list,
.citation-list {
  display: grid;
  gap: 12px;
}

.conversation-item,
.citation-item,
.message-item {
  width: 100%;
  text-align: left;
  padding: 14px;
  border: none;
  border-radius: 16px;
  background: #f8fafc;
}

.conversation-item span {
  display: block;
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.message-list {
  display: grid;
  gap: 12px;
  margin: 18px 0;
}

.assistant-stream {
  background: #edf5ff;
}

.composer {
  display: grid;
  gap: 12px;
}

.citation-title {
  margin-bottom: 8px;
  font-weight: 700;
}

.citation-meta {
  display: grid;
  gap: 4px;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
}

.placeholder-copy {
  color: #64748b;
}
</style>
