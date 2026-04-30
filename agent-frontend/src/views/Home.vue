<template>
  <div class="log-query-container">
    <!-- 顶部搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline label-width="90px">
        <el-form-item label="机构名称">
          <el-select v-model="searchForm.orgCode" clearable filterable
                     placeholder="请选择机构名称" style="width: 200px" @focus="loadHospitals">
            <el-option v-for="h in hospitals" :key="h.orgCode"
                       :label="h.orgName" :value="h.orgCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.bizTypeCode" clearable placeholder="请选择业务类型" style="width: 200px">
            <el-option v-for="b in bizTypes" :key="b.code"
                       :label="b.name" :value="b.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型">
          <el-select v-model="searchForm.serviceType" clearable placeholder="请选择服务类型" style="width: 200px">
            <el-option v-for="s in serviceTypes" :key="s.code"
                       :label="s.name" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.responseStatus" clearable placeholder="全部状态" style="width: 200px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILURE" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="searchForm.dateRange" type="datetimerange"
                          range-separator="至" start-placeholder="开始时间"
                          end-placeholder="结束时间" style="width: 340px" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="TraceID">
          <el-input v-model="searchForm.traceIdPrefix" placeholder="TraceID前缀" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="" label-width="0">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="exportLogs">导出记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="logs" border stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="orgCode" label="机构代码" width="150" show-overflow-tooltip />
        <el-table-column prop="bizTypeCode" label="业务类型" width="80" />
        <el-table-column prop="operation" label="操作" min-width="150" show-overflow-tooltip />
        <el-table-column prop="apiName" label="接口" width="120" show-overflow-tooltip />
        <el-table-column prop="responseStatus" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.responseStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.responseStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="page"
            :page-size="size"
            :total="total"
            layout="prev, pager, next, sizes, total"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange"
            @current-change="search" />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="750px" top="5vh">
      <pre class="detail-content">{{ detailContent }}</pre>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHospitals, getBizTypes, getServiceTypes, queryLogs } from '../api/log'
import { ElMessage } from 'element-plus'

const hospitals = ref([])
const bizTypes = ref([])
const serviceTypes = ref([])
const logs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

const searchForm = ref({
  orgCode: '',
  bizTypeCode: '',
  serviceType: '',
  responseStatus: '',
  dateRange: null,
  userId: '',
  traceIdPrefix: ''
})

const detailVisible = ref(false)
const detailContent = ref('')

const loadHospitals = async () => {
  try { hospitals.value = await getHospitals() } catch (e) {}
}
const loadDicts = async () => {
  try {
    bizTypes.value = await getBizTypes()
    serviceTypes.value = await getServiceTypes()
  } catch (e) {}
}

const search = async () => {
  loading.value = true
  try {
    const params = {
      orgCode: searchForm.value.orgCode,
      bizTypeCode: searchForm.value.bizTypeCode,
      serviceType: searchForm.value.serviceType,
      responseStatus: searchForm.value.responseStatus,
      userId: searchForm.value.userId,
      traceIdPrefix: searchForm.value.traceIdPrefix,
      page: page.value,
      size: size.value
    }
    if (searchForm.value.dateRange) {
      const [start, end] = searchForm.value.dateRange
      params.startTime = formatDateTime(start)
      params.endTime = formatDateTime(end)
    }
    const res = await queryLogs(params)
    logs.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  searchForm.value = {
    orgCode: '',
    bizTypeCode: '',
    serviceType: '',
    responseStatus: '',
    dateRange: null,
    userId: '',
    traceIdPrefix: ''
  }
  page.value = 1
  search()
}

const exportLogs = () => {
  ElMessage.info('导出功能暂未开放，敬请期待')
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  search()
}

const showDetail = (row) => {
  detailContent.value = JSON.stringify(row, null, 2)
  detailVisible.value = true
}

const formatDateTime = (date) => {
  if (!date) return null
  const d = new Date(date)
  return d.toISOString().replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadDicts()
})
</script>

<style scoped>
.log-query-container {
  padding: 20px;
  background: #f5f7fb;
  min-height: calc(100vh - 60px);
}
.search-card {
  margin-bottom: 16px;
}
.table-card {
  margin-top: 0;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.detail-content {
  max-height: 500px;
  overflow: auto;
  background: #f8f8f8;
  padding: 16px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>