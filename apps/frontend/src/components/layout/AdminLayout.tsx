import React, { useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, NavLink, Navigate, useNavigate } from 'react-router-dom';

import { useAuth } from '../../contexts/AuthContext';
import { Button } from '../ui/Button';
import LanguageSwitch from './LanguageSwitch';

/** Nhãn để ở dạng khoá dịch, dịch lúc dựng để đổi được theo VI/EN. */
const NAV_ITEMS = [
  { to: '/admin', icon: 'monitoring', labelKey: 'admin.dashboard', end: true },
  { to: '/admin/users', icon: 'group', labelKey: 'admin.users' },
  { to: '/admin/content', icon: 'library_books', labelKey: 'admin.content' },
  { to: '/admin/topics', icon: 'sell', labelKey: 'admin.topics' },
  { to: '/admin/notifications', icon: 'campaign', labelKey: 'admin.notificationsTitle' },
];

/**
 * Khung của phân hệ quản trị.
 *
 * Tách hẳn khỏi khu vực người học: sidebar riêng, không có trợ lý AI,
 * và chặn ngay từ đầu nếu tài khoản không phải quản trị viên.
 */
const AdminLayout: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isAdmin, user, logout } = useAuth();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);

  // Người dùng thường lạc vào đây thì đưa sang trang báo thiếu quyền.
  if (!isAdmin) return <Navigate to="/403" replace />;

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="min-h-screen">
      <aside
        aria-label={t('admin.navAria')}
        className={`fixed inset-y-0 left-0 z-40 flex w-sidebar flex-col bg-sidebar transition-transform duration-300 ease-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-header shrink-0 items-center gap-2.5 px-5">
          <span className="grid h-9 w-9 shrink-0 place-items-center rounded-md bg-action text-white">
            <span aria-hidden="true" className="material-symbols-outlined text-[22px]">
              admin_panel_settings
            </span>
          </span>
          <div className="min-w-0">
            <p className="truncate text-[15px] font-extrabold tracking-tight text-white">
              {t('admin.title')}
            </p>
            <p className="truncate text-[11px] text-sidebar-muted">En-Learning</p>
          </div>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 pb-4">
          <ul className="flex flex-col gap-1">
            {NAV_ITEMS.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.end}
                  onClick={() => setIsOpen(false)}
                  className={({ isActive }) =>
                    `group flex min-h-[44px] items-center gap-3 rounded-md px-3 text-[13.5px] font-semibold transition-colors duration-200 ${
                      isActive
                        ? 'bg-sidebar-active text-white'
                        : 'text-sidebar-fg hover:bg-white/[0.07] hover:text-white'
                    }`
                  }
                >
                  <span
                    aria-hidden="true"
                    className="material-symbols-outlined shrink-0 text-[21px]"
                  >
                    {item.icon}
                  </span>
                  {t(item.labelKey)}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <div className="shrink-0 border-t border-white/10 p-3">
          <Link
            to="/dashboard"
            className="flex min-h-[44px] items-center gap-3 rounded-md px-3 text-[13.5px] font-semibold text-sidebar-fg transition-colors hover:bg-white/[0.07] hover:text-white"
          >
            <span aria-hidden="true" className="material-symbols-outlined text-[21px]">
              school
            </span>
            {t('admin.backToLearning')}
          </Link>
        </div>
      </aside>

      {isOpen && (
        <button
          type="button"
          aria-label={t('sidebar.closeNav')}
          onClick={() => setIsOpen(false)}
          className="fixed inset-0 z-30 bg-brand-950/40 backdrop-blur-sm lg:hidden"
        />
      )}

      <div className="flex min-h-screen flex-col lg:ml-sidebar">
        <header className="sticky top-0 z-30 flex h-header items-center justify-between gap-4 border-b border-hairline bg-surface-main/85 px-4 backdrop-blur sm:px-6">
          <button
            type="button"
            onClick={() => setIsOpen((v) => !v)}
            aria-label={t('header.toggleSidebar')}
            className="grid h-10 w-10 place-items-center rounded-md text-ink-muted transition-colors hover:bg-surface-hover hover:text-ink lg:hidden"
          >
            <span aria-hidden="true" className="material-symbols-outlined text-[24px]">
              menu
            </span>
          </button>

          <p className="min-w-0 flex-1 truncate text-[15px] font-extrabold tracking-tight text-ink">
            {t('admin.area')}
          </p>

          <div className="flex items-center gap-3">
            {/* Khu quản trị cũng phải đổi được ngôn ngữ, không chỉ khu học. */}
            <LanguageSwitch />

            <span className="hidden text-right sm:block">
              <span className="block max-w-[12rem] truncate text-[13px] font-bold text-ink">
                {user?.fullName}
              </span>
              <span className="block text-[11px] text-ink-muted">
                {t('header.admin')}
              </span>
            </span>

            <Button variant="subtle" size="sm" icon="logout" onClick={handleLogout}>
              {t('header.logout')}
            </Button>
          </div>
        </header>

        <main className="flex-1">{children}</main>
      </div>
    </div>
  );
};

export default AdminLayout;
