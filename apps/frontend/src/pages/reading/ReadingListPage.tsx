import React, { useState } from 'react';
import { Link } from 'react-router-dom';

import FilterBar, { EMPTY_FILTER, type FilterState } from '../../components/ui/FilterBar';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import Pagination from '../../components/ui/Pagination';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useDebounced } from '../../hooks/useDebounced';
import { readingService, topicService } from '../../services/contentService';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';
import { useApiError } from '../../hooks/useApiError';

const ReadingListPage: React.FC = () => {
  const { t } = useTranslation();
  const { describe } = useApiError();
  const { L } = useLanguage();
  const [filter, setFilter] = useState<FilterState>(EMPTY_FILTER);
  const [page, setPage] = useState(1);
  // Đổi bộ lọc/sắp xếp thì về trang 1; gộp vào cùng sự kiện thay đổi thay vì dùng
  // useEffect (bị eslint react-hooks/set-state-in-effect chặn setState trong effect).
  const handleFilterChange = (next: FilterState) => {
    setFilter(next);
    setPage(1);
  };
  const search = useDebounced(filter.search, 350);

  const topics = useApi(() => topicService.list(), []);

  const lessons = useApi(
    () =>
      readingService.list({
        search,
        topicId: filter.topicId,
        level: filter.level,
        sort: filter.sort,
        page,
        pageSize: 12,
      }),
    [search, filter.topicId, filter.level, filter.sort, page],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('reading.title')}
        description={t('reading.subtitle')}
      />

      <FilterBar
        value={filter}
        onChange={handleFilterChange}
        resultCount={lessons.data?.total}
        topics={topics.data ?? []}
        searchPlaceholderKey="reading.searchPlaceholder"
        sortable
      />

      {lessons.error ? (
        <Card flush>
          <ErrorState message={describe(lessons.error)} onRetry={lessons.reload} />
        </Card>
      ) : lessons.isLoading || !lessons.data ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: 8 }, (_, i) => (
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
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {lessons.data.items.map((lesson) => (
              <Link
                key={lesson.id}
                to={`/reading/${lesson.id}`}
                className="flex flex-col overflow-hidden rounded-lg border border-hairline bg-surface shadow-sm transition-shadow duration-200 hover:shadow-md"
              >
                <div className="flex flex-1 flex-col p-5">
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

                  <div className="mt-auto flex flex-wrap items-center gap-x-4 gap-y-1 pt-4 text-caption text-ink-muted">
                    <span className="inline-flex items-center gap-1">
                      <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                        notes
                      </span>
                      {lesson.wordCount} {t('common.words')}
                    </span>
                    <span className="inline-flex items-center gap-1">
                      <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                        help
                      </span>
                      {lesson.questionCount} {t('common.questionsShort')}
                    </span>
                    {lesson.timeLimitMinutes > 0 && (
                      <span className="inline-flex items-center gap-1">
                        <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                          timer
                        </span>
                        {lesson.timeLimitMinutes} {t('common.minutes')}
                      </span>
                    )}
                    {lesson.lastScore !== undefined && (
                      <span className="ml-auto font-extrabold text-ink">
                        {formatScore(lesson.lastScore)} {t('common.points')}
                      </span>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
          <Pagination
            page={lessons.data.page}
            pageSize={lessons.data.pageSize}
            total={lessons.data.total}
            onChange={setPage}
          />
        </>
      )}
    </div>
  );
};

export default ReadingListPage;
