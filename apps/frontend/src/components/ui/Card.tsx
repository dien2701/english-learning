import React, { type ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  className?: string;
  /** Bỏ phần đệm mặc định khi nội dung cần chạm sát mép, ví dụ ảnh bìa. */
  flush?: boolean;
}

/** Khối card trắng bo 16px, viền mảnh, bóng nhẹ — dùng lại ở mọi trang. */
export const Card: React.FC<CardProps> = ({ children, className = '', flush }) => (
  <section
    className={`rounded-lg border border-hairline bg-surface shadow-sm ${
      flush ? '' : 'p-5'
    } ${className}`}
  >
    {children}
  </section>
);

interface CardHeaderProps {
  title: string;
  /** Dòng mô tả ngắn dưới tiêu đề. */
  description?: string;
  /** Nút hoặc liên kết đặt bên phải, ví dụ "Xem tất cả". */
  action?: ReactNode;
  className?: string;
}

export const CardHeader: React.FC<CardHeaderProps> = ({
  title,
  description,
  action,
  className = '',
}) => (
  <div className={`flex items-start justify-between gap-3 ${className}`}>
    <div className="min-w-0">
      <h2 className="text-card-title text-ink">{title}</h2>
      {description && (
        <p className="mt-0.5 text-caption font-normal text-ink-muted">
          {description}
        </p>
      )}
    </div>
    {action && <div className="shrink-0">{action}</div>}
  </div>
);

export default Card;
