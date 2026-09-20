/**
 * Định dạng thuần số, không phụ thuộc ngôn ngữ.
 *
 * Thời lượng, ngày tháng và mốc thời gian tương đối đã chuyển sang hook
 * `useFormat` vì chúng cần chữ theo ngôn ngữ đang chọn ("4 giờ 45 phút" /
 * "4 hr 45 min"). Đừng thêm lại hàm ghép chuỗi tiếng Việt vào đây.
 */

/** Điểm số một chữ số thập phân: 8 → "8.0". */
export function formatScore(score: number): string {
  return score.toFixed(1);
}

/** Thêm dấu cộng cho số dương để thấy rõ chiều tăng giảm. */
export function formatSignedPercent(value: number): string {
  return `${value > 0 ? '+' : ''}${value}%`;
}
