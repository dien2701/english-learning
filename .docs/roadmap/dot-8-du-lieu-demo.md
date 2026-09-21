# Đợt 8: Dữ liệu demo cho từng trang

Mục tiêu: mọi trang (người học + quản trị) có nội dung thật, đẹp khi demo. Chỉ chạy ở profile `dev`.

## Quyết định đã chốt (21/09/2026)

- Quy mô vừa: ~50 user, 8-10 topic, ~15 deck × 20 thẻ, mỗi kỹ năng 10-15 bài, 5 đề thi, lịch sử học 90 ngày.
- Cách nạp: mở rộng JSON trong `src/main/resources/seed/` + `SeedService`/`HistorySeedService`. Không dùng Flyway, không thêm endpoint.
- Thêm cờ `app.seed.mode` (`if-empty` mặc định | `reset-demo`): `reset-demo` xoá dữ liệu có đánh dấu demo rồi seed lại, không đụng user thật.
- Media: URL ngoài miễn phí (ảnh `picsum.photos`/Unsplash cố định theo seed; audio phát âm `api.dictionaryapi.dev`). Bài nghe thiếu file: FE fallback `speechSynthesis` đọc transcript.
- Nội dung: 4 mảng, mỗi mảng gắn topic + level:
  - Giao tiếp hằng ngày A1-B1 (du lịch, ăn uống, mua sắm, gia đình, sức khoẻ).
  - IELTS/TOEIC (đề thi, đọc hiểu, Writing Task 1/2, Part 1-7 TOEIC rút gọn).
  - Công sở/IT (email, họp, phỏng vấn, từ vựng kỹ thuật).
  - Học thuật B2-C1 (bài đọc dài, từ vựng AWL).
- Nguồn nội dung: tự viết (không chép nguyên văn đề có bản quyền). Từ vựng tham khảo danh sách mở (Oxford 3000 để chọn từ, AWL), câu ví dụ và bài đọc tự soạn. Mọi nội dung song ngữ `xxxVi/xxxEn`.
- Dữ liệu sinh ngẫu nhiên dùng `Random` có seed cố định để lần nào chạy cũng ra kết quả giống nhau.

## 8.1 Người dùng và cài đặt (`users.json` + sinh bằng code)

- [x] 3 tài khoản cố định giữ nguyên (mật khẩu `123456`), thêm 47 user giả: tên Việt/Anh đa dạng, avatar `i.pravatar.cc`, `createdAt` rải 6 tháng, ~5% `LOCKED`, ~10% chưa hoạt động 30 ngày.
- [x] `user_settings`: ngôn ngữ, mục tiêu phút/ngày, bật/tắt nhắc học phân bố hợp lý.
- [x] Đánh dấu user demo (email đuôi `@demo.enlearning.vn`) để `reset-demo` nhận diện.

## 8.2 Nội dung học tập (JSON, tự soạn)

- [x] `topics.json`: 10 topic song ngữ (bảng `topics` chỉ có slug/nameVi/nameEn nên không có ảnh bìa, icon, level). File mới ở `resources/seed-demo/`.
- [x] `decks.json` (`seed-demo/`): 15 deck × 20 thẻ (IPA, loại từ, nghĩa VI/EN, ví dụ EN/VI, ảnh picsum; `audioUrl` để null, FE dùng giọng trình duyệt), 2 deck `INACTIVE`. Đã xong: `SeedService` đọc topics/decks từ `seed-demo/` và còn nạp chủ đề cũ ở `seed/topics.json` cho tới khi 8c/8d chuyển bài nghe/đọc/nói/viết sang `seed-demo/` (lúc đó xoá đoạn này, và `seed/topics.json`, `seed/decks.json`).
- [x] `listening.json` (`seed-demo/`): 12 bài (transcript, `audioUrl` null để FE đọc bằng giọng trình duyệt, 5-6 câu/bài), 1 bài `INACTIVE`. `SeedService` đọc từ `seed-demo/`; Sau 8d, `SeedService` chỉ còn đọc `seed/users.json`; các file cũ `seed/{topics,decks,listening,reading,speaking,writing,exams}.json` không còn dùng, xoá tay (thư mục `seed/` bị chặn với Claude).
- [x] `reading.json` (`seed-demo/`): 12 bài (130-300 từ theo level, 6-8 câu, giải thích đáp án bằng tiếng Việt, có bài True/False/Not Given), 1 bài `INACTIVE`. Đã xoá `seed/reading.json` khỏi luồng nạp tương tự.
- [x] `writing.json` (`seed-demo/`): 12 đề (đoạn văn, email, ý kiến, IELTS Task 1/2), gợi ý dàn ý, số từ tối thiểu; 1 đề `INACTIVE`.
- [x] `speaking.json`: 12 bài × 5 câu (giao tiếp, công sở/IT, IELTS Part 1-3; `phonetic` để null, có nghĩa VI); 1 bài `INACTIVE`.
- [x] `exams.json`: 5 đề (TOEIC mini ×2, IELTS mini ×2, tổng hợp cơ bản), 30-40 câu/đề (167 câu), gồm trắc nghiệm, điền từ, True/False/Not Given; nghe/đọc là đoạn ngắn nhúng trong nội dung câu; IELTS mini 2 `INACTIVE`.
- [x] 1 bài `INACTIVE` mỗi loại (nghe, đọc, viết, nói, đề thi).

## 8.3 Hoạt động người học (`HistorySeedService`, sinh bằng code)

- [x] `study_sessions` 90 ngày: có chuỗi ngày liên tục (streak) cho user demo chính, cuối tuần ít hơn.
- [x] `practice_attempts` + `answers` cho Nghe/Đọc/Kiểm tra, điểm thang 10 tăng dần theo thời gian.
- [x] `user_flashcard_progress`: trạng thái new/learning/mastered, hạn ôn rải đều (có thẻ đến hạn hôm nay).
- [x] `writing_submissions` + `ai_feedbacks`, `speaking_attempts` (transcript, điểm; vài bản `FAILED`).
- [x] Người dùng demo `ACTIVE` (hoạt động trong 30 ngày) có lịch sử nhẹ (phiên học thưa + 3-8 lượt làm bài) cho trang quản trị. `reset-demo` cũng xoá và nạp lại lịch sử của `hocvien@`.
- [x] `chat_conversations` + `chat_messages`: 3-5 hội thoại mẫu cho user chính.

## 8.4 Quản trị và thông báo

- [x] `notifications` 10 cái (hệ thống, nhắc học, nội dung mới) + `user_notifications` có đã đọc/chưa đọc. Nạp bởi `NotificationSeedService` (9 đã gửi + 1 nháp; `reset-demo` xoá theo tiêu đề).
- [x] `email_logs` 30 ngày (SENT/FAILED) để trang admin có số liệu.
- [x] Kiểm `/admin/dashboard`: biểu đồ user mới, lượt làm bài theo ngày có dữ liệu.

## 8.5 FE hỗ trợ media

- [x] Ảnh lỗi/thiếu: placeholder theo token màu. Audio thiếu: nút nghe dùng `speechSynthesis` (en-US).

## Kiểm tra hoàn thành

Đợt 8 chỉ nạp dữ liệu (không thêm endpoint) nên không có folder Postman mới; các mục dưới đây người dùng tự kiểm (chạy lại collection cũ để hồi quy).

- [ ] Xoá sạch DB (hoặc `app.seed.mode=reset-demo`), chạy BE dev: log seed không lỗi, chạy lại lần 2 không nhân đôi dữ liệu.
- [ ] SQL: `SELECT COUNT(*)` từng bảng khớp quy mô trên; không có bản ghi thiếu `xxxVi/xxxEn`.
- [ ] Đăng nhập user demo chính: Dashboard, Thống kê, Flashcard, 5 kỹ năng, Đề thi, Chat, Thông báo đều có dữ liệu, ảnh/audio hiển thị hoặc fallback.
- [ ] Đăng nhập admin: danh sách user phân trang, lọc `INACTIVE`, dashboard có biểu đồ.
- [ ] `./mvnw -q compile`, `npm run lint`, `npm run build` sạch; chạy lại collection Postman các đợt cũ vẫn xanh.
