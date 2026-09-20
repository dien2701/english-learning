# 06-Reading-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **ReadingCategoryPage [SMART]**: Quản lý và điều phối dữ liệu trang danh mục bài luyện đọc (IELTS, TOEIC, Nền tảng, Công việc), theo dõi tổng quan tiến độ theo từng nhóm mục tiêu và điều hướng sang danh sách bài đọc chi tiết.
  * **ReadingCategoryHeader [DUMB]**: Tiêu đề trang "Luyện đọc tiếng Anh", mô tả ngắn định hướng rèn luyện kỹ năng đọc hiểu theo nhóm mục tiêu.
  * **ReadingCategoryList [DUMB]**: Danh sách hiển thị theo chiều dọc 04 danh mục luyện đọc chính.
    * **ReadingCategoryCard [DUMB]**: Thẻ đại diện cho từng danh mục (tên danh mục, mô tả ngắn gọn, số lượng bài luyện hiện có và nút CTA duy nhất `Xem thêm`).
  * **ReadingCategorySkeleton [DUMB]**: *(Shared UI)* Khung xương tải dữ liệu cho trang danh mục luyện đọc.
  * **CategoryEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi không có danh mục khả dụng kèm nút tải lại.

* **ReadingArticleListPage [SMART]**: Điều phối dữ liệu danh sách bài luyện đọc thuộc danh mục được chọn, xử lý logic tìm kiếm theo tên bài, bộ lọc đa tiêu chí (chủ đề, trình độ, thời lượng, trạng thái làm bài) và phân trang.
  * **ArticleListHeader [DUMB]**: Khối tiêu đề danh mục đang chọn, mô tả ngắn, breadcrumb quay lại trang danh mục và bộ đếm tổng số bài luyện tìm thấy.
    * **Breadcrumb [DUMB]**: *(Shared UI)* Thanh dẫn đường phân cấp (`Trang chủ > Luyện đọc > IELTS`).
  * **ArticleFilterBar [DUMB]**: Thanh công cụ tìm kiếm và lọc bài luyện đọc.
    * **ArticleSearchBar [DUMB]**: *(Shared UI)* Ô nhập từ khóa tìm kiếm tên bài đọc hỗ trợ debounce và nút xóa nhanh nội dung tìm kiếm.
    * **TopicFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo chủ đề (Khoa học, Công nghệ, Môi trường, Kinh doanh, Văn hóa - Xã hội...).
    * **LevelFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo trình độ (A1 - C2 / IELTS 5.0 - 8.5 / TOEIC).
    * **DurationFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo thời lượng (10, 15, 20, 30, 45 phút).
    * **ArticleStatusTabs [DUMB]**: *(Shared UI)* Thanh tab lọc nhanh trạng thái bài (`Tất cả`, `Chưa làm`, `Đã làm`).
  * **ArticleGrid [DUMB]**: Lưới responsive hiển thị các thẻ bài luyện đọc (1 cột mobile, 2-3 cột desktop).
    * **ArticleCard [DUMB]**: Thẻ bài luyện đọc (tên bài, chủ đề, huy hiệu trình độ, thời gian làm bài, số lượng câu hỏi, trạng thái đã làm và nút CTA `Bắt đầu làm`).
    * **LevelBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị cấp độ chuẩn hóa.
    * **StatusBadge [DUMB]**: *(Shared UI)* Huy hiệu trạng thái (`Chưa làm`, `Đã làm`).
    * **DurationBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị thời lượng quy định (VD: `20 phút`).
  * **PaginationControl [DUMB]**: *(Shared UI)* Thanh điều khiển chuyển trang số hoặc nút `Tải thêm bài đọc`.
  * **ArticleEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi không tìm thấy bài đọc nào khớp với bộ lọc kèm nút `Đặt lại bộ lọc`.
  * **ArticleListSkeleton [DUMB]**: *(Shared UI)* Khung xương tải trang danh sách bài luyện đọc.

* **ReadingPracticePage [SMART]**: Quản lý toàn bộ phiên làm bài luyện đọc: nạp bài đọc và câu hỏi (chặn hiển thị đáp án), kiểm soát đồng hồ đếm ngược, lưu trữ câu trả lời của người học theo thời gian thực, xác thực điều kiện nộp bài, kích hoạt nộp tự động khi hết giờ và điều hướng sang màn hình chấm điểm.
  * **ReadingPracticeHeader [DUMB]**: Thanh điều hướng trên cùng tối giản chống xao nhãng (tên bài đọc, chủ đề, chỉ báo số câu đã trả lời, đồng hồ đếm ngược và nút thoát có xác nhận).
    * **CountdownTimer [DUMB]**: *(Shared UI)* Đồng hồ đếm ngược thời gian quy định, tự động chuyển màu cảnh báo khi thời gian dưới 3 phút.
    * **AnswerProgressIndicator [DUMB]**: *(Shared UI)* Hiển thị tỷ lệ số câu đã trả lời trên tổng số câu hỏi (VD: `8/10 câu`).
  * **ReadingWorkspaceLayout [DUMB]**: Bố cục không gian làm việc chia 2 cột trên Desktop (Cột trái: Bài đọc; Cột phải: Danh sách câu hỏi & nhập đáp án) và xếp dọc trên Mobile.
    * **ReadingPassagePanel [DUMB]**: Khung hiển thị nội dung bài đọc bên trái.
      * **PassageToolbar [DUMB]**: Thanh tác vụ hỗ trợ đọc (tùy chỉnh kích cỡ chữ `A-` / `A+`, giãn dòng).
      * **PassageContentBody [DUMB]**: Nội dung văn bản bài đọc định dạng chuẩn, chia đoạn rõ ràng, cỡ chữ tối ưu cho việc đọc tập trung và chống mỏi mắt.
    * **ReadingQuestionPanel [DUMB]**: Khung danh sách câu hỏi và nhập đáp án bên phải.
      * **QuestionNavigationPills [DUMB]**: *(Shared UI)* Thanh danh số câu hỏi mini, hiển thị trực quan trạng thái đã trả lời/chưa trả lời và hỗ trợ nhảy nhanh đến câu tương ứng.
      * **QuestionListContainer [DUMB]**: Danh sách hiển thị các câu hỏi được đánh số thứ tự tuần tự.
        * **QuestionItemCard [DUMB]**: Khung câu hỏi đơn lẻ (số thứ tự câu, đề bài câu hỏi, trạng thái đã/chưa trả lời).
          * **MultipleChoiceQuestion [DUMB]**: Danh sách các phương án lựa chọn A, B, C, D với radio selection trực quan.
          * **FillBlankQuestion [DUMB]**: Ô nhập liệu văn bản trả lời cho dạng câu hỏi điền từ vào chỗ trống.
  * **PracticeControlBar [DUMB]**: Thanh tác vụ chân trang cố định chứa nút CTA chính `Nộp bài để chấm điểm`.
  * **ConfirmSubmitIncompleteModal [DUMB]**: *(Shared UI)* Hộp thoại cảnh báo xác nhận khi người học nhấn nộp bài mà vẫn còn câu hỏi chưa hoàn thành.
  * **TimeUpSubmitModal [DUMB]**: *(Shared UI)* Hộp thoại thông báo đã hết thời gian làm bài quy định và hệ thống tự động lưu đáp án để nộp.
  * **ConfirmExitModal [DUMB]**: *(Shared UI)* Hộp thoại xác nhận thoát khỏi bài luyện đọc dở dang để bảo vệ tiến trình.

* **ReadingGradingStatusPage [SMART]**: Quản lý trạng thái trung gian gửi bài và chấm điểm: gửi bài nộp, vô hiệu hóa chỉnh sửa, hiển thị trạng thái "Đang chấm bài" kèm hoạt ảnh tải nhẹ và thông báo bài làm đã được lưu an toàn, cho phép người dùng có thể chờ hoặc rời đi mà không mất kết quả, tự động chuyển tiếp sang màn hình kết quả khi hoàn tất.
  * **GradingStatusCard [DUMB]**: Thẻ thông báo trạng thái chấm bài trung tâm.
    * **ReadingScanAnimation [DUMB]**: *(Shared UI)* Hoạt ảnh quét biểu tượng bài đọc biểu thị hệ thống đang đối chiếu đáp án đọc hiểu.
    * **GradingProgressSteps [DUMB]**: Danh sách chỉ báo tiến độ chấm bài trực quan (`Đã lưu bài làm an toàn` -> `Đang đối chiếu đáp án đọc hiểu` -> `Đang tổng hợp điểm số và giải thích chi tiết`).
    * **GradingNoticeBox [DUMB]**: Khung thông báo trấn an người học rằng kết quả đã được lưu trong lịch sử học tập, có thể rời trang bất kỳ lúc nào và sẽ nhận thông báo khi hoàn tất.
    * **GradingActions [DUMB]**: Cụm nút tác vụ gồm `Quay về danh sách bài luyện` cho phép người dùng rời trang trong lúc hệ thống chấm ngầm.

* **ReadingResultPage [SMART]**: Quản lý và hiển thị toàn bộ kết quả bài luyện đọc theo `attemptId`: tải điểm tổng, số câu đúng/tổng số câu, thời gian đã sử dụng, trạng thái hoàn thành, đối chiếu từng câu với đáp án đúng và giải thích ngắn, điều phối tương tác highlight trích dẫn giữa câu hỏi và bài đọc, cung cấp các CTA tiếp tục.
  * **ResultHeroHeader [DUMB]**: Tiêu đề trang kết quả, tên bài đọc, danh mục, thời điểm hoàn thành và tổng thời gian làm bài thực tế.
  * **ScoreSummaryCard [DUMB]**: Thẻ tổng quan kết quả (điểm tổng, số câu đúng / tổng số câu, số câu sai, số câu bỏ trống, tỷ lệ chính xác % và trạng thái hoàn thành).
    * **ScoreBadge [DUMB]**: *(Shared UI)* Huy hiệu điểm số nổi bật với màu sắc biểu thị mức độ hoàn thành (Xanh: Xuất sắc/Tốt, Vàng: Khá, Đỏ: Cần cải thiện).
    * **ResultStatItem [DUMB]**: *(Shared UI)* Khối thống kê chỉ số thành phần (Số câu đúng, số câu sai, thời gian làm bài).
  * **ResultWorkspaceLayout [DUMB]**: Bố cục đối chiếu kết quả 2 cột (Cột trái: Bài đọc kèm khả năng highlight đoạn trích dẫn; Cột phải: Danh sách chi tiết câu hỏi và giải thích).
    * **ResultPassageViewer [DUMB]**: Khung xem lại bài đọc gốc, hỗ trợ tự động cuộn tới và tô sáng (highlight) đoạn văn chứa bằng chứng trả lời khi người học click vào câu hỏi tương ứng.
    * **QuestionReviewList [DUMB]**: Danh sách chi tiết toàn bộ câu hỏi đã làm bài.
      * **QuestionReviewItem [DUMB]**: Thẻ câu hỏi chi tiết bao gồm: nội dung câu hỏi, câu trả lời của người học, đáp án đúng của hệ thống, huy hiệu `ĐÚNG` / `SAI`, đoạn trích dẫn chứng từ bài đọc và giải thích ngắn gọn.
  * **ResultActionFooter [DUMB]**: Thanh tác vụ chân trang kết quả chứa các nút điều hướng: nút CTA `Làm lại bài này`, nút `Bài luyện tiếp theo` và nút `Quay về danh sách bài luyện`.
  * **ResultSkeleton [DUMB]**: *(Shared UI)* Hiệu ứng tải trang kết quả bài luyện đọc.

---

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Zustand`) — thông tin người dùng đang đăng nhập (`userId`, tên, quyền hạn) để định danh học viên, gắn quyền nộp bài, ghi nhận kết quả vào lịch sử học tập và cập nhật bảng thống kê kỹ năng đọc.
* `readingPreferences`: **Global State** (`Zustand`) — thiết lập tùy biến trải nghiệm đọc cá nhân hóa (cỡ chữ mặc định: `sm` | `base` | `lg` | `xl`, độ giãn dòng line-height) giúp duy trì sự đồng nhất thoải mái thị giác qua từng bài đọc.
* `readingCategories`: **Server State** (`RTK Query`) — danh sách 04 nhóm danh mục luyện đọc (IELTS, TOEIC, Nền tảng, Công việc) kèm số lượng bài luyện hiện có của từng nhóm.
* `articlesByCategory`: **Server State** (`RTK Query`) — danh sách bài luyện đọc thuộc danh mục đang chọn, hỗ trợ tìm kiếm theo tên, bộ lọc theo chủ đề, trình độ, thời lượng, trạng thái làm bài và phân trang.
* `articlePracticeDetail`: **Server State** (`RTK Query`) — dữ liệu bài luyện đọc phục vụ làm bài (tiêu đề, chủ đề, nội dung bài đọc, thời gian giới hạn, danh sách câu hỏi và các phương án lựa chọn; tuyệt đối không kèm đáp án đúng hoặc gợi ý).
* `readingGradingStatus`: **Server State** (`RTK Query`) — trạng thái chấm bài từ backend (`PENDING`, `GRADING`, `COMPLETED`, `FAILED`), hỗ trợ cấu hình tự động kiểm tra mỗi 1.5 - 2 giây để chuyển trang kết quả khi người dùng ở lại trang.
* `readingAttemptResult`: **Server State** (`RTK Query`) — toàn bộ dữ liệu kết quả chấm bài (điểm tổng, số câu đúng/tổng số câu, thời gian đã sử dụng, trạng thái hoàn thành, danh sách chi tiết từng câu gồm đáp án học viên, đáp án đúng, trạng thái đúng/sai, giải thích ngắn và gợi ý bài luyện tiếp theo).
* `isLoading`, `isFetching`, `isError`: **Server State** (`RTK Query`) — các cờ trạng thái gọi API tự động từ query hook, không tạo state cục bộ trùng lặp trong component.
* `category`: **URL Query Parameter** (`?category=ielts`) — định danh danh mục đang xem trên trang danh sách bài luyện.
* `topic`: **URL Query Parameter** (`?topic=technology`) — chủ đề bài đọc đang được chọn để lọc.
* `level`: **URL Query Parameter** (`?level=b2`) — cấp độ trình độ đang được lọc trên trang danh sách.
* `duration`: **URL Query Parameter** (`?duration=20`) — thời lượng làm bài cần lọc (phút).
* `status`: **URL Query Parameter** (`?status=not_started`) — trạng thái bài luyện cần lọc (`all`, `not_started`, `completed`).
* `search`: **URL Query Parameter** (`?search=artificial+intelligence`) — từ khóa tìm kiếm tên bài đọc trên URL, giúp duy trì kết quả khi chia sẻ liên kết hoặc tải lại trang.
* `page`: **URL Query Parameter** (`?page=1`) — chỉ số trang hiện tại của danh sách bài luyện đọc.
* `activeReviewQuestion`: **URL Query Parameter** (`?question=2`) — mã/thứ tự câu hỏi đang chọn xem ở màn hình kết quả để đồng bộ vị trí đoạn trích dẫn chứng trên bài đọc.
* `userAnswers`: **Local State** (`useState`) — bảng lưu trữ câu trả lời của người học theo dạng key-value (`Record<string, string>`, trong đó key là `questionId`, value là đáp án lựa chọn hoặc chuỗi điền từ).
* `timeRemainingSeconds`: **Local State** (`useState`) — số giây đếm ngược còn lại của phiên làm bài đọc.
* `activeQuestionId`: **Local State** (`useState`) — ID câu hỏi đang được focus hoặc cuộn tới trong phiên làm bài.
* `passageFontSize`: **Local State** (`useState`) — kích cỡ chữ bài đọc hiện tại (`'sm' | 'base' | 'lg' | 'xl'`) cho phép học viên tăng giảm linh hoạt ngay khi đang làm bài.
* `isConfirmSubmitOpen`: **Local State** (`useState`) — điều khiển hiển thị hộp thoại cảnh báo khi người học nhấn nộp bài mà vẫn còn câu hỏi chưa trả lời.
* `isTimeUpModalOpen`: **Local State** (`useState`) — điều khiển hiển thị hộp thoại thông báo đã hết thời gian làm bài quy định và hệ thống tự động nộp bài.
* `isConfirmExitOpen`: **Local State** (`useState`) — điều khiển hiển thị hộp thoại xác nhận thoát phòng luyện đọc khi đang làm bài dở dang.
* `selectedEvidenceOffset`: **Local State** (`useState`) — vị trí phân đoạn trích dẫn trong bài đọc cần được tô sáng (highlight) khi người học xem xét kết quả câu hỏi.

---

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
// ==================== ENUMS & LITERAL TYPES ====================

export type ReadingCategoryType = 'IELTS' | 'TOEIC' | 'FOUNDATION' | 'WORK';

export type ProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type ArticleCompletionStatus = 'NOT_STARTED' | 'COMPLETED';

export type QuestionType = 'MULTIPLE_CHOICE' | 'FILL_IN_BLANK';

export type ReadingGradingStatusType = 'PENDING' | 'GRADING' | 'COMPLETED' | 'FAILED';

export type FontSizeLevel = 'sm' | 'base' | 'lg' | 'xl';

// ==================== CORE ENTITY MODELS ====================

export interface ReadingCategoryItem {
  id: string;
  type: ReadingCategoryType;
  title: string;
  description: string;
  totalArticles: number;
  iconName: string;
}

export interface ReadingArticleSummary {
  id: string;
  category: ReadingCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  durationMinutes: number;
  totalQuestions: number;
  status: ArticleCompletionStatus;
  bestScore?: number;
}

export interface QuestionOption {
  key: 'A' | 'B' | 'C' | 'D';
  content: string;
}

export interface ReadingPracticeQuestion {
  id: string;
  orderNumber: number;
  type: QuestionType;
  prompt: string;
  options?: QuestionOption[];
}

export interface ReadingArticleDetail {
  id: string;
  category: ReadingCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  durationMinutes: number;
  content: string;
  totalQuestions: number;
  questions: ReadingPracticeQuestion[];
}

export interface UserReadingAnswerItem {
  questionId: string;
  answer: string;
}

export interface SubmitReadingAttemptPayload {
  articleId: string;
  timeSpentSeconds: number;
  answers: UserReadingAnswerItem[];
}

export interface QuestionEvidence {
  paragraphIndex: number;
  startOffset?: number;
  endOffset?: number;
  snippetText: string;
}

export interface ReadingQuestionReviewDetail {
  questionId: string;
  orderNumber: number;
  type: QuestionType;
  prompt: string;
  options?: QuestionOption[];
  userAnswer: string;
  correctAnswer: string;
  isCorrect: boolean;
  explanation: string;
  evidence?: QuestionEvidence;
}

export interface ReadingAttemptResult {
  attemptId: string;
  articleId: string;
  articleTitle: string;
  category: ReadingCategoryType;
  completedAt: string;
  timeSpentSeconds: number;
  totalQuestions: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  overallScore: number;
  isCompleted: boolean;
  content: string;
  questionReviews: ReadingQuestionReviewDetail[];
  nextArticleId?: string;
}

// ==================== DUMB COMPONENT PROPS ====================

// --- Màn hình 1: Danh mục bài luyện đọc ---

export interface ReadingCategoryHeaderProps {
  title: string;
  description: string;
}

export interface ReadingCategoryCardProps {
  category: ReadingCategoryItem;
  onSelectCategory: (categoryType: ReadingCategoryType) => void;
}

export interface ReadingCategoryListProps {
  categories: ReadingCategoryItem[];
  onSelectCategory: (categoryType: ReadingCategoryType) => void;
}

// --- Màn hình 2: Danh sách bài luyện theo danh mục ---

export interface ArticleListHeaderProps {
  categoryTitle: string;
  description: string;
  totalArticles: number;
  breadcrumbItems: { label: string; path?: string }[];
}

export interface ArticleFilterBarProps {
  searchQuery: string;
  selectedTopic: string;
  selectedLevel: string;
  selectedDuration: string;
  selectedStatus: string;
  topicOptions: { value: string; label: string }[];
  levelOptions: { value: string; label: string }[];
  durationOptions: { value: string; label: string }[];
  onSearchChange: (value: string) => void;
  onTopicChange: (topic: string) => void;
  onLevelChange: (level: string) => void;
  onDurationChange: (duration: string) => void;
  onStatusChange: (status: string) => void;
  onResetFilters: () => void;
}

export interface ArticleCardProps {
  article: ReadingArticleSummary;
  onStartArticle: (articleId: string) => void;
}

export interface ArticleGridProps {
  articles: ReadingArticleSummary[];
  onStartArticle: (articleId: string) => void;
}

// --- Màn hình 3: Làm bài luyện đọc ---

export interface CountdownTimerProps {
  remainingSeconds: number;
  urgentThresholdSeconds?: number;
}

export interface AnswerProgressIndicatorProps {
  answeredCount: number;
  totalQuestions: number;
}

export interface ReadingPracticeHeaderProps {
  articleTitle: string;
  topic: string;
  remainingSeconds: number;
  answeredCount: number;
  totalQuestions: number;
  onExitClick: () => void;
}

export interface PassageToolbarProps {
  currentFontSize: FontSizeLevel;
  onChangeFontSize: (size: FontSizeLevel) => void;
}

export interface PassageContentBodyProps {
  title: string;
  content: string;
  fontSize: FontSizeLevel;
}

export interface ReadingPassagePanelProps {
  title: string;
  content: string;
  fontSize: FontSizeLevel;
  onChangeFontSize: (size: FontSizeLevel) => void;
}

export interface QuestionNavigationPillsProps {
  questions: { id: string; orderNumber: number }[];
  userAnswers: Record<string, string>;
  activeQuestionId: string;
  onSelectQuestion: (questionId: string) => void;
}

export interface MultipleChoiceQuestionProps {
  questionId: string;
  options: QuestionOption[];
  selectedAnswer: string;
  onSelectOption: (questionId: string, optionKey: string) => void;
}

export interface FillBlankQuestionProps {
  questionId: string;
  answerValue: string;
  onChangeAnswer: (questionId: string, value: string) => void;
}

export interface QuestionItemCardProps {
  question: ReadingPracticeQuestion;
  currentAnswer: string;
  isActive: boolean;
  onAnswerChange: (questionId: string, answer: string) => void;
}

export interface QuestionListContainerProps {
  questions: ReadingPracticeQuestion[];
  userAnswers: Record<string, string>;
  activeQuestionId: string;
  onAnswerChange: (questionId: string, answer: string) => void;
}

export interface ReadingQuestionPanelProps {
  questions: ReadingPracticeQuestion[];
  userAnswers: Record<string, string>;
  activeQuestionId: string;
  onSelectQuestion: (questionId: string) => void;
  onAnswerChange: (questionId: string, answer: string) => void;
}

export interface ReadingWorkspaceLayoutProps {
  passagePanel: React.ReactNode;
  questionPanel: React.ReactNode;
}

export interface PracticeControlBarProps {
  answeredCount: number;
  totalQuestions: number;
  isSubmitting: boolean;
  onSubmitAttempt: () => void;
}

export interface ConfirmSubmitIncompleteModalProps {
  isOpen: boolean;
  unansweredCount: number;
  onConfirm: () => void;
  onCancel: () => void;
}

export interface TimeUpSubmitModalProps {
  isOpen: boolean;
  onAutoSubmit: () => void;
}

// --- Màn hình 4: Đang chấm bài ---

export interface ReadingScanAnimationProps {
  statusText?: string;
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
  articleTitle: string;
  currentStepIndex: number;
  steps: { label: string; isCompleted: boolean; isProcessing: boolean }[];
  onReturnToList: () => void;
}

// --- Màn hình 5: Kết quả bài luyện đọc ---

export interface ResultHeroHeaderProps {
  articleTitle: string;
  category: ReadingCategoryType;
  completedAt: string;
  timeSpentSeconds: number;
}

export interface ScoreSummaryCardProps {
  overallScore: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  totalQuestions: number;
  isCompleted: boolean;
}

export interface ResultPassageViewerProps {
  title: string;
  content: string;
  highlightEvidence?: QuestionEvidence;
}

export interface QuestionReviewItemProps {
  review: ReadingQuestionReviewDetail;
  isSelected: boolean;
  onSelectReviewItem: (questionId: string) => void;
}

export interface QuestionReviewListProps {
  reviews: ReadingQuestionReviewDetail[];
  selectedQuestionId?: string;
  onSelectQuestion: (questionId: string) => void;
}

export interface ResultActionFooterProps {
  onReplayArticle: () => void;
  onNextArticle?: () => void;
  onBackToArticleList: () => void;
}

// --- Shared UI Components ---

export interface BreadcrumbProps {
  items: { label: string; path?: string }[];
}

export interface LevelBadgeProps {
  level: ProficiencyLevel;
}

export interface StatusBadgeProps {
  status: ArticleCompletionStatus;
}

export interface DurationBadgeProps {
  minutes: number;
}

export interface ScoreBadgeProps {
  score: number;
  maxScore?: number;
}

export interface ResultStatItemProps {
  label: string;
  value: string | number;
  variant?: 'success' | 'danger' | 'warning' | 'info';
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

export interface ConfirmExitModalProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}
```
