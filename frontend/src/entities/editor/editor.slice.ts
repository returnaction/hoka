import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { EditorState } from './types'
import type { Template } from '@/entities/suggestions/types'

const initialState: EditorState = { origin: null }

const slice = createSlice({
  name: 'editor',
  initialState,
  reducers: {
    setOrigin: (s, a: PayloadAction<Template>) => { 
      // Создаем новый объект с timestamp, чтобы React всегда видел изменение
      s.origin = { ...a.payload, _timestamp: Date.now() }
    }
  }
})

export const editorActions = slice.actions
export default slice.reducer