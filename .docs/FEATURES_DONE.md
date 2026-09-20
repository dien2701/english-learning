# TIẾN ĐỘ: EN-LEARNING

Giai đoạn hiện tại: **Frontend xong với dữ liệu mock; Database đã thiết kế**.
Chưa triển khai API Backend. Frontend vẫn lấy dữ liệu từ mock trong `src/mocks`.

Cập nhật: 20/09/2026 (vòng 2: sáng/tối, đa ngôn ngữ, ảnh thẻ, bộ lọc; thiết kế Database)

---

## Database (thiết kế xong, chưa có API)

- `apps/backend/src/main/resources/db/migration/V1__init_schema.sql`: 26 bảng,
  40 khoá ngoại, 85 index, 18 ràng buộc CHECK. Đã chạy thử Flyway và
  Hibernate `validate` trên MySQL 9.0 thành công, kèm ghi/đọc thử các cột JSON
  và ràng buộc CHECK của 5 bảng mới qua JPA.
- 26 entity JPA ở `apps/backend/.../entity`, 19 enum ở `entity/enums`.
- Đã rút gọn từ 35 bảng xuống 21 (gộp danh sách nhỏ thành cột JSON), rồi khôi phục
  Chat AI và Luyện nói và thêm `study_sessions`, lên 26 bảng (20/09/2026):
  `chat_conversations`, `chat_messages`, `speaking_lessons`, `speaking_attempts`, `study_sessions`.
- Luyện nói không lưu âm thanh, chỉ lưu transcript, điểm và nhận xét; lượt chấm lỗi
  là `FAILED` và người học ghi âm lại.
- Thời gian học ở Dashboard tính từ `study_sessions` (heartbeat ~30 giây, gom theo
  ngày ở múi giờ Asia/Ho_Chi_Minh). Cách tính chi tiết: mục 2 của `ARCHITECTURE.md`.
- Chi tiết bảng và quy ước: mục 1 của `ARCHITECTURE.md`.
- Chưa làm: Repository, Service, Controller, JWT; dữ liệu mẫu (seed).

---

## Trạng thái 10 chức năng

| # | Chức năng | Trạng thái |
|---|---|---|
| 1 | Đăng ký và Đăng nhập | Xong |
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
- `.agent/AGENTS.md` ghi sai tech stack: nói Node/Express/MongoDB và
  CRA + Redux + Bootstrap, trong khi dự án thực tế là Vite + Ant Design +
  Tailwind, không dùng Redux, backend dự kiến là Spring Boot.
- Thư mục lồng `english-learning/english-learning`.
- Ảnh bìa bộ thẻ lấy từ Unsplash qua đường dẫn ngoài; khi có backend nên
  chuyển sang Cloudinary.
