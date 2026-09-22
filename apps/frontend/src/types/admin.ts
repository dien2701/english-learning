import type { AccountStatus, Level, Role, Skill } from './common';
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
  /** Vắng khi tài khoản chưa từng hoạt động. */
  lastActiveAt?: string;
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
  /** Vắng với đề kiểm tra (không thuộc chủ đề). */
  topicName?: L10n;
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

/** Loại câu hỏi phía BE: trắc nghiệm đúng một đáp án, hoặc điền từ. */
export type QuestionKindInput = 'SINGLE_CHOICE' | 'FILL_BLANK';

export interface AdminOptionInput {
  content: string;
  correct: boolean;
}

/** Câu hỏi Nghe/Đọc/Kiểm tra. `skill` chỉ cần cho đề kiểm tra. */
export interface AdminQuestionInput {
  id?: string;
  skill?: Skill;
  kind: QuestionKindInput;
  content: string;
  explanation?: string;
  options?: AdminOptionInput[];
  acceptedAnswers?: string[];
}

export type AudioSource = 'TTS' | 'UPLOAD';

/** Trạng thái audio của bài nghe (`/admin/listening/:id/audio`); `audioUrl`/`audioSource` null khi chưa có. */
export interface ListeningAudio {
  id: string;
  audioUrl: string | null;
  audioSource: AudioSource | null;
  durationSeconds: number;
}

export interface AdminCardInput {
  id?: string;
  word: string;
  phonetic?: string;
  meaningVi: string;
  meaningEn?: string;
  partOfSpeechVi?: string;
  partOfSpeechEn?: string;
  example?: string;
  exampleMeaning?: string;
  imageUrl?: string;
  audioUrl?: string;
}

export interface AdminPromptInput {
  id?: string;
  text: string;
  phonetic?: string;
  meaningVi?: string;
}

interface ContentPayloadBase {
  titleVi: string;
  titleEn?: string;
  level: Level;
}

interface TopicalPayload extends ContentPayloadBase {
  topicId: string;
}

interface DescribedPayload {
  descriptionVi?: string;
  descriptionEn?: string;
}

/**
 * Body POST/PUT `/admin/content`: kiểu đa hình theo `skill`, khớp BE (`AdminContentRequest`).
 * PUT thay toàn bộ; phần tử con có `id` thì sửa tại chỗ, không có thì tạo mới, vắng mặt thì bị xoá.
 */
export type AdminContentPayload =
  | (TopicalPayload & DescribedPayload & { skill: 'VOCABULARY'; coverImageUrl?: string; cards: AdminCardInput[] })
  | (TopicalPayload &
      DescribedPayload & {
        skill: 'LISTENING';
        audioUrl?: string;
        durationSeconds?: number;
        transcript: string;
        questions: AdminQuestionInput[];
      })
  | (TopicalPayload &
      DescribedPayload & {
        skill: 'READING';
        timeLimitMinutes?: number;
        paragraphs: string[];
        questions: AdminQuestionInput[];
      })
  | (TopicalPayload & {
      skill: 'WRITING';
      instructions: string;
      suggestedMinutes?: number;
      minWords?: number;
      hints?: string[];
    })
  | (TopicalPayload & DescribedPayload & { skill: 'SPEAKING'; prompts: AdminPromptInput[] })
  | (ContentPayloadBase &
      DescribedPayload & { skill: 'EXAM'; timeLimitMinutes?: number; questions: AdminQuestionInput[] });
