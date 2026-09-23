import React from 'react';
import { Input, Select } from 'antd';
import { useTranslation } from 'react-i18next';

import { Button } from './Button';

import { useLanguage } from '../../hooks/useLanguage';
import type { Topic } from '../../types/practice';

export interface FilterState {
  search: string;
  topicId: string;
  level: string;
  status: string;
  sort: string;
}

export const EMPTY_FILTER: FilterState = {
  search: '',
  topicId: '',
  level: '',
  status: '',
  sort: 'az',
};

export interface StatusOption {
  value: string;
  /** Khoá dịch, ví dụ 'listening.completed'. */
  labelKey: string;
}

interface FilterBarProps {
  value: FilterState;
  onChange: (next: FilterState) => void;
  topics?: Topic[];
  /** Bỏ trống thì không hiện ô lọc trạng thái. */
  statusOptions?: StatusOption[];
  /** Khoá dịch cho chữ mờ trong ô tìm kiếm. */
  searchPlaceholderKey: string;
  /** Số kết quả đang hiển thị; bỏ trống thì không hiện dòng đếm. */
  resultCount?: number;
  /** Hiện ô chọn sắp xếp A-Z/Z-A/mới nhất; chỉ bật ở trang backend đã hỗ trợ tham số `sort`. */
  sortable?: boolean;
}

/** Nhãn nhỏ in hoa đặt trên mỗi ô lọc. */
const FieldLabel: React.FC<{ htmlFor: string; children: React.ReactNode }> = ({
  htmlFor,
  children,
}) => (
  <label
    htmlFor={htmlFor}
    className="mb-1.5 block text-[11px] font-bold uppercase tracking-[0.07em] text-ink-subtle"
  >
    {children}
  </label>
);

/**
 * Khung tìm kiếm và bộ lọc dùng chung cho mọi trang danh sách.
 *
 * Ô tìm kiếm cố ý to và tách riêng một hàng vì đó là cách vào chính của
 * các trang này. Mỗi ô lọc có nhãn riêng phía trên nên đọc được ngay
 * đang lọc theo tiêu chí gì mà không cần mở dropdown ra xem.
 */
const FilterBar: React.FC<FilterBarProps> = ({
  value,
  onChange,
  topics,
  statusOptions,
  searchPlaceholderKey,
  resultCount,
  sortable,
}) => {
  const { t } = useTranslation();
  const { L } = useLanguage();

  const set = (patch: Partial<FilterState>) => onChange({ ...value, ...patch });

  const hasFilter =
    value.search !== '' ||
    value.topicId !== '' ||
    value.level !== '' ||
    value.status !== '';

  const levelOptions = [
    { value: '', label: t('filter.allLevels') },
    { value: 'BEGINNER', label: t('level.BEGINNER') },
    { value: 'INTERMEDIATE', label: t('level.INTERMEDIATE') },
    { value: 'ADVANCED', label: t('level.ADVANCED') },
  ];

  return (
    <section
      aria-label={t('filter.searchLabel')}
      className="mb-6 rounded-lg border border-hairline bg-surface p-4 shadow-sm sm:p-5"
    >
      {/* Ô tìm kiếm: một hàng riêng, cao 56px, chữ 16px */}
      <Input
        id="filter-search"
        allowClear
        value={value.search}
        onChange={(e) => set({ search: e.target.value })}
        placeholder={t(searchPlaceholderKey)}
        aria-label={t('filter.searchLabel')}
        prefix={
          <span
            aria-hidden="true"
            className="material-symbols-outlined mr-1 text-[22px] text-ink-subtle"
          >
            search
          </span>
        }
        className="!h-14 !text-[16px]"
      />

      {/* Hàng bộ lọc, mỗi ô có nhãn riêng phía trên */}
      <div className="mt-4 flex flex-wrap items-end gap-3">
        {topics && topics.length > 0 && (
          <div className="min-w-[170px] flex-1 sm:flex-none">
            <FieldLabel htmlFor="filter-topic">{t('filter.topic')}</FieldLabel>
            <Select
              id="filter-topic"
              size="large"
              value={value.topicId}
              onChange={(v) => set({ topicId: v })}
              className="w-full sm:w-[190px]"
              options={[
                { value: '', label: t('filter.allTopics') },
                ...topics.map((topic) => ({ value: topic.id, label: L(topic.name) })),
              ]}
            />
          </div>
        )}

        <div className="min-w-[170px] flex-1 sm:flex-none">
          <FieldLabel htmlFor="filter-level">{t('filter.level')}</FieldLabel>
          <Select
            id="filter-level"
            size="large"
            value={value.level}
            onChange={(v) => set({ level: v })}
            className="w-full sm:w-[190px]"
            options={levelOptions}
          />
        </div>

        {statusOptions && (
          <div className="min-w-[170px] flex-1 sm:flex-none">
            <FieldLabel htmlFor="filter-status">{t('filter.status')}</FieldLabel>
            <Select
              id="filter-status"
              size="large"
              value={value.status}
              onChange={(v) => set({ status: v })}
              className="w-full sm:w-[200px]"
              options={statusOptions.map((option) => ({
                value: option.value,
                label: t(option.labelKey),
              }))}
            />
          </div>
        )}

        {sortable && (
          <div className="min-w-[170px] flex-1 sm:flex-none">
            <FieldLabel htmlFor="filter-sort">{t('filter.sort')}</FieldLabel>
            <Select
              id="filter-sort"
              size="large"
              value={value.sort}
              onChange={(v) => set({ sort: v })}
              className="w-full sm:w-[170px]"
              options={[
                { value: 'az', label: t('filter.sortAz') },
                { value: 'za', label: t('filter.sortZa') },
                { value: 'newest', label: t('filter.sortNewest') },
              ]}
            />
          </div>
        )}

        {/* Số kết quả và nút xoá lọc, đẩy về cuối hàng */}
        <div className="ml-auto flex items-center gap-3 self-end pb-0.5">
          {resultCount !== undefined && (
            <span className="text-[13px] font-semibold text-ink-muted">
              {t('filter.resultCount', { count: resultCount })}
            </span>
          )}

          {hasFilter && (
            <Button
              variant="subtle"
              size="sm"
              icon="filter_alt_off"
              onClick={() => onChange(EMPTY_FILTER)}
            >
              {t('filter.clear')}
            </Button>
          )}
        </div>
      </div>
    </section>
  );
};

export default FilterBar;
