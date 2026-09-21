/** Mock cho Thống kê, Hồ sơ và Cài đặt. */

import { fail, get, patch, post } from '../router';
import {
  accounts,
  findAccountByToken,
  monthPoints,
  toPublicUser,
  weekPoints,
} from '../db';
import { attemptHistory } from '../data/attempts';
import type { Skill } from '../../types/common';

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
