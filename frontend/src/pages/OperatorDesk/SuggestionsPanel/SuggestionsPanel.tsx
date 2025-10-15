import React from 'react'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import {
  Paper, Typography, Stack, Button, Box, Divider, Skeleton,
  Drawer, List, ListItemButton, ListItemText
} from '@mui/material'
import { editorActions } from '@/entities/editor/editor.slice'

export const SuggestionsPanel: React.FC = () => {
  const { items, status } = useAppSelector(s => s.suggestions)
  const dispatch = useAppDispatch()
  const top = items[0]
  const [open, setOpen] = React.useState(false)

  const renderPlaceholder = () => (
    <Stack spacing={2} alignItems="center" sx={{ opacity: 0.6, py: 3 }}>
      <Box sx={{ width: '100%', height: 80 }}>
        <Skeleton variant="rounded" height={80} />
      </Box>
      <Typography variant="body2">
        Выберите шаблон из базы знаний или дождитесь подсказок.
      </Typography>
      <Button variant="outlined" size="small" onClick={() => setOpen(true)}>
        Открыть шаблоны
      </Button>
    </Stack>
  )

  return (
    <Paper sx={{ p: 2, position: 'sticky', top: 16 }}>
      <Typography variant="h6" sx={{ mb: 1.5, fontWeight: 700 }}>
        Шаблон ответа
      </Typography>

      {status === 'idle' && renderPlaceholder()}

      {status === 'loading' && (
        <Stack spacing={1.5}>
          <Skeleton variant="rounded" height={84} />
          <Skeleton variant="rounded" height={160} />
          <Skeleton variant="rounded" height={40} />
        </Stack>
      )}

      {status === 'empty' && (
        <Typography>Подходящих шаблонов не найдено.</Typography>
      )}
      {status === 'error' && (
        <Typography>Ошибка загрузки шаблонов.</Typography>
      )}

      {status === 'ready' && top && (
        <Stack spacing={2}>
          <Box
            sx={{
              p: 1.5,
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 2,
              background: 'rgba(255,255,255,0.02)'
            }}
          >
            <Typography variant="subtitle2" sx={{ mb: 0.5, opacity: 0.9 }}>
              Источник: {top.code} · “{top.title}”
            </Typography>
            {top.summary && (
              <Typography variant="body2" sx={{ mb: 0.5, opacity: 0.9 }}>
                <b>Резюме запроса:</b> {top.summary}
              </Typography>
            )}
            {top.recommendation && (
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                <b>Рекомендация:</b> {top.recommendation}
              </Typography>
            )}
          </Box>

          <Box>
            <Typography variant="subtitle2" sx={{ mb: 0.5, opacity: 0.9 }}>
              Текст шаблона:
            </Typography>
            <Typography
              variant="body2"
              sx={{ whiteSpace: 'pre-wrap', lineHeight: 1.55 }}
            >
              {top.body}
            </Typography>
          </Box>

          <Divider />

          <Stack direction="row" spacing={1}>
            <Button
              variant="contained"
              onClick={() => dispatch(editorActions.setOrigin(top))}
            >
              Вставить в ответ
            </Button>
            <Button variant="outlined" onClick={() => setOpen(true)}>
              Открыть шаблоны
            </Button>
          </Stack>
        </Stack>
      )}

      <Drawer
        anchor="right"
        open={open}
        onClose={() => setOpen(false)}
        PaperProps={{ sx: { width: 420 } }}
      >
        <Box sx={{ p: 2 }}>
          <Typography variant="h6" sx={{ mb: 1 }}>
            Шаблоны KB
          </Typography>
          <List>
            {items.map((t, i) => (
              <ListItemButton
                key={i}
                onClick={() => {
                  dispatch(editorActions.setOrigin(t))
                  setOpen(false)
                }}
              >
                <ListItemText
                  primary={`${t.code} · ${t.title}`}
                  secondary={t.summary || t.body.slice(0, 80) + '…'}
                  primaryTypographyProps={{ sx: { fontWeight: 600 } }}
                />
              </ListItemButton>
            ))}
          </List>
        </Box>
      </Drawer>
    </Paper>
  )
}
