<template>
  <div class="user-management-page">
    <div class="page-header">
      <h3>人员管理</h3>
      <el-button type="primary" @click="showAddDialog">添加用户</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索用户名、姓名或手机号" 
        style="width: 300px"
        @keyup.enter="fetchList"
      />
      <el-select v-model="roleFilter" placeholder="按角色筛选" clearable style="width: 150px; margin-left: 10px">
        <el-option label="居民" :value="1" />
        <el-option label="维修人员" :value="2" />
        <el-option label="物业管理员" :value="3" />
        <el-option label="家政服务员" :value="4" />
        <el-option label="超级管理员" :value="0" />
      </el-select>
      <el-button type="default" @click="fetchList" style="margin-left: 10px">搜索</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <!-- 判断是否有操作权限 -->
          <template v-if="canOperate(row)">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
          <el-tag v-else-if="row.role === 0" type="danger" size="small">超级管理员</el-tag>
          <el-tag v-else-if="row.role === 3" type="success" size="small">物业管理员</el-tag>
          <span v-else>-</span>
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

    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="isEdit ? '编辑用户' : '添加用户'" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="居民" :value="1" />
            <el-option label="维修人员" :value="2" />
            <el-option label="物业管理员" :value="3" />
            <el-option label="家政服务员" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存修改' : '添加用户' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const role = userInfo.role || 'resident'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const searchKeyword = ref('')
const roleFilter = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const submitting = ref(false)
const form = reactive({ id: '', username: '', realName: '', phone: '', role: 1 })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleMap = { 0: '超级管理员', 1: '居民', 2: '维修人员', 3: '物业管理员', 4: '家政服务员' }
const roleTypeMap = { 0: 'danger', 1: '', 2: 'warning', 3: 'success', 4: 'info' }

const getRoleLabel = (role) => roleMap[role] || role
const getRoleType = (role) => roleTypeMap[role] || ''

// 判断当前用户是否可以操作该行
const canOperate = (row) => {
  // 超级管理员不能操作超级管理员
  if (role === 'admin') {
    return row.role !== 0
  }
  // 物业管理员只能操作居民、维修人员、家政服务员，不能操作超级管理员和其他物业管理员
  if (role === 'manager') {
    return row.role !== 0 && row.role !== 3
  }
  return false
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
      params: { 
        page: page.value, 
        size: size.value,
        keyword: searchKeyword.value || undefined,
        role: roleFilter.value || undefined
      }
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

const showAddDialog = () => {
  isEdit.value = false
  form.id = ''
  form.username = ''
  form.realName = ''
  form.phone = ''
  form.role = 1
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.phone = row.phone
  form.role = row.role
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let res
      if (isEdit.value) {
        res = await request.put(`/admin/users/${form.id}`, {
          username: form.username,
          realName: form.realName,
          phone: form.phone,
          role: form.role
        })
      } else {
        res = await request.post('/admin/users', {
          username: form.username,
          realName: form.realName,
          phone: form.phone,
          role: form.role,
          communityId: 1
        })
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        dialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res.message || (isEdit.value ? '修改失败' : '添加失败'))
      }
    } catch (e) {
      ElMessage.error('请求失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该用户?', '提示', { type: 'warning' })
    const res = await request.delete(`/admin/users/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.user-management-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #303133; }
.search-bar { margin-bottom: 20px; }
</style>
