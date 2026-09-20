import React from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

interface NavItem {
  to: string;
  icon: string;
  labelKey: string;
  /** Khớp cả các đường dẫn con, ví dụ /flashcard/3/study. */
  matchPrefix?: boolean;
}

interface NavGroup {
  titleKey: string;
  items: NavItem[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    titleKey: 'sidebar.learning',
    items: [
      { to: '/dashboard', icon: 'grid_view', labelKey: 'sidebar.dashboard' },
      { to: '/flashcard', icon: 'style', labelKey: 'sidebar.flashcard', matchPrefix: true },
      { to: '/writing', icon: 'edit_note', labelKey: 'sidebar.writing', matchPrefix: true },
      { to: '/listening', icon: 'headphones', labelKey: 'sidebar.listening', matchPrefix: true },
      { to: '/reading', icon: 'menu_book', labelKey: 'sidebar.reading', matchPrefix: true },
      { to: '/speaking', icon: 'mic', labelKey: 'sidebar.speaking', matchPrefix: true },
      { to: '/exam', icon: 'quiz', labelKey: 'sidebar.exam', matchPrefix: true },
    ],
  },
  {
    titleKey: 'sidebar.tracking',
    items: [{ to: '/statistics', icon: 'bar_chart', labelKey: 'sidebar.statistics' }],
  },
  {
    titleKey: 'sidebar.interactionSystem',
    items: [
      { to: '/chat', icon: 'forum', labelKey: 'sidebar.chat', matchPrefix: true },
      { to: '/notifications', icon: 'notifications', labelKey: 'sidebar.notifications' },
      { to: '/settings', icon: 'manage_accounts', labelKey: 'sidebar.settings' },
    ],
  },
];

interface SidebarProps {
  isOpen: boolean;
  /** Đóng sidebar sau khi chọn mục, chỉ áp dụng trên màn hình nhỏ. */
  onNavigate?: () => void;
  /** Mở cửa sổ chat thu nhỏ gắn ở góc màn hình. */
  onOpenAssistant: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  isOpen,
  onNavigate,
  onOpenAssistant,
}) => {
  const { t } = useTranslation();

  return (
    <aside
      id="main-sidebar"
      aria-label={t('sidebar.mainNav')}
      className={[
        'fixed inset-y-0 left-0 z-40 flex flex-col bg-sidebar',
        'transition-[width,transform] duration-300 ease-out',
        'lg:translate-x-0',
        isOpen
          ? 'w-sidebar translate-x-0'
          : 'w-sidebar -translate-x-full lg:w-sidebar-sm',
      ].join(' ')}
    >
      {/* Logo */}
      <div
        className={`flex h-header shrink-0 items-center gap-2.5 ${
          isOpen ? 'px-5' : 'px-5 lg:justify-center lg:px-0'
        }`}
      >
        <span className="grid h-9 w-9 shrink-0 place-items-center rounded-md bg-action text-white">
          <span className="material-symbols-outlined text-[22px]">school</span>
        </span>
        <span
          className={`whitespace-nowrap text-[17px] font-extrabold tracking-tight text-white transition-opacity duration-200 ${
            isOpen ? 'opacity-100' : 'opacity-0 lg:hidden'
          }`}
        >
          En-Learning
        </span>
      </div>

      {/* Danh sách điều hướng */}
      <nav className="flex-1 overflow-y-auto overflow-x-hidden px-3 pb-4">
        {NAV_GROUPS.map((group) => (
          <div key={group.titleKey} className="mb-5 last:mb-0">
            <p
              className={`px-3 pb-2 pt-1 text-[11px] font-bold uppercase tracking-[0.08em] text-sidebar-muted transition-opacity duration-200 ${
                isOpen ? 'opacity-100' : 'opacity-0 lg:h-0 lg:overflow-hidden lg:p-0'
              }`}
            >
              {t(group.titleKey)}
            </p>

            <ul className="flex flex-col gap-1">
              {group.items.map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={!item.matchPrefix}
                    title={t(item.labelKey)}
                    onClick={onNavigate}
                    className={({ isActive }) =>
                      [
                        'group flex items-center rounded-md text-[13.5px] font-semibold',
                        'transition-colors duration-200',
                        // Chiều cao tối thiểu 44px cho vùng chạm trên cảm ứng.
                        'min-h-[44px] px-3',
                        isOpen ? 'gap-3' : 'gap-3 lg:justify-center lg:px-0',
                        isActive
                          ? 'bg-sidebar-active text-white'
                          : 'text-sidebar-fg hover:bg-white/[0.07] hover:text-white',
                      ].join(' ')
                    }
                  >
                    <span className="material-symbols-outlined shrink-0 text-[21px]">
                      {item.icon}
                    </span>
                    <span
                      className={`whitespace-nowrap transition-opacity duration-200 ${
                        isOpen ? 'opacity-100' : 'opacity-0 lg:hidden'
                      }`}
                    >
                      {t(item.labelKey)}
                    </span>
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>

      {/* Khối trợ lý AI ghim ở đáy, theo mô tả chức năng 9 */}
      <div className={`shrink-0 p-3 ${isOpen ? '' : 'lg:hidden'}`}>
        <div className="rounded-lg bg-white/[0.06] p-4 text-center">
          <span className="mx-auto mb-2 grid h-10 w-10 place-items-center rounded-pill bg-white/15 text-white">
            <span className="material-symbols-outlined text-[22px]">
              smart_toy
            </span>
          </span>
          <p className="text-[13.5px] font-bold text-white">
            {t('sidebar.assistantTitle')}
          </p>
          <p className="mt-1 text-[11.5px] leading-snug text-sidebar-muted">
            {t('sidebar.assistantDesc')}
          </p>
          <button
            type="button"
            onClick={onOpenAssistant}
            className="mt-3 flex min-h-[40px] w-full items-center justify-center rounded-pill bg-action px-4 text-[13px] font-bold text-white ring-1 ring-inset ring-brand-400/60 transition-colors duration-200 hover:bg-action-hover"
          >
            {t('sidebar.assistantCta')}
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
