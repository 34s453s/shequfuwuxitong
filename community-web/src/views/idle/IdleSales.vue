<template>
  <div class="idle-sales-page">
    <div class="page-header">
      <h3>我卖出的</h3>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="itemTitle" label="物品" min-width="200">
        <template #default="{ row }">
          <div class="item-cell">
            <img :src="row.itemCover" class="item-thumb" />
            <span>{{ row.itemTitle }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="成交价" width="100">
        <template #default="{ row }">
          <span class="price">¥{{ row.price || '0' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="buyerMessage" label="买家留言" width="150" show-overflow-tooltip />
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column prop="buyerPhone" label="买家电话" width="120" />
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" @click="confirmOrder(row)">确认售出</el-button>
          <el-button v-if="row.status === 0" type="danger" size="small" @click="cancelOrder(row)">取消</el-button>
          <span v-if="row.status !== 0" class="status-text">{{ getStatusText(row.status) }}</span>
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

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/idle/order/my-sales', {
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

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || ''
}

const getStatusLabel = (status) => {
  const labels = { 0: '待确认', 1: '已完成', 2: '已取消' }
  return labels[status] || status
}

const getStatusText = (status) => {
  const texts = { 1: '交易完成', 2: '已取消' }
  return texts[status] || ''
}

const confirmOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确认将此物品售出给该买家?', '确认交易', { type: 'success' })
    const res = await request.put(`/idle/order/${row.id}/confirm`)
    if (res.code === 200) {
      ElMessage.success('交易已确认，请联系买家完成交易')
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {}
}

const cancelOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消订单?', '提示', { type: 'warning' })
    const res = await request.put(`/idle/order/${row.id}/cancel`)
    if (res.code === 200) {
      ElMessage.success('已取消')
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.idle-sales-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #303133; }
.price { color: #f56c6c; font-weight: bold; }
.item-cell { display: flex; align-items: center; gap: 10px; }
.item-thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; }
.status-text { color: #909399; font-size: 12px; }
</style>
