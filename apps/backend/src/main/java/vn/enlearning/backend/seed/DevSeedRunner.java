package vn.enlearning.backend.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.seed.SeedProperties.Mode;

/**
 * Tự nạp dữ liệu mẫu khi khởi động với {@code SPRING_PROFILES_ACTIVE=dev}; không bao giờ chạy ở profile khác.
 * Cờ {@code app.seed.mode=reset-demo} xoá dữ liệu demo trước khi nạp lại (xem {@link SeedProperties}).
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevSeedRunner implements ApplicationRunner {

	private final SeedProperties properties;
	private final SeedService seedService;
	private final DemoUserSeedService demoUsers;
	private final HistorySeedService historySeed;
	private final NotificationSeedService notificationSeed;

	@Override
	public void run(ApplicationArguments args) {
		if (properties.mode() == Mode.RESET_DEMO) {
			log.warn("app.seed.mode=reset-demo: xoá dữ liệu demo rồi nạp lại.");
			notificationSeed.deleteDemo();
			demoUsers.deleteAll();
			historySeed.deleteMainHistory();
		}
		seedService.seedIfEmpty();
		demoUsers.seedIfMissing();
		// Chạy cả khi DB đã có người dùng từ trước: chỉ nạp cho người dùng chưa có lịch sử học.
		historySeed.seedIfEmpty();
		historySeed.seedDemoUsers();
		notificationSeed.seedIfMissing();
	}
}
