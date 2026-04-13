<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createLandlord, getLandlords, updateLandlord } from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const page = ref({ list: [], total: 0 })
const { tableCardRef, tableHeight, scheduleTableHeightUpdate } = useAdaptiveTableHeight()
const form = reactive({
  name: '',
  mobile: '',
  idNo: '',
  address: '',
  bankAccount: '',
  remark: ''
})

async function loadData() {
  loading.value = true
  try {
    page.value = await getLandlords(query)
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', mobile: '', idNo: '', address: '', bankAccount: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateLandlord(editingId.value, form)
    ElMessage.success('房东信息更新成功')
  } else {
    await createLandlord(form)
    ElMessage.success('房东信息创建成功')
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
          <h2>房东管理</h2>
          <div class="page-subtitle">维护房东身份信息、联系方式和收款账户信息。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增房东</el-button>
      </div>
      <el-form inline class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="请输入房东姓名或手机号" />
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
        <el-table-column prop="idNo" label="证件号 / 执照号" />
        <el-table-column prop="address" label="联系地址" />
        <el-table-column prop="bankAccount" label="收款账户" />
        <el-table-column prop="houseCount" label="名下房源数" width="110" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑房东' : '新增房东'" width="620px">
      <el-form label-width="120px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
        <el-form-item label="证件号 / 执照号"><el-input v-model="form.idNo" /></el-form-item>
        <el-form-item label="联系地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="收款账户"><el-input v-model="form.bankAccount" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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
