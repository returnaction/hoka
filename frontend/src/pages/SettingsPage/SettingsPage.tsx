import React from 'react'
import { Box, Button, Container, Paper, Stack, TextField, Typography, Alert, Snackbar } from '@mui/material'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { updateConfig } from '@/shared/config/config.slice'

export const SettingsPage: React.FC = () => {
  const dispatch = useAppDispatch()
  const config = useAppSelector(s => s.config)
  const [saved, setSaved] = React.useState(false)

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)
    
    dispatch(updateConfig({
      apiBaseUrl: String(formData.get('apiBaseUrl')),
      apiKey: String(formData.get('apiKey')),
      modelsPath: String(formData.get('modelsPath')),
      chatCompletionsPath: String(formData.get('chatCompletionsPath')),
      healthPath: String(formData.get('healthPath')),
      classifyPath: String(formData.get('classifyPath')),
      kbSearchPath: String(formData.get('kbSearchPath')),
      acceptStream: String(formData.get('acceptStream')),
      defaultModel: String(formData.get('defaultModel')),
      appTitle: String(formData.get('appTitle'))
    }))
    setSaved(true)
  }

  return (
    <Container maxWidth="md" sx={{ py: 3 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>Настройки API</Typography>
        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <TextField 
              name="apiBaseUrl" 
              label="API Base URL" 
              defaultValue={config.apiBaseUrl}
              fullWidth
            />
            <TextField 
              name="apiKey" 
              label="API Key" 
              type="password"
              defaultValue={config.apiKey}
              fullWidth
              helperText="Bearer token for API authentication"
            />
            <TextField 
              name="modelsPath" 
              label="Models Path" 
              defaultValue={config.modelsPath}
              fullWidth
            />
            <TextField 
              name="chatCompletionsPath" 
              label="Chat Completions Path" 
              defaultValue={config.chatCompletionsPath}
              fullWidth
            />
            <TextField 
              name="healthPath" 
              label="Health Path" 
              defaultValue={config.healthPath}
              fullWidth
            />
            <TextField 
              name="classifyPath" 
              label="Classify Path" 
              defaultValue={config.classifyPath}
              fullWidth
            />
            <TextField 
              name="kbSearchPath" 
              label="KB Search Path" 
              defaultValue={config.kbSearchPath}
              fullWidth
            />
            <TextField 
              name="acceptStream" 
              label="Accept Stream" 
              defaultValue={config.acceptStream}
              fullWidth
            />
            <TextField 
              name="defaultModel" 
              label="Default Model" 
              defaultValue={config.defaultModel}
              fullWidth
            />
            <TextField 
              name="appTitle" 
              label="App Title" 
              defaultValue={config.appTitle}
              fullWidth
            />
            <Stack direction="row" spacing={2}>
              <Button type="submit" variant="contained">Сохранить</Button>
            </Stack>
          </Stack>
        </Box>
      </Paper>
      
      <Snackbar 
        open={saved} 
        autoHideDuration={3000} 
        onClose={() => setSaved(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="success" onClose={() => setSaved(false)}>
          Настройки сохранены!
        </Alert>
      </Snackbar>
    </Container>
  )
}
