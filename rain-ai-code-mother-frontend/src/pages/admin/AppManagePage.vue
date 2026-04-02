<template>
  <div id="appManagePage">
    <!-- Search Form -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" allow-clear />
      </a-form-item>
      <a-form-item label="应用ID">
        <a-input v-model:value="searchParams.id" placeholder="输入ID" allow-clear style="width: 120px" />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" allow-clear style="width: 120px" />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchParams.codeGenType"
          :options="codeGenTypeSelectOptions"
          style="width: 160px"
          @change="(v: string) => { searchParams.codeGenType = v || undefined }"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="resetSearch">重置</a-button>
      </a-form-item>
    </a-form>

    <a-divider />

    <!-- Table -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :loading="loading"
      :scroll="{ x: 1200 }"
      row-key="id"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image v-if="record.cover" :src="record.cover" :width="60" :height="40" style="object-fit: cover; border-radius: 4px" />
          <span v-else class="text-gray">—</span>
        </template>
        <template v-else-if="column.dataIndex === 'appName'">
          <a @click="goToApp(record)">{{ record.appName ?? '未命名' }}</a>
        </template>
        <template v-else-if="column.dataIndex === 'initPrompt'">
          <a-tooltip :title="record.initPrompt">
            <span class="ellipsis-text">{{ record.initPrompt }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag :color="record.priority === 99 ? 'gold' : 'default'">
            {{ record.priority === 99 ? '精选' : (record.priority ?? 0) }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">{{ dayjs(record.deployedTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.key === 'creator'">
          <a-space>
            <a-avatar :src="record.user?.userAvatar" :size="24">
              {{ record.user?.userName?.[0] ?? 'U' }}
            </a-avatar>
            <span>{{ record.user?.userName ?? '-' }}</span>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="goToEdit(record)">
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              :style="{ color: record.priority === 99 ? '#faad14' : '' }"
              @click="doFeatured(record)"
            >
              {{ record.priority === 99 ? '取消精选' : '精选' }}
            </a-button>
            <a-popconfirm
              title="确认删除此应用？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="doDelete(record.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  deleteAppByAdmin,
  listAppVoByPageByAdmin,
  updateAppByAdmin,
} from '@/api/appController.ts'
import { CODE_GEN_TYPE_OPTIONS } from '@/constants/appConstant.ts'

const router = useRouter()

/** Options for the 生成类型 select (maps constant list to ant-design option shape) */
const codeGenTypeSelectOptions = CODE_GEN_TYPE_OPTIONS.map((o) => ({
  label: o.label,
  value: o.value,
}))

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '应用名称', dataIndex: 'appName', width: 160 },
  { title: '封面', dataIndex: 'cover', width: 90 },
  { title: '初始提示词', dataIndex: 'initPrompt', width: 200, ellipsis: true },
  { title: '生成类型', dataIndex: 'codeGenType', width: 120 },
  { title: '优先级', dataIndex: 'priority', width: 90 },
  { title: '部署Key', dataIndex: 'deployKey', width: 90, ellipsis: true },
  { title: '部署时间', dataIndex: 'deployedTime', width: 170 },
  { title: '创建者', key: 'creator', width: 130 },
  { title: '创建时间', dataIndex: 'createTime', width: 170 },
  { title: '操作', key: 'action', fixed: 'right', width: 180 },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({ ...searchParams })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取应用列表失败：' + res.data.message)
    }
  } finally {
    loading.value = false
  }
}

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`,
}))

const doTableChange = (page: any) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const resetSearch = () => {
  searchParams.appName = undefined
  searchParams.id = undefined
  searchParams.userId = undefined
  searchParams.codeGenType = undefined
  searchParams.pageNum = 1
  fetchData()
}

const doDelete = async (id?: string) => {
  if (!id) return
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败：' + res.data.message)
  }
}

const doFeatured = async (record: API.AppVO) => {
  if (!record.id) return
  const newPriority = record.priority === 99 ? 0 : 99
  const res = await updateAppByAdmin({
    id: record.id,
    priority: newPriority,
  })
  if (res.data.code === 0) {
    message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
    fetchData()
  } else {
    message.error('操作失败：' + res.data.message)
  }
}

const goToApp = (record: API.AppVO) => {
  router.push(`/app/${record.id}`)
}

const goToEdit = (record: API.AppVO) => {
  router.push(`/app/edit/${record.id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#appManagePage {
  margin: 8px 16px;
}

.ellipsis-text {
  display: inline-block;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

.text-gray {
  color: #ccc;
}
</style>
