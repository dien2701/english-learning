# 09-Chat-plan

### 1. PHÂN RÃ COMPONENT (COMPONENT TREE)

* **AiChatWidgetContainer [SMART]**: Quản lý toàn bộ vòng đời và hoạt động của cửa sổ chat nổi Trợ lý AI trên hệ thống: điều phối trạng thái hiển thị (Mở, Thu nhỏ, Mở rộng toàn màn hình), khởi tạo và duy trì kết nối WebSocket/Streaming API, nạp gợi ý cá nhân hóa và lịch sử trò chuyện, quản lý gửi tin nhắn, xử lý stream phản hồi từ AI, xử lý lỗi mất kết nối mạng và hỗ trợ điều hướng nhanh theo các liên kết gợi ý học tập.
  * **AiChatFloatingButton [DUMB]**: *(Shared UI)* Nút bấm nổi (FAB) cố định ở góc dưới bên phải màn hình khi cửa sổ chat đang đóng hoặc thu nhỏ, hiển thị biểu tượng Trợ lý AI kèm huy hiệu số tin nhắn mới chưa đọc hoặc trạng thái sẵn sàng.
  * **AiChatWindowLayout [DUMB]**: Khung bố cục bao đóng cửa sổ chat nổi (hỗ trợ hiển thị dạng hộp nổi góc phải trên Desktop, chế độ phóng to, hoặc dạng Bottom Sheet/toàn màn hình trên Mobile).
    * **AiChatHeader [DUMB]**: Thanh tiêu đề cửa sổ chat (thông tin định danh trợ lý, trạng thái hoạt động và các phím điều khiển thao tác cửa sổ).
      * **AiAvatarStatus [DUMB]**: *(Shared UI)* Ảnh đại diện AI đại diện hệ thống En-Learning kèm chấm đèn chỉ báo trạng thái trực tuyến (Online status indicator).
      * **AiHeaderInfo [DUMB]**: Tên đại diện "Trợ lý học tiếng Anh" và dòng mô tả ngắn về trạng thái phản hồi.
      * **AiChatHeaderActions [DUMB]**: Cụm nút bấm điều khiển trên thanh tiêu đề:
        * Nút `Làm mới cuộc trò chuyện` (New conversation / Clear context).
        * Nút `Thu nhỏ` (Minimize window xuống góc màn hình).
        * Nút `Phóng to / Thu gọn kích thước` (Toggle expand/collapse layout).
        * Nút `Đóng` (Close floating window).
    * **AiChatBody [DUMB]**: Khu vực trung tâm hiển thị nội dung trao đổi, tự động chuyển đổi giữa giao diện Chào mừng ban đầu và giao diện Lịch sử hội thoại.
      * **AiWelcomeScreen [DUMB]**: Giao diện trạng thái chào mừng hiển thị khi người dùng mở chat lần đầu hoặc chưa có nội dung hội thoại (Màn 1).
        * **AiWelcomeGreeting [DUMB]**: Khối chào mừng thân thiện kèm biểu tượng đón tiếp và thông điệp mở đầu ("Chào bạn, hôm nay bạn cần hỗ trợ phần tiếng Anh nào?").
        * **PersonalizedStudyHintCard [DUMB]**: Thẻ gợi ý cá nhân hóa dựa trên dữ liệu học tập thực tế của học viên (nhắc nhở ôn từ vựng dở dang, chủ điểm ngữ pháp còn yếu hoặc đề xuất bài luyện tiếp theo).
        * **QuickPromptSection [DUMB]**: Khối phân nhóm và danh sách các câu hỏi mẫu gợi ý nhanh để người dùng chọn tương tác ngay.
          * **QuickPromptCategoryChips [DUMB]**: Các chip lọc phân loại nhóm câu hỏi nhanh (`Tất cả`, `Ngữ pháp`, `Từ vựng`, `Gợi ý bài tập`, `Cải thiện kỹ năng`).
          * **QuickPromptList [DUMB]**: Danh sách các nút câu hỏi mẫu định hình sẵn (VD: “Giải thích ngữ pháp”, “Hỏi nghĩa từ vựng”, “Gợi ý bài luyện cho tôi”, “Tôi nên cải thiện gì?”).
            * **QuickPromptButton [DUMB]**: Nút bấm câu hỏi gợi ý đơn lẻ hỗ trợ hiệu ứng hover và icon trực quan.
      * **AiConversationView [DUMB]**: Giao diện dòng thời gian hội thoại chi tiết (Màn 2).
        * **MessageListContainer [DUMB]**: Danh sách cuộn chứa toàn bộ các bong bóng tin nhắn trao đổi giữa Người dùng và Trợ lý AI.
          * **ChatMessageItem [DUMB]**: Vỏ bọc từng tin nhắn đơn lẻ trong danh sách, tự động căn lề và phối màu theo vai trò (User hoặc AI).
            * **UserMessageBubble [DUMB]**: Bong bóng tin nhắn của người dùng (căn phải, màu nền Primary, hiển thị nội dung văn bản, mốc thời gian gửi và biểu tượng trạng thái gửi).
            * **AiMessageBubble [DUMB]**: Bong bóng tin nhắn phản hồi của AI (căn trái, màu nền Card trắng/sáng, avatar AI, định dạng Markdown hỗ trợ cấu trúc văn bản phong phú).
              * **MarkdownMessageContent [DUMB]**: *(Shared UI)* Trình hiển thị nội dung câu trả lời hỗ trợ cú pháp Markdown (in đậm, danh sách gạch đầu dòng, bảng so sánh ngữ pháp, đoạn mã, ví dụ câu tiếng Anh).
              * **AiTypingIndicator [DUMB]**: *(Shared UI)* Hoạt ảnh sóng ba chấm chuyển động mượt mà kèm thông báo "Trợ lý AI đang trả lời..." trong khi đang chờ hoặc stream dữ liệu từ máy chủ.
              * **AiRecommendationCardList [DUMB]**: Danh sách các thẻ gợi ý hành động học tập được AI đính kèm ngay bên dưới câu trả lời phù hợp với nội dung trao đổi.
                * **AiRecommendationCard [DUMB]**: Thẻ đề xuất bài luyện cụ thể (icon phân loại kỹ năng, tiêu đề bài học, lý do đề xuất ngắn gọn và nút CTA điều hướng trực tiếp đến Flashcard, Luyện viết, Luyện nghe, Bài kiểm tra hoặc Thống kê).
                  * **SkillCategoryBadge [DUMB]**: *(Shared UI)* Huy hiệu kỹ năng liên quan (`Flashcard`, `Writing`, `Listening`, `Exam`, `Grammar`, `Vocabulary`).
            * **MessageErrorNotice [DUMB]**: Khung thông báo lỗi khi tin nhắn gửi thất bại hoặc mất kết nối mạng, kèm nút `Gửi lại` (Retry) mà không làm mất nội dung đã nhập.
            * **AiFallbackNotice [DUMB]**: Khung thông báo khi AI chưa thể giải đáp chính xác nội dung câu hỏi, kèm lời khuyên diễn đạt lại hoặc đề xuất các câu hỏi gợi ý liên quan.
        * **ScrollToBottomButton [DUMB]**: *(Shared UI)* Nút bấm nổi cho phép cuộn nhanh xuống cuối danh sách tin nhắn khi người dùng cuộn lên trên để đọc lại lịch sử cũ.
    * **AiChatInputBar [DUMB]**: Khu vực nhập liệu tin nhắn cố định ở chân cửa sổ chat.
      * **ChatTextAreaInput [DUMB]**: Ô nhập văn bản hỗ trợ tự động co giãn chiều cao theo độ dài dòng (auto-resize), hỗ trợ phím tắt `Enter` để gửi và `Shift + Enter` để xuống dòng mới.
      * **ChatInputActionBar [DUMB]**: Thanh công cụ thao tác nhanh bên trong ô nhập:
        * **InputWordCountBadge [DUMB]**: Chỉ báo hiển thị số lượng ký tự đã nhập trên giới hạn cho phép.
        * **VoiceInputButton [DUMB]**: *(Shared UI)* Nút kích hoạt nhập liệu nhanh bằng giọng nói (Voice-to-text) qua Web Speech API.
        * **ClearInputButton [DUMB]**: Nút xóa nhanh toàn bộ nội dung đang gõ dở trong ô văn bản.
        * **SendMessageButton [DUMB]**: Nút gửi tin nhắn (màu Primary nổi bật, biểu tượng máy bay giấy/mũi tên, tự động vô hiệu hóa khi ô nhập rỗng hoặc AI đang xử lý).
  * **ConfirmResetChatModal [DUMB]**: *(Shared UI)* Hộp thoại cảnh báo xác nhận khi người học nhấn làm mới phiên trò chuyện để xóa ngữ cảnh cũ.
  * **AiChatNetworkAlert [DUMB]**: *(Shared UI)* Thanh banner cảnh báo mất kết nối mạng internet hoặc ngắt kết nối WebSocket/Streaming API.

---

### 2. QUẢN LÝ TRẠNG THÁI (STATE MANAGEMENT)

* `currentUser`: **Global State** (`Redux Toolkit`) — thông tin người dùng đang đăng nhập (`userId`, họ tên, email, trình độ hiện tại, mục tiêu học tập) phục vụ định danh học viên và cung cấp thông tin ngữ cảnh cho Trợ lý AI.
* `isAiChatOpen`: **Global State** (`Redux Toolkit`) — cờ bật/tắt hiển thị cửa sổ chat nổi trên toàn bộ các trang của hệ thống (được kích hoạt từ mục "Chat với AI" trên Sidebar hoặc nút nổi FAB).
* `chatWindowMode`: **Global State** (`Redux Toolkit`) — chế độ hiển thị của khung chat nổi (`'FLOATING' | 'MINIMIZED' | 'EXPANDED' | 'BOTTOM_SHEET'`) nhằm giữ nguyên trạng thái cửa sổ khi chuyển đổi giữa các bài học.
* `activeConversationId`: **Global State** (`Redux Toolkit`) — mã định danh của phiên hội thoại hiện tại, hỗ trợ khôi phục ngữ cảnh hội thoại xuyên suốt phiên đăng nhập.
* `unreadAiMessagesCount`: **Global State** (`Redux Toolkit`) — số lượng tin nhắn phản hồi mới từ AI nhận được khi cửa sổ chat đang ở trạng thái thu nhỏ (`MINIMIZED`).
* `chatContextMetadata`: **Global State** (`Redux Toolkit`) — siêu dữ liệu về trang học tập người dùng đang đứng (VD: đang ở bài Flashcard deck nào, đang luyện bài viết nào) để tự động gửi kèm ngữ cảnh cho AI khi đặt câu hỏi.
* `userLearningSummary`: **Server State** (`RTK Query`) — dữ liệu tóm tắt kết quả học tập của học viên từ máy chủ (bộ từ vựng cần ôn lại, chủ điểm ngữ pháp còn sai sót, lịch sử thi gần nhất) phục vụ hiển thị thẻ gợi ý cá nhân hóa ở màn chào mừng.
* `chatHistory`: **Server State** (`RTK Query` / WebSocket / SSE) — danh sách lịch sử các tin nhắn của phiên hội thoại đang kích hoạt (`ChatMessageItemData[]`).
* `quickPrompts`: **Server State** (`RTK Query`) — danh mục các câu hỏi mẫu gợi ý nhanh được nạp từ hệ thống theo chuyên mục (`GRAMMAR`, `VOCABULARY`, `RECOMMENDATION`, `IMPROVEMENT`).
* `isAiResponding`: **Server State** / **Hook State** (`WebSocket / SSE / RTK Query`) — cờ báo hiệu AI đang trong quá trình sinh nội dung trả lời (streaming / generating response).
* `chatOpen`: **URL Query Parameter** (`?chat=open`) — cho phép kích hoạt tự động mở cửa sổ chat khi truy cập qua đường dẫn chia sẻ hoặc từ thông báo hệ thống.
* `contextType`: **URL Query Parameter** (`?context=writing&id=prompt-102`) — truyền ngữ cảnh học tập cụ thể lên URL khi chuyển hướng từ các module học tập khác sang chat.
* `initialPrompt`: **URL Query Parameter** (`?prompt=explain_grammar`) — nạp sẵn câu hỏi mẫu vào ô nhập liệu khi người dùng bấm vào nút "Hỏi AI về câu này" từ trang làm bài kiểm tra hoặc bài luyện đọc.
* `inputMessage`: **Local State** (`useState`) — chuỗi ký tự người dùng đang nhập trong ô văn bản (`string`).
* `isComposing`: **Local State** (`useState`) — cờ theo dõi trạng thái gõ bộ gõ tiếng Việt (IME Composition) để tránh việc vô tình kích hoạt sự kiện gửi tin nhắn khi bấm `Enter` để chọn dấu.
* `failedMessagePayload`: **Local State** (`useState`) — lưu trữ tạm thời nội dung tin nhắn gửi thất bại để phục vụ tính năng `Gửi lại` (Retry).
* `showScrollToBottom`: **Local State** (`useState`) — cờ điều khiển hiển thị nút cuộn nhanh xuống đáy màn hình khi người dùng cuộn lên trên xem lịch sử cũ.
* `isConfirmResetModalOpen`: **Local State** (`useState`) — điều khiển đóng/mở hộp thoại xác nhận làm mới cuộc trò chuyện.
* `isListeningVoice`: **Local State** (`useState`) — trạng thái kích hoạt thu âm giọng nói để chuyển thành văn bản trong ô nhập liệu.
* `selectedQuickPromptCategory`: **Local State** (`useState`) — danh mục câu hỏi mẫu đang chọn hiển thị trên màn chào mừng (`'ALL' | 'GRAMMAR' | 'VOCABULARY' | 'RECOMMENDATION' | 'IMPROVEMENT'`).

---

### 3. CẤU TRÚC DỮ LIỆU (DATA INTERFACES)

```typescript
// ==================== ENUMS & LITERAL TYPES ====================

export type ChatRoleType = 'USER' | 'ASSISTANT' | 'SYSTEM';

export type MessageDeliveryStatus = 'SENDING' | 'SENT' | 'STREAMING' | 'FAILED';

export type ChatWindowModeType = 'FLOATING' | 'MINIMIZED' | 'EXPANDED' | 'BOTTOM_SHEET';

export type QuickPromptCategoryType = 'ALL' | 'GRAMMAR' | 'VOCABULARY' | 'RECOMMENDATION' | 'IMPROVEMENT';

export type StudyRecommendationActionType = 
  | 'FLASHCARD_DECK' 
  | 'WRITING_PRACTICE' 
  | 'LISTENING_LESSON' 
  | 'READING_PASSAGE' 
  | 'EXAM_TEST' 
  | 'LEARNING_STATISTICS'
  | 'GRAMMAR_LESSON';

export type SkillBadgeType = 
  | 'FLASHCARD' 
  | 'WRITING' 
  | 'LISTENING' 
  | 'READING' 
  | 'EXAM' 
  | 'GRAMMAR' 
  | 'VOCABULARY';

// ==================== CORE ENTITY MODELS ====================

export interface StudyRecommendationItem {
  id: string;
  actionType: StudyRecommendationActionType;
  title: string;
  reason: string;
  targetUrl: string;
  skillBadge: SkillBadgeType;
  actionLabel: string;
}

export interface ChatMessageItemData {
  id: string;
  conversationId: string;
  role: ChatRoleType;
  content: string;
  createdAt: string;
  status: MessageDeliveryStatus;
  recommendations?: StudyRecommendationItem[];
  errorReason?: string;
}

export interface QuickPromptItem {
  id: string;
  category: QuickPromptCategoryType;
  label: string;
  promptText: string;
  iconName?: string;
}

export interface PersonalizedStudyContext {
  userId: string;
  weakTopics: string[];
  recentDeckToReview?: {
    deckId: string;
    deckTitle: string;
  };
  recommendedSkill?: SkillBadgeType;
  summaryNote: string;
}

export interface CurrentPageContext {
  pageType: 'FLASHCARD' | 'WRITING' | 'LISTENING' | 'READING' | 'EXAM' | 'DASHBOARD' | 'GENERAL';
  entityId?: string;
  entityTitle?: string;
  currentQuestionOrText?: string;
}

export interface SendMessagePayload {
  conversationId: string;
  content: string;
  context?: CurrentPageContext;
}

// ==================== DUMB COMPONENT PROPS ====================

// --- Floating Trigger & Layout Props ---

export interface AiChatFloatingButtonProps {
  isOpen: boolean;
  unreadCount: number;
  isAiResponding: boolean;
  onClick: () => void;
}

export interface AiAvatarStatusProps {
  size?: 'sm' | 'md' | 'lg';
  isOnline: boolean;
}

export interface AiHeaderInfoProps {
  title: string;
  subtitle: string;
}

export interface AiChatHeaderActionsProps {
  windowMode: ChatWindowModeType;
  onResetChat: () => void;
  onMinimize: () => void;
  onToggleExpand: () => void;
  onClose: () => void;
}

export interface AiChatHeaderProps {
  isOnline: boolean;
  windowMode: ChatWindowModeType;
  onResetChat: () => void;
  onMinimize: () => void;
  onToggleExpand: () => void;
  onClose: () => void;
}

export interface AiChatWindowLayoutProps {
  isOpen: boolean;
  windowMode: ChatWindowModeType;
  header: React.ReactNode;
  body: React.ReactNode;
  inputBar: React.ReactNode;
  networkAlert?: React.ReactNode;
}

// --- Màn 1: Trạng thái Chào mừng Props ---

export interface AiWelcomeGreetingProps {
  greetingText: string;
  subGreetingText: string;
}

export interface PersonalizedStudyHintCardProps {
  hint: PersonalizedStudyContext;
  onApplyPrompt: (promptText: string) => void;
}

export interface QuickPromptCategoryChipsProps {
  categories: { key: QuickPromptCategoryType; label: string }[];
  activeCategory: QuickPromptCategoryType;
  onSelectCategory: (category: QuickPromptCategoryType) => void;
}

export interface QuickPromptButtonProps {
  prompt: QuickPromptItem;
  onSelectPrompt: (promptText: string) => void;
}

export interface QuickPromptListProps {
  prompts: QuickPromptItem[];
  onSelectPrompt: (promptText: string) => void;
}

export interface QuickPromptSectionProps {
  categories: { key: QuickPromptCategoryType; label: string }[];
  activeCategory: QuickPromptCategoryType;
  prompts: QuickPromptItem[];
  onSelectCategory: (category: QuickPromptCategoryType) => void;
  onSelectPrompt: (promptText: string) => void;
}

export interface AiWelcomeScreenProps {
  personalizedHint?: PersonalizedStudyContext;
  promptCategories: { key: QuickPromptCategoryType; label: string }[];
  selectedCategory: QuickPromptCategoryType;
  prompts: QuickPromptItem[];
  onSelectCategory: (category: QuickPromptCategoryType) => void;
  onSelectPrompt: (promptText: string) => void;
}

// --- Màn 2: Đang hội thoại Props ---

export interface MarkdownMessageContentProps {
  content: string;
}

export interface AiTypingIndicatorProps {
  indicatorText?: string;
}

export interface SkillCategoryBadgeProps {
  badgeType: SkillBadgeType;
}

export interface AiRecommendationCardProps {
  recommendation: StudyRecommendationItem;
  onNavigateToRecommendation: (targetUrl: string) => void;
}

export interface AiRecommendationCardListProps {
  recommendations: StudyRecommendationItem[];
  onNavigateToRecommendation: (targetUrl: string) => void;
}

export interface MessageErrorNoticeProps {
  errorMessage: string;
  onRetry: () => void;
}

export interface AiFallbackNoticeProps {
  message: string;
  suggestionPrompts?: string[];
  onSelectSuggestionPrompt?: (prompt: string) => void;
}

export interface UserMessageBubbleProps {
  message: ChatMessageItemData;
  onRetryMessage?: (messageId: string) => void;
}

export interface AiMessageBubbleProps {
  message: ChatMessageItemData;
  onNavigateToRecommendation: (targetUrl: string) => void;
}

export interface ChatMessageItemProps {
  message: ChatMessageItemData;
  onRetryMessage?: (messageId: string) => void;
  onNavigateToRecommendation: (targetUrl: string) => void;
}

export interface MessageListContainerProps {
  messages: ChatMessageItemData[];
  isAiResponding: boolean;
  onRetryMessage?: (messageId: string) => void;
  onNavigateToRecommendation: (targetUrl: string) => void;
}

export interface ScrollToBottomButtonProps {
  visible: boolean;
  onClick: () => void;
}

export interface AiConversationViewProps {
  messages: ChatMessageItemData[];
  isAiResponding: boolean;
  showScrollToBottom: boolean;
  onScrollToBottom: () => void;
  onRetryMessage?: (messageId: string) => void;
  onNavigateToRecommendation: (targetUrl: string) => void;
}

// --- Khung nhập liệu tin nhắn (Input Bar) Props ---

export interface ChatTextAreaInputProps {
  value: string;
  placeholder?: string;
  disabled?: boolean;
  onChange: (value: string) => void;
  onKeyDown: (event: React.KeyboardEvent<HTMLTextAreaElement>) => void;
  onCompositionStart: () => void;
  onCompositionEnd: () => void;
}

export interface InputWordCountBadgeProps {
  currentLength: number;
  maxLength: number;
}

export interface SendMessageButtonProps {
  disabled: boolean;
  onClick: () => void;
}

export interface VoiceInputButtonProps {
  isListening: boolean;
  disabled?: boolean;
  onToggleVoice: () => void;
}

export interface ClearInputButtonProps {
  visible: boolean;
  onClick: () => void;
}

export interface ChatInputActionBarProps {
  currentLength: number;
  maxLength: number;
  hasContent: boolean;
  canSend: boolean;
  isListeningVoice: boolean;
  onClearInput: () => void;
  onToggleVoice: () => void;
  onSend: () => void;
}

export interface AiChatInputBarProps {
  inputValue: string;
  maxLength: number;
  disabled: boolean;
  isListeningVoice: boolean;
  onChangeInput: (value: string) => void;
  onSend: () => void;
  onClearInput: () => void;
  onToggleVoice: () => void;
}

// --- Modals & Alerts (Shared UI) Props ---

export interface ConfirmResetChatModalProps {
  isOpen: boolean;
  onConfirmReset: () => void;
  onClose: () => void;
}

export interface AiChatNetworkAlertProps {
  isConnected: boolean;
  onReconnect: () => void;
}
```
