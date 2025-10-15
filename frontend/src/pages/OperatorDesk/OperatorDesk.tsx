import React from 'react'
import { Grid } from '@mui/material'
import { ChatPanel } from './ChatPanel/ChatPanel'
import { SuggestionsPanel } from './SuggestionsPanel/SuggestionsPanel'
import { TicketTabs } from '@/widgets/TicketTabs'
import { TicketHeader } from '@/widgets/TicketHeader'

export const OperatorDesk: React.FC = () => {
  return (
    <>
      <TicketTabs />
      <TicketHeader />
      <Grid container spacing={2}>
        <Grid size={{ xs: 8, md: 8, lg: 8 }}>
          <ChatPanel />
          {/* <EditorPanel /> */}
        </Grid>
        <Grid size={{ xs: 4, md: 4, lg: 4 }}>
          <SuggestionsPanel />
        </Grid>
      </Grid>
    </>
  )
}