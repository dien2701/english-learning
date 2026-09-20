import React, { useMemo, useState } from 'react';
import { App } from 'antd';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { useLanguage } from '../../hooks/useLanguage';
import { notificationService, type UserNotification } from '../../services/userService';

const NotificationCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { relativeTime } = useFormat();
  const { describe } = useApiError();

  const { data, isLoading, error, reload } = useApi(
    () => notificationService.list(),
    [],
  );

  /* Thay vì sao chép danh sách vào state rồi phải đồng bộ lại mỗi lần tải,
     chỉ giữ phần người dùng vừa thay đổi rồi phủ lên dữ liệu gốc. */
  const [readIds, setReadIds] = useState<ReadonlySet<string>>(new Set());
  const [isAllRead, setIsAllRead] = useState(false);

  const items = useMemo<UserNotification[]>(
    () =>
      (data?.items ?? []).map((n) => ({
        ...n,
        isRead: n.isRead || isAllRead || readIds.has(n.id),
      })),
    [data, readIds, isAllRead],
  );

  const unreadCount = items.filter((n) => !n.isRead).length;

  const markAllRead = async () => {
    setIsAllRead(true);
    try {
      await notificationService.markAllRead();
      message.success(t('notifications.markedAll'));
    } catch {
      setIsAllRead(false);
      message.error(t('notifications.updateError'));
    }
  };

  const markRead = async (id: string) => {
    setReadIds((prev) => new Set(prev).add(id));
    try {
      await notificationService.markRead(id);
    } catch {
      // Đánh dấu đã đọc không thành công thì cũng không cần báo lỗi ồn ào.
    }
  };

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('notifications.title')}
        description={
          unreadCount > 0
            ? t('notifications.unreadCount', { count: unreadCount })
            : t('notifications.allRead')
        }
        action={
          unreadCount > 0 ? (
            <Button variant="subtle" icon="done_all" onClick={markAllRead}>
              {t('notifications.markAllRead')}
            </Button>
          ) : undefined
        }
      />

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : isLoading || !data ? (
        <Skeleton className="h-[400px] w-full" />
      ) : items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="notifications_off"
            title={t('notifications.emptyTitle')}
            message={t('notifications.emptyHint')}
          />
        </Card>
      ) : (
        <Card flush>
          <ul className="divide-y divide-hairline">
            {items.map((notification) => (
              <li key={notification.id}>
                <Link
                  to={notification.path}
                  onClick={() => markRead(notification.id)}
                  className={`flex min-h-[44px] gap-3 p-4 transition-colors hover:bg-surface-hover ${
                    notification.isRead ? '' : 'bg-accent-subtle'
                  }`}
                >
                  <span className="grid h-10 w-10 shrink-0 place-items-center rounded-pill bg-accent-soft text-accent">
                    <span
                      aria-hidden="true"
                      className="material-symbols-outlined text-[20px]"
                    >
                      {notification.icon}
                    </span>
                  </span>

                  <span className="min-w-0 flex-1">
                    <span className="flex items-center justify-between gap-2">
                      <span className="truncate text-[14px] font-bold text-ink">
                        {L(notification.title)}
                      </span>
                      {!notification.isRead && (
                        <>
                          <span
                            aria-hidden="true"
                            className="h-2 w-2 shrink-0 rounded-pill bg-brand-500"
                          />
                          <span className="sr-only">
                            {t('notifications.unread')}
                          </span>
                        </>
                      )}
                    </span>

                    <span className="mt-0.5 block text-[13px] text-ink-muted">
                      {L(notification.body)}
                    </span>
                    <span className="mt-1 block text-[11.5px] text-ink-subtle">
                      {relativeTime(notification.createdAt)}
                    </span>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default NotificationCenterPage;
