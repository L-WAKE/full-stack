<script setup>
import { computed, onMounted, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

import { useAuthStore } from "../../stores/auth";
import { useWorkspaceStore } from "../../stores/workspace";

const authStore = useAuthStore();
const workspaceStore = useWorkspaceStore();

const scenarioTemplates = [
  {
    name: "客户支持知识库",
    description: "面向客服、售后团队，沉淀 FAQ、工单处理流程、产品异常排查和客户沟通口径。",
  },
  {
    name: "售前与实施交付知识库",
    description: "沉淀产品资料、标书答疑、实施 checklist、接口规范和项目交付模板。",
  },
  {
    name: "制度与合规政策知识库",
    description: "用于沉淀权限管理、审批规则、内部制度、流程边界和审计要求。",
  },
];

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

onMounted(async () => {
  await workspaceStore.loadSpaces();
  await workspaceStore.loadActiveSpaceDetail();
});

function applyTemplate(item) {
  createForm.name = item.name;
  createForm.description = item.description;
}

async function createNewSpace() {
  if (!createForm.name) return;
  await workspaceStore.addSpace(createForm);
  createForm.name = "";
  createForm.description = "";
  ElMessage.success("知识空间已创建");
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
    ElMessage.warning("请输入用户 ID，多个用户请用英文逗号分隔");
    return;
  }

  await workspaceStore.addSpaceMembers({ user_ids: ids, role: memberForm.role });
  memberForm.userIds = "";
  ElMessage.success("空间成员已更新");
}

async function changeRole(row, role) {
  try {
    await workspaceStore.changeMemberRole(row.user_id, role);
    ElMessage.success("成员角色已更新");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "角色更新失败");
  }
}

async function removeMember(row) {
  try {
    await ElMessageBox.confirm(`确认移除成员 ${row.display_name} 吗？`, "移除确认", { type: "warning" });
    await workspaceStore.deleteMember(row.user_id);
    ElMessage.success("成员已移除");
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
          <h2>按企业业务域组织知识空间</h2>
          <p class="page-subtitle">
            用空间隔离不同业务场景和权限边界，让文档、成员和问答上下文保持一致，适合客服、售前、交付、制度管理等场景。
          </p>
        </div>
      </div>
    </section>

    <section class="space-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Templates</span>
            <h2>企业场景模板</h2>
          </div>
        </div>
        <div class="template-list">
          <button v-for="item in scenarioTemplates" :key="item.name" type="button" class="template-item" @click="applyTemplate(item)">
            <strong>{{ item.name }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </div>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Create</span>
            <h2>创建知识空间</h2>
          </div>
        </div>
        <el-form label-position="top" class="panel-form">
          <el-form-item label="空间名称">
            <el-input v-model="createForm.name" placeholder="例如：客户支持知识库" />
          </el-form-item>
          <el-form-item label="空间说明">
            <el-input v-model="createForm.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-button type="primary" @click="createNewSpace">新建空间</el-button>
        </el-form>
      </article>
    </section>

    <article class="table-card">
      <div class="table-header">
        <div>
          <h3>空间列表</h3>
          <p>查看当前空间的文档规模与成员规模。</p>
        </div>
      </div>
      <el-table :data="workspaceStore.spaces" style="width: 100%">
        <el-table-column prop="name" label="空间" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="240" />
        <el-table-column prop="document_count" label="文档数" width="100" />
        <el-table-column prop="member_count" label="成员数" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="refreshCurrentSpace(row.id)">查看成员</el-button>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <article class="table-card">
      <div class="table-header">
        <div>
          <h3>成员与权限管理</h3>
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
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button
                link
                type="danger"
                :disabled="!canManageMembers || row.user_id === authStore.profile?.id"
                @click="removeMember(row)"
              >
                移除
              </el-button>
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
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.template-list,
.panel-form,
.member-form {
  display: grid;
  gap: var(--space-3);
}

.template-item {
  display: grid;
  gap: 8px;
  text-align: left;
  padding: 12px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.94));
  cursor: pointer;
}

.template-item span {
  color: var(--text-muted);
  line-height: 1.7;
}

.table-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
  padding: 14px 16px 10px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
}

.table-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.member-actions {
  padding: 12px 16px 16px;
}

@media (max-width: 1100px) {
  .space-grid {
    grid-template-columns: 1fr;
  }
}
</style>
