import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '仪表盘', icon: 'HomeOutline' }
        },
        {
          path: 'agent/hospital',
          name: 'Hospital',
          component: () => import('@/views/agent/hospital/index.vue'),
          meta: { title: '在线医院', icon: 'BuildOutline' }
        },
        {
          path: 'agent/log',
          name: 'LogQuery',
          component: () => import('@/views/agent/log/index.vue'),
          meta: { title: '日志查询', icon: 'SearchOutline' }
        },
        {
          path: 'agent/log/:logId',
          name: 'LogDetail',
          component: () => import('@/views/agent/log/detail.vue'),
          meta: { title: '日志明细', icon: 'DocumentTextOutline' }
        },
        {
          path: 'system/user',
          name: 'User',
          component: () => import('@/views/system/user/index.vue'),
          meta: { title: '用户管理', icon: 'PersonOutline' }
        },
        {
          path: 'system/role',
          name: 'Role',
          component: () => import('@/views/system/role/index.vue'),
          meta: { title: '角色管理', icon: 'PeopleOutline' }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }
  if (!token) {
    next(`/login?redirect=${to.path}`)
    return
  }
  next()
})

export default router
