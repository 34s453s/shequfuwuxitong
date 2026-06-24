<template>
  <el-container class="layout-container">
    <!-- 顶部栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <div class="brand-logo">
          <el-icon size="24" color="#409EFF"><Flag /></el-icon>
          <span class="system-title">社区便民服务</span>
        </div>
      </div>
      <div class="header-center">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
            <template v-if="index === breadcrumbItems.length - 1">{{ item.label }}</template>
            <template v-else><a href="javascript:void(0)" @click="goPath(item.path)">{{ item.label }}</a></template>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand" class="user-dropdown" trigger="click">
          <div class="user-info">
            <div class="user-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div class="user-detail">
              <span class="username">{{ userInfo.username }}</span>
              <span class="user-role" :class="roleTagType">{{ roleLabel }}</span>
            </div>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="layout-body">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="layout-aside">
        <div class="sidebar-brand">
          <el-icon size="28" color="#fff"><Flag /></el-icon>
          <span class="sidebar-title">社区服务</span>
        </div>
        <el-menu
          :default-active="currentRoute"
          router
          class="aside-menu"
          mode="vertical"
        >
          <!-- 普通居民菜单 -->
          <template v-if="role === 'resident'">
            <el-menu-item index="/notice">
              <template #icon><el-icon><Bell /></el-icon></template>
              <template #title>社区公告</template>
            </el-menu-item>
            <el-sub-menu index="/repair">
              <template #icon><el-icon><Tools /></el-icon></template>
              <template #title>便民报修</template>
              <el-menu-item index="/repair/submit">提交报修</el-menu-item>
              <el-menu-item index="/repair/list">我的报修</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="/idle">
              <template #icon><el-icon><Goods /></el-icon></template>
              <template #title>闲置流转</template>
              <el-menu-item index="/idle/list">邻里闲置</el-menu-item>
              <el-menu-item index="/idle/my">我的闲置</el-menu-item>
              <el-menu-item index="/idle/order">我的订单</el-menu-item>
              <el-menu-item index="/idle/sales">我卖出的</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="/activity">
              <template #icon><el-icon><Calendar /></el-icon></template>
              <template #title>活动中心</template>
              <el-menu-item index="/activity/list">社区活动</el-menu-item>
              <el-menu-item index="/activity/my">我的活动</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="/housekeeping">
              <template #icon><el-icon><Setting /></el-icon></template>
              <template #title>家政预约</template>
              <el-menu-item index="/housekeeping/list">预约家政</el-menu-item>
              <el-menu-item index="/housekeeping/order">我的预约</el-menu-item>
            </el-sub-menu>
          </template>

          <!-- 维修人员菜单 -->
          <template v-if="role === 'maintainer'">
            <el-menu-item index="/notice">
              <template #icon><el-icon><Bell /></el-icon></template>
              <template #title>社区公告</template>
            </el-menu-item>
            <el-menu-item index="/repair/list">
              <template #icon><el-icon><Tools /></el-icon></template>
              <template #title>我的任务</template>
            </el-menu-item>
            <el-menu-item index="/idle/list">
              <template #icon><el-icon><Goods /></el-icon></template>
              <template #title>邻里闲置</template>
            </el-menu-item>
            <el-sub-menu index="/activity">
              <template #icon><el-icon><Calendar /></el-icon></template>
              <template #title>活动中心</template>
              <el-menu-item index="/activity/list">社区活动</el-menu-item>
              <el-menu-item index="/activity/my">我的活动</el-menu-item>
            </el-sub-menu>
          </template>

          <!-- 物业管理员菜单 -->
          <template v-if="role === 'manager'">
            <el-menu-item index="/notice">
              <template #icon><el-icon><Bell /></el-icon></template>
              <template #title>公告管理</template>
            </el-menu-item>
            <el-menu-item index="/repair/list">
              <template #icon><el-icon><Tools /></el-icon></template>
              <template #title>报修管理</template>
            </el-menu-item>
            <el-menu-item index="/idle/audit">
              <template #icon><el-icon><Goods /></el-icon></template>
              <template #title>闲置审核</template>
            </el-menu-item>
            <el-menu-item index="/activity/list">
              <template #icon><el-icon><Calendar /></el-icon></template>
              <template #title>活动管理</template>
            </el-menu-item>
            <el-menu-item index="/housekeeping/list">
              <template #icon><el-icon><Home /></el-icon></template>
              <template #title>家政管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <template #icon><el-icon><Star /></el-icon></template>
              <template #title>人员管理</template>
            </el-menu-item>
            <el-menu-item index="/dashboard">
              <template #icon><el-icon><DataAnalysis /></el-icon></template>
              <template #title>数据统计</template>
            </el-menu-item>
          </template>

          <!-- 超级管理员菜单 -->
          <template v-if="role === 'admin'">
            <el-menu-item index="/notice">
              <template #icon><el-icon><Bell /></el-icon></template>
              <template #title>公告管理</template>
            </el-menu-item>
            <el-menu-item index="/repair/list">
              <template #icon><el-icon><Tools /></el-icon></template>
              <template #title>报修管理</template>
            </el-menu-item>
            <el-menu-item index="/idle/audit">
              <template #icon><el-icon><Goods /></el-icon></template>
              <template #title>闲置审核</template>
            </el-menu-item>
            <el-menu-item index="/activity/list">
              <template #icon><el-icon><Calendar /></el-icon></template>
              <template #title>活动管理</template>
            </el-menu-item>
            <el-menu-item index="/housekeeping/list">
              <template #icon><el-icon><Home /></el-icon></template>
              <template #title>家政管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <template #icon><el-icon><Star /></el-icon></template>
              <template #title>人员管理</template>
            </el-menu-item>
            <el-menu-item index="/dashboard">
              <template #icon><el-icon><DataAnalysis /></el-icon></template>
              <template #title>数据统计</template>
            </el-menu-item>
          </template>
        </el-menu>
        <div class="sidebar-footer">
          <div class="version">v1.0.0</div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  User, ArrowDown, Tools, Goods,
  Calendar, Setting, Flag, Star, DataAnalysis, SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const currentRoute = computed(() => route.path)

const roleMap = {
  resident: '居民',
  maintainer: '维修人员',
  manager: '物业管理员',
  admin: '超级管理员'
}
const roleLabel = computed(() => roleMap[role] || '未知角色')

const roleTagType = computed(() => {
  const map = { resident: 'default', maintainer: 'warning', manager: 'success', admin: 'danger' }
  return map[role] || 'default'
})

const breadcrumbItems = computed(() => {
  const pathMap = {
    '/notice': { label: '公告管理', path: '/notice' },
    '/repair': { label: '报修管理', path: '/repair/list' },
    '/repair/submit': { label: '提交报修', path: '/repair/submit' },
    '/repair/list': { label: '我的报修', path: '/repair/list' },
    '/idle': { label: '闲置流转', path: '/idle/list' },
    '/idle/list': { label: '邻里闲置', path: '/idle/list' },
    '/idle/my': { label: '我的闲置', path: '/idle/my' },
    '/idle/order': { label: '我的订单', path: '/idle/order' },
    '/idle/sales': { label: '我卖出的', path: '/idle/sales' },
    '/idle/audit': { label: '闲置审核', path: '/idle/audit' },
    '/activity': { label: '活动中心', path: '/activity/list' },
    '/activity/list': { label: '社区活动', path: '/activity/list' },
    '/activity/my': { label: '我的活动', path: '/activity/my' },
    '/housekeeping': { label: '家政服务', path: '/housekeeping/list' },
    '/housekeeping/list': { label: '预约家政', path: '/housekeeping/list' },
    '/housekeeping/order': { label: '我的预约', path: '/housekeeping/order' },
    '/admin/users': { label: '人员管理', path: '/admin/users' },
    '/dashboard': { label: '数据统计', path: '/dashboard' },
    '/profile': { label: '个人中心', path: '/profile' }
  }
  const items = []
  let currentPath = route.path
  items.push({ label: '首页', path: '/' })
  
  if (pathMap[currentPath]) {
    items.push(pathMap[currentPath])
  }
  
  return items
})

const goPath = (path) => {
  router.push(path)
}



const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: 64px;
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.header-left {
  flex: 0 0 auto;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.system-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 1px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb a {
  color: #409EFF;
  text-decoration: none;
}

.breadcrumb a:hover {
  text-decoration: underline;
}

.header-right {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  --el-button-bg-color: transparent;
  --el-button-text-color: #606266;
  --el-button-hover-bg-color: #f0f0f0;
}



.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 20px;
  transition: all 0.2s ease;
}

.user-info:hover {
  background: rgba(64, 158, 255, 0.1);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-role {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.user-role.default {
  background: #f5f5f5;
  color: #606266;
}

.user-role.warning {
  background: #fdf6ec;
  color: #E6A23C;
}

.user-role.success {
  background: #f0f9eb;
  color: #67C23A;
}

.user-role.danger {
  background: #fef0f0;
  color: #F56C6C;
}

.arrow-icon {
  font-size: 14px;
  color: #909399;
}

.layout-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 0;
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.aside-menu {
  border-right: none;
  background: #fff;
}

.aside-menu :deep(.el-menu-item),
.aside-menu :deep(.el-sub-menu__title) {
  color: #606266;
  height: 48px;
  line-height: 48px;
  margin: 0;
  border-radius: 0;
  transition: all 0.2s ease;
}

.aside-menu :deep(.el-menu-item:hover),
.aside-menu :deep(.el-sub-menu__title:hover) {
  color: #409EFF;
  background: #ecf5ff;
}

.aside-menu :deep(.el-menu-item.is-active) {
  color: #409EFF;
  background: #ecf5ff;
  font-weight: 500;
}

.aside-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #409EFF;
  border-radius: 0 2px 2px 0;
}

.aside-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #409EFF;
}

.aside-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  font-size: 13px;
}

.aside-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: #ecf5ff;
}

.aside-menu :deep(.el-sub-menu .el-menu-item.is-active::before) {
  height: 16px;
}

.sidebar-footer {
  padding: 16px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.version {
  font-size: 12px;
  color: #909399;
}

.layout-main {
  background: #f5f7fa;
  overflow-y: auto;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
