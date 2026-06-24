<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 左侧导航 -->
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="avatar-wrapper">
            <el-avatar :size="80" :src="profile.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'">
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
          </div>
          <div class="user-info">
            <h3>{{ profile.realName || profile.username }}</h3>
            <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
          </div>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="profile-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="info">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="records">
            <el-icon><Document /></el-icon>
            <span>我的记录</span>
          </el-menu-item>
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>账号安全</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 个人信息 -->
        <div v-if="activeMenu === 'info'" class="info-section">
          <h2>个人信息</h2>
          <el-card class="info-card">
            <el-form :model="profileForm" label-width="100px">
              <el-form-item label="头像">
                <el-avatar :size="60" :src="profileForm.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                <el-button type="primary" link @click="triggerAvatarUpload">更换头像</el-button>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="profileForm.realName" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="角色">
                <el-input :value="roleLabel" disabled />
              </el-form-item>
              <el-form-item>
                <el-button v-if="!isEditing" type="primary" @click="startEdit">编辑</el-button>
                <template v-else>
                  <el-button type="primary" @click="saveProfile">保存</el-button>
                  <el-button @click="cancelEdit">取消</el-button>
                </template>
              </el-form-item>
            </el-form>
          </el-card>
          
          <!-- 统计卡片 -->
          <h2 style="margin-top: 24px;">业务统计</h2>
          <div class="stats-grid">
            <!-- 居民统计 -->
            <el-card v-if="profile.role === 'resident'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.repairCount || 0 }}</div>
                <div class="stat-label">报修总数</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'resident'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.idleCount || 0 }}</div>
                <div class="stat-label">闲置发布</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'resident'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.activitySignupCount || 0 }}</div>
                <div class="stat-label">活动报名</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'resident'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.housekeepingOrderCount || 0 }}</div>
                <div class="stat-label">家政订单</div>
              </div>
            </el-card>
            
            <!-- 维修人员统计 -->
            <el-card v-if="profile.role === 'maintainer'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.pendingCount || 0 }}</div>
                <div class="stat-label">待处理</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'maintainer'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.completedCount || 0 }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </el-card>
            
            <!-- 物业管理员统计 -->
            <el-card v-if="profile.role === 'manager'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.pendingIdleCount || 0 }}</div>
                <div class="stat-label">待审核闲置</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'manager'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.pendingRepairCount || 0 }}</div>
                <div class="stat-label">待处理报修</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'manager'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.monthActivityCount || 0 }}</div>
                <div class="stat-label">本月活动</div>
              </div>
            </el-card>
            
            <!-- 超级管理员统计 -->
            <el-card v-if="profile.role === 'admin'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.totalUserCount || 0 }}</div>
                <div class="stat-label">全站用户</div>
              </div>
            </el-card>
            <el-card v-if="profile.role === 'admin'" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ profile.totalRepairCount || 0 }}</div>
                <div class="stat-label">总工单</div>
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 我的记录 -->
        <div v-if="activeMenu === 'records'" class="records-section">
          <h2>我的记录</h2>
          <el-tabs v-model="activeRecordTab" @tab-change="handleRecordTabChange">
            <!-- 居民标签 -->
            <el-tab-pane v-if="profile.role === 'resident'" label="报修工单" name="repair" />
            <el-tab-pane v-if="profile.role === 'resident'" label="闲置物品" name="idle" />
            <el-tab-pane v-if="profile.role === 'resident'" label="活动报名" name="activity" />
            <el-tab-pane v-if="profile.role === 'resident'" label="家政订单" name="housekeeping" />
            
            <!-- 维修人员标签 -->
            <el-tab-pane v-if="profile.role === 'maintainer'" label="我的工单" name="repair_assign" />
            
            <!-- 物业标签 -->
            <el-tab-pane v-if="profile.role === 'manager'" label="发布公告" name="notice_publish" />
            
            <!-- 超级管理员标签 -->
            <el-tab-pane v-if="profile.role === 'admin'" label="操作日志" name="logs" />
          </el-tabs>
          
          <el-table :data="recordList" stripe border v-loading="recordLoading">
            <!-- 根据不同类型显示不同列 -->
            <template v-if="activeRecordTab === 'repair' || activeRecordTab === 'repair_assign'">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="address" label="地址" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="getRepairStatusType(row.status)">{{ getRepairStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" />
            </template>
            <template v-else-if="activeRecordTab === 'idle'">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="category" label="分类" />
              <el-table-column prop="price" label="价格" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="getIdleStatusType(row.status)">{{ getIdleStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
            </template>
            <template v-else-if="activeRecordTab === 'activity'">
              <el-table-column prop="activityId" label="活动ID" />
              <el-table-column prop="signupTime" label="报名时间" />
            </template>
            <template v-else-if="activeRecordTab === 'housekeeping'">
              <el-table-column prop="serviceId" label="服务ID" />
              <el-table-column prop="appointTime" label="预约时间" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="getHousekeepingStatusType(row.status)">{{ getHousekeepingStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
            </template>
            <template v-else-if="activeRecordTab === 'notice_publish'">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="isTop" label="置顶">
                <template #default="{ row }">
                  <el-tag v-if="row.isTop === 1" type="warning">置顶</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="发布时间" />
            </template>
            <template v-else>
              <el-table-column prop="message" label="内容" />
              <el-table-column prop="createTime" label="时间" />
            </template>
          </el-table>
          
          <el-pagination
            v-model:current-page="recordPage"
            v-model:page-size="recordSize"
            :total="recordTotal"
            layout="total, prev, pager, next"
            @current-change="fetchRecords"
          />
        </div>
        
        <!-- 账号安全 -->
        <div v-if="activeMenu === 'security'" class="security-section">
          <h2>账号安全</h2>
          <el-card class="security-card">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <div style="margin-top: 24px;">
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 隐藏的文件上传 -->
    <input type="file" ref="fileInput" accept="image/*" style="display: none" @change="handleFileChange" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Lock, Camera } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const profile = ref({})
const activeMenu = ref('info')
const isEditing = ref(false)
const profileForm = reactive({})
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 记录相关
const activeRecordTab = ref('')
const recordList = ref([])
const recordPage = ref(1)
const recordSize = ref(10)
const recordTotal = ref(0)
const recordLoading = ref(false)
const fileInput = ref(null)

const roleLabel = computed(() => {
  const labels = {
    resident: '普通居民',
    maintainer: '维修人员',
    manager: '物业管理员',
    admin: '超级管理员',
    housekeeper: '家政服务员'
  }
  return labels[profile.value.role] || profile.value.role
})

const roleTagType = computed(() => {
  const types = {
    resident: 'info',
    maintainer: 'primary',
    manager: 'success',
    admin: 'danger',
    housekeeper: 'warning'
  }
  return types[profile.value.role] || ''
})

const fetchProfile = async () => {
  try {
    const res = await request.get('/user/profile')
    if (res.code === 200) {
      profile.value = res.data
      Object.assign(profileForm, res.data)
      // 设置默认的记录tab
      if (profile.value.role === 'resident') {
        activeRecordTab.value = 'repair'
      } else if (profile.value.role === 'maintainer') {
        activeRecordTab.value = 'repair_assign'
      } else if (profile.value.role === 'manager') {
        activeRecordTab.value = 'notice_publish'
      } else if (profile.value.role === 'admin') {
        activeRecordTab.value = 'logs'
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const handleMenuSelect = (key) => {
  activeMenu.value = key
  if (key === 'records') {
    fetchRecords()
  }
}

const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  Object.assign(profileForm, profile.value)
}

const saveProfile = async () => {
  try {
    const res = await request.put('/user/profile', {
      username: profileForm.username,
      realName: profileForm.realName,
      phone: profileForm.phone
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
      profile.value.username = profileForm.username
      profile.value.realName = profileForm.realName
      profile.value.phone = profileForm.phone
      // 更新localStorage
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.username = profileForm.username
      userInfo.realName = profileForm.realName
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const triggerAvatarUpload = () => {
  fileInput.value.click()
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!beforeAvatarUpload(file)) return
  await handleAvatarUpload({ file })
  e.target.value = ''
}

const handleAvatarUpload = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await request.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      ElMessage.success('头像上传成功')
      profile.value.avatar = res.data.avatar
      profileForm.avatar = res.data.avatar
      // 更新localStorage
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.avatar = res.data.avatar
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = await request.put('/user/password', passwordForm)
      if (res.code === 200) {
        ElMessage.success('密码修改成功')
        passwordFormRef.value.resetFields()
      } else {
        ElMessage.error(res.message || '密码修改失败')
      }
    } catch (e) {
      ElMessage.error('密码修改失败')
    }
  })
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } catch {
    // 取消
  }
}

const handleRecordTabChange = () => {
  recordPage.value = 1
  fetchRecords()
}

const fetchRecords = async () => {
  if (!activeRecordTab.value) return
  recordLoading.value = true
  try {
    const res = await request.get('/user/records', {
      params: {
        type: activeRecordTab.value,
        page: recordPage.value,
        size: recordSize.value
      }
    })
    if (res.code === 200) {
      recordList.value = res.data?.records || []
      recordTotal.value = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    recordLoading.value = false
  }
}

// 状态标签函数
const getRepairStatusType = (status) => {
  const types = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'success' }
  return types[status] || ''
}
const getRepairStatusLabel = (status) => {
  const labels = { 0: '待受理', 1: '处理中', 2: '待确认', 3: '已完成', 4: '已评价' }
  return labels[status] || status
}

const getIdleStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'primary', 3: 'danger' }
  return types[status] || ''
}
const getIdleStatusLabel = (status) => {
  const labels = { 0: '待审核', 1: '已发布', 2: '已售出', 3: '已下架' }
  return labels[status] || status
}

const getHousekeepingStatusType = (status) => {
  const types = { 0: 'info', 1: 'primary', 2: '', 3: 'success', 4: 'success' }
  return types[status] || ''
}
const getHousekeepingStatusLabel = (status) => {
  const labels = { 0: '已下单', 1: '已接单', 2: '服务中', 3: '已完成', 4: '已评价' }
  return labels[status] || status
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.profile-container {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 0 0 40px 40px;
}

.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-uploader {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.user-info h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.profile-menu {
  border: none;
}

.profile-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.profile-content h2 {
  margin: 0 0 20px;
  font-size: 20px;
}

.info-card, .security-card {
  max-width: 600px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  cursor: default;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
}

.records-section .el-tabs {
  margin-bottom: 16px;
}

.records-section .el-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
