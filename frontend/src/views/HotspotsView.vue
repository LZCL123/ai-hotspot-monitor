<template>
  <AppShell title="热点列表" subtitle="按关键词、来源和重要性筛选采集结果">
    <div class="panel">
      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="关键词" clearable />
        <el-select v-model="filters.importance" placeholder="重要性" clearable>
          <el-option label="LOW" value="LOW" />
          <el-option label="MEDIUM" value="MEDIUM" />
          <el-option label="HIGH" value="HIGH" />
          <el-option label="CRITICAL" value="CRITICAL" />
        </el-select>
        <el-select v-model="filters.source" placeholder="来源" clearable>
          <el-option label="HackerNews" value="hackernews" />
          <el-option label="Bing" value="bing" />
        </el-select>
        <el-button type="primary" @click="load">筛选</el-button>
      </div>
      <el-table :data="rows" stripe @row-click="goDetail">
        <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="keyword" label="关键词" width="140" />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="importance" label="重要性" width="120" />
        <el-table-column prop="relevanceScore" label="相关性" width="100" />
        <el-table-column prop="createdAt" label="采集时间" width="190" />
      </el-table>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import { apiGet } from '../api/client'

interface Hotspot {
  id: number
  title: string
  keyword: string
  source: string
  importance: string
  relevanceScore: number
  createdAt: string
}

const router = useRouter()
const rows = ref<Hotspot[]>([])
const filters = reactive({ keyword: '', importance: '', source: '' })

async function load() {
  const page = await apiGet<{ records: Hotspot[] }>('/hotspots', { ...filters, page: 1, size: 50 })
  rows.value = page.records
}

function goDetail(row: Hotspot) {
  router.push(`/hotspots/${row.id}`)
}

onMounted(load)
</script>
