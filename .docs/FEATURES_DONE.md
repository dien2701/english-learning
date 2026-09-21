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
