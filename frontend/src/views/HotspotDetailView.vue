<template>
  <AppShell title="热点详情" subtitle="AI 摘要、评分、标签和原始链接">
    <div v-if="item" class="detail-layout">
      <section class="panel">
        <div class="detail-title">
          <el-tag>{{ item.source }}</el-tag>
          <h2>{{ item.title }}</h2>
          <a :href="item.url" target="_blank" rel="noreferrer">打开原文</a>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="关键词">{{ item.keyword }}</el-descriptions-item>
          <el-descriptions-item label="重要性">{{ item.importance }}</el-descriptions-item>
          <el-descriptions-item label="相关性">{{ item.relevanceScore }}</el-descriptions-item>
          <el-descriptions-item label="事件类型">{{ item.eventType }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ item.publishedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采集时间">{{ item.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </section>
      <section class="panel">
        <h2>AI 摘要</h2>
        <p class="summary">{{ item.summary }}</p>
        <div class="tag-row">
          <el-tag v-for="tag in tags" :key="tag" type="info">{{ tag }}</el-tag>
        </div>
      </section>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import { apiGet } from '../api/client'

interface Hotspot {
  id: number
  title: string
  url: string
  source: string
  keyword: string
  summary: string
  relevanceScore: number
  importance: string
  eventType: string
  tags: string
  publishedAt: string
  createdAt: string
}

const route = useRoute()
const item = ref<Hotspot>()
const tags = computed(() => {
  try {
    return JSON.parse(item.value?.tags || '[]')
  } catch {
    return []
  }
})

onMounted(async () => {
  item.value = await apiGet<Hotspot>(`/hotspots/${route.params.id}`)
})
</script>
