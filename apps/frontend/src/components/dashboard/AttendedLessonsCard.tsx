import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { Card, CardHeader } from '../ui/Card';
import { Chip, LevelChip } from '../ui/Chip';
import { EmptyBlock } from '../ui/StateBlocks';
import Pagination from '../ui/Pagination';
import { SKILL_ICON } from '../../types/common';
import type { AttendedLesson, LessonStatus } from '../../types/dashboard';
import { formatScore } from '../../utils/format';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { useLanguage } from '../../hooks/useLanguage';

type FilterKey = 'ALL' | LessonStatus;

const FILTERS: Array<{ key: FilterKey; labelKey: string }> = [
  { key: 'ALL', labelKey: 'dashboard.filterAll' },
  { key: 'IN_PROGRESS', labelKey: 'dashboard.filterInProgress' },
  { key: 'COMPLETED', labelKey: 'dashboard.filterCompleted' },
];

/** Danh sách bài học đã tham gia, gồm cả bài đang dở lẫn bài đã hoàn thành. */
const AttendedLessonsCard: React.FC<{ lessons: AttendedLesson[] }> = ({
  lessons,
}) => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { skill } = useLabels();
  const { relativeTime } = useFormat();
  const [filter, setFilter] = useState<FilterKey>('ALL');
  const [page, setPage] = useState(1);

  const visible =
    filter === 'ALL' ? lessons : lessons.filter((l) => l.status === filter);
  
  const paginatedLessons = visible.slice((page - 1) * 10, page * 10);

  const handleFilterChange = (newFilter: FilterKey) => {
    setFilter(newFilter);
    setPage(1);
  };

  return (
    <Card className="flex flex-col">
      <CardHeader
        title={t('dashboard.attendedTitle')}
        description={t('dashboard.attendedCount', { count: lessons.length })}
        action={
          <div
            role="group"
            aria-label={t('filter.status')}
            className="flex items-center gap-1 rounded-pill bg-surface-muted p-1"
          >
            {FILTERS.map((f) => (
              <button
                key={f.key}
                type="button"
                onClick={() => handleFilterChange(f.key)}
                aria-pressed={filter === f.key}
                className={`min-h-[32px] rounded-pill px-3 text-[12.5px] font-bold transition-colors duration-200 ${
                  filter === f.key
                    ? 'bg-[#15803D] text-white shadow-xs'
                    : 'text-ink-muted hover:text-ink'
                }`}
              >
                {t(f.labelKey)}
              </button>
            ))}
          </div>
        }
      />

      {visible.length === 0 ? (
        <EmptyBlock
          icon="menu_book"
          title={t('dashboard.attendedEmptyTitle')}
          message={t('dashboard.attendedEmptyHint')}
        />
      ) : (
        <>
          <ul className="mt-2 divide-y divide-hairline">
            {paginatedLessons.map((lesson) => (
              <li key={`${lesson.skill}-${lesson.id}`}>
                <Link
                  to={lesson.detailPath}
                  className="flex min-h-[44px] items-center gap-3 py-3 transition-colors duration-200 hover:bg-surface-hover sm:gap-4"
                >
                  <span className="grid h-10 w-10 shrink-0 place-items-center rounded-md bg-accent-soft text-accent">
                    <span
                      aria-hidden="true"
                      className="material-symbols-outlined text-[20px]"
                    >
                      {SKILL_ICON[lesson.skill]}
                    </span>
                  </span>

                  <span className="min-w-0 flex-1">
                    <span className="block truncate text-[14px] font-bold text-ink">
                      {L(lesson.title)}
                    </span>
                    <span className="mt-1 flex flex-wrap items-center gap-2">
                      <LevelChip level={lesson.level} />
                      <span className="text-[11.5px] text-ink-subtle">
                        {skill(lesson.skill)} ·{' '}
                        {relativeTime(lesson.lastActivityAt).toLowerCase()}
                      </span>
                    </span>
                  </span>

                  {/* Cột tiến độ, ẩn ở khổ hẹp để không chen chúc */}
                  <span className="hidden w-32 shrink-0 sm:block">
                    {lesson.status === 'IN_PROGRESS' ? (
                      <>
                        <span className="mb-1 block text-[11.5px] font-bold text-ink-muted">
                          {lesson.progress}%
                        </span>
                        <span className="block h-1.5 overflow-hidden rounded-pill bg-surface-muted">
                          <span
                            className="block h-full rounded-pill bg-brand-500"
                            style={{ width: `${lesson.progress}%` }}
                          />
                        </span>
                      </>
                    ) : (
                      <Chip tone="success" icon="check">
                        {t('dashboard.completed')}
                      </Chip>
                    )}
                  </span>

                  <span className="w-14 shrink-0 text-right">
                    {lesson.score !== undefined ? (
                      <>
                        <span className="block text-[16px] font-extrabold leading-none text-ink">
                          {formatScore(lesson.score)}
                        </span>
                        <span className="mt-0.5 block text-[11px] text-ink-subtle">
                          {t('common.points')}
                        </span>
                      </>
                    ) : (
                      <span
                        aria-hidden="true"
                        className="material-symbols-outlined text-[20px] text-ink-subtle"
                      >
                        chevron_right
                      </span>
                    )}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
          
          <div className="pb-4">
            <Pagination
              page={page}
              pageSize={10}
              total={visible.length}
              onChange={setPage}
            />
          </div>
        </>
      )}
    </Card>
  );
};

export default AttendedLessonsCard;
