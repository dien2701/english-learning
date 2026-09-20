import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { attemptService } from '../../services/contentService';
import { SKILL_ICON } from '../../types/common';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';

const ExamHistoryPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { skill } = useLabels();
  const { date } = useFormat();
  const { describe } = useApiError();
  const { data, isLoading, error, reload } = useApi(
    () => attemptService.history(),
    [],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('exam.history')}
        description={t('exam.historySubtitle')}
        backTo={{ label: t('exam.title'), to: '/exam' }}
      />

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : isLoading || !data ? (
        <Skeleton className="h-[320px] w-full" />
      ) : data.items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="history"
            title={t('exam.historyEmptyTitle')}
            message={t('exam.historyEmptyHint')}
            action={
              <ButtonLink to="/exam">
                {t('exam.pickExam')}
              </ButtonLink>
            }
          />
        </Card>
      ) : (
        <Card>
          <ul className="divide-y divide-hairline">
            {data.items.map((attempt) => (
              <li key={attempt.attemptId}>
                <Link
                  to={attempt.detailPath}
                  className="flex min-h-[44px] items-center gap-3 py-3.5 transition-colors hover:bg-surface-hover sm:gap-4"
                >
                  <span className="grid h-10 w-10 shrink-0 place-items-center rounded-md bg-accent-soft text-accent">
                    <span
                      aria-hidden="true"
                      className="material-symbols-outlined text-[20px]"
                    >
                      {SKILL_ICON[attempt.skill]}
                    </span>
                  </span>

                  <span className="min-w-0 flex-1">
                    <span className="block truncate text-[14.5px] font-bold text-ink">
                      {L(attempt.lessonTitle)}
                    </span>
                    <span className="mt-1 flex flex-wrap items-center gap-2">
                      <Chip tone="neutral">{skill(attempt.skill)}</Chip>
                      <span className="text-[11.5px] text-ink-subtle">
                        {date(attempt.submittedAt)} ·{' '}
                        {t('exam.correctOf', {
                          correct: attempt.correctCount,
                          total: attempt.totalQuestions,
                        })}
                      </span>
                    </span>
                  </span>

                  <span className="shrink-0 text-right">
                    <span className="block text-[18px] font-extrabold leading-none text-ink">
                      {formatScore(attempt.score)}
                    </span>
                    <span className="mt-0.5 block text-[11px] text-ink-subtle">
                      {t('common.points')}
                    </span>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default ExamHistoryPage;
