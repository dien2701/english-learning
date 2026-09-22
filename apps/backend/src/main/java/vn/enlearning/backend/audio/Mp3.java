package vn.enlearning.backend.audio;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Đọc khung MP3 (Layer III, MPEG-1/2/2.5) để ghép nhiều đoạn TTS thành một file và đo thời lượng, không cần ffmpeg.
 * Bỏ thẻ ID3v2 ở đầu, ID3v1 ở cuối và khung Xing/Info/VBRI (khung mô tả, không phải âm thanh) của từng đoạn;
 * phần còn lại là chuỗi khung liên tiếp nên nối thẳng được nếu cùng tần số lấy mẫu và kênh.
 */
public final class Mp3 {

	private static final int[][] BITRATES_KBPS = {
			{ 0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320 },
			{ 0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 } };
	private static final int[][] SAMPLE_RATES = { { 44100, 48000, 32000 }, { 22050, 24000, 16000 }, { 11025, 12000, 8000 } };
	private static final int ID3V1_LENGTH = 128;

	private Mp3() {
	}

	/**
	 * @param frames     chỉ các khung âm thanh, nối liền nhau
	 * @param seconds    tổng thời lượng
	 * @param sampleRate tần số lấy mẫu của khung đầu tiên
	 * @param mono       kênh đơn hay không (khung đầu tiên)
	 */
	public record Audio(byte[] frames, double seconds, int sampleRate, boolean mono) {
	}

	private record Header(int length, int samples, int sampleRate, boolean mono, int infoOffset) {
	}

	/** @throws IllegalArgumentException khi không có khung MP3 hợp lệ nào */
	public static Audio parse(byte[] data) {
		int pos = skipId3v2(data);
		int end = data.length;
		if (end - ID3V1_LENGTH >= pos && data[end - ID3V1_LENGTH] == 'T' && data[end - ID3V1_LENGTH + 1] == 'A'
				&& data[end - ID3V1_LENGTH + 2] == 'G') {
			end -= ID3V1_LENGTH;
		}
		ByteArrayOutputStream frames = new ByteArrayOutputStream(Math.max(0, end - pos));
		double seconds = 0;
		int count = 0;
		Header firstHeader = null;
		boolean synced = false;
		boolean first = true;
		while (pos + 4 <= end) {
			Header h = header(data, pos, end);
			// Khi đang dò lại điểm khớp, đòi khung kế tiếp cũng hợp lệ để không nhận nhầm byte ngẫu nhiên.
			if (h == null || pos + h.length() > end || (!synced && !confirmed(data, pos + h.length(), end))) {
				synced = false;
				pos++;
				continue;
			}
			synced = true;
			if (first && isInfoFrame(data, pos, h)) {
				first = false;
				pos += h.length();
				continue;
			}
			first = false;
			frames.write(data, pos, h.length());
			seconds += h.samples() / (double) h.sampleRate();
			if (firstHeader == null) {
				firstHeader = h;
			}
			count++;
			pos += h.length();
		}
		if (count == 0) {
			throw new IllegalArgumentException("Không có khung MP3 hợp lệ");
		}
		return new Audio(frames.toByteArray(), seconds, firstHeader.sampleRate(), firstHeader.mono());
	}

	/**
	 * Nối các đoạn MP3 thành một. Các đoạn phải cùng tần số lấy mẫu và số kênh.
	 *
	 * @throws IllegalArgumentException khi rỗng, có đoạn hỏng hoặc các đoạn khác định dạng
	 */
	public static Audio join(List<byte[]> parts) {
		if (parts.isEmpty()) {
			throw new IllegalArgumentException("Không có đoạn nào để ghép");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		double seconds = 0;
		Audio first = null;
		for (byte[] part : parts) {
			Audio audio = parse(part);
			if (first == null) {
				first = audio;
			} else if (audio.sampleRate() != first.sampleRate() || audio.mono() != first.mono()) {
				throw new IllegalArgumentException("Các đoạn MP3 khác tần số lấy mẫu hoặc số kênh");
			}
			out.writeBytes(audio.frames());
			seconds += audio.seconds();
		}
		return new Audio(out.toByteArray(), seconds, first.sampleRate(), first.mono());
	}

	private static boolean confirmed(byte[] data, int pos, int end) {
		if (pos + 4 > end) {
			return pos == end;
		}
		return header(data, pos, end) != null;
	}

	private static int skipId3v2(byte[] d) {
		if (d.length >= 10 && d[0] == 'I' && d[1] == 'D' && d[2] == '3') {
			int size = ((d[6] & 0x7F) << 21) | ((d[7] & 0x7F) << 14) | ((d[8] & 0x7F) << 7) | (d[9] & 0x7F);
			int footer = (d[5] & 0x10) != 0 ? 10 : 0;
			return Math.min(d.length, 10 + size + footer);
		}
		return 0;
	}

	private static Header header(byte[] d, int pos, int end) {
		if (pos + 4 > end || (d[pos] & 0xFF) != 0xFF || (d[pos + 1] & 0xE0) != 0xE0) {
			return null;
		}
		int version = (d[pos + 1] >> 3) & 3; // 3 = MPEG-1, 2 = MPEG-2, 0 = MPEG-2.5, 1 = dành riêng
		int layer = (d[pos + 1] >> 1) & 3; // 1 = Layer III
		int bitrateIndex = (d[pos + 2] >> 4) & 0xF;
		int sampleRateIndex = (d[pos + 2] >> 2) & 3;
		if (version == 1 || layer != 1 || bitrateIndex == 0 || bitrateIndex == 15 || sampleRateIndex == 3) {
			return null;
		}
		boolean mpeg1 = version == 3;
		int bitrate = BITRATES_KBPS[mpeg1 ? 0 : 1][bitrateIndex] * 1000;
		int sampleRate = SAMPLE_RATES[version == 3 ? 0 : version == 2 ? 1 : 2][sampleRateIndex];
		int padding = (d[pos + 2] >> 1) & 1;
		int length = (mpeg1 ? 144 : 72) * bitrate / sampleRate + padding;
		boolean mono = ((d[pos + 3] >> 6) & 3) == 3;
		boolean crc = (d[pos + 1] & 1) == 0;
		int sideInfo = mpeg1 ? (mono ? 17 : 32) : (mono ? 9 : 17);
		return new Header(length, mpeg1 ? 1152 : 576, sampleRate, mono, 4 + (crc ? 2 : 0) + sideInfo);
	}

	/** Khung đầu chứa "Xing"/"Info" (hoặc "VBRI" ở offset 36) là khung mô tả VBR, không có âm thanh. */
	private static boolean isInfoFrame(byte[] d, int pos, Header h) {
		return tagAt(d, pos + h.infoOffset(), "Xing") || tagAt(d, pos + h.infoOffset(), "Info") || tagAt(d, pos + 36, "VBRI");
	}

	private static boolean tagAt(byte[] d, int at, String tag) {
		if (at + 4 > d.length) {
			return false;
		}
		return new String(d, at, 4, StandardCharsets.ISO_8859_1).equals(tag);
	}
}
