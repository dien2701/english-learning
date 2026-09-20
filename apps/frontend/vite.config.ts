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

  optimizeDeps: {
    // Khai báo rõ để Vite gộp sẵn, tránh phải tối ưu lại giữa chừng.
    include: ['react', 'react-dom', 'antd', '@ant-design/cssinjs'],
  },
})
