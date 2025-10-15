import React from 'react'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import {
  Paper, Typography, Stack, Button, Box, Divider, Skeleton,
  Drawer, List, ListItemButton, ListItemText, Chip, LinearProgress, Alert
} from '@mui/material'
import CategoryIcon from '@mui/icons-material/Category'
import { editorActions } from '@/entities/editor/editor.slice'

export const SuggestionsPanel: React.FC = () => {
  const { items, status, category, subcategory } = useAppSelector(s => s.suggestions)
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

  const extractScore = (summary?: string): number => {
    if (!summary) return 0
    const match = summary.match(/(\d+\.?\d*)%/)
    return match ? parseFloat(match[1]) : 0
  }

  return (
    <Paper
      sx={{
        p: 6,
        pt: '6em',
        maxHeight: 'calc(100vh - 200px)',
        overflowY: 'auto'
      }}
    >
      <Typography variant="h6" sx={{ pt: 5.5, mb: 1.5, fontWeight: 700, zIndex: 5 }}>
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
        <Alert severity="info">Подходящих шаблонов не найдено.</Alert>
      )}
      {status === 'error' && (
        <Alert severity="error">Ошибка загрузки шаблонов.</Alert>
      )}

      {status === 'ready' && (
        <Stack spacing={2}>
          {/* Категория от классификатора */}
          {category && (
            <Alert icon={<CategoryIcon />} severity="success" sx={{ mb: 1 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                Категория: {category}
                {subcategory && ` / ${subcategory}`}
              </Typography>
            </Alert>
          )}

          {top && (
            <>
              <Box
                sx={{
                  p: 1.5,
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 2,
                  background: 'rgba(255,255,255,0.02)'
                }}
              >
                <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                  <Typography variant="subtitle2" sx={{ flex: 1, opacity: 0.9 }}>
                    Источник: {top.code} · "{top.title}"
                  </Typography>
                  {top.recommendation && (
                    <Chip label={top.recommendation} size="small" color="success" />
                  )}
                </Stack>

                {top.summary && (
                  <Box sx={{ mb: 1 }}>
                    <Typography variant="body2" sx={{ mb: 0.5, opacity: 0.9 }}>
                      <b>{top.summary}</b>
                    </Typography>
                    <LinearProgress 
                      variant="determinate" 
                      value={extractScore(top.summary)} 
                      sx={{ height: 6, borderRadius: 1 }}
                      color={extractScore(top.summary) > 70 ? 'success' : 'warning'}
                    />
                  </Box>
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
                  Все шаблоны ({items.length})
                </Button>
              </Stack>
            </>
          )}
        </Stack>
      )}

      <Drawer
        anchor="right"
        open={open}
        onClose={() => setOpen(false)}
        PaperProps={{ sx: { width: 420, p: 2 } }}
      >
        <Typography variant="h6" sx={{ mb: 2 }}>
          Все найденные шаблоны ({items.length})
        </Typography>
        {category && (
          <Alert icon={<CategoryIcon />} severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              {category} {subcategory && `/ ${subcategory}`}
            </Typography>
          </Alert>
        )}
        <List>
          {items.map((t, i) => (
            <ListItemButton
              key={i}
              onClick={() => {
                dispatch(editorActions.setOrigin(t))
                setOpen(false)
              }}
              sx={{ 
                mb: 1, 
                border: '1px solid', 
                borderColor: 'divider', 
                borderRadius: 1,
                flexDirection: 'column',
                alignItems: 'flex-start'
              }}
            >
              <Stack direction="row" spacing={1} alignItems="center" sx={{ width: '100%', mb: 0.5 }}>
                <ListItemText
                  primary={`${t.code} · ${t.title}`}
                  secondary={t.source}
                  primaryTypographyProps={{ sx: { fontWeight: 600, fontSize: '0.9rem' } }}
                  secondaryTypographyProps={{ sx: { fontSize: '0.75rem' } }}
                  sx={{ flex: 1 }}
                />
                {t.recommendation && (
                  <Chip label="TOP" size="small" color="success" />
                )}
              </Stack>
              {t.summary && (
                <Box sx={{ width: '100%' }}>
                  <Typography variant="caption" sx={{ opacity: 0.8 }}>
                    {t.summary}
                  </Typography>
                  <LinearProgress 
                    variant="determinate" 
                    value={extractScore(t.summary)} 
                    sx={{ height: 4, borderRadius: 1, mt: 0.5 }}
                    color={extractScore(t.summary) > 70 ? 'success' : 'warning'}
                  />
                </Box>
              )}
            </ListItemButton>
          ))}
        </List>
      </Drawer>
    </Paper>
  )
}
