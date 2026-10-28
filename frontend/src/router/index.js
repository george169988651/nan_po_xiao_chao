import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/menu',
    children: [
      {
        path: 'menu',
        name: 'menu',
        component: () => import('@/views/MenuView.vue'),
        meta: { title: '点餐' },
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/OrdersView.vue'),
        meta: { title: '订单' },
      },
      {
        path: 'chat',
        name: 'chat',
        component: () => import('@/views/ChatView.vue'),
        meta: { title: 'AI 服务员' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/menu' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && auth.isLogin) {
    return '/menu'
  }
  document.title = to.meta.title ? `${to.meta.title} - 南坡小炒` : '南坡小炒'
  return true
})

export default router
