import React, { useState } from 'react';
import { App, Input, Select, Table, Tag } from 'antd';
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
import { adminService } from '../../services/adminService';
import type { AdminUser } from '../../types/admin';
import type { AccountStatus } from '../../types/common';

const STATUS_KEY: Record<AccountStatus, string> = {
  ACTIVE: 'admin.statusActive',
  LOCKED: 'admin.statusLocked',
  PENDING: 'admin.statusPending',
};

const STATUS_COLOR: Record<AccountStatus, string> = {
  ACTIVE: 'success',
  LOCKED: 'error',
  PENDING: 'warning',
};

const ManageUsersPage: React.FC = () => {
  const { message, modal } = App.useApp();
  const { t } = useTranslation();
  const { date, relativeTime } = useFormat();
  const { describe } = useApiError();

  const [search, setSearch] = useState('');
  const [role, setRole] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);

  const debouncedSearch = useDebounced(search, 350);

  const { data, isLoading, error, reload } = useApi(
    () =>
      adminService.listUsers({
        search: debouncedSearch,
        role,
        status,
        page,
        pageSize: 10,
      }),
    [debouncedSearch, role, status, page],
  );

  const changeStatus = (user: AdminUser, next: AccountStatus) => {
    modal.confirm({
      title:
        next === 'LOCKED' ? t('admin.lockTitle') : t('admin.unlockTitle'),
      content:
        next === 'LOCKED'
          ? t('admin.lockBody', { name: user.fullName })
          : t('admin.unlockBody', { name: user.fullName }),
      okText: next === 'LOCKED' ? t('admin.lock') : t('admin.unlock'),
      cancelText: t('common.cancel'),
      okButtonProps: next === 'LOCKED' ? { danger: true } : undefined,
      onOk: async () => {
        try {
          await adminService.updateUser(user.id, { status: next });
          message.success(
            next === 'LOCKED' ? t('admin.locked') : t('admin.unlocked'),
          );
          reload();
        } catch (updateError) {
          message.error(describe(updateError, 'admin.userUpdateError'));
        }
      },
    });
  };

  const changeRole = async (user: AdminUser, nextRole: 'USER' | 'ADMIN') => {
    try {
      await adminService.updateUser(user.id, { role: nextRole });
      message.success(t('admin.roleUpdated'));
      reload();
    } catch (updateError) {
      message.error(describe(updateError, 'admin.roleUpdateError'));
    }
  };

  const columns: ColumnsType<AdminUser> = [
    {
      title: t('admin.users'),
      dataIndex: 'fullName',
      render: (_, user) => (
        <div className="min-w-0">
          <p className="truncate text-[13.5px] font-bold text-ink">{user.fullName}</p>
          <p className="truncate text-[12px] text-ink-muted">{user.email}</p>
        </div>
      ),
    },
    {
      title: t('filter.role'),
      dataIndex: 'role',
      width: 140,
      render: (_, user) => (
        <Select
          size="small"
          value={user.role}
          onChange={(value) => changeRole(user, value)}
          className="w-full"
          aria-label={t('admin.roleOf', { name: user.fullName })}
          options={[
            { value: 'USER', label: t('admin.roleUser') },
            { value: 'ADMIN', label: t('admin.roleAdmin') },
          ]}
        />
      ),
    },
    {
      title: t('filter.status'),
      dataIndex: 'status',
      width: 150,
      render: (_, user) => (
        <Tag color={STATUS_COLOR[user.status]} variant="filled">
          {t(STATUS_KEY[user.status])}
        </Tag>
      ),
    },
    {
      title: t('admin.completedLessons'),
      dataIndex: 'completedLessons',
      width: 130,
      align: 'right',
    },
    {
      title: t('admin.lastActive'),
      dataIndex: 'lastActiveAt',
      width: 150,
      render: (value: string) => (
        <span className="text-[12.5px] text-ink-muted">
          {relativeTime(value)}
        </span>
      ),
    },
    {
      title: t('admin.createdAt'),
      dataIndex: 'createdAt',
      width: 120,
      render: (value: string) => (
        <span className="text-[12.5px] text-ink-muted">{date(value)}</span>
      ),
    },
    {
      title: '',
      key: 'actions',
      width: 110,
      render: (_, user) => (
        <Button
          size="sm"
          variant={user.status === 'LOCKED' ? 'secondary' : 'danger'}
          onClick={() =>
            changeStatus(user, user.status === 'LOCKED' ? 'ACTIVE' : 'LOCKED')
          }
        >
          {user.status === 'LOCKED' ? t('admin.unlock') : t('admin.lock')}
        </Button>
      ),
    },
  ];

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('admin.usersTitle')}
        description={t('admin.usersSubtitle')}
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
          placeholder={t('admin.userSearchPlaceholder')}
          aria-label={t('admin.searchUsers')}
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
          value={role}
          onChange={(value) => {
            setRole(value);
            setPage(1);
          }}
          aria-label={t('admin.filterByRole')}
          className="min-w-[150px]"
          options={[
            { value: '', label: t('admin.anyRole') },
            { value: 'USER', label: t('admin.roleUser') },
            { value: 'ADMIN', label: t('admin.roleAdmin') },
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
            { value: 'LOCKED', label: t('admin.statusLocked') },
          ]}
        />
      </div>

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : (
        <div className="overflow-hidden rounded-lg border border-hairline bg-surface shadow-sm">
          <Table<AdminUser>
            rowKey="id"
            columns={columns}
            dataSource={data?.items ?? []}
            loading={isLoading}
            scroll={{ x: 900 }}
            pagination={{
              current: data?.page ?? 1,
              pageSize: data?.pageSize ?? 10,
              total: data?.total ?? 0,
              onChange: setPage,
              showSizeChanger: false,
              showTotal: (total) => t('admin.userCount', { count: total }),
            }}
          />
        </div>
      )}
    </div>
  );
};

export default ManageUsersPage;
