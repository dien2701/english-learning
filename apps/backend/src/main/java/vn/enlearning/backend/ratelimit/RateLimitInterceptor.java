package vn.enlearning.backend.ratelimit;

import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Trừ một lượt của địa chỉ IP trước khi controller chạy. Ném ngoại lệ từ đây vẫn đi qua
 * {@code GlobalExceptionHandler}, nên phản hồi 429 cùng vỏ với mọi lỗi khác. IP lấy từ kết nối trực tiếp
 * (như {@code ClientInfo}); đứng sau reverse proxy thì cấu hình {@code server.forward-headers-strategy}.
 */
class RateLimitInterceptor implements HandlerInterceptor {

	private final RateLimiter limiter;
	private final RateLimiter.Rule rule;
	private final HttpMethod method;

	RateLimitInterceptor(RateLimiter limiter, RateLimiter.Rule rule, HttpMethod method) {
		this.limiter = limiter;
		this.rule = rule;
		this.method = method;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (method.matches(request.getMethod())) {
			limiter.consume(rule, request.getRemoteAddr());
		}
		return true;
	}
}
