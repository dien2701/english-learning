import React, { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Area,
  CartesianGrid,
  ComposedChart,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import { ErrorState, Skeleton } from '../ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useLanguage } from '../../hooks/useLanguage';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { dashboardService } from '../../services/dashboardService';
import type { StudyPeriod, StudyTimePoint } from '../../types/dashboard';
import { formatSignedPercent } from '../../utils/format';

/* Màu chuỗi lấy từ biến CSS nên tự đổi theo chế độ sáng/tối.
   Cả hai bộ giá trị đã chạy qua bộ kiểm tra màu: đạt ngưỡng tách màu
   cho người mù màu và tương phản tối thiểu 3:1 với nền. */
const SERIES_CURRENT = 'var(--chart-1)';
const SERIES_PREVIOUS = 'var(--chart-2)';

/** Hai kỳ xem được, kèm khoá dịch cho nhãn nút và câu so sánh. */
const PERIODS: Array<{
  key: StudyPeriod;
  labelKey: string;
  compareKey: string;
}> = [
  { key: 'WEEK', labelKey: 'dashboard.week', compareKey: 'dashboard.lastWeek' },
  { key: 'MONTH', labelKey: 'dashboard.month', compareKey: 'dashboard.lastMonth' },
];

const compareKeyOf = (period: StudyPeriod) =>
  PERIODS.find((p) => p.key === period)!.compareKey;

const labelKeyOf = (period: StudyPeriod) =>
  PERIODS.find((p) => p.key === period)!.labelKey;

/** Nội dung tooltip khi rê chuột lên biểu đồ. */
const ChartTooltip: React.FC<{
  active?: boolean;
  payload?: Array<{ dataKey?: string | number; value?: number }>;
  label?: string;
  period: StudyPeriod;
}> = ({ active, payload, label, period }) => {
  const { t } = useTranslation();
  const { duration } = useFormat();

  if (!active || !payload?.length) return null;

  const current = payload.find((p) => p.dataKey === 'minutes')?.value ?? 0;
  const previous = payload.find((p) => p.dataKey === 'previousMinutes')?.value ?? 0;

  return (
    <div className="rounded-md border border-hairline bg-surface px-3 py-2 shadow-md">
      <p className="text-caption font-bold text-ink">{label}</p>
      <ul className="mt-1.5 flex flex-col gap-1">
        <li className="flex items-center gap-2 text-[12.5px]">
          <span
            aria-hidden="true"
            className="h-2 w-2 shrink-0 rounded-pill"
            style={{ background: SERIES_CURRENT }}
          />
          <span className="text-ink-muted">{t('dashboard.thisPeriod')}</span>
          <span className="ml-auto font-bold text-ink">
            {duration(current)}
          </span>
        </li>
        <li className="flex items-center gap-2 text-[12.5px]">
          <span
            aria-hidden="true"
            className="h-2 w-2 shrink-0 rounded-pill"
            style={{ background: SERIES_PREVIOUS }}
          />
          <span className="text-ink-muted">{t(compareKeyOf(period))}</span>
          <span className="ml-auto font-bold text-ink">
            {duration(previous)}
          </span>
        </li>
      </ul>
    </div>
  );
};

/**
 * Điểm dữ liệu đã dịch nhãn sẵn. Recharts đọc `dataKey="label"` nên nhãn
 * phải là chuỗi thường, không thể để nguyên kiểu song ngữ `L10n`.
 */
type PlottedPoint = Omit<StudyTimePoint, 'label'> & { label: string };

/** Bảng số liệu thay thế, phục vụ trình đọc màn hình và người muốn xem số. */
const DataTable: React.FC<{ points: PlottedPoint[]; period: StudyPeriod }> = ({
  points,
  period,
}) => {
  const { t } = useTranslation();
  const { duration } = useFormat();

  return (
    <table className="w-full border-collapse text-[13px]">
      <caption className="sr-only">
        {t('dashboard.tableCaption', {
          period: t(labelKeyOf(period)).toLowerCase(),
          previous: t(compareKeyOf(period)),
        })}
      </caption>
      <thead>
        <tr className="border-b border-hairline text-left text-ink-muted">
          <th scope="col" className="py-2 font-semibold">
            {t('dashboard.tableMilestone')}
          </th>
          <th scope="col" className="py-2 text-right font-semibold">
            {t('dashboard.thisPeriod')}
          </th>
          <th scope="col" className="py-2 text-right font-semibold">
            {t('dashboard.lastPeriod')}
          </th>
        </tr>
      </thead>
      <tbody>
        {points.map((point) => (
          <tr key={point.label} className="border-b border-hairline last:border-0">
            <th scope="row" className="py-2 text-left font-medium text-ink">
              {point.label}
            </th>
            <td className="py-2 text-right text-ink">{duration(point.minutes)}</td>
            <td className="py-2 text-right text-ink-muted">
              {duration(point.previousMinutes)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

const StudyTimeChart: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { duration } = useFormat();
  const { describe } = useApiError();
  const [period, setPeriod] = useState<StudyPeriod>('WEEK');
  const [showTable, setShowTable] = useState(false);

  const { data, isLoading, error, reload } = useApi(
    () => dashboardService.getStudyTime(period),
    [period],
  );

  const isUp = (data?.changePercent ?? 0) >= 0;

  /* Nhãn trục dịch một lần ở đây rồi dùng lại cho cả biểu đồ lẫn bảng. */
  const points = useMemo<PlottedPoint[]>(
    () => (data?.points ?? []).map((point) => ({ ...point, label: L(point.label) })),
    [data, L],
  );

  return (
    <Card className="flex h-full flex-col">
      {/* Hàng điều khiển: tiêu đề bên trái, bộ lọc kỳ bên phải */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-card-title text-ink">{t('dashboard.studyTime')}</h2>
          <p className="mt-0.5 text-caption font-normal text-ink-muted">
            {t('dashboard.comparedTo', { period: t(compareKeyOf(period)) })}
          </p>
        </div>

        <div
          role="group"
          aria-label={t('dashboard.selectPeriod')}
          className="flex items-center gap-1 rounded-pill bg-surface-muted p-1"
        >
          {PERIODS.map(({ key, labelKey }) => (
            <button
              key={key}
              type="button"
              onClick={() => setPeriod(key)}
              aria-pressed={period === key}
              className={`min-h-[32px] rounded-pill px-4 text-[13px] font-bold transition-colors duration-200 ${
                period === key
                  ? 'bg-surface text-accent shadow-xs'
                  : 'text-ink-muted hover:text-ink'
              }`}
            >
              {t(labelKey)}
            </button>
          ))}
        </div>
      </div>

      {error ? (
        <ErrorState message={describe(error)} onRetry={reload} />
      ) : isLoading || !data ? (
        <div className="mt-5 flex flex-col gap-3">
          <Skeleton className="h-9 w-40" />
          <Skeleton className="h-[240px] w-full" />
        </div>
      ) : (
        <>
          {/* Con số tổng — dẫn dắt trước khi người đọc nhìn vào hình */}
          <div className="mt-4 flex flex-wrap items-center gap-3">
            <p className="text-[30px] font-extrabold leading-none tracking-tight text-ink">
              {duration(data.totalMinutes)}
            </p>

            <span
              className={`inline-flex items-center gap-1 rounded-pill px-2.5 py-1 text-[12px] font-bold ${
                isUp ? 'bg-success-bg text-success-fg' : 'bg-warning-bg text-warning-fg'
              }`}
            >
              <span
                aria-hidden="true"
                className="material-symbols-outlined text-[15px]"
              >
                {isUp ? 'trending_up' : 'trending_down'}
              </span>
              {t('dashboard.changeVs', {
                change: formatSignedPercent(data.changePercent),
                period: t(compareKeyOf(period)),
              })}
            </span>
          </div>

          {/* Chú giải — luôn hiện vì có từ hai chuỗi trở lên */}
          <ul className="mt-4 flex flex-wrap items-center gap-x-5 gap-y-2">
            <li className="flex items-center gap-2 text-[12.5px] text-ink-muted">
              <span
                aria-hidden="true"
                className="h-2.5 w-2.5 rounded-pill"
                style={{ background: SERIES_CURRENT }}
              />
              {t('dashboard.thisPeriod')}
            </li>
            <li className="flex items-center gap-2 text-[12.5px] text-ink-muted">
              <span
                aria-hidden="true"
                className="h-0.5 w-5 rounded-pill"
                style={{
                  backgroundImage: `repeating-linear-gradient(90deg, ${SERIES_PREVIOUS} 0 5px, transparent 5px 9px)`,
                }}
              />
              {t('dashboard.lastPeriod')}
            </li>
          </ul>

          <div className="mt-3 h-[260px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart
                data={points}
                margin={{ top: 8, right: 8, bottom: 0, left: -18 }}
              >
                <defs>
                  <linearGradient id="studyTimeFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor={SERIES_CURRENT} stopOpacity={0.22} />
                    <stop offset="100%" stopColor={SERIES_CURRENT} stopOpacity={0} />
                  </linearGradient>
                </defs>

                {/* Lưới mờ, chỉ kẻ ngang để không cắt vụn đường dữ liệu */}
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
                  width={52}
                  tickFormatter={(value: number) =>
                    `${value}${t('common.minutesShort')}`
                  }
                />

                <Tooltip
                  content={<ChartTooltip period={period} />}
                  cursor={{ stroke: 'var(--border-strong)', strokeWidth: 1 }}
                />

                {/* Kỳ trước vẽ trước để nằm dưới, nét đứt cho dễ phân biệt
                    kể cả khi in đen trắng */}
                <Line
                  type="monotone"
                  dataKey="previousMinutes"
                  name={t('dashboard.lastPeriod')}
                  stroke={SERIES_PREVIOUS}
                  strokeWidth={2}
                  strokeDasharray="5 4"
                  dot={false}
                  activeDot={{ r: 4, strokeWidth: 2, stroke: 'var(--surface)' }}
                />

                <Area
                  type="monotone"
                  dataKey="minutes"
                  name={t('dashboard.thisPeriod')}
                  stroke={SERIES_CURRENT}
                  strokeWidth={2}
                  fill="url(#studyTimeFill)"
                  dot={false}
                  activeDot={{ r: 5, strokeWidth: 2, stroke: 'var(--surface)' }}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>

          {/* Bảng số liệu thay thế cho hình vẽ */}
          <div className="mt-2">
            <Button
              variant="link"
              icon={showTable ? 'expand_less' : 'table_rows'}
              onClick={() => setShowTable((v) => !v)}
              aria-expanded={showTable}
            >
              {showTable ? t('dashboard.hideTable') : t('dashboard.showTable')}
            </Button>

            {showTable && (
              <div className="mt-2 overflow-x-auto">
                <DataTable points={points} period={period} />
              </div>
            )}
          </div>
        </>
      )}
    </Card>
  );
};

export default StudyTimeChart;
