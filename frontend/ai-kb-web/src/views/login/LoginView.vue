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
    <section class="login-hero">
      <span class="login-kicker">Enterprise RAG Platform</span>
      <h1>AI Knowledge Base</h1>
      <p>把文档治理、向量检索、可追溯问答和模型策略组织成一套硬朗、清晰、可运营的企业知识系统。</p>
    </section>

    <section class="login-card">
      <div class="login-brand">
        <div class="login-brand__mark">AI</div>
        <div>
          <div class="login-brand__title">知识库控制台</div>
          <div class="login-brand__subtitle">Sharp interface for precise answers</div>
        </div>
      </div>

      <div class="login-copy">
        <span class="login-copy__eyebrow">Access</span>
        <h2>登录后台</h2>
        <p>默认演示账号：admin / demo</p>
      </div>

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
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 0.9fr);
  background: var(--white);
}

.login-hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 72px;
  color: var(--white);
  background:
    linear-gradient(135deg, rgba(28, 105, 212, 0.76), rgba(28, 105, 212, 0) 42%),
    linear-gradient(180deg, #1a1a1a, #262626);
}

.login-kicker,
.login-copy__eyebrow {
  color: var(--silver);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.login-hero h1 {
  max-width: 760px;
  margin: 18px 0 16px;
  font-size: clamp(52px, 8vw, 96px);
  font-weight: 300;
  line-height: 1.04;
  text-transform: uppercase;
}

.login-hero p {
  max-width: 540px;
  margin: 0;
  color: var(--silver);
  font-size: 16px;
  line-height: 1.45;
}

.login-card {
  align-self: center;
  width: min(460px, calc(100% - 48px));
  justify-self: center;
  padding: 32px;
  border: 1px solid var(--border);
  background: var(--white);
}

.login-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.login-brand__mark {
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  color: var(--white);
  background: var(--bmw-blue);
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.login-brand__title {
  font-size: 20px;
  font-weight: 900;
}

.login-brand__subtitle {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 12px;
}

.login-copy {
  margin: 30px 0 20px;
}

.login-copy h2 {
  margin: 10px 0 8px;
  font-size: 36px;
  font-weight: 300;
  line-height: 1.1;
  text-transform: uppercase;
}

.login-copy p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.6;
}

.login-button {
  width: 100%;
  min-height: 46px;
  margin-top: 8px;
}

@media (max-width: 900px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-hero {
    min-height: 42vh;
    padding: 48px 24px;
  }

  .login-card {
    width: calc(100% - 32px);
    margin: 24px 0;
  }
}
</style>
