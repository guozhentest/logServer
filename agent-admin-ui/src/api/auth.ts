import request from './request'

export function login(username: string, password: string) {
  return request.post('/auth/login', { username, password })
}

export function logout() {
  return request.delete('/auth/logout')
}

export function getInfo() {
  return request.get('/auth/getInfo')
}

export function getRouters() {
  return request.get('/auth/getRouters')
}
