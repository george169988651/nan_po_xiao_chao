<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { chat, chatStream, chatStreamWithImage, clearMemory, loadHistory } from '@/api/ai'
import { orderByAi } from '@/api/order'

// 与后端 spring.servlet.multipart.max-file-size 保持一致
const MAX_IMAGE_SIZE = 10 * 1024 * 1024

// 招呼语:新会话和清空之后,列表第一项始终是它
const GREETING = {
  role: 'assistant',
  content:
    '你好,我是南坡小炒的 AI 服务员。\n可以直接跟我聊天,也可以说"来一份鱼香肉丝"让我帮你下单。',
}

const messages = ref([{ ...GREETING }])

const input = ref('')
const sending = ref(false)
const streaming = ref(false)
const aiOrdering = ref(false)
const streamFailedOnce = ref(false) // 流式接口不可用时提示一次
let abortController = null

// 待发送的图片:{ file, url }
const pendingImage = ref(null)
const fileInputRef = ref()

// 所有创建过的 objectURL 都登记在这里,统一在清空会话/离开页面时释放
const objectUrls = new Set()

const canSend = computed(() => Boolean(input.value.trim() || pendingImage.value))

const listRef = ref()

async function scrollToBottom() {
  await nextTick()
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight
  }
}

function stopStream() {
  abortController?.abort()
}

function trackUrl(url) {
  objectUrls.add(url)
  return url
}

function releaseUrl(url) {
  if (url && objectUrls.delete(url)) {
    URL.revokeObjectURL(url)
  }
}

/** 清空会话或离开页面时,把登记过的 objectURL 全部释放 */
function releaseAll() {
  objectUrls.forEach((url) => URL.revokeObjectURL(url))
  objectUrls.clear()
  pendingImage.value = null
}

function pickImage() {
  fileInputRef.value?.click()
}

function onPickImage(e) {
  const file = e.target.files?.[0]
  // 清掉 input 的值,否则连续两次选同一个文件不会再触发 change
  e.target.value = ''
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.warning('只能选择图片文件')
    return
  }
  if (file.size > MAX_IMAGE_SIZE) {
    ElMessage.warning('图片不能超过 10MB')
    return
  }

  // 换一张就释放上一张的预览
  releaseUrl(pendingImage.value?.url)
  pendingImage.value = { file, url: trackUrl(URL.createObjectURL(file)) }
}

function removePendingImage() {
  releaseUrl(pendingImage.value?.url)
  pendingImage.value = null
}

/** 进入页面先拉服务端历史,接着上一次的会话继续聊 */
async function loadServerHistory() {
  try {
    const res = await loadHistory()
    const list = Array.isArray(res?.data) ? res.data : []
    // 只展示 user/assistant,工具调用等中间消息不显示
    const shown = list
      .filter((m) => m && (m.role === 'user' || m.role === 'assistant') && m.content)
      .map((m) => ({ role: m.role, content: m.content }))
    messages.value = [{ ...GREETING }, ...shown]
    scrollToBottom()
  } catch {
    // 拉不到就当新会话,别挡着聊天(错误提示已由请求拦截器统一弹出)
    messages.value = [{ ...GREETING }]
  }
}

async function resetConversation() {
  stopStream()
  try {
    await clearMemory()
    messages.value = [{ ...GREETING }]
    // 图片是前端本地数据,服务端清理成功后一并释放
    releaseAll()
    ElMessage.success('会话记忆已清空')
  } catch {
    // 清空失败就保持现状,免得前端显示的和服务端存的对不上
  }
}

async function send() {
  const text = input.value.trim()
  const image = pendingImage.value
  // 可以只发图片(让后端按默认提示词分析),也可以图文一起发
  if ((!text && !image) || sending.value) return

  input.value = ''
  sending.value = true
  // 图片的 objectURL 继续挂在消息上,由 releaseAll 统一回收
  messages.value.push({ role: 'user', content: text, imageUrl: image?.url })
  pendingImage.value = null

  const reply = reactive({ role: 'assistant', content: '', failed: false })
  messages.value.push(reply)
  scrollToBottom()

  try {
    streaming.value = true
    abortController = new AbortController()
    const handlers = {
      signal: abortController.signal,
      onChunk: (_chunk, fullText) => {
        reply.content = fullText
        scrollToBottom()
      },
    }
    // 带图片走多模态接口,纯文字走原来的文本接口
    const full = image
      ? await chatStreamWithImage(image.file, text, handlers)
      : await chatStream(text, handlers)
    reply.content = full || reply.content
  } catch (err) {
    if (err?.name === 'AbortError') {
      reply.content = reply.content || '(已停止)'
    } else if (image) {
      // 图片接口没有同步版本可降级,只能如实报错
      reply.failed = true
      reply.content =
        err?.status === 404 || err?.status === 405
          ? '后端还没有实现图片分析接口(POST /ai/chat/stream/image)'
          : '图片分析失败,请稍后再试'
    } else {
      // 流式接口不通时降级为同步 POST /ai/chat
      try {
        const res = await chat(text)
        reply.content = res.data
        if (!streamFailedOnce.value) {
          streamFailedOnce.value = true
          ElMessage.info('流式接口未就绪,本次使用同步模式返回')
        }
      } catch (e2) {
        reply.failed = true
        reply.content = 'AI 服务暂时不可用,请稍后再试'
      }
    }
  } finally {
    streaming.value = false
    sending.value = false
    abortController = null
    scrollToBottom()
  }
}

/** 第18课:结构化下单——说句话,后端解析成订单并落库 */
async function aiOrder() {
  const text = input.value.trim()
  if (!text || sending.value || streaming.value) return
  input.value = ''
  sending.value = true
  aiOrdering.value = true
  messages.value.push({ role: 'user', content: text })
  const reply = reactive({ role: 'assistant', content: '', failed: false })
  messages.value.push(reply)
  scrollToBottom()

  try {
    const res = await orderByAi(text)
    const order = res.data || {}
    const items = (order.itemList || [])
      .map((item) => `${item.dishName} ×${item.count}`)
      .join('、')
    reply.content = `已下单:${items || '无菜品'}\n订单号 ${order.orderId},合计 ¥${order.totalMoney} 元`
  } catch {
    reply.failed = true
    reply.content = 'AI 下单失败,换个说法再试一次'
  } finally {
    sending.value = false
    aiOrdering.value = false
    scrollToBottom()
  }
}

function onInputKeydown(e) {
  // Enter 发送,Shift+Enter 换行
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

onMounted(() => {
  // 每次进入页面都先续上服务端保存的历史
  loadServerHistory()
})

onBeforeUnmount(() => {
  stopStream()
  releaseAll()
})
</script>

<template>
  <el-card
    shadow="never"
    class="chat-card"
    :body-style="{ padding: 0, display: 'flex', flexDirection: 'column', height: '100%' }"
  >
    <div class="chat-toolbar">
      <span>会话按登录用户隔离,服务端有记忆</span>
      <el-button link :disabled="streaming" @click="resetConversation">清空会话</el-button>
    </div>

    <div ref="listRef" class="chat-list">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="chat-row"
        :class="msg.role === 'user' ? 'row-user' : 'row-ai'"
      >
        <div class="avatar" :class="{ 'avatar-ai': msg.role !== 'user' }">
          {{ msg.role === 'user' ? '我' : 'AI' }}
        </div>
        <div class="bubble" :class="{ 'bubble-failed': msg.failed }">
          <img
            v-if="msg.imageUrl"
            class="bubble-image"
            :src="msg.imageUrl"
            alt="用户上传的图片"
          />
          <span v-if="msg.content" class="bubble-text">{{ msg.content }}</span
          ><span
            v-if="msg.role === 'assistant' && streaming && idx === messages.length - 1"
            class="cursor"
            >▍</span
          >
        </div>
      </div>
    </div>

    <div class="chat-input">
      <!-- 待发送的图片预览 -->
      <div v-if="pendingImage" class="pending">
        <img class="pending-thumb" :src="pendingImage.url" alt="待发送的图片" />
        <span class="pending-name">{{ pendingImage.file.name }}</span>
        <el-button link class="pending-remove" @click="removePendingImage">移除</el-button>
      </div>

      <el-input
        v-model="input"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="输入消息,Enter 发送,Shift+Enter 换行"
        :disabled="sending"
        @keydown="onInputKeydown"
      />
      <div class="chat-actions">
        <div class="actions-left">
          <input
            ref="fileInputRef"
            class="file-input"
            type="file"
            accept="image/*"
            @change="onPickImage"
          />
          <el-button :icon="Picture" :disabled="sending" @click="pickImage">图片</el-button>
        </div>
        <div class="actions-right">
          <el-button v-if="streaming" plain @click="stopStream">停止</el-button>
          <el-button
            :loading="aiOrdering"
            :disabled="!input.trim() || sending || streaming"
            @click="aiOrder"
          >
            AI 下单
          </el-button>
          <el-button type="primary" :loading="sending" :disabled="!canSend" @click="send">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.chat-card {
  height: calc(100vh - var(--npc-header-h) - 40px);
  overflow: hidden;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--npc-border);
  font-size: 13px;
  color: var(--npc-text-3);
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
}

.chat-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.row-user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
  background: var(--npc-surface);
  font-size: 12px;
  line-height: 30px;
  text-align: center;
  color: var(--npc-text-3);
}

.avatar-ai {
  color: var(--npc-accent);
  border-color: var(--el-color-primary-light-7);
}

.bubble {
  max-width: 70%;
  padding: 10px 14px;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
  background: var(--npc-surface);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.row-user .bubble {
  border-color: var(--el-color-primary-light-7);
  background: var(--el-color-primary-light-9);
}

.bubble-failed {
  color: var(--el-color-danger);
}

/* 气泡里的图片:只看缩略,不铺大图 */
.bubble-image {
  display: block;
  max-width: 160px;
  max-height: 160px;
  margin-bottom: 8px;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
}

/* 流式生成时的光标:静态方块,不做闪烁动画 */
.cursor {
  margin-left: 2px;
  color: var(--npc-accent);
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--npc-border);
}

/* 待发送图片:一条描边的小卡片,不占地方 */
.pending {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  padding: 8px;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
}

.pending-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
}

.pending-name {
  max-width: 260px;
  overflow: hidden;
  font-size: 12px;
  color: var(--npc-text-3);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-remove {
  margin-left: auto;
}

/* 原生 file input 藏在按钮后面 */
.file-input {
  display: none;
}

.chat-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.actions-right {
  display: flex;
  gap: 8px;
}
</style>
