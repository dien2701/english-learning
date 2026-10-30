# EN-Learning

**Học tiếng Anh mỗi ngày, đủ cả 4 kỹ năng Nghe, Nói, Đọc, Viết, có AI chấm bài và trò chuyện cùng bạn.**

EN-Learning là website học tiếng Anh dành cho người Việt. Giao diện dùng được bằng tiếng Việt hoặc tiếng Anh. Người học tự luyện theo nhịp của mình, còn hệ thống ghi lại tiến độ, nhắc học và chỉ ra kỹ năng cần cải thiện.

## Video demo

[![Xem video demo EN-Learning](https://img.youtube.com/vi/I40x8kGZiw0/hqdefault.jpg)](https://www.youtube.com/watch?v=I40x8kGZiw0)

▶ Xem trên YouTube: https://www.youtube.com/watch?v=I40x8kGZiw0

---

## Phần 1. Giới thiệu sản phẩm

### Người học làm được gì?

| Chức năng | Bạn sẽ làm gì |
|---|---|
| 📊 **Trang tổng quan** | Xem mình đã học bao nhiêu, chuỗi ngày học liên tục, kỹ năng nào mạnh, kỹ năng nào yếu |
| 🃏 **Flashcard từ vựng** | Học từ mới theo chủ đề, mỗi thẻ có hình minh hoạ và phát âm |
| 🎧 **Luyện nghe** | Nghe đoạn hội thoại, bài nói rồi trả lời câu hỏi |
| 📖 **Luyện đọc** | Đọc bài ngắn rồi trả lời câu hỏi để kiểm tra mức hiểu |
| ✍️ **Luyện viết** | Viết bài theo đề, AI chấm điểm và góp ý cách sửa |
| 🎤 **Luyện nói** | Đọc to từng câu vào micro, AI chấm phát âm từng câu |
| 📝 **Bài kiểm tra** | Làm đề tổng hợp có tính giờ, xem lại kết quả và lịch sử làm bài |
| 💬 **Trò chuyện với AI** | Nói chuyện bằng tiếng Anh với trợ lý AI như với một người bạn (tối đa 30 tin mỗi ngày) |

Ngoài ra còn có hồ sơ cá nhân với ảnh đại diện, cài đặt ngôn ngữ, thông báo trong ứng dụng và email nhắc học khi bạn quên vào học.

### Người quản trị làm được gì?

- Thêm, sửa, xoá bài học: chủ đề, từ vựng, bài nghe, bài đọc, đề viết, đề nói, đề kiểm tra. Có thể nhập nhanh hàng loạt từ file CSV.
- Quản lý tài khoản người học.
- Gửi thông báo tới người học.
- Xem thống kê chung của toàn hệ thống.

### Điểm nổi bật

- **Đủ 4 kỹ năng trên một nơi**, không phải dùng nhiều ứng dụng khác nhau.
- **AI chấm ngay**: bài viết và bài nói được chấm điểm, kèm nhận xét cụ thể.
- **Nội dung thật**: hàng trăm từ vựng theo 20 chủ đề, bài nghe có giọng đọc của người bản xứ.
- **Song ngữ Việt/Anh**: đổi ngôn ngữ giao diện bất cứ lúc nào.
- **Bảo mật**: mật khẩu được mã hoá, phiên đăng nhập tự gia hạn an toàn, quên mật khẩu thì lấy lại bằng mã gửi qua email.

### Dùng thử

Sau khi chạy hệ thống trên máy (xem Phần 2), đăng nhập bằng một trong các tài khoản mẫu, mật khẩu `123456`:

| Email | Dành cho |
|---|---|
| `hocvien@enlearning.vn` | Học viên đã có sẵn lịch sử học, để xem biểu đồ và thống kê |
| `khoa@enlearning.vn` | Học viên mới |

---

## Phần 2. Dành cho đội kỹ thuật

### Công nghệ

- **Backend:** Java 21, Spring Boot 4.1, Spring Security (JWT HS256), JPA/Hibernate, MySQL 9.0, Flyway.
- **Frontend:** React 19, TypeScript, Vite, Ant Design 6, Tailwind 4, React Router 7, i18next, Recharts.
- **Dịch vụ ngoài (tuỳ chọn):** Gemini 3.5 Flash-Lite (chấm Viết/Nói, Chat), Cloudinary (lưu audio, ảnh), Gmail SMTP (email). Thiếu khoá thì hệ thống tự dùng bản giả hoặc lưu cục bộ.

### Yêu cầu

- JDK 21
- Node.js 20 trở lên
- MySQL 9.0, đã tạo sẵn database (ví dụ `en_learning`)

### Chạy backend (`apps/backend`)

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

> Trên PowerShell: dùng `copy` thay cho `cp`, và đặt biến bằng `$env:SPRING_PROFILES_ACTIVE="dev"` trước khi chạy `./mvnw spring-boot:run`.

### Chạy frontend (`apps/frontend`)

```bash
cd apps/frontend
cp .env.example .env
npm install
npm run dev
```

Mở `http://localhost:5173`. Vite chuyển `/api` sang backend ở cổng 8080, nên cần chạy backend trước.

Các lệnh khác: `npm run lint`, `npm run build`.

### Kiến trúc

Monorepo hai ứng dụng, giao tiếp qua REST JSON.

```
Trình duyệt ──► Frontend (Vite, :5173) ──/api──► Backend (Spring Boot, :8080/api) ──► MySQL
                                                       │
                                                       └──► Gemini · Cloudinary · SMTP
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
