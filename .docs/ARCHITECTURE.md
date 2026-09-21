KIẾN TRÚC HỆ THỐNG: EN-LEARNING
1. Sơ đồ dữ liệu cốt lõi (Core Database Entities)
Nguồn sự thật: apps/backend/src/main/resources/db/migration/V1__init_schema.sql (Flyway).
Entity JPA nằm ở apps/backend/src/main/java/vn/enlearning/backend/entity và phải khớp SQL
vì Hibernate chạy ddl-auto=validate. Khi có môi trường thật, đổi schema thì thêm file V2, V3... , không sửa V1.
Trước đó V1 vẫn được viết lại: lần gần nhất 20/09/2026, khi thêm cột attempts cho password_reset_tokens (mã OTP đặt lại mật khẩu);
trước đó cùng ngày là khi khôi phục Chat AI và Luyện nói và thêm thời gian học. Chạy Flyway trên một DB nào đó là ghi checksum V1 vào DB ấy.

Quy ước: khoá chính UUID v7 (BINARY(16)); nội dung song ngữ dùng cặp cột xxxVi/xxxEn;
điểm DECIMAL(3,1) thang 10; xoá mềm bằng deletedAt cho User, nội dung, Question, Notification, ChatConversation.
Danh sách nhỏ không truy vấn riêng (hints, paragraphs, acceptedAnswers, issues, prompts, improvements,
promptFeedback, links) lưu ở cột JSON của bảng cha, thứ tự mảng là thứ tự hiển thị. Hệ thống gồm 26 bảng.
Nội dung đã có trong lịch sử học bị FK RESTRICT chặn xoá, chỉ được chuyển INACTIVE.

Xác thực & người dùng
User: Lưu tài khoản, mật khẩu BCrypt và vai trò USER, ADMIN.
UserSetting: Ngôn ngữ, giao diện, mục tiêu học mỗi ngày, múi giờ IANA (mặc định Asia/Ho_Chi_Minh), bật/tắt và giờ email nhắc học (1-1 với User).
RefreshToken: Lưu SHA-256 của token ngẫu nhiên 256 bit, không lưu token thô.
PasswordResetToken: Lưu HMAC-SHA256 (khoá RESET_CODE_SECRET, băm kèm id người dùng) của mã OTP 6 số, kèm attempts là số lần nhập sai; không lưu mã thô.

Nội dung học
Topic: Chủ đề dùng chung cho mọi kỹ năng.
FlashcardDeck & Flashcard: Lưu bộ từ và từng từ vựng.
WritingPrompt: Đề viết, kèm các gợi ý (cột JSON hints).
ListeningLesson: Bài nghe (audio, transcript).
ReadingLesson: Bài đọc, các đoạn văn nằm trong cột JSON paragraphs, có giới hạn thời gian.
SpeakingLesson: Bài luyện nói, các câu cần đọc to nằm trong cột JSON prompts (mỗi câu có id ổn định). Không dùng Question.
Exam: Đề kiểm tra tổng hợp nhiều kỹ năng.
Question, QuestionOption: Câu hỏi dùng chung cho bài nghe, bài đọc và bài kiểm tra
(thay cho ExamQuestion riêng). Mỗi câu thuộc đúng một trong ba cha; Service phải đảm bảo điều này.
Đáp án chấp nhận của câu điền từ nằm ở cột JSON acceptedAnswers; Service so khớp không phân biệt hoa/thường.

Tiến độ & kết quả (luôn lưu MySQL)
UserFlashcardProgress: Mức độ nhớ từ, số lần ôn và ngày ôn tiếp theo.
WritingSubmission & AiFeedback: Bài nộp và phản hồi AI; danh sách lỗi (AiFeedbackIssue) nằm trong cột JSON issues.
PracticeAttempt & PracticeAttemptAnswer: Lượt làm bài nghe, đọc và kiểm tra
(gộp ListeningAttempt, ReadingAttempt, ExamAttempt; CHECK đảm bảo đúng một chủ sở hữu).

SpeakingAttempt: Một lần nộp bài luyện nói kèm kết quả AI: sáu điểm thành phần, danh sách điểm cần cải thiện
(JSON improvements) và nhận xét từng câu (JSON promptFeedback, có transcript). Âm thanh KHÔNG được lưu.
Trạng thái GRADING, GRADED, FAILED; điểm để trống cho đến khi GRADED. Vì không còn âm thanh nên không chấm lại được,
AI lỗi thì người học ghi âm lại.

Chat AI
ChatConversation & ChatMessage: Hội thoại và tin nhắn với trợ lý. Gợi ý bài học kèm câu trả lời nằm trong cột JSON links,
có cờ isRefusal cho câu từ chối ngoài phạm vi học tiếng Anh. Tin của trợ lý ghi thêm modelName và số token để theo dõi
chi phí OpenAI. Xoá hội thoại là xoá mềm; đoạn xem trước và số tin nhắn tính khi đọc, không lưu cột riêng.

Thời gian học
StudySession: Một phiên học liên tục, nguồn số liệu biểu đồ Tuần/Tháng của Dashboard (xem mục 2, "Thời gian học").
skill gồm sáu nhóm nội dung cộng CHAT; refId trỏ tới bộ thẻ, đề, bài hoặc hội thoại (không có FK).

Thông báo & email
Notification & UserNotification: Thông báo Admin soạn và bản nhận kèm trạng thái đã đọc.
EmailLog: Lịch sử gửi email. UNIQUE (user, reminderDate) chặn gửi trùng nhắc học trong ngày.

Chưa thiết kế: Recommendation (Dashboard đã bỏ mục gợi ý) và AuditLog cho khối hoạt động của Admin.

2. Luồng nghiệp vụ tối quan trọng (Critical Business Logic)
Lưu kết quả học:
User học Flashcard/làm bài → Backend kiểm tra → lưu MySQL → cập nhật thống kê và gợi ý.
Caffeine chỉ dùng để cache (trong bộ nhớ). Tiến độ học phải luôn lưu trong MySQL.
Flashcard:
Frontend chỉ gửi flashcardId và mức độ nhớ.
Backend tự tính ngày ôn tiếp theo.
Chấm AI:
Backend lưu bài viết trước, gọi OpenAI sau và lưu phản hồi AI.
Không để OpenAI API key trong React.
Luyện nói:
Frontend gửi bản ghi âm lên backend; backend chuyển thành văn bản, chấm bằng OpenAI, lưu SpeakingAttempt
(transcript, điểm, nhận xét) rồi bỏ âm thanh. Không lưu âm thanh nên không cần kho lưu trữ file cho giọng nói.
Chat AI:
Backend lưu tin nhắn của người học trước, gọi OpenAI sau và lưu câu trả lời kèm modelName, số token, gợi ý bài học.
Thời gian học:
Frontend gửi heartbeat khoảng 30 giây một lần khi tab học đang mở. Backend cộng vào StudySession.activeSeconds, mỗi heartbeat
tính tối đa 60 giây; im lặng quá 2 phút thì phiên kết thúc và heartbeat sau mở phiên mới. Biểu đồ Dashboard = tổng
activeSeconds của người dùng gom theo ngày ở múi giờ của họ (UserSetting.timeZone, mặc định Asia/Ho_Chi_Minh), không phải
ngày UTC; thời gian của một phiên tính vào ngày startedAt. Giờ nhắc học (reminderTime) cũng hiểu theo múi giờ này. Phần trăm so với kỳ trước chạy lại cùng truy vấn cho khoảng liền trước.
Bài kiểm tra:
Trắc nghiệm tự chấm bằng đáp án trong database.
Không trả đáp án đúng trước khi User nộp bài.
Email nhắc học:
`@Scheduled` mỗi phút → `EmailSender` → lưu EmailLog.
Không gửi trùng email trong cùng ngày.

3. Module Auth (Xác thực, phân quyền & bảo mật)
Sử dụng Spring Security + JWT để quản lý đăng nhập.
Mật khẩu mã hóa bằng BCrypt.
Phân quyền cơ bản: USER, ADMIN.
USER chỉ xem và sửa dữ liệu của chính mình.
ADMIN quản lý người dùng, nội dung học và thông báo.
JWT secret, OpenAI API key và mật khẩu email đặt trong .env; không commit lên GitHub.

Đã triển khai (backend, 20/09/2026): auth (controller, service, repository, dto, validation), security (JwtService, 401/403 dạng JSON),
mail (EmailSender), seed. Các quy tắc:
- Access token: JWT HS256 (Spring Security OAuth2 Resource Server, Nimbus), sống 15 phút. Claim sub = id người dùng, role, iss = en-learning; không mang email hay tên.
- Refresh token: chuỗi ngẫu nhiên 256 bit trong cookie refresh_token (HttpOnly, SameSite=Lax, Path=/api/auth, Secure theo COOKIE_SECURE), sống 7 ngày. Thân phản hồi không có refreshToken.
  Mỗi lần refresh là xoay vòng. Dùng lại token đã thu hồi quá 10 giây thì thu hồi mọi phiên của tài khoản; trong 10 giây coi là hai tab refresh cùng lúc.
- Đăng nhập: sai mật khẩu và không có tài khoản trả cùng một lỗi (kể cả thời gian phản hồi, nhờ băm BCrypt giả); chỉ báo tài khoản bị khoá khi đã đúng mật khẩu.
- Quên mật khẩu: mã OTP 6 số, hạn 10 phút, dùng một lần, sai 5 lần thì huỷ, xin mã mới cách nhau 60 giây (xin sớm hơn thì bỏ qua lặng lẽ) và mã mới vô hiệu mã cũ.
  forgot-password luôn trả cùng một thông điệp và xử lý nền để không lộ email nào có tài khoản. Đặt lại thành công thì thu hồi mọi refresh token.
- Email: Gmail SMTP khi có MAIL_USERNAME và MAIL_PASSWORD (App Password); không thì ghi mã ra log, và profile prod từ chối khởi động. Mỗi email ghi một dòng email_logs.
- Giới hạn tần suất đăng nhập, check-email, forgot-password bằng bucket4j trong bộ nhớ (mục 4). Khoá tài khoản chặn được đăng nhập, refresh và /auth/me; access token đang sống còn hiệu lực tối đa 15 phút.

Hợp đồng API auth (context-path /api, vỏ thành công {success,data}, vỏ lỗi {message,code,messageKey,fieldErrorKeys}; messageKey là khoá dịch của Frontend):
POST /auth/register {fullName,email,password,confirmPassword} → 201 {token,user} + Set-Cookie
POST /auth/login {email,password} → 200 {token,user} + Set-Cookie
POST /auth/refresh (cookie) → 200 {token,user} + cookie mới; lỗi 401 UNAUTHORIZED
DELETE /auth/session (cookie) → 200 {loggedOut:true} + xoá cookie
GET /auth/me (Bearer) → user; GET /auth/check-email?email= → {available}
POST /auth/forgot-password {email} → 200 {message} cố định
POST /auth/reset-password {email,code,password,confirmPassword} → 200 {message}
Mã lỗi: VALIDATION 400, INVALID_CREDENTIALS 401, ACCOUNT_LOCKED 403, EMAIL_TAKEN 409, INVALID_CODE 400, UNAUTHORIZED 401, FORBIDDEN 403, INTERNAL 500.
/admin/** cần ROLE_ADMIN; mọi đường dẫn khác cần đăng nhập, trừ các endpoint auth ở trên.

Dữ liệu mẫu: trong apps/backend chạy SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run (cần .env theo .env.example). DevSeedRunner (chỉ profile dev) gọi
SeedService.seedIfEmpty(), nạp resources/seed/*.json khi bảng users trống nên chạy lại không nhân đôi. Gồm 3 tài khoản mật khẩu 123456
(hocvien@enlearning.vn USER, admin@enlearning.vn ADMIN, khoa@enlearning.vn USER bị khoá), 6 chủ đề, 6 bộ thẻ (36 từ), 4 bài nghe, 4 bài đọc,
3 bài nói (13 câu), 5 đề viết, 3 đề kiểm tra, 45 câu hỏi (144 phương án). JSON trong resources/seed là nguồn sự thật (mock FE đã xoá).
createdAt của user seed là lúc seed. Chưa seed lịch sử học (tiến độ thẻ, lượt làm bài, study_sessions, thông báo).

4. Kiến trúc triển khai (Application Architecture)
Backend dùng modular monolith: một Spring Boot, chia module auth, flashcard, writing, exam, learning, notification, admin.
Luồng Backend: Controller → Service → Repository → MySQL.
Frontend: React + TypeScript + Ant Design.
Hạ tầng đơn giản, chạy trong một tiến trình, không cần dịch vụ ngoài:
- Caffeine (`spring.cache.*`, `@Cacheable`/`@CacheEvict`): đệm danh sách chủ đề (TTL 5 phút, xoá khi admin ghi); cũng giữ bucket của rate limit.
- `@Scheduled`: email nhắc học mỗi phút (`reminder/`, chống trùng bằng UNIQUE `email_logs` (user_id, reminder_date)); dọn refresh token, OTP hết hạn và phiên học rỗng mỗi giờ (`cleanup/`).
- bucket4j trong bộ nhớ (`ratelimit/`, theo IP): `login` 10/phút, `check-email` 30/phút, `forgot-password` 5/phút, trả 429 `RATE_LIMITED`.
- Email qua `EmailSender` (Gmail SMTP, hoặc ghi log khi chưa cấu hình); audio/ảnh chỉ giữ đường dẫn ngoài.