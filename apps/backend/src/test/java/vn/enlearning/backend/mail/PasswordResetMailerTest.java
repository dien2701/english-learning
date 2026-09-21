package vn.enlearning.backend.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.enlearning.backend.entity.EmailLog;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;
import vn.enlearning.backend.entity.enums.UiLanguage;

@ExtendWith(MockitoExtension.class)
class PasswordResetMailerTest {

	private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");

	@Mock
	private EmailSender emailSender;
	@Mock
	private EmailLogRepository emailLogs;

	private PasswordResetMailer mailer;
	private User user;

	@BeforeEach
	void setUp() {
		mailer = new PasswordResetMailer(emailSender, emailLogs, Clock.fixed(NOW, ZoneOffset.UTC));
		user = new User();
		user.setEmail("hocvien@enlearning.vn");
		user.setFullName("Học <b>Viên</b>");
		when(emailLogs.save(any(EmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	@DisplayName("Gửi được: email chứa mã và thời hạn, dòng email_logs chuyển PENDING → SENT")
	void successfulSendIsLoggedAsSent() {
		mailer.send(user, "482913", Duration.ofMinutes(10), UiLanguage.VI);

		ArgumentCaptor<EmailMessage> message = ArgumentCaptor.forClass(EmailMessage.class);
		verify(emailSender).send(message.capture());
		assertThat(message.getValue().to()).isEqualTo("hocvien@enlearning.vn");
		assertThat(message.getValue().text()).contains("482913", "10 phút");
		assertThat(message.getValue().subject()).doesNotContain("482913");
		// Tên người dùng là dữ liệu tự do nên phải được escape trong bản HTML.
		assertThat(message.getValue().html()).contains("482913").doesNotContain("<b>Viên</b>").contains("&lt;b&gt;");

		ArgumentCaptor<EmailLog> saved = ArgumentCaptor.forClass(EmailLog.class);
		verify(emailLogs, times(2)).save(saved.capture());
		EmailLog entry = saved.getValue();
		assertThat(entry.getType()).isEqualTo(EmailType.PASSWORD_RESET);
		assertThat(entry.getStatus()).isEqualTo(EmailStatus.SENT);
		assertThat(entry.getSentAt()).isEqualTo(NOW);
		assertThat(entry.getRecipientEmail()).isEqualTo("hocvien@enlearning.vn");
	}

	@Test
	@DisplayName("Ngôn ngữ EN dùng nội dung tiếng Anh")
	void englishUsersGetEnglishContent() {
		mailer.send(user, "482913", Duration.ofMinutes(10), UiLanguage.EN);

		ArgumentCaptor<EmailMessage> message = ArgumentCaptor.forClass(EmailMessage.class);
		verify(emailSender).send(message.capture());
		assertThat(message.getValue().subject()).isEqualTo("Your En-Learning password reset code");
		assertThat(message.getValue().text()).contains("482913", "10 minutes");
	}

	@Test
	@DisplayName("Gửi hỏng: không ném lỗi ra ngoài, dòng log là FAILED kèm nguyên nhân gốc trên một dòng")
	void failedSendIsLoggedWithRootCauseAndNeverThrows() {
		doThrow(new EmailSendException("Không gửi được email qua SMTP",
				new IllegalStateException("535 5.7.8 Username and Password\nnot accepted")))
				.when(emailSender).send(any());

		assertThatCode(() -> mailer.send(user, "482913", Duration.ofMinutes(10), UiLanguage.VI)).doesNotThrowAnyException();

		ArgumentCaptor<EmailLog> saved = ArgumentCaptor.forClass(EmailLog.class);
		verify(emailLogs, times(2)).save(saved.capture());
		EmailLog entry = saved.getValue();
		assertThat(entry.getStatus()).isEqualTo(EmailStatus.FAILED);
		assertThat(entry.getSentAt()).isNull();
		assertThat(entry.getErrorMessage())
				.contains("Không gửi được email qua SMTP", "IllegalStateException", "535 5.7.8 Username and Password not accepted")
				.doesNotContain("\n")
				.hasSizeLessThanOrEqualTo(500);
	}
}
