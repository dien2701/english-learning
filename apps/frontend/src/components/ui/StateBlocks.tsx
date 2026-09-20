import React from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from './Button';

/**
 * Khối xương cá dùng trong lúc chờ dữ liệu.
 * aria-hidden vì nội dung thật sẽ được thông báo khi tải xong.
 */
export const Skeleton: React.FC<{ className?: string }> = ({
  className = '',
}) => (
  <div
    aria-hidden="true"
    className={`animate-pulse rounded-md bg-surface-muted ${className}`}
  />
);

interface ErrorStateProps {
  /** Bỏ trống thì dùng câu mặc định "Không tải được dữ liệu". */
  title?: string;
  message: string;
  onRetry?: () => void;
  className?: string;
}

/** Trạng thái lỗi kèm nút thử lại, dùng chung cho mọi khối gọi API. */
export const ErrorState: React.FC<ErrorStateProps> = ({
  title,
  message,
  onRetry,
  className = '',
}) => {
  const { t } = useTranslation();

  return (
    <div
      role="alert"
      className={`flex flex-col items-center gap-3 px-4 py-10 text-center ${className}`}
    >
      <span className="grid h-12 w-12 place-items-center rounded-pill bg-danger-bg text-danger">
        <span aria-hidden="true" className="material-symbols-outlined text-[26px]">
          error
        </span>
      </span>
      <div>
        <p className="text-[15px] font-bold text-ink">
          {title ?? t('common.errorTitle')}
        </p>
        <p className="mt-1 max-w-sm text-body text-ink-muted">{message}</p>
      </div>
      {onRetry && (
        <Button size="sm" icon="refresh" onClick={onRetry} className="mt-1">
          {t('common.retry')}
        </Button>
      )}
    </div>
  );
};

interface EmptyStateProps {
  icon?: string;
  title: string;
  message?: string;
  action?: React.ReactNode;
  className?: string;
}

/** Trạng thái chưa có dữ liệu. */
export const EmptyBlock: React.FC<EmptyStateProps> = ({
  icon = 'inbox',
  title,
  message,
  action,
  className = '',
}) => (
  <div className={`flex flex-col items-center gap-3 px-4 py-10 text-center ${className}`}>
    <span className="grid h-12 w-12 place-items-center rounded-pill bg-surface-muted text-ink-subtle">
      <span aria-hidden="true" className="material-symbols-outlined text-[26px]">
        {icon}
      </span>
    </span>
    <div>
      <p className="text-[15px] font-bold text-ink">{title}</p>
      {message && <p className="mt-1 max-w-sm text-body text-ink-muted">{message}</p>}
    </div>
    {action}
  </div>
);
