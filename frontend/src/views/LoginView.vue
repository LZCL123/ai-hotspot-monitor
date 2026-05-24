<template>
  <div class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <p class="eyebrow">AI Hotspot Monitor</p>
        <h1>面向 AI 行业的热点采集与分析工作台</h1>
        <p>登录后管理关键词订阅、触发采集、查看 AI 摘要和实时推送结果。</p>
      </div>
      <el-form class="login-form" :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="loading">登录</el-button>
        <p class="hint">默认账号：admin / admin123，可通过环境变量覆盖。</p>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function submit() {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>
