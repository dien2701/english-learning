import React, { useState } from 'react';
import { Link } from 'react-router-dom';

import FilterBar, { EMPTY_FILTER, type FilterState } from '../../components/ui/FilterBar';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useDebounced } from '../../hooks/useDebounced';
import { speakingService, topicService } from '../../services/contentService';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';

const SpeakingListPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const [filter, setFilter] = useState<FilterState>(EMPTY_FILTER);
  const search = useDebounced(filter.search, 350);

  const topics = useApi(() => topicService.list(), []);

  const lessons = useApi(
    () =>
      speakingService.list({
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
        title={t('speaking.title')}
        description={t('speaking.subtitle')}
      />

      <FilterBar
        value={filter}
        onChange={setFilter}
        resultCount={lessons.data?.total}
        topics={topics.data ?? []}
        searchPlaceholderKey="speaking.searchPlaceholder"
      />

      {lessons.error ? (
        <Card flush>
          <ErrorState message={lessons.error.message} onRetry={lessons.reload} />
        </Card>
      ) : lessons.isLoading || !lessons.data ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 3 }, (_, i) => (
            <Skeleton key={i} className="h-[170px] w-full" />
          ))}
        </div>
      ) : lessons.data.items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="search_off"
            title={t('filter.noResultTitle')}
            message={t('filter.noResultHint')}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {lessons.data.items.map((lesson) => (
            <Link
              key={lesson.id}
              to={`/speaking/${lesson.id}`}
              className="flex flex-col rounded-lg border border-hairline bg-surface p-5 shadow-sm transition-shadow duration-200 hover:shadow-md"
            >
              <div className="flex flex-wrap items-center gap-2">
                <LevelChip level={lesson.level} />
                <Chip tone="neutral">{L(lesson.topicName)}</Chip>
                {lesson.isCompleted && (
                  <Chip tone="success" icon="check">
                    {t('listening.done')}
                  </Chip>
                )}
              </div>

              <h2 className="mt-3 text-[16px] font-extrabold leading-snug text-ink">
                {L(lesson.title)}
              </h2>
              <p className="mt-1 line-clamp-2 text-[13px] text-ink-muted">
                {L(lesson.description)}
              </p>

              <div className="mt-auto flex flex-wrap items-center gap-x-4 pt-4 text-caption text-ink-muted">
                <span className="inline-flex items-center gap-1">
                  <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                    record_voice_over
                  </span>
                  {lesson.promptCount} {t('common.questionsShort')}
                </span>
                {lesson.lastScore !== undefined && (
                  <span className="ml-auto font-extrabold text-ink">
                    {formatScore(lesson.lastScore)} {t('common.points')}
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

export default SpeakingListPage;
