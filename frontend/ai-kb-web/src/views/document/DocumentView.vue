<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

import { useAuthStore } from "../../stores/auth";
import { useWorkspaceStore } from "../../stores/workspace";

const authStore = useAuthStore();
const workspaceStore = useWorkspaceStore();

const currentDocumentId = ref(null);
const parsingId = ref(null);
const visibilityScope = ref("SPACE");
const visibilityFilter = ref("ALL");
let pollingTimer = null;

const currentSpaceRole = computed(() => {
  const members = workspaceStore.activeSpaceDetail?.members || [];
  const current = members.find((item) => item.user_id === authStore.profile?.id);
  return current?.role || "-";
});

const filteredDocuments = computed(() => {
  if (visibilityFilter.value === "ALL") {
    return workspaceStore.documents;
  }
  return workspaceStore.documents.filter((item) => item.visibility_scope === visibilityFilter.value);
});

function canManageDocument(row) {
  const myId = authStore.profile?.id;
  const isAdmin = authStore.profile?.role === "admin";
  return isAdmin || currentSpaceRole.value === "owner" || row.created_by === myId;
}

function documentActionReason(row) {
  if (canManageDocument(row)) {
    return "";
  }
  if (row.visibility_scope === "PRIVATE") {
    return "这是他人的私有文档。只有上传者本人、当前空间 owner 或系统 admin 可以操作。";
  }
  return "只有上传者本人、当前空间 owner 或系统 admin 可以修改可见范围、触发解析和删除。";
}

function uploaderLabel(row) {
  const display = row.created_by_name || row.created_by_username || `用户 ${row.created_by}`;
  if (row.created_by === authStore.profile?.id) {
    return `${display}（我）`;
  }
  if (row.created_by_username) {
    return `${display} / ${row.created_by_username}`;
  }
  return display;
}

onMounted(async () => {
  await workspaceStore.loadSpaces();
  await workspaceStore.loadActiveSpaceDetail();
  await workspaceStore.loadDocuments();
});

async function handleUpload(uploadFile) {
  try {
    await workspaceStore.addDocument(uploadFile.raw, visibilityScope.value);
    ElMessage.success("文档已上传，当前状态为 PENDING。");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "上传失败");
  }
}

async function parseDocument(documentId) {
  try {
    parsingId.value = documentId;
    await workspaceStore.parseDocument(documentId);
    await workspaceStore.loadDocuments();
    startPolling(documentId);
    ElMessage.success("解析任务已提交，系统正在后台处理。");
  } catch (error) {
    parsingId.value = null;
    ElMessage.error(error?.response?.data?.detail || "解析失败");
  }
}

async function previewChunks(documentId) {
  currentDocumentId.value = documentId;
  await workspaceStore.loadChunks(documentId);
}

async function updateVisibility(row, visibility) {
  try {
    await workspaceStore.changeDocumentVisibility(row.id, visibility);
    ElMessage.success("文档可见范围已更新。");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "更新失败");
  }
}

async function removeDocument(row) {
  try {
    await ElMessageBox.confirm(`确认删除文档《${row.file_name}》吗？`, "删除确认", {
      type: "warning",
    });
    await workspaceStore.removeDocument(row.id);
    ElMessage.success("文档已删除。");
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error?.response?.data?.detail || "删除失败");
    }
  }
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
}

function startPolling(documentId) {
  stopPolling();
  pollingTimer = setInterval(async () => {
    const documents = (await workspaceStore.loadDocuments()) || [];
    const current = documents.find((item) => item.id === documentId);
    if (!current) {
      stopPolling();
      parsingId.value = null;
      return;
    }
    if (current.parse_status === "READY" || current.parse_status === "FAILED") {
      stopPolling();
      parsingId.value = null;
      await previewChunks(documentId);
      if (current.parse_status === "READY") {
        ElMessage.success("后台解析已完成。");
      } else {
        ElMessage.error(current.failure_reason || "后台解析失败");
      }
    }
  }, 2000);
}

onBeforeUnmount(() => {
  stopPolling();
});
</script>

<template>
  <div class="document-grid">
    <el-card shadow="never">
      <div class="card-title">上传文档</div>
      <p>支持 PDF / DOCX 上传、异步解析、切片预览与可见范围控制。</p>
      <el-alert
        type="info"
        :closable="false"
        style="margin: 12px 0 16px"
        :title="`当前空间角色：${currentSpaceRole}`"
        description="上传者本人、当前空间 owner 或系统 admin 可以修改可见范围、触发解析和删除文档。"
      />
      <el-form label-position="top" style="margin-bottom: 12px">
        <el-form-item label="上传时可见范围">
          <el-segmented
            v-model="visibilityScope"
            :options="[
              { label: '空间可见', value: 'SPACE' },
              { label: '仅自己可见', value: 'PRIVATE' }
            ]"
          />
        </el-form-item>
      </el-form>
      <el-upload drag :auto-upload="false" :show-file-list="false" :limit="1" :on-change="handleUpload">
        <div>将文件拖到这里，或点击选择上传</div>
      </el-upload>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <div class="card-title">文档中心</div>
        <el-select v-model="visibilityFilter" style="width: 180px">
          <el-option label="全部文档" value="ALL" />
          <el-option label="空间可见" value="SPACE" />
          <el-option label="仅自己可见" value="PRIVATE" />
        </el-select>
      </div>
      <el-table :data="filteredDocuments" style="width: 100%">
        <el-table-column prop="file_name" label="文件名" min-width="220" />
        <el-table-column prop="file_type" label="类型" width="90" />
        <el-table-column label="上传者" min-width="180">
          <template #default="{ row }">
            <span>{{ uploaderLabel(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="visibility_scope" label="可见范围" width="170">
          <template #default="{ row }">
            <el-tooltip :disabled="canManageDocument(row)" :content="documentActionReason(row)" placement="top">
              <div>
                <el-select
                  :model-value="row.visibility_scope"
                  size="small"
                  style="width: 130px"
                  :disabled="!canManageDocument(row)"
                  @change="(value) => updateVisibility(row, value)"
                >
                  <el-option label="空间可见" value="SPACE" />
                  <el-option label="仅自己可见" value="PRIVATE" />
                </el-select>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="parse_status" label="状态" width="120" />
        <el-table-column label="任务时间" width="220">
          <template #default="{ row }">
            <div>{{ row.parse_started_at || row.parse_requested_at || "-" }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="chunk_count" label="切片数" width="90" />
        <el-table-column label="错误 / 提示" min-width="260">
          <template #default="{ row }">
            <span>{{ row.failure_reason || "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-tooltip :disabled="canManageDocument(row)" :content="documentActionReason(row)" placement="top">
              <div class="action-group">
                <el-button
                  link
                  type="primary"
                  :loading="parsingId === row.id"
                  :disabled="!canManageDocument(row)"
                  @click="parseDocument(row.id)"
                >
                  解析
                </el-button>
                <el-button link @click="previewChunks(row.id)">查看切片</el-button>
                <el-button link type="danger" :disabled="!canManageDocument(row)" @click="removeDocument(row)">
                  删除
                </el-button>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="chunk-card">
      <div class="card-title">切片预览 {{ currentDocumentId ? `#${currentDocumentId}` : "" }}</div>
      <el-empty v-if="workspaceStore.chunks.length === 0" description="选择文档后查看切片" />
      <div v-else class="chunk-list">
        <div v-for="chunk in workspaceStore.chunks" :key="chunk.id" class="chunk-item">
          <div class="chunk-head">{{ chunk.section_path || "未命名分段" }} / 第 {{ chunk.page_no || "-" }} 页</div>
          <div>{{ chunk.content_preview }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.document-grid {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.action-group {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.chunk-card {
  grid-column: 1 / -1;
}

.chunk-list {
  display: grid;
  gap: 12px;
}

.chunk-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.chunk-head {
  margin-bottom: 8px;
  font-weight: 600;
}
</style>
