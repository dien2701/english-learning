package vn.enlearning.backend.mail;

import org.springframework.web.util.HtmlUtils;

import vn.enlearning.backend.entity.enums.UiLanguage;

/** Nội dung email mã đặt lại mật khẩu, theo ngôn ngữ người dùng đã chọn. */
final class PasswordResetMailTemplate {

	private PasswordResetMailTemplate() {
	}

	static EmailMessage build(String to, String fullName, String code, long validMinutes, UiLanguage language) {
		return language == UiLanguage.EN
				? english(to, fullName, code, validMinutes)
				: vietnamese(to, fullName, code, validMinutes);
	}

	private static EmailMessage vietnamese(String to, String fullName, String code, long minutes) {
		String subject = "Mã đặt lại mật khẩu En-Learning";
		String text = """
				Xin chào %s,

				Mã đặt lại mật khẩu của bạn là: %s

				Mã có hiệu lực trong %d phút và chỉ dùng được một lần. Không chia sẻ mã này với bất kỳ ai, \
				kể cả người tự nhận là En-Learning.

				Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này; mật khẩu của bạn vẫn giữ nguyên.
				""".formatted(fullName, code, minutes);
		String html = layout("Xin chào %s,".formatted(HtmlUtils.htmlEscape(fullName)),
				"Mã đặt lại mật khẩu của bạn là:", code,
				"Mã có hiệu lực trong %d phút và chỉ dùng được một lần. Không chia sẻ mã này với bất kỳ ai, "
						.formatted(minutes) + "kể cả người tự nhận là En-Learning.",
				"Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này; mật khẩu của bạn vẫn giữ nguyên.");
		return new EmailMessage(to, subject, text, html);
	}

	private static EmailMessage english(String to, String fullName, String code, long minutes) {
		String subject = "Your En-Learning password reset code";
		String text = """
				Hello %s,

				Your password reset code is: %s

				The code is valid for %d minutes and can be used once. Do not share it with anyone, \
				including people claiming to be En-Learning.

				If you did not request a password reset, ignore this email; your password stays unchanged.
				""".formatted(fullName, code, minutes);
		String html = layout("Hello %s,".formatted(HtmlUtils.htmlEscape(fullName)),
				"Your password reset code is:", code,
				"The code is valid for %d minutes and can be used once. Do not share it with anyone, "
						.formatted(minutes) + "including people claiming to be En-Learning.",
				"If you did not request a password reset, ignore this email; your password stays unchanged.");
		return new EmailMessage(to, subject, text, html);
	}

	static String layout(String greeting, String lead, String code, String warning, String footer) {
		return """
				<div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:0 auto;color:#1f2937">
				  <p>%s</p>
				  <p>%s</p>
				  <p style="font-size:32px;font-weight:700;letter-spacing:8px;margin:16px 0">%s</p>
				  <p>%s</p>
				  <p style="color:#6b7280;font-size:13px">%s</p>
				</div>
				""".formatted(greeting, lead, code, warning, footer);
	}
}
