import React from 'react';
import { useTranslation } from 'react-i18next';

/**
 * Nút chuyển VI/EN dùng chung.
 *
 * Tách riêng vì nó phải có mặt ở cả header của khu người học, khu quản trị
 * lẫn các màn hình xác thực — người chưa đăng nhập cũng phải đổi được ngôn
 * ngữ, nếu không trang đăng nhập sẽ mãi là tiếng Việt.
 *
 * Dùng `aria-pressed` thay vì chỉ đổi màu: đây là cặp nút bật/tắt, trình
 * đọc màn hình cần biết bên nào đang chọn.
 */

interface LanguageSwitchProps {
  className?: string;
  /** Cỡ nhỏ dành cho header dày đặc; cỡ mặc định đạt vùng chạm 44px. */
  size?: 'sm' | 'md';
}

const LanguageSwitch: React.FC<LanguageSwitchProps> = ({
  className = '',
  size = 'sm',
}) => {
  const { t, i18n } = useTranslation();
  const isEnglish = i18n.language === 'en';

  const button = (active: boolean) =>
    [
      'rounded-pill font-bold transition-colors duration-200',
      size === 'sm'
        ? 'min-h-[30px] px-3 text-[12px]'
        : 'min-h-[38px] px-4 text-[13px]',
      active
        ? 'bg-[#15803D] text-white shadow-xs'
        : 'text-ink-muted hover:text-ink',
    ].join(' ');

  return (
    <div
      className={`inline-flex items-center gap-0.5 rounded-pill bg-surface-muted p-1 ${className}`}
      role="group"
      aria-label={t('header.language')}
    >
      <button
        type="button"
        onClick={() => void i18n.changeLanguage('vi')}
        aria-pressed={!isEnglish}
        className={button(!isEnglish)}
      >
        VI
      </button>
      <button
        type="button"
        onClick={() => void i18n.changeLanguage('en')}
        aria-pressed={isEnglish}
        className={button(isEnglish)}
      >
        EN
      </button>
    </div>
  );
};

export default LanguageSwitch;
