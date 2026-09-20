import { http } from '../shared/api/client';
import type {
  DashboardSummary,
  StudyPeriod,
  StudyTimeChart,
} from '../types/dashboard';

export const dashboardService = {
  /** Toàn bộ nội dung trang Dashboard trong một lần gọi. */
  getSummary: (): Promise<DashboardSummary> =>
    http.get<DashboardSummary>('/dashboard/summary'),

  /** Biểu đồ thời gian học, gọi lại mỗi khi đổi giữa tuần và tháng. */
  getStudyTime: (period: StudyPeriod): Promise<StudyTimeChart> =>
    http.get<StudyTimeChart>('/dashboard/study-time', { params: { period } }),
};

export default dashboardService;
