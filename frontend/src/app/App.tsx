import React from 'react'
import { ThemeProvider, CssBaseline, Container, Box, Tabs, Tab } from '@mui/material'
import { theme } from '@/shared/theme/theme'
import { HeaderBar } from '@/widgets/HeaderBar'
import { StatsStrip } from '@/widgets/StatsStrip'
import { OperatorDesk } from '@/pages/OperatorDesk/OperatorDesk'
import { SettingsPage } from '@/pages/SettingsPage'
import { ApiTestPage } from '@/pages/ApiTestPage'

export const App: React.FC = () => {
  const [tab, setTab] = React.useState(0)

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
          <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
            <Tab label="Operator Desk" />
            <Tab label="API Test" />
            <Tab label="Settings" />
          </Tabs>

          {tab === 0 && (
            <>
              <Box mt={2}>
                <StatsStrip />
              </Box>
              <Box mt={2}>
                <OperatorDesk />
              </Box>
            </>
          )}
          
          {tab === 1 && <ApiTestPage />}
          
          {tab === 2 && <SettingsPage />}
        </Container>
      </Box>
    </ThemeProvider>
  )
}
