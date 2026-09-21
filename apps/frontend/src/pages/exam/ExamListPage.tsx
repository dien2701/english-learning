import React, { useState } from 'react';
import { Link } from 'react-router-dom';

import FilterBar, { EMPTY_FILTER, type FilterState } from '../../components/ui/FilterBar';
import { ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useLabels } from '../../hooks/useLabels';
import { useDebounced } from '../../hooks/useDebounced';
import { examService } from '../../services/contentService';
import type { ExamStatus } from '../../types/practice';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';
import { useApiError } from '../../hooks/useApiError';

const STATUS_OPTIONS = [
  { value: '', labelKey: 'filter.allStatuses' },
  { value: 'NOT_TAKEN', labelKey: 'exam.notTaken' },
  { value: 'COMPLETED', labelKey: 'exam.completed' },
];

const STATUS_KEY: Record<ExamStatus, string> = {
  NOT_TAKEN: 'exam.notTaken',
  IN_PROGRESS: 'exam.inProgress',
  COMPLETED: 'exam.completed',
};

const ExamListPage: React.FC = () => {
  const { t } = useTranslation();
  const { describe } = useApiError();
  const { skill } = useLabels();
  const { L } = useLanguage();
  const [filter, setFilter] = useState<FilterState>(EMPTY_FILTER);
  const search = useDebounced(filter.search, 350);

  const exams = useApi(
    () =>
      examService.list({
        search,
        level: filter.level,
        status: filter.status,
        pageSize: 24,
      }),
    [search, filter.level, filter.status],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('exam.title')}
        description={t('exam.subtitle')}
        action={
          <ButtonLink to="/exam/history" variant="subtle" icon="history">
            {t('exam.history')}
          </ButtonLink>
        }
      />

      <FilterBar
        value={filter}
        onChange={setFilter}
        resultCount={exams.data?.total}
        statusOptions={STATUS_OPTIONS}
        searchPlaceholderKey="exam.searchPlaceholder"
      />

      {exams.error ? (
        <Card flush>
          <ErrorState message={describe(exams.error)} onRetry={exams.reload} />
        </Card>
      ) : exams.isLoading || !exams.data ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 3 }, (_, i) => (
            <Skeleton key={i} className="h-[190px] w-full" />
          ))}
        </div>
      ) : exams.data.items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="search_off"
            title={t('filter.noResultTitle')}
            message={t('filter.noResultHint')}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {exams.data.items.map((exam) => (
            <Link
              key={exam.id}
              to={`/exam/${exam.id}`}
              className="flex flex-col rounded-lg border border-hairline bg-surface p-5 shadow-sm transition-shadow duration-200 hover:shadow-md"
            >
              <div className="flex flex-wrap items-center gap-2">
                <LevelChip level={exam.level} />
                <Chip tone={exam.status === 'COMPLETED' ? 'success' : 'neutral'}>
                  {t(STATUS_KEY[exam.status])}
                </Chip>
              </div>

              <h2 className="mt-3 text-[16px] font-extrabold leading-snug text-ink">
                {L(exam.title)}
              </h2>
              <p className="mt-1 line-clamp-2 text-[13px] text-ink-muted">
                {L(exam.description)}
              </p>

              <div className="mt-3 flex flex-wrap gap-1.5">
                {exam.skills.map((value) => (
                  <Chip key={value} tone="brand">
                    {skill(value)}
                  </Chip>
                ))}
              </div>

              <div className="mt-auto flex flex-wrap items-center gap-x-4 pt-4 text-caption text-ink-muted">
                <span className="inline-flex items-center gap-1">
                  <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                    help
                  </span>
                  {exam.questionCount} {t('common.questionsShort')}
                </span>
                <span className="inline-flex items-center gap-1">
                  <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                    timer
                  </span>
                  {exam.timeLimitMinutes} {t('common.minutes')}
                </span>
                {exam.lastScore !== undefined && (
                  <span className="ml-auto font-extrabold text-ink">
                    {formatScore(exam.lastScore)} {t('common.points')}
                  </span>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default ExamListPage;
