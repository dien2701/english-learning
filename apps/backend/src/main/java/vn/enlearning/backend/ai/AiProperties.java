package vn.enlearning.backend.ai;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình AI đọc từ {@code app.ai.*}.
 *
 * @param openaiApiKey   để trống thì mọi cổng AI dùng bản giả ({@link OpenAiKeyMissing})
 * @param gradingTimeout bài đang GRADING quá thời gian này coi như AI không trả lời, chuyển NEEDS_RETRY
 * @param fakeLatency    độ trễ mô phỏng của bản giả, để thấy trạng thái GRADING khi thử tay
 */
@Validated
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
		@DefaultValue("") String openaiApiKey,
		@NotNull @DefaultValue("60s") Duration gradingTimeout,
		@NotNull @DefaultValue("1500ms") Duration fakeLatency) {
}
