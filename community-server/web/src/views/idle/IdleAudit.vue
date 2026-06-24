<template>
  <div class="idle-audit-page">
    <div class="page-header">
      <h3>闲置审核</h3>
      <el-select v-model="statusFilter" placeholder="状态筛选" @change="fetchList" style="width: 150px">
        <el-option label="待审核" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已售出" :value="2" />
        <el-option label="已下架" :value="3" />
      </el-select>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image v-if="row.coverImg" :src="row.coverImg" fit="cover" style="width:50px;height:50px;border-radius:4px;" />
          <span v-else>-</span>
        </template>
      </el-table-column>
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
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" @click="audit(row, 1)">通过</el-button>
          <el-button v-if="row.status === 0" type="danger" size="small" @click="audit(row, 3)">驳回</el-button>
          <el-button v-if="row.status === 1" type="warning" size="small" @click="forceOffline(row)">强制下架</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const statusFilter = ref(0)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/idle/audit-list', {
      params: { page: page.value, size: size.value, status: statusFilter.value }
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

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'primary', 3: 'danger' }
  return types[status] || ''
}

const getStatusLabel = (status) => {
  const labels = { 0: '待审核', 1: '已发布', 2: '已售出', 3: '已下架' }
  return labels[status] || status
}

const audit = async (row, status) => {
  try {
    if (status === 3) {
      const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入驳回原因'
      })
      const res = await request.put(`/idle/audit/${row.id}`, null, { params: { status, reason: value } })
      if (res.code === 200) {
        ElMessage.success('已驳回')
        fetchList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } else {
      await ElMessageBox.confirm('确定通过审核?', '提示', { type: 'success' })
      const res = await request.put(`/idle/audit/${row.id}`, null, { params: { status } })
      if (res.code === 200) {
        ElMessage.success('已通过')
        fetchList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    }
  } catch (e) {}
}

const forceOffline = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入下架原因', '强制下架', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入下架原因'
    })
    const res = await request.put(`/idle/force-offline/${row.id}`, null, { params: { reason: value } })
    if (res.code === 200) {
      ElMessage.success('已强制下架')
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.idle-audit-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #303133; }
.price { color: #f56c6c; font-weight: bold; }
</style>