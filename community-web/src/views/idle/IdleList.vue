<template>
  <div class="idle-list-page">
    <div class="page-header">
      <h3>邻里闲置</h3>
      <div class="header-actions">
        <el-input v-model="keyword" placeholder="搜索物品" clearable style="width: 220px"
          @clear="fetchList" @keyup.enter="fetchList" />
        <el-select v-model="category" placeholder="全部分类" clearable @change="fetchList" style="width: 130px">
          <el-option label="我想要" value="favorite" />
          <el-option label="家具" value="家具" />
          <el-option label="电器" value="电器" />
          <el-option label="图书" value="图书" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-if="role === 'manager' || role === 'admin'" v-model="statusFilter" placeholder="状态筛选" clearable @change="fetchList" style="width: 120px">
          <el-option label="全部" :value="''" />
          <el-option label="待审核" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已售出" :value="2" />
          <el-option label="已下架" :value="3" />
        </el-select>
        <el-button v-if="role === 'resident' || role === 'maintainer'" type="primary" @click="goPublish">发布闲置</el-button>
      </div>
    </div>

    <div class="card-grid" v-loading="loading">
      <el-card v-for="item in list" :key="item.id" shadow="hover" class="idle-card">
        <template #header>
          <div class="card-header">
            <el-tag size="small" :type="item.tradeType === '赠送' ? 'success' : 'warning'">{{ item.tradeType }}</el-tag>
            <el-tag v-if="(role === 'manager' || role === 'admin') && item.status !== 1" size="small" :type="getStatusType(item.status)">{{ getStatusLabel(item.status) }}</el-tag>
            <span class="price">¥{{ item.price || '0' }}</span>
          </div>
        </template>
        <div class="card-body" @click="goDetail(item)">
          <div class="card-img">
            <el-image :src="item.coverImg" fit="cover" style="width:100%;height:160px;border-radius:4px;" />
          </div>
          <div class="card-info">
            <h4 class="card-title">{{ item.title }}</h4>
            <p class="card-meta">{{ item.publisher }} · {{ item.publishTime }}</p>
          </div>
        </div>
        <div class="card-footer">
          <el-button 
            v-if="role === 'resident' || role === 'maintainer'" 
            icon="Star" 
            :type="favoriteIds.includes(item.id) ? 'warning' : 'default'" 
            size="small" 
            @click.stop="toggleFavorite(item)"
          >
            {{ favoriteIds.includes(item.id) ? '已收藏' : '我想要' }}
          </el-button>
        </div>
      </el-card>
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @current-change="fetchList" />
    </div>

    <IdleDetailModal :visible="showDetail" :item="selectedItem" @close="showDetail = false" @favorite-change="handleFavoriteChange" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import IdleDetailModal from '@/components/IdleDetailModal.vue'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const list = ref([])
const keyword = ref('')
const category = ref('')
const statusFilter = ref('')
const page = ref(1)
const size = ref(12)
const total = ref(0)
const loading = ref(false)
const showDetail = ref(false)
const selectedItem = ref({})
const favoriteIds = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    let url = '/idle/list'
    if (category.value === 'favorite') {
      url = '/idle/favorite/list'
    }
    const res = await request.get(url, {
      params: { 
        page: page.value, 
        size: size.value, 
        keyword: keyword.value, 
        category: category.value === 'favorite' ? undefined : category.value || undefined,
        status: statusFilter.value || undefined
      }
    })
    if (res.code === 200) {
      list.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    list.value = [
      { id: 1, title: '九成新落地灯', category: '家具', price: 80, tradeType: '低价转让', publisher: '王芳', publishTime: '2025-06-01', coverImg: '' },
      { id: 2, title: '《活着》等三本文学书', category: '图书', price: 0, tradeType: '赠送', publisher: '刘强', publishTime: '2025-05-30', coverImg: '' },
      { id: 3, title: '小米电水壶 全新', category: '电器', price: 45, tradeType: '低价转让', publisher: '赵丽', publishTime: '2025-05-28', coverImg: '' }
    ]
    total.value = 3
  } finally { loading.value = false }
}

const loadFavorites = async () => {
  try {
    const res = await request.get('/idle/favorite/list')
    if (res.code === 200) {
      favoriteIds.value = (res.data?.records || []).map(item => item.id)
    }
  } catch (e) {}
}

const toggleFavorite = async (item) => {
  const isFavorite = favoriteIds.value.includes(item.id)
  try {
    const res = await request.post(`/idle/favorite/${isFavorite ? 'remove' : 'add'}/${item.id}`)
    if (res.code === 200) {
      if (isFavorite) {
        favoriteIds.value = favoriteIds.value.filter(id => id !== item.id)
      } else {
        favoriteIds.value.push(item.id)
      }
      ElMessage.success(isFavorite ? '已取消收藏' : '收藏成功')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 处理详情弹窗中的收藏状态变化
const handleFavoriteChange = ({ itemId, isFavorite }) => {
  if (isFavorite) {
    if (!favoriteIds.value.includes(itemId)) {
      favoriteIds.value.push(itemId)
    }
  } else {
    favoriteIds.value = favoriteIds.value.filter(id => id !== itemId)
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

const goPublish = () => router.push('/idle/publish')
const goDetail = (item) => {
  selectedItem.value = item
  showDetail.value = true
}

onMounted(async () => {
  await fetchList()
  if (role === 'resident') {
    await loadFavorites()
  }
})
</script>

<style scoped>
.idle-list-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.page-header h3 { margin: 0; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.idle-card { cursor: pointer; transition: transform 0.2s; }
.idle-card:hover { transform: translateY(-4px); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 18px; font-weight: bold; color: #f56c6c; }
.card-body { cursor: pointer; }
.card-info { margin-top: 10px; }
.card-title { margin: 0; font-size: 15px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-meta { margin: 6px 0 0; font-size: 12px; color: #909399; }
.card-footer { display: flex; justify-content: flex-end; padding-top: 10px; border-top: 1px solid #f0f0f0; }
.pagination-wrap { margin-top: 24px; display: flex; justify-content: flex-end; }
</style>
