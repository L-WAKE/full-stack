<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createEmployee, getEmployees, getRoles, updateEmployee, updateEmployeeStatus } from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { employeeStatusMap, localizeRoleName } from '../../utils/locale'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const roles = ref([])
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const page = ref({ list: [], total: 0 })
const { tableCardRef, tableHeight, scheduleTableHeightUpdate } = useAdaptiveTableHeight()
const form = reactive({
  code: '',
  name: '',
  mobile: '',
  position: '',
  status: 'ENABLED',
  roleCode: 'OPERATOR'
})

async function loadData() {
  loading.value = true
  try {
    const [employeePage, roleList] = await Promise.all([getEmployees(query), getRoles()])
    page.value = employeePage
    roles.value = roleList
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { code: '', name: '', mobile: '', position: '', status: 'ENABLED', roleCode: 'OPERATOR' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateEmployee(editingId.value, form)
    ElMessage.success('员工信息更新成功')
  } else {
    await createEmployee(form)
    ElMessage.success('员工信息创建成功')
  }
  dialogVisible.value = false
  await loadData()
}

async function toggleStatus(row) {
  await updateEmployeeStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success('员工状态更新成功')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell page-shell--table">
    <div class="page-toolbar">
      <div class="page-title">
        <div>
          <h2>员工管理</h2>
          <div class="page-subtitle">管理员工档案、角色分配和账号启停状态。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增员工</el-button>
      </div>
      <el-form inline class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="请输入员工编号或姓名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div ref="tableCardRef" class="table-card">
      <el-table v-loading="loading" :data="page.list" :height="tableHeight" stripe>
        <el-table-column prop="code" label="员工编号" width="120" />
        <el-table-column prop="name" label="姓名" width="140" />
        <el-table-column prop="mobile" label="手机号" width="140" />
        <el-table-column prop="position" label="岗位" />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">{{ localizeRoleName({ code: row.roleCode, name: row.roleCode }) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ employeeStatusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑员工' : '新增员工'" width="620px">
      <el-form label-width="120px">
        <el-form-item label="员工编号"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
        <el-form-item label="岗位"><el-input v-model="form.position" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode">
            <el-option v-for="role in roles" :key="role.id" :label="localizeRoleName(role)" :value="role.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
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
