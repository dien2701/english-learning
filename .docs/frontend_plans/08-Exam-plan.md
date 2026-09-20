# 08-Exam-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **ExamCategoryPage [SMART]**: Quản lý và điều phối dữ liệu danh mục bài kiểm tra (IELTS, TOEIC, Nền tảng, Công việc), nạp dữ liệu danh mục, theo dõi tóm tắt tiến độ người học (bài làm dở, điểm gần nhất, tổng số bài hoàn thành) và điều hướng sang trang danh sách bài kiểm tra.
  * **ExamCategoryHeader [DUMB]**: Tiêu đề trang "Bài kiểm tra tiếng Anh", mô tả ngắn định hướng kiểm tra kiến thức theo chuẩn quốc tế và mục tiêu công việc.
  * **ExamProgressSummaryBanner [DUMB]**: *(Shared UI)* Khung tóm tắt tiến độ học viên (tổng bài đã hoàn thành, điểm số bài gần nhất, bài đang làm dở).
    * **ExamResumeCard [DUMB]**: Thẻ hiển thị bài kiểm tra đang làm dở (tên bài, thời gian còn lại, tiến độ câu đã làm, nút CTA `Tiếp tục làm`).
    * **ExamQuickStats [DUMB]**: Cụm số liệu thống kê nhanh (tổng số bài đã nộp, điểm trung bình gần đây).
  * **ExamCategoryList [DUMB]**: Danh sách hiển thị theo chiều dọc 04 danh mục bài kiểm tra chính (IELTS, TOEIC, Nền tảng, Công việc).
    * **ExamCategoryCard [DUMB]**: Thẻ đại diện từng danh mục (icon/ảnh minh họa, tên danh mục, mô tả ngắn, tổng số bài kiểm tra hiện có, nút CTA duy nhất `Xem thêm`).
  * **ExamCategorySkeleton [DUMB]**: *(Shared UI)* Khung xương tải dữ liệu cho trang danh mục bài kiểm tra.
  * **ExamCategoryEmptyState [DUMB]**: *(Shared UI)* Khung trạng thái rỗng khi chưa có danh mục bài kiểm tra.

* **ExamListPage [SMART]**: Điều phối dữ liệu danh sách bài kiểm tra thuộc danh mục đã chọn, xử lý logic tìm kiếm bài theo tên, bộ lọc đa tiêu chí (chủ đề, kỹ năng, cấp độ, thời lượng, trạng thái làm bài) và phân trang.
  * **ExamListHeader [DUMB]**: Khối tiêu đề danh mục đang chọn, mô tả ngắn, breadcrumb phân cấp và tổng số bài thi tìm thấy.
    * **Breadcrumb [DUMB]**: *(Shared UI)* Thanh dẫn đường phân cấp (`Trang chủ > Bài kiểm tra > IELTS`).
  * **ExamFilterBar [DUMB]**: Thanh công cụ tìm kiếm và lọc bài kiểm tra đa điều kiện.
    * **ExamSearchBar [DUMB]**: *(Shared UI)* Ô nhập từ khóa tìm kiếm tên bài kiểm tra hỗ trợ debounce và nút xóa nhanh nội dung tìm kiếm.
    * **TopicFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo chủ đề (Kinh doanh, Học thuật, Khoa học, Xã hội, Đời sống...).
    * **SkillFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo kỹ năng (Reading, Listening, Writing, Grammar & Vocabulary, Full Test).
    * **LevelFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo trình độ chuẩn hóa (A1 - C2, IELTS 4.0 - 8.5, TOEIC 250 - 990).
    * **DurationFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo thời lượng (15, 30, 45, 60, 90, 120 phút).
    * **ExamStatusTabs [DUMB]**: *(Shared UI)* Thanh tab lọc nhanh trạng thái bài (`Tất cả`, `Chưa làm`, `Đang làm`, `Đã hoàn thành`).
  * **ExamGrid [DUMB]**: Lưới responsive hiển thị các thẻ bài kiểm tra (1 cột mobile, 2-3 cột desktop).
    * **ExamCard [DUMB]**: Thẻ bài kiểm tra (tên bài, chủ đề, huy hiệu kỹ năng/cấp độ, số câu hỏi, thời gian làm bài, mô tả ngắn, trạng thái làm bài, nút CTA `Bắt đầu làm` hoặc `Tiếp tục làm`).
    * **LevelBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị cấp độ chuẩn hóa.
    * **SkillBadge [DUMB]**: *(Shared UI)* Huy hiệu biểu thị kỹ năng bài kiểm tra.
    * **DurationBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị thời lượng quy định (VD: `60 phút`).
    * **QuestionCountBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị số lượng câu hỏi (VD: `40 câu`).
    * **StatusBadge [DUMB]**: *(Shared UI)* Huy hiệu trạng thái (`Chưa làm`, `Đang làm`, `Đã hoàn thành`).
  * **PaginationControl [DUMB]**: *(Shared UI)* Thanh điều khiển phân trang hoặc nút tải thêm.
  * **ExamListEmptyState [DUMB]**: *(Shared UI)* Khung trạng thái rỗng khi không tìm thấy bài kiểm tra nào khớp với bộ lọc kèm nút đặt lại bộ lọc.
  * **ExamListSkeleton [DUMB]**: *(Shared UI)* Khung xương tải dữ liệu danh sách bài kiểm tra.

* **ExamTakingPage [SMART]**: Quản lý toàn bộ phiên làm bài kiểm tra: tải nội dung đề thi (tuyệt đối không nhận đáp án đúng trước khi nộp), điều phối đồng hồ đếm ngược, lưu trữ và đồng bộ câu trả lời cục bộ và lưu tạm lên server, quản lý đánh dấu câu xem lại, xử lý tự động nộp bài khi hết giờ và kích hoạt nộp bài chủ động với cảnh báo câu chưa làm.
  * **ExamTakingHeader [DUMB]**: Thanh điều hướng trên cùng tối giản chống xao nhãng (tên bài thi, nút thoát có xác nhận, trạng thái lưu tạm).
    * **ExamStickyTimerBar [DUMB]**: *(Shared UI)* Thanh đồng hồ đếm ngược cố định trên đỉnh màn hình khi cuộn trang kèm thanh tiến độ hoàn thành câu hỏi.
      * **CountdownTimer [DUMB]**: Đồng hồ đếm ngược trực quan với cảnh báo đổi màu khi còn dưới 5 phút.
      * **ExamProgressBar [DUMB]**: *(Shared UI)* Thanh hiển thị tỷ lệ số câu đã trả lời trên tổng số câu.
    * **SaveDraftIndicator [DUMB]**: Chỉ báo thời điểm lưu tạm bài làm gần nhất hoặc trạng thái đang đồng bộ.
  * **ExamTakingLayout [DUMB]**: Bố cục phòng thi chia làm 2 khu vực: vùng nội dung câu hỏi chính và thanh điều hướng danh sách câu (Sidebar).
    * **ExamQuestionArea [DUMB]**: Khu vực hiển thị danh sách hoặc từng câu hỏi kèm hướng dẫn làm bài.
      * **ExamInstructionBox [DUMB]**: Hướng dẫn làm bài tổng quan của đề thi hoặc từng phần thi (Section).
      * **QuestionItemCard [DUMB]**: Thẻ bao đóng từng câu hỏi (số thứ tự câu, điểm số câu, nút gắn cờ xem lại `Flag for review`, nội dung câu hỏi).
        * **QuestionPromptDisplay [DUMB]**: Văn bản câu hỏi, đoạn văn đọc hiểu (Reading Passage) hoặc trình phát audio nghe (Listening Audio Player).
        * **SingleChoiceQuestion [DUMB]**: Giao diện chọn một đáp án duy nhất (Radio group).
        * **MultipleChoiceQuestion [DUMB]**: Giao diện chọn nhiều đáp án (Checkbox group).
        * **FillBlankQuestion [DUMB]**: Giao diện điền từ vào chỗ trống trong đoạn văn hoặc ô nhập liệu ngắn.
        * **EssayQuestion [DUMB]**: Giao diện viết đoạn văn/bài luận tự luận kèm bộ đếm số từ trực tiếp (Word counter).
    * **ExamSidebarNavigation [DUMB]**: Thanh điều hướng danh sách câu hỏi cố định bên phải (desktop) hoặc dạng ngăn kéo Drawer (mobile).
      * **QuestionNavPalette [DUMB]**: Lưới ma trận các nút số thứ tự câu hỏi biểu diễn trạng thái trực quan (Đã làm, Chưa làm, Đang xem, Đánh dấu xem lại) cho phép nhảy nhanh đến câu đó.
        * **QuestionNavButton [DUMB]**: Nút bấm câu hỏi đơn lẻ kèm hiệu ứng màu sắc theo trạng thái.
      * **ExamPaletteLegend [DUMB]**: Bảng chú giải ý nghĩa các màu sắc biểu thị trạng thái câu.
      * **ExamQuickActionBar [DUMB]**: Khối cụm nút thao tác nhanh: nút `Lưu tạm bài làm` và nút `Nộp bài để chấm điểm`.
  * **ConfirmSubmitExamModal [DUMB]**: *(Shared UI)* Hộp thoại cảnh báo trước khi nộp bài (thống kê rõ số câu đã làm, số câu chưa trả lời, cảnh báo không thể sửa lại sau khi nộp).
  * **ConfirmExitExamModal [DUMB]**: *(Shared UI)* Hộp thoại xác nhận khi người dùng muốn rời phòng thi (cảnh báo lưu tạm hoặc hủy phiên làm bài).
  * **ExamTakingSkeleton [DUMB]**: *(Shared UI)* Khung xương tải đề thi.

* **ExamGradingPage [SMART]**: Quản lý trạng thái trung gian sau khi người dùng nộp bài hoặc hết thời gian làm bài, hiển thị thông báo đã lưu bài an toàn, quản lý polling hoặc WebSocket chờ kết quả chấm trắc nghiệm và đánh giá AI cho phần tự luận, sau đó tự động điều hướng sang màn hình kết quả.
  * **ExamGradingLayout [DUMB]**: Khung bố cục trung tâm tối giản, trang trọng, không gây bối rối cho người học.
    * **GradingStatusCard [DUMB]**: Thẻ trạng thái trung tâm thông báo bài nộp đã được hệ thống ghi nhận thành công.
      * **GradingSpinnerAnimation [DUMB]**: *(Shared UI)* Hoạt ảnh vòng tròn xoay hoặc đồng hồ cát mượt mà, đơn giản.
      * **GradingStatusTitle [DUMB]**: Tiêu đề trạng thái rõ ràng "Đang chấm bài thi...".
      * **GradingStatusMessage [DUMB]**: Đoạn thông điệp ngắn giải thích hệ thống đang xử lý và tổng hợp điểm số, yêu cầu người dùng không tắt trình duyệt.
      * **GradingTypeHint [DUMB]**: Thông báo phân biệt loại bài thi (Trắc nghiệm tự động chấm ngay hoặc bài thi có câu tự luận đang được AI phân tích chi tiết).

* **ExamResultPage [SMART]**: Quản lý và trình bày kết quả chi tiết bài kiểm tra theo `attemptId`: tổng điểm, tỷ lệ câu đúng, đối chiếu đáp án từng câu với đáp án đúng và lời giải thích, phân tích các điểm cần cải thiện, đề xuất lộ trình rèn luyện và điều hướng làm lại bài.
  * **ExamResultHeader [DUMB]**: Tiêu đề trang kết quả, tên bài kiểm tra, danh mục, thời điểm hoàn thành và tổng thời gian làm bài thực tế.
    * **Breadcrumb [DUMB]**: *(Shared UI)* Thanh dẫn đường phân cấp (`Trang chủ > Bài kiểm tra > Kết quả bài thi`).
  * **OverallScoreSummaryCard [DUMB]**: Thẻ tóm tắt kết quả tổng quan.
    * **ExamScoreBadge [DUMB]**: *(Shared UI)* Huy hiệu điểm số tổng nổi bật với màu sắc biểu thị xếp loại (Đạt/Không đạt, Thang điểm chuẩn IELTS Band / TOEIC Score / 100 điểm).
    * **ScoreBreakdownStats [DUMB]**: Cụm số liệu chi tiết: số câu đúng / tổng số câu, số câu sai, số câu bỏ qua, tỷ lệ phần trăm độ chính xác.
    * **ExamResultGeneralFeedback [DUMB]**: Đánh giá tổng quan ngắn gọn về năng lực bài làm.
  * **AreasForImprovementSection [DUMB]**: Khối phân tích các vùng kiến thức và kỹ năng cần cải thiện.
    * **WeakTopicsCard [DUMB]**: Danh sách các chủ đề hoặc dạng bài người học làm sai nhiều nhất.
    * **RecommendedNextStepsCard [DUMB]**: Gợi ý các bộ thẻ từ vựng hoặc bài học ngữ pháp liên quan để ôn luyện bù đắp lỗ hổng kiến thức.
  * **DetailedQuestionReviewSection [DUMB]**: Danh sách đối chiếu và giải thích chi tiết từng câu hỏi trong đề thi.
    * **QuestionReviewFilterTabs [DUMB]**: Thanh tab lọc câu hỏi hiển thị (`Tất cả`, `Câu đúng`, `Câu sai`, `Chưa trả lời`).
    * **QuestionReviewItemCard [DUMB]**: Thẻ chi tiết cho từng câu hỏi sau khi chấm:
      * **QuestionReviewHeader [DUMB]**: Thứ tự câu, kỹ năng/chủ đề, điểm số đạt được, huy hiệu trạng thái `Đúng` / `Sai` / `Chưa làm`.
      * **QuestionPromptReview [DUMB]**: Đề bài gốc và tài liệu kèm theo (đoạn văn đọc hiểu hoặc file nghe).
      * **UserAnswerReviewBox [DUMB]**: Hiển thị đáp án người dùng đã chọn/nhập kèm màu sắc trực quan (Xanh lá nếu đúng, Đỏ gạch nếu sai).
      * **CorrectAnswerBox [DUMB]**: Hiển thị đáp án đúng chính thức từ hệ thống.
      * **AnswerExplanationBox [DUMB]**: Lời giải thích cặn kẽ, dẫn chứng từ vựng/ngữ pháp hoặc vị trí đoạn văn chứa thông tin trả lời.
  * **ExamResultActionFooter [DUMB]**: Cụm nút chân trang điều hướng tiếp theo:
    * Nút CTA chính `Làm lại bài này` (Retake Exam).
    * Nút phụ `Chọn bài kiểm tra khác` (Choose another exam).
    * Nút `Về trang Bài kiểm tra` (Back to Exam Categories).
  * **ExamResultSkeleton [DUMB]**: *(Shared UI)* Khung xương tải trang kết quả.

---

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Redux Toolkit`) — thông tin người dùng đang đăng nhập (`userId`, họ tên, email, mục tiêu học tập) phục vụ định danh học viên, phân quyền và lưu trữ lịch sử thi.
* `examDraftProgress`: **Global State** (`Redux Toolkit`) — lưu trữ tạm thời tiến độ làm bài thi dở dang trên máy khách (theo từng `examId`) phòng trường hợp người dùng vô tình tải lại trang hoặc ngắt kết nối mạng đột ngột.
* `examCategories`: **Server State** (`RTK Query`) — danh sách các danh mục bài kiểm tra (IELTS, TOEIC, Nền tảng, Công việc) kèm số lượng bài thi hiện có.
* `examProgressSummary`: **Server State** (`RTK Query`) — thông tin bài kiểm tra đang làm dở gần nhất và tóm tắt tiến độ làm bài của học viên.
* `examsByCategory`: **Server State** (`RTK Query`) — danh sách bài kiểm tra theo danh mục, tự động đồng bộ theo bộ lọc tìm kiếm, kỹ năng, cấp độ, thời lượng và phân trang.
* `examDetail`: **Server State** (`RTK Query`) — nội dung chi tiết đề kiểm tra theo `examId` (tiêu đề, cấu trúc các phần thi, danh sách câu hỏi, dạng câu, thời lượng quy định; TUYỆT ĐỐI KHÔNG chứa đáp án đúng trước khi nộp theo đúng quy tắc bảo mật hệ thống).
* `examSubmissionStatus`: **Server State** (`RTK Query`) — trạng thái chấm điểm của bài thi theo `attemptId` (`GRADING` hoặc `COMPLETED`), sử dụng cơ chế polling hoặc trigger khi chuyển màn hình.
* `examAttemptResult`: **Server State** (`RTK Query`) — toàn bộ dữ liệu kết quả chấm chi tiết theo `attemptId` (tổng điểm, số câu đúng/sai, đáp án đúng của từng câu, giải thích chi tiết, tổng hợp nội dung cần cải thiện).
* `isLoading`, `isFetching`, `isError`: **Server State** (`RTK Query`) — các cờ trạng thái xử lý mạng từ các hook RTK Query, không tạo state cục bộ dư thừa.
* `category`: **URL Query Parameter** (`?category=ielts`) — danh mục bài kiểm tra đang chọn (`ielts`, `toeic`, `foundation`, `work`).
* `search`: **URL Query Parameter** (`?search=cambridge`) — từ khóa tìm kiếm tên bài kiểm tra, hỗ trợ lưu lịch sử duyệt và chia sẻ liên kết.
* `topic`: **URL Query Parameter** (`?topic=business`) — chủ đề cần lọc (`academic`, `business`, `technology`, `daily_life`...).
* `skill`: **URL Query Parameter** (`?skill=reading`) — kỹ năng kiểm tra cần lọc (`all`, `reading`, `listening`, `writing`, `grammar`).
* `level`: **URL Query Parameter** (`?level=b2`) — cấp độ trình độ cần lọc (`a1`, `a2`, `b1`, `b2`, `c1`, `c2`).
* `duration`: **URL Query Parameter** (`?duration=60`) — thời lượng làm bài cần lọc tính theo phút (`15`, `30`, `45`, `60`, `90`, `120`).
* `status`: **URL Query Parameter** (`?status=not_started`) — trạng thái bài kiểm tra (`all`, `not_started`, `in_progress`, `completed`).
* `page`: **URL Query Parameter** (`?page=1`) — chỉ số trang hiện tại của danh sách bài kiểm tra.
* `reviewFilter`: **URL Query Parameter** (`?filter=incorrect`) — bộ lọc xem lại câu hỏi ở trang kết quả (`all`, `correct`, `incorrect`, `skipped`).
* `answers`: **Local State** (`useState`) — bản đồ lưu trữ các câu trả lời của học viên trong phiên làm bài (`Record<string, UserAnswerValue>`), ánh xạ từ `questionId` sang giá trị trả lời (chuỗi string, mảng string[] hoặc nội dung tự luận).
* `flaggedQuestionIds`: **Local State** (`useState`) — danh sách mã định danh các câu hỏi được học viên đánh dấu cờ xem lại (`string[]`).
* `currentQuestionIndex`: **Local State** (`useState`) — chỉ số câu hỏi đang được tập trung (focus) hoặc hiển thị trên giao diện (bắt đầu từ 0 đến N-1).
* `remainingTimeSeconds`: **Local State** (`useState`) — số giây làm bài còn lại được đồng hồ đếm ngược cập nhật từng giây một.
* `isSavingDraft`: **Local State** (`useState`) — cờ trạng thái đang thực hiện gọi API lưu tạm bài làm lên máy chủ.
* `lastSavedAt`: **Local State** (`useState`) — chuỗi thời gian hoặc timestamp ghi nhận lần lưu tạm thành công gần nhất để hiển thị cho người học an tâm.
* `isAutoSubmitting`: **Local State** (`useState`) — cờ đánh dấu hệ thống đang tự động kích hoạt tiến trình nộp bài do hết thời gian làm bài.
* `isConfirmSubmitModalOpen`: **Local State** (`useState`) — điều khiển đóng/mở hộp thoại xác nhận nộp bài kiểm tra kèm cảnh báo các câu chưa làm.
* `isConfirmExitModalOpen`: **Local State** (`useState`) — điều khiển đóng/mở hộp thoại xác nhận khi người học nhấn thoát bài thi giữa chừng.

---

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
// ==================== ENUMS & LITERAL TYPES ====================

export type ExamCategoryType = 'IELTS' | 'TOEIC' | 'FOUNDATION' | 'WORK';

export type ExamProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type ExamSkillType = 'READING' | 'LISTENING' | 'WRITING' | 'GRAMMAR_VOCAB' | 'FULL_TEST';

export type ExamCompletionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'FILL_BLANK' | 'ESSAY';

export type GradingStatusType = 'GRADING' | 'COMPLETED' | 'FAILED';

export type ReviewFilterType = 'ALL' | 'CORRECT' | 'INCORRECT' | 'SKIPPED';

export type UserAnswerValue = string | string[];

// ==================== CORE ENTITY MODELS ====================

export interface ExamCategoryItem {
  id: string;
  type: ExamCategoryType;
  title: string;
  description: string;
  totalExams: number;
  iconName: string;
}

export interface ExamResumeItem {
  attemptId: string;
  examId: string;
  category: ExamCategoryType;
  title: string;
  topic: string;
  level: ExamProficiencyLevel;
  remainingSeconds: number;
  totalQuestions: number;
  answeredCount: number;
  lastAttemptAt: string;
}

export interface ExamSummaryItem {
  id: string;
  category: ExamCategoryType;
  title: string;
  topic: string;
  skill: ExamSkillType;
  level: ExamProficiencyLevel;
  durationMinutes: number;
  totalQuestions: number;
  status: ExamCompletionStatus;
  description: string;
  bestScore?: number;
}

export interface QuestionChoiceOption {
  id: string;
  label: string; // VD: 'A', 'B', 'C', 'D'
  text: string;
}

export interface ExamQuestionItem {
  id: string;
  orderIndex: number;
  sectionTitle?: string;
  questionType: QuestionType;
  passageText?: string;
  audioUrl?: string;
  promptText: string;
  points: number;
  options?: QuestionChoiceOption[]; // Dành cho SINGLE_CHOICE và MULTIPLE_CHOICE
  maxWords?: number; // Dành cho ESSAY
}

export interface ExamDetail {
  id: string;
  category: ExamCategoryType;
  title: string;
  topic: string;
  skill: ExamSkillType;
  level: ExamProficiencyLevel;
  durationMinutes: number;
  totalQuestions: number;
  instructions: string;
  questions: ExamQuestionItem[]; // TUYỆT ĐỐI không chứa đáp án đúng trước khi nộp
}

export interface SaveDraftPayload {
  examId: string;
  attemptId?: string;
  remainingSeconds: number;
  answers: Record<string, UserAnswerValue>;
  flaggedQuestionIds: string[];
}

export interface SubmitExamPayload {
  examId: string;
  attemptId?: string;
  timeSpentSeconds: number;
  answers: Record<string, UserAnswerValue>;
}

export interface QuestionResultReview {
  questionId: string;
  orderIndex: number;
  questionType: QuestionType;
  promptText: string;
  passageText?: string;
  audioUrl?: string;
  userAnswer: UserAnswerValue;
  correctAnswer: UserAnswerValue;
  isCorrect: boolean;
  earnedPoints: number;
  maxPoints: number;
  explanation: string;
}

export interface AreaForImprovementItem {
  topicOrSkill: string;
  incorrectCount: number;
  totalCount: number;
  recommendation: string;
}

export interface ExamResultDetail {
  attemptId: string;
  examId: string;
  examTitle: string;
  category: ExamCategoryType;
  skill: ExamSkillType;
  completedAt: string;
  timeSpentSeconds: number;
  totalScore: number;
  maxScore: number;
  correctAnswersCount: number;
  totalQuestionsCount: number;
  isPassed: boolean;
  generalComment: string;
  areasForImprovement: AreaForImprovementItem[];
  questionReviews: QuestionResultReview[];
}

// ==================== DUMB COMPONENT PROPS ====================

// --- Màn 1: Danh mục bài kiểm tra ---

export interface ExamCategoryHeaderProps {
  title: string;
  description: string;
}

export interface ExamResumeCardProps {
  resumeItem: ExamResumeItem;
  onResumeExam: (examId: string, attemptId: string) => void;
}

export interface ExamQuickStatsProps {
  completedExamsCount: number;
  averageScore?: number;
}

export interface ExamProgressSummaryBannerProps {
  resumeItem?: ExamResumeItem;
  completedExamsCount: number;
  averageScore?: number;
  onResumeExam: (examId: string, attemptId: string) => void;
}

export interface ExamCategoryCardProps {
  category: ExamCategoryItem;
  onSelectCategory: (categoryType: ExamCategoryType) => void;
}

export interface ExamCategoryListProps {
  categories: ExamCategoryItem[];
  onSelectCategory: (categoryType: ExamCategoryType) => void;
}

// --- Màn 2: Danh sách bài kiểm tra theo danh mục ---

export interface ExamListHeaderProps {
  categoryTitle: string;
  description: string;
  totalExams: number;
  breadcrumbItems: { label: string; path?: string }[];
}

export interface ExamFilterBarProps {
  searchQuery: string;
  selectedTopic: string;
  selectedSkill: string;
  selectedLevel: string;
  selectedDuration: string;
  selectedStatus: string;
  topicOptions: { value: string; label: string }[];
  skillOptions: { value: string; label: string }[];
  levelOptions: { value: string; label: string }[];
  durationOptions: { value: string; label: string }[];
  onSearchChange: (query: string) => void;
  onTopicChange: (topic: string) => void;
  onSkillChange: (skill: string) => void;
  onLevelChange: (level: string) => void;
  onDurationChange: (duration: string) => void;
  onStatusChange: (status: string) => void;
  onResetFilters: () => void;
}

export interface ExamCardProps {
  exam: ExamSummaryItem;
  onStartExam: (examId: string) => void;
  onResumeExam?: (examId: string) => void;
}

export interface ExamGridProps {
  exams: ExamSummaryItem[];
  onStartExam: (examId: string) => void;
  onResumeExam?: (examId: string) => void;
}

// --- Màn 3: Làm bài kiểm tra ---

export interface ExamStickyTimerBarProps {
  remainingSeconds: number;
  answeredCount: number;
  totalQuestions: number;
  isWarningTime: boolean;
}

export interface CountdownTimerProps {
  remainingSeconds: number;
  isWarning: boolean;
}

export interface SaveDraftIndicatorProps {
  isSaving: boolean;
  lastSavedAt?: string;
}

export interface ExamTakingHeaderProps {
  examTitle: string;
  remainingSeconds: number;
  answeredCount: number;
  totalQuestions: number;
  isSavingDraft: boolean;
  lastSavedAt?: string;
  onExitClick: () => void;
}

export interface ExamInstructionBoxProps {
  instructions: string;
}

export interface SingleChoiceQuestionProps {
  questionId: string;
  options: QuestionChoiceOption[];
  selectedValue?: string;
  onSelectOption: (questionId: string, optionId: string) => void;
}

export interface MultipleChoiceQuestionProps {
  questionId: string;
  options: QuestionChoiceOption[];
  selectedValues: string[];
  onToggleOption: (questionId: string, optionId: string) => void;
}

export interface FillBlankQuestionProps {
  questionId: string;
  value: string;
  onChangeValue: (questionId: string, text: string) => void;
}

export interface EssayQuestionProps {
  questionId: string;
  value: string;
  maxWords?: number;
  onChangeValue: (questionId: string, text: string) => void;
}

export interface QuestionPromptDisplayProps {
  promptText: string;
  passageText?: string;
  audioUrl?: string;
}

export interface QuestionItemCardProps {
  question: ExamQuestionItem;
  currentAnswer?: UserAnswerValue;
  isFlagged: boolean;
  onAnswerChange: (questionId: string, value: UserAnswerValue) => void;
  onToggleFlag: (questionId: string) => void;
}

export interface QuestionNavButtonProps {
  orderIndex: number;
  isCurrent: boolean;
  isAnswered: boolean;
  isFlagged: boolean;
  onClick: () => void;
}

export interface QuestionNavPaletteProps {
  questions: ExamQuestionItem[];
  currentIndex: number;
  answers: Record<string, UserAnswerValue>;
  flaggedQuestionIds: string[];
  onSelectQuestion: (index: number) => void;
}

export interface ExamPaletteLegendProps {
  answeredCount: number;
  unansweredCount: number;
  flaggedCount: number;
}

export interface ExamQuickActionBarProps {
  isSavingDraft: boolean;
  onSaveDraft: () => void;
  onSubmitExam: () => void;
}

export interface ExamSidebarNavigationProps {
  questions: ExamQuestionItem[];
  currentIndex: number;
  answers: Record<string, UserAnswerValue>;
  flaggedQuestionIds: string[];
  isSavingDraft: boolean;
  onSelectQuestion: (index: number) => void;
  onSaveDraft: () => void;
  onSubmitExam: () => void;
}

export interface ExamQuestionAreaProps {
  instructions: string;
  currentQuestion: ExamQuestionItem;
  currentAnswer?: UserAnswerValue;
  isFlagged: boolean;
  onAnswerChange: (questionId: string, value: UserAnswerValue) => void;
  onToggleFlag: (questionId: string) => void;
  onPreviousQuestion?: () => void;
  onNextQuestion?: () => void;
  isFirstQuestion: boolean;
  isLastQuestion: boolean;
}

export interface ExamTakingLayoutProps {
  questionArea: React.ReactNode;
  sidebarNavigation: React.ReactNode;
}

export interface ConfirmSubmitExamModalProps {
  isOpen: boolean;
  totalQuestions: number;
  answeredCount: number;
  unansweredCount: number;
  onConfirmSubmit: () => void;
  onClose: () => void;
}

export interface ConfirmExitExamModalProps {
  isOpen: boolean;
  onConfirmExit: () => void;
  onSaveAndExit: () => void;
  onClose: () => void;
}

// --- Màn 4: Đang chấm bài ---

export interface GradingSpinnerAnimationProps {
  size?: 'sm' | 'md' | 'lg';
}

export interface GradingStatusCardProps {
  title: string;
  message: string;
  hintText: string;
}

export interface ExamGradingLayoutProps {
  statusCard: React.ReactNode;
}

// --- Màn 5: Kết quả bài kiểm tra ---

export interface ExamResultHeaderProps {
  examTitle: string;
  category: ExamCategoryType;
  skill: ExamSkillType;
  completedAt: string;
  timeSpentSeconds: number;
}

export interface ScoreBreakdownStatsProps {
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  totalQuestions: number;
}

export interface OverallScoreSummaryCardProps {
  totalScore: number;
  maxScore: number;
  correctCount: number;
  totalQuestions: number;
  isPassed: boolean;
  generalComment: string;
}

export interface WeakTopicsCardProps {
  areas: AreaForImprovementItem[];
}

export interface RecommendedNextStepsCardProps {
  recommendations: string[];
}

export interface AreasForImprovementSectionProps {
  areas: AreaForImprovementItem[];
}

export interface QuestionReviewHeaderProps {
  orderIndex: number;
  isCorrect: boolean;
  earnedPoints: number;
  maxPoints: number;
}

export interface UserAnswerReviewBoxProps {
  userAnswer: UserAnswerValue;
  isCorrect: boolean;
}

export interface CorrectAnswerBoxProps {
  correctAnswer: UserAnswerValue;
}

export interface AnswerExplanationBoxProps {
  explanation: string;
}

export interface QuestionReviewItemCardProps {
  review: QuestionResultReview;
}

export interface QuestionReviewFilterTabsProps {
  activeFilter: ReviewFilterType;
  counts: {
    all: number;
    correct: number;
    incorrect: number;
    skipped: number;
  };
  onFilterChange: (filter: ReviewFilterType) => void;
}

export interface DetailedQuestionReviewSectionProps {
  questionReviews: QuestionResultReview[];
  activeFilter: ReviewFilterType;
  onFilterChange: (filter: ReviewFilterType) => void;
}

export interface ExamResultActionFooterProps {
  onRetakeExam: () => void;
  onChooseOtherExam: () => void;
  onBackToCategories: () => void;
}

// --- Shared UI Components ---

export interface BreadcrumbProps {
  items: { label: string; path?: string }[];
}

export interface LevelBadgeProps {
  level: ExamProficiencyLevel;
}

export interface SkillBadgeProps {
  skill: ExamSkillType;
}

export interface StatusBadgeProps {
  status: ExamCompletionStatus;
}

export interface DurationBadgeProps {
  minutes: number;
}

export interface QuestionCountBadgeProps {
  count: number;
}

export interface ExamScoreBadgeProps {
  score: number;
  maxScore: number;
  isPassed: boolean;
}

export interface ExamProgressBarProps {
  completed: number;
  total: number;
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
