import React from 'react'
import { AppBar, Toolbar, Typography, Box } from '@mui/material'

export const HeaderBar: React.FC = () => {
  return (
    <AppBar position="sticky" elevation={4}>
      <Toolbar sx={{ minHeight: 64 }}>
        <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: 0.2 }}>
          Operator Desk
        </Typography>
        <Box sx={{ flex: 1 }} />
        <Typography variant="body2" sx={{ opacity: 0.8 }}>
          v0.1
        </Typography>
      </Toolbar>
    </AppBar>
  )
}
