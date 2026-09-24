package vn.enlearning.backend.auth.validation;

import java.nio.charset.StandardCharsets;

/** Quy tắc mật khẩu dùng chung cho đăng ký và đặt lại mật khẩu. */
public final class PasswordPolicy {

	/** Khớp ô nhập ở frontend ({@code auth.validation.passwordMin}). */
	public static final int MIN_LENGTH = 8;

	/** BCrypt chỉ dùng 72 byte đầu; dài hơn thì Spring Security từ chối chứ không cắt lặng lẽ. */
	public static final int MAX_BYTES = 72;

	private PasswordPolicy() {
	}

	/** Trả về khoá dịch của lỗi, hoặc {@code null} nếu mật khẩu hợp lệ. */
	public static String violationKey(String password) {
		if (password.length() < MIN_LENGTH || !hasRequiredCharacters(password)) {
			return "auth.validation.passwordMin";
		}
		if (exceedsBcryptLimit(password)) {
			return "errors.newPasswordInvalid";
		}
		return null;
	}

	/** Phải có ít nhất một chữ cái, một chữ số và một ký tự đặc biệt (không phải chữ, số hay khoảng trắng). */
	public static boolean hasRequiredCharacters(String password) {
		return password.codePoints().anyMatch(Character::isLetter)
				&& password.codePoints().anyMatch(Character::isDigit)
				&& password.codePoints().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c));
	}

	public static boolean exceedsBcryptLimit(String password) {
		return password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
	}
}
