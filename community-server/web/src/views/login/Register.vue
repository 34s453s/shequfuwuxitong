<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册账号</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item prop="repassword">
          <el-input v-model="form.repassword" type="password" placeholder="请确认密码" />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名（选填）" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width:100%">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="goLogin" style="width:100%">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  repassword: '',
  realName: '',
  phone: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  repassword: [{ required: true, validator: validatePass, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = await request.post('/auth/register', {
        username: form.username,
        password: form.password,
        realName: form.realName,
        phone: form.phone
      })
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.message || '注册失败')
      }
    } catch (error) {
      ElMessage.error('注册请求失败')
    }
  })
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}
.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
</style>
