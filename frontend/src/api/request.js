import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一请求实例:
 * - 请求拦截:自动附加 Authorization: Bearer <accessToken>
 * - 响应拦截:剥掉 axios 外壳直接返回 Result/PageResult;
 *   401 时用 refreshToken 无感刷新一次并重放原请求,刷新失败踢回登录页
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

request.interceptors.request.use((config) => {
  // 动态引入,避免 request -> store -> api -> request 的循环依赖在模块加载期触发
  return import('@/stores/auth').then(({ useAuthStore }) => {
    const auth = useAuthStore()
    if (auth.accessToken) {
      config.headers.Authorization = `Bearer ${auth.accessToken}`
    }
    return config
  })
})

// 并发 401 时只发起一次刷新,其余请求等同一个 Promise
let refreshingPromise = null

function clearAuthAndGoLogin() {
  return import('@/stores/auth').then(({ useAuthStore }) => {
    const auth = useAuthStore()
    auth.clearAuth()
    return import('@/router').then(({ default: router }) => {
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    })
  })
}

request.interceptors.response.use(
  (response) => {
    const res = response.data
    // Result / PageResult 都有 code 字段;非 200 视为业务失败
    if (res && typeof res.code === 'number' && res.code !== 200) {
      if (!response.config._silent) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  async (error) => {
    const { response, config } = error
    // 调用方可以通过 _silent 关掉全局错误提示(例如探测某张图片是否存在)
    const silent = config?._silent

    if (!response) {
      if (!silent) {
        ElMessage.error('网络异常,请确认后端服务已启动(localhost:8081)')
      }
      return Promise.reject(error)
    }

    // access token 过期/无效:刷新后重放一次
    if (response.status === 401 && config && !config._retried) {
      config._retried = true
      const { useAuthStore } = await import('@/stores/auth')
      const auth = useAuthStore()
      try {
        refreshingPromise = refreshingPromise || auth.doRefresh().finally(() => {
          refreshingPromise = null
        })
        await refreshingPromise
        return request(config)
      } catch (e) {
        await clearAuthAndGoLogin()
        ElMessage.error('登录已过期,请重新登录')
        return Promise.reject(error)
      }
    }

    if (!silent) {
      const msg = response.data?.message || error.message || `请求失败(${response.status})`
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
