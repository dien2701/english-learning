import { theme, type ThemeConfig } from 'antd';
import { brand, palettes, radius, type ThemeMode } from './palette';

/**
 * Dựng cấu hình Ant Design theo chế độ sáng hoặc tối.
 *
 * Tailwind đổi màu qua biến CSS nên chỉ cần bật class .dark là xong.
 * Ant Design thì phải nhận giá trị cụ thể, nên hàm này được gọi lại mỗi
 * khi người dùng đổi chế độ — nếu quên, các component của AntD (Table,
 * Select, Modal…) sẽ kẹt ở màu chế độ sáng trên nền tối.
 *
 * Quy ước trong dự án: Tailwind dựng layout và các khối hiển thị, Ant
 * Design lo phần phức tạp (Table, Form, Modal, Select, DatePicker,
 * Upload, message).
 */
export function buildThemeConfig(mode: ThemeMode): ThemeConfig {
  const c = palettes[mode];
  const isDark = mode === 'dark';

  return {
    algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,

    token: {
      colorPrimary: c.action,
      colorSuccess: c.success,
      colorWarning: c.warning,
      colorError: c.danger,
      colorInfo: c.info,

      /* AntD mặc định suy colorLink từ colorInfo. colorInfo của dự án là
         xám xanh (bảng màu đã bỏ hẳn xanh lam), nên phải chỉ định rõ màu
         link, nếu không mọi thẻ a sẽ thành xám. */
      colorLink: c.accent,
      colorLinkHover: c.accentHover,
      colorLinkActive: c.accentHover,

      colorBgLayout: c.page,
      colorBgContainer: c.surface,
      colorBgElevated: c.surface,

      colorText: c.ink,
      colorTextSecondary: c.inkMuted,
      colorTextTertiary: c.inkSubtle,
      colorTextPlaceholder: c.inkSubtle,

      colorBorder: c.border,
      colorBorderSecondary: c.border,

      fontFamily: "'Plus Jakarta Sans', Inter, 'Segoe UI', system-ui, sans-serif",
      fontSize: 14,

      borderRadius: radius.md,
      borderRadiusLG: radius.lg,
      borderRadiusSM: radius.sm,

      /* Ô nhập và nút cao hơn mặc định để khung tìm kiếm và bộ lọc
         nhìn rõ ràng, dễ bấm. */
      controlHeight: 44,
      controlHeightLG: 52,
      controlHeightSM: 34,

      wireframe: false,
    },

    components: {
      Button: {
        borderRadius: radius.pill,
        borderRadiusLG: radius.pill,
        borderRadiusSM: radius.pill,
        fontWeight: 600,
        primaryShadow: isDark
          ? '0 6px 16px rgba(21, 128, 61, 0.35)'
          : '0 6px 16px rgba(21, 128, 61, 0.24)',
        defaultShadow: 'none',
      },

      Card: {
        borderRadiusLG: radius.lg,
        paddingLG: 24,
        headerFontSize: 18,
      },

      /* Ô nhập: nền mint nhạt tách khỏi card trắng, viền đậm hơn
         hairline, focus đổi nền trắng kèm viền xanh và quầng sáng.
         Phần viền 2px lúc focus nằm ở @layer components trong
         index.css vì AntD không có token cho độ dày viền. */
      Input: {
        borderRadius: radius.md,
        paddingBlock: 10,
        /* Khung tìm kiếm là điểm vào chính của mọi trang danh sách,
           nên chữ để 16px cho dễ đọc. */
        fontSizeLG: 16,
        colorBgContainer: c.fieldBg,
        colorBorder: c.fieldBorder,
        hoverBg: c.fieldBgHover,
        hoverBorderColor: c.fieldBorderHover,
        activeBg: c.fieldBgFocus,
        activeBorderColor: c.fieldFocus,
        activeShadow: c.fieldFocusShadow,
      },

      Select: {
        borderRadius: radius.md,
        fontSizeLG: 15,
        colorBgContainer: c.fieldBg,
        colorBorder: c.fieldBorder,
        hoverBorderColor: c.fieldBorderHover,
        activeBorderColor: c.fieldFocus,
        activeOutlineColor: 'transparent',
        optionSelectedBg: isDark ? '#1b3a28' : brand[100],
        optionSelectedColor: c.accent,
      },

      DatePicker: {
        borderRadius: radius.md,
        colorBgContainer: c.fieldBg,
        colorBorder: c.fieldBorder,
        hoverBorderColor: c.fieldBorderHover,
        activeBorderColor: c.fieldFocus,
        activeShadow: c.fieldFocusShadow,
      },

      InputNumber: {
        borderRadius: radius.md,
        colorBgContainer: c.fieldBg,
        colorBorder: c.fieldBorder,
        hoverBorderColor: c.fieldBorderHover,
        activeBorderColor: c.fieldFocus,
        activeShadow: c.fieldFocusShadow,
      },

      Modal: { borderRadiusLG: radius.lg, titleFontSize: 18 },

      Table: {
        borderRadius: radius.lg,
        headerBg: c.surfaceMuted,
        headerColor: c.inkMuted,
        headerSplitColor: 'transparent',
        rowHoverBg: c.surfaceMuted,
        cellPaddingBlock: 14,
      },

      Tag: {
        borderRadiusSM: radius.pill,
        defaultBg: isDark ? '#1b3a28' : brand[100],
        defaultColor: c.accent,
      },

      Progress: {
        defaultColor: brand[500],
        remainingColor: c.surfaceMuted,
      },

      Tabs: {
        itemSelectedColor: c.accent,
        inkBarColor: c.accent,
        horizontalItemPadding: '12px 0',
      },

      Segmented: {
        itemSelectedBg: '#15803D',
        itemSelectedColor: '#ffffff',
      },

      Switch: {
        colorPrimary: '#15803D',
        colorPrimaryHover: '#15803D',
      },

      Radio: {
        buttonSolidCheckedBg: '#15803D',
        buttonSolidCheckedColor: '#ffffff',
        buttonCheckedBg: '#15803D',
        colorPrimary: '#15803D',
      },

      Menu: {
        itemBorderRadius: radius.md,
        itemSelectedBg: isDark ? '#1b3a28' : brand[100],
        itemSelectedColor: c.accent,
        itemHeight: 44,
      },

      Message: { borderRadiusLG: radius.md },
      Tooltip: { borderRadius: radius.sm },
    },
  };
}

export default buildThemeConfig;
