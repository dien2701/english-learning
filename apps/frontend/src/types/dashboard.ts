import type { Level, Skill } from './common';
import type { L10n } from './l10n';

/** Bài học đang làm dở, nguồn của nút "Tiếp tục học". */
export interface ContinueLearning {
  id: string;
  skill: Skill;
  title: L10n;
  /** Chủ đề hoặc bộ từ mà bài học thuộc về. */
  topic: L10n;
  level: Level;
  /** Phần trăm đã hoàn thành, 0–100. */
  progress: number;
  /** Ví dụ: 12 trong tổng số 40 thẻ. */
  completedItems: number;
  totalItems: number;
  coverUrl: string;
  /** Đường dẫn đưa thẳng người dùng về đúng chỗ đang học dở. */
  resumePath: string;
  lastStudiedAt: string;
}

/** Trạng thái của một bài trong danh sách đã tham gia. */
export type LessonStatus = 'IN_PROGRESS' | 'COMPLETED';

/** Bài học đã tham gia, hiển thị ở danh sách phía dưới banner. */
export interface AttendedLesson {
  id: string;
  skill: Skill;
  title: L10n;
  level: Level;
  status: LessonStatus;
  /** Phần trăm đã hoàn thành, 0–100. */
  progress: number;
  /** Điểm quy về thang 10, để trống với bài chưa chấm hoặc không chấm điểm. */
  score?: number;
  /** Lần tương tác gần nhất với bài này. */
  lastActivityAt: string;
  detailPath: string;
}

/** Một cột trên biểu đồ thời gian học. */
export interface StudyTimePoint {
  /** Nhãn trục hoành: 'T2'…'CN' với chế độ tuần, '01'…'31' với tháng. */
  /** Nhãn trục hoành; song ngữ vì "T2"/"Tuần 1" không đọc được ở chế độ EN. */
  label: L10n;
  /** Số phút học trong kỳ hiện tại. */
  minutes: number;
  /** Số phút học ở cùng vị trí của kỳ trước, dùng để so sánh. */
  previousMinutes: number;
}

export type StudyPeriod = 'WEEK' | 'MONTH';

/** Dữ liệu biểu đồ kèm phần tổng hợp so sánh. */
export interface StudyTimeChart {
  period: StudyPeriod;
  points: StudyTimePoint[];
  /** Tổng số phút của kỳ hiện tại. */
  totalMinutes: number;
  /** Tổng số phút của kỳ liền trước. */
  previousTotalMinutes: number;
  /** Chênh lệch theo phần trăm, số âm nghĩa là học ít hơn kỳ trước. */
  changePercent: number;
}

/**
 * Toàn bộ dữ liệu một lần gọi cho trang Dashboard.
 *
 * Theo yêu cầu, màn hình này cố ý giữ gọn: chỉ một bài cần học tiếp,
 * một biểu đồ thời gian và danh sách bài đã tham gia. Không có ô chỉ số
 * tổng hợp và không có mục gợi ý.
 */
export interface DashboardSummary {
  greetingName: string;
  continueLearning: ContinueLearning | null;
  attendedLessons: AttendedLesson[];
}
