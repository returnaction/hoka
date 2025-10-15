import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import { env } from '@/shared/config/env'

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({ baseUrl: env.API_BASE_URL }),
  endpoints: () => ({})
})