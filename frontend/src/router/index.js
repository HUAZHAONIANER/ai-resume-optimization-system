import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/resume',
    name: 'resume',
    component: () => import('../views/ResumeManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/optimize',
    name: 'optimize',
    component: () => import('../views/ResumeOptimize.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/jobs',
    name: 'jobs',
    component: () => import('../views/JobList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/match',
    name: 'match',
    component: () => import('../views/JobMatch.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/resume/preview/:id',
    name: 'resumePreview',
    component: () => import('../views/ResumePreview.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 全局路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('user')
  // 如果要访问的页面需要登录 && 当前未登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'login' })
  }
  // 如果已登录却要去登录页，重定向到首页
  else if (to.name === 'login' && isLoggedIn) {
    next({ name: 'home' })
  }
  else {
    next()
  }
})

export default router
