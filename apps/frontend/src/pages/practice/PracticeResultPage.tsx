import React from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useParams } from 'react-router-dom';

import { ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { AnswerReview, ResultOverview } from '../../components/practice/ResultSummary';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { attemptService } from '../../services/contentService';
import type { PracticeResult } from '../../types/practice';

interface PracticeResultPageProps {
  /** Khoá dịch của tiêu đề trang và của nhãn đường dẫn quay lại. */
  titleKey: string;
  backLabelKey: string;
  backTo: string;
  /** Đường dẫn để làm lại bài, dựng từ id bài học. */
  retryPath: (lessonId: string) => string;
}

/**
 * Màn hình kết quả dùng chung cho Luyện nghe và Luyện đọc.
 *
 * Kết quả được truyền qua state khi vừa nộp xong để hiện ngay, đồng thời
 * vẫn gọi API theo attemptId để mở lại bằng đường dẫn trực tiếp cũng được.
 */
const PracticeResultPage: React.FC<PracticeResultPageProps> = ({
  titleKey,
  backLabelKey,
  backTo,
  retryPath,
}) => {
  const { t } = useTranslation();
  const { describe } = useApiError();
  const { attemptId = '' } = useParams();
  const location = useLocation();

  const passed = (location.state as { result?: PracticeResult } | null)?.result;

  const { data, isLoading, error, reload } = useApi(
    () => attemptService.get(attemptId),
    [attemptId],
  );

  const result = passed ?? data;

  if (error && !passed) {
    return (
      <div className="mx-auto w-full max-w-4xl px-4 py-6 sm:px-6 lg:px-8">
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (!result || (isLoading && !passed)) {
    return (
      <div className="mx-auto w-full max-w-4xl px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[240px] w-full" />
        <Skeleton className="mt-5 h-[400px] w-full" />
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-4xl px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t(titleKey)}
        backTo={{ label: t(backLabelKey), to: backTo }}
      />

      <div className="mb-5">
        <ResultOverview result={result} />
      </div>

      {result.transcript && (
        <Card className="mb-5">
          <h2 className="text-card-title text-ink">{t('listening.transcript')}</h2>
          <p className="mt-2.5 whitespace-pre-line text-[14px] leading-relaxed text-ink-muted">
            {result.transcript}
          </p>
        </Card>
      )}

      <h2 className="mb-3 text-section text-ink">{t('exam.perQuestion')}</h2>
      <AnswerReview answers={result.answers} />

      <div className="mt-6 flex flex-wrap gap-3">
        <ButtonLink to={retryPath(result.lessonId)}>{t('exam.retry')}</ButtonLink>
        <ButtonLink to={backTo} variant="subtle">
          {t(backLabelKey)}
        </ButtonLink>
      </div>
    </div>
  );
};

export default PracticeResultPage;
