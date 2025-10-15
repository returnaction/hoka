import React from 'react'
import { Box, Button, Paper, Stack, Typography, Alert } from '@mui/material'
import { useAppSelector } from '@/shared/hooks'

export const ApiTestPage: React.FC = () => {
  const config = useAppSelector(s => s.config)
  const [testResults, setTestResults] = React.useState<{
    endpoint: string
    status: string
    response?: string
  }[]>([])

  const testEndpoint = async (name: string, path: string) => {
    const url = config.apiBaseUrl + path
    try {
      const response = await fetch(url)
      const text = await response.text()
      setTestResults(prev => [...prev, {
        endpoint: name,
        status: response.ok ? 'Success' : `Error ${response.status}`,
        response: text.substring(0, 200)
      }])
    } catch (error) {
      setTestResults(prev => [...prev, {
        endpoint: name,
        status: 'Failed',
        response: String(error)
      }])
    }
  }

  const runTests = () => {
    setTestResults([])
    testEndpoint('Health', config.healthPath)
    testEndpoint('Models', config.modelsPath)
    testEndpoint('Classify', config.classifyPath)
    testEndpoint('KB Search', config.kbSearchPath)
  }

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3, maxWidth: 800, mx: 'auto' }}>
        <Typography variant="h5" sx={{ mb: 2 }}>API Connection Test</Typography>
        
        <Alert severity="info" sx={{ mb: 2 }}>
          <Typography variant="body2">Base URL: {config.apiBaseUrl}</Typography>
        </Alert>

        <Button variant="contained" onClick={runTests} sx={{ mb: 3 }}>
          Run Tests
        </Button>

        <Stack spacing={2}>
          {testResults.map((result, i) => (
            <Paper key={i} sx={{ p: 2, bgcolor: result.status.includes('Success') ? 'success.dark' : 'error.dark' }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                {result.endpoint}: {result.status}
              </Typography>
              {result.response && (
                <Typography variant="body2" sx={{ mt: 1, fontFamily: 'monospace', fontSize: '0.85rem' }}>
                  {result.response}
                </Typography>
              )}
            </Paper>
          ))}
        </Stack>
      </Paper>
    </Box>
  )
}
