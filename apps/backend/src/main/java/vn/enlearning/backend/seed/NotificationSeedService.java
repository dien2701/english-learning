package vn.enlearning.backend.seed;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.entity.EmailLog;
import vn.enlearning.backend.entity.Notification;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserNotification;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;
import vn.enlearning.backend.entity.enums.NotificationAudience;
import vn.enlearning.backend.entity.enums.NotificationStatus;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.mail.EmailLogRepository;
import vn.enlearning.backend.notification.repository.NotificationRepository;
import vn.enlearning.backend.notification.repository.UserNotificationRepository;

/**
 * Nạp thông báo (kèm hộp thư đã đọc/chưa đọc) và nhật ký email 30 ngày để trang quản trị và chuông thông báo có dữ liệu.
 * Thông báo nhận diện bằng tiêu đề trong {@link #NOTIFICATIONS}, nhật ký email bằng đuôi email người nhận demo, nên
 * {@link #deleteDemo()} chỉ xoá đúng dữ liệu này. Random có seed cố định; chạy lại khi đã có dữ liệu thì bỏ qua.
 */
@Slf4j
@Service
@Profile({ "dev", "test" })
@RequiredArgsConstructor
public class NotificationSeedService {

	private static final long RANDOM_SEED = 20260921L ^ 0x4e;
	private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final Duration ACTIVE_WINDOW = Duration.ofDays(30);
	private static final int EMAIL_DAYS = 30;
	private static final String REMINDER_SUBJECT = "Đến giờ học tiếng Anh rồi!";
	private static final String RESET_SUBJECT = "Mã đặt lại mật khẩu En-Learning";
	private static final List<String> FAILURES = List.of("550 5.1.1 Mailbox unavailable",
			"Connection timed out after 10000ms", "452 4.2.2 Mailbox full");

	/** {@code sent = false} là bản nháp (chưa gửi, không có bản nhận). */
	private record Seed(String title, String content, NotificationAudience audience, int daysAgo, boolean sent) {
	}

	/** Cũ đến mới: hệ thống, nhắc học và nội dung mới; một thông báo quản trị nội bộ và một bản nháp. */
	private static final List<Seed> NOTIFICATIONS = List.of(
			new Seed("Chào mừng bạn đến với EN-Learning",
					"Cảm ơn bạn đã tham gia! Hãy bắt đầu bằng một bộ từ vựng và đặt mục tiêu học mỗi ngày trong phần Cài đặt.",
					NotificationAudience.ALL, 28, true),
			new Seed("Bộ thẻ mới: Từ vựng du lịch",
					"Bộ thẻ Từ vựng du lịch đã có mặt với 20 từ thông dụng khi đi sân bay, khách sạn và nhà hàng.",
					NotificationAudience.ALL, 21, true),
			new Seed("Nhắc học: giữ chuỗi ngày học của bạn",
					"Bạn đã vắng mặt một thời gian. Chỉ cần 15 phút hôm nay là đủ để bắt nhịp lại, thử ôn vài thẻ từ vựng nhé.",
					NotificationAudience.INACTIVE, 18, true),
			new Seed("Bảo trì hệ thống cuối tuần",
					"Hệ thống sẽ bảo trì từ 01:00 đến 03:00 sáng Chủ nhật. Bạn nên nộp các bài đang làm dở trước giờ này.",
					NotificationAudience.ALL, 14, true),
			new Seed("Đề thi TOEIC mini mới",
					"Hai đề TOEIC mini vừa được thêm vào mục Kiểm tra, mỗi đề khoảng 30 câu, phù hợp để thử sức trong 30 phút.",
					NotificationAudience.ACTIVE, 10, true),
			new Seed("Mẹo luyện nghe mỗi ngày",
					"Nghe một đoạn ba lần: lần đầu nắm ý chính, lần hai bắt chi tiết, lần ba đối chiếu với transcript.",
					NotificationAudience.ACTIVE, 7, true),
			new Seed("Bài luyện viết IELTS Task 2 mới",
					"Hai đề IELTS Task 2 mới về công nghệ trong giáo dục và cuộc sống thành thị đang chờ bạn ở mục Luyện viết.",
					NotificationAudience.ACTIVE, 4, true),
			new Seed("Nhắc học: bạn đã lâu chưa quay lại",
					"Nhiều bài học mới đã được thêm trong thời gian bạn vắng mặt. Vào học lại để không mất chuỗi tiến bộ nhé.",
					NotificationAudience.INACTIVE, 2, true),
			new Seed("Báo cáo hệ thống tuần này",
					"Tuần này có thêm người dùng mới và nhiều lượt làm bài. Không có sự cố nghiêm trọng nào được ghi nhận.",
					NotificationAudience.ADMIN, 1, true),
			new Seed("Sắp ra mắt: thi thử IELTS Speaking",
					"Bản nháp: giới thiệu tính năng thi thử IELTS Speaking có chấm điểm phát âm, dự kiến ra mắt vào tháng sau.",
					NotificationAudience.ALL, 0, false));

	private final UserRepository users;
	private final UserSettingRepository settings;
	private final NotificationRepository notifications;
	private final UserNotificationRepository inbox;
	private final EmailLogRepository emailLogs;
	private final EntityManager em;
	private final Clock clock;

	/** Số dòng đã nạp; test đối chiếu với dữ liệu đọc lại. */
	public record Report(int notifications, int inboxItems, int emailLogs) {
	}

	/** @return báo cáo nếu đã nạp; rỗng nếu đã có dữ liệu mẫu nên bỏ qua */
	@Transactional
	public Optional<Report> seedIfMissing() {
		Instant now = clock.instant();
		Random random = new Random(RANDOM_SEED);
		List<User> all = users.findAll();
		int notificationCount = 0;
		int inboxCount = 0;
		if (count("select count(*) from notifications where title = :v", NOTIFICATIONS.get(0).title()) == 0) {
			User admin = users.findByEmail("admin@enlearning.vn").orElse(null);
			for (Seed seed : NOTIFICATIONS) {
				inboxCount += seedNotification(seed, admin, all, now, random);
				notificationCount++;
			}
		}
		int logCount = 0;
		if (count("select count(*) from email_logs where recipient_email like :v",
				"%" + DemoUserSeedService.EMAIL_SUFFIX) == 0) {
			logCount = seedEmailLogs(all, now, random);
		}
		if (notificationCount == 0 && logCount == 0) {
			log.info("Đã có thông báo và nhật ký email mẫu, bỏ qua.");
			return Optional.empty();
		}
		Report report = new Report(notificationCount, inboxCount, logCount);
		log.info("Đã nạp thông báo và nhật ký email mẫu: {}", report);
		return Optional.of(report);
	}

	/** Xoá thông báo và nhật ký email mẫu (chế độ {@code reset-demo}); dữ liệu khác giữ nguyên. */
	@Transactional
	public void deleteDemo() {
		em.createNativeQuery("delete from notifications where title in (:titles)")
				.setParameter("titles", NOTIFICATIONS.stream().map(Seed::title).toList()).executeUpdate();
		em.createNativeQuery("delete from email_logs where recipient_email like :suffix")
				.setParameter("suffix", "%" + DemoUserSeedService.EMAIL_SUFFIX).executeUpdate();
		log.info("Đã xoá thông báo và nhật ký email mẫu.");
	}

	// --- Thông báo -----------------------------------------------------------------------------------

	/** @return số bản nhận đã tạo */
	private int seedNotification(Seed seed, User admin, List<User> all, Instant now, Random random) {
		Notification notification = new Notification();
		notification.setTitle(seed.title());
		notification.setContent(seed.content());
		notification.setAudience(seed.audience());
		notification.setCreatedBy(admin);
		Instant sentAt = now.minus(Duration.ofDays(seed.daysAgo())).minus(Duration.ofMinutes(90));
		if (!seed.sent()) {
			notification.setStatus(NotificationStatus.DRAFT);
			notifications.save(notification);
			backdateNotification(notification.getId(), now.minus(Duration.ofHours(6)));
			return 0;
		}

		List<User> recipients = all.stream().filter(u -> receives(u, seed.audience(), sentAt, now)).toList();
		notification.setStatus(NotificationStatus.SENT);
		notification.setRecipientCount(recipients.size());
		notification.setSentAt(sentAt);
		notifications.save(notification);

		// Càng cũ càng nhiều người đã đọc; học viên chính luôn còn thông báo mới chưa đọc để chuông có số.
		double readChance = Math.min(0.85, 0.3 + 0.03 * seed.daysAgo());
		List<UserNotification> items = new ArrayList<>();
		for (User user : recipients) {
			UserNotification item = new UserNotification();
			item.setNotification(notification);
			item.setUser(user);
			boolean forcedUnread = HistorySeedService.DEMO_EMAIL.equals(user.getEmail()) && seed.daysAgo() <= 7;
			if (!forcedUnread && random.nextDouble() < readChance) {
				Instant readAt = sentAt.plus(Duration.ofMinutes(5 + random.nextInt(72 * 60)));
				item.setReadAt(readAt.isAfter(now) ? now : readAt);
			}
			items.add(item);
		}
		inbox.saveAll(items);
		em.flush();
		backdateNotification(notification.getId(), sentAt.minus(Duration.ofMinutes(30)));
		em.createNativeQuery("update user_notifications set created_at = :t where notification_id = :id")
				.setParameter("t", sentAt).setParameter("id", notification.getId()).executeUpdate();
		return items.size();
	}

	/** Cùng quy tắc nhóm nhận với gửi thật, tính tại thời điểm gửi: người tạo tài khoản sau đó không nhận. */
	private static boolean receives(User user, NotificationAudience audience, Instant sentAt, Instant now) {
		if (user.getStatus() != AccountStatus.ACTIVE || user.getCreatedAt().isAfter(sentAt)) {
			return false;
		}
		boolean recent = user.getLastActiveAt() != null && !user.getLastActiveAt().isBefore(now.minus(ACTIVE_WINDOW));
		return switch (audience) {
			case ALL -> true;
			case ACTIVE -> recent;
			case INACTIVE -> !recent;
			case ADMIN -> user.getRole() == Role.ADMIN;
		};
	}

	/** {@code created_at} không sửa được qua entity: đặt lại bằng SQL để danh sách xếp đúng thứ tự thời gian. */
	private void backdateNotification(UUID id, Instant at) {
		em.flush();
		em.createNativeQuery("update notifications set created_at = :t, updated_at = :t where id = :id")
				.setParameter("t", at).setParameter("id", id).executeUpdate();
	}

	// --- Nhật ký email -------------------------------------------------------------------------------

	/**
	 * Ba loại email trong {@value #EMAIL_DAYS} ngày qua, chỉ gửi cho người dùng demo: nhắc học (người bật nhắc, mỗi
	 * người tối đa một email mỗi ngày như ràng buộc UNIQUE), mã đặt lại mật khẩu, và bản email của các thông báo.
	 * Khoảng 8% ở trạng thái {@code FAILED} kèm lý do.
	 */
	private int seedEmailLogs(List<User> all, Instant now, Random random) {
		Map<UUID, UserSetting> settingByUser = new HashMap<>();
		for (UserSetting s : settings.findAll()) {
			settingByUser.put(s.getUser().getId(), s);
		}
		List<User> demo = all.stream().filter(u -> u.getEmail().endsWith(DemoUserSeedService.EMAIL_SUFFIX)
				&& u.getStatus() == AccountStatus.ACTIVE).toList();
		if (demo.isEmpty()) {
			return 0;
		}
		List<EmailLog> rows = new ArrayList<>();
		List<Instant> times = new ArrayList<>();
		for (int d = 1; d <= EMAIL_DAYS; d++) {
			for (User user : demo) {
				UserSetting setting = settingByUser.get(user.getId());
				if (setting == null || !setting.isEmailReminders() || random.nextDouble() >= 0.35) {
					continue;
				}
				ZoneId zone = zoneOf(setting);
				LocalDate day = LocalDate.now(clock.withZone(zone)).minusDays(d);
				Instant at = day.atTime(setting.getReminderTime()).atZone(zone).toInstant();
				if (user.getCreatedAt().isAfter(at)) {
					continue;
				}
				EmailLog row = log(user, EmailType.STUDY_REMINDER, REMINDER_SUBJECT, at, random);
				row.setReminderDate(day);
				rows.add(row);
				times.add(at);
			}
		}
		for (int i = 0; i < 5; i++) {
			Instant at = now.minus(Duration.ofDays(1 + random.nextInt(EMAIL_DAYS - 1))).minusSeconds(random.nextInt(3600));
			rows.add(log(demo.get(random.nextInt(demo.size())), EmailType.PASSWORD_RESET, RESET_SUBJECT, at, random));
			times.add(at);
		}
		for (Seed seed : NOTIFICATIONS) {
			if (!seed.sent() || seed.daysAgo() > EMAIL_DAYS || seed.audience() == NotificationAudience.ADMIN) {
				continue;
			}
			Instant at = now.minus(Duration.ofDays(seed.daysAgo())).minus(Duration.ofMinutes(89));
			for (int i = 0; i < 2; i++) {
				rows.add(log(demo.get(random.nextInt(demo.size())), EmailType.NOTIFICATION, seed.title(), at, random));
				times.add(at);
			}
		}
		emailLogs.saveAll(rows);
		em.flush();
		for (int i = 0; i < rows.size(); i++) {
			em.createNativeQuery("update email_logs set created_at = :t where id = :id")
					.setParameter("t", times.get(i)).setParameter("id", rows.get(i).getId()).executeUpdate();
		}
		return rows.size();
	}

	private static EmailLog log(User user, EmailType type, String subject, Instant at, Random random) {
		EmailLog row = new EmailLog();
		row.setUser(user);
		row.setType(type);
		row.setRecipientEmail(user.getEmail());
		row.setSubject(subject);
		if (random.nextInt(100) < 8) {
			row.setStatus(EmailStatus.FAILED);
			row.setErrorMessage(FAILURES.get(random.nextInt(FAILURES.size())));
		} else {
			row.setStatus(EmailStatus.SENT);
			row.setSentAt(at.plusSeconds(1 + random.nextInt(4)));
		}
		return row;
	}

	private static ZoneId zoneOf(UserSetting setting) {
		try {
			return ZoneId.of(setting.getTimeZone());
		} catch (DateTimeException | NullPointerException e) {
			return DEFAULT_ZONE;
		}
	}

	private long count(String sql, String value) {
		return ((Number) em.createNativeQuery(sql).setParameter("v", value).getSingleResult()).longValue();
	}
}
