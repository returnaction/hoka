import { baseApi } from './baseApi'

type ClassifyResp = { category: string; subcategory?: string; score: number }

export const classifyApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    classify: build.query<ClassifyResp, { query: string }>({
      query: ({ query }) => ({ url: '/classify', method: 'POST', body: { query } })
    })
  })
})

export const { useClassifyQuery } = classifyApi