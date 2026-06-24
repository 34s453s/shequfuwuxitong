<template>
  <div class="repair-detail-page">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回列表</el-button>
    </div>

    <div class="detail-card" v-loading="loading">
      <el-descriptions :title="detail.title" :column="2" border size="large">
        <el-descriptions-item label="报修地址">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[detail.status]?.type">{{ statusMap[detail.status]?.label || detail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ detail.maintainer || '待指派' }}</el-descriptions-item>
        <el-descriptions-item label="评分" v-if="detail.rating">
          <el-rate v-model="detail.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="详细描述" :span="2">{{ detail.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="维修结果" :span="2" v-if="detail.result">{{ detail.result }}</el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2" v-if="detail.comment">{{ detail.comment }}</el-descriptions-item>
      </el-descriptions>

      <!-- 现场图片 -->
      <div class="images-section" v-if="detail.images && detail.images.length">
        <h4>现场图片</h4>
        <el-image
          v-for="(img, i) in detail.images"
          :key="i"
          :src="img"
          :preview-src-list="detail.images"
          fit="cover"
          style="width: 140px; height: 110px; margin-right: 10px; border-radius: 6px;"
          @error="handleImageError"
        />
      </div>

      <!-- 操作按钮区 -->
      <div class="action-bar">
        <!-- 居民：待确认状态可确认完工 -->
        <el-button
          v-if="role === 'resident' && detail.status === 'CONFIRMING'"
          type="success"
          @click="showConfirmDialog"
        >确认完工</el-button>

        <!-- 维修人员：处理中状态可完工上报 -->
        <el-button
          v-if="role === 'maintainer' && detail.status === 'PROCESSING'"
          type="primary"
          @click="showCompleteDialog"
        >完工上报</el-button>

        <!-- 物业管理员：待受理状态可指派 -->
        <el-button
          v-if="(role === 'manager' || role === 'admin') && detail.status === 'PENDING'"
          type="warning"
          @click="showAssignDialog"
        >指派维修人员</el-button>
      </div>
    </div>

    <!-- 确认完工弹窗（居民） -->
    <el-dialog v-model="confirmVisible" title="确认完工并评价" width="460px">
      <el-form label-width="80px">
        <el-form-item label="服务评分">
          <el-rate v-model="ratingValue" show-text :texts="['很差','较差','一般','满意','非常满意']" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="commentText" type="textarea" :rows="3" placeholder="请输入评价（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="actionLoading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 完工上报弹窗（维修人员） -->
    <el-dialog v-model="completeVisible" title="完工上报" width="460px">
      <el-form label-width="80px">
        <el-form-item label="维修结果" required>
          <el-input v-model="resultText" type="textarea" :rows="4" placeholder="请描述维修结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleComplete" :loading="actionLoading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 指派弹窗（物业管理员） -->
    <el-dialog v-model="assignVisible" title="指派维修人员" width="420px">
      <el-select v-model="selectedMaintainer" placeholder="请选择维修人员" style="width: 100%">
        <el-option v-for="m in maintainerList" :key="m.id" :label="m.name" :value="m.id" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign" :loading="actionLoading">确认指派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const statusMap = {
  PENDING:    { label: '待受理', type: 'info' },
  PROCESSING: { label: '处理中', type: '' },
  CONFIRMING: { label: '待确认', type: 'warning' },
  COMPLETED:  { label: '已完成', type: 'success' },
  RATED:      { label: '已评价', type: 'success' }
}

const loading = ref(false)
const detail = ref({})

const handleImageError = (e) => {
  const target = e.target
  // 防止循环触发
  if (target.src && target.src.startsWith('data:image/svg+xml')) {
    return
  }
  target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="140" height="110" viewBox="0 0 140 110"%3E%3Crect fill="%23f5f5f5" width="140" height="110"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="12" x="50%25" y="50%25" text-anchor="middle" dy=".3em"%3E图片加载失败%3C/text%3E%3C/svg%3E'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/repair/${route.params.id}`)
    if (res.code === 200) {
      const item = res.data || {}
      // 状态转换
      let statusStr
      switch (item.status) {
        case 0: statusStr = 'PENDING'; break
        case 1: statusStr = 'PROCESSING'; break
        case 2: statusStr = 'CONFIRMING'; break
        case 3: statusStr = 'COMPLETED'; break
        case 4: statusStr = 'RATED'; break
        default: statusStr = item.status
      }
      // 图片解析和路径处理
      let images = item.images || []
      if (typeof images === 'string') {
        try {
          images = JSON.parse(images)
        } catch (e) {
          images = []
        }
      }
      // 确保图片路径正确，添加 /uploads 前缀
      images = images.map(img => {
        if (img && typeof img === 'string') {
          if (!img.startsWith('http') && !img.startsWith('/uploads/')) {
            if (img.startsWith('/')) {
              return '/uploads' + img
            } else if (img.startsWith('uploads/')) {
              return '/' + img
            } else {
              return '/uploads/' + img
            }
          }
        }
        return img
      }).filter(Boolean)
      detail.value = {
        ...item,
        status: statusStr,
        submitTime: item.createTime,
        maintainer: item.handlerId ? '维修员' + item.handlerId : '待指派',
        images
      }
    }
  } catch (e) {
    console.error('获取工单详情失败', e)
    ElMessage.error('获取工单详情失败，请检查网络或重新登录')
    detail.value = {}
  } finally {
    loading.value = false
  }
}

// 操作相关
const actionLoading = ref(false)

// 确认完工（居民）
const confirmVisible = ref(false)
const ratingValue = ref(5)
const commentText = ref('')
const showConfirmDialog = () => { confirmVisible.value = true }
const handleConfirm = async () => {
  actionLoading.value = true
  try {
    // 先确认，再评价
    const confirmRes = await request.put(`/repair/confirm/${route.params.id}`)
    if (confirmRes.code === 200) {
      // 然后评价
      const rateRes = await request.put(`/repair/rate/${route.params.id}`, null, {
        params: { rating: ratingValue.value, comment: commentText.value }
      })
      if (rateRes.code === 200) {
        ElMessage.success('已确认完工，感谢评价')
        confirmVisible.value = false
        fetchDetail()
      }
    }
  } catch (e) { ElMessage.error('操作失败') }
  finally { actionLoading.value = false }
}

// 完工上报（维修人员）
const completeVisible = ref(false)
const resultText = ref('')
const showCompleteDialog = () => { completeVisible.value = true }
const handleComplete = async () => {
  if (!resultText.value) { ElMessage.warning('请填写维修结果'); return }
  actionLoading.value = true
  try {
    const res = await request.put(`/repair/complete/${route.params.id}`, null, {
      params: { result: resultText.value }
    })
    if (res.code === 200) {
      ElMessage.success('完工上报成功')
      completeVisible.value = false
      fetchDetail()
    }
  } catch (e) { ElMessage.error('操作失败') }
  finally { actionLoading.value = false }
}

// 指派（物业管理员）
const assignVisible = ref(false)
const selectedMaintainer = ref('')
const maintainerList = ref([{ id: '1', name: '张工' }, { id: '2', name: '李工' }, { id: '3', name: '王工' }])
const showAssignDialog = async () => {
  try {
    const res = await request.get('/repair/handlers')
    if (res.code === 200) maintainerList.value = res.data || []
  } catch (e) {}
  assignVisible.value = true
}
const handleAssign = async () => {
  if (!selectedMaintainer.value) { ElMessage.warning('请选择维修人员'); return }
  actionLoading.value = true
  try {
    const res = await request.put(`/repair/assign/${route.params.id}`, null, {
      params: { handlerId: selectedMaintainer.value }
    })
    if (res.code === 200) {
      ElMessage.success('指派成功')
      assignVisible.value = false
      fetchDetail()
    }
  } catch (e) { ElMessage.error('操作失败') }
  finally { actionLoading.value = false }
}

const goBack = () => router.push('/repair/list')

onMounted(fetchDetail)
</script>

<style scoped>
.repair-detail-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.detail-card {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.images-section { margin-top: 24px; }
.images-section h4 { color: #303133; margin-bottom: 12px; }
.action-bar { margin-top: 30px; display: flex; gap: 12px; }
</style>
