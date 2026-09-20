import { http, tokenStore } from '../shared/api/client';
import type { User } from '../types/common';

/** Giữ lại đường import cũ `import type { User } from '../services/authService'`. */
export type { User };

export interface AuthSession {
  token: string;
  refreshToken: string;
  user: User;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface ResetPasswordPayload {
  email: string;
  code: string;
  password: string;
  confirmPassword: string;
}

/**
 * Mọi hàm ở đây gọi axios như với backend thật. Trong giai đoạn hiện tại
 * request bị mock adapter chặn lại (xem src/shared/api/mockAdapter.ts),
 * nên đổi sang server thật chỉ cần sửa biến môi trường VITE_USE_MOCK.
 */
export const authService = {
  login: async (payload: LoginPayload): Promise<AuthSession> => {
    const session = await http.post<AuthSession>('/auth/login', payload);
    tokenStore.set(session.token, session.refreshToken);
    return session;
  },

  register: async (payload: RegisterPayload): Promise<AuthSession> => {
    const session = await http.post<AuthSession>('/auth/register', payload);
    tokenStore.set(session.token, session.refreshToken);
    return session;
  },

  /**
   * Hỏi xem email đã có tài khoản chưa, dùng lúc người dùng rời ô email ở
   * trang đăng ký. Biết sớm thì sửa sớm, không phải điền hết form rồi mới
   * nhận lỗi.
   */
  checkEmail: (email: string): Promise<{ available: boolean }> =>
    http.get<{ available: boolean }>('/auth/check-email', { params: { email } }),

  /** Lấy lại thông tin người dùng từ token đang lưu, dùng khi tải lại trang. */
  me: (): Promise<User> => http.get<User>('/auth/me'),

  logout: async (): Promise<void> => {
    try {
      await http.delete('/auth/session');
    } finally {
      // Dù gọi server thất bại vẫn phải xoá token ở máy người dùng.
      tokenStore.clear();
    }
  },

  forgotPassword: (email: string): Promise<{ message: string }> =>
    http.post<{ message: string }>('/auth/forgot-password', { email }),

  resetPassword: (payload: ResetPasswordPayload): Promise<{ message: string }> =>
    http.post<{ message: string }>('/auth/reset-password', payload),
};

export default authService;
