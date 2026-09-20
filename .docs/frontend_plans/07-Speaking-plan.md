# 07-Speaking-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **SpeakingCategoryPage [SMART]**: Quản lý và điều phối dữ liệu trang danh mục luyện nói (IELTS, TOEIC, Nền tảng, Công việc), nạp danh mục, theo dõi danh sách bài học tiếp tục dở dang hoặc gợi ý, kiểm tra quyền truy cập microphone ban đầu và điều hướng.
  * **SpeakingCategoryHeader [DUMB]**: Tiêu đề trang "Luyện nói tiếng Anh", mô tả ngắn định hướng rèn luyện phát âm, độ trôi chảy và tự tin giao tiếp.
  * **MicrophonePermissionNotice [DUMB]**: *(Shared UI)* Khung thông báo hướng dẫn người học cấp quyền micro cho trình duyệt kèm chỉ báo trạng thái cấp quyền hiện tại.
  * **SpeakingResumeBanner [DUMB]**: Khối hiển thị bài luyện đang làm dở hoặc bài học được AI đề xuất cho học viên tiếp tục nhanh.
    * **ResumeCard [DUMB]**: Thẻ bài nói đang làm dở (tên bài, chủ đề, tiến độ hoàn thành số câu/đoạn, nút CTA `Tiếp tục luyện`).
  * **SpeakingCategoryList [DUMB]**: Danh sách hiển thị theo chiều dọc 04 danh mục luyện nói chính.
    * **SpeakingCategoryCard [DUMB]**: Thẻ đại diện từng danh mục (biểu tượng đại diện, tên danh mục, mô tả ngắn, số lượng bài luyện hiện có và nút CTA duy nhất `Xem thêm`).
  * **SpeakingCategorySkeleton [DUMB]**: *(Shared UI)* Khung xương tải dữ liệu cho trang danh mục luyện nói.
  * **SpeakingCategoryEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi không có danh mục khả dụng kèm nút tải lại.

* **SpeakingLessonListPage [SMART]**: Điều phối dữ liệu danh sách bài luyện nói thuộc danh mục đã chọn, xử lý logic tìm kiếm bài theo tên, bộ lọc đa tiêu chí (chủ đề, trình độ, thời lượng, trạng thái làm bài) và phân trang.
  * **SpeakingLessonListHeader [DUMB]**: Khối tiêu đề danh mục đang chọn, mô tả ngắn, breadcrumb dẫn đường và bộ đếm tổng số bài luyện tìm thấy.
    * **Breadcrumb [DUMB]**: *(Shared UI)* Thanh dẫn đường phân cấp (`Trang chủ > Luyện nói > Giao tiếp công việc`).
  * **SpeakingLessonFilterBar [DUMB]**: Thanh công cụ tìm kiếm và lọc bài luyện nói.
    * **LessonSearchBar [DUMB]**: *(Shared UI)* Ô nhập từ khóa tìm kiếm tên bài nói hỗ trợ debounce và nút xóa nhanh nội dung tìm kiếm.
    * **TopicFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo chủ đề (Học thuật, Phỏng vấn xin việc, Thuyết trình, Du lịch, Đàm phán...).
    * **LevelFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo trình độ (A1 - C2 / IELTS 5.0 - 8.5 / TOEIC Speaking).
    * **DurationFilterDropdown [DUMB]**: *(Shared UI)* Dropdown lọc theo thời lượng dự kiến (5, 10, 15, 20 phút).
    * **LessonStatusTabs [DUMB]**: *(Shared UI)* Thanh tab lọc nhanh trạng thái bài (`Tất cả`, `Chưa làm`, `Đang làm`, `Đã hoàn thành`).
  * **SpeakingLessonGrid [DUMB]**: Lưới responsive hiển thị các thẻ bài luyện nói (1 cột mobile, 2-3 cột desktop).
    * **SpeakingLessonCard [DUMB]**: Thẻ bài luyện nói (tên bài, chủ đề, huy hiệu trình độ, số câu/đoạn cần đọc, thời lượng dự kiến, trạng thái làm bài và nút CTA `Bắt đầu làm`).
    * **LevelBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị cấp độ chuẩn hóa.
    * **StatusBadge [DUMB]**: *(Shared UI)* Huy hiệu trạng thái (`Chưa làm`, `Đang làm`, `Đã hoàn thành`).
    * **DurationBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị thời lượng quy định (VD: `10 phút`).
    * **SentenceCountBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị số lượng câu/đoạn cần đọc (VD: `8 câu`).
  * **PaginationControl [DUMB]**: *(Shared UI)* Thanh điều khiển chuyển trang số hoặc nút `Tải thêm bài luyện`.
  * **SpeakingLessonEmptyState [DUMB]**: *(Shared UI)* Hiển thị khi không tìm thấy bài nói nào khớp với bộ lọc kèm nút `Đặt lại bộ lọc`.
  * **SpeakingLessonListSkeleton [DUMB]**: *(Shared UI)* Khung xương tải trang danh sách bài luyện nói.

* **SpeakingPracticePage [SMART]**: Quản lý toàn bộ phiên phòng luyện nói: kiểm soát quyền và luồng âm thanh micro (Web Audio & MediaRecorder API), nạp danh sách câu/đoạn văn cần đọc, quản lý vòng đời thu âm (Bắt đầu - Dừng - Ghi lại), gửi file âm thanh từng câu lên AI phân tích theo thời gian thực, hiển thị góp ý ngắn gọn sau mỗi lượt đọc, quản lý chuyển câu hoặc đọc lại, kích hoạt kết thúc bài luyện và điều hướng sang màn hình kết quả tổng quan.
  * **SpeakingPracticeHeader [DUMB]**: Thanh điều hướng trên cùng tối giản chống xao nhãng (nút thoát phòng có xác nhận, tên bài nói, chỉ báo tiến độ câu hiện tại và số câu còn lại).
    * **PracticeProgressBar [DUMB]**: *(Shared UI)* Thanh tiến độ thanh mảnh hiển thị tỷ lệ số câu đã hoàn thành trên tổng số câu (VD: `Câu 3/8`).
  * **SpeakingStudioLayout [DUMB]**: Bố cục phòng luyện nói trung tâm tập trung thị giác vào câu đọc và khu vực tương tác micro.
    * **SentencePromptCard [DUMB]**: Thẻ trung tâm hiển thị câu/đoạn văn cần đọc với cỡ chữ to rõ ràng, độ tương phản cao, hỗ trợ nút nghe phát âm mẫu bản xứ.
      * **NativeAudioButton [DUMB]**: Nút bấm nghe audio giọng đọc chuẩn bản xứ kèm hiệu ứng đang phát âm thanh.
      * **SentenceTextDisplay [DUMB]**: Nội dung câu chữ rõ ràng, phân đoạn chuẩn xác, khoảng cách dòng thoáng giúp người học tập trung đọc thành tiếng.
      * **PhoneticGuideHint [DUMB]**: Khung gợi ý phiên âm IPA hoặc đánh dấu trọng âm cho các từ vựng học thuật/từ khó trong câu.
    * **AudioRecordingController [DUMB]**: Khung điều khiển tương tác micro và thu âm.
      * **MicStatusIndicator [DUMB]**: Đèn chỉ báo trạng thái hoạt động của micro (`Sẵn sàng`, `Đang thu âm`, `Đang phân tích`, `Chưa cấp quyền`).
      * **LiveAudioWaveform [DUMB]**: *(Shared UI)* Hoạt ảnh sóng âm động phản hồi theo thời gian thực dựa trên âm lượng giọng nói thực tế của người học.
      * **RecordingTimer [DUMB]**: Bộ đếm thời gian ghi âm của lượt đọc hiện tại (VD: `00:08 / 00:30`).
      * **RecordingActionButtons [DUMB]**: Cụm nút thao tác tương tác thu âm:
        * Nút `Bắt đầu ghi âm` (Record Button - màu Primary nổi bật, biểu tượng Micro).
        * Nút `Dừng` (Stop Button - biểu tượng vuông đỏ khi đang thu âm).
        * Nút `Nghe lại bản thu` (Playback Button - nghe lại file âm thanh người học vừa đọc).
        * Nút `Đọc lại` (Re-record Button - biểu tượng xoay vòng để xóa bản thu hiện tại và ghi âm lại).
    * **RealtimeAiFeedbackCard [DUMB]**: Khung hiển thị góp ý ngắn gọn, súc tích từ AI ngay sau mỗi lượt đọc (tuyệt đối không hiển thị quá nhiều gây áp lực).
      * **SentenceScorePill [DUMB]**: Điểm số tổng quan lượt đọc của câu (thang điểm 100 hoặc Band score).
      * **CriteriaPillList [DUMB]**: Cụm 4 chỉ số nhanh (Phát âm, Độ trôi chảy, Từ vựng, Ngữ pháp) kèm màu sắc biểu thị.
      * **SentenceHighlightFeedback [DUMB]**: Hiển thị lại câu đọc với các từ được tô màu trực quan (Xanh lá: Phát âm tốt; Vàng: Ngữ điệu/trọng âm cần chỉnh; Đỏ: Phát âm sai/nuốt âm).
      * **ConciseTipBox [DUMB]**: Một lời khuyên ngắn gọn, tích cực mang tính hỗ trợ học tập (VD: *"Lưu ý bật rõ âm đuôi /s/ ở từ 'species' và duy trì nhịp thở đều hơn"*).
    * **PracticeFlowActionBar [DUMB]**: Thanh tác vụ điều hướng tiến trình câu:
      * Nút phụ `Thử lại câu này` (cho phép đọc lại để cải thiện điểm câu).
      * Nút CTA chính `Câu tiếp theo` (chuyển sang câu kế tiếp khi chưa hết câu).
      * Nút CTA chính `Hoàn thành bài luyện` (hiển thị ở câu cuối cùng để kết thúc bài và nộp tổng kết).
  * **AiAnalyzingModal [DUMB]**: *(Shared UI)* Lớp phủ / Hoạt ảnh chờ AI đang xử lý và phân tích âm thanh giọng đọc.
  * **MicPermissionDeniedModal [DUMB]**: *(Shared UI)* Hộp thoại hướng dẫn từng bước mở quyền truy cập micro trên trình duyệt kèm nút kiểm tra lại.
  * **AudioSilenceWarningModal [DUMB]**: *(Shared UI)* Hộp thoại cảnh báo khi micro không thu được âm thanh hoặc âm lượng quá nhỏ.
  * **ConfirmExitPracticeModal [DUMB]**: *(Shared UI)* Hộp thoại cảnh báo xác nhận khi người học muốn thoát phòng luyện nói dở dang.

* **SpeakingResultPage [SMART]**: Quản lý và hiển thị toàn bộ kết quả bài luyện nói và góp ý AI chi tiết theo `attemptId`: tải điểm tổng, phân tích 4 nhóm kỹ năng (Phát âm, Độ trôi chảy, Từ vựng, Ngữ pháp), tổng hợp danh sách câu và từ cần luyện lại kèm audio nghe đối chiếu, hiển thị khu vực điểm làm tốt và điểm cần cải thiện, điều hướng các hành động tiếp theo.
  * **SpeakingResultHeader [DUMB]**: Tiêu đề trang kết quả, tên bài nói, danh mục, thời điểm hoàn thành và tổng thời gian luyện tập thực tế.
  * **OverallScoreCard [DUMB]**: Thẻ tổng quan kết quả (điểm đánh giá tổng thể, nhận xét ngắn tích cực từ AI, tỷ lệ hoàn thành).
    * **SpeakingScoreBadge [DUMB]**: *(Shared UI)* Huy hiệu điểm số nổi bật với màu sắc biểu thị mức độ năng lực (Xanh: Xuất sắc, Vàng: Khá, Cam/Đỏ: Cần cải thiện).
    * **AiSummaryQuoteBox [DUMB]**: Khung trích dẫn lời nhận xét tổng quan mang tính khích lệ, tạo động lực từ AI.
  * **CriteriaEvaluationGrid [DUMB]**: Lưới hiển thị chi tiết 04 nhóm tiêu chí đánh giá cốt lõi:
    * **PronunciationScoreCard [DUMB]**: Đánh giá độ chuẩn xác phát âm từng âm vị, phụ âm cuối (ending sounds) và trọng âm từ.
    * **FluencyScoreCard [DUMB]**: Đánh giá tốc độ nói (WPM - Words Per Minute), độ trơn tru, quãng ngập ngừng và nhịp điệu.
    * **VocabularyScoreCard [DUMB]**: Đánh giá khả năng sử dụng từ ngữ chính xác, phong phú và phát âm chuẩn các thuật ngữ theo ngữ cảnh.
    * **GrammarScoreCard [DUMB]**: Đánh giá trật tự từ, độ liền mạch ngữ pháp và biến đổi hình thái từ khi nói.
  * **StrengthWeaknessSection [DUMB]**: Bố cục 2 cột phân tích điểm mạnh và điểm cần cải thiện:
    * **StrengthsCard [DUMB]**: Khu vực "Điểm làm tốt" liệt kê các ưu điểm nổi bật (tạo cảm giác tự tin và hứng thú học tập).
    * **ImprovementsCard [DUMB]**: Khu vực "Cần cải thiện" liệt kê các lỗi phát âm/ngữ điệu lặp lại và định hướng khắc phục cụ thể.
  * **DetailedSentenceReviewSection [DUMB]**: Danh sách đối chiếu chi tiết từng câu trong bài luyện nói.
    * **SentenceReviewCard [DUMB]**: Thẻ đánh giá chi tiết cho từng câu đơn lẻ:
      * **SentenceReviewHeader [DUMB]**: Thứ tự câu, điểm số câu và thời lượng ghi âm.
      * **ColoredSentenceText [DUMB]**: Văn bản câu với màu sắc trực quan cho từng từ tương ứng với mức độ phát âm chuẩn xác.
      * **AudioCompareToolbar [DUMB]**: Cụm thanh nghe đối chiếu song song:
        * Nút phát bản thu âm của học viên (`Bản thu của bạn`).
        * Nút phát bản thu âm chuẩn của người bản xứ (`Giọng đọc mẫu`).
      * **MispronouncedWordsBox [DUMB]**: Danh sách các từ người học phát âm chưa chuẩn, kèm phiên âm IPA đúng, lỗi sai thường gặp và nút bấm phát âm riêng từ đó.
      * **SentenceImprovementTip [DUMB]**: Lời khuyên cải thiện ngắn gọn, dễ thực hiện cho câu đó.
  * **SpeakingResultActionFooter [DUMB]**: Thanh tác vụ chân trang kết quả chứa các nút điều hướng:
    * Nút CTA chính `Luyện lại bài này` (Primary Button).
    * Nút `Chọn bài luyện khác` (Secondary Button).
    * Nút `Về Dashboard` (Subtle/Link Button).
  * **SpeakingResultSkeleton [DUMB]**: *(Shared UI)* Khung xương tải trang kết quả bài luyện nói.

---

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Redux Toolkit` / `Zustand`) — thông tin người dùng đang đăng nhập (`userId`, tên, email, trình độ hiện tại) phục vụ định danh học viên, kiểm tra quyền truy cập và ghi nhận lịch sử tiến độ luyện nói.
* `audioDeviceSettings`: **Global State** (`Redux Toolkit` / `Zustand`) — cấu hình micro và thiết bị thu âm của người dùng (mã thiết bị micro đang chọn `selectedMicrophoneId`, âm lượng thu âm, trạng thái bật khử tiếng ồn `noiseSuppression` và chống vọng `echoCancellation`).
* `speakingCategories`: **Server State** (`RTK Query`) — danh sách 04 nhóm danh mục luyện nói (IELTS, TOEIC, Nền tảng, Công việc) kèm số lượng bài luyện hiện có của từng nhóm.
* `speakingResumeItem`: **Server State** (`RTK Query`) — bài luyện nói đang làm dở gần nhất hoặc bài học được hệ thống gợi ý để tiếp tục nhanh.
* `speakingLessonsByCategory`: **Server State** (`RTK Query`) — danh sách bài luyện nói theo danh mục được chọn, đồng bộ theo các tham số bộ lọc trên URL (tìm kiếm theo tên, chủ đề, trình độ, thời lượng, trạng thái) và phân trang.
* `speakingLessonDetail`: **Server State** (`RTK Query`) — chi tiết nội dung bài luyện nói (tiêu đề, chủ đề, trình độ, danh sách các câu/đoạn văn cần đọc, file âm thanh mẫu chuẩn của người bản xứ; tuyệt đối không kèm nhận xét trước khi học viên nộp).
* `speakingAttemptResult`: **Server State** (`RTK Query`) — toàn bộ dữ liệu kết quả chấm và góp ý AI chi tiết theo `attemptId` (điểm tổng, điểm 4 nhóm kỹ năng: Phát âm, Trôi chảy, Từ vựng, Ngữ pháp, danh sách điểm làm tốt, danh sách cần cải thiện, chi tiết từng câu kèm file ghi âm học viên và gợi ý sửa từ).
* `isLoading`, `isFetching`, `isError`: **Server State** (`RTK Query`) — các cờ trạng thái xử lý mạng từ hook RTK Query, không tạo state cục bộ dư thừa.
* `category`: **URL Query Parameter** (`?category=ielts`) — danh mục luyện nói đang được chọn (`ielts`, `toeic`, `foundation`, `work`).
* `topic`: **URL Query Parameter** (`?topic=interview`) — chủ đề bài nói cần lọc (`academic`, `daily_life`, `business`, `technology`...).
* `level`: **URL Query Parameter** (`?level=b2`) — cấp độ trình độ cần lọc (`a1`, `a2`, `b1`, `b2`, `c1`, `c2`).
* `duration`: **URL Query Parameter** (`?duration=10`) — thời lượng bài nói cần lọc theo phút (`5`, `10`, `15`, `20`).
* `status`: **URL Query Parameter** (`?status=not_started`) — trạng thái bài luyện (`all`, `not_started`, `in_progress`, `completed`).
* `search`: **URL Query Parameter** (`?search=presentation`) — từ khóa tìm kiếm tên bài nói trên URL, hỗ trợ chia sẻ link và giữ trạng thái khi tải lại trang.
* `page`: **URL Query Parameter** (`?page=1`) — chỉ số trang hiện tại của danh sách bài luyện nói.
* `activeSentenceReviewIndex`: **URL Query Parameter** (`?sentence=1`) — chỉ số câu đang được chọn xem lại ở màn hình kết quả để tự động cuộn hoặc đồng bộ phát audio.
* `currentSentenceIndex`: **Local State** (`useState`) — chỉ số câu/đoạn văn hiện tại đang được luyện trong phiên làm bài (bắt đầu từ 0 đến N-1).
* `recordingStatus`: **Local State** (`useState`) — trạng thái vòng đời thu âm (`'IDLE' | 'RECORDING' | 'ANALYZING' | 'EVALUATED' | 'ERROR'`).
* `recordedAudioBlob`: **Local State** (`useState`) — dữ liệu nhị phân (Blob) tệp âm thanh thu được của câu hiện tại để phát lại hoặc gửi lên server phân tích.
* `recordedAudioUrl`: **Local State** (`useState`) — chuỗi URL tạm thời (`blob:...`) tạo qua `URL.createObjectURL` để người học bấm nghe lại bản thu của chính mình.
* `recordingSeconds`: **Local State** (`useState`) — số giây thu âm thực tế của lượt đọc hiện tại để hiển thị đồng hồ thời gian.
* `micPermissionStatus`: **Local State** (`useState`) — trạng thái cấp quyền micro của trình duyệt (`'PROMPT' | 'GRANTED' | 'DENIED'`).
* `sentenceFeedbackMap`: **Local State** (`useState`) — bộ nhớ tạm lưu trữ kết quả phân tích AI của từng câu trong phiên làm bài (`Record<number, SentenceAiFeedback>`), cho phép người học xem lại hoặc nộp toàn bộ khi kết thúc.
* `isPlayingNativeAudio`: **Local State** (`useState`) — cờ báo hiệu âm thanh phát âm mẫu chuẩn đang được phát.
* `isPlayingUserAudio`: **Local State** (`useState`) — cờ báo hiệu âm thanh bản thu của học viên đang được phát lại.
* `isMicPermissionModalOpen`: **Local State** (`useState`) — điều khiển đóng/mở modal cảnh báo và hướng dẫn cấp quyền microphone.
* `isSilenceWarningModalOpen`: **Local State** (`useState`) — điều khiển hiển thị cảnh báo không thu được tín hiệu âm thanh hoặc âm lượng quá nhỏ.
* `isConfirmExitModalOpen`: **Local State** (`useState`) — điều khiển hiển thị hộp thoại xác nhận khi người học muốn thoát phòng luyện nói dở dang.

---

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
// ==================== ENUMS & LITERAL TYPES ====================

export type SpeakingCategoryType = 'IELTS' | 'TOEIC' | 'FOUNDATION' | 'WORK';

export type ProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type LessonCompletionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export type RecordingStatusType = 'IDLE' | 'RECORDING' | 'ANALYZING' | 'EVALUATED' | 'ERROR';

export type MicPermissionStatusType = 'PROMPT' | 'GRANTED' | 'DENIED';

export type WordAccuracyLevel = 'EXCELLENT' | 'GOOD' | 'NEEDS_WORK' | 'POOR';

// ==================== CORE ENTITY MODELS ====================

export interface SpeakingCategoryItem {
  id: string;
  type: SpeakingCategoryType;
  title: string;
  description: string;
  totalLessons: number;
  iconName: string;
}

export interface SpeakingResumeItem {
  lessonId: string;
  category: SpeakingCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  completedSentences: number;
  totalSentences: number;
  lastAttemptAt: string;
}

export interface SpeakingLessonSummary {
  id: string;
  category: SpeakingCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  durationMinutes: number;
  totalSentences: number;
  status: LessonCompletionStatus;
  bestScore?: number;
}

export interface SpeakingSentenceItem {
  id: string;
  orderIndex: number;
  text: string;
  phoneticHint?: string;
  nativeAudioUrl?: string;
}

export interface SpeakingLessonDetail {
  id: string;
  category: SpeakingCategoryType;
  title: string;
  topic: string;
  level: ProficiencyLevel;
  durationMinutes: number;
  totalSentences: number;
  sentences: SpeakingSentenceItem[];
}

export interface EvaluatedWord {
  word: string;
  phoneticIpa?: string;
  accuracy: WordAccuracyLevel;
  feedbackNote?: string;
}

export interface SentenceAiFeedback {
  sentenceId: string;
  orderIndex: number;
  overallScore: number;
  pronunciationScore: number;
  fluencyScore: number;
  vocabularyScore: number;
  grammarScore: number;
  words: EvaluatedWord[];
  conciseTip: string;
  userAudioUrl?: string;
}

export interface SpeakingCriteriaScore {
  score: number;
  bandScore?: number;
  comment: string;
}

export interface MispronouncedWordItem {
  word: string;
  correctIpa: string;
  userMispronunciation?: string;
  audioExampleUrl?: string;
  tip: string;
}

export interface SentenceReviewDetail {
  sentenceId: string;
  orderIndex: number;
  originalText: string;
  score: number;
  userAudioUrl: string;
  nativeAudioUrl: string;
  evaluatedWords: EvaluatedWord[];
  mispronouncedWords: MispronouncedWordItem[];
  improvementTip: string;
}

export interface SpeakingAttemptResult {
  attemptId: string;
  lessonId: string;
  lessonTitle: string;
  category: SpeakingCategoryType;
  completedAt: string;
  timeSpentSeconds: number;
  overallScore: number;
  overallBandScore?: number;
  aiGeneralComment: string;
  pronunciation: SpeakingCriteriaScore;
  fluency: SpeakingCriteriaScore;
  vocabulary: SpeakingCriteriaScore;
  grammar: SpeakingCriteriaScore;
  strengths: string[];
  improvements: string[];
  sentenceReviews: SentenceReviewDetail[];
  nextLessonId?: string;
}

export interface SubmitSentenceAudioPayload {
  lessonId: string;
  sentenceId: string;
  orderIndex: number;
  audioBlob: Blob;
  durationSeconds: number;
}

export interface CompleteSpeakingLessonPayload {
  lessonId: string;
  timeSpentSeconds: number;
  completedSentenceCount: number;
}

// ==================== DUMB COMPONENT PROPS ====================

// --- Màn hình 1: Trang danh mục luyện nói ---

export interface SpeakingCategoryHeaderProps {
  title: string;
  description: string;
}

export interface MicrophonePermissionNoticeProps {
  permissionStatus: MicPermissionStatusType;
  onRequestPermission: () => void;
}

export interface ResumeCardProps {
  resumeItem: SpeakingResumeItem;
  onResumeLesson: (lessonId: string) => void;
}

export interface SpeakingResumeBannerProps {
  resumeItem?: SpeakingResumeItem;
  onResumeLesson: (lessonId: string) => void;
}

export interface SpeakingCategoryCardProps {
  category: SpeakingCategoryItem;
  onSelectCategory: (categoryType: SpeakingCategoryType) => void;
}

export interface SpeakingCategoryListProps {
  categories: SpeakingCategoryItem[];
  onSelectCategory: (categoryType: SpeakingCategoryType) => void;
}

// --- Màn hình 2: Danh sách bài luyện theo danh mục ---

export interface SpeakingLessonListHeaderProps {
  categoryTitle: string;
  description: string;
  totalLessons: number;
  breadcrumbItems: { label: string; path?: string }[];
}

export interface SpeakingLessonFilterBarProps {
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

export interface SpeakingLessonCardProps {
  lesson: SpeakingLessonSummary;
  onStartLesson: (lessonId: string) => void;
}

export interface SpeakingLessonGridProps {
  lessons: SpeakingLessonSummary[];
  onStartLesson: (lessonId: string) => void;
}

// --- Màn hình 3: Luyện nói (Speaking Studio) ---

export interface SpeakingPracticeHeaderProps {
  lessonTitle: string;
  currentSentenceIndex: number;
  totalSentences: number;
  onExitClick: () => void;
}

export interface NativeAudioButtonProps {
  audioUrl?: string;
  isPlaying: boolean;
  onPlaySample: () => void;
}

export interface SentenceTextDisplayProps {
  text: string;
}

export interface PhoneticGuideHintProps {
  hintText?: string;
}

export interface SentencePromptCardProps {
  sentence: SpeakingSentenceItem;
  isPlayingSample: boolean;
  onPlaySample: () => void;
}

export interface MicStatusIndicatorProps {
  status: RecordingStatusType;
  permissionStatus: MicPermissionStatusType;
}

export interface LiveAudioWaveformProps {
  isRecording: boolean;
  audioStream?: MediaStream | null;
}

export interface RecordingTimerProps {
  seconds: number;
}

export interface RecordingActionButtonsProps {
  recordingStatus: RecordingStatusType;
  hasRecordedAudio: boolean;
  isPlayingUserAudio: boolean;
  onStartRecord: () => void;
  onStopRecord: () => void;
  onPlayRecordedAudio: () => void;
  onReRecord: () => void;
}

export interface AudioRecordingControllerProps {
  recordingStatus: RecordingStatusType;
  recordingSeconds: number;
  hasRecordedAudio: boolean;
  isPlayingUserAudio: boolean;
  permissionStatus: MicPermissionStatusType;
  audioStream?: MediaStream | null;
  onStartRecord: () => void;
  onStopRecord: () => void;
  onPlayRecordedAudio: () => void;
  onReRecord: () => void;
}

export interface CriteriaPillListProps {
  pronunciationScore: number;
  fluencyScore: number;
  vocabularyScore: number;
  grammarScore: number;
}

export interface SentenceHighlightFeedbackProps {
  words: EvaluatedWord[];
}

export interface RealtimeAiFeedbackCardProps {
  feedback: SentenceAiFeedback;
}

export interface PracticeFlowActionBarProps {
  isLastSentence: boolean;
  hasEvaluatedCurrentSentence: boolean;
  isSubmittingFinal: boolean;
  onRetryCurrentSentence: () => void;
  onNextSentence: () => void;
  onFinishLesson: () => void;
}

export interface SpeakingStudioLayoutProps {
  promptCard: React.ReactNode;
  recordingController: React.ReactNode;
  feedbackCard?: React.ReactNode;
  flowActionBar: React.ReactNode;
}

export interface AiAnalyzingModalProps {
  isOpen: boolean;
}

export interface MicPermissionDeniedModalProps {
  isOpen: boolean;
  onClose: () => void;
  onRetryCheckPermission: () => void;
}

export interface AudioSilenceWarningModalProps {
  isOpen: boolean;
  onRetryRecord: () => void;
  onClose: () => void;
}

export interface ConfirmExitPracticeModalProps {
  isOpen: boolean;
  onConfirmExit: () => void;
  onCancel: () => void;
}

// --- Màn hình 4: Kết quả và góp ý AI ---

export interface SpeakingResultHeaderProps {
  lessonTitle: string;
  category: SpeakingCategoryType;
  completedAt: string;
  timeSpentSeconds: number;
}

export interface OverallScoreCardProps {
  overallScore: number;
  overallBandScore?: number;
  aiGeneralComment: string;
}

export interface CriteriaCardProps {
  title: string;
  score: number;
  comment: string;
  variant: 'pronunciation' | 'fluency' | 'vocabulary' | 'grammar';
}

export interface CriteriaEvaluationGridProps {
  pronunciation: SpeakingCriteriaScore;
  fluency: SpeakingCriteriaScore;
  vocabulary: SpeakingCriteriaScore;
  grammar: SpeakingCriteriaScore;
}

export interface StrengthsCardProps {
  strengths: string[];
}

export interface ImprovementsCardProps {
  improvements: string[];
}

export interface StrengthWeaknessSectionProps {
  strengths: string[];
  improvements: string[];
}

export interface AudioCompareToolbarProps {
  userAudioUrl: string;
  nativeAudioUrl: string;
}

export interface MispronouncedWordListProps {
  words: MispronouncedWordItem[];
  onPlayWordAudio?: (audioUrl: string) => void;
}

export interface SentenceReviewCardProps {
  review: SentenceReviewDetail;
  isSelected: boolean;
  onSelectReview: (sentenceId: string) => void;
}

export interface DetailedSentenceReviewSectionProps {
  sentenceReviews: SentenceReviewDetail[];
  selectedSentenceId?: string;
  onSelectSentence: (sentenceId: string) => void;
}

export interface SpeakingResultActionFooterProps {
  onRetryLesson: () => void;
  onChooseAnotherLesson: () => void;
  onGoToDashboard: () => void;
}

// --- Shared UI Components ---

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

export interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

export interface LevelBadgeProps {
  level: ProficiencyLevel;
}

export interface StatusBadgeProps {
  status: LessonCompletionStatus;
}

export interface DurationBadgeProps {
  minutes: number;
}

export interface SentenceCountBadgeProps {
  count: number;
}

export interface SpeakingScoreBadgeProps {
  score: number;
  maxScore?: number;
  bandScore?: number;
}

export interface PracticeProgressBarProps {
  currentIndex: number;
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
