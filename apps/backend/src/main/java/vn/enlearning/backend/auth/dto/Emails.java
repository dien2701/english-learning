package vn.enlearning.backend.auth.dto;

import java.util.Locale;

/** Chuẩn hoá và kiểm định dạng email, dùng chung cho các request của module auth. */
public final class Emails {

	/** Cùng biểu thức với {@code EMAIL_PATTERN} trong mock của frontend. */
	public static final String PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

	private Emails() {
	}

	/** Bỏ khoảng trắng hai đầu và về chữ thường; email không phân biệt hoa/thường. */
	public static String normalize(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	public static boolean isValid(String email) {
		return email != null && email.length() <= 255 && email.matches(PATTERN);
	}
}
