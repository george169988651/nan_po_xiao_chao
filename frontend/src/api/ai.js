import request from './request'
import { ElMessage } from 'element-plus'

/**
 * 同步对话:POST /ai/chat,请求体 {"message":"xxx"}
 * 会话按登录用户隔离,前端不需要传会话 id
 * @returns {Promise<{code:number, data:string}>}
 */
export const chat = (message) => request.post('/ai/chat', { message })

/**
 * 查询当前用户的历史会话记录
 * 契约:GET /ai/memory -> Result<Array<{role:'user'|'assistant', content:string, time?:string}>>
 * @returns {Promise<{code:number, data:Array<{role:string, content:string, time?:string}>}>}
 */
export const loadHistory = () => request.get('/ai/memory')

/**
 * 清空当前用户的会话记忆,契约:DELETE /ai/memory -> Result
 */
export const clearMemory = () => request.delete('/ai/memory')

/** 流式接口统一要带的认证头 */
async function authHeaders(extra = {}) {
  // 动态引入,避免 request -> store -> api -> request 的循环依赖在模块加载期触发
  const { useAuthStore } = await import('@/stores/auth')
  const auth = useAuthStore()
  return { Authorization: `Bearer ${auth.accessToken}`, ...extra }
}

/**
 * 手工解析 SSE 响应体。
 * EventSource 无法自定义请求头,所以两个流式接口都用 fetch + ReadableStream 收流,
 * 解析逻辑一样,这里抽出来复用。
 *
 * 约定格式(与后端 SseEmitter 逐条 send 的内容一致):
 *   data: 你
 *   data: 好
 *   data: [DONE]
 *
 * @param {Response} res
 * @param {{onChunk?:(chunk:string, full:string)=>void}} [handlers]
 * @returns {Promise<string>} 完整回复
 */
async function readSse(res, { onChunk } = {}) {
  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let full = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    // 按行拆 SSE,最后一段可能不完整,留在 buffer 等下一轮
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed.startsWith('data:')) continue
      const payload = trimmed.slice(5).trim()
      if (!payload) continue
      if (payload === '[DONE]') return full
      full += payload
      onChunk?.(payload, full)
    }
  }
  return full
}

/**
 * 流式对话:POST /api/ai/chat/stream
 *   请求头:Authorization: Bearer <accessToken> + Content-Type: application/json
 *   请求体:{"message": "你好"}
 *   响应:  Content-Type: text/event-stream;charset=UTF-8(格式见 readSse)
 *
 * @param {string} message
 * @param {{onChunk?:(chunk:string, full:string)=>void, signal?:AbortSignal}} [handlers]
 * @returns {Promise<string>} 完整回复
 */
export async function chatStream(message, { onChunk, signal } = {}) {
  const res = await fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: await authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ message }),
    signal,
  })

  if (!res.ok) {
    const err = new Error(`流式接口响应异常(${res.status})`)
    err.status = res.status
    throw err
  }

  ElMessage.info('开始生成回复...')
  return readSse(res, { onChunk })
}

/**
 * 多模态流式对话:发一张图片(可附带一句提问),返回对图片的分析,同样是流式。
 *
 * 【后端待实现】契约:POST /api/ai/chat/stream/image,multipart/form-data
 *   请求头:Authorization: Bearer <accessToken>
 *          —— 不要手写 Content-Type,交给浏览器补上 multipart 的 boundary
 *   表单字段:
 *     file    : 图片文件,必填
 *     message : 用户附带的问题,字符串,可缺省(缺省时由后端决定用什么提示词)
 *   响应:Content-Type: text/event-stream;charset=UTF-8,格式与 /ai/chat/stream 完全一致
 *
 * @param {File} file
 * @param {string} [message]
 * @param {{onChunk?:(chunk:string, full:string)=>void, signal?:AbortSignal}} [handlers]
 * @returns {Promise<string>} 完整回复
 */
export async function chatStreamWithImage(file, message, { onChunk, signal } = {}) {
  const form = new FormData()
  form.append('file', file)
  if (message) {
    form.append('message', message)
  }

  const res = await fetch('/api/ai/chat/stream/image', {
    method: 'POST',
    headers: await authHeaders(),
    body: form,
    signal,
  })

  if (!res.ok) {
    const err = new Error(`图片分析接口响应异常(${res.status})`)
    err.status = res.status
    throw err
  }

  ElMessage.info('正在分析图片...')
  return readSse(res, { onChunk })
}
