import React, { type ReactNode } from 'react';
import { Link } from 'react-router-dom';

interface PageHeaderProps {
  title: string;
  description?: string;
  /** Liên kết quay lại, hiện thành một dòng nhỏ phía trên tiêu đề. */
  backTo?: { label: string; to: string };
  action?: ReactNode;
}

/** Tiêu đề trang dùng chung cho mọi màn hình bên trong ứng dụng. */
const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  description,
  backTo,
  action,
}) => (
  <header className="mb-5">
    {backTo && (
      <Link
        to={backTo.to}
        className="mb-2 inline-flex items-center gap-1 text-[13px] font-bold text-ink-muted transition-colors hover:text-accent"
      >
        <span aria-hidden="true" className="material-symbols-outlined text-[18px]">
          arrow_back
        </span>
        {backTo.label}
      </Link>
    )}

    <div className="flex flex-wrap items-start justify-between gap-3">
      <div className="min-w-0">
        <h1 className="text-page-title text-ink">{title}</h1>
        {description && (
          <p className="mt-1 max-w-2xl text-body text-ink-muted">{description}</p>
        )}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  </header>
);

export default PageHeader;
