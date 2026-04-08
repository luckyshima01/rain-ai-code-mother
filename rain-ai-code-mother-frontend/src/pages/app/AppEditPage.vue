<template>
  <div id="appEditPage">
    <!-- Page Header -->
    <div class="page-header">
      <a-button type="text" class="back-btn" @click="router.back()">
        <template #icon><ArrowLeftOutlined /></template>
        返回
      </a-button>
      <h2 class="page-title">编辑应用信息</h2>
    </div>

    <!-- Basic Info Card -->
    <a-card class="section-card" title="基本信息" :loading="cardLoading">
      <a-form layout="vertical" :model="formState" ref="formRef" @finish="handleSubmit">
        <!-- App Name -->
        <a-form-item
          label="应用名称"
          name="appName"
          :rules="[{ required: true, message: '请输入应用名称' }]"
        >
          <a-input
            v-model:value="formState.appName"
            placeholder="请输入应用名称"
            :maxlength="50"
            show-count
          />
        </a-form-item>

        <!-- Cover (admin only) -->
        <template v-if="isAdmin">
          <a-form-item label="应用封面" name="cover">
            <a-input
              v-model:value="formState.cover"
              placeholder="请输入图片链接"
            />
            <div v-if="formState.cover" class="cover-preview-wrap">
              <img :src="formState.cover" alt="封面预览" class="cover-preview" />
            </div>
            <div class="field-hint">支持图片链接，建议尺寸：400x300</div>
          </a-form-item>

          <!-- Priority (admin only) -->
          <a-form-item label="优先级" name="priority">
            <a-input-number
              v-model:value="formState.priority"
              :min="0"
              :max="99"
              style="width: 180px"
            />
            <div class="field-hint">设置为99表示精选应用</div>
          </a-form-item>
        </template>

        <!-- Init Prompt (readonly) -->
        <a-form-item label="初始提示词">
          <a-textarea
            :value="appDetail?.initPrompt ?? ''"
            :auto-size="{ minRows: 3, maxRows: 6 }"
            :maxlength="1000"
            show-count
            disabled
          />
          <div class="field-hint">初始提示词不可修改</div>
        </a-form-item>

        <!-- Code Gen Type (readonly) -->
        <a-form-item label="生成类型">
          <a-input :value="appDetail?.codeGenType ?? ''" disabled />
          <div class="field-hint">生成类型不可修改</div>
        </a-form-item>

        <!-- Deploy Key (readonly) -->
        <a-form-item label="部署密钥">
          <a-input :value="appDetail?.deployKey ?? ''" disabled />
          <div class="field-hint">部署密钥不可修改</div>
        </a-form-item>

        <!-- Actions -->
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">
              保存修改
            </a-button>
            <a-button @click="resetForm">重置</a-button>
            <a-button type="link" @click="goToChat">进入对话</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- App Meta Info Card -->
    <a-card class="section-card" title="应用信息" :loading="cardLoading">
      <div class="meta-grid">
        <div class="meta-cell">
          <span class="meta-label">应用ID</span>
          <span class="meta-value">{{ appDetail?.id ?? '-' }}</span>
        </div>
        <div class="meta-cell">
          <span class="meta-label">创建者</span>
          <span class="meta-value">
            <a-space>
              <a-avatar :src="appDetail?.user?.userAvatar" :size="20">
                {{ appDetail?.user?.userName?.[0] ?? 'U' }}
              </a-avatar>
              <span>{{ appDetail?.user?.userName ?? '-' }}</span>
            </a-space>
          </span>
        </div>
        <div class="meta-cell">
          <span class="meta-label">创建时间</span>
          <span class="meta-value">{{ formatDateTime(appDetail?.createTime) }}</span>
        </div>
        <div class="meta-cell">
          <span class="meta-label">更新时间</span>
          <span class="meta-value">{{ formatDateTime(appDetail?.updateTime) }}</span>
        </div>
        <div class="meta-cell">
          <span class="meta-label">部署时间</span>
          <span class="meta-value">{{ formatDateTime(appDetail?.deployedTime) }}</span>
        </div>
        <div class="meta-cell">
          <span class="meta-label">访问链接</span>
          <span class="meta-value">
            <a
              v-if="appDetail?.deployKey"
              :href="getDeployUrl(appDetail.deployKey)"
              target="_blank"
              class="meta-link"
            >查看预览</a>
            <span v-else class="meta-empty">-</span>
          </span>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getAppVoById, getAppVoByIdByAdmin, updateApp, updateAppByAdmin } from '@/api/appController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { getDeployUrl } from '@/config/env'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.appId as string
const cardLoading = ref(true)
const submitting = ref(false)
const formRef = ref()

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

interface FormState {
  appName: string
  cover?: string
  priority?: number
}

const formState = reactive<FormState>({
  appName: '',
  cover: undefined,
  priority: undefined,
})

const appDetail = ref<API.AppVO | null>(null)

const syncFormFromDetail = () => {
  if (!appDetail.value) return
  formState.appName = appDetail.value.appName ?? ''
  formState.cover = appDetail.value.cover ?? undefined
  formState.priority = appDetail.value.priority ?? 0
}

const fetchAppDetail = async () => {
  cardLoading.value = true
  try {
    const res = isAdmin.value
      ? await getAppVoByIdByAdmin({ id: appId })
      : await getAppVoById({ id: appId })
    if (res.data.code === 0 && res.data.data) {
      appDetail.value = res.data.data
      syncFormFromDetail()
    } else {
      message.error('获取应用信息失败：' + res.data.message)
      router.back()
    }
  } catch {
    message.error('获取应用信息失败')
    router.back()
  } finally {
    cardLoading.value = false
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = isAdmin.value
      ? await updateAppByAdmin({
          id: appId,
          appName: formState.appName,
          cover: formState.cover,
          priority: formState.priority,
        })
      : await updateApp({ id: appId, appName: formState.appName })

    if (res.data.code === 0) {
      message.success('保存成功')
      await fetchAppDetail()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  syncFormFromDetail()
  formRef.value?.clearValidate()
}

const goToChat = () => {
  router.push(`/app/${appId}?view=1`)
}

const formatDateTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  fetchAppDetail()
})
</script>

<style scoped>
#appEditPage {
  max-width: 900px;
  margin: 0 auto;
  padding: 4px 0 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0 0;
}

.back-btn {
  color: #555;
  font-size: 14px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #111;
}

.section-card {
  border-radius: 12px;
}

.cover-preview-wrap {
  margin: 10px 0 4px;
  padding: 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  display: inline-block;
}

.cover-preview {
  max-width: 280px;
  max-height: 180px;
  object-fit: cover;
  border-radius: 6px;
  display: block;
}

.field-hint {
  margin-top: 4px;
  color: #aaa;
  font-size: 12px;
}

/* Metadata grid */
.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border-top: 1px solid #f0f0f0;
  border-left: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.meta-cell {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  border-right: 1px solid #f0f0f0;
  background: #fff;
  gap: 0;
}

.meta-label {
  width: 80px;
  color: #888;
  font-size: 13px;
  flex-shrink: 0;
  font-weight: 500;
}

.meta-value {
  color: #333;
  font-size: 13px;
  word-break: break-all;
}

.meta-link {
  color: #1677ff;
  text-decoration: none;
}

.meta-link:hover {
  text-decoration: underline;
}

.meta-empty {
  color: #ccc;
}
</style>
