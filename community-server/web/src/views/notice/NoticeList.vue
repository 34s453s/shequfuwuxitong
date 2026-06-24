<template>
  <div class="notice-list-page">
    <div class="page-header">
      <h3>社区公告</h3>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索公告标题"
          clearable
          style="width: 260px"
          @clear="fetchList"
          @keyup.enter="fetchList"
        >
          <template #append>
            <el-button @click="fetchList">搜索</el-button>
          </template>
        </el-input>
        <el-button
          v-if="role === 'manager' || role === 'admin'"
          type="primary"
          @click="showPublishDialog"
        >发布公告</el-button>
      </div>
    </div>

    <el-table :data="list" stripe border style="width: 100%" @row-click="goDetail" v-loading="loading">
      <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-tag v-if="row.isTop" type="danger" size="small" style="margin-right: 6px">置顶</el-tag>
          <span>{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="100" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <template v-if="role === 'manager' || role === 'admin'">
            <el-button type="primary" size="small" @click.stop="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <!-- 发布公告弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="580px" destroy-on-close>
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="publishForm.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="publishForm.isTop" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="/api/upload/image"
            list-type="picture-card"
            :auto-upload="true"
            v-model:file-list="publishForm.fileList"
            :limit="3"
            accept="image/*"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleUploadRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="margin-top: 8px; color: #909399; font-size: 13px;">
            支持 JPG、PNG 格式，最多上传 3 张图片
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const list = ref([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/list', {
      params: { page: page.value, size: size.value, keyword: keyword.value }
    })
    if (res.code === 200) {
      list.value = res.data?.records || []
      total.value = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取公告列表失败')
      list.value = []
    }
  } catch (e) {
    console.error('获取公告列表失败', e)
    ElMessage.error('获取公告列表失败，请检查网络或重新登录')
    list.value = []
  } finally {
    loading.value = false
  }
}

const goDetail = (row) => {
  router.push(`/notice/${row.id}`)
}

// 发布公告相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const publishing = ref(false)
const publishFormRef = ref()
const token = localStorage.getItem('token') || ''
const publishForm = reactive({ id: '', title: '', content: '', isTop: false, fileList: [] })
const publishRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const showPublishDialog = () => {
  isEdit.value = false
  publishForm.id = ''
  publishForm.title = ''
  publishForm.content = ''
  publishForm.isTop = false
  publishForm.fileList = []
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  publishForm.id = row.id
  publishForm.title = row.title
  publishForm.content = row.content || ''
  publishForm.isTop = row.isTop === 1 || row.isTop === true
  // 初始化图片列表
  publishForm.fileList = []
  if (row.images) {
    try {
      const images = JSON.parse(row.images)
      if (Array.isArray(images)) {
        publishForm.fileList = images.map((url, index) => ({
          uid: String(index),
          name: `图片${index + 1}`,
          url: url.startsWith('/') ? url : `/uploads/${url.replace(/^uploads\//, '')}`
        }))
      }
    } catch (e) {
      console.error('解析图片失败', e)
    }
  }
  dialogVisible.value = true
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200 && response.data && response.data.url) {
    file.url = response.data.url
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleUploadError = (error) => {
  ElMessage.error('图片上传失败')
}

const handleUploadRemove = () => {
}

const handlePublish = async () => {
  if (!publishFormRef.value) return
  await publishFormRef.value.validate(async (valid) => {
    if (!valid) return
    publishing.value = true
    try {
      const { fileList, id, isTop, ...data } = publishForm
      const imageUrls = fileList.map(f => {
        const url = f.url || ''
        // 提取uploads路径部分
        const match = url.match(/\/uploads\/.*$/)
        return match ? match[0] : url
      }).filter(Boolean)
      
      let res
      if (isEdit.value) {
        res = await request.put(`/notice/${id}`, {
          ...data,
          isTop: isTop ? 1 : 0,
          images: JSON.stringify(imageUrls)
        })
      } else {
        res = await request.post('/notice', {
          ...data,
          isTop: isTop ? 1 : 0,
          images: JSON.stringify(imageUrls)
        })
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
        dialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res.message || (isEdit.value ? '修改失败' : '发布失败'))
      }
    } catch (e) {
      ElMessage.error('请求失败')
    } finally {
      publishing.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该公告?', '提示', { type: 'warning' })
    const res = await request.delete(`/notice/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.notice-list-page { padding: 20px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h3 { margin: 0; color: #303133; }
.header-actions { display: flex; gap: 12px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.el-table tr { cursor: pointer; }
</style>
