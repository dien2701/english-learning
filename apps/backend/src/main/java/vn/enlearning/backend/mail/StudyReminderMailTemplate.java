package vn.enlearning.backend.mail;

import org.springframework.web.util.HtmlUtils;

import vn.enlearning.backend.entity.enums.UiLanguage;

/** Nội dung email nhắc học hằng ngày, theo ngôn ngữ người dùng đã chọn. */
public final class StudyReminderMailTemplate {

	private StudyReminderMailTemplate() {
	}

	public static EmailMessage build(String to, String fullName, int goalMinutes, int studiedMinutes,
			UiLanguage language) {
		return language == UiLanguage.EN
				? english(to, fullName, goalMinutes, studiedMinutes)
				: vietnamese(to, fullName, goalMinutes, studiedMinutes);
	}

	private static EmailMessage vietnamese(String to, String fullName, int goal, int studied) {
		String subject = "Đến giờ học tiếng Anh rồi!";
		String progress = "Hôm nay bạn đã học %d/%d phút. Chỉ cần thêm %d phút nữa là đạt mục tiêu."
				.formatted(studied, goal, goal - studied);
		String closing = "Bạn có thể tắt nhắc học trong Cài đặt bất cứ lúc nào.";
		String text = "Xin chào %s,\n\n%s\n\n%s\n".formatted(fullName, progress, closing);
		return new EmailMessage(to, subject, text,
				layout("Xin chào %s,".formatted(HtmlUtils.htmlEscape(fullName)), progress, closing));
	}

	private static EmailMessage english(String to, String fullName, int goal, int studied) {
		String subject = "Time for your English practice!";
		String progress = "You have studied %d of %d minutes today. Just %d more minutes to reach your goal."
				.formatted(studied, goal, goal - studied);
		String closing = "You can turn off reminders in Settings at any time.";
		String text = "Hello %s,\n\n%s\n\n%s\n".formatted(fullName, progress, closing);
		return new EmailMessage(to, subject, text,
				layout("Hello %s,".formatted(HtmlUtils.htmlEscape(fullName)), progress, closing));
	}

	private static String layout(String greeting, String progress, String closing) {
		return """
				<div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:0 auto;color:#1f2937">
				  <p>%s</p>
				  <p>%s</p>
				  <p style="color:#6b7280;font-size:13px">%s</p>
				</div>
				""".formatted(greeting, progress, closing);
	}
}
