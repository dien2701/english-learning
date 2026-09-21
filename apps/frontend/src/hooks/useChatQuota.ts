import { useCallback, useEffect, useState } from 'react';

import { chatService } from '../services/chatService';
import type { ChatQuota } from '../types/chat';

/**
 * Số lượt nhắn còn lại hôm nay. Chỉ tải khi `enabled`; lỗi tải thì bỏ qua
 * (không hiện đếm lượt, backend vẫn là bên chặn khi hết lượt).
 */
export function useChatQuota(enabled = true) {
  const [quota, setQuota] = useState<ChatQuota | null>(null);

  useEffect(() => {
    if (!enabled) return;
    let active = true;
    chatService
      .quota()
      .then((next) => {
        if (active) setQuota(next);
      })
      .catch(() => undefined);
    return () => {
      active = false;
    };
  }, [enabled]);

  /** Cập nhật theo `remaining` mà backend trả kèm mỗi lần gửi thành công. */
  const setRemaining = useCallback((remaining: number) => {
    setQuota((q) => (q ? { ...q, remaining, used: q.limit - remaining } : q));
  }, []);

  /** Backend báo hết lượt (429) dù chưa biết hạn mức. */
  const markExhausted = useCallback(() => {
    setQuota((q) => ({
      limit: q?.limit ?? 0,
      used: q?.limit ?? 0,
      remaining: 0,
      resetAt: q?.resetAt ?? '',
    }));
  }, []);

  return {
    quota,
    setRemaining,
    markExhausted,
    exhausted: quota !== null && quota.remaining <= 0,
  };
}

export default useChatQuota;
