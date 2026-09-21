package vn.enlearning.backend.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Giới hạn tần suất theo địa chỉ IP, đọc từ {@code app.rate-limit.*}. Mỗi luật cho phép tối đa
 * {@code capacity} request trong mỗi {@code window}.
 */
@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
		@DefaultValue("true") boolean enabled,
		@Valid @NotNull @DefaultValue Rule login,
		@Valid @NotNull @DefaultValue Rule checkEmail,
		@Valid @NotNull @DefaultValue Rule forgotPassword) {

	public record Rule(@Min(1) @DefaultValue("10") int capacity, @NotNull @DefaultValue("1m") Duration window) {
	}
}
