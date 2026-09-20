import axios from 'axios';
import type { AxiosRequestConfig, AxiosResponse } from 'axios';

import { mockAdapter } from './mockAdapter';
import { ApiError } from './types';
import type { ApiResponse } from './types';

/* -------------------------------------------------------------------
 * Cấu hình
 * ----------------------------------------------------------------- */

export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export const TOKEN_KEY = 'en_learning_token';
export const REFRESH_TOKEN_KEY = 'en_learning_refresh_token';

/* -------------------------------------------------------------------
 * Quản lý token
 * ----------------------------------------------------------------- */

export const tokenStore = {
  get: (): string | null => {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },

  set: (token: string, refreshToken?: string): void => {
    try {
      localStorage.setItem(TOKEN_KEY, token);
      if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    } catch {
      /* Chế độ ẩn danh có thể chặn localStorage — bỏ qua, phiên sẽ chỉ
         tồn tại trong bộ nhớ của tab hiện tại. */
    }
  },

  clear: (): void => {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    } catch {
      /* bỏ qua */
    }
  },
};

/* -------------------------------------------------------------------
 * Khởi tạo axios
 * ----------------------------------------------------------------- */

export const client = axios.create({
  baseURL: BASE_URL,
  timeout: 20_000,
  headers: { 'Content-Type': 'application/json' },
  ...(USE_MOCK ? { adapter: mockAdapter } : {}),
});

/** Gắn token vào mọi request nếu người dùng đã đăng nhập. */
client.interceptors.request.use((config) => {
  const token = tokenStore.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Hàm được gọi khi phiên hết hạn. AuthContext sẽ đăng ký hàm thật
 * vào đây để tránh việc lớp API phải phụ thuộc ngược vào React.
 */
let onUnauthorized: (() => void) | null = null;

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler;
}

/** Đổi mọi loại lỗi của axios về một kiểu ApiError duy nhất. */
client.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (!axios.isAxiosError(error)) {
      return Promise.reject(
        new ApiError(0, 'Đã xảy ra lỗi không xác định', 'UNKNOWN', undefined, {
          messageKey: 'errors.unknown',
        }),
      );
    }

    if (error.code === 'ECONNABORTED') {
      return Promise.reject(
        new ApiError(
          0,
          'Yêu cầu quá thời gian chờ. Vui lòng thử lại.',
          'TIMEOUT',
          undefined,
          { messageKey: 'errors.timeout' },
        ),
      );
    }

    if (!error.response) {
      return Promise.reject(
        new ApiError(
          0,
          'Không kết nối được tới máy chủ. Kiểm tra lại đường truyền.',
          'NETWORK',
          undefined,
          { messageKey: 'errors.network' },
        ),
      );
    }

    const { status, data } = error.response;
    const payload = data as
      | {
          message?: string;
          code?: string;
          fieldErrors?: Record<string, string>;
          messageKey?: string;
          fieldErrorKeys?: Record<string, string>;
        }
      | undefined;

    if (status === 401) {
      tokenStore.clear();
      onUnauthorized?.();
    }

    /* Thông báo lỗi phải đổi theo ngôn ngữ, nên ưu tiên khoá dịch do
       server trả về; không có thì suy ra từ mã trạng thái. Câu tiếng
       Việt dựng sẵn chỉ còn là lớp dự phòng cuối cùng. */
    return Promise.reject(
      new ApiError(
        status,
        payload?.message ?? defaultMessage(status),
        payload?.code ?? `HTTP_${status}`,
        payload?.fieldErrors,
        {
          messageKey: payload?.messageKey ?? defaultMessageKey(status),
          fieldErrorKeys: payload?.fieldErrorKeys,
        },
      ),
    );
  },
);

/** Khoá dịch suy ra từ mã trạng thái, dùng khi server không gửi khoá. */
function defaultMessageKey(status: number): string {
  switch (status) {
    case 400:
    case 422:
      return 'errors.badRequest';
    case 401:
      return 'errors.sessionExpired';
    case 403:
      return 'errors.forbidden';
    case 404:
      return 'errors.notFound';
    case 409:
      return 'errors.conflict';
    case 500:
      return 'errors.server';
    default:
      return 'errors.unknown';
  }
}

function defaultMessage(status: number): string {
  switch (status) {
    case 400:
      return 'Dữ liệu gửi lên không hợp lệ.';
    case 401:
      return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
    case 403:
      return 'Bạn không có quyền thực hiện thao tác này.';
    case 404:
      return 'Không tìm thấy dữ liệu yêu cầu.';
    case 409:
      return 'Dữ liệu bị trùng hoặc đang xung đột.';
    case 422:
      return 'Dữ liệu chưa hợp lệ.';
    case 500:
      return 'Máy chủ gặp sự cố. Vui lòng thử lại sau.';
    default:
      return 'Đã xảy ra lỗi. Vui lòng thử lại.';
  }
}

/* -------------------------------------------------------------------
 * Lớp bọc mỏng, đã bóc sẵn vỏ ApiResponse
 * ----------------------------------------------------------------- */

function unwrap<T>(response: AxiosResponse<ApiResponse<T>>): T {
  return response.data.data;
}

export const http = {
  get: <T>(url: string, config?: AxiosRequestConfig) =>
    client.get<ApiResponse<T>>(url, config).then(unwrap),

  post: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    client.post<ApiResponse<T>>(url, body, config).then(unwrap),

  put: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    client.put<ApiResponse<T>>(url, body, config).then(unwrap),

  patch: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    client.patch<ApiResponse<T>>(url, body, config).then(unwrap),

  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    client.delete<ApiResponse<T>>(url, config).then(unwrap),
};

export default client;
