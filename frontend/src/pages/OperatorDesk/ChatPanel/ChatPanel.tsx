import React from 'react'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { dialogActions } from '@/entities/dialog/dialog.slice'
import { Box, Paper, TextField, Button, Stack, Typography, IconButton, Chip } from '@mui/material'
import ClearIcon from '@mui/icons-material/Clear'
import PersonIcon from '@mui/icons-material/Person'
import SupportAgentIcon from '@mui/icons-material/SupportAgent'

export const ChatPanel: React.FC = () => {
  const dispatch = useAppDispatch()
  const messages = useAppSelector(s => s.dialog.messages)
  const origin = useAppSelector(s => s.editor.origin)

  const [draft, setDraft] = React.useState('')
  const [clientDraft, setClientDraft] = React.useState('')

  // подставляем выбранный шаблон из правой панели
  React.useEffect(() => {
    if (origin?.body) setDraft(origin.body)
  }, [origin])

  const sendOperator = () => {
    const text = draft.trim()
    if (!text) return
    dispatch(dialogActions.sendOperator(text))
    setDraft('')
  }

  const sendClient = () => {
    const text = clientDraft.trim()
    if (!text) return
    dispatch(dialogActions.sendClient(text))
    setClientDraft('')
  }

  const onKeyDown: React.KeyboardEventHandler<HTMLDivElement> = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') sendOperator()
  }

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Typography variant="h6" sx={{ mb: 1.5, fontWeight: 700 }}>История переписки</Typography>

      <Box sx={{ height: 260, overflow: 'auto', mb: 2, p: 1, borderRadius: 2, background: 'rgba(255,255,255,0.02)' }}>
        {messages.map((m, i) => (
          <Box key={i} sx={{ mb: 1, display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <Chip
              icon={m.author === 'client' ? <PersonIcon /> : <SupportAgentIcon />}
              label={m.author === 'client' ? 'Клиент' : 'Оператор'}
              size="small"
              color={m.author === 'client' ? 'primary' : 'success'}
              sx={{ minWidth: 100 }}
            />
            <Typography variant="body2" sx={{ flex: 1, pt: 0.5 }}>
              {m.text}
            </Typography>
          </Box>
        ))}
      </Box>

      {/* Симуляция клиента для тестирования */}
      <Box sx={{ mb: 2, p: 1.5, border: '1px dashed', borderColor: 'primary.main', borderRadius: 1, background: 'rgba(25,118,210,0.05)' }}>
        <Typography variant="subtitle2" sx={{ mb: 0.75, opacity: .85 }}>
          <PersonIcon sx={{ fontSize: 16, verticalAlign: 'text-bottom', mr: 0.5 }} />
          Симуляция клиента (для теста)
        </Typography>
        <Stack direction="row" spacing={1}>
          <TextField
            fullWidth
            size="small"
            value={clientDraft}
            onChange={e => setClientDraft(e.target.value)}
            placeholder="Сообщение от клиента..."
            onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendClient() } }}
          />
          <Button variant="outlined" onClick={sendClient} disabled={!clientDraft.trim()}>
            Отправить
          </Button>
        </Stack>
      </Box>

      <Typography variant="subtitle2" sx={{ mb: .75, opacity: .85 }}>
        <SupportAgentIcon sx={{ fontSize: 16, verticalAlign: 'text-bottom', mr: 0.5 }} />
        Оператор — вводит ответ…
      </Typography>

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
          <Button variant="contained" onClick={sendOperator}>
            Отправить
          </Button>
          <IconButton size="small" onClick={() => setDraft('')} title="Очистить">
            <ClearIcon fontSize="small" />
          </IconButton>
        </Stack>
      </Stack>
    </Paper>
  )
}
