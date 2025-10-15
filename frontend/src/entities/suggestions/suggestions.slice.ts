import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { Template, Status } from './types'

type State = { items: Template[]; status: Status }

const initialState: State = { items: [], status: 'idle' }

const slice = createSlice({
  name: 'suggestions',
  initialState,
  reducers: {
    loading: (s) => { s.status = 'loading'; s.items = [] },
    ready: (s, a: PayloadAction<Template[]>) => {
      s.items = a.payload
      s.status = a.payload.length ? 'ready' : 'empty'
    },
    error: (s) => { s.status = 'error' }
  }
})

export const suggestionsActions = slice.actions
export default slice.reducer