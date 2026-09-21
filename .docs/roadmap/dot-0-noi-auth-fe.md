# Đợt 0: Nối Auth với FE, mock theo module

Chỉ sửa FE. Phụ thuộc: không. Chia phiên: 1 phiên, mô hình rẻ. Prompt nêu tên file, không để Claude tự dò.
File cần đụng: `src/shared/api/client.ts`, `src/shared/api/mockAdapter.ts`, `src/services/authService.ts`, kiểu `AuthSession` (trong `src/types`), `src/mocks/router.ts`, `vite.config.ts`, `.env.example`.

## Việc làm
- [x] Bỏ `refreshToken` khỏi `authService.ts` (localStorage) và khỏi kiểu `AuthSession`. BE chỉ trả `{token,user}`, refresh token nằm trong cookie HttpOnly.
- [x] `client.ts`: bật `withCredentials: true`. Khi gặp 401 thì gọi `POST /auth/refresh` một lần rồi thử lại request gốc. Nhiều request 401 cùng lúc chỉ refresh một lần. Không refresh cho `/auth/login` và `/auth/refresh`. Refresh lỗi thì xoá phiên và về `/login`.
- [x] Mock theo module: thêm `VITE_MOCK_MODULES` (danh sách tiền tố, ví dụ `flashcard,writing,exam`). Request có tiền tố trong danh sách thì đi qua mock, còn lại gọi BE thật. Danh sách rỗng nghĩa là mọi thứ gọi BE thật. Auth (`/auth/*`) không còn mock. Bỏ `VITE_USE_MOCK` hoặc để tương thích ngược, ghi rõ trong `.env.example`.
- [x] Mock Dashboard và Hồ sơ hiện đọc tài khoản theo token `mock.<id>.…`; sửa để chạy được khi token là JWT thật (quyết khi làm: đọc claim `sub` hoặc dùng người dùng mock mặc định).
- [x] Vite proxy `/api` sang `http://localhost:8080` để cookie refresh cùng site; `VITE_API_BASE_URL=/api`.

## Tiêu chí xong
- [x] Đăng ký, đăng nhập, đăng xuất, quên và đặt lại mật khẩu (OTP ra log khi chưa cấu hình mail) chạy trên tài khoản seed.
- [x] Tải lại trang vẫn giữ phiên. Access token hết hạn thì tự refresh, request gốc vẫn thành công.
- [x] Tài khoản ADMIN vào thẳng `/admin`; tài khoản bị khoá (`khoa@enlearning.vn`) nhận đúng lỗi.
- [x] `npm run lint` và `npm run build` sạch (phiên 0a sửa 25 lỗi lint có sẵn).

## Kiểm tra hoàn thành

API Auth đã có sẵn nên bước này cụ thể ngay, không cần chờ code. Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: folder "Dot 0 - Auth" (19 request, đã có trong collection)
Chạy cả folder bằng Run, kỳ vọng 19/19 test đạt.

| # | Kiểm tra | Kỳ vọng |
|---|---|---|
| 01-02 | Login admin và user | 200, `data.token` + `data.user`, đúng `role`; cookie `refresh_token` HttpOnly; thân phản hồi không có `refreshToken`, `passwordHash` |
| 03-04 | `GET /auth/me` có và không token | 200 đúng người dùng; 401 `UNAUTHORIZED` |
| 05 | `POST /auth/refresh` (cookie) | 200, token mới, cookie được xoay |
| 06-07 | `GET /auth/check-email` | `data.available` false với email seed, true với email mới |
| 08-10 | Register | 201, `data.token` + `data.user` (không `refreshToken`); trùng email 409 `EMAIL_TAKEN` (`fieldErrorKeys.email`); sai xác nhận mật khẩu 400 `VALIDATION` kèm `fieldErrorKeys` |
| 11-12 | Login sai | 401 `INVALID_CREDENTIALS`; tài khoản khoá 403 `ACCOUNT_LOCKED` |
| 13-15 | Quên/đặt lại mật khẩu | 200, `data.message` giống hệt dù email có hay không; sai mã 400 `INVALID_CODE` (`fieldErrorKeys.code`) |
| 16-17 | `/admin/users` với token USER và không token | 403 `FORBIDDEN`; 401 `UNAUTHORIZED` |
| 18-19 | Logout (`DELETE /auth/session`) rồi Refresh | 200, `data.loggedOut` = true, cookie bị xoá; Refresh sau đó 401 `UNAUTHORIZED` |

Kiểm tay thêm (không tự động được): đặt lại mật khẩu thật. Gọi `POST /auth/forgot-password` cho email vừa tạo ở request 08 (`{{newEmail}}`), lấy mã 6 số trong log BE (dòng `[EMAIL GIẢ LẬP ...]`), gọi `POST /auth/reset-password` với mã đó, kỳ vọng 200; đăng nhập bằng mật khẩu mới thành công, mật khẩu cũ 401; refresh bằng cookie cũ 401 (mọi phiên bị thu hồi). Không dùng tài khoản seed cho bước này.

### B. SQL kiểm tra dữ liệu
```sql
-- Sau logout, refresh token mới nhất phải có revoked_at
SELECT BIN_TO_UUID(user_id) AS uid, created_at, revoked_at, expires_at
FROM refresh_tokens ORDER BY created_at DESC LIMIT 5;

-- Mật khẩu là BCrypt, không plain text (kỳ vọng bắt đầu bằng $2)
SELECT email, LEFT(password_hash, 4) AS prefix FROM users WHERE email LIKE 'postman.%';

-- Sau request 15 (sai mã), attempts >= 1, used_at còn NULL
SELECT attempts, used_at, expires_at FROM password_reset_tokens ORDER BY created_at DESC LIMIT 3;
```

### C. Thử trên giao diện FE
1. `apps/frontend/.env`: đặt `VITE_MOCK_MODULES=topics,flashcard,writing,listening,reading,speaking,exams,attempts,dashboard,statistics,chat,notifications,profile,settings,admin` (Auth gọi BE thật, phần còn lại vẫn mock). `npm run dev`, mở `http://localhost:5173`.
2. Đăng nhập `hocvien@enlearning.vn` / `123456` vào được Dashboard. F5 vẫn giữ phiên.
3. DevTools, tab Application: cookie `refresh_token` có HttpOnly; localStorage không còn `refreshToken`.
4. Tự refresh: làm hỏng access token trong localStorage (khoá do `authService.ts` lưu), tải lại trang. App không bị đá ra `/login`; tab Network thấy `POST /auth/refresh` trả 200.
5. Đăng nhập `admin@enlearning.vn` vào thẳng `/admin`; `khoa@enlearning.vn` báo tài khoản bị khoá; đăng xuất thì cookie mất.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md`.
2. Mở phiên mới cho Đợt 1 (Đợt 2 có thể làm song song trong phiên khác):

```text
Đợt 1, bước 1.1: Topic + Flashcard BE.
Đọc .docs/ROADMAP.md và .docs/roadmap/dot-1-flashcard-ho-so.md. Mẫu cấu trúc: module auth/.
Làm: repository, service, controller, DTO, test cho các endpoint 1.1. Chỉ chạy test hẹp
(-Dtest=Flashcard*). Không đụng FE, không sửa V1. Xong khi test xanh; cuối phiên ghi
tối đa 5 dòng vào FEATURES_DONE.md.
```
