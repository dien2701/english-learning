package vn.enlearning.backend.mail;

import lombok.extern.slf4j.Slf4j;

/**
 * Chỉ dùng khi phát triển mà chưa cấu hình SMTP: ghi nội dung email (kèm mã OTP) ra log thay vì gửi đi.
 * {@link MailConfig} không cho dùng nó ở profile {@code prod}.
 */
@Slf4j
public class LoggingEmailSender implements EmailSender {

	@Override
	public void send(EmailMessage message) {
		log.warn("[EMAIL GIẢ LẬP - chưa cấu hình SMTP] Tới: {} | Tiêu đề: {}\n{}",
				message.to(), message.subject(), message.text());
	}
}
