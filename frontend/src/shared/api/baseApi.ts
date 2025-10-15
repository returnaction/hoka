import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import type { RootState } from '@/app/store/store'

const dynamicBaseQuery: ReturnType<typeof fetchBaseQuery> = async (args, api, extraOptions) => {
  const state = api.getState() as RootState
  const baseUrl = state.config.apiBaseUrl
  const apiKey = state.config.apiKey
  
  const baseQuery = fetchBaseQuery({ 
    baseUrl,
    prepareHeaders: (headers) => {
      if (apiKey) {
        headers.set('Authorization', `Bearer ${apiKey}`)
      }
      return headers
    }
  })
  return baseQuery(args, api, extraOptions)
}

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery: dynamicBaseQuery,
  endpoints: () => ({})
})