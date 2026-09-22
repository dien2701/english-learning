package vn.enlearning.backend.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/** Nhận diện định dạng file audio Admin tải lên (theo Content-Type và byte đầu) và đo thời lượng khi đọc được. */
public final class AudioFiles {

	/** Kích thước tối đa của một file tải lên. */
	public static final long MAX_UPLOAD_BYTES = 20L * 1024 * 1024;

	private static final Map<String, String> EXTENSION_BY_TYPE = Map.ofEntries(
			Map.entry("audio/mpeg", "mp3"), Map.entry("audio/mp3", "mp3"),
			Map.entry("audio/mp4", "m4a"), Map.entry("audio/x-m4a", "m4a"), Map.entry("audio/m4a", "m4a"),
			Map.entry("audio/wav", "wav"), Map.entry("audio/x-wav", "wav"), Map.entry("audio/wave", "wav"),
			Map.entry("audio/vnd.wave", "wav"));

	private AudioFiles() {
	}

	/** {@code mp3}, {@code m4a} hoặc {@code wav} theo Content-Type; {@code null} nếu không được hỗ trợ. */
	public static String extensionFor(String contentType) {
		if (contentType == null) {
			return null;
		}
		int semicolon = contentType.indexOf(';');
		String base = (semicolon < 0 ? contentType : contentType.substring(0, semicolon)).trim().toLowerCase(Locale.ROOT);
		return EXTENSION_BY_TYPE.get(base);
	}

	/**
	 * Kiểm tra nội dung khớp với đuôi đã khai và đo thời lượng.
	 *
	 * @return -1 nếu nội dung không phải định dạng đã khai; 0 nếu hợp lệ nhưng không đo được; ngược lại là số giây (làm tròn, tối thiểu 1)
	 */
	public static int probeSeconds(String extension, byte[] data) {
		return switch (extension) {
			case "mp3" -> mp3Seconds(data);
			case "wav" -> wavSeconds(data);
			case "m4a" -> data.length > 12 && ascii(data, 4, "ftyp") ? 0 : -1;
			default -> -1;
		};
	}

	private static int mp3Seconds(byte[] data) {
		try {
			return roundSeconds(Mp3.parse(data).seconds());
		} catch (IllegalArgumentException e) {
			return -1;
		}
	}

	/** Đọc byteRate ở khối "fmt " và kích thước khối "data" của tệp RIFF/WAVE. */
	private static int wavSeconds(byte[] d) {
		if (d.length < 12 || !ascii(d, 0, "RIFF") || !ascii(d, 8, "WAVE")) {
			return -1;
		}
		ByteBuffer buf = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN);
		long byteRate = 0;
		int pos = 12;
		while (pos + 8 <= d.length) {
			long size = buf.getInt(pos + 4) & 0xFFFFFFFFL;
			if (ascii(d, pos, "fmt ") && pos + 20 <= d.length) {
				byteRate = buf.getInt(pos + 16) & 0xFFFFFFFFL;
			} else if (ascii(d, pos, "data")) {
				long dataBytes = Math.min(size, d.length - (pos + 8L));
				return byteRate > 0 ? roundSeconds(dataBytes / (double) byteRate) : 0;
			}
			pos += 8 + (int) Math.min(size + (size & 1), d.length);
		}
		return 0;
	}

	private static int roundSeconds(double seconds) {
		return Math.max(1, (int) Math.round(seconds));
	}

	private static boolean ascii(byte[] d, int at, String text) {
		return at + text.length() <= d.length
				&& new String(d, at, text.length(), StandardCharsets.ISO_8859_1).equals(text);
	}
}
