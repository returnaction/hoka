import { createListenerMiddleware, isAnyOf } from '@reduxjs/toolkit'
import { dialogActions } from '@/entities/dialog/dialog.slice'
import { suggestionsActions } from '@/entities/suggestions/suggestions.slice'
import type { Template } from '@/entities/suggestions/types'
import { classifyApi } from '@/shared/api/classify.api'
import { kbApi } from '@/shared/api/kb.api'
import { rerank } from '@/shared/lib/rerank'

export const suggestionsMw = createListenerMiddleware()

suggestionsMw.startListening({
  matcher: isAnyOf(dialogActions.sendClient),
  effect: async (action, api) => {
    const text = String(action.payload)
    api.dispatch(suggestionsActions.loading())

    try {
      const cls = await api.dispatch(classifyApi.endpoints.classify.initiate({ query: text })).unwrap()
      const kb = await api.dispatch(kbApi.endpoints.search.initiate({ query: text, topN: 10 })).unwrap()
      const items = rerank(kb.items as Template[], cls)
      api.dispatch(suggestionsActions.ready(items))
    } catch (e) {
      // dev-fallback: if backend not available, push demo item
      const mock = [{
        code: 'KB-001',
        title: 'Сброс пароля',
        body:
`Здравствуйте, Иван!
Письмо для сброса пароля может не доходить по следующим причинам:
1) Проверьте папку «Спам». 
2) Убедитесь, что домен example.com не заблокирован.
3) Если письмо всё ещё не пришло, могу выслать одноразовый код прямо сюда — подтвердите, пожалуйста.`,
        summary: 'клиент не получает письмо для сброса пароля.',
        recommendation: 'использовать шаблон “Reset Email Not Received”.'
      }]
      api.dispatch(suggestionsActions.ready(mock as any))
    }
  }
})