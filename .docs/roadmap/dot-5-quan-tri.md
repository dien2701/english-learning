# Đợt 5: Quản trị, Thông báo

Phụ thuộc: đợt 2 và 4 (cần bảng lịch sử để áp quy tắc không xoá nội dung đã dùng). Chia phiên: 5.1 Người dùng + Thông báo + Dashboard admin; 5.2 CRUD nội dung + Chủ đề; 5.3 nối FE.
Với CRUD nội dung: làm MỘT loại hoàn chỉnh trước, rồi nhân theo mẫu cho các loại còn lại (dùng mô hình rẻ).

## Endpoint
- `GET /admin/dashboard` (biểu đồ đăng ký, thống kê kho nội dung)
- `GET /admin/users`, `GET /admin/users/:id`, `PATCH /admin/users/:id` (khoá/mở khoá, đổi vai trò)
- `GET|POST /admin/content`, `GET|PATCH|PUT /admin/content/:id`
- `GET|POST /admin/topics`, `PUT /admin/topics/:id`
- `GET|POST /admin/notifications`, `POST /admin/notifications/:id/send`
- Phía người học: `GET /notifications`, `GET /notifications/unread-count`, `PATCH /notifications/:id/read`, `POST /notifications/read-all`

## Việc cần quyết khi làm
- `POST /admin/content` xử lý nhiều loại (bộ thẻ, nghe, đọc, nói, viết, đề kiểm tra): đề xuất cách chia DTO theo loại.
- Khoá tài khoản: access token đang sống còn hiệu lực tối đa 15 phút. Chọn kiểm tra trạng thái khoá ở mỗi request hay giữ nguyên.
- Xoá cache Caffeine (đợt 1) khi Admin sửa bộ thẻ hoặc chủ đề.

## Việc làm
- [ ] `/admin/**` cần `ROLE_ADMIN`; có test 403 cho USER và 401 khi chưa đăng nhập.
- [ ] Nội dung đã có trong lịch sử học: không xoá, chỉ chuyển `INACTIVE`. Bắt lỗi khoá ngoại và trả lỗi rõ ràng, không để lộ thành 500.
- [ ] Gửi thông báo tạo bản nhận `UserNotification` cho người học; trạng thái đã đọc.
- [ ] FE: bỏ `admin`, `notifications` khỏi `VITE_MOCK_MODULES`.

## Tiêu chí xong
- [ ] Admin tạo, sửa, ngừng hoạt động từng loại nội dung; người học thấy thay đổi.
- [ ] Gửi một thông báo, người học nhận và đánh dấu đã đọc.
- [ ] Test xanh, lint/build sạch.

## Kiểm tra hoàn thành

Folder "Dot 5 - Admin, Thong bao" sẽ được thêm khi đợt xong. Bảng dưới là DỰ KIẾN. Dùng `{{adminToken}}` cho `/admin/**`, `{{token}}` cho phía người học. Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: đường thành công
| # | Request | Kỳ vọng |
|---|---|---|
| 1 | `GET /admin/dashboard` | 200, biểu đồ đăng ký và thống kê kho nội dung |
| 2 | `GET /admin/users`, `/admin/users/{{userId}}` | 200, không có `passwordHash` |
| 3 | `PATCH /admin/users/{{userId}}` khoá tài khoản (trường và giá trị chốt khi làm) | 200; Login tài khoản đó trả 403 `ACCOUNT_LOCKED`; mở khoá lại thì Login 200 |
| 4 | `POST /admin/topics`, `PUT /admin/topics/{{id}}` | 200/201; `GET /topics` bằng token người học thấy thay đổi |
| 5 | `POST /admin/content` cho từng loại (bộ thẻ, nghe, đọc, nói, viết, đề); `GET /admin/content`, `/:id`; `PATCH`/`PUT` | tạo, đọc, sửa được; người học thấy nội dung mới |
| 6 | `PATCH` đưa một nội dung sang INACTIVE | người học không còn thấy trong danh sách |
| 7 | `POST /admin/notifications` (nháp), rồi `POST /admin/notifications/{{id}}/send` | 200; `status` SENT, `recipientCount` > 0 |
| 8 | (token người học) `GET /notifications`, `/notifications/unread-count` | thấy thông báo vừa gửi, số chưa đọc tăng |
| 9 | `PATCH /notifications/{{id}}/read`, `POST /notifications/read-all` | số chưa đọc giảm rồi về 0 |

### B. Ca lỗi và bảo mật
- Mọi `/admin/**` với token USER: 403; không token: 401 (thêm vào folder cho từng nhóm endpoint).
- Xoá hoặc ngừng nội dung đã có trong lịch sử học (ví dụ bộ thẻ hocvien đã học): kết quả là 409 hoặc chuyển INACTIVE, tuyệt đối không phải 500.
- Tạo nội dung thiếu trường bắt buộc: 400 kèm `fieldErrorKeys`.
- Người học A đánh dấu đã đọc thông báo của B: 404.
- Ghi nhận hành vi: token của tài khoản vừa bị khoá còn hiệu lực tối đa 15 phút (hoặc bị chặn ngay, tuỳ quyết định ở mục "Việc cần quyết").

### C. SQL kiểm tra dữ liệu
```sql
-- Thông báo đã gửi: recipient_count khớp số dòng user_notifications
SELECT BIN_TO_UUID(n.id) AS id, n.title, n.status, n.recipient_count, n.sent_at,
       (SELECT COUNT(*) FROM user_notifications un WHERE un.notification_id = n.id) AS actual
FROM notifications n ORDER BY n.created_at DESC LIMIT 3;

-- Bản nhận của người học: read_at có giá trị sau khi đánh dấu đã đọc
SELECT n.title, un.read_at
FROM user_notifications un
JOIN notifications n ON n.id = un.notification_id
JOIN users u ON u.id = un.user_id
WHERE u.email = 'hocvien@enlearning.vn' ORDER BY un.created_at DESC LIMIT 5;

-- Khoá/mở khoá và ngừng nội dung
SELECT email, role, status FROM users WHERE email = 'khoa@enlearning.vn';
SELECT title_vi, status, deleted_at FROM flashcard_decks ORDER BY updated_at DESC LIMIT 5;
```

### D. Thử trên giao diện FE
1. Bỏ `admin`, `notifications` khỏi `VITE_MOCK_MODULES` (danh sách nên rỗng nếu các đợt trước đã xong).
2. Đăng nhập admin: bảng điều khiển có số liệu thật; khoá rồi mở khoá một người dùng; tạo, sửa, ngừng một nội dung; tạo và gửi một thông báo.
3. Đăng nhập người học: chuông thông báo hiện số chưa đọc; mở Trung tâm thông báo, đánh dấu đã đọc.
4. Đăng nhập tài khoản USER rồi vào `/admin`: bị chuyển sang trang 403.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md`.
2. Mở phiên mới cho Đợt 6 (mô hình rẻ đủ dùng):

```text
Đợt 6, bước 6.1: email nhắc học bằng @Scheduled.
Đọc .docs/ROADMAP.md và .docs/roadmap/dot-6-ha-tang-phu.md. Không thêm RabbitMQ/Redis.
Chọn người dùng đến giờ theo múi giờ riêng, ghi email_logs, UNIQUE chặn gửi trùng, dùng EmailSender.
Viết test; chạy test hẹp. Không đụng FE.
```
