package vn.enlearning.backend.mail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/** Gửi qua SMTP. Gmail luôn ghi đè người gửi bằng chính tài khoản đăng nhập, nên {@code from} nên trùng nó. */
public class SmtpEmailSender implements EmailSender {

	private static final String SENDER_NAME = "En-Learning";

	private final JavaMailSender mailSender;
	private final String from;

	public SmtpEmailSender(JavaMailSender mailSender, String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public void send(EmailMessage message) {
		try {
			MimeMessage mime = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
			helper.setFrom(from, SENDER_NAME);
			helper.setTo(message.to());
			helper.setSubject(message.subject());
			helper.setText(message.text(), message.html());
			mailSender.send(mime);
		} catch (MessagingException | UnsupportedEncodingException | MailException e) {
			throw new EmailSendException("Không gửi được email qua SMTP", e);
		}
	}
}
