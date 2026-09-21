package vn.enlearning.backend.mail;

/** Cổng gửi email. Có hai bản: SMTP thật và bản ghi log dùng khi phát triển chưa cấu hình SMTP. */
public interface EmailSender {

	/** @throws EmailSendException khi không gửi được */
	void send(EmailMessage message);
}
