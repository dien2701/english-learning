import React, { useMemo, useState } from 'react';

import DeckCard from '../../components/flashcard/DeckCard';
import { Button } from '../../components/ui/Button';
import FilterBar, { EMPTY_FILTER, type FilterState } from '../../components/ui/FilterBar';
import PageHeader from '../../components/ui/PageHeader';
import Pagination from '../../components/ui/Pagination';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useDebounced } from '../../hooks/useDebounced';
import { flashcardService, topicService } from '../../services/contentService';
import { useTranslation } from 'react-i18next';

const STATUS_OPTIONS = [
  { value: '', labelKey: 'filter.allStatuses' },
  { value: 'NOT_STARTED', labelKey: 'flashcard.notStarted' },
  { value: 'IN_PROGRESS', labelKey: 'flashcard.inProgress' },
  { value: 'COMPLETED', labelKey: 'flashcard.completed' },
];

const FlashcardListPage: React.FC = () => {
  const { t } = useTranslation();
  const { describe } = useApiError();
  const [filter, setFilter] = useState<FilterState>(EMPTY_FILTER);
  const [page, setPage] = useState(1);
  // Đổi bộ lọc/sắp xếp thì về trang 1; gộp vào cùng sự kiện thay đổi thay vì dùng
  // useEffect (bị eslint react-hooks/set-state-in-effect chặn setState trong effect).
  const handleFilterChange = (next: FilterState) => {
    setFilter(next);
    setPage(1);
  };

  // Chờ người dùng gõ xong mới gọi API, tránh bắn request mỗi ký tự.
  const search = useDebounced(filter.search, 350);

  const topics = useApi(() => topicService.list(), []);

  const decks = useApi(
    () =>
      flashcardService.listDecks({
        search,
        topicId: filter.topicId,
        level: filter.level,
        status: filter.status,
        sort: filter.sort,
        page,
        pageSize: 12,
      }),
    [search, filter.topicId, filter.level, filter.status, filter.sort, page],
  );

  const isFiltering = useMemo(
    () => Boolean(search || filter.topicId || filter.level || filter.status),
    [search, filter.topicId, filter.level, filter.status],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('flashcard.title')}
        description={t('flashcard.subtitle')}
      />

      <FilterBar
        value={filter}
        onChange={handleFilterChange}
        resultCount={decks.data?.total}
        topics={topics.data ?? []}
        statusOptions={STATUS_OPTIONS}
        searchPlaceholderKey="flashcard.searchPlaceholder"
        sortable
      />

      {decks.error ? (
        <div className="rounded-lg border border-hairline bg-surface shadow-sm">
          <ErrorState message={describe(decks.error)} onRetry={decks.reload} />
        </div>
      ) : decks.isLoading || !decks.data ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: 8 }, (_, i) => (
            <Skeleton key={i} className="h-[300px] w-full" />
          ))}
        </div>
      ) : decks.data.items.length === 0 ? (
        <div className="rounded-lg border border-hairline bg-surface shadow-sm">
          <EmptyBlock
            icon="search_off"
            title={
              isFiltering ? t('filter.noResultTitle') : t('common.noContentTitle')
            }
            message={
              isFiltering ? t('filter.noResultHint') : t('common.noContentHint')
            }
            action={
              isFiltering ? (
                <Button size="sm" onClick={() => handleFilterChange(EMPTY_FILTER)}>
                  {t('filter.clear')}
                </Button>
              ) : undefined
            }
          />
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {decks.data.items.map((deck) => (
              <DeckCard key={deck.id} deck={deck} />
            ))}
          </div>
          <Pagination
            page={decks.data.page}
            pageSize={decks.data.pageSize}
            total={decks.data.total}
            onChange={setPage}
          />
        </>
      )}
    </div>
  );
};

export default FlashcardListPage;
