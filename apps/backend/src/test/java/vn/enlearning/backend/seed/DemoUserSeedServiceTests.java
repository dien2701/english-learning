package vn.enlearning.backend.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;

/** Nạp người dùng demo trong giao dịch test rồi rollback. Bỏ qua khi DB dev đã có người dùng demo. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DemoUserSeedServiceTests {

	private static final String SUFFIX = DemoUserSeedService.EMAIL_SUFFIX;

	@Autowired
	private DemoUserSeedService demoUsers;
	@Autowired
	private UserRepository users;
	@Autowired
	private UserSettingRepository settings;

	@Test
	@DisplayName("Nạp 47 user demo có cài đặt, đúng đuôi email, ~5% khoá, ~10% im lặng hơn 30 ngày; chạy lại thì bỏ qua")
	void seedsFortySevenDemoUsersOnce() {
		assumeTrue(users.countByEmailSuffixIncludingDeleted(SUFFIX) == 0, "DB dev đã có người dùng demo");

		DemoUserSeedService.Report report = demoUsers.seedIfMissing().orElseThrow();

		assertThat(report.users()).isEqualTo(DemoUserSeedService.COUNT);
		assertThat(users.countByEmailSuffixIncludingDeleted(SUFFIX)).isEqualTo(DemoUserSeedService.COUNT);
		List<User> demo = users.findAll().stream().filter(u -> u.getEmail().endsWith(SUFFIX)).toList();
		assertThat(demo).hasSize(DemoUserSeedService.COUNT)
				.allSatisfy(u -> assertThat(settings.findByUserId(u.getId())).isPresent());
		assertThat(demo.stream().filter(u -> u.getStatus() == AccountStatus.LOCKED).count()).isEqualTo(report.locked());
		assertThat(report.locked()).isBetween(2, 4);

		Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
		assertThat(demo.stream().filter(u -> u.getLastActiveAt().isBefore(monthAgo)).count())
				.isEqualTo(report.dormant());
		assertThat(demo).allSatisfy(u -> assertThat(u.getLastActiveAt()).isAfterOrEqualTo(u.getCreatedAt()));

		assertThat(demoUsers.seedIfMissing()).isEmpty();
	}

	@Test
	@DisplayName("deleteAll chỉ xoá user demo, kèm cài đặt của họ")
	void deleteAllRemovesOnlyDemoUsers() {
		assumeTrue(users.countByEmailSuffixIncludingDeleted(SUFFIX) == 0, "DB dev đã có người dùng demo");
		long before = users.countIncludingDeleted();
		demoUsers.seedIfMissing();
		long settingsBefore = settings.count();

		assertThat(demoUsers.deleteAll()).isEqualTo(DemoUserSeedService.COUNT);

		assertThat(users.countIncludingDeleted()).isEqualTo(before);
		assertThat(settings.count()).isEqualTo(settingsBefore - DemoUserSeedService.COUNT);
	}
}
