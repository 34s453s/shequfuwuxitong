<template>
  <div class="activity-list-page">
    <div class="page-header">
      <h3>社区活动</h3>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="活动状态" clearable @change="fetchList" style="width: 130px">
          <el-option label="报名中" value="SIGNING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
        <el-button v-if="role === 'manager' || role === 'admin'" type="primary" @click="showPublishDialog()">发布活动</el-button>
      </div>
    </div>

    <div class="activity-grid" v-loading="loading">
      <el-card v-for="item in list" :key="item.id" shadow="hover" class="activity-card">
        <div class="activity-cover">
          <el-image :src="item.coverImg" fit="cover" style="width:100%;height:180px;" />
          <el-tag class="status-badge" :type="statusMap[item.status]?.type" size="small">
            {{ statusMap[item.status]?.label }}
          </el-tag>
        </div>
        <div class="activity-info">
          <h4>{{ item.title }}</h4>
          <p><el-icon><Calendar /></el-icon> {{ item.startTime }} ~ {{ item.endTime }}</p>
          <p><el-icon><LocationFilled /></el-icon> {{ item.location }}</p>
          <div class="activity-footer">
            <span class="signup-count">已报名 {{ item.signupCount }}/{{ item.maxCount }} 人</span>
            <div class="action-btns">
              <el-button
                v-if="(role === 'resident' || role === 'maintainer') && item.status === 'SIGNING' && item.signupCount < item.maxCount && !item.hasSignedUp"
                type="primary"
                size="small"
                @click.stop="handleSignup(item)"
              >立即报名</el-button>
              <el-tag v-else-if="(role === 'resident' || role === 'maintainer')" size="small" type="info">
                {{ item.hasSignedUp ? '已报名' : (item.signupCount >= item.maxCount ? '已满额' : '不可报名') }}
              </el-tag>
              <template v-if="role === 'manager' || role === 'admin'">
                <el-button type="warning" size="small" @click.stop="showEditDialog(item)">编辑</el-button>
                <el-button v-if="item.status !== 'CANCELLED'" type="danger" size="small" @click.stop="handleCancel(item)">取消</el-button>
              </template>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @current-change="fetchList" />
    </div>

    <!-- 发布/编辑活动弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '发布活动'" width="560px" destroy-on-close>
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="90px">
        <el-form-item label="活动标题" prop="title"><el-input v-model="publishForm.title" /></el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            action="/api/upload/image"
            list-type="picture-card"
            :auto-upload="true"
            :file-list="coverFileList"
            :limit="1"
            accept="image/*"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleCoverUploadSuccess"
            :on-error="handleCoverUploadError"
            :on-remove="handleCoverRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="publishForm.startTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="publishForm.endTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location"><el-input v-model="publishForm.location" /></el-form-item>
        <el-form-item label="人数上限" prop="maxCount">
          <el-input-number v-model="publishForm.maxCount" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="活动简介"><el-input v-model="publishForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存修改' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, LocationFilled, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'
const token = localStorage.getItem('token') || ''

const statusMap = {
  SIGNING: { label: '报名中', type: 'success' },
  ONGOING: { label: '进行中', type: '' },
  ENDED:   { label: '已结束', type: 'info' }
}

const list = ref([])
const statusFilter = ref('')
const page = ref(1)
const size = ref(9)
const total = ref(0)
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await request.get('/activity/list', {
      params: {
        page: page.value,
        size: size.value,
        status: statusFilter.value || undefined,
        userId: userInfo.id
      }
    })
    if (res.code === 200) { list.value = res.data?.records || []; total.value = res.data?.total || 0 }
  } catch (e) {
    list.value = [
      { id: 1, title: '端午节包粽子活动', startTime: '2025-06-05 09:00', endTime: '2025-06-05 12:00', location: '社区活动中心', status: 'SIGNING', signupCount: 28, maxCount: 50, coverImg: '', hasSignedUp: false },
      { id: 2, title: '社区篮球友谊赛', startTime: '2025-06-10 15:00', endTime: '2025-06-10 18:00', location: '社区篮球场', status: 'SIGNING', signupCount: 16, maxCount: 20, coverImg: '', hasSignedUp: false },
      { id: 3, title: '环保知识竞赛', startTime: '2025-05-20 14:00', endTime: '2025-05-20 16:00', location: '物业会议室', status: 'ENDED', signupCount: 40, maxCount: 40, coverImg: '', hasSignedUp: false }
    ]
    total.value = 3
  } finally { loading.value = false }
}

const handleSignup = async (item) => {
  await ElMessageBox.confirm(`确认报名「${item.title}」？`, '报名确认', { confirmButtonText: '确认报名', cancelButtonText: '取消' })
  try {
    const res = await request.post(`/activity/signup/${item.id}`)
    if (res.code === 200) { ElMessage.success('报名成功！'); fetchList() }
    else ElMessage.error(res.message || '报名失败')
  } catch (e) { ElMessage.error('请求失败') }
}

// 发布活动
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const publishFormRef = ref()
const coverFileList = ref([])
const submitting = ref(false)
const publishForm = reactive({ title: '', startTime: '', endTime: '', location: '', maxCount: 50, description: '', coverImage: '' })
const publishRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

const handleCoverUploadSuccess = (response, file) => {
  if (response.code === 200) {
    publishForm.coverImage = response.data.url
    file.url = response.data.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('封面上传失败')
  }
}

const handleCoverUploadError = () => {
  ElMessage.error('封面上传失败')
}

const handleCoverRemove = () => {
  publishForm.coverImage = ''
}

const resetForm = () => {
  publishForm.title = ''
  publishForm.startTime = ''
  publishForm.endTime = ''
  publishForm.location = ''
  publishForm.maxCount = 50
  publishForm.description = ''
  publishForm.coverImage = ''
  coverFileList.value = []
}

const showPublishDialog = () => {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (item) => {
  isEdit.value = true
  editId.value = item.id
  publishForm.title = item.title
  publishForm.startTime = item.startTime
  publishForm.endTime = item.endTime
  publishForm.location = item.location
  publishForm.maxCount = item.maxCount
  publishForm.description = item.description
  publishForm.coverImage = item.coverImg || ''
  coverFileList.value = item.coverImg ? [{ url: item.coverImg }] : []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!publishFormRef.value) {
    ElMessage.error('表单未初始化')
    return
  }
  await publishFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写必填项')
      return
    }
    submitting.value = true
    try {
      const data = {
        ...publishForm,
        startTime: publishForm.startTime ? new Date(publishForm.startTime).toISOString().replace('T', ' ').substring(0, 19) : null,
        endTime: publishForm.endTime ? new Date(publishForm.endTime).toISOString().replace('T', ' ').substring(0, 19) : null
      }
      let res
      if (isEdit.value) {
        res = await request.put(`/activity/${editId.value}`, data)
      } else {
        res = await request.post('/activity', data)
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '活动发布成功')
        dialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (e) {
      console.error('发布活动失败:', e)
      ElMessage.error('请求失败，请检查网络连接')
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = async (item) => {
  await ElMessageBox.confirm(`确定取消活动「${item.title}」？`, '取消活动', { confirmButtonText: '确定取消', cancelButtonText: '否' })
  try {
    const res = await request.delete(`/activity/${item.id}`)
    if (res.code === 200) { ElMessage.success('已取消'); fetchList() }
    else ElMessage.error(res.message || '取消失败')
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.activity-list-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.page-header h3 { margin: 0; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.activity-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.activity-card { overflow: hidden; }
.activity-cover { position: relative; }
.status-badge { position: absolute; top: 10px; right: 10px; }
.activity-info { padding: 12px 0 0; }
.activity-info h4 { margin: 0 0 8px; font-size: 16px; color: #303133; }
.activity-info p { margin: 4px 0; font-size: 13px; color: #606266; display: flex; align-items: center; gap: 4px; }
.activity-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.activity-footer .action-btns { display: flex; gap: 6px; align-items: center; }
.signup-count { font-size: 13px; color: #909399; }
.pagination-wrap { margin-top: 24px; display: flex; justify-content: flex-end; }
</style>
