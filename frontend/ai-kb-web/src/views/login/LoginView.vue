<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";

import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const form = reactive({
  username: "admin",
  password: "123456",
});

async function submit() {
  try {
    loading.value = true;
    await authStore.login(form);
    await authStore.hydrate();
    router.push("/dashboard");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "登录失败，请检查账号密码");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-copy">
        <p class="eyebrow">House AI Knowledge Base</p>
        <h1>为房源业务搭一个可持续演进的 AI 知识库</h1>
        <p>
          当前版本先完成登录、空间、文档、聊天与 SSE 基础链路，后续继续接解析、切片、向量化与权限隔离。
        </p>
      </div>
      <el-card shadow="never" class="login-card">
        <div class="card-title">登录系统</div>
        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="账号">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="full-width" @click="submit">
            进入控制台
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
}

.login-panel {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.2fr 420px;
  gap: 28px;
  align-items: stretch;
}

.login-copy {
  padding: 36px;
  border-radius: 28px;
  background: linear-gradient(135deg, rgba(9, 57, 126, 0.96), rgba(19, 145, 127, 0.92));
  color: #f5fbff;
  box-shadow: 0 24px 80px rgba(7, 28, 76, 0.18);
}

.login-copy h1 {
  margin: 18px 0;
  font-size: 42px;
  line-height: 1.15;
}

.eyebrow {
  letter-spacing: 0.16em;
  text-transform: uppercase;
  opacity: 0.72;
}

.login-card {
  border-radius: 28px;
  padding: 12px;
}

.full-width {
  width: 100%;
}
</style>
