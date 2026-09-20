/** @type {import('tailwindcss').Config} */

/*
 * Mọi màu trỏ vào biến CSS khai báo trong src/styles/tokens.css.
 * Dạng `rgb(var(--x-rgb) / <alpha-value>)` giúp vừa đổi được theo chế độ
 * sáng/tối chỉ bằng cách đổi biến, vừa giữ các biến thể độ mờ như
 * `bg-surface/50` hay `bg-action/20`.
 *
 * Thêm màu mới thì phải khai báo biến ở tokens.css trước, cho cả :root
 * lẫn .dark — nếu không, chế độ tối sẽ lặng lẽ dùng giá trị của chế độ sáng.
 */
const withAlpha = (name) => `rgb(var(--${name}-rgb) / <alpha-value>)`;

export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        /* --- Thang thương hiệu: cố định ở cả hai chế độ ---------
         * Chỉ dùng cho mảng trang trí và nền. KHÔNG dùng cho chữ —
         * chữ phải dùng nhóm `accent` để đổi được theo chế độ.    */
        brand: {
          50: withAlpha('brand-50'),
          100: withAlpha('brand-100'),
          200: withAlpha('brand-200'),
          300: withAlpha('brand-300'),
          400: withAlpha('brand-400'),
          500: withAlpha('brand-500'),
          600: withAlpha('brand-600'),
          700: withAlpha('brand-700'),
          800: withAlpha('brand-800'),
          900: withAlpha('brand-900'),
          950: withAlpha('brand-950'),
        },

        /* --- Hành động: nền nút chính, luôn đi với chữ trắng --- */
        action: {
          DEFAULT: withAlpha('action'),
          hover: withAlpha('action-hover'),
          fg: withAlpha('on-action'),
        },

        /* --- Nhấn: chữ link, nhãn, nền chip -------------------- */
        accent: {
          DEFAULT: withAlpha('accent'),
          soft: withAlpha('accent-soft'),
          'soft-fg': withAlpha('accent-soft-fg'),
          subtle: withAlpha('accent-subtle'),
          line: withAlpha('accent-line'),
        },

        /* --- Bề mặt --- */
        surface: {
          DEFAULT: withAlpha('surface'),
          main: withAlpha('bg'),
          mint: withAlpha('bg-mint'),
          muted: withAlpha('surface-muted'),
          hover: withAlpha('surface-hover'),
        },

        /* --- Sidebar --- */
        sidebar: {
          DEFAULT: withAlpha('sidebar'),
          fg: withAlpha('sidebar-fg'),
          muted: withAlpha('sidebar-muted'),
          active: withAlpha('sidebar-active'),
        },

        /* --- Chữ --- */
        ink: {
          DEFAULT: withAlpha('ink'),
          muted: withAlpha('ink-muted'),
          subtle: withAlpha('ink-subtle'),
        },

        /* --- Ô nhập liệu --- */
        field: {
          DEFAULT: withAlpha('field-bg'),
          hover: withAlpha('field-bg-hover'),
          focus: withAlpha('field-bg-focus'),
          border: withAlpha('field-border'),
          'border-hover': withAlpha('field-border-hover'),
          ring: withAlpha('field-focus'),
        },

        /* --- Viền --- */
        hairline: {
          DEFAULT: withAlpha('hairline'),
          strong: withAlpha('hairline-strong'),
        },

        /* --- Trạng thái --- */
        success: {
          DEFAULT: withAlpha('success'),
          bg: withAlpha('success-bg'),
          fg: withAlpha('success-fg'),
        },
        warning: {
          DEFAULT: withAlpha('warning'),
          bg: withAlpha('warning-bg'),
          fg: withAlpha('warning-fg'),
        },
        danger: {
          DEFAULT: withAlpha('danger'),
          bg: withAlpha('danger-bg'),
          fg: withAlpha('danger-fg'),
        },
        /* Không dùng xanh lam ở bất cứ đâu: thông tin dùng xám xanh. */
        info: {
          DEFAULT: withAlpha('info'),
          bg: withAlpha('info-bg'),
          fg: withAlpha('info-fg'),
        },

        /* --- Trình độ --- */
        level: {
          'beginner-bg': withAlpha('level-beginner-bg'),
          'beginner-fg': withAlpha('level-beginner-fg'),
          'intermediate-bg': withAlpha('level-intermediate-bg'),
          'intermediate-fg': withAlpha('level-intermediate-fg'),
          'advanced-bg': withAlpha('level-advanced-bg'),
          'advanced-fg': withAlpha('level-advanced-fg'),
        },

        /* --- Biểu đồ --- */
        chart: {
          1: withAlpha('chart-1'),
          2: withAlpha('chart-2'),
          3: withAlpha('chart-3'),
          4: withAlpha('chart-4'),
          5: withAlpha('chart-5'),
          grid: withAlpha('chart-grid'),
        },
      },

      fontFamily: {
        sans: [
          'Plus Jakarta Sans',
          'Inter',
          'Segoe UI',
          'system-ui',
          'sans-serif',
        ],
      },

      fontSize: {
        display: ['40px', { lineHeight: '48px', letterSpacing: '-0.02em', fontWeight: '700' }],
        'page-title': ['28px', { lineHeight: '36px', letterSpacing: '-0.01em', fontWeight: '700' }],
        section: ['20px', { lineHeight: '28px', fontWeight: '600' }],
        'card-title': ['18px', { lineHeight: '26px', fontWeight: '600' }],
        body: ['14px', { lineHeight: '22px', fontWeight: '400' }],
        caption: ['12px', { lineHeight: '18px', fontWeight: '500' }],
      },

      borderRadius: {
        sm: '8px',
        md: '12px',
        lg: '16px',
        xl: '24px',
        pill: '999px',
      },

      boxShadow: {
        xs: 'var(--shadow-xs)',
        sm: 'var(--shadow-sm)',
        md: 'var(--shadow-md)',
        lg: 'var(--shadow-lg)',
        brand: 'var(--shadow-brand)',
      },

      spacing: {
        header: '64px',
        sidebar: '260px',
        'sidebar-sm': '80px',
      },

      maxWidth: {
        content: '1360px',
      },

      transitionTimingFunction: {
        out: 'cubic-bezier(0.16, 1, 0.3, 1)',
      },

      keyframes: {
        'fade-up': {
          from: { opacity: '0', transform: 'translateY(8px)' },
          to: { opacity: '1', transform: 'none' },
        },
      },

      animation: {
        'fade-up': 'fade-up 320ms cubic-bezier(0.16, 1, 0.3, 1) both',
      },
    },
  },
  plugins: [],
};
