import React from 'react';
import { useTranslation } from 'react-i18next';

/**
 * Màn hình chờ toàn trang, dùng khi ứng dụng đang khôi phục phiên đăng nhập.
 * Có role="status" để trình đọc màn hình thông báo được trạng thái đang tải.
 */
const FullPageLoader: React.FC<{ label?: string }> = ({ label }) => {
  const { t } = useTranslation();

  return (
    <div
      className="flex min-h-screen flex-col items-center justify-center gap-4 bg-surface-main"
      role="status"
      aria-live="polite"
    >
      <span className="h-10 w-10 animate-spin rounded-pill border-[3px] border-accent-line border-t-brand-600" />
      <p className="text-body text-ink-muted">{label ?? t('common.loading')}</p>
    </div>
  );
};

export default FullPageLoader;
