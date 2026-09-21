package vn.enlearning.backend.seed;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.dto.Emails;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.entity.enums.UiLanguage;
import vn.enlearning.backend.entity.enums.UiTheme;

/**
 * Sinh 47 học viên giả (cùng 3 tài khoản cố định thành 50) kèm cài đặt cá nhân. Toàn bộ dùng {@link Random}
 * có seed cố định nên mỗi lần nạp ra cùng một bộ tên, trạng thái và cài đặt (chỉ mốc thời gian trượt theo giờ chạy).
 *
 * <p>Người dùng demo được nhận diện bằng email đuôi {@link #EMAIL_SUFFIX}; {@link #deleteAll()} chỉ xoá
 * những tài khoản này, không đụng người dùng thật.
 */
@Slf4j
@Service
@Profile({ "dev", "test" })
@RequiredArgsConstructor
public class DemoUserSeedService {

	public static final String EMAIL_SUFFIX = "@demo.enlearning.vn";
	static final int COUNT = 47;
	private static final long RANDOM_SEED = 20260921L;
	private static final String PASSWORD = "123456";

	private static final String[] FAMILY = { "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ",
			"Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương" };
	private static final String[] MALE_MIDDLE = { "Văn", "Minh", "Quốc", "Gia", "Đức" };
	private static final String[] MALE_GIVEN = { "An", "Bình", "Dũng", "Hải", "Hiếu", "Hùng", "Khánh", "Long", "Nam",
			"Quân", "Sơn", "Tuấn", "Đạt", "Phúc" };
	private static final String[] FEMALE_MIDDLE = { "Thị", "Ngọc", "Thanh", "Khánh", "Bảo" };
	private static final String[] FEMALE_GIVEN = { "Chi", "Giang", "Hà", "Hạnh", "Lan", "Linh", "Mai", "Phương",
			"Quỳnh", "Thảo", "Trang", "Vy", "Ngân", "Yến" };
	private static final String[] ENGLISH_GIVEN = { "James", "Emily", "Daniel", "Sophie", "Kevin", "Olivia",
			"Michael", "Grace", "Andrew", "Hannah" };
	private static final String[] ENGLISH_FAMILY = { "Nguyen", "Tran", "Le", "Pham", "Hoang", "Vu", "Dang", "Bui" };

	private static final int[] GOALS = { 15, 20, 30, 45, 60 };
	private static final LocalTime[] REMINDER_TIMES = { LocalTime.of(7, 0), LocalTime.of(12, 30), LocalTime.of(20, 0),
			LocalTime.of(21, 0) };

	private final UserRepository users;
	private final UserSettingRepository settings;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	/** Số dòng đã nạp; test đối chiếu với dữ liệu đọc lại. */
	public record Report(int users, int locked, int dormant) {
	}

	/** @return báo cáo nếu đã nạp; rỗng nếu đã có người dùng demo nên bỏ qua */
	@Transactional
	public Optional<Report> seedIfMissing() {
		if (users.countByEmailSuffixIncludingDeleted(EMAIL_SUFFIX) > 0) {
			log.info("Đã có người dùng demo, bỏ qua nạp người dùng giả.");
			return Optional.empty();
		}
		Random random = new Random(RANDOM_SEED);
		Instant now = clock.instant();
		// BCrypt cost 12 chậm: băm một lần rồi dùng chung cho cả 47 tài khoản.
		String passwordHash = passwordEncoder.encode(PASSWORD);

		List<User> created = new ArrayList<>();
		List<Instant[]> stamps = new ArrayList<>();
		int locked = 0;
		int dormant = 0;
		for (int i = 0; i < COUNT; i++) {
			String fullName = fullName(i, random);
			boolean isLocked = i % 16 == 6;
			boolean isDormant = !isLocked && i % 9 == 8;

			User user = new User();
			user.setEmail(Emails.normalize(slug(fullName) + "." + String.format("%02d", i + 1) + EMAIL_SUFFIX));
			user.setPasswordHash(passwordHash);
			user.setFullName(fullName);
			user.setAvatarUrl("https://i.pravatar.cc/150?u=" + user.getEmail());
			user.setRole(Role.USER);
			user.setStatus(isLocked ? AccountStatus.LOCKED : AccountStatus.ACTIVE);
			users.save(user);
			created.add(user);

			// Người ngủ đông tạo tài khoản từ 60 ngày trước để "im lặng hơn 30 ngày" luôn hợp lý.
			int createdDaysAgo = isDormant ? 60 + random.nextInt(121) : 1 + random.nextInt(180);
			int activeDaysAgo = isDormant ? 31 + random.nextInt(createdDaysAgo - 31)
					: random.nextInt(Math.min(createdDaysAgo, 30));
			Instant createdAt = now.minus(createdDaysAgo, ChronoUnit.DAYS);
			Instant lastActiveAt = now.minus(activeDaysAgo, ChronoUnit.DAYS).minus(random.nextInt(1440),
					ChronoUnit.MINUTES);
			stamps.add(new Instant[] { createdAt, lastActiveAt });

			settings.save(settingOf(user, random));
			if (isLocked) {
				locked++;
			}
			if (isDormant) {
				dormant++;
			}
		}
		for (int i = 0; i < created.size(); i++) {
			users.backdate(created.get(i).getId(), stamps.get(i)[0], stamps.get(i)[1]);
		}

		Report report = new Report(created.size(), locked, dormant);
		log.info("Đã nạp người dùng demo: {}", report);
		return Optional.of(report);
	}

	/** Xoá mọi người dùng demo (kèm dữ liệu học của họ theo khoá ngoại); người dùng thật giữ nguyên. */
	@Transactional
	public int deleteAll() {
		int deleted = users.deleteByEmailSuffix(EMAIL_SUFFIX);
		log.info("Đã xoá {} người dùng demo.", deleted);
		return deleted;
	}

	private static UserSetting settingOf(User user, Random random) {
		UserSetting setting = new UserSetting();
		setting.setUser(user);
		setting.setLanguage(random.nextInt(10) < 7 ? UiLanguage.VI : UiLanguage.EN);
		setting.setTheme(random.nextInt(4) == 0 ? UiTheme.DARK : UiTheme.LIGHT);
		setting.setEmailReminders(random.nextInt(100) < 65);
		setting.setReminderTime(REMINDER_TIMES[random.nextInt(REMINDER_TIMES.length)]);
		setting.setDailyGoalMinutes(GOALS[random.nextInt(GOALS.length)]);
		return setting;
	}

	/** Khoảng 30% tên kiểu Anh (Emily Tran), còn lại tên Việt theo giới. */
	private static String fullName(int index, Random random) {
		if (index % 10 == 3 || index % 10 == 6 || index % 10 == 9) {
			return pick(ENGLISH_GIVEN, random) + " " + pick(ENGLISH_FAMILY, random);
		}
		boolean female = random.nextBoolean();
		return pick(FAMILY, random) + " " + pick(female ? FEMALE_MIDDLE : MALE_MIDDLE, random) + " "
				+ pick(female ? FEMALE_GIVEN : MALE_GIVEN, random);
	}

	private static String pick(String[] values, Random random) {
		return values[random.nextInt(values.length)];
	}

	/** "Nguyễn Văn An" thành "nguyen.van.an". */
	private static String slug(String fullName) {
		String ascii = Normalizer.normalize(fullName, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replace('đ', 'd')
				.replace('Đ', 'D');
		return ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.|\\.$", "");
	}
}
