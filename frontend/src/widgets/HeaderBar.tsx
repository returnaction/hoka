import React from 'react'
import { AppBar, Toolbar, Typography, Box, Chip, Stack } from '@mui/material'
import SupportAgentIcon from '@mui/icons-material/SupportAgent'
import BoltIcon from '@mui/icons-material/Bolt'

export const HeaderBar: React.FC = () => {
  return (
    <AppBar 
      position="sticky" 
      elevation={0}
      sx={{
        background: 'linear-gradient(135deg, rgba(91,140,255,0.15) 0%, rgba(155,123,255,0.15) 100%)',
        backdropFilter: 'blur(12px)',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
        transition: 'all 0.3s ease'
      }}
    >
      <Toolbar sx={{ minHeight: 72 }}>
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            sx={{
              background: 'linear-gradient(135deg, #5B8CFF 0%, #9B7BFF 100%)',
              borderRadius: '50%',
              p: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 16px rgba(91,140,255,0.5)',
              transition: 'all 0.3s ease',
              '&:hover': {
                transform: 'rotate(360deg) scale(1.1)',
                boxShadow: '0 8px 24px rgba(91,140,255,0.7)'
              }
            }}
          >
            <SupportAgentIcon sx={{ fontSize: 28, color: '#fff' }} />
          </Box>
          <Typography 
            variant="h5" 
            sx={{ 
              fontWeight: 800, 
              letterSpacing: 0.5,
              background: 'linear-gradient(135deg, #5B8CFF 0%, #9B7BFF 100%)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              transition: 'all 0.3s ease',
              '&:hover': {
                letterSpacing: 1
              }
            }}
          >
            Operator Desk
          </Typography>
        </Stack>

        <Box sx={{ flex: 1 }} />

        <Stack direction="row" spacing={1.5} alignItems="center">
          <Chip 
            icon={<BoltIcon sx={{ fontSize: 16 }} />}
            label="AI Powered" 
            size="small"
            sx={{
              fontWeight: 700,
              background: 'linear-gradient(135deg, rgba(60,203,127,0.2) 0%, rgba(60,203,127,0.1) 100%)',
              border: '1px solid rgba(60,203,127,0.3)',
              color: '#3CCB7F',
              transition: 'all 0.2s ease',
              '&:hover': {
                transform: 'scale(1.05)',
                boxShadow: '0 4px 16px rgba(60,203,127,0.3)'
              }
            }}
          />
          <Chip 
            label="v0.1" 
            size="small"
            variant="outlined"
            sx={{
              fontWeight: 600,
              borderColor: 'rgba(255,255,255,0.2)',
              transition: 'all 0.2s ease',
              '&:hover': {
                borderColor: 'rgba(255,255,255,0.4)',
                background: 'rgba(255,255,255,0.05)'
              }
            }}
          />
        </Stack>
      </Toolbar>
    </AppBar>
  )
}

