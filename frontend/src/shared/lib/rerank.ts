import type { Template } from '@/entities/suggestions/types'

export function rerank(items: Template[], _cls: any): Template[] {
  // naive stable sort: keep order as-is for now
  return items
}