import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { Message } from './types'

type State = { messages: Message[] }

const initialState: State = {
  messages: [{ author: 'client', text: 'Забыл пароль, не приходит письмо для сброса.' }]
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
    }
  }
})

export const dialogActions = slice.actions
export default slice.reducer