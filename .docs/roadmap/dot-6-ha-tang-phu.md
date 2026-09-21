# Đợt 6: Hạ tầng phụ (bản đơn giản)

Phụ thuộc: đợt 1 và 5. Chia phiên: 6.1 email nhắc học; 6.2 rate limit + dọn dẹp. Mô hình rẻ đủ dùng.
KHÔNG thêm Redis, RabbitMQ, Cloudinary. Code đặt sau interface để nâng cấp sau.

## Việc làm
- [x] Email nhắc học: `@Scheduled` chạy mỗi phút, chọn người dùng đã bật nhắc và đến giờ theo múi giờ riêng (`UserSetting`). Bỏ qua người đã đủ mục tiêu trong ngày. Ghi `email_logs`; UNIQUE (user, reminderDate) chặn gửi trùng. Dùng lại `EmailSender` (Gmail SMTP hoặc ghi log khi chưa cấu hình).
- [x] Giới hạn tần suất bằng bucket4j trong bộ nhớ cho `login`, `check-email`, `forgot-password`; trả 429 kèm `messageKey`.
- [x] Rà lại Caffeine (đã đặt ở đợt 1): TTL và xoá cache.
- [x] Job định kỳ dọn refresh token, mã OTP hết hạn và `study_sessions` treo.

## Tiêu chí xong
- [ ] Đặt giờ nhắc gần hiện tại, nhận đúng một email (hoặc một dòng log), chạy lại không gửi trùng.
- [ ] Vượt ngưỡng đăng nhập nhận 429.
- [x] Ghi vào `ARCHITECTURE.md` (mục 4) rằng hạ tầng là Caffeine, `@Scheduled`, bucket4j thay cho Redis, RabbitMQ, Cloudinary.
- [x] Test xanh.

## Kiểm tra hoàn thành

Folder Postman "Dot 6 - Gioi han tan suat" (3 request, mỗi request tự gọi lặp bằng script; chạy CUỐI CÙNG, từng request một, đợi 1 phút giữa các request). Bảng dưới đã chốt theo BE thật. Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: giới hạn tần suất
| # | Request | Kỳ vọng |
|---|---|---|
| 1 | `01 Login sai lặp 12 lần` | 10 lần đầu 401, sau đó 429; `code` RATE_LIMITED, `messageKey` `errors.tooManyRequests`, header `Retry-After` |
| 2 | `02 check-email lặp 32 lần` | 30 lần đầu 200, sau đó 429 |
| 3 | `03 forgot-password lặp 7 lần` | 5 lần đầu 200 (luôn cùng thông điệp, chống dò email), sau đó 429 |
| 4 | Đợi hết 1 phút, Login đúng (folder Dot 0) | 200 trở lại |

Lưu ý: khi đã có rate limit, chạy lại folder "Dot 0 - Auth" nhiều lần liên tiếp có thể chạm ngưỡng; nếu gặp 429 thì đợi hết cửa sổ rồi chạy lại.

### B. Email nhắc học (không dùng Postman)
1. Đăng nhập bằng Postman, `PATCH /settings` bật nhắc học và đặt `reminderTime` là phút kế tiếp (theo múi giờ của người dùng).
2. Đợi tối đa một phút. Có SMTP thì kiểm hộp thư; chưa có thì tìm dòng `[EMAIL GIẢ LẬP ...]` trong log BE.
3. Chờ qua phút đó và quan sát tiếp: không có email thứ hai trong cùng ngày.
4. Người đã học đủ mục tiêu trong ngày không nhận email (ghi thêm thời gian học bằng `POST /study/heartbeat` rồi thử lại).

### C. SQL kiểm tra dữ liệu
```sql
-- Có đúng một dòng cho ngày hôm nay
SELECT type, status, recipient_email, reminder_date, sent_at, error_message
FROM email_logs ORDER BY created_at DESC LIMIT 5;

-- Không trùng: kỳ vọng không có dòng nào
SELECT user_id, reminder_date, COUNT(*) AS c
FROM email_logs GROUP BY user_id, reminder_date HAVING c > 1;

-- Sau khi job dọn dẹp chạy: không còn refresh token hết hạn quá ngưỡng cấu hình
SELECT COUNT(*) AS expired_left FROM refresh_tokens WHERE expires_at < NOW() - INTERVAL 1 DAY;
```
Job dọn dẹp chạy theo lịch nên kiểm bằng test tích hợp gọi trực tiếp phương thức dọn; không cần đợi.

### D. Thử trên giao diện FE
Cài đặt: bật/tắt nhắc học và đổi giờ, lưu, F5 giữ nguyên. Đăng nhập sai nhiều lần, thấy thông báo chờ (429) bằng đúng ngôn ngữ đang chọn.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md`. Đồng thời cập nhật `ARCHITECTURE.md` mục 4 theo hạ tầng thực tế.
2. Nếu đợt 7 chưa xong, làm đợt 7; xong rồi thì sang đợt cuối:

```text
Đợt cuối: dọn dẹp.
Đọc .docs/ROADMAP.md (mục "Đợt cuối"). Xoá src/mocks và mockAdapter khi VITE_MOCK_MODULES rỗng,
cập nhật FEATURES_DONE.md, ARCHITECTURE.md, AGENTS.md cho khớp. Chạy lint, build và cả bộ test BE.
```
