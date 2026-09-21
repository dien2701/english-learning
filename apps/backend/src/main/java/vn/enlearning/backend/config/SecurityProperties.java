package vn.enlearning.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cấu hình bảo mật đọc từ {@code app.security.*}. Bí mật thiếu hoặc quá ngắn thì app không khởi động,
 * thay vì chạy với khoá yếu.
 *
 * @param jwtSecret        khoá ký JWT (HS256), tối thiểu 32 ký tự = 256 bit
 * @param resetCodeSecret  khoá HMAC băm mã OTP; phải khác {@code jwtSecret}
 * @param cookieSecure     true khi chạy sau HTTPS để cookie refresh token có cờ Secure
 */
@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
		@NotBlank @Size(min = 32) String jwtSecret,
		@NotBlank @DefaultValue("en-learning") String jwtIssuer,
		@NotNull @DefaultValue("15m") Duration accessTokenTtl,
		@NotNull @DefaultValue("7d") Duration refreshTokenTtl,
		/** Thu hồi trong khoảng này vẫn coi là hai tab refresh cùng lúc, không phải bị đánh cắp token. */
		@NotNull @DefaultValue("10s") Duration refreshReuseGrace,
		boolean cookieSecure,
		@NotBlank @Size(min = 32) String resetCodeSecret,
		@NotNull @DefaultValue("10m") Duration resetCodeTtl,
		@Min(1) @Max(10) @DefaultValue("5") int resetCodeMaxAttempts,
		@NotNull @DefaultValue("60s") Duration resetCodeResendCooldown) {
}
