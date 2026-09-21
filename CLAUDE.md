# EN-Learning: hướng dẫn cho Claude Code

Nền tảng học tiếng Anh. Monorepo: `apps/frontend` (React) + `apps/backend` (Spring Boot).
Trả lời ngắn gọn bằng tiếng Việt, chỉ in đoạn code thay đổi, không giải thích dài trừ khi được hỏi.
Tên file, tên hàm, tiêu chí xong viết tiếng Anh nếu được.

## Cách làm việc (tiết kiệm token)
- KHÔNG đọc sẵn `.docs/ARCHITECTURE.md` hay `.docs/FEATURES_DONE.md`. Chỉ đọc đúng mục cần (schema: mục 1; luồng nghiệp vụ: mục 2; Auth: mục 3).
- Làm theo đợt: đọc bảng trạng thái `.docs/ROADMAP.md`, rồi đúng MỘT file `.docs/roadmap/dot-N-*.md`. Một phiên một module.
- Làm UI thì đọc `.docs/STYLEGUIDE.md`. Không đọc `.docs/design_briefs`, `.docs/frontend_plans`, `.docs/ideas` (đã cũ) trừ khi được yêu cầu.
- Mẫu cấu trúc BE: module `auth/`. Không khám phá rộng khi đã có đường dẫn.
- KHÔNG tự kiểm thử chức năng: không chạy server, trình duyệt, Postman/curl, không chụp ảnh. Người dùng tự test thủ công (Postman, SQL, giao diện). Chỉ chạy lệnh biên dịch/lint (`tsc`, `npm run lint`, `npm run build`, `./mvnw -q compile`) cho tới khi sạch, và cuối phiên nêu ngắn gọn việc cần tự kiểm. Không dùng agent phụ hay chạy song song.
- Prompt `Làm phiên <mã>.`: tra dòng `<mã>` trong bảng "Phiên làm việc" của `ROADMAP.md`, chỉ làm đúng phạm vi dòng đó (đọc file đợt tương ứng). Bảng này ưu tiên hơn mục "Bước tiếp theo" trong file đợt.
- Cuối phiên: tick checkbox trong file đợt, tick cột Trạng thái của phiên trong `ROADMAP.md` (và bảng đợt nếu đợt xong), thêm tối đa 5 dòng vào `.docs/FEATURES_DONE.md`.
- Dòng CUỐI CÙNG của mỗi phiên luôn in đúng khối sau (lấy dòng chưa tick kế tiếp trong bảng):
  `➡ Phiên tiếp: /clear → Model: <model> · Effort: <effort> → Prompt: "Làm phiên <mã>."` kèm 1 dòng việc cần tự kiểm (Postman/SQL/giao diện) trước khi sang phiên đó.
- Kết đợt: làm theo mục "Đóng đợt (chung)" trong `ROADMAP.md`. Chốt bảng "Kiểm tra hoàn thành" của file đợt theo DTO thật, rồi thêm folder Postman vào `.docs/postman/en-learning.postman_collection.json` bằng script Node (nạp JSON, thêm folder, ghi lại; script đặt ở thư mục tạm). KHÔNG đọc cả file collection. Collection đã có sẵn folder "Dot 0 - Auth".

## Lệnh
- BE (`apps/backend`, cần JDK 21, PATH mặc định là Java 8): `./mvnw -q test -Dtest=XxxTests`; chạy: `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` (cổng 8080, context-path `/api`, cần `.env` theo `.env.example`; profile `dev` tự seed khi bảng `users` trống, 3 tài khoản mật khẩu `123456`).
- FE (`apps/frontend`): `npm run dev` (5173), `npm run lint`, `npm run build` (gồm `tsc -b`). Xong khi lint và build sạch.

## Backend
- Java 21, Spring Boot 4.1, JPA/Hibernate, Spring Security (JWT HS256), MySQL 9.0, Flyway. Package `vn.enlearning.backend`: `auth`, `common`, `config`, `content`, `entity`, `mail`, `security`, `seed`. Module mới đặt cạnh `auth/` với `controller`, `dto`, `repository`, `service`.
- Luồng Controller (mỏng) → Service (toàn bộ nghiệp vụ) → Repository. Vỏ thành công `{success,data}`; lỗi `{message,code,messageKey,fieldErrorKeys}` qua `ApiException` + `GlobalExceptionHandler`.
- DB: `V1__init_schema.sql` KHÔNG sửa nữa; đổi schema thì thêm `V2`, `V3`... và sửa entity cùng lúc (`ddl-auto: validate`). Khoá chính UUID v7, nội dung song ngữ `xxxVi/xxxEn`, xoá mềm `deletedAt`, điểm thang 10.
- Test tích hợp (`*Tests`, MockMvc) chạy trên chính DB `en_learning`, tự rollback. Trước khi dọn hay drop bảng, kiểm tra cổng 8080 xem người dùng có đang chạy server không.

## Frontend
- React 19, TypeScript, Vite, Ant Design 6 + Tailwind 4 (màu theo token CSS), React Router 7, i18next VI/EN, Recharts. KHÔNG Redux, RTK Query, Bootstrap.
- Luồng dữ liệu: `src/shared/api/client.ts` (axios) → `src/services/*` → hook `useApi`. Kiểu dùng chung ở `src/types`, phải khớp phản hồi BE.
- `components/ui` và `components/practice` chỉ nhận props, không gọi API. Chuỗi giao diện dùng `t('...')`; nội dung song ngữ dùng kiểu `L10n` + `useLanguage`.

## Bảo mật
- BCrypt cost 12; access token JWT 15 phút; refresh token trong cookie HttpOnly, xoay vòng, DB chỉ lưu SHA-256. OTP đặt lại mật khẩu chỉ lưu HMAC.
- Không trả `passwordHash` hay dữ liệu nhạy cảm trong API. Bí mật (JWT_SECRET, RESET_CODE_SECRET, OPENAI_API_KEY, mật khẩu DB/email) ở `.env`, không commit, không để trong React.

## Quyết định đã chốt (21/09/2026)
- FE gọi BE thật cho mọi module; cơ chế mock (`src/mocks`, `VITE_MOCK_MODULES`) đã xoá ở phiên 9a.
- AI: interface + bản giả; chấm Viết/Nói và Chat dùng Gemini 2.5 Flash khi `GEMINI_API_KEY` có giá trị (đợt 11). OpenAI chỉ cho TTS bài nghe khi `OPENAI_API_KEY` có giá trị (đợt 12). Luyện nói vẫn nhận file âm thanh ở BE, chỉ lưu transcript/điểm.
- Hạ tầng đơn giản: `@Scheduled` + `email_logs`, Caffeine, bucket4j trong bộ nhớ, audio/ảnh giữ đường dẫn ngoài. Audio bài nghe lưu Cloudinary khi có `CLOUDINARY_URL`, thiếu thì lưu cục bộ (đợt 12). KHÔNG thêm Redis, RabbitMQ, Docker, CI hay test tự động FE trừ khi người dùng đổi ý.
- Phạm vi ngoài 10 chức năng: chỉ thêm hoàn tất song ngữ VI/EN.
