import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/notice',
    meta: { requiresAuth: true },
    children: [
      // 公告模块
      { path: 'notice', name: 'Notice', component: () => import('@/views/notice/NoticeList.vue') },
      { path: 'notice/:id', name: 'NoticeDetail', component: () => import('@/views/notice/NoticeDetail.vue') },
      // 报修模块
      { path: 'repair/submit', name: 'RepairSubmit', component: () => import('@/views/repair/RepairSubmit.vue') },
      { path: 'repair/list', name: 'RepairList', component: () => import('@/views/repair/RepairList.vue') },
      { path: 'repair/:id', name: 'RepairDetail', component: () => import('@/views/repair/RepairDetail.vue') },
      // 闲置模块
      { path: 'idle/list', name: 'IdleList', component: () => import('@/views/idle/IdleList.vue') },
      { path: 'idle/publish', name: 'IdlePublish', component: () => import('@/views/idle/IdlePublish.vue') },
      { path: 'idle/my', name: 'MyIdle', component: () => import('@/views/idle/MyIdle.vue') },
      { path: 'idle/audit', name: 'IdleAudit', component: () => import('@/views/idle/IdleAudit.vue'), meta: { roles: ['manager', 'admin'] } },
      { path: 'idle/order', name: 'IdleOrder', component: () => import('@/views/idle/IdleOrder.vue') },
      { path: 'idle/sales', name: 'IdleSales', component: () => import('@/views/idle/IdleSales.vue') },
      // 活动模块
      { path: 'activity/list', name: 'ActivityList', component: () => import('@/views/activity/ActivityList.vue') },
      { path: 'activity/my', name: 'MyActivities', component: () => import('@/views/activity/MyActivities.vue') },
      { path: 'activity/:id', name: 'ActivityDetail', component: () => import('@/views/activity/ActivityDetail.vue') },
      // 家政模块
      { path: 'housekeeping/list', name: 'HousekeepingList', component: () => import('@/views/housekeeping/HousekeepingList.vue') },
      { path: 'housekeeping/order', name: 'HousekeepingOrder', component: () => import('@/views/housekeeping/HousekeepingOrder.vue') },
      // 数据统计大屏
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Dashboard.vue'), meta: { roles: ['manager', 'admin'] } },
      // 用户管理
      { path: 'admin/users', name: 'UserManagement', component: () => import('@/views/admin/UserManagement.vue'), meta: { roles: ['manager', 'admin'] } },
      // 个人中心
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转到登录页，已登录访问登录页跳转到首页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) {
      next('/')  // 已登录用户访问登录页 → 跳转到首页
    } else {
      next()     // 未登录 → 正常进入登录页
    }
  } else if (to.meta.requiresAuth && !token) {
    next('/login') // 未登录访问受保护路由 → 跳登录
  } else {
    next()         // 其他情况 → 正常放行
  }
})

export default router
