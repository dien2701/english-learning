package vn.enlearning.backend.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Tự nạp dữ liệu mẫu khi khởi động với {@code SPRING_PROFILES_ACTIVE=dev}; không bao giờ chạy ở profile khác. */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevSeedRunner implements ApplicationRunner {

	private final SeedService seedService;
	private final HistorySeedService historySeed;

	@Override
	public void run(ApplicationArguments args) {
		seedService.seedIfEmpty();
		// Chạy cả khi DB đã có người dùng từ trước: chỉ nạp nếu học viên mẫu chưa có lịch sử học.
		historySeed.seedIfEmpty();
	}
}
