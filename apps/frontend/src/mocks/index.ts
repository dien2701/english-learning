/**
 * Điểm nạp toàn bộ mock API.
 *
 * File này chỉ cần được import một lần ở main.tsx. Mỗi file handler tự
 * đăng ký route của mình khi được import, nên thêm module mới chỉ việc
 * thêm một dòng import ở dưới.
 */

import { routeCount } from './router';

import './handlers/dashboard';
import './handlers/flashcard';
import './handlers/writing';
import './handlers/practice';
import './handlers/speaking';
import './handlers/chat';
import './handlers/admin';
import './handlers/misc';

/** Ghi một dòng log để biết module nào đang mock và có bao nhiêu route. */
export function announceMocks(modules: readonly string[]): void {
  if (import.meta.env.DEV) {
    console.info(
      `%c[mock] Module giả lập: ${modules.join(', ')} (${routeCount()} route). ` +
        `Bỏ module khỏi VITE_MOCK_MODULES trong .env để gọi backend thật.`,
      'color:#15803d;font-weight:600',
    );
  }
}
