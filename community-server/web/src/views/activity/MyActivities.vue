<template>
  <div class="my-activities-page">
    <div class="page-header">
      <h3>我的活动</h3>
    </div>

    <el-table :data="list" stripe border v-loading="loading" style="width: 100%">
      <el-table-column prop="activityTitle" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="activityLocation" label="活动地点" width="150" show-overflow-tooltip />
      <el-table-column prop="activityStartTime" label="开始时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.activityStartTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="activityEndTime" label="结束时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.activityEndTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small">
            {{ statusMap[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="signupTime" label="报名时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.signupTime) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <el-empty v-if="!loading && list.length === 0" description="您还没有报名任何活动" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const statusMap = {
  SIGNING: { label: '报名中', type: 'success' },
  FULL: { label: '已满员', type: 'warning' },
  ONGOING: { label: '进行中', type: '' },
  ENDED: { label: '已结束', type: 'info' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string') {
    return time.replace('T', ' ')
  }
  return time
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/my-signups')
    if (res.code === 200) {
      list.value = res.data || []
      total.value = res.data?.length || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.my-activities-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #303133; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
