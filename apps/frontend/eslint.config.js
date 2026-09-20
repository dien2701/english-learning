import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
    },
    rules: {
      // Cho phép bỏ bớt trường bằng cách destructure rồi không dùng,
      // ví dụ `const { cards: _cards, ...summary } = deck`. Đây là cách
      // gọn nhất để loại một thuộc tính khỏi object mà vẫn giữ kiểu.
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
          ignoreRestSiblings: true,
        },
      ],
    },
  },
  {
    // Các file này cố ý export thêm hằng số hoặc kiểu dùng chung cạnh component.
    files: ['src/components/ui/FilterBar.tsx'],
    rules: { 'react-refresh/only-export-components': 'off' },
  },
])
