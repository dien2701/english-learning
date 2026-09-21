import React from 'react';
import { useTranslation } from 'react-i18next';

import type { ChatQuota } from '../../types/chat';

interface ChatStatusBarProps {
  quota: ChatQuota | null;
  /** Hết lượt hôm nay: hiện thông báo thay cho đếm lượt. */
  exhausted: boolean;
  /** Thông báo lỗi tạm thời của lần gửi gần nhất; có thì hiện kèm nút thử lại. */
  errorMessage?: string | null;
  onRetry?: () => void;
}

/** Dải trạng thái phía trên ô soạn tin: lỗi AI + nút thử lại, hết lượt, hoặc số lượt còn lại. */
const ChatStatusBar: React.FC<ChatStatusBarProps> = ({
  quota,
  exhausted,
  errorMessage,
  onRetry,
}) => {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col gap-1.5 px-3 pt-2">
      {errorMessage && !exhausted && (
        <div
          role="alert"
          className="flex items-center justify-between gap-2 rounded-md bg-danger-bg px-3 py-2 text-[13px] text-danger-fg"
        >
          <span>{errorMessage}</span>
          {onRetry && (
            <button
              type="button"
              onClick={onRetry}
              className="shrink-0 rounded-pill px-3 py-1 text-[13px] font-bold underline hover:bg-surface-hover"
            >
              {t('chat.retry')}
            </button>
          )}
        </div>
      )}

      {exhausted ? (
        <p role="status" className="text-[13px] font-semibold text-danger-fg">
          {t('chat.quotaExhausted')}
        </p>
      ) : (
        quota &&
        quota.limit > 0 && (
          <p className="text-[12px] text-ink-muted">
            {t('chat.quotaLeft', {
              remaining: quota.remaining,
              limit: quota.limit,
            })}
          </p>
        )
      )}
    </div>
  );
};

export default ChatStatusBar;
