import { baseApi } from './baseApi'

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface ChatCompletionRequest {
  model: string
  messages: ChatMessage[]
  temperature?: number
  max_tokens?: number
  stream?: boolean
}

export interface ChatCompletionResponse {
  id: string
  object: string
  created: number
  model: string
  choices: Array<{
    index: number
    message: ChatMessage
    finish_reason: string
  }>
  usage?: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

export const chatApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    sendChatMessage: build.mutation<ChatCompletionResponse, ChatCompletionRequest>({
      query: (body) => ({
        url: '/v1/chat/completions',
        method: 'POST',
        body
      })
    })
  })
})

export const { useSendChatMessageMutation } = chatApi
