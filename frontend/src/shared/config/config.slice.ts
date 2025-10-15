import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import { env } from './env'

export interface ConfigState {
  apiBaseUrl: string
  apiKey: string
  modelsPath: string
  chatCompletionsPath: string
  healthPath: string
  classifyPath: string
  kbSearchPath: string
  acceptStream: string
  defaultModel: string
  appTitle: string
}

const initialState: ConfigState = {
  apiBaseUrl: env.API_BASE_URL,
  apiKey: env.API_KEY,
  modelsPath: env.MODELS_PATH,
  chatCompletionsPath: env.CHAT_COMPLETIONS_PATH,
  healthPath: env.HEALTH_PATH,
  classifyPath: env.CLASSIFY_PATH,
  kbSearchPath: env.KB_SEARCH_PATH,
  acceptStream: env.ACCEPT_STREAM,
  defaultModel: env.DEFAULT_MODEL,
  appTitle: env.APP_TITLE
}

export const configSlice = createSlice({
  name: 'config',
  initialState,
  reducers: {
    updateConfig: (state, action: PayloadAction<Partial<ConfigState>>) => {
      return { ...state, ...action.payload }
    },
    resetConfig: () => initialState
  }
})

export const { updateConfig, resetConfig } = configSlice.actions
export default configSlice.reducer
