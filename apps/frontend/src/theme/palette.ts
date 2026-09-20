/**
 * Bảng màu dùng cho phía JavaScript (Ant Design, Recharts).
 * Giá trị phải khớp với src/styles/tokens.css và tailwind.config.js.
 *
 * Tailwind đọc màu qua biến CSS nên tự đổi theo chế độ sáng/tối.
 * Ant Design thì không — nó cần nhận giá trị cụ thể lúc dựng theme,
 * nên ở đây phải khai báo riêng hai bộ.
 */

export type ThemeMode = 'light' | 'dark';

/** Thang màu thương hiệu, cố định ở cả hai chế độ. */
export const brand = {
  50: '#f0fdf4',
  100: '#dcfce7',
  200: '#bbf7d0',
  300: '#86efac',
  400: '#4ade80',
  500: '#22c55e',
  600: '#15803d',
  700: '#166534',
  800: '#14532d',
  900: '#173f2e',
  950: '#0f2e20',
} as const;

interface ModePalette {
  /** Nền nút chính; luôn đi với chữ trắng ở cả hai chế độ. */
  action: string;
  actionHover: string;
  /** Chữ link và nhãn nhấn. */
  accent: string;
  accentHover: string;

  surface: string;
  surfaceMuted: string;
  page: string;

  ink: string;
  inkMuted: string;
  inkSubtle: string;

  border: string;

  /** Ô nhập: nền, viền và màu viền lúc focus. Khớp với nhóm --field-*
      trong tokens.css. */
  fieldBg: string;
  fieldBgHover: string;
  fieldBgFocus: string;
  fieldBorder: string;
  fieldBorderHover: string;
  fieldFocus: string;
  fieldFocusShadow: string;

  success: string;
  warning: string;
  danger: string;
  /** Không dùng xanh lam — thông tin dùng xám xanh. */
  info: string;

  /** Thứ tự màu cho chuỗi dữ liệu biểu đồ, cố định, không xoay vòng. */
  chart: readonly string[];
  chartGrid: string;
}

export const palettes: Record<ThemeMode, ModePalette> = {
  light: {
    action: '#15803d',
    actionHover: '#166534',
    accent: '#166534',
    accentHover: '#14532d',

    surface: '#ffffff',
    surfaceMuted: '#f1f6f3',
    page: '#f4faf6',

    ink: '#14261c',
    inkMuted: '#5a6b62',
    inkSubtle: '#8a9a91',

    border: '#dde8e1',

    fieldBg: '#f1f7f3',
    fieldBgHover: '#eaf3ed',
    fieldBgFocus: '#ffffff',
    fieldBorder: '#c4d6cb',
    fieldBorderHover: '#9abaa6',
    fieldFocus: '#15803d',
    fieldFocusShadow: '0 0 0 4px rgb(21 128 61 / 0.12)',

    success: '#15803d',
    warning: '#b45309',
    danger: '#dc2626',
    info: '#3d4f45',

    chart: ['#15803d', '#8b5cf6', '#b45309', '#be185d', '#4f6459'],
    chartGrid: '#e8f0eb',
  },

  dark: {
    action: '#15803d',
    actionHover: '#1a9e4b',
    accent: '#86efac',
    accentHover: '#bbf7d0',

    surface: '#14251b',
    surfaceMuted: '#1a2f23',
    page: '#0d1a13',

    ink: '#e6f0ea',
    inkMuted: '#b0c5b9',
    inkSubtle: '#8da396',

    border: '#23402f',

    fieldBg: '#1a2f23',
    fieldBgHover: '#1f3629',
    fieldBgFocus: '#14251b',
    fieldBorder: '#2f5340',
    fieldBorderHover: '#457057',
    fieldFocus: '#4ade80',
    fieldFocusShadow: '0 0 0 4px rgb(74 222 128 / 0.18)',

    success: '#86efac',
    warning: '#fcd34d',
    danger: '#f87171',
    info: '#b0c5b9',

    chart: ['#16a34a', '#8b5cf6', '#b45309', '#be185d', '#4f6459'],
    chartGrid: '#23402f',
  },
};

export const radius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  pill: 999,
} as const;
