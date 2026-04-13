<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createMenu, getMenuTree, updateMenu } from '../../api/resources'
import { useAdaptiveTableHeight } from '../../composables/useAdaptiveTableHeight'
import { localizeMenus, menuTypeMap } from '../../utils/locale'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const menus = ref([])
const { tableCardRef, tableHeight, scheduleTableHeightUpdate } = useAdaptiveTableHeight()
const menuOptions = computed(() => [{ id: 0, title: '根目录' }, ...menus.value.filter((item) => item.type === 'MENU')])
const form = reactive({
  parentId: 0,
  name: '',
  title: '',
  path: '',
  type: 'MENU',
  permissionCode: ''
})

async function loadData() {
  loading.value = true
  try {
    menus.value = localizeMenus(await getMenuTree())
  } finally {
    loading.value = false
    await scheduleTableHeightUpdate()
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { parentId: 0, name: '', title: '', path: '', type: 'MENU', permissionCode: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await updateMenu(editingId.value, form)
    ElMessage.success('菜单更新成功')
  } else {
    await createMenu(form)
    ElMessage.success('菜单创建成功')
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
          <h2>菜单管理</h2>
          <div class="page-subtitle">统一维护菜单树和按钮权限编码。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增菜单</el-button>
      </div>
    </div>
    <div ref="tableCardRef" class="table-card">
      <el-table v-loading="loading" :data="menus" :height="tableHeight" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="parentId" label="父级" width="80" />
        <el-table-column prop="name" label="菜单标识" width="150" />
        <el-table-column prop="title" label="菜单名称" width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ menuTypeMap[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="permissionCode" label="权限编码" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="620px">
      <el-form label-width="120px">
        <el-form-item label="父级菜单">
          <el-select v-model="form.parentId">
            <el-option v-for="item in menuOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单标识"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="路由路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限编码"><el-input v-model="form.permissionCode" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
