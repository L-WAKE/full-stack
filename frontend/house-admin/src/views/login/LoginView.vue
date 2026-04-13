<script setup>
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
    <div class="login-hero">
      <div class="login-copy">
        <span class="hero-label">演示版本</span>
        <h1>房源管理系统</h1>
        <p>面向租赁资产运营场景的后台系统，包含登录鉴权、动态菜单、经营看板和房源台账管理能力。</p>
        <ul>
          <li>统一房源主模型，支持整租、合租、集中式多模式管理</li>
          <li>内置菜单权限与按钮权限控制</li>
          <li>前后端分离，接入 MySQL、MyBatis-Plus 与 Redis</li>
        </ul>
      </div>
      <div class="login-card">
        <div class="login-card__title">欢迎登录</div>
        <div class="login-card__subtitle">默认账号：admin / Admin@123</div>
        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" class="login-button" :loading="loading" @click="submit">
            登录系统
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(245, 159, 0, 0.2), transparent 28%),
    radial-gradient(circle at bottom right, rgba(11, 114, 133, 0.22), transparent 26%),
    linear-gradient(135deg, #f8fbfd, #eef4f8);
}

.login-hero {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: 1.2fr 420px;
  gap: 24px;
  align-items: stretch;
}

.login-copy,
.login-card {
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(217, 226, 236, 0.9);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.08);
}

.login-copy {
  padding: 44px;
}

.hero-label {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary);
  font-weight: 700;
}

.login-copy h1 {
  font-size: 54px;
  margin: 18px 0 16px;
}

.login-copy p,
.login-copy li {
  color: var(--text-muted);
  font-size: 17px;
  line-height: 1.7;
}

.login-card {
  padding: 32px;
  align-self: center;
}

.login-card__title {
  font-size: 28px;
  font-weight: 700;
}

.login-card__subtitle {
  margin: 10px 0 24px;
  color: var(--text-muted);
}

.login-button {
  width: 100%;
  height: 44px;
}

@media (max-width: 960px) {
  .login-hero {
    grid-template-columns: 1fr;
  }

  .login-copy h1 {
    font-size: 38px;
  }
}
</style>
