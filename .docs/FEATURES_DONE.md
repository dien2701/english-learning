# TIẾN ĐỘ: EN-LEARNING

Giai đoạn hiện tại: **Frontend xong với dữ liệu mock; Database đã thiết kế; Backend đã có module Auth và dữ liệu mẫu**.
Backend mới có API Auth; các module còn lại chưa có API. Frontend vẫn lấy toàn bộ dữ liệu từ mock trong `src/mocks`.

Cập nhật: 20/09/2026 (vòng 2: sáng/tối, đa ngôn ngữ, ảnh thẻ, bộ lọc; thiết kế Database; Backend Auth và dữ liệu mẫu)

---

## Backend: Auth và dữ liệu mẫu (xong, chưa nối Frontend)

- Đăng ký, đăng nhập, refresh token (cookie HttpOnly, xoay vòng, phát hiện dùng lại), đăng xuất, kiểm tra email,
  quên và đặt lại mật khẩu bằng OTP 6 số qua Gmail SMTP. Hợp đồng API và quy tắc bảo mật: mục 3 của `ARCHITECTURE.md`.
- Dữ liệu mẫu: `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` trong `apps/backend` nạp 3 tài khoản (mật khẩu `123456`),
  6 chủ đề, 6 bộ thẻ (36 từ), 4 nghe, 4 đọc, 3 nói, 5 đề viết, 3 đề kiểm tra, 45 câu hỏi. Chưa seed lịch sử học.
- 61 test: 33 test đơn vị (Mockito) và 28 test tích hợp chạy trên MySQL thật, tự rollback nên không để lại dữ liệu.
  Test tích hợp đặt tên `*Tests` vì Surefire không nhận đuôi `*IT` (chưa cấu hình Failsafe).
  Đã chạy thật với profile `dev` và kiểm bằng `curl` mọi luồng.
- **Chỗ lệch giữa Frontend và Backend, xử lý khi nối** (chưa sửa vì lần này không đụng Frontend):
  1. Backend không còn trả `refreshToken` trong thân phản hồi (nó nằm trong cookie), nhưng `authService.ts` vẫn lưu nó ở localStorage
     và kiểu `AuthSession.refreshToken` còn là bắt buộc.
  2. `client.ts` chưa bật `withCredentials` và chưa tự gọi `POST /auth/refresh` khi gặp 401, nên phiên chỉ sống 15 phút.
  3. Mock Dashboard, Hồ sơ đọc tài khoản theo token dạng `mock.<id>.…` nên không đọc được JWT thật; nối module nào thì bỏ mock của module đó.
  4. `GET /auth/check-email` chưa giới hạn tần suất (chưa có Redis).
  5. Cookie refresh dùng `SameSite=Lax`, chạy được khi Frontend và Backend cùng site (localhost khác cổng); khác tên miền gốc thì phải đổi
     sang `SameSite=None; Secure`.

---

## Database (thiết kế xong)

- `apps/backend/src/main/resources/db/migration/V1__init_schema.sql`: 26 bảng,
  40 khoá ngoại, 85 index, 20 ràng buộc CHECK. Đã chạy thử Flyway và
  Hibernate `validate` trên MySQL 9.0 thành công, kèm ghi/đọc thử các cột JSON
  và ràng buộc CHECK của 5 bảng mới qua JPA.
- 26 entity JPA ở `apps/backend/.../entity`, 19 enum ở `entity/enums`.
- Đã rút gọn từ 35 bảng xuống 21 (gộp danh sách nhỏ thành cột JSON), rồi khôi phục
  Chat AI và Luyện nói và thêm `study_sessions`, lên 26 bảng (20/09/2026):
  `chat_conversations`, `chat_messages`, `speaking_lessons`, `speaking_attempts`, `study_sessions`.
- Luyện nói không lưu âm thanh, chỉ lưu transcript, điểm và nhận xét; lượt chấm lỗi
  là `FAILED` và người học ghi âm lại.
- Thời gian học ở Dashboard tính từ `study_sessions` (heartbeat ~30 giây, gom theo
  ngày theo `user_settings.time_zone`, mặc định Asia/Ho_Chi_Minh). Cách tính chi tiết:
  mục 2 của `ARCHITECTURE.md`.
- Chi tiết bảng và quy ước: mục 1 của `ARCHITECTURE.md`.
- Repository, Service, Controller, JWT và dữ liệu mẫu đã có cho module Auth và seed (mục trên). Chưa làm cho các module còn lại.

---

## Trạng thái 10 chức năng

| # | Chức năng | Trạng thái |
|---|---|---|
| 1 | Đăng ký và Đăng nhập | Xong (Frontend dùng mock; Backend đã có API) |
| 2 | Dashboard | Xong |
| 3 | Flashcard | Xong |
| 4 | Luyện viết (AI chấm) | Xong |
| 5 | Luyện nghe | Xong |
| 6 | Luyện đọc | Xong |
| 7 | Luyện nói (thu âm thật) | Xong |
| 8 | Bài kiểm tra | Xong |
| 9 | Chat với AI | Xong |
| 10 | Quản trị hệ thống | Xong |

---

## Vòng sửa mới nhất

- **Chế độ sáng/tối chạy đúng toàn hệ thống.** Trước đó hỏng: card vẫn trắng,
  chữ vẫn đen trên nền tối, vì màu Tailwind là mã hex cứng. Đã chuyển sang
  biến CSS và tách token theo vai trò. Ant Design đổi theo qua `ThemeContext`.
- **VI/EN đổi cả nhãn giao diện lẫn thẻ nội dung.** Thêm kiểu `L10n` và hook
  `useLanguage`; tên/mô tả bộ từ, bài học, đề bài đều có hai bản.
- **Câu hỏi và bài tập hoàn toàn bằng tiếng Anh** ở cả hai chế độ ngôn ngữ.
- **Flashcard có ảnh minh hoạ cho từng từ**, kèm khối dự phòng khi ảnh hỏng,
  bị chặn, hoặc request treo (có ngưỡng chờ 5 giây).
- **Khung tìm kiếm và bộ lọc thiết kế lại**: ô tìm cao 56px chữ 16px trên một
  hàng riêng, mỗi ô lọc có nhãn in hoa phía trên, thêm dòng đếm kết quả và
  nút xoá lọc.

---

## Nền tảng

- **Design tokens** theo chủ đề LinguaMerse — `src/styles/tokens.css`,
  `tailwind.config.js`, `src/theme/palette.ts`, `themeConfig.ts`. Có cả chế
  độ tối. Đã bỏ sạch mọi sắc xanh lam.
- **Thứ tự lớp CSS**: reset và CSS-in-JS của Ant Design được đẩy vào
  `@layer antd` nằm dưới Tailwind. Xem mục 4 của STYLEGUIDE.
- **Lớp API** — `src/shared/api/client.ts`: axios instance, interceptor gắn
  JWT, chuẩn hoá mọi lỗi về `ApiError`, xử lý 401 tập trung.
- **Mock API** — `src/mocks`: bộ định tuyến riêng khớp method + đường dẫn
  động, gắn vào axios qua adapter. Bật/tắt bằng `VITE_USE_MOCK` trong `.env`.
- **Hook dùng chung** — `useApi`, `useDebounced`, `useCountdown`, `useSpeech`,
  `useRecorder`.
- **Primitive dùng chung** — `Card`, `Chip`, `LevelChip`, `PageHeader`,
  `FilterBar`, `Skeleton`, `ErrorState`, `EmptyBlock`, `FullPageLoader`.
- **Component luyện tập** — `QuestionList`, `ExamTimer`, `AudioPlayer`,
  `ResultSummary` (`ScoreRing`, `ResultOverview`, `AnswerReview`).

Tài khoản mẫu, mật khẩu đều `123456`:

| Email | Vai trò |
|---|---|
| `hocvien@enlearning.vn` | USER |
| `admin@enlearning.vn` | ADMIN |
| `khoa@enlearning.vn` | USER, đang bị khoá |

---

## Ghi chú theo module

**1. Xác thực** — Đăng nhập, đăng ký, quên và đặt lại mật khẩu. Thông báo lỗi
cố ý không tiết lộ email có tồn tại hay không. Lỗi server đổ về đúng ô nhập
qua `ApiError.fieldErrors`. Phiên khôi phục được sau khi tải lại trang. Tài
khoản ADMIN vào thẳng khu quản trị.

**2. Dashboard** — Đúng ba khối theo yêu cầu: banner bài đang học dở kèm nút
"Tiếp tục học", biểu đồ thời gian học Tuần/Tháng có so sánh kỳ trước, và danh
sách bài đã tham gia lọc được theo trạng thái. Đã bỏ ô chỉ số và mục gợi ý.

**3. Flashcard** — Danh sách có tìm kiếm và lọc theo chủ đề, trình độ, trạng
thái. Phiên học lật thẻ, ba mức tự đánh giá, có phím tắt. Phát âm bằng giọng
đọc của trình duyệt. Tiến độ lưu sau từng thẻ nên thoát giữa chừng không mất.
Màn hình kết quả liệt kê từ cần ôn lại.

**4. Luyện viết** — Đếm số từ, cảnh báo khi chưa đủ tối thiểu. Nộp xong chuyển
sang trạng thái "AI đang chấm" rồi hỏi lại tới khi có kết quả. Phản hồi gồm
điểm tổng, ba điểm thành phần, nhận xét và danh sách lỗi kèm cách sửa. Có
trạng thái "cần chấm lại" để không mất bài khi AI không trả kết quả.

**5. Luyện nghe** — Trình phát có play/pause, nghe lại, bốn mức tốc độ. Chưa
có tệp mp3 nên đọc transcript bằng Web Speech API. Nộp bài rồi mới trả đáp án
và transcript.

**6. Luyện đọc** — Bài đọc bên trái, câu hỏi bên phải. Có giới hạn thời gian,
hết giờ tự nộp.

**7. Luyện nói** — Thu âm thật bằng MediaRecorder, có sóng âm theo thời gian
thực và nghe lại được. Xử lý cả trường hợp trình duyệt không hỗ trợ hoặc người
dùng từ chối quyền micro. Bản ghi chỉ nằm trong trình duyệt, không gửi đi đâu.

**8. Bài kiểm tra** — Đếm ngược, thanh tiến độ dính, nhảy nhanh tới từng câu,
xác nhận trước khi nộp, hết giờ tự nộp. Kết quả tách điểm theo kỹ năng.

**9. Chat với AI** — Trang đầy đủ và cửa sổ thu nhỏ mở từ sidebar. Trả lời kèm
liên kết bài học gợi ý. Câu hỏi ngoài phạm vi học tiếng Anh bị từ chối lịch sự.

**10. Quản trị** — Khu vực tách riêng, chặn tài khoản không phải ADMIN. Bảng
điều khiển có biểu đồ đăng ký và kho nội dung. Quản lý người dùng, nội dung,
chủ đề, thông báo. Nội dung đang nằm trong lịch sử học thì **không cho xoá**,
chỉ được chuyển sang ngừng hoạt động.

---

## Kiểm tra đã chạy

- `tsc -b` sạch, `eslint src` sạch, `npm run build` thành công.
- Tab trình duyệt mới: không có lỗi console.
- Đã rà bằng mắt: Dashboard, Flashcard, Luyện nghe, Luyện nói, Bài kiểm tra,
  Chat, Quản trị (bảng điều khiển, người dùng, nội dung).

---

## Nợ kỹ thuật

- **Đa ngôn ngữ mới xong một phần.** Hạ tầng đã đủ (bộ khoá dịch đầy đủ ở
  `vi.json`/`en.json`, kiểu `L10n`, hook `useLanguage`) và **toàn bộ nội dung**
  đã song ngữ. Nhưng **45 file** trong `src/pages` và `src/components` vẫn còn
  chuỗi tiếng Việt viết cứng, nên khi chọn EN những chỗ đó chưa đổi. Đã xong:
  sidebar, header, footer, bộ lọc, sáu trang danh sách, Dashboard. Còn lại:
  các trang làm bài, kết quả, hồ sơ, cài đặt, chat, quản trị, trang lỗi.
  Khoá dịch cho những chỗ này **đã có sẵn** trong hai file locale, chỉ cần
  thay chuỗi bằng `t('...')`.

- Gói build ra một chunk 1,77 MB (gzip 533 kB), chủ yếu do Ant Design. Nên
  tách chunk hoặc nạp động khu quản trị.
- Thư mục lồng `english-learning/english-learning`.
- Ảnh bìa bộ thẻ lấy từ Unsplash qua đường dẫn ngoài; khi có backend nên
  chuyển sang Cloudinary.

## Đợt 0 (FE): nối Auth với backend, mock theo module

- `client.ts`: `withCredentials`, 401 thì `POST /auth/refresh` một lần (dùng chung một promise) rồi gửi lại; bỏ `refreshToken` khỏi `AuthSession`/localStorage.
- `VITE_MOCK_MODULES` thay `VITE_USE_MOCK`: adapter chọn mock/BE theo tiền tố từng request; `/auth/*` không bao giờ mock, đã xoá `mocks/handlers/auth.ts`.
- Mock Dashboard/Hồ sơ đọc claim `role` của JWT thật (ADMIN dùng admin mock, còn lại dùng học viên mock).
- Vite proxy `/api` sang `localhost:8080`, `VITE_API_BASE_URL=/api`; `.env` và `.env.example` đã cập nhật.
- Phiên 0a: lint sạch. `ContentEditorDrawer` dùng `Form.useWatch` + `useEffectEvent` (hết setState trong effect, hết tải lại do `onClose` inline); `ProfilePage` suy ra avatar từ dữ liệu; bỏ `any` (kiểu `AdminContentChild`).

## Đợt 1 (BE): Topic + Flashcard (phiên 1a)

- `GET /topics` (kèm `itemCount` = số bộ thẻ ACTIVE); module `topic/` và `flashcard/` cạnh `auth/`. Thêm `common/L10n`, `common/PageResponse` (trang từ 1, tối đa 100/trang).
- `GET /flashcard/decks` (lọc `search/topicId/level/status`), `GET /flashcard/decks/:id`, `POST .../progress`, `POST .../finish`. Người học chỉ thấy bộ ACTIVE; bộ ẩn/không có/thẻ bộ khác trả 404, mức nhớ sai 400.
- Trạng thái suy ra từ `user_flashcard_progress`: `learnedCards` = số thẻ REMEMBERED; NOT_STARTED khi chưa đánh giá thẻ nào, COMPLETED khi mọi thẻ REMEMBERED.
- `ReviewScheduler`: NOT_REMEMBERED → 0 ngày (ôn lại sau 10 phút); ALMOST → 1 ngày rồi giữ khoảng cũ; REMEMBERED → bậc kế của 3/7/14/30/60/120/180 ngày.
- Test: `FlashcardApiTests` (MockMvc) + `ReviewSchedulerTest`, xanh. Chưa làm Caffeine (để 6a).

## Đợt 1 (BE): Hồ sơ, Cài đặt, đổi mật khẩu, heartbeat (phiên 1b)

- `GET|PATCH /profile` (đổi tên/email/SĐT/avatar; email trùng 409, avatar quá 500 ký tự 400), `GET|PATCH /settings` (language/theme viết thường, `reminderTime` HH:mm, `dailyGoalMinutes` 5..600, `timeZone` IANA; tự tạo dòng cài đặt nếu thiếu).
- `POST /profile/password`: sai mật khẩu hiện tại 400 `WRONG_PASSWORD`; thu hồi MỌI refresh token rồi cấp cookie mới cho thiết bị đang gọi (cookie chỉ gửi kèm `/auth/*` nên không nhận ra phiên hiện tại).
- `POST /study/heartbeat`: cộng tối đa 60 giây/lần, im lặng quá 2 phút hoặc đổi kỹ năng/`refId` thì mở phiên mới (bắt đầu 0 giây). Số liệu Profile: `totalMinutes` từ `study_sessions`, `masteredWords` = thẻ REMEMBERED.
- Test: `ProfileApiTests`, `StudySessionServiceTest`, xanh.

## Đợt 1 (FE): nối Topic, Flashcard, Hồ sơ, Cài đặt (phiên 1c)

- `.env.example`: bỏ `topics,flashcard,profile,settings` khỏi `VITE_MOCK_MODULES` (người dùng tự sửa `.env` cục bộ). Kiểu FE đã khớp DTO; thêm `UserSettings.timeZone?`.
- `useStudyHeartbeat(skill, refId)` gửi `POST /study/heartbeat` mỗi 30 giây khi tab hiện và có tương tác; gắn vào `FlashcardStudyPage` (kỹ năng khác gắn ở đợt sau).
- Hồ sơ: bỏ tải ảnh (data URL vượt cột 500 ký tự của BE), thay bằng ô nhập `avatarUrl`. Lint và build sạch.

## Đợt 1 (đóng đợt, phiên 1z)

- Folder Postman "Dot 1 - Flashcard, Profile" (35 request) đã thêm vào collection; bảng "Kiểm tra hoàn thành" chốt theo DTO thật.

## Đợt 2 (phiên 2a: bộ chấm điểm chung)

- `practice/service/AnswerGrader`: chấm trắc nghiệm theo `QuestionOption`, điền từ so `acceptedAnswers` (bỏ khoảng trắng thừa, không phân biệt hoa/thường); câu bỏ trống tính sai; điểm thang 10 một chữ số; tách điểm theo `Skill`.
- `PracticeAttemptService.submitListening/Reading/Exam`: chỉ nhận bài ACTIVE (404 nếu không), lưu `PracticeAttempt` COMPLETED + một `PracticeAttemptAnswer` cho MỖI câu; nộp lại = lượt mới; `timedOut` khi quá giới hạn + 10 giây (không lưu DB, suy ra từ `durationSeconds`).
- DTO `SubmitRequest`/`AnswerSubmission`; sai `questionId`/`optionId`/trùng câu/text > 500 ký tự: 400 `errors.invalidAnswers`. Test: `AnswerGraderTest`, `PracticeAttemptServiceTest`.

## Đợt 2 (phiên 2b: endpoint Nghe, Đọc, Kiểm tra, lịch sử)

- `GET /listening/lessons|/reading/lessons|/exams` (lọc `search/topicId/level/status`, phân trang) và `GET .../{id}`: chỉ nội dung ACTIVE, có `isCompleted`/`lastScore` (exam: `status` NOT_TAKEN/COMPLETED); chi tiết không có đáp án, giải thích, transcript.
- `POST .../{id}/submit` trả `PracticeResultResponse` (điểm, từng câu kèm đáp án đúng, `transcript` cho bài nghe, `breakdown` cho đề, `timedOut`); `GET /attempts` (lọc `skill`, `SPEAKING` luôn rỗng) và `GET /attempts/{id}` (lượt người khác: 404), `detailPath` dạng `/listening/result/{id}`.
- Test tích hợp `PracticeApiTests` (10 ca: lộ đáp án, 400, 404/401, nộp trễ, tách điểm, quyền xem lượt, heartbeat); bộ `practice` 22 test xanh.

## Đợt 2 (phiên 2c: nối FE)

- `.env.example`: bỏ `listening,reading,exams,attempts` khỏi `VITE_MOCK_MODULES` (người dùng tự sửa `.env` cục bộ). Kiểu FE: `ListeningDetail.audioUrl` nhận null, `PracticeResult.timedOut?`; các kiểu còn lại đã khớp DTO.
- `useStudyHeartbeat` gắn vào trang làm bài Nghe, Đọc, Kiểm tra. Lint và build sạch.

## Đợt 3 (phiên 3a: BE Dashboard/Thống kê)

- `GET /dashboard/summary[?status]` (bộ thẻ học dở là "Tiếp tục học"; Nghe/Đọc/Kiểm tra theo lượt nộp mới nhất của từng bài; tối đa 20 bài), `GET /dashboard/study-time?period=WEEK|MONTH` (tuần T2-CN, tháng 4 nhóm; gom ngày theo `UserSetting.timeZone`, kèm kỳ trước và `changePercent`, chia 0 an toàn), `GET /statistics/overview` (điểm TB, số lượt, chênh 30 ngày cho Nghe/Đọc/Kiểm tra; Viết/Nói thêm ở đợt 4). `period`/`status` sai: 400.
- Truy vấn tổng hợp là phương thức repository riêng (`findSlices`, `deckActivity`, `listeningAverage`/`readingAverage`/`examAverage`); service ở package `dashboard`.
- `HistorySeedService` (profile dev/test) nạp 60 ngày phiên học (có phiên 23:30 và 00:10 giờ VN), tiến độ thẻ, lượt làm bài cho `hocvien@enlearning.vn`; `DevSeedRunner` gọi cả khi DB đã có người dùng.
- Test: `StudyTimeServiceTests` (6, biên tuần/tháng, lệch múi giờ, kỳ trước rỗng) và `DashboardApiTests` (8, 401/400, rỗng, lọc trạng thái, cách ly người dùng, seed) xanh.

## Đợt 3 (phiên 3b: nối FE)

- `.env.example`: bỏ `dashboard,statistics` khỏi `VITE_MOCK_MODULES` (người dùng tự sửa `.env` cục bộ). Kiểu FE đã khớp DTO; trang Thống kê ẩn điểm/chênh lệch (hiện "–") với kỹ năng chưa có lượt nào. Lint và build sạch.

## Đợt 3 (phiên 3z: đóng đợt)

- Folder Postman "Dot 3 - Dashboard, Thống kê" (13 request: thành công, lọc trạng thái, 400, 401, cách ly bằng `adminToken`); bảng kiểm tra trong `dot-3-dashboard.md` khớp DTO thật. "Tiêu chí xong" chờ bạn tự kiểm.

## Đợt 4 (phiên 4a: khung AI + Luyện viết BE)

- Gói `ai/`: `AiProperties` (`app.ai.*`), `WritingGrader` + `FakeWritingGrader` (nạp khi `OPENAI_API_KEY` trống; `[fail]` lỗi một lần rồi chấm lại được, `[fail-always]` luôn lỗi), executor `aiTaskExecutor` có giới hạn.
- Module `writing/`: 6 endpoint `/writing/**`; nộp bài lưu `GRADING` (commit) rồi chấm nền, ghi `AiFeedback` (`issues` JSON, `modelName`). Lỗi AI hoặc quá hạn (`AI_GRADING_TIMEOUT`, 60s, kiểm khi đọc) thì `NEEDS_RETRY`; `regrade` chuyển trạng thái nguyên tử, sai trạng thái là 409 `INVALID_STATE`.
- Khoá lỗi mới cho FE (4d): `errors.field.essayTooShort`, `errors.field.essayTooLong`, `errors.invalidState`. Test: `WritingApiTests` (8), `FakeWritingGraderTest` (3) xanh. Chưa lưu số token cho bài viết (`ai_feedbacks` không có cột; chat thì có).

## Đợt 4 (phiên 4b: Chat AI BE)

- `ChatAssistant` + `FakeChatAssistant` (từ khoá, từ chối câu ngoài phạm vi, `[fail]` ép lỗi). Module `chat/`: `/chat/conversations` (list, tạo, chi tiết, PATCH đổi tên, DELETE xoá mềm), `/messages`, `/starters`.
- Gửi tin: lưu tin người học (commit) → gọi AI ngoài giao dịch → lưu trả lời kèm `modelName`, token, `links`. AI lỗi: 503 `AI_UNAVAILABLE`. Ngữ cảnh gửi AI tối đa 10 tin, 1000 ký tự/tin, 6000 tổng; tin tối đa 2000 ký tự.
- Gợi ý bài học do BE chọn nội dung ACTIVE thật (`/writing/:id`...), AI chỉ nêu nhóm kỹ năng. Khoá i18n mới cho FE (4d): `errors.field.messageRequired|messageTooLong|titleRequired|titleTooLong`, `errors.aiUnavailable`. Test `ChatApiTests` (8), `ChatContextTest` (2) xanh.

## Đợt 4 (phiên 4c: Luyện nói BE)

- `SpeakingGrader` + `FakeSpeakingGrader` (transcript = câu đề, điểm cố định; tên tệp chứa `fail` thì lỗi). Module `speaking/`: 4 endpoint `/speaking/**`; nộp multipart (`promptIds`+`audio`+`durationSeconds`), kiểm số lượng, câu hợp lệ, định dạng (415), kích thước 5MB/tệp, 15MB/lần (413).
- Lưu lượt `GRADING` rồi chấm nền; âm thanh chỉ ở bộ nhớ, không có cột/tệp lưu. AI lỗi hoặc quá hạn (`AI_GRADING_TIMEOUT`) là `FAILED` (không chấm lại, người học ghi âm lại). Bài `isCompleted`/`lastScore` chỉ tính lượt `GRADED`.
- Khoá i18n mới cho FE (4d): `errors.field.audioMismatch`, `errors.field.invalidPrompt`, `errors.audioTooLarge`, `errors.audioUnsupported`. `SpeakingApiTests` (7) xanh; 413 ở tầng servlet (`max-file-size`) chưa test bằng MockMvc.

## Đợt 4 (phiên 4d: nối FE)

- `.env.example`: `VITE_MOCK_MODULES=notifications,admin`. Luyện nói: `speakingService.submit` gửi multipart (`promptIds`+`audio`+`durationSeconds`, blob giữ đúng mime trình duyệt thu); kiểu `SpeakingResult` có `status`, điểm tuỳ chọn, `transcript` từng câu.
- Trang kết quả nói hỏi lại tới khi hết `GRADING`, có màn `FAILED` (ghi âm lại). Trang kết quả viết: nút chấm lại cập nhật trạng thái và hỏi lại; nộp bài thiếu số từ bị chặn ở nút (BE trả 400). Chat không cần sửa mã. Thêm 16 khoá i18n VI/EN. Lint và build sạch.

## Đợt 4 (phiên 4z: đóng đợt)

- Ba luồng Viết (4a), Chat (4b), Nói (4c) chạy trọn với bản giả: trạng thái `GRADING`/lỗi/chấm lại/ghi âm lại đều hoạt động. BE: `./mvnw test` (35 test AI xanh); FE: `npm run lint` và `npm run build` sạch. FE bỏ `writing,chat,speaking` khỏi mock (`.env.example` đã sửa). Tiêu chí "Người dùng chỉ xem được dữ liệu của mình" được xác thực qua `*ApiTests` (cách ly bằng `userId` ở repository/service).

## Đợt 5 (phiên 5a: BE quản trị nội dung)

- `admin/`: `/admin/content` (GET danh sách lọc `search,skill,status,level` + trang, GET/PUT/PATCH/DELETE `/{id}`, POST 201) và `/admin/topics` (GET, POST, PUT, DELETE). Body POST/PUT đa hình theo `skill` (`VOCABULARY|LISTENING|READING|WRITING|SPEAKING|EXAM`, chủ đề theo `topicId`); GET chi tiết trả `payload` cùng dạng body kèm id thẻ/câu hỏi/câu nói.
- PUT khớp phần tử con theo `id` (có id sửa tại chỗ, không id tạo mới, vắng mặt xoá mềm). Nội dung đã có trong lịch sử học (`inUse`): DELETE hoặc bớt phần tử con trả 409 `CONTENT_IN_USE`; chủ đề còn nội dung không xoá được. PATCH `{status}` chuyển ACTIVE/INACTIVE, người học thấy ngay. `DataIntegrityViolationException` cũng thành 409, không còn 500.
- Slug chủ đề sinh từ tên (bỏ dấu, thêm `-2`, `-3` nếu trùng, kể cả slug đã xoá mềm). Chưa có Caffeine nên chưa xoá cache (làm ở đợt 6).
- `AdminContentApiTests` (8 test): 401/403 mọi endpoint, tạo/đọc lại 6 loại, 400 `fieldErrorKeys`, INACTIVE ẩn với người học, PUT theo id, 409 khi đã có lượt làm, xoá mềm, vòng đời chủ đề. `./mvnw test`: 152 test xanh.

## Đợt 5 (phiên 5b: BE người dùng, thông báo, dashboard admin)

- `/admin/users` (GET lọc `search,role,status` + trang, GET `/{id}`, PATCH `{status?,role?}`): khoá thu hồi mọi refresh token (access token cũ sống tối đa 15 phút, đã chốt giữ nguyên); không tự đổi mình (409), `PENDING` không nhận (400). `completedLessons` = lượt Nghe/Đọc/Thi đã nộp + Viết đã chấm + Nói đã chấm. `/admin/dashboard`: tổng quan, 6 tháng đăng ký (giờ VN), số nội dung 6 loại, `activities` rỗng.
- `/admin/notifications` (GET, POST 201 với `send:true` gửi ngay, POST `/{id}/send`, 409 nếu đã gửi). Nhóm nhận chỉ tính tài khoản ACTIVE: ALL, ACTIVE (`lastActiveAt` trong 30 ngày), INACTIVE (còn lại), ADMIN; tạo `UserNotification` cho từng người, `recipientCount` khớp số dòng.
- Người học: `/notifications` (trang), `/unread-count`, `PATCH /{id}/read` (id là id bản nhận; của người khác 404), `POST /read-all`. Tiêu đề/nội dung lặp lại cho VI và EN, `icon=campaign`, `path=/notifications`.
- `AdminUserNotificationApiTests` (6 test); `./mvnw test`: 158 test xanh.

## Đợt 5 (phiên 5c: nối FE)

- `.env.example`: `VITE_MOCK_MODULES=` (rỗng); đã xoá `mocks/handlers/admin.ts` và phần thông báo trong `misc.ts`. Kiểu `AdminContentPayload` đổi thành union theo `skill` khớp BE; `contentMapping.ts` đổi qua lại giữa giá trị form và payload (trắc nghiệm `MULTIPLE_CHOICE` ↔ `SINGLE_CHOICE`, đúng một đáp án; đoạn Đọc cách nhau dòng trống).
- Form nội dung: chọn chủ đề bằng `TopicField` (`topicId`), thêm thời lượng/giới hạn thời gian, giải thích câu hỏi, kỹ năng từng câu cho đề kiểm tra; Viết có đề bài + số từ + gợi ý; Nói có nghĩa tiếng Việt. Chủ đề nhập tên VI/EN. `topicName` và `lastActiveAt` tuỳ chọn; Dashboard hiện trạng thái rỗng cho hoạt động. Thêm khoá i18n `errors.field.*` và `admin.*` VI/EN. Lint và build sạch.

## Đợt 5 (phiên 5z: đóng đợt)

- Đợt 5 xong: BE quản trị nội dung/chủ đề/người dùng/thông báo/dashboard + hộp thư người học, FE đã nối (không còn module mock). BE `./mvnw test` 158 test xanh; FE lint và build sạch. Folder Postman "Dot 5 - Admin, Thong bao" (63 request) và bảng "Kiểm tra hoàn thành" đã chốt theo DTO thật. Các ô "Tiêu chí xong" chờ bạn tự kiểm rồi tick.

## Đợt 6 (phiên 6a: hạ tầng phụ)

- Nhắc học: `reminder/StudyReminderService` (`@Scheduled` mỗi phút) chọn người đã bật nhắc, đến giờ theo múi giờ riêng (bù tối đa `app.reminder.catch-up`=1h), chưa đủ mục tiêu; ghi `email_logs` PENDING trước khi gửi, UNIQUE (user, ngày) chặn trùng, lỗi gửi ghi FAILED và không thử lại trong ngày. Template `StudyReminderMailTemplate` VI/EN.
- Giới hạn tần suất: `ratelimit/` (bucket4j + Caffeine, theo IP) cho `POST /auth/login` (10/phút), `GET /auth/check-email` (30/phút), `POST /auth/forgot-password` (5/phút), chỉnh ở `app.rate-limit.*`; 429 `RATE_LIMITED` kèm `Retry-After`, `messageKey` `errors.tooManyRequests` (đã thêm vào `vi.json`/`en.json`).
- Caffeine: thực tế chưa có từ đợt 1, nay thêm `CacheConfig` + `spring.cache.*`; đệm `topics` (TTL 5 phút) cho `TopicService.list`, xoá ở mọi thao tác ghi chủ đề/nội dung của admin. Test đặt `spring.cache.type=none`, `app.scheduling.enabled=false`, `app.rate-limit.enabled=false`.
- Dọn dẹp: `cleanup/CleanupService` (`@Scheduled` phút 15 hằng giờ) xoá refresh token và OTP hết hạn quá 1 ngày, phiên học 0 giây cũ hơn 30 ngày (`app.cleanup.*`).
- Test mới: `RateLimitApiTests`, `StudyReminderServiceTests`, `CleanupServiceTests`; cả bộ BE 171 test xanh.

## Đợt 6 (phiên 6z: đóng đợt)

- Đợt 6 xong: nhắc học, rate limit 429, Caffeine, job dọn dẹp; `ARCHITECTURE.md` mục 4 đã cập nhật (Caffeine, `@Scheduled`, bucket4j thay Redis/RabbitMQ/Cloudinary). BE 171 test xanh (phiên 6a). Folder Postman "Dot 6 - Gioi han tan suat" (3 request lặp bằng script) và bảng "Kiểm tra hoàn thành" đã chốt. Hai ô "Tiêu chí xong" về email và 429 chờ bạn tự kiểm rồi tick.

## Đợt 7 (phiên 7a: song ngữ trang làm bài và kết quả)

- Quét `pages`/`components` của Flashcard, Viết, Nghe, Đọc, Nói, Kiểm tra, `practice`: không còn chuỗi tiếng Việt cứng ngoài chú thích code; mọi khoá `t(...)` đã có ở cả `vi.json` và `en.json` (hai file khớp bộ khoá). Lint, build sạch; không phải sửa mã nguồn.

## Đợt 7 (phiên 7b: song ngữ Hồ sơ, Cài đặt, Thống kê, Thông báo, Chat)

- Quét `profile/`, `statistics/`, `notification/`, `chat/`, `components/chat`, `StudyTimeChart`: chỉ còn tiếng Việt trong chú thích code; mọi khoá `t(...)` đủ ở `vi.json` và `en.json`. Sửa duy nhất `alt` ảnh đại diện ở `ProfilePage.tsx` dùng `profile.avatarUrl`. Lint, build sạch.

## Đợt 7 (phiên 7c: song ngữ Quản trị, xác thực, lỗi BE; đóng đợt)

- Form nội dung quản trị (`components/admin/content-forms`, 8 file) và tab CSV chuyển sang `t()`/`Trans`; thêm namespace `contentForm` cùng các khoá còn thiếu (`common.saved/created`, `admin.addContent/editContent/titleVi/titleEn/detailsForSkill/loadError/saveError`, `errors.invalidAnswers` mà BE trả) vào `vi.json`/`en.json`.
- 6 trang danh sách + Dashboard + `WritingPracticePage` hiển thị lỗi qua `describe()` (dịch theo `messageKey`) thay vì `error.message`; `useApi` gắn `errors.unknown`; placeholder email đổi sang `name@example.com`.
- Mọi khoá `messageKey` BE dùng đều có ở cả hai locale; quét mã ngoài chú thích không còn chuỗi tiếng Việt hiển thị. Lint, build sạch. Hai ô duyệt giao diện VI/EN và sáng/tối chờ bạn tự kiểm rồi tick.

## Đợt cuối (phiên 9a: xoá mock, chia chunk, tài liệu)

- Xoá `src/mocks`, `mockAdapter.ts`, `VITE_MOCK_MODULES`/`USE_MOCK`; `client.ts` chỉ còn adapter thật, refresh 401 áp cho mọi request. `.env` và `.env.example` chỉ còn `VITE_API_BASE_URL`.
- Bỏ dòng "mã xác thực luôn là 123456" ở trang Quên mật khẩu (mã thật gửi qua email); đổi lời nhắc tài khoản mẫu ở trang Đăng nhập thành "tài khoản seed dev".
- Chia build: `AdminArea` nạp động (`React.lazy`), `vite.config.ts` tách `vendor-antd`, `vendor-charts`, `vendor`; bundle chính 1,55 MB → 241 kB. Lint, build sạch.
- `ARCHITECTURE.md`, `.agent/AGENTS.md`, `CLAUDE.md` bỏ tham chiếu Redis, RabbitMQ, Cloudinary, mock.

## Đợt 8 (phiên 8a: người dùng demo + cờ seed)

- Cờ pp.seed.mode (SEED_MODE; if-empty mặc định | 
eset-demo) qua SeedProperties; DevSeedRunner ở 
eset-demo xoá user demo rồi nạp lại.
- DemoUserSeedService: 47 user giả (Random seed cố định, email @demo.enlearning.vn, avatar i.pravatar.cc, createdAt rải 6 tháng, 3 LOCKED, 5 im lặng >30 ngày) + user_settings đa dạng; cùng 3 tài khoản cố định thành 50.
- UserRepository: countByEmailSuffixIncludingDeleted, deleteByEmailSuffix, ackdate (SQL native vì created_at không sửa được qua entity). Test: DemoUserSeedServiceTests. Compile sạch.

## Đợt 8 (phiên 8b: chủ đề + bộ thẻ demo)

- 
esources/seed-demo/topics.json (10 chủ đề) và decks.json (15 bộ × 20 thẻ, 4 mảng: giao tiếp, IELTS/TOEIC, công sở/IT, học thuật; 2 bộ INACTIVE). Sinh từ nguồn văn bản tự soạn bằng script tạm, không chép đề bản quyền.
- SeedService đọc topics/decks từ seed-demo/, giữ chủ đề cũ ở seed/topics.json cho lesson cũ tới 8c/8d; SeedDeck thêm status tuỳ chọn. SeedServiceTests cập nhật (15 deck, 300 thẻ). Compile sạch.

## Đợt 8 (phiên 8c: bài nghe + bài đọc demo)

- `resources/seed-demo/listening.json` (12 bài, `audioUrl` null nên FE đọc transcript bằng giọng trình duyệt, 5-6 câu/bài, gồm dạng IELTS/TOEIC, 1 bài INACTIVE) và `reading.json` (12 bài 130-300 từ, 6-8 câu, có True/False/Not Given, 1 bài INACTIVE). Sinh từ script tạm, nội dung tự soạn.
- `SeedService` đọc nghe/đọc từ `seed-demo/`; `SeedListening`/`SeedReading` thêm `status` tuỳ chọn. `SeedServiceTests` cập nhật (12 + 12 bài, ≥146 câu). Compile + test-compile sạch.

## Đợt 8 (phiên 8d: viết, nói, đề thi demo)

- `seed-demo/writing.json` (12 đề), `speaking.json` (12 bài × 5 câu), `exams.json` (5 đề, 167 câu, gồm trắc nghiệm/điền từ/True-False-Not Given); mỗi loại 1 mục `INACTIVE`. Sinh từ script tạm, nội dung tự soạn.
- `SeedService` chỉ còn đọc `users.json` từ `seed/`; bỏ nạp chủ đề cũ. `SeedWriting`/`SeedSpeaking`/`SeedExam` thêm `status`. `SeedServiceTests` cập nhật (10 chủ đề, 12+12+12+12 bài, 5 đề, 313 câu). Compile + test-compile sạch.

## Đợt 8 (phiên 8e: lịch sử học demo)

- `HistorySeedService` mở rộng cho học viên mẫu chính: 90 ngày học (14 ngày liền gần đây, cuối tuần ít hơn, đủ 7 kỹ năng), 36 lượt Nghe/Đọc/Kiểm tra điểm tăng dần, tiến độ 6 bộ thẻ (new/learning/mastered, có thẻ đến hạn hôm nay).
- Luyện viết 7 bài (6 `GRADED` kèm `ai_feedbacks`, 1 `NEEDS_RETRY`), Luyện nói 8 lượt (1 `FAILED`), 4 hội thoại Chat (có link gợi ý và 1 câu từ chối); nội dung ở `seed/HistorySeedContent.java`.
- `seedDemoUsers()`: người dùng demo `ACTIVE` hoạt động trong 30 ngày có phiên học thưa và 3-8 lượt làm bài giữa ngày tạo và lần hoạt động cuối. `reset-demo` xoá thêm lịch sử của `hocvien@` (`deleteMainHistory`) rồi nạp lại. Compile + test-compile sạch.

## Đợt 8 (phiên 8f: thông báo, email_logs, fallback media)

- `NotificationSeedService` (dev): 10 thông báo (9 `SENT` + 1 `DRAFT`, đủ nhóm ALL/ACTIVE/INACTIVE/ADMIN) kèm `user_notifications` đã đọc/chưa đọc theo nhóm nhận tại thời điểm gửi; `email_logs` 30 ngày (nhắc học tuân UNIQUE theo ngày, đặt lại mật khẩu, thông báo; ~8% `FAILED`). `reset-demo` xoá theo tiêu đề/đuôi email demo.
- FE: `components/ui/SafeImage.tsx` (ảnh lỗi thì hiện khối theo token màu hoặc chữ cái đầu) dùng cho ảnh bìa bộ thẻ, banner Dashboard, avatar Header/Hồ sơ; `AudioPlayer` chuyển sang `speechSynthesis` khi tệp audio lỗi. Compile, lint, build sạch.

## Đợt 8 (phiên 8z: đóng đợt)

- Đợt 8 xong phần mã: dữ liệu demo cho người dùng, nội dung, lịch sử học, thông báo, email log, fallback media. Không có endpoint mới nên không thêm folder Postman. Compile/test-compile BE, lint và build FE sạch; bảng "Kiểm tra hoàn thành" của file đợt để người dùng tự kiểm.

## Đợt 10 (phiên 10a: sửa lỗi giao diện)

- Thời gian làm bài đếm theo `Date.now()` (`useCountdown`), kết quả hiện `mm:ss` (`formatClock`) thay vì làm tròn phút.
- Sidebar: chữ và icon luôn trắng ở mọi trạng thái; logo En-Learning là `Link` tới `/dashboard`.
- `hooks/useLeaveGuard` (useBlocker + beforeunload, `release()` sau khi nộp) gắn cho Nghe, Đọc, Kiểm tra, Viết, Nói; `main.tsx` chuyển sang `createBrowserRouter` + `RouterProvider`. Lint, build sạch; không có endpoint mới nên không thêm folder Postman.

## Đợt 11 (phiên 11a: khung Gemini + chấm Viết)

- `ai/GeminiClient` (RestClient, `generateContent`, JSON theo `responseSchema`, đọc token), `GeminiException`, điều kiện `GeminiKeyPresent/Missing` thay cặp `OpenAiKey*`; `app.ai.gemini-api-key|model|base-url|timeout`, `GEMINI_API_KEY` trong `.env.example`.
- `GeminiWritingGrader` + prompt `resources/ai/writing-grader.txt`: điểm kẹp 0-10 làm tròn 0.5, `issues` tối đa 8; lỗi Gemini → `AiGradingException` → NEEDS_RETRY. Test `GeminiWritingGraderTest` (HttpServer giả).
- `FakeWritingGrader` chỉ nạp khi thiếu key Gemini; `FakeChatAssistant`/`FakeSpeakingGrader` tạm luôn nạp tới 11b/11d.

## Đợt 11 (phiên 11b: Chat Gemini + hạn mức)

- `ai/GeminiChatAssistant` + prompt `resources/ai/chat-assistant.txt`: JSON `content/suggestedSkills/refusal`, gộp lượt liền nhau cùng vai, lưu token (kể cả thinking) và model; `FakeChatAssistant` chỉ nạp khi thiếu key.
- Hạn mức `app.chat.daily-limit` (30, `CHAT_DAILY_LIMIT`): đếm câu trợ lý trong DB theo ngày `Asia/Ho_Chi_Minh` (native query, tính cả hội thoại đã xoá), kiểm trước khi lưu tin; hết lượt → 429 `CHAT_DAILY_LIMIT` (`errors.chatDailyLimit`).
- `GET /chat/quota` → `{limit, used, remaining, resetAt}`; `POST .../messages` thêm `remaining`. Test: `ChatQuotaTests`, `GeminiChatAssistantTest`.

## Đợt 11 (phiên 11c: FE Chat hạn mức + thử lại)

- `hooks/useChatQuota` (GET `/chat/quota`, cập nhật theo `remaining` mỗi lần gửi), `components/chat/ChatStatusBar` (đếm "Còn X/30 lượt hôm nay", thông báo hết lượt, banner lỗi AI + nút "Thử lại").
- `ChatPage` và `ChatWidget`: 429 `CHAT_DAILY_LIMIT` khoá ô nhập; lỗi khác giữ tin và "Thử lại" gửi lại đúng tin; chuỗi VI/EN (`chat.quotaLeft|quotaExhausted|retry`, `errors.chatDailyLimit`). Lint, build sạch.

## Đợt 11 (phiên 11d: BE Luyện nói chấm từng câu)

- `V2__speaking_prompt_results.sql`: status thêm `IN_PROGRESS`, bảng `speaking_prompt_results` (unique attempt+prompt, `word_issues`/`tips` JSON, CASCADE); entity `SpeakingPromptResult`, `SpeakingWordIssue`.
- `SpeakingAttemptService`: `POST /speaking/lessons/:id/attempts` (dùng lại lượt dở), `POST /speaking/attempts/:id/prompts/:promptId/assess` (multipart `audio`, AI đồng bộ, ghi đè khi thu lại, AI lỗi → 503 không lưu), `POST /speaking/attempts/:id/submit` (thiếu câu → 400 `SPEAKING_INCOMPLETE`/`errors.speakingIncomplete`; điểm từ `SpeakingScoring`, `improvements` từ AI văn bản; → GRADED). Endpoint submit multipart cũ giữ tới 11e.
- `SpeakingGrader` thêm `assessPrompt`/`writeImprovements`; `GeminiSpeakingGrader` (audio inline base64, prompt `ai/speaking-assess.txt`, `speaking-summary.txt`); `FakeSpeakingGrader` chỉ nạp khi thiếu key. Cleanup xoá lượt `IN_PROGRESS` quá 24h (`app.cleanup.speaking-in-progress-retention`).
- Test: `SpeakingApiTests` (luồng mới), `GeminiSpeakingGraderTest`, `SpeakingScoringTest`, `CleanupServiceTests`.

## Đợt 11 (phiên 11e: FE Luyện nói chấm từng câu)

- `SpeakingPracticePage`: mở lượt `IN_PROGRESS` khi vào trang (lượt dở khôi phục các câu đã chấm), dừng thu là gửi `assess` ngay (loading, lỗi + "Thử chấm lại" giữ bản thu), nộp chỉ bật khi mọi câu có kết quả; `speakingService.startAttempt|assess|submit`.
- `components/practice/PronunciationFeedbackCard`: điểm câu, câu mẫu tô từ lỗi (gạch lượn + tooltip `heardAs`/mẹo), danh sách "Cần cải thiện"; chuỗi VI/EN `speaking.feedback.*`, `speaking.assessing|assessFailed|retryAssess|submitNeedAll`.
- BE: xoá endpoint submit multipart cũ, `SpeakingGradingService`, `SpeakingGrader.grade`; test cũ thay bằng luồng từng câu. Lint, build, test liên quan sạch.

## Đợt 11 đóng (phiên 11z)

- Đợt 11 hoàn tất: Viết, Chat (30 tin/ngày) và Nói (chấm phát âm từng câu) dùng Gemini khi có `GEMINI_API_KEY`, thiếu key thì bản giả.
- Folder Postman "Dot 11 - AI Gemini (Viet, Chat, Noi)" (8 request) và bảng "Kiểm tra hoàn thành" chốt theo DTO thật.

## Đợt 12 - phiên 12a (BE audio bài nghe)

- Module `audio/`: cổng `AudioStorage` (Cloudinary REST có ký, hoặc `uploads/audio` phát ở `/api/media/audio/**`) và `SpeechSynthesizer` (OpenAI TTS, thiếu key thì MP3 im lặng); migration V3 (`audio_source`, `audio_public_id`).
- `TranscriptVoicePlanner` tách `Tên: câu` thành đoạn theo giọng cố định mỗi người nói; `Mp3` bỏ ID3/Xing và nối khung, đo thời lượng, không cần ffmpeg.
- Admin: `POST /admin/listening/{id}/audio/generate`, `POST .../audio` (multipart mp3/m4a/wav ≤ 20MB), `DELETE .../audio`; PUT nội dung không đè audio do TTS/tải lên quản lý; chi tiết admin có `audioSource`.
- Test: `Mp3Test`, `TranscriptVoicePlannerTest`, `AudioServicesHttpTest` (chạy xanh), `ListeningAudioApiTests` (chưa chạy, cần DB).
- 12b: trang nghe phát `audioUrl`, fallback đọc `speechText` (transcript, chỉ trả khi chưa có audio); admin có panel sinh AI/tải lên/xoá audio bài nghe.
- 12c: `SeedAudioRunner` (cờ `AUDIO_GENERATE_SEED=true`) sinh audio 12 bài nghe seed chưa có, ghi URL Cloudinary ngược vào `seed-demo/listening.json`; cập nhật CLAUDE.md, ARCHITECTURE, `.env.example`.
- Đóng đợt 12: thêm folder Postman "Dot 12 - Audio bai nghe" (10 request), chốt bảng kiểm tra theo DTO thật.

## Đợt 13 - phiên 13a (Schema ảnh)

- Migration V4: `image_url/image_author/image_author_url` cho topics, listening_lessons, writing_prompts, speaking_lessons; chỉ thêm `*_author`/`*_author_url` cho flashcard_decks (`cover_image_*`) và flashcards (đã có `image_url`).
- Entity + DTO (TopicResponse, DeckSummary/Detail, CardResponse, ListeningSummary/Detail, WritingPromptSummary/Detail, SpeakingSummary/Detail) trả kèm ảnh và ghi công.
- FE: type mới ở flashcard/practice/writing/speaking.ts; component dùng chung `PhotoCredit` (khoá dịch `common.photoBy`) hiện "Ảnh: X" đè lên ảnh bìa của DeckCard và danh sách Nghe/Đọc/Viết/Nói, `WordImage` hiện ghi công ở mặt trước thẻ từ; fallback ảnh dùng `SafeImage`/`WordImage` có sẵn.
- Bổ sung V5: `reading_lessons` bị bỏ sót ở V4 (không có ảnh minh hoạ), đã thêm entity/DTO/FE giống các mục Nghe/Viết/Nói.

## Đợt 13 - phiên 13b (Crawl từ vựng thật)

- `tools/crawler/` (Node 20+, không phụ thuộc package ngoài): tải Oxford 3000 (`data/oxford-3000.json`, vendor sẵn), lọc còn ~3.069 từ đơn, lấy mẫu đều tới `targetWordCount` (mặc định 1.500, đổi bằng `--limit`).
- `dictionaryClient.js` gọi Free Dictionary API lấy phiên âm/từ loại/định nghĩa/ví dụ EN, cache theo từng từ (`.cache/dictionary/<word>.json`), retry + timeout 15s, từ không có trong từ điển ghi vào `.cache/skipped-words.json`.
- `geminiClient.js` gom 50 từ/lần gọi Gemini dịch nghĩa/từ loại/ví dụ sang VI và gán 1 trong 20 chủ đề cố định (`src/topics.js`) + mức độ; cache theo lô (hash danh sách từ) để resume khi chạy lại.
- `index.js` ghi `topics.json` (20 chủ đề) và `words.json` vào `apps/backend/src/main/resources/seed/real/`.
- Đã kiểm tra syntax (`node --check`) và chạy thử bước tải Oxford 3000 + Dictionary API (cache/resume hoạt động đúng); môi trường sandbox không gọi được `api.dictionaryapi.dev` (timeout mạng) nên chưa chạy hết 1.500 từ và chưa gọi Gemini — người dùng cần tự chạy `node tools/crawler/src/index.js` trên máy có mạng ổn định.

## Đợt 13 - phiên 13c (Sinh đề)

- `generateExercises.js`: sinh bài tập trắc nghiệm từ vựng trực tiếp từ `words.json`/`topics.json` (13.2), **không gọi AI** — mỗi chủ đề đủ từ tạo 1 đề 15 câu, đáp án đúng là `meaningVi` thật, 3 nhiễu lấy nghĩa từ khác cùng chủ đề (đã kiểm tra bằng dữ liệu giả: luôn đúng 1 đáp án/4 lựa chọn). Ghi `exercises.json`.
- `generateLessons.js`: với mỗi chủ đề trong 20 chủ đề, gọi Gemini (dùng chung `callGeminiJson`/`callGeminiCached` tách ra từ `geminiClient.js`) sinh 2 đề Viết, 1 bài Nói (5 câu), 2 bài Nghe (transcript + 5 câu hỏi) → tổng đúng 40 bài nghe. Ghi `writing.json`, `speaking.json`, `listening.json`, cache/resume theo `topicSlug`.
- `generate.js` là entrypoint gộp cả hai bước (`node tools/crawler/src/generate.js`), chạy sau khi đã có `words.json`/`topics.json` từ phiên 13b.
- Sửa lỗi tương thích Windows trong guard "chạy trực tiếp" (`import.meta.url === file://${process.argv[1]}` sai trên Windows do dấu `\`) bằng `pathToFileURL`.
- Đã kiểm tra syntax sạch và chạy thật `generateExercises.js` với dữ liệu giả (28 từ/2 chủ đề); chưa chạy được `generateLessons.js` (cần `GEMINI_API_KEY` thật + mạng gọi được `generativelanguage.googleapis.com`, sandbox không đảm bảo) — người dùng cần tự chạy `node tools/crawler/src/generate.js` sau khi có `words.json`/`topics.json` thật.

## Đợt 13 - phiên 13d (Ảnh Unsplash)

- `unsplashClient.js`: gọi Unsplash Search Photos (Client-ID), cache theo `kind/key` (`.cache/unsplash/`), trả `null` khi không có kết quả; gọi `download_location` sau mỗi ảnh theo điều khoản Unsplash; gặp 403/429 (hết hạn mức) tự chờ 1 giờ rồi thử lại.
- `generateImages.js`: ghi `imageUrl/imageAuthor/imageAuthorUrl` trực tiếp vào `topics.json`, `words.json`, `listening.json`, `writing.json`, `speaking.json` (13.2/13.3 đã có sẵn); gian cách mỗi request ~72s để giữ dưới 50 req/giờ, ghi JSON định kỳ nên dừng giữa chừng rồi chạy lại resume đúng phần còn thiếu (mục đã có `imageUrl`, kể cả `null`, được bỏ qua).

## Đợt 13 - phiên 13e (BE seeder dữ liệu thật)

- `RealDataSeedService` (`app.seed.real-data=true`, env `REAL_DATA_SEED`) đọc `seed/real/{topics,words,listening,writing,speaking,exercises}.json`, gom `words.json` theo `topicSlug` vào đúng một bộ thẻ "Từ vựng thật" mỗi chủ đề, ảnh Unsplash ghi thẳng vào chủ đề/thẻ/bài học.
- Idempotent: chủ đề khoá theo `slug`, từ khoá theo `word` trong đúng bộ thẻ, bài Nghe/Viết/Nói khoá theo `titleVi` trong đúng chủ đề, đề kiểm tra khoá theo `titleVi`; chạy lại bỏ qua bản ghi đã có, log số lượng tạo mới/bỏ qua qua `RealDataSeedService.Report`.
- `RealDataSeedRunner` chạy sau `DevSeedRunner` (`@Order(90)`, profile `dev`), audio bài nghe (`audioUrl`/`durationSeconds`) để trống cho lệnh sinh audio (13.6) điền sau.
- Thêm khoá tra cứu idempotent vào các repository liên quan (`findByTopicIdAndTitleVi`, `existsByDeckIdAndWordIgnoreCase`, `existsByTitleVi`...) và `UserRepository.findFirstByRoleOrderByCreatedAtAsc` để gán `createdBy`.
- Test `RealDataSeedServiceTests` nạp thật rồi chạy lại trong cùng giao dịch, kiểm không nhân đôi bản ghi và câu trắc nghiệm luôn có đúng một đáp án đúng.
- Thêm `UNSPLASH_ACCESS_KEY` vào `.env.example`. Đã kiểm tra syntax (`node --check`) sạch; script gọi mạng thật (~1.600 lượt tìm, chạy nhiều giờ) nên người dùng cần tự chạy `node tools/crawler/src/generateImages.js` sau khi có key.

## Đợt 13 - phiên 13f (Audio người đọc thật, đổi khỏi OpenAI TTS)

- Đổi quyết định: audio 40 bài nghe seed lấy từ Tatoeba.org (câu tiếng Anh có audio người đọc thật, miễn phí, CC BY) thay vì OpenAI TTS; đã kiểm tra thật `Mp3.parse`/`Mp3.join` (`tools/crawler/src/mp3.js`) và tìm/tải câu qua `tatoebaClient.js` bằng file mp3 tải trực tiếp từ `tatoeba.org/en/audio/download/{id}`.
- `generateAudio.js`: với mỗi bài nghe, tìm ≤8 câu có audio (khớp từ khoá tiêu đề, rơi về chủ đề rồi câu bất kỳ), ghép mp3, **viết lại `transcript` bằng chính câu thật vừa chọn** rồi gọi lại Gemini sinh 5 câu hỏi mới khớp transcript đó (transcript/câu hỏi cũ ở 13.3 không còn dùng cho 40 bài này), tự tải file ghép lên Cloudinary (`cloudinaryClient.js`, ký SHA-1 giống `CloudinaryAudioStorage.java`), ghi `audioUrl/audioPublicId/durationSeconds/audioCredit` thẳng vào `listening.json`.
- BE: thêm `AudioSource.REAL` (`V6__listening_audio_source_real.sql`), `RealDataSeedService.seedListening` đọc `audioUrl` có sẵn trong JSON để lưu `audio_source=REAL` luôn, không cần chạy `AUDIO_GENERATE_SEED` (TTS) cho seed thật nữa; tính năng Admin "Sinh lại audio" (TTS) không đổi.
- `audioCredit` (tên tác giả Tatoeba, để ghi công CC BY) có trong JSON/record nhưng seeder không lưu vào DB — app chưa có chỗ hiển thị ghi công cho audio (khác `imageAuthor` của ảnh).
- Đã biên dịch BE sạch (`./mvnw -q compile`) và kiểm tra thật (không phải giả lập) luồng tìm câu Tatoeba + tải + ghép mp3 bằng file thật; **chưa** gọi Gemini/Cloudinary thật (cần `GEMINI_API_KEY`/`CLOUDINARY_URL`) — người dùng cần tự chạy `cd tools/crawler && npm run generate-audio` rồi seeder.

## Đợt 13 - phiên 13z (Đóng đợt)

- `./mvnw -q test`: 217/219 xanh; 2 test `RealDataSeedServiceTests` đỏ vì `apps/backend/src/main/resources/seed/real/*.json` chưa tồn tại — người dùng cần tự chạy `tools/crawler` (crawl từ vựng, sinh đề, ảnh Unsplash, audio) để tạo các JSON thật trước khi test này xanh và trước khi bật `REAL_DATA_SEED=true`.
- FE `npm run lint` và `npm run build` sạch, không đổi mã nguồn.
- Thêm folder `Dot 13 - Du lieu that` vào `.docs/postman/en-learning.postman_collection.json` (GET `/topics` kiểm 20 chủ đề + `imageUrl`, GET `/listening/lessons` kiểm `imageUrl`/`audioUrl`, GET `/reading/lessons`).

## Đợt 13 - phiên 13g (Crawl thu gọn: 500 từ từ cache, ảnh Đọc/Viết/Nói seed)

- `tools/crawler/src/selectWords.js` (mới): chọn lại 500 từ trải đều A-Z từ 1.000 bản dịch Gemini đã cache sẵn (`.cache/gemini/`) + tra cứu từ điển từ cache (`.cache/dictionary/`), **không gọi Gemini/Dictionary API qua mạng** — ghi `apps/backend/src/main/resources/seed/real/words.json` (500 từ, đủ cả 20 chủ đề).
- `tools/crawler/src/annotateDemoImages.js` (mới): tìm ảnh minh hoạ (chuỗi Pexels → Openverse → Wikimedia → Unsplash, `imageClient.js`) cho đúng 3 file bài seed demo đang dùng ở app — `apps/backend/src/main/resources/seed/{reading,writing,speaking}.json` — ghi `imageUrl/imageAuthor/imageAuthorUrl`, không sinh bài mới. Kết quả: Đọc 4/4, Nói 3/3, Viết 3/5 có ảnh (2 đề trừu tượng để trống theo quy ước).
- Cả hai script đã chạy thật (không phải người dùng tự chạy): word selection không đụng mạng, ảnh dùng nguồn miễn phí Openverse/Wikimedia (chưa có `UNSPLASH_ACCESS_KEY`/`PEXELS_API_KEY` trong `.env`).
- `real/words.json` (500 từ) khác cấu trúc "seed đang dùng"; seeder BE đọc file này (`RealDataSeedService`) không đổi ở phiên này — chờ 13i nạp cùng bộ JSON mới.
- Bảng "Kiểm tra hoàn thành" trong `dot-13-du-lieu-that.md` giữ nguyên (đã đúng theo DTO thật, không cần sửa).

## Đợt 13 - phiên 13h (Nghe: 20 bài thật từ VOA Learning English, thay Tatoeba)

- Bản đầu cào mirror tĩnh `manythings.org/voa/...` — người dùng chạy thử thì **toàn bộ** mp3 gốc trên `www.voanews.com/MediaAssets2/...` báo "fetch failed" (lỗi mạng cả loạt, không phải 404 rải rác): hạ tầng audio đó đã bị VOA gỡ hẳn. Đổi sang cào CHÍNH trang hiện tại `learningenglish.voanews.com` (CDN `voa-audio.voanews.eu` còn sống); danh sách bài theo "zone" (`/z/<zoneId>?p=<trang>`, `config.voaZones`, 9 zone) thay cho category cũ.
- `tools/crawler/src/voaClient.js`: `listZoneArticles` (phân trang zone), `parseArticle` (transcript cắt từ `div.wsw`, mốc "Direct link" bỏ khung phát nhạc, dừng ở "Words in This Story"/gạch dưới; mp3 lấy từ link `.c-mmp__fallback-link`), `checkAudioAlive` (HEAD rồi GET Range nếu CDN chặn HEAD).
- `tools/crawler/src/generateListening.js`: duyệt zone, nhận bài có transcript + mp3 còn sống, gọi Gemini 1 lần/bài sinh `titleVi/descriptionVi/descriptionEn` + 5 câu hỏi, tải mp3 lên Cloudinary `Home/En-Learning`, tìm ảnh qua `imageClient.js`, ghi `listening.json` (đúng khuôn `RealSeedFiles.RealListening`), dừng khi đủ 20 bài; resume qua cache `.cache/voa/` (đã xoá cache cũ của mirror hỏng).
- Đã chạy thật xong: **8/8 bài** (giảm từ 20 do free tier Gemini hết quota nhanh) trong `seed/real/listening.json`, đều có 5 câu hỏi + audio, 2/8 có ảnh. Gặp 2 lỗi môi trường không phải do mã: domain `voanews.com` bị chặn mạng ở VN (cần VPN) và model `gemini-3.5-flash-lite` không hỗ trợ `responseSchema` (400 chung, không rõ nghĩa) — đổi mặc định sang `gemini-3.1-flash-lite` (đã kiểm hỗ trợ schema, quota ~500/ngày so với ~20/ngày của dòng Flash đầy đủ).
- `RealDataSeedService`/seeder **chưa đổi** (đợt 13i nạp `listening.json` mới, thay bộ 40 bài Tatoeba cũ, số lượng 8 thay vì 20).

## Đợt 13 - phiên 13i (Seeder nạp bộ JSON mới, đóng đợt 13)

- `RealDataSeedService` thêm `seedReading`, sửa `seedWriting`/`seedSpeaking` đọc đúng `resources/seed/{reading,writing,speaking}.json` (bài seed cũ gắn ảnh ở 13g, cộng dồn `seed-demo/` theo `topicId+titleVi`); bỏ `seedExams` (file nguồn không tồn tại). Thêm `LEGACY_TOPIC_ALIASES`/`legacyTopic()` (slug cũ `life`→`daily-life`, slug lạ khác thì bỏ qua bản ghi thay vì crash server); `seedWords` sửa để cập nhật ảnh cho flashcard đã tồn tại thay vì chỉ tạo mới.
- `V7__widen_image_author.sql` + entity: nới `image_author` 200→500 ký tự cho cả 7 bảng có ảnh — Wikimedia/Openverse (nguồn ảnh 500 từ, không cần API key) trả tên tác giả dài hơn Unsplash, vượt cột cũ (`Data too long for column 'image_author'`).
- Người dùng đã tự chạy thật và xác nhận đến bước cuối: 20 chủ đề/500 từ (đều có ảnh)/8 bài nghe/4 đọc/5 viết/3 nói nạp đúng, không còn lỗi slug.
- `./mvnw -q compile` sạch. Đóng đợt 13 trong `ROADMAP.md` (bảng "Trạng thái" và "Phiên làm việc" đều `[x]`).
- Còn lại: chạy `REAL_DATA_SEED=true` một lần nữa (Flyway tự áp V7 trước) để xác nhận đẩy ảnh 500 từ vào DB thành công, rồi test DB thật/Postman/giao diện.

## Đợt 13 - sửa lỗi tự kiểm (phiên âm ARPAbet, ảnh bìa chưa lên dù đã seed)

- Phiên âm sai (`/AE1 K T IH0 V/` thay vì `/ˈæktɪv/`): `dictionaryClient.js` dùng Datamuse trả ARPAbet (đổi từ `dictionaryapi.dev` ở 13z) chứ không phải IPA. Thêm `arpabetToIpa.js` (bảng ánh xạ + gộp âm tiết), áp dụng ngay khi crawl và chạy một lần qua `npm run fix-phonetics` (script mới) để sửa lại `.cache/dictionary/` + `words.json` đã có sẵn — không gọi lại API.
- Ảnh bìa topic/deck/reading/writing/speaking không lên dù `REAL_DATA_SEED=true` đã chạy: `RealDataSeedService` chỉ gán ảnh khi TẠO MỚI bản ghi; bản ghi đã tồn tại (topic có sẵn, hoặc bài Đọc/Viết/Nói trùng `titleVi` với seed demo cũ đợt 8) bị `skip` hoàn toàn, không backfill ảnh. Thêm backfill ảnh (và phiên âm cho flashcard) cho topic, deck, reading, writing, speaking khi bản ghi đã có nhưng thiếu ảnh — đổi `ReadingLessonRepository`/`WritingPromptRepository`/`SpeakingLessonRepository` từ `existsBy...` sang `findBy...TitleVi`.
- `./mvnw -q compile`/`test-compile` và `npm run lint` (FE) sạch.
- Việc cần tự kiểm: chạy lại `REAL_DATA_SEED=true`, kiểm ảnh bìa lên trong flashcards/writing/listening/reading/speaking và phiên âm flashcard hiển thị dạng IPA (`/ˈæktɪv/`) trên giao diện + SQL.

## Ảnh minh hoạ đầy đủ + trang danh sách 4 cột/12 thẻ mỗi trang, sắp xếp A-Z

- Crawler: `imageClient.js` hỗ trợ nhiều từ khoá dự phòng (tiêu đề cụ thể → tên chủ đề → từ khoá chung theo loại nội dung) thay vì 1 từ khoá duy nhất; `generateImages.js`/`annotateDemoImages.js` dùng chuỗi này và thử lại các mục trước đó bị null. Kết quả chạy thật: chủ đề 20/20, từ 500/500, nghe 8/8 (từ 2/8), Đọc/Viết/Nói demo đều đủ 4/4, 5/5, 3/3 (từ 3/5) có ảnh.
- BE: thêm `ContentSort` (dùng chung `titleVi`/`createdAt`) và tham số `sort` (`az`/`za`/`newest`, mặc định `az`) cho `FlashcardController`, `Listening`/`ReadingController` (`PracticeCatalogService`), `WritingController`, `SpeakingController`; `pageSize` mặc định vẫn là 12.
- FE: `FilterBar` thêm ô chọn sắp xếp (`sortable` prop, chỉ bật ở 5 trang Flashcards/Nghe/Đọc/Viết/Nói, không đụng trang Đề kiểm tra); thêm `components/ui/Pagination.tsx` (bọc `antd Pagination`); cả 5 trang danh sách đổi lưới thành `sm:2 → lg:3 → xl:4` cột, `pageSize=12`, có phân trang, về trang 1 khi đổi bộ lọc/sắp xếp (gộp vào `handleFilterChange` thay vì `useEffect` để không vi phạm eslint `react-hooks/set-state-in-effect`).
- `./mvnw -q compile`/`test-compile`, `npm run lint`, `npm run build` (FE) đều sạch.
- Việc cần tự kiểm: chạy lại `REAL_DATA_SEED=true` rồi mở giao diện — kiểm 5 trang hiện 4 thẻ/dòng, phân trang 12 thẻ/trang, đổi được sắp xếp A-Z/Z-A/Mới nhất; trang Đề kiểm tra không đổi.
