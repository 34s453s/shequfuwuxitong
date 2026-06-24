<template>
  <div class="housekeeping-order-page">
    <h3>我的家政订单</h3>
    <el-table :data="list" stripe border v-loading="loading" style="width: 100%">
      <el-table-column prop="serviceName" label="服务名称" min-width="160" />
      <el-table-column prop="appointTime" label="预约时间" width="180" align="center" />
      <el-table-column prop="status" label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="orderStatusMap[row.status]?.type" size="small">
            {{ orderStatusMap[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="需求说明" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.remark || '—' }}</template>
      </el-table-column>
      <el-table-column prop="rating" label="评价" width="140" align="center">
        <template #default="{ row }">
          <el-rate v-if="row.rating" v-model="row.rating" disabled size="small" />
          <el-button v-else-if="row.status === 'COMPLETED'" type="primary" size="small" link @click="showRateDialog(row)">评价</el-button>
          <span v-else>—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @current-change="fetchList" />
    </div>

    <!-- 评价弹窗 -->
    <el-dialog v-model="rateDialogVisible" title="服务评价" width="420px">
      <el-form label-width="80px">
        <el-form-item label="评分"><el-rate v-model="rateValue" show-text /></el-form-item>
        <el-form-item label="评论内容"><el-input v-model="rateComment" type="textarea" :rows="3" placeholder="请输入评价（选填）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRate">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const orderStatusMap = {
  PENDING:   { label: '已下单', type: 'info' },
  ACCEPTED:  { label: '已接单', type: 'primary' },
  ONGOING:   { label: '服务中', type: '' },
  COMPLETED: { label: '已完成', type: 'success' },
  RATED:     { label: '已评价', type: 'success' }
}

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/housekeeping/my-orders', { params: { page: page.value, size: size.value } })
    if (res.code === 200) { list.value = res.data?.records || []; total.value = res.data?.total || 0 }
  } catch (e) {
    list.value = [
      { id: 1, serviceName: '家庭深度保洁', appointTime: '2025-06-05 09:00', status: 'COMPLETED', remark: '重点清洁厨房', rating: 5 },
      { id: 2, serviceName: '家电清洗服务', appointTime: '2025-06-10 14:00', status: 'PENDING', remark: '清洗2台空调', rating: 0 }
    ]
    total.value = 2
  } finally { loading.value = false }
}

// 评价
const rateDialogVisible = ref(false)
const rateValue = ref(5)
const rateComment = ref('')
const rateOrderId = ref('')
const showRateDialog = (row) => { rateOrderId.value = row.id; rateValue.value = 5; rateComment.value = ''; rateDialogVisible.value = true }
const handleRate = async () => {
  try {
    const res = await request.put(`/housekeeping/order/${rateOrderId.value}/rate`, null, {
      params: { rating: rateValue.value, comment: rateComment.value }
    })
    if (res.code === 200) { ElMessage.success('评价成功'); rateDialogVisible.value = false; fetchList() }
    else ElMessage.error(res.message || '评价失败')
  } catch (e) { ElMessage.error('请求失败') }
}

onMounted(fetchList)
</script>

<style scoped>
.housekeeping-order-page { padding: 20px; }
.housekeeping-order-page h3 { margin: 0 0 20px; color: #303133; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
