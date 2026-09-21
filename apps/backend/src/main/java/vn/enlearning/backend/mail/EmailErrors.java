package vn.enlearning.backend.mail;

import org.springframework.core.NestedExceptionUtils;

/** Rút lỗi gửi email thành một dòng đủ ngắn để lưu vào {@code email_logs.error_message}. */
public final class EmailErrors {

	private static final int MAX_ERROR_LENGTH = 500;

	private EmailErrors() {
	}

	/**
	 * Gồm cả nguyên nhân gốc (ví dụ "535 Username and Password not accepted") vì câu bọc ngoài một mình
	 * không đủ để biết vì sao gửi hỏng. Thông báo lỗi SMTP không chứa mật khẩu.
	 */
	public static String describe(RuntimeException e) {
		Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
		String text = root == e ? e.getMessage()
				: e.getMessage() + " | " + root.getClass().getSimpleName() + ": " + root.getMessage();
		if (text == null) {
			return e.getClass().getSimpleName();
		}
		String oneLine = text.replaceAll("\\s+", " ").trim();
		return oneLine.length() <= MAX_ERROR_LENGTH ? oneLine : oneLine.substring(0, MAX_ERROR_LENGTH);
	}
}
