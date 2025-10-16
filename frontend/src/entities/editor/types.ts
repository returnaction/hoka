import type { Template } from '@/entities/suggestions/types'

export type TemplateWithTimestamp = Template & { _timestamp?: number }
export type EditorState = { origin: TemplateWithTimestamp | null }