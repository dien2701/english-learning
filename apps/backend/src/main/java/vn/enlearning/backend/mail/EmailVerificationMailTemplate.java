package vn.enlearning.backend.mail;

import vn.enlearning.backend.entity.enums.UiLanguage;

/** Nội dung email mã xác minh khi đăng ký. */
final class EmailVerificationMailTemplate {

	private EmailVerificationMailTemplate() {
	}

	static EmailMessage build(String to, String code, long minutes, UiLanguage language) {
		if (language == UiLanguage.EN) {
			String warning = "The code is valid for %d minutes and can be used once. Do not share it with anyone."
					.formatted(minutes);
			String footer = "If you did not sign up for En-Learning, ignore this email.";
			return new EmailMessage(to, "Your En-Learning verification code",
					"Hello,\n\nYour verification code is: %s\n\n%s\n\n%s\n".formatted(code, warning, footer),
					PasswordResetMailTemplate.layout("Hello,", "Your verification code is:", code, warning, footer));
		}
		String warning = "Mã có hiệu lực trong %d phút và chỉ dùng được một lần. Không chia sẻ mã này với bất kỳ ai."
				.formatted(minutes);
		String footer = "Nếu bạn không đăng ký En-Learning, hãy bỏ qua email này.";
		return new EmailMessage(to, "Mã xác minh email En-Learning",
				"Xin chào,\n\nMã xác minh email của bạn là: %s\n\n%s\n\n%s\n".formatted(code, warning, footer),
				PasswordResetMailTemplate.layout("Xin chào,", "Mã xác minh email của bạn là:", code, warning, footer));
	}
}
