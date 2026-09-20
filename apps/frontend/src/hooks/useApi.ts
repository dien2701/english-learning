import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../shared/api/types';

interface Settled<T> {
  /** Chữ ký của lần gọi đã cho ra kết quả này. */
  key: string;
  data: T | null;
  error: ApiError | null;
}

interface UseApiResult<T> {
  data: T | null;
  isLoading: boolean;
  error: ApiError | null;
  /** Gọi lại request, dùng cho nút "Thử lại". */
  reload: () => void;
}

/**
 * Gọi một hàm service và theo dõi ba trạng thái: đang tải, có lỗi, có dữ liệu.
 *
 * `deps` hoạt động như mảng phụ thuộc của useEffect: đổi giá trị thì gọi lại.
 *
 * Trạng thái đang tải được suy ra bằng cách so chữ ký của lần gọi hiện tại với
 * chữ ký của kết quả đang giữ, thay vì gọi setState ngay trong thân effect.
 * Nhờ vậy không phát sinh thêm một vòng render thừa mỗi lần deps đổi.
 */
export function useApi<T>(
  fetcher: () => Promise<T>,
  deps: unknown[] = [],
): UseApiResult<T> {
  const [nonce, setNonce] = useState(0);
  const [settled, setSettled] = useState<Settled<T> | null>(null);

  const key = `${JSON.stringify(deps)}|${nonce}`;

  // Giữ tham chiếu mới nhất để không phải đưa fetcher vào mảng phụ thuộc.
  // Hàm này phải được khai báo trước effect gọi API để chạy trước nó.
  const fetcherRef = useRef(fetcher);
  useEffect(() => {
    fetcherRef.current = fetcher;
  });

  useEffect(() => {
    let active = true;

    fetcherRef
      .current()
      .then((data) => {
        if (active) setSettled({ key, data, error: null });
      })
      .catch((error: unknown) => {
        if (!active) return;
        setSettled({
          key,
          data: null,
          error:
            error instanceof ApiError
              ? error
              : new ApiError(0, 'Đã xảy ra lỗi không xác định'),
        });
      });

    return () => {
      active = false;
    };
  }, [key]);

  const reload = useCallback(() => setNonce((n) => n + 1), []);

  // Kết quả đang giữ thuộc về lần gọi khác thì nghĩa là vẫn đang tải.
  const isFresh = settled?.key === key;

  return {
    data: isFresh ? settled.data : null,
    error: isFresh ? settled.error : null,
    isLoading: !isFresh,
    reload,
  };
}

export default useApi;
