<template>
  <a-modal
    :open="open"
    title="应用详情"
    :footer="null"
    :width="400"
    @update:open="emit('update:open', $event)"
  >
    <div class="app-detail-body">
      <div class="app-detail-row">
        <span class="app-detail-label">创建者：</span>
        <a-space class="app-detail-value">
          <a-avatar :src="app?.user?.userAvatar" :size="24">
            {{ app?.user?.userName?.[0] ?? 'U' }}
          </a-avatar>
          <span>{{ app?.user?.userName ?? '未知用户' }}</span>
        </a-space>
      </div>
      <div class="app-detail-row">
        <span class="app-detail-label">创建时间：</span>
        <span class="app-detail-value">{{ formatDetailTime(app?.createTime) }}</span>
      </div>
      <div v-if="app?.codeGenType" class="app-detail-row">
        <span class="app-detail-label">生成类型：</span>
        <a-tag color="blue" class="app-detail-value">{{ getCodeGenTypeLabel(app.codeGenType) }}</a-tag>
      </div>
      <div v-if="canEdit" class="app-detail-actions">
        <a-button type="primary" @click="emit('edit')">
          <template #icon><EditOutlined /></template>
          修改
        </a-button>
        <a-popconfirm
          title="确认删除此应用？"
          ok-text="确认"
          cancel-text="取消"
          @confirm="handleDelete"
        >
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            删除
          </a-button>
        </a-popconfirm>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { message } from 'ant-design-vue'
import { DeleteOutlined, EditOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { deleteApp } from '@/api/appController.ts'
import { CODE_GEN_TYPE_LABEL } from '@/constants/appConstant.ts'

interface Props {
  open: boolean
  app: API.AppVO | null
  /** Controls visibility of edit/delete action buttons */
  canEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), { canEdit: false })

const emit = defineEmits<{
  'update:open': [value: boolean]
  /** Fired when the user clicks the "修改" button */
  edit: []
  /** Fired after the app has been successfully deleted */
  deleted: []
}>()

const formatDetailTime = (time?: string) => {
  if (!time) return '未知'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getCodeGenTypeLabel = (type?: string) => {
  if (!type) return ''
  return CODE_GEN_TYPE_LABEL[type] ?? type
}

const handleDelete = async () => {
  if (!props.app?.id) return
  try {
    const res = await deleteApp({ id: props.app.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      emit('update:open', false)
      emit('deleted')
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch {
    message.error('删除失败')
  }
}
</script>

<style scoped>
.app-detail-body {
  padding: 4px 0 8px;
}

.app-detail-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.app-detail-row:last-of-type {
  border-bottom: none;
}

.app-detail-label {
  width: 80px;
  color: #888;
  flex-shrink: 0;
}

.app-detail-value {
  color: #333;
}

.app-detail-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
</style>
