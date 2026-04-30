import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8888,
    proxy: {
      '/agent-mcp-server': 'http://localhost:8999'
    }
  }
})