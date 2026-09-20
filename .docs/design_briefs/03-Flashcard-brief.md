# 03-Flashcard-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased`.
* **Main Container:** `w-full max-w-7xl mx-auto px-4 py-6 md:px-6 md:py-8 lg:px-8 flex flex-col gap-6 md:gap-8 flex-1`.
* **Home Header Section:** `flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between w-full`.
* **Search & Filter Stack:** `flex flex-col gap-4 w-full`.
* **Category Section List:** `flex flex-col gap-8 w-full`.
* **Category Section Container:** `flex flex-col gap-4 w-full`.
* **Deck Grid Standard:** `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
* **Filter Bar Container:** `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4`.
* **Form Page Container:** `w-full max-w-2xl mx-auto flex flex-col gap-6 py-4`.
* **Form Card Standard:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col gap-6`.
* **Detail Page Container:** `w-full max-w-4xl mx-auto flex flex-col gap-8 py-4`.
* **Detail Hero Card:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col gap-6`.
* **Preview Word Grid:** `grid grid-cols-1 md:grid-cols-2 gap-3 w-full`.
* **Study Session Container:** `w-full max-w-2xl mx-auto min-h-[calc(100vh-8rem)] flex flex-col justify-between py-6 px-4 gap-6`.
* **Study Card Wrapper:** `w-full flex items-center justify-center flex-1 my-auto`.
* **Modal Overlay:** `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
* **Modal Box:** `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
* **Responsive Rules:**
  * Mobile (< 768px): Lưới bộ từ co về 1 cột `grid-cols-1`; nút CTA chính mở rộng toàn hàng `w-full`; thanh tab danh mục trượt ngang `overflow-x-auto`; bộ lọc xếp chồng `flex-col`.
  * Tablet (768px - 1023px): Lưới bộ từ hiển thị 2 cột `grid-cols-2`; khoảng cách chuẩn `gap-6`.
  * Desktop (>= 1024px): Lưới bộ từ cố định 3 cột `grid-cols-3`; khung trang giới hạn `max-w-7xl` căn giữa `mx-auto`.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

* **FlashcardHeaderAction [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm w-full sm:w-auto`.
  * Interaction: `hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer`.

* **FlashcardSearchBar [DUMB]**:
  * Box Style: `relative w-full`.
  * Input: `w-full h-11 pl-11 pr-10 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all`.
  * Search Icon: `absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4 pointer-events-none`.
  * Clear Button: `absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1 rounded-md transition-colors cursor-pointer`.

* **FlashcardCategoryFilterTabs [DUMB]**:
  * Box Style: `flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none w-full`.
  * Tab Item Default: `px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-colors cursor-pointer`.
  * Tab Item Active: `px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap bg-sky-600 text-white border border-sky-600 shadow-sm`.

* **CategoryRowHeader [DUMB]**:
  * Box Style: `flex items-center justify-between w-full py-1`.
  * Left Group: `flex items-center gap-2.5`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900`.
  * Typography Count: `px-2.5 py-0.5 rounded-full bg-slate-100 text-xs font-semibold text-slate-600`.
  * View More CTA: `inline-flex items-center gap-1 text-sm font-semibold text-sky-600 hover:text-sky-700 hover:underline transition-all cursor-pointer`.

* **DeckGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.

* **DeckCard [DUMB]**:
  * Box Style: `group relative flex flex-col justify-between bg-white border border-slate-200 rounded-xl shadow-sm p-5 hover:border-slate-300 hover:shadow-md transition-all cursor-pointer`.
  * Top Row: `flex items-start justify-between gap-3`.
  * Typography Title: `text-base md:text-lg font-bold text-slate-900 group-hover:text-sky-600 transition-colors line-clamp-1`.
  * Typography Description: `text-sm text-slate-500 line-clamp-2 mt-2 leading-relaxed min-h-[40px]`.
  * Meta Row: `flex items-center justify-between text-xs font-medium text-slate-500 mt-4 pt-4 border-t border-slate-100`.
  * Delete Action: `opacity-0 group-hover:opacity-100 p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-all cursor-pointer`.

* **ProgressBar [DUMB]**:
  * Box Style: `w-full flex flex-col gap-1.5`.
  * Track: `w-full h-2 rounded-full bg-slate-100 overflow-hidden`.
  * Fill: `h-full rounded-full bg-sky-600 transition-all duration-300`.
  * Completed Fill: `bg-emerald-600`.
  * Label: `flex items-center justify-between text-xs font-medium text-slate-500`.

* **LevelBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-semibold border`.
  * Beginner Level (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate Level (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced Level (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.

* **EmptyDeckState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Action Button: `h-10 px-5 rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.

* **FlashcardSkeleton [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
  * Card Skeleton: `bg-white border border-slate-200 rounded-xl p-5 flex flex-col gap-4 animate-pulse pointer-events-none`.
  * Line Skeleton: `h-4 rounded bg-slate-200`.
  * Title Skeleton: `h-6 w-2/3 rounded bg-slate-200`.
  * Bar Skeleton: `h-2 w-full rounded bg-slate-100`.

* **DeleteDeckModal [DUMB]**:
  * Overlay: `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
  * Modal Box: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Warning: `text-sm text-slate-600 leading-relaxed`.
  * Button Group: `flex items-center justify-end gap-3 pt-2`.
  * Cancel Button: `h-10 px-4 rounded-xl border border-slate-200 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.
  * Confirm Delete Button: `h-10 px-4 rounded-xl bg-red-600 text-sm font-semibold text-white hover:bg-red-700 active:bg-red-800 disabled:bg-slate-300 disabled:cursor-not-allowed transition-colors cursor-pointer`.

* **CategoryPageHeader [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Subtitle: `text-sm text-slate-500 font-medium`.

* **Breadcrumb [DUMB]**:
  * Box Style: `flex items-center gap-2 text-sm text-slate-500`.
  * Link: `hover:text-sky-600 transition-colors cursor-pointer`.
  * Separator: `text-slate-300 select-none`.
  * Active Item: `font-semibold text-slate-900 pointer-events-none`.

* **FlashcardFilterBar [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4`.
  * Search Container: `flex-1 min-w-[240px]`.
  * Filter Selects: `flex items-center gap-3 flex-wrap`.

* **PaginationControl [DUMB]**:
  * Box Style: `flex items-center justify-center gap-2 w-full py-4`.
  * Button Base: `min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition-colors cursor-pointer flex items-center justify-center`.
  * Default State: `border-slate-200 bg-white text-slate-700 hover:bg-slate-50`.
  * Active State: `border-sky-600 bg-sky-600 text-white font-semibold shadow-sm`.
  * Disabled State: `border-slate-200 text-slate-300 bg-slate-50 cursor-not-allowed`.

* **CreateDeckHeader [DUMB]**:
  * Box Style: `flex items-center gap-4 w-full`.
  * Back Button: `p-2.5 rounded-xl border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-colors cursor-pointer`.
  * Typography Title: `text-2xl font-bold text-slate-900 tracking-tight`.

* **CreateDeckForm [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col gap-6`.

* **FormField [DUMB]**:
  * Box Style: `flex flex-col gap-1.5 w-full`.
  * Typography Label: `text-sm font-semibold text-slate-700`.
  * Required Mark: `text-red-500 ml-0.5`.
  * Input Standard: `w-full h-11 px-4 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all`.
  * Input Error: `border-red-400 focus:ring-red-500/20 focus:border-red-500`.
  * Typography Error Message: `text-xs text-red-600 font-medium mt-0.5`.

* **SelectDropdown [DUMB]**:
  * Box Style: `relative w-full`.
  * Select Standard: `w-full h-11 px-4 pr-10 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Select Icon: `absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **SubmitButton [DUMB]**:
  * Box Style: `h-11 px-6 rounded-xl font-semibold text-sm shadow-sm transition-all flex items-center justify-center gap-2 cursor-pointer w-full`.
  * Active State: `bg-sky-600 text-white hover:bg-sky-700 active:bg-sky-800`.
  * Disabled State: `bg-slate-200 text-slate-400 cursor-not-allowed shadow-none`.

* **DeckDetailHero [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col gap-6`.
  * Top Meta: `flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4`.
  * Typography Title: `text-2xl md:text-3xl font-bold text-slate-900 tracking-tight`.
  * Typography Description: `text-sm md:text-base text-slate-600 leading-relaxed`.
  * Badge Group: `flex items-center gap-2.5 flex-wrap`.
  * Action Row: `flex flex-col sm:flex-row items-center gap-3 pt-4 border-t border-slate-100`.
  * Primary Action: `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold hover:bg-sky-700 active:bg-sky-800 transition-colors shadow-sm cursor-pointer w-full sm:w-auto`.
  * Secondary Action: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.

* **WordPreviewSection [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full`.
  * Header: `flex items-center justify-between`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Note: `text-xs text-slate-500 font-medium`.
  * Grid: `grid grid-cols-1 md:grid-cols-2 gap-3 w-full`.

* **WordPreviewItem [DUMB]**:
  * Box Style: `p-4 rounded-xl border border-slate-200 bg-white hover:border-slate-300 transition-all flex flex-col gap-1.5`.
  * Term Row: `flex items-center justify-between gap-2`.
  * Typography Term: `text-base font-bold text-slate-900`.
  * Typography POS: `text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-600`.
  * Typography Phonetic: `text-xs text-slate-500 font-mono`.
  * Typography Meaning: `text-sm text-slate-700 mt-1`.

* **EmptyPreviewState [DUMB]**:
  * Box Style: `w-full p-8 border border-dashed border-slate-200 rounded-xl flex flex-col items-center justify-center text-center gap-2 bg-slate-50`.
  * Typography Title: `text-sm font-semibold text-slate-700`.
  * Typography Description: `text-xs text-slate-500`.

* **StudyHeader [DUMB]**:
  * Box Style: `flex items-center justify-between w-full py-3 border-b border-slate-200`.
  * Exit Button: `inline-flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-slate-800 transition-colors cursor-pointer`.
  * Typography Deck Title: `text-base font-bold text-slate-900 line-clamp-1 max-w-[200px] sm:max-w-md`.
  * Counter Badge: `px-3 py-1 rounded-full bg-sky-50 text-xs font-bold text-sky-700`.

* **StudyProgressBar [DUMB]**:
  * Box Style: `w-full h-2 rounded-full bg-slate-100 overflow-hidden`.
  * Fill: `h-full rounded-full bg-sky-600 transition-all duration-300`.

* **FlashcardItem [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-3xl shadow-md p-6 sm:p-10 flex flex-col items-center text-center gap-6 min-h-[440px] justify-center relative`.
  * Image Container: `w-32 h-32 sm:w-40 sm:h-40 rounded-2xl overflow-hidden bg-slate-50 border border-slate-100 flex items-center justify-center shadow-inner`.
  * Image: `w-full h-full object-cover`.
  * Word Line: `flex items-center gap-3 justify-center`.
  * Typography Word: `text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight`.
  * Typography Phonetic: `text-sm sm:text-base text-slate-500 font-mono`.
  * Definition Box: `w-full p-4 rounded-2xl bg-sky-50/60 border border-sky-100 flex flex-col gap-2`.
  * Typography Meaning: `text-lg sm:text-xl font-bold text-sky-900`.
  * Typography Explanation: `text-sm text-slate-600 leading-relaxed`.

* **AudioPronounceButton [DUMB]**:
  * Box Style: `p-2.5 rounded-full bg-sky-100 text-sky-700 hover:bg-sky-200 active:bg-sky-300 transition-colors cursor-pointer flex items-center justify-center`.
  * Playing State: `bg-sky-600 text-white animate-pulse ring-4 ring-sky-100`.

* **StudyControlBar [DUMB]**:
  * Box Style: `flex items-center justify-center w-full pt-4`.
  * Next Button: `h-12 px-8 rounded-xl bg-sky-600 text-white text-base font-bold shadow-md hover:bg-sky-700 active:bg-sky-800 transition-all cursor-pointer w-full sm:w-64`.
  * Complete Button: `h-12 px-8 rounded-xl bg-emerald-600 text-white text-base font-bold shadow-md hover:bg-emerald-700 active:bg-emerald-800 transition-all cursor-pointer w-full sm:w-64`.

* **StudyCompletionModal [DUMB]**:
  * Overlay: `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
  * Modal Box: `w-full max-w-sm bg-white border border-slate-200 rounded-3xl shadow-xl p-8 flex flex-col items-center text-center gap-5`.
  * Icon Circle: `w-16 h-16 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center text-3xl font-bold`.
  * Typography Title: `text-2xl font-bold text-slate-900`.
  * Typography Subtitle: `text-sm text-slate-600 leading-relaxed`.
  * Highlight Number: `text-base font-semibold text-emerald-700`.
  * Finish CTA: `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold hover:bg-sky-700 active:bg-sky-800 transition-colors shadow-sm cursor-pointer w-full`.

---

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary Color:** `bg-sky-600`, `text-sky-600`, `border-sky-600`.
* **Primary Hover:** `bg-sky-700`, `text-sky-700`.
* **Primary Active:** `bg-sky-800`.
* **Primary Light / Background Active:** `bg-sky-50`, `text-sky-700`, `border-sky-100`.
* **Page Background:** `bg-slate-50`.
* **Card / Container Surface:** `bg-white`.
* **Border Default:** `border-slate-200`.
* **Border Hover:** `border-slate-300`.
* **Border Light / Divider:** `border-slate-100`.
* **Text Primary:** `text-slate-900`.
* **Text Secondary:** `text-slate-500`, `text-slate-600`.
* **Text Muted / Placeholder:** `text-slate-400`.
* **Success / Completed:** `bg-emerald-50`, `text-emerald-700`, `bg-emerald-600`, `border-emerald-200`.
* **Warning:** `bg-amber-50`, `text-amber-700`, `border-amber-200`.
* **Danger / Error / Delete:** `bg-red-50`, `text-red-600`, `bg-red-600`, `border-red-200`, `border-red-400`.
* **Advanced / Special Level (C1, C2, IELTS, TOEIC):** `bg-purple-50`, `text-purple-700`, `border-purple-200`.
* **Disabled:** `bg-slate-200`, `text-slate-400`, `cursor-not-allowed`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG dùng mã màu HEX, RGB tự chế (chỉ dùng Tailwind color classes).
  * TUYỆT ĐỐI KHÔNG dùng hiệu ứng gradient đa sắc, không hiệu ứng kính mờ (glassmorphism/backdrop-blur phức tạp).
  * TUYỆT ĐỐI KHÔNG sử dụng màu neon chói lóa.

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockCategories = [
  { key: "ALL", label: "Tất cả" },
  { key: "RECENT", label: "Truy cập gần đây" },
  { key: "IELTS", label: "IELTS" },
  { key: "TOEIC", label: "TOEIC" },
  { key: "FOUNDATION", label: "Nền tảng" },
  { key: "WORK", label: "Công việc" }
];

const mockLevels = [
  { value: "ALL", label: "Tất cả trình độ" },
  { value: "A1", label: "A1 - Căn bản" },
  { value: "A2", label: "A2 - Sơ cấp" },
  { value: "B1", label: "B1 - Trung cấp" },
  { value: "B2", label: "B2 - Trung cao cấp" },
  { value: "C1", label: "C1 - Cao cấp" },
  { value: "C2", label: "C2 - Thành thạo" }
];

const mockRecentDecks = [
  {
    id: "deck_rec_01",
    name: "300 Từ vựng tiếng Anh giao tiếp công sở",
    description: "Bộ từ vựng thực chiến dành cho dân văn phòng, đàm phán và gửi thư điện tử.",
    category: "WORK",
    level: "B1",
    totalWords: 30,
    progressPercent: 65,
    canDelete: true
  },
  {
    id: "deck_rec_02",
    name: "500 Từ vựng IELTS Academic Band 7.0+",
    description: "Tổng hợp từ vựng học thuật quan trọng cho bài thi Viết và Nói IELTS.",
    category: "IELTS",
    level: "C1",
    totalWords: 50,
    progressPercent: 40,
    canDelete: false
  },
  {
    id: "deck_rec_03",
    name: "Từ vựng cốt lõi TOEIC 750+",
    description: "Các nhóm từ thường xuyên xuất hiện trong bài thi TOEIC Part 5, 6 và 7.",
    category: "TOEIC",
    level: "B2",
    totalWords: 40,
    progressPercent: 80,
    canDelete: true
  }
];

const mockIeltsDecks = [
  {
    id: "deck_ielts_01",
    name: "IELTS Speaking Part 1 Topics",
    description: "Từ vựng và cụm từ theo các chủ đề thường gặp: Gia đình, Sở thích, Du lịch.",
    category: "IELTS",
    level: "B2",
    totalWords: 35,
    progressPercent: 20,
    canDelete: true
  },
  {
    id: "deck_ielts_02",
    name: "IELTS Writing Task 2 Lexical Resource",
    description: "Từ vựng nâng cao chuyên sâu về Giáo dục, Môi trường và Trí tuệ nhân tạo.",
    category: "IELTS",
    level: "C1",
    totalWords: 45,
    progressPercent: 0,
    canDelete: false
  },
  {
    id: "deck_ielts_03",
    name: "IELTS Collocations Thông dụng",
    description: "Các cụm từ kết hợp tự nhiên giúp cải thiện độ mượt mà khi nói và viết.",
    category: "IELTS",
    level: "B2",
    totalWords: 50,
    progressPercent: 90,
    canDelete: true
  }
];

const mockToeicDecks = [
  {
    id: "deck_toeic_01",
    name: "Từ vựng TOEIC Văn phòng & Nhân sự",
    description: "Thuật ngữ tuyển dụng, hợp đồng lao động và phỏng vấn ứng viên.",
    category: "TOEIC",
    level: "B1",
    totalWords: 25,
    progressPercent: 100,
    canDelete: true
  },
  {
    id: "deck_toeic_02",
    name: "Từ vựng TOEIC Tài chính & Ngân hàng",
    description: "Các thuật ngữ về giao dịch ngân hàng, báo cáo thuế và đầu tư dự án.",
    category: "TOEIC",
    level: "B2",
    totalWords: 30,
    progressPercent: 15,
    canDelete: true
  },
  {
    id: "deck_toeic_03",
    name: "Từ vựng TOEIC Du lịch & Khách sạn",
    description: "Từ vựng đặt vé máy bay, dịch vụ khách sạn và giải quyết phản hồi khách hàng.",
    category: "TOEIC",
    level: "A2",
    totalWords: 20,
    progressPercent: 50,
    canDelete: false
  }
];

const mockFoundationDecks = [
  {
    id: "deck_found_01",
    name: "1000 Từ vựng tiếng Anh cơ bản A1",
    description: "Bộ từ nền móng cho người mới bắt đầu hoặc mất gốc học lại từ đầu.",
    category: "FOUNDATION",
    level: "A1",
    totalWords: 50,
    progressPercent: 75,
    canDelete: false
  },
  {
    id: "deck_found_02",
    name: "Động từ bất quy tắc thiết yếu",
    description: "Toàn bộ các động từ bất quy tắc hay gặp nhất trong giao tiếp thường ngày.",
    category: "FOUNDATION",
    level: "A2",
    totalWords: 60,
    progressPercent: 30,
    canDelete: true
  },
  {
    id: "deck_found_03",
    name: "Từ vựng Miêu tả Cảm xúc & Tính cách",
    description: "Cách diễn đạt cảm xúc, tính cách con người một cách phong phú và chính xác.",
    category: "FOUNDATION",
    level: "B1",
    totalWords: 30,
    progressPercent: 0,
    canDelete: true
  }
];

const mockWorkDecks = [
  {
    id: "deck_work_01",
    name: "Viết Email Công việc Chuẩn Quốc tế",
    description: "Mẫu câu và từ vựng thông dụng dùng trong thư điện tử trao đổi công việc hàng ngày.",
    category: "WORK",
    level: "B1",
    totalWords: 25,
    progressPercent: 85,
    canDelete: true
  },
  {
    id: "deck_work_02",
    name: "Thuyết trình & Đàm phán Dự án",
    description: "Từ vựng mở đầu, dẫn dắt, bảo vệ quan điểm và chốt hợp đồng đối tác.",
    category: "WORK",
    level: "B2",
    totalWords: 35,
    progressPercent: 10,
    canDelete: false
  },
  {
    id: "deck_work_03",
    name: "Thuật ngữ Tiếng Anh ngành Công nghệ (IT)",
    description: "Từ vựng dành cho Lập trình viên, Quản trị dự án và Kỹ sư hệ thống.",
    category: "WORK",
    level: "B2",
    totalWords: 40,
    progressPercent: 45,
    canDelete: true
  }
];

const mockDeckDetail = {
  id: "deck_rec_02",
  name: "500 Từ vựng IELTS Academic Band 7.0+",
  description: "Tổng hợp các từ vựng học thuật quan trọng, được tuyển chọn từ các đề thi IELTS chính thức gần đây. Giúp bạn nâng cấp vốn từ trong kỹ năng Viết và Nói một cách tự nhiên và chính xác nhất.",
  category: "IELTS",
  level: "C1",
  totalWords: 20,
  progressPercent: 40,
  learnedWords: 8
};

const mockWordPreviews = [
  {
    id: "w_01",
    term: "Ubiquitous",
    phonetic: "/juːˈbɪk.wə.təs/",
    partOfSpeech: "adj",
    meaningVi: "Có mặt ở khắp mọi nơi, phổ biến rộng rãi"
  },
  {
    id: "w_02",
    term: "Mitigate",
    phonetic: "/ˈmɪt.ɪ.ɡeɪt/",
    partOfSpeech: "verb",
    meaningVi: "Giảm nhẹ, làm bớt nghiêm trọng hoặc bớt đau đớn"
  },
  {
    id: "w_03",
    term: "Pragmatic",
    phonetic: "/præɡˈmæt.ɪk/",
    partOfSpeech: "adj",
    meaningVi: "Thực tế, thực dụng, dựa trên giải pháp cụ thể"
  },
  {
    id: "w_04",
    term: "Substantiate",
    phonetic: "/səbˈstæn.ʃi.eɪt/",
    partOfSpeech: "verb",
    meaningVi: "Chứng minh, xác minh bằng bằng chứng rõ ràng"
  },
  {
    id: "w_05",
    term: "Dichotomy",
    phonetic: "/daɪˈkɒt.ə.mi/",
    partOfSpeech: "noun",
    meaningVi: "Sự phân đôi, sự đối lập giữa hai điều đối nghịch"
  },
  {
    id: "w_06",
    term: "Exacerbate",
    phonetic: "/ɪɡˈzæs.ə.beɪt/",
    partOfSpeech: "verb",
    meaningVi: "Làm trầm trọng thêm một vấn đề hoặc căn bệnh"
  }
];

const mockStudySession = {
  deckId: "deck_rec_02",
  deckName: "500 Từ vựng IELTS Academic Band 7.0+",
  currentIndex: 5,
  totalCards: 20,
  currentCard: {
    id: "w_05",
    term: "Ubiquitous",
    phonetic: "/juːˈbɪk.wə.təs/",
    meaningVi: "Có mặt ở khắp mọi nơi, phổ biến rộng rãi",
    explanationVi: "Thường dùng để miêu tả công nghệ, điện thoại di động hoặc các hiện tượng xã hội xuất hiện tràn ngập trong đời sống thường nhật.",
    imageUrl: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=400&q=80",
    audioUrl: "https://api.dictionary.example.com/audio/ubiquitous.mp3"
  }
};

const mockCreateDeckInitial = {
  name: "Từ vựng IELTS Task 1 biểu đồ",
  category: "IELTS",
  level: "B2",
  totalWords: 20
};

const mockEmptyState = {
  title: "Không tìm thấy bộ từ vựng nào",
  description: "Không có bộ Flashcard nào khớp với từ khóa tìm kiếm hoặc bộ lọc hiện tại của bạn.",
  actionLabel: "Xóa bộ lọc"
};

const mockDeleteModalData = {
  deckName: "Viết Email Công việc Chuẩn Quốc tế",
  warningText: "Hành động này sẽ xóa vĩnh viễn bộ Flashcard và toàn bộ lịch sử tiến độ học tập đã lưu của bộ từ này. Bạn không thể khôi phục sau khi xóa."
};

const mockCompletionModalData = {
  totalLearned: 20,
  deckName: "500 Từ vựng IELTS Academic Band 7.0+",
  congratsMessage: "Tuyệt vời! Bạn đã hoàn thành toàn bộ 20 từ vựng của phiên học hôm nay. Tiến độ của bạn đã được lưu tự động vào hồ sơ."
};
```
