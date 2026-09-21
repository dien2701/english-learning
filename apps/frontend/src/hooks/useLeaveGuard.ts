import { useCallback, useEffect, useRef } from 'react';
import { App } from 'antd';
import { useBlocker } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

/**
 * Hỏi xác nhận khi người dùng rời trang lúc đang làm bài dở.
 * Chặn chuyển route (useBlocker) và đóng/tải lại tab (beforeunload).
 *
 * Trả về `release()`: gọi ngay trước `navigate` sau khi nộp thành công để
 * tắt guard đồng bộ, vì state `active` chưa kịp cập nhật.
 */
export function useLeaveGuard(active: boolean): () => void {
  const { t } = useTranslation();
  const { modal } = App.useApp();
  const activeRef = useRef(active);
  const released = useRef(false);

  useEffect(() => {
    activeRef.current = active;
  }, [active]);

  const isGuarding = () => activeRef.current && !released.current;

  const blocker = useBlocker(({ currentLocation, nextLocation }) =>
    isGuarding() && currentLocation.pathname !== nextLocation.pathname,
  );

  useEffect(() => {
    if (blocker.state !== 'blocked') return;
    modal.confirm({
      title: t('exam.leaveTitle'),
      content: t('exam.leaveBody'),
      okText: t('exam.leaveOk'),
      cancelText: t('exam.leaveStay'),
      onOk: () => blocker.proceed(),
      onCancel: () => blocker.reset(),
    });
  }, [blocker, modal, t]);

  useEffect(() => {
    if (!active) return;
    const handler = (e: BeforeUnloadEvent) => {
      if (!isGuarding()) return;
      e.preventDefault();
      e.returnValue = '';
    };
    window.addEventListener('beforeunload', handler);
    return () => window.removeEventListener('beforeunload', handler);
  }, [active]);

  return useCallback(() => {
    released.current = true;
  }, []);
}
