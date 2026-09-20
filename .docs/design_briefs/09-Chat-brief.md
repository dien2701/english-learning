# 09-Chat-brief

### 1. HỆ THỐNG LƯỚI & BỐ CỤC (LAYOUT SYSTEM)

* **Root Floating FAB Trigger (Vị trí nút nổi mở chat):** `fixed bottom-6 right-6 z-50 flex items-center justify-center`.
* **Chat Window Floating Box (Desktop - Trạng thái nổi mặc định):** `fixed bottom-6 right-6 z-50 w-[420px] h-[640px] max-h-[calc(100vh-2rem)] bg-white border border-slate-200 rounded-2xl shadow-2xl flex flex-col overflow-hidden transition-all duration-300 ease-out`.
* **Chat Window Expanded Box (Desktop - Chế độ phóng to):** `fixed bottom-6 right-6 z-50 w-[720px] h-[800px] max-h-[calc(100vh-2rem)] bg-white border border-slate-200 rounded-2xl shadow-2xl flex flex-col overflow-hidden transition-all duration-300 ease-out`.
* **Chat Window Minimized Bar (Desktop - Trạng thái thu nhỏ góc màn hình):** `fixed bottom-6 right-6 z-50 h-14 px-4 bg-sky-600 text-white rounded-full shadow-xl flex items-center gap-3 cursor-pointer hover:bg-sky-700 active:bg-sky-800 transition-all`.
* **Chat Window Mobile Sheet (Mobile - < 768px):** `fixed inset-0 z-50 bg-white flex flex-col w-full h-full sm:rounded-none overflow-hidden`.
* **Header Bar Layout:** `w-full h-16 bg-white border-b border-slate-200 px-4 flex items-center justify-between flex-shrink-0 select-none`.
* **Network Alert Banner:** `w-full bg-amber-500 text-white px-4 py-2 text-xs font-semibold flex items-center justify-between gap-2 flex-shrink-0 animate-fadeIn`.
* **Body Scroll Area (Khu vực nội dung hội thoại & chào mừng):** `flex-1 w-full overflow-y-auto overflow-x-hidden p-4 flex flex-col gap-4 bg-slate-50/50`.
* **Màn 1 - Welcome Screen Container:** `flex flex-col gap-5 w-full py-2`.
  * **Welcome Greeting Box:** `w-full bg-sky-50/70 border border-sky-100 rounded-2xl p-4 flex flex-col gap-2`.
  * **Personalized Hint Card Box:** `w-full bg-white border border-sky-200 rounded-xl p-4 shadow-xs flex flex-col gap-3 relative overflow-hidden`.
  * **Quick Prompt Section Box:** `w-full flex flex-col gap-3`.
  * **Category Chips Scroll Container:** `flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar w-full`.
  * **Quick Prompt Grid / Vertical List:** `flex flex-col gap-2.5 w-full`.
* **Màn 2 - Conversation View Container:** `flex flex-col gap-4 w-full flex-1 relative`.
  * **Message List Scroll Area:** `flex flex-col gap-4 w-full flex-1`.
  * **Message Row Layout (User):** `w-full flex justify-end items-end gap-2 pl-10`.
  * **Message Row Layout (AI):** `w-full flex justify-start items-start gap-2.5 pr-6`.
  * **Recommendation Cards Container:** `w-full flex flex-col gap-2.5 mt-3 pt-3 border-t border-slate-200/80`.
  * **Scroll To Bottom Floating Container:** `sticky bottom-3 self-center z-20 flex justify-center`.
* **Footer Input Bar Layout:** `w-full bg-white border-t border-slate-200 p-3 flex flex-col gap-2 flex-shrink-0`.
  * **Text Area Input Wrapper:** `w-full relative flex items-center bg-slate-50 border border-slate-200 rounded-2xl focus-within:border-sky-600 focus-within:bg-white focus-within:ring-2 focus-within:ring-sky-600/15 transition-all p-2.5`.
  * **Input Action Bar Container:** `flex items-center justify-between gap-2 px-1 pt-1`.
* **Modal Overlay:** `fixed inset-0 z-60 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 animate-fadeIn`.
* **Modal Box Standard:** `w-full max-w-sm bg-white border border-slate-200 rounded-2xl shadow-xl p-5 flex flex-col gap-4`.
* **Responsive Quy định:**
  * **Mobile (< 768px):** Cửa sổ chat chiếm trọn màn hình `fixed inset-0 w-full h-full rounded-none border-none`; Ẩn nút "Phóng to / Thu gọn kích thước" trên Header; Ô nhập văn bản ghim sát cạnh đáy màn hình an toàn (`pb-safe`); Danh mục Quick Prompt hiển thị thanh cuộn ngang `overflow-x-auto`; Lời nhắc cá nhân hóa hiển thị dạng 1 cột thu gọn.
  * **Tablet (768px - 1023px):** Cửa sổ chat hiển thị dạng Floating Widget góc dưới phải kích thước `w-[400px] h-[580px] rounded-2xl`; Hỗ trợ thu nhỏ `MINIMIZED` thành nút viên thuốc.
  * **Desktop (>= 1024px):** Kích thước chuẩn `w-[420px] h-[640px]`; Khi bật chế độ Phóng to `EXPANDED`, kích thước mở rộng lên `w-[720px] h-[800px]` hiển thị định dạng bảng so sánh Markdown và các thẻ đề xuất bài học thành lưới 2 cột `grid grid-cols-2 gap-3`.

---

### 2. ĐẶC TẢ COMPONENT (COMPONENT SPECS)

#### A. Khung giao diện nổi & Vỏ bọc cửa sổ (Floating Shell & Window)

* **AiChatFloatingButton [DUMB]**:
  * Box Style: `group relative h-14 w-14 rounded-full bg-sky-600 text-white shadow-xl hover:shadow-sky-600/30 flex items-center justify-center border border-white/20 transition-all duration-300 hover:scale-105 active:scale-95 cursor-pointer`.
  * Icon Container: `w-6 h-6 flex items-center justify-center text-white`.
  * Unread Badge: `absolute -top-1 -right-1 min-w-[20px] h-5 px-1.5 rounded-full bg-rose-500 text-white text-[11px] font-bold flex items-center justify-center border-2 border-white shadow-xs animate-bounce`.
  * Responding Pulse Ring: `absolute inset-0 rounded-full border-2 border-sky-400 animate-ping pointer-events-none`.

* **AiChatWindowLayout [DUMB]**:
  * Box Style: `relative w-full h-full bg-white flex flex-col overflow-hidden text-slate-900`.
  * Header Slot: `w-full flex-shrink-0`.
  * Network Alert Slot: `w-full flex-shrink-0`.
  * Body Slot: `flex-1 w-full overflow-hidden flex flex-col`.
  * Input Bar Slot: `w-full flex-shrink-0`.

* **AiChatHeader [DUMB]**:
  * Box Style: `w-full h-16 bg-white border-b border-slate-200 px-4 flex items-center justify-between flex-shrink-0 shadow-xs`.
  * Left Identity Stack: `flex items-center gap-3 min-w-0 flex-1`.
  * Right Action Controls: `flex items-center gap-1 flex-shrink-0`.

* **AiAvatarStatus [DUMB]**:
  * Box Style: `relative flex-shrink-0`.
  * Avatar Circle (sm): `w-8 h-8 rounded-full bg-gradient-to-tr from-sky-600 to-sky-400 p-0.5 border border-sky-200 shadow-2xs flex items-center justify-center text-white`.
  * Avatar Circle (md): `w-10 h-10 rounded-full bg-gradient-to-tr from-sky-600 to-sky-400 p-0.5 border border-sky-200 shadow-2xs flex items-center justify-center text-white`.
  * Avatar Circle (lg): `w-12 h-12 rounded-full bg-gradient-to-tr from-sky-600 to-sky-400 p-0.5 border border-sky-200 shadow-xs flex items-center justify-center text-white`.
  * Avatar Inner Icon: `w-5 h-5 text-white`.
  * Online Dot: `absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-emerald-500 border-2 border-white shadow-2xs`.
  * Offline Dot: `absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-slate-400 border-2 border-white`.

* **AiHeaderInfo [DUMB]**:
  * Box Style: `flex flex-col min-w-0 flex-1`.
  * Typography Title: `text-sm font-bold text-slate-900 truncate tracking-tight flex items-center gap-1.5`.
  * Official Badge: `px-1.5 py-0.2 rounded bg-sky-100 text-sky-700 text-[10px] font-semibold tracking-normal`.
  * Typography Subtitle: `text-xs text-slate-500 font-medium truncate flex items-center gap-1`.

* **AiChatHeaderActions [DUMB]**:
  * Box Style: `flex items-center gap-0.5 text-slate-500`.
  * Header Action Button: `w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-100 active:bg-slate-200 transition-colors cursor-pointer`.
  * Close Button: `w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-rose-600 hover:bg-rose-50 active:bg-rose-100 transition-colors cursor-pointer`.

* **AiChatBody [DUMB]**:
  * Box Style: `w-full flex-1 overflow-y-auto p-4 bg-slate-50/50 flex flex-col gap-4 scroll-smooth`.

---

#### B. Màn hình 1: Trạng thái chào mừng (AiWelcomeScreen)

* **AiWelcomeScreen [DUMB]**:
  * Box Style: `w-full flex flex-col gap-5 py-2 animate-fadeIn`.

* **AiWelcomeGreeting [DUMB]**:
  * Box Style: `w-full bg-gradient-to-br from-sky-50/90 to-sky-100/40 border border-sky-100 rounded-2xl p-4.5 flex items-start gap-3.5 shadow-2xs`.
  * Icon Container: `w-10 h-10 rounded-xl bg-sky-600 text-white flex items-center justify-center text-lg flex-shrink-0 shadow-xs`.
  * Content Stack: `flex flex-col gap-1 flex-1 min-w-0`.
  * Typography Title: `text-base font-bold text-slate-900 tracking-tight`.
  * Typography Subtitle: `text-xs text-slate-600 font-normal leading-relaxed`.

* **PersonalizedStudyHintCard [DUMB]**:
  * Box Style: `w-full bg-white border border-sky-200 rounded-xl p-4 shadow-xs flex flex-col gap-3 relative overflow-hidden group hover:border-sky-300 transition-all`.
  * Top Badge Row: `flex items-center justify-between gap-2`.
  * Pill Badge: `inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-sky-100 text-sky-700 text-[11px] font-bold`.
  * Content Text: `text-xs text-slate-700 font-normal leading-relaxed`.
  * Highlighting Text: `font-semibold text-slate-900`.
  * Action Prompt Button: `inline-flex items-center justify-between w-full h-8 px-3 rounded-lg bg-sky-50 text-sky-700 hover:bg-sky-100 active:bg-sky-200 text-xs font-semibold transition-colors cursor-pointer`.
  * Arrow Icon: `w-3.5 h-3.5 text-sky-600 group-hover:translate-x-0.5 transition-transform`.

* **QuickPromptSection [DUMB]**:
  * Box Style: `w-full flex flex-col gap-3`.
  * Section Title: `text-xs font-bold uppercase tracking-wider text-slate-400 px-1`.

* **QuickPromptCategoryChips [DUMB]**:
  * Box Style: `flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar w-full`.
  * Chip Default: `h-7 px-3 rounded-full text-xs font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 hover:text-slate-900 transition-colors whitespace-nowrap cursor-pointer flex-shrink-0`.
  * Chip Active: `h-7 px-3 rounded-full text-xs font-bold text-sky-700 bg-sky-100 border border-sky-200 shadow-2xs whitespace-nowrap cursor-pointer flex-shrink-0`.

* **QuickPromptList [DUMB]**:
  * Box Style: `flex flex-col gap-2 w-full`.

* **QuickPromptButton [DUMB]**:
  * Box Style: `group w-full p-3 rounded-xl bg-white border border-slate-200 shadow-2xs hover:border-sky-300 hover:bg-sky-50/40 hover:shadow-xs transition-all duration-200 flex items-center justify-between gap-3 text-left cursor-pointer`.
  * Left Stack: `flex items-center gap-2.5 min-w-0 flex-1`.
  * Icon Container: `w-7 h-7 rounded-lg bg-slate-100 text-slate-600 group-hover:bg-sky-100 group-hover:text-sky-600 flex items-center justify-center flex-shrink-0 transition-colors`.
  * Typography Label: `text-xs font-medium text-slate-700 group-hover:text-slate-900 leading-snug truncate`.
  * Chevron Icon: `w-4 h-4 text-slate-400 group-hover:text-sky-600 group-hover:translate-x-0.5 transition-all flex-shrink-0`.

---

#### C. Màn hình 2: Dòng thời gian hội thoại (AiConversationView)

* **AiConversationView [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full flex-1 relative`.

* **MessageListContainer [DUMB]**:
  * Box Style: `flex flex-col gap-4 w-full flex-1 min-h-0`.

* **ChatMessageItem [DUMB]**:
  * Box Style: `w-full flex flex-col gap-1`.

* **UserMessageBubble [DUMB]**:
  * Outer Wrapper: `flex justify-end items-end gap-2 pl-12`.
  * Bubble Box: `max-w-[85%] bg-sky-600 text-white rounded-2xl rounded-tr-xs px-4 py-2.5 shadow-xs flex flex-col gap-1 relative text-sm leading-relaxed break-words`.
  * Typography Text: `text-sm text-white font-normal`.
  * Meta Row: `flex items-center justify-end gap-1.5 text-[10px] text-sky-200 select-none`.
  * Status Icon: `w-3 h-3 text-sky-200`.

* **AiMessageBubble [DUMB]**:
  * Outer Wrapper: `flex items-start gap-2.5 pr-6`.
  * Avatar Wrapper: `flex-shrink-0 mt-0.5`.
  * Bubble Box: `max-w-[90%] bg-white text-slate-900 border border-slate-200/80 rounded-2xl rounded-tl-xs p-4 shadow-xs flex flex-col gap-3 text-sm leading-relaxed break-words`.

* **MarkdownMessageContent [DUMB]**:
  * Box Style: `text-sm text-slate-800 leading-relaxed space-y-2.5 font-normal`.
  * Heading 2: `text-base font-bold text-slate-900 mt-2 mb-1`.
  * Heading 3: `text-sm font-bold text-slate-900 mt-1.5 mb-1`.
  * Paragraph: `text-sm text-slate-800 leading-relaxed`.
  * Bold Text: `font-bold text-slate-900`.
  * List Unordered: `list-disc list-inside space-y-1 text-slate-700 pl-1`.
  * List Ordered: `list-decimal list-inside space-y-1 text-slate-700 pl-1`.
  * Example Sentence Block: `p-2.5 rounded-lg bg-sky-50/70 border-l-4 border-sky-500 text-slate-800 text-xs italic leading-relaxed my-2`.
  * Inline Code: `px-1.5 py-0.5 rounded bg-slate-100 text-slate-800 font-mono text-xs font-semibold`.
  * Code Block: `p-3 rounded-xl bg-slate-900 text-slate-100 font-mono text-xs overflow-x-auto my-2`.
  * Grammar Table: `w-full border-collapse border border-slate-200 text-xs my-2 rounded-lg overflow-hidden`.
  * Table Header: `bg-slate-100 text-slate-700 font-bold p-2 border border-slate-200 text-left`.
  * Table Cell: `p-2 border border-slate-200 text-slate-800`.

* **AiTypingIndicator [DUMB]**:
  * Box Style: `flex items-center gap-2.5 p-3 rounded-2xl rounded-tl-xs bg-white border border-slate-200/80 shadow-2xs w-fit max-w-[240px]`.
  * Dots Wrapper: `flex items-center gap-1.5`.
  * Dot Item: `w-2 h-2 rounded-full bg-sky-500 animate-bounce`.
  * Typography Text: `text-xs text-slate-500 font-medium tracking-tight`.

* **AiRecommendationCardList [DUMB]**:
  * Box Style: `w-full flex flex-col gap-2.5 mt-2 pt-3 border-t border-slate-100`.
  * Title Row: `flex items-center gap-1.5 text-xs font-bold text-slate-600 uppercase tracking-wider`.

* **AiRecommendationCard [DUMB]**:
  * Box Style: `group w-full bg-slate-50/80 hover:bg-sky-50/50 border border-slate-200 hover:border-sky-300 rounded-xl p-3 shadow-2xs hover:shadow-xs transition-all duration-200 flex flex-col gap-2 cursor-pointer`.
  * Top Meta Row: `flex items-center justify-between gap-2`.
  * Typography Title: `text-xs font-bold text-slate-900 group-hover:text-sky-700 transition-colors leading-snug`.
  * Typography Reason: `text-[11px] text-slate-500 leading-normal line-clamp-2`.
  * Bottom Action Row: `flex items-center justify-between gap-2 pt-1 border-t border-slate-100/80`.
  * Action Link Text: `text-xs font-semibold text-sky-600 group-hover:text-sky-700 flex items-center gap-1`.
  * Chevron Icon: `w-3.5 h-3.5 text-sky-600 group-hover:translate-x-0.5 transition-transform`.

* **SkillCategoryBadge [DUMB]**:
  * Badge Flashcard: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-sky-100 text-sky-700 text-[10px] font-bold border border-sky-200/60`.
  * Badge Writing: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-purple-100 text-purple-700 text-[10px] font-bold border border-purple-200/60`.
  * Badge Listening: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-indigo-100 text-indigo-700 text-[10px] font-bold border border-indigo-200/60`.
  * Badge Reading: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-blue-100 text-blue-700 text-[10px] font-bold border border-blue-200/60`.
  * Badge Exam: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-amber-100 text-amber-700 text-[10px] font-bold border border-amber-200/60`.
  * Badge Grammar: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700 text-[10px] font-bold border border-emerald-200/60`.
  * Badge Vocabulary: `inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-teal-100 text-teal-700 text-[10px] font-bold border border-teal-200/60`.

* **MessageErrorNotice [DUMB]**:
  * Box Style: `w-full p-3 rounded-xl bg-rose-50 border border-rose-200 flex items-center justify-between gap-3 text-xs text-rose-700 shadow-2xs`.
  * Text Stack: `flex items-center gap-2 min-w-0 flex-1`.
  * Icon Container: `w-4 h-4 text-rose-500 flex-shrink-0`.
  * Message Text: `truncate font-medium`.
  * Retry Button: `inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-rose-600 hover:bg-rose-700 active:bg-rose-800 text-white font-semibold text-xs shadow-2xs transition-colors cursor-pointer flex-shrink-0`.

* **AiFallbackNotice [DUMB]**:
  * Box Style: `w-full p-3.5 rounded-xl bg-amber-50 border border-amber-200 flex flex-col gap-2.5 text-xs text-amber-800 shadow-2xs`.
  * Header Row: `flex items-start gap-2`.
  * Icon Container: `w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5`.
  * Content Text: `leading-relaxed text-slate-700`.
  * Suggestion List: `flex flex-col gap-1.5 pt-1`.
  * Suggestion Chip: `text-left px-2.5 py-1.5 rounded-lg bg-white border border-amber-200 text-slate-800 hover:border-amber-300 hover:bg-amber-100/50 transition-colors font-medium text-xs cursor-pointer`.

* **ScrollToBottomButton [DUMB]**:
  * Box Style: `inline-flex items-center gap-1.5 h-8 px-3 rounded-full bg-white text-slate-700 border border-slate-200 shadow-md hover:bg-slate-50 hover:text-sky-600 hover:border-sky-300 transition-all text-xs font-semibold cursor-pointer animate-fadeIn`.
  * Icon Container: `w-3.5 h-3.5 text-slate-500`.

---

#### D. Khung nhập liệu tin nhắn (AiChatInputBar)

* **AiChatInputBar [DUMB]**:
  * Box Style: `w-full bg-white border-t border-slate-200 p-3 flex flex-col gap-2 flex-shrink-0 select-none`.

* **ChatTextAreaInput [DUMB]**:
  * Box Style: `w-full bg-transparent text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none resize-none min-h-[44px] max-h-32 leading-relaxed`.
  * Disabled Style: `opacity-60 cursor-not-allowed`.

* **ChatInputActionBar [DUMB]**:
  * Box Style: `flex items-center justify-between gap-2 pt-1 w-full`.
  * Left Group: `flex items-center gap-1 text-slate-400`.
  * Right Group: `flex items-center gap-2`.

* **InputWordCountBadge [DUMB]**:
  * Box Style: `text-[11px] font-mono text-slate-400 select-none`.
  * Warning Overlimit: `text-rose-500 font-bold`.

* **VoiceInputButton [DUMB]**:
  * Idle Style: `w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100 active:bg-slate-200 transition-colors cursor-pointer`.
  * Listening Style: `w-8 h-8 rounded-lg flex items-center justify-center bg-rose-100 text-rose-600 animate-pulse ring-2 ring-rose-400/30 transition-all cursor-pointer`.

* **ClearInputButton [DUMB]**:
  * Box Style: `w-7 h-7 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors cursor-pointer`.

* **SendMessageButton [DUMB]**:
  * Box Style: `inline-flex items-center justify-center w-9 h-9 rounded-xl bg-sky-600 text-white shadow-xs hover:bg-sky-700 active:bg-sky-800 disabled:bg-slate-200 disabled:text-slate-400 disabled:cursor-not-allowed transition-all duration-200 cursor-pointer`.
  * Send Icon: `w-4 h-4 text-inherit`.

---

#### E. Hộp thoại & Cảnh báo (Modals & Alerts)

* **ConfirmResetChatModal [DUMB]**:
  * Overlay: `fixed inset-0 z-60 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 animate-fadeIn`.
  * Modal Card: `w-full max-w-sm bg-white border border-slate-200 rounded-2xl shadow-xl p-5 flex flex-col gap-4`.
  * Icon Warning: `w-11 h-11 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center text-xl flex-shrink-0`.
  * Typography Title: `text-base font-bold text-slate-900 tracking-tight`.
  * Typography Description: `text-xs text-slate-500 leading-relaxed`.
  * Action Row: `flex items-center justify-end gap-2.5 pt-2`.
  * Cancel Button: `h-9 px-4 rounded-xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 text-xs font-semibold transition-colors cursor-pointer`.
  * Confirm Button: `h-9 px-4 rounded-xl bg-rose-600 hover:bg-rose-700 active:bg-rose-800 text-white text-xs font-semibold shadow-xs transition-colors cursor-pointer`.

* **AiChatNetworkAlert [DUMB]**:
  * Box Style: `w-full bg-amber-500 text-white px-3.5 py-2 text-xs font-semibold flex items-center justify-between gap-2 flex-shrink-0 animate-fadeIn select-none`.
  * Text Stack: `flex items-center gap-1.5`.
  * Retry Button: `underline hover:text-amber-100 cursor-pointer font-bold`.

---

### 3. RÀNG BUỘC MÀU SẮC (COLOR CONSTRAINTS)

* **Primary Color (Chuẩn En-Learning #008FD5):** `bg-sky-600`, `text-sky-600`, `border-sky-600`.
* **Primary Hover:** `bg-sky-700`, `text-sky-700`.
* **Primary Active:** `bg-sky-800`.
* **Primary Light / Nền mục chọn:** `bg-sky-50`, `text-sky-700`, `border-sky-100`.
* **Page / Window Body Background:** `bg-slate-50`.
* **Card / Window Chrome Background:** `bg-white`.
* **Border Standard:** `border-slate-200`.
* **Border Subtle / Divider:** `border-slate-100`.
* **Border Focus / Highlight:** `border-sky-600`, `ring-sky-600/15`.
* **Text Primary:** `text-slate-900`.
* **Text Secondary:** `text-slate-600`, `text-slate-500`.
* **Text Muted / Placeholder:** `text-slate-400`.
* **Bong bóng tin nhắn Người dùng (User Message Bubble):** `bg-sky-600 text-white`, timestamp: `text-sky-200`.
* **Bong bóng tin nhắn Trợ lý AI (AI Message Bubble):** `bg-white text-slate-900 border-slate-200/80`, timestamp: `text-slate-400`.
* **Khối ví dụ câu văn tiếng Anh (Example sentence block):** `bg-sky-50/70 border-l-4 border-sky-500 text-slate-800`.
* **Chỉ báo AI đang phản hồi (Typing Indicator):** Dots: `bg-sky-500`, Text: `text-slate-500`.
* **Huy hiệu Kỹ năng (Skill Badges trên Thẻ đề xuất):**
  * Flashcard (Từ vựng): `bg-sky-100 text-sky-700 border-sky-200/60`.
  * Luyện viết (Writing): `bg-purple-100 text-purple-700 border-purple-200/60`.
  * Luyện nghe (Listening): `bg-indigo-100 text-indigo-700 border-indigo-200/60`.
  * Đọc hiểu (Reading): `bg-blue-100 text-blue-700 border-blue-200/60`.
  * Kiểm tra (Exam): `bg-amber-100 text-amber-700 border-amber-200/60`.
  * Ngữ pháp (Grammar): `bg-emerald-100 text-emerald-700 border-emerald-200/60`.
  * Từ vựng (Vocabulary): `bg-teal-100 text-teal-700 border-teal-200/60`.
* **Trạng thái Trực tuyến AI (Avatar Online Status):**
  * Đang trực tuyến (Online): `bg-emerald-500 border-white`.
  * Ngoại tuyến (Offline): `bg-slate-400 border-white`.
* **Trạng thái cảnh báo & Lỗi (Status & Alerts):**
  * Mất mạng (Network Disconnected): `bg-amber-500 text-white`.
  * Gửi tin nhắn thất bại (Message Error): `bg-rose-50 text-rose-700 border-rose-200`.
  * AI phản hồi dự phòng (Fallback Prompt): `bg-amber-50 text-amber-800 border-amber-200`.
* **Trạng thái nút nhập bằng giọng nói (Voice Input States):**
  * Đang nghe (Listening): `bg-rose-100 text-rose-600 ring-rose-400/30 animate-pulse`.
  * Chờ (Idle): `text-slate-400 hover:text-slate-700 hover:bg-slate-100`.
* **Quy tắc cấm kỵ:**
  * TUYỆT ĐỐI KHÔNG sử dụng mã màu HEX hoặc RGB tùy tiện trong JSX, chỉ sử dụng các class Tailwind CSS được chuẩn hóa từ Styleguide.
  * TUYỆT ĐỐI KHÔNG dùng hiệu ứng kính mờ sặc sỡ (glassmorphism), gradient chói mắt hay hoạt hình rườm rà làm phân tâm quá trình đọc bài giải thích ngữ pháp.
  * Không dùng tông màu tối (Dark mode) trong giai đoạn này vì toàn hệ thống tuân theo chuẩn sáng Light Theme (`bg-white`, `bg-slate-50`).

---

### 4. MOCK DATA (DỮ LIỆU HIỂN THỊ)

```javascript
const mockPersonalizedStudyHint = {
  userId: "user_dien2701",
  weakTopics: ["Thì Quá khứ hoàn thành (Past Perfect)", "Mệnh đề quan hệ rút gọn"],
  recentDeckToReview: {
    deckId: "deck_biz_01",
    deckTitle: "Tiếng Anh Giao tiếp Công sở (Business Communication)"
  },
  recommendedSkill: "FLASHCARD",
  summaryNote: "Dành riêng cho bạn: Bạn có 18 từ vựng cần ôn tập dở dang trong chủ đề 'Tiếng Anh Công sở' và làm sai 3 câu về thì Quá khứ hoàn thành ở bài tập gần nhất."
};

const mockQuickPromptCategories = [
  { key: "ALL", label: "Tất cả gợi ý" },
  { key: "GRAMMAR", label: "Ngữ pháp" },
  { key: "VOCABULARY", label: "Từ vựng" },
  { key: "RECOMMENDATION", label: "Gợi ý bài tập" },
  { key: "IMPROVEMENT", label: "Cải thiện kỹ năng" }
];

const mockQuickPrompts = [
  {
    id: "prompt_01",
    category: "GRAMMAR",
    label: "Giải thích thì Hiện tại hoàn thành và Quá khứ đơn",
    promptText: "Hãy phân biệt cách dùng thì Hiện tại hoàn thành (Present Perfect) và Quá khứ đơn (Past Simple) kèm ví dụ minh họa trực quan.",
    iconName: "BookOpen"
  },
  {
    id: "prompt_02",
    category: "VOCABULARY",
    label: "Hỏi nghĩa và cách dùng từ 'Intermittent'",
    promptText: "Giải thích nghĩa của từ 'Intermittent', từ đồng nghĩa, từ trái nghĩa và đặt 3 câu ví dụ trong ngữ cảnh công việc.",
    iconName: "HelpCircle"
  },
  {
    id: "prompt_03",
    category: "RECOMMENDATION",
    label: "Gợi ý bài luyện tập tiếp theo cho tôi",
    promptText: "Dựa vào kết quả học tập gần đây của tôi, hãy đề xuất bài học hoặc bài kiểm tra mà tôi nên làm ngay hôm nay.",
    iconName: "Sparkles"
  },
  {
    id: "prompt_04",
    category: "IMPROVEMENT",
    label: "Tôi nên làm gì để cải thiện kỹ năng Viết?",
    promptText: "Chỉ ra 3 điểm tôi cần chú ý nhất để tăng điểm từ vựng và sự liên kết (Coherence) trong phần thi Viết luận.",
    iconName: "PenTool"
  },
  {
    id: "prompt_05",
    category: "GRAMMAR",
    label: "Cách dùng mệnh đề quan hệ rút gọn",
    promptText: "Hướng dẫn các trường hợp rút gọn mệnh đề quan hệ bằng V-ing, V-ed/P2 và To-infinitive.",
    iconName: "FileText"
  }
];

const mockConversationMessages = [
  {
    id: "msg_01",
    conversationId: "conv_ai_chat_001",
    role: "USER",
    content: "Chào Trợ lý AI! Bạn có thể giải thích giúp mình khi nào dùng 'Although' và khi nào dùng 'In spite of' không?",
    createdAt: "10:30",
    status: "SENT"
  },
  {
    id: "msg_02",
    conversationId: "conv_ai_chat_001",
    role: "ASSISTANT",
    content: `Chào bạn! Rất vui được hỗ trợ bạn. Cả **Although** và **In spite of** đều mang nghĩa là *"Mặc dù / Dẫu cho"*, dùng để chỉ sự tương phản giữa hai mệnh đề. Tuy nhiên, cấu trúc ngữ pháp đi kèm của chúng hoàn toàn khác nhau:

### 1. Cấu trúc với Although
Sau **Although** bắt buộc phải là một **mệnh đề hoàn chỉnh** (chứa Chủ ngữ + Động từ).
> **Although + S + V, S + V**

*Ví dụ:*
- *Although it rained heavily, we went to the office on time.* (Mặc dù trời mưa to, chúng tôi vẫn đến văn phòng đúng giờ.)

### 2. Cấu trúc với In spite of
Sau **In spite of** luôn là một **Danh từ**, **Cụm danh từ** hoặc **Danh động từ (V-ing)**.
> **In spite of + Noun / V-ing, S + V**

*Ví dụ:*
- *In spite of the heavy rain, we went to the office on time.*
- *In spite of feeling exhausted, she finished the report.*

---
💡 **Mẹo ghi nhớ nhanh:** Cứ thấy có động từ chia thì đi liền phía sau thì chọn **Although**, nếu chỉ có danh từ hoặc động từ thêm *-ing* thì chọn **In spite of**.`,
    createdAt: "10:31",
    status: "SENT",
    recommendations: [
      {
        id: "rec_01",
        actionType: "WRITING_PRACTICE",
        title: "Luyện viết câu phức với Liên từ tương phản",
        reason: "Áp dụng ngay kiến thức vừa học qua 5 bài tập chuyển đổi giữa 'Although' và 'In spite of'.",
        targetUrl: "/writing/practice/conjunctions-contrast",
        skillBadge: "WRITING",
        actionLabel: "Luyện viết ngay"
      },
      {
        id: "rec_02",
        actionType: "FLASHCARD_DECK",
        title: "Bộ thẻ từ nối học thuật (Academic Connectors)",
        reason: "Củng cố thêm 20 từ nối chỉ nguyên nhân, kết quả và tương phản nâng cao.",
        targetUrl: "/flashcards/deck_connectors_01",
        skillBadge: "FLASHCARD",
        actionLabel: "Ôn từ vựng"
      }
    ]
  },
  {
    id: "msg_03",
    conversationId: "conv_ai_chat_001",
    role: "USER",
    content: "Vậy mình có thể dùng 'In spite of that' ở đầu câu được không?",
    createdAt: "10:33",
    status: "SENT"
  },
  {
    id: "msg_04",
    conversationId: "conv_ai_chat_001",
    role: "ASSISTANT",
    content: `**Hoàn toàn chính xác!** Bạn có thể dùng **"In spite of that,"** hoặc **"Despite that,"** đứng ở đầu câu làm trạng từ liên kết, theo sau là một dấu phẩy. Nó có nghĩa tương đương với *Nevertheless* hoặc *However* (Tuy nhiên, dẫu vậy).

*Ví dụ thực tế:*
- *The project faced numerous technical obstacles. In spite of that, the engineering team delivered on schedule.* (Dự án đối mặt với vô số khó khăn kỹ thuật. Dẫu vậy, đội ngũ kỹ sư vẫn bàn giao đúng tiến độ.)`,
    createdAt: "10:33",
    status: "SENT"
  }
];

const mockTypingState = {
  isResponding: true,
  indicatorText: "Trợ lý AI đang soạn câu trả lời..."
};

const mockNetworkErrorState = {
  isConnected: false,
  bannerMessage: "Mất kết nối với máy chủ AI. Đang tự động kết nối lại...",
  retryText: "Thử lại ngay"
};

const mockFallbackSuggestions = {
  message: "Xin lỗi, tôi chưa hiểu rõ yêu cầu này của bạn. Bạn có thể diễn đạt cụ thể hơn hoặc tham khảo các câu hỏi mẫu bên dưới:",
  suggestionPrompts: [
    "Giải thích cấu trúc câu điều kiện loại 2 và 3",
    "Gợi ý 5 từ đồng nghĩa của 'Significant'",
    "Đề xuất bài kiểm tra Reading cơ bản"
  ]
};
```
