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
import { topicService, writingService } from '../../services/contentService';
import type { WritingStatus } from '../../types/writing';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';
import { useApiError } from '../../hooks/useApiError';

const STATUS_TONE: Record<WritingStatus, 'neutral' | 'brand' | 'success' | 'warning' | 'danger'> = {
  NOT_STARTED: 'neutral',
  DRAFT: 'neutral',
  GRADING: 'brand',
  GRADED: 'success',
  NEEDS_RETRY: 'danger',
};

const WritingListPage: React.FC = () => {
  const { t } = useTranslation();
  const { describe } = useApiError();
  const { writingStatus } = useLabels();
  const { L } = useLanguage();
  const [filter, setFilter] = useState<FilterState>(EMPTY_FILTER);
  const search = useDebounced(filter.search, 350);

  const topics = useApi(() => topicService.list(), []);

  const prompts = useApi(
    () =>
      writingService.listPrompts({
        search,
        topicId: filter.topicId,
        level: filter.level,
        pageSize: 24,
      }),
    [search, filter.topicId, filter.level],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('writing.title')}
        description={t('writing.subtitle')}
        action={
          <ButtonLink to="/writing/history" variant="subtle" icon="history">
            {t('writing.history')}
          </ButtonLink>
        }
      />

      <FilterBar
        value={filter}
        onChange={setFilter}
        resultCount={prompts.data?.total}
        topics={topics.data ?? []}
        searchPlaceholderKey="writing.searchPlaceholder"
      />

      {prompts.error ? (
        <Card flush>
          <ErrorState message={describe(prompts.error)} onRetry={prompts.reload} />
        </Card>
      ) : prompts.isLoading || !prompts.data ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }, (_, i) => (
            <Skeleton key={i} className="h-[180px] w-full" />
          ))}
        </div>
      ) : prompts.data.items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="search_off"
            title={t('filter.noResultTitle')}
            message={t('filter.noResultHint')}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {prompts.data.items.map((prompt) => (
            <Link
              key={prompt.id}
              to={`/writing/${prompt.id}`}
              className="flex flex-col rounded-lg border border-hairline bg-surface p-5 shadow-sm transition-shadow duration-200 hover:shadow-md"
            >
              <div className="flex flex-wrap items-center gap-2">
                <LevelChip level={prompt.level} />
                <Chip tone="neutral">{L(prompt.topicName)}</Chip>
                {prompt.status !== 'NOT_STARTED' && (
                  <Chip tone={STATUS_TONE[prompt.status]}>
                    {writingStatus(prompt.status)}
                  </Chip>
                )}
              </div>

              <h2 className="mt-3 text-[16px] font-extrabold leading-snug text-ink">
                {L(prompt.title)}
              </h2>

              <div className="mt-auto flex flex-wrap items-center gap-x-4 gap-y-1 pt-4 text-caption text-ink-muted">
                <span className="inline-flex items-center gap-1">
                  <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                    schedule
                  </span>
                  {prompt.suggestedMinutes} {t('common.minutes')}
                </span>
                <span className="inline-flex items-center gap-1">
                  <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                    notes
                  </span>
                  {t('writing.minWords', { count: prompt.minWords })}
                </span>
                {prompt.lastScore !== undefined && (
                  <span className="ml-auto font-extrabold text-ink">
                    {formatScore(prompt.lastScore)} {t('common.points')}
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

export default WritingListPage;
