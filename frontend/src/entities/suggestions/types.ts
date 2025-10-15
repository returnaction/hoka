export type Template = {
  code: string           // KB-001
  title: string          // "Сброс пароля"
  body: string           // сам текст шаблона
  source?: string        // ссылка/ид KB или раздел БЗ
  summary?: string       // краткое резюме запроса
  recommendation?: string// "использовать шаблон ..."
}

export type Status = 'idle' | 'loading' | 'ready' | 'empty' | 'error'
