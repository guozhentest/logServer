import request from './request'

export function getList(params: any) {
  return request.get('/system/user/list', { params })
}

export function getUser(userId: number) {
  return request.get(`/system/user/${userId}`)
}

export function addUser(data: any) {
  return request.post('/system/user', data)
}

export function updateUser(data: any) {
  return request.put('/system/user', data)
}

export function delUser(ids: string) {
  return request.delete(`/system/user/${ids}`)
}

export function changeStatus(data: any) {
  return request.put('/system/user/changeStatus', data)
}

export function resetPwd(data: any) {
  return request.put('/system/user/resetPwd', data)
}
