<template>
  <div class="idle-detail-modal" v-if="visible" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ item.title }}</h3>
        <el-button type="text" @click="close">×</el-button>
      </div>
      
      <div class="modal-body">
        <div class="image-section">
          <el-carousel height="280px" :autoplay="false" indicator-position="none">
            <el-carousel-item v-for="(img, index) in images" :key="index">
              <el-image :src="img" fit="contain" style="width:100%;height:100%;" />
            </el-carousel-item>
          </el-carousel>
          <div v-if="images.length > 1" class="image-indicators">
            <span v-for="(_, index) in images" :key="index" 
              :class="{ active: currentImageIndex === index }"
              @click="currentImageIndex = index" />
          </div>
        </div>
        
        <div class="info-section">
          <div class="price-row">
            <span class="price">¥{{ item.price || '0' }}</span>
            <el-tag :type="item.tradeType === '赠送' ? 'success' : 'warning'" size="small">
              {{ item.tradeType }}
            </el-tag>
          </div>
          
          <div class="detail-row">
            <span class="label">分类</span>
            <span class="value">{{ item.category }}</span>
          </div>
          
          <div class="detail-row">
            <span class="label">发布者</span>
            <span class="value">{{ item.publisher }}</span>
          </div>
          
          <div class="detail-row">
            <span class="label">发布时间</span>
            <span class="value">{{ formatTime(item.createTime) }}</span>
          </div>
          
          <div class="description-section">
            <div class="label">物品描述</div>
            <p class="description">{{ item.description }}</p>
          </div>
          
          <div class="contact-section">
            <div class="label">联系卖家</div>
            <div class="contact-info">
              <span class="phone">{{ item.phone || '未填写' }}</span>
              <el-button v-if="item.phone" type="primary" size="small" @click="callSeller">
                <el-icon><Phone /></el-icon> 拨打电话
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="modal-footer">
        <el-button @click="close">关闭</el-button>
        <el-button :type="isFavorite ? 'warning' : 'success'" @click="toggleFavorite">
          <el-icon><Star /></el-icon> {{ isFavorite ? '已收藏' : '我想要' }}
        </el-button>
        <el-button v-if="canBuy" type="danger" @click="showBuyDialog = true">
          <el-icon><ShoppingCart /></el-icon> 立即购买
        </el-button>
      </div>
    </div>

    <!-- 购买确认弹窗 -->
    <el-dialog v-model="showBuyDialog" title="确认购买" width="400px" center>
      <div class="buy-confirm">
        <div class="buy-item">
          <img :src="item.coverImg || images[0]" />
          <div class="buy-info">
            <h4>{{ item.title }}</h4>
            <span class="buy-price">¥{{ item.price }}</span>
          </div>
        </div>
        <el-divider />
        <div class="buy-form">
          <p class="buyer-tip">请留言给卖家（选填）：</p>
          <el-input v-model="buyMessage" type="textarea" :rows="3" placeholder="可以说说您的需求或交易时间" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showBuyDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmBuy" :loading="buying">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Phone, Star, ShoppingCart } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  visible: Boolean,
  item: Object
})

const emit = defineEmits(['close', 'favorite-change'])

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id
const currentRole = userInfo.role || 'resident'

const currentImageIndex = ref(0)
const isFavorite = ref(false)
const showBuyDialog = ref(false)
const buyMessage = ref('')
const buying = ref(false)

const images = computed(() => {
  if (!props.item?.images) return []
  try {
    const imgs = JSON.parse(props.item.images)
    return Array.isArray(imgs) ? imgs : [props.item.coverImg]
  } catch {
    return props.item.coverImg ? [props.item.coverImg] : []
  }
})

// 可以购买的条件：已发布状态、不是自己发布、是居民或维修人员
const canBuy = computed(() => {
  return props.item?.status === 1 &&
         props.item?.userId !== currentUserId &&
         (currentRole === 'resident' || currentRole === 'maintainer')
})

watch(() => props.visible, async (val) => {
  if (val) {
    currentImageIndex.value = 0
    await checkFavorite()
  }
})

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

const close = () => {
  emit('close')
}

const callSeller = () => {
  if (props.item.phone) {
    window.location.href = `tel:${props.item.phone}`
  }
}

const checkFavorite = async () => {
  try {
    const res = await request.get(`/idle/favorite/check/${props.item.id}`)
    if (res.code === 200) {
      isFavorite.value = res.data.favorite
    }
  } catch (e) {}
}

const toggleFavorite = async () => {
  try {
    const url = `/idle/favorite/${isFavorite.value ? 'remove' : 'add'}/${props.item.id}`
    const res = await request.post(url)
    if (res.code === 200) {
      isFavorite.value = !isFavorite.value
      emit('favorite-change', { itemId: props.item.id, isFavorite: isFavorite.value })
      ElMessage.success(isFavorite.value ? '收藏成功，卖家已收到通知' : '已取消收藏')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const confirmBuy = async () => {
  buying.value = true
  try {
    const res = await request.post('/idle/order/buy', {
      itemId: props.item.id,
      message: buyMessage.value
    })
    if (res.code === 200) {
      ElMessage.success('购买请求已发送，请等待卖家确认')
      showBuyDialog.value = false
      buyMessage.value = ''
    } else {
      ElMessage.error(res.message || '购买失败')
    }
  } catch (e) {
    ElMessage.error('购买失败')
  } finally {
    buying.value = false
  }
}
</script>

<style scoped>
.idle-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.modal-header button {
  font-size: 24px;
  color: #909399;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
  overflow-y: auto;
}

.image-section {
  width: 40%;
  min-width: 280px;
  position: relative;
}

.image-indicators {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.image-indicators span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
}

.image-indicators span.active {
  background: #409eff;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
}

.detail-row {
  display: flex;
  gap: 12px;
}

.detail-row .label {
  color: #909399;
  width: 60px;
}

.detail-row .value {
  color: #303133;
}

.description-section {
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.description-section .label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.description {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.contact-section {
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.contact-section .label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.contact-info .phone {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.buy-confirm {
  padding: 10px 0;
}

.buy-item {
  display: flex;
  gap: 16px;
  align-items: center;
}

.buy-item img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.buy-info h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.buy-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.buy-form {
  margin-top: 16px;
}

.buyer-tip {
  color: #909399;
  margin-bottom: 8px;
  font-size: 14px;
}
</style>
