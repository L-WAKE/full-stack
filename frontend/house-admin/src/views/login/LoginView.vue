<script setup>
import { House, Lock, User } from '@element-plus/icons-vue'
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
    <div class="login-grid">
      <section class="login-showcase">
        <span class="page-eyebrow">Smart Rental Ops</span>
        <h1>更像当下主流 SaaS 的房源管理后台。</h1>
        <p>
          以现代管理后台常见的大圆角卡片、轻玻璃质感、数据看板化布局为基础，
          把房源、客户、工单和权限管理统一到同一套视觉语言里。
        </p>

        <div class="showcase-metrics">
          <div class="showcase-metric">
            <span>统一资产视图</span>
            <strong>Housing</strong>
          </div>
          <div class="showcase-metric">
            <span>流程管理闭环</span>
            <strong>Workflow</strong>
          </div>
          <div class="showcase-metric">
            <span>组织权限协同</span>
            <strong>Control</strong>
          </div>
        </div>

        <div class="showcase-list">
          <div class="showcase-list__item">
            <el-icon><House /></el-icon>
            <span>支持整租、合租、集中式房源的统一台账管理</span>
          </div>
          <div class="showcase-list__item">
            <el-icon><User /></el-icon>
            <span>租客、房东、员工资料沉淀在同一后台系统中</span>
          </div>
          <div class="showcase-list__item">
            <el-icon><Lock /></el-icon>
            <span>动态菜单、按钮权限和组织角色配置开箱即用</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card__header">
          <div>
            <div class="login-card__eyebrow">欢迎回来</div>
            <h2>登录房源管理系统</h2>
            <p>默认演示账号：admin / Admin@123</p>
          </div>
        </div>

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
  </div>
</template>

<style scoped lang="scss">
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 12% 18%, rgba(37, 99, 235, 0.20), transparent 26%),
    radial-gradient(circle at 84% 12%, rgba(16, 185, 129, 0.18), transparent 24%),
    radial-gradient(circle at 82% 84%, rgba(249, 115, 22, 0.16), transparent 26%),
    linear-gradient(135deg, #f8fbff, #eef4fa 42%, #edf3f8 100%);
}

.login-grid {
  width: min(1200px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) 420px;
  gap: 24px;
  align-items: stretch;
}

.login-showcase,
.login-card {
  position: relative;
  overflow: hidden;
  border-radius: 34px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.10);
  backdrop-filter: blur(18px);
}

.login-showcase {
  padding: 42px;
}

.login-showcase::after,
.login-card::after {
  content: "";
  position: absolute;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.12), transparent 68%);
}

.login-showcase::after {
  right: -80px;
  top: -40px;
}

.login-showcase h1 {
  max-width: 640px;
  margin: 18px 0 16px;
  font-size: clamp(38px, 5vw, 62px);
  line-height: 1.05;
  letter-spacing: -0.05em;
}

.login-showcase p {
  max-width: 620px;
  margin: 0;
  color: var(--text-muted);
  font-size: 16px;
  line-height: 1.85;
}

.showcase-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 32px;
}

.showcase-metric {
  padding: 20px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.76));
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.showcase-metric span {
  color: var(--text-muted);
  font-size: 13px;
}

.showcase-metric strong {
  display: block;
  margin-top: 14px;
  font-size: 26px;
  letter-spacing: -0.04em;
}

.showcase-list {
  display: grid;
  gap: 14px;
  margin-top: 26px;
}

.showcase-list__item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.56);
  border: 1px solid rgba(148, 163, 184, 0.14);
  color: var(--text-secondary);
}

.showcase-list__item .el-icon {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(37, 99, 235, 0.08);
  color: var(--primary);
  font-size: 18px;
}

.login-card {
  padding: 32px;
  align-self: center;
}

.login-card::after {
  left: -70px;
  bottom: -80px;
}

.login-card__header,
.login-card .el-form {
  position: relative;
  z-index: 1;
}

.login-card__eyebrow {
  color: var(--primary);
  font-size: 13px;
  font-weight: 700;
}

.login-card h2 {
  margin: 12px 0 10px;
  font-size: 30px;
  letter-spacing: -0.03em;
}

.login-card p {
  margin: 0 0 24px;
  color: var(--text-muted);
}

.login-button {
  width: 100%;
  min-height: 48px;
  margin-top: 8px;
}

@media (max-width: 1024px) {
  .login-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .login-shell {
    padding: 14px;
  }

  .login-showcase,
  .login-card {
    border-radius: 24px;
  }

  .login-showcase {
    padding: 24px;
  }

  .showcase-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
