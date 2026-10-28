import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, refresh as refreshApi } from '@/api/auth'

const STORAGE_KEY = 'npc_auth'

function loadSaved() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {}
  } catch {
    return {}
  }
}

/**
 * 登录态:双令牌 + 昵称,持久化到 localStorage,刷新页面不丢
 */
export const useAuthStore = defineStore('auth', {
  state: () => {
    const saved = loadSaved()
    return {
      accessToken: saved.accessToken || '',
      refreshToken: saved.refreshToken || '',
      nickname: saved.nickname || '',
      // access token 过期时间戳(ms),由 accessExpiresIn(分钟)推算
      expireAt: saved.expireAt || 0,
    }
  },
  getters: {
    isLogin: (state) => Boolean(state.accessToken),
  },
  actions: {
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          accessToken: this.accessToken,
          refreshToken: this.refreshToken,
          nickname: this.nickname,
          expireAt: this.expireAt,
        })
      )
    },
    clearAuth() {
      this.accessToken = ''
      this.refreshToken = ''
      this.nickname = ''
      this.expireAt = 0
      localStorage.removeItem(STORAGE_KEY)
    },
    async login(username, password) {
      const res = await loginApi({ username, password })
      const vo = res.data || {}
      this.accessToken = vo.accessToken || ''
      this.refreshToken = vo.refreshToken || ''
      this.nickname = vo.nickname || username
      this.expireAt = Date.now() + (vo.accessExpiresIn || 15) * 60 * 1000
      this.persist()
    },
    async register(payload) {
      await registerApi(payload)
    },
    /** 用 refreshToken 换发新令牌;失败抛异常,由调用方(401 拦截器)踢回登录页 */
    async doRefresh() {
      if (!this.refreshToken) {
        throw new Error('无 refreshToken')
      }
      const res = await refreshApi(this.refreshToken)
      const vo = res.data || {}
      if (!vo.accessToken) {
        throw new Error('刷新令牌无效')
      }
      this.accessToken = vo.accessToken
      if (vo.refreshToken) this.refreshToken = vo.refreshToken
      if (vo.nickname) this.nickname = vo.nickname
      this.expireAt = Date.now() + (vo.accessExpiresIn || 15) * 60 * 1000
      this.persist()
    },
    logout() {
      this.clearAuth()
      import('@/router').then(({ default: router }) => {
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
      })
    },
  },
})
