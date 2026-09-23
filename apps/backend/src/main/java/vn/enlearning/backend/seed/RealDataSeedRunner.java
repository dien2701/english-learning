package vn.enlearning.backend.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lệnh chạy một lần: bật bằng {@code app.seed.real-data=true} (env {@code REAL_DATA_SEED=true}). Nạp dữ liệu
 * thật của đợt 13 (~1.500 từ, 20 chủ đề, 40 bài nghe...) từ {@code seed/real/*.json}, xem
 * {@link RealDataSeedService}. Idempotent nên chạy lại (kể cả khi DB đã có dữ liệu demo hoặc dữ liệu thật cũ)
 * không nhân đôi bản ghi.
 */
@Slf4j
@Component
@Profile("dev")
@Order(90) // sau DevSeedRunner, để chủ đề/người dùng demo đã có sẵn khi khởi động lần đầu
@ConditionalOnProperty(prefix = "app.seed", name = "real-data", havingValue = "true")
@RequiredArgsConstructor
public class RealDataSeedRunner implements ApplicationRunner {

	private final RealDataSeedService realDataSeedService;

	@Override
	public void run(ApplicationArguments args) {
		realDataSeedService.seed();
		log.info("Hãy đặt lại REAL_DATA_SEED=false (hoặc bỏ cờ) để lần khởi động sau không chạy lại.");
	}
}
