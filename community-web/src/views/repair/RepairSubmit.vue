<template>
  <div class="repair-submit-page">
    <h3>提交报修</h3>
    <div class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="报修标题" prop="title">
          <el-input v-model="form.title" placeholder="请简要描述问题，如：卫生间漏水" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细描述问题情况，便于维修人员判断" />
        </el-form-item>
        <el-form-item label="报修地址" prop="address">
          <el-input v-model="form.address" placeholder="如：3栋2单元501室" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="现场图片">
          <el-upload
            action="/api/upload/image"
            list-type="picture-card"
            :auto-upload="true"
            v-model:file-list="fileList"
            :limit="5"
            accept="image/*"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleUploadRemove"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">最多上传5张图片，每张不超过5MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" style="width: 120px">提交报修</el-button>
          <el-button @click="resetForm" style="width: 100px">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const fileList = ref([])
const token = localStorage.getItem('token') || ''

const form = reactive({
  title: '',
  description: '',
  address: '',
  phone: ''
})

const phoneValidator = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  address: [{ required: true, message: '请输入报修地址', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { validator: phoneValidator, trigger: 'blur' }
  ]
}

// 图片上传处理
const handleUploadSuccess = (response, file) => {
  if (response.code === 200 && response.data?.url) {
    // 直接更新当前文件的 url
    file.url = response.data.url
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const handleUploadRemove = (file) => {
  // 从文件列表中移除
  const index = fileList.value.indexOf(file)
  if (index > -1) fileList.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 收集已上传的图片URL
      const imageUrls = fileList.value.map(f => f.url).filter(url => url)
      const res = await request.post('/repair/submit', {
        ...form,
        images: JSON.stringify(imageUrls)
      })
      if (res.code === 200) {
        ElMessage.success('报修成功，等待物业受理')
        router.push('/repair/list')
      } else {
        ElMessage.error(res.message || '提交失败')
      }
    } catch (e) {
      ElMessage.error('请求失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  fileList.value = []
}
</script>

<style scoped>
.repair-submit-page { padding: 20px; }
.repair-submit-page h3 { margin: 0 0 20px; color: #303133; }
.form-card {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  max-width: 680px;
}
</style>
