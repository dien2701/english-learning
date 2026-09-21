package vn.enlearning.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Bật {@code @Cacheable}. Bộ nhớ đệm là Caffeine trong tiến trình, tên và TTL khai báo ở
 * {@code spring.cache.*} của {@code application.yml}. Chỉ đệm dữ liệu dùng chung cho mọi người dùng và
 * ít đổi; mọi thao tác ghi tương ứng phải có {@code @CacheEvict}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

	public static final String TOPICS = "topics";
}
