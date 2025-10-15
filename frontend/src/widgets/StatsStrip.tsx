import React from 'react'
import { Paper, Stack, Typography } from '@mui/material'

export const StatsStrip: React.FC = () => {
  return (
    <Paper sx={{ p: 2 }}>
      <Stack direction="row" spacing={4}>
        <Typography variant="body1" sx={{ opacity: 0.9 }}>
          Открытых решений: <b>2450</b>
        </Typography>
        <Typography variant="body1" sx={{ opacity: 0.9 }}>
          Среднее время: <b>13 мин</b>
        </Typography>
      </Stack>
    </Paper>
  )
}
