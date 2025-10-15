import { createListenerMiddleware, isAnyOf } from '@reduxjs/toolkit'
import { dialogActions } from '@/entities/dialog/dialog.slice'
import { suggestionsActions } from '@/entities/suggestions/suggestions.slice'
import type { Template } from '@/entities/suggestions/types'
import { kbApi, type KbCandidate } from '@/shared/api/kb.api'

export const suggestionsMw = createListenerMiddleware()

suggestionsMw.startListening({
  matcher: isAnyOf(dialogActions.sendClient),
  effect: async (action, api) => {
    const text = String(action.payload)
    api.dispatch(suggestionsActions.loading())

    try {
      // Семантический поиск в базе знаний
      const response = await api.dispatch(
        kbApi.endpoints.semanticSearch.initiate({ text, topK: 5 })
      ).unwrap()

      // Преобразуем кандидатов в шаблоны
      const templates: Template[] = response.candidates.map((c: KbCandidate, idx: number) => ({
        code: `KB-${c.id}`,
        title: c.question,
        body: c.answer,
        source: `${c.category} / ${c.subcategory}`,
        summary: `Совпадение: ${(c.score * 100).toFixed(1)}%`,
        recommendation: idx === 0 ? 'Лучшее совпадение' : undefined
      }))

      api.dispatch(suggestionsActions.ready({
        templates,
        category: response.category,
        subcategory: response.subcategory
      }))
    } catch (e) {
      console.error('KB semantic search failed:', e)
      // Fallback: показываем демо-шаблон если база пустая
      const mock: Template[] = [{
        code: 'DEMO-001',
        title: 'База знаний пуста',
        body:
`Здравствуйте!

Для работы системы необходимо загрузить FAQ данные.
Используйте endpoint: POST /api/v1/faq/import

После загрузки данных здесь будут отображаться релевантные шаблоны ответов.`,
        summary: 'База знаний не содержит данных',
        recommendation: 'Загрузите FAQ через API'
      }]
      api.dispatch(suggestionsActions.ready({ templates: mock }))
    }
  }
})
