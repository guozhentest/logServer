<template>
  <n-card title="角色管理">
    <n-space vertical>
      <n-space>
        <n-input v-model:value="queryParams.roleName" placeholder="角色名称" style="width:160px" clearable />
        <n-select v-model:value="queryParams.status" placeholder="状态" style="width:120px" clearable :options="[
          { label: '正常', value: '0' }, { label: '停用', value: '1' }
        ]" />
        <n-button type="primary" @click="handleQuery">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="primary" @click="handleAdd">新增</n-button>
      </n-space>
      <n-data-table :columns="columns" :data="data" :loading="loading" :pagination="pagination"
        size="small" @update:page="handlePageChange" />
    </n-space>

    <n-modal v-model:show="showModal" :title="modalTitle" preset="card" style="width:600px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="90">
        <n-form-item path="roleName" label="角色名称">
          <n-input v-model:value="form.roleName" placeholder="角色名称" />
        </n-form-item>
        <n-form-item path="roleKey" label="权限字符">
          <n-input v-model:value="form.roleKey" placeholder="权限字符" />
        </n-form-item>
        <n-form-item path="roleSort" label="显示顺序">
          <n-input-number v-model:value="form.roleSort" style="width:120px" />
        </n-form-item>
        <n-form-item path="status" label="状态">
          <n-radio-group v-model:value="form.status">
            <n-radio value="0">正常</n-radio>
            <n-radio value="1">停用</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="菜单权限">
          <n-tree v-model:checked-keys="form.menuIds" :data="menuOptions" checkable key-field="menuId" label-field="menuName"
            :default-expand-all="true" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { getList, getRole, addRole, updateRole, delRole } from '@/api/role'
import { getRouters } from '@/api/auth'
import { useMessage, NButton, NSpace, NTag, NPopconfirm } from 'naive-ui'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const data = ref<any[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive<any>({ status: '0', roleSort: 0, menuIds: [] })
const modalTitle = ref('')
const menuOptions = ref<any[]>([])

const queryParams = reactive({ roleName: '', status: '0', pageNum: 1, pageSize: 10 })
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 })

const rules = {
  roleName: { required: true, message: '请输入角色名称', trigger: 'blur' },
  roleKey: { required: true, message: '请输入权限字符', trigger: 'blur' }
}

const columns = [
  { title: 'ID', key: 'roleId', width: 60 },
  { title: '角色名称', key: 'roleName', width: 140 },
  { title: '权限字符', key: 'roleKey', width: 140 },
  { title: '显示顺序', key: 'roleSort', width: 80 },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      return h(NTag, { type: row.status === '0' ? 'success' : 'default' }, { default: () => row.status === '0' ? '正常' : '停用' })
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 160,
    render(row: any) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'tiny', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.roleId) }, {
            default: () => '确定删除?',
            trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' })
          })
        ]
      })
    }
  }
]

onMounted(async () => {
  await loadData()
  const res = await getRouters()
  menuOptions.value = res.data.data || []
})

async function loadData() {
  loading.value = true
  const res = await getList(queryParams)
  data.value = res.data.rows || []
  pagination.itemCount = res.data.total || 0
  loading.value = false
}

function handleQuery() { queryParams.pageNum = 1; loadData() }
function handleReset() { Object.assign(queryParams, { roleName: '', status: '0' }); handleQuery() }
function handlePageChange(page: number) { pagination.page = page; queryParams.pageNum = page; loadData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增角色'
  Object.assign(form, { roleId: '', roleName: '', roleKey: '', roleSort: 0, status: '0', menuIds: [] })
  showModal.value = true
}

async function handleEdit(row: any) {
  isEdit.value = true
  modalTitle.value = '编辑角色'
  const res = await getRole(row.roleId)
  const role = res.data.data
  Object.assign(form, {
    roleId: role.roleId, roleName: role.roleName, roleKey: role.roleKey,
    roleSort: role.roleSort, status: role.status, menuIds: role.menuIds || []
  })
  showModal.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateRole({ ...form })
    } else {
      await addRole({ ...form })
    }
    message.success('操作成功')
    showModal.value = false
    loadData()
  } catch (e: any) {
    message.error(e.response?.data?.msg || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await delRole(String(id))
  message.success('删除成功')
  loadData()
}
</script>
