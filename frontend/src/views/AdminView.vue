<template>
  <AppShell title="管理日志" subtitle="数据源状态、采集日志和 AI 调用日志">
    <div class="panel">
      <div class="panel-header"><h2>数据源</h2></div>
      <el-table :data="sources" stripe>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="id" label="标识" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="panel">
      <div class="panel-header"><h2>采集日志</h2></div>
      <el-table :data="collectorLogs" stripe>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="keyword" label="关键词" width="140" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="message" label="信息" min-width="240" show-overflow-tooltip />
        <el-table-column prop="startedAt" label="开始时间" width="190" />
      </el-table>
    </div>
    <div class="panel">
      <div class="panel-header"><h2>AI 调用日志</h2></div>
      <el-table :data="aiLogs" stripe>
        <el-table-column prop="provider" label="服务" width="130" />
        <el-table-column prop="model" label="模型" width="200" />
        <el-table-column prop="inputHash" label="输入哈希" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="190" />
      </el-table>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { apiGet } from '../api/client'

const sources = ref([])
const collectorLogs = ref([])
const aiLogs = ref([])

onMounted(async () => {
  sources.value = await apiGet('/sources')
  collectorLogs.value = await apiGet('/collector-logs')
  aiLogs.value = await apiGet('/ai-logs')
})
</script>
