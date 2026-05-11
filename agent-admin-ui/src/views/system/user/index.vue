<template>
  <n-card title="用户管理">
    <n-space vertical>
      <n-space>
        <n-input v-model:value="queryParams.userName" placeholder="用户名" style="width:160px" clearable />
        <n-input v-model:value="queryParams.phonenumber" placeholder="手机号" style="width:160px" clearable />
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
        <n-form-item v-if="!isEdit" path="userName" label="用户名">
          <n-input v-model:value="form.userName" placeholder="用户名" />
        </n-form-item>
        <n-form-item path="nickName" label="昵称">
          <n-input v-model:value="form.nickName" placeholder="昵称" />
        </n-form-item>
        <n-form-item path="phonenumber" label="手机号">
          <n-input v-model:value="form.phonenumber" placeholder="手机号" />
        </n-form-item>
        <n-form-item path="email" label="邮箱">
          <n-input v-model:value="form.email" placeholder="邮箱" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="留空则不修改" />
        </n-form-item>
        <n-form-item path="status" label="状态">
          <n-radio-group v-model:value="form.status">
            <n-radio value="0">正常</n-radio>
            <n-radio value="1">停用</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="角色">
          <n-select v-model:value="form.roleIds" multiple placeholder="选择角色" :options="roleOptions" label-field="roleName" value-field="roleId" />
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
import { getList, getUser, addUser, updateUser, delUser, changeStatus, resetPwd } from '@/api/user'
import { optionselect } from '@/api/role'
import { useMessage, NButton, NSpace, NTag, NPopconfirm } from 'naive-ui'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const data = ref<any[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive<any>({ status: '0', password: '', roleIds: [] })
const roleOptions = ref<any[]>([])
const modalTitle = ref('')

const queryParams = reactive({ userName: '', phonenumber: '', status: '0', pageNum: 1, pageSize: 10 })
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0 })

const rules = {
  userName: { required: true, message: '请输入用户名', trigger: 'blur' },
  nickName: { required: true, message: '请输入昵称', trigger: 'blur' }
}

const columns = [
  { title: 'ID', key: 'userId', width: 60 },
  { title: '用户名', key: 'userName', width: 120 },
  { title: '昵称', key: 'nickName', width: 120 },
  { title: '手机号', key: 'phonenumber', width: 120 },
  { title: '邮箱', key: 'email', width: 180 },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      return h(NTag, { type: row.status === '0' ? 'success' : 'default' }, { default: () => row.status === '0' ? '正常' : '停用' })
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 200,
    render(row: any) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'tiny', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.userId) }, {
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
  const res = await optionselect()
  roleOptions.value = res.data.data || []
})

async function loadData() {
  loading.value = true
  const res = await getList(queryParams)
  data.value = res.data.rows || []
  pagination.itemCount = res.data.total || 0
  loading.value = false
}

function handleQuery() { queryParams.pageNum = 1; loadData() }
function handleReset() { Object.assign(queryParams, { userName: '', phonenumber: '', status: '0' }); handleQuery() }
function handlePageChange(page: number) { pagination.page = page; queryParams.pageNum = page; loadData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增用户'
  Object.assign(form, { userId: '', userName: '', nickName: '', phonenumber: '', email: '', password: '', status: '0', roleIds: [] })
  showModal.value = true
}

async function handleEdit(row: any) {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  const res = await getUser(row.userId)
  const user = res.data.data
  Object.assign(form, {
    userId: user.userId, userName: user.userName, nickName: user.nickName,
    phonenumber: user.phonenumber, email: user.email, password: '', status: user.status,
    roleIds: user.roleIds || []
  })
  showModal.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateUser({ ...form })
    } else {
      await addUser({ ...form })
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
  await delUser(String(id))
  message.success('删除成功')
  loadData()
}
</script>
