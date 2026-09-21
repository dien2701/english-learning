package vn.enlearning.backend.auth.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Nơi phiên đăng nhập được tạo, lưu kèm refresh token để sau này người dùng nhận ra phiên lạ.
 * IP lấy từ kết nối trực tiếp; đứng sau reverse proxy thì phải cấu hình {@code server.forward-headers-strategy}.
 */
public record ClientInfo(String userAgent, String ipAddress) {

	private static final int MAX_USER_AGENT = 255;
	private static final int MAX_IP = 45;

	public static ClientInfo from(HttpServletRequest request) {
		return new ClientInfo(
				truncate(request.getHeader("User-Agent"), MAX_USER_AGENT),
				truncate(request.getRemoteAddr(), MAX_IP));
	}

	private static String truncate(String value, int max) {
		if (value == null) {
			return null;
		}
		return value.length() <= max ? value : value.substring(0, max);
	}
}
