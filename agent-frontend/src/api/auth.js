import axios from './index'

export const login = (username, password) => {
  return axios.post('/agent/login', { username, password })
}