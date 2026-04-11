<template>
  <div id="appGenPage">
    <!-- Internal Top Bar -->
    <div class="top-bar">
      <div class="top-bar-left">
        <img src="/logo.png" alt="logo" class="top-logo" />
        <a-dropdown v-if="app">
          <a-space class="app-name-trigger">
            <span class="app-name-text">{{ app.appName ?? '未命名应用' }}</span>
            <DownOutlined style="font-size: 12px" />
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="goToEdit">
                <EditOutlined />
                编辑应用信息
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-tag v-if="app?.codeGenType" color="blue" class="code-gen-type-tag">
          {{ getAppTypeLabel(app.codeGenType) }}
        </a-tag>
        <span v-else-if="!app" class="app-name-text">加载中...</span>
      </div>
      <div class="top-bar-right">
        <a-button @click="appDetailVisible = true">
          <template #icon><InfoCircleOutlined /></template>
          应用详情
        </a-button>
        <a-button
          :loading="downloading"
          :disabled="!app?.codeGenType || generating"
          @click="handleDownload"
        >
          <template #icon><DownloadOutlined /></template>
          下载代码
        </a-button>
        <a-button
          type="primary"
          :loading="deploying"
          :disabled="!app?.codeGenType || generating"
          @click="handleDeploy"
        >
          <template #icon><CloudUploadOutlined /></template>
          部署
        </a-button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <!-- Left: Chat Panel -->
      <div class="chat-panel">
        <!-- Messages Area -->
        <div ref="messagesRef" class="messages-area">
          <!-- Initial history loading spinner -->
          <div v-if="historyLoading && messages.length === 0" class="history-init-loading">
            <a-spin size="small" />
            <span>加载历史消息...</span>
          </div>

          <!-- Load more (top of list → loads older messages) -->
          <div v-if="hasMoreHistory && messages.length > 0" class="load-more-bar">
            <a-spin v-if="historyLoading" size="small" />
            <span
              v-else
              class="load-more-text"
              @click="!generating && handleLoadMore()"
            >加载更多历史消息</span>
          </div>

          <!-- Empty state -->
          <div v-if="messages.length === 0 && !generating && !historyLoading" class="empty-messages">
            <img src="/logo.png" alt="logo" class="empty-logo" />
            <p>暂无对话，请在下方输入提示词开始生成</p>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <!-- AI message -->
            <template v-if="msg.role === 'assistant'">
              <div class="msg-avatar ai-avatar">
                <img src="/logo.png" alt="AI" />
              </div>
              <div class="msg-content ai-content">
                <div class="msg-bubble ai-bubble">
                  <a-spin v-if="msg.loading && !msg.content" size="small" />
                  <template v-else>
                    <div class="md-body" v-html="renderMarkdown(msg.content)" />
                    <span v-if="msg.loading" class="cursor-blink">|</span>
                  </template>
                </div>
                <div v-if="!msg.loading && msg.content" class="msg-meta">
                  已保存 · {{ formatTime(msg.time) }}
                </div>
              </div>
            </template>

            <!-- User message: avatar first in DOM so row-reverse places it on the far right -->
            <template v-else>
              <div class="msg-avatar user-avatar">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="32">
                  {{ loginUserStore.loginUser.userName?.[0] ?? 'U' }}
                </a-avatar>
              </div>
              <div class="msg-content user-content">
                <div class="msg-bubble user-bubble">
                  <span class="msg-text">{{ msg.content }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- Input Area -->
        <div class="input-area">
          <!-- Selected element info (shown when user clicked an element in edit mode) -->
          <a-alert
            v-if="selectedElement"
            type="info"
            show-icon
            closable
            class="selected-element-alert"
            @close="clearSelectedElement"
          >
            <template #message>
              <span class="selected-el-summary">
                已选中：
                <code class="el-token el-tag">&lt;{{ selectedElement.tagName }}&gt;</code>
                <code v-if="selectedElement.id" class="el-token el-id">#{{ selectedElement.id }}</code>
                <code v-if="selectedElement.classes" class="el-token el-class">.{{ selectedElement.classes.trim().split(/\s+/).filter(Boolean).join(' .') }}</code>
                <span v-if="selectedElement.text" class="el-text"> "{{ selectedElement.text.length > 40 ? selectedElement.text.slice(0, 40) + '…' : selectedElement.text }}"</span>
              </span>
            </template>
          </a-alert>

          <div class="input-row">
            <a-tooltip
              :title="!canChat ? '无法在别人的作品下对话哦~' : ''"
              placement="top"
            >
              <a-textarea
                v-model:value="inputMessage"
                placeholder="描述越详细，页面越具体，可以一步一步完善生成效果"
                :auto-size="{ minRows: 2, maxRows: 5 }"
                :disabled="generating || !canChat"
                class="chat-input"
                @keydown.ctrl.enter="handleSendMessage"
              />
            </a-tooltip>
            <a-button
              type="primary"
              shape="circle"
              :loading="generating"
              :disabled="!inputMessage.trim() || !canChat"
              class="send-btn"
              @click="handleSendMessage"
            >
              <template #icon><ArrowUpOutlined /></template>
            </a-button>
          </div>
        </div>
      </div>

      <!-- Right: Preview Panel -->
      <div class="preview-panel">
        <!-- Generating loading state -->
        <div v-if="generating" class="preview-loading">
          <a-spin size="large" />
          <p class="preview-loading-text">正在生成网站......</p>
        </div>

        <!-- Empty state: no code yet and not generating -->
        <div v-else-if="!previewUrl" class="preview-empty">
          <div class="preview-empty-content">
            <img src="/logo.png" alt="logo" class="preview-logo" />
            <p class="preview-empty-title">等待生成中</p>
            <p class="preview-empty-desc">AI 正在为您生成网站，完成后将在此处展示</p>
          </div>
        </div>

        <!-- Preview iframe -->
        <div v-else class="preview-content">
          <div class="preview-toolbar">
            <span class="preview-url-text">{{ previewUrl }}</span>
            <a-space>
              <a-button
                size="small"
                :type="isEditMode ? 'primary' : 'default'"
                :disabled="generating || !canChat"
                class="edit-mode-btn"
                @click="toggleEditMode"
              >
                <template #icon><EditOutlined /></template>
                {{ isEditMode ? '退出编辑' : '编辑模式' }}
              </a-button>
              <a-button size="small" @click="refreshPreview">
                <template #icon><ReloadOutlined /></template>
                刷新
              </a-button>
              <a-button size="small" type="primary" @click="openInNewTab">
                <template #icon><LinkOutlined /></template>
                新窗口打开
              </a-button>
            </a-space>
          </div>
          <transition name="edit-hint">
            <div v-if="isEditMode" class="edit-mode-hint">
              <span class="edit-mode-hint-dot"></span>
              <div class="edit-mode-hint-text">
                <span class="edit-mode-hint-title">编辑模式已开启</span>
                <span class="edit-mode-hint-desc">悬浮查看元素，点击选中元素</span>
              </div>
            </div>
          </transition>
          <iframe
            ref="iframeRef"
            :src="previewUrl"
            class="preview-iframe"
            sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
            @load="onIframeLoad"
          />
        </div>
      </div>
    </div>

    <!-- App Detail Modal -->
    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="app"
      :can-edit="canChat"
      @edit="goToEdit"
      @deleted="router.push('/')"
    />

    <!-- Deploy Result Modal -->
    <a-modal
      v-model:open="deployModalVisible"
      title="部署成功"
      :footer="null"
      centered
    >
      <div class="deploy-result">
        <CheckCircleOutlined class="deploy-success-icon" />
        <p class="deploy-url-label">您的应用已成功部署，访问地址：</p>
        <a-input :value="deployUrl" readonly class="deploy-url-input" />
        <div class="deploy-actions">
          <a-button type="primary" @click="openDeployUrl">
            <template #icon><LinkOutlined /></template>
            立即访问
          </a-button>
          <a-button @click="copyDeployUrl">
            <template #icon><CopyOutlined /></template>
            复制链接
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowUpOutlined,
  CheckCircleOutlined,
  CloudUploadOutlined,
  CopyOutlined,
  DownloadOutlined,
  DownOutlined,
  EditOutlined,
  InfoCircleOutlined,
  LinkOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import { useVisualEditor } from '@/utils/useVisualEditor'
import AppDetailModal from '@/components/AppDetailModal.vue'
import dayjs from 'dayjs'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { deployApp, downloadAppCode, getAppVoById } from '@/api/appController.ts'
import { CODE_GEN_TYPE_LABEL } from '@/constants/appConstant.ts'
import { listAppChatHistory } from '@/api/chatHistoryController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

// ---- Markdown renderer setup ----
const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  highlight(str, lang) {
    const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
    try {
      const highlighted = hljs.highlight(str, { language, ignoreIllegals: true }).value
      return `<pre class="md-code-block"><div class="md-code-lang">${language}</div><code class="hljs language-${language}">${highlighted}</code></pre>`
    } catch {
      return `<pre class="md-code-block"><code class="hljs">${md.utils.escapeHtml(str)}</code></pre>`
    }
  },
})

const renderMarkdown = (content: string): string => md.render(content)

import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  loading?: boolean
  time?: string
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.appId as string

const app = ref<API.AppVO | null>(null)

/** True only when the current user owns this app or is an admin */
const canChat = computed(() => {
  const userId = loginUserStore.loginUser?.id
  if (!userId || !app.value) return false
  if (loginUserStore.loginUser.userRole === 'admin') return true
  return String(app.value.userId) === String(userId)
})

const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const generating = ref(false)
const previewUrl = ref('')
const messagesRef = ref<HTMLElement | null>(null)
const iframeRef = ref<HTMLIFrameElement | null>(null)
let eventSource: EventSource | null = null

const {
  isEditMode,
  selectedElement,
  toggleEditMode,
  exitEditMode,
  clearSelectedElement,
  onIframeLoad,
  buildPromptWithContext,
  cleanup: cleanupVisualEditor,
} = useVisualEditor(iframeRef)

// ---- Chat history state ----
const historyLoading = ref(false)
const hasMoreHistory = ref(false)
/** createTime of the oldest loaded message, used as cursor for "load more" */
const oldestHistoryTime = ref<string | undefined>(undefined)

const PAGE_SIZE = 10

const loadHistory = async (loadMore = false) => {
  historyLoading.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId as unknown as number,
      pageSize: PAGE_SIZE,
    }
    if (loadMore && oldestHistoryTime.value) {
      params.lastCreateTime = oldestHistoryTime.value
    }

    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records ?? []

      // API returns newest-first; reverse so oldest is at the top
      const loginUserId = loginUserStore.loginUser?.id
      const histMsgs: ChatMessage[] = [...records].reverse().map((r) => {
        const typeUpper = (r.messageType ?? '').toUpperCase()
        let role: 'user' | 'assistant'
        if (typeUpper === 'AI' || typeUpper === 'ASSISTANT') {
          role = 'assistant'
        } else if (typeUpper === 'USER') {
          role = 'user'
        } else {
          // Fallback: if the message's userId matches current user → user msg, else AI
          role =
            loginUserId && r.userId && String(r.userId) === String(loginUserId)
              ? 'user'
              : 'assistant'
        }
        return { role, content: r.message ?? '', time: r.createTime }
      })

      if (loadMore) {
        messages.value = [...histMsgs, ...messages.value]
      } else {
        messages.value = histMsgs
      }

      // Cursor = createTime of the oldest message we have (first in the reversed list)
      if (histMsgs.length > 0) {
        oldestHistoryTime.value = histMsgs[0].time
      }

      hasMoreHistory.value = records.length >= PAGE_SIZE
    }
  } finally {
    historyLoading.value = false
  }
}

/** Load older messages while maintaining the current scroll position */
const handleLoadMore = async () => {
  const el = messagesRef.value
  const prevScrollHeight = el?.scrollHeight ?? 0
  await loadHistory(true)
  await nextTick()
  if (el) {
    el.scrollTop = el.scrollHeight - prevScrollHeight
  }
}

// ---- Deploy state ----
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// ---- Download state ----
const downloading = ref(false)

const appDetailVisible = ref(false)

const fetchAppDetail = async () => {
  try {
    const res = await getAppVoById({ id: appId as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data
    }
  } catch {
    message.error('获取应用信息失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const sendMessage = (content: string) => {
  if (generating.value) return

  messages.value.push({ role: 'user', content, time: new Date().toISOString() })
  const aiMsgIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '', loading: true, time: new Date().toISOString() })
  generating.value = true
  scrollToBottom()

  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  let streamDone = false

  const finishGeneration = () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    messages.value[aiMsgIndex].loading = false
    generating.value = false
    // Always show preview after a completed generation
    fetchAppDetail().then(() => {
      if (app.value?.codeGenType) {
        previewUrl.value = getStaticPreviewUrl(app.value.codeGenType, appId)
      }
    })
    scrollToBottom()
  }

  const url = `${API_BASE_URL}/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(content)}`
  eventSource = new EventSource(url, { withCredentials: true })

  eventSource.onmessage = (event: MessageEvent) => {
    try {
      const parsed = JSON.parse(event.data) as { d: string }
      messages.value[aiMsgIndex].content += parsed.d ?? ''
    } catch {
      messages.value[aiMsgIndex].content += event.data
    }
    scrollToBottom()
  }

  eventSource.addEventListener('done', () => {
    streamDone = true
    finishGeneration()
  })

  eventSource.onerror = () => {
    if (!streamDone) {
      finishGeneration()
    }
  }
}

const handleSendMessage = () => {
  const content = inputMessage.value.trim()
  if (!content) return
  inputMessage.value = ''
  const prompt = buildPromptWithContext(content)
  exitEditMode()
  sendMessage(prompt)
}

const handleDeploy = async () => {
  if (!app.value?.codeGenType) {
    message.warning('请先生成应用代码')
    return
  }
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      await fetchAppDetail()
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } finally {
    deploying.value = false
  }
}

const handleDownload = async () => {
  if (!app.value?.codeGenType) {
    message.warning('请先生成应用代码')
    return
  }
  downloading.value = true
  try {
    const res = await downloadAppCode(
      { appId: appId as unknown as number },
      { responseType: 'blob' },
    )
    const blob = res.data as Blob
    const disposition: string = res.headers?.['content-disposition'] ?? ''
    let fileName = `${appId}.zip`
    const match = disposition.match(/filename="?([^";\r\n]+)"?/)
    if (match?.[1]) {
      fileName = decodeURIComponent(match[1])
    }
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = fileName
    document.body.appendChild(anchor)
    anchor.click()
    document.body.removeChild(anchor)
    URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch {
    message.error('下载代码失败')
  } finally {
    downloading.value = false
  }
}

const openDeployUrl = () => window.open(deployUrl.value, '_blank')

const copyDeployUrl = () => {
  navigator.clipboard.writeText(deployUrl.value).then(() => {
    message.success('链接已复制')
  })
}

const refreshPreview = () => {
  if (iframeRef.value && previewUrl.value) {
    iframeRef.value.src = previewUrl.value
  }
}

const openInNewTab = () => {
  if (previewUrl.value) window.open(previewUrl.value, '_blank')
}

const goToEdit = () => router.push(`/app/edit/${appId}`)

const formatTime = (time?: string) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

const getAppTypeLabel = (type?: string) => {
  if (!type) return ''
  return CODE_GEN_TYPE_LABEL[type] ?? type
}

onMounted(async () => {
  await fetchAppDetail()
  await loadHistory()

  // Show preview if app is already generated and there are enough messages
  if (app.value?.codeGenType && messages.value.length >= 2) {
    previewUrl.value = getStaticPreviewUrl(app.value.codeGenType, appId)
  }

  // Scroll to newest message after loading history
  scrollToBottom()

  // Auto-send initPrompt only if: own app + no history at all
  if (canChat.value && messages.value.length === 0 && app.value?.initPrompt) {
    sendMessage(app.value.initPrompt)
  }
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  cleanupVisualEditor()
})
</script>

<style scoped>
#appGenPage {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px - 38px - 48px);
  margin: -24px;
  overflow: hidden;
  background: #f4f6f9;
}

/* Top Bar */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 46px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
  z-index: 10;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-logo {
  width: 28px;
  height: 28px;
  border-radius: 6px;
}

.app-name-trigger {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.app-name-trigger:hover {
  background: #f5f5f5;
}

.app-name-text {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.code-gen-type-tag {
  font-size: 12px;
  margin: 0;
}

/* Main Content */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* Chat Panel — 2:3 ratio with preview panel */
.chat-panel {
  flex: 2;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
  background: #fff;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* History loading states */
.history-init-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #999;
  font-size: 13px;
}

.load-more-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 4px 0 10px;
}

.load-more-text {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s;
}

.load-more-text:hover {
  color: #1677ff;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: #999;
  gap: 12px;
  padding: 60px 0;
}

.empty-logo {
  width: 56px;
  height: 56px;
  opacity: 0.35;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.user-message {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
}

.ai-avatar img {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  object-fit: contain;
  background: #f0f4ff;
  padding: 3px;
}

.msg-content {
  max-width: calc(100% - 52px);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-content {
  align-items: flex-end;
}

.ai-content {
  align-items: flex-start;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  max-width: 100%;
  word-break: break-word;
}

.user-bubble {
  background: #1677ff;
  color: #fff;
  border-radius: 12px 2px 12px 12px;
}

.ai-bubble {
  background: #f5f5f5;
  color: #1a1a1a;
  border-radius: 2px 12px 12px 12px;
}

.msg-text {
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  padding: 0;
  font-size: 13px;
  line-height: 1.6;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
  color: #1677ff;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.msg-meta {
  font-size: 11px;
  color: #aaa;
  padding: 0 4px;
}

/* Input Area */
.input-area {
  flex-shrink: 0;
  padding: 10px 14px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.chat-input {
  flex: 1;
  border-radius: 10px;
  font-size: 13px;
  resize: none;
}

.send-btn {
  flex-shrink: 0;
  margin-bottom: 2px;
}

/* Preview Panel — 3 in the 2:3 ratio */
.preview-panel {
  flex: 3;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f0f2f5;
}

.preview-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  background: #f8f9fc;
}

.preview-loading-text {
  font-size: 15px;
  color: #666;
  margin: 0;
  letter-spacing: 0.5px;
}

.preview-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-empty-content {
  text-align: center;
  color: #999;
}

.preview-logo {
  width: 64px;
  height: 64px;
  opacity: 0.3;
  margin-bottom: 16px;
}

.preview-empty-title {
  font-size: 16px;
  color: #bbb;
  margin-bottom: 8px;
}

.preview-empty-desc {
  font-size: 13px;
  color: #ccc;
}

.preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.preview-url-text {
  font-size: 12px;
  color: #666;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.preview-iframe {
  flex: 1;
  border: none;
  width: 100%;
  height: 100%;
  background: #fff;
}

/* Edit mode hint overlay (floating badge over iframe) */
.edit-mode-hint {
  position: absolute;
  top: 52px;
  right: 12px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.35);
  pointer-events: none;
  user-select: none;
}

.edit-mode-hint-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
  flex-shrink: 0;
  animation: hint-pulse 1.4s ease-in-out infinite;
}

@keyframes hint-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.75); }
}

.edit-mode-hint-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.edit-mode-hint-title {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
}

.edit-mode-hint-desc {
  font-size: 11px;
  opacity: 0.85;
  line-height: 1.3;
}

/* Fade transition for the hint */
.edit-hint-enter-active,
.edit-hint-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.edit-hint-enter-from,
.edit-hint-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* Selected element alert */
.selected-element-alert {
  margin-bottom: 8px;
  border-radius: 8px;
  font-size: 13px;
}

.selected-el-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.el-token {
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
}

.el-tag {
  background: #e6f4ff;
  color: #1677ff;
  border: 1px solid #bae0ff;
}

.el-id {
  background: #fff7e6;
  color: #d46b08;
  border: 1px solid #ffd591;
}

.el-class {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.el-text {
  color: #666;
  font-size: 12px;
  font-style: italic;
}

/* Deploy Modal */
.deploy-result {
  text-align: center;
  padding: 16px 0;
}

.deploy-success-icon {
  font-size: 48px;
  color: #52c41a;
  margin-bottom: 16px;
}

.deploy-url-label {
  color: #666;
  margin-bottom: 12px;
}

.deploy-url-input {
  margin-bottom: 20px;
}

.deploy-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>

<!-- Global styles for v-html markdown content (scoped :deep doesn't reliably style injected HTML) -->
<style>
.md-body {
  font-size: 13px;
  line-height: 1.7;
  color: #1a1a1a;
  word-break: break-word;
  overflow-wrap: break-word;
}

.md-body p { margin: 0 0 8px; }
.md-body p:last-child { margin-bottom: 0; }

.md-body h1,
.md-body h2,
.md-body h3,
.md-body h4 {
  margin: 12px 0 6px;
  font-weight: 600;
  line-height: 1.3;
  color: #111;
}
.md-body h1 { font-size: 18px; }
.md-body h2 { font-size: 16px; }
.md-body h3 { font-size: 14px; }
.md-body h4 { font-size: 13px; }

.md-body ul,
.md-body ol { margin: 4px 0 8px; padding-left: 20px; }
.md-body li { margin-bottom: 2px; }

.md-body blockquote {
  border-left: 3px solid #d0d7de;
  margin: 8px 0;
  padding: 4px 12px;
  color: #57606a;
  background: #f6f8fa;
  border-radius: 0 4px 4px 0;
}

.md-body a { color: #1677ff; text-decoration: none; }
.md-body a:hover { text-decoration: underline; }
.md-body hr { border: none; border-top: 1px solid #e8e8e8; margin: 10px 0; }

.md-body code {
  background: #eef0f3;
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  color: #d63384;
}

.md-body .md-code-block {
  margin: 10px 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f6f8fa;
  border: 1px solid #e8eaed;
}
.md-body .md-code-block code {
  background: transparent;
  padding: 12px 14px;
  color: inherit;
  font-size: 12px;
  line-height: 1.6;
  display: block;
  overflow-x: auto;
}
.md-body .md-code-lang {
  font-size: 11px;
  color: #666;
  background: #eef0f3;
  padding: 4px 12px;
  border-bottom: 1px solid #e8eaed;
  font-family: monospace;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.md-body table { border-collapse: collapse; width: 100%; margin: 8px 0; font-size: 12px; }
.md-body th,
.md-body td { border: 1px solid #d0d7de; padding: 5px 10px; }
.md-body th { background: #f6f8fa; font-weight: 600; }
</style>
