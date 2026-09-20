# 03-Flashcard-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **FlashcardHomePage [SMART]**: Điều phối toàn bộ dữ liệu trang chủ Flashcard: danh mục bộ từ, danh sách truy cập gần đây, tìm kiếm, lọc và kích hoạt modal xác nhận xóa.
  * **FlashcardHeaderAction [DUMB]**: Tiêu đề trang "Flashcard" và nút CTA chính `Tạo bộ từ mới`.
  * **FlashcardSearchBar [DUMB]**: *(Shared UI)* Ô tìm kiếm theo tên bộ từ, hỗ trợ debounce và xóa nhanh từ khóa.
  * **FlashcardCategoryFilterTabs [DUMB]**: *(Shared UI)* Thanh tab chuyển đổi nhanh danh mục (Tất cả, Truy cập gần đây, IELTS, TOEIC, Nền tảng, Công việc).
  * **CategoryDeckRowSection [SMART]**: Quản lý dữ liệu danh sách bộ từ theo từng danh mục cụ thể (tối đa 3 bộ/hàng).
    * **CategoryRowHeader [DUMB]**: Hiển thị tên danh mục, số lượng bộ từ hiện có và CTA `Xem thêm` chuyển sang trang danh sách đầy đủ.
    * **DeckGrid [DUMB]**: Layout grid hiển thị danh sách các thẻ bộ từ (3 cột trên desktop, 1 cột trên mobile).
    * **DeckCard [DUMB]**: Thẻ hiển thị thông tin bộ Flashcard (tên, mô tả ngắn, số lượng từ, badge trình độ, tiến độ học, nút thao tác xóa và chuyển đến chi tiết).
    * **ProgressBar [DUMB]**: *(Shared UI)* Thanh hiển thị tỷ lệ % hoàn thành tiến độ học.
    * **LevelBadge [DUMB]**: *(Shared UI)* Huy hiệu hiển thị trình độ (A1, A2, B1, B2, C1, C2, IELTS 6.5+, TOEIC 750+).
  * **EmptyDeckState [DUMB]**: *(Shared UI)* Hiển thị khi không tìm thấy bộ từ phù hợp kèm CTA `Xóa bộ lọc`.
  * **FlashcardSkeleton [DUMB]**: *(Shared UI)* Hiệu ứng tải trang dạng skeleton cho các hàng thẻ.
  * **DeleteDeckModal [DUMB]**: *(Shared UI)* Hộp thoại xác nhận xóa bộ từ (tên bộ từ, cảnh báo ngắn, nút `Hủy` và nút `Xóa bộ từ`).

* **FlashcardCategoryPage [SMART]**: Quản lý trang danh sách đầy đủ các bộ từ theo một danh mục cụ thể, hỗ trợ tìm kiếm, lọc theo trình độ và phân trang.
  * **CategoryPageHeader [DUMB]**: Hiển thị tên danh mục đang chọn, tổng số bộ từ và breadcrumb quay về trang Flashcard chính.
  * **Breadcrumb [DUMB]**: *(Shared UI)* Đường dẫn điều hướng phân cấp.
  * **FlashcardFilterBar [DUMB]**: Thanh công cụ tích hợp: ô tìm kiếm, bộ lọc danh mục và bộ lọc trình độ học.
  * **DeckGrid [DUMB]**: Lưới hiển thị toàn bộ các bộ từ thuộc danh mục đã chọn.
  * **DeckCard [DUMB]**: Tái sử dụng thẻ hiển thị bộ từ kèm nút xóa theo quyền hạn của người dùng.
  * **PaginationControl [DUMB]**: *(Shared UI)* Thanh phân trang số hoặc nút `Xem thêm` khi danh sách bộ từ lớn.
  * **EmptyDeckState [DUMB]**: *(Shared UI)* Hiển thị trạng thái trống khi danh mục chưa có bộ từ nào.

* **CreateFlashcardDeckPage [SMART]**: Quản lý logic form tạo bộ từ mới, validate dữ liệu đầu vào, xử lý submit API và thông báo toast kết quả.
  * **CreateDeckHeader [DUMB]**: Tiêu đề trang `Tạo bộ Flashcard mới` và nút điều hướng quay lại.
  * **CreateDeckForm [DUMB]**: Form thu thập dữ liệu bộ từ mới.
    * **FormField [DUMB]**: *(Shared UI)* Khối trường nhập liệu bọc label, input, và dòng cảnh báo lỗi validation ngay bên dưới.
    * **SelectDropdown [DUMB]**: *(Shared UI)* Dropdown chọn danh mục (IELTS, TOEIC, Nền tảng...) và trình độ (A1 - C2).
    * **SubmitButton [DUMB]**: *(Shared UI)* Nút bấm chính có trạng thái disable khi form chưa hợp lệ và loading khi đang gửi request.

* **FlashcardDeckDetailPage [SMART]**: Tải và quản lý thông tin chi tiết của một bộ từ, danh sách từ vựng xem trước và điều hướng bắt đầu học.
  * **DeckDetailHero [DUMB]**: Khối thông tin chi tiết bộ từ (tên bộ từ, mô tả, danh mục, badge trình độ, số lượng từ, thanh tiến độ hiện tại, nút CTA chính `Bắt đầu học` và nút quay lại).
  * **WordPreviewSection [DUMB]**: Khu vực xem trước danh sách một số từ vựng tiêu biểu trước khi vào phiên học.
    * **WordPreviewItem [DUMB]**: Thẻ từ tóm tắt (từ tiếng Anh, phiên âm, từ loại và nghĩa tiếng Việt ngắn gọn).
  * **EmptyPreviewState [DUMB]**: *(Shared UI)* Hiển thị khi bộ từ chưa có từ vựng xem trước.

* **FlashcardStudyPage [SMART]**: Điều phối toàn bộ phiên học Flashcard: quản lý thẻ hiện tại, điều khiển âm thanh phát âm, chuyển thẻ, gửi kết quả ghi nhận tiến độ lên backend và kích hoạt modal hoàn thành.
  * **StudyHeader [DUMB]**: Thanh tiêu đề chế độ học tối giản (nút thoát phiên học, tên bộ từ đang học và bộ đếm tiến trình `05/20 từ`).
  * **StudyProgressBar [DUMB]**: *(Shared UI)* Thanh tiến độ trực quan hiển thị tỷ lệ số từ đã học trên tổng số từ của phiên.
  * **FlashcardItem [DUMB]**: Khối thẻ từ trung tâm hiển thị đầy đủ thông tin: từ tiếng Anh, phiên âm chuẩn IPA, nút phát âm thanh, hình ảnh minh họa, nghĩa tiếng Việt và ví dụ/giải thích chi tiết.
    * **AudioPronounceButton [DUMB]**: *(Shared UI)* Nút loa kích hoạt phát âm chuẩn với hiệu ứng đang phát âm thanh.
  * **StudyControlBar [DUMB]**: Thanh điều hướng cuối trang (nút `Tiếp tục` chuyển thẻ tiếp theo, tự động đổi thành nút `Hoàn thành` ở thẻ cuối cùng).
  * **StudyCompletionModal [DUMB]**: Hộp thoại chúc mừng khi học xong từ cuối cùng (hiển thị thông báo hoàn thành, số từ đã học, CTA quay về trang Flashcard chính).

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Zustand`) — thông tin tài khoản đăng nhập để xác định quyền xóa bộ từ (`USER` hay `ADMIN`, chủ sở hữu bộ từ).
* `activeAudioId`: **Global State** (`Zustand`) — ID của từ vựng đang được phát âm thanh, đảm bảo chỉ có một âm thanh phát tại một thời điểm trên toàn ứng dụng.
* `recentDecks`: **Server State** (`RTK Query`) — danh sách các bộ từ truy cập gần đây của người dùng.
* `categoryDecks`: **Server State** (`RTK Query`) — danh sách bộ từ nhóm theo từng danh mục hiển thị trên trang chính (tối đa 3 bộ/hàng).
* `categoryDeckList`: **Server State** (`RTK Query`) — danh sách bộ từ đầy đủ có phân trang cho màn hình danh mục.
* `deckDetail`: **Server State** (`RTK Query`) — thông tin chi tiết một bộ Flashcard và danh sách từ vựng xem trước.
* `studySessionCards`: **Server State** (`RTK Query`) — danh sách từ vựng đầy đủ của bộ từ phục vụ phiên học flashcard.
* `isLoading`, `isError`, `isFetching`: **Server State** (`RTK Query`) — trạng thái fetch API tự động từ query hook, không lưu riêng bằng `useState`.
* `search`: **URL Query Parameter** (`?search=ielts`) — lưu từ khóa tìm kiếm bộ từ trên URL để duy trì kết quả khi reload hoặc chia sẻ link.
* `category`: **URL Query Parameter** (`?category=ielts`) — lưu định danh danh mục đang lọc.
* `level`: **URL Query Parameter** (`?level=b2`) — lưu cấp độ trình độ đang lọc trên trang danh mục.
* `page`: **URL Query Parameter** (`?page=1`) — số trang hiện tại của danh sách bộ từ theo danh mục.
* `isDeleteModalOpen`: **Local State** (`useState`) — đóng/mở modal xác nhận xóa bộ từ.
* `selectedDeckToDelete`: **Local State** (`useState`) — đối tượng bộ từ được chọn để xóa trong modal xác nhận.
* `createDeckFormData`: **Local State** (`useState`) — dữ liệu các trường nhập liệu trong form tạo bộ từ mới (`{ name, category, level, totalWords }`).
* `formErrors`: **Local State** (`useState`) — danh sách các thông báo lỗi validation theo từng trường của form tạo mới.
* `currentCardIndex`: **Local State** (`useState`) — vị trí index của thẻ từ đang hiển thị trong phiên học Flashcard (`FlashcardStudyPage`).
* `isCompletionModalOpen`: **Local State** (`useState`) — trạng thái hiển thị modal tổng kết hoàn thành phiên học.

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
type DeckCategory = 'RECENT' | 'IELTS' | 'TOEIC' | 'FOUNDATION' | 'WORK';

type ProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

interface FlashcardDeckItem {
  id: string;
  name: string;
  description: string;
  category: DeckCategory;
  level: ProficiencyLevel;
  totalWords: number;
  progressPercent: number;
  canDelete: boolean;
}

interface DeckCardProps {
  deck: FlashcardDeckItem;
  onSelect: (deckId: string) => void;
  onDelete?: (deck: FlashcardDeckItem) => void;
}

interface CategoryRowHeaderProps {
  categoryTitle: string;
  totalCount: number;
  onViewMore: () => void;
}

interface DeckGridProps {
  decks: FlashcardDeckItem[];
  onSelectDeck: (deckId: string) => void;
  onDeleteDeck?: (deck: FlashcardDeckItem) => void;
}

interface FlashcardSearchBarProps {
  value: string;
  placeholder?: string;
  onChange: (query: string) => void;
  onClear: () => void;
}

interface FlashcardCategoryFilterTabsProps {
  categories: { key: string; label: string }[];
  activeKey: string;
  onSelectCategory: (key: string) => void;
}

interface FlashcardFilterBarProps {
  searchQuery: string;
  selectedCategory: string;
  selectedLevel: string;
  categories: { value: string; label: string }[];
  levels: { value: string; label: string }[];
  onSearchChange: (value: string) => void;
  onCategoryChange: (value: string) => void;
  onLevelChange: (value: string) => void;
}

interface CreateDeckFormValues {
  name: string;
  category: string;
  level: string;
  totalWords: number;
}

interface FormFieldProps {
  label: string;
  required?: boolean;
  errorMessage?: string;
  children: React.ReactNode;
}

interface CreateDeckFormProps {
  initialValues: CreateDeckFormValues;
  errors: Partial<Record<keyof CreateDeckFormValues, string>>;
  isSubmitting: boolean;
  isValid: boolean;
  onChange: (field: keyof CreateDeckFormValues, value: string | number) => void;
  onSubmit: () => void;
}

interface WordPreviewItemData {
  id: string;
  term: string;
  phonetic: string;
  partOfSpeech: string;
  meaningVi: string;
}

interface DeckDetailHeroProps {
  name: string;
  description: string;
  category: string;
  level: string;
  totalWords: number;
  progressPercent: number;
  onStartStudy: () => void;
  onBack: () => void;
}

interface WordPreviewSectionProps {
  words: WordPreviewItemData[];
}

interface WordPreviewItemProps {
  word: WordPreviewItemData;
}

interface FlashcardStudyWord {
  id: string;
  term: string;
  meaningVi: string;
  phonetic: string;
  explanationVi: string;
  imageUrl?: string;
  audioUrl: string;
}

interface FlashcardItemProps {
  card: FlashcardStudyWord;
  isPlayingAudio: boolean;
  onPlayAudio: () => void;
}

interface StudyHeaderProps {
  deckName: string;
  currentIndex: number;
  totalCards: number;
  onExit: () => void;
}

interface StudyControlBarProps {
  isLastCard: boolean;
  onNext: () => void;
}

interface StudyCompletionModalProps {
  isOpen: boolean;
  totalLearned: number;
  onConfirm: () => void;
}

interface DeleteDeckModalProps {
  isOpen: boolean;
  deckName: string;
  isLoading: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

interface ProgressBarProps {
  value: number;
  max?: number;
  label?: string;
}

interface EmptyDeckStateProps {
  title?: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
}
```
