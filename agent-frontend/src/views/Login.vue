<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>智能日志查询系统</h2>
      </template>
      <el-form :model="form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="admin123" />
        </el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const store = useAuthStore()
const form = ref({ username: 'admin', password: 'admin123' })
const loading = ref(false)
const handleLogin = async () => {
  loading.value = true
  try {
    await store.login(form.value.username, form.value.password)
    window.location.href = '/'
  } catch (e) {
    console.log(e)
    // 显示后端返回的具体错误信息
    ElMessage.error(e.message || '登录失败，用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fb;
}
.login-card {
  width: 350px;
}
</style>