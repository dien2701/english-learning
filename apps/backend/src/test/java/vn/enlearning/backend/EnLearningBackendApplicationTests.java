package vn.enlearning.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Flyway migrate + Hibernate {@code ddl-auto: validate}: entity phải khớp V1 và toàn bộ bean phải dựng được. */
@SpringBootTest
@ActiveProfiles("test")
class EnLearningBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
