<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCleaningOrder, deleteCleaningOrder, getCleaningOrders, updateCleaningOrder } from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { cleaningStatusMap } from '../../utils/locale'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const page = ref({ list: [], total: 0 })
const { tableCardRef, tableHeight, scheduleTableHeightUpdate } = useAdaptiveTableHeight()
const form = reactive({
  houseName: '',
  cleaningType: '',
  appointmentTime: '',
  assigneeName: '',
  status: 'PENDING_ASSIGN'
})

async function loadData() {
  loading.value = true
  try {
    page.value = await getCleaningOrders(query)
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { houseName: '', cleaningType: '', appointmentTime: '', assigneeName: '', status: 'PENDING_ASSIGN' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateCleaningOrder(editingId.value, form)
    ElMessage.success('保洁工单更新成功')
  } else {
    await createCleaningOrder(form)
    ElMessage.success('保洁工单创建成功')
  }
  dialogVisible.value = false
  await loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除保洁工单“${row.orderNo}”吗？`, '删除确认', { type: 'warning' })
  await deleteCleaningOrder(row.id)
  ElMessage.success('保洁工单删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell page-shell--table">
    <div class="page-toolbar">
      <div class="page-title">
        <div>
          <h2>保洁工单</h2>
          <div class="page-subtitle">管理保洁预约、服务人员和工单完成状态。</div>
        </div>
        <el-button type="primary" @click="openCreate">新建工单</el-button>
      </div>
      <el-form inline class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入工单号或房源名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div ref="tableCardRef" class="table-card">
      <el-table v-loading="loading" :data="page.list" :height="tableHeight" stripe>
        <el-table-column prop="orderNo" label="工单号" width="140" />
        <el-table-column prop="houseName" label="房源" />
        <el-table-column prop="cleaningType" label="保洁类型" />
        <el-table-column prop="appointmentTime" label="预约时间" width="160" />
        <el-table-column prop="assigneeName" label="服务人员" width="120" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">{{ cleaningStatusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑保洁工单' : '新建保洁工单'" width="640px">
      <el-form label-width="120px">
        <el-form-item label="房源"><el-input v-model="form.houseName" /></el-form-item>
        <el-form-item label="保洁类型"><el-input v-model="form.cleaningType" /></el-form-item>
        <el-form-item label="预约时间"><el-input v-model="form.appointmentTime" placeholder="YYYY-MM-DD HH:mm" /></el-form-item>
        <el-form-item label="服务人员"><el-input v-model="form.assigneeName" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option v-for="(label, key) in cleaningStatusMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-form {
  margin-top: 18px;
}
</style>
