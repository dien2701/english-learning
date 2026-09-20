import { useCallback, useMemo } from 'react';
import { useTranslation } from 'react-i18next';

import { pick, toLanguage, type L10n, type Language } from '../types/l10n';

interface UseLanguageResult {
  language: Language;
  /** Lấy bản dịch của một chuỗi nội dung song ngữ. */
  L: (value: L10n) => string;
  setLanguage: (language: Language) => void;
}

/**
 * Ngôn ngữ đang chọn, kèm hàm lấy bản dịch cho nội dung song ngữ.
 *
 * Nhãn giao diện dùng `t()` như bình thường; hàm `L` ở đây dành cho dữ
 * liệu đến từ kho nội dung (tên bộ từ, tiêu đề bài học, mô tả…) — thứ
 * mà i18next không quản lý.
 */
export function useLanguage(): UseLanguageResult {
  const { i18n } = useTranslation();

  const language = toLanguage(i18n.language);

  const L = useCallback((value: L10n) => pick(value, language), [language]);

  const setLanguage = useCallback(
    (next: Language) => {
      void i18n.changeLanguage(next);
    },
    [i18n],
  );

  return useMemo(
    () => ({ language, L, setLanguage }),
    [language, L, setLanguage],
  );
}

export default useLanguage;
