package vn.enlearning.backend.reminder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.entity.EmailLog;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.mail.EmailLogRepository;
import vn.enlearning.backend.mail.EmailMessage;
import vn.enlearning.backend.mail.EmailSendException;
import vn.enlearning.backend.mail.EmailSender;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Chạy trên MySQL thật trong giao dịch tự rollback. Service được dựng tay với đồng hồ cố định; người dùng seed
 * (nếu có) cũng bị quét nhưng mọi kiểm tra chỉ nhìn vào người dùng của test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudyReminderServiceTests {

	/** 20:00 giờ Việt Nam ngày 21/09/2026. */
	private static final Instant EIGHT_PM_VN = Instant.parse("2026-09-21T13:00:00Z");
	private static final LocalDate DAY = LocalDate.of(2026, 9, 21);

	@Autowired
	private UserRepository users;
	@Autowired
	private UserSettingRepository settings;
	@Autowired
	private StudySessionRepository sessions;
	@Autowired
	private EmailLogRepository emailLogs;
	@Autowired
	private TransactionTemplate transactionTemplate;

	private EmailSender sender;
	private String email;
	private UserSetting setting;

	@BeforeEach
	void setUp() {
		sender = mock(EmailSender.class);
		email = "reminder-" + UUID.randomUUID() + "@test.local";
		User user = new User();
		user.setEmail(email);
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người Thử");
		user = users.saveAndFlush(user);

		setting = new UserSetting();
		setting.setUser(user);
		setting.setEmailReminders(true);
		setting.setReminderTime(LocalTime.of(20, 0));
		setting.setDailyGoalMinutes(30);
		setting.setTimeZone("Asia/Ho_Chi_Minh");
		setting = settings.saveAndFlush(setting);
	}

	private StudyReminderService serviceAt(Instant now) {
		return new StudyReminderService(settings, sessions, emailLogs, sender,
				new ReminderProperties(Duration.ofHours(1)), Clock.fixed(now, ZoneOffset.UTC), transactionTemplate);
	}

	private long sentToTestUser() {
		ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
		verify(sender, atLeast(0)).send(captor.capture());
		return captor.getAllValues().stream().filter(m -> email.equals(m.to())).count();
	}

	private List<EmailLog> logsOfTestUser() {
		return emailLogs.findAll().stream()
				.filter(l -> email.equals(l.getRecipientEmail()))
				.toList();
	}

	@Test
	@DisplayName("Đến giờ: gửi đúng một email, ghi email_logs SENT; quét lại không gửi trùng")
	void sendsOnceAndNeverTwice() {
		StudyReminderService service = serviceAt(EIGHT_PM_VN);
		service.sendDueReminders();
		service.sendDueReminders();
		serviceAt(EIGHT_PM_VN.plusSeconds(600)).sendDueReminders();

		assertThat(sentToTestUser()).isEqualTo(1);
		List<EmailLog> logs = logsOfTestUser();
		assertThat(logs).hasSize(1);
		assertThat(logs.get(0).getType()).isEqualTo(EmailType.STUDY_REMINDER);
		assertThat(logs.get(0).getStatus()).isEqualTo(EmailStatus.SENT);
		assertThat(logs.get(0).getReminderDate()).isEqualTo(DAY);
		assertThat(logs.get(0).getSentAt()).isNotNull();
	}

	@Test
	@DisplayName("Chưa đến giờ hoặc đã quá cửa sổ bù thì không gửi")
	void skipsOutsideWindow() {
		serviceAt(EIGHT_PM_VN.minusSeconds(60)).sendDueReminders();
		serviceAt(EIGHT_PM_VN.plus(Duration.ofHours(1))).sendDueReminders();
		assertThat(logsOfTestUser()).isEmpty();
	}

	@Test
	@DisplayName("Đã học đủ mục tiêu hôm nay thì không gửi")
	void skipsWhenGoalReached() {
		StudySession session = new StudySession();
		session.setUser(setting.getUser());
		session.setSkill(StudySkill.VOCABULARY);
		session.setStartedAt(Instant.parse("2026-09-21T02:00:00Z"));
		session.setLastHeartbeatAt(Instant.parse("2026-09-21T02:40:00Z"));
		session.setActiveSeconds(30 * 60);
		sessions.saveAndFlush(session);

		serviceAt(EIGHT_PM_VN).sendDueReminders();
		assertThat(logsOfTestUser()).isEmpty();
	}

	@Test
	@DisplayName("Học chưa đủ mục tiêu thì vẫn nhắc")
	void remindsWhenGoalNotReached() {
		StudySession session = new StudySession();
		session.setUser(setting.getUser());
		session.setSkill(StudySkill.VOCABULARY);
		session.setStartedAt(Instant.parse("2026-09-21T02:00:00Z"));
		session.setLastHeartbeatAt(Instant.parse("2026-09-21T02:10:00Z"));
		session.setActiveSeconds(10 * 60);
		sessions.saveAndFlush(session);

		serviceAt(EIGHT_PM_VN).sendDueReminders();
		assertThat(sentToTestUser()).isEqualTo(1);
	}

	@Test
	@DisplayName("Tắt nhắc học thì không gửi")
	void skipsWhenDisabled() {
		setting.setEmailReminders(false);
		settings.saveAndFlush(setting);

		serviceAt(EIGHT_PM_VN).sendDueReminders();
		assertThat(logsOfTestUser()).isEmpty();
	}

	@Test
	@DisplayName("Giờ nhắc tính theo múi giờ của người dùng")
	void usesUserTimeZone() {
		setting.setTimeZone("America/New_York");
		settings.saveAndFlush(setting);

		// 20:00 ở New York (UTC-4 vào tháng 9) là 00:00 UTC hôm sau; 20:00 giờ Việt Nam thì chưa tới lượt họ.
		serviceAt(EIGHT_PM_VN).sendDueReminders();
		assertThat(logsOfTestUser()).isEmpty();

		serviceAt(Instant.parse("2026-09-22T00:00:30Z")).sendDueReminders();
		List<EmailLog> logs = logsOfTestUser();
		assertThat(logs).hasSize(1);
		assertThat(logs.get(0).getReminderDate()).isEqualTo(DAY);
	}

	@Test
	@DisplayName("Gửi lỗi thì ghi FAILED và không thử lại trong ngày")
	void recordsFailureWithoutRetry() {
		doThrow(new EmailSendException("SMTP hỏng", null)).when(sender).send(any(EmailMessage.class));

		serviceAt(EIGHT_PM_VN).sendDueReminders();
		serviceAt(EIGHT_PM_VN.plusSeconds(120)).sendDueReminders();

		List<EmailLog> logs = logsOfTestUser();
		assertThat(logs).hasSize(1);
		assertThat(logs.get(0).getStatus()).isEqualTo(EmailStatus.FAILED);
		assertThat(logs.get(0).getErrorMessage()).contains("SMTP hỏng");
	}
}
