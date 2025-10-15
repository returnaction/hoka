import { createListenerMiddleware, isAnyOf } from '@reduxjs/toolkit'
import { dialogActions } from '@/entities/dialog/dialog.slice'
import { chatApi } from '@/shared/api/chat.api'
import type { RootState } from '../store'

export const chatMw = createListenerMiddleware()

chatMw.startListening({
  matcher: isAnyOf(dialogActions.sendOperator),
  effect: async (action, api) => {
    const text = String(action.payload)
    const state = api.getState() as RootState
    
    // Получаем текущую модель и историю сообщений
    const model = state.config.defaultModel || 'Qwen2.5-72B-Instruct-AWQ'
    const messages = state.dialog.messages.map(msg => ({
      role: msg.author === 'client' ? 'user' as const : 'assistant' as const,
      content: msg.text
    }))

    // Добавляем новое сообщение оператора
    messages.push({
      role: 'assistant' as const,
      content: text
    })

    api.dispatch(dialogActions.setLoading(true))

    try {
      // Отправляем запрос к LLM API
      const result = await api.dispatch(
        chatApi.endpoints.sendChatMessage.initiate({
          model,
          messages,
          temperature: 0.7,
          max_tokens: 1000
        })
      ).unwrap()

      // Добавляем ответ ассистента в диалог
      if (result.choices && result.choices.length > 0) {
        const assistantMessage = result.choices[0].message.content
        api.dispatch(dialogActions.receiveAssistant(assistantMessage))
      }
    } catch (error) {
      console.error('Failed to get AI response:', error)
      // В случае ошибки можно добавить сообщение об ошибке
      api.dispatch(dialogActions.receiveAssistant('Извините, произошла ошибка при получении ответа.'))
    } finally {
      api.dispatch(dialogActions.setLoading(false))
    }
  }
})
