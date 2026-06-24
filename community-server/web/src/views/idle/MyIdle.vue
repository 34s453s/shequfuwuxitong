<template>
  <div class="my-idle-page">
    <div class="page-header">
      <h3>我的闲置</h3>
      <el-button type="primary" @click="goPublish">发布闲置</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="title" label="物品标题" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          <span class="price">¥{{ row.price || '0' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="tradeType" label="交易方式" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 0 || row.status === 1" type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button v-if="row.status === 1" type="warning" size="small" @click="markSold(row)">标记售出</el-button>
          <el-button v-if="row.status === 1" type="danger" size="small" @click="offline(row)">下架</el-button>
          <el-button v-if="row.status === 3 && row.auditReason" type="info" size="small" @click="showReason(row)">查看原因</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchList"
      style="margin-top: 16px; justify-content: flex-end;"
    />

    <!-- 编辑弹窗 -->
    <el-dialog title="编辑闲置物品" v-model="editDialogVisible" width="600px" @close="resetEditForm">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="物品标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入物品标题" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="物品描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入物品描述" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择分类" style="width: 200px">
            <el-option label="家具" value="家具" />
            <el-option label="电器" value="电器" />
            <el-option label="图书" value="图书" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格（元）" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="交易方式" prop="tradeType">
          <el-radio-group v-model="editForm.tradeType">
            <el-radio label="赠送" />
            <el-radio label="低价转让" />
            <el-radio label="交换" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="物品图片">
          <el-upload
            action="/api/upload/image"
            list-type="picture-card"
            :auto-upload="true"
            v-model:file-list="editFileList"
            :limit="4"
            accept="image/*"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleEditUploadSuccess"
            :on-error="handleEditUploadError"
            :on-remove="handleEditUploadRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editing">保存修改</el-button>
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
const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

// 编辑相关
const editDialogVisible = ref(false)
const editFormRef = ref()
const editing = ref(false)
const token = localStorage.getItem('token') || ''
const editFileList = ref([])
const editForm = reactive({ id: '', title: '', description: '', category: '', price: 0, tradeType: '低价转让' })

const editRules = {
  title: [{ required: true, message: '请输入物品标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  tradeType: [{ required: true, message: '请选择交易方式', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/idle/my-list', {
      params: { page: page.value, size: size.value }
    })
    if (res.code === 200) {
      list.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goPublish = () => router.push('/idle/publish')

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'primary', 3: 'danger' }
  return types[status] || ''
}

const getStatusLabel = (status) => {
  const labels = { 0: '待审核', 1: '已发布', 2: '已售出', 3: '已下架' }
  return labels[status] || status
}

const showEditDialog = (row) => {
  editForm.id = row.id
  editForm.title = row.title
  editForm.description = row.description || ''
  editForm.category = row.category
  editForm.price = row.price || 0
  editForm.tradeType = row.tradeType || '低价转让'
  // 初始化图片列表
  editFileList.value = []
  if (row.images) {
    try {
      const images = JSON.parse(row.images)
      if (Array.isArray(images)) {
        editFileList.value = images.map((url, index) => ({
          uid: String(index),
          name: `图片${index + 1}`,
          // 确保图片路径正确
          url: url.startsWith('/') ? url : `/uploads/${url.replace(/^uploads\//, '')}`
        }))
      }
    } catch (e) {
      console.error('解析图片失败', e)
    }
  }
  editDialogVisible.value = true
}

const resetEditForm = () => {
  editForm.id = ''
  editForm.title = ''
  editForm.description = ''
  editForm.category = ''
  editForm.price = 0
  editForm.tradeType = '低价转让'
  editFileList.value = []
  editFormRef.value?.resetFields()
}

const handleEditUploadSuccess = (response, file) => {
  if (response.code === 200) {
    file.url = response.data.url
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleEditUploadError = () => {
  ElMessage.error('图片上传失败')
}

const handleEditUploadRemove = (file) => {
  const index = editFileList.value.indexOf(file)
  if (index > -1) {
    editFileList.value.splice(index, 1)
  }
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editing.value = true
    try {
      // 收集图片URL，确保路径正确
      const images = editFileList.value.filter(f => f.url).map(f => {
        let url = f.url
        // 移除可能的完整URL，只保留相对路径
        if (url.startsWith('http')) {
          const idx = url.indexOf('/uploads/')
          if (idx > -1) {
            url = url.substring(idx)
          }
        }
        // 确保以 /uploads/ 开头
        if (!url.startsWith('/uploads/')) {
          if (url.startsWith('/')) {
            url = '/uploads' + url
          } else if (url.startsWith('uploads/')) {
            url = '/' + url
          } else {
            url = '/uploads/' + url
          }
        }
        return url
      })
      const data = {
        title: editForm.title,
        description: editForm.description,
        category: editForm.category,
        price: editForm.price,
        tradeType: editForm.tradeType
      }
      // 如果有图片变化才提交图片
      if (images.length > 0) {
        data.images = JSON.stringify(images)
      }
      const res = await request.put(`/idle/${editForm.id}`, data)
      if (res.code === 200) {
        ElMessage.success('修改成功，等待审核')
        editDialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res.message || '修改失败')
      }
    } catch (e) {
      ElMessage.error('请求失败')
    } finally {
      editing.value = false
    }
  })
}

const markSold = async (row) => {
  try {
    await ElMessageBox.confirm('确定标记为已售出?', '提示', { type: 'warning' })
    const res = await request.put(`/idle/status/${row.id}`, null, { params: { status: 2 } })
    if (res.code === 200) {
      ElMessage.success('已标记售出')
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {}
}

const offline = async (row) => {
  try {
    await ElMessageBox.confirm('确定下架此物品?', '提示', { type: 'warning' })
    const res = await request.put(`/idle/status/${row.id}`, null, { params: { status: 3 } })
    if (res.code === 200) {
      ElMessage.success('已下架')
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {}
}

const showReason = (row) => {
  ElMessageBox.alert(row.auditReason || '无审核原因', '审核原因', {
    confirmButtonText: '知道了',
    type: 'info'
  })
}

onMounted(fetchList)
</script>

<style scoped>
.my-idle-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #303133; }
.price { color: #f56c6c; font-weight: bold; }
</style>