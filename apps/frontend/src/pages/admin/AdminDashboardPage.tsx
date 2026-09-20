import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import PageHeader from '../../components/ui/PageHeader';
import { Card, CardHeader } from '../../components/ui/Card';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { useLanguage } from '../../hooks/useLanguage';
import { adminService } from '../../services/adminService';
import { SKILL_ICON } from '../../types/common';

/** Tooltip của biểu đồ đăng ký mới. */
const SignupTooltip: React.FC<{
  active?: boolean;
  payload?: Array<{ value?: number }>;
  label?: string;
}> = ({ active, payload, label }) => {
  const { t } = useTranslation();

  if (!active || !payload?.length) return null;

  return (
    <div className="rounded-md border border-hairline bg-surface px-3 py-2 shadow-md">
      <p className="text-caption font-bold text-ink">{label}</p>
      <p className="mt-0.5 text-[13px] text-ink-muted">
        {t('admin.signupCount', { count: payload[0].value ?? 0 })}
      </p>
    </div>
  );
};

const AdminDashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { skill } = useLabels();
  const { relativeTime } = useFormat();
  const { describe } = useApiError();

  const { data, isLoading, error, reload } = useApi(
    () => adminService.dashboard(),
    [],
  );

  /* Recharts đọc dataKey="label" nên nhãn phải là chuỗi thường; dịch sẵn
     ở đây thay vì để nguyên kiểu song ngữ. */
  const signups = useMemo(
    () => (data?.signups ?? []).map((point) => ({ ...point, label: L(point.label) })),
    [data, L],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('admin.dashboard')}
        description={t('admin.dashboardSubtitle')}
      />

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : isLoading || !data ? (
        <div className="flex flex-col gap-5">
          <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
            {[0, 1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-[120px] w-full" />
            ))}
          </div>
          <Skeleton className="h-[320px] w-full" />
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          {/* Bốn số liệu tổng quan */}
          <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
            {[
              {
                icon: 'group',
                label: t('admin.totalUsers'),
                value: data.overview.totalUsers,
              },
              {
                icon: 'bolt',
                label: t('admin.activeUsers'),
                value: data.overview.activeUsers,
              },
              {
                icon: 'school',
                label: t('admin.studySessions'),
                value: data.overview.studySessions,
              },
              {
                icon: 'library_books',
                label: t('admin.totalContent'),
                value: data.overview.totalContent,
              },
            ].map((tile) => (
              <div
                key={tile.label}
                className="rounded-lg border border-hairline bg-surface p-4 shadow-sm"
              >
                <span className="grid h-9 w-9 place-items-center rounded-pill bg-accent-soft text-accent">
                  <span
                    aria-hidden="true"
                    className="material-symbols-outlined text-[19px]"
                  >
                    {tile.icon}
                  </span>
                </span>
                <p className="mt-3 text-[26px] font-extrabold leading-none tracking-tight text-ink">
                  {tile.value.toLocaleString('vi-VN')}
                </p>
                <p className="mt-1.5 text-caption text-ink-muted">{tile.label}</p>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
            {/* Biểu đồ đăng ký mới */}
            <div className="lg:col-span-7">
              <Card className="h-full">
                <CardHeader
                  title={t('admin.signups')}
                  description={t('admin.signupsHint')}
                />

                <div className="mt-4 h-[260px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={signups}
                      margin={{ top: 8, right: 8, bottom: 0, left: -20 }}
                    >
                      <CartesianGrid
                        vertical={false}
                        stroke="var(--chart-grid)"
                        strokeDasharray="4 4"
                      />
                      <XAxis
                        dataKey="label"
                        tickLine={false}
                        axisLine={false}
                        tick={{ fill: 'var(--text-subtle)', fontSize: 12 }}
                        dy={6}
                      />
                      <YAxis
                        tickLine={false}
                        axisLine={false}
                        tick={{ fill: 'var(--text-subtle)', fontSize: 12 }}
                        width={44}
                      />
                      <Tooltip
                        content={<SignupTooltip />}
                        cursor={{ fill: 'var(--surface-muted)' }}
                      />
                      <Bar dataKey="count" radius={[4, 4, 0, 0]} maxBarSize={44}>
                        {signups.map((point) => (
                          <Cell key={point.label} fill="var(--chart-1)" />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </Card>
            </div>

            {/* Kho nội dung theo kỹ năng */}
            <div className="lg:col-span-5">
              <Card className="h-full">
                <CardHeader
                  title={t('admin.contentLibrary')}
                  description={t('admin.contentLibraryHint')}
                />

                <ul className="mt-4 flex flex-col gap-2.5">
                  {data.contentCounts.map((row) => {
                    const max = Math.max(...data.contentCounts.map((c) => c.count), 1);

                    return (
                      <li key={row.skill}>
                        <div className="flex items-center justify-between gap-2">
                          <span className="inline-flex items-center gap-2 text-[13.5px] text-ink">
                            <span
                              aria-hidden="true"
                              className="material-symbols-outlined text-[18px] text-ink-subtle"
                            >
                              {SKILL_ICON[row.skill]}
                            </span>
                            {skill(row.skill)}
                          </span>
                          <span className="text-[13px] font-extrabold text-ink">
                            {row.count}
                          </span>
                        </div>
                        <div className="mt-1.5 h-1.5 overflow-hidden rounded-pill bg-surface-muted">
                          <div
                            className="h-full rounded-pill bg-brand-500"
                            style={{ width: `${(row.count / max) * 100}%` }}
                          />
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </Card>
            </div>
          </div>

          {/* Hoạt động gần đây */}
          <Card>
            <CardHeader title={t('admin.recentActivity')} />

            <ul className="mt-3 divide-y divide-hairline">
              {data.activities.map((activity) => (
                <li key={activity.id} className="flex items-center gap-3 py-3">
                  <span className="grid h-8 w-8 shrink-0 place-items-center rounded-pill bg-surface-muted text-ink-muted">
                    <span
                      aria-hidden="true"
                      className="material-symbols-outlined text-[17px]"
                    >
                      {activity.actor === 'SYSTEM' ? 'settings' : 'person'}
                    </span>
                  </span>

                  <div className="min-w-0 flex-1">
                    <p className="text-[13.5px] text-ink">{L(activity.action)}</p>
                    <p className="mt-0.5 text-[11.5px] text-ink-subtle">
                      {activity.actor === 'SYSTEM'
                        ? t('admin.actorSystem')
                        : t('admin.actorAdmin')}{' '}
                      · {relativeTime(activity.occurredAt)}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      )}
    </div>
  );
};

export default AdminDashboardPage;
