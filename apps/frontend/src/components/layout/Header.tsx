import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useThemeMode } from '../../contexts/ThemeContext';
import { useApi } from '../../hooks/useApi';
import { useFormat } from '../../hooks/useFormat';
import { useLanguage } from '../../hooks/useLanguage';
import { notificationService } from '../../services/userService';
import LanguageSwitch from './LanguageSwitch';

/** Lấy tối đa hai chữ cái đầu để làm ảnh đại diện chữ. */
function initialsOf(fullName: string): string {
  const parts = fullName.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

interface HeaderProps {
  toggleSidebar: () => void;
}

export const Header: React.FC<HeaderProps> = ({ toggleSidebar }) => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { relativeTime } = useFormat();
  const navigate = useNavigate();
  const { user, isAdmin, logout } = useAuth();

  /* Lấy thẳng từ API thông báo thay vì giữ một danh sách mẫu viết cứng
     trong component — danh sách cứng đó không đổi được theo VI/EN và
     luôn lệch với Trung tâm thông báo. */
  const { data: notifications } = useApi(() => notificationService.list(), []);

  const items = (notifications?.items ?? []).slice(0, 3);
  const unreadCount = (notifications?.items ?? []).filter((n) => !n.isRead).length;

  const [openMenu, setOpenMenu] = useState<'notif' | 'profile' | null>(null);

  // Chế độ sáng/tối dùng chung toàn hệ thống, không giữ riêng ở đây nữa.
  const { isDark, toggle: toggleTheme } = useThemeMode();

  const notifRef = useRef<HTMLDivElement>(null);
  const profileRef = useRef<HTMLDivElement>(null);

  /* Đóng menu khi bấm ra ngoài hoặc nhấn Esc. */
  useEffect(() => {
    if (openMenu === null) return;

    const onPointerDown = (event: MouseEvent) => {
      const target = event.target as Node;
      if (notifRef.current?.contains(target)) return;
      if (profileRef.current?.contains(target)) return;
      setOpenMenu(null);
    };

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpenMenu(null);
    };

    document.addEventListener('mousedown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [openMenu]);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const displayName = user?.fullName ?? '';
  const firstName = displayName.trim().split(/\s+/).pop() ?? '';

  const iconButton =
    'grid h-10 w-10 place-items-center rounded-md text-ink-muted transition-colors duration-200 hover:bg-surface-hover hover:text-ink';

  return (
    <header className="sticky top-0 z-30 flex h-header items-center justify-between gap-4 border-b border-hairline bg-surface-main/85 px-4 backdrop-blur sm:px-6">
      <div className="flex min-w-0 items-center gap-3">
        <button
          type="button"
          onClick={toggleSidebar}
          aria-label={t('header.toggleSidebar')}
          aria-controls="main-sidebar"
          className={iconButton}
        >
          <span className="material-symbols-outlined text-[24px]">menu</span>
        </button>

        <div className="min-w-0">
          <p className="truncate text-[15px] font-extrabold tracking-tight text-ink">
            {t('header.greeting')}
            {firstName && `, ${firstName}`}
          </p>
          <p className="hidden truncate text-[12px] text-ink-muted sm:block">
            {t('header.greetingSub')}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2 sm:gap-3">
        {/* Chuyển ngôn ngữ — hiện ở mọi bề rộng: trước đây bị ẩn dưới
            640px nên trên điện thoại không có cách nào đổi sang EN. */}
        <LanguageSwitch />

        {/* Thông báo */}
        <div className="relative" ref={notifRef}>
          <button
            type="button"
            onClick={() => setOpenMenu(openMenu === 'notif' ? null : 'notif')}
            aria-expanded={openMenu === 'notif'}
            aria-haspopup="true"
            aria-label={`${t('header.notifications')} — ${t(
              'notifications.unreadCount',
              { count: unreadCount },
            )}`}
            className={`relative ${iconButton}`}
          >
            <span className="material-symbols-outlined text-[23px]">
              notifications
            </span>
            {unreadCount > 0 && (
              <span
                aria-hidden="true"
                className="absolute right-1.5 top-1.5 grid h-4 min-w-4 place-items-center rounded-pill bg-danger px-1 text-[10px] font-bold text-white ring-2 ring-surface-main"
              >
                {unreadCount}
              </span>
            )}
          </button>

          {openMenu === 'notif' && (
            <div className="absolute right-0 mt-2 w-[min(22rem,calc(100vw-2rem))] origin-top-right overflow-hidden rounded-lg border border-hairline bg-surface shadow-lg">
              <div className="flex items-center justify-between border-b border-hairline px-4 py-3">
                <h2 className="text-[15px] font-bold text-ink">
                  {t('header.notifications')}
                </h2>
                <button
                  type="button"
                  className="text-[12px] font-bold text-accent hover:underline"
                >
                  {t('header.markAllRead')}
                </button>
              </div>

              <ul className="max-h-80 divide-y divide-hairline overflow-y-auto">
                {items.map((n) => (
                  <li key={n.id}>
                    <Link
                      to={n.path}
                      onClick={() => setOpenMenu(null)}
                      className={`flex gap-3 px-4 py-3 transition-colors hover:bg-surface-hover ${
                        n.isRead ? '' : 'bg-accent-subtle'
                      }`}
                    >
                      <span className="grid h-9 w-9 shrink-0 place-items-center rounded-pill bg-accent-soft text-accent">
                        <span className="material-symbols-outlined text-[19px]">
                          {n.icon}
                        </span>
                      </span>
                      <span className="min-w-0 flex-1">
                        <span className="flex items-center justify-between gap-2">
                          <span className="truncate text-[13px] font-bold text-ink">
                            {L(n.title)}
                          </span>
                          {!n.isRead && (
                            <span
                              aria-hidden="true"
                              className="h-2 w-2 shrink-0 rounded-pill bg-brand-500"
                            />
                          )}
                        </span>
                        <span className="mt-0.5 line-clamp-2 block text-[12.5px] text-ink-muted">
                          {L(n.body)}
                        </span>
                        <span className="mt-1 block text-[11px] text-ink-subtle">
                          {relativeTime(n.createdAt)}
                        </span>
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>

              <Link
                to="/notifications"
                onClick={() => setOpenMenu(null)}
                className="block border-t border-hairline px-4 py-3 text-center text-[13px] font-bold text-accent hover:bg-surface-hover"
              >
                {t('sidebar.notifications')}
              </Link>
            </div>
          )}
        </div>

        <div aria-hidden="true" className="h-6 w-px bg-hairline" />

        {/* Hồ sơ */}
        <div className="relative" ref={profileRef}>
          <button
            type="button"
            onClick={() => setOpenMenu(openMenu === 'profile' ? null : 'profile')}
            aria-expanded={openMenu === 'profile'}
            aria-haspopup="true"
            className="flex min-h-[44px] items-center gap-2.5 rounded-md px-1.5 text-left transition-colors duration-200 hover:bg-surface-hover sm:px-2"
          >
            <span className="grid h-9 w-9 shrink-0 place-items-center rounded-pill bg-action text-[12.5px] font-bold text-white">
              {initialsOf(displayName)}
            </span>
            <span className="hidden leading-tight sm:block">
              <span className="block max-w-[10rem] truncate text-[13px] font-bold text-ink">
                {displayName}
              </span>
              <span className="block text-[11px] font-medium text-ink-muted">
                {isAdmin ? t('header.admin') : t('header.student')}
              </span>
            </span>
            <span
              aria-hidden="true"
              className={`material-symbols-outlined text-[18px] text-ink-subtle transition-transform duration-200 ${
                openMenu === 'profile' ? 'rotate-180' : ''
              }`}
            >
              expand_more
            </span>
          </button>

          {openMenu === 'profile' && (
            <div className="absolute right-0 mt-2 w-[min(18rem,calc(100vw-2rem))] origin-top-right overflow-hidden rounded-lg border border-hairline bg-surface shadow-lg">
              <div className="border-b border-hairline px-4 py-3">
                <p className="truncate text-[14px] font-bold text-ink">
                  {displayName}
                </p>
                <p className="truncate text-[12px] text-ink-muted">
                  {user?.email}
                </p>
              </div>

              <div className="py-1">
                <Link
                  to="/profile"
                  onClick={() => setOpenMenu(null)}
                  className="flex min-h-[44px] items-center gap-3 px-4 text-[13.5px] text-ink transition-colors hover:bg-surface-hover"
                >
                  <span className="material-symbols-outlined text-[20px] text-ink-subtle">
                    person
                  </span>
                  {t('header.profile')}
                </Link>

                <button
                  type="button"
                  onClick={toggleTheme}
                  className="flex min-h-[44px] w-full items-center justify-between gap-3 px-4 text-left text-[13.5px] text-ink transition-colors hover:bg-surface-hover"
                >
                  <span className="flex items-center gap-3">
                    <span className="material-symbols-outlined text-[20px] text-ink-subtle">
                      {isDark ? 'dark_mode' : 'light_mode'}
                    </span>
                    {t('header.theme')}
                  </span>
                  <span className="text-[12px] font-bold text-accent">
                    {isDark ? t('header.themeDark') : t('header.themeLight')}
                  </span>
                </button>
              </div>

              <div className="border-t border-hairline py-1">
                <button
                  type="button"
                  onClick={handleLogout}
                  className="flex min-h-[44px] w-full items-center gap-3 px-4 text-left text-[13.5px] font-semibold text-danger transition-colors hover:bg-danger-bg"
                >
                  <span className="material-symbols-outlined text-[20px]">
                    logout
                  </span>
                  {t('header.logout')}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
