<template>
  <div class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <p class="eyebrow">AI Hotspot Monitor</p>
        <h1>面向 AI 行业的热点采集与分析工作台</h1>
        <p>登录后管理关键词订阅、触发采集、查看 AI 摘要和实时推送结果。</p>
      </div>
      <el-form class="login-form" :model="form" @submit.prevent="submit">
        <el-segmented v-model="mode" :options="modeOptions" @change="switchMode" />
        <el-form-item>
          <el-input v-model.trim="form.username" size="large" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item v-if="isRegister">
          <el-input
            v-model="form.confirmPassword"
            size="large"
            type="password"
            placeholder="确认密码"
            show-password
          />
        </el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="loading">
          {{ isRegister ? '注册并登录' : '登录' }}
        </el-button>
        <p class="hint" v-if="isRegister">用户名 3-64 位，密码 6-64 位。注册成功后会自动登录。</p>
        <p class="hint" v-else>默认账号：admin / admin123，也可以切换到注册创建新用户。</p>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const mode = ref(route.path === '/register' ? 'register' : 'login')
const modeOptions = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' }
]
const form = reactive({ username: 'admin', password: 'admin123', confirmPassword: '' })
const isRegister = computed(() => mode.value === 'register')

watch(
  () => route.path,
  (path) => {
    mode.value = path === '/register' ? 'register' : 'login'
    if (mode.value === 'register' && form.username === 'admin' && form.password === 'admin123') {
      form.username = ''
      form.password = ''
    }
  }
)

function switchMode(value: string | number | boolean) {
  router.replace(value === 'register' ? '/register' : '/login')
}

function validateForm() {
  if (!form.username || !form.password) {
    ElMessage.error('请输入用户名和密码')
    return false
  }
  if (!isRegister.value) {
    return true
  }
  if (form.username.length < 3 || form.username.length > 64) {
    ElMessage.error('用户名长度必须在 3-64 位之间')
    return false
  }
  if (form.password.length < 6 || form.password.length > 64) {
    ElMessage.error('密码长度必须在 6-64 位之间')
    return false
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return false
  }
  return true
}

async function submit() {
  if (!validateForm()) {
    return
  }
  loading.value = true
  try {
    if (isRegister.value) {
      await auth.register(form.username, form.password)
      ElMessage.success('注册成功')
    } else {
      await auth.login(form.username, form.password)
    }
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>
