import { useCallback, useMemo } from 'react';
import { useTranslation } from 'react-i18next';

/**
 * Định dạng thời gian và ngày tháng theo ngôn ngữ đang chọn.
 *
 * `utils/format.ts` trước đây ghép thẳng chuỗi tiếng Việt ("4 giờ 45 phút",
 * "Hôm qua"), nên chọn EN thì mọi mốc thời gian vẫn là tiếng Việt. Các hàm
 * ở đây lấy chữ từ bộ khoá `time.*` và chọn locale cho `toLocaleDateString`
 * theo ngôn ngữ hiện tại.
 *
 * Phần thuần số — điểm, phần trăm — vẫn nằm ở `utils/format.ts` vì không
 * phụ thuộc ngôn ngữ.
 */
interface UseFormatResult {
  /** 285 → "4 giờ 45 phút" / "4 hr 45 min". */
  duration: (totalMinutes: number) => string;
  /** "5 phút trước", "Hôm qua", hoặc ngày cụ thể nếu quá một tuần. */
  relativeTime: (iso: string) => string;
  /** Ngày dạng ngắn, theo quy ước của ngôn ngữ đang chọn. */
  date: (iso: string) => string;
}

export function useFormat(): UseFormatResult {
  const { t, i18n } = useTranslation();
  const locale = i18n.language === 'en' ? 'en-GB' : 'vi-VN';

  const date = useCallback(
    (iso: string): string => {
      const value = new Date(iso);
      if (Number.isNaN(value.getTime())) return '';
      return value.toLocaleDateString(locale, {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      });
    },
    [locale],
  );

  const duration = useCallback(
    (totalMinutes: number): string => {
      const minutes = Math.max(0, Math.round(totalMinutes));
      if (minutes < 60) return t('time.minutes', { count: minutes });

      const hours = Math.floor(minutes / 60);
      const rest = minutes % 60;

      return rest === 0
        ? t('time.hours', { count: hours })
        : t('time.hoursMinutes', { hours, minutes: rest });
    },
    [t],
  );

  const relativeTime = useCallback(
    (iso: string): string => {
      const then = new Date(iso).getTime();
      if (Number.isNaN(then)) return '';

      const diffMinutes = Math.round((Date.now() - then) / 60_000);

      if (diffMinutes < 1) return t('time.justNow');
      if (diffMinutes < 60) return t('time.minutesAgo', { count: diffMinutes });

      const diffHours = Math.round(diffMinutes / 60);
      if (diffHours < 24) return t('time.hoursAgo', { count: diffHours });

      const diffDays = Math.round(diffHours / 24);
      if (diffDays === 1) return t('time.yesterday');
      if (diffDays < 7) return t('time.daysAgo', { count: diffDays });

      // Quá một tuần thì mốc tương đối hết hữu ích, đổi sang ngày cụ thể.
      return date(iso);
    },
    [t, date],
  );

  return useMemo(
    () => ({ duration, relativeTime, date }),
    [duration, relativeTime, date],
  );
}

export default useFormat;
