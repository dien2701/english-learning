/** Mock cho trang Dashboard (chức năng 2). */

import { get } from '../router';
import {
  attendedLessons,
  continueLearning,
  findAccountByToken,
  monthPoints,
  weekPoints,
} from '../db';
import type {
  DashboardSummary,
  StudyPeriod,
  StudyTimeChart,
} from '../../types/dashboard';

/* --- GET /dashboard/summary ---------------------------------------- */

get('/dashboard/summary', ({ token }): DashboardSummary => {
  const account = findAccountByToken(token);

  // Lấy tên gọi thân mật: chữ cuối trong họ tên đầy đủ.
  const fullName = account?.fullName ?? 'bạn';
  const greetingName = fullName.trim().split(/\s+/).pop() ?? fullName;

  return {
    greetingName,
    continueLearning,
    attendedLessons,
  };
});

/* --- GET /dashboard/study-time?period=WEEK|MONTH -------------------- */

get('/dashboard/study-time', ({ query }): StudyTimeChart => {
  const raw = (query.get('period') ?? 'WEEK').toUpperCase();
  const period: StudyPeriod = raw === 'MONTH' ? 'MONTH' : 'WEEK';

  const points = period === 'MONTH' ? monthPoints : weekPoints;

  const totalMinutes = points.reduce((sum, p) => sum + p.minutes, 0);
  const previousTotalMinutes = points.reduce(
    (sum, p) => sum + p.previousMinutes,
    0,
  );

  const changePercent =
    previousTotalMinutes === 0
      ? 0
      : Math.round(
          ((totalMinutes - previousTotalMinutes) / previousTotalMinutes) * 100,
        );

  return {
    period,
    points,
    totalMinutes,
    previousTotalMinutes,
    changePercent,
  };
});
