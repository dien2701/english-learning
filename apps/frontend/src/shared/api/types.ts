/** Kiểu dữ liệu dùng chung cho toàn bộ lớp gọi API. */

/** Vỏ bọc mà backend sẽ trả về cho mọi endpoint. */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

/** Trang dữ liệu cho danh sách có phân trang. */
export interface Page<T> {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}

/** Tham số phân trang gửi lên server. */
export interface PageQuery {
  page?: number;
  pageSize?: number;
  search?: string;
  sort?: string;
}

/**
 * Lỗi đã được chuẩn hoá. Mọi lỗi từ axios — dù là lỗi mạng, timeout
 * hay lỗi backend trả về — đều được interceptor đổi sang kiểu này,
 * nên tầng giao diện chỉ cần xử lý một dạng lỗi duy nhất.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  /**
   * Khoá dịch của thông báo lỗi, ví dụ `errors.emailTaken`.
   *
   * Lỗi phải đổi theo ngôn ngữ đang chọn, mà lớp API thì nằm ngoài cây
   * React nên không gọi được `t()`. Vì vậy phía server (và mock) chỉ trả
   * khoá; tầng giao diện dịch bằng `useApiError()`. Backend thật chưa có
   * khoá thì `message` vẫn là câu dựng sẵn để không mất thông tin.
   */
  readonly messageKey?: string;
  /** Lỗi theo từng trường, dùng để đổ vào Form của Ant Design. */
  readonly fieldErrors?: Record<string, string>;
  /** Khoá dịch của lỗi theo từng trường, cùng vai trò với `messageKey`. */
  readonly fieldErrorKeys?: Record<string, string>;

  constructor(
    status: number,
    message: string,
    code = 'UNKNOWN',
    fieldErrors?: Record<string, string>,
    options?: {
      messageKey?: string;
      fieldErrorKeys?: Record<string, string>;
    },
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
    this.messageKey = options?.messageKey;
    this.fieldErrorKeys = options?.fieldErrorKeys;
  }

  /** Lỗi mạng, server sập, hoặc request bị huỷ. */
  get isNetworkError(): boolean {
    return this.status === 0;
  }

  /** Chưa đăng nhập hoặc phiên đã hết hạn. */
  get isUnauthorized(): boolean {
    return this.status === 401;
  }

  /** Đã đăng nhập nhưng không đủ quyền. */
  get isForbidden(): boolean {
    return this.status === 403;
  }

  get isNotFound(): boolean {
    return this.status === 404;
  }
}
