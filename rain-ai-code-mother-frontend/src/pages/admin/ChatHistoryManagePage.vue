<template>
  <div id="chatHistoryManagePage">
    <!-- Search Form -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用ID">
        <a-input-number
          v-model:value="searchParams.appId"
          placeholder="输入应用ID"
          :controls="false"
          allow-clear
          style="width: 140px"
        />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input-number
          v-model:value="searchParams.userId"
          placeholder="输入用户ID"
          :controls="false"
          allow-clear
          style="width: 140px"
        />
      </a-form-item>
      <a-form-item label="消息类型">
        <a-select
          v-model:value="searchParams.messageType"
          :options="messageTypeOptions"
          style="width: 120px"
          @change="(v: string) => { searchParams.messageType = v || undefined }"
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
      :scroll="{ x: 1000 }"
      row-key="id"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'message'">
          <a-tooltip :title="record.message">
            <span class="ellipsis-text">{{ record.message }}</span>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'messageType'">
          <a-tag :color="record.messageType === 'ai' ? 'blue' : 'green'">
            {{ record.messageType === 'ai' ? 'AI' : '用户' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'appId'">
          <a @click="goToApp(record.appId)">{{ record.appId }}</a>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
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
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController.ts'

const router = useRouter()

const messageTypeOptions = [
  { label: '全部', value: '' },
  { label: 'AI', value: 'AI' },
  { label: '用户', value: 'USER' },
]

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '消息内容', dataIndex: 'message', width: 300, ellipsis: true },
  { title: '消息类型', dataIndex: 'messageType', width: 100 },
  { title: '应用ID', dataIndex: 'appId', width: 120 },
  { title: '用户ID', dataIndex: 'userId', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
]

const data = ref<API.ChatHistory[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({ ...searchParams })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取对话列表失败：' + res.data.message)
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
  searchParams.appId = undefined
  searchParams.userId = undefined
  searchParams.messageType = undefined
  searchParams.pageNum = 1
  fetchData()
}

const goToApp = (appId?: number) => {
  if (appId) router.push(`/app/${appId}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#chatHistoryManagePage {
  margin: 8px 16px;
}

.ellipsis-text {
  display: inline-block;
  max-width: 280px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}
</style>
