package vn.enlearning.backend.mail;

import java.time.Clock;
import java.time.Duration;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.entity.EmailLog;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;
import vn.enlearning.backend.entity.enums.UiLanguage;

/**
 * Gửi email mã đặt lại mật khẩu và ghi {@code email_logs}: dòng log tạo trước ở trạng thái PENDING,
 * rồi chuyển SENT hoặc FAILED. Không ném lỗi ra ngoài vì người gọi không thể làm gì với một email hỏng
 * (và không được phép để lộ chuyện đó cho người đang hỏi).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetMailer {

	private final EmailSender emailSender;
	private final EmailLogRepository emailLogs;
	private final Clock clock;

	public void send(User user, String code, Duration validFor, UiLanguage language) {
		EmailMessage message = PasswordResetMailTemplate.build(
				user.getEmail(), user.getFullName(), code, validFor.toMinutes(), language);

		EmailLog entry = new EmailLog();
		entry.setUser(user);
		entry.setType(EmailType.PASSWORD_RESET);
		entry.setRecipientEmail(message.to());
		entry.setSubject(message.subject());
		entry = emailLogs.save(entry);

		try {
			emailSender.send(message);
			entry.setStatus(EmailStatus.SENT);
			entry.setSentAt(clock.instant());
		} catch (RuntimeException e) {
			log.error("Gửi email đặt lại mật khẩu thất bại (email_logs {})", entry.getId(), e);
			entry.setStatus(EmailStatus.FAILED);
			entry.setErrorMessage(EmailErrors.describe(e));
		}
		emailLogs.save(entry);
	}
}
