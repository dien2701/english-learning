package vn.enlearning.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import vn.enlearning.backend.config.SecurityProperties;

/**
 * Cookie chứa refresh token. HttpOnly để JavaScript (và mọi lỗ hổng XSS) không đọc được; {@code Path} chỉ
 * trỏ vào {@code /auth} nên trình duyệt không gửi nó kèm các request khác.
 */
@Component
public class RefreshCookieFactory {

	public static final String COOKIE_NAME = "refresh_token";

	private final SecurityProperties properties;
	private final String path;

	public RefreshCookieFactory(SecurityProperties properties,
			@Value("${server.servlet.context-path:}") String contextPath) {
		this.properties = properties;
		this.path = contextPath + "/auth";
	}

	public ResponseCookie issue(String rawToken) {
		return base(rawToken).maxAge(properties.refreshTokenTtl()).build();
	}

	public ResponseCookie clear() {
		return base("").maxAge(0).build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String value) {
		return ResponseCookie.from(COOKIE_NAME, value)
				.httpOnly(true)
				.secure(properties.cookieSecure())
				// Lax vẫn gửi cookie giữa hai cổng của localhost (cùng site) mà chặn được POST chéo site.
				.sameSite("Lax")
				.path(path);
	}
}
