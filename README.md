# EN-Learning

Nền tảng học tiếng Anh trên web, giao diện song ngữ Việt/Anh. Người học có flashcard, luyện nghe, đọc, viết, nói, làm bài kiểm tra và chat với AI. Người quản trị quản lý nội dung, người dùng và thông báo.

## Chức năng

| # | Chức năng | Mô tả ngắn |
|---|---|---|
| 1 | Đăng ký, đăng nhập | JWT, refresh token xoay vòng trong cookie HttpOnly, quên mật khẩu bằng OTP qua email |
| 2 | Dashboard | Tiến độ, chuỗi ngày học, thống kê theo kỹ năng |
| 3 | Flashcard | Học từ vựng theo chủ đề, có ảnh và phát âm |
| 4 | Luyện viết | AI chấm điểm và nhận xét bài viết |
| 5 | Luyện nghe | Bài nghe có audio, câu hỏi trắc nghiệm |
| 6 | Luyện đọc | Bài đọc kèm câu hỏi |
| 7 | Luyện nói | Thu âm thật, AI chấm phát âm từng câu |
| 8 | Bài kiểm tra | Đề thi tổng hợp, tính giờ, lưu lịch sử |
| 9 | Chat với AI | Hội thoại luyện tiếng Anh (giới hạn 30 tin/ngày) |
| 10 | Quản trị | Nội dung (nhập CSV), người dùng, thông báo, dashboard quản trị |

Ngoài ra: nhắc học qua email, giới hạn tần suất gọi API, hồ sơ và ảnh đại diện, cài đặt cá nhân.

## Công nghệ

- **Backend:** Java 21, Spring Boot 4.1, Spring Security (JWT HS256), JPA/Hibernate, MySQL 9.0, Flyway.
- **Frontend:** React 19, TypeScript, Vite, Ant Design 6, Tailwind 4, React Router 7, i18next, Recharts.
- **Dịch vụ ngoài (tuỳ chọn):** Gemini 3.5 Flash-Lite (chấm Viết/Nói, Chat), Cloudinary (lưu audio, ảnh), Gmail SMTP (email). Thiếu khoá thì hệ thống tự dùng bản giả hoặc lưu cục bộ.

## Cài đặt và chạy

### Yêu cầu

- JDK 21
- Node.js 20 trở lên
- MySQL 9.0, đã tạo sẵn database (ví dụ `en_learning`)

### Backend (`apps/backend`)

```bash
cd apps/backend
cp .env.example .env
```

Mở `.env`, điền tối thiểu `DB_*`, `JWT_SECRET`, `RESET_CODE_SECRET` (mỗi khoá ít nhất 32 ký tự, sinh bằng `openssl rand -hex 32`). Các biến còn lại có thể để trống, ý nghĩa ghi ngay trong `.env.example`.

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

API chạy ở `http://localhost:8080/api`. Flyway tự tạo bảng. Profile `dev` tự nạp dữ liệu mẫu khi bảng `users` trống. Muốn nạp thêm dữ liệu thật (chủ đề, từ vựng, bài nghe), đặt `REAL_DATA_SEED=true` cho một lần khởi động.

Chạy test: `./mvnw -q test` (test tích hợp chạy trên DB đã cấu hình và tự rollback).

### Frontend (`apps/frontend`)

```bash
cd apps/frontend
cp .env.example .env
npm install
npm run dev
```

Mở `http://localhost:5173`. Vite chuyển `/api` sang backend ở cổng 8080, nên cần chạy backend trước.

Các lệnh khác: `npm run lint`, `npm run build`.

### Tài khoản mẫu (profile `dev`)

Mật khẩu chung: `123456`

| Email | Vai trò |
|---|---|
| `admin@enlearning.vn` | Quản trị |
| `hocvien@enlearning.vn` | Học viên (có sẵn lịch sử học) |
| `khoa@enlearning.vn` | Học viên |

## Kiến trúc

Monorepo hai ứng dụng, giao tiếp qua REST JSON.

```
Trình duyệt ──► Frontend (Vite, :5173) ──/api──► Backend (Spring Boot, :8080/api) ──► MySQL
                                                       │
                                                       └──► Gemini · OpenAI TTS · Cloudinary · SMTP
```

**Backend:** mỗi module theo luồng `Controller` (mỏng) → `Service` (nghiệp vụ) → `Repository`. Phản hồi thành công có dạng `{ success, data }`, lỗi có dạng `{ message, code, messageKey, fieldErrorKeys }`. Khoá chính UUID v7, nội dung song ngữ `xxxVi/xxxEn`, xoá mềm bằng `deletedAt`, điểm thang 10.

**Frontend:** `shared/api/client.ts` (axios) → `services/*` → hook `useApi` → trang. Component trong `components/ui` và `components/practice` chỉ nhận props, không gọi API.

### Cấu trúc thư mục

```
.
├── apps/
│   ├── backend/                     Spring Boot
│   │   └── src/main/
│   │       ├── java/vn/enlearning/backend/
│   │       │   ├── auth/            đăng ký, đăng nhập, token, quên mật khẩu
│   │       │   ├── topic/ flashcard/ practice/ writing/ speaking/ chat/
│   │       │   ├── dashboard/ study/ profile/ avatar/ notification/
│   │       │   ├── admin/           quản trị nội dung, người dùng
│   │       │   ├── ai/ audio/ mail/ reminder/ ratelimit/ cleanup/
│   │       │   ├── security/ config/ common/ entity/
│   │       │   └── seed/            nạp dữ liệu mẫu và dữ liệu thật
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── db/              migration Flyway (V1, V2, ...)
│   │           └── seed/ seed-demo/ dữ liệu JSON
│   └── frontend/                    React
│       └── src/
│           ├── pages/               mỗi chức năng một thư mục, admin/ cho quản trị
│           ├── components/          ui/, practice/, ...
│           ├── services/            gọi API theo module
│           ├── shared/api/          axios client
│           ├── types/               kiểu dữ liệu khớp phản hồi BE
│           ├── i18n/                bản dịch VI/EN
│           └── hooks/ contexts/ theme/ styles/ utils/
└── .docs/                           tài liệu thiết kế
    ├── ARCHITECTURE.md              schema, luồng nghiệp vụ, Auth
    ├── FEATURES_DONE.md             tiến độ chi tiết
    ├── ROADMAP.md, roadmap/         kế hoạch theo đợt
    ├── STYLEGUIDE.md                quy ước giao diện
    └── postman/                     collection Postman
```
