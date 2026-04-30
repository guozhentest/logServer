import axios from './index'

export const getHospitals = () => axios.get('/agent/hospitals').then(r => r.data.data)
export const getBizTypes = () => axios.get('/agent/dict/biz-types').then(r => r.data.data)
export const getServiceTypes = () => axios.get('/agent/dict/service-types').then(r => r.data.data)
export const queryLogs = (params) => axios.post('/agent/query-direct', params)