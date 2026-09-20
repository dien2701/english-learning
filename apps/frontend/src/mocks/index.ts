/**
 * Điểm nạp toàn bộ mock API.
 *
 * File này chỉ cần được import một lần ở main.tsx. Mỗi file handler tự
 * đăng ký route của mình khi được import, nên thêm module mới chỉ việc
 * thêm một dòng import ở dưới.
 */

import { routeCount } from './router';

import './handlers/auth';
import './handlers/dashboard';
import './handlers/flashcard';
import './handlers/writing';
import './handlers/practice';
import './handlers/speaking';
import './handlers/chat';
import './handlers/admin';
import './handlers/misc';

export { demoCredentials } from './handlers/auth';

/** Ghi một dòng log để biết mock đang bật và có bao nhiêu route. */
export function announceMocks(): void {
  if (import.meta.env.DEV) {
    console.info(
      `%c[mock] Đang chạy với dữ liệu giả lập — ${routeCount()} route. ` +
        `Đặt VITE_USE_MOCK=false trong .env để gọi backend thật.`,
      'color:#15803d;font-weight:600',
    );
  }
}
