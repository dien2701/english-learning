import { useEffect, useState } from 'react';

/**
 * Trả về giá trị đã trễ lại một nhịp.
 *
 * Dùng cho ô tìm kiếm: người dùng gõ liên tục nhưng chỉ gọi API sau khi
 * họ ngừng gõ, thay vì bắn một request cho mỗi ký tự.
 */
export function useDebounced<T>(value: T, delayMs = 300): T {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const id = window.setTimeout(() => setDebounced(value), delayMs);
    return () => window.clearTimeout(id);
  }, [value, delayMs]);

  return debounced;
}

export default useDebounced;
