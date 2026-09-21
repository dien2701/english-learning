package vn.enlearning.backend.config;

import java.time.Clock;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Luồng nền cho việc gửi email, và đồng hồ hệ thống để test đổi được thời gian. */
@Configuration
@EnableAsync
public class AsyncConfig {

	public static final String AUTH_TASK_EXECUTOR = "authTaskExecutor";

	/**
	 * Test đặt {@code app.async.synchronous=true} để việc bất đồng bộ chạy ngay trong giao dịch test
	 * (và tự rollback theo), thay vì chạy ở luồng khác không thấy dữ liệu chưa commit.
	 */
	@Bean(name = AUTH_TASK_EXECUTOR)
	Executor authTaskExecutor(@Value("${app.async.synchronous:false}") boolean synchronous) {
		if (synchronous) {
			return new SyncTaskExecutor();
		}
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("auth-async-");
		// Tắt server thì chờ nốt các email đang gửi thay vì bỏ ngang.
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(15);
		return executor;
	}

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
