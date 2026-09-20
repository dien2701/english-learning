import type { FormInstance } from 'antd';

type FieldsArg<T> = Parameters<FormInstance<T>['setFields']>[0];

/**
 * Đổ lỗi theo từng trường vào đúng ô nhập của Ant Design.
 *
 * Tên trường đến từ backend nên chỉ là chuỗi, trong khi Form đã được định
 * kiểu chặt — phải ép kiểu ở đúng một chỗ này thay vì rải khắp các trang.
 *
 * Nhận vào bản đã dịch chứ không nhận thẳng `ApiError`: thông báo lỗi phải
 * đổi theo ngôn ngữ, mà việc dịch cần `t()` nên chỉ làm được trong cây
 * React. Các trang dùng `useApiError().applyTo(form, error)` thay vì gọi
 * trực tiếp hàm này.
 */
export function applyFieldErrors<T>(
  form: FormInstance<T>,
  fieldErrors: Record<string, string> | undefined,
): void {
  if (!fieldErrors) return;

  const fields = Object.entries(fieldErrors).map(([name, message]) => ({
    name,
    errors: [message],
  }));

  form.setFields(fields as FieldsArg<T>);
}
