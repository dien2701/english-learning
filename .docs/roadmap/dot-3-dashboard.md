# Đợt 3: Dashboard, Thống kê

Phụ thuộc: đợt 1 và 2 (cần dữ liệu tiến độ, lượt làm bài, `study_sessions`). Chia phiên: 3.1 BE + seed lịch sử; 3.2 nối FE.

## Endpoint
- `GET /dashboard/summary` (bài đang học dở, danh sách bài đã tham gia lọc theo trạng thái)
- `GET /dashboard/study-time?period=week|month` (kèm so sánh kỳ trước)
- `GET /statistics/overview`
- Số liệu tổng trong `GET /profile`: thời gian học, bài đã hoàn thành, từ đã thuộc.

## Lưu ý
- Gom theo ngày theo `UserSetting.timeZone` (mặc định Asia/Ho_Chi_Minh), không theo UTC. Thời gian của một phiên tính vào ngày `startedAt`.
- Phần trăm so với kỳ trước chạy lại cùng truy vấn cho khoảng liền trước; xử lý chia cho 0.
- Viết các truy vấn tổng hợp thành phương thức repository riêng để test riêng. Thêm index nếu chậm.

## Việc làm
- [x] Seed dev thêm lịch sử học: tiến độ thẻ, lượt làm bài, `study_sessions` (`HistorySeedService`, chạy cho `hocvien@enlearning.vn` khi chưa có phiên học nào, cả khi DB đã có người dùng).
- [x] Truy vấn và service cho ba endpoint trên, test có dữ liệu ở biên tuần/tháng và lệch múi giờ (`StudyTimeServiceTests`, `DashboardApiTests`; `summary` nhận thêm `?status=IN_PROGRESS|COMPLETED`, tuần T2-CN, tháng 4 nhóm 1-7/8-14/15-21/22-hết).
- [x] FE: bỏ `dashboard`, `statistics` và phần Hồ sơ còn lại khỏi `VITE_MOCK_MODULES`.

## Tiêu chí xong
- [ ] Biểu đồ Tuần/Tháng khớp tổng `activeSeconds` trong DB theo múi giờ người dùng.
- [ ] Người dùng chưa học gì thấy trạng thái rỗng đúng, không lỗi.
- [ ] Test xanh, lint/build sạch.

## Kiểm tra hoàn thành

Folder Postman "Dot 3 - Dashboard, Thống kê" (13 request, chạy từ trên xuống, dùng `token` và `adminToken` của Dot 0) khớp DTO thật. Cách chạy chung: mục "Kiểm tra bằng Postman" trong `ROADMAP.md`.

### A. Postman: đường thành công
| # | Request | Kỳ vọng |
|---|---|---|
| 01 | `GET /dashboard/summary` | 200; `greetingName`, `continueLearning` (object hoặc null), `attendedLessons` ≤ 20 bài, mỗi bài có `status` IN_PROGRESS/COMPLETED, `progress` 0-100, `detailPath`, `score` (vắng với bộ thẻ) |
| 02-03 | `GET /dashboard/summary?status=COMPLETED` / `IN_PROGRESS` | 200, danh sách chỉ còn đúng trạng thái đó |
| 04 | `GET /dashboard/study-time?period=WEEK` | 200, 7 cột (T2-CN) `label{vi,en}`, `minutes`, `previousMinutes`; `totalMinutes`, `previousTotalMinutes`, `changePercent` |
| 05 | `GET /dashboard/study-time?period=month` | như trên, 4 cột "Tuần 1-4", `period` = MONTH |
| 06 | `GET /statistics/overview` | 200; `weekPoints` 7, `monthPoints` 4, `skills` = LISTENING, READING, EXAM (điểm 0-10), `recentAttempts` ≤ 8 |
| 07 | `GET /profile` | có `totalMinutes`, `completedLessons`, `masteredWords` |

### B. Ca lỗi và bảo mật
- `period` sai (08) hoặc `status` sai (09): 400 kèm `fieldErrorKeys`, không phải 500.
- Người dùng mới chưa học gì: 200 với dữ liệu rỗng, không có `NaN`, không lỗi chia cho 0 ở phần trăm.
- Không có token (10, 11): 401. Bằng `adminToken` (12, 13): rỗng, toàn 0, `changePercent` 0, không NaN. Mỗi người chỉ thấy số liệu của chính mình (đăng nhập hai người, so sánh).

### C. SQL kiểm tra dữ liệu
```sql
-- Tổng giây học theo ngày, múi giờ +07:00 (Asia/Ho_Chi_Minh); phải khớp biểu đồ tuần
SELECT DATE(CONVERT_TZ(s.started_at, '+00:00', '+07:00')) AS d, SUM(s.active_seconds) AS secs
FROM study_sessions s JOIN users u ON u.id = s.user_id
WHERE u.email = 'hocvien@enlearning.vn' AND s.started_at >= NOW() - INTERVAL 7 DAY
GROUP BY d ORDER BY d;
```
Kiểm biên: một phiên bắt đầu lúc 23:30 giờ Việt Nam phải tính vào đúng ngày đó, không phải ngày UTC.

### D. Thử trên giao diện FE
1. Bỏ `dashboard`, `statistics` khỏi `VITE_MOCK_MODULES`.
2. Biểu đồ Tuần/Tháng khớp SQL ở mục C; banner "Tiếp tục học" mở đúng bài; lọc danh sách theo trạng thái chạy đúng.
3. Đăng ký người mới: Dashboard hiện trạng thái rỗng đúng, không lỗi.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md`.
2. Mở phiên mới cho Đợt 4 (khung AI, dùng mô hình mạnh cho bước này):

```text
Đợt 4, bước 4.1: khung AI chung + Luyện viết BE (bản giả).
Đọc .docs/ROADMAP.md và .docs/roadmap/dot-4-ai.md. Viết interface WritingGrader, FakeWritingGrader
(@Profile dev), chấm bất đồng bộ với trạng thái DRAFT/GRADING/GRADED/NEEDS_RETRY, các endpoint
writing, test. Chưa gọi OpenAI thật. Chạy test hẹp. Không đụng FE.
```
