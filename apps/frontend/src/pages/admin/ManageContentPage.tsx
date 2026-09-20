import React, { useState } from 'react';
import { App, Input, Select, Switch, Table, Tag, Tooltip } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { ErrorState } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useDebounced } from '../../hooks/useDebounced';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { adminService } from '../../services/adminService';
import type { Level } from '../../types/common';
import type { AdminContentItem } from '../../types/admin';
import { useLanguage } from '../../hooks/useLanguage';

/**
 * Quản lý toàn bộ nội dung học trong một bảng.
 *
 * Nội dung đã nằm trong lịch sử học của người dùng thì không cho xoá —
 * chỉ được chuyển sang ngừng hoạt động, đúng quy định nghiệp vụ.
 */
const ManageContentPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { skill: skillLabel, level } = useLabels();
  const { date } = useFormat();
  const { describe } = useApiError();
  const { message, modal } = App.useApp();

  const [search, setSearch] = useState('');
  const [skill, setSkill] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);

  const debouncedSearch = useDebounced(search, 350);

  const { data, isLoading, error, reload } = useApi(
    () =>
      adminService.listContent({
        search: debouncedSearch,
        skill,
        status,
        page,
        pageSize: 10,
      }),
    [debouncedSearch, skill, status, page],
  );

  const toggleStatus = async (item: AdminContentItem, active: boolean) => {
    try {
      await adminService.setContentStatus(item.id, active ? 'ACTIVE' : 'INACTIVE');
      message.success(active ? t('admin.enabled') : t('admin.disabled'));
      reload();
    } catch (updateError) {
      message.error(describe(updateError, 'admin.statusUpdateError'));
    }
  };

  const remove = (item: AdminContentItem) => {
    modal.confirm({
      title: t('admin.deleteTitle'),
      content: t('admin.deleteBody', { title: L(item.title) }),
      okText: t('common.delete'),
      cancelText: t('common.cancel'),
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await adminService.deleteContent(item.id);
          message.success(t('admin.deleted'));
          reload();
        } catch (deleteError) {
          // Server chặn khi nội dung đang được dùng; hiện nguyên thông báo đó.
          message.error(describe(deleteError, 'admin.deleteError'));
        }
      },
    });
  };

  const columns: ColumnsType<AdminContentItem> = [
    {
      title: t('admin.content'),
      dataIndex: 'title',
      render: (_, item) => (
        <div className="min-w-0">
          <p className="truncate text-[13.5px] font-bold text-ink">{L(item.title)}</p>
          <p className="truncate text-[12px] text-ink-muted">
            {L(item.topicName)} · {item.itemCount}{' '}
            {item.skill === 'VOCABULARY'
              ? t('admin.cardsUnit')
              : t('admin.itemsUnit')}
          </p>
        </div>
      ),
    },
    {
      title: t('filter.skill'),
      dataIndex: 'skill',
      width: 130,
      render: (_, item) => <Tag variant="filled">{skillLabel(item.skill)}</Tag>,
    },
    {
      title: t('filter.level'),
      dataIndex: 'level',
      width: 120,
      render: (value: string) => (
        <span className="text-[12.5px] text-ink-muted">
          {level(value as Level)}
        </span>
      ),
    },
    {
      title: t('admin.updatedAt'),
      dataIndex: 'updatedAt',
      width: 120,
      render: (value: string) => (
        <span className="text-[12.5px] text-ink-muted">{date(value)}</span>
      ),
    },
    {
      title: t('admin.active'),
      dataIndex: 'status',
      width: 120,
      render: (_, item) => (
        <Switch
          checked={item.status === 'ACTIVE'}
          onChange={(checked) => toggleStatus(item, checked)}
          aria-label={t('admin.activeOf', { title: L(item.title) })}
        />
      ),
    },
    {
      title: '',
      key: 'actions',
      width: 90,
      render: (_, item) =>
        item.inUse ? (
          <Tooltip title={t('admin.inUseTooltip')}>
            <span className="inline-flex cursor-not-allowed items-center gap-1 text-[12.5px] font-semibold text-ink-subtle">
              <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
                lock
              </span>
              {t('admin.inUse')}
            </span>
          </Tooltip>
        ) : (
          <Button size="sm" variant="danger" onClick={() => remove(item)}>
            {t('common.delete')}
          </Button>
        ),
    },
  ];

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('admin.contentTitle')}
        description={t('admin.contentSubtitle')}
      />

      <div className="mb-5 flex flex-wrap items-center gap-2.5">
        <Input
          allowClear
          size="large"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(1);
          }}
          placeholder={t('admin.contentSearchPlaceholder')}
          aria-label={t('admin.searchContent')}
          prefix={
            <span
              aria-hidden="true"
              className="material-symbols-outlined text-[19px] text-ink-subtle"
            >
              search
            </span>
          }
          className="min-w-[200px] flex-1 sm:max-w-xs"
        />

        <Select
          size="large"
          value={skill}
          onChange={(value) => {
            setSkill(value);
            setPage(1);
          }}
          aria-label={t('admin.filterBySkill')}
          className="min-w-[160px]"
          options={[
            { value: '', label: t('admin.anySkill') },
            ...(
              ['VOCABULARY', 'LISTENING', 'READING', 'WRITING', 'SPEAKING', 'EXAM'] as const
            ).map((key) => ({ value: key, label: skillLabel(key) })),
          ]}
        />

        <Select
          size="large"
          value={status}
          onChange={(value) => {
            setStatus(value);
            setPage(1);
          }}
          aria-label={t('admin.filterByStatus')}
          className="min-w-[170px]"
          options={[
            { value: '', label: t('admin.anyStatus') },
            { value: 'ACTIVE', label: t('admin.statusActive') },
            { value: 'INACTIVE', label: t('admin.statusInactive') },
          ]}
        />
      </div>

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : (
        <div className="overflow-hidden rounded-lg border border-hairline bg-surface shadow-sm">
          <Table<AdminContentItem>
            rowKey="id"
            columns={columns}
            dataSource={data?.items ?? []}
            loading={isLoading}
            scroll={{ x: 860 }}
            pagination={{
              current: data?.page ?? 1,
              pageSize: data?.pageSize ?? 10,
              total: data?.total ?? 0,
              onChange: setPage,
              showSizeChanger: false,
              showTotal: (total) => t('admin.contentCount', { count: total }),
            }}
          />
        </div>
      )}
    </div>
  );
};

export default ManageContentPage;
