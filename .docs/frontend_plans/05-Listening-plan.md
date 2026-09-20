# 05-Listening-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **ListeningCategoryPage [SMART]**: Quản lý và điều phối dữ liệu trang danh mục luyện nghe (IELTS, TOEIC, Nền tảng, Công việc), theo dõi tổng quan tiến độ các kỹ năng nghe và điều hướng sang danh sách bài nghe chi tiết.
  * **ListeningCategoryHeader [DUMB]**: Tiêu đề trang "Luyện nghe tiếng Anh", mô tả ngắn định hướng luyện nghe theo chuẩn đề thi và ứng dụng thực tế.
  * **ListeningCategoryList [DUMB]**: Danh sách hiển thị theo chiều dọc 4 danh mục luyện nghe chính.
    * **ListeningCategoryCard [DUMB]**: Thẻ đại diện từng danh mục (biểu tượng đại diện, tên danh mục, mô tả ngắn mục tiêu, số lượng bài luyện hiện có và nút CTA `Xem thêm`).
  * **ListeningCategorySkeleton [DUMB]**: *(Shared UI)* Hiệu ứng khung xương tải dữ liệu cho danh mục luyện nghe.
  * **CategoryEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi không tải được danh mục bài nghe kèm nút tải lại.

* **ListeningLessonListPage [SMART]**: Điều phối dữ liệu danh sách bài nghe theo danh mục đã chọn, xử lý logic tìm kiếm bài nghe theo tên, bộ lọc theo chủ đề và trình độ, xử lý phân trang hoặc cuộn vô tận.
  * **LessonListHeader [DUMB]**: Khối tiêu đề danh mục đang chọn, breadcrumb dẫn đường và bộ đếm tổng số bài luyện tìm thấy.
    * **Breadcrumb [DUMB]**: *(Shared UI)* Thanh dẫn đường phân cấp (`Trang chủ > Luyện nghe > IELTS`).
  * **LessonFilterBar [DUMB]**: Thanh công cụ tìm kiếm và lọc bài nghe.
    * **LessonSearchBar [DUMB]**: *(Shared UI)* Ô nhập từ khóa tìm kiếm tên bài nghe có debounce và nút xóa nhanh nội dung tìm kiếm.
    * **TopicFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo chủ đề (Học thuật, Đời sống thường nhật, Kinh doanh, Khoa học công nghệ...).
    * **LevelFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo trình độ (A1 - C2 / IELTS 5.0 - 8.5 / TOEIC 300 - 900).
    * **LessonStatusTabs [DUMB]**: *(Shared UI)* Thanh tab lọc nhanh trạng thái bài (`Tất cả`, `Chưa làm`, `Đang làm`, `Đã hoàn thành`).
  * **LessonGrid [DUMB]**: Lưới responsive hiển thị các thẻ bài nghe (1 cột mobile, 2-3 cột desktop).
    * **LessonCard [DUMB]**: Thẻ bài luyện nghe (tên bài, chủ đề, huy hiệu trình độ, thời lượng audio, số lượng câu hỏi, huy hiệu trạng thái làm bài và nút CTA `Bắt đầu làm`).
    * **LevelBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị cấp độ chuẩn hóa.
    * **StatusBadge [DUMB]**: *(Shared UI)* Huy hiệu trạng thái (`Chưa làm`, `Đang làm`, `Đã hoàn thành`).
    * **DurationBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị thời lượng audio (VD: `05:30`).
  * **PaginationControl [DUMB]**: *(Shared UI)* Thanh điều khiển phân trang hoặc nút `Tải thêm bài luyện`.
  * **LessonEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi không có bài nghe nào khớp với bộ lọc hoặc từ khóa tìm kiếm kèm nút `Xóa bộ lọc`.
  * **LessonListSkeleton [DUMB]**: *(Shared UI)* Khung xương tải trang danh sách bài luyện nghe.

* **ListeningPracticePage [SMART]**: Quản lý toàn bộ phiên làm bài luyện nghe: nạp dữ liệu bài làm và audio stream (chặn hiển thị transcript/đáp án), kiểm soát trình phát âm thanh, bộ đếm ngược thời gian, ghi nhận câu trả lời người học theo thời gian thực, tự động nộp bài khi hết giờ và điều hướng sang màn hình chấm điểm.
  * **ListeningPracticeHeader [DUMB]**: Thanh điều hướng trên cùng tối giản chống xao nhãng (nút thoát phiên có cảnh báo, tên bài nghe, chủ đề, đồng hồ đếm ngược và chỉ báo tiến độ số câu đã trả lời).
    * **CountdownTimer [DUMB]**: *(Shared UI)* Đồng hồ đếm ngược thời gian quy định, tự động chuyển màu cảnh báo khi thời gian còn dưới 2 phút.
    * **AnswerProgressIndicator [DUMB]**: Hiển thị tỷ lệ số câu đã trả lời trên tổng số câu hỏi (VD: `Đã trả lời: 8/10 câu`).
  * **ListeningWorkspaceLayout [DUMB]**: Bố cục làm việc chia 2 khu vực trực quan (Khu vực trên/cột cố định: Trình phát audio trung tâm; Khu vực dưới/cột cuộn: Danh sách câu hỏi).
    * **AudioPlayerSection [DUMB]**: Trình phát audio kích thước lớn, giao diện hiện đại, rõ ràng.
      * **AudioWaveformBar [DUMB]**: Thanh tiến trình thời lượng phát audio có thể kéo/tua mượt mà kèm mốc thời gian (`02:15 / 05:40`).
      * **AudioControlToolbar [DUMB]**: Cụm nút điều khiển phát nhạc: nút Phát/Tạm dừng (Play/Pause), tua lùi 5 giây, tua tới 5 giây, thanh trượt âm lượng và nút chọn tốc độ phát (`0.75x`, `1.0x`, `1.25x`, `1.5x`).
    * **QuestionListSection [DUMB]**: Danh sách hiển thị các câu hỏi được đánh số thứ tự rõ ràng, hỗ trợ nhảy nhanh đến từng câu hỏi.
      * **QuestionNavigationPills [DUMB]**: Thanh số thứ tự câu hỏi mini cho phép học viên theo dõi câu nào đã trả lời, câu nào chưa và cuộn nhanh đến câu đó.
      * **QuestionItemCard [DUMB]**: Khung câu hỏi đơn lẻ (số thứ tự, đề bài câu hỏi, loại câu hỏi trắc nghiệm hoặc điền từ).
        * **MultipleChoiceQuestion [DUMB]**: Danh sách các phương án lựa chọn A, B, C, D với radio selection trực quan.
        * **FillBlankQuestion [DUMB]**: Ô nhập liệu văn bản trả lời cho dạng câu hỏi điền từ vào chỗ trống.
  * **PracticeControlBar [DUMB]**: Thanh tác vụ chân trang cố định chứa nút CTA chính `Nộp bài để chấm điểm`.
  * **ConfirmSubmitIncompleteModal [DUMB]**: *(Shared UI)* Hộp thoại cảnh báo xác nhận khi người học nhấn nộp bài mà vẫn còn câu hỏi chưa hoàn thành.
  * **TimeUpSubmitModal [DUMB]**: *(Shared UI)* Hộp thoại thông báo đã hết thời gian làm bài quy định và hệ thống tự động lưu đáp án để nộp.
  * **ConfirmExitModal [DUMB]**: *(Shared UI)* Hộp thoại xác nhận thoát khỏi bài luyện nghe dở dang để bảo vệ tiến trình.

* **ListeningGradingStatusPage [SMART]**: Quản lý giai đoạn trung gian lưu bài và chấm điểm: gửi bài nộp, vô hiệu hóa việc chỉnh sửa đáp án, hiển thị hoạt ảnh loading thông báo bài làm đã được lưu an toàn, tự động chuyển tiếp sang màn hình kết quả sau khi chấm điểm xong.
  * **GradingStatusCard [DUMB]**: Thẻ thông báo trạng thái chấm bài trung tâm.
    * **AudioWaveGradingAnimation [DUMB]**: *(Shared UI)* Hoạt ảnh sóng âm đồ họa biểu thị hệ thống đang phân tích và đối chiếu đáp án.
    * **GradingProgressSteps [DUMB]**: Danh sách chỉ báo tiến độ chấm bài (`Đã lưu bài làm thành công` -> `Đang đối chiếu đáp án âm thanh` -> `Đang tổng hợp điểm số và phân tích transcript`).
    * **GradingNoticeBox [DUMB]**: Thông báo người học vui lòng không tải lại trang và cam kết bảo lưu điểm số.

* **ListeningResultPage [SMART]**: Quản lý và hiển thị toàn bộ kết quả bài luyện nghe theo `attemptId`: tải điểm tổng, số câu đúng/sai, đối chiếu chi tiết bài làm với đáp án chuẩn, mở khóa nội dung transcript toàn bài kèm audio nghe lại, và điều hướng các hành động tiếp theo.
  * **ResultHeroHeader [DUMB]**: Tiêu đề trang kết quả, tên bài nghe, danh mục, thời điểm hoàn thành và tổng thời gian làm bài thực tế.
  * **ScoreSummaryCard [DUMB]**: Thẻ tổng quan kết quả (điểm tổng, số câu đúng, số câu sai, số câu bỏ trống, tỷ lệ chính xác % và nhận xét ngắn đánh giá năng lực nghe).
    * **ScoreBadge [DUMB]**: *(Shared UI)* Huy hiệu điểm số nổi bật với màu sắc biểu thị mức độ hoàn thành.
    * **ResultStatItem [DUMB]**: Khối thống kê chỉ số thành phần (Số câu đúng/sai/thời gian làm).
  * **ResultWorkspaceLayout [DUMB]**: Bố cục đối chiếu kết quả linh hoạt (cho phép vừa nghe lại audio, vừa xem transcript và đối chiếu đáp án câu hỏi).
    * **ReviewAudioPlayer [DUMB]**: Trình phát audio thu gọn để người học nghe lại các phân đoạn nghe khó.
    * **TranscriptAccordionPanel [DUMB]**: Khung nội dung văn bản Audio Transcript (chỉ mở ở màn hình này), hỗ trợ tô sáng các câu/đoạn chứa dẫn chứng cho từng câu hỏi.
    * **QuestionReviewList [DUMB]**: Danh sách chi tiết toàn bộ câu hỏi đã làm bài.
      * **QuestionReviewItem [DUMB]**: Thẻ câu hỏi chi tiết bao gồm: nội dung câu hỏi, câu trả lời của người học, đáp án đúng của hệ thống, huy hiệu `ĐÚNG` / `SAI`, trích đoạn mốc thời gian (timestamp) trong audio liên quan và giải thích ngắn nguyên nhân/dẫn chứng.
  * **ResultActionFooter [DUMB]**: Thanh tác vụ chân trang kết quả chứa các nút điều hướng: `Nghe lại bài này`, `Bài tiếp theo` và `Quay về danh sách bài luyện`.
  * **ResultSkeleton [DUMB]**: *(Shared UI)* Hiệu ứng tải trang kết quả bài luyện nghe.

---

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Zustand`) — thông tin người dùng đang đăng nhập, lưu `userId`, phân quyền tài khoản để ghi nhận lịch sử học và tiến độ cá nhân.
* `globalAudioSettings`: **Global State** (`Zustand`) — thiết lập âm lượng mặc định (`volume`) và tốc độ phát ưa thích (`playbackRate`) của người dùng để áp dụng nhất quán giữa các bài nghe.
* `listeningCategories`: **Server State** (`RTK Query`) — danh sách 04 nhóm danh mục luyện nghe (IELTS, TOEIC, Nền tảng, Công việc) kèm số lượng bài luyện mỗi nhóm.
* `lessonsByCategory`: **Server State** (`RTK Query`) — danh sách bài luyện nghe thuộc danh mục đang chọn, hỗ trợ lọc theo chủ đề, trình độ, trạng thái và phân trang.
* `lessonPracticeDetail`: **Server State** (`RTK Query`) — dữ liệu bài luyện nghe phục vụ làm bài (tiêu đề, chủ đề, audioUrl, thời gian cho phép, danh sách câu hỏi và các phương án; tuyệt đối không kèm transcript và đáp án đúng).
* `attemptGradingStatus`: **Server State** (`RTK Query`) — trạng thái chấm bài từ backend (`SUBMITTED`, `GRADING`, `COMPLETED`), hỗ trợ cơ chế polling tự động mỗi 1 - 2 giây để chuyển trang kết quả.
* `listeningAttemptResult`: **Server State** (`RTK Query`) — toàn bộ dữ liệu kết quả chấm bài (điểm số, số câu đúng/sai, đáp án học viên, đáp án đúng, giải thích chi tiết, transcript âm thanh và gợi ý bài học tiếp theo).
* `isLoading`, `isFetching`, `isError`: **Server State** (`RTK Query`) — trạng thái gọi API tự động từ RTK Query hook, không tạo state trùng lặp trong component.
* `category`: **URL Query Parameter** (`?category=ielts`) — định danh danh mục đang xem trên trang danh sách bài luyện.
* `topic`: **URL Query Parameter** (`?topic=education`) — chủ đề bài nghe đang chọn để lọc.
* `level`: **URL Query Parameter** (`?level=b2`) — cấp độ trình độ lọc trên trang danh sách.
* `status`: **URL Query Parameter** (`?status=not_started`) — trạng thái bài luyện cần lọc (`all`, `not_started`, `in_progress`, `completed`).
* `search`: **URL Query Parameter** (`?search=conversation+at+airport`) — từ khóa tìm kiếm tên bài nghe, giúp duy trì trạng thái khi chia sẻ liên kết hoặc reload.
* `page`: **URL Query Parameter** (`?page=1`) — số trang hiện tại của danh sách bài luyện nghe.
* `resultTab`: **URL Query Parameter** (`?tab=questions` hoặc `?tab=transcript`) — tab hiển thị đối chiếu trên màn hình kết quả bài nghe.
* `userAnswers`: **Local State** (`useState`) — bảng lưu trữ câu trả lời của người học theo dạng key-value (`Record<string, string>`, trong đó key là `questionId`, value là đáp án đã chọn hoặc chuỗi điền).
* `timeRemainingSeconds`: **Local State** (`useState`) — số giây đếm ngược còn lại của phiên làm bài nghe.
* `audioCurrentTime`: **Local State** (`useState`) — thời điểm phát hiện tại của audio (tính bằng giây).
* `audioDuration`: **Local State** (`useState`) — tổng thời lượng của tệp audio (tính bằng giây).
* `isPlaying`: **Local State** (`useState`) — trạng thái audio đang phát hoặc tạm dừng (`boolean`).
* `playbackSpeed`: **Local State** (`useState`) — tốc độ phát âm thanh được chọn trong phiên (`0.75 | 1.0 | 1.25 | 1.5`).
* `audioVolume`: **Local State** (`useState`) — mức âm lượng của audio player (`0` đến `1`).
* `isConfirmSubmitOpen`: **Local State** (`useState`) — điều khiển hiển thị modal xác nhận nộp bài khi vẫn còn câu chưa trả lời.
* `isTimeUpModalOpen`: **Local State** (`useState`) — điều khiển hiển thị modal thông báo hết giờ làm bài.
* `isConfirmExitOpen`: **Local State** (`useState`) — điều khiển hiển thị modal xác nhận thoát phòng luyện nghe khi đang làm bài.
* `activeQuestionId`: **Local State** (`useState`) — ID câu hỏi đang được focus hoặc cuộn tới trong phiên làm bài.
* `isTranscriptExpanded`: **Local State** (`useState`) — đóng/mở khối hiển thị Audio Transcript trên trang kết quả.

---

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
// ==================== ENUMS & LITERAL TYPES ====================

export type ListeningCategoryType = 'IELTS' | 'TOEIC' | 'FOUNDATION' | 'WORK';

export type ProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type LessonCompletionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export type QuestionType = 'MULTIPLE_CHOICE' | 'FILL_IN_BLANK';

export type AttemptGradingStatus = 'PENDING' | 'GRADING' | 'COMPLETED' | 'FAILED';

export type PlaybackSpeed = 0.75 | 1.0 | 1.25 | 1.5 | 2.0;

// ==================== CORE ENTITY MODELS ====================

export interface ListeningCategoryItem {
  id: string;
  type: ListeningCategoryType;
  title: string;
  description: string;
  totalLessons: number;
  iconName: string;
}

export interface ListeningLessonSummary {
  id: string;
  category: ListeningCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  durationSeconds: number;
  totalQuestions: number;
  status: LessonCompletionStatus;
  bestScore?: number;
}

export interface QuestionOption {
  key: 'A' | 'B' | 'C' | 'D';
  content: string;
}

export interface ListeningPracticeQuestion {
  id: string;
  orderNumber: number;
  type: QuestionType;
  prompt: string;
  options?: QuestionOption[];
}

export interface ListeningPracticeDetail {
  id: string;
  category: ListeningCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  audioUrl: string;
  durationMinutes: number;
  totalQuestions: number;
  questions: ListeningPracticeQuestion[];
}

export interface UserAnswerItem {
  questionId: string;
  answer: string;
}

export interface SubmitListeningAttemptPayload {
  lessonId: string;
  timeSpentSeconds: number;
  answers: UserAnswerItem[];
}

export interface QuestionReviewDetail {
  questionId: string;
  orderNumber: number;
  type: QuestionType;
  prompt: string;
  options?: QuestionOption[];
  userAnswer: string;
  correctAnswer: string;
  isCorrect: boolean;
  explanation: string;
  audioTimestamp?: number;
}

export interface ListeningAttemptResult {
  attemptId: string;
  lessonId: string;
  lessonTitle: string;
  category: ListeningCategoryType;
  completedAt: string;
  timeSpentSeconds: number;
  totalQuestions: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  overallScore: number;
  generalComment: string;
  audioUrl: string;
  transcript: string;
  questionReviews: QuestionReviewDetail[];
  nextLessonId?: string;
}

// ==================== DUMB COMPONENT PROPS ====================

// --- Màn hình 1: Danh mục luyện nghe ---

export interface ListeningCategoryHeaderProps {
  title: string;
  description: string;
}

export interface ListeningCategoryCardProps {
  category: ListeningCategoryItem;
  onSelectCategory: (categoryType: ListeningCategoryType) => void;
}

export interface ListeningCategoryListProps {
  categories: ListeningCategoryItem[];
  onSelectCategory: (categoryType: ListeningCategoryType) => void;
}

// --- Màn hình 2: Danh sách bài luyện theo danh mục ---

export interface LessonListHeaderProps {
  categoryTitle: string;
  totalLessons: number;
  breadcrumbItems: { label: string; path?: string }[];
}

export interface LessonFilterBarProps {
  searchQuery: string;
  selectedTopic: string;
  selectedLevel: string;
  selectedStatus: string;
  topicOptions: { value: string; label: string }[];
  levelOptions: { value: string; label: string }[];
  onSearchChange: (value: string) => void;
  onTopicChange: (topic: string) => void;
  onLevelChange: (level: string) => void;
  onStatusChange: (status: string) => void;
  onResetFilters: () => void;
}

export interface LessonCardProps {
  lesson: ListeningLessonSummary;
  onStartLesson: (lessonId: string) => void;
}

export interface LessonGridProps {
  lessons: ListeningLessonSummary[];
  onStartLesson: (lessonId: string) => void;
}

// --- Màn hình 3: Làm bài luyện nghe ---

export interface CountdownTimerProps {
  remainingSeconds: number;
  urgentThresholdSeconds?: number;
}

export interface AnswerProgressIndicatorProps {
  answeredCount: number;
  totalQuestions: number;
}

export interface ListeningPracticeHeaderProps {
  lessonTitle: string;
  topic: string;
  remainingSeconds: number;
  answeredCount: number;
  totalQuestions: number;
  onExitClick: () => void;
}

export interface AudioWaveformBarProps {
  currentTime: number;
  duration: number;
  onSeek: (targetTime: number) => void;
}

export interface AudioControlToolbarProps {
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  volume: number;
  playbackSpeed: PlaybackSpeed;
  onTogglePlay: () => void;
  onRewind: (seconds: number) => void;
  onForward: (seconds: number) => void;
  onChangeSpeed: (speed: PlaybackSpeed) => void;
  onChangeVolume: (volume: number) => void;
}

export interface AudioPlayerSectionProps {
  audioUrl: string;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  volume: number;
  playbackSpeed: PlaybackSpeed;
  onTogglePlay: () => void;
  onSeek: (targetTime: number) => void;
  onRewind: (seconds: number) => void;
  onForward: (seconds: number) => void;
  onChangeSpeed: (speed: PlaybackSpeed) => void;
  onChangeVolume: (volume: number) => void;
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
  question: ListeningPracticeQuestion;
  currentAnswer: string;
  isActive: boolean;
  onAnswerChange: (questionId: string, answer: string) => void;
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

export interface AudioWaveGradingAnimationProps {
  statusText?: string;
}

export interface GradingProgressStepsProps {
  currentStepIndex: number;
  steps: { label: string; isCompleted: boolean; isProcessing: boolean }[];
}

export interface GradingStatusCardProps {
  lessonTitle: string;
  currentStepIndex: number;
  steps: { label: string; isCompleted: boolean; isProcessing: boolean }[];
}

// --- Màn hình 5: Kết quả bài luyện nghe ---

export interface ResultHeroHeaderProps {
  lessonTitle: string;
  category: ListeningCategoryType;
  completedAt: string;
  timeSpentSeconds: number;
}

export interface ScoreSummaryCardProps {
  overallScore: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  totalQuestions: number;
  generalComment: string;
}

export interface ReviewAudioPlayerProps {
  audioUrl: string;
  initialSeekTime?: number;
}

export interface TranscriptAccordionPanelProps {
  transcript: string;
  isExpanded: boolean;
  activeTimestamp?: number;
  onToggleExpand: () => void;
}

export interface QuestionReviewItemProps {
  review: QuestionReviewDetail;
  onJumpToAudioTimestamp?: (timestamp: number) => void;
}

export interface QuestionReviewListProps {
  reviews: QuestionReviewDetail[];
  onPlayTimestamp?: (timestamp: number) => void;
}

export interface ResultActionFooterProps {
  onReplayLesson: () => void;
  onNextLesson?: () => void;
  onBackToLessonList: () => void;
}

// --- Shared UI Components ---

export interface BreadcrumbProps {
  items: { label: string; path?: string }[];
}

export interface LevelBadgeProps {
  level: ProficiencyLevel;
}

export interface StatusBadgeProps {
  status: LessonCompletionStatus;
}

export interface DurationBadgeProps {
  seconds: number;
}

export interface ScoreBadgeProps {
  score: number;
  maxScore?: number;
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
