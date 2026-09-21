package vn.enlearning.backend.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import vn.enlearning.backend.mail.EmailSender;

/** Bật lại giới hạn tần suất (test mặc định tắt) với ngưỡng nhỏ để chạm được nhanh. */
@SpringBootTest(properties = {
		"app.rate-limit.enabled=true",
		"app.rate-limit.login.capacity=3",
		"app.rate-limit.check-email.capacity=2",
		"app.rate-limit.forgot-password.capacity=2",
		"app.rate-limit.login.window=1m",
		"app.rate-limit.check-email.window=1m",
		"app.rate-limit.forgot-password.window=1m"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitApiTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private RateLimiter limiter;

	/** Không để email thật nào rời khỏi máy khi thử forgot-password. */
	@MockitoBean
	private EmailSender emailSender;

	@BeforeEach
	void resetBuckets() {
		limiter.reset();
	}

	private ResultActions login() throws Exception {
		return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"khong-ton-tai@test.local\",\"password\":\"sai-mat-khau\"}"));
	}

	private ResultActions forgotPassword() throws Exception {
		return mvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"khong-ton-tai@test.local\"}"));
	}

	private ResultActions checkEmail() throws Exception {
		return mvc.perform(get("/auth/check-email").param("email", "khong-ton-tai@test.local"));
	}

	@Test
	@DisplayName("login: vượt ngưỡng thì 429 kèm messageKey và Retry-After")
	void loginIsRateLimited() throws Exception {
		for (int i = 0; i < 3; i++) {
			login().andExpect(status().isUnauthorized());
		}
		login().andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("RATE_LIMITED"))
				.andExpect(jsonPath("$.messageKey").value("errors.tooManyRequests"));
	}

	@Test
	@DisplayName("check-email: vượt ngưỡng thì 429")
	void checkEmailIsRateLimited() throws Exception {
		checkEmail().andExpect(status().isOk());
		checkEmail().andExpect(status().isOk());
		checkEmail().andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.messageKey").value("errors.tooManyRequests"));
	}

	@Test
	@DisplayName("forgot-password: vượt ngưỡng thì 429")
	void forgotPasswordIsRateLimited() throws Exception {
		forgotPassword().andExpect(status().isOk());
		forgotPassword().andExpect(status().isOk());
		forgotPassword().andExpect(status().isTooManyRequests());
	}

	@Test
	@DisplayName("Mỗi endpoint có bộ đếm riêng")
	void bucketsAreIndependentPerEndpoint() throws Exception {
		for (int i = 0; i < 4; i++) {
			login();
		}
		login().andExpect(status().isTooManyRequests());
		checkEmail().andExpect(status().isOk());
	}
}
