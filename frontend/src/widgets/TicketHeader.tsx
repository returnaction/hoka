import React from 'react'
import { Paper, Typography } from '@mui/material'

type Props = {
  id?: string; client?: string; title?: string;
  channel?: string; priority?: string; status?: string;
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
    <Paper sx={{ p: 2, mb: 2 }}>
      <Typography variant="h6" sx={{ mb: .5, fontWeight: 700 }}>
        Тикет {id} · {client} · «{title}»
      </Typography>
      <Typography variant="body2" sx={{ opacity: .85 }}>
        Канал: {channel} · Приоритет: {priority} · Статус: {status}
      </Typography>
    </Paper>
  )
}