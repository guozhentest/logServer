import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const instance = axios.create({
  baseURL: '/agent-mcp-server',
  timeout: 10000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('jwtToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

instance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwtToken')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default instance