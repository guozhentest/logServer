import { defineStore } from 'pinia'
import { ref } from 'vue'
import { setToken, getToken, removeToken } from '@/utils/auth'
import { login as loginApi, logout as logoutApi, getInfo, getRouters } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<any>(null)
  const roles = ref<any[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<any[]>([])

  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    const t = res.data.data.token
    setToken(t)
    token.value = t
    return t
  }

  async function fetchInfo() {
    const res = await getInfo()
    userInfo.value = res.data.data.user
    roles.value = res.data.data.roles || []
    permissions.value = res.data.data.permissions || []
  }

  async function fetchRouters() {
    const res = await getRouters()
    menus.value = res.data.data || []
  }

  async function logout() {
    await logoutApi()
    token.value = null
    userInfo.value = null
    roles.value = []
    permissions.value = []
    removeToken()
  }

  return { token, userInfo, roles, permissions, menus, login, fetchInfo, fetchRouters, logout }
})
