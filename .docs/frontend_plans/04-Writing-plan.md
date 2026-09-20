# 04-Writing-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **WritingCategoryPage [SMART]**: Quản lý và điều phối dữ liệu trang tổng quan danh mục luyện viết (IELTS, TOEIC, Nền tảng, Công việc), theo dõi tiến độ tổng thể và điều hướng sang danh sách đề.
  * **WritingCategoryHeader [DUMB]**: Tiêu đề trang "Luyện viết tiếng Anh", mô tả tóm tắt mục tiêu và lợi ích luyện viết với AI.
  * **WritingCategoryList [DUMB]**: Danh sách hiển thị theo chiều dọc 4 danh mục chính.
    * **WritingCategoryCard [DUMB]**: Thẻ đại diện cho từng danh mục (icon đại diện, tên danh mục, mô tả mục tiêu, số lượng đề bài hiện có, huy hiệu trình độ tương ứng và CTA `Xem thêm` chuyển sang danh sách đề chi tiết).
  * **WritingCategorySkeleton [DUMB]**: *(Shared UI)* Hiệu ứng khung xương tải dữ liệu cho danh mục luyện viết.
  * **CategoryEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi chưa có danh mục khả dụng kèm nút tải lại.

* **WritingPromptListPage [SMART]**: Điều phối dữ liệu danh sách đề bài thuộc danh mục được chọn, xử lý logic tìm kiếm từ khóa, bộ lọc đa tiêu chí (chủ đề, trình độ, thời lượng, trạng thái làm bài) và phân trang.
  * **PromptListHeader [DUMB]**: Khối tiêu đề danh mục đang chọn, breadcrumb điều hướng và tổng số đề bài tìm thấy.
    * **Breadcrumb [DUMB]**: *(Shared UI)* Thanh dẫn đường phân cấp (`Trang chủ > Luyện viết > IELTS`).
  * **PromptFilterBar [DUMB]**: Thanh công cụ tìm kiếm và lọc đề bài.
    * **PromptSearchBar [DUMB]**: *(Shared UI)* Ô nhập từ khóa tìm kiếm đề bài hỗ trợ debounce và nút xóa nhanh từ khóa.
    * **FilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo chủ đề (Xã hội, Công nghệ, Môi trường...), trình độ (A2 - C2 / IELTS 5.0 - 8.0) và thời lượng (15, 30, 45, 60 phút).
    * **StatusFilterTabs [DUMB]**: *(Shared UI)* Thanh tab lọc nhanh trạng thái bài (`Tất cả`, `Chưa làm`, `Đã làm`).
  * **PromptGrid [DUMB]**: Lưới hiển thị danh sách các đề bài theo định dạng responsive (1 cột mobile, 2-3 cột desktop).
    * **PromptCard [DUMB]**: Thẻ đề bài hiển thị thông tin tóm tắt (tiêu đề đề bài, trích đoạn mô tả ngắn, tag chủ đề, badge trình độ, thời lượng ước tính, số từ tối thiểu, huy hiệu `Đã hoàn thành` kèm điểm số cao nhất nếu có, và nút CTA `Bắt đầu làm`).
    * **LevelBadge [DUMB]**: *(Shared UI)* Huy hiệu trình độ quy chuẩn.
    * **StatusBadge [DUMB]**: *(Shared UI)* Huy hiệu trạng thái làm bài (`Chưa làm`, `Đã chấm điểm`).
  * **PaginationControl [DUMB]**: *(Shared UI)* Thanh điều khiển chuyển trang số hoặc nút `Tải thêm đề bài`.
  * **EmptyPromptState [DUMB]**: *(Shared UI)* Hiển thị khi không có đề bài nào khớp bộ lọc kèm nút `Đặt lại bộ lọc`.
  * **PromptSkeleton [DUMB]**: *(Shared UI)* Hiệu ứng tải trang dạng skeleton cho danh sách đề bài.

* **WritingPracticePage [SMART]**: Quản lý toàn bộ phiên làm bài viết: điều khiển đồng hồ đếm ngược, cơ chế tự động lưu bản nháp (auto-save), bộ đếm số từ theo thời gian thực, xác thực điều kiện nộp bài, kích hoạt nộp bài tự động khi hết giờ và điều hướng sang màn hình chấm điểm.
  * **WritingPracticeHeader [DUMB]**: Thanh điều hướng trên cùng tối giản chống xao nhãng (nút thoát phiên có xác nhận, tiêu đề đề bài thu gọn, đồng hồ đếm ngược và đèn báo trạng thái tự động lưu).
    * **CountdownTimer [DUMB]**: *(Shared UI)* Đồng hồ đếm ngược thời gian làm bài, tự động đổi màu cảnh báo khi thời gian dưới 5 phút.
    * **AutoSaveIndicator [DUMB]**: *(Shared UI)* Đèn chỉ báo trạng thái lưu bài (`Đã lưu nháp`, `Đang lưu...`, `Chưa lưu`).
  * **WritingWorkspaceLayout [DUMB]**: Bố cục không gian làm việc chia 2 cột linh hoạt (Cột trái: Đề bài; Cột phải: Khu vực viết bài).
    * **PromptDetailPanel [DUMB]**: Khung chi tiết đề bài bên trái (tiêu đề đầy đủ, nội dung yêu cầu bài viết, các điểm gợi ý/hướng dẫn, số từ tối thiểu và thời gian quy định).
    * **EssayEditorPanel [DUMB]**: Khung soạn thảo văn bản bên phải.
      * **EssayTextarea [DUMB]**: Khu vực nhập liệu bài viết toàn màn hình/cuộn mượt, font chữ chuẩn dễ đọc, hỗ trợ spellcheck trình duyệt.
      * **WordCountProgress [DUMB]**: Thanh hiển thị số lượng từ đã viết so với số từ tối thiểu (`X / Y từ`), thanh tiến độ trực quan kèm cảnh báo nếu chưa đủ số từ.
      * **EditorControlBar [DUMB]**: Thanh tác vụ cuối trang gồm nút `Lưu bản nháp` và nút CTA chính `Nộp bài để AI chấm điểm` (bị vô hiệu hóa nếu chưa đạt số từ tối thiểu hoặc bài rỗng).
  * **ConfirmExitModal [DUMB]**: *(Shared UI)* Hộp thoại xác nhận khi người học nhấn nút thoát nhằm tránh mất dữ liệu bài viết chưa lưu.
  * **TimeUpSubmissionModal [DUMB]**: *(Shared UI)* Hộp thoại thông báo hết thời gian quy định và tự động chuyển sang nộp bài cho AI.

* **WritingGradingStatusPage [SMART]**: Quản lý vòng đời trạng thái chấm điểm của AI thông qua cơ chế Polling API theo `submissionId`, điều phối chuyển cảnh tự động sang trang kết quả khi AI hoàn thành hoặc hiển thị tùy chọn cho phép người dùng rời trang mà không mất kết quả.
  * **GradingStatusCard [DUMB]**: Thẻ trung tâm hiển thị trạng thái xử lý của AI.
    * **AiScanAnimation [DUMB]**: *(Shared UI)* Hoạt ảnh minh họa AI đang quét và phân tích văn bản bài viết.
    * **GradingProgressSteps [DUMB]**: Danh sách các bước xử lý trực quan theo từng giai đoạn (`Đã lưu bài viết an toàn` -> `Đang kiểm tra ngữ pháp & chính tả` -> `Đang phân tích vốn từ vựng & ngữ cảnh` -> `Đang tổng hợp điểm số và nhận xét`).
    * **GradingNoticeBox [DUMB]**: Thông báo trấn an người học rằng bài viết đã được lưu an toàn trên hệ thống và có thể rời trang bất kỳ lúc nào để quay lại xem kết quả sau.
    * **GradingActions [DUMB]**: Nút điều hướng phụ `Quay về danh mục đề` trong khi AI tiếp tục chấm ngầm.

* **WritingResultPage [SMART]**: Tải và quản lý toàn bộ dữ liệu kết quả chấm bài từ AI theo `submissionId`, điều khiển tương tác liên kết giữa góp ý AI và vị trí lỗi trên bài viết gốc, hỗ trợ chia sẻ kết quả hoặc làm lại bài.
  * **ResultHeroHeader [DUMB]**: Tiêu đề trang kết quả, tên đề bài, ngày giờ nộp bài, tổng thời gian làm bài thực tế và tổng số từ đã viết.
  * **OverallScoreCard [DUMB]**: Thẻ tổng kết điểm số tổng quan trên thang điểm 10, nhận xét khái quát từ AI và bảng điểm chi tiết theo từng tiêu chí (Ngữ pháp, Từ vựng, Tính mạch lạc, Mức độ đáp ứng yêu cầu).
    * **ScoreBadge [DUMB]**: *(Shared UI)* Huy hiệu điểm số tổng với mã màu trực quan (Xanh: Xuất sắc/Tốt, Vàng: Khá, Cam: Cần cố gắng).
    * **CriteriaScoreItem [DUMB]**: Mục hiển thị điểm từng tiêu chí thành phần kèm thanh điểm số mini.
  * **ResultComparisonLayout [DUMB]**: Bố cục 2 cột đối chiếu giữa bài viết gốc và nhận xét AI (có thể chuyển dạng Tab trên thiết bị di động).
    * **OriginalEssayViewer [DUMB]**: Khung xem bài viết gốc của người học, hỗ trợ làm nổi bật (highlight) các đoạn văn/từ vựng tương ứng khi người học chọn một góp ý bên khung nhận xét.
    * **AiFeedbackContainer [DUMB]**: Khung hiển thị chi tiết phản hồi chuyên sâu từ AI.
      * **FeedbackCategoryTabs [DUMB]**: Tab lọc nhóm góp ý (`Tất cả`, `Ngữ pháp`, `Từ vựng`, `Cách diễn đạt`).
      * **FeedbackCardList [DUMB]**: Danh sách các thẻ nhận xét và sửa lỗi chi tiết.
        * **FeedbackCardItem [DUMB]**: Thẻ góp ý đơn lẻ (đoạn câu gốc có lỗi, giải thích nguyên nhân lỗi, câu đề xuất cải thiện chuẩn xác và giải thích lý do thay đổi).
      * **GeneralAdviceBox [DUMB]**: Hộp tóm tắt các lời khuyên tổng thể để nâng band điểm trong các bài viết tiếp theo.
  * **ResultActionFooter [DUMB]**: Thanh tác vụ cuối trang (nút CTA `Viết lại bài này`, nút `Luyện đề khác` và nút `Xem lịch sử bài viết`).
  * **ResultSkeleton [DUMB]**: *(Shared UI)* Hiệu ứng tải dữ liệu trang kết quả bài viết.

* **WritingHistoryPage [SMART]**: Quản lý danh sách lịch sử các bài viết đã nộp của người học, hỗ trợ tìm kiếm bài viết cũ, lọc theo danh mục và điểm số, xem lại chi tiết nhận xét AI.
  * **HistoryHeader [DUMB]**: Tiêu đề trang "Lịch sử bài viết", tổng số bài đã làm và điểm trung bình tích lũy.
  * **HistoryFilterBar [DUMB]**: Thanh tìm kiếm theo tên đề bài và dropdown lọc theo danh mục hoặc thang điểm.
  * **HistoryTable [DUMB]**: Bảng danh sách các bài viết đã nộp (tên đề, danh mục, thời điểm nộp, số từ, thời gian làm, điểm số, trạng thái và CTA `Xem chi tiết`).
  * **HistoryEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi người học chưa có bài viết nào trong lịch sử kèm CTA `Bắt đầu viết ngay`.

---

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Zustand`) — thông tin người dùng đang đăng nhập để xác định quyền hạn, định danh tác giả bài viết và gắn `userId` vào bản ghi nộp bài.
* `activeDraftCache`: **Global State** (`Zustand`) — bộ nhớ đệm lưu trữ nội dung bài viết và ID đề bài đang làm dở dang, giúp khôi phục tức thì khi người dùng vô tình reload trang hoặc rớt mạng.
* `writingCategories`: **Server State** (`RTK Query`) — danh sách các nhóm danh mục luyện viết (IELTS, TOEIC, Nền tảng, Công việc) và số lượng đề bài tương ứng.
* `promptsByCategory`: **Server State** (`RTK Query`) — danh sách đề bài theo danh mục, hỗ trợ tìm kiếm, lọc theo trình độ và phân trang.
* `promptDetail`: **Server State** (`RTK Query`) — dữ liệu chi tiết một đề bài cụ thể (tiêu đề, yêu cầu, tiêu chuẩn từ tối thiểu, gợi ý dàn ý, thời lượng cho phép).
* `submissionStatus`: **Server State** (`RTK Query`) — dữ liệu trạng thái xử lý chấm bài của AI (hỗ trợ cấu hình `pollingInterval` tự động kiểm tra mỗi 3 giây cho đến khi trạng thái chuyển sang `COMPLETED`).
* `submissionResult`: **Server State** (`RTK Query`) — toàn bộ kết quả chấm bài của AI gồm điểm tổng, điểm thành phần, bài viết gốc và danh sách chi tiết các nhận xét ngữ pháp/từ vựng/diễn đạt.
* `writingHistoryList`: **Server State** (`RTK Query`) — danh sách lịch sử làm bài viết của người học có hỗ trợ phân trang.
* `isLoading`, `isFetching`, `isError`: **Server State** (`RTK Query`) — các cờ trạng thái gọi API tự động từ query hook, không tạo state cục bộ trùng lặp.
* `search`: **URL Query Parameter** (`?search=ielts+task+2`) — từ khóa tìm kiếm đề bài trên URL, phục vụ việc duy trì kết quả khi reload hoặc chia sẻ liên kết.
* `category`: **URL Query Parameter** (`?category=ielts`) — định danh danh mục đang xem.
* `topic`: **URL Query Parameter** (`?topic=technology`) — chủ đề lọc đề bài trên trang danh sách.
* `level`: **URL Query Parameter** (`?level=b2`) — trình độ lọc đề bài.
* `duration`: **URL Query Parameter** (`?duration=30`) — thời lượng làm bài cần lọc.
* `page`: **URL Query Parameter** (`?page=1`) — chỉ số trang hiện tại của danh sách đề hoặc lịch sử bài viết.
* `feedbackTab`: **URL Query Parameter** (`?tab=grammar`) — nhóm góp ý AI đang chọn hiển thị trên trang kết quả (`all`, `grammar`, `vocabulary`, `expression`).
* `essayText`: **Local State** (`useState`) — nội dung bài văn người học đang soạn thảo trong editor của `WritingPracticePage`.
* `wordCount`: **Local State** (`useState` hoặc `useMemo`) — số lượng từ hiện tại được tính toán trực tiếp theo thời gian thực từ `essayText`.
* `timeRemainingSeconds`: **Local State** (`useState`) — số giây đếm ngược còn lại của phiên làm bài.
* `autoSaveStatus`: **Local State** (`useState`) — trạng thái lưu tự động của bài viết (`IDLE`, `SAVING`, `SAVED`, `ERROR`).
* `lastSavedTime`: **Local State** (`useState`) — mốc thời gian lưu bản nháp thành công gần nhất.
* `isConfirmExitOpen`: **Local State** (`useState`) — kiểm soát hiển thị modal cảnh báo khi người học nhấn nút rời khỏi phòng luyện viết.
* `isTimeUpModalOpen`: **Local State** (`useState`) — kiểm soát hiển thị thông báo hết giờ làm bài trước khi hệ thống nộp bài tự động.
* `selectedFeedbackId`: **Local State** (`useState`) — ID của nhận xét AI đang được người dùng nhấn chọn để tô sáng (highlight) đoạn văn tương ứng trên bài viết gốc.

---

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
// ==================== ENUMS & ALIAS TYPES ====================

export type WritingCategoryType = 'IELTS' | 'TOEIC' | 'FOUNDATION' | 'WORK';

export type ProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type SubmissionStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export type FeedbackType = 'GRAMMAR' | 'VOCABULARY' | 'EXPRESSION';

export type AutoSaveState = 'IDLE' | 'SAVING' | 'SAVED' | 'ERROR';

// ==================== ENTITY MODELS ====================

export interface WritingCategoryItem {
  id: string;
  type: WritingCategoryType;
  title: string;
  description: string;
  totalPrompts: number;
  iconName: string;
}

export interface WritingPromptItem {
  id: string;
  category: WritingCategoryType;
  title: string;
  shortDescription: string;
  fullRequirement: string;
  topic: string;
  level: ProficiencyLevel;
  durationMinutes: number;
  minWords: number;
  isCompleted: boolean;
  bestScore?: number;
}

export interface FeedbackDetailItem {
  id: string;
  type: FeedbackType;
  originalText: string;
  suggestedText: string;
  explanation: string;
  startOffset?: number;
  endOffset?: number;
}

export interface CriteriaScore {
  criterion: string;
  score: number;
  maxScore: number;
  comment: string;
}

export interface WritingSubmissionResultData {
  id: string;
  promptId: string;
  promptTitle: string;
  submittedAt: string;
  timeSpentSeconds: number;
  totalWords: number;
  overallScore: number;
  criteriaScores: CriteriaScore[];
  generalFeedback: string;
  feedbacks: FeedbackDetailItem[];
  originalEssay: string;
}

export interface WritingHistoryRecord {
  submissionId: string;
  promptTitle: string;
  category: WritingCategoryType;
  submittedAt: string;
  totalWords: number;
  timeSpentSeconds: number;
  overallScore: number;
  status: SubmissionStatus;
}

// ==================== DUMB COMPONENT PROPS ====================

// --- Màn hình 1: Danh mục luyện viết ---

export interface WritingCategoryCardProps {
  category: WritingCategoryItem;
  onSelectCategory: (categoryType: WritingCategoryType) => void;
}

export interface WritingCategoryListProps {
  categories: WritingCategoryItem[];
  onSelectCategory: (categoryType: WritingCategoryType) => void;
}

export interface WritingCategoryHeaderProps {
  title: string;
  subtitle: string;
}

// --- Màn hình 2: Danh sách đề bài theo danh mục ---

export interface PromptListHeaderProps {
  categoryTitle: string;
  totalCount: number;
  breadcrumbItems: { label: string; path?: string }[];
}

export interface PromptCardProps {
  prompt: WritingPromptItem;
  onStart: (promptId: string) => void;
}

export interface PromptGridProps {
  prompts: WritingPromptItem[];
  onStartPrompt: (promptId: string) => void;
}

export interface PromptFilterBarProps {
  searchQuery: string;
  selectedTopic: string;
  selectedLevel: string;
  selectedDuration: string;
  selectedStatus: string;
  topicOptions: { value: string; label: string }[];
  levelOptions: { value: string; label: string }[];
  durationOptions: { value: string; label: string }[];
  onSearchChange: (query: string) => void;
  onTopicChange: (topic: string) => void;
  onLevelChange: (level: string) => void;
  onDurationChange: (duration: string) => void;
  onStatusChange: (status: string) => void;
  onResetFilters: () => void;
}

// --- Màn hình 3: Luyện viết (Practice) ---

export interface CountdownTimerProps {
  remainingSeconds: number;
  isUrgentThresholdSeconds?: number;
}

export interface AutoSaveIndicatorProps {
  status: AutoSaveState;
  lastSavedAt?: string;
}

export interface WritingPracticeHeaderProps {
  promptTitle: string;
  remainingSeconds: number;
  saveStatus: AutoSaveState;
  lastSavedAt?: string;
  onExitClick: () => void;
}

export interface PromptDetailPanelProps {
  title: string;
  requirement: string;
  minWords: number;
  durationMinutes: number;
  topic: string;
  level: ProficiencyLevel;
}

export interface WordCountProgressProps {
  currentCount: number;
  minWords: number;
}

export interface EssayTextareaProps {
  content: string;
  placeholder?: string;
  onChangeContent: (text: string) => void;
}

export interface EditorControlBarProps {
  isSubmitDisabled: boolean;
  isSubmitting: boolean;
  disabledReason?: string;
  onManualSaveDraft: () => void;
  onSubmitEssay: () => void;
}

export interface WritingWorkspaceLayoutProps {
  promptPanel: React.ReactNode;
  editorPanel: React.ReactNode;
}

export interface ConfirmExitModalProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export interface TimeUpSubmissionModalProps {
  isOpen: boolean;
  onAutoSubmit: () => void;
}

// --- Màn hình 4: Đang AI chấm bài ---

export interface AiScanAnimationProps {
  message?: string;
}

export interface GradingProgressStepsProps {
  currentStepIndex: number;
  steps: { label: string; isCompleted: boolean; isProcessing: boolean }[];
}

export interface GradingNoticeBoxProps {
  title: string;
  description: string;
}

export interface GradingStatusCardProps {
  promptTitle: string;
  currentStepIndex: number;
  steps: { label: string; isCompleted: boolean; isProcessing: boolean }[];
  onReturnToList: () => void;
}

// --- Màn hình 5: Kết quả bài viết ---

export interface ScoreBadgeProps {
  score: number;
  maxScore?: number;
}

export interface CriteriaScoreItemProps {
  criterion: string;
  score: number;
  maxScore: number;
  comment: string;
}

export interface OverallScoreCardProps {
  overallScore: number;
  generalFeedback: string;
  criteriaScores: CriteriaScore[];
}

export interface FeedbackCategoryTabsProps {
  activeTab: FeedbackType | 'ALL';
  grammarCount: number;
  vocabularyCount: number;
  expressionCount: number;
  onSelectTab: (tab: FeedbackType | 'ALL') => void;
}

export interface FeedbackCardItemProps {
  feedback: FeedbackDetailItem;
  isSelected: boolean;
  onSelect: (feedbackId: string) => void;
}

export interface FeedbackCardListProps {
  feedbacks: FeedbackDetailItem[];
  selectedFeedbackId?: string;
  onSelectFeedback: (feedbackId: string) => void;
}

export interface OriginalEssayViewerProps {
  essayText: string;
  feedbacks: FeedbackDetailItem[];
  selectedFeedbackId?: string;
  onSelectHighlight: (feedbackId: string) => void;
}

export interface ResultActionFooterProps {
  onRewritePrompt: () => void;
  onExploreOtherPrompts: () => void;
  onViewHistory: () => void;
}

// --- Màn hình bổ trợ: Lịch sử bài viết ---

export interface HistoryTableProps {
  records: WritingHistoryRecord[];
  onViewDetail: (submissionId: string) => void;
}

export interface HistoryFilterBarProps {
  searchQuery: string;
  selectedCategory: string;
  categories: { value: string; label: string }[];
  onSearchChange: (val: string) => void;
  onCategoryChange: (val: string) => void;
}

// --- Shared UI Components ---

export interface BreadcrumbProps {
  items: { label: string; path?: string }[];
}

export interface LevelBadgeProps {
  level: ProficiencyLevel;
}

export interface StatusBadgeProps {
  status: 'NOT_STARTED' | 'COMPLETED' | 'IN_PROGRESS';
}

export interface PaginationControlProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export interface CommonEmptyStateProps {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
}
```
