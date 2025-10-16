export const env = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL as string || '',
  API_KEY: import.meta.env.VITE_API_KEY as string || '',
  
  // API Paths
  MODELS_PATH: import.meta.env.VITE_API_MODELS_PATH as string || '/api/v1/models',
  CHAT_COMPLETIONS_PATH: import.meta.env.VITE_API_CHAT_COMPLETIONS_PATH as string || '/api/v1/chat/completions',
  HEALTH_PATH: import.meta.env.VITE_API_HEALTH_PATH as string || '/api/v1/health',
  CLASSIFY_PATH: import.meta.env.VITE_API_CLASSIFY_PATH as string || '/api/v1/classify',
  KB_SEARCH_PATH: import.meta.env.VITE_API_KB_SEARCH_PATH as string || '/api/v1/classify/hybrid',
  
  // Settings
  ACCEPT_STREAM: import.meta.env.VITE_ACCEPT_STREAM as string || 'text/event-stream',
  DEFAULT_MODEL: import.meta.env.VITE_DEFAULT_MODEL as string || 'bge-m3',
  APP_TITLE: import.meta.env.VITE_APP_TITLE as string || 'Smart Support System'
}