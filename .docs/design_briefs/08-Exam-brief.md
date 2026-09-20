# 08-Exam-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased`.
* **Main Container Chung:** `w-full max-w-7xl mx-auto px-4 py-6 md:px-6 md:py-8 lg:px-8 flex flex-col gap-6 md:gap-8 flex-1`.
* **Category Vertical Layout (Màn 1):** `w-full max-w-5xl mx-auto flex flex-col gap-6 md:gap-8 py-4`.
  * **Progress Summary Banner Section:** `w-full bg-white border border-slate-200 rounded-2xl p-5 md:p-6 shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-6`.
  * **Category List Vertical Container:** `flex flex-col gap-4 md:gap-5 w-full`.
* **Exam List Layout (Màn 2):** `w-full flex flex-col gap-6`.
  * **Filter Bar Container:** `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
  * **Exam Grid Standard:** `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
* **Exam Taking Studio Layout (Màn 3 - Phòng thi tập trung):** `w-full min-h-screen bg-slate-50 flex flex-col justify-between`.
  * **Taking Header Bar:** `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * **Sticky Sub-Header Timer & Progress Bar:** `w-full bg-slate-50/95 backdrop-blur-xs border-b border-slate-200 px-4 py-2.5 md:px-8 flex items-center justify-between sticky top-[57px] z-20 shadow-2xs`.
  * **Taking Workspace Container:** `w-full max-w-7xl mx-auto px-4 py-6 md:py-8 flex flex-col lg:flex-row gap-6 lg:gap-8 items-start flex-1`.
  * **Exam Question Workspace (Main Area):** `flex-1 w-full flex flex-col gap-6`.
  * **Exam Question Item Card Box:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col gap-6 relative`.
  * **Exam Navigation Sidebar (Desktop):** `w-full lg:w-80 flex-shrink-0 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm flex flex-col gap-5 lg:sticky lg:top-28 self-start`.
  * **Mobile Floating Drawer (Mobile Nav):** `w-full fixed bottom-0 inset-x-0 z-40 bg-white border-t border-slate-200 p-4 shadow-2xl flex flex-col gap-3 lg:hidden`.
* **Exam Grading Layout (Màn 4 - Trung gian chấm thi):** `w-full min-h-[70vh] flex items-center justify-center px-4 py-12`.
  * **Grading Status Card Box:** `w-full max-w-lg bg-white border border-slate-200 rounded-2xl shadow-lg p-8 md:p-10 flex flex-col items-center text-center gap-6`.
* **Exam Result Layout (Màn 5 - Tổng kết & Đánh giá):** `w-full max-w-5xl mx-auto flex flex-col gap-8 py-4 md:py-6`.
  * **Result Header Box:** `w-full flex flex-col gap-4 pb-4 border-b border-slate-200`.
  * **Overall Score Card Box:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col md:flex-row items-center gap-6 md:gap-8`.
  * **Areas for Improvement Grid (2 Columns):** `grid grid-cols-1 md:grid-cols-2 gap-6 w-full`.
  * **Detailed Question Review List:** `w-full flex flex-col gap-4`.
  * **Result Action Footer:** `w-full bg-white border border-slate-200 rounded-2xl p-5 md:p-6 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4 mt-4`.
* **Modal Overlay:** `fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4`.
* **Modal Box Standard:** `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
* **Responsive Rules:**
  * **Mobile (< 768px):** Toàn bộ lưới co về 1 cột `flex-col`; Lưới bài kiểm tra chuyển `grid-cols-1`; Bảng ma trận câu hỏi trong phòng thi chuyển thành floating drawer phía dưới hoặc collapsible accordion; Các nút nộp bài và lưu tạm mở rộng full chiều ngang `w-full`; Đồng hồ đếm ngược gắn cố định ở header thu gọn với phông số đậm tối thiểu `text-base`.
  * **Tablet (768px - 1023px):** Lưới bài kiểm tra chia 2 cột `grid-cols-2`; Khung tóm tắt tiến độ xếp chồng dọc; Khu vực phòng thi hiển thị cột câu hỏi và cột điều hướng xếp theo chiều dọc (Sidebar đẩy xuống dưới câu hỏi); Lưới khu vực cần cải thiện chia 2 cột `grid-cols-2`.
  * **Desktop (>= 1024px):** Lưới bài kiểm tra cố định 3 cột `grid-cols-3`; Phòng thi chia 2 cột rõ rệt: Khu vực câu hỏi chính chiếm khoảng 72% chiều ngang (`flex-1`) và Thanh điều hướng danh sách câu hỏi cố định chiếm 28% chiều ngang (`w-80 lg:sticky lg:top-28`); Giới hạn khung nội dung tối đa `max-w-7xl` cho phòng thi và `max-w-5xl` cho danh mục và kết quả.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

#### Màn hình 1: Danh mục bài kiểm tra (ExamCategoryPage)

* **ExamCategoryHeader [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full text-center max-w-2xl mx-auto py-2`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Description: `text-sm md:text-base text-slate-500 font-normal leading-relaxed`.

* **ExamProgressSummaryBanner [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl p-5 md:p-6 shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-6`.
  * Left Wrapper: `flex-1 flex flex-col gap-3`.
  * Right Wrapper: `flex items-center gap-6 border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6 flex-shrink-0`.

* **ExamResumeCard [DUMB]**:
  * Box Style: `group w-full bg-sky-50/60 border border-sky-200 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:border-sky-300 hover:shadow-xs transition-all`.
  * Left Stack: `flex items-center gap-3.5 flex-1 min-w-0`.
  * Icon Container: `w-11 h-11 rounded-xl bg-sky-100 border border-sky-200 flex items-center justify-center text-sky-700 text-xl flex-shrink-0`.
  * Info Stack: `flex flex-col gap-1 min-w-0 flex-1`.
  * Typography Title: `text-sm md:text-base font-bold text-slate-900 group-hover:text-sky-700 transition-colors truncate`.
  * Meta Row: `flex items-center gap-3 text-xs text-slate-600 font-medium`.
  * Progress Badge: `px-2 py-0.5 rounded-md bg-white border border-sky-200 text-sky-700 font-semibold text-xs`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-9 px-4 rounded-xl bg-sky-600 text-white text-xs font-semibold shadow-xs hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **ExamQuickStats [DUMB]**:
  * Box Style: `flex items-center gap-5 sm:gap-8 justify-around md:justify-end w-full md:w-auto`.
  * Stat Item: `flex flex-col items-center md:items-start gap-0.5`.
  * Typography Value: `text-xl md:text-2xl font-black tracking-tight text-slate-900`.
  * Typography Label: `text-xs font-medium text-slate-500 uppercase tracking-wider`.

* **ExamCategoryList [DUMB]**:
  * Box Style: `flex flex-col gap-4 md:gap-5 w-full`.

* **ExamCategoryCard [DUMB]**:
  * Box Style: `group relative w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all cursor-pointer`.
  * Left Content: `flex items-start gap-4 sm:gap-5 flex-1`.
  * Icon / Image Box: `w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 text-2xl sm:text-3xl flex-shrink-0 group-hover:scale-105 transition-transform`.
  * Info Stack: `flex flex-col gap-1.5 flex-1 min-w-0`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900 group-hover:text-sky-600 transition-colors`.
  * Typography Description: `text-sm text-slate-500 leading-relaxed line-clamp-2`.
  * Meta Row: `flex items-center gap-3 mt-1`.
  * Total Exams Badge: `inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-slate-100 text-xs font-semibold text-slate-600 border border-slate-200/60`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-xs hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **ExamCategorySkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full animate-pulse pointer-events-none`.
  * Card Skeleton: `w-full bg-white border border-slate-200 rounded-2xl p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5`.
  * Icon Skeleton: `w-14 h-14 rounded-2xl bg-slate-200 flex-shrink-0`.
  * Content Skeleton: `flex flex-col gap-2.5 flex-1`.
  * Line Title: `h-6 w-1/3 rounded bg-slate-200`.
  * Line Description: `h-4 w-3/4 rounded bg-slate-100`.
  * Button Skeleton: `h-10 w-28 rounded-xl bg-slate-200 flex-shrink-0`.

* **ExamCategoryEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Box: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Action Button: `h-10 px-5 rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.

---

#### Màn hình 2: Danh sách bài kiểm tra theo danh mục (ExamListPage)

* **ExamListHeader [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full pb-2`.
  * Title Row: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Total Count: `text-sm font-medium text-slate-500`.

* **Breadcrumb [DUMB]**:
  * Box Style: `flex items-center gap-2 text-sm text-slate-500 flex-wrap`.
  * Link Item: `hover:text-sky-600 transition-colors cursor-pointer`.
  * Separator: `text-slate-300 select-none`.
  * Active Item: `font-semibold text-slate-900 pointer-events-none`.

* **ExamFilterBar [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col gap-4`.
  * Search & Dropdown Row: `flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 flex-wrap`.
  * Tabs & Reset Row: `flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-3 border-t border-slate-100`.

* **ExamSearchBar [DUMB]**:
  * Box Style: `relative w-full lg:max-w-xs`.
  * Input: `w-full h-11 pl-11 pr-10 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all`.
  * Search Icon: `absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4 pointer-events-none`.
  * Clear Button: `absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1 rounded-md transition-colors cursor-pointer`.

* **TopicFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[140px] flex-1 sm:flex-initial`.
  * Select: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **SkillFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[140px] flex-1 sm:flex-initial`.
  * Select: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **LevelFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[130px] flex-1 sm:flex-initial`.
  * Select: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **DurationFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[130px] flex-1 sm:flex-initial`.
  * Select: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **ExamStatusTabs [DUMB]**:
  * Box Style: `inline-flex items-center bg-slate-100 p-1 rounded-xl gap-1 border border-slate-200/60 overflow-x-auto max-w-full`.
  * Tab Default: `px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer whitespace-nowrap`.
  * Tab Active: `px-3 py-1.5 rounded-lg text-xs font-bold bg-white text-sky-700 shadow-xs whitespace-nowrap`.

* **ExamGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.

* **ExamCard [DUMB]**:
  * Box Style: `group relative flex flex-col justify-between bg-white border border-slate-200 rounded-2xl shadow-sm p-5 md:p-6 hover:border-sky-300 hover:shadow-md transition-all`.
  * Header Badges Row: `flex items-center justify-between gap-2 mb-3`.
  * Badge Group: `flex items-center gap-1.5 flex-wrap`.
  * Typography Title: `text-base md:text-lg font-bold text-slate-900 group-hover:text-sky-600 transition-colors line-clamp-2 leading-snug`.
  * Typography Description: `text-xs text-slate-500 line-clamp-2 mt-1.5 leading-relaxed`.
  * Metrics Row: `flex items-center gap-4 text-xs font-medium text-slate-500 mt-4 pt-3 border-t border-slate-100`.
  * Metric Item: `flex items-center gap-1.5`.
  * Best Score Row: `mt-3 p-2.5 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-between text-xs font-semibold text-emerald-700`.
  * Action Button Start: `mt-4 w-full h-10 px-4 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-xs hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer flex items-center justify-center gap-2`.
  * Action Button Resume: `mt-4 w-full h-10 px-4 rounded-xl bg-amber-500 text-white text-sm font-semibold shadow-xs hover:bg-amber-600 active:bg-amber-700 transition-colors cursor-pointer flex items-center justify-center gap-2`.

* **LevelBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-semibold border`.
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.

* **SkillBadge [DUMB]**:
  * Box Style: `inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-xs font-semibold border`.
  * Reading: `bg-blue-50 text-blue-700 border-blue-200`.
  * Listening: `bg-indigo-50 text-indigo-700 border-indigo-200`.
  * Writing: `bg-purple-50 text-purple-700 border-purple-200`.
  * Grammar & Vocab: `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Full Test: `bg-amber-50 text-amber-700 border-amber-200`.

* **DurationBadge [DUMB]**:
  * Box Style: `inline-flex items-center gap-1 text-xs font-medium text-slate-500`.
  * Icon: `w-3.5 h-3.5 text-slate-400`.

* **QuestionCountBadge [DUMB]**:
  * Box Style: `inline-flex items-center gap-1 text-xs font-medium text-slate-500`.
  * Icon: `w-3.5 h-3.5 text-slate-400`.

* **StatusBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border`.
  * Chưa làm (NOT_STARTED): `bg-slate-100 text-slate-600 border-slate-200`.
  * Đang làm (IN_PROGRESS): `bg-amber-50 text-amber-700 border-amber-200`.
  * Đã hoàn thành (COMPLETED): `bg-emerald-50 text-emerald-700 border-emerald-200`.

* **PaginationControl [DUMB]**:
  * Box Style: `flex items-center justify-center gap-2 w-full py-4`.
  * Button Base: `min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition-colors cursor-pointer flex items-center justify-center`.
  * Default Page: `border-slate-200 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100`.
  * Active Page: `border-sky-600 bg-sky-600 text-white font-semibold shadow-xs`.
  * Disabled: `border-slate-200 text-slate-300 bg-slate-50 cursor-not-allowed pointer-events-none`.

* **ExamListEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Box: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Reset Button: `h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-xs hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer`.

* **ExamListSkeleton [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full animate-pulse pointer-events-none`.
  * Card Skeleton: `w-full bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-4`.
  * Line Header: `h-4 w-1/3 rounded bg-slate-200`.
  * Line Title: `h-6 w-3/4 rounded bg-slate-200`.
  * Line Meta: `h-4 w-1/2 rounded bg-slate-100`.
  * Line Button: `h-10 w-full rounded-xl bg-slate-100 mt-2`.

---

#### Màn hình 3: Làm bài kiểm tra (Exam Taking Studio - ExamTakingPage)

* **ExamTakingHeader [DUMB]**:
  * Box Style: `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * Exit Button: `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl border border-slate-200 text-slate-600 hover:text-slate-900 hover:bg-slate-50 text-xs font-semibold transition-colors cursor-pointer`.
  * Center Title Box: `flex flex-col items-center text-center max-w-md truncate px-2`.
  * Typography Title: `text-sm md:text-base font-bold text-slate-900 truncate`.
  * Right Action Container: `flex items-center gap-3`.

* **ExamStickyTimerBar [DUMB]**:
  * Box Style: `w-full bg-slate-50/95 backdrop-blur-xs border-b border-slate-200 px-4 py-2.5 md:px-8 flex flex-col sm:flex-row items-center justify-between gap-3 sticky top-[57px] z-20 shadow-2xs`.
  * Left Timer Stack: `flex items-center gap-3 w-full sm:w-auto justify-between sm:justify-start`.
  * Right Progress Stack: `flex items-center gap-3 w-full sm:w-auto flex-1 max-w-sm justify-end`.

* **CountdownTimer [DUMB]**:
  * Normal Style (> 5 phút): `inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-white border border-slate-200 font-mono text-sm md:text-base font-bold text-slate-800 shadow-xs`.
  * Warning Style (<= 5 phút): `inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-rose-50 border border-rose-300 font-mono text-sm md:text-base font-black text-rose-600 animate-pulse shadow-xs`.
  * Timer Icon: `w-4 h-4`.

* **ExamProgressBar [DUMB]**:
  * Box Style: `w-full h-2 rounded-full bg-slate-200 overflow-hidden relative`.
  * Indicator: `h-full bg-sky-600 rounded-full transition-all duration-300 ease-out`.
  * Progress Label: `text-xs font-semibold text-slate-600 whitespace-nowrap`.

* **SaveDraftIndicator [DUMB]**:
  * Normal Saved Style: `inline-flex items-center gap-1.5 text-xs text-slate-500 font-medium`.
  * Saving Active Style: `inline-flex items-center gap-1.5 text-xs text-sky-600 font-semibold animate-pulse`.
  * Status Dot: `w-2 h-2 rounded-full`.

* **ExamTakingLayout [DUMB]**:
  * Box Style: `w-full max-w-7xl mx-auto px-4 py-6 md:py-8 flex flex-col lg:flex-row gap-6 lg:gap-8 items-start flex-1`.
  * Main Slot: `flex-1 w-full min-w-0`.
  * Sidebar Slot: `w-full lg:w-80 flex-shrink-0`.

* **ExamQuestionArea [DUMB]**:
  * Box Style: `w-full flex flex-col gap-6`.
  * Question Nav Footer: `flex items-center justify-between gap-4 pt-4 border-t border-slate-200`.
  * Prev Button: `inline-flex items-center gap-2 h-10 px-5 rounded-xl border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 text-sm font-semibold transition-colors cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed`.
  * Next Button: `inline-flex items-center gap-2 h-10 px-5 rounded-xl bg-sky-600 text-white hover:bg-sky-700 active:bg-sky-800 text-sm font-semibold transition-colors cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed`.

* **ExamInstructionBox [DUMB]**:
  * Box Style: `w-full bg-sky-50/70 border border-sky-200 rounded-2xl p-4 md:p-5 flex items-start gap-3.5 text-xs md:text-sm text-sky-900 leading-relaxed`.
  * Info Icon: `w-5 h-5 text-sky-600 flex-shrink-0 mt-0.5`.

* **QuestionItemCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col gap-6 relative`.
  * Top Meta Row: `flex items-center justify-between gap-2 pb-4 border-b border-slate-100`.
  * Question Index Tag: `inline-flex items-center gap-2 text-sm font-bold text-slate-900`.
  * Point Badge: `px-2.5 py-0.5 rounded-md bg-slate-100 text-slate-600 text-xs font-semibold`.
  * Flag Button Default: `inline-flex items-center gap-1.5 px-3 py-1 rounded-lg border border-slate-200 text-slate-500 hover:text-amber-600 hover:border-amber-300 text-xs font-semibold transition-colors cursor-pointer`.
  * Flag Button Active: `inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-amber-50 border border-amber-300 text-amber-700 text-xs font-bold shadow-2xs cursor-pointer`.

* **QuestionPromptDisplay [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full`.
  * Passage Box (Đoạn văn đọc hiểu): `p-5 rounded-xl bg-slate-50 border border-slate-200 text-sm text-slate-800 leading-relaxed max-h-72 overflow-y-auto font-serif`.
  * Audio Player Box (Bài nghe): `w-full p-4 rounded-xl bg-slate-100 border border-slate-200 flex items-center gap-3`.
  * Prompt Text: `text-base md:text-lg font-semibold text-slate-900 leading-snug`.

* **SingleChoiceQuestion [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full`.
  * Option Row: `group w-full p-4 rounded-xl border transition-all flex items-start gap-3.5 cursor-pointer text-left`.
  * Default State: `border-slate-200 bg-white hover:border-sky-300 hover:bg-slate-50/70 text-slate-800`.
  * Selected State: `border-sky-600 bg-sky-50/60 ring-1 ring-sky-600 text-sky-950 font-medium`.
  * Radio Dot Container: `w-5 h-5 rounded-full border flex items-center justify-center flex-shrink-0 mt-0.5`.
  * Radio Dot Default: `border-slate-300 group-hover:border-sky-400 bg-white`.
  * Radio Dot Selected: `border-sky-600 bg-sky-600`.
  * Inner Dot: `w-2 h-2 rounded-full bg-white`.
  * Option Label: `w-6 h-6 rounded-lg bg-slate-100 text-slate-700 font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5`.
  * Option Text: `text-sm leading-relaxed flex-1`.

* **MultipleChoiceQuestion [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full`.
  * Option Row: `group w-full p-4 rounded-xl border transition-all flex items-start gap-3.5 cursor-pointer text-left`.
  * Default State: `border-slate-200 bg-white hover:border-sky-300 hover:bg-slate-50/70 text-slate-800`.
  * Selected State: `border-sky-600 bg-sky-50/60 ring-1 ring-sky-600 text-sky-950 font-medium`.
  * Checkbox Box Container: `w-5 h-5 rounded-md border flex items-center justify-center flex-shrink-0 mt-0.5`.
  * Checkbox Default: `border-slate-300 group-hover:border-sky-400 bg-white`.
  * Checkbox Selected: `border-sky-600 bg-sky-600 text-white`.
  * Option Label: `w-6 h-6 rounded-lg bg-slate-100 text-slate-700 font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5`.
  * Option Text: `text-sm leading-relaxed flex-1`.

* **FillBlankQuestion [DUMB]**:
  * Box Style: `w-full flex flex-col gap-3`.
  * Input Container: `relative w-full max-w-lg`.
  * Input: `w-full h-12 px-4 rounded-xl border border-slate-300 bg-white text-base text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all font-medium`.
  * Word Count / Character Hint: `text-xs text-slate-500 mt-1`.

* **EssayQuestion [DUMB]**:
  * Box Style: `w-full flex flex-col gap-2.5`.
  * Textarea: `w-full h-56 p-4 rounded-xl border border-slate-300 bg-white text-sm md:text-base text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all leading-relaxed resize-y font-mono`.
  * Footer Info: `flex items-center justify-between text-xs text-slate-500 px-1`.
  * Word Counter: `font-semibold text-slate-700`.

* **ExamSidebarNavigation [DUMB]**:
  * Box Style: `w-full lg:w-80 flex-shrink-0 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm flex flex-col gap-5 lg:sticky lg:top-28 self-start`.
  * Header Title: `text-sm font-bold text-slate-900 uppercase tracking-wider flex items-center justify-between`.

* **QuestionNavPalette [DUMB]**:
  * Box Style: `grid grid-cols-5 sm:grid-cols-6 lg:grid-cols-5 gap-2 max-h-80 overflow-y-auto p-1`.

* **QuestionNavButton [DUMB]**:
  * Base Style: `h-10 rounded-xl border text-xs font-bold transition-all cursor-pointer flex items-center justify-center relative select-none`.
  * Chưa làm (UNANSWERED): `border-slate-200 bg-white text-slate-700 hover:border-slate-300 hover:bg-slate-50`.
  * Đã làm (ANSWERED): `border-sky-600 bg-sky-600 text-white shadow-xs`.
  * Đang xem (CURRENT FOCUS): `ring-2 ring-sky-500 ring-offset-2 border-sky-700 font-black`.
  * Đánh dấu xem lại (FLAGGED): `border-amber-400 bg-amber-50 text-amber-800 after:content-[''] after:w-2 after:h-2 after:bg-amber-500 after:rounded-full after:absolute after:top-1.5 after:right-1.5`.
  * Đã làm + Cắm cờ (ANSWERED & FLAGGED): `border-amber-400 bg-sky-600 text-white after:content-[''] after:w-2 after:h-2 after:bg-amber-300 after:rounded-full after:absolute after:top-1.5 after:right-1.5`.

* **ExamPaletteLegend [DUMB]**:
  * Box Style: `flex flex-wrap items-center gap-3 pt-3 border-t border-slate-100 text-xs text-slate-600`.
  * Legend Item: `flex items-center gap-1.5`.
  * Dot Answered: `w-3 h-3 rounded-md bg-sky-600`.
  * Dot Unanswered: `w-3 h-3 rounded-md border border-slate-300 bg-white`.
  * Dot Flagged: `w-3 h-3 rounded-md bg-amber-100 border border-amber-400 relative after:w-1.5 after:h-1.5 after:bg-amber-500 after:rounded-full`.

* **ExamQuickActionBar [DUMB]**:
  * Box Style: `flex flex-col gap-2.5 pt-4 border-t border-slate-100 w-full`.
  * Save Draft Button: `w-full h-10 px-4 rounded-xl border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 text-xs font-semibold transition-colors cursor-pointer flex items-center justify-center gap-2`.
  * Submit Exam Button: `w-full h-11 px-4 rounded-xl bg-emerald-600 text-white hover:bg-emerald-700 active:bg-emerald-800 text-sm font-bold shadow-xs transition-colors cursor-pointer flex items-center justify-center gap-2`.

* **ConfirmSubmitExamModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
  * Icon Box: `w-12 h-12 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center text-xl self-center`.
  * Typography Title: `text-lg font-bold text-slate-900 text-center`.
  * Typography Message: `text-sm text-slate-600 leading-relaxed text-center`.
  * Stats Warning Box: `p-3.5 rounded-xl bg-slate-50 border border-slate-200 flex flex-col gap-2 text-xs text-slate-700`.
  * Button Group: `flex items-center justify-end gap-3 mt-2`.
  * Cancel Button: `h-10 px-4 rounded-xl border border-slate-200 text-slate-700 text-sm font-medium hover:bg-slate-50 cursor-pointer`.
  * Confirm Button: `h-10 px-5 rounded-xl bg-emerald-600 text-white text-sm font-semibold shadow-xs hover:bg-emerald-700 cursor-pointer`.

* **ConfirmExitExamModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
  * Icon Box: `w-12 h-12 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center text-xl self-center`.
  * Typography Title: `text-lg font-bold text-slate-900 text-center`.
  * Typography Message: `text-sm text-slate-600 leading-relaxed text-center`.
  * Button Stack: `flex flex-col sm:flex-row items-center justify-end gap-2.5 mt-2`.
  * Stay Button: `w-full sm:w-auto h-10 px-4 rounded-xl border border-slate-200 text-slate-700 text-sm font-medium hover:bg-slate-50 cursor-pointer`.
  * Save & Exit Button: `w-full sm:w-auto h-10 px-4 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-xs hover:bg-sky-700 cursor-pointer`.
  * Discard Exit Button: `w-full sm:w-auto h-10 px-4 rounded-xl bg-rose-600 text-white text-sm font-semibold shadow-xs hover:bg-rose-700 cursor-pointer`.

* **ExamTakingSkeleton [DUMB]**:
  * Box Style: `w-full max-w-7xl mx-auto px-4 py-6 flex flex-col lg:flex-row gap-6 animate-pulse pointer-events-none`.
  * Question Box Skeleton: `flex-1 h-96 bg-slate-200 rounded-2xl`.
  * Sidebar Skeleton: `w-full lg:w-80 h-80 bg-slate-200 rounded-2xl`.

---

#### Màn hình 4: Đang chấm bài (ExamGradingPage)

* **ExamGradingLayout [DUMB]**:
  * Box Style: `w-full min-h-[70vh] flex items-center justify-center px-4 py-12`.

* **GradingStatusCard [DUMB]**:
  * Box Style: `w-full max-w-lg bg-white border border-slate-200 rounded-2xl shadow-lg p-8 md:p-10 flex flex-col items-center text-center gap-6`.

* **GradingSpinnerAnimation [DUMB]**:
  * Box Style: `w-16 h-16 rounded-2xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 text-3xl animate-spin`.

* **GradingStatusTitle [DUMB]**:
  * Typography: `text-xl md:text-2xl font-bold tracking-tight text-slate-900`.

* **GradingStatusMessage [DUMB]**:
  * Typography: `text-sm text-slate-600 leading-relaxed max-w-sm`.

* **GradingTypeHint [DUMB]**:
  * Trắc nghiệm (AUTO): `w-full p-3 rounded-xl bg-emerald-50 border border-emerald-100 text-xs font-semibold text-emerald-800`.
  * Tự luận / AI (AI_ANALYZING): `w-full p-3 rounded-xl bg-sky-50 border border-sky-100 text-xs font-semibold text-sky-800 flex items-center justify-center gap-2`.

---

#### Màn hình 5: Kết quả bài kiểm tra (ExamResultPage)

* **ExamResultHeader [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 w-full pb-4 border-b border-slate-200`.
  * Left Info Stack: `flex flex-col gap-1`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Meta Row: `flex items-center gap-3 text-xs md:text-sm text-slate-500 font-medium`.

* **OverallScoreSummaryCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col md:flex-row items-center gap-6 md:gap-8`.
  * Left Score Box: `flex flex-col items-center justify-center p-6 rounded-2xl bg-sky-50 border border-sky-100 min-w-[180px] flex-shrink-0`.
  * Right Details Stack: `flex flex-col gap-4 flex-1 text-center md:text-left w-full`.

* **ExamScoreBadge [DUMB]**:
  * Score Text: `text-4xl md:text-5xl font-black tracking-tight text-sky-600`.
  * Max Score Suffix: `text-base font-semibold text-slate-400 ml-1`.
  * Status Pill (Đạt / Không đạt): `mt-2.5 px-3.5 py-1 rounded-full text-xs font-bold`.
  * Passed Pill: `bg-emerald-100 text-emerald-800 border border-emerald-200`.
  * Failed Pill: `bg-rose-100 text-rose-800 border border-rose-200`.

* **ScoreBreakdownStats [DUMB]**:
  * Box Style: `grid grid-cols-2 sm:grid-cols-4 gap-3 w-full pt-3 border-t border-slate-100`.
  * Stat Block: `flex flex-col items-center md:items-start p-2.5 rounded-xl bg-slate-50 border border-slate-100`.
  * Value Text: `text-lg font-bold text-slate-900`.
  * Label Text: `text-xs text-slate-500 font-medium`.

* **ExamResultGeneralFeedback [DUMB]**:
  * Box Style: `p-4 rounded-xl bg-slate-50 border-l-4 border-sky-600 text-slate-700 text-sm leading-relaxed`.

* **AreasForImprovementSection [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 gap-6 w-full`.

* **WeakTopicsCard [DUMB]**:
  * Box Style: `w-full bg-white border border-rose-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4`.
  * Header: `flex items-center gap-2.5 text-base font-bold text-rose-800`.
  * List: `flex flex-col gap-3`.
  * Item: `p-3 rounded-xl bg-rose-50/60 border border-rose-100 flex items-start justify-between gap-3 text-xs md:text-sm`.
  * Topic Name: `font-bold text-slate-900`.
  * Error Ratio Badge: `px-2 py-0.5 rounded-md bg-rose-100 text-rose-700 font-semibold text-xs whitespace-nowrap`.

* **RecommendedNextStepsCard [DUMB]**:
  * Box Style: `w-full bg-white border border-sky-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4`.
  * Header: `flex items-center gap-2.5 text-base font-bold text-sky-800`.
  * List: `flex flex-col gap-2.5`.
  * Item: `flex items-start gap-2.5 text-xs md:text-sm text-slate-700 leading-relaxed`.
  * Target Icon: `w-4 h-4 text-sky-600 mt-0.5 flex-shrink-0`.

* **DetailedQuestionReviewSection [DUMB]**:
  * Box Style: `w-full flex flex-col gap-4`.
  * Section Header Row: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3`.
  * Section Title: `text-lg font-bold text-slate-900`.

* **QuestionReviewFilterTabs [DUMB]**:
  * Box Style: `inline-flex items-center bg-slate-100 p-1 rounded-xl gap-1 border border-slate-200/60 overflow-x-auto`.
  * Tab Default: `px-3 py-1 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer whitespace-nowrap`.
  * Tab Active: `px-3 py-1 rounded-lg text-xs font-bold bg-white text-sky-700 shadow-xs whitespace-nowrap`.

* **QuestionReviewItemCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-5 md:p-6 flex flex-col gap-4 transition-all`.

* **QuestionReviewHeader [DUMB]**:
  * Box Style: `flex items-center justify-between gap-2 pb-3 border-b border-slate-100`.
  * Question Index: `text-sm font-bold text-slate-900`.
  * Result Status Pill: `px-2.5 py-0.5 rounded-full text-xs font-bold border`.
  * Correct Status: `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Incorrect Status: `bg-rose-50 text-rose-700 border-rose-200`.
  * Skipped Status: `bg-slate-100 text-slate-600 border-slate-200`.
  * Points Text: `text-xs font-semibold text-slate-500`.

* **QuestionPromptReview [DUMB]**:
  * Box Style: `flex flex-col gap-3 text-sm text-slate-900 font-medium leading-relaxed`.

* **UserAnswerReviewBox [DUMB]**:
  * Base Box: `w-full p-3.5 rounded-xl border flex flex-col gap-1 text-xs md:text-sm`.
  * Correct Box: `bg-emerald-50/70 border-emerald-200 text-emerald-900`.
  * Incorrect Box: `bg-rose-50/70 border-rose-200 text-rose-900`.
  * Label: `text-[11px] font-bold uppercase tracking-wider opacity-75`.
  * Content: `font-semibold`.

* **CorrectAnswerBox [DUMB]**:
  * Box Style: `w-full p-3.5 rounded-xl bg-emerald-50/50 border border-emerald-200 text-xs md:text-sm text-emerald-950 flex flex-col gap-1`.
  * Label: `text-[11px] font-bold uppercase tracking-wider text-emerald-700`.
  * Content: `font-bold text-emerald-800`.

* **AnswerExplanationBox [DUMB]**:
  * Box Style: `w-full p-4 rounded-xl bg-slate-50 border border-slate-200/80 flex flex-col gap-1.5 text-xs md:text-sm text-slate-700 leading-relaxed`.
  * Header: `font-bold text-slate-900 flex items-center gap-1.5`.

* **ExamResultActionFooter [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl p-5 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4 mt-2`.
  * Retake Button: `inline-flex items-center justify-center gap-2 h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-xs hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto`.
  * Choose Other Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 text-sm font-semibold transition-colors cursor-pointer w-full sm:w-auto`.
  * Back to Categories Button: `inline-flex items-center justify-center gap-2 h-11 px-4 text-slate-500 hover:text-slate-900 text-sm font-medium transition-colors cursor-pointer w-full sm:w-auto`.

* **ExamResultSkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-6 w-full animate-pulse pointer-events-none`.
  * Header Skeleton: `w-full h-24 bg-slate-200 rounded-2xl`.
  * Score Skeleton: `w-full h-48 bg-slate-200 rounded-2xl`.
  * Detail Skeleton: `w-full h-72 bg-slate-200 rounded-2xl`.

---

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary Color (Chủ đạo En-Learning #008FD5):** `bg-sky-600`, `text-sky-600`, `border-sky-600`.
* **Primary Hover:** `bg-sky-700`, `text-sky-700`.
* **Primary Active:** `bg-sky-800`.
* **Primary Light / Nền mục chọn:** `bg-sky-50`, `text-sky-700`, `border-sky-100`.
* **Page Background:** `bg-slate-50`.
* **Card / Container Background:** `bg-white`.
* **Border Standard:** `border-slate-200`.
* **Border Hover:** `border-slate-300`, `border-sky-300`.
* **Border Subtle / Divider:** `border-slate-100`.
* **Text Primary:** `text-slate-900`.
* **Text Secondary:** `text-slate-600`, `text-slate-500`.
* **Text Muted / Placeholder:** `text-slate-400`.
* **Đồng hồ đếm ngược phòng thi (Countdown Timer States):**
  * Bình thường (> 5 phút): `bg-white text-slate-800 border-slate-200`.
  * Cảnh báo nguy cấp (<= 5 phút): `bg-rose-50 text-rose-600 border-rose-300 animate-pulse`.
* **Trạng thái ma trận nút câu hỏi (Question Nav Palette States):**
  * Chưa trả lời (UNANSWERED): `bg-white text-slate-700 border-slate-200 hover:bg-slate-50 hover:border-slate-300`.
  * Đã trả lời (ANSWERED): `bg-sky-600 text-white border-sky-600 shadow-xs`.
  * Đang mở xem (CURRENT FOCUS): `ring-2 ring-sky-500 ring-offset-2 border-sky-700 font-black`.
  * Đánh dấu xem lại (FLAGGED): `bg-amber-50 text-amber-800 border-amber-400 after:bg-amber-500`.
  * Đã làm + Đánh dấu xem lại (ANSWERED & FLAGGED): `bg-sky-600 text-white border-amber-400 after:bg-amber-300`.
* **Trạng thái chấm bài (Exam Grading):**
  * Vòng xoay chấm bài: `text-sky-600 bg-sky-50 border-sky-100`.
  * Trắc nghiệm tự động: `bg-emerald-50 text-emerald-800 border-emerald-100`.
  * Tự luận / AI phân tích: `bg-sky-50 text-sky-800 border-sky-100`.
* **Kết quả đối chiếu câu hỏi (Exam Result & Review):**
  * Câu trả lời đúng: `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Câu trả lời sai: `bg-rose-50 text-rose-700 border-rose-200`.
  * Câu bỏ qua / Chưa làm: `bg-slate-100 text-slate-600 border-slate-200`.
  * Kết quả Đạt (PASSED): `bg-emerald-100 text-emerald-800 border-emerald-200`.
  * Kết quả Không đạt (FAILED): `bg-rose-100 text-rose-800 border-rose-200`.
* **Kỹ năng kiểm tra (Skill Badges):**
  * Đọc hiểu (Reading): `bg-blue-50 text-blue-700 border-blue-200`.
  * Nghe hiểu (Listening): `bg-indigo-50 text-indigo-700 border-indigo-200`.
  * Viết luận (Writing): `bg-purple-50 text-purple-700 border-purple-200`.
  * Từ vựng & Ngữ pháp (Grammar & Vocab): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Bài thi đầy đủ (Full Test): `bg-amber-50 text-amber-700 border-amber-200`.
* **Cấp độ trình độ (Level Badges):**
  * Sơ cấp (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Trung cấp (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Nâng cao (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.
* **Trạng thái bài thi (Status Badges):**
  * Chưa làm (NOT_STARTED): `bg-slate-100 text-slate-600 border-slate-200`.
  * Đang làm dở (IN_PROGRESS): `bg-amber-50 text-amber-700 border-amber-200`.
  * Đã hoàn thành (COMPLETED): `bg-emerald-50 text-emerald-700 border-emerald-200`.
* **Khối cải thiện năng lực (Areas for Improvement):**
  * Chủ đề yếu (Weak Topics): `bg-white border-rose-200 text-rose-800`, item: `bg-rose-50/60 border-rose-100`.
  * Đề xuất rèn luyện (Next Steps): `bg-white border-sky-200 text-sky-800`, item: `text-slate-700`.
* **Nút bấm thao tác (Action Buttons):**
  * Nút chính (Start / Submit CTA): `bg-sky-600 hover:bg-sky-700 active:bg-sky-800 text-white`.
  * Nút nộp bài trực tiếp trong phòng thi: `bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white`.
  * Nút tiếp tục làm dở: `bg-amber-500 hover:bg-amber-600 active:bg-amber-700 text-white`.
  * Nút phụ / Thoát: `border-slate-300 bg-white text-slate-700 hover:bg-slate-50`.
  * Nút cảnh báo thoát / Hủy bài: `bg-rose-600 hover:bg-rose-700 text-white`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG sử dụng mã màu HEX hoặc RGB tùy tiện trong JSX, chỉ sử dụng các class Tailwind CSS được chuẩn hóa.
  * TUYỆT ĐỐI KHÔNG dùng hiệu ứng kính mờ sặc sỡ (glassmorphism), gradient chói mắt hay hoạt hình rườm rà gây mất tập trung trong phòng thi và bảng câu hỏi.
  * TUYỆT ĐỐI KHÔNG dùng màu neon gây mỏi mắt khi đọc các bài đọc dài.

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockExamCategories = [
  {
    id: "cat_ielts",
    type: "IELTS",
    title: "Bài kiểm tra IELTS",
    description: "Bộ đề đánh giá kỹ năng Học thuật (Academic) theo chuẩn đề thi quốc tế, bao gồm Reading, Listening và Writing với thang điểm Band 1.0 - 9.0.",
    totalExams: 32,
    iconName: "GraduationCap"
  },
  {
    id: "cat_toeic",
    type: "TOEIC",
    title: "Bài kiểm tra TOEIC",
    description: "Đánh giá khả năng sử dụng tiếng Anh trong môi trường giao tiếp kinh doanh và công sở quốc tế theo thang điểm chuẩn 10 - 990.",
    totalExams: 45,
    iconName: "Briefcase"
  },
  {
    id: "cat_foundation",
    type: "FOUNDATION",
    title: "Bài kiểm tra Nền tảng",
    description: "Khảo sát và củng cố toàn diện ngữ pháp cốt lõi, từ vựng trọng tâm và cấu trúc câu cho người mới bắt đầu hoặc ôn lại căn bản (A1 - B1).",
    totalExams: 24,
    iconName: "Sparkles"
  },
  {
    id: "cat_work",
    type: "WORK",
    title: "Tiếng Anh Công sở & Dự án",
    description: "Đánh giá năng lực tiếng Anh thực chiến qua các tình huống viết email, đàm phán hợp đồng, phân tích báo cáo và giao tiếp văn phòng.",
    totalExams: 28,
    iconName: "Building2"
  }
];

const mockExamProgressSummary = {
  resumeItem: {
    attemptId: "att_exam_2026_01",
    examId: "exam_ielts_rd_04",
    category: "IELTS",
    title: "IELTS Academic Reading Test 04 - Science & Society",
    topic: "ACADEMIC",
    level: "B2",
    remainingSeconds: 1420,
    totalQuestions: 40,
    answeredCount: 26,
    lastAttemptAt: "10:15 - Hôm nay"
  },
  completedExamsCount: 18,
  averageScore: 78.5
};

const mockTopicOptions = [
  { value: "ALL", label: "Tất cả chủ đề" },
  { value: "ACADEMIC", label: "Học thuật & Nghiên cứu" },
  { value: "BUSINESS", label: "Kinh doanh & Thương mại" },
  { value: "TECHNOLOGY", label: "Công nghệ & Kỹ thuật số" },
  { value: "ENVIRONMENT", label: "Môi trường & Sinh thái" },
  { value: "DAILY_COMMUNICATION", label: "Giao tiếp công sở" }
];

const mockSkillOptions = [
  { value: "ALL", label: "Tất cả kỹ năng" },
  { value: "READING", label: "Đọc hiểu (Reading)" },
  { value: "LISTENING", label: "Nghe hiểu (Listening)" },
  { value: "WRITING", label: "Viết luận (Writing)" },
  { value: "GRAMMAR_VOCAB", label: "Ngữ pháp & Từ vựng" },
  { value: "FULL_TEST", label: "Đề thi tổng hợp (Full Test)" }
];

const mockLevelOptions = [
  { value: "ALL", label: "Tất cả trình độ" },
  { value: "A1", label: "A1 - Mới bắt đầu" },
  { value: "A2", label: "A2 - Sơ cấp" },
  { value: "B1", label: "B1 - Trung cấp" },
  { value: "B2", label: "B2 - Trung cao cấp" },
  { value: "C1", label: "C1 - Cao cấp" }
];

const mockDurationOptions = [
  { value: "ALL", label: "Mọi thời lượng" },
  { value: "15", label: "15 phút (Mini Test)" },
  { value: "30", label: "30 phút" },
  { value: "45", label: "45 phút" },
  { value: "60", label: "60 phút (Chuẩn)" },
  { value: "90", label: "90 phút" }
];

const mockStatusTabs = [
  { value: "ALL", label: "Tất cả" },
  { value: "NOT_STARTED", label: "Chưa làm" },
  { value: "IN_PROGRESS", label: "Đang làm dở" },
  { value: "COMPLETED", label: "Đã hoàn thành" }
];

const mockExamList = [
  {
    id: "exam_ielts_rd_04",
    category: "IELTS",
    title: "IELTS Academic Reading Test 04 - Science & Society",
    topic: "ACADEMIC",
    skill: "READING",
    level: "B2",
    durationMinutes: 60,
    totalQuestions: 40,
    status: "IN_PROGRESS",
    description: "Bài thi Đọc hiểu học thuật gồm 3 đoạn văn nghiên cứu về biến đổi khí hậu, trí tuệ nhân tạo và di truyền học.",
    bestScore: null
  },
  {
    id: "exam_toeic_rc_08",
    category: "TOEIC",
    title: "TOEIC Reading Part 5 & 6 Practice Test 08",
    topic: "BUSINESS",
    skill: "GRAMMAR_VOCAB",
    level: "B1",
    durationMinutes: 45,
    totalQuestions: 46,
    status: "COMPLETED",
    description: "Đề rèn luyện tốc độ xử lý câu hỏi ngữ pháp điền từ và đọc hiểu thông báo nội bộ doanh nghiệp.",
    bestScore: 88
  },
  {
    id: "exam_work_email_02",
    category: "WORK",
    title: "Professional Workplace Communication & Email Drafting",
    topic: "DAILY_COMMUNICATION",
    skill: "FULL_TEST",
    level: "B2",
    durationMinutes: 30,
    totalQuestions: 20,
    status: "NOT_STARTED",
    description: "Kiểm tra kỹ năng xử lý phản hồi khách hàng, phân tích email khiếu nại và viết tóm tắt dự án.",
    bestScore: null
  }
];

const mockActiveExamSession = {
  examId: "exam_ielts_rd_04",
  examTitle: "IELTS Academic Reading Test 04 - Science & Society",
  category: "IELTS",
  skill: "READING",
  level: "B2",
  durationMinutes: 60,
  remainingSeconds: 1420,
  totalQuestions: 5,
  instructions: "Đọc kỹ từng đoạn văn và trả lời các câu hỏi từ 1 đến 5. Đối với câu trắc nghiệm, chọn đáp án đúng nhất. Đối với câu tự luận, đảm bảo không vượt quá số từ quy định. Bạn có thể lưu tạm bài làm bất cứ lúc nào.",
  isSavingDraft: false,
  lastSavedAt: "10:35:12",
  questions: [
    {
      id: "q_01",
      orderIndex: 1,
      sectionTitle: "Reading Passage 1: Renewable Energy Integration",
      questionType: "SINGLE_CHOICE",
      passageText: "The rapid expansion of renewable energy sources, particularly solar and wind, has fundamentally altered traditional electrical grids. While early skeptics raised concerns regarding the intermittent nature of solar irradiance and wind velocity, advanced utility-scale battery storage and algorithmic predictive modeling have dramatically stabilized power dispatch. In 2025, several European regions achieved continuous 72-hour operational cycles powered exclusively by zero-emission resources.",
      promptText: "According to the passage, what technology has significantly resolved the issue of intermittent renewable power generation?",
      points: 2,
      options: [
        { id: "opt_a", label: "A", text: "Hydroelectric auxiliary pumping systems" },
        { id: "opt_b", label: "B", text: "Utility-scale battery storage and algorithmic predictive modeling" },
        { id: "opt_c", label: "C", text: "Traditional coal-fired baseload backup units" },
        { id: "opt_d", label: "D", text: "Manual grid frequency modulation protocols" }
      ]
    },
    {
      id: "q_02",
      orderIndex: 2,
      questionType: "MULTIPLE_CHOICE",
      promptText: "Which TWO of the following statements are explicitly confirmed by the passage regarding modern electrical grids? (Choose 2 answers)",
      points: 3,
      options: [
        { id: "opt_a", label: "A", text: "Solar and wind are primary drivers of recent grid transformation." },
        { id: "opt_b", label: "B", text: "All European nations now run 100% on renewable energy permanently." },
        { id: "opt_c", label: "C", text: "Early skeptics worried about the fluctuation of sunlight and wind strength." },
        { id: "opt_d", label: "D", text: "Battery storage remains too expensive for commercial deployment." }
      ]
    },
    {
      id: "q_03",
      orderIndex: 3,
      questionType: "FILL_BLANK",
      promptText: "Fill in the blank with NO MORE THAN THREE WORDS from the passage: In 2025, certain European regions accomplished 72-hour continuous operations fueled exclusively by ______ resources.",
      points: 2
    },
    {
      id: "q_04",
      orderIndex: 4,
      questionType: "SINGLE_CHOICE",
      promptText: "What does the word 'intermittent' in the passage closest in meaning to?",
      points: 2,
      options: [
        { id: "opt_a", label: "A", text: "Continuous and reliable" },
        { id: "opt_b", label: "B", text: "Occurring at irregular intervals" },
        { id: "opt_c", label: "C", text: "Extremely destructive" },
        { id: "opt_d", label: "D", text: "Easily measurable" }
      ]
    },
    {
      id: "q_05",
      orderIndex: 5,
      sectionTitle: "Section 2: Analytical Response",
      questionType: "ESSAY",
      promptText: "Briefly explain in your own words (50 - 100 words) why traditional power grids struggled to accommodate renewable energy prior to the introduction of predictive modeling.",
      points: 5,
      maxWords: 100
    }
  ],
  userAnswers: {
    q_01: "opt_b",
    q_02: ["opt_a", "opt_c"],
    q_03: "zero-emission",
    q_04: "opt_b",
    q_05: "Traditional electrical networks required steady, predictable power supply to keep the grid frequency stable. Because weather conditions like wind and sunshine change rapidly, grid operators could not predict the exact output, causing potential blackouts."
  },
  flaggedQuestionIds: ["q_03"]
};

const mockGradingStatus = {
  attemptId: "att_exam_2026_01",
  status: "GRADING",
  gradingType: "AI_ANALYZING",
  title: "Đang chấm bài kiểm tra...",
  message: "Hệ thống đã lưu bài làm thành công. Các câu hỏi trắc nghiệm đã được chấm tự động, chuyên viên AI đang phân tích bài luận tự luận của bạn.",
  hintText: "Quá trình đánh giá thường mất từ 5 - 15 giây. Vui lòng giữ nguyên cửa sổ trình duyệt."
};

const mockExamResult = {
  attemptId: "att_exam_2026_01",
  examId: "exam_ielts_rd_04",
  examTitle: "IELTS Academic Reading Test 04 - Science & Society",
  category: "IELTS",
  skill: "READING",
  completedAt: "11:05 - 13/09/2026",
  timeSpentSeconds: 2180,
  totalScore: 12,
  maxScore: 14,
  correctAnswersCount: 4,
  totalQuestionsCount: 5,
  isPassed: true,
  generalComment: "Kết quả bài làm rất ấn tượng! Bạn thể hiện khả năng đọc hiểu học thuật xuất sắc, bắt từ khóa nhanh và giải thích bài luận rõ ràng, mạch lạc.",
  scoreBreakdown: {
    correctCount: 4,
    incorrectCount: 1,
    skippedCount: 0,
    totalQuestions: 5,
    accuracyPercent: 80
  },
  areasForImprovement: [
    {
      topicOrSkill: "Tóm tắt từ vựng chính xác (Word limitation)",
      incorrectCount: 1,
      totalCount: 2,
      recommendation: "Cần chú ý giới hạn số lượng từ cho phép trong đề bài dạng Fill Blank để không bị mất điểm đáng tiếc."
    },
    {
      topicOrSkill: "Nhận diện từ đồng nghĩa học thuật (Academic Paraphrasing)",
      incorrectCount: 0,
      totalCount: 3,
      recommendation: "Tiếp tục duy trì kỹ năng tra cứu và ghi chú các cặp từ đồng nghĩa trong các bài báo khoa học."
    }
  ],
  recommendedNextSteps: [
    "Luyện thêm bộ thẻ từ vựng 'Academic Vocabulary for Environmental Studies' (30 từ).",
    "Hoàn thành bài kiểm tra đọc số 05: 'Urban Planning and Green Architecture'.",
    "Xem lại quy tắc giới hạn từ trong bài thi IELTS Reading (NO MORE THAN THREE WORDS)."
  ],
  questionReviews: [
    {
      questionId: "q_01",
      orderIndex: 1,
      questionType: "SINGLE_CHOICE",
      promptText: "According to the passage, what technology has significantly resolved the issue of intermittent renewable power generation?",
      userAnswer: "Utility-scale battery storage and algorithmic predictive modeling",
      correctAnswer: "Utility-scale battery storage and algorithmic predictive modeling",
      isCorrect: true,
      earnedPoints: 2,
      maxPoints: 2,
      explanation: "Đoạn văn nêu rõ: '...advanced utility-scale battery storage and algorithmic predictive modeling have dramatically stabilized power dispatch.'"
    },
    {
      questionId: "q_02",
      orderIndex: 2,
      questionType: "MULTIPLE_CHOICE",
      promptText: "Which TWO of the following statements are explicitly confirmed by the passage regarding modern electrical grids? (Choose 2 answers)",
      userAnswer: ["Solar and wind are primary drivers of recent grid transformation.", "Early skeptics worried about the fluctuation of sunlight and wind strength."],
      correctAnswer: ["Solar and wind are primary drivers of recent grid transformation.", "Early skeptics worried about the fluctuation of sunlight and wind strength."],
      isCorrect: true,
      earnedPoints: 3,
      maxPoints: 3,
      explanation: "Đoạn văn xác nhận cả hai ý: sự thay đổi lưới điện do năng lượng mặt trời/gió và lo ngại ban đầu của những người hoài nghi về sự biến động của nắng và gió."
    },
    {
      questionId: "q_03",
      orderIndex: 3,
      questionType: "FILL_BLANK",
      promptText: "Fill in the blank with NO MORE THAN THREE WORDS from the passage: In 2025, certain European regions accomplished 72-hour continuous operations fueled exclusively by ______ resources.",
      userAnswer: "zero-emission",
      correctAnswer: "zero-emission",
      isCorrect: true,
      earnedPoints: 2,
      maxPoints: 2,
      explanation: "Từ khóa trong bài đọc: 'powered exclusively by zero-emission resources'."
    },
    {
      questionId: "q_04",
      orderIndex: 4,
      questionType: "SINGLE_CHOICE",
      promptText: "What does the word 'intermittent' in the passage closest in meaning to?",
      userAnswer: "Continuous and reliable",
      correctAnswer: "Occurring at irregular intervals",
      isCorrect: false,
      earnedPoints: 0,
      maxPoints: 2,
      explanation: "'Intermittent' có nghĩa là ngắt quãng, không liên tục, tương đương với 'Occurring at irregular intervals'. Bạn đã nhầm lẫn với từ trái nghĩa."
    },
    {
      questionId: "q_05",
      orderIndex: 5,
      questionType: "ESSAY",
      promptText: "Briefly explain in your own words (50 - 100 words) why traditional power grids struggled to accommodate renewable energy prior to the introduction of predictive modeling.",
      userAnswer: "Traditional electrical networks required steady, predictable power supply to keep the grid frequency stable. Because weather conditions like wind and sunshine change rapidly, grid operators could not predict the exact output, causing potential blackouts.",
      correctAnswer: "Đánh giá bởi AI: Bài luận đạt 5/5 điểm. Nêu đủ lý do về tần số lưới điện và tính bất định của thời tiết.",
      isCorrect: true,
      earnedPoints: 5,
      maxPoints: 5,
      explanation: "AI Feedback: Luận điểm rất rõ ràng, vốn từ vựng học thuật tự nhiên (steady power supply, grid frequency, unpredictable output). Độ dài 42 từ cô đọng, đúng trọng tâm."
    }
  ]
};
```
