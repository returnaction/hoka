import { baseApi } from './baseApi'
import { env } from '@/shared/config/env'

type ClassifyResp = { category: string; subcategory?: string; score: number }

export const classifyApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    classify: build.query<ClassifyResp, { query: string }>({
      query: ({ query }) => ({ url: env.CLASSIFY_PATH, method: 'POST', body: { query } })
    })
  })
})

export const { useClassifyQuery } = classifyApi