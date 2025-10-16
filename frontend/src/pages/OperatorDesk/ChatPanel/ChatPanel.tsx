import React from 'react'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { dialogActions } from '@/entities/dialog/dialog.slice'
import { Box, Paper, TextField, Button, Stack, Typography, IconButton, Chip, Fade, Zoom, Tooltip } from '@mui/material'
import ClearIcon from '@mui/icons-material/Clear'
import PersonIcon from '@mui/icons-material/Person'
import SupportAgentIcon from '@mui/icons-material/SupportAgent'
import SendIcon from '@mui/icons-material/Send'
import HistoryIcon from '@mui/icons-material/History'

export const ChatPanel: React.FC = () => {
  const dispatch = useAppDispatch()
  const messages = useAppSelector(s => s.dialog.messages)
  const origin = useAppSelector(s => s.editor.origin)

  const [draft, setDraft] = React.useState('')
  const [clientDraft, setClientDraft] = React.useState('')
  const messagesEndRef = React.useRef<HTMLDivElement>(null)

  // Автоскролл к последнему сообщению
  React.useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

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
    <Paper 
      sx={{ 
        p: 3, 
        mb: 2,
        background: 'linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%)',
        transition: 'transform 0.2s ease, box-shadow 0.2s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 16px 40px rgba(0,0,0,0.5)'
        }
      }}
    >
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <HistoryIcon sx={{ color: 'primary.main' }} />
        <Typography variant="h6" sx={{ fontWeight: 700, flex: 1 }}>
          История переписки
        </Typography>
        <Chip 
          label={`${messages.length} сообщений`} 
          size="small" 
          color="primary" 
          variant="outlined"
          sx={{ fontWeight: 600 }}
        />
      </Stack>

      {/* История сообщений */}
      <Box sx={{ 
        height: 320, 
        overflow: 'auto', 
        mb: 2.5, 
        p: 2, 
        borderRadius: 3, 
        background: 'rgba(0,0,0,0.25)',
        border: '1px solid rgba(255,255,255,0.06)',
        '&::-webkit-scrollbar-thumb': {
          background: 'rgba(91,140,255,0.3)'
        }
      }}>
        {messages.map((m, i) => (
          <Zoom in key={i} style={{ transitionDelay: `${i * 50}ms` }}>
            <Box sx={{ 
              mb: 1.5, 
              display: 'flex', 
              alignItems: 'flex-start', 
              gap: 1.5,
              animation: 'slideIn 0.3s ease',
              '@keyframes slideIn': {
                from: { opacity: 0, transform: 'translateX(-10px)' },
                to: { opacity: 1, transform: 'translateX(0)' }
              }
            }}>
              <Chip
                icon={m.author === 'client' ? <PersonIcon /> : <SupportAgentIcon />}
                label={m.author === 'client' ? 'Клиент' : 'Оператор'}
                size="small"
                color={m.author === 'client' ? 'primary' : 'success'}
                sx={{ 
                  minWidth: 110,
                  fontWeight: 600,
                  boxShadow: m.author === 'client' 
                    ? '0 4px 12px rgba(91,140,255,0.25)' 
                    : '0 4px 12px rgba(60,203,127,0.25)',
                  transition: 'transform 0.2s ease',
                  '&:hover': {
                    transform: 'scale(1.05)'
                  }
                }}
              />
              <Paper
                elevation={0}
                sx={{ 
                  flex: 1, 
                  p: 1.5,
                  pt: 1.2,
                  background: m.author === 'client' 
                    ? 'linear-gradient(135deg, rgba(91,140,255,0.12) 0%, rgba(91,140,255,0.06) 100%)'
                    : 'linear-gradient(135deg, rgba(60,203,127,0.12) 0%, rgba(60,203,127,0.06) 100%)',
                  border: `1px solid ${m.author === 'client' ? 'rgba(91,140,255,0.2)' : 'rgba(60,203,127,0.2)'}`,
                  borderRadius: 2.5,
                  transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                  '&:hover': {
                    transform: 'translateX(4px)',
                    boxShadow: m.author === 'client'
                      ? '0 8px 24px rgba(91,140,255,0.2)'
                      : '0 8px 24px rgba(60,203,127,0.2)'
                  }
                }}
              >
                <Typography variant="body2" sx={{ lineHeight: 1.6 }}>
                  {m.text}
                </Typography>
              </Paper>
            </Box>
          </Zoom>
        ))}
        <div ref={messagesEndRef} />
      </Box>

      {/* Симуляция клиента */}
      <Fade in>
        <Box sx={{ 
          mb: 2.5, 
          p: 2, 
          border: '2px dashed', 
          borderColor: 'primary.main', 
          borderRadius: 2.5, 
          background: 'linear-gradient(135deg, rgba(91,140,255,0.08) 0%, rgba(91,140,255,0.03) 100%)',
          transition: 'all 0.3s ease',
          '&:hover': {
            borderColor: 'primary.light',
            background: 'linear-gradient(135deg, rgba(91,140,255,0.12) 0%, rgba(91,140,255,0.05) 100%)',
            transform: 'scale(1.01)'
          }
        }}>
          <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 1.5 }}>
            <PersonIcon sx={{ fontSize: 18, color: 'primary.main' }} />
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.light' }}>
              Симуляция клиента (для теста)
            </Typography>
          </Stack>
          <Stack direction="row" spacing={1}>
            <TextField
              fullWidth
              size="small"
              value={clientDraft}
              onChange={e => setClientDraft(e.target.value)}
              placeholder="Сообщение от клиента..."
              onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendClient() } }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  background: 'rgba(255,255,255,0.04)',
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    background: 'rgba(255,255,255,0.06)'
                  }
                }
              }}
            />
            <Tooltip title="Отправить (Enter)">
              <span>
                <Button 
                  variant="contained" 
                  onClick={sendClient} 
                  disabled={!clientDraft.trim()}
                  endIcon={<SendIcon />}
                  sx={{
                    minWidth: 120,
                    transition: 'all 0.2s ease',
                    '&:not(:disabled):hover': {
                      transform: 'translateY(-2px) scale(1.02)'
                    }
                  }}
                >
                  Отправить
                </Button>
              </span>
            </Tooltip>
          </Stack>
        </Box>
      </Fade>

      {/* Ответ оператора */}
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 1 }}>
        <SupportAgentIcon sx={{ fontSize: 18, color: 'success.main' }} />
        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'success.light' }}>
          Оператор — вводит ответ…
        </Typography>
      </Stack>

      <Stack direction="row" spacing={1} alignItems="flex-start">
        <TextField
          fullWidth
          value={draft}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={onKeyDown}
          placeholder="Текст ответа или вставьте шаблон справа… (Ctrl+Enter для отправки)"
          multiline
          minRows={3}
          maxRows={8}
          sx={{
            '& .MuiOutlinedInput-root': {
              background: 'rgba(255,255,255,0.03)',
              transition: 'all 0.2s ease',
              '&:hover': {
                background: 'rgba(255,255,255,0.05)'
              },
              '&.Mui-focused': {
                background: 'rgba(255,255,255,0.06)'
              }
            }
          }}
        />
        <Stack direction="column" spacing={1}>
          <Tooltip title="Отправить (Ctrl+Enter)">
            <Button 
              variant="contained" 
              color="success"
              onClick={sendOperator}
              disabled={!draft.trim()}
              startIcon={<SendIcon />}
              sx={{
                minWidth: 120,
                height: 48,
                transition: 'all 0.2s ease',
                '&:not(:disabled):hover': {
                  transform: 'translateY(-2px) scale(1.02)',
                  boxShadow: '0 12px 32px rgba(60,203,127,0.45)'
                }
              }}
            >
              Отправить
            </Button>
          </Tooltip>
          <Tooltip title="Очистить">
            <IconButton 
              onClick={() => setDraft('')} 
              disabled={!draft}
              sx={{
                transition: 'all 0.2s ease',
                '&:not(:disabled):hover': {
                  transform: 'rotate(90deg) scale(1.1)',
                  color: 'error.main'
                }
              }}
            >
              <ClearIcon />
            </IconButton>
          </Tooltip>
        </Stack>
      </Stack>
    </Paper>
  )
}
