import React from 'react';
import { useTranslation } from 'react-i18next';

const linkClass =
  'inline-flex items-center gap-1.5 text-ink-muted transition-colors duration-200 hover:text-accent';

export const GlobalFooter: React.FC = () => {
  const { t } = useTranslation();

  return (
    <footer className="mt-auto w-full border-t border-hairline px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto grid max-w-content grid-cols-1 gap-8 md:grid-cols-3">
        {/* Cột 1 — giới thiệu */}
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-2.5">
            <span className="grid h-7 w-7 place-items-center rounded-sm bg-action text-white">
              <span
                aria-hidden="true"
                className="material-symbols-outlined text-[17px]"
              >
                school
              </span>
            </span>
            <span className="text-[15px] font-extrabold tracking-tight text-ink">
              En-Learning
            </span>
          </div>

          <p className="max-w-sm text-caption font-normal leading-relaxed text-ink-muted">
            {t('footer.description')}
          </p>

          <a href="#ho-tro" className={`${linkClass} text-caption font-bold`}>
            {t('footer.support')}
            <span
              aria-hidden="true"
              className="material-symbols-outlined text-[14px]"
            >
              arrow_outward
            </span>
          </a>
        </div>

        {/* Cột 2 — chính sách */}
        <div className="flex flex-col gap-3">
          <h2 className="text-caption font-bold uppercase tracking-wider text-ink">
            {t('footer.policies')}
          </h2>
          <ul className="flex flex-col gap-2.5 text-caption font-normal">
            <li>
              <a href="#chinh-sach-bao-mat" className={linkClass}>
                {t('footer.privacyPolicy')}
              </a>
            </li>
            <li>
              <a href="#dieu-khoan-su-dung" className={linkClass}>
                {t('footer.termsOfUse')}
              </a>
            </li>
            <li>
              <a href="#quy-dinh-hoc-tap" className={linkClass}>
                {t('footer.learningRules')}
              </a>
            </li>
          </ul>
        </div>

        {/* Cột 3 — liên hệ */}
        <div className="flex flex-col gap-3">
          <h2 className="text-caption font-bold uppercase tracking-wider text-ink">
            {t('footer.contact')}
          </h2>
          <ul className="flex flex-col gap-2.5 text-caption font-normal">
            <li>
              <a href="#trung-tam-ho-tro" className={linkClass}>
                <span
                  aria-hidden="true"
                  className="material-symbols-outlined text-[16px]"
                >
                  headset_mic
                </span>
                {t('footer.supportCenter')}
              </a>
            </li>
            <li>
              <a href="#gui-phan-hoi" className={linkClass}>
                <span
                  aria-hidden="true"
                  className="material-symbols-outlined text-[16px]"
                >
                  rate_review
                </span>
                {t('footer.sendFeedback')}
              </a>
            </li>
            <li className="pt-1 text-ink-subtle">
              {t('footer.rights')}
            </li>
          </ul>
        </div>
      </div>
    </footer>
  );
};

export default GlobalFooter;
