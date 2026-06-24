<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="brand-section">
          <div class="brand-icon">
            <el-icon size="48" color="#fff"><Flag /></el-icon>
          </div>
          <h1 class="brand-title">社区便民生活服务系统</h1>
          <p class="brand-desc">让生活更便捷，让社区更温暖</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon size="20" color="rgba(255,255,255,0.8)"><Bell /></el-icon>
              <span>社区公告</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="rgba(255,255,255,0.8)"><Tools /></el-icon>
              <span>便民报修</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="rgba(255,255,255,0.8)"><Goods /></el-icon>
              <span>闲置流转</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="rgba(255,255,255,0.8)"><Flag /></el-icon>
              <span>社区活动</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-box">
          <div class="login-header">
            <h2 class="login-title">欢迎回来</h2>
            <p class="login-subtitle">请登录您的账号</p>
          </div>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="login-form">
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-icon class="input-icon" color="#909399"><User /></el-icon>
                <el-input
                  v-model="form.username"
                  placeholder="用户名"
                  size="large"
                  class="form-input"
                />
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-icon class="input-icon" color="#909399"><Lock /></el-icon>
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="密码"
                  size="large"
                  show-password
                  class="form-input"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>
            <el-form-item class="login-actions">
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                <el-icon v-if="!loading"><Right /></el-icon>
                登 录
              </el-button>
            </el-form-item>
            <div class="login-footer">
              <span class="footer-text">还没有账号？</span>
              <el-link type="primary" class="register-link" @click="goRegister">立即注册</el-link>
            </div>
          </el-form>
          <div class="demo-info">
            <span class="demo-label">演示账号：</span>
            <span class="demo-account">admin / 123456</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { Flag, Bell, Tools, Goods, Star, User, Lock, Right } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 登录方法
const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      if (res.code === 200) {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (err) {
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 跳转注册页
const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 900px;
  height: 500px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.brand-section {
  text-align: center;
}

.brand-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.9;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 320px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
}

.login-form {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 15px;
  z-index: 1;
}

.form-input {
  padding-left: 45px !important;
  border-radius: 10px;
  height: 48px;
  font-size: 15px;
}

.login-actions {
  margin-top: 10px;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #7c8eea 0%, #865ba2 100%);
}

.login-footer {
  text-align: center;
  padding-top: 10px;
}

.footer-text {
  font-size: 14px;
  color: #909399;
}

.register-link {
  font-size: 14px;
  font-weight: 500;
}

.demo-info {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.demo-label {
  font-size: 12px;
  color: #909399;
}

.demo-account {
  font-size: 12px;
  color: #409EFF;
  font-family: 'Courier New', monospace;
  margin-left: 8px;
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    height: auto;
  }
  
  .login-left {
    padding: 40px 20px;
  }
  
  .login-right {
    padding: 30px 20px;
  }
  
  .feature-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
