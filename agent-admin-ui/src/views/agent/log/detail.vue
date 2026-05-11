<template>
  <n-card title="日志明细">
    <n-spin :show="loading">
      <n-descriptions v-if="detail" bordered :column="2" size="small">
        <n-descriptions-item label="日志ID">{{ detail.id }}</n-descriptions-item>
        <n-descriptions-item label="TraceID">{{ detail.traceId }}</n-descriptions-item>
        <n-descriptions-item label="机构代码">{{ detail.orgCode }}</n-descriptions-item>
        <n-descriptions-item label="业务类型">{{ detail.bizTypeCode }}</n-descriptions-item>
        <n-descriptions-item label="API名称">{{ detail.apiName }}</n-descriptions-item>
        <n-descriptions-item label="操作描述">{{ detail.operation }}</n-descriptions-item>
        <n-descriptions-item label="用户ID">{{ detail.userId }}</n-descriptions-item>
        <n-descriptions-item label="登录ID">{{ detail.loginId }}</n-descriptions-item>
        <n-descriptions-item label="请求URL">{{ detail.requestUrl }}</n-descriptions-item>
        <n-descriptions-item label="服务类型">{{ detail.serviceType }}</n-descriptions-item>
        <n-descriptions-item label="请求ID">{{ detail.requestId }}</n-descriptions-item>
        <n-descriptions-item label="响应状态">
          <n-tag :type="detail.responseStatus === 'SUCCESS' ? 'success' : 'error'">{{ detail.responseStatus }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="耗时">{{ detail.costMs }}ms</n-descriptions-item>
        <n-descriptions-item label="订单号">{{ detail.orderNo || '-' }}</n-descriptions-item>
        <n-descriptions-item label="操作时间">{{ detail.createdAt }}</n-descriptions-item>
      </n-descriptions>
      <template v-if="detail">
        <n-divider>请求参数</n-divider>
        <n-code :code="formatJson(detail.requestBody)" language="json" />
        <n-divider>请求头</n-divider>
        <n-code :code="formatJson(detail.requestHeaders)" language="json" />
        <n-divider>响应体</n-divider>
        <n-code :code="formatJson(detail.responseBody)" language="json" />
      </template>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getDetail } from '@/api/log'
import { useMessage } from 'naive-ui'

const route = useRoute()
const message = useMessage()
const loading = ref(false)
const detail = ref<any>(null)

function formatJson(str: string | null): string {
  if (!str) return '无'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

onMounted(async () => {
  const logId = route.params.logId as string
  const orgCode = route.query.orgCode as string
  if (!logId || !orgCode) {
    message.error('缺少日志ID或机构代码参数')
    return
  }
  loading.value = true
  try {
    const res = await getDetail(logId, orgCode)
    detail.value = res.data.data
    if (!detail.value) {
      message.warning('未获取到日志详情')
    }
  } catch (e) {
    message.error('查询日志详情失败')
  } finally {
    loading.value = false
  }
})
</script>
