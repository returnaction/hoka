import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { Message } from './types'

type State = { 
  messages: Message[]
  isLoading: boolean
}

const initialState: State = {
  messages: [{ author: 'client', text: 'Забыл пароль, не приходит письмо для сброса.' }],
  isLoading: false
}

const slice = createSlice({
  name: 'dialog',
  initialState,
  reducers: {
    sendClient: (state, action: PayloadAction<string>) => {
      state.messages.push({ author: 'client', text: action.payload })
    },
    sendOperator: (state, action: PayloadAction<string>) => {
      state.messages.push({ author: 'operator', text: action.payload })
    },
    receiveAssistant: (state, action: PayloadAction<string>) => {
      state.messages.push({ author: 'assistant', text: action.payload })
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload
    }
  }
})

export const dialogActions = slice.actions
export default slice.reducer