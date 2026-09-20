import React from 'react';
import { useTranslation } from 'react-i18next';

import { formatClock } from '../../hooks/useCountdown';

interface ExamTimerProps {
  /** Số giây còn lại; truyền null khi bài không giới hạn thời gian. */
  remaining: number | null;
  elapsed: number;
}

/**
 * Đồng hồ làm bài.
 *
 * Khi còn dưới một phút, đồng hồ đổi sang tông cảnh báo và thêm chữ
 * "sắp hết giờ" — không chỉ dựa vào màu sắc để báo hiệu.
 */
const ExamTimer: React.FC<ExamTimerProps> = ({ remaining, elapsed }) => {
  const { t } = useTranslation();
  const isLimited = remaining !== null;
  const isUrgent = isLimited && remaining <= 60;

  return (
    <div
      role="timer"
      aria-live={isUrgent ? 'assertive' : 'off'}
      className={`inline-flex items-center gap-2 rounded-pill px-4 py-2 ${
        isUrgent ? 'bg-danger-bg text-danger-fg' : 'bg-surface-muted text-ink'
      }`}
    >
      <span aria-hidden="true" className="material-symbols-outlined text-[19px]">
        {isUrgent ? 'alarm' : 'schedule'}
      </span>

      <span className="font-mono text-[15px] font-extrabold tabular-nums">
        {formatClock(isLimited ? remaining : elapsed)}
      </span>

      <span className="text-[12px] font-semibold">
        {isUrgent
          ? t('exam.timeAlmostUp')
          : isLimited
            ? t('exam.timeLeft')
            : t('exam.timeSpent')}
      </span>
    </div>
  );
};

export default ExamTimer;
