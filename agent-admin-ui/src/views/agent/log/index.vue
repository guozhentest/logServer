<template>
  <n-card title="日志查询">
    <n-form label-placement="left" label-width="90">
      <n-grid :cols="4" :x-gap="16">
        <n-form-item-gi label="机构代码">
          <n-select v-model:value="form.orgCode" placeholder="选择机构" filterable clearable :options="hospitalOptions"
            @update:value="onOrgCodeChange" />
        </n-form-item-gi>
        <n-form-item-gi label="用户工号">
          <n-input v-model:value="form.userId" placeholder="用户工号" />
        </n-form-item-gi>
        <n-form-item-gi label="业务类型">
          <n-select v-model:value="form.bizTypeCode" placeholder="业务类型" clearable :options="bizOptions" />
        </n-form-item-gi>
        <n-form-item-gi label="服务类型">
          <n-select v-model:value="form.serviceType" placeholder="服务类型" clearable :options="serviceOptions" />
        </n-form-item-gi>
        <n-form-item-gi label="执行状态">
          <n-select v-model:value="form.responseStatus" placeholder="执行状态" clearable :options="[
            { label: '成功', value: 'SUCCESS' }, { label: '失败', value: 'FAILURE' }
          ]" />
        </n-form-item-gi>
        <n-form-item-gi label="订单号">
          <n-input v-model:value="form.orderNo" placeholder="订单号" />
        </n-form-item-gi>
        <n-form-item-gi label="开始时间">
          <n-date-picker v-model:formatted-value="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            clearable placeholder="选择开始时间" />
        </n-form-item-gi>
        <n-form-item-gi label="结束时间">
          <n-date-picker v-model:formatted-value="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            clearable placeholder="选择结束时间" />
        </n-form-item-gi>
        <n-form-item-gi label="操作">
          <n-space>
            <n-button type="primary" :loading="loading" @click="handleQuery">查询</n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </n-form-item-gi>
      </n-grid>
    </n-form>
    <n-data-table :columns="columns" :data="data" :loading="loading" :pagination="pagination" size="small"
      style="margin-top:16px" @update:page="handlePageChange" />
  </n-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { query, getDict } from '@/api/log'
import { getList } from '@/api/hospital'
import { useMessage, NTag, NButton } from 'naive-ui'
import { h } from 'vue'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const data = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0 })
const hospitalOptions = ref<Array<{ label: string; value: string }>>([])
const bizOptions = ref<Array<{ label: string; value: string }>>([])
const serviceOptions = ref<Array<{ label: string; value: string }>>([])

const form = reactive<any>({
  orgCode: null,
  userId: '',
  bizTypeCode: null,
  serviceType: null,
  responseStatus: null,
  orderNo: '',
  startTime: null,
  endTime: null,
  traceIdPrefix: ''
})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '操作时间', key: 'createdAt', width: 170 },
  { title: '业务类型', key: 'bizTypeCode', width: 100 },
  { title: '服务类型', key: 'serviceType', width: 100 },
  { title: '操作描述', key: 'operation', width: 160 },
  { title: 'API名称', key: 'apiName', width: 140 },
  { title: '用户', key: 'loginId', width: 100 },
  {
    title: '状态', key: 'responseStatus', width: 80,
    render(row: any) {
      const ok = row.responseStatus === 'SUCCESS'
      return h(NTag, { type: ok ? 'success' : 'error' }, { default: () => ok ? '成功' : '失败' })
    }
  },
  { title: '耗时(ms)', key: 'costMs', width: 80 },
  {
    title: '操作', key: 'actions', width: 80,
    render(row: any) {
      return h(NButton, { size: 'small', onClick: () => handleDetail(row.id) }, { default: () => '详情' })
    }
  }
]

async function onOrgCodeChange(orgCode: string | null) {
  bizOptions.value = []
  serviceOptions.value = []
  form.bizTypeCode = null
  form.serviceType = null
  if (!orgCode) return
  try {
    const res = await getDict(orgCode)
    const d = res.data.data
    bizOptions.value = d.bizTypes || []
    serviceOptions.value = d.serviceTypes || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

async function handleQuery() {
  loading.value = true
  try {
    const params: any = {}
    for (const key of Object.keys(form)) {
      params[key] = form[key] ?? ''
    }
    const res = await query({
      ...params,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    })
    data.value = res.data.data.rows || []
    const total = res.data.data.total || 0
    if (typeof total === 'object') {
      pagination.itemCount = total[Object.keys(total)[0]] || 0
    } else {
      pagination.itemCount = total
    }
  } catch (e) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  Object.assign(form, {
    orgCode: null, userId: '', bizTypeCode: null, serviceType: null,
    responseStatus: null, orderNo: '', startTime: null, endTime: null, traceIdPrefix: ''
  })
  bizOptions.value = []
  serviceOptions.value = []
}

function handlePageChange(page: number) {
  pagination.page = page
  handleQuery()
}

function handleDetail(logId: number) {
  router.push({ name: 'LogDetail', params: { logId }, query: { orgCode: form.orgCode } })
}

onMounted(async () => {
  try {
    const res = await getList()
    const rows = res.data.data || res.data.rows || []
    hospitalOptions.value = rows.map((h: any) => ({
      label: h.orgName + ' (' + h.orgCode + ')',
      value: h.orgCode
    }))
  } catch (e) {
    console.error('加载医院列表失败', e)
  }
})
</script>
