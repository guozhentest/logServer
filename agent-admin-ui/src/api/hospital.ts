import request from './request'

export function getList() {
  return request.get('/agent/hospital/list')
}

export function getById(id: number) {
  return request.get(`/agent/hospital/${id}`)
}

export function update(id: number, data: any) {
  return request.put(`/agent/hospital/${id}`, data)
}
