import React from 'react'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { dialogActions } from '@/entities/dialog/dialog.slice'
import { Box, Paper, TextField, Button, Stack, Typography, IconButton, CircularProgress } from '@mui/material'
import ClearIcon from '@mui/icons-material/Clear'

export const ChatPanel: React.FC = () => {
  const dispatch = useAppDispatch()
  const messages = useAppSelector(s => s.dialog.messages)
  const isLoading = useAppSelector(s => s.dialog.isLoading)
  const origin = useAppSelector(s => s.editor.origin)

  const [draft, setDraft] = React.useState('')

  // подставляем выбранный шаблон из правой панели
  React.useEffect(() => {
    if (origin?.body) setDraft(origin.body)
  }, [origin])

  const send = () => {
    const text = draft.trim()
    if (!text) return
    dispatch(dialogActions.sendOperator(text))
    setDraft('')
  }

  const onKeyDown: React.KeyboardEventHandler<HTMLDivElement> = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') send()
  }

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Typography variant="h6" sx={{ mb: 1.5, fontWeight: 700 }}>История переписки</Typography>

      <Box sx={{ height: 260, overflow: 'auto', mb: 2, p: 1, borderRadius: 2, background: 'rgba(255,255,255,0.02)' }}>
        {messages.map((m, i) => (
          <Typography variant="body2" key={i} sx={{ mb: .5, opacity: .95 }}>
            <b>{m.author === 'assistant' ? 'AI Assistant' : m.author}:</b> {m.text}
          </Typography>
        ))}
        {isLoading && (
          <Typography variant="body2" sx={{ mb: .5, opacity: .7, fontStyle: 'italic' }}>
            <CircularProgress size={12} sx={{ mr: 1 }} />
            AI генерирует ответ...
          </Typography>
        )}
      </Box>

      <Typography variant="subtitle2" sx={{ mb: .75, opacity: .85 }}>Оператор — вводит ответ…</Typography>

      <Stack direction="row" spacing={1} alignItems="flex-start">
        <TextField
          fullWidth
          value={draft}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={onKeyDown}
          placeholder="Текст ответа или вставьте шаблон справа…"
          multiline
          minRows={2}
        />
        <Stack direction="column" spacing={1}>
          <Button variant="contained" onClick={send} disabled={isLoading}>
            {isLoading ? 'Отправка...' : 'Отправить'}
          </Button>
          <IconButton size="small" onClick={() => setDraft('')} title="Очистить" disabled={isLoading}>
            <ClearIcon fontSize="small" />
          </IconButton>
        </Stack>
      </Stack>
    </Paper>
  )
}
