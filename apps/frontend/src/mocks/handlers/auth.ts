/**
 * Mock cho module Xác thực.
 *
 * Bám theo mô tả chức năng 1: thông báo lỗi không được tiết lộ email đó
 * có tồn tại trong hệ thống hay không, nên đăng nhập sai và email không
 * tồn tại đều trả về đúng một câu như nhau.
 */

import { del, fail, get, post } from '../router';
import {
  accounts,
  daysAgo,
  DEMO_PASSWORD,
  findAccountByToken,
  issueToken,
  resetCodes,
  revokedTokens,
  toPublicUser,
} from '../db';
import type { MockAccount } from '../db';
import type { User } from '../../types/common';

interface Credentials {
  email?: string;
  password?: string;
}

interface RegisterPayload extends Credentials {
  fullName?: string;
  confirmPassword?: string;
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Câu thông báo dùng chung, cố ý mơ hồ vì lý do bảo mật. */
const INVALID_CREDENTIALS = 'errors.invalidCredentials';

function requireAuth(token?: string): MockAccount {
  const account = findAccountByToken(token);
  if (!account) {
    fail(401, 'errors.sessionExpired', 'UNAUTHORIZED');
  }
  return account;
}

/* --- POST /auth/login ---------------------------------------------- */

post('/auth/login', ({ body }) => {
  const { email, password } = (body ?? {}) as Credentials;

  const fieldErrors: Record<string, string> = {};
  if (!email) fieldErrors.email = 'auth.validation.emailRequired';
  else if (!EMAIL_PATTERN.test(email)) fieldErrors.email = 'auth.validation.emailFormat';
  if (!password) fieldErrors.password = 'auth.validation.passwordRequired';

  if (Object.keys(fieldErrors).length > 0) {
    fail(400, 'errors.checkLoginInfo', 'VALIDATION', fieldErrors);
  }

  const account = accounts.find(
    (a) => a.email.toLowerCase() === email!.toLowerCase(),
  );

  // Sai mật khẩu và không tồn tại tài khoản trả về cùng một thông báo.
  if (!account || account.password !== password) {
    fail(401, INVALID_CREDENTIALS, 'INVALID_CREDENTIALS');
  }

  if (account.status === 'LOCKED') {
    fail(
      403,
      'errors.accountLocked',
      'ACCOUNT_LOCKED',
    );
  }

  const token = issueToken(account.id);

  return {
    token,
    refreshToken: `refresh.${token}`,
    user: toPublicUser(account),
  };
});

/* --- POST /auth/register ------------------------------------------- */

post('/auth/register', ({ body }) => {
  const { fullName, email, password, confirmPassword } =
    (body ?? {}) as RegisterPayload;

  const fieldErrors: Record<string, string> = {};

  if (!fullName?.trim()) fieldErrors.fullName = 'auth.validation.nameRequired';
  else if (fullName.trim().length < 2) fieldErrors.fullName = 'auth.validation.nameShort';

  if (!email) fieldErrors.email = 'auth.validation.emailRequired';
  else if (!EMAIL_PATTERN.test(email)) fieldErrors.email = 'auth.validation.emailFormat';

  if (!password) fieldErrors.password = 'auth.validation.passwordRequired';
  else if (password.length < 6) fieldErrors.password = 'auth.validation.passwordMin';

  if (confirmPassword !== undefined && confirmPassword !== password) {
    fieldErrors.confirmPassword = 'auth.validation.confirmMismatch';
  }

  if (Object.keys(fieldErrors).length > 0) {
    fail(400, 'errors.checkRegisterInfo', 'VALIDATION', fieldErrors);
  }

  const existed = accounts.some(
    (a) => a.email.toLowerCase() === email!.toLowerCase(),
  );

  if (existed) {
    fail(409, 'errors.emailTaken', 'EMAIL_TAKEN', {
      email: 'errors.emailTaken',
    });
  }

  const account: MockAccount = {
    id: `u-${String(accounts.length + 1).padStart(3, '0')}`,
    email: email!.toLowerCase(),
    password: password!,
    fullName: fullName!.trim(),
    role: 'USER',
    status: 'ACTIVE',
    createdAt: daysAgo(0),
  };

  accounts.push(account);

  const token = issueToken(account.id);

  return {
    token,
    refreshToken: `refresh.${token}`,
    user: toPublicUser(account),
  };
});

/* --- GET /auth/check-email ------------------------------------------
   Trang đăng ký hỏi trước khi gửi form để người dùng biết email đã có
   tài khoản ngay lúc rời ô, thay vì chờ tới lúc bấm nút.

   Khác với /auth/forgot-password (cố ý mơ hồ để không lộ email nào tồn
   tại), ở đây buộc phải trả lời thẳng: thiếu nó thì người dùng không
   sửa được lỗi. Backend thật nên giới hạn tần suất gọi endpoint này.   */

get('/auth/check-email', ({ query }) => {
  const email = (query.get('email') ?? '').trim().toLowerCase();

  if (!email || !EMAIL_PATTERN.test(email)) {
    fail(400, 'auth.validation.emailFormat', 'VALIDATION', {
      email: 'auth.validation.emailFormat',
    });
  }

  return { available: !accounts.some((a) => a.email.toLowerCase() === email) };
});

/* --- GET /auth/me --------------------------------------------------- */

get('/auth/me', ({ token }): User => toPublicUser(requireAuth(token)));

/* --- DELETE /auth/session (đăng xuất) ------------------------------- */

del('/auth/session', ({ token }) => {
  if (token) revokedTokens.add(token);
  return { loggedOut: true };
});

/* --- POST /auth/forgot-password ------------------------------------- */

post('/auth/forgot-password', ({ body }) => {
  const { email } = (body ?? {}) as Credentials;

  if (!email || !EMAIL_PATTERN.test(email)) {
    fail(400, 'auth.validation.emailFormat', 'VALIDATION', {
      email: 'auth.validation.emailFormat',
    });
  }

  const account = accounts.find(
    (a) => a.email.toLowerCase() === email.toLowerCase(),
  );

  // Chỉ phát hành mã khi tài khoản có thật, nhưng phản hồi thì luôn giống
  // nhau để không lộ email nào đang tồn tại.
  if (account) {
    resetCodes.set(account.email, '123456');
  }

  return {
    message:
      'Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi đi.',
  };
});

/* --- POST /auth/reset-password -------------------------------------- */

interface ResetPayload {
  email?: string;
  code?: string;
  password?: string;
  confirmPassword?: string;
}

post('/auth/reset-password', ({ body }) => {
  const { email, code, password, confirmPassword } = (body ?? {}) as ResetPayload;

  const fieldErrors: Record<string, string> = {};

  if (!code) fieldErrors.code = 'auth.validation.codeRequired';
  if (!password) fieldErrors.password = 'auth.validation.newPasswordRequired';
  else if (password.length < 6) fieldErrors.password = 'auth.validation.passwordMin';
  if (confirmPassword !== undefined && confirmPassword !== password) {
    fieldErrors.confirmPassword = 'auth.validation.confirmMismatch';
  }

  if (Object.keys(fieldErrors).length > 0) {
    fail(400, 'errors.checkInfo', 'VALIDATION', fieldErrors);
  }

  const account = accounts.find(
    (a) => a.email.toLowerCase() === (email ?? '').toLowerCase(),
  );

  if (!account || resetCodes.get(account.email) !== code) {
    fail(400, 'errors.invalidCode', 'INVALID_CODE', {
      code: 'errors.invalidCode',
    });
  }

  account.password = password!;
  resetCodes.delete(account.email);

  return { message: 'Đặt lại mật khẩu thành công. Bạn có thể đăng nhập lại.' };
});

/** Thông tin tài khoản mẫu, dùng để hiện gợi ý ở màn hình đăng nhập. */
export const demoCredentials = {
  user: { email: 'hocvien@enlearning.vn', password: DEMO_PASSWORD },
  admin: { email: 'admin@enlearning.vn', password: DEMO_PASSWORD },
};
