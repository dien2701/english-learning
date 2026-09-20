/**
 * Kho dữ liệu giả lập, giữ trong bộ nhớ.
 *
 * Dữ liệu được sinh tương đối theo thời điểm hiện tại để màn hình lúc nào
 * cũng trông như vừa có hoạt động mới. Handler được phép sửa trực tiếp các
 * mảng ở đây; thay đổi sẽ mất khi tải lại trang, đúng như mong đợi của mock.
 */

import { decks } from './data/flashcards';
import type { L10n } from '../types/l10n';
import type { User } from '../types/common';
import type {
  AttendedLesson,
  ContinueLearning,
  StudyTimePoint,
} from '../types/dashboard';

/* -------------------------------------------------------------------
 * Tiện ích thời gian
 * ----------------------------------------------------------------- */

const MINUTE = 60 * 1000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

export const minutesAgo = (n: number): string =>
  new Date(Date.now() - n * MINUTE).toISOString();

export const hoursAgo = (n: number): string =>
  new Date(Date.now() - n * HOUR).toISOString();

export const daysAgo = (n: number): string =>
  new Date(Date.now() - n * DAY).toISOString();

/* -------------------------------------------------------------------
 * Tài khoản
 * ----------------------------------------------------------------- */

/** Mật khẩu dùng chung cho mọi tài khoản mẫu. */
export const DEMO_PASSWORD = '123456';

export interface MockAccount extends User {
  password: string;
}

export const accounts: MockAccount[] = [
  {
    id: 'u-001',
    email: 'hocvien@enlearning.vn',
    password: DEMO_PASSWORD,
    fullName: 'Trịnh Xuân Diện',
    role: 'USER',
    status: 'ACTIVE',
    createdAt: daysAgo(96),
  },
  {
    id: 'u-002',
    email: 'admin@enlearning.vn',
    password: DEMO_PASSWORD,
    fullName: 'Quản trị viên',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdAt: daysAgo(240),
  },
  {
    id: 'u-003',
    email: 'khoa@enlearning.vn',
    password: DEMO_PASSWORD,
    fullName: 'Nguyễn Đăng Khoa',
    role: 'USER',
    status: 'LOCKED',
    createdAt: daysAgo(48),
  },
];

/**
 * Dựng đối tượng trả ra cho giao diện.
 *
 * Liệt kê từng trường thay vì loại bỏ `password` khỏi bản sao: nếu sau này
 * MockAccount có thêm trường nhạy cảm, cách này vẫn không để lọt ra ngoài.
 */
export function toPublicUser(account: MockAccount): User {
  return {
    id: account.id,
    email: account.email,
    fullName: account.fullName,
    avatarUrl: account.avatarUrl,
    role: account.role,
    status: account.status,
    createdAt: account.createdAt,
  };
}

/**
 * Token giả lập có dạng `mock.<userId>.<chuỗi ngẫu nhiên>`.
 *
 * Id người dùng nằm ngay trong token, giống cách JWT mang sẵn phần
 * payload, nên phiên vẫn còn hiệu lực sau khi tải lại trang dù kho dữ
 * liệu này nằm trong bộ nhớ. Nếu tra cứu qua một Map thì mỗi lần F5 sẽ
 * bị đá về màn hình đăng nhập.
 */
export function issueToken(userId: string): string {
  return `mock.${userId}.${Math.random().toString(36).slice(2, 10)}`;
}

/** Các token đã đăng xuất, thay cho danh sách đen trên Redis. */
export const revokedTokens = new Set<string>();

export function findAccountByToken(token?: string): MockAccount | undefined {
  if (!token || revokedTokens.has(token)) return undefined;

  const [prefix, userId] = token.split('.');
  if (prefix !== 'mock' || !userId) return undefined;

  return accounts.find((a) => a.id === userId);
}

/** Mã đặt lại mật khẩu đã phát hành: email → mã. */
export const resetCodes = new Map<string, string>();

/* -------------------------------------------------------------------
 * Dữ liệu Dashboard
 * ----------------------------------------------------------------- */

/* Bài đang học dở lấy thẳng số liệu từ bộ thẻ tương ứng. Viết số tay ở
   đây từng khiến Dashboard và trang Flashcard báo hai con số khác nhau. */
const activeDeck = decks.find((d) => d.id === 'deck-toeic-600')!;

export const continueLearning: ContinueLearning = {
  id: activeDeck.id,
  skill: 'VOCABULARY',
  title: activeDeck.title,
  topic: activeDeck.topicName,
  level: activeDeck.level,
  progress: activeDeck.progressPercent,
  completedItems: activeDeck.learnedCards,
  totalItems: activeDeck.totalCards,
  coverUrl: activeDeck.coverImageUrl ?? '',
  resumePath: `/flashcard/${activeDeck.id}/study`,
  lastStudiedAt: hoursAgo(5),
};

/** Danh sách bài đã tham gia, gồm cả bài đang dở lẫn bài đã xong. */
export const attendedLessons: AttendedLesson[] = [
  {
    id: activeDeck.id,
    skill: 'VOCABULARY',
    title: activeDeck.title,
    level: activeDeck.level,
    status: 'IN_PROGRESS',
    progress: activeDeck.progressPercent,
    lastActivityAt: hoursAgo(5),
    detailPath: `/flashcard/${activeDeck.id}`,
  },
  {
    id: 'ex-005',
    skill: 'EXAM',
    title: { vi: 'Kiểm tra tổng hợp giữa khoá', en: 'Mid-course Mixed Test' },
    level: 'INTERMEDIATE',
    status: 'COMPLETED',
    progress: 100,
    score: 8.2,
    lastActivityAt: hoursAgo(20),
    detailPath: '/exam/ex-005/result',
  },
  {
    id: 'ls-021',
    skill: 'LISTENING',
    title: { vi: 'Hội thoại nơi công sở — Part 3', en: 'Workplace Conversation — Part 3' },
    level: 'INTERMEDIATE',
    status: 'COMPLETED',
    progress: 100,
    score: 8.5,
    lastActivityAt: daysAgo(1),
    detailPath: '/listening/ls-021/result',
  },
  {
    id: 'wr-014',
    skill: 'WRITING',
    title: { vi: 'Viết email xin nghỉ phép', en: 'Write a Leave Request Email' },
    level: 'BEGINNER',
    status: 'COMPLETED',
    progress: 100,
    score: 7.0,
    lastActivityAt: daysAgo(2),
    detailPath: '/writing/wr-014/result',
  },
  {
    id: 'sp-003',
    skill: 'SPEAKING',
    title: { vi: 'Luyện nói: Giới thiệu bản thân', en: 'Speaking: Introducing Yourself' },
    level: 'BEGINNER',
    status: 'COMPLETED',
    progress: 100,
    score: 6.8,
    lastActivityAt: daysAgo(2),
    detailPath: '/speaking/sp-003/result',
  },
  {
    id: 'rd-008',
    skill: 'READING',
    title: { vi: 'Thói quen của người thành công', en: 'The Habits of Productive People' },
    level: 'ADVANCED',
    status: 'COMPLETED',
    progress: 100,
    score: 6.5,
    lastActivityAt: daysAgo(4),
    detailPath: '/reading/rd-008/result',
  },
  {
    id: 'deck-ielts-food',
    skill: 'VOCABULARY',
    title: { vi: 'IELTS — Chủ đề Ẩm thực', en: 'IELTS — Food and Cooking' },
    level: 'INTERMEDIATE',
    status: 'IN_PROGRESS',
    progress: 67,
    lastActivityAt: daysAgo(3),
    detailPath: '/flashcard/deck-ielts-food',
  },
];

/* -------------------------------------------------------------------
 * Biểu đồ thời gian học
 * ----------------------------------------------------------------- */

const WEEK_LABELS: L10n[] = [
  { vi: 'T2', en: 'Mon' },
  { vi: 'T3', en: 'Tue' },
  { vi: 'T4', en: 'Wed' },
  { vi: 'T5', en: 'Thu' },
  { vi: 'T6', en: 'Fri' },
  { vi: 'T7', en: 'Sat' },
  { vi: 'CN', en: 'Sun' },
];

const WEEK_CURRENT = [35, 52, 28, 64, 45, 80, 30];
const WEEK_PREVIOUS = [40, 38, 45, 30, 55, 42, 48];

export const weekPoints: StudyTimePoint[] = WEEK_LABELS.map((label, i) => ({
  label,
  minutes: WEEK_CURRENT[i],
  previousMinutes: WEEK_PREVIOUS[i],
}));

/** Chế độ tháng gom theo tuần cho dễ đọc hơn là vẽ 30 cột. */
const MONTH_LABELS: L10n[] = [
  { vi: 'Tuần 1', en: 'Week 1' },
  { vi: 'Tuần 2', en: 'Week 2' },
  { vi: 'Tuần 3', en: 'Week 3' },
  { vi: 'Tuần 4', en: 'Week 4' },
];
const MONTH_CURRENT = [246, 318, 275, 334];
const MONTH_PREVIOUS = [280, 254, 298, 262];

export const monthPoints: StudyTimePoint[] = MONTH_LABELS.map((label, i) => ({
  label,
  minutes: MONTH_CURRENT[i],
  previousMinutes: MONTH_PREVIOUS[i],
}));
