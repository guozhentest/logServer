import request from './request'

export function query(params: any) {
  return request.post('/agent/log/query', params)
}

export function getDict(orgCode: string) {
  return request.get(`/agent/log/dict/${orgCode}`)
}

export function getDetail(logId: string, orgCode: string) {
  return request.get(`/agent/log/detail/${logId}`, { params: { orgCode } })
}
