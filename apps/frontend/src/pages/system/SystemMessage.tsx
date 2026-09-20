import React from 'react';
import { useTranslation } from 'react-i18next';

import { Button, ButtonLink } from '../../components/ui/Button';

interface SystemMessageProps {
  code: string;
  icon: string;
  /** Khoá dịch của tiêu đề và mô tả, không truyền chuỗi đã dịch sẵn. */
  titleKey: string;
  descriptionKey: string;
  /** Nút chính, mặc định đưa về trang Tổng quan. */
  primary?: { labelKey: string; to: string };
}

/** Khung chung cho các trang 403, 404 và 500. */
const SystemMessage: React.FC<SystemMessageProps> = ({
  code,
  icon,
  titleKey,
  descriptionKey,
  primary = { labelKey: 'system.backToDashboard', to: '/dashboard' },
}) => {
  const { t } = useTranslation();

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10">
      <div className="w-full max-w-md text-center">
        <span className="mx-auto grid h-16 w-16 place-items-center rounded-pill bg-accent-soft text-accent">
          <span aria-hidden="true" className="material-symbols-outlined text-[34px]">
            {icon}
          </span>
        </span>

        <p className="mt-5 text-[13px] font-bold uppercase tracking-[0.12em] text-ink-subtle">
          {t('system.errorCode', { code })}
        </p>

        <h1 className="mt-1.5 text-[26px] font-extrabold tracking-tight text-ink">
          {t(titleKey)}
        </h1>

        <p className="mt-2 text-body text-ink-muted">{t(descriptionKey)}</p>

        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <ButtonLink to={primary.to} size="lg">
            {t(primary.labelKey)}
          </ButtonLink>

          <Button
            variant="subtle"
            size="lg"
            onClick={() => window.history.back()}
          >
            {t('system.goBack')}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SystemMessage;
