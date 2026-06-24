<template>
  <div class="idle-publish-page">
    <h3>发布闲置物品</h3>
    <div class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="物品标题" prop="title">
          <el-input v-model="form.title" placeholder="如：九成新落地灯" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="物品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述物品成色、使用情况等" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 200px">
            <el-option label="家具" value="家具" />
            <el-option label="电器" value="电器" />
            <el-option label="图书" value="图书" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格（元）" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="交易方式" prop="tradeType">
          <el-radio-group v-model="form.tradeType">
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
            v-model:file-list="fileList"
            :limit="4"
            accept="image/*"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
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

const form = reactive({ title: '', description: '', category: '', price: 0, tradeType: '低价转让' })
const rules = {
  title: [{ required: true, message: '请输入物品标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  tradeType: [{ required: true, message: '请选择交易方式', trigger: 'change' }]
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    file.url = response.data.url
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const images = fileList.value.filter(f => f.url).map(f => f.url)
      const res = await request.post('/idle', {
        ...form,
        images: JSON.stringify(images)
      })
      if (res.code === 200) {
        ElMessage.success('发布成功，等待审核')
        router.push('/idle/list')
      } else { ElMessage.error(res.message || '发布失败') }
    } catch (e) { ElMessage.error('请求失败') }
    finally { submitting.value = false }
  })
}
</script>

<style scoped>
.idle-publish-page { padding: 20px; }
.idle-publish-page h3 { margin: 0 0 20px; color: #303133; }
.form-card { background: #fff; padding: 32px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); max-width: 680px; }
</style>
