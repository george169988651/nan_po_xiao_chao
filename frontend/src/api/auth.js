import axios from 'axios'
import request from './request'

// 刷新令牌走"裸" axios:不能经过带 401 拦截的实例,否则刷新失败会递归刷新
const raw = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

/**
 * 注册
 * @param {{username:string, password:string, nickname:string}} data
 * @returns {Promise<{code:number, message:string, data:null}>}
 */
export const register = (data) => request.post('/user/register', data)

/**
 * 登录,返回双令牌
 * @param {{username:string, password:string}} data
 * @returns {Promise<{code:number, data:{accessToken:string, refreshToken:string, accessExpiresIn:number, nickname:string}}>}
 */
export const login = (data) => request.post('/user/login', data)

/**
 * 用 refreshToken 换发新令牌,返回结构与登录一致
 * @param {string} refreshToken
 */
export const refresh = (refreshToken) => raw.post('/user/refresh', { refreshToken })
