import { AxiosError } from 'axios';
import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

import { matchRoute } from '../../mocks/router';
import { ApiError } from './types';

/**
 * Adapter giả lập cho axios.
 *
 * Khi VITE_USE_MOCK=true, adapter này chặn request ngay trước lúc gửi đi
 * và trả kết quả từ các handler trong src/mocks. Tầng service vẫn viết
 * đúng cú pháp axios như gọi server thật, nên lúc nối backend chỉ cần
 * đổi biến môi trường thành false.
 */

/** Độ trễ giả lập, tính bằng mili giây, để thấy được trạng thái loading. */
const LATENCY_MIN = 180;
const LATENCY_MAX = 520;

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

function randomLatency(): number {
  return LATENCY_MIN + Math.random() * (LATENCY_MAX - LATENCY_MIN);
}

/** Tách phần đường dẫn và query khỏi url của request. */
function splitUrl(config: InternalAxiosRequestConfig): {
  pathname: string;
  query: URLSearchParams;
} {
  const raw = config.url ?? '';
  // Dùng một origin giả để URL parse được cả đường dẫn tương đối.
  const url = new URL(raw, 'http://mock.local');

  let pathname = url.pathname;
  const base = config.baseURL ?? '';

  // Bỏ tiền tố baseURL để handler chỉ cần khai báo '/auth/login'.
  if (base) {
    const basePath = new URL(base, 'http://mock.local').pathname.replace(/\/+$/, '');
    if (basePath && pathname.startsWith(basePath)) {
      pathname = pathname.slice(basePath.length) || '/';
    }
  }

  const query = url.searchParams;

  // axios gom params vào config.params, cần trộn vào cùng query.
  const extra = config.params as Record<string, unknown> | undefined;
  if (extra) {
    for (const [key, value] of Object.entries(extra)) {
      if (value === undefined || value === null || value === '') continue;
      if (Array.isArray(value)) {
        value.forEach((v) => query.append(key, String(v)));
      } else {
        query.set(key, String(value));
      }
    }
  }

  return { pathname, query };
}

/** Đọc body của request, chấp nhận cả chuỗi JSON lẫn object. */
function parseBody(data: unknown): unknown {
  if (data === undefined || data === null || data === '') return undefined;
  if (typeof data !== 'string') return data;
  try {
    return JSON.parse(data);
  } catch {
    return data;
  }
}

function readToken(config: InternalAxiosRequestConfig): string | undefined {
  const raw = config.headers?.Authorization ?? config.headers?.authorization;
  if (typeof raw !== 'string') return undefined;
  return raw.replace(/^Bearer\s+/i, '') || undefined;
}

function buildResponse(
  config: InternalAxiosRequestConfig,
  status: number,
  payload: unknown,
): AxiosResponse {
  return {
    data: payload,
    status,
    statusText: status === 200 ? 'OK' : String(status),
    headers: {},
    config,
    request: { mock: true },
  };
}

export const mockAdapter: AxiosAdapter = async (config) => {
  const { pathname, query } = splitUrl(config);
  const method = (config.method ?? 'get').toUpperCase();

  await delay(randomLatency());

  const matched = matchRoute(method, pathname);

  if (!matched) {
    const response = buildResponse(config, 404, {
      success: false,
      data: null,
      message: `Mock chưa có handler cho ${method} ${pathname}`,
      code: 'ERR_MOCK_NOT_FOUND',
      messageKey: 'errors.notFound',
    });

    throw new AxiosError(
      `Mock chưa có handler cho ${method} ${pathname}`,
      'ERR_MOCK_NOT_FOUND',
      config,
      response.request,
      response,
    );
  }

  try {
    const data = await matched.handler({
      params: matched.params,
      query,
      body: parseBody(config.data),
      token: readToken(config),
    });

    return buildResponse(config, 200, { success: true, data });
  } catch (error) {
    const status = error instanceof ApiError ? error.status : 500;
    const message =
      error instanceof Error ? error.message : 'errors.unknown';
    const code = error instanceof ApiError ? error.code : 'MOCK_INTERNAL';
    const fieldErrors =
      error instanceof ApiError ? error.fieldErrors : undefined;
    /* Mock trả khoá dịch thay vì câu tiếng Việt, xem `fail()` ở router. */
    const messageKey =
      error instanceof ApiError ? error.messageKey : 'errors.unknown';
    const fieldErrorKeys =
      error instanceof ApiError ? error.fieldErrorKeys : undefined;

    const response = buildResponse(config, status, {
      success: false,
      data: null,
      message,
      code,
      fieldErrors,
      messageKey,
      fieldErrorKeys,
    });

    throw new AxiosError(message, code, config, response.request, response);
  }
};
