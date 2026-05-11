import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    Components({
      resolvers: [NaiveUiResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/auth': { target: 'http://localhost:8090/agent-admin-server', changeOrigin: true },
      '/system': { target: 'http://localhost:8090/agent-admin-server', changeOrigin: true },
      '/agent': { target: 'http://localhost:8090/agent-admin-server', changeOrigin: true }
    }
  }
})
