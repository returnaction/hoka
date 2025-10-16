import React from 'react'
import { Paper, Typography, Stack, Chip, Box, Divider } from '@mui/material'
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber'
import PersonIcon from '@mui/icons-material/Person'
import EmailIcon from '@mui/icons-material/Email'
import PriorityHighIcon from '@mui/icons-material/PriorityHigh'
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord'

type Props = {
  id?: string; client?: string; title?: string;
  channel?: string; priority?: string; status?: string;
}

const getStatusColor = (status: string): 'success' | 'warning' | 'default' => {
  const colors: Record<string, 'success' | 'warning' | 'default'> = {
    'открыт': 'success',
    'в работе': 'warning',
    'закрыт': 'default'
  }
  return colors[status] || 'default'
}

const getPriorityColor = (priority: string): 'error' | 'warning' | 'info' => {
  const colors: Record<string, 'error' | 'warning' | 'info'> = {
    'высокий': 'error',
    'средний': 'warning',
    'низкий': 'info'
  }
  return colors[priority] || 'info'
}

export const TicketHeader: React.FC<Props> = ({
  id = '#32451',
  client = 'Иван Петров',
  title = 'Не могу войти в аккаунт',
  channel = 'email',
  priority = 'средний',
  status = 'открыт'
}) => {
  return (
    <Paper 
      sx={{ 
        p: 2.5, 
        mb: 2,
        background: 'linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%)',
        border: '1px solid rgba(255,255,255,0.1)',
        transition: 'all 0.3s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.3)'
        }
      }}
    >
      <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
        <Box
          sx={{
            background: 'linear-gradient(135deg, #9B7BFF 0%, #7B5FE0 100%)',
            borderRadius: '50%',
            p: 1.5,
            display: 'flex',
            boxShadow: '0 4px 16px rgba(155,123,255,0.4)'
          }}
        >
          <ConfirmationNumberIcon sx={{ color: '#fff', fontSize: 28 }} />
        </Box>
        <Box sx={{ flex: 1 }}>
          <Typography 
            variant="h5" 
            sx={{ 
              fontWeight: 800,
              mb: 0.5,
              background: 'linear-gradient(135deg, #5B8CFF 0%, #9B7BFF 100%)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}
          >
            Тикет {id} · «{title}»
          </Typography>
          <Stack direction="row" spacing={1} alignItems="center">
            <PersonIcon sx={{ fontSize: 18, opacity: 0.7, color: 'rgba(255,255,255,0.9)' }} />
            <Typography variant="body2" sx={{ fontWeight: 600, color: 'rgba(255,255,255,0.9)' }}>
              {client}
            </Typography>
          </Stack>
        </Box>
        <Chip 
          icon={<FiberManualRecordIcon />}
          label={status.toUpperCase()} 
          color={getStatusColor(status)}
          sx={{ 
            fontWeight: 700,
            height: 32,
            fontSize: '0.85rem'
          }}
        />
      </Stack>

      <Divider sx={{ mb: 2, opacity: 0.2 }} />

            <Stack direction="row" spacing={2} flexWrap="wrap">
        <Chip 
          icon={<EmailIcon sx={{ fontSize: 16 }} />}
          label={`Канал: ${channel}`}
          variant="outlined"
          size="small"
          sx={{ 
            fontWeight: 600,
            color: 'rgba(255,255,255,0.9)',
            borderColor: 'rgba(91,140,255,0.3)',
            '&:hover': {
              background: 'rgba(91,140,255,0.1)',
              borderColor: 'rgba(91,140,255,0.5)'
            }
          }}
        />
        <Chip 
          icon={<PriorityHighIcon sx={{ fontSize: 16 }} />}
          label={`Приоритет: ${priority}`}
          color={getPriorityColor(priority)}
          size="small"
          variant="outlined"
          sx={{ 
            fontWeight: 600,
            color: 'rgba(255,255,255,0.9)'
          }}
        />
      </Stack>
    </Paper>
  )
}