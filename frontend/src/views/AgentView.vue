<template>
  <AppShell title="智能客服" subtitle="基于已采集热点的轻量 RAG 问答">
    <section class="agent-layout">
      <div class="panel agent-chat">
        <div class="agent-messages">
          <div v-for="message in messages" :key="message.id" :class="['agent-message', message.role]">
            <span>{{ message.role === 'user' ? '你' : 'Agent' }}</span>
            <p>{{ message.content }}</p>
          </div>
        </div>
        <div class="agent-input">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="例如：最近有哪些值得关注的 AI 模型发布？"
            @keydown.ctrl.enter.prevent="send"
          />
          <el-button type="primary" :loading="loading" :disabled="!question.trim()" @click="send">
            发送
          </el-button>
        </div>
      </div>

      <aside class="panel agent-references">
        <div class="panel-header">
          <h2>引用资料</h2>
          <el-tag>{{ references.length }} 条</el-tag>
        </div>
        <el-empty v-if="references.length === 0" description="提问后显示匹配热点" />
        <div v-else class="reference-list">
          <a
            v-for="item in references"
            :key="item.id"
            class="reference-item"
            :href="item.url"
            target="_blank"
            rel="noreferrer"
          >
            <strong>{{ item.title }}</strong>
            <span>{{ item.keyword }} · {{ item.source }} · {{ item.relevanceScore ?? '-' }}</span>
            <p>{{ item.summary }}</p>
          </a>
        </div>
      </aside>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { apiPost } from '../api/client'

interface AgentReference {
  id: number
  title: string
  url: string
  source: string
  keyword: string
  summary: string
  relevanceScore?: number
}

interface AgentResponse {
  answer: string
  references: AgentReference[]
}

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
}

const question = ref('')
const loading = ref(false)
const references = ref<AgentReference[]>([])
const messages = ref<Message[]>([
  {
    id: Date.now(),
    role: 'assistant',
    content: '你可以问我已采集热点里的趋势、事件、来源和摘要。我会先检索相关热点，再基于引用回答。'
  }
])

async function send() {
  const content = question.value.trim()
  if (!content || loading.value) {
    return
  }
  messages.value.push({ id: Date.now(), role: 'user', content })
  question.value = ''
  loading.value = true
  try {
    const result = await apiPost<AgentResponse>('/agent/chat', { question: content }, { timeout: 60000 })
    references.value = result.references
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: result.answer })
  } finally {
    loading.value = false
  }
}
</script>
