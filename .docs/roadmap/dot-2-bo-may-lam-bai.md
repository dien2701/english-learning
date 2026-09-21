# Đợt 2: Bộ máy làm bài (Nghe, Đọc, Kiểm tra)

Phụ thuộc: đợt 0 (song song được với đợt 1). Chia phiên: 2.1 engine chung + Nghe; 2.2 Đọc + Kiểm tra; 2.3 nối FE.
Ba chức năng dùng chung `Question`, `QuestionOption`, `PracticeAttempt`, `PracticeAttemptAnswer`. Làm engine một lần, sau đó chỉ là cấu hình.

## Endpoint
- `GET /listening/lessons`, `GET /listening/lessons/:id`, `POST /listening/lessons/:id/submit`
- `GET /reading/lessons`, `GET /reading/lessons/:id`, `POST /reading/lessons/:id/submit`
- `GET /exams`, `GET /exams/:id`, `POST /exams/:id/submit`
- `GET /attempts`, `GET /attempts/:attemptId`

## Việc cần quyết khi làm
- Nộp trễ (Reading, Exam có giới hạn thời gian): nhận bài nhưng đánh dấu hết giờ, dung sai vài giây.
- Bài có tiến độ dở dang: FE có lưu nháp không; nếu không thì "đang học dở" chỉ dựa vào `study_sessions` (cần cho đợt 3).

## Việc làm
- [ ] Service chấm điểm chung: trắc nghiệm theo `QuestionOption`; câu điền từ so khớp `acceptedAnswers` không phân biệt hoa/thường. Đảm bảo mỗi `Question` thuộc đúng một cha (bài nghe, bài đọc hoặc đề).
- [ ] Lưu `PracticeAttempt` + `PracticeAttemptAnswer` (đúng một chủ sở hữu). Điểm thang 10, `DECIMAL(3,1)`.
- [ ] Chi tiết bài học KHÔNG trả đáp án đúng và transcript; chỉ trả sau khi nộp.
- [ ] Kết quả Exam tách điểm theo kỹ năng; danh sách lịch sử làm bài lọc theo kỹ năng.
- [ ] `study_sessions` nhận `skill` và `refId` cho bài nghe/đọc/kiểm tra.
- [ ] FE: bỏ `listening`, `reading`, `exams`, `attempts` khỏi `VITE_MOCK_MODULES`.

## Tiêu chí xong
- [ ] Làm trọn một bài nghe, một bài đọc, một đề kiểm tra trên seed; kết quả và xem lại đáp án đúng.
- [ ] Không có API nào lộ đáp án trước khi nộp (có test cho điều này).
- [ ] Người dùng A không xem được lượt làm của người dùng B (test 403/404).
- [ ] Test xanh, lint/build sạch.

## Kiểm tra hoàn thành

Folder "Dot 2 - Nghe, Doc, Kiem tra" sẽ được thêm khi đợt xong. Bảng dưới là DỰ KIẾN. Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: đường thành công
| # | Request | Kỳ vọng |
|---|---|---|
| 1 | `GET /listening/lessons`, `/reading/lessons`, `/exams` | 200, danh sách có phân trang/lọc |
| 2 | `GET /listening/lessons/{{lessonId}}` (tương tự reading, exam) | 200, có danh sách câu hỏi và phương án; KHÔNG có đáp án đúng và transcript |
| 3 | `POST /listening/lessons/{{lessonId}}/submit` body `{"answers":[{"questionId":"...","optionId":"..."},{"questionId":"...","text":"..."}],"durationSeconds":120}` | 200, có `attemptId`, `score`, `correctCount`, `totalQuestions`; nay mới có đáp án đúng và transcript |
| 4 | `POST /reading/lessons/{{lessonId}}/submit` | như trên |
| 5 | `POST /exams/{{examId}}/submit` | 200, điểm tách theo kỹ năng |
| 6 | `GET /attempts` (lọc theo kỹ năng/trạng thái) | 200, thấy các lượt vừa nộp |
| 7 | `GET /attempts/{{attemptId}}` | 200, chi tiết từng câu, đáp án chọn và đáp án đúng |

### B. Ca lỗi và bảo mật
- Chi tiết bài trước khi nộp không chứa các khoá `isCorrect`, `correctOptionId`, `acceptedAnswers`, `transcript` (thêm test tìm chuỗi này trong phản hồi).
- Câu điền từ: nộp `"Apple"` khi đáp án là `apple` vẫn đúng.
- Nộp `questionId` thuộc bài khác hoặc thiếu câu: 400, không phải 500.
- Nộp trễ hạn (Reading/Exam có giới hạn thời gian): vẫn nhận, có đánh dấu hết giờ.
- Nộp hai lần cho cùng một lượt: bị từ chối (409 hoặc 400, chốt khi làm).
- Người dùng B gọi `GET /attempts/{{attemptId của A}}`: 404 hoặc 403.
- Không có token: 401.

### C. SQL kiểm tra dữ liệu
```sql
-- Mỗi lượt thuộc đúng MỘT bài: đúng một trong ba cột is_* bằng 1
SELECT BIN_TO_UUID(a.id) AS id, a.status, a.score, a.correct_count, a.total_questions, a.duration_seconds,
       a.listening_lesson_id IS NOT NULL AS is_listening,
       a.reading_lesson_id IS NOT NULL AS is_reading,
       a.exam_id IS NOT NULL AS is_exam
FROM practice_attempts a JOIN users u ON u.id = a.user_id
WHERE u.email = 'hocvien@enlearning.vn' ORDER BY a.created_at DESC LIMIT 5;

-- Số câu đúng khớp cột correct_count của lượt (thay <attemptId>)
SELECT COUNT(*) AS total, SUM(is_correct) AS correct
FROM practice_attempt_answers WHERE attempt_id = UUID_TO_BIN('<attemptId>');
```

### D. Thử trên giao diện FE
1. Bỏ `listening`, `reading`, `exams`, `attempts` khỏi `VITE_MOCK_MODULES`.
2. Làm trọn một bài nghe, một bài đọc, một đề kiểm tra: kết quả, xem lại đáp án; lịch sử làm bài có đủ.
3. Đề có giới hạn thời gian: để hết giờ, bài tự nộp và kết quả hiện đúng.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md`.
2. Khi cả đợt 1 và 2 đã xong, mở phiên mới cho Đợt 3:

```text
Đợt 3, bước 3.1: Dashboard + Thống kê BE.
Đọc .docs/ROADMAP.md và .docs/roadmap/dot-3-dashboard.md. Trước hết seed dev thêm lịch sử học,
rồi viết truy vấn tổng hợp, service, controller, test. Gom ngày theo UserSetting.timeZone.
Chạy test hẹp. Không đụng FE.
```
Nếu đợt 1 chưa xong thì làm đợt 1 trước.
