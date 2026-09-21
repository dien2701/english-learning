package vn.enlearning.backend.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/** Gắn giới hạn tần suất vào các endpoint xác thực công khai, dễ bị dò mật khẩu hoặc dò email. */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

	private final RateLimiter limiter;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RateLimitInterceptor(limiter, RateLimiter.Rule.LOGIN, HttpMethod.POST))
				.addPathPatterns("/auth/login");
		registry.addInterceptor(new RateLimitInterceptor(limiter, RateLimiter.Rule.CHECK_EMAIL, HttpMethod.GET))
				.addPathPatterns("/auth/check-email");
		registry.addInterceptor(new RateLimitInterceptor(limiter, RateLimiter.Rule.FORGOT_PASSWORD, HttpMethod.POST))
				.addPathPatterns("/auth/forgot-password");
	}
}
