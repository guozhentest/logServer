<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:#f0f2f5">
    <n-card style="width:420px" title="智能中心管理后台">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="请输入用户名" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" block :loading="loading" @click="handleLogin">登 录</n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessage } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    await userStore.fetchInfo()
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (e: any) {
    message.error(e.response?.data?.msg || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
