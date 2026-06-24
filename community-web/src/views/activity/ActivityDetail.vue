<template>
  <div class="activity-detail-page">
    <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
    <div class="detail-card" v-loading="loading">
      <h2>{{ detail.title }}</h2>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动时间">{{ detail.startTime }} ~ {{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="活动地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="报名情况">{{ detail.signupCount }} / {{ detail.maxCount }} 人</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[detail.status]?.type">{{ statusMap[detail.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动简介" :span="2">{{ detail.description || '暂无' }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 20px" v-if="role === 'resident' && detail.status === 'SIGNING'">
        <el-button type="primary" size="large" @click="handleSignup">立即报名</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const statusMap = { SIGNING: { label: '报名中', type: 'success' }, ONGOING: { label: '进行中', type: '' }, ENDED: { label: '已结束', type: 'info' } }

const loading = ref(false)
const detail = ref({})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/activity/${route.params.id}`)
    if (res.code === 200) detail.value = res.data || {}
  } catch (e) {
    detail.value = { id: route.params.id, title: '端午节包粽子活动', startTime: '2025-06-05 09:00', endTime: '2025-06-05 12:00', location: '社区活动中心', status: 'SIGNING', signupCount: 28, maxCount: 50, description: '邀请社区居民一起包粽子，共度端午佳节！现场提供粽叶、糯米等材料，也有专业老师指导。' }
  } finally { loading.value = false }
}

const handleSignup = async () => {
  await ElMessageBox.confirm('确认报名该活动？', '报名确认')
  try {
    const res = await request.post(`/activity/signup/${route.params.id}`)
    if (res.code === 200) { ElMessage.success('报名成功！'); fetchDetail() }
    else ElMessage.error(res.message || '报名失败')
  } catch (e) { ElMessage.error('请求失败') }
}

const goBack = () => router.push('/activity/list')
onMounted(fetchDetail)
</script>

<style scoped>
.activity-detail-page { padding: 20px; }
.detail-card { background: #fff; padding: 32px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); margin-top: 20px; }
.detail-card h2 { margin: 0 0 20px; color: #303133; text-align: center; }
</style>
