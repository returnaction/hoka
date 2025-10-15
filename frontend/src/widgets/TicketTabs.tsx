import React from 'react'
import { Paper, Tabs, Tab } from '@mui/material'

export const TicketTabs: React.FC = () => {
  const [tab, setTab] = React.useState(0)
  return (
    <Paper sx={{ p: .5, mb: 2 }}>
      <Tabs
        value={tab}
        onChange={(_, v)=>setTab(v)}
        variant="scrollable"
        scrollButtons="auto"
      >
        <Tab label="Входящие" />
        <Tab label="В работе" />
        <Tab label="Закрытые" />
      </Tabs>
    </Paper>
  )
}