import { baseApi } from './baseApi'
import type { Template } from '@/entities/suggestions/types'

type SearchResp = { items: Template[] }

export const kbApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    search: build.query<SearchResp, { query: string; topN: number }>({
      query: (p) => ({ url: '/kb/search', method: 'POST', body: p })
    })
  })
})

export const { useSearchQuery } = kbApi