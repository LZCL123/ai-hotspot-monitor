<template>
  <AppShell title="Dashboard" subtitle="今日新增、订阅活跃度和热点趋势">
    <div class="stats-grid">
      <div class="metric">
        <span>今日新增</span>
        <strong>{{ stats?.todayHotspots ?? 0 }}</strong>
      </div>
      <div class="metric accent">
        <span>高重要性</span>
        <strong>{{ stats?.highImportanceHotspots ?? 0 }}</strong>
      </div>
      <div class="metric green">
        <span>活跃订阅</span>
        <strong>{{ stats?.activeSubscriptions ?? 0 }}</strong>
      </div>
      <div class="metric">
        <span>数据源</span>
        <strong>{{ sources.length }}</strong>
      </div>
    </div>
    <div class="panel">
      <div class="panel-header">
        <h2>近 7 天热点趋势</h2>
        <el-button type="primary" :loading="refreshing" @click="refresh">立即采集</el-button>
      </div>
      <div ref="chartRef" class="chart"></div>
    </div>
    <div class="panel">
      <div class="panel-header">
        <h2>实时推送</h2>
        <el-tag>{{ messages.length }} 条</el-tag>
      </div>
      <el-empty v-if="messages.length === 0" description="暂无实时热点" />
      <div v-else class="message-list">
        <div v-for="message in messages" :key="message.hotspotId" class="message-item">
          <strong>{{ message.title }}</strong>
          <span>{{ message.keyword }} · {{ message.importance }} · {{ message.relevanceScore }}</span>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { Client } from '@stomp/stompjs'
import * as echarts from 'echarts'
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppShell from '../components/AppShell.vue'
import { apiGet, apiPost } from '../api/client'
import { simplifyApiData } from '../utils/zh'

interface Stats {
  todayHotspots: number
  highImportanceHotspots: number
  activeSubscriptions: number
  trend: { date: string; count: number }[]
}

interface PushMessage {
  hotspotId: number
  title: string
  keyword: string
  importance: string
  relevanceScore: number
}

const stats = ref<Stats>()
const sources = ref<unknown[]>([])
const chartRef = ref<HTMLElement>()
const messages = ref<PushMessage[]>([])
const refreshing = ref(false)
let chart: echarts.ECharts | undefined
let stomp: Client | undefined

async function load() {
  stats.value = await apiGet<Stats>('/hotspots/trending')
  sources.value = await apiGet<unknown[]>('/sources')
  await nextTick()
  chart = echarts.init(chartRef.value!)
  chart.setOption({
    color: ['#2563eb'],
    tooltip: {},
    grid: { left: 36, right: 18, top: 28, bottom: 30 },
    xAxis: { type: 'category', data: stats.value.trend.map((item) => item.date.slice(5)) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: stats.value.trend.map((item) => item.count) }]
  })
}

async function refresh() {
  refreshing.value = true
  try {
    const count = await apiPost<number>('/hotspots/refresh', undefined, { timeout: 120000 })
    ElMessage.success(`采集完成，新增 ${count} 条热点`)
    await load()
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  load()
  stomp = new Client({
    brokerURL: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,
    onConnect: () => {
      stomp?.subscribe('/topic/hotspots', (frame) => {
        messages.value.unshift(simplifyApiData(JSON.parse(frame.body)))
        messages.value = messages.value.slice(0, 8)
      })
    }
  })
  stomp.activate()
})

onUnmounted(() => {
  chart?.dispose()
  stomp?.deactivate()
})
</script>
