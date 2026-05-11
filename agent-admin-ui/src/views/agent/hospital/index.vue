<template>
  <n-card title="在线医院">
    <n-data-table :columns="columns" :data="data" :loading="!connected" :pagination="false" size="small" />
    <n-modal v-model:show="showEdit" title="编辑医院" preset="card" style="width:600px">
      <n-form :model="editForm" label-placement="left" label-width="100">
        <n-form-item label="医院名称">
          <n-input v-model:value="editForm.orgName" />
        </n-form-item>
        <n-form-item label="服务地址">
          <n-input v-model:value="editForm.baseUrl" />
        </n-form-item>
        <n-form-item label="API密钥">
          <n-input v-model:value="editForm.apiKey" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch :value="editForm.status === 1" @update:value="v => editForm.status = v ? 1 : 0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEdit = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { update } from '@/api/hospital'
import { useMessage, NTag, NButton } from 'naive-ui'
import { h } from 'vue'
import { useHospitalSSE } from '@/composables/useHospitalSSE'

const message = useMessage()
const { hospitals: data, connected } = useHospitalSSE()
const saving = ref(false)
const showEdit = ref(false)
const editForm = ref<any>({})

const columns = [
  { title: '机构代码', key: 'orgCode', width: 180 },
  { title: '医院名称', key: 'orgName', width: 160 },
  {
    title: '在线状态', key: 'online', width: 100,
    render(row: any) {
      return h(NTag, { type: row.online ? 'success' : 'default' }, { default: () => row.online ? '在线' : '离线' })
    }
  },
  {
    title: '启用状态', key: 'status', width: 100,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'info' : 'warning' }, { default: () => row.status === 1 ? '已启用' : '已停用' })
    }
  },
  { title: '服务地址', key: 'baseUrl', ellipsis: { tooltip: true } },
  {
    title: '最后心跳', key: 'lastHeartbeatTime', width: 180,
    render(row: any) {
      if (row.online || !row.lastHeartbeatTime) return ''
      return new Date(row.lastHeartbeatTime).toLocaleString('zh-CN')
    }
  },
  {
    title: '操作', key: 'actions', width: 120,
    render(row: any) {
      return h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' })
    }
  }
]

function handleEdit(row: any) {
  editForm.value = { ...row }
  showEdit.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await update(editForm.value.id, {
      orgName: editForm.value.orgName,
      baseUrl: editForm.value.baseUrl,
      apiKey: editForm.value.apiKey,
      status: editForm.value.status
    })
    message.success('保存成功')
    showEdit.value = false
  } catch (e) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>
