import { combineReducers } from '@reduxjs/toolkit'
import dialogReducer from '@/entities/dialog/dialog.slice'
import suggestionsReducer from '@/entities/suggestions/suggestions.slice'
import editorReducer from '@/entities/editor/editor.slice'
import settingsReducer from '@/entities/settings/settings.slice'
import { baseApi } from '@/shared/api/baseApi'

export const rootReducer = combineReducers({
  dialog: dialogReducer,
  suggestions: suggestionsReducer,
  editor: editorReducer,
  settings: settingsReducer,
  [baseApi.reducerPath]: baseApi.reducer
})