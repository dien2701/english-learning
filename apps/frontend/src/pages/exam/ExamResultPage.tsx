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
import { useLabels } from '../../hooks/useLabels';
import { attemptService } from '../../services/contentService';
import type { ExamResult } from '../../types/practice';
import { formatScore } from '../../utils/format';

const ExamResultPage: React.FC = () => {
  const { t } = useTranslation();
  const { skill } = useLabels();
  const { describe } = useApiError();
  const { attemptId = '' } = useParams();
  const location = useLocation();

  const passed = (location.state as { result?: ExamResult } | null)?.result;

  const { data, isLoading, error, reload } = useApi(
    () => attemptService.get(attemptId),
    [attemptId],
  );

  const result = (passed ?? data) as ExamResult | undefined;

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
        title={t('exam.resultTitle')}
        backTo={{ label: t('exam.allExams'), to: '/exam' }}
      />

      <div className="mb-5">
        <ResultOverview result={result} />
      </div>

      {result.breakdown && result.breakdown.length > 0 && (
        <Card className="mb-5">
          <h2 className="text-card-title text-ink">{t('exam.breakdown')}</h2>

          <ul className="mt-4 flex flex-col gap-3.5">
            {result.breakdown.map((row) => (
              <li key={row.skill}>
                <div className="flex items-baseline justify-between gap-2">
                  <span className="text-[13.5px] font-semibold text-ink">
                    {skill(row.skill)}
                  </span>
                  <span className="text-[13px] text-ink-muted">
                    {t('exam.correctOf', {
                      correct: row.correctCount,
                      total: row.totalQuestions,
                    })}{' '}
                    ·{' '}
                    <span className="font-extrabold text-ink">
                      {formatScore(row.score)}
                    </span>
                  </span>
                </div>

                <div
                  role="progressbar"
                  aria-valuenow={row.score}
                  aria-valuemin={0}
                  aria-valuemax={10}
                  aria-label={`${t('common.score')} ${skill(row.skill)}`}
                  className="mt-1.5 h-2 overflow-hidden rounded-pill bg-surface-muted"
                >
                  <div
                    className="h-full rounded-pill bg-brand-500"
                    style={{ width: `${(row.score / 10) * 100}%` }}
                  />
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}

      <h2 className="mb-3 text-section text-ink">{t('exam.perQuestion')}</h2>
      <AnswerReview answers={result.answers} />

      <div className="mt-6 flex flex-wrap gap-3">
        <ButtonLink to={`/exam/${result.lessonId}`}>
          {t('exam.retry')}
        </ButtonLink>
        <ButtonLink to="/exam/history" variant="subtle">
          {t('exam.history')}
        </ButtonLink>
      </div>
    </div>
  );
};

export default ExamResultPage;
