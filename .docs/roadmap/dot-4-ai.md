# Đợt 4: AI (Viết, Chat, Nói) với bản giả trước

Phụ thuộc: chỉ cần Auth (độc lập với đợt 1-3, làm sớm được). Chia phiên: 4.1 Viết + khung chung; 4.2 Chat; 4.3 Nói; 4.4 gắn OpenAI thật (khi có key).
Dùng mô hình mạnh cho 4.1 (dựng khung bất đồng bộ). 4.2 và 4.3 tái dùng khung nên có thể dùng mô hình rẻ hơn.

## Khung chung
- Interface `WritingGrader`, `SpeakingGrader`, `ChatAssistant`. Mỗi cái có `Fake*` (`@Profile("dev")`) và `OpenAi*`. `OPENAI_API_KEY` trống thì tự dùng bản giả.
- Chấm bất đồng bộ bằng executor có giới hạn. Trạng thái bài viết: `DRAFT`, `GRADING`, `GRADED`, `NEEDS_RETRY`; bài nói: `GRADING`, `GRADED`, `FAILED`; có timeout; ghi `modelName` và số token.
- Bản giả mô phỏng được độ trễ và lỗi để test luồng "cần chấm lại".
- Luôn lưu dữ liệu người dùng trước, gọi AI sau. Không để API key trong React.

## 4.1 Luyện viết
Endpoint: `GET /writing/prompts`, `GET /writing/prompts/:id`, `POST /writing/prompts/:id/submit`, `GET /writing/submissions`, `GET /writing/submissions/:id`, `POST /writing/submissions/:id/regrade`.
- [x] Nộp bài (kiểm số từ tối thiểu ở BE), lưu `WritingSubmission`, chấm nền, lưu `AiFeedback` (danh sách lỗi ở cột JSON `issues`). FE hỏi lại tới khi có kết quả.
- [x] `regrade` cho trạng thái `NEEDS_RETRY`.

## 4.2 Chat AI
Endpoint: `GET|POST /chat/conversations`, `GET /chat/conversations/:id`, `POST /chat/conversations/:id/messages`, `GET /chat/starters`. Đã thêm `DELETE /chat/conversations/:id` (xoá mềm) và `PATCH /chat/conversations/:id` `{title}` (đổi tiêu đề); FE (4d) cần gọi thêm PATCH nếu muốn dùng. `POST .../messages` trả `{userMessage, reply}`; AI lỗi thì 503 `AI_UNAVAILABLE` (tin người học vẫn được lưu). Bản giả lỗi khi tin chứa `[fail]`.
- [x] Lưu tin nhắn người học trước, gọi AI sau, lưu câu trả lời kèm `modelName`, token, gợi ý bài học (cột JSON `links`).
- [x] Từ chối câu ngoài phạm vi học tiếng Anh (`isRefusal`). Giới hạn độ dài ngữ cảnh gửi lên.

## 4.3 Luyện nói
Endpoint: `GET /speaking/lessons`, `GET /speaking/lessons/:id`, `POST /speaking/lessons/:id/submit` (multipart), `GET /speaking/results/:attemptId`.
Hợp đồng multipart (đã chốt): `promptIds` (UUID, lặp lại), `audio` (tệp, lặp lại, cùng thứ tự với `promptIds`), `durationSeconds`. Mỗi tệp tối đa 5MB, cả lần nộp 15MB; định dạng `audio/webm|ogg|mpeg|mp4|aac|wav`. Sai: 400 (`errors.noRecording`, `errors.field.audioMismatch`, `errors.field.invalidPrompt`), 415 `AUDIO_UNSUPPORTED`, 413 `AUDIO_TOO_LARGE`. Trả `SpeakingResult` có thêm `status` (GRADING|GRADED|FAILED); điểm và `scores` chỉ có khi GRADED; `promptFeedback[]` có `text` (câu đề) và `transcript`. Bản giả: tên tệp chứa `fail` thì FAILED. FE (4d) phải đổi mock submit từ JSON sang multipart.
- [x] Nhận file âm thanh (kiểm kích thước, định dạng), chuyển thành văn bản, chấm, lưu `SpeakingAttempt` (transcript, sáu điểm thành phần, `improvements`, `promptFeedback`), rồi BỎ âm thanh.
- [x] Lỗi thì `FAILED` và người học ghi âm lại (không chấm lại được vì không lưu âm thanh). Bản giả trả transcript cố định.

## 4.4 OpenAI thật (chờ người dùng có key)
- [ ] Thêm client OpenAI, prompt chấm, cấu hình model qua biến môi trường; test tự động vẫn dùng bản giả.

## Tiêu chí xong
- [x] Ba luồng chạy trọn với bản giả, gồm cả trạng thái `GRADING`, lỗi (`NEEDS_RETRY` với bài viết, `FAILED` với bài nói) và chấm lại/ghi âm lại.
- [x] FE bỏ `writing`, `chat`, `speaking` khỏi `VITE_MOCK_MODULES` (`.env.example` đã sửa; bạn tự sửa `.env` cục bộ thành `notifications,admin`).
- [x] Người dùng chỉ xem được dữ liệu của mình. Test xanh, lint/build sạch.

## Kiểm tra hoàn thành

Folder "Dot 4 - AI (ban gia)" sẽ được thêm khi đợt xong. Bảng dưới là DỰ KIẾN. AI chạy bản giả nên kết quả cố định; bản OpenAI thật kiểm riêng ở 4.4. Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: đường thành công
| # | Request | Kỳ vọng |
|---|---|---|
| 1 | `GET /writing/prompts`, `/writing/prompts/{{promptId}}` | 200 |
| 2 | `POST /writing/prompts/{{promptId}}/submit` body `{"content":"<đủ số từ tối thiểu>"}` | 200/202, `status` = GRADING (hoặc GRADED nếu bản giả trả ngay) |
| 3 | `GET /writing/submissions/{{submissionId}}` (chạy lại vài lần, cách 1-2 giây) | chuyển sang GRADED; có điểm tổng, 3 điểm thành phần, nhận xét, danh sách lỗi kèm cách sửa |
| 4 | `GET /writing/submissions` | có bài vừa nộp |
| 5 | Nộp nội dung chứa dấu hiệu lỗi của bản giả (đã chốt: thêm `[fail]` vào nội dung, lỗi ở lần đầu; `[fail-always]` thì chấm lại vẫn lỗi), rồi `POST /writing/submissions/{{id}}/regrade` | NEEDS_RETRY; sau regrade thành GRADED |
| 6 | `GET /chat/starters` | 200 |
| 7 | `POST /chat/conversations` | 200/201, có `id` |
| 8 | `POST /chat/conversations/{{convId}}/messages` body `{"content":"How do I use the present perfect?"}` | 200, có tin của trợ lý kèm gợi ý bài học |
| 9 | Cùng hội thoại: `{"content":"Who won the last World Cup?"}` | tin trợ lý có `isRefusal` = true, từ chối lịch sự |
| 10 | `GET /chat/conversations`, `/chat/conversations/{{convId}}` | thấy hội thoại, tin nhắn đúng thứ tự |
| 11 | `GET /speaking/lessons`, `/speaking/lessons/{{lessonId}}` | 200 |
| 12 | `POST /speaking/lessons/{{lessonId}}/submit` (Body: form-data, file âm thanh; đã chốt: `promptIds` lặp lại + `audio` lặp lại cùng thứ tự + `durationSeconds`) | 200/202, status GRADING |
| 13 | `GET /speaking/results/{{attemptId}}` | GRADED; điểm tổng và 5 điểm thành phần (phát âm, từ vựng, ngữ pháp, trôi chảy, đúng chủ đề); có `improvements` và nhận xét từng câu kèm transcript |

### B. Ca lỗi và bảo mật
- Không có token: 401. Bài viết thiếu số từ tối thiểu: 400. Tin nhắn rỗng: 400.
- Người dùng B xem bài viết, kết quả nói hoặc hội thoại của A: 404.
- File âm thanh quá lớn hoặc sai định dạng: 4xx (400/413/415), không phải 500.
- Hội thoại đã xoá (nếu có endpoint xoá): 404.
- Bản giả có chế độ lỗi: nộp bài nói lỗi thì trạng thái FAILED, người học ghi âm lại được.

### C. SQL kiểm tra dữ liệu
```sql
-- Bài viết đã lưu trước khi chấm, trạng thái đúng
SELECT s.status, s.word_count, s.submitted_at
FROM writing_submissions s JOIN users u ON u.id = s.user_id
WHERE u.email = 'hocvien@enlearning.vn' ORDER BY s.created_at DESC LIMIT 5;

-- Phản hồi AI của bài mới nhất
SELECT * FROM ai_feedbacks ORDER BY created_at DESC LIMIT 1;

-- Chat: tin người học lưu trước, tin trợ lý có model_name và số token (thay <convId>)
SELECT role, LEFT(content, 40) AS content, is_refusal, model_name, prompt_tokens, completion_tokens
FROM chat_messages WHERE conversation_id = UUID_TO_BIN('<convId>') ORDER BY created_at;

-- Luyện nói: chỉ lưu transcript và điểm, không có cột âm thanh
SELECT status, overall_score, model_name, submitted_at FROM speaking_attempts ORDER BY created_at DESC LIMIT 5;
SHOW COLUMNS FROM speaking_attempts;
```

### D. Thử trên giao diện FE
1. Bỏ `writing`, `chat`, `speaking` khỏi `VITE_MOCK_MODULES`.
2. Luyện viết: nộp bài, thấy "AI đang chấm" rồi kết quả; ép lỗi bằng dấu hiệu của bản giả, thấy "cần chấm lại" và chấm lại được; lịch sử bài viết có đủ.
3. Chat: cửa sổ thu nhỏ và trang đầy đủ; câu ngoài phạm vi bị từ chối lịch sự; gợi ý bài học bấm vào mở đúng trang.
4. Luyện nói: ghi âm, nộp, xem kết quả; kiểm Network thấy request multipart.

### E. Bước 4.4 (khi đã có `OPENAI_API_KEY`)
Đặt key vào `.env`, chạy BE, thử một bài viết, một tin chat, một bài nói thật. Kiểm `model_name` và số token có giá trị trong SQL ở mục C. Bộ test tự động vẫn dùng bản giả và phải xanh.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md`. Mỗi bước 4.1 đến 4.3 nên đóng riêng bằng cách tick checkbox của bước đó.
2. Mở phiên mới cho Đợt 5:

```text
Đợt 5, bước 5.1: Người dùng + Thông báo + Dashboard admin BE.
Đọc .docs/ROADMAP.md và .docs/roadmap/dot-5-quan-tri.md. /admin/** cần ROLE_ADMIN, có test 403.
Viết service, controller, DTO, test cho 5.1; chạy test hẹp. Không đụng FE, không sửa V1.
```
