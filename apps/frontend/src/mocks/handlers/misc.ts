/** Mock cho Thống kê, Thông báo của người dùng, Hồ sơ và Cài đặt. */

import { fail, get, num, paginate, patch, post } from '../router';
import {
  accounts,
  daysAgo,
  findAccountByToken,
  hoursAgo,
  minutesAgo,
  monthPoints,
  toPublicUser,
  weekPoints,
} from '../db';
import { attemptHistory } from '../data/attempts';
import type { Skill } from '../../types/common';
import type { L10n } from '../../types/l10n';

/* -------------------------------------------------------------------
 * Thống kê
 * ----------------------------------------------------------------- */

export interface SkillStat {
  skill: Skill;
  /** Điểm trung bình trên thang 10. */
  averageScore: number;
  attempts: number;
  /** Chênh lệch điểm so với kỳ trước. */
  change: number;
}

get('/statistics/overview', () => ({
  totalMinutes: weekPoints.reduce((s, p) => s + p.minutes, 0),
  totalMinutesPrevious: weekPoints.reduce((s, p) => s + p.previousMinutes, 0),
  weekPoints,
  monthPoints,
  skills: [
    { skill: 'VOCABULARY', averageScore: 8.4, attempts: 26, change: 0.6 },
    { skill: 'LISTENING', averageScore: 7.1, attempts: 14, change: -0.4 },
    { skill: 'READING', averageScore: 7.8, attempts: 11, change: 0.3 },
    { skill: 'WRITING', averageScore: 7.0, attempts: 6, change: 0.5 },
    { skill: 'SPEAKING', averageScore: 6.8, attempts: 4, change: 0.2 },
    { skill: 'EXAM', averageScore: 8.2, attempts: 3, change: 0.8 },
  ] satisfies SkillStat[],
  recentAttempts: attemptHistory.slice(0, 8),
}));

/* -------------------------------------------------------------------
 * Thông báo của người dùng
 * ----------------------------------------------------------------- */

export interface UserNotification {
  id: string;
  icon: string;
  /* Song ngữ như mọi nội dung khác — xem mục 5 của STYLEGUIDE. */
  title: L10n;
  body: L10n;
  path: string;
  createdAt: string;
  isRead: boolean;
}

const userNotifications: UserNotification[] = [
  {
    id: 'un-1',
    icon: 'style',
    title: {
      vi: 'Nhắc ôn từ vựng',
      en: 'Vocabulary review reminder',
    },
    body: {
      vi: 'Bạn có 20 từ đến hạn ôn tập hôm nay trong bộ TOEIC 600.',
      en: 'You have 20 words due for review today in the TOEIC 600 deck.',
    },
    path: '/flashcard/deck-toeic-600',
    createdAt: minutesAgo(5),
    isRead: false,
  },
  {
    id: 'un-2',
    icon: 'edit_note',
    title: {
      vi: 'AI đã chấm xong bài viết',
      en: 'The AI has graded your essay',
    },
    body: {
      vi: 'Bài "Viết email xin nghỉ phép" đạt 7.0 điểm. Xem nhận xét chi tiết.',
      en: 'Your essay "Write a leave request email" scored 7.0. See the full feedback.',
    },
    path: '/writing/history',
    createdAt: hoursAgo(2),
    isRead: false,
  },
  {
    id: 'un-3',
    icon: 'quiz',
    title: {
      vi: 'Kết quả bài kiểm tra',
      en: 'Test results',
    },
    body: {
      vi: 'Kiểm tra tổng hợp giữa khoá: 8.2 điểm, đúng 41/50 câu.',
      en: 'Mid-course general test: 8.2 points, 41 of 50 answers correct.',
    },
    path: '/exam/history',
    createdAt: hoursAgo(20),
    isRead: false,
  },
  {
    id: 'un-4',
    icon: 'campaign',
    title: {
      vi: 'Bộ đề TOEIC mới đã sẵn sàng',
      en: 'New TOEIC test set is ready',
    },
    body: {
      vi: 'Ba bộ đề mô phỏng TOEIC vừa được thêm vào kho nội dung.',
      en: 'Three TOEIC practice tests have been added to the library.',
    },
    path: '/exam',
    createdAt: daysAgo(5),
    isRead: true,
  },
  {
    id: 'un-5',
    icon: 'headphones',
    title: {
      vi: 'Gợi ý luyện nghe',
      en: 'Listening suggestion',
    },
    body: {
      vi: 'Bạn hay mất điểm ở câu hỏi về số liệu. Thử bài nghe chuyên đề này.',
      en: 'You often lose points on number questions. Try this focused lesson.',
    },
    path: '/listening/ls-030',
    createdAt: daysAgo(7),
    isRead: true,
  },
];

get('/notifications', ({ query }) =>
  paginate(userNotifications, num(query, 'page', 1), num(query, 'pageSize', 20)),
);

get('/notifications/unread-count', () => ({
  count: userNotifications.filter((n) => !n.isRead).length,
}));

patch('/notifications/:id/read', ({ params }) => {
  const notification = userNotifications.find((n) => n.id === params.id);
  if (!notification) fail(404, 'errors.notificationNotFound', 'NOT_FOUND');
  notification.isRead = true;
  return notification;
});

post('/notifications/read-all', () => {
  userNotifications.forEach((n) => {
    n.isRead = true;
  });
  return { count: userNotifications.length };
});

/* -------------------------------------------------------------------
 * Hồ sơ và cài đặt
 * ----------------------------------------------------------------- */

export interface UserSettings {
  language: 'vi' | 'en';
  theme: 'light' | 'dark';
  emailReminders: boolean;
  /** Giờ gửi nhắc học hằng ngày, định dạng HH:mm. */
  reminderTime: string;
  dailyGoalMinutes: number;
}

const settings: UserSettings = {
  language: 'vi',
  theme: 'light',
  emailReminders: true,
  reminderTime: '20:00',
  dailyGoalMinutes: 30,
};

get('/profile', ({ token }) => {
  const account = findAccountByToken(token);
  if (!account) fail(401, 'errors.sessionExpired', 'UNAUTHORIZED');

  return {
    ...toPublicUser(account),
    joinedAt: account.createdAt,
    totalMinutes: 1287,
    completedLessons: 37,
    masteredWords: 486,
  };
});

patch('/profile', ({ token, body }) => {
  const account = findAccountByToken(token);
  if (!account) fail(401, 'errors.sessionExpired', 'UNAUTHORIZED');

  const { fullName, email, phoneNumber, avatarUrl } = (body ?? {}) as { fullName?: string; email?: string; phoneNumber?: string; avatarUrl?: string };

  const fieldErrors: Record<string, string> = {};
  if (fullName !== undefined && !fullName.trim()) {
    fieldErrors.fullName = 'auth.validation.nameRequired';
  }
  if (email !== undefined) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      fieldErrors.email = 'auth.validation.emailFormat';
    } else if (
      accounts.some(
        (a) => a.id !== account.id && a.email.toLowerCase() === email.toLowerCase(),
      )
    ) {
      fieldErrors.email = 'Email này đã được dùng cho tài khoản khác.';
    }
  }

  if (Object.keys(fieldErrors).length > 0) {
    fail(400, 'errors.checkInfo', 'VALIDATION', fieldErrors);
  }

  if (fullName) account.fullName = fullName.trim();
  if (email) account.email = email.toLowerCase();
  if (phoneNumber !== undefined) account.phoneNumber = phoneNumber;
  if (avatarUrl !== undefined) account.avatarUrl = avatarUrl;

  return toPublicUser(account);
});

post('/profile/password', ({ token, body }) => {
  const account = findAccountByToken(token);
  if (!account) fail(401, 'errors.sessionExpired', 'UNAUTHORIZED');

  const { currentPassword, newPassword } = (body ?? {}) as {
    currentPassword?: string;
    newPassword?: string;
  };

  if (account.password !== currentPassword) {
    fail(400, 'errors.wrongPassword', 'WRONG_PASSWORD', {
      currentPassword: 'errors.wrongPassword',
    });
  }

  if (!newPassword || newPassword.length < 6) {
    fail(400, 'errors.newPasswordInvalid', 'VALIDATION', {
      newPassword: 'auth.validation.passwordMin',
    });
  }

  account.password = newPassword;
  return { message: 'Đã đổi mật khẩu thành công.' };
});

get('/settings', (): UserSettings => settings);

patch('/settings', ({ body }): UserSettings => {
  Object.assign(settings, (body ?? {}) as Partial<UserSettings>);
  return settings;
});
