<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createRole, getRoles, updateRole } from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { localizeRoleName } from '../../utils/locale'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const roles = ref([])
const { tableCardRef, tableHeight, scheduleTableHeightUpdate } = useAdaptiveTableHeight()
const form = reactive({ code: '', name: '', remark: '' })

async function loadData() {
  loading.value = true
  try {
    roles.value = await getRoles()
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { code: '', name: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateRole(editingId.value, form)
    ElMessage.success('角色更新成功')
  } else {
    await createRole(form)
    ElMessage.success('角色创建成功')
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
          <h2>角色管理</h2>
          <div class="page-subtitle">维护 RBAC 角色编码和说明，为权限扩展提供基础。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增角色</el-button>
      </div>
    </div>
    <div ref="tableCardRef" class="table-card">
      <el-table v-loading="loading" :data="roles" :height="tableHeight" stripe>
        <el-table-column prop="code" label="角色编码" width="160" />
        <el-table-column label="角色名称" width="180">
          <template #default="{ row }">{{ localizeRoleName(row) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="560px">
      <el-form label-width="100px">
        <el-form-item label="角色编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
