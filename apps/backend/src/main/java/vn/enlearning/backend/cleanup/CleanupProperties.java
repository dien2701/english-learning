package vn.enlearning.backend.cleanup;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình dọn dẹp đọc từ {@code app.cleanup.*}.
 *
 * @param tokenRetention        giữ refresh token và mã OTP thêm bao lâu sau khi hết hạn
 * @param emptySessionRetention giữ phiên học 0 giây (chỉ một heartbeat rồi im) bao lâu
 * @param speakingInProgressRetention giữ lượt Luyện nói {@code IN_PROGRESS} bị bỏ dở (không hoạt động) bao lâu
 */
@Validated
@ConfigurationProperties(prefix = "app.cleanup")
public record CleanupProperties(
		@NotNull @DefaultValue("1d") Duration tokenRetention,
		@NotNull @DefaultValue("30d") Duration emptySessionRetention,
		@NotNull @DefaultValue("24h") Duration speakingInProgressRetention) {
}
