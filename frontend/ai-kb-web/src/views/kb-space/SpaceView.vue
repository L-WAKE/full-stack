<script setup>
import { computed, onMounted, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

import { useAuthStore } from "../../stores/auth";
import { useWorkspaceStore } from "../../stores/workspace";

const authStore = useAuthStore();
const workspaceStore = useWorkspaceStore();

const createForm = reactive({
  name: "",
  description: "",
});

const memberForm = reactive({
  userIds: "",
  role: "member",
});

const currentRole = computed(() => {
  const members = workspaceStore.activeSpaceDetail?.members || [];
  const current = members.find((item) => item.user_id === authStore.profile?.id);
  return current?.role || "";
});

const canManageMembers = computed(() => authStore.profile?.role === "admin" || currentRole.value === "owner");

function memberActionReason(row) {
  if (canManageMembers.value && row.user_id !== authStore.profile?.id) return "";
  if (!canManageMembers.value) return "只有当前空间 owner 或系统 admin 可以调整成员角色。";
  return "暂不支持修改或移除自己，请由其他 owner 操作。";
}

onMounted(async () => {
  await workspaceStore.loadSpaces();
  await workspaceStore.loadActiveSpaceDetail();
});

async function createNewSpace() {
  if (!createForm.name) return;
  await workspaceStore.addSpace(createForm);
  createForm.name = "";
  createForm.description = "";
  ElMessage.success("知识库空间已创建。");
}

async function refreshCurrentSpace(spaceId) {
  workspaceStore.setActiveSpace(spaceId);
  await workspaceStore.loadActiveSpaceDetail();
}

async function assignMembers() {
  const ids = memberForm.userIds
    .split(",")
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isInteger(item) && item > 0);

  if (ids.length === 0) {
    ElMessage.warning("请输入用户 ID，多个用户请用英文逗号分隔。");
    return;
  }

  await workspaceStore.addSpaceMembers({ user_ids: ids, role: memberForm.role });
  memberForm.userIds = "";
  ElMessage.success("空间成员已更新。");
}

async function changeRole(row, role) {
  try {
    await workspaceStore.changeMemberRole(row.user_id, role);
    ElMessage.success("成员角色已更新。");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "角色更新失败");
  }
}

async function removeMember(row) {
  try {
    await ElMessageBox.confirm(`确认移除成员 ${row.display_name} 吗？`, "移除确认", { type: "warning" });
    await workspaceStore.deleteMember(row.user_id);
    ElMessage.success("成员已移除。");
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error?.response?.data?.detail || "成员移除失败");
    }
  }
}
</script>

<template>
  <div class="page-shell page-shell--table">
    <section class="page-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Knowledge Spaces</span>
          <h2>空间管理</h2>
          <p class="page-subtitle">用于管理知识空间、成员角色与协作权限，让文档与问答建立清晰边界。</p>
        </div>
      </div>
    </section>

    <section class="space-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Create</span>
            <h2>创建空间</h2>
          </div>
        </div>
        <el-form label-position="top" class="panel-form">
          <el-form-item label="空间名称">
            <el-input v-model="createForm.name" placeholder="例如：租赁业务知识库" />
          </el-form-item>
          <el-form-item label="空间说明">
            <el-input v-model="createForm.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-button type="primary" @click="createNewSpace">新建空间</el-button>
        </el-form>
      </article>

      <article class="table-card">
        <div class="table-header">
          <div>
            <h3>空间列表</h3>
            <p>查看当前空间的文档与成员规模。</p>
          </div>
        </div>
        <el-table :data="workspaceStore.spaces" style="width: 100%">
          <el-table-column prop="name" label="空间" min-width="180" />
          <el-table-column prop="description" label="描述" min-width="220" />
          <el-table-column prop="document_count" label="文档数" width="100" />
          <el-table-column prop="member_count" label="成员数" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="refreshCurrentSpace(row.id)">查看成员</el-button>
            </template>
          </el-table-column>
        </el-table>
      </article>
    </section>

    <article class="table-card">
      <div class="table-header">
        <div>
          <h3>成员角色管理</h3>
          <p>当前空间角色：{{ currentRole || "-" }}</p>
        </div>
        <span v-if="currentRole" class="status-chip">{{ currentRole }}</span>
      </div>

      <el-empty v-if="!workspaceStore.activeSpaceDetail" description="请选择一个空间查看成员" />

      <template v-else>
        <el-table :data="workspaceStore.activeSpaceDetail.members" style="width: 100%">
          <el-table-column prop="display_name" label="成员" min-width="160" />
          <el-table-column prop="username" label="账号" min-width="140" />
          <el-table-column prop="role" label="空间角色" width="220">
            <template #default="{ row }">
              <el-tooltip
                :disabled="canManageMembers && row.user_id !== authStore.profile?.id"
                :content="memberActionReason(row)"
                placement="top"
              >
                <div>
                  <el-select
                    :model-value="row.role"
                    size="small"
                    style="width: 140px"
                    :disabled="!canManageMembers || row.user_id === authStore.profile?.id"
                    @change="(value) => changeRole(row, value)"
                  >
                    <el-option label="owner" value="owner" />
                    <el-option label="member" value="member" />
                  </el-select>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-tooltip
                :disabled="canManageMembers && row.user_id !== authStore.profile?.id"
                :content="memberActionReason(row)"
                placement="top"
              >
                <div>
                  <el-button
                    link
                    type="danger"
                    :disabled="!canManageMembers || row.user_id === authStore.profile?.id"
                    @click="removeMember(row)"
                  >
                    移除
                  </el-button>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <div class="member-actions">
          <el-form v-if="canManageMembers" class="member-form">
            <el-form-item label="用户 ID">
              <el-input v-model="memberForm.userIds" placeholder="例如 2,3" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="memberForm.role">
                <el-option label="member" value="member" />
                <el-option label="owner" value="owner" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="assignMembers">分配成员角色</el-button>
            </el-form-item>
          </el-form>

          <el-alert
            v-else
            type="info"
            :closable="false"
            title="只有当前空间 owner 或系统 admin 可以调整成员角色。"
          />
        </div>
      </template>
    </article>
  </div>
</template>

<style scoped>
.space-grid {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: var(--space-4);
}

.panel-form,
.member-form {
  display: grid;
  gap: var(--space-3);
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
  padding: var(--space-5) var(--space-5) var(--space-3);
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
}

.table-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.member-actions {
  padding: var(--space-4) var(--space-5) var(--space-5);
}

@media (max-width: 1100px) {
  .space-grid {
    grid-template-columns: 1fr;
  }
}
</style>
