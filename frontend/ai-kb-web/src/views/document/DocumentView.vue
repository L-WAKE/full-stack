<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

import { useAuthStore } from "../../stores/auth";
import { useWorkspaceStore } from "../../stores/workspace";

const authStore = useAuthStore();
const workspaceStore = useWorkspaceStore();

const currentDocumentId = ref(null);
const parsingId = ref(null);
const uploadRef = ref(null);
const uploading = ref(false);
const visibilityScope = ref("SPACE");
const visibilityFilter = ref("ALL");
let pollingTimer = null;

const currentSpaceRole = computed(() => {
  const members = workspaceStore.activeSpaceDetail?.members || [];
  const current = members.find((item) => item.user_id === authStore.profile?.id);
  return current?.role || "-";
});

const filteredDocuments = computed(() => {
  if (visibilityFilter.value === "ALL") return workspaceStore.documents;
  return workspaceStore.documents.filter((item) => item.visibility_scope === visibilityFilter.value);
});

function canManageDocument(row) {
  const myId = authStore.profile?.id;
  const isAdmin = authStore.profile?.role === "admin";
  return isAdmin || currentSpaceRole.value === "owner" || row.created_by === myId;
}

function uploaderLabel(row) {
  const display = row.created_by_name || row.created_by_username || `user-${row.created_by}`;
  if (row.created_by === authStore.profile?.id) return `${display}（我）`;
  if (row.created_by_username) return `${display} / ${row.created_by_username}`;
  return display;
}

onMounted(async () => {
  await workspaceStore.loadSpaces();
  await workspaceStore.loadActiveSpaceDetail();
  await workspaceStore.loadDocuments();
});

function validateUploadFile(uploadFile) {
  const rawFile = uploadFile?.raw || uploadFile;
  const fileName = rawFile?.name || uploadFile?.name || "";
  const extension = fileName.split(".").pop()?.toLowerCase();
  if (!["pdf", "docx"].includes(extension)) {
    ElMessage.warning("当前仅支持 PDF / DOCX，请先转换后再上传。");
    uploadRef.value?.clearFiles();
    return null;
  }
  return rawFile;
}

function handleUploadExceed() {
  uploadRef.value?.clearFiles();
  ElMessage.info("已重置上传选择，请重新选择一个 PDF 或 DOCX 文件。");
}

async function handleUpload(uploadFile) {
  const rawFile = validateUploadFile(uploadFile);
  if (!rawFile) return;

  try {
    uploading.value = true;
    const documents = await workspaceStore.addDocument(rawFile, visibilityScope.value);
    const latestDocument = [...(documents || [])]
      .filter((item) => item.file_name === rawFile.name)
      .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())[0];

    if (latestDocument) {
      currentDocumentId.value = latestDocument.id;
      await workspaceStore.parseDocument(latestDocument.id);
      await workspaceStore.loadDocuments();
      startPolling(latestDocument.id);
      ElMessage.success("文档已上传并自动触发解析，系统正在后台处理。");
    } else {
      ElMessage.success("文档已上传，当前状态为 pending。");
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "上传失败，请检查文件格式和服务状态。");
  } finally {
    uploading.value = false;
    uploadRef.value?.clearFiles();
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
    ElMessage.success("文档可见范围已更新");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "更新失败");
  }
}

async function removeDocument(row) {
  try {
    await ElMessageBox.confirm(`确认删除文档《${row.file_name}》吗？`, "删除确认", { type: "warning" });
    await workspaceStore.removeDocument(row.id);
    ElMessage.success("文档已删除");
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
        ElMessage.success("后台解析已完成");
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
  <div class="page-shell page-shell--table">
    <section class="page-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Document Governance</span>
          <h2>文档入库、解析和可见范围治理</h2>
          <p class="page-subtitle">
            面向企业知识库管理 PDF / DOCX 文档的上传、解析、切片预览和权限边界，适合 SOP、FAQ、制度规范和实施手册场景。
          </p>
        </div>
      </div>
    </section>

    <section class="document-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Upload</span>
            <h2>上传知识文档</h2>
          </div>
        </div>

        <el-alert
          type="info"
          :closable="false"
          :title="`当前空间角色：${currentSpaceRole}`"
          description="owner、文档上传人和系统 admin 可以管理文档可见范围与删除操作。"
        />

        <el-form label-position="top" class="panel-form">
          <el-form-item label="上传后可见范围">
            <el-segmented
              v-model="visibilityScope"
              :options="[
                { label: '空间共享', value: 'SPACE' },
                { label: '仅自己可见', value: 'PRIVATE' },
              ]"
            />
          </el-form-item>
        </el-form>

        <el-upload
          ref="uploadRef"
          drag
          accept=".pdf,.docx"
          :auto-upload="false"
          :show-file-list="false"
          :limit="1"
          :disabled="uploading"
          :on-change="handleUpload"
          :on-exceed="handleUploadExceed"
        >
          <div class="upload-copy">
            <strong>将文档拖到这里，或点击选择上传</strong>
            <span>支持 PDF / DOCX，适合产品手册、业务 SOP、FAQ 和制度文档。</span>
          </div>
        </el-upload>
        <div class="upload-warning">仅支持 PDF / DOCX</div>
      </article>

      <article class="table-card">
        <div class="table-header">
          <div>
            <h3>文档治理列表</h3>
            <p>查看当前空间文档的解析状态、共享范围、失败原因和切片规模。</p>
          </div>
          <el-select v-model="visibilityFilter" class="visibility-filter">
            <el-option label="全部文档" value="ALL" />
            <el-option label="空间共享" value="SPACE" />
            <el-option label="仅自己可见" value="PRIVATE" />
          </el-select>
        </div>

        <el-table :data="filteredDocuments" style="width: 100%">
          <el-table-column prop="file_name" label="文档名" min-width="220" />
          <el-table-column prop="file_type" label="类型" width="90" />
          <el-table-column label="上传人" min-width="180">
            <template #default="{ row }">
              <span>{{ uploaderLabel(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="visibility_scope" label="可见范围" width="160">
            <template #default="{ row }">
              <el-select
                :model-value="row.visibility_scope"
                size="small"
                style="width: 120px"
                :disabled="!canManageDocument(row)"
                @change="(value) => updateVisibility(row, value)"
              >
                <el-option label="共享" value="SPACE" />
                <el-option label="私有" value="PRIVATE" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="parse_status" label="解析状态" width="120" />
          <el-table-column prop="chunk_count" label="切片数" width="90" />
          <el-table-column label="失败原因 / 提示" min-width="220">
            <template #default="{ row }">
              <span>{{ row.failure_reason || "-" }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
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
            </template>
          </el-table-column>
        </el-table>
      </article>
    </section>

    <article class="panel-card">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Chunk Preview</span>
          <h2>切片预览 {{ currentDocumentId ? `#${currentDocumentId}` : "" }}</h2>
          <p class="page-subtitle">
            解析完成后在这里快速检查分段结构、页码和摘要内容，判断切片是否适合进入检索链路。
          </p>
        </div>
      </div>

      <el-empty v-if="workspaceStore.chunks.length === 0" description="选择文档后查看切片结果" />
      <div v-else class="chunk-list">
        <article v-for="chunk in workspaceStore.chunks" :key="chunk.id" class="chunk-item">
          <div class="chunk-head">{{ chunk.section_path || "未命名分段" }} / page {{ chunk.page_no || "-" }}</div>
          <div class="chunk-meta">token estimate {{ chunk.token_estimate }}</div>
          <div class="chunk-body">{{ chunk.content_preview }}</div>
        </article>
      </div>
    </article>
  </div>
</template>

<style scoped>
.document-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: var(--space-4);
}

.panel-form {
  display: grid;
  gap: var(--space-3);
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
  padding: 18px 20px 10px;
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

.visibility-filter {
  width: 180px;
}

.upload-copy strong,
.upload-copy span {
  display: block;
}

.upload-copy strong {
  font-weight: 900;
  text-transform: uppercase;
}

.upload-copy span {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.upload-warning {
  margin-top: 8px;
  color: var(--danger);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.action-group {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.chunk-list {
  display: grid;
  gap: var(--space-3);
}

.chunk-item {
  padding: 14px;
  border: 1px solid var(--border);
  background: var(--white);
}

.chunk-head {
  font-weight: 900;
}

.chunk-meta {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.chunk-body {
  margin-top: 8px;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .document-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
  }

  .visibility-filter {
    width: 100%;
  }
}
</style>
