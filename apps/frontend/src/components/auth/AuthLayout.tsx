import React, { type ReactNode } from 'react';

import LanguageSwitch from '../layout/LanguageSwitch';

interface AuthLayoutProps {
  children: ReactNode;
  title: string;
  subtitle?: string;
  /** Liên kết phụ dưới thẻ, ví dụ "Chưa có tài khoản? Đăng ký ngay". */
  footer?: ReactNode;
}

/**
 * Khung chung cho các màn hình xác thực.
 * Một cột căn giữa, nền mint nhạt, không trang trí thừa.
 */
const AuthLayout: React.FC<AuthLayoutProps> = ({
  children,
  title,
  subtitle,
  footer,
}) => (
  <div className="flex min-h-screen items-center justify-center px-4 py-10">
    <div className="w-full max-w-[420px]">
      {/* Người chưa đăng nhập cũng phải đổi được ngôn ngữ, nếu không cả
          nhóm màn hình xác thực sẽ kẹt ở tiếng Việt. */}
      <div className="mb-4 flex justify-end">
        <LanguageSwitch />
      </div>

      <div className="mb-7 flex items-center justify-center gap-2.5">
        <span className="grid h-10 w-10 place-items-center rounded-md bg-action text-white">
          <span aria-hidden="true" className="material-symbols-outlined text-[23px]">
            school
          </span>
        </span>
        <span className="text-[22px] font-extrabold tracking-tight text-ink">
          En-Learning
        </span>
      </div>

      <div className="rounded-lg border border-hairline bg-surface p-7 shadow-md sm:p-8">
        <div className="mb-6 text-center">
          <h1 className="text-[24px] font-extrabold tracking-tight text-ink">
            {title}
          </h1>
          {subtitle && (
            <p className="mt-1.5 text-body text-ink-muted">{subtitle}</p>
          )}
        </div>

        {children}
      </div>

      {footer && (
        <p className="mt-5 text-center text-body text-ink-muted">{footer}</p>
      )}
    </div>
  </div>
);

export default AuthLayout;
