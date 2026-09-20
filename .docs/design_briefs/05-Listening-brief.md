# 05-Listening-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased`.
* **Main Container:** `w-full max-w-7xl mx-auto px-4 py-6 md:px-6 md:py-8 lg:px-8 flex flex-col gap-6 md:gap-8 flex-1`.
* **Category Vertical List Container:** `flex flex-col gap-4 md:gap-5 w-full max-w-4xl mx-auto`.
* **Category Card Row:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all`.
* **Lesson List Container:** `w-full flex flex-col gap-6`.
* **Lesson Filter Bar Container:** `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
* **Lesson Grid Standard:** `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
* **Practice Top Navigation Bar:** `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
* **Practice Workspace Layout (Split 2 Columns / Stacked on Mobile):** `w-full max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start`.
  * **Audio Sticky Column (Left / Top):** `lg:col-span-5 w-full flex flex-col gap-4 lg:sticky lg:top-20`.
  * **Questions Scrollable Column (Right / Bottom):** `lg:col-span-7 w-full flex flex-col gap-6`.
* **Practice Bottom Control Bar:** `sticky bottom-0 z-20 w-full bg-white/95 backdrop-blur-sm border-t border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between shadow-lg`.
* **Grading Status Center Layout:** `w-full max-w-2xl mx-auto min-h-[calc(100vh-12rem)] flex flex-col items-center justify-center p-4`.
* **Result Page Container:** `w-full max-w-7xl mx-auto flex flex-col gap-8 py-4`.
* **Result Score Summary Grid:** `grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 w-full`.
* **Result Comparison Layout (Split 2 Columns):** `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full items-start`.
  * **Audio & Transcript Column (Left):** `lg:col-span-5 w-full flex flex-col gap-4 lg:sticky lg:top-20`.
  * **Question Review Column (Right):** `lg:col-span-7 w-full flex flex-col gap-4`.
* **Modal Overlay:** `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
* **Modal Box Standard:** `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
* **Responsive Rules:**
  * **Mobile (< 768px):** Toàn bộ chia cột chuyển về `flex-col`; Lưới bài nghe co về 1 cột `grid-cols-1`; Trình phát audio ghim ở phần trên màn hình khi làm bài; Cụm câu hỏi hiển thị dạng thẻ xếp dọc; Các nút CTA chính mở rộng toàn chiều ngang `w-full`.
  * **Tablet (768px - 1023px):** Lưới bài nghe hiển thị 2 cột `grid-cols-2`; Thanh điều khiển Audio và danh sách câu hỏi bố trí liền mạch; Header giữ padding `px-6`.
  * **Desktop (>= 1024px):** Lưới bài nghe cố định 3 cột `grid-cols-3`; Màn hình làm bài chia 2 cột tỉ lệ `5/7` với cột Audio Player ghim cố định `sticky top-20`; Màn hình kết quả chia 2 cột `5/7` đối chiếu giữa Audio/Transcript và chi tiết từng câu hỏi; Giới hạn độ rộng nội dung `max-w-7xl mx-auto`.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

#### Màn hình 1: Danh mục luyện nghe (ListeningCategoryPage)

* **ListeningCategoryHeader [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full text-center max-w-2xl mx-auto py-4`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Description: `text-sm md:text-base text-slate-500 font-normal leading-relaxed`.

* **ListeningCategoryList [DUMB]**:
  * Box Style: `flex flex-col gap-4 md:gap-5 w-full max-w-4xl mx-auto`.

* **ListeningCategoryCard [DUMB]**:
  * Box Style: `group relative w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all cursor-pointer`.
  * Left Content: `flex items-start gap-4 flex-1`.
  * Icon Container: `w-14 h-14 rounded-xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 flex-shrink-0 group-hover:scale-105 transition-transform`.
  * Info Stack: `flex flex-col gap-1.5 flex-1`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900 group-hover:text-sky-600 transition-colors`.
  * Typography Description: `text-sm text-slate-500 leading-relaxed line-clamp-2`.
  * Badge Row: `flex items-center gap-2 mt-1`.
  * Count Badge: `px-2.5 py-0.5 rounded-full bg-slate-100 text-xs font-semibold text-slate-600`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **ListeningCategorySkeleton [DUMB]**:
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

#### Màn hình 2: Danh sách bài luyện theo danh mục (ListeningLessonListPage)

* **LessonListHeader [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full pb-2`.
  * Title Row: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Count: `text-sm font-medium text-slate-500`.

* **Breadcrumb [DUMB]**:
  * Box Style: `flex items-center gap-2 text-sm text-slate-500`.
  * Link Item: `hover:text-sky-600 transition-colors cursor-pointer`.
  * Separator: `text-slate-300 select-none`.
  * Active Item: `font-semibold text-slate-900 pointer-events-none`.

* **LessonFilterBar [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
  * Left Group: `flex-1 flex flex-col sm:flex-row items-stretch sm:items-center gap-3`.
  * Right Group: `flex items-center gap-3 flex-wrap`.

* **LessonSearchBar [DUMB]**:
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

* **LessonStatusTabs [DUMB]**:
  * Box Style: `inline-flex items-center bg-slate-100 p-1 rounded-xl gap-1 border border-slate-200/60`.
  * Tab Default: `px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer`.
  * Tab Active: `px-3 py-1.5 rounded-lg text-xs font-bold bg-white text-sky-700 shadow-sm`.

* **LessonGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.

* **LessonCard [DUMB]**:
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
  * Đang làm (IN_PROGRESS): `bg-amber-50 text-amber-700 border-amber-200`.
  * Đã hoàn thành (COMPLETED): `bg-emerald-50 text-emerald-700 border-emerald-200`.

* **DurationBadge [DUMB]**:
  * Box Style: `inline-flex items-center gap-1 text-xs font-medium text-slate-500`.
  * Icon: `w-3.5 h-3.5 text-slate-400`.

* **PaginationControl [DUMB]**:
  * Box Style: `flex items-center justify-center gap-2 w-full py-4`.
  * Button Base: `min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition-colors cursor-pointer flex items-center justify-center`.
  * Default: `border-slate-200 bg-white text-slate-700 hover:bg-slate-50`.
  * Active: `border-sky-600 bg-sky-600 text-white font-semibold shadow-sm`.
  * Disabled: `border-slate-200 text-slate-300 bg-slate-50 cursor-not-allowed`.

* **LessonEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Reset Button: `h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer`.

* **LessonListSkeleton [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
  * Card Skeleton: `bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-4 animate-pulse pointer-events-none`.
  * Badge Skeleton: `h-5 w-16 rounded bg-slate-200`.
  * Title Skeleton: `h-5 w-3/4 rounded bg-slate-200`.
  * Meta Skeleton: `h-4 w-1/2 rounded bg-slate-100`.
  * Button Skeleton: `h-10 w-full rounded-xl bg-slate-200 mt-2`.

---

#### Màn hình 3: Làm bài luyện nghe (ListeningPracticePage)

* **ListeningPracticeHeader [DUMB]**:
  * Box Style: `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * Left Group: `flex items-center gap-3`.
  * Exit Button: `inline-flex items-center justify-center w-9 h-9 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors cursor-pointer`.
  * Title Block: `flex flex-col`.
  * Typography Title: `text-base md:text-lg font-bold text-slate-900 line-clamp-1`.
  * Typography Topic: `text-xs text-slate-400 font-medium`.
  * Right Group: `flex items-center gap-4`.

* **CountdownTimer [DUMB]**:
  * Box Style Normal: `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-slate-100 border border-slate-200 text-slate-700 font-mono text-sm font-bold`.
  * Box Style Urgent (< 2 phút): `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-rose-50 border border-rose-300 text-rose-600 font-mono text-sm font-bold animate-pulse`.
  * Clock Icon: `w-4 h-4`.

* **AnswerProgressIndicator [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 text-xs md:text-sm font-semibold text-slate-600 bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-200`.
  * Counter Text: `font-bold text-sky-600`.

* **ListeningWorkspaceLayout [DUMB]**:
  * Box Style: `w-full max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start`.
  * Audio Area: `lg:col-span-5 w-full flex flex-col gap-4 lg:sticky lg:top-20`.
  * Question Area: `lg:col-span-7 w-full flex flex-col gap-5`.

* **AudioPlayerSection [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col gap-5`.
  * Header Badge Row: `flex items-center justify-between pb-2 border-b border-slate-100`.
  * Player Title: `text-sm font-bold text-slate-800 flex items-center gap-2`.
  * Format Badge: `px-2 py-0.5 rounded text-xs font-semibold bg-sky-50 text-sky-700 border border-sky-100`.

* **AudioWaveformBar [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full`.
  * Track Container: `relative w-full h-8 bg-slate-100 rounded-xl flex items-center px-3 cursor-pointer group hover:bg-slate-200/70 transition-colors`.
  * Progress Fill: `absolute left-0 top-0 bottom-0 bg-sky-100 rounded-xl pointer-events-none transition-all`.
  * Scrubbing Thumb: `absolute w-4 h-4 rounded-full bg-sky-600 shadow-md border-2 border-white -translate-x-1/2 pointer-events-none group-hover:scale-110 transition-transform`.
  * Time Indicator Row: `flex items-center justify-between text-xs font-mono font-medium text-slate-500`.

* **AudioControlToolbar [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row items-center justify-between gap-4 w-full pt-2`.
  * Main Controls: `flex items-center gap-3`.
  * Play/Pause Button: `w-12 h-12 rounded-full bg-sky-600 text-white flex items-center justify-center text-lg shadow-md hover:bg-sky-700 active:bg-sky-800 transition-all cursor-pointer hover:scale-105 active:scale-95`.
  * Skip Button: `w-9 h-9 rounded-lg border border-slate-200 text-slate-600 flex items-center justify-center hover:bg-slate-100 active:bg-slate-200 transition-colors cursor-pointer text-xs font-semibold`.
  * Speed Selector: `inline-flex items-center bg-slate-100 p-0.5 rounded-lg border border-slate-200`.
  * Speed Button: `px-2 py-1 rounded text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer`.
  * Speed Active: `bg-white text-sky-700 font-bold shadow-xs`.
  * Volume Slider: `flex items-center gap-2 w-28 text-slate-500`.
  * Volume Range Input: `w-full h-1.5 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-sky-600`.

* **QuestionListSection [DUMB]**:
  * Box Style: `flex flex-col gap-5 w-full`.

* **QuestionNavigationPills [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-3 shadow-xs flex items-center gap-2 overflow-x-auto scrollbar-none`.
  * Pill Base: `min-w-8 h-8 px-2.5 rounded-lg text-xs font-bold flex items-center justify-center transition-all cursor-pointer flex-shrink-0`.
  * Pill Unanswered: `border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100`.
  * Pill Answered: `bg-sky-50 text-sky-700 border border-sky-200 font-bold`.
  * Pill Active Focus: `ring-2 ring-sky-600 ring-offset-1 border-sky-600 bg-sky-600 text-white`.

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

#### Màn hình 4: Đang chấm bài (ListeningGradingStatusPage)

* **GradingStatusCard [DUMB]**:
  * Box Style: `w-full max-w-xl bg-white border border-slate-200 rounded-3xl p-8 md:p-10 shadow-sm flex flex-col items-center text-center gap-6`.
  * Title Line: `text-xl md:text-2xl font-bold text-slate-900`.
  * Subtitle Line: `text-sm text-slate-500`.

* **AudioWaveGradingAnimation [DUMB]**:
  * Box Style: `flex items-center justify-center gap-1.5 h-16 w-full my-2`.
  * Wave Bar Base: `w-1.5 bg-sky-500 rounded-full animate-bounce`.
  * Bar 1: `h-6 animation-delay-100`.
  * Bar 2: `h-12 animation-delay-200`.
  * Bar 3: `h-16 animation-delay-300`.
  * Bar 4: `h-10 animation-delay-150`.
  * Bar 5: `h-7 animation-delay-250`.

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

---

#### Màn hình 5: Kết quả bài luyện nghe (ListeningResultPage)

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
  * Comment Box: `flex-1 bg-slate-50 border border-slate-200 rounded-xl p-4 text-sm text-slate-700 leading-relaxed`.

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

* **ResultWorkspaceLayout [DUMB]**:
  * Box Style: `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full items-start`.
  * Media Column: `lg:col-span-5 w-full flex flex-col gap-4 lg:sticky lg:top-20`.
  * Reviews Column: `lg:col-span-7 w-full flex flex-col gap-4`.

* **ReviewAudioPlayer [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col gap-3`.
  * Header: `flex items-center justify-between text-xs font-bold text-slate-700`.
  * Controls: `flex items-center gap-3 w-full`.
  * Play Mini Button: `w-9 h-9 rounded-full bg-sky-600 text-white flex items-center justify-center text-sm shadow-xs hover:bg-sky-700 cursor-pointer`.
  * Progress Mini: `flex-1 h-2 bg-slate-100 rounded-full overflow-hidden`.
  * Progress Bar: `h-full bg-sky-600`.

* **TranscriptAccordionPanel [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden flex flex-col`.
  * Accordion Header: `p-4 flex items-center justify-between bg-slate-50 hover:bg-slate-100/80 transition-colors cursor-pointer`.
  * Title: `text-sm font-bold text-slate-900 flex items-center gap-2`.
  * Chevron: `w-4 h-4 text-slate-500 transition-transform`.
  * Content Body: `p-5 text-sm text-slate-700 leading-relaxed border-t border-slate-200 max-h-96 overflow-y-auto font-normal`.
  * Highlight Evidence: `bg-amber-100 text-amber-900 px-1 py-0.5 rounded border-b border-amber-300 font-medium`.

* **QuestionReviewList [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full`.

* **QuestionReviewItem [DUMB]**:
  * Box Style Correct: `w-full bg-white border-l-4 border-l-emerald-500 border-y border-r border-slate-200 rounded-2xl p-5 shadow-xs flex flex-col gap-3`.
  * Box Style Incorrect: `w-full bg-white border-l-4 border-l-rose-500 border-y border-r border-slate-200 rounded-2xl p-5 shadow-xs flex flex-col gap-3`.
  * Header Row: `flex items-center justify-between gap-2`.
  * Order Badge: `text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-700`.
  * Status Badge Correct: `px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1`.
  * Status Badge Incorrect: `px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-50 text-rose-600 border border-rose-200 flex items-center gap-1`.
  * Question Prompt: `text-sm md:text-base font-semibold text-slate-900`.
  * Answer Comparison Box: `p-3 rounded-xl bg-slate-50 border border-slate-200 flex flex-col gap-1.5 text-xs md:text-sm`.
  * User Answer Line: `flex items-center gap-2`.
  * Correct Answer Line: `flex items-center gap-2 font-bold text-emerald-700`.
  * Timestamp Trigger: `inline-flex items-center gap-1 text-xs font-semibold text-sky-600 hover:text-sky-700 cursor-pointer pt-1`.
  * Explanation Box: `p-3 rounded-xl bg-sky-50/60 border border-sky-100 text-xs md:text-sm text-slate-700 leading-relaxed`.

* **ResultActionFooter [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row items-center justify-between gap-4 w-full pt-4 border-t border-slate-200`.
  * Left Group: `flex items-center gap-3 w-full sm:w-auto`.
  * Replay Button: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Back List Button: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Next Lesson Button (CTA Chính): `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex items-center justify-center gap-2`.

* **ResultSkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-6 w-full animate-pulse pointer-events-none`.
  * Banner Skeleton: `w-full h-36 bg-slate-200 rounded-2xl`.
  * Content Skeleton: `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full`.
  * Col Left: `lg:col-span-5 h-80 bg-slate-200 rounded-2xl`.
  * Col Right: `lg:col-span-7 h-96 bg-slate-200 rounded-2xl`.

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
* **Warning / Đồng hồ khẩn cấp (< 2 phút):** `bg-amber-50`, `text-amber-700`, `border-amber-300`, `text-amber-600`.
* **Danger / Sai / Hủy bài:** `bg-rose-50`, `text-rose-600`, `bg-rose-600`, `border-rose-200`, `border-rose-400`.
* **Audio Player & Waveform Elements:**
  * Track nền thanh trượt: `bg-slate-200`.
  * Tiến trình phát (Progress fill): `bg-sky-500`, `bg-sky-600`.
  * Điểm kéo (Scrubber handle): `bg-sky-600 border-2 border-white shadow-md`.
  * Cột sóng âm Audio (Active Wave): `bg-sky-500`.
  * Cột sóng âm Audio (Inactive Wave): `bg-slate-300`.
* **Transcript & Evidence Mapping:**
  * Khung đối chiếu dẫn chứng: `bg-amber-100 text-amber-900 border-amber-300`.
  * Trích dẫn mốc thời gian audio: `text-sky-600 hover:text-sky-700 bg-sky-50 border-sky-200`.
* **Proficiency Levels:**
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.
* **Disabled State:** `bg-slate-200`, `text-slate-400`, `border-slate-200`, `cursor-not-allowed`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG dùng mã màu HEX hoặc RGB tự chế trong JSX, chỉ dùng Tailwind classes chuẩn hóa.
  * TUYỆT ĐỐI KHÔNG dùng hiệu ứng gradient đa sắc, không glassmorphism phức tạp làm xao nhãng trải nghiệm nghe.
  * TUYỆT ĐỐI KHÔNG sử dụng màu neon chói lóa.

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockListeningCategories = [
  {
    id: "cat_ielts",
    type: "IELTS",
    title: "Luyện nghe IELTS",
    description: "Bộ đề luyện nghe chuẩn 4 Section theo cấu trúc đề thi IELTS Academic & General Training.",
    totalLessons: 32,
    iconName: "Headphones"
  },
  {
    id: "cat_toeic",
    type: "TOEIC",
    title: "Luyện nghe TOEIC",
    description: "Rèn phản xạ nghe hiểu tranh ảnh, hỏi - đáp ngắn và đoạn hội thoại công sở Part 1 - 4.",
    totalLessons: 45,
    iconName: "Briefcase"
  },
  {
    id: "cat_foundation",
    type: "FOUNDATION",
    title: "Luyện nghe Nền tảng",
    description: "Luyện phát âm chuẩn, nhận diện trọng âm từ, nối âm và ngữ điệu trong giao tiếp hàng ngày.",
    totalLessons: 28,
    iconName: "BookOpen"
  },
  {
    id: "cat_work",
    type: "WORK",
    title: "Tiếng Anh Công việc",
    description: "Nghe hiểu cuộc họp trực tuyến, thuyết trình kinh doanh và đàm phán hợp đồng thương mại.",
    totalLessons: 20,
    iconName: "Award"
  }
];

const mockTopicOptions = [
  { value: "ALL", label: "Tất cả chủ đề" },
  { value: "DAILY_CONVERSATION", label: "Giao tiếp hàng ngày" },
  { value: "ACADEMIC_LECTURE", label: "Bài giảng học thuật" },
  { value: "BUSINESS_MEETING", label: "Hội họp doanh nghiệp" },
  { value: "TRAVEL_AIRPORT", label: "Du lịch & Di chuyển" },
  { value: "SCIENCE_TECH", label: "Khoa học & Đổi mới" }
];

const mockLevelOptions = [
  { value: "ALL", label: "Tất cả trình độ" },
  { value: "A1", label: "A1 - Mới bắt đầu" },
  { value: "A2", label: "A2 - Sơ cấp" },
  { value: "B1", label: "B1 - Trung cấp" },
  { value: "B2", label: "B2 - Trung cao cấp" },
  { value: "C1", label: "C1 - Cao cấp" }
];

const mockStatusFilterTabs = [
  { value: "ALL", label: "Tất cả" },
  { value: "NOT_STARTED", label: "Chưa làm" },
  { value: "IN_PROGRESS", label: "Đang làm" },
  { value: "COMPLETED", label: "Đã hoàn thành" }
];

const mockLessonsList = [
  {
    id: "lesson_ielts_01",
    category: "IELTS",
    title: "Campus Accommodation & Facilities Tour",
    topic: "ACADEMIC_LECTURE",
    level: "B2",
    durationSeconds: 340,
    totalQuestions: 10,
    status: "COMPLETED",
    bestScore: 9.0
  },
  {
    id: "lesson_ielts_02",
    category: "IELTS",
    title: "Environmental Impact of Renewable Wind Energy",
    topic: "SCIENCE_TECH",
    level: "C1",
    durationSeconds: 420,
    totalQuestions: 10,
    status: "NOT_STARTED"
  },
  {
    id: "lesson_toeic_01",
    category: "TOEIC",
    title: "Quarterly Budget Meeting & Financial Review",
    topic: "BUSINESS_MEETING",
    level: "B1",
    durationSeconds: 280,
    totalQuestions: 8,
    status: "IN_PROGRESS"
  },
  {
    id: "lesson_found_01",
    category: "FOUNDATION",
    title: "Checking In at the International Airport",
    topic: "TRAVEL_AIRPORT",
    level: "A2",
    durationSeconds: 195,
    totalQuestions: 6,
    status: "COMPLETED",
    bestScore: 8.5
  }
];

const mockActivePracticeSession = {
  lessonId: "lesson_ielts_01",
  title: "Campus Accommodation & Facilities Tour",
  category: "IELTS",
  topic: "ACADEMIC_LECTURE",
  level: "B2",
  audioUrl: "https://assets.en-learning.internal/audio/ielts_campus_tour.mp3",
  durationSeconds: 340,
  remainingSeconds: 845,
  totalQuestions: 4,
  answeredCount: 3,
  questions: [
    {
      id: "q_01",
      orderNumber: 1,
      type: "MULTIPLE_CHOICE",
      prompt: "What is the maximum rental period allowed for undergraduate students in North Hall?",
      options: [
        { key: "A", content: "One academic semester (4 months)" },
        { key: "B", content: "One full academic year (9 months)" },
        { key: "C", content: "Up to two calendar years" },
        { key: "D", content: "Unlimited throughout their degree" }
      ],
      userAnswer: "B"
    },
    {
      id: "q_02",
      orderNumber: 2,
      type: "MULTIPLE_CHOICE",
      prompt: "Which facility has recently been relocated to the basement floor?",
      options: [
        { key: "A", content: "The student cafeteria" },
        { key: "B", content: "The laundry and drying room" },
        { key: "C", content: "The 24-hour study lounge" },
        { key: "D", content: "The postal reception office" }
      ],
      userAnswer: "B"
    },
    {
      id: "q_03",
      orderNumber: 3,
      type: "FILL_IN_BLANK",
      prompt: "Students must register their guest vehicles at the main security gate before [BLANK] PM.",
      userAnswer: "10:00"
    },
    {
      id: "q_04",
      orderNumber: 4,
      type: "FILL_IN_BLANK",
      prompt: "The monthly high-speed internet subscription fee is included in the [BLANK] payment.",
      userAnswer: ""
    }
  ]
};

const mockGradingSteps = [
  { label: "Đã lưu bản câu trả lời bài thi an toàn lên hệ thống", isCompleted: true, isProcessing: false },
  { label: "Đang đối chiếu dữ liệu đáp án chuẩn và phân đoạn âm thanh", isCompleted: false, isProcessing: true },
  { label: "Đang tổng hợp điểm số, tỷ lệ đúng/sai và mở khóa transcript", isCompleted: false, isProcessing: false }
];

const mockListeningResult = {
  attemptId: "att_ielts_9901",
  lessonId: "lesson_ielts_01",
  lessonTitle: "Campus Accommodation & Facilities Tour",
  category: "IELTS",
  completedAt: "15:20 - 12/09/2026",
  timeSpentSeconds: 520,
  totalQuestions: 4,
  correctCount: 3,
  incorrectCount: 1,
  skippedCount: 0,
  overallScore: 7.5,
  generalComment: "Khả năng bắt từ khóa và nhận diện chi tiết trong đoạn băng khá tốt. Cần lưu ý các bẫy đổi từ đồng nghĩa (paraphrasing) đối với câu hỏi dạng điền từ.",
  audioUrl: "https://assets.en-learning.internal/audio/ielts_campus_tour.mp3",
  transcript: "Welcome to the North Campus Accommodation induction tour. First off, regarding tenancy agreements, undergraduate students are eligible for a contract spanning one full academic year, precisely nine months from September to May. During summer breaks, rooms must be vacated for cleaning.\n\nNow moving over to our common amenities: due to noise complaints from residents on the ground floor, the laundry and drying room was officially relocated to the basement floor last month. Please remember to bring your digital student card for door access.\n\nVisitors are strictly required to check in. Any guest vehicles must be officially registered at the security barrier before 10:00 PM; overnight unpermitted parking incurs penalty fees. Lastly, all utility bills including heating, water, and unlimited fiber internet are comprehensively bundled into your regular housing payment.",
  questionReviews: [
    {
      questionId: "q_01",
      orderNumber: 1,
      type: "MULTIPLE_CHOICE",
      prompt: "What is the maximum rental period allowed for undergraduate students in North Hall?",
      userAnswer: "B",
      correctAnswer: "B",
      isCorrect: true,
      audioTimestamp: 22,
      explanation: "Dẫn chứng ở giây 00:22: 'undergraduate students are eligible for a contract spanning one full academic year, precisely nine months'."
    },
    {
      questionId: "q_02",
      orderNumber: 2,
      type: "MULTIPLE_CHOICE",
      prompt: "Which facility has recently been relocated to the basement floor?",
      userAnswer: "B",
      correctAnswer: "B",
      isCorrect: true,
      audioTimestamp: 65,
      explanation: "Dẫn chứng ở giây 01:05: 'the laundry and drying room was officially relocated to the basement floor last month'."
    },
    {
      questionId: "q_03",
      orderNumber: 3,
      type: "FILL_IN_BLANK",
      prompt: "Students must register their guest vehicles at the main security gate before [BLANK] PM.",
      userAnswer: "10:00",
      correctAnswer: "10:00",
      isCorrect: true,
      audioTimestamp: 98,
      explanation: "Dẫn chứng ở giây 01:38: 'Any guest vehicles must be officially registered at the security barrier before 10:00 PM'."
    },
    {
      questionId: "q_04",
      orderNumber: 4,
      type: "FILL_IN_BLANK",
      prompt: "The monthly high-speed internet subscription fee is included in the [BLANK] payment.",
      userAnswer: "tuition",
      correctAnswer: "housing",
      isCorrect: false,
      audioTimestamp: 124,
      explanation: "Dẫn chứng ở giây 02:04: 'all utility bills including ... internet are comprehensively bundled into your regular housing payment'. Người học điền nhầm thành 'tuition'."
    }
  ],
  nextLessonId: "lesson_ielts_02"
};

const mockConfirmSubmitIncomplete = {
  title: "Nộp bài khi chưa hoàn thành?",
  description: "Bạn vẫn còn 1 câu hỏi chưa điền đáp án. Nếu nộp bài ngay bây giờ, các câu chưa trả lời sẽ được tính là sai. Bạn có chắc chắn muốn nộp không?"
};

const mockConfirmExit = {
  title: "Thoát khỏi phòng luyện nghe?",
  description: "Tiến trình làm bài nghe hiện tại chưa được lưu. Nếu rời đi bây giờ, bạn sẽ phải làm lại từ đầu. Bạn có chắc chắn muốn thoát?"
};

const mockTimeUp = {
  title: "Đã hết thời gian làm bài!",
  description: "Thời gian quy định đã kết thúc. Hệ thống tự động ghi nhận các đáp án đã chọn và gửi bài để chấm điểm ngay bây giờ."
};
```
