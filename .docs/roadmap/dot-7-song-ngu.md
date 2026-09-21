# Đợt 7: Hoàn tất song ngữ VI/EN

Chỉ sửa FE, độc lập với BE, làm xen kẽ giữa các đợt được. Việc cơ học nhưng nhiều file: dùng mô hình rẻ, mỗi phiên 8-10 file.
Hạ tầng đã đủ: `vi.json`/`en.json`, kiểu `L10n`, hook `useLanguage`. Khoá dịch của các chỗ còn lại phần lớn đã có sẵn.

## Cách làm tiết kiệm
1. Một lệnh `grep` liệt kê file và dòng còn chuỗi có dấu tiếng Việt trong `src/pages` và `src/components` (loại trừ file locale). Lưu danh sách để chia phiên.
2. Mỗi phiên nhận danh sách file của mình. KHÔNG đọc cả `vi.json`/`en.json`; chỉ tìm khoá cần dùng, thiếu thì thêm vào cả hai file.
3. Thay chuỗi cứng bằng `t('...')`.

## Thứ tự
Đã xong: sidebar, header, footer, bộ lọc, sáu trang danh sách, Dashboard.
- [x] Trang làm bài và kết quả (Flashcard, Viết, Nghe, Đọc, Nói, Kiểm tra, `practice`)
- [x] Hồ sơ, Cài đặt, Thống kê, Thông báo
- [x] Chat
- [x] Quản trị (5 trang)
- [x] Trang lỗi (`system/`) và xác thực
- [x] Lỗi từ BE hiển thị qua `messageKey` và `fieldErrorKeys`

## Tiêu chí xong
- [x] Quét chuỗi có dấu tiếng Việt ngoài file locale trong `src/pages` và `src/components` không còn kết quả.
- [ ] Duyệt nhanh hai chế độ VI/EN và sáng/tối trên các trang đã sửa (đọc bằng `read_page`, chụp ảnh tối đa một lần cuối).
- [x] `npm run lint` và `npm run build` sạch.

## Kiểm tra hoàn thành

Đợt này chỉ sửa FE nên không có folder Postman mới. Cách kiểm chính là quét chuỗi và duyệt giao diện.

### A. Quét chuỗi tiếng Việt viết cứng
Nhờ Claude chạy Grep (ripgrep) trên `src/pages` và `src/components`, glob `*.tsx`, mẫu các ký tự có dấu:

```text
[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđĐ]
```
Kỳ vọng: không còn kết quả (không tính hai file locale). Chuỗi trong chú thích code không cần sửa, nhưng nên loại khỏi kết quả trước khi đếm.

### B. Khoá dịch đủ hai ngôn ngữ
So sánh bộ khoá của `vi.json` và `en.json`: không khoá nào chỉ có ở một bên. Nhờ Claude viết một lệnh Node ngắn để so sánh, không cần đọc cả hai file.

### C. Postman: lỗi từ BE hiển thị đúng ngôn ngữ
Postman chỉ kiểm phía BE trả `messageKey` và `fieldErrorKeys` (ví dụ request 11 và 10 trong Dot 0 đã có sẵn). Phần hiển thị kiểm trên giao diện ở mục D.

### D. Thử trên giao diện FE
1. Đổi sang EN: mở lần lượt các trang làm bài và kết quả (Flashcard, Viết, Nghe, Đọc, Nói, Kiểm tra), Hồ sơ, Cài đặt, Thống kê, Thông báo, Chat, 5 trang quản trị, trang lỗi 403/404/500, các trang xác thực. Không còn chữ tiếng Việt.
2. Đăng nhập sai mật khẩu khi đang ở EN: thông báo lỗi bằng tiếng Anh, không hiện khoá thô như `errors.invalidCredentials`.
3. Lặp lại nhanh với VI, và với giao diện tối.
4. `npm run lint` và `npm run build` sạch.

## Bước tiếp theo
1. Đóng đợt theo mục "Đóng đợt (chung)" trong `ROADMAP.md` (bỏ bước Postman).
2. Khi các đợt 1 đến 6 cũng đã xong, mở phiên mới cho đợt cuối:

```text
Đợt cuối: dọn dẹp.
Đọc .docs/ROADMAP.md (mục "Đợt cuối"). Xoá src/mocks và mockAdapter khi VITE_MOCK_MODULES rỗng,
cập nhật FEATURES_DONE.md, ARCHITECTURE.md, AGENTS.md cho khớp. Chạy lint, build và cả bộ test BE.
```
