import React from 'react';

import AttendedLessonsCard from '../components/dashboard/AttendedLessonsCard';
import ContinueLearningBanner from '../components/dashboard/ContinueLearningBanner';
import StudyTimeChart from '../components/dashboard/StudyTimeChart';
import { ErrorState, Skeleton } from '../components/ui/StateBlocks';
import { useApi } from '../hooks/useApi';
import { dashboardService } from '../services/dashboardService';
import { useTranslation } from 'react-i18next';
import { useApiError } from '../hooks/useApiError';

/**
 * Dashboard cố ý giữ gọn ba khối: một bài cần học tiếp, một biểu đồ thời
 * gian học, và danh sách bài đã tham gia. Không có ô chỉ số tổng hợp,
 * không có mục gợi ý.
 */
export const DashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const { describe } = useApiError();
  const { data, isLoading, error, reload } = useApi(
    () => dashboardService.getSummary(),
    [],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <header className="mb-5">
        <h1 className="text-page-title text-ink">{t('dashboard.title')}</h1>
        <p className="mt-1 text-body text-ink-muted">
          {t('dashboard.subtitle')}
        </p>
      </header>

      {error ? (
        <div className="rounded-lg border border-hairline bg-surface shadow-sm">
          <ErrorState message={describe(error)} onRetry={reload} />
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          {isLoading || !data ? (
            <Skeleton className="h-[300px] w-full" />
          ) : (
            <ContinueLearningBanner item={data.continueLearning} />
          )}

          <StudyTimeChart />

          {isLoading || !data ? (
            <Skeleton className="h-[360px] w-full" />
          ) : (
            <AttendedLessonsCard lessons={data.attendedLessons} />
          )}
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
