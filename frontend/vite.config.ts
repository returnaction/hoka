import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    host: '0.0.0.0', // Allow external connections in Docker
    port: 5173,
    watch: {
      usePolling: true, // Enable polling for Docker volumes
    },
    hmr: {
      clientPort: 5173, // Hot module replacement port
    },
  },
})
