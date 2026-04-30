import { defineStore } from 'pinia'
import { login as apiLogin } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('jwtToken') || null,
    user: null
  }),
  actions: {
    async login(username, password) {
      const res = await apiLogin(username, password)

      // 检查业务状态码
      if (res.data.code !== 0) {
        throw new Error(res.data.message || '登录失败')
      }

      const { token } = res.data.data
      this.token = token
      this.user = username
      localStorage.setItem('jwtToken', token)
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('jwtToken')
      window.location.href = '/login'
    }
  }
})