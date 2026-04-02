/**
 * App code generation type constants.
 * Mirrors the backend CodeGenTypeEnum.
 */
export const CODE_GEN_TYPE_OPTIONS = [
  { label: '全部', value: '' },
  { label: '原生 HTML 模式', value: 'html' },
  { label: '原生多文件模式', value: 'multi_file' },
] as const

export type CodeGenTypeValue = (typeof CODE_GEN_TYPE_OPTIONS)[number]['value']

/** Map from value → display label (excludes the "全部" placeholder) */
export const CODE_GEN_TYPE_LABEL: Record<string, string> = {
  html: '原生 HTML 模式',
  multi_file: '原生多文件模式',
}
