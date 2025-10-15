import { baseApi } from './baseApi'

export type ClassifyResponse = { 
  category: string
  confidence: number 
}

export const classifyApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    classify: build.mutation<ClassifyResponse, { text: string }>({
      query: (params) => ({ 
        url: '/v1/classify', 
        method: 'POST', 
        body: params 
      })
    })
  })
})

export const { useClassifyMutation } = classifyApi