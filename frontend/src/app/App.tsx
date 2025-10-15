import React from 'react'
import { ThemeProvider, CssBaseline, Container, Box } from '@mui/material'
import { theme } from '@/shared/theme/theme'
import { HeaderBar } from '@/widgets/HeaderBar'
import { StatsStrip } from '@/widgets/StatsStrip'
import { OperatorDesk } from '@/pages/OperatorDesk/OperatorDesk'

export const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <HeaderBar />
      <Box
        sx={{
          minHeight: '100vh',
          pt: 2,
          pb: 6
        }}
      >
        <Container maxWidth="xl">
          <Box mt={2}>
            <StatsStrip />
          </Box>
          <Box mt={2}>
            <OperatorDesk />
          </Box>
        </Container>
      </Box>
    </ThemeProvider>
  )
}
