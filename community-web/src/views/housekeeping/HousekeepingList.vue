<template>
  <div class="housekeeping-list-page">
    <div class="page-header">
      <h3>家政服务</h3>
      <div class="header-actions">
        <el-input v-model="keyword" placeholder="搜索服务名称" clearable style="width: 220px"
          @clear="fetchList" @keyup.enter="fetchList" />
        <el-button v-if="role === 'manager' || role === 'admin'" type="primary" @click="showAddDialog">新增服务</el-button>
      </div>
    </div>

    <el-table :data="list" stripe border v-loading="loading" style="width: 100%">
      <el-table-column prop="name" label="服务名称" min-width="160" />
      <el-table-column prop="category" label="分类" width="110" align="center" />
      <el-table-column prop="price" label="参考价格" width="120" align="center">
        <template #default="{ row }"><span style="color:#f56c6c;font-weight:bold">¥{{ row.price }}</span>/次</template>
      </el-table-column>
      <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
      <el-table-column prop="phone" label="联系电话" width="130" align="center" />
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="role === 'resident' || role === 'maintainer'" type="primary" size="small" @click="showOrderDialog(row)">预约</el-button>
          <el-button v-if="role === 'manager' || role === 'admin'" size="small" @click="editService(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @current-change="fetchList" />
    </div>

    <!-- 新增/编辑服务弹窗 -->
    <el-dialog v-model="addDialogVisible" :title="isEdit ? '编辑服务' : '新增服务'" width="520px" destroy-on-close>
      <el-form :model="serviceForm" :rules="serviceRules" ref="serviceFormRef" label-width="90px">
        <el-form-item label="服务名称" prop="name"><el-input v-model="serviceForm.name" /></el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="serviceForm.category" style="width:100%">
            <el-option label="保洁" value="保洁" />
            <el-option label="维修" value="维修" />
            <el-option label="保姆" value="保姆" />
            <el-option label="月嫂" value="月嫂" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格（元）" prop="price">
          <el-input-number v-model="serviceForm.price" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="serviceForm.phone" /></el-form-item>
        <el-form-item label="服务简介"><el-input v-model="serviceForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveService">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预约弹窗 -->
    <el-dialog v-model="orderDialogVisible" title="预约家政服务" width="460px">
      <p style="margin:0 0 16px;color:#606266">服务：<strong>{{ selectedService.name }}</strong></p>
      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="预约时间" required>
          <el-date-picker v-model="orderForm.appointTime" type="datetime" style="width:100%" placeholder="请选择预约时间" />
        </el-form-item>
        <el-form-item label="需求说明">
          <el-input v-model="orderForm.demand" type="textarea" :rows="3" placeholder="请描述您的需求（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOrder">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const list = ref([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/housekeeping/list', {
      params: { page: page.value, size: size.value, keyword: keyword.value }
    })
    if (res.code === 200) { list.value = res.data?.records || []; total.value = res.data?.total || 0 }
  } catch (e) {
    list.value = [
      { id: 1, name: '家庭深度保洁', category: '保洁', price: 280, description: '全屋深度清洁，含厨房卫生间', phone: '400-111-2222' },
      { id: 2, name: '家电清洗服务', category: '维修', price: 120, description: '空调、油烟机、洗衣机清洗', phone: '400-333-4444' },
      { id: 3, name: '月嫂服务', category: '月嫂', price: 8800, description: '专业月嫂，持有上岗证', phone: '400-555-6666' }
    ]
    total.value = 3
  } finally { loading.value = false }
}

// 新增/编辑服务
const addDialogVisible = ref(false)
const isEdit = ref(false)
const serviceFormRef = ref()
const serviceForm = reactive({ id: '', name: '', category: '', price: 100, phone: '', description: '' })
const serviceRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}
const showAddDialog = () => { isEdit.value = false; Object.assign(serviceForm, { id: '', name: '', category: '', price: 100, phone: '', description: '' }); addDialogVisible.value = true }
const editService = (row) => { isEdit.value = true; Object.assign(serviceForm, row); addDialogVisible.value = true }
const handleSaveService = async () => {
  if (!serviceFormRef.value) return
  await serviceFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const url = isEdit.value ? '/housekeeping' : '/housekeeping'
      const method = isEdit.value ? 'put' : 'post'
      const res = await request[method](url, serviceForm)
      if (res.code === 200) { ElMessage.success('保存成功'); addDialogVisible.value = false; fetchList() }
      else ElMessage.error(res.message || '保存失败')
    } catch (e) { ElMessage.error('请求失败') }
  })
}

// 预约
const orderDialogVisible = ref(false)
const selectedService = ref({})
const orderForm = reactive({ appointTime: '', demand: '' })
const showOrderDialog = (row) => { selectedService.value = row; orderForm.appointTime = ''; orderForm.demand = ''; orderDialogVisible.value = true }
const handleOrder = async () => {
  if (!orderForm.appointTime) { ElMessage.warning('请选择预约时间'); return }
  try {
    const res = await request.post('/housekeeping/order', {
      serviceId: selectedService.value.id,
      appointTime: orderForm.appointTime,
      demand: orderForm.demand
    })
    if (res.code === 200) { ElMessage.success('预约成功！'); orderDialogVisible.value = false }
    else ElMessage.error(res.message || '预约失败')
  } catch (e) { ElMessage.error('请求失败') }
}

onMounted(fetchList)
</script>

<style scoped>
.housekeeping-list-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.page-header h3 { margin: 0; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
