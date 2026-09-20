import { http } from '../shared/api/client';
import type { L10n } from '../types/l10n';
import type { Page } from '../shared/api/types';
import type { User, Skill } from '../types/common';
import type { AttemptHistoryItem } from '../types/practice';
import type { StudyTimePoint } from '../types/dashboard';

export interface SkillStat {
  skill: Skill;
  averageScore: number;
  attempts: number;
  change: number;
}

export interface StatisticsOverview {
  totalMinutes: number;
  totalMinutesPrevious: number;
  weekPoints: StudyTimePoint[];
  monthPoints: StudyTimePoint[];
  skills: SkillStat[];
  recentAttempts: AttemptHistoryItem[];
}

export interface UserNotification {
  id: string;
  icon: string;
  /* Thông báo do hệ thống sinh ra nên là nội dung song ngữ, giống tên bộ
     từ hay tiêu đề bài học — xem mục 5 của STYLEGUIDE. */
  title: L10n;
  body: L10n;
  path: string;
  createdAt: string;
  isRead: boolean;
}

export interface ProfileData extends User {
  joinedAt: string;
  totalMinutes: number;
  completedLessons: number;
  masteredWords: number;
}

export interface UserSettings {
  language: 'vi' | 'en';
  theme: 'light' | 'dark';
  emailReminders: boolean;
  reminderTime: string;
  dailyGoalMinutes: number;
}

export const statisticsService = {
  overview: (): Promise<StatisticsOverview> =>
    http.get<StatisticsOverview>('/statistics/overview'),
};

export const notificationService = {
  list: (page = 1): Promise<Page<UserNotification>> =>
    http.get<Page<UserNotification>>('/notifications', { params: { page } }),

  unreadCount: (): Promise<{ count: number }> =>
    http.get<{ count: number }>('/notifications/unread-count'),

  markRead: (id: string): Promise<UserNotification> =>
    http.patch<UserNotification>(`/notifications/${id}/read`),

  markAllRead: (): Promise<{ count: number }> =>
    http.post<{ count: number }>('/notifications/read-all'),
};

export const profileService = {
  get: (): Promise<ProfileData> => http.get<ProfileData>('/profile'),

  update: (payload: { fullName?: string; email?: string }): Promise<User> =>
    http.patch<User>('/profile', payload),

  changePassword: (payload: {
    currentPassword: string;
    newPassword: string;
  }): Promise<{ message: string }> =>
    http.post<{ message: string }>('/profile/password', payload),

  getSettings: (): Promise<UserSettings> => http.get<UserSettings>('/settings'),

  updateSettings: (payload: Partial<UserSettings>): Promise<UserSettings> =>
    http.patch<UserSettings>('/settings', payload),
};
