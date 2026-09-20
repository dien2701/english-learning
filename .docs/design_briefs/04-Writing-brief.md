# 04-Writing-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased`.
* **Main Container:** `w-full max-w-7xl mx-auto px-4 py-6 md:px-6 md:py-8 lg:px-8 flex flex-col gap-6 md:gap-8 flex-1`.
* **Category Vertical List:** `flex flex-col gap-4 md:gap-5 w-full max-w-4xl mx-auto`.
* **Category Card Row:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5`.
* **Prompt List Container:** `w-full flex flex-col gap-6`.
* **Prompt Filter Bar Container:** `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
* **Prompt Grid Standard:** `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
* **Practice Top Bar:** `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
* **Practice Workspace Split Layout (2 Columns):** `w-full max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start`.
  * **Prompt Panel (Left):** `lg:col-span-5 w-full flex flex-col gap-4 lg:sticky lg:top-20`.
  * **Editor Panel (Right):** `lg:col-span-7 w-full flex flex-col gap-4`.
* **Grading Status Center Layout:** `w-full max-w-2xl mx-auto min-h-[calc(100vh-10rem)] flex flex-col items-center justify-center p-4`.
* **Result Page Container:** `w-full max-w-7xl mx-auto flex flex-col gap-8 py-4`.
* **Result Score Overview Grid:** `grid grid-cols-1 lg:grid-cols-3 gap-6 w-full`.
* **Result Comparison Split Layout (2 Columns):** `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full items-start`.
  * **Original Essay Panel (Left):** `lg:col-span-6 w-full flex flex-col gap-4`.
  * **AI Feedback Panel (Right):** `lg:col-span-6 w-full flex flex-col gap-4`.
* **History Page Container:** `w-full max-w-6xl mx-auto flex flex-col gap-6 py-4`.
* **Modal Overlay:** `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
* **Modal Box Standard:** `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
* **Responsive Rules:**
  * **Mobile (< 768px):** Toàn bộ chia cột co về 1 cột `flex-col`; lưới đề bài chuyển `grid-cols-1`; Workspace làm bài xếp chồng Đề bài lên trên Editor; trang kết quả chuyển dạng Tab trượt hoặc xếp dọc bài viết và nhận xét AI; các nút CTA mở rộng `w-full`.
  * **Tablet (768px - 1023px):** Lưới đề bài hiển thị 2 cột `grid-cols-2`; Workspace làm bài xếp chồng theo thứ tự ưu tiên; Header giữ khoảng cách `px-6`.
  * **Desktop (>= 1024px):** Lưới đề bài cố định 3 cột `grid-cols-3`; Workspace chia 2 cột tỉ lệ `5/7` với Đề bài sticky; Trang kết quả chia 2 cột đối chiếu `6/6` song song; khung trang giới hạn `max-w-7xl mx-auto`.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

#### Màn hình 1: Danh mục luyện viết (WritingCategoryPage)

* **WritingCategoryHeader [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full text-center max-w-2xl mx-auto py-4`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Subtitle: `text-sm md:text-base text-slate-500 font-normal leading-relaxed`.

* **WritingCategoryList [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full max-w-4xl mx-auto`.

* **WritingCategoryCard [DUMB]**:
  * Box Style: `group relative w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all`.
  * Left Content: `flex items-start gap-4 flex-1`.
  * Icon Container: `w-14 h-14 rounded-xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 flex-shrink-0 group-hover:scale-105 transition-transform`.
  * Info Stack: `flex flex-col gap-1.5 flex-1`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900 group-hover:text-sky-600 transition-colors`.
  * Typography Description: `text-sm text-slate-500 leading-relaxed line-clamp-2`.
  * Badge Row: `flex items-center gap-2 mt-1`.
  * Count Badge: `px-2.5 py-0.5 rounded-full bg-slate-100 text-xs font-semibold text-slate-600`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **WritingCategorySkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full max-w-4xl mx-auto`.
  * Card Skeleton: `w-full bg-white border border-slate-200 rounded-2xl p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 animate-pulse pointer-events-none`.
  * Icon Skeleton: `w-14 h-14 rounded-xl bg-slate-200 flex-shrink-0`.
  * Text Stack Skeleton: `flex flex-col gap-2.5 flex-1`.
  * Title Line: `h-6 w-1/3 rounded bg-slate-200`.
  * Desc Line: `h-4 w-3/4 rounded bg-slate-100`.
  * Button Skeleton: `h-11 w-32 rounded-xl bg-slate-200 flex-shrink-0`.

* **CategoryEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Action Button: `h-10 px-5 rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.

---

#### Màn hình 2: Danh sách đề bài theo danh mục (WritingPromptListPage)

* **PromptListHeader [DUMB]**:
  * Box Style: `flex flex-col gap-3 w-full pb-2`.
  * Breadcrumb Container: `w-full`.
  * Title Row: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Total Count: `text-sm font-medium text-slate-500`.

* **Breadcrumb [DUMB]**:
  * Box Style: `flex items-center gap-2 text-sm text-slate-500`.
  * Link: `hover:text-sky-600 transition-colors cursor-pointer`.
  * Separator: `text-slate-300 select-none`.
  * Active Item: `font-semibold text-slate-900 pointer-events-none`.

* **PromptFilterBar [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
  * Left Controls: `flex-1 flex flex-col sm:flex-row items-stretch sm:items-center gap-3`.
  * Right Controls: `flex items-center gap-3 flex-wrap`.

* **PromptSearchBar [DUMB]**:
  * Box Style: `relative w-full sm:max-w-xs`.
  * Input: `w-full h-11 pl-11 pr-10 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all`.
  * Search Icon: `absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4 pointer-events-none`.
  * Clear Button: `absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1 rounded-md transition-colors cursor-pointer`.

* **FilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[130px]`.
  * Select Standard: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **StatusFilterTabs [DUMB]**:
  * Box Style: `inline-flex items-center bg-slate-100 p-1 rounded-xl gap-1 border border-slate-200/60`.
  * Tab Default: `px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer`.
  * Tab Active: `px-3 py-1.5 rounded-lg text-xs font-bold bg-white text-sky-700 shadow-sm`.

* **PromptGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.

* **PromptCard [DUMB]**:
  * Box Style: `group relative flex flex-col justify-between bg-white border border-slate-200 rounded-2xl shadow-sm p-5 hover:border-sky-300 hover:shadow-md transition-all`.
  * Header Meta: `flex items-center justify-between gap-2 mb-3`.
  * Badge Group: `flex items-center gap-1.5 flex-wrap`.
  * Typography Title: `text-base md:text-lg font-bold text-slate-900 group-hover:text-sky-600 transition-colors line-clamp-2 leading-snug`.
  * Typography Description: `text-sm text-slate-500 line-clamp-3 mt-2 leading-relaxed min-h-[60px]`.
  * Info Metrics Row: `flex items-center gap-4 text-xs font-medium text-slate-500 mt-4 pt-3 border-t border-slate-100`.
  * Metric Item: `flex items-center gap-1.5`.
  * Score Banner: `mt-3 p-2 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-between text-xs font-semibold text-emerald-700`.
  * Action Button: `mt-4 w-full h-10 px-4 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer flex items-center justify-center gap-2`.

* **LevelBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-semibold border`.
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.

* **StatusBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border`.
  * Not Started: `bg-slate-100 text-slate-600 border-slate-200`.
  * Completed: `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * In Progress: `bg-amber-50 text-amber-700 border-amber-200`.

* **PaginationControl [DUMB]**:
  * Box Style: `flex items-center justify-center gap-2 w-full py-4`.
  * Button Base: `min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition-colors cursor-pointer flex items-center justify-center`.
  * Default: `border-slate-200 bg-white text-slate-700 hover:bg-slate-50`.
  * Active: `border-sky-600 bg-sky-600 text-white font-semibold shadow-sm`.
  * Disabled: `border-slate-200 text-slate-300 bg-slate-50 cursor-not-allowed`.

* **EmptyPromptState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Action Button: `h-10 px-5 rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.

* **PromptSkeleton [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
  * Card Skeleton: `bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-4 animate-pulse pointer-events-none`.
  * Badge Skeleton: `h-5 w-16 rounded bg-slate-200`.
  * Title Skeleton: `h-6 w-4/5 rounded bg-slate-200`.
  * Desc Line: `h-4 w-full rounded bg-slate-100`.
  * Meta Skeleton: `h-4 w-1/2 rounded bg-slate-100 pt-3`.
  * Button Skeleton: `h-10 w-full rounded-xl bg-slate-200 mt-2`.

---

#### Màn hình 3: Luyện viết (WritingPracticePage)

* **WritingPracticeHeader [DUMB]**:
  * Box Style: `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * Left Group: `flex items-center gap-3 max-w-[50%]`.
  * Exit Button: `inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-colors cursor-pointer`.
  * Typography Prompt Title: `text-sm font-bold text-slate-900 truncate hidden sm:inline-block`.
  * Center / Right Group: `flex items-center gap-3 sm:gap-6`.

* **CountdownTimer [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl font-mono text-sm font-bold border transition-colors`.
  * Normal State (> 5 phút): `bg-slate-50 text-slate-800 border-slate-200`.
  * Urgent State (<= 5 phút): `bg-amber-50 text-amber-700 border-amber-300 animate-pulse`.

* **AutoSaveIndicator [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 text-xs font-medium`.
  * Saved: `text-slate-500`.
  * Indicator Dot Saved: `w-2 h-2 rounded-full bg-emerald-500`.
  * Saving: `text-sky-600`.
  * Indicator Dot Saving: `w-2 h-2 rounded-full bg-sky-500 animate-ping`.
  * Error: `text-red-600`.
  * Indicator Dot Error: `w-2 h-2 rounded-full bg-red-500`.

* **WritingWorkspaceLayout [DUMB]**:
  * Box Style: `w-full max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start`.

* **PromptDetailPanel [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-5`.
  * Header Row: `flex items-center justify-between gap-2`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900 leading-snug`.
  * Requirements Box: `p-4 rounded-xl bg-slate-50 border border-slate-200/80 flex flex-col gap-2.5`.
  * Typography Requirement: `text-sm text-slate-700 leading-relaxed whitespace-pre-line`.
  * Instruction List: `text-xs text-slate-600 flex flex-col gap-1.5 list-disc pl-4`.
  * Target Box: `flex items-center justify-between pt-3 border-t border-slate-100 text-xs font-semibold text-slate-500`.

* **EssayEditorPanel [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4`.

* **EssayTextarea [DUMB]**:
  * Box Style: `w-full min-h-[380px] lg:min-h-[460px] p-4 rounded-xl border border-slate-200 bg-white text-base text-slate-900 leading-relaxed font-sans placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all resize-y`.

* **WordCountProgress [DUMB]**:
  * Box Style: `flex flex-col gap-1.5 w-full`.
  * Count Info Row: `flex items-center justify-between text-xs font-semibold`.
  * Current Count Normal: `text-slate-600`.
  * Current Count Met: `text-emerald-600 font-bold`.
  * Track: `w-full h-2 rounded-full bg-slate-100 overflow-hidden`.
  * Fill Normal: `h-full rounded-full bg-sky-600 transition-all duration-300`.
  * Fill Completed: `bg-emerald-600`.

* **EditorControlBar [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-4 border-t border-slate-100`.
  * Left Actions: `flex items-center gap-2`.
  * Draft Button: `h-11 px-4 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Submit Button Active: `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer flex items-center justify-center gap-2 w-full sm:w-auto`.
  * Submit Button Disabled: `h-11 px-6 rounded-xl bg-slate-200 text-slate-400 text-sm font-semibold cursor-not-allowed flex items-center justify-center gap-2 w-full sm:w-auto`.
  * Warning Hint: `text-xs text-amber-600 font-medium text-center sm:text-right`.

* **ConfirmExitModal [DUMB]**:
  * Overlay: `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
  * Modal Box: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Content: `text-sm text-slate-600 leading-relaxed`.
  * Button Group: `flex items-center justify-end gap-3 pt-2`.
  * Cancel Button: `h-10 px-4 rounded-xl border border-slate-200 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.
  * Exit Confirm Button: `h-10 px-4 rounded-xl bg-red-600 text-sm font-semibold text-white hover:bg-red-700 active:bg-red-800 transition-colors cursor-pointer`.

* **TimeUpSubmissionModal [DUMB]**:
  * Overlay: `fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4`.
  * Modal Box: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col text-center items-center gap-4`.
  * Icon Circle: `w-14 h-14 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center text-2xl font-bold`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `text-sm text-slate-600 leading-relaxed`.
  * Auto Submit Button: `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold hover:bg-sky-700 active:bg-sky-800 transition-colors shadow-sm cursor-pointer w-full`.

---

#### Màn hình 4: Đang AI chấm bài (WritingGradingStatusPage)

* **GradingStatusCard [DUMB]**:
  * Box Style: `w-full max-w-xl bg-white border border-slate-200 rounded-2xl shadow-sm p-6 sm:p-8 flex flex-col items-center text-center gap-6`.
  * Typography Prompt Title: `text-base font-bold text-slate-900 line-clamp-1`.

* **AiScanAnimation [DUMB]**:
  * Box Style: `relative w-28 h-28 flex items-center justify-center`.
  * Outer Pulse Circle: `absolute inset-0 rounded-full bg-sky-100 animate-ping opacity-75`.
  * Inner Circle: `relative w-20 h-20 rounded-full bg-sky-50 border-2 border-sky-600 flex items-center justify-center text-sky-600 text-3xl font-bold shadow-sm`.

* **GradingProgressSteps [DUMB]**:
  * Box Style: `w-full flex flex-col gap-3 py-2 text-left`.
  * Step Item: `flex items-center gap-3 p-3 rounded-xl border text-sm transition-all`.
  * Step Completed: `bg-emerald-50/50 border-emerald-200 text-emerald-800 font-semibold`.
  * Step Processing: `bg-sky-50 border-sky-200 text-sky-800 font-bold animate-pulse`.
  * Step Pending: `bg-slate-50 border-slate-100 text-slate-400 font-medium`.
  * Step Icon: `w-5 h-5 flex items-center justify-center text-xs flex-shrink-0`.

* **GradingNoticeBox [DUMB]**:
  * Box Style: `w-full p-4 rounded-xl bg-slate-50 border border-slate-200 text-left flex flex-col gap-1`.
  * Typography Title: `text-xs font-bold text-slate-700`.
  * Typography Desc: `text-xs text-slate-500 leading-relaxed`.

* **GradingActions [DUMB]**:
  * Box Style: `w-full pt-2`.
  * Return CTA: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full flex items-center justify-center gap-2`.

---

#### Màn hình 5: Kết quả bài viết (WritingResultPage)

* **ResultHeroHeader [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4`.
  * Left Info: `flex flex-col gap-1.5`.
  * Typography Title: `text-xl md:text-2xl font-bold text-slate-900 tracking-tight`.
  * Meta Row: `flex items-center gap-3 text-xs font-medium text-slate-500 flex-wrap`.

* **OverallScoreCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-5`.
  * Score Banner: `flex items-center justify-between p-4 rounded-xl bg-sky-50/70 border border-sky-100`.
  * Score Number: `text-3xl md:text-4xl font-extrabold text-sky-700 tracking-tight font-sans`.
  * General Feedback: `text-sm text-slate-700 leading-relaxed border-t border-slate-100 pt-4`.
  * Criteria List: `flex flex-col gap-3.5 pt-2`.

* **ScoreBadge [DUMB]**:
  * Box Style: `inline-flex items-center px-3 py-1 rounded-xl text-sm font-bold border`.
  * Excellent (>= 8.0): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Good (6.5 - 7.5): `bg-sky-50 text-sky-700 border-sky-200`.
  * Needs Work (< 6.5): `bg-amber-50 text-amber-700 border-amber-200`.

* **CriteriaScoreItem [DUMB]**:
  * Box Style: `flex flex-col gap-1.5 w-full`.
  * Label Row: `flex items-center justify-between text-xs font-semibold text-slate-700`.
  * Track: `w-full h-2 rounded-full bg-slate-100 overflow-hidden`.
  * Fill: `h-full rounded-full bg-sky-600 transition-all duration-300`.
  * Comment Text: `text-xs text-slate-500 italic mt-0.5`.

* **ResultComparisonLayout [DUMB]**:
  * Box Style: `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full items-start`.

* **OriginalEssayViewer [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4`.
  * Header: `flex items-center justify-between border-b border-slate-100 pb-3`.
  * Typography Title: `text-base font-bold text-slate-900`.
  * Essay Container: `text-base text-slate-800 leading-loose whitespace-pre-wrap font-sans min-h-[300px]`.
  * Highlight Error Grammar: `bg-rose-100 text-rose-900 border-b-2 border-rose-400 px-1 py-0.5 rounded cursor-pointer hover:bg-rose-200 transition-colors`.
  * Highlight Error Vocabulary: `bg-violet-100 text-violet-900 border-b-2 border-violet-400 px-1 py-0.5 rounded cursor-pointer hover:bg-violet-200 transition-colors`.
  * Highlight Error Expression: `bg-sky-100 text-sky-900 border-b-2 border-sky-400 px-1 py-0.5 rounded cursor-pointer hover:bg-sky-200 transition-colors`.
  * Active Highlight: `ring-2 ring-sky-600 ring-offset-1 font-semibold`.

* **AiFeedbackContainer [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-5`.
  * Typography Header: `text-base font-bold text-slate-900`.

* **FeedbackCategoryTabs [DUMB]**:
  * Box Style: `flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none w-full border-b border-slate-100`.
  * Tab Default: `px-3.5 py-1.5 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100 transition-colors whitespace-nowrap cursor-pointer`.
  * Tab Active: `px-3.5 py-1.5 rounded-xl text-xs font-bold bg-sky-600 text-white shadow-sm whitespace-nowrap`.

* **FeedbackCardList [DUMB]**:
  * Box Style: `flex flex-col gap-3.5 w-full max-h-[600px] overflow-y-auto pr-1`.

* **FeedbackCardItem [DUMB]**:
  * Box Style: `p-4 rounded-xl border transition-all flex flex-col gap-2.5 cursor-pointer`.
  * Default Card: `bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm`.
  * Selected Card: `bg-sky-50/40 border-sky-600 ring-1 ring-sky-600 shadow-sm`.
  * Tag Row: `flex items-center justify-between`.
  * Tag Grammar: `px-2 py-0.5 rounded text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200`.
  * Tag Vocabulary: `px-2 py-0.5 rounded text-xs font-semibold bg-violet-50 text-violet-700 border border-violet-200`.
  * Tag Expression: `px-2 py-0.5 rounded text-xs font-semibold bg-sky-50 text-sky-700 border border-sky-200`.
  * Original Snippet: `text-xs text-rose-600 line-through font-mono bg-rose-50/50 p-1.5 rounded`.
  * Suggested Snippet: `text-xs text-emerald-700 font-semibold font-mono bg-emerald-50/50 p-1.5 rounded`.
  * Explanation Text: `text-xs text-slate-600 leading-relaxed`.

* **GeneralAdviceBox [DUMB]**:
  * Box Style: `p-4 rounded-xl bg-sky-50/60 border border-sky-100 flex flex-col gap-2 mt-2`.
  * Typography Title: `text-xs font-bold text-sky-900`.
  * Typography Advice: `text-xs text-slate-700 leading-relaxed`.

* **ResultActionFooter [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row items-center justify-between gap-3 pt-6 border-t border-slate-200 w-full`.
  * Left CTA: `h-11 px-5 rounded-xl border border-slate-200 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Right Action Group: `flex items-center gap-3 w-full sm:w-auto`.
  * Rewrite CTA: `h-11 px-5 rounded-xl border border-sky-600 text-sky-600 bg-white text-sm font-semibold hover:bg-sky-50 active:bg-sky-100 transition-colors cursor-pointer w-full sm:w-auto`.
  * Next Prompt CTA: `h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto`.

* **ResultSkeleton [DUMB]**:
  * Box Style: `w-full flex flex-col gap-6 animate-pulse pointer-events-none`.
  * Hero Skeleton: `h-24 w-full rounded-2xl bg-slate-200`.
  * Grid Skeleton: `grid grid-cols-1 lg:grid-cols-12 gap-6 w-full`.
  * Left Box Skeleton: `lg:col-span-6 h-96 rounded-2xl bg-slate-200`.
  * Right Box Skeleton: `lg:col-span-6 h-96 rounded-2xl bg-slate-200`.

---

#### Màn hình bổ trợ: Lịch sử bài viết (WritingHistoryPage)

* **HistoryHeader [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 w-full`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Stat Group: `flex items-center gap-4 text-sm font-medium text-slate-500`.

* **HistoryFilterBar [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4`.

* **HistoryTable [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden`.
  * Table: `w-full text-left text-sm`.
  * Thead: `bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider`.
  * Th: `px-5 py-3.5`.
  * Tr: `border-b border-slate-100 hover:bg-slate-50/80 transition-colors`.
  * Td: `px-5 py-4 text-slate-700 font-medium`.
  * Action View Detail: `text-xs font-semibold text-sky-600 hover:text-sky-700 hover:underline cursor-pointer`.

* **HistoryEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * CTA Button: `h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold hover:bg-sky-700 transition-colors shadow-sm cursor-pointer`.

---

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary Color (Chủ đạo):** `bg-sky-600`, `text-sky-600`, `border-sky-600`.
* **Primary Hover:** `bg-sky-700`, `text-sky-700`.
* **Primary Active:** `bg-sky-800`.
* **Primary Light / Background Mềm:** `bg-sky-50`, `text-sky-700`, `border-sky-100`.
* **Page Background:** `bg-slate-50`.
* **Card / Panel Surface:** `bg-white`.
* **Border Standard:** `border-slate-200`.
* **Border Hover:** `border-slate-300`, `border-sky-300`.
* **Border Subtle / Divider:** `border-slate-100`.
* **Text Primary:** `text-slate-900`.
* **Text Secondary:** `text-slate-600`, `text-slate-500`.
* **Text Muted / Placeholder:** `text-slate-400`.
* **Success / High Score:** `bg-emerald-50`, `text-emerald-700`, `bg-emerald-600`, `border-emerald-200`.
* **Warning / Urgent Timer:** `bg-amber-50`, `text-amber-700`, `border-amber-300`, `text-amber-600`.
* **Danger / Error / Delete:** `bg-red-50`, `text-red-600`, `bg-red-600`, `border-red-200`, `border-red-400`.
* **AI Feedback Color Mapping:**
  * **Grammar (Ngữ pháp):** `bg-rose-50`, `text-rose-700`, `border-rose-200` / Highlight: `bg-rose-100 text-rose-900 border-rose-400`.
  * **Vocabulary (Từ vựng):** `bg-violet-50`, `text-violet-700`, `border-violet-200` / Highlight: `bg-violet-100 text-violet-900 border-violet-400`.
  * **Expression (Cách diễn đạt):** `bg-sky-50`, `text-sky-700`, `border-sky-200` / Highlight: `bg-sky-100 text-sky-900 border-sky-400`.
* **Proficiency Levels:**
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.
* **Disabled State:** `bg-slate-200`, `text-slate-400`, `border-slate-200`, `cursor-not-allowed`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG dùng mã màu HEX hoặc RGB tùy tiện trong JSX, chỉ dùng Tailwind classes.
  * TUYỆT ĐỐI KHÔNG dùng hiệu ứng gradient đa sắc, không hiệu ứng kính mờ (glassmorphism/backdrop-blur phức tạp).
  * TUYỆT ĐỐI KHÔNG sử dụng màu neon chói lóa.

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockWritingCategories = [
  {
    id: "cat_ielts",
    type: "IELTS",
    title: "IELTS Writing",
    description: "Luyện viết Task 1 phân tích biểu đồ và Task 2 nghị luận xã hội theo chuẩn chấm thi quốc tế.",
    totalPrompts: 48,
    iconName: "PenTool"
  },
  {
    id: "cat_toeic",
    type: "TOEIC",
    title: "TOEIC Writing",
    description: "Rèn luyện kỹ năng viết mô tả hình ảnh, phản hồi thư điện tử và bài luận quan điểm công sở.",
    totalPrompts: 36,
    iconName: "FileText"
  },
  {
    id: "cat_found",
    type: "FOUNDATION",
    title: "Nền tảng (Foundation)",
    description: "Xây dựng câu hoàn chỉnh, củng cố ngữ pháp cơ bản, viết đoạn văn ngắn mạch lạc từ A1 - B1.",
    totalPrompts: 24,
    iconName: "BookOpen"
  },
  {
    id: "cat_work",
    type: "WORK",
    title: "Tiếng Anh Công việc",
    description: "Viết email chuyên nghiệp, báo cáo tiến độ dự án, văn bản đàm phán hợp đồng thương mại.",
    totalPrompts: 30,
    iconName: "Briefcase"
  }
];

const mockCategoryFilterOptions = [
  { value: "ALL", label: "Tất cả danh mục" },
  { value: "IELTS", label: "IELTS" },
  { value: "TOEIC", label: "TOEIC" },
  { value: "FOUNDATION", label: "Nền tảng" },
  { value: "WORK", label: "Công việc" }
];

const mockTopicOptions = [
  { value: "ALL", label: "Tất cả chủ đề" },
  { value: "EDUCATION", label: "Giáo dục & Học đường" },
  { value: "TECHNOLOGY", label: "Công nghệ & AI" },
  { value: "ENVIRONMENT", label: "Môi trường & Biến đổi khí hậu" },
  { value: "BUSINESS", label: "Thương mại & Quản trị" },
  { value: "SOCIETY", label: "Xã hội & Đời sống" }
];

const mockLevelOptions = [
  { value: "ALL", label: "Tất cả trình độ" },
  { value: "A2", label: "A2 - Sơ cấp" },
  { value: "B1", label: "B1 - Trung cấp" },
  { value: "B2", label: "B2 - Trung cao cấp" },
  { value: "C1", label: "C1 - Cao cấp" }
];

const mockDurationOptions = [
  { value: "ALL", label: "Mọi thời lượng" },
  { value: "15", label: "15 phút" },
  { value: "30", label: "30 phút" },
  { value: "40", label: "40 phút" },
  { value: "60", label: "60 phút" }
];

const mockPromptsList = [
  {
    id: "prompt_ielts_01",
    category: "IELTS",
    title: "Impact of Artificial Intelligence on Future Employment",
    shortDescription: "Một số người cho rằng AI sẽ cướp đi việc làm của con người, trong khi số khác tin rằng nó tạo thêm nhiều cơ hội mới. Thảo luận cả hai quan điểm và đưa ra ý kiến.",
    fullRequirement: "Some people believe that Artificial Intelligence will replace human labor in most industries, leading to mass unemployment. Others argue that AI will create new job opportunities and boost economic productivity.\n\nDiscuss both views and give your own opinion. Give reasons for your answer and include any relevant examples from your own knowledge or experience.",
    topic: "TECHNOLOGY",
    level: "C1",
    durationMinutes: 40,
    minWords: 250,
    isCompleted: true,
    bestScore: 7.5
  },
  {
    id: "prompt_ielts_02",
    category: "IELTS",
    title: "Environmental Protection vs Economic Growth",
    shortDescription: "Chính phủ các nước nên ưu tiên phát triển kinh tế hay bảo vệ môi trường? Viết bài luận phân tích quan điểm và giải pháp.",
    fullRequirement: "Environmental pollution is a global concern. Should governments prioritize economic growth or environmental preservation? Discuss both views and give your opinion.",
    topic: "ENVIRONMENT",
    level: "B2",
    durationMinutes: 40,
    minWords: 250,
    isCompleted: false
  },
  {
    id: "prompt_toeic_01",
    category: "TOEIC",
    title: "Responding to a Client Project Delay Inquiry",
    shortDescription: "Khách hàng gửi thư phàn nàn vì dự án phần mềm bị chậm tiến độ 2 tuần. Hãy viết thư giải trình nguyên nhân và đưa ra phương án đền bù thỏa đáng.",
    fullRequirement: "Write an email responding to Ms. Johnson regarding the delayed software delivery. In your email, apologize for the inconvenience, explain the technical reasons for the 2-week delay, and propose a solution along with a revised timeline.",
    topic: "BUSINESS",
    level: "B1",
    durationMinutes: 20,
    minWords: 120,
    isCompleted: true,
    bestScore: 8.0
  },
  {
    id: "prompt_work_01",
    category: "WORK",
    title: "Formal Proposal for Remote Work Policy",
    shortDescription: "Đề xuất chính sách làm việc từ xa (Hybrid Work) gửi ban giám đốc, nêu rõ lợi ích tăng năng suất và giảm chi phí vận hành công ty.",
    fullRequirement: "Draft a formal internal proposal to the Board of Directors recommending a flexible hybrid work model. Highlight benefits regarding employee retention, operational cost reduction, and key performance indicators.",
    topic: "BUSINESS",
    level: "B2",
    durationMinutes: 30,
    minWords: 200,
    isCompleted: false
  },
  {
    id: "prompt_found_01",
    category: "FOUNDATION",
    title: "My Ideal Holiday Destination",
    shortDescription: "Viết đoạn văn ngắn miêu tả địa điểm du lịch yêu thích của bạn, lý do lựa chọn và các hoạt động bạn muốn trải nghiệm tại đó.",
    fullRequirement: "Write a short essay (100 - 150 words) describing your favorite holiday destination. Explain why you like it, the weather, and what activities you would recommend to visitors.",
    topic: "SOCIETY",
    level: "A2",
    durationMinutes: 15,
    minWords: 100,
    isCompleted: true,
    bestScore: 8.5
  }
];

const mockActivePracticeSession = {
  promptId: "prompt_ielts_01",
  title: "Impact of Artificial Intelligence on Future Employment",
  category: "IELTS",
  topic: "TECHNOLOGY",
  level: "C1",
  durationMinutes: 40,
  minWords: 250,
  remainingSeconds: 1845,
  saveStatus: "SAVED",
  lastSavedAt: "14:28:10",
  currentContent: "In recent years, the rapid advancement of artificial intelligence has sparked widespread debate across multiple sectors. While many individuals express anxiety that automation will displace millions of jobs, others maintain that AI will serve as a catalyst for economic innovation and new employment fields.\n\nOn the one hand, it is undeniable that repetitive manual and routine cognitive tasks are increasingly being automated. For example, robotic systems in manufacturing and automated customer service algorithms have reduced the need for human workers. Consequently, individuals without specialized skills might face severe employment challenges.\n\nOn the other hand, technological revolutions have historically generated more jobs than they eradicated...",
  currentWordCount: 112
};

const mockGradingSteps = [
  { label: "Đã lưu bản nộp bài viết an toàn lên hệ thống", isCompleted: true, isProcessing: false },
  { label: "Đang phân tích cấu trúc ngữ pháp và chính tả", isCompleted: true, isProcessing: false },
  { label: "Đang đánh giá vốn từ vựng học thuật và tính mạch lạc", isCompleted: false, isProcessing: true },
  { label: "Đang tổng hợp điểm tổng và lập bảng nhận xét chi tiết", isCompleted: false, isProcessing: false }
];

const mockSubmissionResult = {
  id: "sub_ielts_8821",
  promptId: "prompt_ielts_01",
  promptTitle: "Impact of Artificial Intelligence on Future Employment",
  submittedAt: "14:40:22 - 12/09/2026",
  timeSpentSeconds: 2155,
  totalWords: 284,
  overallScore: 7.5,
  criteriaScores: [
    { criterion: "Task Achievement (Đáp ứng yêu cầu đề bài)", score: 7.5, maxScore: 10, comment: "Bài viết thảo luận cân bằng cả hai quan điểm, lập luận rõ ràng." },
    { criterion: "Coherence & Cohesion (Tính mạch lạc & Liên kết)", score: 7.0, maxScore: 10, comment: "Phân đoạn hợp lý, tuy nhiên cần làm mượt hơn các từ nối chuyển ý." },
    { criterion: "Lexical Resource (Vốn từ vựng)", score: 8.0, maxScore: 10, comment: "Sử dụng tốt các thuật ngữ chuyên ngành công nghệ và cụm từ học thuật." },
    { criterion: "Grammatical Accuracy (Độ chuẩn xác ngữ pháp)", score: 7.5, maxScore: 10, comment: "Cấu trúc câu phức phong phú, chỉ mắc một vài lỗi nhỏ về mạo từ." }
  ],
  generalFeedback: "Bài viết đạt cấu trúc chuẩn IELTS Writing Task 2. Bạn đã phát triển được luận điểm chặt chẽ với các ví dụ thực tiễn sinh động. Để đạt Band 8.0+, hãy chú ý đa dạng hóa cách diễn đạt ở phần kết bài và tinh chỉnh sự tự nhiên của các liên từ.",
  originalEssay: "In recent years, the rapid advancement of artificial intelligence has sparked widespread debate across multiple sectors. While many individuals express anxiety that automation will displace millions of jobs, others maintain that AI will serve as a catalyst for economic innovation and new employment fields.\n\nOn the one hand, it is undeniable that repetitive manual tasks are increasingly being automated. For example, robotic systems in manufacturing and automated customer service algorithms has reduced the need for human staff. Furthermore, people should to adapt rapidly to avoid unemployment.\n\nOn the other hand, new technologies always create innovative professions that did not exist before, such as AI prompt engineers and data analysts. Therefore, governments should invest in education to prepare workers for this paradigm shift.\n\nIn conclusion, although AI poses short-term disruption, its long-term benefits in job creation will outweight the negatives.",
  feedbacks: [
    {
      id: "fb_01",
      type: "GRAMMAR",
      originalText: "algorithms has reduced",
      suggestedText: "algorithms have reduced",
      explanation: "Chủ ngữ 'robotic systems ... and algorithms' là danh từ số nhiều, động từ thì hiện tại hoàn thành phải chia là 'have reduced' thay vì 'has reduced'."
    },
    {
      id: "fb_02",
      type: "GRAMMAR",
      originalText: "should to adapt",
      suggestedText: "should adapt",
      explanation: "Sau động từ khuyết thiếu 'should', sử dụng động từ nguyên thể không 'to' (bare infinitive)."
    },
    {
      id: "fb_03",
      type: "VOCABULARY",
      originalText: "outweight",
      suggestedText: "outweigh",
      explanation: "'Outweigh' là động từ (vượt trội hơn). 'Outweight' là lỗi sai chính tả thường gặp do nhầm lẫn với danh từ 'weight'."
    },
    {
      id: "fb_04",
      type: "EXPRESSION",
      originalText: "its long-term benefits in job creation",
      suggestedText: "its long-term potential for employment generation",
      explanation: "Cụm 'potential for employment generation' mang văn phong học thuật trang trọng và ghi điểm cao hơn trong tiêu chí Lexical Resource."
    }
  ]
};

const mockWritingHistory = [
  {
    submissionId: "sub_ielts_8821",
    promptTitle: "Impact of Artificial Intelligence on Future Employment",
    category: "IELTS",
    submittedAt: "14:40 - 12/09/2026",
    totalWords: 284,
    timeSpentSeconds: 2155,
    overallScore: 7.5,
    status: "COMPLETED"
  },
  {
    submissionId: "sub_toeic_7712",
    promptTitle: "Responding to a Client Project Delay Inquiry",
    category: "TOEIC",
    submittedAt: "10:15 - 10/09/2026",
    totalWords: 135,
    timeSpentSeconds: 1120,
    overallScore: 8.0,
    status: "COMPLETED"
  },
  {
    submissionId: "sub_found_6603",
    promptTitle: "My Ideal Holiday Destination",
    category: "FOUNDATION",
    submittedAt: "16:30 - 05/09/2026",
    totalWords: 120,
    timeSpentSeconds: 840,
    overallScore: 8.5,
    status: "COMPLETED"
  }
];

const mockConfirmExitData = {
  title: "Thoát khỏi phòng luyện viết?",
  description: "Bài viết của bạn đã được lưu tự động thành bản nháp. Tuy nhiên, thời gian làm bài sẽ không tạm dừng nếu bạn rời đi. Bạn có chắc chắn muốn thoát?"
};

const mockTimeUpData = {
  title: "Hết thời gian làm bài!",
  description: "Thời gian quy định cho đề bài này đã kết thúc. Hệ thống sẽ tự động nộp bài viết hiện tại của bạn cho AI để tiến hành chấm điểm và nhận xét."
};
```
