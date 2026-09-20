/**
 * Bộ định tuyến nhỏ cho mock API.
 *
 * Mục tiêu: các handler giả lập được viết gần giống controller thật
 * (khớp method + đường dẫn, có tham số động, có query, có body),
 * nhờ đó khi thay bằng backend Spring Boot thì chỉ cần tắt mock,
 * không phải sửa bất kỳ dòng nào ở tầng service hay giao diện.
 */

import { ApiError } from '../shared/api/types';
import type { L10n } from '../types/l10n';

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface MockContext {
  /** Tham số động trên đường dẫn, ví dụ /decks/:id → { id: '3' }. */
  params: Record<string, string>;
  /** Tham số query, đã giải mã sẵn. */
  query: URLSearchParams;
  /** Body đã parse từ JSON, hoặc undefined nếu không có. */
  body: unknown;
  /** Token lấy từ header Authorization, không kèm tiền tố Bearer. */
  token?: string;
}

export type MockHandler = (ctx: MockContext) => unknown | Promise<unknown>;

interface Route {
  method: HttpMethod;
  /** Các đoạn của đường dẫn, đoạn bắt đầu bằng ':' là tham số động. */
  segments: string[];
  handler: MockHandler;
}

const routes: Route[] = [];

/** Đăng ký một route giả lập. */
export function route(
  method: HttpMethod,
  path: string,
  handler: MockHandler,
): void {
  routes.push({
    method,
    segments: normalize(path).split('/'),
    handler,
  });
}

/** Các hàm rút gọn cho dễ đọc khi khai báo handler. */
export const get = (path: string, h: MockHandler) => route('GET', path, h);
export const post = (path: string, h: MockHandler) => route('POST', path, h);
export const put = (path: string, h: MockHandler) => route('PUT', path, h);
export const patch = (path: string, h: MockHandler) => route('PATCH', path, h);
export const del = (path: string, h: MockHandler) => route('DELETE', path, h);

function normalize(path: string): string {
  return path.replace(/^\/+|\/+$/g, '');
}

export interface MatchedRoute {
  handler: MockHandler;
  params: Record<string, string>;
}

/** Tìm route khớp với method và đường dẫn. Trả về null nếu không có. */
export function matchRoute(
  method: string,
  pathname: string,
): MatchedRoute | null {
  const target = normalize(pathname).split('/');
  const upper = method.toUpperCase();

  for (const r of routes) {
    if (r.method !== upper) continue;
    if (r.segments.length !== target.length) continue;

    const params: Record<string, string> = {};
    let ok = true;

    for (let i = 0; i < r.segments.length; i++) {
      const seg = r.segments[i];
      if (seg.startsWith(':')) {
        params[seg.slice(1)] = decodeURIComponent(target[i]);
      } else if (seg !== target[i]) {
        ok = false;
        break;
      }
    }

    if (ok) return { handler: r.handler, params };
  }

  return null;
}

/** Số route đã đăng ký — dùng để ghi log lúc khởi động. */
export function routeCount(): number {
  return routes.length;
}

/* -------------------------------------------------------------------
 * Tiện ích cho handler
 * ----------------------------------------------------------------- */

/**
 * Ném lỗi có mã trạng thái, adapter sẽ đổi thành ApiError tương ứng.
 *
 * Tham số thứ hai là **khoá dịch** (`errors.emailTaken`) chứ không phải
 * câu tiếng Việt: lỗi phải đổi theo ngôn ngữ đang chọn, mà mock nằm ngoài
 * cây React nên không gọi được `t()`. Tầng giao diện dịch lại bằng
 * `useApiError()`. Các giá trị trong `fieldErrorKeys` cũng là khoá dịch.
 */
export function fail(
  status: number,
  messageKey: string,
  code = 'MOCK_ERROR',
  fieldErrorKeys?: Record<string, string>,
): never {
  throw new ApiError(status, messageKey, code, undefined, {
    messageKey,
    fieldErrorKeys,
  });
}

/** Đọc số từ query, có giá trị mặc định khi thiếu hoặc không hợp lệ. */
export function num(
  query: URLSearchParams,
  key: string,
  fallback: number,
): number {
  const raw = query.get(key);
  if (raw === null) return fallback;
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : fallback;
}

/** Cắt một mảng thành trang, trả đúng dạng Page<T>. */
export function paginate<T>(
  items: T[],
  page: number,
  pageSize: number,
): {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
} {
  const safePage = Math.max(1, page);
  const safeSize = Math.max(1, pageSize);
  const start = (safePage - 1) * safeSize;

  return {
    items: items.slice(start, start + safeSize),
    page: safePage,
    pageSize: safeSize,
    total: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / safeSize)),
  };
}

/**
 * So khớp chuỗi không phân biệt hoa thường và dấu tiếng Việt.
 *
 * Nhận cả chuỗi song ngữ và tìm trong cả hai bản, để người dùng gõ tiếng
 * Việt hay tiếng Anh đều ra kết quả bất kể đang ở chế độ ngôn ngữ nào.
 */
export function matches(haystack: string | L10n, needle: string): boolean {
  if (!needle) return true;
  const text = typeof haystack === 'string' ? haystack : `${haystack.vi} ${haystack.en}`;
  return fold(text).includes(fold(needle));
}

/** Gộp nhiều trường nội dung thành một chuỗi để tìm kiếm. */
export function searchable(...parts: Array<string | L10n | undefined>): string {
  return parts
    .map((part) =>
      part === undefined ? '' : typeof part === 'string' ? part : `${part.vi} ${part.en}`,
    )
    .join(' ');
}

function fold(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd');
}
