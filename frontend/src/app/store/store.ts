import { configureStore } from '@reduxjs/toolkit'
import { rootReducer } from './rootReducer'
import { suggestionsMw } from './middlewares/suggestionsMw'
import { chatMw } from './middlewares/chatMw'
import { toastsMw } from './middlewares/toastsMw'
import { analyticsMw } from './middlewares/analyticsMw'
import { baseApi } from '@/shared/api/baseApi'

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefault) =>
    getDefault().concat(
      baseApi.middleware,
      suggestionsMw.middleware,
      chatMw.middleware,
      toastsMw,
      analyticsMw
    )
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch