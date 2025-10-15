import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { Template, Status } from './types'

type State = { 
  items: Template[]
  status: Status
  category?: string
  subcategory?: string
}

const initialState: State = { items: [], status: 'idle' }

const slice = createSlice({
  name: 'suggestions',
  initialState,
  reducers: {
    loading: (s) => { 
      s.status = 'loading'
      s.items = []
      s.category = undefined
      s.subcategory = undefined
    },
    ready: (s, a: PayloadAction<{ templates: Template[]; category?: string; subcategory?: string }>) => {
      s.items = a.payload.templates
      s.category = a.payload.category
      s.subcategory = a.payload.subcategory
      s.status = a.payload.templates.length ? 'ready' : 'empty'
    },
    error: (s) => { s.status = 'error' }
  }
})

export const suggestionsActions = slice.actions
export default slice.reducer