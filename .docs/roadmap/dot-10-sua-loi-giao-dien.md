# Đợt 10: Sửa lỗi giao diện sau khi nộp bài

Phụ thuộc: không. Chỉ FE (trừ khi 10.1 phát hiện BE lưu sai). Một phiên: 10a.

## 10.1 Thời gian làm bài luôn hiện 0
Nghi vấn (chưa xác minh): `components/practice/ResultSummary.tsx` hiện `Math.round(durationSeconds / 60)` nên bài dưới 30 giây ra 0 phút; và/hoặc trang làm bài gửi `durationSeconds` = 0 (xem `services/contentService.ts` hàm submit Nghe/Đọc/Kiểm tra và nơi gọi).
- [x] Trang làm bài đếm giây thật từ lúc mở bài tới lúc nộp (dùng `Date.now()` lúc bắt đầu, không dùng state của đồng hồ đếm ngược), gửi `durationSeconds` > 0.
- [x] Kết quả hiển thị dạng `mm:ss` (hoặc `X phút Y giây`), không làm tròn về phút. Áp dụng cho Nghe, Đọc, Kiểm tra, Nói (và lịch sử nếu có cột thời gian).

## 10.2 Chữ menu sidebar đổi màu sau khi nộp bài
- [x] Tìm nguyên nhân (thường là class/`:visited`/`selectedKeys` của Menu antd hoặc style trang kết quả ghi đè token). Cố định chữ và icon menu màu trắng `#fff` ở mọi trạng thái (thường, hover, đang chọn, đã truy cập); mục đang chọn phân biệt bằng nền, không bằng màu chữ.
- [x] Đặt style trong component sidebar (token theo `STYLEGUIDE.md`), không dùng `!important` toàn cục.

## 10.3 Xác nhận khi rời bài đang làm
- [x] Hook dùng chung `useLeaveGuard(active: boolean)`: `useBlocker` của React Router + `beforeunload`. Khi đang làm bài (đã bắt đầu, chưa nộp) mà bấm logo, mục sidebar, nút quay lại: hiện `Modal.confirm` ("Bạn đang làm bài, rời trang sẽ mất bài làm?"). Đồng ý thì đi, huỷ thì ở lại.
- [x] Logo EN-Learning là `Link` tới trang Tổng quan (đi qua blocker, không `window.location`).
- [x] Gắn cho Nghe, Đọc, Kiểm tra, Viết (khi có nội dung chưa nộp), Nói (khi có bản thu chưa nộp). Sau khi nộp xong thì tắt guard, không hỏi nữa.
- [x] Chuỗi qua `t('...')` VI/EN.

## Tiêu chí xong
- [x] `npm run lint`, `npm run build` sạch.

## Kiểm tra hoàn thành (giao diện)
| # | Thao tác | Kỳ vọng |
|---|---|---|
| 1 | Làm bài Nghe khoảng 40 giây rồi nộp | Kết quả hiện khoảng `00:40`, không phải 0 |
| 2 | Nộp một bài bất kỳ, nhìn sidebar | Chữ và icon menu vẫn trắng; hover và mục đang chọn cũng trắng |
| 3 | Đang làm bài Đọc, bấm logo | Hỏi xác nhận; Huỷ thì ở lại, câu đã chọn còn nguyên; Đồng ý thì về Tổng quan |
| 4 | Đang làm bài, bấm F5 | Trình duyệt hỏi rời trang |
| 5 | Ở trang kết quả (đã nộp), bấm logo | Đi thẳng, không hỏi |
