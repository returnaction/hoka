import React from 'react'
import { Grid, Box } from '@mui/material'
import { ChatPanel } from './ChatPanel/ChatPanel'
import { SuggestionsPanel } from './SuggestionsPanel/SuggestionsPanel'
import { TicketTabs } from '@/widgets/TicketTabs'
import { TicketHeader } from '@/widgets/TicketHeader'

export const OperatorDesk: React.FC = () => {
  return (
    <Box sx={{ width: '100%' }}>
      <TicketTabs />
      <TicketHeader />
      <Grid container spacing={2.5} sx={{ mt: 0 }}>
        <Grid size={{ xs: 12, md: 8, lg: 8 }}>
          <ChatPanel />
          {/* <EditorPanel /> */}
        </Grid>
        <Grid size={{ xs: 12, md: 4, lg: 4 }}>
          <SuggestionsPanel />
        </Grid>
      </Grid>
    </Box>
  )
}