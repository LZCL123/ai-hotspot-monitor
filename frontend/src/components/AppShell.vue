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
          <el-tag type="success">WebSocket Ready</el-tag>
          <el-button @click="logout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <slot />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

defineProps<{ title: string; subtitle: string }>()

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
