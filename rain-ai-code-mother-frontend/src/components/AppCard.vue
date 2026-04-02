<template>
  <div class="app-card">
    <div class="app-card-cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-card-placeholder">
        <img src="/logo.png" alt="logo" class="placeholder-logo" />
      </div>
      <div class="app-card-overlay">
        <a-button class="overlay-btn" @click.stop="goToApp">查看对话</a-button>
        <a-button v-if="app.deployKey" class="overlay-btn" @click.stop="openDeployedApp">查看作品</a-button>
      </div>
    </div>
    <div class="app-card-info">
      <div class="app-card-name">{{ app.appName ?? '未命名应用' }}</div>
      <!-- Mine: show create time -->
      <div v-if="!showMeta" class="app-card-time">创建于 {{ formatTime(app.createTime) }}</div>
      <!-- Featured: show author + type tag -->
      <div v-else class="app-card-meta">
        <a-space>
          <a-avatar :src="app.user?.userAvatar" :size="20" />
          <span class="app-author">{{ app.user?.userName ?? '匿名用户' }}</span>
          <a-tag color="blue" class="app-type-tag">{{ getAppTypeLabel(app.codeGenType) }}</a-tag>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import zhCn from 'dayjs/locale/zh-cn'
import { CODE_GEN_TYPE_LABEL } from '@/constants/appConstant.ts'

dayjs.extend(relativeTime)
dayjs.locale(zhCn)

interface Props {
  app: API.AppVO
  /** Show author/type metadata (featured mode) instead of create time (mine mode) */
  showMeta?: boolean
}

const props = withDefaults(defineProps<Props>(), { showMeta: false })

const router = useRouter()

const goToApp = () => {
  router.push(`/app/${props.app.id}?view=1`)
}

const openDeployedApp = () => {
  if (props.app.deployKey) {
    const deployBase = import.meta.env.VITE_DEPLOY_BASE_URL as string
    window.open(`${deployBase}/${props.app.deployKey}`, '_blank')
  }
}

const formatTime = (time?: string) => {
  if (!time) return '未知'
  return dayjs(time).fromNow()
}

const getAppTypeLabel = (type?: string) => {
  if (!type) return '应用'
  return CODE_GEN_TYPE_LABEL[type] ?? type
}
</script>

<style scoped>
.app-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.app-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #d0e8ff;
}

.app-card-cover {
  position: relative;
  width: 100%;
  height: 150px;
  overflow: hidden;
  background: #f5f7fa;
}

.app-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-card-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f4fd 100%);
}

.placeholder-logo {
  width: 48px;
  height: 48px;
  opacity: 0.4;
}

.app-card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.48);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.app-card:hover .app-card-overlay {
  opacity: 1;
  pointer-events: auto;
}

.overlay-btn {
  width: 116px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 20px;
  border: 1.5px solid rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  backdrop-filter: blur(4px);
  transition:
    background 0.15s,
    color 0.15s;
}

.overlay-btn:hover {
  background: rgba(255, 255, 255, 0.92) !important;
  color: #1677ff !important;
  border-color: transparent !important;
}

.app-card-info {
  padding: 12px 14px;
}

.app-card-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-card-time {
  font-size: 12px;
  color: #999;
}

.app-card-meta {
  margin-top: 4px;
}

.app-author {
  font-size: 12px;
  color: #666;
}

.app-type-tag {
  font-size: 11px;
  line-height: 1.4;
  padding: 0 6px;
}
</style>
