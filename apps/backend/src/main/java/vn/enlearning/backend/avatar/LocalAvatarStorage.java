package vn.enlearning.backend.avatar;

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
 * Lưu ảnh đại diện vào {@code app.avatar.local-dir} khi chưa có {@code CLOUDINARY_URL}. Thư mục này được phục vụ
 * công khai (chỉ đọc) ở {@code /media/avatars/**} bởi {@link LocalAvatarWebConfig}.
 */
@Slf4j
@Component
@ConditionalOnExpression("'${app.avatar.cloudinary-url:}'.isBlank()")
class LocalAvatarStorage implements AvatarStorage {

	static final String URL_PREFIX = "/media/avatars/";
	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]+");
	private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9_-]+\\.(jpg|png|webp)");
	private static final Set<String> EXTENSIONS = Set.of("jpg", "png", "webp");

	private final Path dir;
	private final String contextPath;

	LocalAvatarStorage(AvatarProperties properties, @Value("${server.servlet.context-path:}") String contextPath) {
		this.dir = Path.of(properties.localDir()).toAbsolutePath().normalize();
		this.contextPath = contextPath;
	}

	Path dir() {
		return dir;
	}

	@Override
	public StoredAvatar store(byte[] data, String name, String extension) {
		if (!NAME.matcher(name).matches() || !EXTENSIONS.contains(extension)) {
			throw new AvatarStorageException("Tên hoặc đuôi file ảnh không hợp lệ");
		}
		String fileName = name + "." + extension;
		try {
			Files.createDirectories(dir);
			Files.write(dir.resolve(fileName), data);
		} catch (IOException e) {
			throw new AvatarStorageException("Không ghi được file ảnh cục bộ", e);
		}
		return new StoredAvatar(contextPath + URL_PREFIX + fileName, fileName);
	}

	@Override
	public void delete(String publicId) {
		// publicId lấy từ DB nhưng vẫn kiểm tra để không bao giờ xoá ngoài thư mục avatar.
		if (publicId == null || !FILE_NAME.matcher(publicId).matches()) {
			return;
		}
		try {
			Files.deleteIfExists(dir.resolve(publicId));
		} catch (IOException e) {
			log.warn("Không xoá được file ảnh cục bộ {}: {}", publicId, e.getClass().getSimpleName());
		}
	}
}
