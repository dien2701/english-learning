package vn.enlearning.backend.audio;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình audio bài nghe đọc từ {@code app.audio.*}.
 *
 * @param openaiApiKey      để trống thì dùng bản giả ({@link FakeSpeechSynthesizer}), không gọi ra ngoài
 * @param openaiTtsModel    model TTS gọi qua {@code /v1/audio/speech}
 * @param openaiBaseUrl     gốc REST của OpenAI (đổi được để trỏ vào máy chủ giả khi test)
 * @param openaiTimeout     thời gian tối đa chờ một lời gọi TTS (kết nối và đọc)
 * @param cloudinaryUrl     {@code cloudinary://<api_key>:<api_secret>@<cloud_name>}; để trống thì lưu cục bộ
 * @param cloudinaryBaseUrl gốc REST của Cloudinary (đổi được khi test)
 * @param cloudinaryTimeout thời gian tối đa cho một lần tải lên/xoá ở Cloudinary
 * @param localDir          thư mục lưu audio khi chưa có Cloudinary, phục vụ qua {@code /media/audio/**}
 * @param generateSeed      {@code true} thì lúc khởi động sinh audio cho các bài nghe chưa có (đợt 12c)
 */
@Validated
@ConfigurationProperties(prefix = "app.audio")
public record AudioProperties(
		@DefaultValue("") String openaiApiKey,
		@NotBlank @DefaultValue("gpt-4o-mini-tts") String openaiTtsModel,
		@NotBlank @DefaultValue("https://api.openai.com") String openaiBaseUrl,
		@NotNull @DefaultValue("90s") Duration openaiTimeout,
		@DefaultValue("") String cloudinaryUrl,
		@NotBlank @DefaultValue("https://api.cloudinary.com") String cloudinaryBaseUrl,
		@NotNull @DefaultValue("60s") Duration cloudinaryTimeout,
		@NotBlank @DefaultValue("uploads/audio") String localDir,
		@DefaultValue("false") boolean generateSeed) {
}
