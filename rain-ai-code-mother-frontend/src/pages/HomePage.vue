<template>
  <div id="homePage">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-inner">
        <h1 class="hero-title">
          <img src="/logo.png" alt="logo" class="hero-logo" />
          AI 应用生成平台
        </h1>
        <p class="hero-subtitle">一句话轻松创建网站应用</p>

        <div class="prompt-container">
          <a-textarea
            v-model:value="promptInput"
            placeholder="帮我创建个人博客网站"
            :auto-size="{ minRows: 3, maxRows: 8 }"
            class="prompt-textarea"
            @keydown.ctrl.enter="handleSubmit"
          />
          <div class="prompt-footer">
            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
            >
              <template #icon><ArrowUpOutlined /></template>
            </a-button>
          </div>
        </div>

        <!-- Quick Prompts -->
        <div class="quick-prompts">
          <a-button
            v-for="item in quickPrompts"
            :key="item.label"
            class="quick-btn"
            @click="promptInput = item.prompt"
          >
            {{ item.label }}
          </a-button>
        </div>
      </div>
    </div>

    <!-- App list sections -->
    <div class="sections-wrapper">
      <!-- My Apps -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <a-spin :spinning="myAppsLoading">
          <div v-if="myApps.length > 0" class="app-grid">
            <AppCard v-for="app in myApps" :key="app.id" :app="app" />
          </div>
          <a-empty v-else-if="!myAppsLoading" description="暂无作品，快来创建吧！" />
        </a-spin>
        <div v-if="myTotal > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="myParams.pageNum"
            :page-size="myParams.pageSize"
            :total="myTotal"
            :show-size-changer="false"
            :show-total="(total) => `共 ${total} 个应用`"
            @change="fetchMyApps"
          />
        </div>
      </div>

      <!-- Featured Apps -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <a-spin :spinning="goodAppsLoading">
          <div v-if="goodApps.length > 0" class="app-grid">
            <AppCard v-for="app in goodApps" :key="app.id" :app="app" show-meta />
          </div>
          <a-empty v-else-if="!goodAppsLoading" description="暂无精选案例" />
        </a-spin>
        <div v-if="goodTotal > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="goodParams.pageNum"
            :page-size="goodParams.pageSize"
            :total="goodTotal"
            :show-size-changer="false"
            :show-total="(total) => `共 ${total} 个应用`"
            @change="fetchGoodApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController.ts'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()

const promptInput = ref('')
const submitting = ref(false)

const quickPrompts = [
  {
    label: '个人博客网站',
    prompt:
      '帮我创建一个简洁美观的个人博客网站，整体采用白色 + 深蓝配色，包含顶部导航栏（Logo、菜单、暗黑模式切换）、首页文章卡片列表（含封面图、标题、摘要、发布时间、标签）、侧边栏（个人简介、热门文章、标签云），页面支持响应式布局，代码风格简洁语义化。',
  },
  {
    label: '企业官网',
    prompt:
      '帮我创建一家 AI 科技公司的企业官网，风格专业大气，包含：顶部固定导航栏（带公司 Logo 和菜单）、全屏英雄区（主标题 + 副标题 + 两个 CTA 按钮 + 背景动画）、产品特性介绍区（图标 + 标题 + 描述的卡片网格）、客户评价轮播区、价格方案对比表格、页脚（联系方式 + 社交链接）。',
  },
  {
    label: '在线商城',
    prompt:
      '帮我创建一个时尚女装在线商城首页，包含：顶部导航栏（Logo、搜索框、购物车图标）、全屏 Banner 轮播图（含促销文案和按钮）、分类入口图标区、新品推荐商品网格（含商品图、名称、价格、加购按钮）、优惠活动横幅、底部页脚信息，整体使用玫瑰粉 + 米白色暖色调风格。',
  },
  {
    label: '作品展示网站',
    prompt:
      '帮我创建一个 UI 设计师的个人作品集展示网站，整体风格时尚、有设计感，包含：顶部固定导航（姓名 Logo + 锚点菜单）、全屏自我介绍区（头像、职位、一句话介绍、下载简历按钮）、作品瀑布流画廊（含悬停遮罩显示项目名称和查看按钮）、技能进度条区域、工作经历时间线、联系我表单，使用深色主题配霓虹紫色点缀。',
  },
]

const myApps = ref<API.AppVO[]>([])
const myTotal = ref(0)
const myAppsLoading = ref(false)
const myParams = reactive<API.AppQueryRequest>({ pageNum: 1, pageSize: 8 })

const goodApps = ref<API.AppVO[]>([])
const goodTotal = ref(0)
const goodAppsLoading = ref(false)
const goodParams = reactive<API.AppQueryRequest>({ pageNum: 1, pageSize: 8 })

const fetchMyApps = async () => {
  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage({ ...myParams })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myTotal.value = res.data.data.totalRow ?? 0
    }
  } finally {
    myAppsLoading.value = false
  }
}

const fetchGoodApps = async () => {
  goodAppsLoading.value = true
  try {
    const res = await listGoodAppVoByPage({ ...goodParams })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodTotal.value = res.data.data.totalRow ?? 0
    }
  } finally {
    goodAppsLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }
  submitting.value = true
  try {
    const res = await addApp({ initPrompt: promptInput.value.trim() })
    if (res.data.code === 0 && res.data.data) {
      router.push(`/app/${res.data.data}`)
    } else {
      message.error('创建应用失败：' + res.data.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<style scoped>
#homePage {
  margin: -24px;
  min-height: 100vh;
  background: linear-gradient(160deg, #a8edea 0%, #c4f0ed 25%, #daf1fb 55%, #edf6ff 100%);
  padding: 0 20px;
}

.hero-section {
  padding: 60px 24px 48px;
  text-align: center;
}

.hero-inner {
  max-width: 900px;
  margin: 0 auto;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-logo {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: contain;
  background: #fff;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  font-size: 16px;
  color: #555;
  margin-bottom: 32px;
}

.prompt-container {
  max-width: 680px;
  margin: 0 auto 24px;
  background: #fff;
  border-radius: 16px;
  padding: 16px 16px 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.prompt-textarea {
  border: none !important;
  box-shadow: none !important;
  resize: none;
  font-size: 15px;
  padding: 0;
}

.prompt-textarea:focus {
  outline: none;
  box-shadow: none !important;
}

.prompt-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 10px;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.quick-btn {
  border-radius: 20px;
  color: #444;
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #fff;
  border-color: #1677ff;
  color: #1677ff;
}

.sections-wrapper {
  max-width: 1100px;
  margin: 0 auto;
  padding: 8px 32px 8px;
  min-height: 300px;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.5);
}
</style>
