package vn.enlearning.backend.audio;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Phục vụ file audio cục bộ ở {@code GET /media/audio/**} (công khai, chỉ đọc, hỗ trợ Range để tua).
 * Mỗi lần sinh/tải lên tạo tên file mới nên cho phép đệm lâu.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.audio.cloudinary-url:}'.isBlank()")
class LocalAudioWebConfig implements WebMvcConfigurer {

	private final LocalAudioStorage storage;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		try {
			// Vị trí thư mục phải có sẵn để URI kết thúc bằng "/".
			Files.createDirectories(storage.dir());
		} catch (IOException e) {
			throw new IllegalStateException("Không tạo được thư mục audio cục bộ", e);
		}
		registry.addResourceHandler(LocalAudioStorage.URL_PREFIX + "**")
				.addResourceLocations(storage.dir().toUri().toString())
				.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
	}
}
