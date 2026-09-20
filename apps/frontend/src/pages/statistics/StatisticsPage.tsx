import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import PageHeader from '../../components/ui/PageHeader';
import { Card, CardHeader } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import StudyTimeChart from '../../components/dashboard/StudyTimeChart';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { statisticsService } from '../../services/userService';
import { SKILL_ICON } from '../../types/common';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';

const StatisticsPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { skill } = useLabels();
  const { date } = useFormat();
  const { describe } = useApiError();
  const { data, isLoading, error, reload } = useApi(
    () => statisticsService.overview(),
    [],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('statistics.title')}
        description={t('statistics.subtitle')}
      />

      <div className="flex flex-col gap-5">
        <StudyTimeChart />

        {error ? (
          <Card flush>
            <ErrorState message={describe(error)} onRetry={reload} />
          </Card>
        ) : isLoading || !data ? (
          <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
            <div className="lg:col-span-7">
              <Skeleton className="h-[340px] w-full" />
            </div>
            <div className="lg:col-span-5">
              <Skeleton className="h-[340px] w-full" />
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
            {/* Điểm trung bình theo kỹ năng */}
            <div className="lg:col-span-7">
              <Card className="h-full">
                <CardHeader
                  title={t('statistics.bySkill')}
                  description={t('statistics.bySkillHint')}
                />

                <ul className="mt-4 flex flex-col gap-4">
                  {data.skills.map((row) => (
                    <li key={row.skill}>
                      <div className="flex items-center justify-between gap-2">
                        <span className="inline-flex items-center gap-2 text-[13.5px] font-semibold text-ink">
                          <span
                            aria-hidden="true"
                            className="material-symbols-outlined text-[18px] text-ink-subtle"
                          >
                            {SKILL_ICON[row.skill]}
                          </span>
                          {skill(row.skill)}
                        </span>

                        <span className="flex items-center gap-2.5">
                          <span className="text-[11.5px] text-ink-subtle">
                            {t('statistics.attempts', { count: row.attempts })}
                          </span>
                          <span
                            className={`inline-flex items-center gap-0.5 text-[12px] font-bold ${
                              row.change >= 0 ? 'text-success' : 'text-warning'
                            }`}
                          >
                            <span
                              aria-hidden="true"
                              className="material-symbols-outlined text-[14px]"
                            >
                              {row.change >= 0 ? 'trending_up' : 'trending_down'}
                            </span>
                            {row.change >= 0 ? '+' : ''}
                            {row.change.toFixed(1)}
                          </span>
                          <span className="w-9 text-right text-[14px] font-extrabold text-ink">
                            {formatScore(row.averageScore)}
                          </span>
                        </span>
                      </div>

                      <div
                        role="progressbar"
                        aria-valuenow={row.averageScore}
                        aria-valuemin={0}
                        aria-valuemax={10}
                        aria-label={t('statistics.avgOf', { skill: skill(row.skill) })}
                        className="mt-1.5 h-2 overflow-hidden rounded-pill bg-surface-muted"
                      >
                        <div
                          className="h-full rounded-pill bg-brand-500"
                          style={{ width: `${(row.averageScore / 10) * 100}%` }}
                        />
                      </div>
                    </li>
                  ))}
                </ul>
              </Card>
            </div>

            {/* Lượt làm bài gần đây */}
            <div className="lg:col-span-5">
              <Card className="h-full">
                <CardHeader title={t('statistics.recentAttempts')} />

                {data.recentAttempts.length === 0 ? (
                  <EmptyBlock
                    icon="history"
                    title={t('statistics.emptyTitle')}
                    message={t('statistics.emptyHint')}
                  />
                ) : (
                  <ul className="mt-3 divide-y divide-hairline">
                    {data.recentAttempts.map((attempt) => (
                      <li key={attempt.attemptId}>
                        <Link
                          to={attempt.detailPath}
                          className="flex min-h-[44px] items-center gap-3 py-3 transition-colors hover:bg-surface-hover"
                        >
                          <span className="min-w-0 flex-1">
                            <span className="block truncate text-[13.5px] font-semibold text-ink">
                              {L(attempt.lessonTitle)}
                            </span>
                            <span className="mt-1 flex items-center gap-2">
                              <Chip tone="neutral">
                                {skill(attempt.skill)}
                              </Chip>
                              <span className="text-[11px] text-ink-subtle">
                                {date(attempt.submittedAt)}
                              </span>
                            </span>
                          </span>

                          <span className="shrink-0 text-[15px] font-extrabold text-ink">
                            {formatScore(attempt.score)}
                          </span>
                        </Link>
                      </li>
                    ))}
                  </ul>
                )}
              </Card>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default StatisticsPage;
