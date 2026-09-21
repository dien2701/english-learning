# ROADMAP: hoàn thiện EN-Learning

Cập nhật: 21/09/2026. Mỗi phiên chỉ đọc file này và đúng một file đợt trong `.docs/roadmap/`.
Quyết định nền (nối FE theo module, AI bản giả, hạ tầng đơn giản, phạm vi) nằm trong `CLAUDE.md`.

## Trạng thái

| Đợt | Nội dung | Phụ thuộc | Trạng thái |
|---|---|---|---|
| 0 | Nối Auth với FE, mock theo module | không | [x] |
| 1 | Flashcard, Hồ sơ/Cài đặt, heartbeat | 0 | [x] |
| 2 | Bộ máy làm bài: Nghe, Đọc, Kiểm tra | 0 | [x] |
| 3 | Dashboard, Thống kê | 1, 2 | [x] |
| 4 | AI: Viết, Chat, Nói (bản giả trước) | 0 | [ ] |
| 5 | Quản trị, Thông báo | 2, 4 | [x] |
| 6 | Email nhắc học, rate limit, cache, dọn dẹp | 1, 5 | [x] |
| 7 | Hoàn tất song ngữ VI/EN (làm xen kẽ được) | không | [x] |
| Cuối | Xoá mock, cập nhật tài liệu, nợ kỹ thuật nhỏ | tất cả | [ ] |

Chi tiết: `.docs/roadmap/dot-0-noi-auth-fe.md`, `dot-1-flashcard-ho-so.md`, `dot-2-bo-may-lam-bai.md`,
`dot-3-dashboard.md`, `dot-4-ai.md`, `dot-5-quan-tri.md`, `dot-6-ha-tang-phu.md`, `dot-7-song-ngu.md`.
Đợt 1 và 2 có thể song song. Đợt 4 chỉ cần Auth nên làm sớm được.

## Phiên làm việc (gói Pro)

Mỗi dòng là MỘT phiên: `/clear` (hoặc phiên mới), chọn model/effort, gõ prompt `Làm phiên <mã>.`
Bảng này thay cho mục "Bước tiếp theo" trong các file đợt. Làm theo thứ tự từ trên xuống.
Opus chỉ dùng khi Sonnet sai hai lần liên tiếp ở cùng một việc (ghi chú vào cột Trạng thái).

| Mã | Đợt | Việc (phạm vi đúng một phiên) | Model | Effort | Trạng thái |
|---|---|---|---|---|---|
| 0a | 0 | Sửa 25 lỗi lint có sẵn, lint/build sạch, đóng Đợt 0 | Sonnet | low | [x] |
| 1a | 1 | BE Topic + Flashcard (mục 1.1): service, controller, DTO, test | Sonnet | medium | [x] |
| 1b | 1 | BE Hồ sơ, Cài đặt, đổi mật khẩu, heartbeat (mục 1.2) | Sonnet | medium | [x] |
| 1c | 1 | FE: bỏ mock `topics,flashcard,profile,settings`, sửa kiểu theo DTO, hook heartbeat | Sonnet | low | [x] |
| 1z | 1 | Đóng đợt: chốt bảng kiểm tra, script thêm folder Postman, tick | Haiku | low | [x] |
| 2a | 2 | BE service chấm điểm chung + lưu `PracticeAttempt`/`Answer`, test | Sonnet | high | [x] |
| 2b | 2 | BE endpoint Nghe/Đọc/Kiểm tra, ẩn đáp án, lịch sử, `study_sessions`, test 403/lộ đáp án | Sonnet | medium | [x] |
| 2c | 2 | FE: bỏ mock `listening,reading,exams,attempts`, sửa kiểu | Sonnet | low | [x] |
| 2z | 2 | Đóng đợt | Haiku | low | [x] |
| 3a | 3 | BE seed lịch sử học + truy vấn Dashboard/Thống kê (múi giờ, biên tuần/tháng), test | Sonnet | high | [x] |
| 3b | 3 | FE: bỏ mock `dashboard,statistics`, trạng thái rỗng | Sonnet | low | [x] |
| 3z | 3 | Đóng đợt | Haiku | low | [x] |
| 4a | 4 | BE khung AI (interface + bản giả `@Profile("dev")`) + Luyện viết (4.1) | Sonnet | high | [x] |
| 4b | 4 | BE Chat AI (4.2) | Sonnet | medium | [x] |
| 4c | 4 | BE Luyện nói (4.3): nhận file, transcript, `FAILED` | Sonnet | medium | [x] |
| 4d | 4 | FE: bỏ mock `writing,chat,speaking`, trạng thái `GRADING`/lỗi | Sonnet | low | [x] |
| 4z | 4 | Đóng đợt (4.4 OpenAI thật để sau, phiên riêng khi có key: Sonnet/medium) | Haiku | low | [x] |
| 5a | 5 | BE quản trị nội dung (CRUD, `INACTIVE`, lỗi khoá ngoại), test 401/403 | Sonnet | medium | [x] |
| 5b | 5 | BE quản trị người dùng + Thông báo (`UserNotification`, đã đọc) + `/admin/dashboard` | Sonnet | medium | [x] |
| 5c | 5 | FE: bỏ mock `admin,notifications` | Sonnet | low | [x] |
| 5z | 5 | Đóng đợt | Haiku | low | [x] |
| 6a | 6 | Email nhắc học `@Scheduled`, bucket4j 429, Caffeine, job dọn dẹp, test | Sonnet | medium | [x] |
| 6z | 6 | Đóng đợt + `ARCHITECTURE.md` mục 4 | Haiku | low | [x] |
| 7a | 7 | Song ngữ: trang làm bài và kết quả | Sonnet | low | [x] |
| 7b | 7 | Song ngữ: Hồ sơ, Cài đặt, Thống kê, Thông báo, Chat | Sonnet | low | [x] |
| 7c | 7 | Song ngữ: Quản trị, trang lỗi, xác thực, `messageKey` từ BE; đóng đợt | Sonnet | low | [x] |
| 9a | Cuối | Xoá mock, cập nhật tài liệu, nợ nhỏ, chia chunk build | Sonnet | medium | [ ] |

Mẹo tiết kiệm hạn mức Pro: mỗi phiên một dòng; hết phiên thì `/clear`, không nối tiếp phiên cũ.
Nếu phiên dài quá (context > ~60%), dừng lại, tick phần đã xong và tách phần còn lại thành dòng mới (vd `2b-2`).
Lỗi do người dùng test thủ công phát hiện: phiên sửa riêng, prompt `Sửa lỗi phiên <mã>: <mô tả + log>`, Sonnet/low.

## Đợt cuối (không có file riêng)

- [ ] Xoá `src/mocks`, `mockAdapter.ts` và các biến `VITE_MOCK_MODULES`/`VITE_USE_MOCK` khi danh sách mock rỗng.
- [ ] Cập nhật `FEATURES_DONE.md` và `ARCHITECTURE.md` (hạ tầng: Caffeine, `@Scheduled`, bucket4j). Sửa `.agent/AGENTS.md` cho khớp nếu vẫn dùng Antigravity.
- [ ] Nợ nhỏ: thư mục lồng `english-learning/english-learning`; chia chunk build 1,77 MB (nạp động khu quản trị).

Kiểm tra hoàn thành (đợt cuối):
- [ ] `VITE_MOCK_MODULES` rỗng; chạy cả collection Postman (Dot 0 đến Dot 6) từ trên xuống, tất cả test xanh.
- [ ] Đăng nhập người học và admin trên giao diện, duyệt qua từng chức năng, không request nào trả 404 hay lỗi mạng trong tab Network.
- [ ] `./mvnw -q test` (cả bộ), `npm run lint`, `npm run build` sạch; xoá `src/mocks` xong build vẫn qua.
- [ ] Không còn tham chiếu Redis, RabbitMQ, Cloudinary trong `ARCHITECTURE.md` và `AGENTS.md`.
- Sau đó: hệ thống hoàn thiện theo phạm vi đã chốt. Việc mới (triển khai, test FE, OpenAI thật nếu chưa có key) là đợt riêng, hỏi lại người dùng.

## Quy ước chung cho mọi đợt

- Mỗi module BE: repository, service, controller, DTO, test đơn vị (Mockito), test tích hợp (`*Tests`, MockMvc). Sau đó kiểm bằng `curl` với profile `dev`, cuối cùng FE bỏ module đó khỏi `VITE_MOCK_MODULES`.
- Đường dẫn API giữ đúng những gì FE đang gọi trong `apps/frontend/src/mocks/handlers`. DTO thật khác mock thì sửa FE, không bẻ BE.
- Đổi schema chỉ thêm `V2`, `V3`, không sửa `V1`.
- Kiểm chứng: Claude chỉ bảo đảm biên dịch/lint sạch (`npm run lint`, `npm run build`, `./mvnw -q compile`). Người dùng tự test thủ công bằng Postman, SQL và giao diện theo mục "Kiểm tra hoàn thành" của đợt.
- Đợt xong khi: mọi checkbox trong file đợt được tick, test xanh, lint/build sạch. Khi đó cập nhật bảng này và `FEATURES_DONE.md` (tối đa 5 dòng).

## Rủi ro đã biết

1. FE chưa gửi heartbeat nên biểu đồ thời gian học sẽ trống nếu bỏ qua đợt 1.
2. Cookie refresh `SameSite=Lax`: chỉ chạy khi FE và BE cùng site (localhost khác cổng). Khác tên miền gốc phải đổi sang `SameSite=None; Secure`.
3. Test tích hợp chạy trên chính DB `en_learning`; nên có DB test riêng nếu được cấp quyền.
4. Không có test tự động FE: kiểm bằng trình duyệt, `tsc`, `eslint`, build.

## Kiểm tra bằng Postman (dùng chung mọi đợt)

1. Chạy BE (JDK 21): `cd apps/backend` rồi `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run`. Cổng 8080, đường dẫn gốc `/api`.
2. Postman: Import `.docs/postman/en-learning.postman_collection.json` và `.docs/postman/local.postman_environment.json`, chọn environment "EN-Learning local" (`baseUrl = http://localhost:8080/api`).
3. Cookie `refresh_token` (HttpOnly, Path `/api/auth`) do Postman tự lưu; giữ nguyên host `localhost`, đừng dùng `127.0.0.1`.
4. Chạy: chuột phải folder của đợt, chọn Run folder, giữ nguyên thứ tự. Mọi test phải xanh. Chạy cả collection để kiểm hồi quy các đợt cũ.
5. Access token sống 15 phút; hết hạn thì chạy lại request Login trong folder Dot 0 (`token`, `adminToken` tự lưu vào environment).
6. Bảng "Dự kiến" trong file đợt là bản mẫu; khi đợt xong, nhờ Claude thêm folder vào collection bằng script Node (nạp JSON, thêm folder, ghi lại). Không đọc cả file collection.
7. Câu SQL: chạy trong Workbench/DBeaver trên DB `en_learning`. Khoá chính là `BINARY(16)`: dùng `BIN_TO_UUID(id)` để xem, `UUID_TO_BIN('...')` để lọc.

## Đóng đợt (chung)

1. Chạy hết folder Postman của đợt và các đợt trước; tất cả xanh.
2. BE: `./mvnw -q test` (cả bộ, nhớ dọn DB theo ghi chú môi trường nếu cần). FE: `npm run lint` và `npm run build`.
3. Tick mọi checkbox trong file đợt; đổi `[ ]` thành `[x]` ở bảng trạng thái phía trên.
4. Thêm tối đa 5 dòng vào `.docs/FEATURES_DONE.md`.
5. Nếu còn dự kiến: nhờ Claude cập nhật bảng request và folder Postman theo DTO thật của đợt.
6. Tuỳ chọn: một lần `/code-review` mức medium cho cả đợt. Rồi dừng phiên, mở phiên mới cho đợt kế.

Prompt mẫu kết đợt (dán vào phiên đang làm đợt đó):

```text
Kết đợt N: chốt bảng "Kiểm tra hoàn thành" theo DTO thật, thêm folder "Dot N - ..." vào
.docs/postman bằng script Node (không đọc cả file collection), cập nhật ROADMAP và
FEATURES_DONE (tối đa 5 dòng). Không sửa mã nguồn khác.
```

## Khi kiểm tra thất bại (chung)

| Triệu chứng | Nguyên nhân thường gặp | Cách xử lý |
|---|---|---|
| Could not send request / ECONNREFUSED | BE chưa chạy, sai cổng, sai `baseUrl` | Chạy BE, kiểm environment đã chọn đúng |
| 401 dù đã login | Access token hết hạn (15 phút), `{{token}}` rỗng | Chạy lại Login trong Dot 0 |
| 401 ở Refresh | Cookie không gửi (dùng host khác localhost, Postman tắt cookie); token bị dùng lại quá 10 giây nên mọi phiên bị thu hồi | Login lại, giữ host `localhost` |
| 403 | Dùng token USER cho `/admin`, hoặc tài khoản bị khoá | Dùng `adminToken`; kiểm `status` người dùng |
| 400 VALIDATION | Body sai hoặc thiếu trường | Đọc `fieldErrorKeys` trong phản hồi |
| 404 | Thiếu tiền tố `/api`, hoặc BE chưa build lại code mới | Restart BE |
| 500 INTERNAL | Lỗi trong BE | Xem log BE, lấy dòng lỗi đầu và khoảng 15 dòng stack trace |
| Test đỏ kiểu "Cannot read properties of undefined" | DTO thật khác bảng dự kiến | Gửi tên request và body phản hồi thực tế |
| BE không khởi động (Flyway/Hibernate) | Lệch schema/entity, checksum V1 | Gửi dòng "Caused by" cuối cùng |

Gửi cho Claude đúng: tên request, status, body phản hồi (bỏ token), 10-15 dòng log lỗi BE. Đừng dán cả log hay cả collection.
