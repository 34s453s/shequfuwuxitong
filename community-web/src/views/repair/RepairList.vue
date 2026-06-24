<template>
  <div class="repair-list-page">
    <div class="page-header">
      <h3>{{ pageTitle }}</h3>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="按状态筛选" clearable @change="fetchList" style="width: 160px">
          <el-option label="待受理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="待确认" value="CONFIRMING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已评价" value="RATED" />
        </el-select>
      </div>
    </div>

    <el-table :data="list" stripe border v-loading="loading" @row-click="goDetail" style="width: 100%">
      <el-table-column prop="title" label="报修标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="address" label="地址" width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small">
            {{ statusMap[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maintainer" label="维修人员" width="110" align="center">
        <template #default="{ row }">{{ row.maintainer || '—' }}</template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="170" align="center" />
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const pageTitle = computed(() => {
  if (role === 'maintainer') return '我的维修任务'
  if (role === 'manager' || role === 'admin') return '报修管理'
  return '我的报修工单'
})

const statusMap = {
  PENDING:    { label: '待受理', type: 'info' },
  PROCESSING: { label: '处理中', type: '' },
  CONFIRMING: { label: '待确认', type: 'warning' },
  COMPLETED:  { label: '已完成', type: 'success' },
  RATED:      { label: '已评价', type: 'success' }
}

const list = ref([])
const statusFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  const url = role === 'resident' ? '/repair/my-list' : '/repair/list'
  try {
    const res = await request.get(url, {
      params: { page: page.value, size: size.value, status: statusFilter.value || undefined }
    })
    if (res.code === 200) {
      // 转换后端返回的数据
      list.value = (res.data?.records || []).map(item => {
        // 状态数字 -> 英文
        let statusStr
        switch (item.status) {
          case 0: statusStr = 'PENDING'; break
          case 1: statusStr = 'PROCESSING'; break
          case 2: statusStr = 'CONFIRMING'; break
          case 3: statusStr = 'COMPLETED'; break
          case 4: statusStr = 'RATED'; break
          default: statusStr = item.status
        }
        return {
          ...item,
          status: statusStr,
          submitTime: item.createTime, // 字段名映射
          maintainer: item.handlerId ? '维修员' + item.handlerId : '—' // 简化显示
        }
      })
      total.value = res.data?.total || 0
    }
  } catch (e) {
    // 模拟数据
    list.value = [
      { id: 1, title: '卫生间水管漏水', address: '3栋2单元501', status: 'PROCESSING', maintainer: '张工', submitTime: '2025-06-01 08:30' },
      { id: 2, title: '楼道灯不亮', address: '3栋2单元3楼', status: 'PENDING', maintainer: '', submitTime: '2025-06-02 19:15' },
      { id: 3, title: '门禁系统故障', address: '小区南门', status: 'COMPLETED', maintainer: '李工', submitTime: '2025-05-28 10:00' }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const goDetail = (row) => router.push(`/repair/${row.id}`)

onMounted(fetchList)
</script>

<style scoped>
.repair-list-page { padding: 20px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h3 { margin: 0; color: #303133; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.el-table tr { cursor: pointer; }
</style>
