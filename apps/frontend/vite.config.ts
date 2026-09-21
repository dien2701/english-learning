import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [tailwindcss(), react()],

  resolve: {
    /* Ép mọi gói dùng chung đúng một bản React. Thiếu dòng này, các gói
       phụ thuộc như @ant-design/cssinjs có thể kéo theo bản React thứ hai
       và gây lỗi "Invalid hook call". */
    dedupe: ['react', 'react-dom'],
  },

  server: {
    /* Chuyển /api sang backend (context-path /api, không cần rewrite). FE và BE
       cùng origin nên cookie refresh_token (SameSite=Lax) được gửi kèm. */
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },

  build: {
    /* antd tự nó đã hơn 1 MB; chunk vendor-antd là mức nền, không tách thêm. */
    chunkSizeWarningLimit: 1200,
    rolldownOptions: {
      output: {
        /* Tách thư viện lớn thành chunk riêng để tránh một file ~1,5 MB. */
        codeSplitting: {
          groups: [
            { name: 'vendor-antd', test: /node_modules[\\/](antd|@ant-design|rc-[^\\/]+|@rc-component)[\\/]/, priority: 30 },
            { name: 'vendor-charts', test: /node_modules[\\/](recharts|d3-[^\\/]+|victory-vendor|es-toolkit)[\\/]/, priority: 20 },
            { name: 'vendor', test: /node_modules[\\/]/, priority: 10 },
          ],
        },
      },
    },
  },

  optimizeDeps: {
    // Khai báo rõ để Vite gộp sẵn, tránh phải tối ưu lại giữa chừng.
    include: ['react', 'react-dom', 'antd', '@ant-design/cssinjs'],
  },
})
