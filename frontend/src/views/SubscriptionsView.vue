<template>
  <AppShell title="关键词订阅" subtitle="创建监控任务，配置数据源、相关性阈值和采集频率">
    <div class="panel">
      <div class="panel-header">
        <h2>订阅列表</h2>
        <el-button type="primary" @click="openCreate">新增订阅</el-button>
      </div>
      <el-table :data="items" stripe>
        <el-table-column prop="keyword" label="关键词" min-width="160" />
        <el-table-column prop="sourceTypes" label="数据源" min-width="180" />
        <el-table-column prop="minRelevanceScore" label="最低分" width="90" />
        <el-table-column prop="intervalMinutes" label="间隔(分钟)" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="toggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" :title="editingId ? '编辑订阅' : '新增订阅'" width="520px">
      <el-form label-position="top">
        <el-form-item label="关键词">
          <el-input v-model="form.keyword" placeholder="例如 AI Agent" />
        </el-form-item>
        <el-form-item label="数据源">
          <el-checkbox-group v-model="form.sourceTypes">
            <el-checkbox label="hackernews">HackerNews</el-checkbox>
            <el-checkbox label="bing">Bing Search</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="最低相关性">
          <el-slider v-model="form.minRelevanceScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="采集间隔">
          <el-input-number v-model="form.intervalMinutes" :min="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </AppShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import AppShell from '../components/AppShell.vue'
import { apiDelete, apiGet, apiPost, apiPut } from '../api/client'

interface Subscription {
  id: number
  keyword: string
  sourceTypes: string
  minRelevanceScore: number
  intervalMinutes: number
  enabled: boolean
}

const items = ref<Subscription[]>([])
const visible = ref(false)
const editingId = ref<number>()
const form = reactive({ keyword: '', sourceTypes: ['hackernews', 'bing'], minRelevanceScore: 60, intervalMinutes: 30, enabled: true })

async function load() {
  items.value = await apiGet<Subscription[]>('/subscriptions')
}

function openCreate() {
  editingId.value = undefined
  Object.assign(form, { keyword: '', sourceTypes: ['hackernews', 'bing'], minRelevanceScore: 60, intervalMinutes: 30, enabled: true })
  visible.value = true
}

function openEdit(row: Subscription) {
  editingId.value = row.id
  Object.assign(form, {
    keyword: row.keyword,
    sourceTypes: JSON.parse(row.sourceTypes || '["hackernews","bing"]'),
    minRelevanceScore: row.minRelevanceScore,
    intervalMinutes: row.intervalMinutes,
    enabled: row.enabled
  })
  visible.value = true
}

async function save() {
  if (editingId.value) {
    await apiPut(`/subscriptions/${editingId.value}`, form)
  } else {
    await apiPost('/subscriptions', form)
  }
  visible.value = false
  await load()
}

async function toggle(row: Subscription) {
  await apiPost(`/subscriptions/${row.id}/${row.enabled ? 'enable' : 'disable'}`)
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除这个订阅？', '删除订阅')
  await apiDelete(`/subscriptions/${id}`)
  await load()
}

onMounted(load)
</script>
