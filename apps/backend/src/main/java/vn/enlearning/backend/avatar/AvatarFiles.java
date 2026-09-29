package vn.enlearning.backend.avatar;

import java.util.Locale;
import java.util.Map;

/** Nhận diện định dạng ảnh đại diện người dùng tải lên (theo Content-Type và byte đầu). */
public final class AvatarFiles {

	/** Kích thước tối đa của một ảnh tải lên. */
	public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024;

	private static final Map<String, String> EXTENSION_BY_TYPE = Map.of(
			"image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

	private AvatarFiles() {
	}

	/** {@code jpg}, {@code png} hoặc {@code webp} theo Content-Type; {@code null} nếu không được hỗ trợ. */
	public static String extensionFor(String contentType) {
		if (contentType == null) {
			return null;
		}
		int semicolon = contentType.indexOf(';');
		String base = (semicolon < 0 ? contentType : contentType.substring(0, semicolon)).trim().toLowerCase(Locale.ROOT);
		return EXTENSION_BY_TYPE.get(base);
	}

	/** Byte đầu tệp có khớp đuôi đã khai không (chặn giả Content-Type). */
	public static boolean matchesSignature(String extension, byte[] data) {
		return switch (extension) {
			case "jpg" -> data.length > 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8;
			case "png" -> data.length > 8 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G';
			case "webp" -> data.length > 12 && ascii(data, 0, "RIFF") && ascii(data, 8, "WEBP");
			default -> false;
		};
	}

	private static boolean ascii(byte[] d, int at, String text) {
		return at + text.length() <= d.length && new String(d, at, text.length(), java.nio.charset.StandardCharsets.ISO_8859_1).equals(text);
	}
}
