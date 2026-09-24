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
  ListeningAudio,
  AudioSource,
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

/** Tên chủ đề song ngữ; `nameEn` trống thì BE dùng lại tên tiếng Việt. */
export interface TopicNameInput {
  nameVi: string;
  nameEn?: string;
}

/** `audioSource` chỉ có ở bài nghe đã có audio do TTS hoặc tải lên. */
export type AdminContentDetail = AdminContentItem & {
  payload: AdminContentPayload;
  audioSource?: AudioSource | null;
};

export const adminService = {
  dashboard: (): Promise<AdminDashboardData> =>
    http.get<AdminDashboardData>('/admin/dashboard'),

  /* --- Người dùng --- */

  listUsers: (query: AdminListQuery = {}): Promise<Page<AdminUser>> =>
    http.get<Page<AdminUser>>('/admin/users', { params: clean(query) }),

  getUser: (id: string): Promise<AdminUser> => http.get<AdminUser>(`/admin/users/${id}`),

  deleteUser: (id: string): Promise<void> => http.delete<void>(`/admin/users/${id}`),

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

  getContent: (id: string): Promise<AdminContentDetail> =>
    http.get<AdminContentDetail>(`/admin/content/${id}`),

  createContent: (payload: AdminContentPayload): Promise<AdminContentItem> =>
    http.post<AdminContentItem>('/admin/content', payload),

  updateContent: (id: string, payload: AdminContentPayload): Promise<AdminContentItem> =>
    http.put<AdminContentItem>(`/admin/content/${id}`, payload),

  /* --- Audio bài nghe --- */

  generateListeningAudio: (id: string): Promise<ListeningAudio> =>
    http.post<ListeningAudio>(`/admin/listening/${id}/audio/generate`, undefined, { timeout: 180_000 }),

  uploadListeningAudio: (id: string, file: File): Promise<ListeningAudio> => {
    const body = new FormData();
    body.append('file', file);
    return http.post<ListeningAudio>(`/admin/listening/${id}/audio`, body, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60_000,
    });
  },

  removeListeningAudio: (id: string): Promise<ListeningAudio> =>
    http.delete<ListeningAudio>(`/admin/listening/${id}/audio`),

  /* --- Chủ đề --- */

  listTopics: (): Promise<Topic[]> => http.get<Topic[]>('/admin/topics'),

  createTopic: (name: TopicNameInput): Promise<Topic> =>
    http.post<Topic>('/admin/topics', name),

  updateTopic: (id: string, name: TopicNameInput): Promise<Topic> =>
    http.put<Topic>(`/admin/topics/${id}`, name),

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
