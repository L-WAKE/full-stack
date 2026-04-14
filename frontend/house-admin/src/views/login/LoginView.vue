<script setup>
import { Lock, User } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'Admin@123'
})

async function submit() {
  loading.value = true
  try {
    await userStore.login(form)
    await userStore.bootstrap()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-shell">
    <section class="login-card">
      <div class="login-brand">
        <div class="login-brand__mark">HM</div>
        <div>
          <div class="login-brand__title">房源管理系统</div>
          <div class="login-brand__subtitle">Smart Housing Console</div>
        </div>
      </div>

      <div class="login-copy">
        <span class="login-copy__eyebrow">欢迎回来</span>
        <h1>登录后台</h1>
        <p>面向房源、客户、工单与系统配置的一体化管理入口。</p>
      </div>

      <div class="login-tip">默认演示账号：`admin / Admin@123`</div>

      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-button type="primary" class="login-button" :loading="loading" @click="submit">
          进入系统
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped lang="scss">
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 16% 18%, rgba(20, 100, 255, 0.16), transparent 28%),
    radial-gradient(circle at 84% 16%, rgba(56, 189, 248, 0.14), transparent 24%),
    linear-gradient(180deg, #f5f8fc 0%, #edf2f8 100%);
}

.login-card {
  width: min(460px, 100%);
  padding: 34px 34px 30px;
  border: 1px solid rgba(255, 255, 255, 0.78);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 26px 70px rgba(15, 23, 42, 0.10);
  backdrop-filter: blur(16px);
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.login-brand__mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  color: #0f172a;
  background: linear-gradient(180deg, #eff6ff, #dbeafe);
  border-radius: 16px;
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
  padding: 4px 10px;
  color: var(--primary);
  background: var(--primary-soft);
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
  background: rgba(248, 250, 252, 0.86);
  color: var(--text-secondary);
  font-size: 13px;
}

.login-button {
  width: 100%;
  min-height: 44px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .login-shell {
    padding: 16px;
  }

  .login-card {
    padding: 26px 22px 22px;
  }

  .login-copy h1 {
    font-size: 34px;
  }
}
</style>
