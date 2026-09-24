package vn.enlearning.backend.mail;

import java.time.Clock;
import java.time.Duration;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.entity.EmailLog;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;
import vn.enlearning.backend.entity.enums.UiLanguage;

/** Gửi email mã xác minh đăng ký và ghi {@code email_logs}. Trả false khi gửi hỏng để người dùng biết mà thử lại. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationMailer {

	private final EmailSender emailSender;
	private final EmailLogRepository emailLogs;
	private final Clock clock;

	public boolean send(String email, String code, Duration validFor, UiLanguage language) {
		EmailMessage message = EmailVerificationMailTemplate.build(email, code, validFor.toMinutes(), language);

		EmailLog entry = new EmailLog();
		entry.setType(EmailType.EMAIL_VERIFICATION);
		entry.setRecipientEmail(message.to());
		entry.setSubject(message.subject());
		entry = emailLogs.save(entry);

		boolean sent;
		try {
			emailSender.send(message);
			entry.setStatus(EmailStatus.SENT);
			entry.setSentAt(clock.instant());
			sent = true;
		} catch (RuntimeException e) {
			log.error("Gửi email xác minh thất bại (email_logs {})", entry.getId(), e);
			entry.setStatus(EmailStatus.FAILED);
			entry.setErrorMessage(EmailErrors.describe(e));
			sent = false;
		}
		emailLogs.save(entry);
		return sent;
	}
}
