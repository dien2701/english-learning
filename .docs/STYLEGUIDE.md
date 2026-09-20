# LUẬT THIẾT KẾ: EN-LEARNING

Chủ đề: **LinguaMerse** — xanh rừng đậm + xanh lá tươi, nền mint rất nhạt,
card trắng bo tròn mềm.

Nguồn tham chiếu duy nhất của màu là `apps/frontend/src/styles/tokens.css`.
Hai nơi phải giữ khớp với file đó: `tailwind.config.js` và `src/theme/palette.ts`.
Đổi màu thì sửa cả ba.

---

## 1. Typography

Font chủ đạo: **Plus Jakarta Sans** (có đủ dấu tiếng Việt), dự phòng Inter.

| Vai trò | Cỡ | Đậm | Lớp Tailwind |
|---|---|---|---|
| Tiêu đề trang | 28px | 700 | `text-page-title` |
| Tiêu đề mục | 20px | 600 | `text-section` |
| Tiêu đề card | 18px | 600 | `text-card-title` |
| Text thường | 14px | 400 | `text-body` |
| Text phụ | 12px | 500 | `text-caption` |

---

## 2. Bảng màu

| Vai trò | Mã | Ghi chú |
|---|---|---|
| Sidebar | `#173F2E` | xanh rừng đậm |
| Sidebar mục đang chọn | `#2D6A4F` | chữ trắng, 6.39:1 |
| Hành động chính | `#15803D` | nút, link, mục đang chọn |
| Nhấn | `#22C55E` | progress, mảng trang trí |
| Nền trang | `#F4FAF6` | phủ thêm lớp mint nhạt ở đỉnh |
| Nền card | `#FFFFFF` | |
| Viền | `#DDE8E1` | |
| Chữ chính | `#14261C` | |
| Chữ phụ | `#5A6B62` | 5.65:1 trên nền trắng |

**Không dùng xanh lam ở bất cứ đâu.** Trạng thái thông tin dùng xám xanh
`#3D4F45`. Chip trình độ dùng ba tông khác hẳn nhau để phân biệt được bằng
cả chữ lẫn màu, kể cả khi in đen trắng:

| Mức | Nền | Chữ |
|---|---|---|
| Cơ bản | `#DCFCE7` mint | `#166534` |
| Trung cấp | `#FEF3C7` hổ phách | `#92400E` |
| Nâng cao | `#EDE9FE` tím | `#5B21B6` |

### Quy tắc tương phản

**Mảng xanh lục nào có chữ hoặc icon đè lên thì luôn để chữ màu trắng, và
nền phải là `#15803D` trở xuống.** Chữ trắng trên xanh lá rực `#22C55E` chỉ
đạt 3.3:1, dưới chuẩn WCAG AA.

- Nút, logo, mục đang chọn: nền `#15803D`, chữ trắng — 5.01:1 ✓
- Mục sidebar đang chọn: nền `#2D6A4F`, chữ trắng — 6.39:1 ✓
- `#22C55E` chỉ dùng cho mảng **không có chữ**: thanh tiến độ, nét biểu đồ,
  chấm trang trí.

Chữ trong sidebar: mục điều hướng `#DCEBE3` (9.7:1), nhãn nhóm và mô tả
`#A9C6B6` (6.4:1).

### Màu biểu đồ

Thứ tự màu cố định, **không xoay vòng**. Đã chạy qua bộ kiểm tra của skill
`dataviz` ở cả hai chế độ và đạt toàn bộ: dải sáng, ngưỡng chroma, tách màu
cho người mù màu, tương phản với nền.

| Chuỗi | Sáng | Tối |
|---|---|---|
| 1 — kỳ hiện tại | `#15803D` | `#16A34A` |
| 2 — kỳ trước | `#8B5CF6` | `#8B5CF6` |

Đổi màu biểu đồ thì phải chạy lại `scripts/validate_palette.js`, không ước lượng bằng mắt.

---

## 3. Hình khối

- Card bo **16px**, viền 1px `#DDE8E1`, bóng nhẹ.
- Nút chính bo **pill** (999px).
- Ô nhập và khối nhỏ bo **12px**.

### Nút

Mọi nút đi qua `src/components/ui/Button.tsx` (`Button`, `ButtonLink`,
`IconButton`). Không viết `<button className="...">` mới: trước đây mỗi trang
tự chế một tổ hợp class nên cùng một loại hành động lại khác cỡ và khác
trạng thái hover.

| Biến thể | Dùng khi | Hình thức |
|---|---|---|
| `primary` | hành động chính, mỗi màn hình một cái | nền `action`, chữ trắng, bóng |
| `secondary` | hành động ngang hàng, cần thấy viền | nền card, viền `hairline-strong` |
| `subtle` | hành động phụ | nền `surface-muted` |
| `danger` | mở hộp xác nhận xoá hoặc khoá | nền `danger-bg`, chữ `danger-fg` |
| `link` | thao tác nhỏ nằm trong dòng chữ | chỉ có chữ màu nhấn |

Ba cỡ: `sm` 40px, `md` 44px (mặc định), `lg` 52px. Nút chỉ có icon phải dùng
`IconButton` và truyền `label` — nút không chữ mà thiếu nhãn thì trình đọc
màn hình chỉ đọc được tên icon. Nút đỏ đặc chỉ xuất hiện trong hộp xác nhận
của Ant Design, không dùng cho hành động trên từng dòng bảng.

### Ô nhập

Ô nhập nền mint rất nhạt (`--field-bg`) để tách khỏi card trắng, viền 1px
`--field-border` đậm hơn hairline. Khi focus: nền chuyển trắng, viền dày
**2px** màu `action` kèm quầng sáng mờ; khi lỗi thì viền và quầng đổi sang
`danger`. Token nằm ở nhóm `--field-*` trong `tokens.css`, ánh xạ sang Ant
Design qua `palettes[mode].field*`, phần viền 2px nằm ở `@layer components`
của `index.css` vì AntD không có token cho độ dày viền.

---

## 4. Hệ token và chế độ sáng/tối

Màu khai báo ở `tokens.css` dưới dạng ba số kênh RGB, Tailwind bọc lại thành
`rgb(var(--x-rgb) / <alpha-value>)`. Nhờ vậy đổi chế độ chỉ cần đổi biến, và
các biến thể độ mờ như `bg-surface/50` vẫn chạy.

Token chia làm hai nhóm:

| Nhóm | Ví dụ | Đổi theo chế độ? |
|---|---|---|
| Thang thương hiệu | `brand-50`…`brand-950` | **Không** |
| Theo vai trò | `action`, `accent`, `surface`, `ink`, `hairline` | **Có** |

Quy tắc: **chữ không bao giờ dùng trực tiếp thang `brand`.** Chữ nhấn dùng
`text-accent`, nền nút dùng `bg-action` + `text-white`, chip dùng
`bg-accent-soft text-accent`. Thang `brand` chỉ cho mảng trang trí
(`bg-brand-500` cho thanh tiến độ, nét biểu đồ).

Ant Design không đọc biến CSS, nên `ThemeContext` dựng lại `buildThemeConfig(mode)`
mỗi khi đổi chế độ. Thiếu bước này thì Table, Select, Modal sẽ kẹt màu sáng
trên nền tối.

---

## 5. Đa ngôn ngữ

Hai loại chuỗi, xử lý khác nhau:

| Loại | Ví dụ | Cách làm |
|---|---|---|
| Nhãn giao diện | nút, tiêu đề trang, thông báo | `t('key')` của i18next |
| Nội dung | tên bộ từ, tiêu đề bài học, mô tả | kiểu `L10n = { vi, en }`, lấy qua `L()` của `useLanguage` |
| Bài tập | câu hỏi, đoạn đọc, câu luyện nói, từ vựng | **luôn tiếng Anh** ở cả hai chế độ |

Nghĩa của thẻ từ là `L10n`: chế độ VI hiện nghĩa tiếng Việt, chế độ EN hiện
định nghĩa bằng tiếng Anh.

**Thông báo lỗi từ API cũng phải đổi ngôn ngữ.** Lớp API nằm ngoài cây React
nên không gọi được `t()`; vì vậy server (và mock) chỉ trả **khoá dịch** trong
`messageKey` / `fieldErrorKeys`, còn tầng giao diện dịch bằng
`useApiError()`:

```tsx
const { describe, applyTo } = useApiError();
// ...
applyTo(form, error);                       // đổ lỗi vào đúng ô nhập
message.error(describe(error, 'auth.loginFailed'));
```

Trong mock, `fail(400, 'errors.emailTaken', ...)` nhận khoá chứ không nhận
câu tiếng Việt.

**Không ghép chuỗi ngày giờ bằng tay.** `useFormat()` cho `date`,
`duration`, `relativeTime`; `useLabels()` cho nhãn của các kiểu enum (trình
độ, kỹ năng, trạng thái bài viết…). `utils/format.ts` chỉ còn phần thuần số.

Nút VI/EN dùng chung `LanguageSwitch`, có mặt ở header khu người học, header
khu quản trị và cả nhóm màn hình xác thực — người chưa đăng nhập cũng phải
đổi được ngôn ngữ.

---

## 6. Thứ tự lớp CSS

Tailwind v4 sinh utility bên trong `@layer utilities`, còn CSS của Ant Design
vốn nằm **ngoài mọi lớp** — mà CSS không phân lớp luôn thắng CSS có lớp, bất
kể specificity. Nếu không xử lý, các quy tắc theo thẻ của AntD (`a`, `button`)
sẽ đè lên mọi utility màu chữ và màu nền. Dự án xử lý ở hai chỗ:

1. `src/index.css` khai báo `@layer theme, base, antd, components, utilities;`
   rồi nạp `@import 'antd/dist/reset.css' layer(antd);`

   Vị trí của lớp `antd` phải nằm **giữa `base` và `components`**. Đặt nó
   dưới cùng thì phần reset của Tailwind (`@layer base`) sẽ thắng, mà reset
   đó có `button { padding: 0; border: 0; background: transparent }` — hậu
   quả là mọi component AntD dựng bằng thẻ `button` (Switch, nút trong
   Modal, Pagination) mất sạch nền, viền và phần đệm.
2. `App.tsx` bọc toàn bộ cây trong `<StyleProvider layer>` của
   `@ant-design/cssinjs` để phần CSS-in-JS sinh lúc chạy cũng vào lớp đó.

Ngoài ra `colorLink` phải khai báo rõ trong `themeConfig`: AntD mặc định suy
nó từ `colorInfo`, mà `colorInfo` của dự án là xám xanh nên mọi thẻ `a` sẽ bị
xám nếu bỏ quên.

---

## 7. Ràng buộc trải nghiệm

- Tailwind dựng layout và khối hiển thị. Ant Design lo Table, Form, Modal,
  Select, DatePicker, Upload, message — chủ yếu ở khu Admin.
- Layout desktop: sidebar trái cố định 260px (thu còn 80px), header dính phía
  trên cột nội dung, lưới 12 cột.
- Mỗi màn hình chỉ một nút chính màu primary.
- Form validate và hiện lỗi ngay cạnh ô nhập; lỗi từ server đổ về đúng trường
  qua `ApiError.fieldErrors`.
- Mọi khối gọi API phải có đủ ba trạng thái Loading, Empty, Error; trạng thái
  lỗi luôn kèm nút thử lại.
- Vùng chạm tối thiểu **44×44px**.
- Không xoá vòng focus mà không thay bằng chỉ báo khác.
- Tôn trọng `prefers-reduced-motion`.
- Responsive kiểm ở 375px, 768px, 1024px, 1440px; không được cuộn ngang.

---

## 8. Cần tránh

- Không dùng emoji thay icon — dùng Material Symbols.
- Không hoạt hình trẻ con, không glassmorphism.
- Không nhồi quá nhiều card hoặc biểu đồ lên Dashboard. Màn hình này chỉ có
  ba khối: **một bài đang học dở**, biểu đồ thời gian học, và danh sách bài đã
  tham gia. Không có ô chỉ số tổng hợp, không có mục gợi ý.
- Không dùng biểu đồ hai trục tung.
- Không dùng riêng màu sắc để truyền đạt ý nghĩa — luôn kèm chữ hoặc icon.
- Không đặt chữ lên nền `#22C55E`.
- Không dùng bất kỳ sắc xanh lam nào.
- Không dùng thang `brand` cho chữ — chữ sẽ không đổi được theo chế độ tối.
- Không viết cứng chuỗi tiếng Việt trong component.
