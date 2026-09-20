### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-[#F7F9FF] dark:bg-slate-900 text-slate-900 dark:text-slate-100 flex flex-col antialiased`.

* **Header:** `sticky top-0 z-40 bg-white/95 dark:bg-slate-900/95 backdrop-blur-sm border-b border-[#E5E8EE] dark:border-slate-800 h-16 w-full flex items-center justify-between px-4 sm:px-6`.

* **Body Container:** `flex-1 flex w-full relative`.

* **Sidebar Desktop:** `fixed top-16 left-0 bottom-0 w-64 bg-white dark:bg-slate-900 border-r border-[#E5E8EE] dark:border-slate-800 flex flex-col z-30 transition-all duration-300 ease-in-out md:translate-x-0`.

* **Mobile Drawer:** Dùng chung style với Sidebar nhưng sử dụng `-translate-x-full` để ẩn đi và có thêm overlay `fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-20 md:hidden`.

* **Main Content:** `flex-1 flex flex-col min-w-0 transition-all duration-300 ease-in-out md:ml-64`.

* **Content Container:** `w-full max-w-7xl mx-auto p-4 sm:p-6 lg:p-8 flex flex-col gap-8 flex-1`.

* **Card chuẩn:** `bg-white dark:bg-slate-800 border border-[#E5E8EE] dark:border-slate-700/80 rounded-2xl p-6 shadow-sm`.

* **Footer:** `w-full bg-white dark:bg-slate-900 border-t border-[#E5E8EE] dark:border-slate-800 px-6 sm:px-8 py-8 mt-auto`. Bên trong sử dụng container `max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8`.

* **Responsive:** Desktop dùng Sidebar cố định bên trái, Main Content đẩy `ml-64`; Tablet/Mobile ẩn Sidebar bằng Drawer; Main Content trải hết `w-full`.

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

* **Logo [DUMB]**:

  * Box Style: `flex items-center gap-2.5 group`.
  * Typography: `text-xl font-bold tracking-tight text-[#008FD5]`.
  * Interaction: `transition-transform group-hover:scale-105 cursor-pointer`.

* **NotificationIcon [DUMB]**:

  * Box Style: `relative p-2 rounded-lg`.
  * Badge: `absolute top-1.5 right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 ring-2 ring-white dark:ring-slate-900`.
  * Typography: `text-[10px] font-bold text-white`.
  * Interaction: `text-slate-500 hover:text-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer`.

* **UserDropdown [DUMB]**:

  * Box Style: `absolute right-0 mt-2 w-72 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-[#E5E8EE] dark:border-slate-700 overflow-hidden`.
  * Typography: `text-sm text-slate-700 dark:text-slate-200`.
  * Interaction: `hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors cursor-pointer`.

* **Sidebar [DUMB]**:

  * Box Style: `flex-1 py-4 overflow-y-auto overflow-x-hidden flex flex-col gap-1`.
  * Typography Category: `text-[11px] font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider`.
  * Typography Item: `text-sm font-medium`.

* **MenuItem [DUMB]**:

  * Box Style: `flex items-center gap-3 px-3 py-2.5 mx-3 rounded-lg`.
  * Typography: `text-sm font-medium`.
  * Default: `text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-200`.
  * Active: `bg-[#E6F4FA] dark:bg-sky-950 text-[#008FD5] dark:text-sky-400 font-semibold`.
  * Interaction: `transition-colors cursor-pointer group`.

* **MobileDrawer [DUMB]**:

  * Box Style: `fixed top-16 left-0 bottom-0 w-64 bg-white dark:bg-slate-900 border-r border-[#E5E8EE] dark:border-slate-800 z-30`.
  * Backdrop: `fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-20 md:hidden`.

* **MainContent [DUMB]**:

  * Box Style: `w-full max-w-7xl mx-auto p-4 sm:p-6 lg:p-8 flex flex-col gap-8 flex-1`.
  * Child Card Standard: `bg-white dark:bg-slate-800 border border-[#E5E8EE] dark:border-slate-700/80 rounded-2xl p-6 shadow-sm`.

* **GlobalFooter [DUMB]**:

  * Box Style: `w-full bg-white dark:bg-slate-900 border-t border-[#E5E8EE] dark:border-slate-800 px-6 sm:px-8 py-8 mt-auto`.
  * Grid Container: `max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8`.

* **FooterColumn [DUMB]**:

  * Box Style: `flex flex-col gap-3`.
  * Typography Title: `text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider`.
  * Typography Link: `text-xs text-slate-500 dark:text-slate-400`.
  * Interaction: `hover:text-[#008FD5] dark:hover:text-sky-400 transition-colors cursor-pointer`.

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary:** `#008FD5`.

* **Primary Hover:** `#007BB8`.

* **Primary Light / Active:** `#E6F4FA`.

* **Page Background:** `#F7F9FF`.

* **Surface / Card / Header / Sidebar:** `bg-white`.

* **Text Primary:** `text-slate-900`.

* **Text Secondary:** `text-slate-500` / `text-slate-600`.

* **Border:** `#E5E8EE`.

* **Notification / Error:** `bg-red-500 text-white`.

* **Success:** `text-emerald-600` / `bg-emerald-50`.

* **Warning:** `text-amber-600` / `bg-amber-50`.

* Có hỗ trợ Dark mode với tiền tố `dark:` (ví dụ `dark:bg-slate-900`, `dark:text-white`).

* Không dùng gradient mạnh, glassmorphism hoặc màu neon.

* Không dùng màu làm tín hiệu trạng thái duy nhất.

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockNotificationCount = 3;

const mockUser = {
  id: "user_001",
  name: "Trịnh Xuân Diện",
  avatarUrl: "https://i.pravatar.cc/150?u=enlearning-user",
  role: "USER"
};

const mockMenuItems = [
  { id: "dashboard", label: "Dashboard", path: "/dashboard", iconName: "grid_view" },
  { id: "flashcards", label: "Flashcard", path: "/flashcards", iconName: "style" },
  { id: "writing", label: "Luyện viết", path: "/writing", iconName: "edit_note" },
  { id: "listening", label: "Luyện nghe", path: "/listening", iconName: "headphones" },
  { id: "exams", label: "Bài kiểm tra", path: "/exams", iconName: "quiz" },
  { id: "statistics", label: "Thống kê", path: "/statistics", iconName: "bar_chart" },
  { id: "recommendations", label: "Gợi ý học tập", path: "/recommendations", iconName: "auto_awesome" },
  { id: "chat", label: "Chat", path: "/chat", iconName: "forum" },
  { id: "profile", label: "Hồ sơ & Cài đặt", path: "/profile", iconName: "manage_accounts" }
];

const mockFooterData = [
  {
    title: "En-Learning",
    links: [
      { label: "Hỗ trợ người học", url: "/support" }
    ]
  },
  { 
    title: "Chính sách",
    links: [
      { label: "Chính sách bảo mật", url: "/privacy" },
      { label: "Điều khoản sử dụng", url: "/terms" },
      { label: "Quy định học tập & chứng chỉ", url: "/rules" }
    ]
  },
  {
    title: "Liên hệ",
    links: [
      { label: "Trung tâm hỗ trợ", url: "/support" },
      { label: "Gửi phản hồi", url: "/feedback" }
    ]
  }
];