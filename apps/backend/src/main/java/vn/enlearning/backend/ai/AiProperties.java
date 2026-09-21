package vn.enlearning.backend.ai;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình AI đọc từ {@code app.ai.*}.
 *
 * @param geminiApiKey   để trống thì các cổng AI dùng bản giả ({@link GeminiKeyMissing})
 * @param geminiModel    tên model Gemini gọi qua {@code generateContent}
 * @param geminiBaseUrl  gốc REST của Gemini (đổi được để trỏ vào máy chủ giả khi test)
 * @param geminiTimeout  thời gian tối đa chờ một lời gọi Gemini (kết nối và đọc)
 * @param gradingTimeout bài đang GRADING quá thời gian này coi như AI không trả lời, chuyển NEEDS_RETRY
 * @param fakeLatency    độ trễ mô phỏng của bản giả, để thấy trạng thái GRADING khi thử tay
 */
@Validated
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
		@DefaultValue("") String geminiApiKey,
		@NotBlank @DefaultValue("gemini-2.5-flash") String geminiModel,
		@NotBlank @DefaultValue("https://generativelanguage.googleapis.com") String geminiBaseUrl,
		@NotNull @DefaultValue("45s") Duration geminiTimeout,
		@NotNull @DefaultValue("60s") Duration gradingTimeout,
		@NotNull @DefaultValue("1500ms") Duration fakeLatency) {
}
