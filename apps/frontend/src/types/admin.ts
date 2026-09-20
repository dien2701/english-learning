import type { AccountStatus, Role, Skill } from './common';
import type { L10n } from './l10n';

/** Số liệu tổng quan trên bảng điều khiển của quản trị viên. */
export interface AdminOverview {
  totalUsers: number;
  activeUsers: number;
  /** Tổng số lượt học trong 30 ngày gần nhất. */
  studySessions: number;
  totalContent: number;
}

/** Một cột trên biểu đồ người dùng đăng ký mới. */
export interface SignupPoint {
  /* Nhãn tháng song ngữ: "T4" không đọc được ở chế độ tiếng Anh. */
  label: L10n;
  count: number;
}

/** Số lượng nội dung theo từng loại. */
export interface ContentCount {
  skill: Skill;
  count: number;
}

/** Ai gây ra hoạt động; dùng khoá thay vì chuỗi để dịch được. */
export type ActivityActor = 'SYSTEM' | 'ADMIN';

export interface AdminActivity {
  id: string;
  actor: ActivityActor;
  /** Mô tả hoạt động — là nội dung nên phải song ngữ. */
  action: L10n;
  occurredAt: string;
}

export interface AdminDashboardData {
  overview: AdminOverview;
  signups: SignupPoint[];
  contentCounts: ContentCount[];
  activities: AdminActivity[];
}

/** Người dùng trong bảng quản lý. */
export interface AdminUser {
  id: string;
  fullName: string;
  email: string;
  role: Role;
  status: AccountStatus;
  createdAt: string;
  lastActiveAt: string;
  /** Tổng số bài đã hoàn thành. */
  completedLessons: number;
}

/** Trạng thái hoạt động của một nội dung. */
export type ContentStatus = 'ACTIVE' | 'INACTIVE';

/** Bản ghi nội dung trong các bảng quản lý. */
export interface AdminContentItem {
  id: string;
  title: L10n;
  skill: Skill;
  topicName: L10n;
  level: string;
  status: ContentStatus;
  /** Số câu hỏi hoặc số thẻ, tuỳ loại nội dung. */
  itemCount: number;
  updatedAt: string;
  /**
   * true khi nội dung đã xuất hiện trong lịch sử học của người dùng.
   * Những nội dung này không được xoá, chỉ được ngừng hoạt động.
   */
  inUse: boolean;
}

export type NotificationStatus = 'DRAFT' | 'SENT';

/** Thứ tự cố định, dùng để dựng danh sách chọn nhóm người nhận. */
export const AUDIENCE_KEYS = ['ALL', 'ACTIVE', 'INACTIVE', 'ADMIN'] as const;

export type AudienceKey = (typeof AUDIENCE_KEYS)[number];

export interface AdminNotification {
  id: string;
  title: string;
  content: string;
  audience: AudienceKey;
  status: NotificationStatus;
  /** Số người nhận, chỉ có khi đã gửi. */
  recipientCount?: number;
  createdAt: string;
  sentAt?: string;
}

export type QuestionType = 'MULTIPLE_CHOICE' | 'FILL_BLANK';

export interface AdminQuestion {
  id?: string;
  type: QuestionType;
  content: string;
  options?: string[];
  correctAnswers: string[];
}

/** Payload dùng chung khi thêm/sửa nội dung. */
export interface AdminContentPayload {
  skill: Skill;
  title: L10n;
  topicName?: L10n; // Flashcard dùng topicName
  level: string;
  prompt?: string; // Đề bài chung cho bài tập (nếu có)
  mediaUrl?: string; // Audio/Video URL
  contentBody?: string; // Đoạn văn / Transcript
  items: any[]; // Dữ liệu con tuỳ thuộc vào kỹ năng (từ vựng, câu hỏi nghe/đọc...)
}

