import { http } from '../shared/api/client';
import type { Page } from '../shared/api/types';
import type {
  AdminContentItem,
  AdminDashboardData,
  AdminNotification,
  AdminUser,
  AudienceKey,
  ContentStatus,
  AdminContentPayload,
} from '../types/admin';
import type { AccountStatus, Role } from '../types/common';
import type { Topic } from '../types/practice';

export interface AdminListQuery {
  search?: string;
  role?: string;
  status?: string;
  skill?: string;
  page?: number;
  pageSize?: number;
}

function clean(query: AdminListQuery): Record<string, string | number> {
  const result: Record<string, string | number> = {};
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== '' && value !== null) result[key] = value;
  }
  return result;
}

export const adminService = {
  dashboard: (): Promise<AdminDashboardData> =>
    http.get<AdminDashboardData>('/admin/dashboard'),

  /* --- Người dùng --- */

  listUsers: (query: AdminListQuery = {}): Promise<Page<AdminUser>> =>
    http.get<Page<AdminUser>>('/admin/users', { params: clean(query) }),

  getUser: (id: string): Promise<AdminUser> => http.get<AdminUser>(`/admin/users/${id}`),

  updateUser: (
    id: string,
    payload: { status?: AccountStatus; role?: Role },
  ): Promise<AdminUser> => http.patch<AdminUser>(`/admin/users/${id}`, payload),

  /* --- Nội dung --- */

  listContent: (query: AdminListQuery = {}): Promise<Page<AdminContentItem>> =>
    http.get<Page<AdminContentItem>>('/admin/content', { params: clean(query) }),

  setContentStatus: (id: string, status: ContentStatus) =>
    http.patch<{ id: string; status: ContentStatus }>(`/admin/content/${id}`, { status }),

  /** Bị từ chối với nội dung đang nằm trong lịch sử học của người dùng. */
  deleteContent: (id: string) =>
    http.delete<{ deleted: boolean }>(`/admin/content/${id}`),

  getContent: (id: string): Promise<AdminContentItem & { payload: AdminContentPayload }> =>
    http.get<AdminContentItem & { payload: AdminContentPayload }>(`/admin/content/${id}`),

  createContent: (payload: AdminContentPayload): Promise<AdminContentItem> =>
    http.post<AdminContentItem>('/admin/content', payload),

  updateContent: (id: string, payload: AdminContentPayload): Promise<AdminContentItem> =>
    http.put<AdminContentItem>(`/admin/content/${id}`, payload),

  /* --- Chủ đề --- */

  listTopics: (): Promise<Topic[]> => http.get<Topic[]>('/admin/topics'),

  createTopic: (name: string): Promise<Topic> =>
    http.post<Topic>('/admin/topics', { name }),

  updateTopic: (id: string, name: string): Promise<Topic> =>
    http.put<Topic>(`/admin/topics/${id}`, { name }),

  deleteTopic: (id: string): Promise<{ deleted: boolean }> =>
    http.delete<{ deleted: boolean }>(`/admin/topics/${id}`),

  /* --- Thông báo --- */

  listNotifications: (page = 1): Promise<Page<AdminNotification>> =>
    http.get<Page<AdminNotification>>('/admin/notifications', { params: { page } }),

  createNotification: (payload: {
    title: string;
    content: string;
    audience: AudienceKey;
    send: boolean;
  }): Promise<AdminNotification> =>
    http.post<AdminNotification>('/admin/notifications', payload),

  sendNotification: (id: string): Promise<AdminNotification> =>
    http.post<AdminNotification>(`/admin/notifications/${id}/send`),
};

export default adminService;
