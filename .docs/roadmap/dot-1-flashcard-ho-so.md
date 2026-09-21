# Đợt 1: Flashcard, Hồ sơ/Cài đặt, heartbeat

Phụ thuộc: đợt 0. Chia phiên: 1.1 BE Topic + Flashcard; 1.2 BE Hồ sơ/Cài đặt + heartbeat; 1.3 nối FE.
Mẫu cấu trúc: `auth/`. Repository Topic/Flashcard đã có ở `content/repository`.

## Endpoint
- `GET /topics`
- `GET /flashcard/decks?search&topicId&level&status` (phân trang)
- `GET /flashcard/decks/:id`
- `POST /flashcard/decks/:id/progress` (chỉ nhận `flashcardId` và mức nhớ; BE tự tính ngày ôn tiếp theo)
- `POST /flashcard/decks/:id/finish`
- `GET|PATCH /profile`
- `POST /profile/password` (thu hồi các refresh token khác)
- `GET|PATCH /settings`
- MỚI `POST /study/heartbeat` (`skill`, `refId`)

## Việc cần quyết khi làm
- Thuật toán ôn tập theo 3 mức tự đánh giá của FE (bảng khoảng cách ngày, dạng SM-2 đơn giản): đề xuất bảng cụ thể trước khi code.
- Trạng thái bộ thẻ (chưa học, đang học, đã thuộc) suy ra từ `UserFlashcardProgress`, không thêm cột.
- Caffeine cache cho danh sách bộ thẻ và chủ đề (đọc nhiều, ít đổi); xoá cache khi Admin sửa (đợt 5).
- Heartbeat: mỗi lần cộng tối đa 60 giây vào `StudySession.activeSeconds`; im lặng quá 2 phút thì mở phiên mới. Múi giờ lấy từ `UserSetting.timeZone`.

## Việc làm
- [x] 1.1 Topic + Flashcard: service, controller, DTO, test.
- [x] 1.2 Hồ sơ, Cài đặt, đổi mật khẩu, heartbeat: service, controller, DTO, test.
- [x] 1.3 FE: bỏ `topics`, `flashcard`, `profile`, `settings` khỏi `VITE_MOCK_MODULES`; thêm hook gửi heartbeat khoảng 30 giây một lần khi tab học đang mở và có tương tác.

## Tiêu chí xong
- [x] Học một bộ thẻ trọn vẹn trên dữ liệu seed: thoát giữa chừng vẫn giữ tiến độ, màn kết quả liệt kê từ cần ôn.
- [x] Đổi ngôn ngữ, giao diện, mục tiêu học, giờ nhắc lưu được và giữ sau khi tải lại.
- [x] Đổi mật khẩu xong thì phiên ở nơi khác hết hiệu lực.
- [x] Bảng `study_sessions` có dòng sau khi học một lúc.
- [x] Test xanh, lint/build sạch.

## Kiểm tra hoàn thành

Folder Postman "Dot 1 - Flashcard, Profile" (35 request, đã thêm vào collection). Token là `{{token}}` (hocvien, chạy "02 Login user" của Dot 0 trước). Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: đường thành công (request 01-30)
| # | Request | Kỳ vọng (theo DTO thật) |
|---|---|---|
| 01 | `GET /topics` | 200, mảng `{id, name{vi,en}, itemCount}` |
| 02-03 | `GET /flashcard/decks`, `?topicId=` | 200, `{items,page,pageSize,total,totalPages}`; mỗi bộ có `title`, `level`, `status` (NOT_STARTED/IN_PROGRESS/COMPLETED), `totalCards`, `learnedCards`, `progressPercent`; lọc đúng `topicId` |
| 05 | `GET /flashcard/decks/{{deckId}}` | 200, `cards[]` có `word`, `meaning{vi,en}`, `recallLevel` (null nếu chưa học) |
| 06-07 | `POST .../progress` `{flashcardId, recallLevel}` | 200 `{savedAt, learnedCards, progressPercent}` |
| 10 | `POST .../finish` `{studiedIds}` | 200, `studiedCards`, `remembered/almostRemembered/notRemembered`, `wordsToReview[]` |
| 12 | `GET` deck lại | `recallLevel` đã lưu, status khác NOT_STARTED |
| 13-16 | `GET|PATCH /profile` | 200, không có `passwordHash`; có `joinedAt`, `totalMinutes`, `completedLessons`, `masteredWords`; PATCH trả `User` |
| 19-23 | `GET|PATCH /settings` | 200; `language`/`theme` viết thường, `reminderTime` HH:mm, `timeZone`; giữ sau khi GET lại |
| 26-28 | `POST /profile/password` | 200, có `Set-Cookie refresh_token` mới; refresh bằng cookie đó 200; đổi lại 123456 |
| 29 | `POST /study/heartbeat` `{skill, refId}` | 200 `{sessionId, activeSeconds}` |

### B. Ca lỗi và bảo mật (request 04, 08, 09, 11, 17, 18, 23-25, 30-35)
- Không token (31-35): 401 `UNAUTHORIZED`.
- `level` sai 400 `VALIDATION`; `recallLevel` sai 400; thẻ không thuộc bộ 404; `studiedIds` lạ 400.
- Email sai định dạng 400; email đã dùng 409 `EMAIL_TAKEN`.
- Mật khẩu hiện tại sai 400 `WRONG_PASSWORD`; mật khẩu mới ngắn 400 `VALIDATION`.
- Đổi mật khẩu thu hồi phiên ở nơi khác và tiến độ không lẫn giữa người dùng: đã có trong test tự động (`ProfileApiTests`, `FlashcardApiTests`); thử tay bằng hai cookie jar nếu muốn.

### C. SQL kiểm tra dữ liệu
```sql
-- Tiến độ thẻ: recall_level đúng, next_review_at sau last_reviewed_at,
-- NOT_REMEMBERED có interval_days nhỏ hơn REMEMBERED
SELECT f.word, p.recall_level, p.review_count, p.interval_days, p.last_reviewed_at, p.next_review_at
FROM user_flashcard_progress p
JOIN flashcards f ON f.id = p.flashcard_id
JOIN users u ON u.id = p.user_id
WHERE u.email = 'hocvien@enlearning.vn' ORDER BY p.updated_at DESC LIMIT 10;

-- Heartbeat: active_seconds tăng dần, mỗi lần cộng không quá 60 giây
SELECT s.skill, BIN_TO_UUID(s.ref_id) AS ref, s.started_at, s.last_heartbeat_at, s.active_seconds
FROM study_sessions s JOIN users u ON u.id = s.user_id
WHERE u.email = 'hocvien@enlearning.vn' ORDER BY s.started_at DESC LIMIT 5;

-- Cài đặt đã lưu
SELECT s.language, s.theme, s.daily_goal_minutes, s.email_reminders, s.reminder_time, s.time_zone
FROM user_settings s JOIN users u ON u.id = s.user_id WHERE u.email = 'hocvien@enlearning.vn';
```

### D. Thử trên giao diện FE
1. Bỏ `topics`, `flashcard`, `profile`, `settings` khỏi `VITE_MOCK_MODULES` rồi chạy lại `npm run dev`.
2. Học một bộ thẻ, thoát giữa chừng rồi quay lại: tiến độ còn; học hết thì màn kết quả liệt kê từ cần ôn.
3. Cài đặt: đổi sang EN và giao diện tối, F5 vẫn giữ; đổi mật khẩu rồi đăng nhập lại bằng mật khẩu mới.
4. Tab Network: `POST /study/heartbeat` khoảng 30 giây một lần khi tab đang mở; tab bị ẩn hoặc không tương tác thì không gửi.

## Bước tiếp theo
1. (Đã đóng đợt ở phiên 1z.)
2. Nếu đợt 2 chưa làm, mở phiên mới:

```text
Đợt 2, bước 2.1: engine chấm điểm chung + Nghe BE.
Đọc .docs/ROADMAP.md và .docs/roadmap/dot-2-bo-may-lam-bai.md. Mẫu cấu trúc: module auth/.
Làm service chấm điểm, lưu PracticeAttempt, endpoint Nghe, test; chạy test hẹp.
Không lộ đáp án trước khi nộp. Không đụng FE, không sửa V1.
```
Nếu đợt 2 đã xong, chuyển sang Đợt 3.
