import request from './request'

export function getList(params: any) {
  return request.get('/system/role/list', { params })
}

export function getRole(roleId: number) {
  return request.get(`/system/role/${roleId}`)
}

export function addRole(data: any) {
  return request.post('/system/role', data)
}

export function updateRole(data: any) {
  return request.put('/system/role', data)
}

export function delRole(ids: string) {
  return request.delete(`/system/role/${ids}`)
}

export function changeStatus(data: any) {
  return request.put('/system/role/changeStatus', data)
}

export function optionselect() {
  return request.get('/system/role/optionselect')
}
