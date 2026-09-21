package vn.enlearning.backend.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.javamail.JavaMailSender;

import lombok.extern.slf4j.Slf4j;

/**
 * Chọn cách gửi email lúc khởi động. Có {@code MAIL_USERNAME} và {@code MAIL_PASSWORD} thì gửi SMTP thật;
 * không thì ghi log, và ở profile {@code prod} thì từ chối khởi động thay vì âm thầm không gửi được mã đặt lại.
 */
@Slf4j
@Configuration
public class MailConfig {

	@Bean
	EmailSender emailSender(ObjectProvider<JavaMailSender> mailSender, Environment environment,
			@Value("${spring.mail.username:}") String username,
			@Value("${spring.mail.password:}") String password,
			@Value("${app.mail.from:}") String from) {
		if (!username.isBlank() && !password.isBlank()) {
			return new SmtpEmailSender(mailSender.getObject(), from.isBlank() ? username : from);
		}
		if (environment.acceptsProfiles(Profiles.of("prod"))) {
			throw new IllegalStateException("Profile prod cần cấu hình SMTP: MAIL_USERNAME và MAIL_PASSWORD.");
		}
		log.warn("Chưa cấu hình SMTP (MAIL_USERNAME, MAIL_PASSWORD): email sẽ chỉ được ghi ra log.");
		return new LoggingEmailSender();
	}
}
