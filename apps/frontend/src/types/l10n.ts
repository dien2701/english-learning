/**
 * Chuỗi nội dung có hai ngôn ngữ.
 *
 * Dùng cho tên và mô tả của bộ từ, bài học, đề bài — những thứ đến từ
 * kho nội dung chứ không phải nhãn giao diện. Nhãn giao diện vẫn đi qua
 * i18next như bình thường.
 *
 * Riêng phần bài tập (câu hỏi, đoạn đọc, câu luyện nói, từ vựng) luôn
 * bằng tiếng Anh ở cả hai chế độ, nên không dùng kiểu này.
 */
export interface L10n {
  vi: string;
  en: string;
}

export type Language = 'vi' | 'en';

/** Chuẩn hoá mã ngôn ngữ của i18next về đúng hai giá trị được hỗ trợ. */
export function toLanguage(code: string | undefined): Language {
  return code?.toLowerCase().startsWith('en') ? 'en' : 'vi';
}

/** Lấy bản dịch theo ngôn ngữ, lùi về tiếng Việt nếu thiếu bản tiếng Anh. */
export function pick(value: L10n, language: Language): string {
  return language === 'en' ? value.en || value.vi : value.vi;
}
