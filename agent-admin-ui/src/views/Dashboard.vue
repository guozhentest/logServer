<template>
  <div>
    <n-grid :cols="3" :x-gap="16">
      <n-grid-item>
        <n-card>
          <n-statistic label="在线医院" :value="onlineCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="总医院数" :value="totalCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="离线医院" :value="offlineCount" />
        </n-card>
      </n-grid-item>
    </n-grid>
    <n-card title="医院在线状态" style="margin-top:16px">
      <n-data-table :columns="columns" :data="hospitals" :loading="!connected" :pagination="false" size="small" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NTag } from 'naive-ui'
import { h } from 'vue'
import { useHospitalSSE } from '@/composables/useHospitalSSE'

const { hospitals, connected } = useHospitalSSE()

const totalCount = computed(() => hospitals.value.length)
const onlineCount = computed(() => hospitals.value.filter(h => h.online).length)
const offlineCount = computed(() => totalCount.value - onlineCount.value)

const columns = [
  { title: '机构代码', key: 'orgCode', width: 180 },
  { title: '医院名称', key: 'orgName', width: 160 },
  {
    title: '状态', key: 'online', width: 100,
    render(row: any) {
      return h(NTag, { type: row.online ? 'success' : 'default' }, { default: () => row.online ? '在线' : '离线' })
    }
  },
  {
    title: '最后心跳', key: 'lastHeartbeatTime', width: 180,
    render(row: any) {
      if (row.online || !row.lastHeartbeatTime) return ''
      return new Date(row.lastHeartbeatTime).toLocaleString('zh-CN')
    }
  },
  { title: '地址', key: 'baseUrl', ellipsis: { tooltip: true } }
]
</script>
