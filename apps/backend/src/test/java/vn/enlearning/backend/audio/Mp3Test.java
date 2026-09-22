package vn.enlearning.backend.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Mp3Test {

	/** MPEG-1 Layer III, 128 kbps, 44,1 kHz, stereo: khung dài 417 byte, 1152 mẫu (~26,12 ms). */
	private static byte[] frame441() {
		byte[] f = new byte[417];
		f[0] = (byte) 0xFF;
		f[1] = (byte) 0xFB;
		f[2] = (byte) 0x90;
		return f;
	}

	/** MPEG-2 Layer III, 64 kbps, 24 kHz, mono (giống đầu ra OpenAI): 72*64000/24000 = 192 byte, 576 mẫu (24 ms). */
	private static byte[] frame24kMono() {
		byte[] f = new byte[192];
		f[0] = (byte) 0xFF;
		f[1] = (byte) 0xF3;
		f[2] = (byte) 0x84; // bitrate index 8 (64k), sample rate index 1 (24k)
		f[3] = (byte) 0xC0; // mono
		return f;
	}

	private static byte[] repeat(byte[] frame, int times) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < times; i++) {
			out.writeBytes(frame);
		}
		return out.toByteArray();
	}

	private static byte[] concat(byte[]... parts) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (byte[] p : parts) {
			out.writeBytes(p);
		}
		return out.toByteArray();
	}

	@Test
	@DisplayName("Đọc khung, đo thời lượng: 100 khung 44,1 kHz ≈ 2,612 giây")
	void measuresDuration() {
		Mp3.Audio audio = Mp3.parse(repeat(frame441(), 100));
		assertThat(audio.frames()).hasSize(100 * 417);
		assertThat(audio.seconds()).isEqualTo(100 * 1152 / 44100.0, org.assertj.core.data.Offset.offset(1e-9));
		assertThat(audio.sampleRate()).isEqualTo(44100);
		assertThat(audio.mono()).isFalse();
	}

	@Test
	@DisplayName("Bỏ thẻ ID3v2 đầu, ID3v1 cuối và khung Xing/Info")
	void stripsTagsAndInfoFrame() {
		byte[] id3v2 = new byte[10 + 20];
		id3v2[0] = 'I';
		id3v2[1] = 'D';
		id3v2[2] = '3';
		id3v2[9] = 20; // kích thước syncsafe = 20
		byte[] info = frame441();
		System.arraycopy("Info".getBytes(StandardCharsets.ISO_8859_1), 0, info, 4 + 32, 4);
		byte[] id3v1 = new byte[128];
		id3v1[0] = 'T';
		id3v1[1] = 'A';
		id3v1[2] = 'G';

		Mp3.Audio audio = Mp3.parse(concat(id3v2, info, repeat(frame441(), 10), id3v1));

		assertThat(audio.frames()).hasSize(10 * 417);
	}

	@Test
	@DisplayName("Bỏ qua rác giữa chừng và khung cụt ở cuối")
	void resyncsOverJunk() {
		byte[] junk = { 1, 2, 3, 4, 5, 6, 7 };
		byte[] truncated = new byte[100];
		System.arraycopy(frame441(), 0, truncated, 0, 100);

		Mp3.Audio audio = Mp3.parse(concat(junk, repeat(frame441(), 5), junk, repeat(frame441(), 5), truncated));

		assertThat(audio.frames()).hasSize(10 * 417);
	}

	@Test
	@DisplayName("Ghép nhiều đoạn cùng định dạng: cộng thời lượng, nối liền khung")
	void joinsParts() {
		Mp3.Audio joined = Mp3.join(List.of(repeat(frame24kMono(), 50), repeat(frame24kMono(), 25)));

		assertThat(joined.frames()).hasSize(75 * 192);
		assertThat(joined.seconds()).isEqualTo(75 * 0.024, org.assertj.core.data.Offset.offset(1e-9));
		assertThat(joined.sampleRate()).isEqualTo(24000);
		assertThat(joined.mono()).isTrue();
	}

	@Test
	@DisplayName("Từ chối đoạn không phải MP3 và các đoạn khác tần số/kênh")
	void rejectsBadParts() {
		assertThatThrownBy(() -> Mp3.parse(new byte[] { 'h', 'i' })).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Mp3.parse("not an mp3 at all, just text".getBytes(StandardCharsets.UTF_8)))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Mp3.join(List.of(repeat(frame441(), 3), repeat(frame24kMono(), 3))))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Mp3.join(List.of())).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("MP3 im lặng của bản giả hợp lệ, dài khoảng 1 giây")
	void silenceResourceIsValid() throws Exception {
		byte[] silence = new FakeSpeechSynthesizer().synthesize("x", "alloy");
		Mp3.Audio audio = Mp3.parse(silence);
		assertThat(audio.seconds()).isBetween(0.9, 1.2);
		assertThat(Mp3.join(List.of(silence, silence)).seconds()).isEqualTo(audio.seconds() * 2, org.assertj.core.data.Offset.offset(1e-9));
	}
}
