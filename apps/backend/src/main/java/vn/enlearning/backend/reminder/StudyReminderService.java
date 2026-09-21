package vn.enlearning.backend.reminder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.entity.EmailLog;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;
import vn.enlearning.backend.mail.EmailErrors;
import vn.enlearning.backend.mail.EmailLogRepository;
import vn.enlearning.backend.mail.EmailMessage;
import vn.enlearning.backend.mail.EmailSender;
import vn.enlearning.backend.mail.StudyReminderMailTemplate;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Email nhắc học hằng ngày. Mỗi phút quét người dùng đã bật nhắc, chọn ai đã đến giờ theo múi giờ riêng và
 * chưa học đủ mục tiêu hôm nay. Ngày tính theo múi giờ người dùng.
 *
 * <p>Chống gửi trùng bằng {@code email_logs}: dòng PENDING được ghi và commit TRƯỚC khi gửi, và UNIQUE
 * (user_id, reminder_date) bảo đảm chỉ một dòng mỗi người mỗi ngày, kể cả khi có hai tiến trình cùng quét.
 * Hệ quả: gửi lỗi (FAILED) cũng không tự thử lại trong ngày đó. Quét toàn bộ người dùng đã bật nhắc rồi lọc
 * giờ trong Java vì múi giờ mỗi người một khác; đủ cho quy mô hiện tại.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyReminderService {

	private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

	private final UserSettingRepository settings;
	private final StudySessionRepository sessions;
	private final EmailLogRepository emailLogs;
	private final EmailSender emailSender;
	private final ReminderProperties properties;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;

	/** Chạy mỗi phút, ở giây 0. @return số email đã xử lý (SENT hoặc FAILED) */
	@Scheduled(cron = "0 * * * * *")
	public int sendDueReminders() {
		Instant now = clock.instant();
		int handled = 0;
		for (UserSetting setting : settings.findRemindable()) {
			try {
				if (remind(setting, now)) {
					handled++;
				}
			} catch (RuntimeException e) {
				log.error("Nhắc học thất bại cho người dùng {}", setting.getUser().getId(), e);
			}
		}
		return handled;
	}

	private boolean remind(UserSetting setting, Instant now) {
		User user = setting.getUser();
		ZoneId zone = zoneOf(setting);
		ZonedDateTime local = now.atZone(zone);
		LocalDate today = local.toLocalDate();

		LocalDateTime dueAt = today.atTime(setting.getReminderTime());
		LocalDateTime localNow = local.toLocalDateTime();
		if (localNow.isBefore(dueAt) || !localNow.isBefore(dueAt.plus(properties.catchUp()))) {
			return false;
		}
		if (emailLogs.existsByUserIdAndReminderDate(user.getId(), today)) {
			return false;
		}

		long goalSeconds = setting.getDailyGoalMinutes() * 60L;
		long studiedSeconds = sessions.sumActiveSeconds(user.getId(),
				today.atStartOfDay(zone).toInstant(), today.plusDays(1).atStartOfDay(zone).toInstant());
		if (studiedSeconds >= goalSeconds) {
			return false;
		}

		EmailMessage message = StudyReminderMailTemplate.build(user.getEmail(), user.getFullName(),
				setting.getDailyGoalMinutes(), (int) (studiedSeconds / 60), setting.getLanguage());
		EmailLog entry = claim(user, message, today);
		if (entry == null) {
			return false;
		}
		deliver(entry, message);
		return true;
	}

	/** Giữ chỗ trong ngày bằng dòng PENDING; null nếu nơi khác đã giữ trước (vi phạm UNIQUE). */
	private EmailLog claim(User user, EmailMessage message, LocalDate reminderDate) {
		return transactionTemplate.execute(status -> {
			EmailLog entry = new EmailLog();
			entry.setUser(user);
			entry.setType(EmailType.STUDY_REMINDER);
			entry.setRecipientEmail(message.to());
			entry.setSubject(message.subject());
			entry.setReminderDate(reminderDate);
			try {
				return emailLogs.saveAndFlush(entry);
			} catch (DataIntegrityViolationException e) {
				status.setRollbackOnly();
				return null;
			}
		});
	}

	private void deliver(EmailLog entry, EmailMessage message) {
		try {
			emailSender.send(message);
			entry.setStatus(EmailStatus.SENT);
			entry.setSentAt(clock.instant());
		} catch (RuntimeException e) {
			log.error("Gửi email nhắc học thất bại (email_logs {})", entry.getId(), e);
			entry.setStatus(EmailStatus.FAILED);
			entry.setErrorMessage(EmailErrors.describe(e));
		}
		emailLogs.save(entry);
	}

	/** Múi giờ đã được kiểm khi lưu; dữ liệu cũ hỏng thì dùng múi giờ mặc định thay vì bỏ cả đợt quét. */
	private static ZoneId zoneOf(UserSetting setting) {
		try {
			return ZoneId.of(setting.getTimeZone());
		} catch (RuntimeException e) {
			return DEFAULT_ZONE;
		}
	}
}
