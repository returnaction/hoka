import React from 'react'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import {
  Paper, Typography, Stack, Button, Box, Divider, Skeleton,
  Drawer, List, ListItemButton, ListItemText, Chip, LinearProgress, Alert, Fade, Zoom, IconButton, Tooltip, Card, CardContent
} from '@mui/material'
import CategoryIcon from '@mui/icons-material/Category'
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome'
import InsertCommentIcon from '@mui/icons-material/InsertComment'
import ListAltIcon from '@mui/icons-material/ListAlt'
import CloseIcon from '@mui/icons-material/Close'
import ThumbUpIcon from '@mui/icons-material/ThumbUp'
import { editorActions } from '@/entities/editor/editor.slice'

export const SuggestionsPanel: React.FC = () => {
  const { items, status, category, subcategory } = useAppSelector(s => s.suggestions)
  const dispatch = useAppDispatch()
  const top = items[0]
  const [open, setOpen] = React.useState(false)

  const renderPlaceholder = () => (
    <Fade in>
      <Stack spacing={2.5} alignItems="center" sx={{ py: 3 }}>
        <Box 
          sx={{
            width: '100%', 
            height: 100,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'linear-gradient(135deg, rgba(155,123,255,0.08) 0%, rgba(91,140,255,0.08) 100%)',
            borderRadius: 2.5,
            border: '2px dashed rgba(155,123,255,0.3)',
            transition: 'all 0.3s ease',
            '&:hover': {
              borderColor: 'rgba(155,123,255,0.5)',
              background: 'linear-gradient(135deg, rgba(155,123,255,0.12) 0%, rgba(91,140,255,0.12) 100%)'
            }
          }}
        >
          <AutoAwesomeIcon sx={{ fontSize: 42, color: 'secondary.main', opacity: 0.5 }} />
        </Box>
        <Typography variant="body2" sx={{ textAlign: 'center', color: 'rgba(255,255,255,0.75)', px: 2 }}>
          Отправьте сообщение от клиента, чтобы получить подсказки из базы знаний
        </Typography>
        <Tooltip title="Открыть все шаблоны">
          <Button 
            variant="outlined" 
            size="small" 
            onClick={() => setOpen(true)}
            startIcon={<ListAltIcon />}
            sx={{
              transition: 'all 0.2s ease',
              color: 'rgba(255,255,255,0.9)',
              '&:hover': {
                transform: 'translateY(-2px)',
                boxShadow: '0 8px 20px rgba(91,140,255,0.3)'
              }
            }}
          >
            Открыть шаблоны
          </Button>
        </Tooltip>
      </Stack>
    </Fade>
  )

  const extractScore = (summary?: string): number => {
    if (!summary) return 0
    const match = summary.match(/(\d+\.?\d*)%/)
    return match ? parseFloat(match[1]) : 0
  }

  return (
    <Paper
      sx={{
        p: 3,
        maxHeight: 'calc(100vh - 200px)',
        overflowY: 'auto',
        background: 'linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%)',
        border: '1px solid rgba(255,255,255,0.1)',
        borderRadius: 2,
        transition: 'transform 0.2s ease, box-shadow 0.2s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.3)'
        },
        '&::-webkit-scrollbar': {
          width: '8px'
        },
        '&::-webkit-scrollbar-track': {
          background: 'rgba(0,0,0,0.2)',
          borderRadius: '4px'
        },
        '&::-webkit-scrollbar-thumb': {
          background: 'rgba(155,123,255,0.4)',
          borderRadius: '4px',
          '&:hover': {
            background: 'rgba(155,123,255,0.6)'
          }
        }
      }}
    >
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <AutoAwesomeIcon sx={{ color: 'secondary.main', fontSize: 24 }} />
        <Typography variant="h6" sx={{ fontWeight: 700, flex: 1, color: 'rgba(255,255,255,0.95)' }}>
          Шаблон ответа
        </Typography>
        {items.length > 0 && (
          <Chip 
            label={`${items.length} вариантов`} 
            size="small" 
            color="secondary" 
            variant="outlined"
            sx={{ fontWeight: 600, color: 'rgba(255,255,255,0.9)' }}
          />
        )}
      </Stack>

      {status === 'idle' && renderPlaceholder()}

      {status === 'loading' && (
        <Fade in>
          <Stack spacing={2}>
            <Skeleton variant="rounded" height={100} sx={{ borderRadius: 3 }} animation="wave" />
            <Skeleton variant="rounded" height={180} sx={{ borderRadius: 3 }} animation="wave" />
            <Skeleton variant="rounded" height={50} sx={{ borderRadius: 3 }} animation="wave" />
          </Stack>
        </Fade>
      )}


      {status === 'empty' && (
        <Fade in>
          <Alert 
            severity="info"
            sx={{ 
              borderRadius: 2.5,
              '& .MuiAlert-message': { width: '100%' }
            }}
          >
            Подходящих шаблонов не найдено. Попробуйте другой запрос.
          </Alert>
        </Fade>
      )}

      {status === 'error' && (
        <Fade in>
          <Alert 
            severity="error"
            sx={{ borderRadius: 2.5 }}
          >
            Ошибка загрузки шаблонов. Проверьте соединение с сервером.
          </Alert>
        </Fade>
      )}

      {status === 'ready' && top && (
        <Fade in>
          <Stack spacing={2.5}>
            {/* Категория от классификатора */}
            {category && (
              <Zoom in style={{ transitionDelay: '100ms' }}>
                <Alert 
                  icon={<CategoryIcon />} 
                  severity="success" 
                  sx={{ 
                    borderRadius: 2.5,
                    background: 'linear-gradient(135deg, rgba(60,203,127,0.15) 0%, rgba(60,203,127,0.08) 100%)',
                    border: '1px solid rgba(60,203,127,0.25)',
                    transition: 'transform 0.2s ease',
                    '&:hover': {
                      transform: 'scale(1.02)'
                    }
                  }}
                >
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    Категория: {category}
                    {subcategory && ` / ${subcategory}`}
                  </Typography>
                </Alert>
              </Zoom>
            )}

            <Zoom in style={{ transitionDelay: '200ms' }}>
              <Card
                sx={{
                  background: 'linear-gradient(135deg, rgba(155,123,255,0.15) 0%, rgba(155,123,255,0.08) 100%)',
                  border: '2px solid rgba(155,123,255,0.3)',
                  borderRadius: 3,
                  position: 'relative',
                  overflow: 'visible',
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: '0 20px 48px rgba(155,123,255,0.35)',
                    border: '2px solid rgba(155,123,255,0.5)'
                  }
                }}
              >
                {top.recommendation && (
                  <Box sx={{ 
                    position: 'absolute', 
                    top: -12, 
                    right: 16,
                    animation: 'pulse 2s ease-in-out infinite',
                    '@keyframes pulse': {
                      '0%, 100%': { transform: 'scale(1)' },
                      '50%': { transform: 'scale(1.05)' }
                    }
                  }}>
                    <Chip 
                      icon={<ThumbUpIcon sx={{ fontSize: 16 }} />}
                      label="Лучшее совпадение" 
                      color="secondary"
                      size="small"
                      sx={{ 
                        fontWeight: 700,
                        boxShadow: '0 4px 16px rgba(155,123,255,0.4)',
                        background: 'linear-gradient(135deg, #9B7BFF 0%, #7B5FE0 100%)'
                      }}
                    />
                  </Box>
                )}

                <CardContent sx={{ pt: top.recommendation ? 3 : 2 }}>
                  <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1.5 }}>
                    <Typography variant="subtitle2" sx={{ flex: 1, fontWeight: 700, color: 'rgba(255,255,255,0.95)' }}>
                      {top.code} · "{top.title}"
                    </Typography>
                  </Stack>

                  {top.summary && (
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" sx={{ mb: 1, fontWeight: 600, color: 'rgba(255,255,255,0.9)' }}>
                        {top.summary}
                      </Typography>
                      <LinearProgress 
                        variant="determinate" 
                        value={extractScore(top.summary)} 
                        sx={{ 
                          height: 8, 
                          borderRadius: 4,
                          background: 'rgba(0,0,0,0.3)',
                          '& .MuiLinearProgress-bar': {
                            borderRadius: 4,
                            background: extractScore(top.summary) > 70
                              ? 'linear-gradient(90deg, #3CCB7F 0%, #34B36E 100%)'
                              : 'linear-gradient(90deg, #FFA726 0%, #FB8C00 100%)',
                            boxShadow: extractScore(top.summary) > 70
                              ? '0 0 12px rgba(60,203,127,0.6)'
                              : '0 0 12px rgba(255,167,38,0.6)'
                          }
                        }}
                      />
                    </Box>
                  )}

                  <Divider sx={{ my: 2, opacity: 0.2 }} />

                  <Typography variant="caption" sx={{ display: 'block', mb: 1, color: 'rgba(255,255,255,0.7)', fontWeight: 600 }}>
                    Текст шаблона:
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{ 
                      whiteSpace: 'pre-wrap', 
                      lineHeight: 1.6,
                      color: 'rgba(255,255,255,0.95)',
                      p: 1.5,
                      background: 'rgba(0,0,0,0.2)',
                      borderRadius: 2,
                      border: '1px solid rgba(255,255,255,0.08)'
                    }}
                  >
                    {top.body}
                  </Typography>
                </CardContent>

                <Box sx={{ p: 2, pt: 0 }}>
                  <Stack direction="row" spacing={1.5}>
                    <Button
                      fullWidth
                      variant="contained"
                      color="secondary"
                      startIcon={<InsertCommentIcon />}
                      onClick={() => dispatch(editorActions.setOrigin(top))}
                      sx={{
                        fontWeight: 700,
                        py: 1.2,
                        transition: 'all 0.2s ease',
                        '&:hover': {
                          transform: 'scale(1.03)',
                          boxShadow: '0 12px 32px rgba(155,123,255,0.5)'
                        }
                      }}
                    >
                      Вставить в ответ
                    </Button>
                    {items.length > 1 && (
                      <Tooltip title="Посмотреть все варианты">
                        <Button 
                          variant="outlined"
                          onClick={() => setOpen(true)}
                          startIcon={<ListAltIcon />}
                          sx={{
                            minWidth: 180,
                            transition: 'all 0.2s ease',
                            '&:hover': {
                              transform: 'translateY(-2px)'
                            }
                          }}
                        >
                          Все ({items.length})
                        </Button>
                      </Tooltip>
                    )}
                  </Stack>
                </Box>
              </Card>
            </Zoom>
          </Stack>
        </Fade>
      )}

      <Drawer
        anchor="right"
        open={open}
        onClose={() => setOpen(false)}
        PaperProps={{ 
          sx: { 
            width: 480, 
            p: 3,
            pt: '88px', // Отступ от верхней панели (HeaderBar 72px + запас)
            position: 'fixed',
            top: 0,
            bottom: 0,
            height: '100vh',
            overflowY: 'auto',
            background: 'linear-gradient(180deg, rgba(20,20,35,0.98) 0%, rgba(15,15,25,0.98) 100%)',
            backdropFilter: 'blur(12px)'
          } 
        }}
      >
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'rgba(255,255,255,0.95)' }}>
            Все найденные шаблоны ({items.length})
          </Typography>
          <IconButton 
            onClick={() => setOpen(false)}
            sx={{
              transition: 'all 0.2s ease',
              '&:hover': {
                transform: 'rotate(90deg)',
                color: 'error.main'
              }
            }}
          >
            <CloseIcon />
          </IconButton>
        </Stack>

        {category && (
          <Alert 
            icon={<CategoryIcon />} 
            severity="info" 
            sx={{ mb: 2, borderRadius: 2 }}
          >
            <Typography variant="body2">
              {category} {subcategory && `/ ${subcategory}`}
            </Typography>
          </Alert>
        )}

        <List sx={{ pb: 2 }}>
          {items.map((t, i) => (
            <Fade in key={i} style={{ transitionDelay: `${i * 50}ms` }}>
              <ListItemButton
                onClick={() => {
                  dispatch(editorActions.setOrigin(t))
                  setOpen(false)
                }}
                sx={{ 
                  mb: 1.5, 
                  border: '1px solid',
                  borderColor: i === 0 ? 'rgba(155,123,255,0.4)' : 'rgba(255,255,255,0.1)',
                  borderRadius: 2.5,
                  flexDirection: 'column',
                  alignItems: 'flex-start',
                  p: 2,
                  background: i === 0 
                    ? 'linear-gradient(135deg, rgba(155,123,255,0.12) 0%, rgba(155,123,255,0.06) 100%)'
                    : 'linear-gradient(135deg, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0.02) 100%)',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateX(-4px)',
                    boxShadow: i === 0
                      ? '0 12px 32px rgba(155,123,255,0.3)'
                      : '0 12px 32px rgba(0,0,0,0.4)',
                    borderColor: i === 0 ? 'rgba(155,123,255,0.6)' : 'rgba(255,255,255,0.2)'
                  }
                }}
              >
                <Stack direction="row" spacing={1} alignItems="center" sx={{ width: '100%', mb: 1 }}>
                  <ListItemText
                    primary={`${t.code} · ${t.title}`}
                    secondary={t.source}
                    primaryTypographyProps={{ sx: { fontWeight: 700, fontSize: '0.95rem', color: 'rgba(255,255,255,0.95)' } }}
                    secondaryTypographyProps={{ sx: { fontSize: '0.75rem', color: 'rgba(255,255,255,0.6)' } }}
                    sx={{ flex: 1 }}
                  />
                  {t.recommendation && (
                    <Chip 
                      label="TOP" 
                      size="small" 
                      color="secondary"
                      sx={{ fontWeight: 700 }}
                    />
                  )}
                </Stack>
                {t.summary && (
                  <Box sx={{ width: '100%' }}>
                    <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)', display: 'block', mb: 0.5 }}>
                      {t.summary}
                    </Typography>
                    <LinearProgress 
                      variant="determinate" 
                      value={extractScore(t.summary)} 
                      sx={{ 
                        height: 5, 
                        borderRadius: 2.5,
                        background: 'rgba(0,0,0,0.25)'
                      }}
                      color={extractScore(t.summary) > 70 ? 'success' : 'warning'}
                    />
                  </Box>
                )}
              </ListItemButton>
            </Fade>
          ))}
        </List>
      </Drawer>
    </Paper>
  )
}
