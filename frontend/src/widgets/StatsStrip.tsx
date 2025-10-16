import React from 'react'
import { Paper, Stack, Typography, Box, Chip } from '@mui/material'
import AssignmentIcon from '@mui/icons-material/Assignment'
import TimerIcon from '@mui/icons-material/Timer'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import PersonIcon from '@mui/icons-material/Person'

export const StatsStrip: React.FC = () => {
  const stats = [
    { 
      label: 'Открытых решений', 
      value: '2450', 
      icon: <AssignmentIcon />, 
      color: '#4A90E2',
      trend: '+12%'
    },
    { 
      label: 'Среднее время', 
      value: '13 мин', 
      icon: <TimerIcon />, 
      color: '#9B7BFF',
      trend: '-3%'
    },
    { 
      label: 'Обработано сегодня', 
      value: '87', 
      icon: <TrendingUpIcon />, 
      color: '#4CAF50',
      trend: '+24%'
    },
    { 
      label: 'Активных операторов', 
      value: '12', 
      icon: <PersonIcon />, 
      color: '#FF9800',
      trend: '100%'
    }
  ]

  return (
    <Paper 
      sx={{ 
        p: 2,
        background: 'linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%)',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        mb: 2,
        transition: 'all 0.3s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.3)'
        }
      }}
    >
      <Stack 
        direction="row" 
        spacing={3} 
        justifyContent="space-between"
        flexWrap="wrap"
        useFlexGap
      >
        {stats.map((stat, index) => (
          <Box 
            key={index}
            sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 1.5,
              transition: 'transform 0.2s ease',
              cursor: 'default',
              '&:hover': {
                transform: 'translateY(-2px)',
                '& .stat-icon': {
                  transform: 'scale(1.1) rotate(5deg)'
                }
              }
            }}
          >
            <Box 
              className="stat-icon"
              sx={{ 
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 40,
                height: 40,
                borderRadius: '50%',
                background: `linear-gradient(135deg, ${stat.color}20 0%, ${stat.color}40 100%)`,
                color: stat.color,
                transition: 'all 0.3s ease'
              }}
            >
              {stat.icon}
            </Box>
            <Box>
              <Typography 
                variant="caption" 
                sx={{ 
                  opacity: 0.7,
                  display: 'block',
                  fontSize: '0.75rem'
                }}
              >
                {stat.label}
              </Typography>
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography 
                  variant="h6" 
                  sx={{ 
                    fontWeight: 700,
                    background: `linear-gradient(135deg, ${stat.color} 0%, ${stat.color}CC 100%)`,
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent'
                  }}
                >
                  {stat.value}
                </Typography>
                <Chip 
                  label={stat.trend}
                  size="small"
                  sx={{
                    height: 18,
                    fontSize: '0.65rem',
                    fontWeight: 600,
                    background: stat.trend.startsWith('+') 
                      ? 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)' 
                      : 'linear-gradient(135deg, #FF9800 0%, #F57C00 100%)',
                    color: 'white',
                    border: 'none'
                  }}
                />
              </Stack>
            </Box>
          </Box>
        ))}
      </Stack>
    </Paper>
  )
}
