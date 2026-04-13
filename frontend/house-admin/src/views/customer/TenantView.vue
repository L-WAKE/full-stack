<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTenant, getTenants, updateTenant } from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { genderMap } from '../../utils/locale'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const page = ref({ list: [], total: 0 })
const { tableCardRef, tableHeight, scheduleTableHeightUpdate } = useAdaptiveTableHeight()
const form = reactive({
  name: '',
  mobile: '',
  gender: 'Male',
  houseName: '',
  checkinDate: '',
  checkoutDate: '',
  emergencyContact: '',
  emergencyMobile: ''
})

async function loadData() {
  loading.value = true
  try {
    page.value = await getTenants(query)
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    mobile: '',
    gender: 'Male',
    houseName: '',
    checkinDate: '',
    checkoutDate: '',
    emergencyContact: '',
    emergencyMobile: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateTenant(editingId.value, form)
    ElMessage.success('租客信息更新成功')
  } else {
    await createTenant(form)
    ElMessage.success('租客信息创建成功')
  }
  dialogVisible.value = false
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell page-shell--table">
    <div class="page-toolbar">
      <div class="page-title">
        <div>
          <h2>租客管理</h2>
          <div class="page-subtitle">维护租客档案、入住信息和紧急联系人资料。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增租客</el-button>
      </div>
      <el-form inline class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="请输入租客姓名或手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div ref="tableCardRef" class="table-card">
      <el-table v-loading="loading" :data="page.list" :height="tableHeight" stripe>
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="mobile" label="手机号" />
        <el-table-column label="性别" width="90">
          <template #default="{ row }">{{ genderMap[row.gender] || row.gender }}</template>
        </el-table-column>
        <el-table-column prop="houseName" label="关联房源" />
        <el-table-column prop="checkinDate" label="入住日期" width="120" />
        <el-table-column prop="checkoutDate" label="退租日期" width="120" />
        <el-table-column prop="emergencyContact" label="紧急联系人" width="150" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑租客' : '新增租客'" width="620px">
      <el-form label-width="130px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender">
            <el-option label="男" value="Male" />
            <el-option label="女" value="Female" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联房源"><el-input v-model="form.houseName" /></el-form-item>
        <el-form-item label="入住日期"><el-input v-model="form.checkinDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="退租日期"><el-input v-model="form.checkoutDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="form.emergencyContact" /></el-form-item>
        <el-form-item label="紧急联系电话"><el-input v-model="form.emergencyMobile" /></el-form-item>
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
