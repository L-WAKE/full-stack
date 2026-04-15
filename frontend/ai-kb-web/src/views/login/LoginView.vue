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
  password: "demo",
});

async function submit() {
  try {
    loading.value = true;
    await authStore.login(form);
    await authStore.hydrate();
    ElMessage.success("登录成功");
    router.push("/dashboard");
  } catch (error) {
    ElMessage.error(error?.response?.data?.detail || "登录失败，请检查账号密码");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-shell">
    <section class="login-card">
      <div class="login-brand">
        <div class="login-brand__mark">AI</div>
        <div>
          <div class="login-brand__title">AI 知识库系统</div>
          <div class="login-brand__subtitle">AI Knowledge Base Console</div>
        </div>
      </div>

      <div class="login-copy">
        <span class="login-copy__eyebrow">欢迎回来</span>
        <h1>登录后台</h1>
        <p>面向知识空间、文档解析、检索问答与模型配置的一体化管理入口。</p>
      </div>

      <div class="login-tip">默认演示账号：admin / demo</div>

      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-button type="primary" class="login-button" :loading="loading" @click="submit">
          进入系统
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: var(--space-6);
  background:
    radial-gradient(circle at 16% 18%, rgba(23, 104, 255, 0.16), transparent 28%),
    radial-gradient(circle at 84% 16%, rgba(15, 159, 110, 0.12), transparent 22%),
    linear-gradient(180deg, #f7faff 0%, #eef4fb 100%);
}

.login-card {
  width: min(460px, 100%);
  padding: 34px 34px 30px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-panel);
  backdrop-filter: blur(16px);
}

.login-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.login-brand__mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  color: var(--text-main);
  background: linear-gradient(180deg, #eff5ff, #dce8ff);
  border-radius: var(--radius-md);
  font-size: 18px;
  font-weight: 800;
}

.login-brand__title {
  font-size: 20px;
  font-weight: 800;
}

.login-brand__subtitle {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.login-copy {
  margin-top: 28px;
}

.login-copy__eyebrow {
  display: inline-block;
  padding: 5px 12px;
  color: var(--primary);
  background: var(--primary-soft);
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 700;
}

.login-copy h1 {
  margin: 14px 0 10px;
  font-size: 42px;
  line-height: 1.02;
  letter-spacing: -0.05em;
}

.login-copy p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.login-tip {
  margin: 22px 0 20px;
  padding: 12px 14px;
  border: 1px solid var(--line-color);
  border-radius: var(--radius-md);
  background: rgba(248, 250, 252, 0.86);
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.login-button {
  width: 100%;
  min-height: 46px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .login-shell {
    padding: var(--space-4);
  }

  .login-card {
    padding: 26px 22px 22px;
    border-radius: var(--radius-lg);
  }

  .login-copy h1 {
    font-size: 34px;
  }
}
</style>
