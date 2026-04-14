<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createHouse, deleteHouse, getHouses, updateHouse, updateHouseStatus } from '../../api/houses'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { useUserStore } from '../../stores/user'
import { houseStatusMap, rentalModeMap } from '../../utils/locale'

const props = defineProps({
  rentalMode: { type: String, required: true },
  title: { type: String, required: true }
})

const userStore = useUserStore()
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const query = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 10 })
const page = ref({ list: [], total: 0 })
const { tableCardRef, tableHeight, scheduleTableHeightUpdate, updateTableHeight } = useAdaptiveTableHeight({
  reservedHeight: 64,
  minHeight: 260
})

const form = reactive({
  houseName: '',
  rentalMode: props.rentalMode,
  projectName: '',
  address: '',
  area: 60,
  rentPrice: 3500,
  status: 'VACANT',
  landlordName: '',
  tenantName: ''
})

const houseMetrics = computed(() => {
  const list = page.value.list || []
  const total = page.value.total || 0
  const vacantCount = list.filter((item) => item.status === 'VACANT').length
  const occupiedCount = list.filter((item) => item.status === 'OCCUPIED').length
  const averageRent = list.length
    ? Math.round(list.reduce((sum, item) => sum + Number(item.rentPrice || 0), 0) / list.length)
    : 0

  return [
    {
      label: `${rentalModeMap[props.rentalMode] || '当前'}房源`,
      value: total,
      meta: '按分页结果同步后台总数'
    },
    {
      label: '当前页已出租',
      value: occupiedCount,
      meta: '帮助快速识别出租集中度'
    },
    {
      label: '当前页待出租',
      value: vacantCount,
      meta: '建议优先跟进空置项目'
    },
    {
      label: '平均租金',
      value: averageRent ? `¥${averageRent}` : '¥0',
      meta: '基于当前筛选结果估算'
    }
  ]
})

async function loadData() {
  loading.value = true
  try {
    page.value = await getHouses({ ...query, rentalMode: props.rentalMode, status: query.status || undefined })
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function resetForm() {
  Object.assign(form, {
    houseName: '',
    rentalMode: props.rentalMode,
    projectName: '',
    address: '',
    area: 60,
    rentPrice: 3500,
    status: 'VACANT',
    landlordName: '',
    tenantName: ''
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

function resolveStatusType(status) {
  if (status === 'OCCUPIED') return 'success'
  if (status === 'VACANT') return 'warning'
  if (status === 'OFFLINE') return 'info'
  if (status === 'RENOVATING') return 'primary'
  return 'info'
}

async function submit() {
  if (!form.houseName?.trim()) {
    ElMessage.warning('请输入房源名称')
    return
  }
  if (!form.projectName?.trim()) {
    ElMessage.warning('请输入所属项目')
    return
  }
  if (!form.address?.trim()) {
    ElMessage.warning('请输入详细地址')
    return
  }
  if (!form.landlordName?.trim()) {
    ElMessage.warning('请输入房东姓名')
    return
  }

  if (editingId.value) {
    await updateHouse(editingId.value, form)
    ElMessage.success('房源更新成功')
  } else {
    await createHouse(form)
    ElMessage.success('房源创建成功')
  }
  dialogVisible.value = false
  await loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除房源“${row.houseName}”吗？`, '删除确认', { type: 'warning' })
  await deleteHouse(row.id)
  ElMessage.success('房源删除成功')
  await loadData()
}

async function quickStatus(row, status) {
  await updateHouseStatus(row.id, status)
  ElMessage.success('房源状态更新成功')
  await loadData()
}

watch(
  () => props.rentalMode,
  () => {
    query.pageNum = 1
    resetForm()
    loadData()
  },
  { immediate: true }
)

onMounted(() => {
  updateTableHeight()
})
</script>

<template>
  <div class="page-shell page-shell--table">
    <section class="page-toolbar housing-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Housing Ledger</span>
          <h2>{{ title }}</h2>
          <div class="page-subtitle">
            以更贴近主流管理后台的方式组织房源台账，让筛选、状态切换和基础信息维护都更直观。
          </div>
        </div>
        <el-button v-if="userStore.hasPermission('house:add')" type="primary" @click="openCreate">
          新增房源
        </el-button>
      </div>

      <el-form class="filter-form" inline>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入房源编号或名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 180px">
            <el-option v-for="(label, key) in houseStatusMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询房源</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="metric-strip">
      <article v-for="item in houseMetrics" :key="item.label" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-meta">{{ item.meta }}</div>
      </article>
    </section>

    <section ref="tableCardRef" class="table-card house-table-card">
      <el-table v-loading="loading" :data="page.list" :height="tableHeight" stripe>
        <el-table-column prop="houseCode" label="房源编号" width="140" />
        <el-table-column prop="houseName" label="房源名称" min-width="180" />
        <el-table-column prop="projectName" label="所属项目" min-width="150" />
        <el-table-column prop="address" label="详细地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="area" label="面积" width="100">
          <template #default="{ row }">{{ row.area }} ㎡</template>
        </el-table-column>
        <el-table-column prop="rentPrice" label="租金" width="120">
          <template #default="{ row }">¥{{ row.rentPrice }}</template>
        </el-table-column>
        <el-table-column prop="landlordName" label="房东" min-width="120" />
        <el-table-column prop="tenantName" label="租客" min-width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="resolveStatusType(row.status)">{{ houseStatusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('house:edit')" link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button link @click="quickStatus(row, 'OCCUPIED')">设为已出租</el-button>
            <el-button link @click="quickStatus(row, 'VACANT')">设为空置</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-card__footer">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑房源' : '新增房源'" width="680px">
      <el-form label-width="110px">
        <el-form-item label="房源名称"><el-input v-model="form.houseName" /></el-form-item>
        <el-form-item label="所属项目"><el-input v-model="form.projectName" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="面积"><el-input-number v-model="form.area" :min="1" /></el-form-item>
        <el-form-item label="租金"><el-input-number v-model="form.rentPrice" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option v-for="(label, key) in houseStatusMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="房东"><el-input v-model="form.landlordName" /></el-form-item>
        <el-form-item label="租客"><el-input v-model="form.tenantName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.housing-toolbar {
  padding-bottom: 14px;
}

.house-table-card :deep(.el-table) {
  width: 100%;
}

.house-table-card :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}
</style>
