<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-icon">AI</span>
        <div>
          <strong>热点监控</strong>
          <small>Monitor Console</small>
        </div>
      </div>
      <nav>
        <router-link to="/">Dashboard</router-link>
        <router-link to="/subscriptions">关键词订阅</router-link>
        <router-link to="/hotspots">热点列表</router-link>
        <router-link to="/agent">智能客服</router-link>
        <router-link to="/admin">管理日志</router-link>
      </nav>
    </aside>
    <section class="main-area">
      <header class="topbar">
        <div>
          <h1>{{ title }}</h1>
          <p>{{ subtitle }}</p>
        </div>
        <div class="topbar-actions">
          <el-tag type="success">{{ auth.role || 'USER' }}</el-tag>
          <span class="user-name">{{ auth.username }}</span>
          <el-button @click="passwordDialogVisible = true">修改密码</el-button>
          <el-button @click="logout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <slot />
      </main>
    </section>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px">
      <el-form class="password-form" :model="passwordForm" @submit.prevent="changePassword">
        <el-form-item>
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="新密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="changePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

defineProps<{ title: string; subtitle: string }>()

const router = useRouter()
const auth = useAuthStore()
const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(() => {
  if (auth.token) {
    auth.fetchMe().catch(() => undefined)
  }
})

function logout() {
  auth.logout()
  router.push('/login')
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.error('请输入当前密码和新密码')
    return
  }
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 64) {
    ElMessage.error('新密码长度必须在 6-64 位之间')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  changingPassword.value = true
  try {
    await auth.changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码已修改，请重新登录')
    passwordDialogVisible.value = false
    resetPasswordForm()
    logout()
  } finally {
    changingPassword.value = false
  }
}
</script>
