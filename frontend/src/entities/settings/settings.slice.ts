import { createSlice } from '@reduxjs/toolkit'
import type { Settings } from './types'

const initialState: Settings = { topN: 5, threshold: 0.5 }

const slice = createSlice({
  name: 'settings',
  initialState,
  reducers: { }
})

export default slice.reducer