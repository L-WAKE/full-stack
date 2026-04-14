<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNotices,
  offlineNotice,
  publishNotice,
  updateNotice,
  updateNoticePin
} from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { noticeStatusMap } from '../../utils/locale'

const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const editingId = ref(null)
const detail = ref(null)
const query = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 10 })
const page = ref({ list: [], total: 0 })
const { tableCardRef, tableHeight, scheduleTableHeightUpdate, updateTableHeight } = useAdaptiveTableHeight({
  reservedHeight: 64,
  minHeight: 260
})

const form = reactive({
  title: '',
  summary: '',
  content: '',
  status: 'DRAFT',
  pinned: false
})

const dialogTitle = computed(() => (editingId.value ? '编辑公告' : '新增公告'))

async function loadData() {
  loading.value = true
  try {
    page.value = await getNotices({
      ...query,
      status: query.status || undefined
    })
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function resetForm() {
  Object.assign(form, {
    title: '',
    summary: '',
    content: '',
    status: 'DRAFT',
    pinned: false
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    summary: row.summary || '',
    content: row.content || '',
    status: row.status,
    pinned: row.pinned
  })
  dialogVisible.value = true
}

async function openDetail(row) {
  detail.value = await getNoticeDetail(row.id)
  detailVisible.value = true
}

async function submit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  if (editingId.value) {
    await updateNotice(editingId.value, form)
    ElMessage.success('公告更新成功')
  } else {
    await createNotice(form)
    ElMessage.success('公告创建成功')
  }

  dialogVisible.value = false
  await loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除公告“${row.title}”吗？`, '删除确认', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('公告删除成功')
  await loadData()
}

async function handlePublish(row) {
  await publishNotice(row.id)
  ElMessage.success('公告已发布')
  await loadData()
}

async function handleOffline(row) {
  await offlineNotice(row.id)
  ElMessage.success('公告已下线')
  await loadData()
}

async function togglePin(row) {
  await updateNoticePin(row.id, !row.pinned)
  ElMessage.success(row.pinned ? '已取消置顶' : '已置顶')
  await loadData()
}

function tagType(status) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'OFFLINE') return 'info'
  return 'warning'
}

function formatDateTime(value) {
  if (!value) return '--'
  return value.replace('T', ' ')
}

onMounted(() => {
  loadData()
  updateTableHeight()
})
</script>

<template>
  <div class="page-shell page-shell--table">
    <div class="page-toolbar">
      <div class="page-title">
        <div>
          <h2>公告管理</h2>
          <div class="page-subtitle">统一管理系统公告的草稿、发布、下线和置顶状态。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增公告</el-button>
      </div>
      <el-form inline class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="请输入标题或摘要" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option v-for="(label, key) in noticeStatusMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div ref="tableCardRef" class="table-card notice-table-card">
      <el-table v-loading="loading" :data="page.list" :height="tableHeight" stripe>
        <el-table-column prop="title" label="公告标题" min-width="200" />
        <el-table-column prop="summary" label="摘要" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ noticeStatusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="90">
          <template #default="{ row }">
            <el-tag :type="row.pinned ? 'danger' : 'info'">{{ row.pinned ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link @click="togglePin(row)">{{ row.pinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 'PUBLISHED'" link @click="handleOffline(row)">下线</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form label-width="100px">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 180px">
            <el-option v-for="(label, key) in noticeStatusMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.pinned" />
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="form.content" type="textarea" :rows="8" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="公告详情" size="520px">
      <template v-if="detail">
        <div class="detail-item">
          <span class="detail-label">标题</span>
          <div class="detail-value">{{ detail.title }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态</span>
          <div class="detail-value">{{ noticeStatusMap[detail.status] || detail.status }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">是否置顶</span>
          <div class="detail-value">{{ detail.pinned ? '是' : '否' }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">发布时间</span>
          <div class="detail-value">{{ formatDateTime(detail.publishTime) }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">更新时间</span>
          <div class="detail-value">{{ formatDateTime(detail.updatedAt) }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">摘要</span>
          <div class="detail-value">{{ detail.summary || '--' }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">正文</span>
          <div class="detail-content">{{ detail.content }}</div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.filter-form {
  margin-top: 18px;
}

.notice-table-card :deep(.el-table) {
  width: 100%;
}

.notice-table-card :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.detail-item + .detail-item {
  margin-top: 18px;
}

.detail-label {
  display: inline-block;
  min-width: 84px;
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 8px;
}

.detail-value {
  color: #1f2937;
  line-height: 1.7;
}

.detail-content {
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
  color: #1f2937;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
