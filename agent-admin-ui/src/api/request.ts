import axios from 'axios'
import { getToken, removeToken } from '@/utils/auth'
import { useRouter } from 'vue-router'

const request = axios.create({
  baseURL: '',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
