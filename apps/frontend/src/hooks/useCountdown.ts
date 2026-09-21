import { useEffect, useRef, useState } from 'react';

interface UseCountdownResult {
  /** Số giây còn lại. */
  remaining: number;
  /** Số giây đã trôi qua kể từ lúc bắt đầu. */
  elapsed: number;
  isRunning: boolean;
}

/**
 * Đồng hồ đếm ngược cho các bài có giới hạn thời gian.
 *
 * `totalSeconds` bằng 0 nghĩa là không giới hạn — khi đó đồng hồ chỉ đếm
 * xuôi để ghi lại thời gian làm bài.
 *
 * `onExpire` được gọi đúng một lần khi hết giờ. Hàm này được giữ trong ref
 * nên bên gọi không cần bọc bằng useCallback.
 */
export function useCountdown(
  totalSeconds: number,
  onExpire?: () => void,
  isActive = true,
): UseCountdownResult {
  const [elapsed, setElapsed] = useState(0);
  const elapsedRef = useRef(0);

  const onExpireRef = useRef(onExpire);
  useEffect(() => {
    onExpireRef.current = onExpire;
  });

  const hasExpired = useRef(false);

  useEffect(() => {
    if (!isActive) return;

    // Tính theo đồng hồ thật để tab nền bị hạn chế timer vẫn không đếm thiếu.
    const startedAt = Date.now() - elapsedRef.current * 1000;
    const id = window.setInterval(() => {
      const next = Math.floor((Date.now() - startedAt) / 1000);
      elapsedRef.current = next;
      setElapsed(next);
    }, 1000);

    return () => window.clearInterval(id);
  }, [isActive]);

  const isLimited = totalSeconds > 0;
  const remaining = isLimited ? Math.max(0, totalSeconds - elapsed) : 0;

  useEffect(() => {
    if (!isLimited || hasExpired.current) return;
    if (remaining > 0) return;

    hasExpired.current = true;
    onExpireRef.current?.();
  }, [isLimited, remaining]);

  return { remaining, elapsed, isRunning: isActive };
}

/** Định dạng giây thành mm:ss, hoặc h:mm:ss khi dài hơn một tiếng. */
export function formatClock(totalSeconds: number): string {
  const s = Math.max(0, Math.floor(totalSeconds));
  const hours = Math.floor(s / 3600);
  const minutes = Math.floor((s % 3600) / 60);
  const seconds = s % 60;

  const pad = (v: number) => String(v).padStart(2, '0');

  return hours > 0
    ? `${hours}:${pad(minutes)}:${pad(seconds)}`
    : `${pad(minutes)}:${pad(seconds)}`;
}
