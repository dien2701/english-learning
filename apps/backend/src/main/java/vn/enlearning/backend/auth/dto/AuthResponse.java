package vn.enlearning.backend.auth.dto;

/**
 * Phần thân của phiên đăng nhập. Không có {@code refreshToken}: nó chỉ đi trong cookie HttpOnly
 * nên JavaScript không đọc được.
 */
public record AuthResponse(String token, UserResponse user) {
}
