# 07-Speaking-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Layout:** `min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased`.
* **Main Container Chung:** `w-full max-w-7xl mx-auto px-4 py-6 md:px-6 md:py-8 lg:px-8 flex flex-col gap-6 md:gap-8 flex-1`.
* **Category Vertical Layout (Màn 1):** `w-full max-w-4xl mx-auto flex flex-col gap-6 md:gap-8 py-4`.
  * **Resume Banner Section:** `w-full flex flex-col gap-3`.
  * **Category List Vertical Container:** `flex flex-col gap-4 md:gap-5 w-full`.
* **Lesson List Layout (Màn 2):** `w-full flex flex-col gap-6`.
  * **Filter Bar Container:** `w-full bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4`.
  * **Lesson Grid Standard:** `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.
* **Speaking Practice Studio Layout (Màn 3 - Focused Studio):** `w-full min-h-screen bg-slate-50 flex flex-col justify-between`.
  * **Studio Header Bar:** `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * **Studio Center Workspace:** `w-full max-w-3xl mx-auto px-4 py-6 md:py-8 flex flex-col gap-6 items-stretch flex-1 justify-center`.
  * **Sentence Prompt Container:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col items-center text-center gap-5`.
  * **Recording Controller Box:** `w-full bg-slate-900 text-white rounded-2xl p-6 md:p-8 flex flex-col items-center justify-center gap-6 shadow-md relative overflow-hidden`.
  * **Realtime Feedback Container:** `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4 animate-in fade-in duration-300`.
  * **Practice Studio Bottom Action Bar:** `sticky bottom-0 z-20 w-full bg-white/95 backdrop-blur-sm border-t border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between shadow-lg`.
* **Speaking Result Layout (Màn 4):** `w-full max-w-5xl mx-auto flex flex-col gap-8 py-4 md:py-6`.
  * **Result Header & Overall Box:** `w-full flex flex-col gap-6`.
  * **Criteria Grid (4 Columns):** `grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 w-full`.
  * **Strengths & Improvements Split (2 Columns):** `grid grid-cols-1 md:grid-cols-2 gap-6 w-full`.
  * **Sentence Detail Review List:** `w-full flex flex-col gap-4`.
  * **Result Action Footer:** `w-full bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4 mt-4`.
* **Modal Overlay:** `fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4`.
* **Modal Box Standard:** `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-5`.
* **Responsive Rules:**
  * **Mobile (< 768px):** Tất cả các lưới thu về 1 cột `flex-col`; Lưới bài học chuyển `grid-cols-1`; Lưới 4 tiêu chí kết quả xếp chồng 1 cột; Cụm nút điều khiển ghi âm ưu tiên nút bấm to tối thiểu 56px (`w-14 h-14`) dễ chạm ngón tay; Các nút CTA chính mở rộng toàn chiều ngang `w-full`.
  * **Tablet (768px - 1023px):** Lưới bài học hiển thị 2 cột `grid-cols-2`; Lưới tiêu chí kết quả chia 2x2 `grid-cols-2`; Khung phòng thu Speaking Studio giới hạn độ rộng `max-w-2xl mx-auto`.
  * **Desktop (>= 1024px):** Lưới bài học cố định 3 cột `grid-cols-3`; Studio thu âm căn giữa tập trung thị giác tối đa `max-w-3xl mx-auto`; Trang kết quả chia rõ 4 cột tiêu chí ngang hàng và 2 cột Điểm mạnh - Điểm yếu đối xứng; Giới hạn độ rộng nội dung `max-w-5xl` hoặc `max-w-7xl`.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

#### Màn hình 1: Trang danh mục luyện nói (SpeakingCategoryPage)

* **SpeakingCategoryHeader [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full text-center max-w-2xl mx-auto py-2`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Typography Description: `text-sm md:text-base text-slate-500 font-normal leading-relaxed`.

* **MicrophonePermissionNotice [DUMB]**:
  * Box Style: `w-full rounded-2xl border p-4 sm:p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition-colors`.
  * Trạng thái Đã cấp quyền (GRANTED): `bg-emerald-50/70 border-emerald-200 text-emerald-900`.
  * Trạng thái Chưa cấp quyền (PROMPT): `bg-sky-50/80 border-sky-200 text-sky-900`.
  * Trạng thái Bị từ chối (DENIED): `bg-rose-50 border-rose-200 text-rose-900`.
  * Icon Container: `w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 text-lg`.
  * Typography Title: `text-sm font-bold`.
  * Typography Description: `text-xs text-slate-600 mt-0.5 leading-relaxed`.
  * Action Button: `h-9 px-4 rounded-xl text-xs font-semibold shadow-xs transition-colors cursor-pointer flex-shrink-0 flex items-center gap-1.5`.

* **SpeakingResumeBanner [DUMB]**:
  * Box Style: `w-full flex flex-col gap-3`.
  * Section Title: `text-sm font-bold uppercase tracking-wider text-slate-400 flex items-center gap-2`.

* **ResumeCard [DUMB]**:
  * Box Style: `group w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:border-sky-300 hover:shadow-md transition-all`.
  * Left Stack: `flex items-center gap-4 flex-1`.
  * Icon Box: `w-12 h-12 rounded-xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 text-xl flex-shrink-0 group-hover:scale-105 transition-transform`.
  * Info Stack: `flex flex-col gap-1 flex-1`.
  * Typography Title: `text-base font-bold text-slate-900 group-hover:text-sky-600 transition-colors line-clamp-1`.
  * Meta Row: `flex items-center gap-3 text-xs text-slate-500 font-medium`.
  * Progress Text: `text-xs font-semibold text-sky-600 bg-sky-50 px-2 py-0.5 rounded-md border border-sky-100`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **SpeakingCategoryList [DUMB]**:
  * Box Style: `flex flex-col gap-4 md:gap-5 w-full`.

* **SpeakingCategoryCard [DUMB]**:
  * Box Style: `group relative w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5 hover:border-sky-300 hover:shadow-md transition-all cursor-pointer`.
  * Left Content: `flex items-start gap-4 flex-1`.
  * Icon Container: `w-14 h-14 rounded-xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 text-2xl flex-shrink-0 group-hover:scale-105 transition-transform`.
  * Info Stack: `flex flex-col gap-1.5 flex-1`.
  * Typography Title: `text-lg md:text-xl font-bold text-slate-900 group-hover:text-sky-600 transition-colors`.
  * Typography Description: `text-sm text-slate-500 leading-relaxed line-clamp-2`.
  * Badge Row: `flex items-center gap-2 mt-1`.
  * Count Badge: `px-2.5 py-0.5 rounded-full bg-slate-100 text-xs font-semibold text-slate-600 border border-slate-200/60`.
  * CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto flex-shrink-0`.

* **SpeakingCategorySkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full animate-pulse pointer-events-none`.
  * Card Skeleton: `w-full bg-white border border-slate-200 rounded-2xl p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-5`.
  * Icon Skeleton: `w-14 h-14 rounded-xl bg-slate-200 flex-shrink-0`.
  * Text Stack Skeleton: `flex flex-col gap-2.5 flex-1`.
  * Title Line: `h-6 w-1/3 rounded bg-slate-200`.
  * Description Line: `h-4 w-3/4 rounded bg-slate-100`.
  * Button Skeleton: `h-11 w-32 rounded-xl bg-slate-200 flex-shrink-0`.

* **SpeakingCategoryEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Action Button: `h-10 px-5 rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 active:bg-slate-100 transition-colors cursor-pointer`.

---

#### Màn hình 2: Danh sách bài luyện theo danh mục (SpeakingLessonListPage)

* **SpeakingLessonListHeader [DUMB]**:
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

* **SpeakingLessonFilterBar [DUMB]**:
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

* **DurationFilterDropdown [DUMB]**:
  * Box Style: `relative min-w-[130px]`.
  * Select Box: `w-full h-11 px-3.5 pr-9 rounded-xl border border-slate-200 bg-white text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-600/20 focus:border-sky-600 transition-all appearance-none cursor-pointer`.
  * Chevron Icon: `absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none w-4 h-4`.

* **LessonStatusTabs [DUMB]**:
  * Box Style: `inline-flex items-center bg-slate-100 p-1 rounded-xl gap-1 border border-slate-200/60`.
  * Tab Default: `px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:text-slate-900 transition-colors cursor-pointer`.
  * Tab Active: `px-3 py-1.5 rounded-lg text-xs font-bold bg-white text-sky-700 shadow-sm`.

* **SpeakingLessonGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full`.

* **SpeakingLessonCard [DUMB]**:
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

* **SentenceCountBadge [DUMB]**:
  * Box Style: `inline-flex items-center gap-1 text-xs font-medium text-slate-500`.
  * Icon: `w-3.5 h-3.5 text-slate-400`.

* **PaginationControl [DUMB]**:
  * Box Style: `flex items-center justify-center gap-2 w-full py-4`.
  * Button Base: `min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition-colors cursor-pointer flex items-center justify-center`.
  * Default: `border-slate-200 bg-white text-slate-700 hover:bg-slate-50`.
  * Active: `border-sky-600 bg-sky-600 text-white font-semibold shadow-sm`.
  * Disabled: `border-slate-200 text-slate-300 bg-slate-50 cursor-not-allowed`.

* **SpeakingLessonEmptyState [DUMB]**:
  * Box Style: `w-full bg-white border border-dashed border-slate-300 rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `max-w-md text-sm text-slate-500 leading-relaxed`.
  * Reset Button: `h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer`.

* **SpeakingLessonListSkeleton [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full animate-pulse pointer-events-none`.
  * Card Skeleton: `w-full bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-4`.
  * Line Header: `h-4 w-1/3 rounded bg-slate-200`.
  * Line Title: `h-6 w-3/4 rounded bg-slate-200`.
  * Line Button: `h-10 w-full rounded-xl bg-slate-100 mt-2`.

---

#### Màn hình 3: Luyện nói (Speaking Studio - SpeakingPracticePage)

* **SpeakingPracticeHeader [DUMB]**:
  * Box Style: `w-full bg-white border-b border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-sm`.
  * Exit Button: `inline-flex items-center gap-2 px-3 py-1.5 rounded-xl border border-slate-200 text-slate-600 hover:text-slate-900 hover:bg-slate-50 text-xs font-semibold transition-colors cursor-pointer`.
  * Center Info: `flex flex-col items-center gap-1`.
  * Typography Lesson Title: `text-sm md:text-base font-bold text-slate-900 line-clamp-1`.
  * Progress Text: `text-xs font-semibold text-sky-600`.
  * Right Action: `w-20 flex justify-end`.

* **PracticeProgressBar [DUMB]**:
  * Box Style: `w-full max-w-xs md:max-w-sm h-1.5 rounded-full bg-slate-100 overflow-hidden`.
  * Indicator: `h-full bg-sky-600 rounded-full transition-all duration-300 ease-out`.

* **SpeakingStudioLayout [DUMB]**:
  * Box Style: `w-full max-w-3xl mx-auto px-4 py-6 md:py-8 flex flex-col gap-6 items-stretch flex-1 justify-center`.

* **SentencePromptCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col items-center text-center gap-5 relative`.
  * Audio Sample Button Container: `self-end sm:absolute sm:top-5 sm:right-5`.

* **NativeAudioButton [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 px-3 py-1.5 rounded-xl border border-sky-200 bg-sky-50 text-sky-700 text-xs font-semibold hover:bg-sky-100 active:bg-sky-200 transition-colors cursor-pointer shadow-xs`.
  * Active Playing Style: `ring-2 ring-sky-400 bg-sky-100 animate-pulse`.
  * Icon: `w-4 h-4`.

* **SentenceTextDisplay [DUMB]**:
  * Box Style: `w-full max-w-2xl py-2 px-1`.
  * Typography: `text-xl md:text-2xl lg:text-3xl font-bold tracking-tight text-slate-900 leading-relaxed md:leading-snug select-text`.

* **PhoneticGuideHint [DUMB]**:
  * Box Style: `w-full max-w-lg bg-slate-50 border border-slate-200/80 rounded-xl px-4 py-2 flex items-center justify-center gap-2 text-xs text-slate-500 font-mono`.
  * Hint Label: `font-sans font-bold text-slate-400 uppercase text-[10px] tracking-wider`.
  * Hint Content: `text-slate-700 font-medium`.

* **AudioRecordingController [DUMB]**:
  * Box Style: `w-full bg-slate-900 text-white border border-slate-800 rounded-2xl p-6 md:p-8 flex flex-col items-center justify-center gap-6 shadow-md relative overflow-hidden`.
  * Top Bar: `w-full flex items-center justify-between text-xs text-slate-400`.

* **MicStatusIndicator [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold border`.
  * Sẵn sàng (IDLE): `bg-slate-800 text-slate-300 border-slate-700`.
  * Đang thu âm (RECORDING): `bg-rose-950/80 text-rose-400 border-rose-800/80 animate-pulse`.
  * Đang phân tích (ANALYZING): `bg-amber-950/80 text-amber-400 border-amber-800/80`.
  * Đã đánh giá (EVALUATED): `bg-emerald-950/80 text-emerald-400 border-emerald-800/80`.
  * Lỗi / Chưa cấp quyền (ERROR / DENIED): `bg-rose-950 text-rose-300 border-rose-700`.
  * Status Dot: `w-2 h-2 rounded-full`.

* **LiveAudioWaveform [DUMB]**:
  * Box Style: `w-full h-16 sm:h-20 flex items-center justify-center gap-1 px-4 overflow-hidden`.
  * Waveform Bar Base: `w-1 sm:w-1.5 rounded-full bg-slate-700 transition-all duration-75`.
  * Active Recording Bar: `bg-sky-400 shadow-sky-400/50`.
  * Inactive Bar: `h-2 bg-slate-800`.

* **RecordingTimer [DUMB]**:
  * Box Style: `font-mono text-lg md:text-xl font-bold tracking-wider text-slate-200`.
  * Active Recording Style: `text-rose-400 animate-pulse`.

* **RecordingActionButtons [DUMB]**:
  * Box Style: `flex items-center justify-center gap-4 sm:gap-6 flex-wrap`.
  * Record CTA (Bắt đầu ghi âm): `w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-sky-600 hover:bg-sky-500 active:scale-95 text-white flex items-center justify-center text-2xl shadow-lg shadow-sky-600/30 transition-all cursor-pointer`.
  * Stop CTA (Dừng thu âm): `w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-rose-600 hover:bg-rose-500 active:scale-95 text-white flex items-center justify-center text-2xl shadow-lg shadow-rose-600/30 transition-all cursor-pointer animate-pulse`.
  * Playback Button (Nghe lại bản thu): `w-11 h-11 rounded-full bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 hover:text-white flex items-center justify-center text-lg transition-colors cursor-pointer`.
  * ReRecord Button (Đọc lại): `w-11 h-11 rounded-full bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 hover:text-white flex items-center justify-center text-lg transition-colors cursor-pointer`.

* **RealtimeAiFeedbackCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-5 md:p-6 flex flex-col gap-4 animate-in fade-in duration-300`.
  * Top Feedback Row: `flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100`.

* **SentenceScorePill [DUMB]**:
  * Box Style: `inline-flex items-center gap-2 px-3 py-1.5 rounded-xl border text-sm font-bold`.
  * Cao (>= 80): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Khá (60 - 79): `bg-sky-50 text-sky-700 border-sky-200`.
  * Cần cố gắng (< 60): `bg-amber-50 text-amber-700 border-amber-200`.

* **CriteriaPillList [DUMB]**:
  * Box Style: `flex items-center gap-2 flex-wrap`.
  * Mini Pill: `inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold border`.
  * Pronunciation Pill: `bg-sky-50 text-sky-700 border-sky-200`.
  * Fluency Pill: `bg-teal-50 text-teal-700 border-teal-200`.
  * Vocabulary Pill: `bg-indigo-50 text-indigo-700 border-indigo-200`.
  * Grammar Pill: `bg-violet-50 text-violet-700 border-violet-200`.

* **SentenceHighlightFeedback [DUMB]**:
  * Box Style: `w-full p-4 rounded-xl bg-slate-50 border border-slate-200/80 flex flex-wrap gap-2 items-center leading-relaxed`.
  * Word Excellent/Good: `px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-800 font-medium text-sm md:text-base border border-emerald-200 cursor-default`.
  * Word Needs Work: `px-2 py-0.5 rounded-md bg-amber-100 text-amber-800 font-medium text-sm md:text-base border border-amber-200 cursor-default`.
  * Word Poor/Sai: `px-2 py-0.5 rounded-md bg-rose-100 text-rose-800 font-medium text-sm md:text-base border border-rose-200 underline decoration-rose-500 decoration-wavy cursor-default`.

* **ConciseTipBox [DUMB]**:
  * Box Style: `w-full p-3.5 rounded-xl bg-sky-50 border border-sky-100 flex items-start gap-3`.
  * Tip Icon: `w-4 h-4 text-sky-600 mt-0.5 flex-shrink-0`.
  * Typography Tip: `text-xs md:text-sm text-sky-900 font-medium leading-relaxed`.

* **PracticeFlowActionBar [DUMB]**:
  * Box Style: `sticky bottom-0 z-20 w-full bg-white/95 backdrop-blur-sm border-t border-slate-200 px-4 py-3 md:px-8 flex items-center justify-between shadow-lg gap-4`.
  * Retry Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 text-sm font-semibold transition-colors cursor-pointer`.
  * Next / Finish CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 disabled:opacity-50 disabled:cursor-not-allowed transition-all cursor-pointer`.

* **AiAnalyzingModal [DUMB]**:
  * Box Style: `w-full max-w-sm bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col items-center text-center gap-4`.
  * Spinner Box: `w-14 h-14 rounded-2xl bg-sky-50 border border-sky-100 flex items-center justify-center text-sky-600 text-2xl animate-spin`.
  * Typography Title: `text-base font-bold text-slate-900`.
  * Typography Description: `text-xs text-slate-500 leading-relaxed`.

* **MicPermissionDeniedModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center text-xl self-center`.
  * Typography Title: `text-lg font-bold text-slate-900 text-center`.
  * Typography Description: `text-sm text-slate-600 leading-relaxed text-center`.
  * Guide Steps Box: `bg-slate-50 border border-slate-200 rounded-xl p-4 text-xs text-slate-600 flex flex-col gap-2`.
  * Button Group: `flex items-center justify-end gap-3 mt-2`.
  * Close Button: `h-10 px-4 rounded-xl border border-slate-200 text-slate-700 text-sm font-medium hover:bg-slate-50 cursor-pointer`.
  * Retry Button: `h-10 px-5 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 cursor-pointer`.

* **AudioSilenceWarningModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col items-center text-center gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center text-xl`.
  * Typography Title: `text-lg font-bold text-slate-900`.
  * Typography Description: `text-sm text-slate-600 leading-relaxed`.
  * Action Button: `w-full h-11 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 cursor-pointer flex items-center justify-center gap-2`.

* **ConfirmExitPracticeModal [DUMB]**:
  * Box Style: `w-full max-w-md bg-white border border-slate-200 rounded-2xl shadow-xl p-6 flex flex-col gap-4`.
  * Icon Container: `w-12 h-12 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center text-xl self-center`.
  * Typography Title: `text-lg font-bold text-slate-900 text-center`.
  * Typography Description: `text-sm text-slate-600 leading-relaxed text-center`.
  * Button Group: `flex items-center justify-end gap-3 mt-2`.
  * Cancel Button: `h-10 px-4 rounded-xl border border-slate-200 text-slate-700 text-sm font-medium hover:bg-slate-50 cursor-pointer`.
  * Confirm Exit Button: `h-10 px-5 rounded-xl bg-rose-600 text-white text-sm font-semibold shadow-sm hover:bg-rose-700 cursor-pointer`.

---

#### Màn hình 4: Kết quả và góp ý AI (SpeakingResultPage)

* **SpeakingResultHeader [DUMB]**:
  * Box Style: `flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 w-full pb-4 border-b border-slate-200`.
  * Title Stack: `flex flex-col gap-1`.
  * Typography Title: `text-2xl md:text-3xl font-bold tracking-tight text-slate-900`.
  * Meta Row: `flex items-center gap-3 text-xs md:text-sm text-slate-500 font-medium`.

* **OverallScoreCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-6 md:p-8 flex flex-col md:flex-row items-center gap-6 md:gap-8`.
  * Score Box: `flex flex-col items-center justify-center p-6 rounded-2xl bg-sky-50 border border-sky-100 min-w-[160px] flex-shrink-0`.
  * Band Pill: `mt-2 px-3 py-0.5 rounded-full bg-sky-600 text-white text-xs font-bold`.
  * Feedback Stack: `flex flex-col gap-3 flex-1 text-center md:text-left`.
  * Typography Greeting: `text-lg md:text-xl font-bold text-slate-900`.

* **SpeakingScoreBadge [DUMB]**:
  * Box Style: `text-4xl md:text-5xl font-black tracking-tight text-sky-600`.
  * Max Score Suffix: `text-base font-semibold text-slate-400 ml-1`.

* **AiSummaryQuoteBox [DUMB]**:
  * Box Style: `p-4 rounded-xl bg-slate-50 border-l-4 border-sky-600 text-slate-700 text-sm italic leading-relaxed`.

* **CriteriaEvaluationGrid [DUMB]**:
  * Box Style: `grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 w-full`.

* **CriteriaCard [DUMB]** (Dùng chung cho Pronunciation, Fluency, Vocabulary, Grammar):
  * Box Style: `bg-white border border-slate-200 rounded-2xl shadow-sm p-5 flex flex-col justify-between gap-4 hover:border-slate-300 transition-colors`.
  * Card Header: `flex items-center justify-between gap-2`.
  * Icon Title Box: `flex items-center gap-2 text-sm font-bold text-slate-900`.
  * Score Badge: `px-2.5 py-1 rounded-lg text-sm font-bold border`.
  * Comment Box: `text-xs text-slate-600 leading-relaxed flex-1`.
  * Progress Bar: `w-full h-1.5 rounded-full bg-slate-100 overflow-hidden mt-1`.

* **StrengthWeaknessSection [DUMB]**:
  * Box Style: `grid grid-cols-1 md:grid-cols-2 gap-6 w-full`.

* **StrengthsCard [DUMB]**:
  * Box Style: `w-full bg-white border border-emerald-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4`.
  * Header: `flex items-center gap-2.5 text-base font-bold text-emerald-800`.
  * List: `flex flex-col gap-2.5`.
  * Item: `flex items-start gap-2.5 text-xs md:text-sm text-slate-700 leading-relaxed`.
  * Check Icon: `w-4 h-4 text-emerald-600 mt-0.5 flex-shrink-0`.

* **ImprovementsCard [DUMB]**:
  * Box Style: `w-full bg-white border border-amber-200 rounded-2xl shadow-sm p-6 flex flex-col gap-4`.
  * Header: `flex items-center gap-2.5 text-base font-bold text-amber-800`.
  * List: `flex flex-col gap-2.5`.
  * Item: `flex items-start gap-2.5 text-xs md:text-sm text-slate-700 leading-relaxed`.
  * Target Icon: `w-4 h-4 text-amber-600 mt-0.5 flex-shrink-0`.

* **DetailedSentenceReviewSection [DUMB]**:
  * Box Style: `w-full flex flex-col gap-4`.
  * Section Header: `text-lg font-bold text-slate-900 flex items-center justify-between gap-2`.

* **SentenceReviewCard [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl shadow-sm p-5 md:p-6 flex flex-col gap-4 transition-all`.
  * Selected State: `border-sky-400 ring-2 ring-sky-100`.

* **SentenceReviewHeader [DUMB]**:
  * Box Style: `flex items-center justify-between gap-2 pb-3 border-b border-slate-100`.
  * Sentence Index Badge: `px-2.5 py-0.5 rounded-md bg-slate-100 text-xs font-bold text-slate-700`.
  * Score & Duration: `flex items-center gap-3 text-xs font-semibold text-slate-500`.

* **ColoredSentenceText [DUMB]**:
  * Box Style: `w-full p-4 rounded-xl bg-slate-50 border border-slate-200/80 flex flex-wrap gap-2 items-center leading-relaxed`.

* **AudioCompareToolbar [DUMB]**:
  * Box Style: `flex items-center gap-3 flex-wrap py-1`.
  * User Audio Button: `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 text-xs font-semibold shadow-xs transition-colors cursor-pointer`.
  * Native Audio Button: `inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl border border-sky-200 bg-sky-50 text-sky-700 hover:bg-sky-100 active:bg-sky-200 text-xs font-semibold shadow-xs transition-colors cursor-pointer`.

* **MispronouncedWordsBox [DUMB]**:
  * Box Style: `w-full bg-rose-50/70 border border-rose-200/80 rounded-xl p-4 flex flex-col gap-2.5`.
  * Box Title: `text-xs font-bold uppercase tracking-wider text-rose-700 flex items-center gap-1.5`.
  * Word Item Row: `flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-2 rounded-lg bg-white border border-rose-100`.
  * Word Info: `flex items-center gap-2 text-xs md:text-sm`.
  * Word Text: `font-bold text-rose-800`.
  * Correct IPA: `font-mono text-xs text-slate-600 bg-slate-100 px-2 py-0.5 rounded`.
  * Word Tip: `text-xs text-slate-600 italic`.

* **SentenceImprovementTip [DUMB]**:
  * Box Style: `w-full p-3 rounded-xl bg-sky-50 border border-sky-100 text-xs text-sky-900 font-medium leading-relaxed flex items-start gap-2`.

* **SpeakingResultActionFooter [DUMB]**:
  * Box Style: `w-full bg-white border border-slate-200 rounded-2xl p-5 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4 mt-2`.
  * Retry CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-6 rounded-xl bg-sky-600 text-white text-sm font-semibold shadow-sm hover:bg-sky-700 active:bg-sky-800 transition-colors cursor-pointer w-full sm:w-auto`.
  * Choose Other CTA Button: `inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 active:bg-slate-100 text-sm font-semibold transition-colors cursor-pointer w-full sm:w-auto`.
  * Dashboard Link Button: `inline-flex items-center justify-center gap-2 h-11 px-4 text-slate-500 hover:text-slate-900 text-sm font-medium transition-colors cursor-pointer w-full sm:w-auto`.

* **SpeakingResultSkeleton [DUMB]**:
  * Box Style: `flex flex-col gap-6 w-full animate-pulse pointer-events-none`.
  * Header Skeleton: `w-full h-24 bg-slate-200 rounded-2xl`.
  * Grid Skeleton: `grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 w-full`.
  * Grid Item Skeleton: `h-36 bg-slate-200 rounded-2xl`.
  * Detail Skeleton: `w-full h-80 bg-slate-200 rounded-2xl`.

---

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary Color (Chủ đạo):** `bg-sky-600`, `text-sky-600`, `border-sky-600`.
* **Primary Hover:** `bg-sky-700`, `text-sky-700`.
* **Primary Active:** `bg-sky-800`.
* **Primary Light / Nền mục chọn:** `bg-sky-50`, `text-sky-700`, `border-sky-100`.
* **Page Background:** `bg-slate-50`.
* **Card / Container Background:** `bg-white`.
* **Studio Controller Background:** `bg-slate-900`, `text-white`, `border-slate-800`.
* **Border Standard:** `border-slate-200`.
* **Border Hover:** `border-slate-300`, `border-sky-300`.
* **Border Subtle / Divider:** `border-slate-100`.
* **Text Primary:** `text-slate-900`.
* **Text Secondary:** `text-slate-600`, `text-slate-500`.
* **Text Muted / Placeholder:** `text-slate-400`.
* **Recording & Mic States (Màu sắc phòng thu):**
  * Sẵn sàng (Ready): `bg-sky-50 text-sky-700 border-sky-200`.
  * Đang ghi âm (Recording Active): Nút dừng `bg-rose-600 hover:bg-rose-500 shadow-rose-600/30`, badge `bg-rose-950/80 text-rose-400 border-rose-800/80 animate-pulse`.
  * Đang phân tích AI (Analyzing): `bg-amber-950/80 text-amber-400 border-amber-800/80`.
  * Đã đánh giá xong câu (Evaluated): `bg-emerald-950/80 text-emerald-400 border-emerald-800/80`.
  * Lỗi microphone / Từ chối quyền: `bg-rose-50 text-rose-700 border-rose-200`.
* **Phát âm từ vựng (Word Accuracy Colors):**
  * Phát âm chuẩn xác / Tốt (EXCELLENT / GOOD): `bg-emerald-100 text-emerald-800 border-emerald-200`.
  * Ngữ điệu / Trọng âm cần chỉnh (NEEDS_WORK): `bg-amber-100 text-amber-800 border-amber-200`.
  * Phát âm sai / Nuốt âm (POOR): `bg-rose-100 text-rose-800 border-rose-200 underline decoration-rose-500 decoration-wavy`.
* **4 Tiêu chí đánh giá (Criteria Group Colors):**
  * Phát âm (Pronunciation): `bg-sky-50 text-sky-700 border-sky-200`.
  * Độ trôi chảy (Fluency): `bg-teal-50 text-teal-700 border-teal-200`.
  * Từ vựng (Vocabulary): `bg-indigo-50 text-indigo-700 border-indigo-200`.
  * Ngữ pháp (Grammar): `bg-violet-50 text-violet-700 border-violet-200`.
* **Đánh giá tổng quan (Score Ranges):**
  * Xuất sắc (>= 80 / Band >= 7.5): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Khá (60 - 79 / Band 6.0 - 7.0): `bg-sky-50 text-sky-700 border-sky-200`.
  * Cần cải thiện (< 60 / Band < 6.0): `bg-amber-50 text-amber-700 border-amber-200`.
* **Điểm mạnh & Cần cải thiện:**
  * Điểm làm tốt (Strengths): `bg-emerald-50 border-emerald-200 text-emerald-800`.
  * Cần cải thiện (Improvements): `bg-amber-50 border-amber-200 text-amber-800`.
* **Proficiency Levels:**
  * Beginner (A1 - A2): `bg-emerald-50 text-emerald-700 border-emerald-200`.
  * Intermediate (B1 - B2): `bg-sky-50 text-sky-700 border-sky-200`.
  * Advanced (C1 - C2 / IELTS / TOEIC): `bg-purple-50 text-purple-700 border-purple-200`.
* **Disabled State:** `bg-slate-200`, `text-slate-400`, `border-slate-200`, `cursor-not-allowed`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG dùng mã màu HEX hoặc RGB tự chế trong JSX, chỉ dùng Tailwind classes chuẩn hóa.
  * TUYỆT ĐỐI KHÔNG dùng gradient chói lóa hay hiệu ứng kính mờ (glassmorphism) phức tạp gây phân tán tập trung khi đọc câu và thu âm.
  * TUYỆT ĐỐI KHÔNG dùng màu neon chói mắt.

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockSpeakingCategories = [
  {
    id: "cat_ielts",
    type: "IELTS",
    title: "Luyện nói IELTS",
    description: "Luyện đọc thành tiếng các chủ đề học thuật, làm quen với cấu trúc câu phức và ngữ điệu chuẩn chuẩn bị cho Speaking Part 1 - 3.",
    totalLessons: 42,
    iconName: "GraduationCap"
  },
  {
    id: "cat_toeic",
    type: "TOEIC",
    title: "Luyện nói TOEIC",
    description: "Rèn luyện đọc to thông báo công ty, bản tin radio và đoạn văn quảng cáo trong môi trường văn phòng quốc tế (TOEIC Speaking Part 1 - 2).",
    totalLessons: 36,
    iconName: "Briefcase"
  },
  {
    id: "cat_foundation",
    type: "FOUNDATION",
    title: "Luyện nói Nền tảng",
    description: "Luyện phát âm chuẩn từng âm vị, nối âm, nuốt âm và nhịp điệu qua các câu hội thoại giao tiếp cơ bản hàng ngày cho người mới bắt đầu.",
    totalLessons: 28,
    iconName: "Sparkles"
  },
  {
    id: "cat_work",
    type: "WORK",
    title: "Tiếng Anh Công việc",
    description: "Thực hành phát âm tự tin các mẫu câu thuyết trình dự án, đàm phán hợp đồng, phỏng vấn xin việc và báo cáo tiến độ với đối tác.",
    totalLessons: 30,
    iconName: "Building2"
  }
];

const mockSpeakingResumeItem = {
  lessonId: "spk_work_01",
  category: "WORK",
  title: "Tự tin mở đầu buổi thuyết trình kế hoạch kinh doanh",
  topic: "PRESENTATION",
  level: "B2",
  completedSentences: 3,
  totalSentences: 6,
  lastAttemptAt: "09:30 - Hôm nay"
};

const mockTopicOptions = [
  { value: "ALL", label: "Tất cả chủ đề" },
  { value: "INTERVIEW", label: "Phỏng vấn xin việc" },
  { value: "PRESENTATION", label: "Thuyết trình & Báo cáo" },
  { value: "DAILY_COMMUNICATION", label: "Giao tiếp hàng ngày" },
  { value: "ACADEMIC", label: "Học thuật & Đời sống" },
  { value: "NEGOTIATION", label: "Đàm phán & Thương thảo" }
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
  { value: "5", label: "5 phút" },
  { value: "10", label: "10 phút" },
  { value: "15", label: "15 phút" },
  { value: "20", label: "20 phút" }
];

const mockStatusTabs = [
  { value: "ALL", label: "Tất cả" },
  { value: "NOT_STARTED", label: "Chưa làm" },
  { value: "IN_PROGRESS", label: "Đang làm" },
  { value: "COMPLETED", label: "Đã hoàn thành" }
];

const mockSpeakingLessonsList = [
  {
    id: "spk_work_01",
    category: "WORK",
    title: "Tự tin mở đầu buổi thuyết trình kế hoạch kinh doanh",
    topic: "PRESENTATION",
    level: "B2",
    durationMinutes: 10,
    totalSentences: 6,
    status: "IN_PROGRESS"
  },
  {
    id: "spk_ielts_01",
    category: "IELTS",
    title: "Thảo luận về tác động của năng lượng tái tạo",
    topic: "ACADEMIC",
    level: "B2",
    durationMinutes: 15,
    totalSentences: 8,
    status: "COMPLETED",
    bestScore: 82
  },
  {
    id: "spk_found_01",
    category: "FOUNDATION",
    title: "Gọi món và thanh toán tại nhà hàng quốc tế",
    topic: "DAILY_COMMUNICATION",
    level: "A2",
    durationMinutes: 5,
    totalSentences: 5,
    status: "NOT_STARTED"
  }
];

const mockActivePracticeSession = {
  lessonId: "spk_work_01",
  lessonTitle: "Tự tin mở đầu buổi thuyết trình kế hoạch kinh doanh",
  currentSentenceIndex: 2,
  totalSentences: 6,
  sentence: {
    id: "sent_03",
    orderIndex: 3,
    text: "Today, I am delighted to introduce our comprehensive strategic expansion plan for the next fiscal year.",
    phoneticHint: "comprehensive: /ˌkɒm.prɪˈhen.sɪv/ | strategic: /strəˈtiː.dʒɪk/ | fiscal: /ˈfɪs.kəl/",
    nativeAudioUrl: "https://assets.enlearning.com/audio/samples/work_01_sent_03.mp3"
  },
  recordingStatus: "EVALUATED",
  recordingSeconds: 8,
  hasRecordedAudio: true,
  isPlayingUserAudio: false,
  permissionStatus: "GRANTED"
};

const mockSentenceAiFeedback = {
  sentenceId: "sent_03",
  orderIndex: 3,
  overallScore: 84,
  pronunciationScore: 82,
  fluencyScore: 88,
  vocabularyScore: 85,
  grammarScore: 90,
  words: [
    { word: "Today,", accuracy: "EXCELLENT" },
    { word: "I", accuracy: "EXCELLENT" },
    { word: "am", accuracy: "EXCELLENT" },
    { word: "delighted", accuracy: "GOOD" },
    { word: "to", accuracy: "EXCELLENT" },
    { word: "introduce", accuracy: "GOOD" },
    { word: "our", accuracy: "EXCELLENT" },
    { word: "comprehensive", accuracy: "NEEDS_WORK", feedbackNote: "Nhấn mạnh âm tiết thứ 3: /ˌkɒm.prɪˈhen.sɪv/" },
    { word: "strategic", accuracy: "EXCELLENT" },
    { word: "expansion", accuracy: "GOOD" },
    { word: "plan", accuracy: "EXCELLENT" },
    { word: "for", accuracy: "EXCELLENT" },
    { word: "the", accuracy: "EXCELLENT" },
    { word: "next", accuracy: "POOR", feedbackNote: "Nuốt âm đuôi /t/ ở từ 'next'" },
    { word: "fiscal", accuracy: "GOOD" },
    { word: "year.", accuracy: "EXCELLENT" }
  ],
  conciseTip: "Ngữ điệu câu mở đầu rất tự tin và trôi chảy. Bạn hãy chú ý phát âm rõ âm đuôi /t/ ở từ 'next' và nhấn đúng trọng âm của 'comprehensive'.",
  userAudioUrl: "blob:http://localhost:3000/user-record-temp-03"
};

const mockSpeakingResult = {
  attemptId: "att_spk_9901",
  lessonId: "spk_work_01",
  lessonTitle: "Tự tin mở đầu buổi thuyết trình kế hoạch kinh doanh",
  category: "WORK",
  completedAt: "10:15 - 13/09/2026",
  timeSpentSeconds: 420,
  overallScore: 82,
  overallBandScore: 7.0,
  aiGeneralComment: "Bạn đã thể hiện rất tốt với phong thái tự tin và tốc độ nói tự nhiên! Hầu hết các câu được thể hiện liền mạch, chỉ cần lưu ý hoàn thiện một số phụ âm cuối để bài nói trở nên hoàn hảo.",
  pronunciation: {
    score: 80,
    bandScore: 7.0,
    comment: "Phát âm các nguyên âm rõ ràng. Cần chú ý bật dứt khoát các phụ âm cuối /t/, /s/, /d/ ở các từ kết thúc bằng cụm phụ âm."
  },
  fluency: {
    score: 85,
    bandScore: 7.5,
    comment: "Tốc độ nói trung bình 135 WPM rất phù hợp cho bài thuyết trình. Nhịp thở đều, ít khoảng dừng ngập ngừng."
  },
  vocabulary: {
    score: 82,
    bandScore: 7.0,
    comment: "Phát âm chuẩn xác các thuật ngữ kinh doanh trọng điểm như 'strategic', 'expansion', 'fiscal'."
  },
  grammar: {
    score: 88,
    bandScore: 7.5,
    comment: "Cấu trúc ngữ điệu tuân thủ đúng ngắt nghỉ theo từng cụm ngữ pháp có nghĩa."
  },
  strengths: [
    "Tốc độ đọc đều đặn, ngữ điệu tự nhiên, giữ được sự tự tin của người thuyết trình.",
    "Phát âm chính xác các nguyên âm dài và nguyên âm đôi trong các thuật ngữ chuyên môn.",
    "Ngắt nghỉ đúng vị trí dấu câu và các mệnh đề phụ trạng ngữ."
  ],
  improvements: [
    "Chú ý không nuốt phụ âm cuối /t/ và /s/ trong các từ như 'next', 'delighted', 'fiscal'.",
    "Từ 'comprehensive' cần nhấn rõ trọng âm chính vào âm tiết thứ ba /hen/.",
    "Cần duy trì độ vang của âm tiết cuối câu để tránh bị hụt hơi ở những câu dài."
  ],
  sentenceReviews: [
    {
      sentenceId: "sent_01",
      orderIndex: 1,
      originalText: "Good morning, ladies and gentlemen, and welcome to our annual meeting.",
      score: 88,
      userAudioUrl: "https://assets.enlearning.com/audio/users/att_9901_sent_01.mp3",
      nativeAudioUrl: "https://assets.enlearning.com/audio/samples/work_01_sent_01.mp3",
      evaluatedWords: [
        { word: "Good", accuracy: "EXCELLENT" },
        { word: "morning,", accuracy: "EXCELLENT" },
        { word: "ladies", accuracy: "EXCELLENT" },
        { word: "and", accuracy: "EXCELLENT" },
        { word: "gentlemen,", accuracy: "GOOD" },
        { word: "and", accuracy: "EXCELLENT" },
        { word: "welcome", accuracy: "EXCELLENT" },
        { word: "to", accuracy: "EXCELLENT" },
        { word: "our", accuracy: "EXCELLENT" },
        { word: "annual", accuracy: "GOOD" },
        { word: "meeting.", accuracy: "EXCELLENT" }
      ],
      mispronouncedWords: [],
      improvementTip: "Câu chào rất ấm áp và chuẩn ngữ điệu người bản xứ!"
    },
    {
      sentenceId: "sent_02",
      orderIndex: 2,
      originalText: "It is an honor to have you all here with us today.",
      score: 85,
      userAudioUrl: "https://assets.enlearning.com/audio/users/att_9901_sent_02.mp3",
      nativeAudioUrl: "https://assets.enlearning.com/audio/samples/work_01_sent_02.mp3",
      evaluatedWords: [
        { word: "It", accuracy: "EXCELLENT" },
        { word: "is", accuracy: "GOOD" },
        { word: "an", accuracy: "EXCELLENT" },
        { word: "honor", accuracy: "EXCELLENT" },
        { word: "to", accuracy: "EXCELLENT" },
        { word: "have", accuracy: "GOOD" },
        { word: "you", accuracy: "EXCELLENT" },
        { word: "all", accuracy: "EXCELLENT" },
        { word: "here", accuracy: "EXCELLENT" },
        { word: "with", accuracy: "NEEDS_WORK", feedbackNote: "Âm /ð/ ở từ 'with' chưa rõ" },
        { word: "us", accuracy: "EXCELLENT" },
        { word: "today.", accuracy: "EXCELLENT" }
      ],
      mispronouncedWords: [
        {
          word: "with",
          correctIpa: "/wɪð/",
          userMispronunciation: "/wɪs/",
          tip: "Đặt đầu lưỡi giữa hai hàm răng để tạo âm /ð/ nhẹ nhàng thay vì phát âm thành /s/."
        }
      ],
      improvementTip: "Luyện tập thêm âm thè lưỡi /ð/ ở từ 'with' khi nối với từ 'us'."
    },
    {
      sentenceId: "sent_03",
      orderIndex: 3,
      originalText: "Today, I am delighted to introduce our comprehensive strategic expansion plan for the next fiscal year.",
      score: 78,
      userAudioUrl: "https://assets.enlearning.com/audio/users/att_9901_sent_03.mp3",
      nativeAudioUrl: "https://assets.enlearning.com/audio/samples/work_01_sent_03.mp3",
      evaluatedWords: [
        { word: "Today,", accuracy: "EXCELLENT" },
        { word: "I", accuracy: "EXCELLENT" },
        { word: "am", accuracy: "EXCELLENT" },
        { word: "delighted", accuracy: "GOOD" },
        { word: "to", accuracy: "EXCELLENT" },
        { word: "introduce", accuracy: "GOOD" },
        { word: "our", accuracy: "EXCELLENT" },
        { word: "comprehensive", accuracy: "NEEDS_WORK" },
        { word: "strategic", accuracy: "EXCELLENT" },
        { word: "expansion", accuracy: "GOOD" },
        { word: "plan", accuracy: "EXCELLENT" },
        { word: "for", accuracy: "EXCELLENT" },
        { word: "the", accuracy: "EXCELLENT" },
        { word: "next", accuracy: "POOR" },
        { word: "fiscal", accuracy: "GOOD" },
        { word: "year.", accuracy: "EXCELLENT" }
      ],
      mispronouncedWords: [
        {
          word: "comprehensive",
          correctIpa: "/ˌkɒm.prɪˈhen.sɪv/",
          tip: "Trọng âm chính rơi vào âm tiết thứ 3 'hen'."
        },
        {
          word: "next",
          correctIpa: "/nekst/",
          tip: "Cần bật rõ phụ âm kép /kst/ ở đuôi từ."
        }
      ],
      improvementTip: "Tập trung bật dứt khoát âm đuôi /t/ ở 'next' và hạ giọng nhẹ ở cuối câu."
    }
  ],
  nextLessonId: "spk_work_02"
};

const mockMicPermissionPrompt = {
  title: "Cấp quyền sử dụng Microphone",
  description: "Để bắt đầu luyện nói và nhận góp ý trực tiếp từ AI, vui lòng cấp quyền cho trình duyệt sử dụng micro trên thiết bị của bạn.",
  actionLabel: "Cho phép Micro"
};

const mockMicPermissionDenied = {
  title: "Microphone chưa được cấp quyền",
  description: "Trình duyệt đang chặn quyền truy cập microphone. Vui lòng bấm vào biểu tượng ổ khóa hoặc cài đặt trên thanh địa chỉ URL, chọn 'Cho phép Microphone' và nhấn 'Thử lại'.",
  retryLabel: "Đã bật quyền, thử lại ngay"
};

const mockAudioSilenceWarning = {
  title: "Không nhận được âm thanh",
  description: "Hệ thống không thu được tín hiệu giọng nói của bạn. Vui lòng kiểm tra lại dây cắm tai nghe/micro hoặc nói to hơn một chút nhé!",
  actionLabel: "Thử thu âm lại"
};

const mockConfirmExitStudio = {
  title: "Thoát khỏi phòng luyện nói?",
  description: "Tiến trình câu đang làm dở sẽ không được lưu vào báo cáo tổng quan. Bạn có chắc chắn muốn rời phòng luyện nói lúc này không?",
  cancelLabel: "Ở lại tiếp tục",
  confirmLabel: "Rời khỏi phòng"
};
```
