import React from 'react'
import { Paper, Tabs, Tab, Chip, Stack } from '@mui/material'
import InboxIcon from '@mui/icons-material/Inbox'
import WorkIcon from '@mui/icons-material/Work'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'

export const TicketTabs: React.FC = () => {
  const [tab, setTab] = React.useState(0)
  
  const tabs = [
    { label: 'Входящие', icon: <InboxIcon />, count: 12, color: 'primary' as const },
    { label: 'В работе', icon: <WorkIcon />, count: 5, color: 'warning' as const },
    { label: 'Закрытые', icon: <CheckCircleIcon />, count: 143, color: 'success' as const }
  ]
  
  return (
    <Paper 
      sx={{ 
        p: 1.5, 
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
      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        variant="fullWidth"
        sx={{
          '& .MuiTab-root': {
            minHeight: 56,
            fontWeight: 600,
            transition: 'all 0.2s ease',
            '&:hover': {
              background: 'rgba(91,140,255,0.1)',
              transform: 'translateY(-2px)'
            },
            '&.Mui-selected': {
              background: 'linear-gradient(135deg, rgba(91,140,255,0.15) 0%, rgba(155,123,255,0.15) 100%)',
            }
          },
          '& .MuiTabs-indicator': {
            height: 3,
            borderRadius: '3px 3px 0 0',
            background: 'linear-gradient(90deg, #5B8CFF 0%, #9B7BFF 100%)'
          }
        }}
      >
        {tabs.map((t, i) => (
          <Tab 
            key={i}
            icon={
              <Stack direction="row" spacing={1} alignItems="center">
                {t.icon}
                <Chip 
                  label={t.count} 
                  size="small" 
                  color={t.color}
                  sx={{ 
                    minWidth: 32,
                    height: 20,
                    fontWeight: 700,
                    fontSize: '0.7rem'
                  }}
                />
              </Stack>
            }
            iconPosition="start"
            label={t.label}
            sx={{
              color: 'rgba(255,255,255,0.7)',
              '&.Mui-selected': {
                color: '#fff'
              }
            }}
          />
        ))}
      </Tabs>
    </Paper>
  )
}