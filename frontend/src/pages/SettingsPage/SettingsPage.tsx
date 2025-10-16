import React from 'react'
import { Box, Button, Container, Paper, Stack, TextField, Typography, Alert, Snackbar, Fade, Divider, Chip, CircularProgress } from '@mui/material'
import SettingsIcon from '@mui/icons-material/Settings'
import SaveIcon from '@mui/icons-material/Save'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { updateConfig } from '@/shared/config/config.slice'

export const SettingsPage: React.FC = () => {
  const dispatch = useAppDispatch()
  const config = useAppSelector(s => s.config)
  const [saved, setSaved] = React.useState(false)
  const [uploading, setUploading] = React.useState(false)
  const [uploadResult, setUploadResult] = React.useState<{ success: boolean; message: string } | null>(null)
  const fileInputRef = React.useRef<HTMLInputElement>(null)

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

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // Валидация размера файла (10 МБ)
    const maxSize = 10 * 1024 * 1024 // 10 MB
    if (file.size > maxSize) {
      setUploadResult({
        success: false,
        message: `Файл слишком большой (${(file.size / 1024 / 1024).toFixed(2)} МБ). Максимум 10 МБ.`
      })
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
      return
    }

    setUploading(true)
    setUploadResult(null)

    const formData = new FormData()
    formData.append('file', file)

    try {
      const response = await fetch(`${config.apiBaseUrl}/api/v1/faq/import`, {
        method: 'POST',
        body: formData
      })

      // Проверяем, что ответ не пустой
      const text = await response.text()
      let result = null
      
      try {
        result = text ? JSON.parse(text) : null
      } catch (parseError) {
        console.error('JSON parse error:', parseError, 'Response text:', text)
        throw new Error('Сервер вернул некорректный ответ')
      }
      
      if (response.ok) {
        setUploadResult({ 
          success: true, 
          message: result?.inserted 
            ? `Успешно загружено ${result.inserted} записей из файла "${file.name}"` 
            : 'Файл успешно загружен'
        })
      } else {
        setUploadResult({ 
          success: false, 
          message: result?.error || result?.message || `Ошибка: ${response.status} ${response.statusText}` 
        })
      }
    } catch (error) {
      console.error('Upload error:', error)
      setUploadResult({ 
        success: false, 
        message: `Ошибка: ${error instanceof Error ? error.message : 'Неизвестная ошибка'}` 
      })
    } finally {
      setUploading(false)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Fade in>
        <Paper 
          sx={{ 
            p: 4,
            background: 'linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%)',
            transition: 'all 0.3s ease',
            '&:hover': {
              transform: 'translateY(-2px)',
              boxShadow: '0 8px 24px rgba(0,0,0,0.3)'
            }
          }}
        >
          <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 3 }}>
            <Box
              sx={{
                background: 'linear-gradient(135deg, #5B8CFF 0%, #9B7BFF 100%)',
                borderRadius: '50%',
                p: 1.5,
                display: 'flex',
                boxShadow: '0 4px 16px rgba(91,140,255,0.4)',
                transition: 'all 0.3s ease',
                '&:hover': {
                  transform: 'rotate(180deg)',
                  boxShadow: '0 8px 24px rgba(91,140,255,0.6)'
                }
              }}
            >
              <SettingsIcon sx={{ fontSize: 28, color: '#fff' }} />
            </Box>
            <Typography 
              variant="h5" 
              sx={{ 
                fontWeight: 800,
                flex: 1,
                background: 'linear-gradient(135deg, #5B8CFF 0%, #9B7BFF 100%)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent'
              }}
            >
              Настройки API
            </Typography>
            <Chip 
              label="Конфигурация" 
              size="small" 
              color="primary"
              variant="outlined"
              sx={{ fontWeight: 600 }}
            />
          </Stack>

          <Divider sx={{ mb: 3, opacity: 0.2 }} />

          <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={2.5}>
              <TextField 
                name="apiBaseUrl" 
                label="API Base URL" 
                defaultValue={config.apiBaseUrl}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    },
                    '&.Mui-focused': {
                      background: 'rgba(91,140,255,0.08)',
                      '& fieldset': {
                        borderWidth: 2
                      }
                    }
                  }
                }}
              />
              <TextField 
                name="apiKey" 
                label="API Key" 
                type="password"
                defaultValue={config.apiKey}
                fullWidth
                helperText="Bearer token for API authentication"
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    },
                    '&.Mui-focused': {
                      background: 'rgba(91,140,255,0.08)'
                    }
                  }
                }}
              />

              <Divider textAlign="left" sx={{ my: 1, opacity: 0.3 }}>
                <Chip label="Endpoints" size="small" sx={{ fontWeight: 600 }} />
              </Divider>

              <TextField 
                name="modelsPath" 
                label="Models Path" 
                defaultValue={config.modelsPath}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />
              <TextField 
                name="chatCompletionsPath" 
                label="Chat Completions Path" 
                defaultValue={config.chatCompletionsPath}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />
              <TextField 
                name="healthPath" 
                label="Health Path" 
                defaultValue={config.healthPath}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />
              <TextField 
                name="classifyPath" 
                label="Classify Path" 
                defaultValue={config.classifyPath}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />
              <TextField 
                name="kbSearchPath" 
                label="KB Search Path" 
                defaultValue={config.kbSearchPath}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />

              <Divider textAlign="left" sx={{ my: 1, opacity: 0.3 }}>
                <Chip label="Настройки" size="small" sx={{ fontWeight: 600 }} />
              </Divider>

              <TextField 
                name="acceptStream" 
                label="Accept Stream" 
                defaultValue={config.acceptStream}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />
              <TextField 
                name="defaultModel" 
                label="Default Model" 
                defaultValue={config.defaultModel}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />
              <TextField 
                name="appTitle" 
                label="App Title" 
                defaultValue={config.appTitle}
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.04)'
                    }
                  }
                }}
              />

              <Divider sx={{ my: 1, opacity: 0.2 }} />

              <Stack direction="row" spacing={2} sx={{ pt: 1 }} flexWrap="wrap" useFlexGap>
                <Button 
                  type="submit" 
                  variant="contained"
                  size="large"
                  startIcon={<SaveIcon />}
                  sx={{
                    px: 4,
                    py: 1.5,
                    fontWeight: 700,
                    background: 'linear-gradient(135deg, #5B8CFF 0%, #9B7BFF 100%)',
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      transform: 'translateY(-2px) scale(1.02)',
                      boxShadow: '0 12px 32px rgba(91,140,255,0.5)',
                      background: 'linear-gradient(135deg, #6B9CFF 0%, #AB8BFF 100%)'
                    }
                  }}
                >
                  Сохранить изменения
                </Button>

                <Stack spacing={1}>
                  <Box sx={{ position: 'relative' }}>
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept=".xlsx,.xls"
                      onChange={handleFileUpload}
                      style={{ display: 'none' }}
                      id="faq-upload-input"
                    />
                    <label htmlFor="faq-upload-input">
                      <Button 
                        component="span"
                        variant="outlined"
                        size="large"
                        disabled={uploading}
                        startIcon={uploading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                        sx={{
                          px: 4,
                          py: 1.5,
                          fontWeight: 700,
                          borderWidth: 2,
                          borderColor: 'primary.main',
                          color: 'primary.main',
                          transition: 'all 0.2s ease',
                          '&:hover': {
                            borderWidth: 2,
                            transform: 'translateY(-2px) scale(1.02)',
                            boxShadow: '0 12px 32px rgba(91,140,255,0.3)',
                            background: 'rgba(91,140,255,0.1)'
                          },
                          '&:disabled': {
                            borderWidth: 2,
                            borderColor: 'rgba(91,140,255,0.3)'
                          }
                        }}
                      >
                        {uploading ? 'Загрузка...' : 'Загрузить БД (Excel)'}
                      </Button>
                    </label>
                  </Box>
                  <Typography 
                    variant="caption" 
                    sx={{ 
                      color: 'rgba(255,255,255,0.6)',
                      fontSize: '0.75rem',
                      ml: 1
                    }}
                  >
                    Поддерживаются файлы .xlsx и .xls (до 10 МБ)
                  </Typography>
                </Stack>
              </Stack>
            </Stack>
          </Box>
        </Paper>
      </Fade>
      
      <Snackbar 
        open={saved} 
        autoHideDuration={3000} 
        onClose={() => setSaved(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          severity="success" 
          onClose={() => setSaved(false)}
          icon={<CheckCircleIcon />}
          sx={{
            borderRadius: 2.5,
            background: 'linear-gradient(135deg, rgba(60,203,127,0.95) 0%, rgba(34,177,110,0.95) 100%)',
            backdropFilter: 'blur(8px)',
            fontWeight: 600,
            boxShadow: '0 8px 32px rgba(60,203,127,0.4)'
          }}
        >
          Настройки успешно сохранены!
        </Alert>
      </Snackbar>

      <Snackbar 
        open={uploadResult !== null} 
        autoHideDuration={5000} 
        onClose={() => setUploadResult(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert 
          severity={uploadResult?.success ? 'success' : 'error'} 
          onClose={() => setUploadResult(null)}
          icon={<CheckCircleIcon />}
          sx={{
            borderRadius: 2.5,
            background: uploadResult?.success 
              ? 'linear-gradient(135deg, rgba(60,203,127,0.95) 0%, rgba(34,177,110,0.95) 100%)'
              : 'linear-gradient(135deg, rgba(244,67,54,0.95) 0%, rgba(211,47,47,0.95) 100%)',
            backdropFilter: 'blur(8px)',
            fontWeight: 600,
            boxShadow: uploadResult?.success 
              ? '0 8px 32px rgba(60,203,127,0.4)'
              : '0 8px 32px rgba(244,67,54,0.4)'
          }}
        >
          {uploadResult?.message}
        </Alert>
      </Snackbar>
    </Container>
  )
}

