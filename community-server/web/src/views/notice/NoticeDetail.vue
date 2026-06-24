<template>
  <div class="notice-detail-page">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回列表</el-button>
    </div>

    <div class="detail-card" v-loading="loading">
      <h2 class="detail-title">{{ detail.title }}</h2>
      <div class="detail-meta">
        <span>发布时间：{{ formatTime(detail.createTime) }}</span>
        <span>发布人：{{ detail.publisherName }}</span>
        <el-tag v-if="detail.isTop" type="danger" size="small">置顶</el-tag>
        <el-tag v-if="role === 'resident'" type="success" size="small">已读</el-tag>
      </div>
      <el-divider />
      <div class="detail-content">{{ detail.content }}</div>
      <div class="detail-images" v-if="parsedImages.length">
        <div class="images-grid">
          <el-image
            v-for="(img, i) in parsedImages"
            :key="i"
            :src="formatImageUrl(img)"
            :preview-src-list="parsedImages.map(img => formatImageUrl(img))"
            fit="cover"
            lazy
            :fallback="fallbackImage"
            class="detail-image"
          />
        </div>
      </div>

      <!-- 管理员统计 -->
      <div class="read-stats" v-if="role === 'manager' || role === 'admin'">
        <el-divider />
        <el-descriptions title="阅读统计" :column="1" border>
          <el-descriptions-item label="已读人数">{{ stats.readCount || 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const loading = ref(false)
const detail = ref({})
const stats = ref({})

// 备用图片
const fallbackImage = ref('data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"%3E%3Crect fill="%23f5f5f5" width="200" height="200"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="14" x="50%25" y="50%25" text-anchor="middle" dominant-baseline="middle"%3E图片加载失败%3C/text%3E%3C/svg%3E')

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 格式化图片URL
const formatImageUrl = (url) => {
  if (!url) return fallbackImage.value
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/uploads/')) return url
  if (url.startsWith('/')) return `/uploads${url}`
  return `/uploads/${url}`
}

// 解析图片JSON字符串为数组
const parsedImages = computed(() => {
  if (!detail.value.images) return []
  try {
    const images = JSON.parse(detail.value.images)
    return Array.isArray(images) ? images.filter(img => img && img.trim()) : []
  } catch {
    return []
  }
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/notice/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data || {}
    } else {
      ElMessage.error(res.message || '获取公告详情失败')
    }
  } catch (e) {
    console.error('获取公告详情失败', e)
    ElMessage.error('获取公告详情失败，请检查网络或重新登录')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  if (role !== 'manager' && role !== 'admin') return
  try {
    const res = await request.get(`/notice/${route.params.id}/read-count`)
    if (res.code === 200) {
      stats.value = { readCount: res.data?.readCount || 0 }
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
    stats.value = { readCount: 0 }
  }
}

const goBack = () => router.push('/notice')

onMounted(() => { fetchDetail(); fetchStats() })
</script>

<style scoped>
.notice-detail-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.detail-card {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.detail-title {
  font-size: 22px;
  color: #303133;
  margin-bottom: 12px;
  text-align: center;
}
.detail-meta {
  display: flex;
  gap: 20px;
  align-items: center;
  color: #909399;
  font-size: 14px;
  justify-content: center;
}
.detail-content {
  font-size: 15px;
  line-height: 2;
  color: #606266;
  white-space: pre-wrap;
}
.detail-images { margin-top: 24px; }
.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.detail-image {
  width: 100%;
  height: 150px;
  border-radius: 8px;
  object-fit: cover;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.detail-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.read-stats { margin-top: 20px; }
</style>
