import { useCallback, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { FormInstance } from 'antd';

import { ApiError } from '../shared/api/types';
import { applyFieldErrors } from '../utils/formErrors';

/**
 * Dịch lỗi từ lớp API sang ngôn ngữ đang chọn.
 *
 * Lớp API nằm ngoài cây React nên không gọi được `t()`; nó chỉ mang theo
 * `messageKey` (xem `ApiError`). Hook này là chỗ duy nhất đổi khoá đó
 * thành câu chữ, nhờ vậy đổi VI/EN là mọi thông báo lỗi đổi theo.
 */
interface UseApiErrorResult {
  /** Câu thông báo đã dịch, kèm khoá dự phòng khi lỗi không rõ nguồn. */
  describe: (error: unknown, fallbackKey?: string) => string;
  /** Lỗi theo từng trường đã dịch, dùng cho Form của Ant Design. */
  fieldErrors: (error: unknown) => Record<string, string> | undefined;
  /** Đổ lỗi theo trường vào đúng ô nhập, đã dịch sẵn. */
  applyTo: <T>(form: FormInstance<T>, error: unknown) => void;
}

export function useApiError(): UseApiErrorResult {
  const { t } = useTranslation();

  const describe = useCallback(
    (error: unknown, fallbackKey = 'errors.unknown'): string => {
      if (error instanceof ApiError) {
        /* Backend thật có thể chưa trả khoá; khi đó giữ nguyên câu của
           server còn hơn hiện một thông báo chung chung. */
        if (error.messageKey) return t(error.messageKey);
        if (error.message) return error.message;
      }
      return t(fallbackKey);
    },
    [t],
  );

  const fieldErrors = useCallback(
    (error: unknown): Record<string, string> | undefined => {
      if (!(error instanceof ApiError)) return undefined;

      if (error.fieldErrorKeys) {
        return Object.fromEntries(
          Object.entries(error.fieldErrorKeys).map(([field, key]) => [
            field,
            t(key),
          ]),
        );
      }

      return error.fieldErrors;
    },
    [t],
  );

  const applyTo = useCallback(
    <T,>(form: FormInstance<T>, error: unknown): void => {
      applyFieldErrors(form, fieldErrors(error));
    },
    [fieldErrors],
  );

  return useMemo(
    () => ({ describe, fieldErrors, applyTo }),
    [describe, fieldErrors, applyTo],
  );
}

export default useApiError;
