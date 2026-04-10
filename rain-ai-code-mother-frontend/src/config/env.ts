/**
 * 环境变量配置
 * 所有 import.meta.env 的读取统一在此处，业务代码只引用此文件的导出值。
 */

// API 基础地址（后端接口 + SSE）
export const API_BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL || '/api'

// 应用部署域名（已部署应用的访问根域名）
export const DEPLOY_DOMAIN: string =
  import.meta.env.VITE_DEPLOY_BASE_URL || 'http://localhost'

// 静态资源基础地址（由 API 地址推导，无需单独配置 VITE_PREVIEW_BASE_URL）
export const STATIC_BASE_URL: string = `${API_BASE_URL}/static`

/**
 * 获取已部署应用的完整访问 URL
 * @example getDeployUrl('abc123') → 'http://example.com/abc123'
 */
export const getDeployUrl = (deployKey: string): string => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

/**
 * 获取生成应用的静态预览 URL
 * - 普通模式（html / multi_file）：{STATIC_BASE_URL}/{codeGenType}_{appId}/
 * - Vue 项目模式（vue_project）：{STATIC_BASE_URL}/{codeGenType}_{appId}/dist/index.html
 */
export const getStaticPreviewUrl = (codeGenType: string, appId: string): string => {
  const baseUrl = `${STATIC_BASE_URL}/${codeGenType}_${appId}`
  if (codeGenType === 'vue_project') {
    return `${baseUrl}/dist/index.html`
  }
  return baseUrl
}
