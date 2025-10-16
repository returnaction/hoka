import { baseApi } from './baseApi'

export type KbCandidate = {
  id: number
  category: string
  subcategory: string
  question: string
  answer: string
  score: number
}

export type KbSearchResponse = {
  category: string
  subcategory: string
  candidates: KbCandidate[]
}

export const kbApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    // Семантический поиск - возвращает категорию + кандидаты
    semanticSearch: build.mutation<KbSearchResponse, { text: string; topK?: number }>({
      query: (params) => ({
        url: '/api/v1/classify/hybrid',
        method: 'POST',
        body: { text: params.text },
        params: { topK: params.topK || 3 }
      })
    })
  })
})

export const { useSemanticSearchMutation } = kbApi