# 06-Reading-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased`.
* **Main Container:** `w-full max-w-7xl mx-auto px-4 py-6 md:px-6 md:py-8 lg:px-8 flex flex-col gap-6 md:gap-8 flex-1`.
* **Category Vertical List Container:** `flex flex-col gap-4 md:gap-5 w-full max-w-4xl mx-auto`.
* **Category Card Row:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all cursor-pointer`.
* **Article List Container:** `w-full flex flex-col gap-6`.
* **Article Filter Bar Container:** `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
* **Article Grid Standard:** `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
* **Practice Top Navigation Bar:** `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
* **Practice Workspace Split Layout (2 Columns):** `w-full max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start`.
  * **Passage Column (Left):** `lg:col-span-7 w-full flex flex-col gap-4 lg:sticky lg:top-20 lg:max-h-[calc(100vh-7rem)] lg:overflow-y-auto pr-1`.
  * **Question & Answer Column (Right):** `lg:col-span-5 w-full flex flex-col gap-5`.
* **Practice Bottom Control Bar:** `sticky bottom-0 z-20 w-full bg-white/95 backdrop-blur-sm border-t border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between shadow-lg`.
* **Grading Status Center Layout:** `w-full max-w-2xl mx-auto min-h-[calc(100vh-12rem)] flex flex-col items-center justify-center p-4`.
* **Result Page Container:** `w-full max-w-7xl mx-auto flex flex-col gap-8 py-4`.
* **Result Score Summary Grid:** `grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 w-full`.
* **Result Comparison Layout (Split 2 Columns):** `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full items-start`.
  * **Result Passage Column (Left):** `lg:col-span-7 w-full flex flex-col gap-4 lg:sticky lg:top-20 lg:max-h-[calc(100vh-7rem)] lg:overflow-y-auto pr-1`.
  * **Question Review Column (Right):** `lg:col-span-5 w-full flex flex-col gap-4`.
* **Modal Overlay:** `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
* **Modal Box Standard:** `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
* **Responsive Rules:**
  * **Mobile (< 768px):** Toàn bộ chia cột co về 1 cột `flex-col`; Lưới bài đọc chuyển `grid-cols-1`; Màn hình làm bài và kết quả xếp chồng Bài đọc lên trên cụm Câu hỏi; Các nút CTA chính mở rộng toàn chiều ngang `w-full`.
  * **Tablet (768px - 1023px):** Lưới bài đọc hiển thị 2 cột `grid-cols-2`; Workspace làm bài hiển thị theo chiều dọc hoặc xếp lớp ưu tiên; Header giữ khoảng cách `px-6`.
  * **Desktop (>= 1024px):** Lưới bài đọc cố định 3 cột `grid-cols-3`; Workspace làm bài chia 2 cột tỉ lệ `7/5` song song với Bài đọc ghim cố định (`sticky top-20`) có thanh cuộn độc lập; Trang kết quả chia 2 cột `7/5` đối chiếu dẫn chứng trực quan; Giới hạn độ rộng nội dung `max-w-7xl mx-auto`.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

#### Màn hình 1: Danh mục bài luyện đọc (ReadingCategoryPage)

* **ReadingCategoryHeader [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full text-center max-w-2xl mx-auto py-4`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Description: `text-sm md:text-base text-slate-500 font-normal leading-relaxed`.

* **ReadingCategoryList [DUMB]**:
  * Box Style: `flex flex-col gap-4 md:gap-5 w-full max-w-4xl mx-auto`.

* **ReadingCategoryCard [DUMB]**:
  * Box Style: `group relative w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all cursor-pointer`.
  * Left Content: `flex items-start gap-4 flex-1`.
  * Icon Container: `w-14 h-14 rounded-xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 flex-shrink-0 group-hover:scale-105 transition-transform`.
  * Info Stack: `flex flex-col gap-1.5 flex-1`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900 group-hover:text-sky-600 transition-colors`.
  * Typography Description: `text-sm text-slate-500 leading-relaxed line-clamp-2`.
  * Badge Row: `flex items-center gap-2 mt-1`.
  * Count Badge: `px-2.5 py-0.5 rounded-full bg-slate-100 text-xs font-semibold text-slate-600`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **ReadingCategorySkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full max-w-4xl mx-auto`.
  * Card Skeleton: `w-full bg-white border border-slate-200 rounded-2xl p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 animate-pulse pointer-events-none`.
  * Icon Skeleton: `w-14 h-14 rounded-xl bg-slate-200 flex-shrink-0`.
  * Text Stack Skeleton: `flex flex-col gap-2.5 flex-1`.
  * Title Line: `h-6 w-1/3 rounded bg-slate-200`.
  * Description Line: `h-4 w-3/4 rounded bg-slate-100`.
  * Button Skeleton: `h-11 w-32 rounded-xl bg-slate-200 flex-shrink-0`.

* **CategoryEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Action Button: `h-10 px-5 rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.

---

#### Màn hình 2: Danh sách bài luyện theo danh mục (ReadingArticleListPage)

* **ArticleListHeader [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full pb-2`.
  * Breadcrumb Container: `w-full`.
  * Title Row: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Total Count: `text-sm font-medium text-slate-500`.

* **Breadcrumb [DUMB]**:
  * Box Style: `flex items-center gap-2 text-sm text-slate-500`.
  * Link Item: `hover:text-sky-600 transition-colors cursor-pointer`.
  * Separator: `text-slate-300 select-none`.
  * Active Item: `font-semibold text-slate-900 pointer-events-none`.

* **ArticleFilterBar [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
  * Left Group: `flex-1 flex flex-col sm:flex-row items-stretch sm:items-center gap-3`.
  * Right Group: `flex items-center gap-3 flex-wrap`.

* **ArticleSearchBar [DUMB]**:
  * Box Style: `relative w-full sm:max-w-xs`.
  * Input: `w-full h-11 pl-11 pr-10 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all`.
  * Search Icon: `absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4 pointer-events-none`.
  * Clear Button: `absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1 rounded-md transition-colors cursor-pointer`.

* **TopicFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[140px]`.
  * Select Box: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **LevelFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[130px]`.
  * Select Box: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **DurationFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[130px]`.
  * Select Box: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **ArticleStatusTabs [DUMB]**:
  * Box Style: `inline-flex items-center bg-slate-100 p-1 rounded-xl gap-1 border border-slate-200/60`.
  * Tab Default: `px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer`.
  * Tab Active: `px-3 py-1.5 rounded-lg text-xs font-bold bg-white text-sky-700 shadow-sm`.

* **ArticleGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.

* **ArticleCard [DUMB]**:
  * Box Style: `group relative flex flex-col justify-between bg-white border border-slate-200 rounded-2xl shadow-sm p-5 hover:border-sky-300 hover:shadow-md transition-all`.
  * Header Meta: `flex items-center justify-between gap-2 mb-3`.
  * Badge Group: `flex items-center gap-1.5 flex-wrap`.
  * Typography Title: `text-base md:text-lg font-bold text-slate-900 group-hover:text-sky-600 transition-colors line-clamp-2 leading-snug`.
  * Subtitle / Topic: `text-xs font-semibold uppercase tracking-wider text-slate-400 mt-1`.
  * Metrics Row: `flex items-center gap-4 text-xs font-medium text-slate-500 mt-4 pt-3 border-t border-slate-100`.
  * Metric Item: `flex items-center gap-1.5`.
  * Best Score Row: `mt-3 p-2 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-between text-xs font-semibold text-emerald-700`.
  * Action Button: `mt-4 w-full h-10 px-4 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer flex items-center justify-center gap-2`.

* **LevelBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-semibold border`.
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.

* **StatusBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border`.
  * Chưa làm (NOT_STARTED): `bg-slate-100 text-slate-600 border-slate-200`.
  * Đã làm (COMPLETED): `bg-emerald-50 text-emerald-700 border-emerald-200`.

* **DurationBadge [DUMB]**:
  * Box Style: `inline-flex items-center gap-1 text-xs font-medium text-slate-500`.
  * Icon: `w-3.5 h-3.5 text-slate-400`.

* **PaginationControl [DUMB]**:
  * Box Style: `flex items-center justify-center gap-2 w-full py-4`.
  * Button Base: `min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition-colors cursor-pointer flex items-center justify-center`.
  * Default: `border-slate-200 bg-white text-slate-700 hover:bg-slate-50`.
  * Active: `border-sky-600 bg-sky-600 text-white font-semibold shadow-sm`.
  * Disabled: `border-slate-200 text-slate-300 bg-slate-50 cursor-not-allowed`.

* **ArticleEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Reset Button: `h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer`.

* **ArticleListSkeleton [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
  * Card Skeleton: `bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-4 animate-pulse pointer-events-none`.
  * Badge Skeleton: `h-5 w-16 rounded bg-slate-200`.
  * Title Skeleton: `h-5 w-3/4 rounded bg-slate-200`.
  * Meta Skeleton: `h-4 w-1/2 rounded bg-slate-100`.
  * Button Skeleton: `h-10 w-full rounded-xl bg-slate-200 mt-2`.

---

#### Màn hình 3: Làm bài luyện đọc (ReadingPracticePage)

* **ReadingPracticeHeader [DUMB]**:
  * Box Style: `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * Left Group: `flex items-center gap-3`.
  * Exit Button: `inline-flex items-center justify-center w-9 h-9 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors cursor-pointer`.
  * Title Block: `flex flex-col`.
  * Typography Title: `text-base md:text-lg font-bold text-slate-900 line-clamp-1`.
  * Typography Topic: `text-xs text-slate-400 font-medium`.
  * Right Group: `flex items-center gap-4`.

* **CountdownTimer [DUMB]**:
  * Box Style Normal: `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-slate-100 border border-slate-200 text-slate-700 font-mono text-sm font-bold`.
  * Box Style Urgent (< 3 phút): `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-amber-50 border border-amber-300 text-amber-700 font-mono text-sm font-bold animate-pulse`.
  * Clock Icon: `w-4 h-4`.

* **AnswerProgressIndicator [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 text-xs md:text-sm font-semibold text-slate-600 bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-200`.
  * Counter Text: `font-bold text-sky-600`.

* **ReadingWorkspaceLayout [DUMB]**:
  * Box Style: `w-full max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start`.

* **ReadingPassagePanel [DUMB]**:
  * Box Style: `lg:col-span-7 w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4 lg:sticky lg:top-20 lg:max-h-[calc(100vh-7rem)] lg:overflow-y-auto`.

* **PassageToolbar [DUMB]**:
  * Box Style: `flex items-center justify-between pb-3 border-b border-slate-100`.
  * Toolbar Label: `text-xs font-bold uppercase tracking-wider text-slate-400`.
  * Font Control Group: `flex items-center gap-1.5 bg-slate-100 p-1 rounded-xl border border-slate-200/60`.
  * Font Button: `px-2.5 py-1 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-white transition-all cursor-pointer`.
  * Font Button Active: `bg-white text-sky-700 font-bold shadow-xs`.

* **PassageContentBody [DUMB]**:
  * Box Style: `flex flex-col gap-4 text-slate-800 leading-relaxed font-sans`.
  * Title: `text-xl md:text-2xl font-bold text-slate-900 tracking-tight leading-snug`.
  * Size 'sm': `text-sm leading-normal`.
  * Size 'base': `text-base leading-relaxed`.
  * Size 'lg': `text-lg leading-loose`.
  * Size 'xl': `text-xl leading-loose`.
  * Paragraph: `indent-0 space-y-3 whitespace-pre-line text-justify`.

* **ReadingQuestionPanel [DUMB]**:
  * Box Style: `lg:col-span-5 w-full flex flex-col gap-5`.

* **QuestionNavigationPills [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-3 shadow-xs flex items-center gap-2 overflow-x-auto scrollbar-none`.
  * Pill Base: `min-w-8 h-8 px-2.5 rounded-lg text-xs font-bold flex items-center justify-center transition-all cursor-pointer flex-shrink-0`.
  * Pill Unanswered: `border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100`.
  * Pill Answered: `bg-sky-50 text-sky-700 border border-sky-200 font-bold`.
  * Pill Active Focus: `ring-2 ring-sky-600 ring-offset-1 border-sky-600 bg-sky-600 text-white`.

* **QuestionListContainer [DUMB]**:
  * Box Style: `flex flex-col gap-5 w-full`.

* **QuestionItemCard [DUMB]**:
  * Box Style Normal: `w-full bg-white border border-slate-200 rounded-2xl p-5 md:p-6 shadow-sm flex flex-col gap-4 transition-all`.
  * Box Style Active: `w-full bg-white border-2 border-sky-500 rounded-2xl p-5 md:p-6 shadow-md flex flex-col gap-4 transition-all ring-4 ring-sky-500/10`.
  * Question Header: `flex items-start gap-3`.
  * Number Badge: `w-7 h-7 rounded-lg bg-slate-100 border border-slate-200 text-slate-700 text-xs font-bold flex items-center justify-center flex-shrink-0`.
  * Typography Prompt: `text-sm md:text-base font-semibold text-slate-900 leading-snug flex-1`.

* **MultipleChoiceQuestion [DUMB]**:
  * Box Style: `grid grid-cols-1 gap-2.5 w-full mt-1`.
  * Option Row Default: `flex items-center gap-3 p-3.5 rounded-xl border border-slate-200 bg-white hover:border-sky-300 hover:bg-sky-50/50 transition-all cursor-pointer`.
  * Option Row Selected: `flex items-center gap-3 p-3.5 rounded-xl border-2 border-sky-600 bg-sky-50 text-sky-900 font-medium transition-all cursor-pointer shadow-xs`.
  * Radio Key: `w-6 h-6 rounded-full border border-slate-300 flex items-center justify-center text-xs font-bold text-slate-600 group-hover:border-sky-600`.
  * Radio Key Selected: `w-6 h-6 rounded-full bg-sky-600 border border-sky-600 text-white flex items-center justify-center text-xs font-bold`.
  * Option Text: `text-sm text-slate-800 flex-1`.

* **FillBlankQuestion [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full mt-1`.
  * Input: `w-full max-w-md h-11 px-4 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all`.
  * Hint Text: `text-xs text-slate-400 font-normal`.

* **PracticeControlBar [DUMB]**:
  * Box Style: `sticky bottom-0 z-20 w-full bg-white/95 backdrop-blur-sm border-t border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between shadow-lg`.
  * Left Info: `text-xs md:text-sm text-slate-500 font-medium`.
  * Submit Button: `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-all cursor-pointer flex items-center gap-2`.
  * Submit Disabled: `h-11 px-6 rounded-xl bg-slate-200 text-slate-400 text-sm font-semibold cursor-not-allowed pointer-events-none`.

* **ConfirmSubmitIncompleteModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
  * Warning Icon: `w-12 h-12 rounded-full bg-amber-50 border border-amber-200 text-amber-600 flex items-center justify-center text-2xl mx-auto`.
  * Typography Title: `text-lg font-bold text-slate-900 text-center`.
  * Typography Message: `text-sm text-slate-500 text-center leading-relaxed`.
  * Button Group: `flex items-center gap-3 mt-2`.
  * Cancel Button: `flex-1 h-11 rounded-xl border border-slate-200 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition-colors cursor-pointer`.
  * Confirm Button: `flex-1 h-11 rounded-xl bg-amber-600 text-white font-semibold text-sm hover:bg-amber-700 active:bg-amber-800 transition-colors cursor-pointer`.

* **TimeUpSubmitModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col items-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-rose-50 border border-rose-200 text-rose-600 flex items-center justify-center text-2xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Message: `text-sm text-slate-500 leading-relaxed`.
  * Auto Submit Button: `w-full h-11 rounded-xl bg-sky-600 text-white font-semibold text-sm hover:bg-sky-700 transition-colors cursor-pointer`.

* **ConfirmExitModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-rose-50 border border-rose-200 text-rose-600 flex items-center justify-center text-2xl mx-auto`.
  * Typography Title: `text-lg font-bold text-slate-900 text-center`.
  * Typography Message: `text-sm text-slate-500 text-center leading-relaxed`.
  * Button Group: `flex items-center gap-3 mt-2`.
  * Stay Button: `flex-1 h-11 rounded-xl border border-slate-200 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition-colors cursor-pointer`.
  * Exit Button: `flex-1 h-11 rounded-xl bg-rose-600 text-white font-semibold text-sm hover:bg-rose-700 active:bg-rose-800 transition-colors cursor-pointer`.

---

#### Màn hình 4: Đang chấm bài (ReadingGradingStatusPage)

* **GradingStatusCard [DUMB]**:
  * Box Style: `w-full max-w-xl bg-white border border-slate-200 rounded-3xl p-8 md:p-10 shadow-sm flex flex-col items-center text-center gap-6`.
  * Title Line: `text-xl md:text-2xl font-bold text-slate-900`.
  * Subtitle Line: `text-sm text-slate-500`.

* **ReadingScanAnimation [DUMB]**:
  * Box Style: `relative w-28 h-28 flex items-center justify-center my-2`.
  * Outer Pulse Circle: `absolute inset-0 rounded-full bg-sky-100 animate-ping opacity-75`.
  * Inner Circle: `relative w-20 h-20 rounded-full bg-sky-50 border-2 border-sky-600 flex items-center justify-center text-sky-600 text-3xl font-bold shadow-sm`.
  * Book Scan Line: `absolute w-12 h-0.5 bg-sky-500 animate-bounce`.

* **GradingProgressSteps [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full text-left my-2`.
  * Step Item: `flex items-center gap-3 p-3 rounded-xl border transition-all`.
  * Step Completed: `bg-emerald-50/60 border-emerald-200 text-emerald-800 text-xs font-semibold`.
  * Step Processing: `bg-sky-50/70 border-sky-300 text-sky-800 text-xs font-bold animate-pulse`.
  * Step Pending: `bg-slate-50 border-slate-200 text-slate-400 text-xs font-medium`.
  * Step Icon Completed: `w-5 h-5 rounded-full bg-emerald-600 text-white flex items-center justify-center text-xs flex-shrink-0`.
  * Step Icon Processing: `w-5 h-5 rounded-full bg-sky-600 text-white flex items-center justify-center text-xs flex-shrink-0 animate-spin`.
  * Step Icon Pending: `w-5 h-5 rounded-full bg-slate-200 text-slate-500 flex items-center justify-center text-xs flex-shrink-0`.

* **GradingNoticeBox [DUMB]**:
  * Box Style: `p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-500 leading-relaxed max-w-md text-center`.

* **GradingActions [DUMB]**:
  * Box Style: `w-full pt-2`.
  * Return Button: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full flex items-center justify-center gap-2`.

---

#### Màn hình 5: Kết quả bài luyện đọc (ReadingResultPage)

* **ResultHeroHeader [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full pb-2`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Meta Row: `flex items-center gap-3 text-xs md:text-sm text-slate-500 flex-wrap`.
  * Meta Item: `flex items-center gap-1.5`.

* **ScoreSummaryCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col md:flex-row items-center justify-between gap-6`.
  * Score Big Display: `flex items-center gap-4`.
  * Score Number: `text-4xl md:text-5xl font-black text-sky-600 tracking-tight font-mono`.
  * Score Label Block: `flex flex-col`.
  * Score Title: `text-base font-bold text-slate-900`.
  * Score Subtitle: `text-xs text-slate-500`.
  * Accuracy Box: `flex-1 bg-slate-50 border border-slate-200 rounded-xl p-4 text-sm text-slate-700 leading-relaxed`.

* **ScoreBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-3 py-1 rounded-full text-xs font-bold border`.
  * Xuất sắc (>= 8.0): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Đạt yêu cầu (5.0 - 7.9): `bg-sky-50 text-sky-700 border-sky-200`.
  * Cần cố gắng (< 5.0): `bg-rose-50 text-rose-600 border-rose-200`.

* **ResultStatItem [DUMB]**:
  * Box Style: `flex flex-col gap-1 p-4 rounded-xl bg-white border border-slate-200 shadow-xs`.
  * Typography Label: `text-xs font-semibold text-slate-500`.
  * Typography Value: `text-xl font-bold text-slate-900 font-mono`.
  * Value Correct: `text-emerald-600 font-bold`.
  * Value Incorrect: `text-rose-600 font-bold`.
  * Value Skipped: `text-slate-400 font-bold`.

* **ResultWorkspaceLayout [DUMB]**:
  * Box Style: `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full items-start`.

* **ResultPassageViewer [DUMB]**:
  * Box Style: `lg:col-span-7 w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4 lg:sticky lg:top-20 lg:max-h-[calc(100vh-7rem)] lg:overflow-y-auto`.
  * Header: `flex items-center justify-between border-b border-slate-100 pb-3`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Passage Body: `text-base text-slate-800 leading-relaxed font-sans whitespace-pre-line text-justify`.
  * Highlight Evidence Inactive: `bg-amber-100 text-amber-900 border-b-2 border-amber-300 px-1 py-0.5 rounded cursor-pointer hover:bg-amber-200 transition-colors`.
  * Highlight Evidence Active: `bg-sky-100 text-sky-900 border-b-2 border-sky-500 ring-2 ring-sky-600 ring-offset-1 px-1 py-0.5 rounded font-medium shadow-xs`.

* **QuestionReviewList [DUMB]**:
  * Box Style: `lg:col-span-5 w-full flex flex-col gap-4`.

* **QuestionReviewItem [DUMB]**:
  * Box Style Correct: `w-full bg-white border-l-4 border-l-emerald-500 border-y border-r border-slate-200 rounded-2xl p-5 shadow-xs flex flex-col gap-3 cursor-pointer hover:border-slate-300 transition-all`.
  * Box Style Incorrect: `w-full bg-white border-l-4 border-l-rose-500 border-y border-r border-slate-200 rounded-2xl p-5 shadow-xs flex flex-col gap-3 cursor-pointer hover:border-slate-300 transition-all`.
  * Box Style Selected: `ring-2 ring-sky-600 ring-offset-1 shadow-sm`.
  * Header Row: `flex items-center justify-between gap-2`.
  * Order Badge: `text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-700`.
  * Status Badge Correct: `px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1`.
  * Status Badge Incorrect: `px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-50 text-rose-600 border border-rose-200 flex items-center gap-1`.
  * Question Prompt: `text-sm md:text-base font-semibold text-slate-900`.
  * Answer Comparison Box: `p-3 rounded-xl bg-slate-50 border border-slate-200 flex flex-col gap-1.5 text-xs md:text-sm`.
  * User Answer Line: `flex items-center gap-2 text-slate-700`.
  * Correct Answer Line: `flex items-center gap-2 font-bold text-emerald-700`.
  * Evidence Trigger: `inline-flex items-center gap-1 text-xs font-semibold text-sky-600 hover:text-sky-700 cursor-pointer pt-1`.
  * Explanation Box: `p-3 rounded-xl bg-sky-50/60 border border-sky-100 text-xs md:text-sm text-slate-700 leading-relaxed`.

* **ResultActionFooter [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row items-center justify-between gap-4 w-full pt-4 border-t border-slate-200`.
  * Left Group: `flex items-center gap-3 w-full sm:w-auto`.
  * Replay Button: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Back List Button: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Next Article Button (CTA Chính): `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex items-center justify-center gap-2`.

* **ResultSkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-6 w-full animate-pulse pointer-events-none`.
  * Banner Skeleton: `w-full h-36 bg-slate-200 rounded-2xl`.
  * Content Skeleton: `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full`.
  * Col Left: `lg:col-span-7 h-96 bg-slate-200 rounded-2xl`.
  * Col Right: `lg:col-span-5 h-96 bg-slate-200 rounded-2xl`.

---

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary Color (Chủ đạo):** `bg-sky-600`, `text-sky-600`, `border-sky-600`.
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
* **Success / Đúng / Điểm cao:** `bg-emerald-50`, `text-emerald-700`, `bg-emerald-600`, `border-emerald-200`.
* **Warning / Đồng hồ khẩn cấp (< 3 phút):** `bg-amber-50`, `text-amber-700`, `border-amber-300`, `text-amber-600`.
* **Danger / Sai / Hủy bài:** `bg-rose-50`, `text-rose-600`, `bg-rose-600`, `border-rose-200`, `border-rose-400`.
* **Reading Evidence & Highlight Mapping:**
  * Đoạn trích dẫn chứng trên bài đọc: `bg-amber-100 text-amber-900 border-amber-300`.
  * Đoạn trích dẫn chứng đang được focus/chọn: `bg-sky-100 text-sky-900 border-sky-500 ring-2 ring-sky-600 ring-offset-1`.
  * Nút bấm nhảy đến dẫn chứng: `text-sky-600 hover:text-sky-700 bg-sky-50 border-sky-200`.
* **Proficiency Levels:**
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.
* **Disabled State:** `bg-slate-200`, `text-slate-400`, `border-slate-200`, `cursor-not-allowed`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG dùng mã màu HEX hoặc RGB tự chế trong JSX, chỉ dùng Tailwind classes chuẩn hóa.
  * TUYỆT ĐỐI KHÔNG dùng hiệu ứng gradient sặc sỡ, không dùng kính mờ (glassmorphism) phức tạp gây mỏi mắt khi đọc văn bản dài.
  * TUYỆT ĐỐI KHÔNG sử dụng màu neon chói lóa.

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockReadingCategories = [
  {
    id: "cat_ielts",
    type: "IELTS",
    title: "Luyện đọc IELTS",
    description: "Các bài đọc học thuật chuẩn Academic Reading Passages 1 - 3 với đa dạng dạng bài phân tích chuyên sâu.",
    totalArticles: 36,
    iconName: "BookOpen"
  },
  {
    id: "cat_toeic",
    type: "TOEIC",
    title: "Luyện đọc TOEIC",
    description: "Rèn luyện đọc nhanh các biểu mẫu, đoạn văn đơn, đoạn kép Part 7 trong môi trường doanh nghiệp quốc tế.",
    totalArticles: 48,
    iconName: "Briefcase"
  },
  {
    id: "cat_foundation",
    type: "FOUNDATION",
    title: "Luyện đọc Nền tảng",
    description: "Đoạn văn ngắn, truyện chêm, từ vựng thông dụng A1 - B1 giúp xây dựng thói quen và phản xạ đọc hiểu.",
    totalArticles: 24,
    iconName: "Layers"
  },
  {
    id: "cat_work",
    type: "WORK",
    title: "Tiếng Anh Công việc",
    description: "Đọc hiểu tài liệu kỹ thuật, email giao dịch đối tác, bản tin ngành và báo cáo phân tích thị trường.",
    totalArticles: 30,
    iconName: "FileText"
  }
];

const mockTopicOptions = [
  { value: "ALL", label: "Tất cả chủ đề" },
  { value: "TECHNOLOGY", label: "Khoa học & Công nghệ" },
  { value: "ENVIRONMENT", label: "Môi trường & Sinh thái" },
  { value: "BUSINESS", label: "Kinh doanh & Khởi nghiệp" },
  { value: "CULTURE_SOCIETY", label: "Văn hóa & Xã hội" },
  { value: "EDUCATION", label: "Giáo dục & Tâm lý" }
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
  { value: "10", label: "10 phút" },
  { value: "15", label: "15 phút" },
  { value: "20", label: "20 phút" },
  { value: "30", label: "30 phút" },
  { value: "45", label: "45 phút" }
];

const mockStatusFilterTabs = [
  { value: "ALL", label: "Tất cả" },
  { value: "NOT_STARTED", label: "Chưa làm" },
  { value: "COMPLETED", label: "Đã làm" }
];

const mockArticlesList = [
  {
    id: "art_ielts_01",
    category: "IELTS",
    title: "The Evolution of Urban Architecture in the 21st Century",
    topic: "CULTURE_SOCIETY",
    level: "B2",
    durationMinutes: 20,
    totalQuestions: 10,
    status: "COMPLETED",
    bestScore: 8.5
  },
  {
    id: "art_ielts_02",
    category: "IELTS",
    title: "Artificial Intelligence and the Future of Medical Diagnostics",
    topic: "TECHNOLOGY",
    level: "C1",
    durationMinutes: 25,
    totalQuestions: 12,
    status: "NOT_STARTED"
  },
  {
    id: "art_toeic_01",
    category: "TOEIC",
    title: "Annual Corporate Sustainability and Clean Energy Report",
    topic: "BUSINESS",
    level: "B1",
    durationMinutes: 15,
    totalQuestions: 8,
    status: "NOT_STARTED"
  },
  {
    id: "art_found_01",
    category: "FOUNDATION",
    title: "A Day in the Life of a Wildlife Conservationist",
    topic: "ENVIRONMENT",
    level: "A2",
    durationMinutes: 10,
    totalQuestions: 6,
    status: "COMPLETED",
    bestScore: 9.0
  }
];

const mockActivePracticeSession = {
  articleId: "art_ielts_01",
  title: "The Evolution of Urban Architecture in the 21st Century",
  category: "IELTS",
  topic: "CULTURE_SOCIETY",
  level: "B2",
  durationMinutes: 20,
  remainingSeconds: 880,
  fontSize: "base",
  content: "Urban architecture in the twenty-first century has transitioned dramatically from purely functional concrete edifices to dynamic, eco-conscious structures designed to harmonize with their surrounding environments.\n\nIn the early 2000s, architects predominantly prioritized maximizing floor area and financial returns for high-density metropolitan zones. However, catastrophic heat waves and escalating municipal carbon emissions prompted a fundamental shift. Modern skyscrapers are now engineered with 'vertical forest' facades, employing thousands of indigenous trees and shrubs that absorb greenhouse gases while insulating building interiors against excessive solar radiation.\n\nFurthermore, modern spatial planners place immense emphasis on community interconnectivity. Contemporary residential complexes intentionally incorporate shared green courtyards, modular co-working spaces, and open-air rooftop farms. These shared amenities have been statistically proven to reduce urban isolation and enhance psychological well-being among urban inhabitants.",
  totalQuestions: 4,
  answeredCount: 3,
  questions: [
    {
      id: "q_01",
      orderNumber: 1,
      type: "MULTIPLE_CHOICE",
      prompt: "What was the primary priority of architects in the early 2000s?",
      options: [
        { key: "A", content: "Preserving cultural heritage and historical authenticity" },
        { key: "B", content: "Maximizing usable floor space and commercial profitability" },
        { key: "C", content: "Implementing renewable solar energy technologies" },
        { key: "D", content: "Reducing thermal insulation costs for low-income residents" }
      ],
      userAnswer: "B"
    },
    {
      id: "q_02",
      orderNumber: 2,
      type: "MULTIPLE_CHOICE",
      prompt: "Which environmental factor spurred the adoption of 'vertical forest' facades?",
      options: [
        { key: "A", content: "Severe droughts affecting agricultural crop yield" },
        { key: "B", content: "Rising sea levels impacting coastal infrastructure" },
        { key: "C", content: "Severe heat waves and municipal carbon emissions" },
        { key: "D", content: "Shortages of imported timber and construction materials" }
      ],
      userAnswer: "C"
    },
    {
      id: "q_03",
      orderNumber: 3,
      type: "FILL_IN_BLANK",
      prompt: "Indigenous plants integrated into modern skyscraper facades help absorb [BLANK] gases.",
      userAnswer: "greenhouse"
    },
    {
      id: "q_04",
      orderNumber: 4,
      type: "FILL_IN_BLANK",
      prompt: "Shared residential courtyards and rooftop farms have been proven to decrease [BLANK] among residents.",
      userAnswer: ""
    }
  ]
};

const mockGradingSteps = [
  { label: "Đã lưu bản câu trả lời bài luyện đọc an toàn lên hệ thống", isCompleted: true, isProcessing: false },
  { label: "Đang đối chiếu đáp án trắc nghiệm và câu hỏi điền từ", isCompleted: false, isProcessing: true },
  { label: "Đang tổng hợp điểm số, tỷ lệ đúng/sai và phân tích dẫn chứng", isCompleted: false, isProcessing: false }
];

const mockReadingResult = {
  attemptId: "att_read_8801",
  articleId: "art_ielts_01",
  articleTitle: "The Evolution of Urban Architecture in the 21st Century",
  category: "IELTS",
  completedAt: "16:15 - 12/09/2026",
  timeSpentSeconds: 620,
  totalQuestions: 4,
  correctCount: 3,
  incorrectCount: 1,
  skippedCount: 0,
  overallScore: 7.5,
  content: "Urban architecture in the twenty-first century has transitioned dramatically from purely functional concrete edifices to dynamic, eco-conscious structures designed to harmonize with their surrounding environments.\n\nIn the early 2000s, architects predominantly prioritized maximizing floor area and financial returns for high-density metropolitan zones. However, catastrophic heat waves and escalating municipal carbon emissions prompted a fundamental shift. Modern skyscrapers are now engineered with 'vertical forest' facades, employing thousands of indigenous trees and shrubs that absorb greenhouse gases while insulating building interiors against excessive solar radiation.\n\nFurthermore, modern spatial planners place immense emphasis on community interconnectivity. Contemporary residential complexes intentionally incorporate shared green courtyards, modular co-working spaces, and open-air rooftop farms. These shared amenities have been statistically proven to reduce urban isolation and enhance psychological well-being among urban inhabitants.",
  questionReviews: [
    {
      questionId: "q_01",
      orderNumber: 1,
      type: "MULTIPLE_CHOICE",
      prompt: "What was the primary priority of architects in the early 2000s?",
      userAnswer: "B",
      correctAnswer: "B",
      isCorrect: true,
      explanation: "Đoạn 2 nêu rõ: 'In the early 2000s, architects predominantly prioritized maximizing floor area and financial returns'.",
      evidence: {
        paragraphIndex: 2,
        snippetText: "In the early 2000s, architects predominantly prioritized maximizing floor area and financial returns"
      }
    },
    {
      questionId: "q_02",
      orderNumber: 2,
      type: "MULTIPLE_CHOICE",
      prompt: "Which environmental factor spurred the adoption of 'vertical forest' facades?",
      userAnswer: "C",
      correctAnswer: "C",
      isCorrect: true,
      explanation: "Đoạn 2 chỉ ra nguyên nhân: 'catastrophic heat waves and escalating municipal carbon emissions prompted a fundamental shift'.",
      evidence: {
        paragraphIndex: 2,
        snippetText: "catastrophic heat waves and escalating municipal carbon emissions prompted a fundamental shift"
      }
    },
    {
      questionId: "q_03",
      orderNumber: 3,
      type: "FILL_IN_BLANK",
      prompt: "Indigenous plants integrated into modern skyscraper facades help absorb [BLANK] gases.",
      userAnswer: "greenhouse",
      correctAnswer: "greenhouse",
      isCorrect: true,
      explanation: "Đoạn 2 ghi nhận cây bản địa: 'absorb greenhouse gases while insulating building interiors'.",
      evidence: {
        paragraphIndex: 2,
        snippetText: "employing thousands of indigenous trees and shrubs that absorb greenhouse gases"
      }
    },
    {
      questionId: "q_04",
      orderNumber: 4,
      type: "FILL_IN_BLANK",
      prompt: "Shared residential courtyards and rooftop farms have been proven to decrease [BLANK] among residents.",
      userAnswer: "depression",
      correctAnswer: "urban isolation",
      isCorrect: false,
      explanation: "Đoạn 3 khẳng định tiện ích này giúp 'reduce urban isolation'. Người học điền nhầm thành 'depression'.",
      evidence: {
        paragraphIndex: 3,
        snippetText: "These shared amenities have been statistically proven to reduce urban isolation"
      }
    }
  ],
  nextArticleId: "art_ielts_02"
};

const mockConfirmSubmitIncomplete = {
  title: "Nộp bài khi chưa hoàn thành?",
  description: "Bạn vẫn còn 1 câu hỏi chưa điền đáp án. Nếu nộp bài ngay bây giờ, các câu chưa trả lời sẽ bị tính là sai. Bạn có chắc chắn muốn nộp không?"
};

const mockConfirmExit = {
  title: "Thoát khỏi phòng luyện đọc?",
  description: "Tiến trình làm bài đọc hiện tại chưa được lưu. Nếu rời đi bây giờ, bạn sẽ phải làm lại từ đầu. Bạn có chắc chắn muốn thoát?"
};

const mockTimeUp = {
  title: "Đã hết thời gian làm bài!",
  description: "Thời gian làm bài quy định đã kết thúc. Hệ thống tự động ghi nhận các đáp án đã chọn và nộp bài để chấm điểm ngay bây giờ."
};
```
