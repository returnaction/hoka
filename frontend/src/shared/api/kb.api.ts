import { baseApi } from './baseApi'
import type { Template } from '@/entities/suggestions/types'
import { env } from '@/shared/config/env'

type SearchResp = { items: Template[] }

export const kbApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    search: build.query<SearchResp, { query: string; topN: number }>({
      query: (p) => ({ url: env.KB_SEARCH_PATH, method: 'POST', body: p })
    })
  })
})

export const { useSearchQuery } = kbApi