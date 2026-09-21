import { useEffect } from 'react';
import { studyService } from '../services/userService';
import type { Skill } from '../types/common';

const INTERVAL_MS = 30_000;
const ACTIVITY_EVENTS = ['pointerdown', 'keydown', 'scroll', 'touchstart'] as const;

/**
 * Báo cho backend biết người dùng đang học, khoảng 30 giây một lần.
 * Chỉ gửi khi tab đang hiện và có tương tác kể từ lần gửi trước; lỗi mạng bị bỏ qua
 * vì đây là số liệu phụ, không đáng làm gián đoạn việc học.
 */
export function useStudyHeartbeat(skill: Skill, refId: string | undefined): void {
  useEffect(() => {
    if (!refId) return;

    // Mở trang cũng tính là một tương tác.
    let interacted = true;
    const markActive = () => {
      interacted = true;
    };
    ACTIVITY_EVENTS.forEach((name) => window.addEventListener(name, markActive, { passive: true }));

    const beat = () => {
      if (document.visibilityState !== 'visible' || !interacted) return;
      interacted = false;
      studyService.heartbeat(skill, refId).catch(() => undefined);
    };

    beat();
    const timer = window.setInterval(beat, INTERVAL_MS);
    return () => {
      window.clearInterval(timer);
      ACTIVITY_EVENTS.forEach((name) => window.removeEventListener(name, markActive));
    };
  }, [skill, refId]);
}
