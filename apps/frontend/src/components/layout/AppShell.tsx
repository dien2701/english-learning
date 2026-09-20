import React, { useCallback, useEffect, useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

import Sidebar from './Sidebar';
import Header from './Header';
import GlobalFooter from './GlobalFooter';
import ChatWidget from '../chat/ChatWidget';

const SIDEBAR_KEY = 'en_learning_sidebar_open';

/** Dưới ngưỡng này sidebar là lớp phủ, phía trên nó nằm cố định bên trái. */
const DESKTOP_QUERY = '(min-width: 1024px)';

/**
 * Khung chung của khu vực người dùng: sidebar cố định bên trái,
 * header dính phía trên cột nội dung, nội dung cuộn bên dưới.
 */
const AppShell: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { t } = useTranslation();

  const [isDesktop, setIsDesktop] = useState(
    () => window.matchMedia(DESKTOP_QUERY).matches,
  );

  const [isAssistantOpen, setIsAssistantOpen] = useState(false);

  const [isOpen, setIsOpen] = useState(() => {
    // Trên màn hình nhỏ luôn bắt đầu ở trạng thái đóng để không che nội dung.
    if (!window.matchMedia(DESKTOP_QUERY).matches) return false;
    try {
      return localStorage.getItem(SIDEBAR_KEY) !== 'false';
    } catch {
      return true;
    }
  });

  useEffect(() => {
    const mql = window.matchMedia(DESKTOP_QUERY);

    const onChange = (event: MediaQueryListEvent) => {
      setIsDesktop(event.matches);
      // Chuyển xuống màn hình nhỏ thì thu sidebar lại.
      if (!event.matches) setIsOpen(false);
    };

    mql.addEventListener('change', onChange);
    return () => mql.removeEventListener('change', onChange);
  }, []);

  const toggleSidebar = useCallback(() => {
    setIsOpen((prev) => {
      const next = !prev;
      if (window.matchMedia(DESKTOP_QUERY).matches) {
        try {
          localStorage.setItem(SIDEBAR_KEY, String(next));
        } catch {
          /* bỏ qua */
        }
      }
      return next;
    });
  }, []);

  const closeOnMobile = useCallback(() => {
    if (!window.matchMedia(DESKTOP_QUERY).matches) setIsOpen(false);
  }, []);

  /* Khi sidebar mở dạng lớp phủ, khoá cuộn nền phía sau. */
  useEffect(() => {
    const locked = isOpen && !isDesktop;
    document.body.style.overflow = locked ? 'hidden' : '';
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen, isDesktop]);

  return (
    <div className="min-h-screen">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-surface focus:px-4 focus:py-2 focus:text-[13px] focus:font-bold focus:text-accent focus:shadow-lg"
      >
        {t('sidebar.skipToContent')}
      </a>

      <Sidebar
        isOpen={isOpen}
        onNavigate={closeOnMobile}
        onOpenAssistant={() => {
          setIsAssistantOpen(true);
          closeOnMobile();
        }}
      />

      {/* Lớp phủ nền khi sidebar mở trên màn hình nhỏ */}
      {isOpen && !isDesktop && (
        <button
          type="button"
          aria-label={t('sidebar.closeNav')}
          onClick={() => setIsOpen(false)}
          className="fixed inset-0 z-30 bg-brand-950/40 backdrop-blur-sm lg:hidden"
        />
      )}

      <div
        className={`flex min-h-screen flex-col transition-[margin] duration-300 ease-out ${
          isOpen ? 'lg:ml-sidebar' : 'lg:ml-sidebar-sm'
        }`}
      >
        <Header toggleSidebar={toggleSidebar} />

        <main id="main-content" className="flex-1">
          {children}
        </main>

        <GlobalFooter />
      </div>

      {/* Cửa sổ chat thu nhỏ, mở từ khối trợ lý ở đáy sidebar */}
      <ChatWidget
        isOpen={isAssistantOpen}
        onClose={() => setIsAssistantOpen(false)}
      />
    </div>
  );
};

export default AppShell;
