package vn.enlearning.backend.audio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Lưu audio vào {@code app.audio.local-dir} khi chưa có {@code CLOUDINARY_URL}. Thư mục này được phục vụ
 * công khai (chỉ đọc) ở {@code /media/audio/**} bởi {@link LocalAudioWebConfig}.
 */
@Slf4j
@Component
@ConditionalOnExpression("'${app.audio.cloudinary-url:}'.isBlank()")
class LocalAudioStorage implements AudioStorage {

	static final String URL_PREFIX = "/media/audio/";
	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]+");
	private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9_-]+\\.(mp3|m4a|wav)");
	private static final Set<String> EXTENSIONS = Set.of("mp3", "m4a", "wav");

	private final Path dir;
	private final String contextPath;

	LocalAudioStorage(AudioProperties properties, @Value("${server.servlet.context-path:}") String contextPath) {
		this.dir = Path.of(properties.localDir()).toAbsolutePath().normalize();
		this.contextPath = contextPath;
	}

	Path dir() {
		return dir;
	}

	@Override
	public StoredAudio store(byte[] data, String name, String extension) {
		if (!NAME.matcher(name).matches() || !EXTENSIONS.contains(extension)) {
			throw new AudioStorageException("Tên hoặc đuôi file audio không hợp lệ");
		}
		String fileName = name + "." + extension;
		try {
			Files.createDirectories(dir);
			Files.write(dir.resolve(fileName), data);
		} catch (IOException e) {
			throw new AudioStorageException("Không ghi được file audio cục bộ", e);
		}
		return new StoredAudio(contextPath + URL_PREFIX + fileName, fileName);
	}

	@Override
	public void delete(String publicId) {
		// publicId lấy từ DB nhưng vẫn kiểm tra để không bao giờ xoá ngoài thư mục audio.
		if (publicId == null || !FILE_NAME.matcher(publicId).matches()) {
			return;
		}
		try {
			Files.deleteIfExists(dir.resolve(publicId));
		} catch (IOException e) {
			log.warn("Không xoá được file audio cục bộ {}: {}", publicId, e.getClass().getSimpleName());
		}
	}
}
